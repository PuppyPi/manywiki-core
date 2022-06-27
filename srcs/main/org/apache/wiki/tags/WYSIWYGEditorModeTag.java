/* 
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.  
 */
package org.apache.wiki.tags;

import java.util.Properties;
import org.apache.commons.lang3.*;
import org.apache.wiki.api.core.*;
import org.apache.wiki.auth.*;
import org.apache.wiki.auth.permissions.*;
import org.apache.wiki.filters.*;
import org.apache.wiki.pages.PageManager;
import org.apache.wiki.parser.MarkupParser;
import org.apache.wiki.render.*;
import org.apache.wiki.ui.*;
import org.apache.wiki.util.TextUtil;
import org.apache.wiki.variables.VariableManager;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.core.Page;
import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.pages.PageManager;
import java.io.IOException;
import javax.servlet.jsp.JspException;

/**
 *  Includes the body in case there is no such page available.
 *
 *  @since 2.0
 */
public class WYSIWYGEditorModeTag extends WikiTagBase {

    private static final long serialVersionUID = 0l;

    
    protected String originalCCLOption;
    
    @Override
    public int doWikiStartTag() throws IOException, ProviderException {
    	
        Context context = Context.findContext( pageContext );
        Engine engine = context.getEngine();

        context.setVariable( Context.VAR_WYSIWYG_EDITOR_MODE, Boolean.TRUE );
        context.setVariable( VariableManager.VAR_RUNFILTERS,  "false" );

        Page wikiPage = context.getPage();
        originalCCLOption = (String)wikiPage.getAttribute( MarkupParser.PROP_CAMELCASELINKS );
        wikiPage.setAttribute( MarkupParser.PROP_CAMELCASELINKS, "false" );
    	
        return EVAL_BODY_INCLUDE;
    }
    
    @Override
    public int doEndTag() throws JspException
    {
        Context context = Context.findContext( pageContext );
        Engine engine = context.getEngine();
        Page wikiPage = context.getPage();

    	   // Disable the WYSIWYG_EDITOR_MODE and reset the other properties immediately
    	   // after the XHTML for the editor has been rendered.
    	   context.setVariable( Context.VAR_WYSIWYG_EDITOR_MODE, Boolean.FALSE );
    	   context.setVariable( VariableManager.VAR_RUNFILTERS,  null );
    	   wikiPage.setAttribute( MarkupParser.PROP_CAMELCASELINKS, originalCCLOption );

    	   /*not used
    	   String templateDir = (String)engine.getWikiProperties().get( Engine.PROP_TEMPLATEDIR );
    	   String protocol = "http://";
    	   if( request.isSecure() ) {
    	       protocol = "https://";
    	   }
    	   */
    	
           return EVAL_PAGE;
    }
}
