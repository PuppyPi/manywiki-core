package net.manywiki.jee.actions.pub;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.WikiEngine;
import org.apache.wiki.api.core.*;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.htmltowiki.HtmlStringToWikiTranslator;
import org.jdom2.JDOMException;
import java.io.IOException;
import javax.servlet.ServletException;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class XHRHtml2Markup_jsp
extends ManyWikiActionBean
{
	//protected static final Logger log = LogManager.getLogger("XHRHtml2Markup");
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		ContextEnum cte = ContextEnum.PAGE_VIEW;
		Context wikiContext = Wiki.context().create( engine, getRequest(), cte.getRequestContext(), getContext().getServletContext() );
		
		if( !engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, getResponse() ) ) return;
		
		getResponse().setContentType("text/plain; charset="+engine.getContentEncoding() );
		//response.setHeader( "Cache-control", "max-age=0" );
		//response.setDateHeader( "Expires", new Date().getTime() );
		//response.setDateHeader( "Last-Modified", new Date().getTime() );
		
		String htmlText = getRequest().getParameter( "htmlPageText" );
		
		if( htmlText != null )
		{
			try
			{
				getResponse().getOutputStream().print(new HtmlStringToWikiTranslator( engine ).translate( htmlText, wikiContext ));
			}
			catch (JDOMException exc)
			{
				throw new ServletException(exc);
			}
			catch (ReflectiveOperationException exc)
			{
				throw new ServletException(exc);
			}
		}
	}
}
