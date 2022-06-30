package net.manywiki.jee.actions.pub;

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
import net.manywiki.jee.actions.shared.InfoContentSharedCode;

public class PageInfo_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		// Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.PAGE_INFO;
		Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext(), getActionBeanContext().getServletContext() );
		if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
		if( wikiContext.getCommand().getTarget() == null ) {
			response.sendRedirect( wikiContext.getURL( wikiContext.getRequestContext(), wikiContext.getName() ) );
			return;
		}
		String pagereq = wikiContext.getName();
		
		WatchDog w = WatchDog.getCurrentWatchDog( engine );
		try {
			w.enterState("Generating INFO response",60);
			
			InfoContentSharedCode.finish(this, wikiContext);
			
		} finally { w.exitState(); }		
	}
}
