package org.codehaus.redback.xmlrpc.service;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.plexus.cache.Cache;
import org.codehaus.plexus.redback.keys.AuthenticationKey;
import org.codehaus.plexus.redback.keys.KeyManager;
import org.codehaus.plexus.redback.keys.jdo.JdoAuthenticationKey;
import org.codehaus.plexus.redback.keys.memory.MemoryAuthenticationKey;
import org.codehaus.plexus.redback.keys.memory.MemoryKeyManager;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Calendar;
import java.util.TimeZone;

@Service
public class LoginServiceImpl
    implements LoginService
{
    private SecuritySystem securitySystem;

    /**
     * cache used for user assignments
     * <p/>
     * plexus.requirement role-hint="userAssignments"
     */
    @Inject
    @Named( value ="cache#userAssignments" )
    private Cache userAssignmentsCache;

    /**
     * cache used for user permissions
     * <p/>
     * plexus.requirement role-hint="userPermissions"
     */
    @Named( value = "cache#userPermissions" )
    private Cache userPermissionsCache;

    /**
     * Cache used for users
     * <p/>
     * plexus.requirement role-hint="users"
     */
    @Named( value = "cache#users" )
    private Cache usersCache;

    @Inject
    public LoginServiceImpl( SecuritySystem securitySystem )
    {
        this.securitySystem = securitySystem;
    }

    public int addAuthenticationKey( String providedKey, String principal, String purpose, int expirationMinutes )
        throws Exception
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

        key.setKey( providedKey );
        key.setForPrincipal( principal );
        key.setPurpose( purpose );

        Calendar now = getNowGMT();
        key.setDateCreated( now.getTime() );

        if ( expirationMinutes >= 0 )
        {
            Calendar expiration = getNowGMT();
            expiration.add( Calendar.MINUTE, expirationMinutes );
            key.setDateExpires( expiration.getTime() );
        }

        keyManager.addKey( key );

        return 0;
    }

    public int removeFromCache( String username )
        throws Exception
    {
        if ( userAssignmentsCache != null )
        {
            userAssignmentsCache.remove( username );
        }
        if ( userPermissionsCache != null )
        {
            userPermissionsCache.remove( username );
        }
        if ( usersCache != null )
        {
            usersCache.remove( username );
        }

        return 0;
    }

    public Boolean ping()
        throws Exception
    {
        return Boolean.TRUE;
    }

    private Calendar getNowGMT()
    {
        return Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
    }
}
