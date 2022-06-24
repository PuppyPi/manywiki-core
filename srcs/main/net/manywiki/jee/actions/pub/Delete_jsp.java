package net.manywiki.jee.actions.pub;

import static rebound.GlobalCodeMetastuffContext.*;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.manywiki.jee.actions.ManyWikiActionBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.ContextEnum;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.attachment.Attachment;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.tags.BreadcrumbsTag;
import org.apache.wiki.tags.BreadcrumbsTag.FixedQueue;
import org.apache.wiki.util.HttpUtil;
import org.apache.wiki.util.TextUtil;

public class Delete_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		
		// Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.PAGE_DELETE;
		Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext() );
		if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
		if( wikiContext.getCommand().getTarget() == null ) {
			response.sendRedirect( wikiContext.getURL( wikiContext.getRequestContext(), wikiContext.getName() ) );
			return;
		}
		String pagereq = wikiContext.getName();
		
		Page wikipage      = wikiContext.getPage();
		Page latestversion = engine.getManager( PageManager.class ).getPage( pagereq );
		
		String delete = request.getParameter( "delete" );
		String deleteall = request.getParameter( "delete-all" );
		
		if( latestversion == null )
		{
			latestversion = wikiContext.getPage();
		}
		
		// If deleting an attachment, go to the parent page.
		String redirTo = pagereq;
		if( wikipage instanceof Attachment ) {
			redirTo = ((Attachment)wikipage).getParentName();
		}
		
		if( deleteall != null ) {
			log.info("Deleting page "+pagereq+". User="+request.getRemoteUser()+", host="+HttpUtil.getRemoteAddress(request) );
			
			engine.getManager( PageManager.class ).deletePage( pagereq );
			
			HttpSession session = request.getSession();
			FixedQueue trail = (FixedQueue) session.getAttribute( BreadcrumbsTag.BREADCRUMBTRAIL_KEY );
			if( trail != null )
			{
				trail.removeItem( pagereq );
				session.setAttribute( BreadcrumbsTag.BREADCRUMBTRAIL_KEY, trail );
			}
			
			response.sendRedirect( TextUtil.replaceString( engine.getURL( ContextEnum.PAGE_VIEW.getRequestContext(), redirTo, "tab="+request.getParameter("tab") ),"&amp;","&" ));
			return;
		} else if( delete != null ) {
			log.info("Deleting a range of pages from "+pagereq);
			
			for( Enumeration< String > params = request.getParameterNames(); params.hasMoreElements(); ) {
				String paramName = params.nextElement();
				
				if( paramName.startsWith("delver") ) {
					int version = Integer.parseInt( paramName.substring(7) );
					
					Page p = engine.getManager( PageManager.class ).getPage( pagereq, version );
					
					log.debug("Deleting version "+version);
					engine.getManager( PageManager.class ).deleteVersion( p );
				}
			}
			
			response.sendRedirect(
			TextUtil.replaceString( engine.getURL( ContextEnum.PAGE_VIEW.getRequestContext(), redirTo, "tab=" + request.getParameter( "tab" ) ),"&amp;","&" )
			);
			
			return;
		}
		
		
		
		
		//FIXME-PP This had no view in JSPWiki!??!
		logBug();
		
		// Set the content type and include the response content
		// FIXME: not so.
		response.setContentType("text/html; charset="+engine.getContentEncoding() );
		
		//String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "EditTemplate.jsp" );
		//%><wiki:Include page="<%=contentPage%>" />
	    setVariableForJSPView("contentSelector", cte.getContentSelector());
		serveJSPView("/templates/default/EditTemplate.jsp");
	}
}
