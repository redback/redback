package org.codehaus.redback.rest.services.interceptors;

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


import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.codehaus.plexus.redback.authorization.AuthorizationException;
import org.codehaus.plexus.redback.authorization.RedbackAuthorization;
import org.codehaus.plexus.redback.system.SecuritySession;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.redback.integration.filter.authentication.basic.HttpBasicAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;

/**
 * @author Olivier Lamy
 * @since 1.3
 */
@Service( "permissionInterceptor#rest" )
public class PermissionsInterceptor
    implements RequestHandler
{

    @Inject
    @Named( value = "securitySystem" )
    private SecuritySystem securitySystem;

    @Inject
    @Named( value = "httpAuthenticator#basic" )
    private HttpBasicAuthentication httpAuthenticator;

    private Logger log = LoggerFactory.getLogger( getClass() );

    public Response handleRequest( Message message, ClassResourceInfo classResourceInfo )
    {
        // FIXME provide a patch to cxf to have it as a constant in Message
        Method method = ( (Method) message.get( "org.apache.cxf.resource.method" ) );
        log.debug( " method name " + method.getName() );

        // FIXME use a constant from cxf
        HttpServletRequest request = (HttpServletRequest) message.get( "HTTP.REQUEST" );

        RedbackAuthorization redbackAuthorization = method.getAnnotation( RedbackAuthorization.class );

        if ( redbackAuthorization != null )
        {
            String permission = redbackAuthorization.permission();
            if ( StringUtils.isNotBlank( permission ) )
            {
                SecuritySession session = httpAuthenticator.getSecuritySession( request.getSession() );
                if ( session != null )
                {
                    if ( session.getAuthenticationResult().isAuthenticated() )
                    {
                        try
                        {
                            if ( securitySystem.isAuthorized( session, permission,
                                                              StringUtils.isBlank( redbackAuthorization.resource() )
                                                                  ? null
                                                                  : redbackAuthorization.resource() ) )
                            {
                                return null;
                            }
                            else
                            {
                                log.debug( "user {} not authorized for permission {}", session.getUser().getPrincipal(),
                                           permission );
                            }
                        }
                        catch ( AuthorizationException e )
                        {
                            log.debug( e.getMessage(), e );
                            return Response.status( Response.Status.FORBIDDEN ).build();
                        }

                    }
                    else
                    {
                        log.debug( "user {} not authenticated", session.getUser().getUsername() );
                    }
                }
            }
        }
        // here we failed to authenticate so 403
        return Response.status( Response.Status.FORBIDDEN ).build();
    }
}
