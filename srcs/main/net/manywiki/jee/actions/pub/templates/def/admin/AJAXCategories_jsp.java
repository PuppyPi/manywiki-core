package net.manywiki.jee.actions.pub.templates.def.admin;

import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.preferences.Preferences;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class AJAXCategories_jsp
extends ManyWikiActionBean
{
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		  // Copied from a top-level jsp -- which would be a better place to put this 
		  Context wikiContext = Wiki.context().create( engine, request, ContextEnum.PAGE_VIEW.getRequestContext(), getActionBeanContext().getServletContext() );
		  if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
		  String pagereq = wikiContext.getPage().getName();

		  response.setContentType("text/html; charset="+engine.getContentEncoding() );
		  
		  serveJSPView();
	}
}
