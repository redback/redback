package org.codehaus.redback.xmlrpc.client;

/*
 * Copyright 2009 The Codehaus.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.net.URL;
import java.util.List;

import org.codehaus.redback.xmlrpc.bean.Role;
import org.codehaus.redback.xmlrpc.service.RoleService;

import com.atlassian.xmlrpc.AuthenticationInfo;
import com.atlassian.xmlrpc.Binder;
import com.atlassian.xmlrpc.DefaultBinder;

public class RoleServiceClient
    implements RoleService, ServiceClient
{
    private RoleService roleService;

    public RoleServiceClient()
    {
    }

    public RoleServiceClient( String url )
        throws Exception
    {
        bind( url );
    }

    public RoleServiceClient( String url, String username, String password )
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
        Binder binder = new DefaultBinder();

        roleService = binder.bind( RoleService.class, new URL( url ), new AuthenticationInfo( username, password ) );
    }

    public Boolean addChildRole( String roleName, String childRoleName )
        throws Exception
    {
        return roleService.addChildRole( roleName, childRoleName );
    }

    public Boolean createRole( String roleName )
        throws Exception
    {
        return roleService.createRole( roleName );
    }

    public Role getRole( String roleName )
        throws Exception
    {
        return roleService.getRole( roleName );
    }

    public List<Role> getRoles()
        throws Exception
    {
        return roleService.getRoles();
    }

    public Boolean ping()
        throws Exception
    {
        return roleService.ping();
    }

    public Boolean removeRole( String roleName )
        throws Exception
    {
        return roleService.removeRole( roleName );
    }

    public Boolean roleExists( String roleName )
        throws Exception
    {
        return roleService.roleExists( roleName );
    }

    public List<String> getChildRoles( String roleName )
        throws Exception
    {
        return roleService.getChildRoles( roleName );
    }

    public Boolean assignRole( String roleId, String username )
        throws Exception
    {
        return roleService.assignRole( roleId, username );
    }

    public Boolean assignRoleByName( String roleName, String username )
        throws Exception
    {
        return roleService.assignRoleByName( roleName, username );
    }

    public Boolean unassignRole( String roleId, String username )
        throws Exception
    {
        return roleService.unassignRole( roleId, username );
    }

    public Boolean unassignRoleByName( String roleName, String username )
        throws Exception
    {
        return roleService.unassignRoleByName( roleName, username );
    }
}
