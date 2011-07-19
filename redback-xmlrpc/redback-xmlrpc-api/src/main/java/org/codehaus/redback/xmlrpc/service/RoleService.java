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

import java.util.List;

import org.codehaus.redback.xmlrpc.bean.Role;

import com.atlassian.xmlrpc.ServiceObject;

@ServiceObject("RoleService")
public interface RoleService
    extends Service
{
    Boolean createRole( String roleName )
        throws Exception;

    Boolean roleExists( String roleName )
        throws Exception;

    Role getRole( String roleName )
        throws Exception;

    List<Role> getRoles()
        throws Exception;

    Boolean removeRole( String roleName )
        throws Exception;

    Boolean addChildRole( String roleName, String childRoleName )
        throws Exception;

    List<String> getChildRoles( String roleName )
        throws Exception;

    Boolean assignRole( String roleId, String username )
        throws Exception;

    Boolean assignRoleByName( String roleName, String username )
        throws Exception;

    Boolean unassignRole( String roleId, String username )
        throws Exception;

    Boolean unassignRoleByName( String roleName, String username )
        throws Exception;
}
