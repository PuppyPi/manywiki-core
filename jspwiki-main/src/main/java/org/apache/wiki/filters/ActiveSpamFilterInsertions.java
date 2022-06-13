package org.apache.wiki.filters;

import javax.annotation.Nullable;

//TODO make this not be static but configure the JSP tag with a SpamFilterInsertions instance field!

public class ActiveSpamFilterInsertions
{
	public static @Nullable SpamFilterInsertions active = null;
}
