package org.codehaus.plexus.redback.struts2.action.admin;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.ehcache.CacheManager;

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
import org.codehaus.plexus.redback.struts2.model.ApplicationRoleDetails;
import org.codehaus.plexus.redback.struts2.model.ApplicationRoleDetails.RoleTableCell;
import org.codehaus.plexus.redback.system.SecuritySession;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.system.SecuritySystemConstants;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.plexus.redback.users.memory.SimpleUser;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import com.opensymphony.xwork2.Action;

/**
 * @todo missing tests for success/fail on standard show/edit functions (non security testing related)
 */
public class AssignmentsActionTest
    extends PlexusInSpringTestCase
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
        CacheManager.getInstance().clearAll();
        CacheManager.getInstance().removalAll();
        CacheManager.getInstance().shutdown();
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
        user.setUsername( "user3" );
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

    private List<SecureActionBundle.AuthorizationTuple> getTuples()
        throws SecureActionException
    {
        return (List<SecureActionBundle.AuthorizationTuple>) action.getSecureActionBundle().getAuthorizationTuples();
    }

    private void addAssignment( String principal, String roleName )
        throws RbacManagerException, RbacObjectInvalidException
    {
        UserAssignment assignment;

        if ( rbacManager.userAssignmentExists( principal ) )
        {
            assignment = rbacManager.getUserAssignment( principal );
        }
        else
        {
            assignment = rbacManager.createUserAssignment( principal );
        }
        assignment.addRoleName( roleName );
        rbacManager.saveUserAssignment( assignment );
    }

    /**
     * Check roles can be assigned if the user has no previous assignments.
     */
    public void testShowWhenUserHasNoAssignments()
        throws Exception
    {
        addAssignment( "user", "Project Administrator - default" );

        action.setPrincipal( "user3" );

        assertEquals( Action.SUCCESS, action.show() );

        assertEquals( 2, action.getApplicationRoleDetails().size() );
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
        List<List<RoleTableCell>> table = details.getTable();
        assertEquals( 1, table.size() );
        assertRow( table, 0, "default", "Project Administrator - default", false );
    }

    @SuppressWarnings( "unchecked" )
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
    // public void testRoleGrantFilteringOnShowGlobalGrant()
    // throws RbacObjectInvalidException, RbacManagerException
    // {
    // addAssignment( "user", "Global Grant Administrator" );
    //
    // assertEquals( Action.SUCCESS, action.show() );
    //
    // assertEquals( 2, action.getApplicationRoleDetails().size() );
    // ApplicationRoleDetails details = (ApplicationRoleDetails) action.getApplicationRoleDetails().get( 0 );
    // assertEquals( "redback-xwork-integration-core", details.getName() );
    // assertEquals( 0, details.getAvailableRoles().size() );
    //
    // details = (ApplicationRoleDetails) action.getApplicationRoleDetails().get( 1 );
    // assertEquals( "Continuum", details.getName() );
    // assertEquals( 0, details.getAvailableRoles().size() );
    //
    // List table = details.getTable();
    // assertEquals( 2, table.size() );
    // assertRow( table, 0, "default", "Project Administrator - default", false );
    // assertRow( table, 1, "other", "Project Administrator - other", false );
    // }
    /**
     * Check security - edituser should skip adding a role that 'user-management-role-grant' is not present for a
     * non-templated role
     */
    public void testRoleGrantFilteringOnAddRolesNotPermittedTemplated()
        throws RbacObjectInvalidException, RbacManagerException
    {
        addAssignment( "user", "Project Administrator - default" );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> dSelectedRoles = new ArrayList<String>();
        dSelectedRoles.add( "Project Administrator - other" );

        action.setAddDSelectedRoles( dSelectedRoles );

        assertTrue( rbacManager.getUserAssignment( "user2" ).getRoleNames().isEmpty() );

        assertEquals( Action.SUCCESS, action.edituser() );

        assertTrue( rbacManager.getUserAssignment( "user2" ).getRoleNames().isEmpty() );
    }

    /**
     * Check security - edituser should skip adding a role that 'user-management-role-grant' is not present for a
     * templated role
     */
    public void testRoleGrantFilteringOnAddRolesNotPermittedNotTemplated()
        throws RbacObjectInvalidException, RbacManagerException
    {
        addAssignment( "user", "Project Administrator - default" );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> ndSelectedRoles = new ArrayList<String>();
        ndSelectedRoles.add( "Continuum Group Project Administrator" );

        action.setAddNDSelectedRoles( ndSelectedRoles );

        assertTrue( rbacManager.getUserAssignment( "user2" ).getRoleNames().isEmpty() );

        assertEquals( Action.SUCCESS, action.edituser() );

        assertTrue( rbacManager.getUserAssignment( "user2" ).getRoleNames().isEmpty() );
    }

    /**
     * Check security - edituser should succeed if adding a role that 'user-management-role-grant' is present for
     * untemplated roles
     */
    public void testRoleGrantFilteringOnAddRolesPermittedNotTemplated()
        throws RbacObjectInvalidException, RbacManagerException, AccountLockedException, AuthenticationException,
        UserNotFoundException
    {
        addAssignment( "user", "Global Grant Administrator" );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> ndSelectedRoles = new ArrayList<String>();
        ndSelectedRoles.add( "Continuum Group Project Administrator" );

        action.setAddNDSelectedRoles( ndSelectedRoles );

        assertTrue( rbacManager.getUserAssignment( "user2" ).getRoleNames().isEmpty() );

        assertEquals( Action.SUCCESS, action.edituser() );

        assertEquals( Arrays.asList( "Continuum Group Project Administrator" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );
    }

    /**
     * Check security - edituser should succeed if adding a role that 'user-management-role-grant' is present for
     * templated roles
     */
    public void testRoleGrantFilteringOnAddRolesPermittedTemplated()
        throws RbacObjectInvalidException, RbacManagerException, AccountLockedException, AuthenticationException,
        UserNotFoundException
    {
        addAssignment( "user", "Project Administrator - default" );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> dSelectedRoles = new ArrayList<String>();
        dSelectedRoles.add( "Project Administrator - default" );

        action.setAddDSelectedRoles( dSelectedRoles );

        assertTrue( rbacManager.getUserAssignment( "user2" ).getRoleNames().isEmpty() );

        assertEquals( Action.SUCCESS, action.edituser() );

        assertEquals( Arrays.asList( "Project Administrator - default" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );
    }

    /**
     * Check security - edituser should succeed if adding a role that 'user-management-role-grant' is present for
     * templated roles
     */
    public void testRoleGrantFilteringOnAddRolesPermittedTemplatedExistingRole()
        throws RbacObjectInvalidException, RbacManagerException, AccountLockedException, AuthenticationException,
        UserNotFoundException
    {
        addAssignment( "user", "Project Administrator - default" );

        addAssignment( "user2", "Project Administrator - other" );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> dSelectedRoles = new ArrayList<String>();
        dSelectedRoles.add( "Project Administrator - default" );

        action.setAddDSelectedRoles( dSelectedRoles );

        assertEquals( Arrays.asList( "Project Administrator - other" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );

        assertEquals( Action.SUCCESS, action.edituser() );

        assertEquals( Arrays.asList( "Project Administrator - default", "Project Administrator - other" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );
    }

    /**
     * Check security - edituser should fail if removing a role that 'user-management-role-grant' is not present for
     * untemplated roles
     */
    public void testRoleGrantFilteringOnRemoveRolesNotPermittedNotTemplated()
        throws RbacObjectInvalidException, RbacManagerException
    {
        addAssignment( "user", "Project Administrator - default" );

        addAssignment( "user2", "Continuum Group Project Administrator" );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> ndSelectedRoles = new ArrayList<String>();
        action.setAddNDSelectedRoles( ndSelectedRoles );

        assertEquals( Arrays.asList( "Continuum Group Project Administrator" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );

        assertEquals( Action.SUCCESS, action.edituser() );

        assertEquals( Arrays.asList( "Continuum Group Project Administrator" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );
    }

    /**
     * Check security - edituser should fail if removing a role that 'user-management-role-grant' is not present for
     * templated roles
     */
    public void testRoleGrantFilteringOnRemoveRolesNotPermittedTemplated()
        throws RbacObjectInvalidException, RbacManagerException
    {
        addAssignment( "user", "Project Administrator - other" );

        addAssignment( "user2", "Project Administrator - default" );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> dSelectedRoles = new ArrayList<String>();
        action.setAddDSelectedRoles( dSelectedRoles );

        assertEquals( Arrays.asList( "Project Administrator - default" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );

        assertEquals( Action.SUCCESS, action.edituser() );

        assertEquals( Arrays.asList( "Project Administrator - default" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );
    }

    /**
     * Check security - edituser should succeed if removing a role that 'user-management-role-grant' is present for
     * untemplated roles
     */
    public void testRoleGrantFilteringOnRemoveRolesPermittedNotTemplated()
        throws RbacObjectInvalidException, RbacManagerException, AccountLockedException, AuthenticationException,
        UserNotFoundException
    {
        addAssignment( "user", "Global Grant Administrator" );

        addAssignment( "user2", "Continuum Group Project Administrator" );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> ndSelectedRoles = new ArrayList<String>();
        action.setAddNDSelectedRoles( ndSelectedRoles );

        assertEquals( Arrays.asList( "Continuum Group Project Administrator" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );

        assertEquals( Action.SUCCESS, action.edituser() );

        assertTrue( rbacManager.getUserAssignment( "user2" ).getRoleNames().isEmpty() );
    }

    /**
     * Check security - edituser should succeed if removing a role that 'user-management-role-grant' is present for
     * templated roles and there is an existing role that is not assignable by the current user.
     */
    public void testRoleGrantFilteringOnRemoveRolesPermittedTemplatedExistingRole()
        throws RbacObjectInvalidException, RbacManagerException
    {
        addAssignment( "user", "Project Administrator - default" );

        addAssignment( "user2", "Project Administrator - default" );
        addAssignment( "user2", "Project Administrator - other" );
        addAssignment( "user2", "Registered User" );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> dSelectedRoles = new ArrayList<String>();
        dSelectedRoles.add( "Project Administrator - other" );
        dSelectedRoles.add( "Registered User" );
        action.setAddDSelectedRoles( dSelectedRoles );

        assertEquals( Arrays.asList( "Project Administrator - default", "Project Administrator - other",
                                     "Registered User" ), rbacManager.getUserAssignment( "user2" ).getRoleNames() );

        assertEquals( Action.SUCCESS, action.edituser() );

        assertEquals( Arrays.asList( "Project Administrator - other", "Registered User" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );
    }

    /**
     * Check security - edituser should succeed if removing a role that 'user-management-role-grant' is present for
     * templated roles
     */
    public void testRoleGrantFilteringOnRemoveRolesPermittedTemplated()
        throws RbacObjectInvalidException, RbacManagerException
    {
        addAssignment( "user", "Project Administrator - default" );

        addAssignment( "user2", "Project Administrator - default" );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> dSelectedRoles = new ArrayList<String>();
        action.setAddDSelectedRoles( dSelectedRoles );

        assertEquals( Arrays.asList( "Project Administrator - default" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );

        assertEquals( Action.SUCCESS, action.edituser() );

        assertTrue( rbacManager.getUserAssignment( "user2" ).getRoleNames().isEmpty() );
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

        List<List<RoleTableCell>> table = details.getTable();
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

        List<List<RoleTableCell>> table = details.getTable();
        assertEquals( 2, table.size() );
        assertRow( table, 0, "default", "Project Administrator - default", false );
        assertRow( table, 1, "other", "Project Administrator - other", false );
    }

    /**
     * Check security - edituser should succeed in adding a role, even without 'user-management-role-grant' or
     * 'user-management-user-role' for the user administrators.
     */
    public void testUserAdminCanAddRoles()
        throws RbacObjectNotFoundException, RbacManagerException, AccountLockedException, AuthenticationException,
        UserNotFoundException
    {
        login( "user-admin", PASSWORD );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> ndSelectedRoles = new ArrayList<String>();
        ndSelectedRoles.add( "Continuum Group Project Administrator" );

        action.setAddNDSelectedRoles( ndSelectedRoles );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> dSelectedRoles = new ArrayList<String>();
        dSelectedRoles.add( "Project Administrator - default" );

        action.setAddDSelectedRoles( dSelectedRoles );

        assertTrue( rbacManager.getUserAssignment( "user2" ).getRoleNames().isEmpty() );

        assertEquals( Action.SUCCESS, action.edituser() );

        assertEquals( Arrays.asList( "Continuum Group Project Administrator", "Project Administrator - default" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );
    }

    /**
     * Check security - edituser should succeed in removing a role, even without 'user-management-role-grant' or
     * 'user-management-user-role' for the user administrators.
     */
    public void testUserAdminCanRemoveRoles()
        throws RbacObjectNotFoundException, RbacManagerException, AccountLockedException, AuthenticationException,
        UserNotFoundException
    {
        login( "user-admin", PASSWORD );

        addAssignment( "user2", "Continuum Group Project Administrator" );
        addAssignment( "user2", "Project Administrator - default" );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> ndSelectedRoles = new ArrayList<String>();
        action.setAddNDSelectedRoles( ndSelectedRoles );

        List<String> dSelectedRoles = new ArrayList<String>();
        action.setAddDSelectedRoles( dSelectedRoles );

        assertEquals( Arrays.asList( "Continuum Group Project Administrator", "Project Administrator - default" ),
                      rbacManager.getUserAssignment( "user2" ).getRoleNames() );

        assertEquals( Action.SUCCESS, action.edituser() );

        assertTrue( rbacManager.getUserAssignment( "user2" ).getRoleNames().isEmpty() );
    }
}
