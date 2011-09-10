package org.codehaus.redback.xmlrpc.security;

/*
 * Copyright 2009 The Codehaus.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping.AuthenticationHandler;
import org.codehaus.plexus.redback.authentication.AuthenticationException;
import org.codehaus.plexus.redback.authentication.PasswordBasedAuthenticationDataSource;
import org.codehaus.plexus.redback.authorization.AuthorizationException;
import org.codehaus.plexus.redback.authorization.AuthorizationResult;
import org.codehaus.plexus.redback.policy.PolicyViolationException;
import org.codehaus.plexus.redback.system.SecuritySession;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.users.UserNotFoundException;

public class XmlRpcAuthenticator
    implements AuthenticationHandler
{
    public static final String USER_MANAGEMENT_USER_CREATE_OPERATION = "user-management-user-create";

    public static final String USER_MANAGEMENT_USER_EDIT_OPERATION = "user-management-user-edit";

    public static final String USER_MANAGEMENT_USER_DELETE_OPERATION = "user-management-user-delete";

    public static final String USER_MANAGEMENT_USER_LIST_OPERATION = "user-management-user-list";

    private final SecuritySystem securitySystem;

    private String username;

    public XmlRpcAuthenticator( SecuritySystem securitySystem )
    {
        this.securitySystem = securitySystem;
    }

    public boolean isAuthorized( XmlRpcRequest pRequest )
        throws XmlRpcException
    {
        if ( pRequest.getConfig() instanceof XmlRpcHttpRequestConfigImpl )
        {
            XmlRpcHttpRequestConfigImpl config = (XmlRpcHttpRequestConfigImpl) pRequest.getConfig();
            username = config.getBasicUserName();
            SecuritySession session = authenticate( new PasswordBasedAuthenticationDataSource( username, config
                .getBasicPassword() ) );

            String method = pRequest.getMethodName();
            AuthorizationResult result = authorize( session, method, username );

            return result.isAuthorized();
        }

        throw new XmlRpcException( "Unsupported transport (must be http)" );
    }

    private SecuritySession authenticate( PasswordBasedAuthenticationDataSource authenticationDataSource )
        throws XmlRpcException
    {
        try
        {
            return securitySystem.authenticate( authenticationDataSource );
        }
        catch ( PolicyViolationException e )
        {
            throw new XmlRpcException( 401, e.getMessage(), e );
        }
        catch ( AuthenticationException e )
        {
            throw new XmlRpcException( 401, e.getMessage(), e );
        }
        catch ( UserNotFoundException e )
        {
            throw new XmlRpcException( 401, e.getMessage(), e );
        }
    }

    private AuthorizationResult authorize( SecuritySession session, String methodName, String username )
        throws XmlRpcException
    {
        try
        {
            if ( ServiceMethodsPermissionsMapping.SERVICE_METHODS_FOR_USER.contains( methodName ) )
            {
                return securitySystem.authorize( session, "user-management-user-create" );
            }
            else if ( ServiceMethodsPermissionsMapping.SERVICE_METHODS_FOR_ROLE.contains( methodName )
                || ServiceMethodsPermissionsMapping.SERVICE_METHODS_FOR_PERMISSION.contains( methodName )
                || ServiceMethodsPermissionsMapping.SERVICE_METHODS_FOR_OPERATION.contains( methodName )
                || ServiceMethodsPermissionsMapping.SERVICE_METHODS_FOR_RESOURCE.contains( methodName ) )
            {
                return securitySystem.authorize( session, "user-management-role-grant" );
            }
            else
            {
                throw new AuthorizationException( "Unauthorized." );
            }
        }
        catch ( AuthorizationException e )
        {
            throw new XmlRpcException( 401, e.getMessage(), e );
        }
    }

    public String getActiveUser()
    {
        return username;
    }
}
