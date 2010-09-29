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
public class Permission
{
    private String name;
    
    private String description;
    
    private String operationName;
    
    private String resourceName;
    
    public Permission()
    {
    }
    
    public Permission( String name, String description, String operationName, String resourceName )
    {
        this.name= name;
        this.description = description;
        this.operationName = operationName;
        this.resourceName = resourceName;
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

    public String getOperation()
    {
        return operationName;
    }

    @ServiceBeanField( "operationName" )
    public void setOperation( String operation )
    {
        this.operationName = operation;
    }

    public String getResource()
    {
        return resourceName;
    }

    @ServiceBeanField( "resourceName" )
    public void setResource( String resource )
    {
        this.resourceName = resource;
    }
}
