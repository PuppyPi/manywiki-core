package net.manywiki.jee.actions.pub;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.*;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import org.apache.commons.lang3.StringUtils;
import org.apache.wiki.*;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.api.exceptions.RedirectException;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.login.CookieAssertionLoginModule;
import org.apache.wiki.filters.SpamFilter;
import org.apache.wiki.htmltowiki.HtmlStringToWikiTranslator;
import org.apache.wiki.pages.PageLock;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.preferences.Preferences.TimeFormat;
import org.apache.wiki.ui.EditorManager;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.util.HttpUtil;
import org.apache.wiki.util.TextUtil;
import org.apache.wiki.variables.VariableManager;
import org.apache.wiki.workflow.DecisionRequiredException;
import org.jdom2.JDOMException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.fmt.*;
import java.io.IOException;
import javax.servlet.ServletException;
import net.manywiki.jee.TemporaryManyWikiRoot;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class Comment_jsp
extends ManyWikiActionBean
{
    protected static final Logger log = LogManager.getLogger("JSPWiki");
    
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		HttpSession session = request.getSession();
		
	    // Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.PAGE_COMMENT;
	    Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext() );
	    if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
	    if( wikiContext.getCommand().getTarget() == null ) {
	        response.sendRedirect( wikiContext.getURL( wikiContext.getRequestContext(), wikiContext.getName() ) );
	        return;
	    }
	    String pagereq = wikiContext.getName();

	    ResourceBundle rb = Preferences.getBundle( wikiContext, "org.apache.wiki.i18n.core.CoreResources" );
	    Session wikiSession = wikiContext.getWikiSession();
	    String storedUser = wikiSession.getUserPrincipal().getName();
	    String commentedBy = storedUser;

	    if( wikiSession.isAnonymous() ) {
	        storedUser  = TextUtil.replaceEntities( request.getParameter( "author" ) );
	        commentedBy = rb.getString( "varmgr.anonymous" );
	    }
	    String commentDate = Preferences.renderDate( wikiContext, Calendar.getInstance().getTime(), TimeFormat.DATETIME );

	    String ok       = request.getParameter( "ok" );
	    String preview  = request.getParameter( "preview" );
	    String cancel   = request.getParameter( "cancel" );
	    String author   = TextUtil.replaceEntities( request.getParameter( "author" ) );
	    String link     = TextUtil.replaceEntities( request.getParameter( "link" ) );
	    String remember = TextUtil.replaceEntities( request.getParameter( "remember" ) );
	    String changenote = MessageFormat.format( rb.getString( "comment.changenote" ), commentDate, commentedBy );

	    Page wikipage = wikiContext.getPage();
	    Page latestversion = engine.getManager( PageManager.class ).getPage( pagereq );

	    session.removeAttribute( EditorManager.REQ_EDITEDTEXT );

	    if( latestversion == null ) {
	        latestversion = wikiContext.getPage();
	    }

	    //
	    //  Setup everything for the editors and possible preview.  We store everything in the session.
	    //

	    if( remember == null ) {
	        remember = (String)session.getAttribute("remember");
	    }

	    if( remember == null ) {
	        remember = "false";
	    } else {
	        remember = "true";
	    }

	    session.setAttribute("remember",remember);

	    if( author == null ) {
	        author = storedUser;
	    }
	    if( author == null || author.length() == 0 ) {
	        author = StringUtils.capitalize( rb.getString( "varmgr.anonymous" ) );
	    }

	    session.setAttribute("author",author);

	    if( link == null ) {
	        link = HttpUtil.retrieveCookieValue( request, "link" );
	        if( link == null ) link = "";
	        link = TextUtil.urlDecodeUTF8(link);
	    }

	    session.setAttribute( "link", link );

	    if( changenote != null ) {
	       session.setAttribute( "changenote", changenote );
	    }

	    //
	    //  Branch
	    //
	    log.debug("preview="+preview+", ok="+ok);

	    if( ok != null ) {
	        log.info("Saving page "+pagereq+". User="+storedUser+", host="+HttpUtil.getRemoteAddress(request) );

	        //  Modifications are written here before actual saving

	        Page modifiedPage = (Page)wikiContext.getPage().clone();

	        SpamFilter spamFilter = TemporaryManyWikiRoot.getSpamFilter();
	        
	        if (spamFilter != null)
	        {
		        String spamhash = request.getParameter( spamFilter.getHashFieldName(request) );
	
		        if(! spamFilter.checkHash(wikiContext,request) ) {
		        	response.sendRedirect(wikiContext.getURL(ContextEnum.PAGE_VIEW.getRequestContext(), "SessionExpired"));
		            return;
		        }
	        }

	        //
	        //  We expire ALL locks at this moment, simply because someone has already broken it.
	        //
	        PageLock lock = engine.getManager( PageManager.class ).getCurrentLock( wikipage );
	        engine.getManager( PageManager.class ).unlockPage( lock );
	        session.removeAttribute( "lock-"+pagereq );

	        //
	        //  Set author and changenote information
	        //
	        modifiedPage.setAuthor( storedUser );

	        if( changenote != null ) {
	            modifiedPage.setAttribute( Page.CHANGENOTE, changenote );
	        } else {
	            modifiedPage.removeAttribute( Page.CHANGENOTE );
	        }

	        //
	        //  Build comment part
	        //
	        StringBuffer pageText = new StringBuffer( engine.getManager( PageManager.class ).getPureText( wikipage ));

	        log.debug("Page initial contents are "+pageText.length()+" chars");

	        //
	        //  Add a line on top only if we need to separate it from the content.
	        //
	        if( pageText.length() > 0 ) {
	            pageText.append( "\n\n----\n\n" );
	        }

	        String commentText = request.getParameter( EditorManager.REQ_EDITEDTEXT );  //TODO-PP we don't need pageContext.findAttribute(..), right??
	        //log.info("comment text"+commentText);

	        //
	        //  WYSIWYG editor sends us its greetings
	        //
	        String htmlText = request.getParameter( "htmlPageText" );  //TODO-PP we don't need pageContext.findAttribute(..), right??
	        if( htmlText != null && cancel == null ) {
	        	try
				{
					commentText = new HtmlStringToWikiTranslator( engine ).translate(htmlText,wikiContext);
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

	        pageText.append( commentText );

	        log.debug("Author name ="+author);
	        if( author != null && author.length() > 0 ) {
	            String signature = author;
	            if( link != null && link.length() > 0 ) {
	                link = HttpUtil.guessValidURI( link );
	                signature = "["+author+"|"+link+"]";
	            }

	            pageText.append( "\n\n%%signature\n"+signature+", " + commentDate + "\n/%" );
	        }

	        if( TextUtil.isPositive(remember) ) {
	            if( link != null ) {
	                Cookie linkcookie = new Cookie("link", TextUtil.urlEncodeUTF8(link) );
	                linkcookie.setMaxAge(1001*24*60*60);
	                response.addCookie( linkcookie );
	            }

	            CookieAssertionLoginModule.setUserCookie( response, author );
	        } else {
	            session.removeAttribute("link");
	            session.removeAttribute("author");
	        }

	        try {
	            wikiContext.setPage( modifiedPage );
	            engine.getManager( PageManager.class ).saveText( wikiContext, pageText.toString() );
	        } catch( DecisionRequiredException e ) {
	        	String redirect = wikiContext.getURL( ContextEnum.PAGE_VIEW.getRequestContext(), "ApprovalRequiredForPageChanges" );
	            response.sendRedirect( redirect );
	            return;
	        } catch( RedirectException e ) {
	            session.setAttribute( VariableManager.VAR_MSG, e.getMessage() );
	            response.sendRedirect( e.getRedirect() );
	            return;
	        }
	        response.sendRedirect(wikiContext.getViewURL(pagereq));
	        return;
	    } else if( preview != null ) {
	        log.debug("Previewing "+pagereq);
	        session.setAttribute(EditorManager.REQ_EDITEDTEXT, request.getParameter( EditorManager.REQ_EDITEDTEXT ));  //TODO we don't need pageContext, right??
	        response.sendRedirect( TextUtil.replaceString( engine.getURL( ContextEnum.PAGE_PREVIEW.getRequestContext(), pagereq, "action=comment"),"&amp;","&") );
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

	    log.info("Commenting page "+pagereq+". User="+request.getRemoteUser()+", host="+HttpUtil.getRemoteAddress(request) );

	    //
	    //  Determine and store the date the latest version was changed.  Since the newest version is the one that is changed,
	    //  we need to track that instead of the edited version.
	    //
	    long lastchange = 0;

	    Date d = latestversion.getLastModified();
	    if( d != null ) lastchange = d.getTime();

	    //TODO-PP sdlfkjdsfldskjf .. sdflkdsfldskjfdsldsjf what on Earth is this for!?!  Other places it's a string for the spam hash!!
	    setVariableForJSPView( "lastchange", Long.toString( lastchange ) );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??

	    //  This is a hack to get the preview to work.
	    // pageContext.setAttribute( "comment", Boolean.TRUE, PageContext.REQUEST_SCOPE );

	    //
	    //  Attempt to lock the page.
	    //
	    PageLock lock = engine.getManager( PageManager.class ).lockPage( wikipage, storedUser );

	    if( lock != null ) {
	        session.setAttribute( "lock-"+pagereq, lock );
	    }

	    // Set the content type and include the response content
	    response.setContentType("text/html; charset="+engine.getContentEncoding() );
	    response.setHeader( "Cache-control", "max-age=0" );
	    response.setDateHeader( "Expires", new Date().getTime() );
	    response.setDateHeader( "Last-Modified", new Date().getTime() );
	    
	    //String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "EditTemplate.jsp" );
	    //%><wiki:Include page="<%=contentPage%>" />
	    setVariableForJSPView("contentSelector", cte.getContentSelector());
	    setVariableForJSPView("editorSelector", EditorSelectors.getActiveEditorsSelector(engine, wikiContext));
	    serveJSPView("/templates/default/EditTemplate.jsp");
	}
}
