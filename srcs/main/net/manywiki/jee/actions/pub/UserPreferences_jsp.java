package net.manywiki.jee.actions.pub;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.ContextEnum;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Session;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.WikiSecurityException;
import org.apache.wiki.auth.login.CookieAssertionLoginModule;
import org.apache.wiki.auth.user.DuplicateUserException;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.i18n.InternationalizationManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.ui.EditorManager;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.util.HttpUtil;
import org.apache.wiki.variables.VariableManager;
import org.apache.wiki.workflow.DecisionRequiredException;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class UserPreferences_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		// Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.WIKI_PREFS;
		Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext() );
		if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
		
		// Extract the user profile and action attributes
		UserManager userMgr = engine.getManager( UserManager.class );
		Session wikiSession = wikiContext.getWikiSession();
		
		/* FIXME: Obsolete
	    if( request.getParameter(EditorManager.PARA_EDITOR) != null )
	    {
	    	String editor = request.getParameter(EditorManager.PARA_EDITOR);
	    	session.setAttribute(EditorManager.PARA_EDITOR,editor);
	    }
		 */
		
		// Are we saving the profile?
		if( "saveProfile".equals( request.getParameter( "action" ) ) ) {
			UserProfile profile = userMgr.parseProfile( wikiContext );
			
			// Validate the profile
			userMgr.validateProfile( wikiContext, profile );
			
			// If no errors, save the profile now & refresh the principal set!
			if( wikiSession.getMessages( "profile" ).length == 0 ) {
				try {
					userMgr.setUserProfile( wikiContext, profile );
					CookieAssertionLoginModule.setUserCookie( response, profile.getFullname() );
				} catch( DuplicateUserException due ) {
					// User collision! (full name or wiki name already taken)
					wikiSession.addMessage( "profile", engine.getManager( InternationalizationManager.class )
					.get( InternationalizationManager.CORE_BUNDLE,
					Preferences.getLocale( wikiContext ), 
					due.getMessage(), due.getArgs() ) );
				} catch( DecisionRequiredException e ) {
					String redirect = engine.getURL( ContextEnum.PAGE_VIEW.getRequestContext(), "ApprovalRequiredForUserProfiles", null );
					response.sendRedirect( redirect );
					return;
				} catch( WikiSecurityException e ) {
					// Something went horribly wrong! Maybe it's an I/O error...
					wikiSession.addMessage( "profile", e.getMessage() );
				}
			}
			if( wikiSession.getMessages( "profile" ).length == 0 ) {
				String redirectPage = request.getParameter( "redirect" );
				
				if( !engine.getManager( PageManager.class ).wikiPageExists( redirectPage ) ) {
					redirectPage = engine.getFrontPage();
				}
				
				String viewUrl = ( "UserPreferences".equals( redirectPage ) ) ? "Wiki.jsp" : wikiContext.getViewURL( redirectPage );
				log.info( "Redirecting user to {}", viewUrl );
				response.sendRedirect( viewUrl );
				return;
			}
		}
		if( "setAssertedName".equals( request.getParameter( "action" ) ) ) {
			Preferences.reloadPreferences( request, getContext().getServletContext() );
			
			String assertedName = request.getParameter( "assertedName" );
			CookieAssertionLoginModule.setUserCookie( response, assertedName );
			
			String redirectPage = request.getParameter( "redirect" );
			if( !engine.getManager( PageManager.class ).wikiPageExists( redirectPage ) )
			{
				redirectPage = engine.getFrontPage();
			}
			String viewUrl = ( "UserPreferences".equals( redirectPage ) ) ? "Wiki.jsp" : wikiContext.getViewURL( redirectPage );
			
			log.info( "Redirecting user to {}", viewUrl );
			response.sendRedirect( viewUrl );
			return;
		}
		if( "clearAssertedName".equals( request.getParameter( "action" ) ) ) {
			HttpUtil.clearCookie( response, Preferences.COOKIE_USER_PREFS_NAME );
			CookieAssertionLoginModule.clearUserCookie( response );
			Preferences.reloadPreferences( request, getContext().getServletContext() );
			
			String redirectPage = request.getParameter( "redirect" );
			if( !engine.getManager( PageManager.class ).wikiPageExists( redirectPage ) )
			{
				redirectPage = engine.getFrontPage();
			}
			String viewUrl = ( "UserPreferences".equals( redirectPage ) ) ? "Wiki.jsp" : wikiContext.getViewURL( redirectPage );
			
			log.info( "Redirecting user to " + viewUrl );
			response.sendRedirect( viewUrl );
			return;
		}
		
		
		response.setContentType( "text/html; charset=" + engine.getContentEncoding() );
		
		//String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "ViewTemplate.jsp" );
		//%><wiki:Include page="<%=contentPage%>" />
	    setVariableForJSPView("contentSelector", cte.getContentSelector());
        serveJSPView("/templates/default/ViewTemplate.jsp");
	}
}
