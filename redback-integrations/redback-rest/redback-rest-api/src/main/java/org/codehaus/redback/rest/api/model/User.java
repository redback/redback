package org.codehaus.redback.rest.api.model;

import org.codehaus.redback.integration.util.DateUtils;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

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
@XmlRootElement( name = "user" )
public class User
    implements Serializable
{
    private String username;

    private String fullName;

    private String email;

    private boolean validated;

    private boolean locked;

    private String password;

    private boolean passwordChangeRequired;

    private boolean permanent;

    private String confirmPassword;

    // Display Only Fields.
    private String timestampAccountCreation;

    private String timestampLastLogin;

    private String timestampLastPasswordChange;

    /**
     * for password change only
     *
     * @since 1.4
     */
    private String previousPassword;

    /**
     * for roles update only <b>not return on user read</b>
     * @since 1.5
     */
    private List<String> assignedRoles;

    public User()
    {
        // no op
    }

    public User( String username, String fullName, String email, boolean validated, boolean locked )
    {
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.validated = validated;
        this.locked = locked;
    }

    public User( org.codehaus.plexus.redback.users.User user )
    {
        setUsername( user.getUsername() );
        this.setEmail( user.getEmail() );
        this.setFullName( user.getFullName() );
        this.setLocked( user.isLocked() );
        this.setPassword( user.getPassword() );
        this.setValidated( user.isValidated() );
        this.setPasswordChangeRequired( user.isPasswordChangeRequired() );
        this.setPermanent( user.isPermanent() );

        setTimestampAccountCreation( DateUtils.formatWithAge( user.getAccountCreationDate(), "ago" ) );
        setTimestampLastLogin( DateUtils.formatWithAge( user.getLastLoginDate(), "ago" ) );
        setTimestampLastPasswordChange( DateUtils.formatWithAge( user.getLastPasswordChange(), "ago" ) );
    }


    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName( String fullName )
    {
        this.fullName = fullName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail( String email )
    {
        this.email = email;
    }

    public boolean isValidated()
    {
        return validated;
    }

    public void setValidated( boolean validated )
    {
        this.validated = validated;
    }

    public boolean isLocked()
    {
        return locked;
    }

    public void setLocked( boolean isLocked )
    {
        this.locked = isLocked;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword( String password )
    {
        this.password = password;
    }

    public boolean isPasswordChangeRequired()
    {
        return passwordChangeRequired;
    }

    public void setPasswordChangeRequired( boolean passwordChangeRequired )
    {
        this.passwordChangeRequired = passwordChangeRequired;
    }

    public boolean isPermanent()
    {
        return permanent;
    }

    public void setPermanent( boolean permanent )
    {
        this.permanent = permanent;
    }

    public String getConfirmPassword()
    {
        return confirmPassword;
    }

    public void setConfirmPassword( String confirmPassword )
    {
        this.confirmPassword = confirmPassword;
    }

    public String getTimestampAccountCreation()
    {
        return timestampAccountCreation;
    }

    public void setTimestampAccountCreation( String timestampAccountCreation )
    {
        this.timestampAccountCreation = timestampAccountCreation;
    }

    public String getTimestampLastLogin()
    {
        return timestampLastLogin;
    }

    public void setTimestampLastLogin( String timestampLastLogin )
    {
        this.timestampLastLogin = timestampLastLogin;
    }

    public String getTimestampLastPasswordChange()
    {
        return timestampLastPasswordChange;
    }

    public void setTimestampLastPasswordChange( String timestampLastPasswordChange )
    {
        this.timestampLastPasswordChange = timestampLastPasswordChange;
    }

    public String getPreviousPassword()
    {
        return previousPassword;
    }

    public void setPreviousPassword( String previousPassword )
    {
        this.previousPassword = previousPassword;
    }

    public List<String> getAssignedRoles()
    {
        return assignedRoles;
    }

    public void setAssignedRoles( List<String> assignedRoles )
    {
        this.assignedRoles = assignedRoles;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append( "User" );
        sb.append( "{username='" ).append( username ).append( '\'' );
        sb.append( ", fullName='" ).append( fullName ).append( '\'' );
        sb.append( ", email='" ).append( email ).append( '\'' );
        sb.append( ", validated=" ).append( validated );
        sb.append( ", locked=" ).append( locked );
        sb.append( ", password='" ).append( password ).append( '\'' );
        sb.append( ", passwordChangeRequired=" ).append( passwordChangeRequired );
        sb.append( ", permanent=" ).append( permanent );
        sb.append( ", confirmPassword='" ).append( confirmPassword ).append( '\'' );
        sb.append( ", timestampAccountCreation='" ).append( timestampAccountCreation ).append( '\'' );
        sb.append( ", timestampLastLogin='" ).append( timestampLastLogin ).append( '\'' );
        sb.append( ", timestampLastPasswordChange='" ).append( timestampLastPasswordChange ).append( '\'' );
        sb.append( ", previousPassword='" ).append( previousPassword ).append( '\'' );
        sb.append( ", assignedRoles=" ).append( assignedRoles );
        sb.append( '}' );
        return sb.toString();
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof User ) )
        {
            return false;
        }

        User user = (User) o;

        if ( !username.equals( user.username ) )
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return username.hashCode();
    }
}
