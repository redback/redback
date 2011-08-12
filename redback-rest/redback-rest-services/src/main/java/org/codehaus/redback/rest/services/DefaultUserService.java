package org.codehaus.redback.rest.services;

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
import org.apache.cxf.jaxrs.model.wadl.DocTarget;
import org.codehaus.plexus.redback.authorization.RedbackAuthorization;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.redback.rest.api.model.User;
import org.codehaus.redback.rest.api.services.UserService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

//Path( "/userService/" )
@Service( "userService#rest" )
public class DefaultUserService
    implements UserService
{
    private UserManager userManager;

    @Inject
    public DefaultUserService( @Named( value = "userManager#cached" ) UserManager userManager )
    {
        this.userManager = userManager;
    }

    @Path( "createUser" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    @RedbackAuthorization( permission = "user-management-user-create" )
    public Boolean createUser( @Description( value = "the user name to create", target = DocTarget.PARAM ) @QueryParam(
        "userName" ) String userName, @QueryParam( "fullName" ) String fullName, @QueryParam( "email" ) String email )
        throws Exception
    {
        org.codehaus.plexus.redback.users.User user = userManager.createUser( userName, fullName, email );
        userManager.addUser( user );
        return Boolean.TRUE;
    }

    @Path( "deleteUser/{userName}" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    @RedbackAuthorization( permission = "user-management-user-create" )
    public Boolean deleteUser( @PathParam( "userName" ) String username )
        throws Exception
    {
        userManager.deleteUser( username );
        return Boolean.TRUE;
    }

    @Path( "getUser/{userName}" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    @RedbackAuthorization( permission = "user-management-user-create" )
    public User getUser( @PathParam( "userName" ) String username )
        throws Exception
    {
        org.codehaus.plexus.redback.users.User user = userManager.findUser( username );
        User simpleUser =
            new User( user.getUsername(), user.getFullName(), user.getEmail(), user.isValidated(), user.isLocked() );
        return simpleUser;
    }

    @Path( "getUsers" )
    @GET
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    @RedbackAuthorization( permission = "user-management-user-create" )
    public List<User> getUsers()
        throws Exception
    {
        List<org.codehaus.plexus.redback.users.User> users = userManager.getUsers();
        List<User> simpleUsers = new ArrayList<User>();

        for ( org.codehaus.plexus.redback.users.User user : users )
        {
            simpleUsers.add( new User( user.getUsername(), user.getFullName(), user.getEmail(), user.isValidated(),
                                       user.isLocked() ) );
        }

        return simpleUsers;
    }

    @Path( "updateUser" )
    @POST
    @Produces( { MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML } )
    @RedbackAuthorization( permission = "user-management-user-create" )
    public Boolean updateUser( User user )
        throws Exception
    {
        org.codehaus.plexus.redback.users.User rawUser = userManager.findUser( user.getUsername() );
        rawUser.setFullName( user.getFullname() );
        rawUser.setEmail( user.getEmail() );
        rawUser.setValidated( user.isValidated() );
        rawUser.setLocked( user.isLocked() );

        userManager.updateUser( rawUser );
        return Boolean.TRUE;
    }


    public Boolean ping()
        throws Exception
    {
        return Boolean.TRUE;
    }
}
