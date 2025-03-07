package net.manywiki.jee.actions.pub;

import java.text.*;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.authorize.Group;
import org.apache.wiki.auth.authorize.GroupManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.ui.TemplateManager;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class NewGroup_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		
		// Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.WIKI_CREATE_GROUP;
		Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext(), getActionBeanContext().getServletContext() );
		if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
		
		// Extract the current user, group name, members and action attributes
		Session wikiSession = wikiContext.getWikiSession();
		GroupManager groupMgr = engine.getManager( GroupManager.class );
		Group group = null;
		try 
		{
			group = groupMgr.parseGroup( wikiContext, true );
			setVariableForJSPView( "Group", group );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??
		} catch ( WikiSecurityException e ) {
			wikiSession.addMessage( GroupManager.MESSAGES_KEY, e.getMessage() );
			response.sendRedirect( "Group.jsp" );
		}
		
		// Are we saving the group?
		if( "save".equals( request.getParameter( "action" ) ) ) {
			// Validate the group
			groupMgr.validateGroup( wikiContext, group );
			
			try {
				groupMgr.getGroup( group.getName() );
				
				// Oops! The group already exists. This is mischief!
				ResourceBundle rb = Preferences.getBundle( wikiContext, "org.apache.wiki.i18n.core.CoreResources");
				wikiSession.addMessage( GroupManager.MESSAGES_KEY,
				MessageFormat.format(rb.getString("newgroup.exists"),group.getName()));
			} catch( NoSuchPrincipalException e ) {
				// Group not found; this is good!
			}
			
			// If no errors, save the group now
			if( wikiSession.getMessages( GroupManager.MESSAGES_KEY ).length == 0 ) {
				try {
					groupMgr.setGroup( wikiSession, group );
				} catch( WikiSecurityException e ) {
					// Something went horribly wrong! Maybe it's an I/O error...
					wikiSession.addMessage( GroupManager.MESSAGES_KEY, e.getMessage() );
				}
			}
			if ( wikiSession.getMessages( GroupManager.MESSAGES_KEY ).length == 0 ) {
				response.sendRedirect( "Group.jsp?group=" + group.getName() );
				return;
			}
		}
		
		
		
		
		setVariableForJSPView("wikiPageContext", wikiContext);
		
		
		// Set the content type and include the response content
		response.setContentType("text/html; charset="+engine.getContentEncoding() );
		
		//serveJSPView("/templates/default/view/NewGroupContent.jsp");  //(this file was just a delegate to PreferencesContent.jsp)
		serveJSPView("/templates/default/view/PreferencesContent.jsp");
	}
}
