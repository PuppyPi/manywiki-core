package net.manywiki.jee.actions.other;

import java.io.IOException;
import javax.servlet.ServletException;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class DefaultAction
extends ManyWikiActionBean
{
	@Override
	protected void doValidAction() throws ServletException, IOException
	{
		sendError(404);
	}
}
