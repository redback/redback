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

import junit.framework.TestCase;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.codehaus.redback.integration.security.role.RedbackRoleConstants;
import org.codehaus.redback.rest.api.services.RoleManagementService;
import org.codehaus.redback.rest.api.services.UserService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;

/**
 * @author Olivier Lamy
 */
@RunWith( JUnit4.class )
//ContextConfiguration( locations = { "classpath*:/META-INF/spring-context.xml" } )
public abstract class AbstractRestServicesTest
    extends TestCase
{
    protected Logger log = LoggerFactory.getLogger( getClass() );

    public Server server = null;

    public int port;

    public String authorizationHeader =
        encode( RedbackRoleConstants.ADMINISTRATOR_ACCOUNT_NAME, FakeCreateAdminService.ADMIN_TEST_PWD );


    public static String encode( String uid, String password )
    {
        return "Basic " + Base64Utility.encode( ( uid + ":" + password ).getBytes() );
    }

    protected String getSpringConfigLocation()
    {
        return "classpath*:META-INF/spring-context.xml";
    }

    @Before
    public void startServer()
        throws Exception
    {

        this.server = new Server( 0 );

        ServletContextHandler context = new ServletContextHandler();

        context.setContextPath( "/" );

        context.setInitParameter( "contextConfigLocation", getSpringConfigLocation() );

        ContextLoaderListener contextLoaderListener = new ContextLoaderListener();

        context.addEventListener( contextLoaderListener );

        ServletHolder sh = new ServletHolder( CXFServlet.class );

        SessionHandler sessionHandler = new SessionHandler();

        context.setSessionHandler( sessionHandler );

        context.addServlet( sh, "/services/*" );
        server.setHandler( context );
        this.server.start();
        Connector connector = this.server.getConnectors()[0];
        this.port = connector.getLocalPort();
        log.info( "start server on port " + this.port );

        FakeCreateAdminService fakeCreateAdminService = getFakeCreateAdminService();

        Boolean res = fakeCreateAdminService.createAdminIfNeeded();
        assertTrue( res.booleanValue() );
    }

    protected FakeCreateAdminService getFakeCreateAdminService()
    {
        return JAXRSClientFactory.create( "http://localhost:" + port + "/services/fakeCreateAdminService/",
                                          FakeCreateAdminService.class );
    }

    @After
    public void stopServer()
        throws Exception
    {
        if ( this.server != null && this.server.isRunning() )
        {
            this.server.stop();
        }
    }

    protected UserService getUserService()
    {
        return getUserService( null );
    }

    protected UserService getUserService( String authzHeader )
    {
        UserService service =
            JAXRSClientFactory.create( "http://localhost:" + port + "/services/redbackServices/", UserService.class );

        if ( authzHeader != null )
        {
            WebClient.client( service ).header( "Authorization", authzHeader );
        }
        return service;
    }

    protected RoleManagementService getRoleManagementService( String authzHeader )
    {
        RoleManagementService service =
            JAXRSClientFactory.create( "http://localhost:" + port + "/services/redbackServices/",
                                       RoleManagementService.class );

        WebClient.getConfig( service ).getHttpConduit().getClient().setReceiveTimeout( 3000 );

        if ( authzHeader != null )
        {
            WebClient.client( service ).header( "Authorization", authzHeader );
        }
        return service;
    }
}
