package org.codehaus.redback.rest.services;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.redback.integration.security.role.RedbackRoleConstants;
import org.codehaus.redback.rest.api.model.User;
import org.codehaus.redback.rest.api.services.UserService;
import org.junit.Test;

/**
 * @author Olivier Lamy
 */
public class LoginServiceTest
    extends AbstractRestServicesTest
{
    @Test
    public void loginAdmin()
        throws Exception
    {
        assertTrue( getLoginService( null ).logIn( RedbackRoleConstants.ADMINISTRATOR_ACCOUNT_NAME,
                                                   FakeCreateAdminService.ADMIN_TEST_PWD ) );
    }

    @Test
    public void createUserThenLog()
        throws Exception
    {
        try
        {

            User user = new User( "toto", "toto the king", "toto@toto.fr", false, false );
            user.setPassword( "foo123" );
            UserService userService = getUserService( authorizationHeader );
            userService.createUser( user );
            user = userService.getUser( "toto" );
            assertNotNull( user );
            assertEquals( "toto the king", user.getFullName() );
            assertEquals( "toto@toto.fr", user.getEmail() );
            //getLoginService( null ).logIn( "toto", "foo123" );
            //getLoginService( null ).ping();
            getLoginService( null ).pingWithAutz();
        }
        catch ( Exception e )
        {
            getUserService( authorizationHeader ).deleteUser( "toto" );
            assertNull( getUserService( authorizationHeader ).findUser( "toto" ) );
        }
    }

}
