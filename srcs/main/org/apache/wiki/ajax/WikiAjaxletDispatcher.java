/*

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */
package org.apache.wiki.ajax;

import static rebound.text.StringUtilities.*;
import java.io.IOException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.permissions.PagePermission;

//Todo make not static and make the toplevel code-config register the things!

/**
 * This provides a simple ajax servlet for handling /ajax/<ClassName> requests.
 * {@link WikiAjaxlet} classes need to be registered using {@link WikiAjaxletDispatcher#registerAjaxlet(String, WikiAjaxlet)}
 *
 * @since 2.10.2-svn12
 */
public class WikiAjaxletDispatcher
{
	private static final Logger log = LogManager.getLogger(WikiAjaxletDispatcher.class.getName());
	
	private static final Map<String, AjaxServletContainer> ajaxServlets = new ConcurrentHashMap<>();
	
	
	
	
	
	
	
	/**
	 * The toplevel method for handling an ajax request!
	 * If the URI was invalid (not a supported request name after the prefix), then nothing is done (no {@link HttpServletResponse#sendError(int, String)}) and false is returned, otherwise it is fully handled (the response is given data) and true is returned.  :>
	 * 
	 * @param ajaxPrefix  usually "/ajax/"
	 */
	public static boolean dispatch(final Engine engine, String ajaxPrefix, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
	{
		final String uri = request.getRequestURI();
		
		final String ajaxletName = getAjaxletName(uri, ajaxPrefix);
		
		if (ajaxletName != null)
		{
			final AjaxServletContainer container = findAjaxletContainer(ajaxletName);
			
			if (container != null)
			{
				final WikiAjaxlet ajaxlet = container.ajaxlet;
				
				if (validatePermission(request, container))
				{
					request.setCharacterEncoding(engine.getContentEncoding().name());
					response.setCharacterEncoding(engine.getContentEncoding().name());
					
					final String actionName = getAjaxletAction(request.getRequestURI(), ajaxPrefix);
					log.debug("actionName=" + actionName);
					
					final String params = request.getParameter("params");
					log.debug("params=" + params);
					
					List<String> paramValues = new ArrayList<>();
					if (params != null && StringUtils.isNotBlank(params))
						paramValues = Arrays.asList(params.trim().split(","));
					
					ajaxlet.service(request, response, actionName, paramValues);
				}
				else
				{
					log.warn("Ajaxlet container " + container + " not authorised. Permission required.");
				}
				
				return true;
			}
			else
			{
				log.error("No registered class for ajaxletName=" + ajaxletName + " in uri=" + uri);
				return false;
			}
		}
		else
		{
			return false;  //it shouldn't even have been handed to us X3
		}
	}
	
	/**
	 * Validate the permission of the {@link WikiAjaxlet} using the {@link AuthorizationManager#checkPermission}
	 *
	 * @param req the servlet request
	 * @param container the container info of the servlet
	 * @return true if permission is valid
	 */
	private static boolean validatePermission(final HttpServletRequest req, final AjaxServletContainer container)
	{
		final Engine e = Wiki.engine().find(req.getSession().getServletContext(), null);
		
		boolean valid;
		
		if (container != null)
			valid = e.getManager(AuthorizationManager.class).checkPermission(Wiki.session().find(e, req), container.permission);
		else
			valid = false;
		
		return valid;
	}
	
	
	/**
	 * Get the AjaxletName from a requestURI like "/ajax/(AjaxletName)".
	 * 
	 * @param ajaxPrefix  usually "/ajax/"
	 * @return The AjaxletName for the requestURI, or null if none/syntaxerror
	 */
	public static @Nullable String getAjaxletName(final @Nonnull String uri, @Nonnull String ajaxPrefix)
	{
		String p = ltrimstrOrNull(uri, ajaxPrefix);
		return nullIfEmpty(removeQueryAndOrAnchorOrPassThroughIfNull(p == null ? null : splitonceReturnPrecedingOrWhole(p, '/')));
	}
	
	
	/**
	 * Get the AjaxletAction from a requestURI like "/ajax/name/(AjaxletAction)".
	 * 
	 * @param ajaxPrefix  usually "/ajax/"
	 * @return The AjaxletName for the requestURI, or null if none/syntaxerror
	 */
	public static @Nullable String getAjaxletAction(final @Nonnull String uri, @Nonnull String ajaxPrefix)
	{
		String p = ltrimstrOrNull(uri, ajaxPrefix);
		
		if (p == null)
			return null;
		
		String pp = splitonceReturnSucceedingOrNull(p, '/');
		
		return nullIfEmpty(removeQueryAndOrAnchorOrPassThroughIfNull(pp == null ? null : splitonceReturnPrecedingOrWhole(pp, '/')));
	}
	
	protected static @Nullable String removeQueryAndOrAnchorOrPassThroughIfNull(final @Nullable String uriFragment)
	{
		return uriFragment == null ? null : splitonceReturnPrecedingOrWhole(splitonceReturnPrecedingOrWhole(uriFragment, '?'), '#');
	}
	
	protected static @Nullable String nullIfEmpty(final @Nullable String s)
	{
		return s != null && s.isEmpty() ? null : s;
	}
	
	
	
	
	
	
	
	
	/**
	 * Register a {@link WikiAjaxlet} with a specific alias, and default permission {@link PagePermission#VIEW}.
	 */
	public static void registerAjaxlet(final String alias, final WikiAjaxlet servlet)
	{
		registerAjaxlet(alias, servlet, PagePermission.VIEW);
	}
	
	/**
	 * Regster a {@link WikiAjaxlet} given an alias, the servlet, and the permission.
	 *
	 * @param alias the uri link to this ajaxlet
	 * @param ajaxlet the servlet being registered
	 * @param perm the permission required to execute the servlet.
	 */
	public static void registerAjaxlet(final String alias, final WikiAjaxlet ajaxlet, final Permission perm)
	{
		log.info("WikiAjaxDispatcherServlet registering " + alias + "=" + ajaxlet + " perm=" + perm);
		ajaxServlets.put(alias, new AjaxServletContainer(alias, ajaxlet, perm));
	}
	
	
	
	
	
	/**
	 * Find the {@link AjaxServletContainer} as registered in {@link #registerServlet}.
	 *
	 * @param ajaxletName the name of the servlet from {@link #getServletName}
	 * @return The first servlet found, or null.
	 */
	private static AjaxServletContainer findAjaxletContainer(final String ajaxletName)
	{
		return ajaxServlets.get(ajaxletName);
	}
	
	
	/**
	 * Find the {@link WikiAjaxlet} given the servletAlias that it was registered with.
	 *
	 * @param ajaxletName the value provided to {@link #registerAjaxlet(String, WikiAjaxlet)}
	 * @return the {@link WikiAjaxlet} given the servletAlias that it was registered with.
	 */
	public static WikiAjaxlet findAjaxletByName(final String ajaxletName)
	{
		final AjaxServletContainer container = findAjaxletContainer(ajaxletName);
		return container != null ? container.ajaxlet : null;
	}
	
	
	
	
	
	private static class AjaxServletContainer
	{
		final String name;
		final WikiAjaxlet ajaxlet;
		final Permission permission;
		
		public AjaxServletContainer(final String name, final WikiAjaxlet ajaxlet, final Permission permission)
		{
			this.name = name;
			this.ajaxlet = ajaxlet;
			this.permission = permission;
		}
		
		@Override
		public String toString()
		{
			return getClass().getSimpleName() + "[" + name + " → " + ajaxlet.getClass().getSimpleName() + " with permission=" + permission + "]";
		}
	}
}
