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
package org.apache.wiki.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import rebound.annotations.semantic.meta.dependencies.DependencyClass;
import rebound.annotations.semantic.meta.dependencies.DependencyFile;

//TODO PPMOD obliterate this with Java-based "configuration" XD

@DependencyFile("./classmappings.xml")

//Dependencies for above file!
@DependencyClass(org.apache.wiki.attachment.AttachmentManager.class)
@DependencyClass(org.apache.wiki.attachment.DefaultAttachmentManager.class)
@DependencyClass(org.apache.wiki.auth.AuthenticationManager.class)
@DependencyClass(org.apache.wiki.auth.AuthorizationManager.class)
@DependencyClass(org.apache.wiki.auth.DefaultAuthenticationManager.class)
@DependencyClass(org.apache.wiki.auth.DefaultAuthorizationManager.class)
@DependencyClass(org.apache.wiki.auth.DefaultUserManager.class)
@DependencyClass(org.apache.wiki.auth.UserManager.class)
@DependencyClass(org.apache.wiki.auth.acl.AclManager.class)
@DependencyClass(org.apache.wiki.auth.acl.DefaultAclManager.class)
@DependencyClass(org.apache.wiki.auth.authorize.DefaultGroupManager.class)
@DependencyClass(org.apache.wiki.auth.authorize.GroupManager.class)
@DependencyClass(org.apache.wiki.cache.CachingManager.class)
//@DependencyClass(org.apache.wiki.cache.EhcacheCachingManager.class)
@DependencyClass(org.apache.wiki.cache.SimpleInMemoryCachingManager.class)
@DependencyClass(org.apache.wiki.content.DefaultPageRenamer.class)
@DependencyClass(org.apache.wiki.content.PageRenamer.class)
@DependencyClass(org.apache.wiki.diff.DefaultDifferenceManager.class)
@DependencyClass(org.apache.wiki.diff.DifferenceManager.class)
@DependencyClass(org.apache.wiki.filters.DefaultFilterManager.class)
@DependencyClass(org.apache.wiki.filters.FilterManager.class)
@DependencyClass(org.apache.wiki.i18n.DefaultInternationalizationManager.class)
@DependencyClass(org.apache.wiki.i18n.InternationalizationManager.class)
@DependencyClass(org.apache.wiki.pages.DefaultPageManager.class)
@DependencyClass(org.apache.wiki.pages.PageManager.class)
@DependencyClass(org.apache.wiki.plugin.DefaultPluginManager.class)
@DependencyClass(org.apache.wiki.plugin.PluginManager.class)
@DependencyClass(org.apache.wiki.references.DefaultReferenceManager.class)
@DependencyClass(org.apache.wiki.references.ReferenceManager.class)
//@DependencyClass(org.apache.wiki.render.DefaultRenderingManager.class)  //TODO restore this in the "proper" configuration system that's done at the toplevel superproject!
@DependencyClass(org.apache.wiki.render.RenderingManager.class)
@DependencyClass(org.apache.wiki.rss.DefaultRSSGenerator.class)
@DependencyClass(org.apache.wiki.rss.RSSGenerator.class)
@DependencyClass(org.apache.wiki.search.DefaultSearchManager.class)
@DependencyClass(org.apache.wiki.search.SearchManager.class)
@DependencyClass(org.apache.wiki.tasks.DefaultTasksManager.class)
@DependencyClass(org.apache.wiki.tasks.TasksManager.class)
@DependencyClass(org.apache.wiki.ui.CommandResolver.class)
@DependencyClass(org.apache.wiki.ui.DefaultCommandResolver.class)
@DependencyClass(org.apache.wiki.ui.DefaultEditorManager.class)
@DependencyClass(org.apache.wiki.ui.DefaultTemplateManager.class)
@DependencyClass(org.apache.wiki.ui.EditorManager.class)
@DependencyClass(org.apache.wiki.ui.TemplateManager.class)
@DependencyClass(org.apache.wiki.ui.admin.AdminBeanManager.class)
@DependencyClass(org.apache.wiki.ui.admin.DefaultAdminBeanManager.class)
@DependencyClass(org.apache.wiki.ui.progress.DefaultProgressManager.class)
@DependencyClass(org.apache.wiki.ui.progress.ProgressManager.class)
@DependencyClass(org.apache.wiki.variables.DefaultVariableManager.class)
@DependencyClass(org.apache.wiki.variables.VariableManager.class)
@DependencyClass(org.apache.wiki.workflow.DefaultWorkflowManager.class)
@DependencyClass(org.apache.wiki.workflow.WorkflowManager.class)



/**
 * Contains useful utilities for class file manipulation. This is a static class, so there is no need to instantiate it.
 *
 * @since 2.1.29.
 */
