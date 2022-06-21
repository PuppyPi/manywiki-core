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
package org.apache.wiki.plugin.plugins;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wiki.ajax.WikiAjaxlet;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.exceptions.PluginException;
import org.apache.wiki.api.plugin.Plugin;

/**
 * @since 2.10.2-svn10
 */
public class SampleAjaxPlugin
implements Plugin, WikiAjaxlet
{
	public static final String AJAXLET_NAME = SampleAjaxPlugin.class.getSimpleName();
	
	@Override
	public String execute(final Context context, final Map<String, String> params) throws PluginException
	{
		final String id = Integer.toString(this.hashCode());
		return "<div onclick='Wiki.ajaxHtmlCall(\"/" + AJAXLET_NAME + "/ajaxAction\",[12,45],\"result" + id + "\",\"Loading...\")' style='color: blue; cursor: pointer'>Press Me</div>\n" + "<div id='result" + id + "'></div>";
	}
	
	@Override
	public void service(final HttpServletRequest request, final HttpServletResponse response, final String actionName, final List<String> params) throws IOException
	{
		try
		{
			Thread.sleep(5000); // Wait 5 seconds
		}
		catch (final Exception e)
		{
		}
		
		response.getWriter().print("You called! actionName=" + actionName + " params=" + params);
	}
}
