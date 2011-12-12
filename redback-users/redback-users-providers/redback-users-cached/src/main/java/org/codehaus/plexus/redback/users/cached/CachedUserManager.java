package org.codehaus.plexus.redback.users.cached;

/*
 * Copyright 2001-2006 The Codehaus.
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

import org.codehaus.plexus.cache.Cache;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserManagerListener;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.plexus.redback.users.UserQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * CachedUserManager
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
@Service( "userManager#cached" )
public class CachedUserManager
    implements UserManager, UserManagerListener
{

    private Logger log = LoggerFactory.getLogger( getClass() );

    @Inject
    @Named( value = "userManager#jdo" )
    private UserManager userImpl;

    @Inject
    @Named( value = "cache#users" )
    private Cache usersCache;

    public boolean isReadOnly()
    {
        return userImpl.isReadOnly();
    }

    public User createGuestUser()
    {
        return userImpl.createGuestUser();
    }

    public User addUser( User user )
    {
        if ( user != null )
        {
            usersCache.remove( user.getPrincipal() );
        }
        return this.userImpl.addUser( user );
    }

    public void addUserManagerListener( UserManagerListener listener )
    {
        this.userImpl.addUserManagerListener( listener );
    }

    public void addUserUnchecked( User user )
    {
        if ( user != null )
        {
            usersCache.remove( user.getPrincipal() );
        }
        this.userImpl.addUserUnchecked( user );
    }

    public User createUser( String username, String fullName, String emailAddress )
    {
        usersCache.remove( username );
        return this.userImpl.createUser( username, fullName, emailAddress );
    }

    public void deleteUser( Object principal )
        throws UserNotFoundException
    {
        usersCache.remove( principal );
        this.userImpl.deleteUser( principal );
    }

    public void deleteUser( String username )
        throws UserNotFoundException
    {
        usersCache.remove( username );
        this.userImpl.deleteUser( username );
    }

    public void eraseDatabase()
    {
        try
        {
            this.userImpl.eraseDatabase();
        }
        finally
        {
            usersCache.clear();
        }
    }

    public User findUser( String username )
        throws UserNotFoundException
    {
        if ( GUEST_USERNAME.equals( username ) )
        {
            return getGuestUser();
        }

        Object el = usersCache.get( username );
        if ( el != null )
        {
            return (User) el;
        }
        else
        {
            User user = this.userImpl.findUser( username );
            usersCache.put( username, user );
            return user;
        }
    }

    public User getGuestUser()
        throws UserNotFoundException
    {
        Object el = usersCache.get( GUEST_USERNAME );
        if ( el != null )
        {
            return (User) el;
        }
        else
        {
            User user = this.userImpl.getGuestUser();
            usersCache.put( GUEST_USERNAME, user );
            return user;
        }
    }

    public User findUser( Object principal )
        throws UserNotFoundException
    {
        Object el = usersCache.get( principal );
        if ( el != null )
        {
            return (User) el;
        }
        else
        {
            User user = this.userImpl.findUser( principal );
            usersCache.put( principal, user );
            return user;
        }
    }

    public UserQuery createUserQuery()
    {
        return userImpl.createUserQuery();
    }


    public List<User> findUsersByQuery( UserQuery query )
    {
        log.debug( "NOT CACHED - .findUsersByQuery(UserQuery)" );
        return this.userImpl.findUsersByQuery( query );
    }

    public List<User> findUsersByEmailKey( String emailKey, boolean orderAscending )
    {
        log.debug( "NOT CACHED - .findUsersByEmailKey(String, boolean)" );
        return this.userImpl.findUsersByEmailKey( emailKey, orderAscending );
    }

    public List<User> findUsersByFullNameKey( String fullNameKey, boolean orderAscending )
    {
        log.debug( "NOT CACHED - .findUsersByFullNameKey(String, boolean)" );
        return this.userImpl.findUsersByFullNameKey( fullNameKey, orderAscending );
    }

    public List<User> findUsersByUsernameKey( String usernameKey, boolean orderAscending )
    {
        log.debug( "NOT CACHED - .findUsersByUsernameKey(String, boolean)" );
        return this.userImpl.findUsersByUsernameKey( usernameKey, orderAscending );
    }

    public String getId()
    {
        return "Cached User Manager [" + this.userImpl.getId() + "]";
    }

    public List<User> getUsers()
    {
        log.debug( "NOT CACHED - .getUsers()" );
        return this.userImpl.getUsers();
    }

    public List<User> getUsers( boolean orderAscending )
    {
        log.debug( "NOT CACHED - .getUsers(boolean)" );
        return this.userImpl.getUsers( orderAscending );
    }

    public void removeUserManagerListener( UserManagerListener listener )
    {
        this.userImpl.removeUserManagerListener( listener );
    }

    public User updateUser( User user )
        throws UserNotFoundException
    {
        return updateUser( user, false );
    }

    public User updateUser( User user, boolean passwordChangeRequired )
        throws UserNotFoundException
    {
        if ( user != null )
        {
            usersCache.remove( user.getPrincipal() );
        }
        return this.userImpl.updateUser( user, passwordChangeRequired );
    }

    public boolean userExists( Object principal )
    {
        if ( usersCache.hasKey( principal ) )
        {
            return true;
        }

        return this.userImpl.userExists( principal );
    }

    public void userManagerInit( boolean freshDatabase )
    {
        if ( userImpl instanceof UserManager )
        {
            ( (UserManagerListener) this.userImpl ).userManagerInit( freshDatabase );
        }

        usersCache.clear();
    }

    public void userManagerUserAdded( User user )
    {
        if ( userImpl instanceof UserManager )
        {
            ( (UserManagerListener) this.userImpl ).userManagerUserAdded( user );
        }

        if ( user != null )
        {
            usersCache.remove( user.getPrincipal() );
        }
    }

    public void userManagerUserRemoved( User user )
    {
        if ( userImpl instanceof UserManager )
        {
            ( (UserManagerListener) this.userImpl ).userManagerUserRemoved( user );
        }

        if ( user != null )
        {
            usersCache.remove( user.getPrincipal() );
        }
    }

    public void userManagerUserUpdated( User user )
    {
        if ( userImpl instanceof UserManager )
        {
            ( (UserManagerListener) this.userImpl ).userManagerUserUpdated( user );
        }

        if ( user != null )
        {
            usersCache.remove( user.getPrincipal() );
        }
    }

    public UserManager getUserImpl()
    {
        return userImpl;
    }

    public void setUserImpl( UserManager userImpl )
    {
        this.userImpl = userImpl;
    }

    public Cache getUsersCache()
    {
        return usersCache;
    }

    public void setUsersCache( Cache usersCache )
    {
        this.usersCache = usersCache;
    }
}