public final class ClassUtil {

    private static final Logger log = LogManager.getLogger(ClassUtil.class);

    /** The location of the classmappings.xml document. It will be searched for in the classpath. Its value is "{@value}". */
    public  static final String MAPPINGS = "org/apache/wiki/util/classmappings.xml";

    /** Initialize the class mappings document. */
    private static final Map< String, String > c_classMappings = populateClassMappingsFrom( MAPPINGS );

    private static boolean classLoaderSetup;
    private static ClassLoader loader;

    private static Map< String, String > populateClassMappingsFrom( final String fileLoc ) {
        final Map< String, String > map = new ConcurrentHashMap<>();
        final List< Element > nodes = XmlUtil.parse( fileLoc, "/classmappings/mapping" );

        if( !nodes.isEmpty() ) {
            for( final Element f : nodes ) {
                final String key = f.getChildText( "requestedClass" );
                final String className = f.getChildText( "mappedClass" );

                map.put( key, className );
                log.debug( "Mapped class '{}' to class '{}'", key, className );
            }
        } else {
            log.info( "Didn't find class mapping document in {}", MAPPINGS );
        }
        return map;
    }

    /**
     * Private constructor to prevent direct instantiation.
     */
    private ClassUtil() {}
    
    /**
     *  Attempts to find a class from a collection of packages.  This will first
     *  attempt to find the class based on just the className parameter, but
     *  should that fail, will iterate through the "packages" -list, prefixes
     *  the package name to the className, and then tries to find the class
     *  again.
     *
     * @param packages A List of Strings, containing different package names.
     *  @param className The name of the class to find.
     * @return The class, if it was found.
     *  @throws ClassNotFoundException if this particular class cannot be found from the list.
     */
    @SuppressWarnings( "unchecked" )
    public static < T > Class< T > findClass( final List< String > packages,  final List< String > externaljars, final String className ) throws ClassNotFoundException {
        if (!classLoaderSetup) {
            loader = setupClassLoader(externaljars);
        }

        try {
            return ( Class< T > )loader.loadClass( className );
        } catch( final ClassNotFoundException e ) {
            for( final String packageName : packages ) {
                try {
                    return ( Class< T > )loader.loadClass( packageName + "." + className );
                } catch( final ClassNotFoundException ex ) {
                    // This is okay, we go to the next package.
                }
            }

        }

        throw new ClassNotFoundException( "Class '" + className + "' not found in search path!" );
    }

    /**
     * Set up the plugin classloader, checking if there are external JARS to add.
     * 
     * @param externaljars external jars to load into the classloader.
     * @return the classloader that can load classes from the configured external jars or, if not specified, the classloader that loaded this class.
     */
    private static ClassLoader setupClassLoader( final List< String > externaljars) {
        classLoaderSetup = true;
        log.info( "setting up classloaders for external (plugin) jars" );
        if( externaljars.size() == 0 ) {
            log.info( "no external jars configured, using standard classloading" );
            return ClassUtil.class.getClassLoader();
        }
        final URL[] urls = new URL[externaljars.size()];
        int i = 0;
        for( final String externaljar : externaljars ) {
            try {
                final File jarFile = new File( externaljar );
                final URL ucl = jarFile.toURI().toURL();
                urls[ i++ ] = ucl;
                log.info( "added {} to list of external jars", ucl );
            } catch( final MalformedURLException e ) {
                log.error( "exception ({}) while setting up classloaders for external jar: {}, continuing without external jars.", e.getMessage(), externaljar );
            }
        }
        
        if( i == 0 ) {
            log.error( "all external jars threw an exception while setting up classloaders for them, continuing with standard classloading. " + 
                       "See https://jspwiki-wiki.apache.org/Wiki.jsp?page=InstallingPlugins for help on how to install custom plugins." );
            return ClassUtil.class.getClassLoader();
        }
        
        return new URLClassLoader(urls, ClassUtil.class.getClassLoader());
    }

    /**
     * It will first attempt to instantiate the class directly from the className, and will then try to prefix it with the packageName.
     *
     * @param packageName A package name (such as "org.apache.wiki.plugins").
     * @param className The class name to find.
     * @return The class, if it was found.
     * @throws ClassNotFoundException if this particular class cannot be found.
     */
    @SuppressWarnings( "unchecked" )
    public static < T > Class< T > findClass( final String packageName, final String className ) throws ClassNotFoundException {
        try {
            return ( Class< T > )ClassUtil.class.getClassLoader().loadClass( className );
        } catch( final ClassNotFoundException e ) {
            return ( Class< T > )ClassUtil.class.getClassLoader().loadClass( packageName + "." + className );
        }
    }
    
