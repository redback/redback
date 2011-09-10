package org.codehaus.redback.xmlrpc.service;

/*
 * Copyright 2009 The Codehaus.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.List;

import org.codehaus.redback.xmlrpc.bean.Resource;

import com.atlassian.xmlrpc.ServiceObject;

@ServiceObject("ResourceService")
public interface ResourceService
    extends Service
{
    Boolean createResource( String identifier )
        throws Exception;

    Boolean removeResource( String identifier )
        throws Exception;

    Resource getResource( String identifier )
        throws Exception;

    List<Resource> getResources()
        throws Exception;
}
