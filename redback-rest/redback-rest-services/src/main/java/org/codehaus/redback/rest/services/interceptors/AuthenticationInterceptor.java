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


import org.apache.cxf.jaxrs.ext.RequestHandler;
import org.apache.cxf.jaxrs.model.ClassResourceInfo;
import org.apache.cxf.message.Message;
import org.codehaus.plexus.redback.authentication.AuthenticationException;
import org.codehaus.plexus.redback.authorization.RedbackAuthorization;
import org.codehaus.redback.integration.filter.authentication.basic.HttpBasicAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

/**
 * This interceptor will check if the user is already logged in the session.
 * If not ask the redback system to authentication trough BASIC http
 *
 * @author Olivier Lamy
 * @since 1.3
 */
@Service( "authenticationInterceptor#rest" )
public class AuthenticationInterceptor
    extends AbstractInterceptor
    implements RequestHandler
{

    @Inject
    @Named( value = "httpAuthenticator#basic" )
    private HttpBasicAuthentication httpAuthenticator;

    private Logger log = LoggerFactory.getLogger( getClass() );

    public Response handleRequest( Message message, ClassResourceInfo classResourceInfo )
    {

        RedbackAuthorization redbackAuthorization = getRedbackAuthorization( message );
        if ( redbackAuthorization == null )
        {
            log.warn( "http path {} doesn't contain any informations regarding permissions ",
                      message.get( Message.REQUEST_URI ) );
            // here we failed to authenticate so 403 as there is no detail on karma for this
            // it must be marked as it's exposed
            return Response.status( Response.Status.FORBIDDEN ).build();
        }

        if ( redbackAuthorization.noRestriction() )
        {
            return null;
        }

        HttpServletRequest request = getHttpServletRequest( message );
        HttpServletResponse response = getHttpServletResponse( message );

        try
        {
            httpAuthenticator.authenticate( request, response );

            return null;
        }
        catch ( AuthenticationException e )
        {
            // FIXME take care about locked mustchange
            log.debug( "failed to authenticate for path {}", message.get( Message.REQUEST_URI ) );
            return Response.status( Response.Status.FORBIDDEN ).build();
        }
    }
}
