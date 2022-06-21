package net.manywiki.jee.actions.pub;

import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthenticationManager;
import org.apache.wiki.auth.login.CookieAssertionLoginModule;
import org.apache.wiki.auth.login.CookieAuthenticationLoginModule;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class Logout_jsp
extends ManyWikiActionBean
{
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		  engine.getManager( AuthenticationManager.class ).logout( request );

		  // Clear the user cookie
		  CookieAssertionLoginModule.clearUserCookie( response );

		  // Delete the login cookie
		  CookieAuthenticationLoginModule.clearLoginCookie( engine, request, response );

		  // Redirect to the webroot
		  // TODO: Should redirect to a "goodbye" -page?
		  response.sendRedirect(".");
	}
}