    /**
     * Lists all the files in classpath under a given package.
     * 
     * @param rootPackage the base package. Can be {code null}.
     * @return all files entries in classpath under the given package
     */
    public static List< String > classpathEntriesUnder( final String rootPackage ) {
        final List< String > results = new ArrayList<>();
        Enumeration< URL > en = null;
        if( StringUtils.isNotEmpty( rootPackage ) ) {
            try {
                en = ClassUtil.class.getClassLoader().getResources( rootPackage );
            } catch( final IOException e ) {
                log.error( e.getMessage(), e );
            }
        }
        
        while( en != null && en.hasMoreElements() ) {
            final URL url = en.nextElement();
            try {
                if( "jar".equals( url.getProtocol() ) ) {
                    jarEntriesUnder( results, ( JarURLConnection )url.openConnection(), rootPackage );
                } else if( "file".equals( url.getProtocol() ) ) {
                    fileEntriesUnder( results, new File( url.getFile() ), rootPackage );
                }
                
            } catch( final IOException ioe ) {
                log.error( ioe.getMessage(), ioe );
            }
        }
        return results;
    }
    
    /**
     * Searchs for all the files in classpath under a given package, for a given {@link File}. If the 
     * {@link File} is a directory all files inside it are stored, otherwise the {@link File} itself is
     * stored
     * 
     * @param results collection in which the found entries are stored
     * @param file given {@link File} to search in.
     * @param rootPackage base package.
     */
    static void fileEntriesUnder( final List< String > results, final File file, final String rootPackage ) {
        log.debug( "scanning [{}]", file.getName() );
        if( file.isDirectory() ) {
            final Iterator< File > files = FileUtils.iterateFiles( file, null, true );
            while( files.hasNext() ) {
                final File subfile = files.next();
                // store an entry similar to the jarSearch(..) below ones
                final String entry = StringUtils.replace( subfile.getAbsolutePath(), file.getAbsolutePath() + File.separatorChar, StringUtils.EMPTY );
                results.add( rootPackage + "/" + entry );
            }
        } else {
            results.add( file.getName() );
        }
    }
    
    /**
     * Searchs for all the files in classpath under a given package, for a given {@link JarURLConnection}.
     * 
     * @param results collection in which the found entries are stored
     * @param jurlcon given {@link JarURLConnection} to search in.
     * @param rootPackage base package.
     */
    static void jarEntriesUnder( final List< String > results, final JarURLConnection jurlcon, final String rootPackage ) {
        try( final JarFile jar = jurlcon.getJarFile() ) {
            log.debug( "scanning [{}]", jar.getName() );
            final Enumeration< JarEntry > entries = jar.entries();
            while( entries.hasMoreElements() ) {
                final JarEntry entry = entries.nextElement();
                if( entry.getName().startsWith( rootPackage ) && !entry.isDirectory() ) {
                    results.add( entry.getName() );
                }
            }
        } catch( final IOException ioe ) {
            log.error( ioe.getMessage(), ioe );
        }
    }
    
    /**
     *  This method is used to locate and instantiate a mapped class.
     *  You may redefine anything in the resource file which is located in your classpath
     *  under the name <code>ClassUtil.MAPPINGS ({@value #MAPPINGS})</code>.
     *  <p>
     *  This is an extremely powerful system, which allows you to remap many of
     *  the JSPWiki core classes to your own class.  Please read the documentation
     *  included in the default <code>{@value #MAPPINGS}</code> file to see
     *  how this method works. 
     *  
     *  @param requestedClass The name of the class you wish to instantiate.
     *  @return An instantiated Object.
     *  @throws IllegalArgumentException If the class cannot be found or instantiated. 
     *  @throws ReflectiveOperationException If the class cannot be found or instantiated.
     *  @since 2.5.40
     */
    public static < T > T getMappedObject( final String requestedClass ) throws ReflectiveOperationException, IllegalArgumentException {
        final Object[] initargs = {};
        return getMappedObject( requestedClass, initargs );
    }

    /**
     *  This method is used to locate and instantiate a mapped class.
     *  You may redefine anything in the resource file which is located in your classpath
     *  under the name <code>{@value #MAPPINGS}</code>.
     *  <p>
     *  This is an extremely powerful system, which allows you to remap many of
     *  the JSPWiki core classes to your own class.  Please read the documentation
     *  included in the default <code>{@value #MAPPINGS}</code> file to see
     *  how this method works. 
     *  <p>
     *  This method takes in an object array for the constructor arguments for classes
     *  which have more than two constructors.
     *  
     *  @param requestedClass The name of the class you wish to instantiate.
     *  @param initargs The parameters to be passed to the constructor. May be <code>null</code>.
     *  @return An instantiated Object.
     *  @throws IllegalArgumentException If the class cannot be found or instantiated. 
     *  @throws ReflectiveOperationException If the class cannot be found or instantiated.
     *  @since 2.5.40
     */
    @SuppressWarnings( "unchecked" )
    public static < T > T getMappedObject( final String requestedClass, final Object... initargs ) throws ReflectiveOperationException, IllegalArgumentException {
        final Class< ? > cl = getMappedClass( requestedClass );
        return ( T )buildInstance( cl, initargs );
    }

