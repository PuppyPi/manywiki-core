package org.apache.wiki.modules;

import java.util.Collection;
import rebound.annotations.semantic.meta.dependencies.DependencyClass;
import rebound.annotations.semantic.meta.dependencies.DependencyFile;

@DependencyFile("./modules.xml")

//Dependencies from the above XML file! x'D
//@DependencyClass(org.apache.wiki.filters.SpamFilter.class)
@DependencyClass(org.apache.wiki.plugin.IfPlugin.class)
@DependencyClass(org.apache.wiki.plugin.Note.class)
@DependencyClass(org.apache.wiki.ui.admin.beans.PlainEditorAdminBean.class)
//@DependencyClass(org.apache.wiki.ui.admin.beans.WikiWizardAdminBean.class)  //The heck is this?! XD

public interface ModuleManager {

    /** Location of the property-files of plugins. (Each plugin should include this property-file in its jar-file) */
    String PLUGIN_RESOURCE_LOCATION = "modules.xml";

    /**
     *  Returns true, if the given module is compatible with this version of JSPWiki.
     *
     *  @param info The module to check
     *  @return True, if the module is compatible.
     */
    boolean checkCompatibility( WikiModuleInfo info );

    /**
     * Returns the {@link WikiModuleInfo} information about the provided moduleName.
     *
     * @param moduleName
     * @return The wikiModuleInfo
     */
    WikiModuleInfo getModuleInfo( String moduleName );

    /**
     * Returns a collection of modules currently managed by this ModuleManager.  Each
     * entry is an instance of the WikiModuleInfo class.  This method should return something
     * which is safe to iterate over, even if the underlying collection changes.
     *
     * @return A Collection of WikiModuleInfo instances.
     */
    Collection< WikiModuleInfo > modules();

}
