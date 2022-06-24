package net.manywiki.jee.actions.pub;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.ui.TemplateManager;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class Upload_jsp
extends ManyWikiActionBean
{
    protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
	    // Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.PAGE_UPLOAD;
	    Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext() );
	    if(!engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response )) return;
	    String pagereq = wikiContext.getName();

	    // Set the content type and include the response content
	    response.setContentType("text/html; charset="+engine.getContentEncoding() );
	    
	    //String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "UploadTemplate.jsp" );
	    //%><wiki:Include page="<%=contentPage%>" />
        serveJSPView("/templates/default/UploadTemplate.jsp");
	}
}
