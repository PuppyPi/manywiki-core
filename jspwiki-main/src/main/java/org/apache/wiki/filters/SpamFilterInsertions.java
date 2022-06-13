package org.apache.wiki.filters;

import javax.annotation.Nullable;
import javax.servlet.jsp.PageContext;

public interface SpamFilterInsertions
{
    public @Nullable String getHTMLInsertion(PageContext pageContext);
}
