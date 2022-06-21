package net.manywiki.jee.actions.errors.pages;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.ContextEnum;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.util.FileUtil;
import rebound.exceptions.UnreachableCodeError;
import rebound.simplejee.SimpleJEEUtilities;
import rebound.spots.ActionBeanContext;
import rebound.spots.util.AbstractActionBean;
import rebound.spots.util.ActionBeanWithViewResourcePath;
import rebound.spots.util.DefaultSimpleJEEActionBeanWithViewResourcePath;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class Error_jsp
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	protected final ActionBeanContext context;
	protected final WikiEngine engine;
	
	public Error_jsp(ActionBeanContext context, WikiEngine engine)
	{
		this.context = context;
		this.engine = engine;
	}
	
	
	public void doLogic(Throwable exception) throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		
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
			SimpleJEEUtilities.serveJSP(getContext().getServletContext(), getRequest(), getResponse(), "/Error.jsp");
		}
	}
	
	
	
	
	
	
	
	
	public void setVariableForJSPView(String varname, Object value)
	{
		getRequest().setAttribute(varname, value);
	}
	
	
	public ActionBeanContext getContext()
	{
		return this.context;
	}
	
	public HttpServletRequest getRequest()
	{
		return getContext().getRequest();
	}
	
	public HttpServletResponse getResponse()
	{
		return getContext().getResponse();
	}
}
