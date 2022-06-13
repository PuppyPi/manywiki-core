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


import org.apache.wiki.filters.ActiveSpamFilterInsertions;
import org.apache.wiki.filters.SpamFilterInsertions;

/**
 * Provides hidden input fields which are checked by the {@code SpamFilter}.
 *
 * @since 2.11.0-M8
 */
public class SpamFilterInputsTag extends WikiTagBase {

	protected SpamFilterInsertions insertions = ActiveSpamFilterInsertions.active;
	
    /**
     * {@inheritDoc}
     */
    @Override
    public int doWikiStartTag() throws Exception {
    	String html = insertions == null ? null : insertions.getHTMLInsertion(pageContext);
    	if (html != null)
    		pageContext.getOut().print( html );
        return SKIP_BODY;
    }

}
