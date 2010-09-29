package org.codehaus.redback.xmlrpc.bean;

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

import com.atlassian.xmlrpc.ServiceBean;
import com.atlassian.xmlrpc.ServiceBeanField;

@ServiceBean
public class Role
{
    private String roleName;

    private String description;

    private boolean isAssignable;

    private boolean isPermanent;

    public Role()
    {
    }

    public Role( String roleName, String description, boolean isAssignable, boolean isPermanent )
    {
        this.roleName = roleName;
        this.description = description;
        this.isAssignable = isAssignable;
        this.isPermanent = isPermanent;
    }

    public String getRoleName()
    {
        return roleName;
    }

    @ServiceBeanField("roleName")
    public void setRoleName( String roleName )
    {
        this.roleName = roleName;
    }

    public String getDescription()
    {
        return description;
    }

    @ServiceBeanField("description")
    public void setDescription( String description )
    {
        this.description = description;
    }

    public boolean isAssignable()
    {
        return isAssignable;
    }

    @ServiceBeanField("isAssignable")
    public void setAssignable( boolean isAssignable )
    {
        this.isAssignable = isAssignable;
    }

    public boolean isPermanent()
    {
        return isPermanent;
    }

    @ServiceBeanField("isPermanent")
    public void setPermanent( boolean isPermanent )
    {
        this.isPermanent = isPermanent;
    }
}
