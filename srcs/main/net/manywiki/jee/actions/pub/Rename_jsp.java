package net.manywiki.jee.actions.pub;

import java.text.*;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.api.exceptions.WikiException;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.content.PageRenamer;
import org.apache.wiki.util.HttpUtil;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.tags.BreadcrumbsTag;
import org.apache.wiki.tags.BreadcrumbsTag.FixedQueue;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.util.TextUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.manywiki.jee.actions.ManyWikiActionBean;
import net.manywiki.jee.actions.shared.InfoContentSharedCode;

public class Rename_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		//TODO what exactly did this do? X'D
		//	<fmt:setBundle basename="org.apache.wiki.i18n.core.CoreResources"/>
		
		
		// Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.PAGE_RENAME;
		Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext() );
		if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
		if( wikiContext.getCommand().getTarget() == null ) {
			response.sendRedirect( wikiContext.getURL( wikiContext.getRequestContext(), wikiContext.getName() ) );
			return;
		}
		
		String renameFrom = wikiContext.getName();
		String renameTo = request.getParameter("renameto");
		
		boolean changeReferences = false;
		
		ResourceBundle rb = Preferences.getBundle( wikiContext, "org.apache.wiki.i18n.core.CoreResources" );
		
		if (request.getParameter("references") != null)
		{
			changeReferences = true;
		}
		
		log.info("Page rename request for page '"+renameFrom+ "' to new name '"+renameTo+"' from "+HttpUtil.getRemoteAddress(request)+" by "+request.getRemoteUser() );
		
		Session wikiSession = wikiContext.getWikiSession();
		try
		{
			if (renameTo.length() > 0)
			{
				String renamedTo = engine.getManager( PageRenamer.class ).renamePage(wikiContext, renameFrom, renameTo, changeReferences);
				
				HttpSession session = request.getSession();
				
				FixedQueue trail = (FixedQueue) session.getAttribute( BreadcrumbsTag.BREADCRUMBTRAIL_KEY );
				if( trail != null ) {
					trail.removeItem( renameFrom );
					session.setAttribute( BreadcrumbsTag.BREADCRUMBTRAIL_KEY, trail );
				}
				
				log.info("Page successfully renamed to '"+renamedTo+"'");
				
				response.sendRedirect( wikiContext.getURL( ContextEnum.PAGE_VIEW.getRequestContext(), renamedTo ) );
				return;
			}
			wikiSession.addMessage("rename", rb.getString("rename.empty"));
			
			log.info("Page rename request failed because new page name was left blank");
		}
		catch (WikiException e)
		{
			if (e.getMessage().equals("You cannot rename the page to itself"))
			{
				log.info("Page rename request failed because page names are identical");
				wikiSession.addMessage("rename", rb.getString("rename.identical") );
			}
			else if (e.getMessage().startsWith("Page already exists "))
			{
				log.info("Page rename request failed because new page name is already in use");
				wikiSession.addMessage("rename", MessageFormat.format(rb.getString("rename.exists"),renameTo));
			}
			else
			{
				wikiSession.addMessage("rename",  MessageFormat.format(rb.getString("rename.unknownerror"),e.toString()));
			}
			
		}
		
		
		//TODO-PP Should this variable be used??  I don't think it is in the JSP!
		setVariableForJSPView( "renameto", TextUtil.replaceEntities( renameTo ) );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??
		
		InfoContentSharedCode.finish(this, wikiContext);
	}
}
