package org.apache.wiki.filters;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.auth.user.UserProfile;

public interface SpamFilter
{
	public static final String HorribleHiddenVariableInHttpServletRequest = "lastchange";
	
	
	public boolean isValidUserProfile( final Context context, final UserProfile profile );
	
	/**
	 * @return the name of a {@link HttpServletRequest#getParameter(String) parameter} in the HTTP request and/or a server-internal {@link HttpSession#getAttribute(String) session attribute}!
	 */
	public @Nonnull String getHashFieldName( final HttpServletRequest request );
	
    public String getSpamHash( final Page page, final HttpServletRequest request );
    
	/**
	 * @return if it passes the check! false = failed, flagged as spam
	 */
    public boolean checkHash(final Context context, final HttpServletRequest request);
}
