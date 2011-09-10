package org.codehaus.redback.rest.api.services;

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

import org.apache.cxf.jaxrs.model.wadl.Description;
import org.apache.cxf.jaxrs.model.wadl.DocTarget;
import org.codehaus.plexus.redback.authorization.RedbackAuthorization;
import org.codehaus.redback.rest.api.model.User;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path( "/userService/" )
public interface UserService
{
    @Path( "getUser/{userName}" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    @RedbackAuthorization( permission = "user-management-user-edit" )
    User getUser( @PathParam( "userName" ) String username )
        throws RedbackServiceException;

    @Path( "getUsers" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    @RedbackAuthorization( permission = "user-management-user-list" )
    List<User> getUsers()
        throws RedbackServiceException;

    @Path( "createUser" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN } )
    @RedbackAuthorization( permission = "user-management-user-create" )
    Boolean createUser( @Description( value = "the user name to create", target = DocTarget.PARAM ) @QueryParam(
        "userName" ) String userName, @QueryParam( "fullName" ) String fullName, @QueryParam( "email" ) String email )
        throws RedbackServiceException;

    @Path( "deleteUser/{userName}" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN } )
    @RedbackAuthorization( permission = "user-management-user-delete" )
    Boolean deleteUser( @PathParam( "userName" ) String username )
        throws RedbackServiceException;

    @Path( "updateUser" )
    @POST
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN } )
    @RedbackAuthorization( permission = "user-management-user-edit" )
    Boolean updateUser( User user )
        throws RedbackServiceException;

    @Path( "ping" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_PLAIN } )
    @RedbackAuthorization( noRestriction = true )
    Boolean ping()
        throws RedbackServiceException;
}
