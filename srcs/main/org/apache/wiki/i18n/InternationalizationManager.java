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
package org.apache.wiki.i18n;

import static rebound.text.StringUtilities.*;
import static rebound.util.collections.CollectionUtilities.*;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import rebound.annotations.semantic.AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse;
import rebound.annotations.semantic.meta.dependencies.DependencyDirectory;
import rebound.exceptions.ResourceException;

/**
 *  Manages all internationalization in JSPWiki.
 *
 *  @since 2.6
 */
@DependencyDirectory("./core")
@DependencyDirectory("./templates")
@DependencyDirectory("./plugin")
public interface InternationalizationManager {
	
	String pkg = InternationalizationManager.class.getPackage().getName();
	
	/** The name of the ResourceBundle which contains any and all JSPWiki core resource strings.  It's value is {@value}. */
	String CORE_BUNDLE = pkg+".core.CoreResources";
	
	/** The name of the ResourceBundle which contains any and all JSPWiki default templates resource strings.  It's value is {@value}. */
	String DEF_TEMPLATE = pkg+".templates.default";
	
	/** Name of the default plugin resource bundle. */
	public static final String CORE_PLUGINS_RESOURCEBUNDLE = pkg+".plugin.PluginResources";
	
	// public static final String JSPWIKI_BUNDLE = "jspwiki";
	// public static final String PLUGINS_BUNDLE = "plugins";
	
	/**
	 *  Returns a String from the CORE_BUNDLE using English as the default locale.
	 *
	 *  @param key Key to find
	 *  @return The English string
	 *  @throws MissingResourceException If there is no such key
	 */
	default String get( final String key ) throws MissingResourceException {
		return get( CORE_BUNDLE, Locale.ENGLISH, key );
	}
	
	/**
	 *  Finds a resource bundle.
	 *
	 *  @param bundle The ResourceBundle to find.  Must exist.
	 *  @param locale The Locale to use.  Set to null to get the default locale.
	 *  @return A localized string
	 *  @throws MissingResourceException If the key cannot be located at all, even from the default locale.
	 */
	default ResourceBundle getBundle( final String bundle, Locale locale ) throws MissingResourceException {
		if( locale == null ) {
			locale = Locale.getDefault();
		}
		
		return ResourceBundle.getBundle( bundle, locale );
	}
	
	/**
	 *  If you are too lazy to open your own bundle, use this method to get a string simply from a bundle.
	 *
	 *  @param bundle Which bundle the string is in
	 *  @param locale Locale to use - null for default
	 *  @param key    Which key to use.
	 *  @return A localized string (or from the default language, if not found)
	 *  @throws MissingResourceException If the key cannot be located at all, even from the default locale.
	 */
	default String get( final String bundle, final Locale locale, final String key ) throws MissingResourceException {
		try
		{
			return getBundle( bundle, locale ).getString( key );
		}
		catch (MissingResourceException exc)
		{
			try
			{
				return getBundle( DEF_TEMPLATE, locale ).getString( key );
			}
			catch (MissingResourceException exc1)
			{
				throw new ResourceException("Can't find resource for bundle "+repr(bundle)+" in locale "+repr(locale.toString())+" for key "+repr(key), exc);
			}
		}
	}
	
	/**
	 *  Obtain a parameterized String from the bundle.
	 *
	 *  @param bundle Which bundle the string is in
	 *  @param locale Locale to use - null for default
	 *  @param key    Which key to use.
	 *  @param args parameters to insert in the String.
	 *  @return A localized string (or from the default language, if not found)
	 *  @throws MissingResourceException If the key cannot be located at all, even from the default locale.
	 */
	default String get( final String bundle, final Locale locale, final String key, final Object... args ) throws MissingResourceException {
		final MessageFormat mf = new MessageFormat( get( bundle, locale, key ), locale );
		return mf.format( args );
	}
	
	
	
	/**
	 * @return keys are the language code, values are the descriptive name to show to the user
	 */
	@AccessedDynamicallyOrExternallyToJavaOrKnownToBeInImportantSerializedUse  //by JSP!
	public default Map<String, String> listLanguages()
	{
		return mapof("en/US", "English, United States");  //FIXME-PP FIXXXXXXxxxxxxxxxxthisssssssss!
	}
}
