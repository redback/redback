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
public class Operation
{
    private String name;
    
    private String description;
    
    private boolean isPermanent;
    
    public Operation()
    {
    }
    
    public Operation( String name, String description )
    {
        this( name, description, false );
    }
    
    public Operation( String name, String description, boolean isPermanent )
    {
        this.name = name;
        this.description = description;
        this.isPermanent = isPermanent;
    }

    public String getName()
    {
        return name;
    }

    @ServiceBeanField( "name" )
    public void setName( String name )
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    @ServiceBeanField( "description" )
    public void setDescription( String description )
    {
        this.description = description;
    }

    public boolean isPermanent()
    {
        return isPermanent;
    }

    @ServiceBeanField( "isPermanent" )
    public void setPermanent( boolean isPermanent )
    {
        this.isPermanent = isPermanent;
    }
}
