package net.manywiki.jee.actions.errors;

import static rebound.util.ExceptionPrettyPrintingUtilities.*;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.errors.pages.ManyWikiErrorHandlerPageFor403Forbidden;
import org.apache.wiki.WikiEngine;
import rebound.simplejee.SimpleJEEErrorHandler;

public class ManyWikiErrorStatusCodeInterceptor
{
	/**
	 * @see SimpleJEEErrorHandler#sendError(int, String)
	 */
	public static boolean sendError(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, int code, String message, WikiEngine engine)
	{
		try
		{
			if (code == 403)
			{
				new ManyWikiErrorHandlerPageFor403Forbidden(request, response, servletContext, engine).doLogic();
				return true;
			}
			
			//TODO-PP Full proper error handling!
		}
		catch (ServletException | IOException exc)
		{
			System.err.println("Error in the error handler!! X'D");
			printStackTraceFully(exc);
			
			if (!response.isCommitted())
			{
				try
				{
					response.sendError(500);  //I wish we didn't have to worry about Tomcat revealing sensitive information to potential hackers here, but oh well, someday we'll use the Rebound Superserver and that'll be a thing of the past! XD
				}
				catch (IOException exc1)
				{
					System.err.println("Error in the error handler error handler!!! X\"D");
					printStackTraceFully(exc1);
					//Then do nothing! XD'
				}
			}
		}
		
		return false;
	}
}