    /**
     *  Finds a mapped class from the c_classMappings list.  If there is no mappped class, will use the requestedClass.
     *  
     *  @param requestedClass requested class.
     *  @return A Class object which you can then instantiate.
     *  @throws ClassNotFoundException if the class is not found.
     */
    public static Class< ? > getMappedClass( final String requestedClass ) throws ClassNotFoundException {
        String mappedClass = c_classMappings.get( requestedClass );
        if( mappedClass == null ) {
            mappedClass = requestedClass;
        }
        
        return Class.forName( mappedClass );
    }
    
    /**
     * checks if {@code srcClassName} is a subclass of {@code parentClassname}.
     * 
     * @param srcClassName expected subclass.
     * @param parentClassName expected parent class.
     * @return {@code true} if {@code srcClassName} is a subclass of {@code parentClassname}, {@code false} otherwise.
     */
    public static boolean assignable( final String srcClassName, final String parentClassName ) {
        try {
            final Class< ? > src = Class.forName( srcClassName );
            final Class< ? > parent = Class.forName( parentClassName );
            return parent.isAssignableFrom( src );
        } catch( final Exception e ) {
            log.error( e.getMessage(), e );
        }
        return false;
    }

    /**
     * Checks if a given class exists in classpath.
     *
     * @param className the class to check for existence.
     * @return {@code true} if it exists, {@code false} otherwise.
     */
    public static boolean exists( final String className ) {
        try {
            Class.forName( className, false, ClassUtil.class.getClassLoader() );
            return true;
        } catch( final ClassNotFoundException e ) {
            return false;
        }
    }

    /**
     * This method is used to instantiate a given class.
     *
     * @param className The name of the class you wish to instantiate.
     * @return An instantiated Object.
     * @throws ReflectiveOperationException If the class cannot be found or instantiated.
     * @since 2.11.1
     */
    public static < T > T buildInstance( final String className ) throws ReflectiveOperationException {
        return buildInstance( "", className );
    }

    /**
     * This method is used to instantiate a given class.
     * <p>
     * * It will first attempt to instantiate the class directly from the className, and will then try to prefix it with the packageName.
     *
     * @param packageName A package name (such as "org.apache.wiki.plugins").
     * @param className The class name to find.
     * @return An instantiated Object.
     * @throws ReflectiveOperationException If the class cannot be found or instantiated.
     * @since 2.11.1
     */
    public static < T > T buildInstance( final String packageName, final String className ) throws ReflectiveOperationException {
        return buildInstance( findClass( packageName, className ) );
    }

    /**
     * This method is used to instantiate a given class.
     *
     * @param from The name of the class you wish to instantiate.
     * @return An instantiated Object.
     * @throws ReflectiveOperationException If the class cannot be found or instantiated.
     * @since 2.11.1
     */
    public static < T > T buildInstance( final Class< T > from ) throws ReflectiveOperationException {
        final Object[] initArgs = {};
        return buildInstance( from, initArgs );
    }

    /**
     * This method is used to instantiate a given class.
     * <p>
     * This method takes in an object array for the constructor arguments for classes
     * which have more than two constructors.
     *
     * @param from The name of the class you wish to instantiate.
     * @param initArgs The parameters to be passed to the constructor. May be <code>null</code>.
     * @return An instantiated Object.
     * @throws ReflectiveOperationException If the class cannot be found or instantiated.
     * @since 2.11.1
     */
    @SuppressWarnings( "unchecked" )
    public static < T > T buildInstance( final Class< T > from, final Object... initArgs ) throws ReflectiveOperationException {
        final Constructor< ? >[] ctors = from.getConstructors();

        //  Try to find the proper constructor by comparing the initargs array classes and the constructor types.
        for( final Constructor< ? > ctor : ctors ) {
            final Class< ? >[] params = ctor.getParameterTypes();
            if( params.length == initArgs.length ) {
                for( int arg = 0; arg < initArgs.length; arg++ ) {
                    if( params[ arg ].isAssignableFrom( initArgs[ arg ].getClass() ) ) {
                        //  Ha, found it!  Instantiating and returning...
                        return ( T )ctor.newInstance( initArgs );
                    }
                }
            }
        }
        //  No arguments, so we can just call a default constructor and ignore the arguments.
        return from.getDeclaredConstructor().newInstance();
    }
    
}
