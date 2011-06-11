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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.redback.xmlrpc.bean.Permission;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;

@Service
public class PermissionServiceImpl
    implements PermissionService
{
    private RBACManager rbacManager;

    @Inject
    public PermissionServiceImpl(@Named( value = "rBACManager#cached" ) RBACManager rbacManager )
    {
        this.rbacManager = rbacManager;
    }

    public Boolean createPermission( String permissionName, String operationName, String resourceName )
        throws Exception
    {
        rbacManager.savePermission( rbacManager.createPermission( permissionName, operationName, resourceName ) );
        return Boolean.TRUE;
    }

    public Boolean removePermission( String permissionName )
        throws Exception
    {
        rbacManager.removePermission( permissionName );
        return Boolean.TRUE;
    }

    public Permission getPermission( String permissionName )
        throws Exception
    {
        org.codehaus.plexus.redback.rbac.Permission permission = rbacManager.getPermission( permissionName );
        Permission simplePermission = new Permission( permission.getName(), permission.getDescription(), permission
            .getOperation().getName(), permission.getResource().getIdentifier() );

        return simplePermission;
    }

    public List<Permission> getPermissions()
        throws Exception
    {
        List<org.codehaus.plexus.redback.rbac.Permission> permissions = rbacManager.getAllPermissions();
        List<Permission> simplePermissions = new ArrayList<Permission>();

        for ( org.codehaus.plexus.redback.rbac.Permission permission : permissions )
        {
            simplePermissions.add( new Permission( permission.getName(), permission.getDescription(), permission
                .getOperation().getName(), permission.getResource().getIdentifier() ) );
        }

        return simplePermissions;
    }

    public Boolean ping()
        throws Exception
    {
        return Boolean.TRUE;
    }

}
