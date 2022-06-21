package net.manywiki.jee.actions.pub.admin;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.*;
import org.apache.wiki.ui.admin.*;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.util.TextUtil;
import org.apache.wiki.preferences.Preferences;
import org.apache.commons.lang3.time.StopWatch;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class Admin_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		
	    String bean = request.getParameter("bean");
	    // Create wiki context and check for authorization
	    Context wikiContext = Wiki.context().create( engine, request, ContextEnum.WIKI_ADMIN.getRequestContext() );
	    if(!engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;

	    // Set the content type and include the response content
	    response.setContentType("text/html; charset="+engine.getContentEncoding() );

	    setVariableForJSPView( "engine", engine );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??
	    setVariableForJSPView( "context", wikiContext );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??

	    if( request.getMethod().equalsIgnoreCase("post") && bean != null ) {
	        AdminBean ab = engine.getManager( AdminBeanManager.class ).findBean( bean );

	        if( ab != null ) {
	            ab.doPost( wikiContext );
	        } else {
	            wikiContext.getWikiSession().addMessage( "No such bean "+bean+" was found!" );
	        }
	    }

	    //%><wiki:Include page="<%=contentPage%>" />		
		serveJSPView("/templates/default/admin/AdminTemplate.jsp");
	}
}
