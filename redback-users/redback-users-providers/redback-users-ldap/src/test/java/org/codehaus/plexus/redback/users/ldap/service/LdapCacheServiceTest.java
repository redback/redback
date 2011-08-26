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
import org.codehaus.plexus.spring.PlexusInSpringTestCase;

/**
 * @author: Maria Odea Ching <oching@apache.org>
 * @version
 */
public class LdapCacheServiceTest
    extends PlexusInSpringTestCase
{
    private LdapCacheService ldapCacheService;

    private static final String USERNAME = "dummy";

    public void setUp()
        throws Exception
    {
        super.setUp();

        ldapCacheService = (LdapCacheService) lookup( LdapCacheService.class.getName() );
    }

    public void tearDown()
        throws Exception
    {
        ldapCacheService.removeAllUsers();
        ldapCacheService.removeAllLdapUserDn();

        super.tearDown();
    }

    public void testLdapUserDnCache()
        throws Exception
    {
        ldapCacheService.addLdapUserDn( USERNAME, "userDn" );

        assertNotNull( ldapCacheService.getLdapUserDn( USERNAME ) );

        ldapCacheService.removeLdapUserDn( USERNAME );

        assertNull( ldapCacheService.getLdapUserDn( USERNAME ) );
    }

    public void testClearLdapUserDnCache()
        throws Exception
    {
        ldapCacheService.addLdapUserDn( USERNAME, "userDn" );

        assertNotNull( ldapCacheService.getLdapUserDn( USERNAME ) );

        ldapCacheService.removeLdapUserDn( USERNAME );

        assertNull( ldapCacheService.getLdapUserDn( USERNAME ) );
    }

    public void testLdapUsersCache()
        throws Exception
    {
        LdapUser ldapUser = new LdapUser( USERNAME );

        ldapCacheService.addUser( ldapUser );

        assertNotNull( ldapCacheService.getUser( USERNAME ) );

        ldapCacheService.removeUser( USERNAME );

        assertNull( ldapCacheService.getUser( USERNAME ) );
    }

    public void testClearLdapUsersCache()
        throws Exception
    {
        LdapUser ldapUser = new LdapUser( USERNAME );

        ldapCacheService.addUser( ldapUser );

        assertNotNull( ldapCacheService.getUser( USERNAME ) );

        ldapCacheService.removeAllUsers();

        assertNull( ldapCacheService.getUser( USERNAME ) );
    }
}
