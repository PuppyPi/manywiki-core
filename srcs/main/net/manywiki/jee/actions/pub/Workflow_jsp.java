package net.manywiki.jee.actions.pub;

import java.util.Collection;
import java.util.Iterator;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.ContextEnum;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Session;
import org.apache.wiki.api.spi.Wiki;
import org.apache.wiki.auth.AuthorizationManager;
import org.apache.wiki.preferences.Preferences;
import org.apache.wiki.ui.TemplateManager;
import org.apache.wiki.workflow.Decision;
import org.apache.wiki.workflow.DecisionQueue;
import org.apache.wiki.workflow.NoSuchOutcomeException;
import org.apache.wiki.workflow.Outcome;
import org.apache.wiki.workflow.Workflow;
import org.apache.wiki.workflow.WorkflowManager;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.manywiki.jee.actions.ManyWikiActionBean;

public class Workflow_jsp
extends ManyWikiActionBean
{
    protected static final Logger log = LogManager.getLogger("JSPWiki");
	
	
	@Override
	protected void doLogic() throws ServletException, IOException
	{
		HttpServletRequest request = getRequest();
		HttpServletResponse response = getResponse();
		
	    // Create wiki context and check for authorization
		ContextEnum cte = ContextEnum.WIKI_WORKFLOW;
	    Context wikiContext = Wiki.context().create( engine, request, cte.getRequestContext(), getActionBeanContext().getServletContext() );
	    if(!engine.getManager( AuthorizationManager.class ).hasAccess( wikiContext, response )) return;
	    
	    // Extract the wiki session
	    Session wikiSession = wikiContext.getWikiSession();
	    
	    // Get the current decisions
	    DecisionQueue dq = engine.getManager( WorkflowManager.class ).getDecisionQueue();

	    if( "decide".equals(request.getParameter("action")) )
	    {
	        try
	        {
	          // Extract parameters for decision ID & decision outcome
	          int id = Integer.parseInt( request.getParameter( "id" ) );
	          String outcomeKey = request.getParameter("outcome");
	          Outcome outcome = Outcome.forName( outcomeKey );
	          // Iterate through our actor decisions and see if we can find an ID match
	          Collection< Decision > decisions = dq.getActorDecisions(wikiSession);
	          for (Iterator< Decision > it = decisions.iterator(); it.hasNext();) {
	            Decision d = it.next();
	            if( d.getId() == id ) {
	              // Cool, we found it. Now make the decision.
	              dq.decide( d, outcome, wikiContext );
	            }
	          }
	        } catch ( NumberFormatException e ) {
	           log.warn("Could not parse integer from parameter 'decision'. Somebody is being naughty.");
	        } catch ( NoSuchOutcomeException e ) {
	           log.warn("Could not look up Outcome from parameter 'outcome'. Somebody is being naughty.");
	        }
	    }
	    if( "abort".equals(request.getParameter("action")) )
	    {
	        try
	        {
	          // Extract parameters for decision ID & decision outcome
	          int id = Integer.parseInt( request.getParameter( "id" ) );
	          // Iterate through our owner decisions and see if we can find an ID match
	          Collection< Workflow > workflows = engine.getManager( WorkflowManager.class ).getOwnerWorkflows(wikiSession);
	          for (Iterator< Workflow > it = workflows.iterator(); it.hasNext();)
	          {
	            Workflow w = it.next();
	            if (w.getId() == id)
	            {
	              // Cool, we found it. Now kill the workflow.
	              w.abort( wikiContext );
	            }
	          }
	        }
	        catch ( NumberFormatException e )
	        {
	           log.warn("Could not parse integer from parameter 'decision'. Somebody is being naughty.");
	        }
	    }
	    
	    // Stash the current decisions/workflows
	    request.setAttribute("decisions",   dq.getActorDecisions(wikiSession));
	    request.setAttribute("workflows",   engine.getManager( WorkflowManager.class ).getOwnerWorkflows( wikiSession ) );
	    request.setAttribute("wikiSession", wikiSession);
		setVariableForJSPView("wikiPageContext", wikiContext);
	    
		
		
	    response.setContentType("text/html; charset="+engine.getContentEncoding() );
        serveJSPView("/templates/default/view/WorkflowContent.jsp");
	}
}
