package org.apache.wiki.ui;

import org.apache.wiki.api.core.Page;

public class WikiTaglibFunctions
{
	public static String formatBytes(long bytes)
	{
		return org.apache.commons.io.FileUtils.byteCountToDisplaySize(bytes);
	}
	
	public static String getChangeNote(Page page)
	{
		return (String)page.getAttribute( Page.CHANGENOTE );
	}
}
