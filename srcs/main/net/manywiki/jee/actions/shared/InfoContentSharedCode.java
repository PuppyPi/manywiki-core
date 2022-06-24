package net.manywiki.jee.actions.shared;

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

public class InfoContentSharedCode
{
	public static void finish(ManyWikiActionBean bean, Context wikiContext) throws ServletException, IOException
	{
		HttpServletRequest request = bean.getRequest();
		HttpServletResponse response = bean.getResponse();
		WikiEngine engine = bean.getWikiEngine();
		
		
		
		
		
		InternationalizationManager i18n = engine.getManager(InternationalizationManager.class);
		
		  Page wikiPage = wikiContext.getPage();
		  int attCount = wikiContext.getEngine().getManager( AttachmentManager.class ).listAttachments( wikiContext.getPage() ).size();
		  //String attTitle = LocaleSupport.getLocalizedMessage(pageContext, "attach.tab");
		  String attTitle = i18n.get(InternationalizationManager.CORE_BUNDLE, Preferences.getLocale(wikiContext), "attach.tab");
		  if( attCount != 0 ) attTitle += " (" + attCount + ")";

		  String creationAuthor ="";

		  //FIXME -- seems not to work correctly for attachments !!
		  Page firstPage = wikiContext.getEngine().getManager( PageManager.class ).getPage( wikiPage.getName(), 1 );
		  if( firstPage != null )
		  {
		    creationAuthor = firstPage.getAuthor();

		    if( creationAuthor != null && creationAuthor.length() > 0 )
		    {
		      creationAuthor = TextUtil.replaceEntities(creationAuthor);
		    }
		    else
		    {
		      creationAuthor = Preferences.getBundle( wikiContext, InternationalizationManager.CORE_BUNDLE ).getString( "common.unknownauthor" );
		    }
		  }

		  int itemcount = 0;  //number of page versions
		  try
		  {
		    itemcount = wikiPage.getVersion(); /* highest version */
		  }
		  catch( Exception  e )  { /* dont care */ }

		  int pagesize = 20;
		  int startitem = itemcount-1; /* itemcount==1-20 -> startitem=0-19 ... */

		  String parm_start = (String)request.getParameter( "start" );
		  if( parm_start != null ) startitem = Integer.parseInt( parm_start ) ;

		  /* round to start of block: 0-19 becomes 0; 20-39 becomes 20 ... */
		  if( startitem > -1 ) startitem = ( startitem / pagesize ) * pagesize;

		  /* startitem drives the pagination logic */
		  /* startitem=-1:show all; startitem=0:show block 1-20; startitem=20:block 21-40 ... */
		
		
		
		
		bean.setVariableForJSPView("pageName", wikiPage.getName());
		bean.setVariableForJSPView("pageLastModified", wikiPage.getLastModified());
		bean.setVariableForJSPView("firstPageLastModified", firstPage.getLastModified());
		bean.setVariableForJSPView("creationAuthor", creationAuthor);
		bean.setVariableForJSPView("textErrorPrefix", i18n.get(InternationalizationManager.CORE_BUNDLE, Preferences.getLocale(wikiContext), "prefs.errorprefix.rename"));  //LocaleSupport.getLocalizedMessage(pageContext,"prefs.errorprefix.rename"));
		bean.setVariableForJSPView("deleteContext", ContextEnum.PAGE_DELETE.getRequestContext());
		bean.setVariableForJSPView("uploadContext", ContextEnum.PAGE_UPLOAD.getRequestContext());
		
		bean.setVariableForJSPView("startitem", startitem);
		bean.setVariableForJSPView("itemcount", itemcount);
		bean.setVariableForJSPView("pagesize", pagesize);
		
		bean.setVariableForJSPView("paginationURL", wikiContext.getURL(ContextEnum.PAGE_INFO.getRequestContext(), wikiPage.getName(), "start=%s"));
		bean.setVariableForJSPView("progressId", wikiContext.getEngine().getManager( ProgressManager.class ).getNewProgressIdentifier());
		
		
		
		
		// Set the content type and include the response content
		response.setContentType("text/html; charset=" + engine.getContentEncoding());
		bean.serveJSPView("/templates/default/view/InfoContent.jsp");
	}
}
