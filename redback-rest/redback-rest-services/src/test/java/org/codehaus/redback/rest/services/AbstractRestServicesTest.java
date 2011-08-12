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
import org.apache.cxf.transport.servlet.CXFServlet;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.context.ContextLoaderListener;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Olivier Lamy
 */
@RunWith( JUnit4.class )
//ContextConfiguration( locations = { "classpath*:/META-INF/spring-context.xml" } )
public abstract class AbstractRestServicesTest
    extends TestCase
{
    private Logger log = LoggerFactory.getLogger( getClass() );

    Server server = null;

    int port;

    @Before
    public void startServer()
        throws Exception
    {

        this.server = new Server( 0 );

        ServletContextHandler context = new ServletContextHandler( );

        context.setContextPath( "/" );

        context.setInitParameter( "contextConfigLocation", "classpath*:META-INF/spring-context.xml" );

        ContextLoaderListener contextLoaderListener = new ContextLoaderListener();

        context.addEventListener( contextLoaderListener );

        ServletHolder sh = new ServletHolder( CXFServlet.class );

        context.addServlet( sh, "/services/*" );
        server.setHandler( context );
        this.server.start();
        Connector connector = this.server.getConnectors()[0];
        this.port = connector.getLocalPort();
        log.info( "start server on port " + this.port );


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
}