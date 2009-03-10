package org.codehaus.plexus.redback.authentication;

/*
 * Copyright 2005 The Codehaus.
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.codehaus.plexus.redback.policy.AccountLockedException;
import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;


/**
 * DefaultAuthenticationManager: the goal of the authentication manager is to act as a conduit for
 * authentication requests into different authentication schemes
 * <p/>
 * For example, the default implementation can be configured with any number of authenticators and will
 * sequentially try them for an authenticated result.  This allows you to have the standard user/pass
 * auth procedure followed by authentication based on a known key for 'remember me' type functionality.
 *
 * @author: Jesse McConnell <jesse@codehaus.org>
 * @version: $Id$
 */
@Service("authenticationManager")
public class DefaultAuthenticationManager
    implements AuthenticationManager
{
    
    private List<Authenticator> authenticators;

    @Resource
    private ApplicationContext applicationContext;
    
    @SuppressWarnings("unchecked")
    @PostConstruct
    public void initialize()
    {
        this.authenticators = PlexusToSpringUtils.lookupList( PlexusToSpringUtils.buildSpringId( Authenticator.class ),
                                                              applicationContext );
    }
    
    
    public String getId()
    {
        return "Default Authentication Manager - " + this.getClass().getName() + " : managed authenticators - " +
            knownAuthenticators();
    }

    public AuthenticationResult authenticate( AuthenticationDataSource source )
        throws AccountLockedException, AuthenticationException
    {
        if ( authenticators == null || authenticators.size() == 0 )
        {
            return ( new AuthenticationResult( false, null, new AuthenticationException(
                "no valid authenticators, can't authenticate" ) ) );
        }

        // put AuthenticationResult exceptions in a map
        Map<String,String> authnResultExceptionsMap = new HashMap<String,String>();
        for ( Authenticator authenticator : authenticators )
        {
            if ( authenticator.supportsDataSource( source ) )
            {
                AuthenticationResult authResult = authenticator.authenticate( source );
                Map<String,String> exceptionsMap = authResult.getExceptionsMap();

                if ( authResult.isAuthenticated() )
                {
                    return authResult;
                }

                if ( exceptionsMap != null )
                {
                    authnResultExceptionsMap.putAll( exceptionsMap );
                }
            }
        }

        return ( new AuthenticationResult( false, null, new AuthenticationException(
            "authentication failed on authenticators: " + knownAuthenticators() ), authnResultExceptionsMap ) );
    }

    public List<Authenticator> getAuthenticators()
    {
        return authenticators;
    }

    private String knownAuthenticators()
    {
        StringBuffer strbuf = new StringBuffer();

        for ( Authenticator authenticator : authenticators )
        {
            strbuf.append( '(' ).append( authenticator.getId() ).append( ") " );
        }

        return strbuf.toString();
    }
}
