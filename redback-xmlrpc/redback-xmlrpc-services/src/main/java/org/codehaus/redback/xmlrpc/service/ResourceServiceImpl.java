package org.codehaus.redback.xmlrpc.service;

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

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.redback.xmlrpc.bean.Resource;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;

@Service
public class ResourceServiceImpl
    implements ResourceService
{
    private RBACManager rbacManager;


    @Inject
    public ResourceServiceImpl(@Named( value = "rBACManager#cached" ) RBACManager rbacManager )
    {
        this.rbacManager = rbacManager;
    }

    public Boolean createResource( String identifier )
        throws Exception
    {
        rbacManager.saveResource( rbacManager.createResource( identifier ) );
        return Boolean.TRUE;
    }

    public Resource getResource( String identifier )
        throws Exception
    {
        org.codehaus.plexus.redback.rbac.Resource resource = rbacManager.getResource( identifier );
        Resource simpleResource = new Resource( resource.getIdentifier(), resource.isPattern(), resource.isPermanent() );
        return simpleResource;
    }

    public List<Resource> getResources()
        throws Exception
    {
        List<org.codehaus.plexus.redback.rbac.Resource> resources = rbacManager.getAllResources();
        List<Resource> simpleResources = new ArrayList<Resource>();

        for ( org.codehaus.plexus.redback.rbac.Resource resource : resources )
        {
            simpleResources
                .add( new Resource( resource.getIdentifier(), resource.isPattern(), resource.isPermanent() ) );
        }

        return simpleResources;
    }

    public Boolean removeResource( String identifier )
        throws Exception
    {
        rbacManager.removeResource( identifier );
        return Boolean.TRUE;
    }

    public Boolean ping()
        throws Exception
    {
        return Boolean.TRUE;
    }

}
