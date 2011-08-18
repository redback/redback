package org.codehaus.redback.rest.services;

/*
 * Copyright 2011 The Codehaus.
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

import org.codehaus.plexus.redback.configuration.UserConfiguration;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.redback.integration.role.RoleConstants;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Olivier Lamy
 */
//Service( "fakeCreateAdminService" )
public class FakeCreateAdminServiceImpl
    implements FakeCreateAdminService
{
    @Inject
    @Named( value = "rBACManager#jdo" )
    private RBACManager rbacManager;

    @Inject
    @Named( value = "userManager#jdo" )
    private UserManager userManager;

    @Inject
    private UserConfiguration config;

    @Inject
    private RoleManager roleManager;

    public Boolean createAdminIfNeeded()
        throws Exception
    {
        try
        {
            userManager.findUser( config.getString( "redback.default.admin" ) );
            return Boolean.TRUE;
        }
        catch ( UserNotFoundException e )
        {
            // ignore
        }
        User user = userManager.createUser( RoleConstants.ADMINISTRATOR_ACCOUNT_NAME, "root user", "foo@foo.com" );
        user.setPassword( ADMIN_TEST_PWD );

        user.setLocked( false );
        user.setPasswordChangeRequired( false );
        user.setPermanent( true );

        userManager.addUser( user );

        roleManager.assignRole( "system-administrator", user.getPrincipal().toString() );

        /*
        UserAssignment userAssignment = rbacManager.createUserAssignment( RoleConstants.ADMINISTRATOR_ACCOUNT_NAME );
        userAssignment.setRoleNames( Collections.singletonList( RoleConstants.USER_ADMINISTRATOR_ROLE ) );
        rbacManager.saveUserAssignment( userAssignment );
        */
        return Boolean.TRUE;
    }

    public Boolean testAuthzWithoutKarmasNeededButAuthz()
    {
        return Boolean.TRUE;
    }
}
