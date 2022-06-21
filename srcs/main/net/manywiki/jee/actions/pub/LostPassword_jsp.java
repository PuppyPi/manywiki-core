package net.manywiki.jee.actions.pub;

import java.util.*;
import java.text.*;
import javax.mail.*;
import javax.servlet.jsp.jstl.fmt.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.ContextEnum;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Session;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.*;
import org.apache.wiki.auth.user.*;
import org.apache.wiki.i18n.*;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.url.URLConstructor;
import org.apache.wiki.util.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class LostPassword_jsp
extends ManyWikiActionBean
{
    protected static final Logger log = LogManager.getLogger( "JSPWiki" );
	
    


    String message = null;

    public boolean resetPassword( Engine wiki, HttpServletRequest request, ResourceBundle rb ) {
        // Reset pw for account name
        String name = request.getParameter( "name" );
        UserDatabase userDatabase = wiki.getManager( UserManager.class ).getUserDatabase();
        boolean success = false;

        try {
            UserProfile profile = null;
            /*
             // This is disabled because it would otherwise be possible to DOS JSPWiki instances
             // by requesting new passwords for all users.  See https://issues.apache.org/jira/browse/JSPWIKI-78
             try {
                 profile = userDatabase.find(name);
             } catch (NoSuchPrincipalException e) {
             // Try email as well
             }
            */
            if( profile == null ) {
                profile = userDatabase.findByEmail( name );
            }

            String email = profile.getEmail();
            String randomPassword = TextUtil.generateRandomPassword();

            // Try sending email first, as that is more likely to fail.

            Object[] args = { profile.getLoginName(), randomPassword, request.getScheme() + "://"+ request.getServerName() + ":" + request.getServerPort() +
                             wiki.getManager( URLConstructor.class ).makeURL( ContextEnum.PAGE_NONE.getRequestContext(), "Login.jsp", "" ), wiki.getApplicationName() };

            String mailMessage = MessageFormat.format( rb.getString( "lostpwd.newpassword.email" ), args );

            Object[] args2 = { wiki.getApplicationName() };
            MailUtil.sendMessage( wiki.getWikiProperties(), 
            		              email, 
            		              MessageFormat.format( rb.getString( "lostpwd.newpassword.subject" ), args2 ),
                                  mailMessage );

            log.info( "User " + email + " requested and received a new password." );

            // Mail succeeded.  Now reset the password.
            // If this fails, we're kind of screwed, because we already emailed.
            profile.setPassword( randomPassword );
            userDatabase.save( profile );
            success = true;
        } catch( NoSuchPrincipalException e ) {
            Object[] args = { name };
            message = MessageFormat.format( rb.getString( "lostpwd.nouser" ), args );
            log.info( "Tried to reset password for non-existent user '" + name + "'" );
        } catch( SendFailedException e ) {
            message = rb.getString( "lostpwd.nomail" );
            log.error( "Tried to reset password and got SendFailedException: " + e );
        } catch( AuthenticationFailedException e ) {
            message = rb.getString( "lostpwd.nomail" );
            log.error( "Tried to reset password and got AuthenticationFailedException: " + e );
        } catch( Exception e ) {
            message = rb.getString( "lostpwd.nomail" );
            log.error( "Tried to reset password and got another exception: " + e );
        }
        return success;
    }
    
    
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		
	    //Create wiki context like in Login.jsp:
	    //don't check for access permissions: if you have lost your password you cannot login!
	    Context wikiContext = ( Context )pageContext.getAttribute( Context.ATTR_CONTEXT, PageContext.REQUEST_SCOPE );
	    
	    // If no context, it means we're using container auth.  So, create one anyway
	    if( wikiContext == null ) {
	        wikiContext = Wiki.context().create( engine, request, ContextEnum.WIKI_LOGIN.getRequestContext() ); /* reuse login context ! */
	        setVariableForJSPView( Context.ATTR_CONTEXT, wikiContext );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??
	    }

	    ResourceBundle rb = Preferences.getBundle( wikiContext, "org.apache.wiki.i18n.core.CoreResources" );

	    Session wikiSession = wikiContext.getWikiSession();
	    String action = request.getParameter( "action" );

	    boolean done = false;

	    if( action != null && action.equals( "resetPassword" ) ) {
	        if( resetPassword( engine, request, rb ) ) {
	            done = true;
	            wikiSession.addMessage( "resetpwok", rb.getString( "lostpwd.emailed" ) );
	            setVariableForJSPView( "passwordreset", "done" );
	        } else {
	            // Error
	            wikiSession.addMessage( "resetpw", message );
	        }
	    }

	    response.setContentType( "text/html; charset=" + engine.getContentEncoding() );
	    response.setHeader( "Cache-control", "max-age=0" );
	    response.setDateHeader( "Expires", new Date().getTime() );
	    response.setDateHeader( "Last-Modified", new Date().getTime() );

	    //String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "ViewTemplate.jsp" );
	    //<wiki:Include page="<%=contentPage%>" />
	    serveJSPView("/templates/default/ViewTemplate.jsp");
	}
}
