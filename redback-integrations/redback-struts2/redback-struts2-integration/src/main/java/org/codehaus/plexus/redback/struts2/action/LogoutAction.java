package org.codehaus.plexus.redback.struts2.action;

/*
 * Copyright 2005-2006 The Codehaus.
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

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.SessionMap;
import org.codehaus.plexus.ehcache.EhcacheComponent;
import org.codehaus.plexus.redback.struts2.interceptor.SecureActionBundle;
import org.codehaus.plexus.redback.struts2.interceptor.SecureActionException;
import org.codehaus.redback.integration.util.AutoLoginCookies;

/**
 * LogoutAction
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action"
 * role-hint="redback-logout"
 * instantiation-strategy="per-lookup"
 */
public class LogoutAction
    extends AbstractSecurityAction
{
    // Result Names.
    private static final String LOGOUT = "security-logout";

    /**
     * cache used for user assignments
     * 
     * @plexus.requirement role-hint="userAssignments"
     */
    private EhcacheComponent userAssignmentsCache;
    
    /**
     * cache used for user permissions
     * 
     * @plexus.requirement role-hint="userPermissions"
     */
    private EhcacheComponent userPermissionsCache;
    
    /**
     * Cache used for users
     * 
     * @plexus.requirement role-hint="users"
     */
    private EhcacheComponent usersCache;
    
    /**
     * @plexus.requirement
     */
    private AutoLoginCookies autologinCookies;

    public String logout()
    {
        if ( getSecuritySession() != null )
        {            
            // [PLXREDBACK-65] this is a bit of a hack around the cached managers since they don't have the ability to 
            // purge their caches through the API.  Instead try and bring them in here and invalidate 
            // the keys directly.  This will not be required once we move to a different model for pre-calculated
            // permission sets since that will not have the overhead that required these caches in the first place.
            Object principal = (String)getSecuritySession().getUser().getPrincipal();
            if ( userAssignmentsCache != null )
            {
                userAssignmentsCache.invalidateKey( principal );
            }
            if ( userPermissionsCache != null )
            {
                userPermissionsCache.invalidateKey( principal );
            }
            if ( usersCache != null )
            {
                usersCache.invalidateKey( principal );
            }
        }
        
        autologinCookies.removeRememberMeCookie( ServletActionContext.getResponse(), ServletActionContext.getRequest() );
        autologinCookies.removeSignonCookie( ServletActionContext.getResponse(), ServletActionContext.getRequest() );

        setAuthTokens( null );

        if ( session != null )
        {
            ( (SessionMap) session ).invalidate();
        }
        
        
        return LOGOUT;
    }

    public SecureActionBundle initSecureActionBundle()
        throws SecureActionException
    {
        return SecureActionBundle.OPEN;
    }
}
