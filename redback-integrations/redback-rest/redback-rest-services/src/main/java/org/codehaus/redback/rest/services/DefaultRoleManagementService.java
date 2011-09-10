package org.codehaus.redback.rest.services;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.redback.rest.api.services.RedbackServiceException;
import org.codehaus.redback.rest.api.services.RoleManagementService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * @author Olivier Lamy
 * @since 1.3
 */
@Service( "roleManagementService#rest" )
public class DefaultRoleManagementService
    implements RoleManagementService
{
    private RoleManager roleManager;

    @Inject
    public DefaultRoleManagementService( RoleManager roleManager )
    {
        this.roleManager = roleManager;
    }

    public Boolean createTemplatedRole( String templateId, String resource )
        throws RedbackServiceException
    {
        try
        {
            roleManager.createTemplatedRole( templateId, resource );
        }
        catch ( RoleManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
        return Boolean.TRUE;
    }

    public Boolean removeTemplatedRole( String templateId, String resource )
        throws RedbackServiceException
    {

        try
        {
            roleManager.removeTemplatedRole( templateId, resource );
        }
        catch ( RoleManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
        return Boolean.TRUE;
    }

    public Boolean updateRole( String templateId, String oldResource, String newResource )
        throws RedbackServiceException
    {
        try
        {
            roleManager.updateRole( templateId, oldResource, newResource );
        }
        catch ( RoleManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
        return Boolean.TRUE;
    }

    public Boolean assignRole( String roleId, String principal )
        throws RedbackServiceException
    {
        try
        {
            roleManager.assignRole( roleId, principal );
        }
        catch ( RoleManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
        return Boolean.TRUE;
    }

    public Boolean assignRoleByName( String roleName, String principal )
        throws RedbackServiceException
    {
        try
        {
            roleManager.assignRoleByName( roleName, principal );
        }
        catch ( RoleManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
        return Boolean.TRUE;
    }

    public Boolean assignTemplatedRole( String templateId, String resource, String principal )
        throws RedbackServiceException
    {
        try
        {
            roleManager.assignTemplatedRole( templateId, resource, principal );
        }
        catch ( RoleManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
        return Boolean.TRUE;
    }

    public Boolean unassignRole( String roleId, String principal )
        throws RedbackServiceException
    {
        try
        {
            roleManager.unassignRole( roleId, principal );
        }
        catch ( RoleManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
        return Boolean.TRUE;
    }

    public Boolean unassignRoleByName( String roleName, String principal )
        throws RedbackServiceException
    {
        try
        {
            roleManager.unassignRoleByName( roleName, principal );
        }
        catch ( RoleManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
        return Boolean.TRUE;
    }

    public Boolean roleExists( String roleId )
        throws RedbackServiceException
    {
        try
        {
            return roleManager.roleExists( roleId );
        }
        catch ( RoleManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
    }

    public Boolean templatedRoleExists( String templateId, String resource )
        throws RedbackServiceException
    {
        try
        {
            return roleManager.templatedRoleExists( templateId, resource );
        }
        catch ( RoleManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }

    }

    public Boolean verifyTemplatedRole( String templateId, String resource )
        throws RedbackServiceException
    {
        try
        {
            roleManager.verifyTemplatedRole( templateId, resource );
        }
        catch ( RoleManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
        return Boolean.TRUE;
    }
}
