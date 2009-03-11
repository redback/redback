package org.codehaus.plexus.redback.authorization.rbac.evaluator;

/*
 * Copyright 2009 Codehaus.
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
import org.codehaus.plexus.redback.rbac.Operation;
import org.codehaus.plexus.redback.rbac.Permission;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.redback.rbac.memory.MemoryOperation;
import org.codehaus.plexus.redback.rbac.memory.MemoryPermission;
import org.codehaus.plexus.redback.rbac.memory.MemoryResource;

public class PermissionEvaluatorTest
    extends PlexusInSpringTestCase
{
    public void testNullResource()
        throws PermissionEvaluationException
    {
        // null resources should be considered as matching if any resource is obtained.
        // we do this instead of using "global" as that is the inverse - you are allocated global rights,
        // which is right to everything. null is the right to anything.

        Resource resource = new MemoryResource();
        resource.setIdentifier( "Resource" );

        Operation operation = new MemoryOperation();
        operation.setName( "Operation" );

        Permission permission = new MemoryPermission();
        permission.setName( "Permission" );
        permission.setOperation( operation );
        permission.setResource( resource );

        PermissionEvaluator eval = (PermissionEvaluator) lookup( PermissionEvaluator.ROLE );
        assertTrue( eval.evaluate( permission, "Operation", null, "brett" ) );
    }
}
