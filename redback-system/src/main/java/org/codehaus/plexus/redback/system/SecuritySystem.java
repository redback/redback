package org.codehaus.plexus.redback.system;

/*
 * Copyright 2005 The Codehaus.
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

import org.codehaus.plexus.redback.authentication.AuthenticationDataSource;
import org.codehaus.plexus.redback.authentication.AuthenticationException;
import org.codehaus.plexus.redback.authorization.AuthorizationException;
import org.codehaus.plexus.redback.authorization.AuthorizationResult;
import org.codehaus.plexus.redback.keys.KeyManager;
import org.codehaus.plexus.redback.policy.AccountLockedException;
import org.codehaus.plexus.redback.policy.MustChangePasswordException;
import org.codehaus.plexus.redback.policy.UserSecurityPolicy;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;

/**
 * SecuritySystem:
 *
 * @author: Jesse McConnell <jesse@codehaus.org>
 * @version: $ID:$
 *
 */
public interface SecuritySystem
{

    // ----------------------------------------------------------------------------
    // Authentication
    // ----------------------------------------------------------------------------

    SecuritySession authenticate( AuthenticationDataSource source )
        throws AuthenticationException, UserNotFoundException, AccountLockedException, MustChangePasswordException;

    boolean isAuthenticated( AuthenticationDataSource source )
        throws AuthenticationException, UserNotFoundException, AccountLockedException, MustChangePasswordException;

    // ----------------------------------------------------------------------------
    // Authorization
    // ----------------------------------------------------------------------------

    AuthorizationResult authorize( SecuritySession session, Object permission )
        throws AuthorizationException;

    boolean isAuthorized( SecuritySession session, Object permission )
        throws AuthorizationException;

    AuthorizationResult authorize( SecuritySession session, Object permission, Object resource )
        throws AuthorizationException;

    boolean isAuthorized( SecuritySession session, Object permission, Object resource )
        throws AuthorizationException;

    // ----------------------------------------------------------------------------
    // User Management
    // ----------------------------------------------------------------------------

    UserManager getUserManager();
    
    // ----------------------------------------------------------------------------
    // Key Management
    // ----------------------------------------------------------------------------
    
    KeyManager getKeyManager();

    // ----------------------------------------------------------------------------
    // Policy Management
    // ----------------------------------------------------------------------------
    
    UserSecurityPolicy getPolicy();

    String getUserManagementId();
    String getAuthenticatorId();
    String getAuthorizerId();
}

