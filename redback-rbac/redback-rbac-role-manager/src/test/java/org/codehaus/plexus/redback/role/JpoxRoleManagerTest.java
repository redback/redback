package org.codehaus.plexus.redback.role;

/*
 * Copyright 2005 The Codehaus.
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

import net.sf.ehcache.CacheManager;
import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.jdo.JdoRbacManager;
import org.codehaus.plexus.redback.rbac.jdo.JdoTool;
import org.jpox.SchemaTool;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * RoleManagerTest:
 *
 * @author: Jesse McConnell <jesse@codehaus.org>
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath*:/META-INF/spring-context.xml", "classpath:/jpox-spring-context.xml" } )
public class JpoxRoleManagerTest
    extends AbstractRoleManagerTest
{
    @Inject
    @Named( value = "jdoFactory#users" )
    DefaultConfigurableJdoFactory jdoFactory;

    @Inject
    @Named( value = "rBACManager#jdo" )
    JdoRbacManager rbacManagerJdo;

    @Inject @Named(value = "roleManager#jpox")
    DefaultRoleManager roleManagerInjected;

    /**
     * Creates a new RbacStore which contains no data.
     */
    @Before
    public void setUp()
        throws Exception
    {

        CacheManager.getInstance().clearAll();

        super.setUp();

        jdoFactory.setPersistenceManagerFactoryClass( "org.jpox.PersistenceManagerFactoryImpl" ); //$NON-NLS-1$

        jdoFactory.setDriverName(
            System.getProperty( "jdo.test.driver", "org.hsqldb.jdbcDriver" ) ); //$NON-NLS-1$  //$NON-NLS-2$

        jdoFactory.setUrl(
            System.getProperty( "jdo.test.url", "jdbc:hsqldb:mem:" + getName() ) ); //$NON-NLS-1$  //$NON-NLS-2$

        jdoFactory.setUserName( System.getProperty( "jdo.test.user", "sa" ) ); //$NON-NLS-1$

        jdoFactory.setPassword( System.getProperty( "jdo.test.pass", "" ) ); //$NON-NLS-1$

        jdoFactory.setProperty( "org.jpox.transactionIsolation", "READ_UNCOMMITTED" ); //$NON-NLS-1$ //$NON-NLS-2$

        jdoFactory.setProperty( "org.jpox.poid.transactionIsolation", "READ_UNCOMMITTED" ); //$NON-NLS-1$ //$NON-NLS-2$

        jdoFactory.setProperty( "org.jpox.autoCreateSchema", "true" ); //$NON-NLS-1$ //$NON-NLS-2$

        jdoFactory.setProperty( "org.jpox.autoCreateTables", "true" );

        jdoFactory.setProperty( "javax.jdo.option.RetainValues", "true" );

        jdoFactory.setProperty( "javax.jdo.option.RestoreValues", "true" );

        // jdoFactory.setProperty( "org.jpox.autoCreateColumns", "true" );

        jdoFactory.setProperty( "org.jpox.validateTables", "true" );

        jdoFactory.setProperty( "org.jpox.validateColumns", "true" );

        jdoFactory.setProperty( "org.jpox.validateConstraints", "true" );

        Properties properties = jdoFactory.getProperties();

        for ( Map.Entry<Object, Object> entry : properties.entrySet() )
        {
            System.setProperty( (String) entry.getKey(), (String) entry.getValue() );
        }

        URL jdoFileUrls[] =
            new URL[]{ getClass().getResource( "/org/codehaus/plexus/redback/rbac/jdo/package.jdo" ) }; //$NON-NLS-1$

        if ( ( jdoFileUrls == null ) || ( jdoFileUrls[0] == null ) )
        {
            fail( "Unable to process test " + getName() + " - missing package.jdo." );
        }

        File propsFile = null; // intentional
        boolean verbose = true;

        SchemaTool.deleteSchemaTables( jdoFileUrls, new URL[]{ }, propsFile, verbose );
        SchemaTool.createSchemaTables( jdoFileUrls, new URL[]{ }, propsFile, verbose, null );

        PersistenceManagerFactory pmf = jdoFactory.getPersistenceManagerFactory();

        assertNotNull( pmf );

        PersistenceManager pm = pmf.getPersistenceManager();

        pm.close();

        rbacManagerJdo.eraseDatabase();

        setRbacManager( rbacManagerJdo );

        roleManagerInjected.initialize();

        setRoleManager( roleManagerInjected );
    }

}