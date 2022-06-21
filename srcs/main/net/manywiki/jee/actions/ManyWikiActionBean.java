package net.manywiki.jee.actions;

import static rebound.GlobalCodeMetastuffContext.*;
import static rebound.text.StringUtilities.*;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.TemporaryManyWikiRoot;
import net.manywiki.jee.actions.errors.ManyWikiErrorStatusCodeInterceptor;
import net.manywiki.jee.actions.errors.pages.ManyWikiCaughtErrorHandlerPage;
import org.apache.wiki.WikiEngine;
import rebound.net.ReURL;
import rebound.simplejee.FlushPendingHttpServletResponseDecorator;
import rebound.simplejee.ReplacementErrorResolutionResponseWrapper;
import rebound.spots.util.binding.annotated.util.AbstractBindingAnnotatedSimpleJEEActionBeanWithViewResourcePath;
import rebound.spots.util.binding.annotated.util.TuneBuffer;
import rebound.text.StringUtilities;
import rebound.util.AngryReflectionUtility;

public class ManyWikiActionBean
extends AbstractBindingAnnotatedSimpleJEEActionBeanWithViewResourcePath
{
	protected final boolean usesFlushPending;
	
	//Put the flush pender after!  Its whole point is to prevent premature flushing and committal of the response!  So if we err, we *want* to discard this one's whole buffer! so that the error page can be sent instead!
	protected FlushPendingHttpServletResponseDecorator flushPendingResponse;  //if usesFlushPending, this must always be null!! 
	protected ReplacementErrorResolutionResponseWrapper errorInterceptorResponse;
	
	protected WikiEngine engine;
	
	
	public ManyWikiActionBean(boolean usesFlushPending)
	{
		this.usesFlushPending = usesFlushPending;
	}
	
	public ManyWikiActionBean()
	{
		this(false);  //DON'T CHANGE THIS WITHOUT UPDATING EVERYTHING XD
	}
	
	
	public void setWikiEngine(WikiEngine engine)
	{
		if (this.engine != null)
			logBug();
		
		this.engine = engine;
	}
	
	
	public HttpServletResponse getResponse()
	{
		ReplacementErrorResolutionResponseWrapper errorInterceptorResponse = this.errorInterceptorResponse;
		{
			if (errorInterceptorResponse == null)
			{
				HttpServletResponse underlying = getContext().getResponse();
				errorInterceptorResponse = new ReplacementErrorResolutionResponseWrapper(underlying, (int code, String message) -> ManyWikiErrorStatusCodeInterceptor.sendError(getContext().getRequest(), underlying, getContext().getServletContext(), code, message, engine));
				this.errorInterceptorResponse = errorInterceptorResponse;
			}
		}
		
		
		if (this.usesFlushPending)
		{
			if (this.flushPendingResponse == null)
				this.flushPendingResponse = new FlushPendingHttpServletResponseDecorator(errorInterceptorResponse);
			return this.flushPendingResponse;
		}
		else
		{
			return errorInterceptorResponse;
		}
	}
	
	/**
	 * You MUST CALL THIS EXPLICITLY if you use {@link #ManyWikiActionBean(boolean) flush-pending}!!
	 */
	protected void actuallyCloseAndFlushResponseIfNeeded() throws IOException
	{
		if (this.flushPendingResponse != null)
			this.flushPendingResponse.reallyClose();
	}
	
	
	
	
	
	
	
	
	//<Main
	@Override
	public final void doAction() throws ServletException, IOException
	{
		log("DFLKDSJFLJF 7) "+getResponse().isCommitted());  //TODO REMOVE
		if (!getRequest().getMethod().equals("GET") && !getRequest().getMethod().equals("POST"))
		{
			//We can include a nice body in this as HTML in case they render it to someone somewhere X3
			//So use getResponse().sendError() which has the error interceptor decorator :3
			
			getResponse().setHeader("Allow", "GET, POST");
			sendError(405);
			return;
		}
		
		
		if
		(
		//+ There is no extra information that would be lost in a redirect (eg, a form payload)
		getRequest().getMethod().equals("GET")
		&&
		
		//Google's been giving us grief since 2012 - mid July about 302 to HTTPS on robots.txt, so leave that as an exception
		!StringUtilities.trim(getRequest().getRequestURI(), '/').equals("robots.txt")
		
		&&
		(
		//+ Request is not secure and FORCE_SECURE is on
		(!getRequest().isSecure() && TemporaryManyWikiRoot.isForceSecure())
		||
		(
		//+ The uri has a problematic trailing slash
		getRequest().getRequestURI().endsWith("/")
		&&
		//+ The uri is not "/"
		!getRequest().getRequestURI().equals("/")
		)
		)
		)
		{
			//Redirect to the same url, but properly
			redirectSame();
		}
		else
		{
			log("DFLKDSJFLJF 8) "+getResponse().isCommitted());  //TODO REMOVE
			doValidAction();
		}
	}
	
	/**
	 * The reason for this is because {@link ManyWikiActionBean} has to intercept doAction. And the way OOP overriding works, a superclass intercepts a method by overriding it and creating a separate method which subclasses should override.<br>
	 */
	protected void doValidAction() throws ServletException, IOException
	{
		try
		{
			log("DFLKDSJFLJF 9) "+getResponse().isCommitted());  //TODO REMOVE
			
			doBufferTuning();
			
			log("DFLKDSJFLJF 10) "+getResponse().isCommitted());  //TODO REMOVE
			
			long start = System.currentTimeMillis();
			try
			{
				if (doBinding()) //This has to come before passFail() because it binds information used in determining the  auth checks, etc.
				{
					log("DFLKDSJFLJF 11) "+getResponse().isCommitted());  //TODO REMOVE
					if (passFail())
					{
						log("DFLKDSJFLJF 12) "+getResponse().isCommitted());  //TODO REMOVE
						doLogic();
					}
				}
			}
			finally
			{
				logBenchmark(start, "Whole action for "+getClass());
			}
		}
		catch (Throwable exc)
		{
			//Note that we VERY MUCH don't want to use getResponse()!! (or else when it invoked response.sendError() it will get intercepted!!).
			//It must use the equivalent of getContext().getResponse() instead—ie, the original response!
			ManyWikiCaughtErrorHandlerPage errorHandler = new ManyWikiCaughtErrorHandlerPage(getContext().getRequest(), getContext().getResponse(), getContext().getServletContext(), engine);
			errorHandler.doLogic(exc);
		}
	}
	
	protected void doBufferTuning()
	{
		TuneBuffer a = getClass().getAnnotation(TuneBuffer.class);
		
		if (a != null)
		{
			getResponse().setBufferSize(a.value());
		}
		else
		{
			getResponse().setBufferSize(usesFlushPending ? TuneBuffer.DEFAULT_RESPONSE_BUFFER_SIZE_YESFLUSHPENDING : TuneBuffer.DEFAULT_RESPONSE_BUFFER_SIZE_NOFLUSHPENDING);
		}
	}
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		//This is the default :3
		log("DFLKDSJFLJF 13) "+getResponse().isCommitted());  //TODO REMOVE
		serveView();
	}
	
	protected void doEventHandling() throws ServletException, IOException
	{
		super.doLogic();
	}
	
	/**
	 * Determines if the logic should be invoked (aside from binding errors)<br>
	 * This should also take the appropriate action to provide a resolution if it returns <code>false</code>.<br>
	 */
	public boolean passFail() throws ServletException, IOException
	{
		return true;
	}
	//Main>
	
	
	
	
	
	
	
	
	
	
	//<URL
	/**
	 * This does NOT append the sessionId.  See {@link HttpServletResponse#encodeRedirectURL(String)}
	 */
	public String getURL(String basic, boolean ssl)
	{
		return getURL(basic, ssl, null);
	}
	
	public String getURL(String basic, boolean ssl, @Nullable String query)
	{
		//TODO this will probably need to be changed once there are dynamic paths and such!
		String path = rtrimstr(basic, "/");
		
		return new ReURL(ssl ? "https" : "http", null, null, ssl ? getDomainForHTTPSecure() : getDomainForHTTPInsecure(), ssl ? getPortForHTTPSecure() : getPortForHTTPInsecure(), path, query, null).toString();
	}
	
	
	
	/**
	 * Gets the domain name of the server we're running on (for http://).
	 */
	public String getDomainForHTTPInsecure()
	{
		return TemporaryManyWikiRoot.getDomainForHTTPInsecure();
	}
	
	/**
	 * Gets the domain name of the server we're running on (for https://).
	 */
	public String getDomainForHTTPSecure()
	{
		return TemporaryManyWikiRoot.getDomainForHTTPSecure();
	}
	
	
	/**
	 * Gets the domain name of the server we're running on (for http://).
	 */
	public int getPortForHTTPInsecure()
	{
		return TemporaryManyWikiRoot.getPortForHTTPInsecure();
	}
	
	/**
	 * Gets the domain name of the server we're running on (for https://).
	 */
	public int getPortForHTTPSecure()
	{
		return TemporaryManyWikiRoot.getPortForHTTPSecure();
	}
	//URL>
	
	
	
	
	
	
	
	//<View
	//<Utils
	/**
	 * Sets a variable to "checked = \"checked\"" based on <code>value</code>.<br>
	 */
	protected void setVariableForJSPViewCheckBox(String trueVarname, String falseVarname, boolean value)
	{
		if (value)
			setVariableForJSPView(trueVarname, "checked = \"checked\"");
		else
			setVariableForJSPView(falseVarname, "checked = \"checked\"");
	}
	
	/**
	 * Sets the variable to "checked = \"checked\"" or "" based on <code>value</code>.<br>
	 */
	protected void setVariableForJSPViewCheckBox(String varname, boolean value)
	{
		setVariableForJSPView(varname, value ? "checked = \"checked\"" : "");
	}
	
	/**
	 * Sets a variable to "selected = \"selected\"" based on <code>value</code>.<br>
	 */
	protected void setVariableForJSPViewSelected(String trueVarname, String falseVarname, boolean value)
	{
		if (value)
			setVariableForJSPView(trueVarname, "selected = \"selected\"");
		else
			setVariableForJSPView(falseVarname, "selected = \"selected\"");
	}
	
	/**
	 * Sets the variable to "selected = \"selected\"" or "" based on <code>value</code>.<br>
	 */
	protected void setVariableForJSPViewSelected(String varname, boolean value)
	{
		setVariableForJSPView(varname, value ? "selected = \"selected\"" : "");
	}
	//Utils>
	
	
	
	
	
	
	
	
	//<Resolutions
	
	//serveView() is the most common (non-failure) resolution!
	
	
	public boolean isRedirectsSecure()
	{
		//Current logic is: If FORCE_SECURE is on, then force it; otherwise perpetuate what the current session is using.
		//Currently, $action is not used
		//NOTE: If you change this, re-evaluate doAction() so it doesn't get caught in a FORCE_SECURE loop
		return getRequest().isSecure() || TemporaryManyWikiRoot.isForceSecure();
	}
	
	
	public void redirectSame() throws IOException
	{
		//TODO this will probably need to be changed once there are dynamic paths and such!
		String url = getURL(getRequest().getRequestURI(), isRedirectsSecure(), getRequest().getQueryString());
		url = getResponse().encodeRedirectURL(url);
		getResponse().sendRedirect(url);
	}
	
	/**
	 * Sends a redirect to the proper (and encoded) URL constructed from the given action (much like {@link ManyWikiActionBean#getURL(String, boolean) getURL()}.
	 */
	public void redirect(String basic) throws IOException
	{
		//TODO this will probably need to be changed once there are dynamic paths and such!
		String url = getURL(basic, isRedirectsSecure());
		url = getResponse().encodeRedirectURL(url);
		getResponse().sendRedirect(url);
	}
	
	/**
	 * Sends a redirect to the encoded URL provided.
	 */
	public void redirectAbsolute(String url) throws IOException
	{
		//TODO this will probably need to be changed once there are dynamic paths and such!
		url = getResponse().encodeRedirectURL(url); //Security) This must not append the sessionId if the url isn't ours (Tomcat meets this requirement)
		getResponse().sendRedirect(url);
	}
	
	
	/**
	 * Sends a redirect to the proper (and encoded) URL constructed from the given action (much like {@link ManyWikiActionBean#getURL(String, boolean) getURL()}.<br>
	 * This is different from {@link ManyWikiActionBean#redirect(String)} in that you can specify form data to include in the url, (action, name1, value1, name2, value2, nameN, valueN) which will automatically be URLEncoded.<br>
	 */
	public void redirect(String basic, String... form) throws IOException
	{
		StringBuilder queryBuilder = new StringBuilder();
		for (int i = 0; i+1 < form.length; i += 2)
		{
			queryBuilder.append(URLEncoder.encode(form[i], "UTF-8"));
			queryBuilder.append('=');
			queryBuilder.append(URLEncoder.encode(form[i+1], "UTF-8"));
			if (i+2 < form.length-1)
				queryBuilder.append('&');
		}
		
		String url = getURL(basic, isRedirectsSecure(), queryBuilder.toString());
		url = getResponse().encodeRedirectURL(url);
		
		if (isDebug())
			System.out.println("Redirecting to \""+url+"\"...");
		
		getResponse().sendRedirect(url);
	}
	
	
	
	
	public void sendError(int httpErrorCode) throws IOException
	{
		getResponse().sendError(httpErrorCode);
	}
	//Resolutions>
	
	
	
	
	
	//<Config
	@Override
	public boolean isDebug()
	{
		return TemporaryManyWikiRoot.isDebug();
	}
	
	@Override
	public boolean isBenchmarking()
	{
		return TemporaryManyWikiRoot.isDebug();
	}
	//Config>
	//View>
	
	
	
	
	//<Utils
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass)
	{
		return AngryReflectionUtility.isInheritedAnnotationPresent(getClass(), annotationClass);
	}
	
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass)
	{
		return AngryReflectionUtility.getInheritedAnnotation(getClass(), annotationClass);
	}
	//Utils>
}
