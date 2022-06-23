package net.manywiki.jee.actions.pub;

import java.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.search.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.*;
import org.apache.wiki.auth.permissions.*;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.search.SearchManager;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.util.TextUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class Search_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWikiSearch");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		
		
		// Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.WIKI_FIND;
		Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext() );
		if(!engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response )) return;
		String pagereq = wikiContext.getName();
		
		// Get the search results
		Collection< SearchResult > list = null;
		String query = request.getParameter( "query");
		String go    = request.getParameter("go");
		
		if( query != null ) {
			log.info("Searching for string "+query);
			
			try {
				list = engine.getManager( SearchManager.class ).findPages( query, wikiContext );
				setVariableForJSPView( "searchresults", list );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??
			} catch( Exception e ) {
				wikiContext.getWikiSession().addMessage( e.getMessage() );
			}
			
			query = TextUtil.replaceEntities( query );
			
			setVariableForJSPView( "query", query );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??
			
			//
			//  Did the user click on "go"?
			//
			if( go != null ) {
				if( list != null && list.size() > 0 ) {
					SearchResult sr = list.iterator().next();
					Page wikiPage = sr.getPage();
					String url = wikiContext.getViewURL( wikiPage.getName() );
					response.sendRedirect( url );
					return;
				}
			}
		}
		
		// Set the content type and include the response content
		response.setContentType("text/html; charset="+engine.getContentEncoding() );
		
		//String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "ViewTemplate.jsp" );
		//%><wiki:Include page="<%=contentPage%>" /><%
	    setVariableForJSPView("contentSelector", cte.getContentSelector());
		serveJSPView("/templates/default/ViewTemplate.jsp");
		
		log.debug("SEARCH COMPLETE");
	}
}
