package net.manywiki.jee.actions.pub.templates.def.admin;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.*;
import org.apache.wiki.auth.permissions.*;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.render.RenderingManager;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class AJAXPreview_jsp
extends ManyWikiActionBean
{
    protected static final Logger log = LogManager.getLogger("JSPWikiSearch");  //Todo should this be just JSPWiki (was that a copy-paste typo on their part) ?
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		  // Copied from a top-level jsp -- which would be a better place to put this 
		  Context wikiContext = Wiki.context().create( engine, request, ContextEnum.PAGE_VIEW.getRequestContext(), getContext().getServletContext() );
		  if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;

		  response.setContentType("text/html; charset="+engine.getContentEncoding() );
		  
		  String wikimarkup = request.getParameter( "wikimarkup" );
		  
		  //TODO-PP finish replacing this..once I'm done with the immediate important things X3''
		  setVariableForJSPView("wikimarkup", wikimarkup);
		  
		  wikimarkup = wikimarkup.trim();
          final String renderedMarkup = engine.getManager( RenderingManager.class ).textToHTML( wikiContext, wikimarkup );
		  //setVariableForJSPView("renderedMarkup", renderedMarkup);
          
		  serveJSPView();
	}
}
