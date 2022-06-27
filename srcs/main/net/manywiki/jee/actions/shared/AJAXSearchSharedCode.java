package net.manywiki.jee.actions.shared;

import java.util.*;
import java.net.URLEncoder;
import org.apache.commons.lang3.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.api.search.SearchResult;
import org.apache.wiki.auth.*;
import org.apache.wiki.auth.permissions.*;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.search.SearchManager;
import org.apache.wiki.ui.*;
import javax.annotation.Nullable;
import javax.servlet.jsp.jstl.fmt.*;
import org.apache.wiki.api.core.*;
import org.apache.wiki.auth.*;
import javax.servlet.jsp.jstl.fmt.*;
import org.apache.wiki.api.core.*;
import org.apache.wiki.auth.*;
import org.apache.wiki.auth.permissions.*;
import org.apache.wiki.attachment.*;
import org.apache.wiki.i18n.InternationalizationManager;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.ui.progress.ProgressManager;
import org.apache.wiki.util.TextUtil;
import java.security.Permission;
import javax.servlet.jsp.jstl.fmt.*;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.wiki.WikiEngine;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class AJAXSearchSharedCode
{
	protected static final Logger log = LogManager.getLogger("JSPWikiSearch");

	
	public static int computeMaxItems(HttpServletRequest request, Collection<SearchResult> searchresults)
	{
		  int startitem = 0; // first item to show
		  int maxitems = 20; // number of items to show in result

		  String parm_start    = request.getParameter( "start");
		  if( parm_start != null ) startitem = Integer.parseInt( parm_start ) ;

		  if( startitem == -1 ) maxitems = searchresults.size(); //show all
		  
		  return maxitems;
	}
}
