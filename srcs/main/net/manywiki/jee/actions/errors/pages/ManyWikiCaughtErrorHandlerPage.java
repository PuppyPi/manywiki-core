package net.manywiki.jee.actions.errors.pages;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.ContextEnum;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.util.FileUtil;
import rebound.simplejee.SimpleJEEUtilities;

public class ManyWikiCaughtErrorHandlerPage
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	protected final HttpServletRequest request;
	protected final HttpServletResponse response;
	protected final ServletContext servletContext;
	protected final WikiEngine engine;
	
	public ManyWikiCaughtErrorHandlerPage(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, WikiEngine engine)
	{
		this.request = request;
		this.response = response;
		this.servletContext = servletContext;
		this.engine = engine;
	}
	
	
	
	public void doLogic(Throwable exception) throws ServletException, IOException
	{
		Context wikiContext = Wiki.context().create( engine, request, ContextEnum.WIKI_ERROR.getRequestContext() );
		String pagereq = wikiContext.getName();
		
		String msg = "An unknown error was caught by Error.jsp";
		
		Throwable realcause = null;
		
		msg = exception.getMessage();
		if( msg == null || msg.length() == 0 )
		{
			msg = "An unknown exception "+exception.getClass().getName()+" was caught by Error.jsp.";
		}
		
		//
		//  This allows us to get the actual cause of the exception.
		//  Note the cast; at least Tomcat has two classes called "JspException"
		//  imported in JSP pages.
		//
		
		if( exception instanceof javax.servlet.jsp.JspException )
		{
			log.debug("IS JSPEXCEPTION");
			realcause = ((javax.servlet.jsp.JspException)exception).getCause();
			log.debug("REALCAUSE="+realcause);
		}
		
		if( realcause == null ) realcause = exception;
		
		log.debug("Error.jsp exception is: ",exception);
		
		
		wikiContext.getWikiSession().addMessage( msg );
		
		if (!getResponse().isCommitted())
		{
			response.setContentType("text/html; charset="+engine.getContentEncoding() );
			
			//FIXME-PP DSLJDSFLKDSJFDLKJF YOU DON'T SEND ERROR DETAILS TO THE POTENTIALLY-UNTRUSTED CLIENT!!LDKJFdlkjflkfj  (Store these in a database or something somewhere!!)
			setVariableForJSPView("errorClassName", realcause.getClass().getName());
			setVariableForJSPView("throwingMethod", FileUtil.getThrowingMethod(realcause));
			SimpleJEEUtilities.serveJSP(servletContext, getRequest(), getResponse(), "/errors/Caught.jsp");
		}
	}
	
	
	
	
	
	
	
	
	public void setVariableForJSPView(String varname, Object value)
	{
		getRequest().setAttribute(varname, value);
	}
	
	
	public HttpServletRequest getRequest()
	{
		return request;
	}
	
	public HttpServletResponse getResponse()
	{
		return response;
	}
}
