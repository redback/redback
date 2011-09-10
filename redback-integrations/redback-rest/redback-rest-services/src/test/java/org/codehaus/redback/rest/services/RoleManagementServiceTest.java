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

import org.apache.cxf.jaxrs.client.ServerWebApplicationException;
import org.codehaus.redback.rest.api.model.User;
import org.codehaus.redback.rest.api.services.UserService;
import org.junit.Test;

/**
 * @author Olivier Lamy
 */
public class RoleManagementServiceTest
    extends AbstractRestServicesTest
{


    @Test
    public void roleExist()
        throws Exception
    {
        assertTrue( getRoleManagementService( authorizationHeader ).roleExists( "guest" ) );
        assertFalse( getRoleManagementService( authorizationHeader ).roleExists( "foo" ) );
    }

    @Test( expected = ServerWebApplicationException.class )
    public void roleExistBadAuthz()
        throws Exception
    {
        try
        {
            assertTrue( getRoleManagementService( null ).roleExists( "guest" ) );
        }
        catch ( ServerWebApplicationException e )
        {
            assertEquals( 403, e.getStatus() );
            throw e;
        }
    }

    @Test
    public void createUserThenAssignRole()
        throws Exception
    {
        try
        {
            User user = new User( "toto", "toto the king", "toto@toto.fr", false, false );
            user.setPassword( "foo123" );
            UserService userService = getUserService( authorizationHeader );
            userService.createUser( user );
            user = userService.getUser( "toto" );
            user.setPasswordChangeRequired( false );
            userService.updateUser( user );
            assertNotNull( user );
            assertEquals( "toto the king", user.getFullName() );
            assertEquals( "toto@toto.fr", user.getEmail() );

            // should fail toto doesn't have karma
            try
            {
                getUserService( encode( "toto", "foo123" ) ).getUsers();
                fail( "should fail with 403" );
            }
            catch ( ServerWebApplicationException e )
            {
                assertEquals( 403, e.getStatus() );

            }

            // assign the role and retry
            getRoleManagementService( authorizationHeader ).assignRole( "edit-users-list", "toto" );

            userService.removeFromCache( "toto" );

            getUserService( encode( "toto", "foo123" ) ).getUsers();
        }
        finally
        {
            getUserService( authorizationHeader ).deleteUser( "toto" );
            getUserService( authorizationHeader ).removeFromCache( "toto" );
            assertNull( getUserService( authorizationHeader ).findUser( "toto" ) );
        }

    }
}
