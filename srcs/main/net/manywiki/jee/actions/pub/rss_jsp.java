package net.manywiki.jee.actions.pub;

import java.text.*;
import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.WatchDog;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.cache.CachingManager;
import org.apache.wiki.plugin.plugins.WeblogPlugin;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.rss.*;
import org.apache.wiki.util.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class rss_jsp
extends ManyWikiActionBean
{
    protected static final Logger log = LogManager.getLogger("JSPWiki");
    
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		CachingManager cacheManager = engine.getManager( CachingManager.class );
		
		ServletOutputStream out = getResponse().getOutputStream();
		
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		
		
	    // Create wiki context and check for authorization
	    Context wikiContext = Wiki.context().create( engine, request, ContextEnum.PAGE_RSS.getRequestContext() );
	    if(!engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
	    Page wikipage = wikiContext.getPage();

	    // Redirect if RSS generation not on
	    if( engine.getManager( RSSGenerator.class ) == null ) {
	        response.sendError( 404, "RSS feeds are disabled at administrator request" );
	        return;
	    }

	    if( wikipage == null || !engine.getManager( PageManager.class ).wikiPageExists( wikipage.getName() ) ) {
	        response.sendError( 404, "No such page " + wikipage.getName() );
	        return;
	    }

	    WatchDog w = WatchDog.getCurrentWatchDog( engine );
	    w.enterState("Generating RSS",60);
	    
	    // Set the mode and type for the feed
	    String      mode        = request.getParameter("mode");
	    String      type        = request.getParameter("type");
	    
	    if( mode == null || !(mode.equals(RSSGenerator.MODE_BLOG) || mode.equals(RSSGenerator.MODE_WIKI)) ) {
	    	   mode = RSSGenerator.MODE_BLOG;
	    }
	    if( type == null || !(type.equals(RSSGenerator.RSS10) || type.equals(RSSGenerator.RSS20) || type.equals(RSSGenerator.ATOM ) ) ) {
	    	   type = RSSGenerator.RSS20;
	    }
	    // Set the content type and include the response content
	    response.setContentType( RSSGenerator.getContentType(type)+"; charset=UTF-8");

	    StringBuffer result = new StringBuffer();
	    SimpleDateFormat iso8601fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	    Properties properties = engine.getWikiProperties();
	    String channelDescription = TextUtil.getRequiredProperty( properties, RSSGenerator.PROP_CHANNEL_DESCRIPTION );
	    String channelLanguage    = TextUtil.getRequiredProperty( properties, RSSGenerator.PROP_CHANNEL_LANGUAGE );

	    //
	    //  Now, list items.
	    //
	    List< Page > changed;
	    
	    if( "blog".equals( mode ) ) {
	        WeblogPlugin plug = new WeblogPlugin();
	        changed = plug.findBlogEntries( engine, wikipage.getName(), new Date(0L), new Date() );
	    } else {
	        changed = engine.getManager( PageManager.class ).getVersionHistory( wikipage.getName() );
	    }
	    
	    //
	    //  Check if nothing has changed, so we can just return a 304
	    //
	    boolean hasChanged = false;
	    Date    latest     = new Date(0);

	    for( Iterator< Page > i = changed.iterator(); i.hasNext(); ) {
	        Page p = i.next();

	        if( !HttpUtil.checkFor304( request, p.getName(), p.getLastModified() ) ) hasChanged = true;
	        if( p.getLastModified().after( latest ) ) latest = p.getLastModified();
	    }

	    if( !hasChanged && changed.size() > 0 ) {
	        response.sendError( HttpServletResponse.SC_NOT_MODIFIED );
	        w.exitState();
	        return;
	    }

	    response.addDateHeader("Last-Modified",latest.getTime());
	    response.addHeader("ETag", HttpUtil.createETag( wikipage.getName(), wikipage.getLastModified() ) );
	    
	    //
	    //  Try to get the RSS XML from the cache.  We build the hashkey
	    //  based on the LastModified-date, so whenever it changes, so does
	    //  the hashkey so we don't have to make any special modifications.
	    //
	    //  TODO: Figure out if it would be a good idea to use a disk-based cache here.
	    //
	    String hashKey = wikipage.getName()+";"+mode+";"+type+";"+latest.getTime();
	    
	    final String mode_ = mode;
	    final String type_ = type;
	    CheckedSupplier<String, RuntimeException> supplier = () -> engine.getManager( RSSGenerator.class ).generateFeed( wikiContext, changed, mode_, type_ );
	    
	    String rss = cacheManager == null ? supplier.get() : cacheManager.get(CachingManager.CACHE_RSS, hashKey, supplier);
	    
	    out.println( rss );
	    
	    w.exitState(); 
	}
}
