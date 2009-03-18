package org.codehaus.plexus.redback.role.processor;

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

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.codehaus.plexus.redback.rbac.Permission;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.Role;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.plexus.redback.role.model.RedbackRoleModel;
import org.codehaus.plexus.redback.role.model.io.stax.RedbackRoleModelStaxReader;
import org.codehaus.plexus.redback.role.validator.RoleModelValidator;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;

/**
 * RoleProfileTest:
 *
 * @author: Jesse McConnell <jesse@codehaus.org>
 * @version: $Id:$
 */
public class RoleModelProcessorTest
    extends PlexusInSpringTestCase
{
    private RBACManager rbacManager;

    private RoleModelValidator modelValidator;
    
    private RoleModelProcessor roleProcessor;

    /**
     * Creates a new RbacStore which contains no data.
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();
 /*
        DefaultConfigurableJdoFactory jdoFactory = (DefaultConfigurableJdoFactory) lookup( JdoFactory.ROLE, "users" );
        assertEquals( DefaultConfigurableJdoFactory.class.getName(), jdoFactory.getClass().getName() );

        jdoFactory.setPersistenceManagerFactoryClass( "org.jpox.PersistenceManagerFactoryImpl" ); //$NON-NLS-1$

        jdoFactory.setDriverName( System.getProperty( "jdo.test.driver", "org.hsqldb.jdbcDriver" ) ); //$NON-NLS-1$  //$NON-NLS-2$

        jdoFactory.setUrl( System.getProperty( "jdo.test.url", "jdbc:hsqldb:mem:" + getName() ) ); //$NON-NLS-1$  //$NON-NLS-2$

        jdoFactory.setUserName( System.getProperty( "jdo.test.user", "sa" ) ); //$NON-NLS-1$

        jdoFactory.setPassword( System.getProperty( "jdo.test.pass", "" ) ); //$NON-NLS-1$

        jdoFactory.setProperty( "org.jpox.transactionIsolation", "READ_COMMITTED" ); //$NON-NLS-1$ //$NON-NLS-2$

        jdoFactory.setProperty( "org.jpox.poid.transactionIsolation", "READ_COMMITTED" ); //$NON-NLS-1$ //$NON-NLS-2$

        jdoFactory.setProperty( "org.jpox.autoCreateSchema", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
        
        jdoFactory.setProperty( "javax.jdo.option.RetainValues", "true" );

        jdoFactory.setProperty( "javax.jdo.option.RestoreValues", "true" );

        // jdoFactory.setProperty( "org.jpox.autoCreateColumns", "true" );
        
        jdoFactory.setProperty( "org.jpox.validateTables", "true" );

        jdoFactory.setProperty( "org.jpox.validateColumns", "true" );

        jdoFactory.setProperty( "org.jpox.validateConstraints", "true" );
        
        Properties properties = jdoFactory.getProperties();

        for ( Iterator it = properties.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();

            System.setProperty( (String) entry.getKey(), (String) entry.getValue() );
        }

        URL jdoFileUrls[] = new URL[] { getClass()
            .getResource( "/org/codehaus/plexus/redback/rbac/jdo/package.jdo" ) }; //$NON-NLS-1$
        
        if ( ( jdoFileUrls == null ) || ( jdoFileUrls[0] == null ) )
        {
            fail( "Unable to process test " + getName() + " - missing package.jdo." );
        }
        
        File propsFile = null; // intentional
        boolean verbose = true;

        SchemaTool.deleteSchemaTables( jdoFileUrls, new URL[] {}, propsFile, verbose );
        SchemaTool.createSchemaTables( jdoFileUrls, new URL[] {}, propsFile, verbose, null );

        PersistenceManagerFactory pmf = jdoFactory.getPersistenceManagerFactory();

        assertNotNull( pmf );

        PersistenceManager pm = pmf.getPersistenceManager();

        pm.close();

        rbacManager = (RBACManager) lookup( RBACManager.ROLE, "jdo" );
        */
        rbacManager = (RBACManager) lookup ( RBACManager.ROLE, "memory" );

        modelValidator = (RoleModelValidator) lookup ( RoleModelValidator.ROLE, "default" );
        
        roleProcessor = (RoleModelProcessor) lookup ( RoleModelProcessor.ROLE, "default" );
    }
    
    public void testProcessing() throws Exception 
    {
        RedbackRoleModel redback = getModelFromFile("/src/test/processor-tests/redback-1.xml");
        
        processModel( redback );
        
        assertTrue( rbacManager.resourceExists( "cornflakes" ) );
        
    }

    private RedbackRoleModel getModelFromFile(String file)
        throws IOException, XMLStreamException
    {
        File resource = new File( getBasedir() + file);
        
        assertNotNull( resource );
        
        RedbackRoleModelStaxReader modelReader = new RedbackRoleModelStaxReader();
        
        RedbackRoleModel redback = modelReader.read( resource.getAbsolutePath() );
        
        assertNotNull( redback );
        return redback;
    }

    private void processModel( RedbackRoleModel redback )
        throws RoleManagerException
    {
        assertTrue( modelValidator.validate( redback ) );
        
        roleProcessor.process( redback );
    }
 
    public void testMultipleProcessing() throws Exception 
    {
        rbacManager.eraseDatabase();
        
        RedbackRoleModel redback = getModelFromFile("/src/test/processor-tests/redback-2.xml");
        
        processModel( redback );
        roleProcessor.process( redback );
        
        Role systemAdmin = rbacManager.getRole( "System Administrator" );
        
        assertTrue( systemAdmin.hasChildRoles() );
    }

    /** @todo there are other things that are not synced - role descriptions, removal of operations, etc. */
    
    public void testSyncPermissionsOnUpgrade()
        throws Exception
    {
        rbacManager.eraseDatabase();
        
        processModel( getModelFromFile("/src/test/processor-tests/redback-1.xml") );

        Role role = rbacManager.getRole( "Baby" );
        assertFalse( hasPermissionOnOperation( role, "Eat Cornflakes" ) );
        assertTrue( hasPermissionOnOperation( role, "Drink Milk" ) );

        processModel( getModelFromFile("/src/test/processor-tests/redback-1-updated.xml") );
        
        role = rbacManager.getRole( "Baby" );
        assertTrue( hasPermissionOnOperation( role, "Eat Cornflakes" ) );
        assertFalse( hasPermissionOnOperation( role, "Drink Milk" ) );
    }

    private boolean hasPermissionOnOperation( Role role, String operation )
    {
        for ( Permission p : role.getPermissions() )
        {
            if ( p.getOperation().getName().equals( operation ) )
            {
                return true;
            }
        }
        return false;
    }
}