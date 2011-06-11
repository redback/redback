package org.codehaus.redback.xmlrpc.service;

/*
 * Copyright 2009 The Codehaus.
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

import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.redback.xmlrpc.bean.Operation;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

@Service
public class OperationServiceImpl
    implements OperationService
{
    private RBACManager rbacManager;

    @Inject
    public OperationServiceImpl( @Named( value = "rBACManager#cached" ) RBACManager rbacManager )
    {
        this.rbacManager = rbacManager;
    }

    public Boolean createOperation( String operationName )
        throws Exception
    {
        rbacManager.saveOperation( rbacManager.createOperation( operationName ) );
        return Boolean.TRUE;
    }

    public Operation getOperation( String operationName )
        throws Exception
    {
        org.codehaus.plexus.redback.rbac.Operation operation = rbacManager.getOperation( operationName );
        Operation simpleOperation =
            new Operation( operation.getName(), operation.getDescription(), operation.isPermanent() );
        return simpleOperation;
    }

    public List<Operation> getOperations()
        throws Exception
    {
        List<org.codehaus.plexus.redback.rbac.Operation> operations = rbacManager.getAllOperations();
        List<Operation> simpleOperations = new ArrayList<Operation>();

        for ( org.codehaus.plexus.redback.rbac.Operation operation : operations )
        {
            simpleOperations.add(
                new Operation( operation.getName(), operation.getDescription(), operation.isPermanent() ) );
        }

        return simpleOperations;
    }

    public Boolean removeOperation( String operationName )
        throws Exception
    {
        rbacManager.removeOperation( operationName );
        return Boolean.TRUE;
    }

    public Boolean ping()
        throws Exception
    {
        return Boolean.TRUE;
    }
}
