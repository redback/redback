package org.codehaus.redback.xmlrpc.client;

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

import java.net.URL;
import java.util.List;

import org.codehaus.redback.xmlrpc.bean.Resource;
import org.codehaus.redback.xmlrpc.service.ResourceService;

import com.atlassian.xmlrpc.ApacheBinder;
import com.atlassian.xmlrpc.Binder;
import com.atlassian.xmlrpc.ConnectionInfo;

public class ResourceServiceClient
    implements ResourceService, ServiceClient
{
    private ResourceService resourceService;

    public ResourceServiceClient()
    {
    }

    public ResourceServiceClient( String url )
        throws Exception
    {
        bind( url );
    }

    public ResourceServiceClient( String url, String username, String password )
        throws Exception
    {
        bind( url, username, password );
    }

    public void bind( String url )
        throws Exception
    {
        bind( url, "", "" );
    }

    public void bind( String url, String username, String password )
        throws Exception
    {
        Binder binder = new ApacheBinder();

        ConnectionInfo info = new ConnectionInfo();
        info.setUsername( username );
        info.setPassword( password );

        resourceService = binder.bind( ResourceService.class, new URL( url ), info );
    }

    public Boolean createResource( String identifier )
        throws Exception
    {
        return resourceService.createResource( identifier );
    }

    public Resource getResource( String identifier )
        throws Exception
    {
        return resourceService.getResource( identifier );
    }

    public List<Resource> getResources()
        throws Exception
    {
        return resourceService.getResources();
    }

    public Boolean removeResource( String identifier )
        throws Exception
    {
        return resourceService.removeResource( identifier );
    }

    public Boolean ping()
        throws Exception
    {
        return resourceService.ping();
    }
}
