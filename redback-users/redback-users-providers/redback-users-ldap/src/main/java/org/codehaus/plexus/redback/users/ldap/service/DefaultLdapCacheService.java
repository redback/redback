package org.codehaus.plexus.redback.users.ldap.service;

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

import org.codehaus.plexus.cache.builder.CacheBuilder;
import org.codehaus.plexus.redback.common.ldap.LdapUser;
import org.codehaus.plexus.redback.common.ldap.connection.LdapConnection;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * DefaultLdapCacheService
 *
 * @author: Maria Odea Ching <oching@apache.org>
 * @version
 */
@Service
public class DefaultLdapCacheService
    implements LdapCacheService
{
    @Inject
    private CacheBuilder cacheBuilder;

    // LDAP Users

    /**
     * @see LdapCacheService#getUser(String)
     */
    public LdapUser getUser( String username )
    {
        return (LdapUser) cacheBuilder.getCache( "ldapUser" ).get( username );
    }

    /**
     * @see LdapCacheService#removeUser(String)
     */
    public boolean removeUser( String username )
    {
        return ( cacheBuilder.getCache( "ldapUser" ).remove( username ) == null ? false : true );
    }

    /**
     * @see LdapCacheService#removeAllUsers()
     */
    public void removeAllUsers()
    {
        cacheBuilder.getCache( "ldapUser" ).clear();
    }

    /**
     * @see LdapCacheService#addUser(org.codehaus.plexus.redback.common.ldap.LdapUser)
     */
    public void addUser( LdapUser user )
    {
        LdapUser existingUser = (LdapUser) cacheBuilder.getCache( "ldapUser" ).get( user.getUsername() );
        if( existingUser != null )
        {
            removeUser( user.getUsername() );
        }

        cacheBuilder.getCache( "ldapUser" ).put( user.getUsername(), user );
    }

    // LDAP Connections

    /**
     * @see LdapCacheService#getLdapConnection(String)
     */
    public LdapConnection getLdapConnection( String username )
    {
        return (LdapConnection) cacheBuilder.getCache( "ldapConnection" ).get( username );
    }

    /**
     * @see LdapCacheService#removeLdapConnection(String)
     */
    public boolean removeLdapConnection( String username )
    {
        return ( cacheBuilder.getCache( "ldapConnection" ).remove( username ) == null ? false : true );
    }

    /**
     * @see LdapCacheService#removeAllLdapConnections()
     */
    public void removeAllLdapConnections()
    {
        cacheBuilder.getCache( "ldapConnection" ).clear();
    }

    /**
     * @see LdapCacheService#addLdapConnection(String, org.codehaus.plexus.redback.common.ldap.connection.LdapConnection)  
     */
    public void addLdapConnection( String username, LdapConnection ldapConnection )
    {
        LdapUser existingUser = (LdapUser) cacheBuilder.getCache( "ldapConnection" ).get( username );
        if( existingUser != null )
        {
            removeLdapConnection( username );
        }

        cacheBuilder.getCache( "ldapConnection" ).put( username, ldapConnection );
    }
}
