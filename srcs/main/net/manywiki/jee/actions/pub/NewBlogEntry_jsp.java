package net.manywiki.jee.actions.pub;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.plugin.*;
import org.apache.wiki.plugin.plugins.WeblogEntryPlugin;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class NewBlogEntry_jsp
extends ManyWikiActionBean
{
    protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
	    // Create wiki context; no need to check for authorization since the redirect will take care of that
		ContextEnum cte = ContextEnum.PAGE_EDIT;
	    Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext(), getContext().getServletContext() );
	    String pagereq = wikiContext.getName();
	    
	    // Redirect if the request was for a 'special page'
	    String specialpage = engine.getSpecialPageReference( pagereq );
	    if( specialpage != null ) {
	        // FIXME: Do Something Else
	        response.sendRedirect( specialpage );
	        return;
	    }

	    WeblogEntryPlugin p = new WeblogEntryPlugin();
	    
	    String newEntry = p.getNewEntryPage( engine, pagereq );

	    // Redirect to a new page for user to edit
	    response.sendRedirect( wikiContext.getURL( ContextEnum.PAGE_EDIT.getRequestContext(), newEntry ) );
	}
}
