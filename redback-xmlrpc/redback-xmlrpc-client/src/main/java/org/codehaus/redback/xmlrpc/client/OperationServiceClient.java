package org.codehaus.redback.xmlrpc.client;

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

import java.net.URL;
import java.util.List;
import java.util.TimeZone;

import org.codehaus.redback.xmlrpc.bean.Operation;
import org.codehaus.redback.xmlrpc.service.OperationService;

import com.atlassian.xmlrpc.ApacheBinder;
import com.atlassian.xmlrpc.Binder;
import com.atlassian.xmlrpc.ConnectionInfo;

public class OperationServiceClient
    implements OperationService, ServiceClient
{
    private OperationService operationService;

    public OperationServiceClient()
    {
    }

    public OperationServiceClient( String url )
        throws Exception
    {
        bind( url );
    }

    public OperationServiceClient( String url, String username, String password )
        throws Exception
    {
        bind( url, username, password );
    }

    public void bind( String url )
        throws Exception
    {
        bind( url, "", "" );
    }

    public void bind( String url, String username, String password )
        throws Exception
    {
        Binder binder = new ApacheBinder();

        ConnectionInfo info = new ConnectionInfo();
        info.setUsername( username );
        info.setPassword( password );
        info.setTimeZone( TimeZone.getDefault() );

        operationService = binder.bind( OperationService.class, new URL( url ), info );
    }

    public Boolean createOperation( String operationName )
        throws Exception
    {
        return operationService.createOperation( operationName );
    }

    public Operation getOperation( String operationName )
        throws Exception
    {
        return operationService.getOperation( operationName );
    }

    public List<Operation> getOperations()
        throws Exception
    {
        return operationService.getOperations();
    }

    public Boolean removeOperation( String operationName )
        throws Exception
    {
        return operationService.removeOperation( operationName );
    }

    public Boolean ping()
        throws Exception
    {
        return operationService.ping();
    }
}
