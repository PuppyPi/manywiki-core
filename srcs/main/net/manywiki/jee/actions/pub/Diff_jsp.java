package net.manywiki.jee.actions.pub;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.WatchDog;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.tags.InsertDiffTag;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.util.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;
import net.manywiki.jee.actions.shared.InfoContentSharedCode;

public class Diff_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		// Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.PAGE_DIFF;
		Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext(), getActionBeanContext().getServletContext() );
		if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
		if( wikiContext.getCommand().getTarget() == null ) {
			response.sendRedirect( wikiContext.getURL( wikiContext.getRequestContext(), wikiContext.getName() ) );
			return;
		}
		String pagereq = wikiContext.getName();
		
		WatchDog w = WatchDog.getCurrentWatchDog( engine );
		try
		{
			w.enterState("Generating INFO response",60);
			
			// Notused ?
			// String pageurl = wiki.encodeName( pagereq );
			
			// If "r1" is null, then assume current version (= -1)
			// If "r2" is null, then assume the previous version (=current version-1)
			
			// FIXME: There is a set of unnecessary conversions here: InsertDiffTag
			//        does the String->int conversion anyway.
			
			Page wikipage = wikiContext.getPage();
			
			String srev1 = request.getParameter("r1");
			String srev2 = request.getParameter("r2");
			
			int ver1 = -1, ver2 = -1;
			
			if( srev1 != null ) {
				ver1 = Integer.parseInt( srev1 );
			}
			
			if( srev2 != null ) {
				ver2 = Integer.parseInt( srev2 );
			} else {
				int lastver = wikipage.getVersion();
				if( lastver > 1 ) {
					ver2 = lastver-1;
				}
			}
			
			setVariableForJSPView( InsertDiffTag.ATTR_OLDVERSION, Integer.valueOf(ver1) );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??
			setVariableForJSPView( InsertDiffTag.ATTR_NEWVERSION, Integer.valueOf(ver2) );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??
			
			// log.debug("Request for page diff for '"+pagereq+"' from "+HttpUtil.getRemoteAddress(request)+" by "+request.getRemoteUser()+".  R1="+ver1+", R2="+ver2 );
			
			//serveJSPView("/templates/default/view/DiffContent.jsp");  //(this file was just a delegate to InfoContent.jsp)
			InfoContentSharedCode.finish(this, wikiContext);
			
		} finally { w.exitState(); }		
	}
}
