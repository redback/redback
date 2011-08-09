package org.codehaus.redback.rest.api.model;

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

public class User
{
    private String username;

    private String fullname;

    private String email;

    private boolean isValidated;

    private boolean isLocked;

    public User()
    {
        // no op
    }

    public User( String username, String fullname, String email, boolean isValidated, boolean isLocked )
    {
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.isValidated = isValidated;
        this.isLocked = isLocked;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername( String username )
    {
        this.username = username;
    }

    public String getFullname()
    {
        return fullname;
    }

    public void setFullname( String fullname )
    {
        this.fullname = fullname;
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
        return isValidated;
    }

    public void setValidated( boolean isValidated )
    {
        this.isValidated = isValidated;
    }

    public boolean isLocked()
    {
        return isLocked;
    }

    public void setLocked( boolean isLocked )
    {
        this.isLocked = isLocked;
    }
}
