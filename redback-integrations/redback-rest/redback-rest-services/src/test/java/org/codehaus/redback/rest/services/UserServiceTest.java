package org.codehaus.redback.rest.services;

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

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.ServerWebApplicationException;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.redback.rest.api.model.User;
import org.codehaus.redback.rest.api.services.UserService;
import org.junit.Test;

import java.util.List;

/**
 * @author Olivier Lamy
 */
public class UserServiceTest
    extends AbstractRestServicesTest
{

    UserService getUserService()
    {
        return JAXRSClientFactory.create( "http://localhost:" + port + "/services/redbackServices/",
                                          UserService.class );
    }


    @Test
    public void ping()
        throws Exception
    {
        // 1000000L
        //WebClient.getConfig( userService ).getHttpConduit().getClient().setReceiveTimeout(3000);

        Boolean res = getUserService().ping();
        assertTrue( res.booleanValue() );
    }

    @Test
    public void getUsers()
        throws Exception
    {
        // 1000000L
        //WebClient.getConfig( userService ).getHttpConduit().getClient().setReceiveTimeout(3000);

        UserService userService = getUserService();

        WebClient.client( userService ).header( "Authorization", authorizationHeader );

        List<User> users = userService.getUsers();
        assertTrue( users != null );
        assertFalse( users.isEmpty() );
    }

    @Test( expected = ServerWebApplicationException.class )
    public void getUsersWithoutAuthz()
        throws Exception
    {
        UserService userService = getUserService();
        try
        {
            userService.getUsers();
        }
        catch ( ServerWebApplicationException e )
        {
            assertEquals( 403, e.getStatus() );
            throw e;
        }

    }

    @Test
    public void getNoPermissionNotAuthz()
        throws Exception
    {

        try
        {
            getFakeCreateAdminService().testAuthzWithoutKarmasNeededButAuthz();
        }
        catch ( ServerWebApplicationException e )
        {
            assertEquals( 403, e.getStatus() );
        }
    }

    @Test
    public void getNoPermissionAuthz()
        throws Exception
    {

        try
        {
            FakeCreateAdminService service = getFakeCreateAdminService();

            WebClient.client( service ).header( "Authorization", authorizationHeader );

            assertTrue( service.testAuthzWithoutKarmasNeededButAuthz().booleanValue() );

        }
        catch ( ServerWebApplicationException e )
        {
            assertEquals( 403, e.getStatus() );
        }
    }

}
