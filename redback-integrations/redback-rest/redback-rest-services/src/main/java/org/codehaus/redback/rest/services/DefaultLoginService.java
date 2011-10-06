package org.codehaus.redback.rest.services;

/*
 * Copyright 2009 The Codehaus.
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

import org.codehaus.plexus.redback.authentication.AuthenticationException;
import org.codehaus.plexus.redback.authentication.PasswordBasedAuthenticationDataSource;
import org.codehaus.plexus.redback.keys.AuthenticationKey;
import org.codehaus.plexus.redback.keys.KeyManager;
import org.codehaus.plexus.redback.keys.jdo.JdoAuthenticationKey;
import org.codehaus.plexus.redback.keys.memory.MemoryAuthenticationKey;
import org.codehaus.plexus.redback.keys.memory.MemoryKeyManager;
import org.codehaus.plexus.redback.policy.AccountLockedException;
import org.codehaus.plexus.redback.policy.MustChangePasswordException;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.redback.rest.api.services.LoginService;
import org.codehaus.redback.rest.api.services.RedbackServiceException;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Olivier Lamy
 * @since 1.3
 */
@Service( "loginService#rest" )
public class DefaultLoginService
    implements LoginService
{
    private SecuritySystem securitySystem;

    @Inject
    public DefaultLoginService(SecuritySystem securitySystem)
    {
        this.securitySystem = securitySystem;
    }


    public String addAuthenticationKey(String providedKey, String principal, String purpose, int expirationMinutes)
        throws RedbackServiceException
    {
        KeyManager keyManager = securitySystem.getKeyManager();
        AuthenticationKey key;

        if ( keyManager instanceof MemoryKeyManager )
        {
            key = new MemoryAuthenticationKey();
        }
        else
        {
            key = new JdoAuthenticationKey();
        }

        key.setKey(providedKey);
        key.setForPrincipal(principal);
        key.setPurpose(purpose);

        Calendar now = getNowGMT();
        key.setDateCreated(now.getTime());

        if ( expirationMinutes >= 0 )
        {
            Calendar expiration = getNowGMT();
            expiration.add(Calendar.MINUTE, expirationMinutes);
            key.setDateExpires(expiration.getTime());
        }

        keyManager.addKey(key);

        return key.getKey();
    }

    public Boolean ping()
        throws RedbackServiceException
    {
        return Boolean.TRUE;
    }

    public Boolean pingWithAutz()
        throws RedbackServiceException
    {
        return Boolean.TRUE;
    }

    public Boolean logIn(String userName, String password)
        throws RedbackServiceException
    {
        PasswordBasedAuthenticationDataSource authDataSource =
            new PasswordBasedAuthenticationDataSource(userName, password);
        try
        {
            return securitySystem.authenticate(authDataSource).getAuthenticationResult().isAuthenticated();
        }
        catch ( AuthenticationException e )
        {
            throw new RedbackServiceException(e.getMessage());
        }
        catch ( UserNotFoundException e )
        {
            throw new RedbackServiceException(e.getMessage());
        }
        catch ( AccountLockedException e )
        {
            throw new RedbackServiceException(e.getMessage());
        }
        catch ( MustChangePasswordException e )
        {
            throw new RedbackServiceException(e.getMessage());
        }
    }

    private Calendar getNowGMT()
    {
        return Calendar.getInstance(TimeZone.getTimeZone("GMT"));
    }
}
