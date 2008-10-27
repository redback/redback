package org.codehaus.redback.integration.checks.security;

/*
 * Copyright 2005-2006 The Codehaus.
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

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.redback.configuration.UserConfiguration;
import org.codehaus.plexus.redback.policy.UserSecurityPolicy;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.system.check.EnvironmentCheck;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;

import java.util.List;

/**
 * RequiredRolesEnvironmentCheck:
 *
 * @author: Jesse McConnell <jesse@codehaus.org>
 * @version: $Id$
 * @plexus.component role="org.codehaus.plexus.redback.system.check.EnvironmentCheck"
 * role-hint="guest-user-check"
 */
public class GuestUserEnvironmentCheck
    extends AbstractLogEnabled
    implements EnvironmentCheck
{

    /**
     * @plexus.requirement role-hint="default"
     */
    private UserConfiguration config;

    /**
     * @plexus.requirement role-hint="default"
     */
    private RoleManager roleManager;

    /**
     * @plexus.requirement role-hint="default"
     */
    private SecuritySystem securitySystem;

    /**
     * boolean detailing if this environment check has been executed
     */
    private boolean checked = false;

    /**
     * @param violations
     */
    public void validateEnvironment( List violations )
    {
        if ( !checked )
        {
            UserManager userManager = securitySystem.getUserManager();
            UserSecurityPolicy policy = securitySystem.getPolicy();

            User guest;
            try
            {
                guest = userManager.getGuestUser();
            }
            catch ( UserNotFoundException e )
            {
                policy.setEnabled( false );
                guest = userManager.createGuestUser();
                policy.setEnabled( true );
            }

            try
            {
                roleManager.assignRole( "guest", guest.getPrincipal().toString() );
            }
            catch ( RoleManagerException rpe )
            {
                violations.add( "unable to initialize guest user properly: " + rpe.getMessage() );
            }

            checked = true;
        }
    }
}
