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

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.config.Configuration;
import com.opensymphony.xwork2.config.ConfigurationManager;
import com.opensymphony.xwork2.inject.Container;
import com.opensymphony.xwork2.util.ValueStack;
import com.opensymphony.xwork2.util.ValueStackFactory;
import org.apache.struts2.dispatcher.Dispatcher;
import org.codehaus.plexus.redback.authentication.AuthenticationException;
import org.codehaus.plexus.redback.authorization.AuthorizationResult;
import org.codehaus.plexus.redback.policy.AccountLockedException;
import org.codehaus.plexus.redback.policy.MustChangePasswordException;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.RbacObjectInvalidException;
import org.codehaus.plexus.redback.rbac.Role;
import org.codehaus.plexus.redback.struts2.model.ApplicationRoleDetails;
import org.codehaus.plexus.redback.struts2.model.ApplicationRoleDetails.RoleTableCell;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;

/**
 * @todo missing tests for success/fail on standard show/edit functions (non security testing related)
 */
public class AssignmentsActionTest
    extends AbstractUserCredentialsActionTest
{
    private AssignmentsAction action;

    @SuppressWarnings("unchecked")
    public void setUp()
        throws Exception
    {
        super.setUp();

        action = (AssignmentsAction) lookup( Action.class, "redback-assignments" );

        login( action, "user", PASSWORD );
        action.setPrincipal( "user2" );
        
        // This fix allow initialization of ActionContext.getContext() to avoid NPE
        
        ConfigurationManager configurationManager = new ConfigurationManager();
        configurationManager.addConfigurationProvider( new com.opensymphony.xwork2.config.providers.XWorkConfigurationProvider() );
        Configuration config = configurationManager.getConfiguration();
        Container container =  config.getContainer();
        
        ValueStack stack = container.getInstance( ValueStackFactory.class ).createValueStack();
        stack.getContext().put( ActionContext.CONTAINER, container );
        ActionContext.setContext( new ActionContext( stack.getContext() ) );
        
        assertNotNull( ActionContext.getContext() );
    }
    
    public static Dispatcher prepareDispatcher( ServletContext servletContext, Map<String, String> params )
    {
        if (params == null)
        {
            params = new HashMap<String, String>();
        }
        Dispatcher dispatcher = new Dispatcher(servletContext, params);
        dispatcher.init();
        Dispatcher.setInstance(dispatcher);
        
        return dispatcher;
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
        assertEquals( "System", details.getName() );
        assertEquals( "Roles that apply system-wide, across all of the applications", details.getDescription() );
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

        // Roles may be out of order, due to removal and subsequent re-add
        List<String> user2roles = rbacManager.getUserAssignment( "user2" ).getRoleNames();
        assertTrue( user2roles.contains( "Project Administrator - other" ) );
        assertTrue( user2roles.contains( "Registered User" ) );
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
     * 
     * @throws MustChangePasswordException
     */
    public void testSystemAdminCanShowRoles()
        throws Exception
    {
        login( action, "admin", PASSWORD );

        assertEquals( Action.SUCCESS, action.show() );

        assertEquals( 2, action.getApplicationRoleDetails().size() );
        ApplicationRoleDetails details = (ApplicationRoleDetails) action.getApplicationRoleDetails().get( 0 );
        assertEquals( "System", details.getName() );
        assertEquals( "Roles that apply system-wide, across all of the applications", details.getDescription() );
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
        throws Exception
    {
        login( action, "user-admin", PASSWORD );

        assertEquals( Action.SUCCESS, action.show() );

        assertEquals( 2, action.getApplicationRoleDetails().size() );
        ApplicationRoleDetails details = (ApplicationRoleDetails) action.getApplicationRoleDetails().get( 0 );
        assertEquals( "System", details.getName() );
        assertEquals( "Roles that apply system-wide, across all of the applications", details.getDescription() );
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
        throws Exception
    {
        login( action, "user-admin", PASSWORD );

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
        throws Exception
    {
        login( action, "user-admin", PASSWORD );

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

    /**
     * Check that a configured struts2 redback app only removes roles configured for the app. Without this, redback
     * applications sharing a user database will remove each other's roles on save.
     */
    public void testUserAdminCannotRemoveNonAppRoles()
        throws Exception
    {
        login( action, "user-admin", PASSWORD );

        // Create a role that isn't configured for apps
        String nonAppRoleName = "Other App Role";
        Role nonAppRole = rbacManager.createRole( nonAppRoleName );
        rbacManager.saveRole( nonAppRole );

        addAssignment( "user2", "Continuum Group Project Administrator" );
        addAssignment( "user2", "Project Administrator - default" );
        addAssignment( "user2", nonAppRoleName );

        // set addDSelectedRoles (dynamic --> Resource Roles) and addNDSelectedRoles (non-dynamic --> Available Roles)
        List<String> ndSelectedRoles = new ArrayList<String>();
        action.setAddNDSelectedRoles( ndSelectedRoles );

        List<String> dSelectedRoles = new ArrayList<String>();
        action.setAddDSelectedRoles( dSelectedRoles );

        assertEquals( Arrays.asList( "Continuum Group Project Administrator", "Project Administrator - default",
                                     nonAppRoleName ), rbacManager.getUserAssignment( "user2" ).getRoleNames() );

        assertEquals( Action.SUCCESS, action.edituser() );

        // All roles except role from other app should be removed.
        List<String> user2roles = rbacManager.getUserAssignment( "user2" ).getRoleNames();
        assertTrue( !user2roles.contains( "Continuum Group Project Administrator" ) );
        assertTrue( !user2roles.contains( "Project Administrator - default" ) );
        assertTrue( user2roles.contains( nonAppRoleName ) );
    }
}
