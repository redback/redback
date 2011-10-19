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
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.startup.Tomcat;
import org.apache.commons.lang.StringUtils;
import org.apache.coyote.http11.Http11Protocol;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.tomcat.util.net.JIoEndpoint;
import org.codehaus.redback.integration.security.role.RedbackRoleConstants;
import org.codehaus.redback.rest.api.services.LoginService;
import org.codehaus.redback.rest.api.services.RoleManagementService;
import org.codehaus.redback.rest.api.services.UserService;
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
import org.springframework.util.ReflectionUtils;
import org.springframework.web.context.ContextLoaderListener;

import java.lang.reflect.Field;
import java.net.ServerSocket;

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

    private Tomcat tomcat;

    public int port;

    public String authorizationHeader = getAdminAuthzHeader();


    public static String encode( String uid, String password )
    {
        return "Basic " + Base64Utility.encode( ( uid + ":" + password ).getBytes() );
    }

    public static String getAdminAuthzHeader()
    {
        String adminPwdSysProps = System.getProperty( "rest.admin.pwd" );
        if ( StringUtils.isBlank( adminPwdSysProps ) )
        {
            return encode( RedbackRoleConstants.ADMINISTRATOR_ACCOUNT_NAME, FakeCreateAdminService.ADMIN_TEST_PWD );
        }
        return encode( RedbackRoleConstants.ADMINISTRATOR_ACCOUNT_NAME, adminPwdSysProps );
    }

    protected String getSpringConfigLocation()
    {
        return "classpath*:META-INF/spring-context.xml";
    }


    protected String getRestServicesPath()
    {
        return "restServices";
    }

    static boolean useTomcat = Boolean.getBoolean( "test.useTomcat" );

    @Before
    public void startServer()
        throws Exception
    {

        if ( useTomcat )
        {
            tomcat = new Tomcat();
            tomcat.setBaseDir( System.getProperty( "java.io.tmpdir" ) );

            Connector connector = new Connector()
            {
                protected void startInternal()
                    throws LifecycleException
                {
                    setState( LifecycleState.STARTING );

                    try
                    {
                        protocolHandler.start();
                    }
                    catch ( Exception e )
                    {
                        String errPrefix = "";
                        if ( this.service != null )
                        {
                            errPrefix += "service.getName(): \"" + this.service.getName() + "\"; ";
                        }

                        throw new LifecycleException(
                            errPrefix + " " + sm.getString( "coyoteConnector.protocolHandlerStartFailed" ), e );
                    }
                    mapperListener.start();
                }
            };
            connector.setPort( 0 );

            tomcat.setConnector( connector );
            tomcat.getService().addConnector( connector );

            Context context = tomcat.addContext( "", System.getProperty( "java.io.tmpdir" ) );

            ApplicationParameter applicationParameter = new ApplicationParameter();
            applicationParameter.setName( "contextConfigLocation" );
            applicationParameter.setValue( getSpringConfigLocation() );
            context.addApplicationParameter( applicationParameter );

            context.addApplicationListener( ContextLoaderListener.class.getName() );

            Tomcat.addServlet( context, "cxf", new CXFServlet() );
            context.addServletMapping( "/" + getRestServicesPath() + "/*", "cxf" );

            tomcat.start();

            Http11Protocol http11Protocol = ( (Http11Protocol) tomcat.getConnector().getProtocolHandler() );

            Field fieldEndpoint = ReflectionUtils.findField( Http11Protocol.class, "endpoint" );
            fieldEndpoint.setAccessible( true );
            JIoEndpoint jIoEndpoint = (JIoEndpoint) fieldEndpoint.get( http11Protocol );

            Field serverSocketField = ReflectionUtils.findField( JIoEndpoint.class, "serverSocket" );
            serverSocketField.setAccessible( true );
            ServerSocket serverSocket = (ServerSocket) serverSocketField.get( jIoEndpoint );

            this.port = serverSocket.getLocalPort();
        }
        else
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

            context.addServlet( sh, "/" + getRestServicesPath() + "/*" );
            server.setHandler( context );
            this.server.start();
            org.eclipse.jetty.server.Connector connector = this.server.getConnectors()[0];
            this.port = connector.getLocalPort();

        }

        log.info( "start server on port " + this.port );
        FakeCreateAdminService fakeCreateAdminService = getFakeCreateAdminService();

        Boolean res = fakeCreateAdminService.createAdminIfNeeded();
        assertTrue( res.booleanValue() );


    }

    protected FakeCreateAdminService getFakeCreateAdminService()
    {
        return JAXRSClientFactory.create(
            "http://localhost:" + port + "/" + getRestServicesPath() + "/fakeCreateAdminService/",
            FakeCreateAdminService.class );
    }

    @After
    public void stopServer()
        throws Exception
    {

        if ( useTomcat )
        {
            tomcat.stop();
        }
        else
        {
            if ( this.server != null && this.server.isRunning() )
            {
                this.server.stop();
            }
        }
    }

    protected UserService getUserService()
    {
        return getUserService( null );
    }

    protected UserService getUserService( String authzHeader )
    {
        UserService service =
            JAXRSClientFactory.create( "http://localhost:" + port + "/" + getRestServicesPath() + "/redbackServices/",
                                       UserService.class );

        // for debuging purpose
        WebClient.getConfig( service ).getHttpConduit().getClient().setReceiveTimeout( 100000 );

        if ( authzHeader != null )
        {
            WebClient.client( service ).header( "Authorization", authzHeader );
        }
        return service;
    }

    protected RoleManagementService getRoleManagementService( String authzHeader )
    {
        RoleManagementService service =
            JAXRSClientFactory.create( "http://localhost:" + port + "/" + getRestServicesPath() + "/redbackServices/",
                                       RoleManagementService.class );

        // for debuging purpose
        WebClient.getConfig( service ).getHttpConduit().getClient().setReceiveTimeout( 100000 );

        if ( authzHeader != null )
        {
            WebClient.client( service ).header( "Authorization", authzHeader );
        }
        return service;
    }

    protected LoginService getLoginService( String authzHeader )
    {
        LoginService service =
            JAXRSClientFactory.create( "http://localhost:" + port + "/" + getRestServicesPath() + "/redbackServices/",
                                       LoginService.class );

        // for debuging purpose
        WebClient.getConfig( service ).getHttpConduit().getClient().setReceiveTimeout( 100000 );

        if ( authzHeader != null )
        {
            WebClient.client( service ).header( "Authorization", authzHeader );
        }
        return service;
    }

}
