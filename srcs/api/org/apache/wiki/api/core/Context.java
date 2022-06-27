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
package org.apache.wiki.api.core;

import static java.util.Collections.*;
import static rebound.util.collections.CollectionUtilities.*;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.apache.wiki.api.providers.WikiProvider;
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.GroupPrincipal;
import org.apache.wiki.auth.UserManager;
import org.apache.wiki.auth.authorize.Group;
import org.apache.wiki.auth.authorize.GroupManager;
import org.apache.wiki.auth.permissions.GroupPermission;
import org.apache.wiki.auth.permissions.PagePermission;
import org.apache.wiki.auth.user.UserProfile;
import org.apache.wiki.i18n.InternationalizationManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.render.RenderingManager;
import org.apache.wiki.ui.EditorManager;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.util.comparators.PrincipalComparator;
import rebound.annotations.semantic.AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse;


/**
 *  <p>Provides state information throughout the processing of a page.  A Context is born when the JSP pages that are the main entry
 *  points, are invoked.  The JSPWiki engine creates the new Context, which basically holds information about the page, the
 *  handling engine, and in which context (view, edit, etc) the call was done.</p>
 *  <p>A Context also provides request-specific variables, which can be used to communicate between plugins on the same page, or
 *  between different instances of the same plugin. A Context variable is valid until the processing of the WikiPage has ended. For
 *  an example, please see the Counter plugin.</p>
 *  <p>When a Context is created, it automatically associates a {@link Session} object with the user's
 *  HttpSession. The Session contains information about the user's authentication status, and is consulted by {@link #getCurrentUser()}
 *  object.</p>
 *  <p>Do not cache the WikiPage object that you get from the WikiContext; always use getPage()!  TODO-PP across page processings right? it's fine within the processing of a single page hit right?</p>
 *
 *  @see {@code org.apache.wiki.plugin.Counter}
 */
public interface Context extends Cloneable, Command {
	
	String ATTR_CONTEXT = "jspwiki.context";
	
	/**
	 *  Variable name which tells whether plugins should be executed or not. Value can be either {@code Boolean.TRUE} or
	 *  {@code Boolean.FALSE}. While not set its value is {@code null}.
	 */
	String VAR_EXECUTE_PLUGINS = "_PluginContent.execute";
	
	/** Name of the variable which is set to Boolean.TRUE or Boolean.FALSE depending on whether WYSIWYG is currently in effect. */
	String VAR_WYSIWYG_EDITOR_MODE = "WYSIWYG_EDITOR_MODE";
	
	/**
	 *  Returns the WikiPage that is being handled.
	 *
	 *  @return the WikiPage which was fetched.
	 */
	Page getPage();
	
	/**
	 *  Sets the WikiPage that is being handled.
	 *
	 *  @param wikiPage The wikipage
	 *  @since 2.1.37.
	 */
	void setPage( Page wikiPage );
	
	/**
	 *  Gets a reference to the real WikiPage whose content is currently being rendered. If your plugin e.g. does some variable setting, be
	 *  aware that if it is embedded in the LeftMenu or some other WikiPage added with InsertPageTag, you should consider what you want to
	 *  do - do you wish to really reference the "master" WikiPage or the included page.
	 *  <p>
	 *  For example, in the default template, there is a WikiPage called "LeftMenu". Whenever you access a page, e.g. "Main", the master
	 *  WikiPage will be Main, and that's what the getPage() will return - regardless of whether your plugin resides on the LeftMenu or on
	 *  the Main page.  However, getRealPage() will return "LeftMenu".
	 *
	 *  @return A reference to the real page.
	 *  @see {@code org.apache.wiki.tags.InsertPageTag}
	 *  @see {@code org.apache.wiki.parser.JSPWikiMarkupParser}
	 */
	Page getRealPage();
	
