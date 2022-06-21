package net.manywiki.jee.actions.pub;

//Login.jsp
import java.security.Principal;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.*;
import org.apache.wiki.auth.login.CookieAssertionLoginModule;
import org.apache.wiki.auth.login.CookieAuthenticationLoginModule;
import org.apache.wiki.auth.user.DuplicateUserException;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.i18n.InternationalizationManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.workflow.DecisionRequiredException;

//LoginForm.jsp
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.ui.TemplateManager;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.manywiki.jee.actions.ManyWikiActionBean;

//Note: this came from both Login.jsp and LoginForm.jsp

public class Login_jsp
extends ManyWikiActionBean
{
	//From LoginForm.jsp but not Login.jsp
    /**
     * This page contains the logic for finding and including
       the correct login form, which is usually loaded from
       the template directory's LoginContent.jsp page.
       It should not be requested directly by users. If
       container-managed authentication is in force, the container
       will prevent direct access to it.
     */
	
	
    protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
	    AuthenticationManager mgr = engine.getManager( AuthenticationManager.class );
	    Context wikiContext = Wiki.context().create( engine, request, ContextEnum.WIKI_LOGIN.getRequestContext() );
	    setVariableForJSPView( Context.ATTR_CONTEXT, wikiContext );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??
	    Session wikiSession = wikiContext.getWikiSession();
	    ResourceBundle rb = Preferences.getBundle( wikiContext, "org.apache.wiki.i18n.core.CoreResources" );

	    // Set the redirect-page variable if one was passed as a parameter
	    if( request.getParameter( "redirect" ) != null ) {
	        wikiContext.setVariable( "redirect", request.getParameter( "redirect" ) );
	    } else {
	        wikiContext.setVariable( "redirect", engine.getFrontPage() );
	    }

	    // Are we saving the profile?
	    if( "saveProfile".equals(request.getParameter("action")) ) {
	        UserManager userMgr = engine.getManager( UserManager.class );
	        UserProfile profile = userMgr.parseProfile( wikiContext );
	         
	        // Validate the profile
	        userMgr.validateProfile( wikiContext, profile );

	        // If no errors, save the profile now & refresh the principal set!
	        if ( wikiSession.getMessages( "profile" ).length == 0 ) {
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
	        if ( wikiSession.getMessages( "profile" ).length == 0 ) {
	            String redirectPage = request.getParameter( "redirect" );
	            response.sendRedirect( wikiContext.getViewURL(redirectPage) );
	            return;
	        }
	    }

	    // If NOT using container auth, perform all of the access control logic here...
	    // (Note: if using the container for auth, it will handle all of this for us.)
	    if( !mgr.isContainerAuthenticated() ) {
	        // If user got here and is already authenticated, it means they just aren't allowed access to what they asked for.
	        // Weepy tears and hankies all 'round.
	        if( wikiSession.isAuthenticated() ) {
	        	response.sendError( HttpServletResponse.SC_FORBIDDEN, rb.getString("login.error.noaccess") );
	            return;
	        }

	        // If using custom auth, we need to do the login now
	        String action = request.getParameter("action");
	        if( request.getParameter("submitlogin") != null ) {
	            String uid    = request.getParameter( "j_username" );
	            String passwd = request.getParameter( "j_password" );
	            log.debug( "Attempting to authenticate user {}", uid );

	            // Log the user in!
	            if ( mgr.login( wikiSession, request, uid, passwd ) ) {
	                log.info( "Successfully authenticated user {} (custom auth)", uid );
	            } else {
	                log.info( "Failed to authenticate user {}", uid );
	                wikiSession.addMessage( "login", rb.getString( "login.error.password" ) );
	            }
	        }
	    } else {
	        //  Have we already been submitted?  If yes, then we can assume that we have been logged in before.
	    	HttpSession session = request.getSession();
	    	
	        Object seen = session.getAttribute( "_redirect" );
	        if( seen != null ) {
	        	response.sendError( HttpServletResponse.SC_FORBIDDEN, rb.getString( "login.error.noaccess" ) );
	            session.removeAttribute( "_redirect" );
	            return;
	        }
	        session.setAttribute( "_redirect","I love Outi" ); // Just any marker will do

	        // If using container auth, the container will have automatically attempted to log in the user before
	        // Login.jsp was loaded. Thus, if we got here, the container must have authenticated the user already.
	        // All we do is simply record that fact. Nice and easy.
	        Principal user = wikiSession.getLoginPrincipal();
	        log.info( "Successfully authenticated user {} (container auth)", user.getName() );
	    }

	    // If user logged in, set the user cookie with the wiki principal's name.
	    // redirect to wherever we're supposed to go. If login.jsp
	    // was called without parameters, this will be the front page. Otherwise,
	    // there's probably a 'redirect' parameter telling us where to go.

	    if( wikiSession.isAuthenticated() ) {
	        String rember = request.getParameter( "j_remember" );

	        // Set user cookie
	        Principal principal = wikiSession.getUserPrincipal();
	        CookieAssertionLoginModule.setUserCookie( response, principal.getName() );

	        if( rember != null ) {
	            CookieAuthenticationLoginModule.setLoginCookie( engine, response, principal.getName() );
	        }

	        // If wiki page was "Login", redirect to main, otherwise use the page supplied
	        String redirectPage = request.getParameter( "redirect" );
	        if( !engine.getManager( PageManager.class ).wikiPageExists( redirectPage ) ) {
	           redirectPage = engine.getFrontPage();
	        }
	        String viewUrl = ( "Login".equals( redirectPage ) ) ? "Wiki.jsp" : wikiContext.getViewURL( redirectPage );

	        // Redirect!
	        log.info( "Redirecting user to {}", viewUrl );
	        response.sendRedirect( viewUrl );
	        return;
	    }

	    // If we've gotten here, the user hasn't authenticated yet.
	    // So, find the login form and include it. This should be in the same directory
	    // as this page. We don't need to use a tagfile.

	    response.setContentType("text/html; charset="+engine.getContentEncoding() );
	    
	    //serveJSPView("/LoginForm.jsp");
	    doLogicLoginForm(wikiContext);
	}
	
	
	
	
    public void doLogicLoginForm(Context wikiContext) throws ServletException, IOException
    {
        // Retrieve the Login page context, then go and find the login form

        //String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "ViewTemplate.jsp" );
        //log.debug( "Login template content is: {}", contentPage );
        
        //%><wiki:Include page="<%=contentPage%>" />
        
        serveJSPView("/templates/default/ViewTemplate.jsp");
    }
}
