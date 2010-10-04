package org.codehaus.redback.xmlrpc.service;

import java.util.Calendar;
import java.util.TimeZone;

import org.codehaus.plexus.cache.Cache;
import org.codehaus.plexus.redback.keys.AuthenticationKey;
import org.codehaus.plexus.redback.keys.KeyManager;
import org.codehaus.plexus.redback.keys.jdo.JdoAuthenticationKey;
import org.codehaus.plexus.redback.keys.memory.MemoryAuthenticationKey;
import org.codehaus.plexus.redback.keys.memory.MemoryKeyManager;
import org.codehaus.plexus.redback.system.SecuritySystem;

public class LoginServiceImpl
    implements LoginService
{
    private SecuritySystem securitySystem;

    /**
     * cache used for user assignments
     * 
     * @plexus.requirement role-hint="userAssignments"
     */
    private Cache userAssignmentsCache;
    
    /**
     * cache used for user permissions
     * 
     * @plexus.requirement role-hint="userPermissions"
     */
    private Cache userPermissionsCache;
    
    /**
     * Cache used for users
     * 
     * @plexus.requirement role-hint="users"
     */
    private Cache usersCache;

    public LoginServiceImpl( SecuritySystem securitySystem )
    {
        this.securitySystem = securitySystem;
    }

    public int addAuthenticationKey( String providedKey, String principal, String purpose, int expirationMinutes )
        throws Exception
    {
        KeyManager keyManager = securitySystem.getKeyManager();
        AuthenticationKey key;

        if ( keyManager instanceof MemoryKeyManager )
        {
            key = new MemoryAuthenticationKey();
        }
        else
        {
            key = new JdoAuthenticationKey();
        }

        key.setKey( providedKey );
        key.setForPrincipal( principal );
        key.setPurpose( purpose );

        Calendar now = getNowGMT();
        key.setDateCreated( now.getTime() );

        if ( expirationMinutes >= 0 )
        {
            Calendar expiration = getNowGMT();
            expiration.add( Calendar.MINUTE, expirationMinutes );
            key.setDateExpires( expiration.getTime() );
        }

        keyManager.addKey( key );

        return 0;
    }

    public int removeFromCache( String username )
        throws Exception
    {
        if ( userAssignmentsCache != null )
        {
            userAssignmentsCache.remove( username );
        }
        if ( userPermissionsCache != null )
        {
            userPermissionsCache.remove( username );
        }
        if ( usersCache != null )
        {
            usersCache.remove( username );
        }

        return 0;
    }

    public Boolean ping()
        throws Exception
    {
        return Boolean.TRUE;
    }

    private Calendar getNowGMT()
    {
        return Calendar.getInstance( TimeZone.getTimeZone( "GMT" ) );
    }
}
