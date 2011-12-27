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

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.redback.rbac.Permission;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.plexus.redback.role.model.ModelApplication;
import org.codehaus.redback.integration.security.role.RedbackRoleConstants;
import org.codehaus.redback.integration.util.RoleSorter;
import org.codehaus.redback.rest.api.model.Application;
import org.codehaus.redback.rest.api.model.ErrorMessage;
import org.codehaus.redback.rest.api.model.Role;
import org.codehaus.redback.rest.api.services.RedbackServiceException;
import org.codehaus.redback.rest.api.services.RoleManagementService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Olivier Lamy
 * @since 1.3
 */
@Service( "roleManagementService#rest" )
public class DefaultRoleManagementService
    implements RoleManagementService
{
    private RoleManager roleManager;

    private RBACManager rbacManager;

    @Inject
    public DefaultRoleManagementService( RoleManager roleManager,
                                         @Named( value = "rBACManager#cached" ) RBACManager rbacManager )
    {
        this.roleManager = roleManager;
        this.rbacManager = rbacManager;
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

    public List<Role> getEffectivelyAssignedRoles( String username )
        throws RedbackServiceException
    {
        if ( StringUtils.isEmpty( username ) )
        {
            throw new RedbackServiceException( new ErrorMessage( "user.cannot.be.null" ) );
        }
        try
        {
            List<org.codehaus.plexus.redback.rbac.Role> roles =
                filterAssignableRoles( rbacManager.getEffectivelyAssignedRoles( username ) );

            List<Role> effectivelyAssignedRoles = new ArrayList<Role>( roles.size() );

            for ( org.codehaus.plexus.redback.rbac.Role r : roles )
            {
                effectivelyAssignedRoles.add( new Role( r ) );
            }
            return effectivelyAssignedRoles;
        }
        catch ( RbacManagerException rme )
        {
            // ignore, this can happen when the user has no roles assigned  
        }
        return new ArrayList<Role>( 0 );
    }


    public List<Application> getApplications( String username )
        throws RedbackServiceException
    {

        List<ModelApplication> modelApplications = roleManager.getModel().getApplications();

        List<Application> applications = new ArrayList<Application>( modelApplications.size() );

        for ( ModelApplication modelApplication : modelApplications )
        {
            Application application = new Application();
            application.setDescription( modelApplication.getDescription() );
            application.setId( modelApplication.getId() );
            application.setLongDescription( modelApplication.getLongDescription() );
            application.setVersion( modelApplication.getVersion() );
            applications.add( application );
        }

        return applications;
    }

    public List<Role> getAllRoles()
        throws RedbackServiceException
    {
        try
        {
            List<org.codehaus.plexus.redback.rbac.Role> roles = rbacManager.getAllRoles();

            if ( roles == null )
            {
                return Collections.emptyList();
            }

            roles = filterRolesForCurrentUserAccess( roles );

            List<Role> res = new ArrayList<Role>( roles.size() );

            for ( org.codehaus.plexus.redback.rbac.Role r : roles )
            {
                res.add( new Role( r ) );
            }
            return res;

        }
        catch ( RbacManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
    }

    private List<org.codehaus.plexus.redback.rbac.Role> filterAssignableRoles(
        Collection<org.codehaus.plexus.redback.rbac.Role> roles )
    {
        List<org.codehaus.plexus.redback.rbac.Role> assignableRoles =
            new ArrayList<org.codehaus.plexus.redback.rbac.Role>( roles.size() );
        for ( org.codehaus.plexus.redback.rbac.Role r : roles )
        {
            if ( r.isAssignable() )
            {
                assignableRoles.add( r );
            }
        }
        return assignableRoles;
    }

    public Role getRole( String roleName )
        throws RedbackServiceException
    {
        try
        {
            org.codehaus.plexus.redback.rbac.Role rbacRole = rbacManager.getRole( roleName );
            Role role = new Role( rbacRole );

            return role;
        }
        catch ( RbacManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
    }

    //----------------------------------------------------------------
    // Internal methods
    //----------------------------------------------------------------

    /**
     * this is a hack. this is a hack around the requirements of putting RBAC constraints into the model. this adds one
     * very major restriction to this security system, that a role name must contain the identifiers of the resource
     * that is being constrained for adding and granting of roles, this is unacceptable in the long term and we need to
     * get the model refactored to include this RBAC concept
     *
     * @param roleList
     * @return
     * @throws org.codehaus.plexus.redback.rbac.RbacManagerException
     *
     */
    protected List<org.codehaus.plexus.redback.rbac.Role> filterRolesForCurrentUserAccess(
        List<org.codehaus.plexus.redback.rbac.Role> roleList )
        throws RedbackServiceException
    {
        RedbackRequestInformation redbackRequestInformation = RedbackAuthenticationThreadLocal.get();
        // olamy: should not happened normally as annotations check this first
        if ( redbackRequestInformation == null || redbackRequestInformation.getUser() == null )
        {
            throw new RedbackServiceException( new ErrorMessage( "login.mandatory" ) );
        }
        String currentUser = redbackRequestInformation.getUser().getUsername();

        List<org.codehaus.plexus.redback.rbac.Role> filteredRoleList =
            new ArrayList<org.codehaus.plexus.redback.rbac.Role>();
        try
        {
            Map<String, List<Permission>> assignedPermissionMap = rbacManager.getAssignedPermissionMap( currentUser );
            List<String> resourceGrants = new ArrayList<String>();

            if ( assignedPermissionMap.containsKey( RedbackRoleConstants.USER_MANAGEMENT_ROLE_GRANT_OPERATION ) )
            {
                List<Permission> roleGrantPermissions =
                    assignedPermissionMap.get( RedbackRoleConstants.USER_MANAGEMENT_ROLE_GRANT_OPERATION );

                for ( Permission permission : roleGrantPermissions )
                {
                    if ( permission.getResource().getIdentifier().equals( Resource.GLOBAL ) )
                    {
                        // the current user has the rights to assign any given role
                        return roleList;
                    }
                    else
                    {
                        resourceGrants.add( permission.getResource().getIdentifier() );
                    }
                }

            }
            else
            {
                return Collections.emptyList();
            }

            String delimiter = " - ";

            // we should have a list of resourceGrants now, this will provide us with the information necessary to restrict
            // the role list
            for ( org.codehaus.plexus.redback.rbac.Role role : roleList )
            {
                int delimiterIndex = role.getName().indexOf( delimiter );
                for ( String resourceIdentifier : resourceGrants )
                {

                    if ( ( role.getName().indexOf( resourceIdentifier ) != -1 ) && ( delimiterIndex != -1 ) )
                    {
                        String resourceName = role.getName().substring( delimiterIndex + delimiter.length() );
                        if ( resourceName.equals( resourceIdentifier ) )
                        {
                            filteredRoleList.add( role );
                        }
                    }
                }
            }
        }
        catch ( RbacManagerException rme )
        {
            // ignore, this can happen when the user has no roles assigned  
        }
        Collections.sort( filteredRoleList, new RoleSorter() );
        return filteredRoleList;
    }


}
