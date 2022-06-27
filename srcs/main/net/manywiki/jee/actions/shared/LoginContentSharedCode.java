package net.manywiki.jee.actions.shared;

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

public class LoginContentSharedCode
{
	public static void finish(ManyWikiActionBean bean, Context wikiContext) throws ServletException, IOException
	{
		HttpServletRequest request = bean.getRequest();
		HttpServletResponse response = bean.getResponse();
		WikiEngine engine = bean.getWikiEngine();
		
		
		
		
	    AuthenticationManager mgr = wikiContext.getEngine().getManager( AuthenticationManager.class );
	    String loginURL = "";

	    if( mgr.isContainerAuthenticated() ) {
	        loginURL = "j_security_check";
	    } else {
	        String redir = (String)wikiContext.getVariable("redirect");
	        if( redir == null ) redir = wikiContext.getEngine().getFrontPage();
	        loginURL = wikiContext.getURL( ContextEnum.WIKI_LOGIN.getRequestContext(), redir );
	    }
		
	    
	    
	    bean.setVariableForJSPView("allowsCookieAuthentication", mgr.allowsCookieAuthentication());
	    bean.setVariableForJSPView("loginURL", loginURL);
		bean.setVariableForJSPView("wikiPageContext", wikiContext);
		
		
		
		// Set the content type and include the response content
		response.setContentType("text/html; charset=" + engine.getContentEncoding());
		bean.serveJSPView("/templates/default/view/LoginContent.jsp");
	}
}
