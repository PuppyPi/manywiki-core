package net.manywiki.jee.actions.other.closet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.ContextEnum;
import org.apache.wiki.api.spi.Wiki;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class CodeFromReaderTemplate
extends ManyWikiActionBean
{
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		HttpSession session = request.getSession();
		
	    // Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.PAGE_COMMENT;
	    Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext(), getContext().getServletContext() );
	    
	    // ...
	    
		setVariableForJSPView("wikiJsonUrl", wikiContext.getURL( ContextEnum.PAGE_NONE.getRequestContext(), "ajax" ));
		setVariableForJSPView("wikiPageContext", wikiContext);
		
        serveJSPView("/templates/reader/ViewTemplate.jsp");
	}
}
