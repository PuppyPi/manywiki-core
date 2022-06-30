package net.manywiki.jee.actions.pub;

import java.text.*;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.attachment.AttachmentManager;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.permissions.*;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.references.ReferenceManager;
import org.apache.wiki.rss.*;
import org.apache.wiki.util.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

//Todo-PP consider the security/privacy ramifications of this.

public class SisterSites_jsp
extends ManyWikiActionBean
{
    protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
	    /*
	     *  This page creates support for the SisterSites standard, as specified by
	     *  http://usemod.com/cgi-bin/mb.pl?SisterSitesImplementationGuide
	     */
	    // Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.PAGE_RSS;
	    Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext(), getActionBeanContext().getServletContext() );
	    if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
	    
	    Set< String > allPages = engine.getManager( ReferenceManager.class ).findCreated();
	    
	    response.setContentType("text/plain; charset=UTF-8");  //Todo-PP What if the stream's encoding isn't UTF-8??
	    
	    ServletOutputStream out = response.getOutputStream();
	    
	    for( String pageName : allPages ) {
	        // Let's not add attachments.
	        if( engine.getManager( AttachmentManager.class ).getAttachmentInfoName( wikiContext, pageName ) != null ) continue;

	        Page wikiPage = engine.getManager( PageManager.class ).getPage( pageName );
	        if( wikiPage != null ) { // there's a possibility the wiki page may get deleted between the call to reference manager and now...
	            PagePermission permission = PermissionFactory.getPagePermission( wikiPage, "view" );
	            boolean allowed = engine.getManager( AuthorizationManager.class ).checkPermission( wikiContext.getWikiSession(), permission );
	            if( allowed ) {
	                String url = wikiContext.getViewURL( pageName );
	                out.print( url + " " + pageName + "\n" );
	            }
	        }
	    }
	}
}
