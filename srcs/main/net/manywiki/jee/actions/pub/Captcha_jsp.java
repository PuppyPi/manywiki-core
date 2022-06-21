package net.manywiki.jee.actions.pub;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.util.*;
import org.apache.wiki.ui.EditorManager;
import org.apache.commons.lang3.time.StopWatch;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class Captcha_jsp
extends ManyWikiActionBean
{
    protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
	    // Create wiki context and check for authorization
	    Context wikiContext = Wiki.context().create( engine, request, ContextEnum.PAGE_VIEW.getRequestContext() );
	    if(!engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response )) return;
	    String pagereq = wikiContext.getName();
	    String reqPage = TextUtil.replaceEntities( request.getParameter( "page" ) );
	    String content = TextUtil.replaceEntities( request.getParameter( "text" ) );

	    if( content != null )
	    {
	        String ticket = TextUtil.replaceEntities( request.getParameter( "Asirra_Ticket" ) );
	        HttpClient client = new HttpClient();
	        HttpMethod method = new GetMethod("http://challenge.asirra.com/cgi/Asirra?action=ValidateTicket&ticket="+ticket);

	        int status = client.executeMethod(method);
	        String body = method.getResponseBodyAsString();

	        if( status == HttpStatus.SC_OK )
	        {
	            if( body.indexOf( "Pass" ) != -1 )
	            {
	                request.getSession().setAttribute( "captcha", "ok" );
	                response.sendRedirect( wikiContext.getURL( ContextEnum.PAGE_EDIT.getRequestContext(), reqPage ) );
	                return;
	            }
	        }

	        response.sendRedirect("Message.jsp?message=NOK");
	    }

	    
	    // Set the content type and include the response content
	    response.setContentType( "text/html; charset=" + engine.getContentEncoding() );
		
	    setVariableForJSPView("reqPage", reqPage);
	    
		serveJSPView();
	}
}
