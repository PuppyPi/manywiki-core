package net.manywiki.jee.actions.pub;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.text.*;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.ui.EditorManager;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.util.TextUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class PageModified_jsp
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
		ContextEnum cte = ContextEnum.PAGE_CONFLICT;
	    Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext() );
	    if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
	    if( wikiContext.getCommand().getTarget() == null ) {
	        response.sendRedirect( wikiContext.getURL( wikiContext.getRequestContext(), wikiContext.getName() ) );
	        return;
	    }
	    String pagereq = wikiContext.getName();

	    String usertext = (String)session.getAttribute( EditorManager.REQ_EDITEDTEXT );

	    // Make the user and conflicting text presentable for display.
	    usertext = StringEscapeUtils.escapeXml11( usertext );

	    String conflicttext = engine.getManager( PageManager.class ).getText(pagereq);
	    conflicttext = StringEscapeUtils.escapeXml11( conflicttext );

	    setVariableForJSPView( "conflicttext", conflicttext );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??

	    log.info("Page concurrently modified "+pagereq);
	    setVariableForJSPView( "usertext", usertext );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??

	    // Set the content type and include the response content
	    response.setContentType("text/html; charset="+engine.getContentEncoding() );
	    
	    //String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "ViewTemplate.jsp" );
	    //%><wiki:Include page="<%=contentPage%>" />
		serveJSPView("/templates/default/ViewTemplate.jsp");
	}
}
