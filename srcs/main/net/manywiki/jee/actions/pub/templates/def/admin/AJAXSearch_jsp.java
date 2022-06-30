package net.manywiki.jee.actions.pub.templates.def.admin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.search.SearchResult;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.auth.permissions.PagePermission;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.search.SearchManager;
import rebound.exceptions.UnreachableCodeError;
import rebound.util.BasicExceptionUtilities;
import rebound.util.ExceptionUtilities;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;
import net.manywiki.jee.actions.shared.AJAXSearchSharedCode;

//TODO-PP is this page actually used!!?!

public class AJAXSearch_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWikiSearch");
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
			
			  Context wikiContext = Wiki.context().create( engine, request, ContextEnum.WIKI_FIND.getRequestContext(), getActionBeanContext().getServletContext() );
			  if(!engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;  //TODO don't send 401?!?

			  String query = request.getParameter( "query");

			  if( (query != null) && ( !query.trim().equals("") ) )
			  {
			    try
			    {
			      Collection< SearchResult > list = engine.getManager( SearchManager.class ).findPages( query, wikiContext );

			      //  Filter down to only those that we actually have a permission to view
			      AuthorizationManager mgr = engine.getManager( AuthorizationManager.class );

			      ArrayList< SearchResult > searchresults = new ArrayList<>();

			      for( Iterator< SearchResult > i = list.iterator(); i.hasNext(); )
			      {
			        SearchResult r = i.next();

			        Page p = r.getPage();

			        PagePermission pp = new PagePermission( p, PagePermission.VIEW_ACTION );

			        try
			        {
			          if( mgr.checkPermission( wikiContext.getWikiSession(), pp ) )
			          {
			            searchresults.add( r );
			          }
			        }
			        catch( Exception e ) { log.error( "Searching for page "+p, e ); }
			      }

					setVariableForJSPView( "searchresults", searchresults );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??
					setVariableForJSPView("maxitems", AJAXSearchSharedCode.computeMaxItems(request, searchresults));
					  
					  response.setContentType("text/html; charset="+engine.getContentEncoding() );
					  serveJSPView();
			    }
			    catch( Exception e )
			    {
			    	//FIXME-PP proper and consistent error handling X'D
			       wikiContext.getWikiSession().addMessage( e.getMessage() );
			       BasicExceptionUtilities.rethrowSafe(e);
			       throw new UnreachableCodeError();
			    }
			  }
			  else
			  {
				  //TODO not send an error???
			       return;
			  }
	}
}
