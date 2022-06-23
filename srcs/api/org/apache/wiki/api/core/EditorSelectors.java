package org.apache.wiki.api.core;

import org.apache.wiki.ui.EditorManager;

////////The "EditorSelector"s here must match up with /WEB-INF/tags/templates/default/Editor.tag !! ////////

public class EditorSelectors
{
	@Deprecated
	public static String getSelector(String oldEditorName)
	{
		return "EditorSelector_"+oldEditorName;
	}
	
	public static String getActiveEditorsSelector(Engine engine, Context wikiContext)
	{
		final EditorManager mgr = engine.getManager(EditorManager.class);
		return getSelector(mgr.getEditorName(wikiContext));
	}
}
