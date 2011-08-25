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

import org.codehaus.plexus.redback.common.ldap.LdapUser;
import org.codehaus.plexus.redback.common.ldap.connection.LdapConnection;

/**
 * LdapCacheService
 *
 * Service that manages the LDAP caches: LDAP connections and LDAP users
 *
 * @author: Maria Odea Ching <oching@apache.org>
 * @version
 */
public interface LdapCacheService
{
    /**
     * Retrieve LDAP user with the given username from the cache.
     * Returns null if user is not found.
     *
     * @param username
     * @return
     */
    LdapUser getUser( String username );

    /**
     * Remove LDAP user with the given username from the cache.
     * Returns the removed object if it was in the cache. Otherwise, returns null.
     * 
     * @param username
     * @return
     */
    boolean removeUser( String username );

    /**
     * Remove all LDAP users in the cache. In short, it flushes the cache.
     *
     */
    void removeAllUsers();

    /**
     * Adds the user to the LDAP users cache.
     *
     * @param user
     */
    void addUser( LdapUser user );

    /**
     * Retrieve LDAP connection for the given user from the cache.
     *
     * @param username
     * @return
     */
    LdapConnection getLdapConnection( String username );

    /**
     * Remove the LDAP connection for the given user from the cache.
     *
     * @param username
     * @return
     */
    boolean removeLdapConnection( String username );

    /**
     * Flushes all LDAP connections in the cache.
     */
    void removeAllLdapConnections();

    /**
     * Adds the LDAP connection to the LDAP connections cache.
     *
     * @param ldapConnection
     */
    void addLdapConnection( String username, LdapConnection ldapConnection );
}
