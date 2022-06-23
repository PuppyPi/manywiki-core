package net.manywiki.jee.actions.pub.templates.def.admin;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.*;
import org.apache.wiki.auth.permissions.*;
import org.apache.wiki.preferences.Preferences;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class AJAXPreview_jsp
extends ManyWikiActionBean
{
    protected static final Logger log = LogManager.getLogger("JSPWikiSearch");
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		  // Copied from a top-level jsp -- which would be a better place to put this 
		  Context wikiContext = Wiki.context().create( engine, request, ContextEnum.PAGE_VIEW.getRequestContext() );
		  if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;

		  response.setContentType("text/html; charset="+engine.getContentEncoding() );
		  
		  String wikimarkup = request.getParameter( "wikimarkup" );
		  
		  setVariableForJSPView("wikimarkup", wikimarkup);
		  serveJSPView();
	}
}
