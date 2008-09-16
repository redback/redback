package org.codehaus.plexus.redback.xwork.action.admin;

/*
 * Copyright 2008 The Codehaus.
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
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.redback.authentication.AuthenticationException;
import org.codehaus.plexus.redback.authentication.PasswordBasedAuthenticationDataSource;
import org.codehaus.plexus.redback.authorization.AuthorizationResult;
import org.codehaus.plexus.redback.policy.AccountLockedException;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.RbacObjectInvalidException;
import org.codehaus.plexus.redback.rbac.RbacObjectNotFoundException;
import org.codehaus.plexus.redback.rbac.UserAssignment;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.system.SecuritySession;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.system.SecuritySystemConstants;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.plexus.redback.users.memory.SimpleUser;
import org.codehaus.plexus.redback.xwork.interceptor.SecureActionBundle;
import org.codehaus.plexus.redback.xwork.interceptor.SecureActionException;
import org.codehaus.plexus.redback.xwork.model.ApplicationRoleDetails;
import org.codehaus.plexus.redback.xwork.model.ApplicationRoleDetails.RoleTableCell;

import com.opensymphony.xwork.Action;

/**
 * @todo missing tests for success/fail on standard show/edit functions (non security testing related)
 */
public class AssignmentsActionTest
    extends PlexusTestCase
{
    private static final String PASSWORD = "password1";

    private AssignmentsAction action;

    private RoleManager roleManager;

    private SecuritySystem system;

    private SecuritySession session;

    private RBACManager rbacManager;

    public void setUp()
        throws Exception
    {
        super.setUp();

        action = (AssignmentsAction) lookup( Action.class, "redback-assignments" );

        system = (SecuritySystem) lookup( SecuritySystem.ROLE );

        roleManager = (RoleManager) lookup( RoleManager.class );
        roleManager.loadRoleModel( getClass().getResource( "/redback.xml" ) );
        roleManager.createTemplatedRole( "project-administrator", "default" );
        roleManager.createTemplatedRole( "project-administrator", "other" );
        roleManager.createTemplatedRole( "project-grant-only", "default" );

        rbacManager = (RBACManager) lookup( RBACManager.class, "memory" );

        UserManager userManager = system.getUserManager();

        User user = new SimpleUser();
        user.setUsername( "user" );
        user.setPassword( PASSWORD );
        userManager.addUserUnchecked( user );

        user = new SimpleUser();
        user.setUsername( "user2" );
        user.setPassword( PASSWORD );
        userManager.addUserUnchecked( user );

        user = new SimpleUser();
        user.setUsername( "admin" );
        user.setPassword( PASSWORD );
        userManager.addUserUnchecked( user );

        user = new SimpleUser();
        user.setUsername( "user-admin" );
        user.setPassword( PASSWORD );
        userManager.addUserUnchecked( user );

        UserAssignment assignment = rbacManager.createUserAssignment( "admin" );
        assignment.addRoleName( "System Administrator" );
        rbacManager.saveUserAssignment( assignment );

        assignment = rbacManager.createUserAssignment( "user-admin" );
        assignment.addRoleName( "User Administrator" );
        rbacManager.saveUserAssignment( assignment );

        assignment = rbacManager.createUserAssignment( "user2" );
        rbacManager.saveUserAssignment( assignment );

        login( "user", PASSWORD );
        action.setPrincipal( "user2" );
    }

    private void login( String principal, String password )
        throws AuthenticationException, UserNotFoundException, AccountLockedException
    {
        PasswordBasedAuthenticationDataSource authdatasource = new PasswordBasedAuthenticationDataSource();
        authdatasource.setPrincipal( principal );
        authdatasource.setPassword( password );
        session = system.authenticate( authdatasource );
        assertTrue( session.isAuthenticated() );

        action.setSession( Collections.singletonMap( SecuritySystemConstants.SECURITY_SESSION_KEY, session ) );
    }

    /**
     * Check security - show/edituser should fail if the permission 'user-management-user-role' is not present, but a
     * valid 'user-management-role-grant' is.
     */
    public void testUserWithOnlyRoleGrantHasNoAccess()
        throws Exception
    {
        addAssignment( "user", "Grant Administrator - default" );

        List<SecureActionBundle.AuthorizationTuple> authorizationTuples = getTuples();
        for ( SecureActionBundle.AuthorizationTuple tuple : authorizationTuples )
        {
            AuthorizationResult authzResult = system.authorize( session, tuple.getOperation(), tuple.getResource() );

            assertFalse( authzResult.isAuthorized() );
        }
    }

    /**
     * Check security - check success if the permission 'user-management-user-role' is present along with global
     * 'user-management-role-grant'.
     */
    public void testUserWithOnlyRoleGrantHasAccess()
        throws Exception
    {
        addAssignment( "user", "Project Administrator - default" );

        List<SecureActionBundle.AuthorizationTuple> authorizationTuples = getTuples();
        boolean result = false;
        for ( SecureActionBundle.AuthorizationTuple tuple : authorizationTuples )
        {
            AuthorizationResult authzResult = system.authorize( session, tuple.getOperation(), tuple.getResource() );

            result |= authzResult.isAuthorized();
        }
        assertTrue( result );
    }

    @SuppressWarnings( "unchecked" )
    private List<SecureActionBundle.AuthorizationTuple> getTuples()
        throws SecureActionException
    {
        return (List<SecureActionBundle.AuthorizationTuple>) action.getSecureActionBundle().getAuthorizationTuples();
    }

    private void addAssignment( String principal, String roleName )
        throws RbacManagerException, RbacObjectInvalidException
    {
        UserAssignment assignment = rbacManager.createUserAssignment( principal );
        assignment.addRoleName( roleName );
        rbacManager.saveUserAssignment( assignment );
    }

    /**
     * Check security - show should filter out roles that the 'user-management-role-grant' is not present for
     */
    public void testRoleGrantFilteringOnShow()
        throws Exception
    {
        addAssignment( "user", "Project Administrator - default" );

        assertEquals( Action.SUCCESS, action.show() );

        assertEquals( 2, action.getApplicationRoleDetails().size() );
        ApplicationRoleDetails details = (ApplicationRoleDetails) action.getApplicationRoleDetails().get( 0 );
        assertEquals( "redback-xwork-integration-core", details.getName() );
        assertEquals( 0, details.getAvailableRoles().size() );
        details = (ApplicationRoleDetails) action.getApplicationRoleDetails().get( 1 );
        assertEquals( "Continuum", details.getName() );
        assertEquals( 0, details.getAvailableRoles().size() );

        // This table rendering code clearly has to go
        List table = details.getTable();
        assertEquals( 1, table.size() );
        assertRow( table, 0, "default", "Project Administrator - default", false );        
    }

    @SuppressWarnings("unchecked")
    private void assertRow( List table, int index, String name, String label, boolean assigned )
    {
        List<RoleTableCell> row = (List<RoleTableCell>) table.get( index );
        assertEquals( name, row.get( 0 ).getName() );
        assertEquals( label, row.get( 1 ).getName() );
        assertEquals( assigned, row.get( 2 ).isAssigned() );
    }

    /**
     * Check security - show should not filter out roles if 'user-management-role-grant' is present for the global
     * resource
     */
    // TODO: currently returns all roles - we really want all templated roles
//    public void testRoleGrantFilteringOnShowGlobalGrant()
//        throws RbacObjectInvalidException, RbacManagerException
//    {
//        addAssignment( "user", "Global Grant Administrator" );
//
//        assertEquals( Action.SUCCESS, action.show() );
//
//        assertEquals( 2, action.getApplicationRoleDetails().size() );
//        ApplicationRoleDetails details = (ApplicationRoleDetails) action.getApplicationRoleDetails().get( 0 );
//        assertEquals( "redback-xwork-integration-core", details.getName() );
//        assertEquals( 0, details.getAvailableRoles().size() );
//
//        details = (ApplicationRoleDetails) action.getApplicationRoleDetails().get( 1 );
//        assertEquals( "Continuum", details.getName() );
//        assertEquals( 0, details.getAvailableRoles().size() );
//
//        List table = details.getTable();
//        assertEquals( 2, table.size() );
//        assertRow( table, 0, "default", "Project Administrator - default", false );
//        assertRow( table, 1, "other", "Project Administrator - other", false );
//    }

    /**
     * Check security - edituser should fail if adding a role that 'user-management-role-grant' is not present for
     */
    public void testRoleGrantFilteringOnAddRolesNotPermitted()
    	throws RbacObjectInvalidException, RbacManagerException
    {
        addAssignment( "user", "Project Administrator - default" );
    	
    	// set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)?
    	List dSelectedRoles = new ArrayList();
    	dSelectedRoles.add( "Continuum Group Project Administrator" );
    	
    	List ndSelectedRoles = new ArrayList();
    	ndSelectedRoles.add( "Project Administrator - no_permission" );
    	
    	action.setAddDSelectedRoles( dSelectedRoles );
    	action.setAddNDSelectedRoles( ndSelectedRoles );
    	
    	//TODO assertEquals( Action.ERROR, action.edituser() );    	    	
    }

    /**
     * Check security - edituser should succeed if adding a role that 'user-management-role-grant' is present for
     */
    public void testRoleGrantFilteringOnAddRolesPermitted()
    	throws RbacObjectInvalidException, RbacManagerException
    {
    	addAssignment( "user", "Project Administrator - default" );
    	
    	List dSelectedRoles = new ArrayList();
    	dSelectedRoles.add( "Continuum Group Project Administrator" );
    	
    	List ndSelectedRoles = new ArrayList();
    	ndSelectedRoles.add( "Project Administrator - no_permission" );
    	
    	action.setAddDSelectedRoles( dSelectedRoles );
    	action.setAddNDSelectedRoles( ndSelectedRoles );
    	
    	// TODO: assertEquals( Action.ERROR, action.edituser() );    	
    	// TODO: check if user role was successfully added
    	//Collection assignedRoles = rbacManager.getAssignedRoles( "user2" );
    }

    /**
     * Check security - edituser should fail if removing a role that 'user-management-role-grant' is not present for
     */
    public void testRoleGrantFilteringOnRemoveRolesNotPermitted()
    {
        // TODO
    }

    /**
     * Check security - edituser should succeed if removing a role that 'user-management-role-grant' is present for
     */
    public void testRoleGrantFilteringOnRemoveRolesPermitted()
    {
        // TODO
    }

    /**
     * Check security - show should succeed and display all roles, even without 'user-management-role-grant' or
     * 'user-management-user-role' for the user administrators.
     */
    public void testSystemAdminCanShowRoles()
        throws AccountLockedException, AuthenticationException, UserNotFoundException, RbacObjectNotFoundException,
        RbacManagerException
    {
        login( "admin", PASSWORD );

        assertEquals( Action.SUCCESS, action.show() );

        assertEquals( 2, action.getApplicationRoleDetails().size() );
        ApplicationRoleDetails details = (ApplicationRoleDetails) action.getApplicationRoleDetails().get( 0 );
        assertEquals( "redback-xwork-integration-core", details.getName() );
        assertEquals( 4, details.getAvailableRoles().size() );
        assertEquals( "Guest", details.getAvailableRoles().get( 0 ) );
        assertEquals( "Registered User", details.getAvailableRoles().get( 1 ) );
        assertEquals( "System Administrator", details.getAvailableRoles().get( 2 ) );
        assertEquals( "User Administrator", details.getAvailableRoles().get( 3 ) );

        details = (ApplicationRoleDetails) action.getApplicationRoleDetails().get( 1 );
        assertEquals( "Continuum", details.getName() );

        assertEquals( 2, details.getAvailableRoles().size() );
        assertEquals( "Continuum Group Project Administrator", details.getAvailableRoles().get( 0 ) );
        assertEquals( "Global Grant Administrator", details.getAvailableRoles().get( 1 ) );

        List table = details.getTable();
        assertEquals( 2, table.size() );
        assertRow( table, 0, "default", "Project Administrator - default", false );
        assertRow( table, 1, "other", "Project Administrator - other", false );
    }

    /**
     * Check security - show should succeed and display all roles, even without 'user-management-role-grant' or
     * 'user-management-user-role' for the user administrators.
     */
    public void testUserAdminCanShowRoles()
        throws AccountLockedException, AuthenticationException, UserNotFoundException, RbacObjectNotFoundException,
        RbacManagerException
    {
        login( "user-admin", PASSWORD );

        assertEquals( Action.SUCCESS, action.show() );

        assertEquals( 2, action.getApplicationRoleDetails().size() );
        ApplicationRoleDetails details = (ApplicationRoleDetails) action.getApplicationRoleDetails().get( 0 );
        assertEquals( "redback-xwork-integration-core", details.getName() );
        // TODO assertEquals( 3, details.getAvailableRoles().size() );
        assertEquals( "Guest", details.getAvailableRoles().get( 0 ) );
        assertEquals( "Registered User", details.getAvailableRoles().get( 1 ) );
        // TODO: assertEquals( "User Administrator", details.getAvailableRoles().get( 2 ) );

        details = (ApplicationRoleDetails) action.getApplicationRoleDetails().get( 1 );
        assertEquals( "Continuum", details.getName() );

        assertEquals( 2, details.getAvailableRoles().size() );
        assertEquals( "Continuum Group Project Administrator", details.getAvailableRoles().get( 0 ) );
        assertEquals( "Global Grant Administrator", details.getAvailableRoles().get( 1 ) );

        List table = details.getTable();
        assertEquals( 2, table.size() );
        assertRow( table, 0, "default", "Project Administrator - default", false );
        assertRow( table, 1, "other", "Project Administrator - other", false );
    }

    /**
     * Check security - edituser should succeed in adding a role, even without 'user-management-role-grant' or
     * 'user-management-user-role' for the user administrators.
     */
    public void testUserAdminCanAddRoles()
    {
        // TODO
    }

    /**
     * Check security - edituser should succeed in removing a role, even without 'user-management-role-grant' or
     * 'user-management-user-role' for the user administrators.
     */
    public void testUserAdminCanRemoveRoles()
    {
        // TODO
    }

    /**
     * Check security - show should succeed for current user to see all their roles, even without
     * 'user-management-role-grant' or 'user-management-user-role'
     */
    public void testCurrentUserShowRoles()
    {
        // TODO
    }

    /**
     * Check security - edituser should fail for current user removing their own role without
     * 'user-management-role-grant' or 'user-management-user-role'
     */
    public void testCurrentUserCannotRemoveRoles()
    {
        // TODO
    }

    /**
     * Check security - edituser should fail for current user adding a role without 'user-management-role-grant' or
     * 'user-management-user-role'
     */
    public void testCurrentUserCannotAddRoles()
    {
        // TODO
    }
}
