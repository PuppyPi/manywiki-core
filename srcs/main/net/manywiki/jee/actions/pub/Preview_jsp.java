package net.manywiki.jee.actions.pub;

import java.util.Date;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.filters.*;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.ui.EditorManager;
import org.apache.wiki.ui.TemplateManager;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.manywiki.jee.TemporaryManyWikiRoot;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class Preview_jsp
extends ManyWikiActionBean
{
	protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
		// Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.PAGE_PREVIEW;
		Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext() );
		if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response ) ) return;
		if( wikiContext.getCommand().getTarget() == null ) {
			response.sendRedirect( wikiContext.getURL( wikiContext.getRequestContext(), wikiContext.getName() ) );
			return;
		}
		String pagereq = wikiContext.getName();
		
		HttpSession session = request.getSession();
		
		setVariableForJSPView( EditorManager.ATTR_EDITEDTEXT, session.getAttribute( EditorManager.REQ_EDITEDTEXT ) );  //Todo is it a problem that we don't specify the scope as PageContext.REQUEST_SCOPE anymore??
		
		SpamFilter spamFilter = TemporaryManyWikiRoot.getSpamFilter();
		if (spamFilter != null)
		{
			String lastchange = spamFilter.getSpamHash( wikiContext.getPage(), request );
			getRequest().setAttribute( SpamFilter.HorribleHiddenVariableInHttpServletRequest, lastchange );
		}
		
		// Set the content type and include the response content
		response.setContentType("text/html; charset="+engine.getContentEncoding() );
		
		//String contentPage = engine.getManager( TemplateManager.class ).findJSP( pageContext, wikiContext.getTemplate(), "ViewTemplate.jsp" );
		//%><wiki:Include page="<%=contentPage%>" />
		serveJSPView("/templates/default/ViewTemplate.jsp");
	}
}
