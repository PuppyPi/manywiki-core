package net.manywiki.jee.actions.errors.pages;

import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wiki.WikiEngine;
import rebound.simplejee.SimpleJEEUtilities;

public class ManyWikiErrorHandlerPageFor403Forbidden
{
	protected final HttpServletRequest request;
	protected final HttpServletResponse response;
	protected final ServletContext servletContext;
	protected final WikiEngine engine;
	
	public ManyWikiErrorHandlerPageFor403Forbidden(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, WikiEngine engine)
	{
		this.request = request;
		this.response = response;
		this.servletContext = servletContext;
		this.engine = engine;
	}



	public void doLogic() throws ServletException, IOException
	{
		//HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		
		if (!getResponse().isCommitted())
		{
			response.setContentType("text/html; charset="+engine.getContentEncoding() );
			
			SimpleJEEUtilities.serveStatically(servletContext, getRequest(), getResponse(), "/errors/Forbidden.html");
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
