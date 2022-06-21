package net.manywiki.jee.actions.pub;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.WatchDog;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.util.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class Wiki_jsp
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
	    if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
	    String pagereq = wikiContext.getName();

	    // Redirect if request was for a special page
	    String redirect = wikiContext.getRedirectURL( );
	    if( redirect != null )
	    {
	        response.sendRedirect( redirect );
	        return;
	    }

	    StopWatch sw = new StopWatch();
	    sw.start();
	    WatchDog w = WatchDog.getCurrentWatchDog( engine );
	    try {
	        w.enterState("Generating VIEW response for "+wikiContext.getPage(),60);

	        // Set the content type and include the response content
	        response.setContentType("text/html; charset="+engine.getContentEncoding() );
	        
	        //String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "ViewTemplate.jsp" );
	        //%><wiki:Include page="<%=contentPage%>" /><%
	        serveJSPView("/templates/default/ViewTemplate.jsp");
	    }
	    finally
	    {
	        sw.stop();
	        if( log.isDebugEnabled() ) log.debug("Total response time from server on page "+pagereq+": "+sw);
	        w.exitState();
	    }
	}
}
