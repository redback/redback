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

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.codehaus.plexus.redback.policy.PasswordRuleViolationException;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.redback.integration.model.UserCredentials;

/**
 * AbstractUserCredentialsAction
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public abstract class AbstractUserCredentialsAction
    extends AbstractSecurityAction
{
    // ------------------------------------------------------------------
    // Plexus Component Requirements
    // ------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    protected SecuritySystem securitySystem;

    // ------------------------------------------------------------------
    // Action Parameters
    // ------------------------------------------------------------------

    protected UserCredentials internalUser;

    // ------------------------------------------------------------------
    // Action Entry Points - (aka Names)
    // ------------------------------------------------------------------

    public void validateCredentialsLoose()
    {
        if ( StringUtils.isEmpty( internalUser.getUsername() ) )
        {
            addFieldError( "user.username", getText( "username.required" ) );
        }
        else if ( internalUser.getUsername().indexOf( " " ) != -1 )
        {
        	addFieldError( "user.username", getText( "username.has.spaces") );
        }
        
        if ( StringUtils.isEmpty( internalUser.getFullName() ) )
        {
            addFieldError( "user.fullName", getText( "fullName.required" ) );
        }

        if ( StringUtils.isEmpty( internalUser.getEmail() ) )
        {
            addFieldError( "user.email", getText( "email.required" ) );
        }

        if ( !StringUtils.equals( internalUser.getPassword(), internalUser.getConfirmPassword() ) )
        {
            addFieldError( "user.confirmPassword", getText( "passwords.does.not.match" ) );
        }
    }

    public void validateCredentialsStrict()
    {
        validateCredentialsLoose();

        try
        {
            if ( !StringUtils.isEmpty( internalUser.getEmail() ) )
            {
                new InternetAddress( internalUser.getEmail(), true );
            }
        }
        catch ( AddressException e )
        {
            addFieldError( "user.email", getText( "email.invalid" ) );
        }

        User tmpuser = internalUser.createUser( securitySystem.getUserManager() );

        try
        {
            securitySystem.getPolicy().validatePassword( tmpuser );
        }
        catch ( PasswordRuleViolationException e )
        {
            processPasswordRuleViolations( e );
        }

        if ( ( StringUtils.isEmpty( internalUser.getPassword() ) ) )
        {
            addFieldError( "user.password", getText( "password.required" ) );
        }
    }
}
