package org.codehaus.plexus.redback.common.ldap.connection;

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

import org.codehaus.plexus.spring.PlexusInSpringTestCase;

public class ConfigurableLdapConnectionFactoryTest
    extends PlexusInSpringTestCase
{
    public void testConfiguration()
    {
        ConfigurableLdapConnectionFactory factory =
            (ConfigurableLdapConnectionFactory) lookup( ConfigurableLdapConnectionFactory.ROLE, "configurable" );

        assertEquals( "dc=codehaus,dc=org", factory.getConfiguration().getBaseDn().toString() );
        assertEquals( "uid=user,dc=codehaus,dc=org", factory.getConfiguration().getBindDn().toString() );
    }
}
