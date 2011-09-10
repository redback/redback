package org.codehaus.redback.rest.api.services;

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

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.codehaus.plexus.redback.authorization.RedbackAuthorization;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path( "/loginService/" )
public interface LoginService
{

    @Path( "addAuthenticationKey" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN } )
    @RedbackAuthorization( noRestriction = true )
    String addAuthenticationKey( @QueryParam( "providerKey" ) String providedKey,
                                 @QueryParam( "principal" ) String principal, @QueryParam( "purpose" ) String purpose,
                                 @QueryParam( "expirationMinutes" ) int expirationMinutes )
        throws RedbackServiceException;


    @Path( "ping" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN } )
    @RedbackAuthorization( noRestriction = true )
    Boolean ping()
        throws RedbackServiceException;


    @Path( "pingWithAutz" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN } )
    @RedbackAuthorization( noRestriction = false, noPermission = true )
    Boolean pingWithAutz()
        throws RedbackServiceException;

    @Path( "logIn" )
    @GET
    @Description( "only to test if username/password can connect" )
    @RedbackAuthorization( noRestriction = true, noPermission = true )
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN } )
    Boolean logIn( @QueryParam( "userName" ) String userName, @QueryParam( "password" ) String password )
        throws RedbackServiceException;
}