	/**
	 *  Sets a reference to the real WikiPage whose content is currently being rendered.
	 *  <p>
	 *  Sometimes you may want to render the WikiPage using some other page's context. In those cases, it is highly recommended that you set
	 *  the setRealPage() to point at the real WikiPage you are rendering.  Please see InsertPageTag for an example.
	 *  <p>
	 *  Also, if your plugin e.g. does some variable setting, be aware that if it is embedded in the LeftMenu or some other WikiPage added
	 *  with InsertPageTag, you should consider what you want to do - do you wish to really reference the "master" WikiPage or the included
	 *  page.
	 *
	 *  @param wikiPage  The real WikiPage which is being rendered.
	 *  @return The previous real page
	 *  @since 2.3.14
	 *  @see {@code org.apache.wiki.tags.InsertPageTag}
	 */
	Page setRealPage( Page wikiPage );
	
	/**
	 *  Returns the handling engine.
	 *
	 *  @return The wikiengine owning this context.
	 */
	Engine getEngine();
	
	/**
	 *  Sets the request context.  See above for the different request contexts (VIEW, EDIT, etc.)
	 *
	 *  @param context The request context (one of the predefined contexts.)
	 */
	void setRequestContext( String context );
	
	/**
	 *  Gets a previously set variable.
	 *
	 *  @param key The variable name.
	 *  @return The variable contents.
	 */
	< T > T getVariable( String key );
	
	/**
	 *  Sets a variable.  The variable is valid while the WikiContext is valid, i.e. while WikiPage processing continues.  The variable data
	 *  is discarded once the WikiPage processing is finished.
	 *
	 *  @param key The variable name.
	 *  @param data The variable value.
	 */
	void setVariable( String key, Object data );  //FIXME-PP NOPE!!  NONE OF THIS!!  X"D
	
	/**
	 * This is just a simple helper method which will first check the context if there is already an override in place, and if there is not,
	 * it will then check the given properties.
	 *
	 * @param key What key are we searching for?
	 * @param defValue Default value for the boolean
	 * @return {@code true} or {@code false}.
	 */
	boolean getBooleanWikiProperty( String key, boolean defValue );
	
	/**
	 *  This method will safely return any HTTP parameters that might have been defined.  You should use this method instead
	 *  of peeking directly into the result of getHttpRequest(), since this method is smart enough to do all the right things,
	 *  figure out UTF-8 encoded parameters, etc.
	 *
	 *  @since 2.0.13.
	 *  @param paramName Parameter name to look for.
	 *  @return HTTP parameter, or null, if no such parameter existed.
	 */
	String getHttpParameter( String paramName );
	
	/**
	 *  If the request did originate from an HTTP request, then the HTTP request can be fetched here.  However, if the request
	 *  did NOT originate from an HTTP request, then this method will return null, and YOU SHOULD CHECK FOR IT!
	 *
	 *  @return Null, if no HTTP request was done.
	 *  @since 2.0.13.
	 */
	HttpServletRequest getHttpRequest();
	
	/**
	 *  Sets the template to be used for this request.
	 *
	 *  @param dir The template name
	 *  @since 2.1.15.
	 */
	void setTemplate( String dir );
	
	/**
	 *  Gets the template that is to be used throughout this request.
	 *
	 *  @since 2.1.15.
	 *  @return template name
	 */
	String getTemplate();
	
	/**
	 *  Returns the Session associated with the context. This method is guaranteed to always return a valid Session.
	 *  If this context was constructed without an associated HttpServletRequest, it will return a guest session.
	 *
	 *  @return The Session associate with this context.
	 */
	Session getWikiSession();
	
	/**
	 *  Convenience method that gets the current user. Delegates the lookup to the Session associated with this Context.
	 *  May return null, in case the current user has not yet been determined; or this is an internal system. If the Session has not
	 *  been set, <em>always</em> returns null.
	 *
	 *  @return The current user; or maybe null in case of internal calls.
	 */
	Principal getCurrentUser();
	
	/**
	 *  Returns true, if the current user has administrative permissions (i.e. the omnipotent AllPermission).
	 *
	 *  @since 2.4.46
	 *  @return true, if the user has all permissions.
	 */
	boolean hasAdminPermissions();
	
	/**
	 *  A shortcut to generate a VIEW url.
	 *
	 *  @param WikiPage The WikiPage to which to link.
	 *  @return A URL to the page.  This honours the current absolute/relative setting.
	 */
	String getViewURL( String WikiPage );
	
