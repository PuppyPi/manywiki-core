package net.manywiki.jee.actions.pub;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.NoSuchPrincipalException;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.authorize.GroupManager;
import org.apache.wiki.preferences.Preferences;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class DeleteGroup_jsp
extends ManyWikiActionBean
{
    protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
	    // Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.GROUP_DELETE;
	    Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext(), getActionBeanContext().getServletContext() );
	    if(!engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response )) return;

	    Session wikiSession = wikiContext.getWikiSession();
	    GroupManager groupMgr = engine.getManager( GroupManager.class );
	    String name = request.getParameter( "group" );

	    if ( name == null )
	    {
	        // Group parameter was null
	        wikiSession.addMessage( GroupManager.MESSAGES_KEY, "Parameter 'group' cannot be null." );
	        response.sendRedirect( "Group.jsp" );
	    }

	    // Check that the group exists first
	    try
	    {
	        groupMgr.getGroup( name );
	    }
	    catch ( NoSuchPrincipalException e )
	    {
	        // Group does not exist
	        wikiSession.addMessage( GroupManager.MESSAGES_KEY, e.getMessage() );
	        response.sendRedirect( "Group.jsp" );
	    }

	    // Now, let's delete the group
	    try
	    {
	        groupMgr.removeGroup( name );
	        //response.sendRedirect( "." );
	        response.sendRedirect( "Group.jsp?group=" + name );
	    }
	    catch ( WikiSecurityException e )
	    {
	        // Send error message
	        wikiSession.addMessage( GroupManager.MESSAGES_KEY, e.getMessage() );
	        response.sendRedirect( "Group.jsp" );
	    }
	}
}
