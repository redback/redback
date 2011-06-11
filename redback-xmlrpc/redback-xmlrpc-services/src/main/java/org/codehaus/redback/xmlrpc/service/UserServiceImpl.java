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

import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.redback.xmlrpc.bean.User;
import org.codehaus.redback.xmlrpc.util.BeanConverterUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UserServiceImpl
    implements UserService
{
    private UserManager userManager;

    @Inject
    public UserServiceImpl( @Named( value = "userManager#cached" ) UserManager userManager )
    {
        this.userManager = userManager;
    }

    public Boolean createUser( String username, String fullname, String email )
        throws Exception
    {
        org.codehaus.plexus.redback.users.User user = userManager.createUser( username, fullname, email );
        userManager.addUser( user );
        return Boolean.TRUE;
    }

    public Boolean deleteUser( String username )
        throws Exception
    {
        userManager.deleteUser( username );
        return Boolean.TRUE;
    }

    public User getUser( String username )
        throws Exception
    {
        org.codehaus.plexus.redback.users.User user = userManager.findUser( username );
        User simpleUser =
            new User( user.getUsername(), user.getFullName(), user.getEmail(), user.isValidated(), user.isLocked() );
        return simpleUser;
    }

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

    public Boolean updateUser( Map<String, String> userMap )
        throws Exception
    {
        User user = BeanConverterUtil.toUser( userMap );

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
