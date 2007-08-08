package org.codehaus.plexus.redback.users.ldap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserManagerListener;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.plexus.redback.users.UserQuery;
import org.codehaus.plexus.redback.users.ldap.ctl.LdapController;
import org.codehaus.plexus.redback.users.ldap.ctl.LdapControllerException;
import org.codehaus.plexus.redback.users.ldap.mapping.MappingException;
import org.codehaus.plexus.redback.users.ldap.mapping.UserMapper;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.DirContext;

public class LdapUserManager
    implements UserManager, LogEnabled
{

    private List<UserManagerListener> listeners = new ArrayList<UserManagerListener>();

    private Logger logger;

    private LdapController controller;

    private UserMapper mapper;

    public void addUserManagerListener( UserManagerListener listener )
    {
        if ( !listeners.contains( listener ) )
        {
            listeners.add( listener );
        }
    }

    public void removeUserManagerListener( UserManagerListener listener )
    {
        listeners.remove( listener );
    }

    protected void fireUserAdded( User addedUser )
    {
        for ( UserManagerListener listener : listeners )
        {
            try
            {
                listener.userManagerUserAdded( addedUser );
            }
            catch ( Exception e )
            {
                getLogger().debug( "Failed to fire user-added event to user-manager: " + e.getMessage(), e );
            }
        }
    }

    protected void fireUserRemoved( User removedUser )
    {
        for ( UserManagerListener listener : listeners )
        {
            try
            {
                listener.userManagerUserRemoved( removedUser );
            }
            catch ( Exception e )
            {
                getLogger().debug( "Failed to fire user-removed event to user-manager: " + e.getMessage(), e );
            }
        }
    }

    protected void fireUserUpdated( User updatedUser )
    {
        for ( UserManagerListener listener : listeners )
        {
            try
            {
                listener.userManagerUserUpdated( updatedUser );
            }
            catch ( Exception e )
            {
                getLogger().debug( "Failed to fire user-updated event to user-manager: " + e.getMessage(), e );
            }
        }
    }

    public User addUser( User user )
    {
        DirContext context = newDirContext();
        try
        {
            controller.createUser( user, context, true );
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

    public void addUserUnchecked( User user )
    {
        DirContext context = newDirContext();
        try
        {
            controller.createUser( user, context, false );
        }
        catch ( LdapControllerException e )
        {
            getLogger().error( "Error mapping user: " + user.getPrincipal() + " to LDAP attributes.", e );
        }
        catch ( MappingException e )
        {
            getLogger().error( "Error mapping user: " + user.getPrincipal() + " to LDAP attributes.", e );
        }
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
        // TODO Auto-generated method stub
        return null;
    }

    public User findUser( Object principal )
        throws UserNotFoundException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    public List findUsersByEmailKey( String emailKey, boolean orderAscending )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    public List findUsersByFullNameKey( String fullNameKey, boolean orderAscending )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    public List findUsersByQuery( UserQuery query )
    {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    public List findUsersByUsernameKey( String usernameKey, boolean orderAscending )
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getId()
    {
        return "LDAP User-Manager: " + getClass().getName();
    }

    @SuppressWarnings("unchecked")
    public List getUsers()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressWarnings("unchecked")
    public List getUsers( boolean orderAscending )
    {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Implement DirContext creation.
        return null;
    }

    protected Logger getLogger()
    {
        if ( logger == null )
        {
            logger = new ConsoleLogger( Logger.LEVEL_DEBUG, "internally-initialized" );
        }

        return logger;
    }


    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

}
