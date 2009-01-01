package org.codehaus.plexus.redback.users.ldap;
/*
 * Copyright 2001-2007 The Codehaus.
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


import org.codehaus.plexus.redback.common.ldap.MappingException;
import org.codehaus.plexus.redback.common.ldap.UserMapper;
import org.codehaus.plexus.redback.common.ldap.connection.LdapConnection;
import org.codehaus.plexus.redback.common.ldap.connection.LdapConnectionFactory;
import org.codehaus.plexus.redback.common.ldap.connection.LdapException;
import org.codehaus.plexus.redback.users.AbstractUserManager;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.plexus.redback.users.UserQuery;
import org.codehaus.plexus.redback.users.ldap.ctl.LdapController;
import org.codehaus.plexus.redback.users.ldap.ctl.LdapControllerException;

import javax.naming.directory.DirContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="jesse@codehaus.org"> jesse
 * @version "$Id$"
 * @plexus.component role="org.codehaus.plexus.redback.users.UserManager" role-hint="ldap"
 */
public class LdapUserManager
    extends AbstractUserManager
{
    /**
     * @plexus.requirement role-hint="configurable"
     */
    private LdapConnectionFactory connectionFactory;

    /**
     * @plexus.requirement role-hint="default"
     */
    private LdapController controller;

    /**
     * @plexus.requirement role-hint="ldap"
     */
    private UserMapper mapper;

    private User guestUser;

    public boolean isReadOnly()
    {
        return true;
    }

    public User addUser( User user )
    {
        return addUser( user, true );
    }

    public void addUserUnchecked( User user )
    {
        addUser( user, false );
    }

    private User addUser( User user, boolean checked )
    {
        if ( user == null )
        {
            return null;
        }

        if ( GUEST_USERNAME.equals( user.getUsername() ) )
        {
            guestUser = user;
            return guestUser;
        }

        DirContext context = newDirContext();
        try
        {
            controller.createUser( user, context, checked );
        }
        catch ( LdapControllerException e )
        {
            getLogger().error( "Error mapping user: " + user.getPrincipal() + " to LDAP attributes.", e );
        }
        catch ( MappingException e )
        {
            getLogger().error( "Error mapping user: " + user.getPrincipal() + " to LDAP attributes.", e );
        }
        return user;
    }

    public User createUser( String username, String fullName, String emailAddress )
    {
        return mapper.newUserInstance( username, fullName, emailAddress );
    }

    public UserQuery createUserQuery()
    {
        // TODO Implement queries!
        return null;
    }

    public void deleteUser( Object principal )
        throws UserNotFoundException
    {
        try
        {
            controller.removeUser( principal, newDirContext() );
        }
        catch ( LdapControllerException e )
        {
            getLogger().error( "Failed to delete user: " + principal, e );
        }
    }

    public void deleteUser( String username )
        throws UserNotFoundException
    {
        try
        {
            controller.removeUser( username, newDirContext() );
        }
        catch ( LdapControllerException e )
        {
            getLogger().error( "Failed to delete user: " + username, e );
        }
    }

    public void eraseDatabase()
    {
        // TODO Implement erase!
    }

    public User findUser( String username )
        throws UserNotFoundException
    {
        if ( username == null )
        {
            throw new UserNotFoundException( "Unable to find user based on null username." );
        }

        if ( GUEST_USERNAME.equals( username ) )
        {
            return getGuestUser();
        }

        try
        {
            User user = controller.getUser( username, newDirContext() );
            if ( user == null )
            {
                throw new UserNotFoundException( "user with name " + username + " not found " );
            }
            return user;
        }
        catch ( LdapControllerException e )
        {
            getLogger().error( "Failed to find user: " + username, e );
            return null;
        }
        catch ( MappingException e )
        {
            getLogger().error( "Failed to map user: " + username, e );
            return null;
        }
    }

    public User getGuestUser()
        throws UserNotFoundException
    {
        if ( guestUser == null )
        {
            throw new UserNotFoundException( "Guest user doesn't exist." );
        }
        return guestUser;
    }

    public User findUser( Object principal )
        throws UserNotFoundException
    {
        if ( principal == null )
        {
            throw new UserNotFoundException( "Unable to find user based on null principal." );
        }

        if ( GUEST_USERNAME.equals( principal.toString() ) )
        {
            return getGuestUser();
        }

        try
        {
            return controller.getUser( principal, newDirContext() );
        }
        catch ( LdapControllerException e )
        {
            getLogger().error( "Failed to find user: " + principal, e );
            return null;
        }
        catch ( MappingException e )
        {
            getLogger().error( "Failed to map user: " + principal, e );
            return null;
        }
    }

    public List<User> findUsersByEmailKey( String emailKey, boolean orderAscending )
    {
        getLogger().warn( "findUsersByEmailKey not implemented in ldap empty list returned"  );
        return Collections.emptyList();
    }

    public List<User> findUsersByFullNameKey( String fullNameKey, boolean orderAscending )
    {
        getLogger().warn( "findUsersByEmailKey not implemented in ldap empty list returned"  );
        return Collections.emptyList();
    }

    public List<User> findUsersByQuery( UserQuery query )
    {
        getLogger().warn( "findUsersByEmailKey not implemented in ldap empty list returned"  );
        return Collections.emptyList();
    }

    /** 
     * @see org.codehaus.plexus.redback.users.UserManager#findUsersByUsernameKey(java.lang.String, boolean)
     */
    public List<User> findUsersByUsernameKey( String usernameKey, boolean orderAscending )
    {
        getLogger().warn( "findUsersByEmailKey not implemented in ldap empty list returned"  );
        return Collections.emptyList();
    }

    public String getId()
    {
        return "LDAP User-Manager: " + getClass().getName();
    }

    /** 
     * @see org.codehaus.plexus.redback.users.UserManager#getUsers()
     */
    public List<User> getUsers()
    {
        try
        {
            List<User> users = new ArrayList<User>();
            users.addAll( controller.getUsers( newDirContext() ) );
            //We add the guest user because it isn't in LDAP
            try
            {
                User u = getGuestUser();
                if ( u != null )
                {
                    users.add( u );
                }
            }
            catch ( UserNotFoundException e )
            {
                //Nothing to do
            }
            return users;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return null;
    }

    public List<User> getUsers( boolean orderAscending )
    {
        return getUsers();
    }

    public User updateUser( User user )
        throws UserNotFoundException
    {
        try
        {
            controller.updateUser( user, newDirContext() );
        }
        catch ( LdapControllerException e )
        {
            getLogger().error( "Failed to update user: " + user.getPrincipal(), e );
        }
        catch ( MappingException e )
        {
            getLogger().error( "Failed to update user: " + user.getPrincipal(), e );
        }

        return user;
    }

    public boolean userExists( Object principal )
    {
        DirContext context = newDirContext();
        try
        {
            return controller.userExists( principal, context );
        }
        catch ( LdapControllerException e )
        {
            getLogger().debug( "Failed to search for user: " + principal, e );
            return false;
        }
    }

    private DirContext newDirContext()
    {
        try
        {
            LdapConnection connection = connectionFactory.getConnection();

            return connection.getDirContext();
        }
        catch ( LdapException le )
        {
            le.printStackTrace();
            return null;
        }
    }

    public LdapConnectionFactory getConnectionFactory()
    {
        return connectionFactory;
    }

    public void setConnectionFactory( LdapConnectionFactory connectionFactory )
    {
        this.connectionFactory = connectionFactory;
    }

}
