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

import java.util.List;

import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.RbacObjectInvalidException;
import org.codehaus.plexus.redback.rbac.Role;

import com.opensymphony.xwork2.Action;

/**
 * @todo missing tests for success/fail on standard show/edit functions (non security testing related)
 */
public class UserEditActionTest
    extends AbstractUserCredentialsActionTest
{
    private UserEditAction action;

    public void setUp()
        throws Exception
    {
        super.setUp();

        action = (UserEditAction) lookup( Action.class, "redback-admin-user-edit" );

        login( action, "user", PASSWORD );
    }

    public void testEditPageShowsAdministratableRoles()
        throws RbacObjectInvalidException, RbacManagerException
    {
        addAssignment( "user", "User Administrator" );

        addAssignment( "user2", "Project Administrator - default" );
        addAssignment( "user2", "Project Administrator - other" );

        action.setUsername( "user2" );
        assertEquals( Action.INPUT, action.edit() );

        List<Role> effectivelyAssignedRoles = action.getEffectivelyAssignedRoles();
        assertEquals( 2, effectivelyAssignedRoles.size() );
        Role r = effectivelyAssignedRoles.get( 0 );
        assertEquals( "Project Administrator - default", r.getName() );
        r = effectivelyAssignedRoles.get( 1 );
        assertEquals( "Project Administrator - other", r.getName() );
        assertFalse( action.isHasHiddenRoles() );
    }

    public void testEditPageHidesUnadministratableRoles()
        throws RbacObjectInvalidException, RbacManagerException
    {
        // REDBACK-29
        // user should not be able to see the other project admin role of user2, but should be able to see the one
        // from their own group

        addAssignment( "user", "Project Administrator - default" );

        addAssignment( "user2", "Project Administrator - default" );
        addAssignment( "user2", "Project Administrator - other" );

        action.setUsername( "user2" );
        assertEquals( Action.INPUT, action.edit() );

        List<Role> effectivelyAssignedRoles = action.getEffectivelyAssignedRoles();
        assertEquals( 1, effectivelyAssignedRoles.size() );
        Role r = effectivelyAssignedRoles.get( 0 );
        assertEquals( "Project Administrator - default", r.getName() );
        assertTrue( action.isHasHiddenRoles() );
    }

    public void testEditPageHidesUnassignableRoles()
        throws RbacObjectInvalidException, RbacManagerException
    {
        // REDBACK-201
        // user should not be able to see the unassignable roles 

        addAssignment( "user", "User Administrator" );

        action.setUsername( "user" );
        assertEquals( Action.INPUT, action.edit() );

        List<Role> effectivelyAssignedRoles = action.getEffectivelyAssignedRoles();
        assertEquals( 1, effectivelyAssignedRoles.size() );
        Role r = effectivelyAssignedRoles.get( 0 );
        assertEquals( "User Administrator", r.getName() );
        assertFalse( action.isHasHiddenRoles() );
    }
}
