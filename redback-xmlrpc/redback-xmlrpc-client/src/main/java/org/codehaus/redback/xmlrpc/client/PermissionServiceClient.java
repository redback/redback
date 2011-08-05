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
import java.util.TimeZone;

import org.codehaus.redback.xmlrpc.bean.Permission;
import org.codehaus.redback.xmlrpc.service.PermissionService;

import com.atlassian.xmlrpc.ApacheBinder;
import com.atlassian.xmlrpc.Binder;
import com.atlassian.xmlrpc.ConnectionInfo;

public class PermissionServiceClient
    implements PermissionService, ServiceClient
{
    private PermissionService permissionService;

    public PermissionServiceClient()
    {
    }

    public PermissionServiceClient( String url )
        throws Exception
    {
        bind( url );
    }

    public PermissionServiceClient( String url, String username, String password )
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
        info.setTimeZone( TimeZone.getDefault() );

        permissionService = binder.bind( PermissionService.class, new URL( url ), info );
    }

    public Boolean createPermission( String permissionName, String operationName, String resourceName )
        throws Exception
    {
        return permissionService.createPermission( permissionName, operationName, resourceName );
    }

    public Permission getPermission( String permissionName )
        throws Exception
    {
        return permissionService.getPermission( permissionName );
    }

    public List<Permission> getPermissions()
        throws Exception
    {
        return permissionService.getPermissions();
    }

    public Boolean removePermission( String permissionName )
        throws Exception
    {
        return permissionService.removePermission( permissionName );
    }

    public Boolean ping()
        throws Exception
    {
        return permissionService.ping();
    }

}