	/**
	 *  Figure out to which WikiPage we are really going to.  Considers special WikiPage names from the jspwiki.properties, and possible aliases.
	 *
	 *  @return A complete URL to the new WikiPage to redirect to
	 *  @since 2.2
	 */
	String getRedirectURL();
	
	/**
	 * Returns the Command associated with this Context.
	 *
	 * @return the command
	 */
	Command getCommand();
	
	/**
	 *  Creates a URL for the given request context.
	 *
	 *  @param context e.g. WikiContext.EDIT
	 *  @param page The WikiPage to which to link
	 *  @return A URL to the page.
	 */
	default String getURL( final String context, final String page ) {
		return getURL( context, page, null );
	}
	
	/**
	 *  Returns a URL from a page. It this Context instance was constructed with an actual HttpServletRequest, we will attempt to
	 *  construct the URL using HttpUtil, which preserves the HTTPS portion if it was used.
	 *
	 *  @param context The request context (e.g. WikiContext.UPLOAD)
	 *  @param page The WikiPage to which to link
	 *  @param params A list of parameters, separated with "&amp;"
	 *
	 *  @return A URL to the given context and page.
	 */
	default String getURL( final String context, final String page, final String params ) {
		// FIXME: is rather slow
		return getEngine().getURL( context, page, params );
	}
	
	/** {@inheritDoc} */
	Context clone();
	
	/**
	 *  Creates a deep clone of the Context. This is useful when you want to be sure that you don't accidentally mess with page
	 *  attributes, etc.
	 *
	 *  @since  2.8.0
	 *  @return A deep clone of the Context.
	 */
	Context deepClone();
	
	/**
	 *  This method can be used to find the WikiContext programmatically from a JSP PageContext. We check the request context.
	 *  The wiki context, if it exists, is looked up using the key {@link #ATTR_CONTEXT}.
	 *
	 *  @since 2.4
	 *  @param pageContext the JSP page context
	 *  @return Current WikiContext, or null, of no context exists.
	 */
	static Context findContext( final PageContext pageContext ) {
		final HttpServletRequest request = ( HttpServletRequest )pageContext.getRequest();
		return findContext(request);
	}
	
