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
import java.util.Map;

import org.codehaus.redback.xmlrpc.bean.User;
import org.codehaus.redback.xmlrpc.service.UserService;

import com.atlassian.xmlrpc.AuthenticationInfo;
import com.atlassian.xmlrpc.Binder;
import com.atlassian.xmlrpc.DefaultBinder;

public class UserServiceClient
    implements UserService, ServiceClient
{
    private UserService userService;

    public UserServiceClient()
    {
    }
    
    public UserServiceClient( String url )
        throws Exception
    {
        bind( url );
    }

    public UserServiceClient( String url, String username, String password )
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
        Binder binder = new DefaultBinder();

        userService = binder.bind( UserService.class, new URL( url ), new AuthenticationInfo( username, password ) );
    }

    public Boolean createUser( String username, String fullname, String email )
        throws Exception
    {
        return userService.createUser( username, fullname, email );
    }

    public Boolean deleteUser( String username )
        throws Exception
    {
        return userService.deleteUser( username );
    }

    public User getUser( String username )
        throws Exception
    {
        return userService.getUser( username );
    }

    public List<User> getUsers()
        throws Exception
    {
        return userService.getUsers();
    }

    public Boolean updateUser( Map<String,String> userMap )
        throws Exception
    {
        return userService.updateUser( userMap );
    }

    public Boolean ping()
        throws Exception
    {
        return userService.ping();
    }

}
