package net.manywiki.jee.actions.pub;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.*;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.exceptions.RedirectException;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.util.HttpUtil;
import org.apache.wiki.filters.SpamFilter;
import org.apache.wiki.htmltowiki.HtmlStringToWikiTranslator;
import org.apache.wiki.pages.PageLock;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.ui.EditorManager;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.util.TextUtil;
import org.apache.wiki.workflow.DecisionRequiredException;
import org.jdom2.JDOMException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.manywiki.jee.TemporaryManyWikiRoot;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class Edit_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	String findParam( String key ) {
		return getRequest().getParameter( key );  //TODO-PP we don't need pageContext.findAttribute(..), right??
	}
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		HttpSession session = request.getSession();
		
		
		// Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.PAGE_EDIT;
		Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext() );
		if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) {
			return;
		}
		if( wikiContext.getCommand().getTarget() == null ) {
			response.sendRedirect( wikiContext.getURL( wikiContext.getRequestContext(), wikiContext.getName() ) );
			return;
		}
		String pagereq = wikiContext.getName();
		
		Session wikiSession = wikiContext.getWikiSession();
		String user = wikiSession.getUserPrincipal().getName();
		String action  = request.getParameter("action");
		String ok      = request.getParameter("ok");
		String preview = request.getParameter("preview");
		String cancel  = request.getParameter("cancel");
		String append  = request.getParameter("append");
		String edit    = request.getParameter("edit");
		String author  = TextUtil.replaceEntities( findParam( "author" ) );
		String changenote = findParam( "changenote" );
		String text    = EditorManager.getEditedText( request );
		String link    = TextUtil.replaceEntities( findParam( "link") );
		String captcha = (String)session.getAttribute("captcha");
		
		if ( !wikiSession.isAuthenticated() && wikiSession.isAnonymous() && author != null ) {
			user  = TextUtil.replaceEntities( findParam( "author" ) );
		}
		
		//
		//  WYSIWYG editor sends us its greetings
		//
		String htmlText = findParam( "htmlPageText" );
		if( htmlText != null && cancel == null ) {
			try
			{
				text = new HtmlStringToWikiTranslator( engine ).translate( htmlText, wikiContext );
			}
			catch (JDOMException exc)
			{
				throw new ServletException(exc);
			}
			catch (ReflectiveOperationException exc)
			{
				throw new ServletException(exc);
			}
		}
		
		Page wikipage = wikiContext.getPage();
		Page latestversion = engine.getManager( PageManager.class ).getPage( pagereq );
		
		if( latestversion == null ) {
			latestversion = wikiContext.getPage();
		}
		
		//
		//  Set the response type before we branch.
		//
		response.setContentType("text/html; charset="+engine.getContentEncoding() );
		response.setHeader( "Cache-control", "max-age=0" );
		response.setDateHeader( "Expires", new Date().getTime() );
		response.setDateHeader( "Last-Modified", new Date().getTime() );
		
		//log.debug("Request character encoding="+request.getCharacterEncoding());
		//log.debug("Request content type+"+request.getContentType());
		log.debug("preview="+preview+", ok="+ok);
		
		SpamFilter spamFilter = TemporaryManyWikiRoot.getSpamFilter();
		
		String spamhash = spamFilter == null ? null : getRequest().getParameter( spamFilter.getHashFieldName(request) );  //Todo-PP we don't need to use pageContext.findAttribute() anymore here because this is a toplevel page serving bean and nothing is communicating to it with hidden variables, right?!
		
		if( ok != null || captcha != null ) {
			log.info("Saving page "+pagereq+". User="+user+", host="+HttpUtil.getRemoteAddress(request) );
			
			if (spamFilter != null)
			{
				//
				//  Check for session expiry
				//
				if( !spamFilter.checkHash(wikiContext, request) ) {
					return;
				}
				
				String h = spamFilter.getSpamHash( latestversion, request );
				
				if( !h.equals(spamhash) ) {
					//
					// Someone changed the page while we were editing it!
					//
					log.info("Page changed, warning user.");
					
					session.setAttribute( EditorManager.REQ_EDITEDTEXT, EditorManager.getEditedText(request) );
					response.sendRedirect( engine.getURL( ContextEnum.PAGE_CONFLICT.getRequestContext(), pagereq, null ) );
					return;
				}
			}
			
			Page modifiedPage = (Page)wikiContext.getPage().clone();
			
			//
			//  We expire ALL locks at this moment, simply because someone has already broken it.
			//
			PageLock lock = engine.getManager( PageManager.class ).getCurrentLock( wikipage );
			engine.getManager( PageManager.class ).unlockPage( lock );
			session.removeAttribute( "lock-"+pagereq );
			
			//
			//  Set author information and other metadata
			//
			modifiedPage.setAuthor( user );
			
			if( changenote == null ) {
				changenote = (String) session.getAttribute("changenote");
			}
			
			session.removeAttribute("changenote");
			
			if( changenote != null && changenote.length() > 0 ) {
				modifiedPage.setAttribute( Page.CHANGENOTE, changenote );
			} else {
				modifiedPage.removeAttribute( Page.CHANGENOTE );
			}
			
			//
			//  Figure out the actual page text
			//
			if( text == null ) {
				throw new ServletException( "No parameter text set!" );
			}
			
			//
			//  If this is an append, then we just append it to the page.
			//  If it is a full edit, then we will replace the previous contents.
			//
			try {
				wikiContext.setPage( modifiedPage );
				
				if( captcha != null ) {
					wikiContext.setVariable( "captcha", Boolean.TRUE );
					session.removeAttribute( "captcha" );
				}
				
				if( append != null ) {
					StringBuffer pageText = new StringBuffer(engine.getManager( PageManager.class ).getText( pagereq ));
					pageText.append( text );
					engine.getManager( PageManager.class ).saveText( wikiContext, pageText.toString() );
				} else {
					engine.getManager( PageManager.class ).saveText( wikiContext, text );
				}
			} catch( DecisionRequiredException ex ) {
				String redirect = wikiContext.getURL(ContextEnum.PAGE_VIEW.getRequestContext(),"ApprovalRequiredForPageChanges");
				response.sendRedirect( redirect );
				return;
			} catch( RedirectException ex ) {
				// FIXME: Cut-n-paste code.
				wikiContext.getWikiSession().addMessage( ex.getMessage() ); // FIXME: should work, but doesn't
				session.setAttribute( "message", ex.getMessage() );
				session.setAttribute(EditorManager.REQ_EDITEDTEXT, EditorManager.getEditedText(request));
				session.setAttribute("author",user);
				session.setAttribute("link",link != null ? link : "" );
				if( htmlText != null ) session.setAttribute( EditorManager.REQ_EDITEDTEXT, text );
				
				session.setAttribute("changenote", changenote != null ? changenote : "" );
				
				if (spamFilter != null)
					session.setAttribute(spamFilter.getHashFieldName(request), spamhash);
				
				response.sendRedirect( ex.getRedirect() );
				return;
			}
			
			response.sendRedirect(wikiContext.getViewURL(pagereq));
			return;
		} else if( preview != null ) {
			log.debug("Previewing "+pagereq);
			session.setAttribute(EditorManager.REQ_EDITEDTEXT, EditorManager.getEditedText(request));
			session.setAttribute("author",user);
			session.setAttribute("link",link != null ? link : "" );
			
			if( htmlText != null ) {
				session.setAttribute( EditorManager.REQ_EDITEDTEXT, text );
			}
			
			session.setAttribute("changenote", changenote != null ? changenote : "" );
			response.sendRedirect( engine.getURL( ContextEnum.PAGE_PREVIEW.getRequestContext(), pagereq, null ) );
			return;
		} else if( cancel != null ) {
			log.debug("Cancelled editing "+pagereq);
			PageLock lock = (PageLock) session.getAttribute( "lock-"+pagereq );
			if( lock != null ) {
				engine.getManager( PageManager.class ).unlockPage( lock );
				session.removeAttribute( "lock-"+pagereq );
			}
			response.sendRedirect( wikiContext.getViewURL(pagereq) );
			return;
		}
		
		session.removeAttribute( EditorManager.REQ_EDITEDTEXT );
		
		log.info("Editing page "+pagereq+". User="+user+", host="+HttpUtil.getRemoteAddress(request) );
		
		
		if (spamFilter != null)
		{
			//
			//  Determine and store the date the latest version was changed.  Since
			//  the newest version is the one that is changed, we need to track
			//  that instead of the edited version.
			//
			String lastchange = spamFilter.getSpamHash( latestversion, request );
			getRequest().setAttribute( SpamFilter.HorribleHiddenVariableInHttpServletRequest, lastchange );
		}
		
		//
		//  Attempt to lock the page.
		//
		PageLock lock = engine.getManager( PageManager.class ).lockPage( wikipage, user );
		if( lock != null ) {
			session.setAttribute( "lock-"+pagereq, lock );
		}
		
		
		//String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "EditTemplate.jsp" );
		//%><wiki:Include page="<%=contentPage%>" />		
	    setVariableForJSPView("contentSelector", cte.getContentSelector());
		serveJSPView("/templates/default/EditTemplate.jsp");
	}
}
