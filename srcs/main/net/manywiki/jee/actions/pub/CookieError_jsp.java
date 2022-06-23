package net.manywiki.jee.actions.pub;

import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.ui.TemplateManager;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class CookieError_jsp
extends ManyWikiActionBean
{
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		// Create wiki context; authorization check not needed
		ContextEnum cte = ContextEnum.PAGE_VIEW;
		Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext() );
		
		// Set the content type and include the response content
		response.setContentType("text/html; charset="+engine.getContentEncoding() );
		
		//String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "CookieErrorTemplate.jsp" );
		//%><wiki:Include page="<%=contentPage%>" />
        serveJSPView("/templates/default/CookieErrorTemplate.jsp");  //FIXME-PP THIS DOESN'T EXIST X'D
	}
}
