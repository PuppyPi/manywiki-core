package net.manywiki.jee.actions.pub;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.authorize.Group;
import org.apache.wiki.auth.authorize.GroupManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.ui.TemplateManager;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class Group_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		
	    // Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.GROUP_VIEW;
	    Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext(), getActionBeanContext().getServletContext() );
	    if(!engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response )) return;
	    
	    // Extract the current user, group name, members
	    Session wikiSession = wikiContext.getWikiSession();
	    GroupManager groupMgr = engine.getManager( GroupManager.class );
	    Group group = null;
	    try {
	        group = groupMgr.parseGroup( wikiContext, false );
	        setVariableForJSPView ( "Group", group );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??
	    } catch ( NoSuchPrincipalException e ) {
	        // New group; let GroupContent print out the message...
	    } catch ( WikiSecurityException e ) {
	        wikiSession.addMessage( GroupManager.MESSAGES_KEY, e.getMessage() );
	    }
	    
	    
	    
	    
		setVariableForJSPView("wikiPageContext", wikiContext);
		
	    
	    // Set the content type and include the response content
	    response.setContentType("text/html; charset="+engine.getContentEncoding() );
	    
		//serveJSPView("/templates/default/view/GroupContent.jsp");  //(this file was just a delegate to PreferencesContent.jsp)
		serveJSPView("/templates/default/view/PreferencesContent.jsp");
	}
}
