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
package org.apache.wiki.api.core;

//////// The "ContentSelector"s here must match up with /WEB-INF/tags/templates/default/Content.tag !! ////////

public enum ContextEnum {

    GROUP_DELETE( "deleteGroup", "%uDeleteGroup.jsp?group=%n", null ),
    GROUP_EDIT( "editGroup", "%uEditGroup.jsp?group=%n", "ContentSelector_EditGroupContent" ),
    GROUP_VIEW( "viewGroup", "%uGroup.jsp?group=%n", "ContentSelector_GroupContent" ),

    PAGE_ATTACH( "att", "%uattach/%n", null ),
    PAGE_COMMENT( "comment", "%uComment.jsp?page=%n", "ContentSelector_CommentContent" ),
    PAGE_CONFLICT ( "conflict", "%uPageModified.jsp?page=%n", "ContentSelector_ConflictContent" ),
    PAGE_DELETE( "del", "%uDelete.jsp?page=%n", null ),
    PAGE_DIFF( "diff", "%uDiff.jsp?page=%n", "ContentSelector_DiffContent" ),
    PAGE_EDIT( "edit", "%uEdit.jsp?page=%n", "ContentSelector_EditContent" ),
    PAGE_INFO( "info", "%uPageInfo.jsp?page=%n", "ContentSelector_InfoContent" ),
    PAGE_NONE( "", "%u%n", null ),
    PAGE_PREVIEW( "preview", "%uPreview.jsp?page=%n", "ContentSelector_PreviewContent" ),
    PAGE_RENAME( "rename", "%uRename.jsp?page=%n", "ContentSelector_InfoContent" ),
    PAGE_RSS( "rss", "%urss.jsp", null ),
    PAGE_UPLOAD( "upload", "%uUpload.jsp?page=%n", null ),
    PAGE_VIEW( "view", "%uWiki.jsp?page=%n", "ContentSelector_PageContent" ),

    REDIRECT( "", "%u%n", null ),

    WIKI_ADMIN( "admin", "%uadmin/Admin.jsp", null ),
    WIKI_CREATE_GROUP( "createGroup", "%uNewGroup.jsp", "ContentSelector_NewGroupContent" ),
    WIKI_ERROR( "error", "%uError.jsp", null ),
    WIKI_FIND( "find", "%uSearch.jsp", "ContentSelector_FindContent" ),
    WIKI_INSTALL( "install", "%uInstall.jsp", null ),
    WIKI_LOGIN( "login", "%uLogin.jsp?redirect=%n", "ContentSelector_LoginContent" ),
    WIKI_LOGOUT( "logout", "%uLogout.jsp", null ),
    WIKI_MESSAGE( "message", "%uMessage.jsp", "ContentSelector_DisplayMessage" ),
    WIKI_PREFS( "prefs", "%uUserPreferences.jsp", "ContentSelector_PreferencesContent" ),
    WIKI_WORKFLOW( "workflow", "%uWorkflow.jsp", "ContentSelector_WorkflowContent" );

    private final String contentSelector;
    private final String requestContext;
    private final String urlPattern;

    ContextEnum( final String requestContext, final String urlPattern, final String contentSelector ) {
        this.requestContext = requestContext;
        this.urlPattern = urlPattern;
        this.contentSelector = contentSelector;
    }

    public String getRequestContext() {
        return requestContext;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public String getContentSelector() {
        return contentSelector;
    }
}
