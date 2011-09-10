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
public class Resource
{
    private String identifier;

    private boolean isPattern;

    private boolean isPermanent;

    public Resource()
    {
    }

    public Resource( String identifier )
    {
        this( identifier, false, false );
    }

    public Resource( String identifier, boolean isPattern, boolean isPermanent )
    {
        this.identifier = identifier;
        this.isPattern = isPattern;
        this.isPermanent = isPermanent;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    @ServiceBeanField("identifier")
    public void setIdentifier( String identifier )
    {
        this.identifier = identifier;
    }

    public boolean isPattern()
    {
        return isPattern;
    }

    @ServiceBeanField("isPattern")
    public void setPattern( boolean isPattern )
    {
        this.isPattern = isPattern;
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
