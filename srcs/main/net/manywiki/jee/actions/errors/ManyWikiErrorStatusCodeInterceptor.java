package net.manywiki.jee.actions.errors;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rebound.simplejee.SimpleJEEErrorHandler;

public class ManyWikiErrorStatusCodeInterceptor
{
	/**
	 * @see SimpleJEEErrorHandler#sendError(HttpServletRequest, HttpServletResponse, ServletContext, int, String)
	 */
	public static boolean sendError(HttpServletRequest request, HttpServletResponse response, ServletContext context, int code, String message) throws IOException
	{
		//TODO-PP Error handling!
		return false;
	}
}
