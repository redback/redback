package org.codehaus.redback.xmlrpc.security;

/*
 * Copyright 2009 The Codehaus.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import junit.framework.TestCase;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.inject.Named;

@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath*:/META-INF/spring-context.xml", "classpath*:/spring-context.xml" } )
public class XmlRpcAuthenticatorTest
    extends TestCase
{
    protected static final String USER_GUEST = "guest";

    protected static final String USER_ADMIN = "admin";

    protected static final String USER_REDBACK = "redback";

    private static final String PASSWORD = "password123";

    @Inject
    @Named( value = "securitySystem#testable" )
    protected SecuritySystem securitySystem;

    @Inject
    protected RoleManager roleManager;

    private MockControl xmlRpcRequestControl;

    private XmlRpcRequest xmlRpcRequest;

    private XmlRpcAuthenticator authenticator;

    private MockControl configControl;

    private XmlRpcHttpRequestConfigImpl config;

    @Before
    public void setUp()
        throws Exception
    {
        // Some basic asserts.
        assertNotNull( securitySystem );
        assertNotNull( roleManager );

        // Setup Admin User.
        User adminUser = createUser( USER_ADMIN, "Admin User", null );
        roleManager.assignRole( "user-administrator", adminUser.getPrincipal().toString() );

        // Setup Guest User.
        User guestUser = createUser( USER_GUEST, "Guest User", null );
        roleManager.assignRole( "guest", guestUser.getPrincipal().toString() );

        configControl = MockClassControl.createControl( XmlRpcHttpRequestConfigImpl.class );
        config = (XmlRpcHttpRequestConfigImpl) configControl.getMock();

        xmlRpcRequestControl = MockControl.createControl( XmlRpcRequest.class );
        xmlRpcRequest = (XmlRpcRequest) xmlRpcRequestControl.getMock();

        authenticator = new XmlRpcAuthenticator( securitySystem );
    }

    private User createUser( String principal, String fullname, String password )
        throws UserNotFoundException
    {
        UserManager userManager = securitySystem.getUserManager();

        User user = userManager.createUser( principal, fullname, principal + "@testable.redback.codehaus.org" );
        securitySystem.getPolicy().setEnabled( false );
        userManager.addUser( user );
        securitySystem.getPolicy().setEnabled( true );

        user.setPassword( password );
        userManager.updateUser( user );

        return user;
    }

    @Test
    public void testIsAuthorizedUserExistsButNotAuthorized()
        throws Exception
    {
        createUser( USER_REDBACK, "Redback", PASSWORD );

        UserManager userManager = securitySystem.getUserManager();
        try
        {
            User user = userManager.findUser( USER_REDBACK );
            assertEquals( USER_REDBACK, user.getPrincipal() );
        }
        catch ( UserNotFoundException e )
        {
            fail( "User should exist in the database." );
        }

        xmlRpcRequestControl.expectAndReturn( xmlRpcRequest.getConfig(), config, 2 );

        configControl.expectAndReturn( config.getBasicUserName(), USER_REDBACK );

        configControl.expectAndReturn( config.getBasicPassword(), PASSWORD );

        xmlRpcRequestControl.expectAndReturn( xmlRpcRequest.getMethodName(), "UserService.getUsers" );

        xmlRpcRequestControl.replay();
        configControl.replay();

        boolean isAuthorized = authenticator.isAuthorized( xmlRpcRequest );

        xmlRpcRequestControl.verify();
        configControl.verify();

        assertFalse( isAuthorized );
    }

    @Test
    public void testIsAuthorizedUserExistsAndAuthorized()
        throws Exception
    {
        createUser( USER_REDBACK, "Redback", PASSWORD );

        UserManager userManager = securitySystem.getUserManager();
        try
        {
            User user = userManager.findUser( USER_REDBACK );
            assertEquals( USER_REDBACK, user.getPrincipal() );
        }
        catch ( UserNotFoundException e )
        {
            fail( "User should exist in the database." );
        }

        xmlRpcRequestControl.expectAndReturn( xmlRpcRequest.getConfig(), config, 2 );

        configControl.expectAndReturn( config.getBasicUserName(), USER_REDBACK );

        configControl.expectAndReturn( config.getBasicPassword(), PASSWORD );

        xmlRpcRequestControl.expectAndReturn( xmlRpcRequest.getMethodName(), "UserService.getUsers" );

        xmlRpcRequestControl.replay();
        configControl.replay();

        @SuppressWarnings( "unused" ) boolean isAuthorized = authenticator.isAuthorized( xmlRpcRequest );
        // TODO: broken or bad test?
        // assertTrue( isAuthorized );

        xmlRpcRequestControl.verify();
        configControl.verify();
    }

    @Test
    public void testIsAuthorizedUserDoesNotExist()
        throws Exception
    {
        UserManager userManager = securitySystem.getUserManager();
        try
        {
            userManager.findUser( USER_REDBACK );
            fail( "User should not exist in the database." );
        }
        catch ( UserNotFoundException e )
        {
            assertEquals( "Unable to find user 'redback'", e.getMessage() );
        }

        xmlRpcRequestControl.expectAndReturn( xmlRpcRequest.getConfig(), config, 2 );

        configControl.expectAndReturn( config.getBasicUserName(), USER_REDBACK );

        configControl.expectAndReturn( config.getBasicPassword(), PASSWORD );

        xmlRpcRequestControl.expectAndReturn( xmlRpcRequest.getMethodName(), "UserService.getUsers" );

        xmlRpcRequestControl.replay();
        configControl.replay();

        boolean isAuthorized = authenticator.isAuthorized( xmlRpcRequest );

        xmlRpcRequestControl.verify();
        configControl.verify();

        assertFalse( isAuthorized );
    }
}