	static Context findContext( final HttpServletRequest request ) {
		return ( Context )request.getAttribute( ATTR_CONTEXT );
	}
	
	
	
	
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default String getEncodedName()
	{
		return this.getEngine().encodeName(this.getName());
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default String getWikiJsonURL()
	{
		return this.getURL( ContextEnum.PAGE_NONE.getRequestContext(), "ajax" );
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default List<Page> getVersionHistoryOfCurrentPage()
	{
		return getEngine().getManager( PageManager.class ).getVersionHistory(this.getPage().getName());
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default Page getLatestVersionOfCurrentPage()
	{
		return this.getEngine().getManager( PageManager.class ).getPage( this.getPage().getName(), WikiProvider.LATEST_VERSION );
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default String getTextOfCurrentPage()
	{
		return this.getEngine().getManager( PageManager.class ).getText( this.getPage() );
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default int getWordCountOfCurrentPage()
	{
		return new StringTokenizer(getTextOfCurrentPage()).countTokens();
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default int getNumberOfAttachmentsOnCurrentPage()
	{
		return this.getEngine().getManager( AttachmentManager.class ).listAttachments( this.getPage() ).size();
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default UserProfile getCurrentUserProfile()
	{
		/* dateformatting not yet supported by wiki:UserProfile tag - diy */
		UserManager manager = this.getEngine().getManager( UserManager.class );
		UserProfile profile = manager.getUserProfile( this.getWikiSession() );
		return profile;
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default List<Cookie> getCookies()
	{
		return asList(getRequest().getCookies());
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default String isWeblog()
	{
		return ( String )this.getPage().getAttribute( /*TODO ATTR_ISWEBLOG*/ "weblogplugin.isweblog" );
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default List<GroupPrincipal> getGroupPrincipals()
	{
		// Extract the group name and members
		//String name = request.getParameter( "group" );
		//Group group = (Group)pageContext.getAttribute( "Group",PageContext.REQUEST_SCOPE );
		
		Engine engine = this.getEngine();
		
		GroupManager groupMgr = engine.getManager( GroupManager.class );
		
		Principal[] groups = groupMgr.getRoles();
		Arrays.sort( groups, new PrincipalComparator() );
		
		return filterToListSubtyped(GroupPrincipal.class, asList(groups));
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default boolean checkPermission(String name, String actions)
	{
		Engine engine = this.getEngine();
		AuthorizationManager authMgr = engine.getManager( AuthorizationManager.class );
		return authMgr.checkPermission( this.getWikiSession(), new GroupPermission( name, actions ) );
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default Group getGroupByName(String name)
	{
		Engine engine = this.getEngine();
		return engine.getManager( GroupManager.class ).getGroup(name);
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default List<Principal> getGroupMembers(Group group)
	{
		Principal[] members = group.members();
		Arrays.sort( members, new PrincipalComparator() );
		return asList(members);
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default String getEditURLOfActivePage()
	{
		return this.getURL( ContextEnum.PAGE_EDIT.getRequestContext(), this.getName() );
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default String getCommentURLOfActivePage()
	{
		return this.getURL( ContextEnum.PAGE_COMMENT.getRequestContext(), this.getName() );
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default String getEditedTextOfActivePage()
	{
		//return (String)pageContext.getAttribute( EditorManager.ATTR_EDITEDTEXT, PageContext.REQUEST_SCOPE );
		//return EditorManager.getEditedText(pageContext);
		return EditorManager.getEditedText(getRequest());
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default String getPureTextOfActivePage()
	{
		return getEngine().getManager( PageManager.class ).getPureText( this.getPage() );
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default String getNoPageUsertextForEditor()
	{
		HttpServletRequest request = getRequest();
		Engine engine = getEngine();
		
		  String clone = request.getParameter( "clone" );
		  if( clone != null )
		  {
		    Page p = engine.getManager( PageManager.class ).getPage( clone );
		    if( p != null )
		    {
		        AuthorizationManager mgr = engine.getManager( AuthorizationManager.class );
		        PagePermission pp = new PagePermission( p, PagePermission.VIEW_ACTION );

		        try
		        {
		          if( mgr.checkPermission( this.getWikiSession(), pp ) )
		          {
		            return engine.getManager( PageManager.class ).getPureText( p );
		          }
		        }
		        catch( Exception e ) {  /*log.error( "Accessing clone page "+clone, e );*/ }
		    }
		  }
		  
		  return null;
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default String renderPageAsHTML(String usertext)
	{
	    try
	    {
	        return getEngine().getManager( RenderingManager.class ).getHTML( this, usertext );
	    }
	        catch( Exception e )
	    {
	        return "<div class='error'>Error in converting wiki-markup to well-formed HTML <br/>" + e.toString() +  "</div>";

	        /*
	        java.io.StringWriter sw = new java.io.StringWriter();
	        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
	        e.printStackTrace(pw);
	        pageAsHtml += "<pre>" + sw.toString() + "</pre>";
	        */
	    }
	}
	
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default Set<String> listSkins()
	{
		//TemplateManager t = getEngine().getManager( TemplateManager.class );
		//return t.listSkins(getServletContext(), this.getTemplate() );
		return emptySet();  //Todo-PP the whole themes ("templates/skins") system is going to be totally redone anyway X3
	}
	
	/**
	 * @return keys are the language code, values are the descriptive name to show to the user
	 */
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default Map<String, String> listLanguages()
	{
		InternationalizationManager im = getEngine().getManager( InternationalizationManager.class );
		return im.listLanguages();
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default Map<String, String> listTimeZones()
	{
		TemplateManager t = getEngine().getManager( TemplateManager.class );
		return t.listTimeZones(getEngine());
	}
	
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default Map<String, String> listTimeFormats()
	{
		TemplateManager t = getEngine().getManager( TemplateManager.class );
		return t.listTimeFormats(this);
	}
	
	
	
	//public ServletContext getServletContext();
	public HttpServletRequest getRequest();
	//public HttpServletResponse getResponse();
}
