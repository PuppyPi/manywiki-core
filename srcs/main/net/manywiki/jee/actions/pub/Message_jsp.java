package net.manywiki.jee.actions.pub;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.ui.TemplateManager;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

//Todo-PP what did this do?   <%@ page isErrorPage="true" %>

public class Message_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		ContextEnum cte = ContextEnum.WIKI_MESSAGE;
		Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext() );
		
		// Stash the wiki context and message text
		request.setAttribute( Context.ATTR_CONTEXT, wikiContext );
		request.setAttribute( "message", request.getParameter( "message" ) );
		
		// Set the content type and include the response content
		response.setContentType( "text/html; charset=" + engine.getContentEncoding() );
		
		//String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "ViewTemplate.jsp" );
		//%><wiki:Include page="<%=contentPage%>" />		
	    setVariableForJSPView("contentSelector", cte.getContentSelector());
		serveJSPView("/templates/default/ViewTemplate.jsp");
	}
}
