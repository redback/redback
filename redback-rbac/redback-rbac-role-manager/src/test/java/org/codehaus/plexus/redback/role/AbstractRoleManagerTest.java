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

import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.UserAssignment;

import java.util.Iterator;
import java.util.List;

/**
 * AbstractRoleManagerTest:
 *
 * @author: Jesse McConnell <jesse@codehaus.org>
 * @version: $Id:$
 */
public abstract class AbstractRoleManagerTest extends PlexusInSpringTestCase
{

    protected RBACManager rbacManager;
    protected RoleManager roleManager;

    public AbstractRoleManagerTest()
    {
        super();
    }

    public void testLoading() throws Exception
    {       
        assertTrue( rbacManager.resourceExists( "*" ) );
        assertTrue( rbacManager.operationExists( "Test Operation" ) );
        assertTrue( rbacManager.roleExists( "Test Role" ) );
        assertTrue( rbacManager.roleExists( "Test Role 1" ) );
        assertTrue( rbacManager.roleExists( "Test Role 2" ) );
        
        assertTrue( rbacManager.roleExists( "Role for cornflake eaters drinking milk in the bowl" ) );
        
        roleManager.createTemplatedRole( "test-template-2", "foo" );
        
        assertTrue( roleManager.templatedRoleExists( "test-template-2", "foo" ) );
        assertTrue( roleManager.templatedRoleExists( "test-template", "foo" ) );
        
        roleManager.updateRole( "test-template-2", "foo", "bar" );
        
        assertTrue( roleManager.templatedRoleExists( "test-template-2", "bar" ) );
        assertTrue( roleManager.templatedRoleExists( "test-template", "bar" ) );
        
        roleManager.createTemplatedRole( "test-template-2", "hot" );
        
        assertTrue( roleManager.templatedRoleExists( "test-template-2", "hot" ) );
    }
    
    public void testUserAssignmentUpdate() throws Exception
    {
    	String principal = "joe";
    	
    	roleManager.assignRole("test-role", principal );
    	roleManager.createTemplatedRole( "test-template-2", "cold" );
    	roleManager.assignTemplatedRole( "test-template-2", "cold", principal);
    	
    	roleManager.updateRole("test-template-2", "cold", "frigid" );
    	
    	assertTrue( roleManager.templatedRoleExists("test-template-2", "frigid" ) );
    	
    	UserAssignment assignment = rbacManager.getUserAssignment( principal );
    	
    	List assignments = assignment.getRoleNames();
    	
    	assertEquals( 2, assignments.size() );
    	
    	for ( Iterator i = assignments.iterator(); i.hasNext(); )
    	{
    		String roleName = (String)i.next();
    		System.out.println( roleName );
    		assertTrue( "Test Role".equals( roleName ) || "Foo 2 - frigid".equals( roleName ) );
    	}
    }

}