package org.codehaus.redback.xmlrpc.service;

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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.redback.xmlrpc.bean.Role;

public class RoleServiceImpl
    implements RoleService
{
    private RBACManager rbacManager;

    private RoleManager roleManager;

    public RoleServiceImpl( RBACManager rbacManager, RoleManager roleManager )
    {
        this.rbacManager = rbacManager;
        this.roleManager = roleManager;
    }

    public Boolean addChildRole( String roleName, String childRoleName )
        throws Exception
    {
        rbacManager.addChildRole( rbacManager.getRole( roleName ), rbacManager.getRole( childRoleName ) );
        return Boolean.TRUE;
    }

    public Boolean createRole( String roleName )
        throws Exception
    {
        rbacManager.saveRole( rbacManager.createRole( roleName ) );
        return Boolean.TRUE;
    }

    public Role getRole( String roleName )
        throws Exception
    {
        org.codehaus.plexus.redback.rbac.Role role = rbacManager.getRole( roleName );

        List<String> permissionNames = new ArrayList<String>();
        for ( org.codehaus.plexus.redback.rbac.Permission permission : role.getPermissions() )
        {
            permissionNames.add( permission.getName() );
        }

        Role simpleRole = new Role( role.getName(), role.getDescription(), role.isAssignable(), role.isPermanent() );

        return simpleRole;
    }

    public List<Role> getRoles()
        throws Exception
    {
        List<org.codehaus.plexus.redback.rbac.Role> roles = rbacManager.getAllRoles();
        List<Role> simpleRoles = new ArrayList<Role>();

        for ( org.codehaus.plexus.redback.rbac.Role role : roles )
        {
            List<String> permissionNames = new ArrayList<String>();
            for ( org.codehaus.plexus.redback.rbac.Permission permission : role.getPermissions() )
            {
                permissionNames.add( permission.getName() );
            }

            simpleRoles
                .add( new Role( role.getName(), role.getDescription(), role.isAssignable(), role.isPermanent() ) );
        }

        return simpleRoles;
    }

    public Boolean removeRole( String roleName )
        throws Exception
    {
        rbacManager.removeRole( roleName );
        return Boolean.TRUE;
    }

    public Boolean roleExists( String roleName )
        throws Exception
    {
        return rbacManager.roleExists( roleName );
    }

    public Boolean ping()
        throws Exception
    {
        return Boolean.TRUE;
    }

    public List<String> getChildRoles( String roleName )
        throws Exception
    {
        return rbacManager.getRole( roleName ).getChildRoleNames();
    }

    public Boolean assignRole( String roleName, String username )
        throws Exception
    {
        roleManager.assignRole( roleName, username );
        return Boolean.TRUE;
    }

    public Boolean unassignRole( String roleName, String username )
        throws Exception
    {
        roleManager.unassignRole( roleName, username );
        return Boolean.TRUE;
    }
}
