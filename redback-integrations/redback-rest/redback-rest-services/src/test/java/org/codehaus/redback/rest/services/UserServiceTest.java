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
import org.codehaus.redback.rest.services.mock.EmailMessage;
import org.codehaus.redback.rest.services.mock.ServicesAssert;
import org.junit.Test;

import java.util.List;

/**
 * @author Olivier Lamy
 */
public class UserServiceTest
    extends AbstractRestServicesTest
{


    @Test
    public void ping()
        throws Exception
    {
        Boolean res = getUserService().ping();
        assertTrue( res.booleanValue() );
    }

    @Test
    public void getUsers()
        throws Exception
    {
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

    @Test
    public void register()
        throws Exception
    {
        try
        {
            UserService service = getUserService();
            User u = new User();
            u.setFullName( "the toto" );
            u.setUsername( "toto" );
            u.setEmail( "toto@toto.fr" );
            u.setPassword( "toto123" );
            u.setConfirmPassword( "toto123" );
            u = service.registerUser( u );

            ServicesAssert assertService =
                JAXRSClientFactory.create( "http://localhost:" + port + "/" + getRestServicesPath() + "/testsService/",
                                           ServicesAssert.class );

            //log.info( "emails " + assertService.getEmailMessageSended() );
            List<EmailMessage> emailMessages = assertService.getEmailMessageSended();
            assertEquals( 1, emailMessages.size() );
            assertEquals( "toto@toto.fr", emailMessages.get( 0 ).getTos().get( 0 ) );
            assertEquals( "olamy@localhost", emailMessages.get( 0 ).getFrom() );
            assertEquals( "Welcome", emailMessages.get( 0 ).getSubject() );
            assertTrue(
                emailMessages.get( 0 ).getText().contains( "Use the following URL to validate your account." ) );

        }
        catch ( Exception e )
        {
            log.error( e.getMessage(), e );
            throw e;
        }

    }

    public void guestUserCreate()
        throws Exception
    {
        UserService userService = getUserService( authorizationHeader );
        assertNull( userService.getGuestUser() );
        assertNull( userService.createGuestUser() );

    }

}
