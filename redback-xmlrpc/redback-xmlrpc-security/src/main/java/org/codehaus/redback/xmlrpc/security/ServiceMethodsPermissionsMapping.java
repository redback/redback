package org.codehaus.redback.xmlrpc.security;

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

public class ServiceMethodsPermissionsMapping
{
    public static final List<String> SERVICE_METHODS_FOR_USER = new ArrayList<String>()
    {
        {
            add( "UserService.getUser" );
            add( "UserService.getUsers" );
            add( "UserService.createUser" );
            add( "UserService.deleteUser" );
            add( "UserService.updateUser" );
        }
    };

    public static final List<String> SERVICE_METHODS_FOR_ROLE = new ArrayList<String>()
    {
        {
            add( "RoleService.createRole" );
            add( "RoleService.roleExists" );
            add( "RoleService.getRole" );
            add( "RoleService.getRoles" );
            add( "RoleService.addChildRole" );
            add( "RoleService.assignRole" );
            add( "RoleService.unassignRole" );
            add( "RoleService.assignRoleByName" );
            add( "RoleService.unassignRoleByName" );
        }
    };

    public static final List<String> SERVICE_METHODS_FOR_PERMISSION = new ArrayList<String>()
    {
        {
            add( "PermissionService.createPermission" );
            add( "PermissionService.removePermission" );
            add( "PermissionService.getPermission" );
            add( "PermissionService.getPermissions" );
        }
    };

    public static final List<String> SERVICE_METHODS_FOR_OPERATION = new ArrayList<String>()
    {
        {
            add( "OperationService.createOperation" );
            add( "OperationService.removeOperation" );
            add( "OperationService.getOperation" );
            add( "OperationService.getOperations" );
        }
    };

    public static final List<String> SERVICE_METHODS_FOR_RESOURCE = new ArrayList<String>()
    {
        {
            add( "ResourceService.createResource" );
            add( "ResourceService.removeResource" );
            add( "ResourceService.getResource" );
            add( "ResourceService.getResources" );
        }
    };
}
