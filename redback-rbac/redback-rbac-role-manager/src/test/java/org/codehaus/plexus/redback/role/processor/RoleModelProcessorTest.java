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

        modelValidator = (RoleModelValidator) lookup ( RoleModelValidator.ROLE, "default" );
        
        roleProcessor = (RoleModelProcessor) lookup ( RoleModelProcessor.ROLE, "default" );

        rbacManager = (RBACManager) lookup ( RBACManager.ROLE, "memory" );
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