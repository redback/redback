package org.codehaus.redback.rest.services;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.redback.rest.api.services.RedbackServiceException;
import org.codehaus.redback.rest.api.services.UtilServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author Olivier Lamy
 * @since 1.4
 */
@Service( "utilServices#rest" )
public class DefaultUtilServices
    implements UtilServices
{

    private Logger log = LoggerFactory.getLogger( getClass() );

    public String getI18nResources( String locale )
        throws RedbackServiceException
    {

        Properties properties = new Properties();
        InputStream is = null;

        try
        {
            StringBuilder resourceName = new StringBuilder( "org/codehaus/redback/i18n/default" );
            if ( StringUtils.isNotEmpty( locale ) )
            {
                resourceName.append( "_" + locale );
            }
            resourceName.append( ".properties" );

            is = Thread.currentThread().getContextClassLoader().getResourceAsStream( resourceName.toString() );
            if ( is != null )
            {
                properties.load( is );
            }
        }
        catch ( IOException e )
        {
            log.warn( "skip error loading properties with locale {}", locale );
        }
        finally
        {
            IOUtils.closeQuietly( is );
        }
        return properties.toString();
        /*
        for (Map.Entry<Object,Object> entry : properties.entrySet() )
        {
            
        } */
        
    }
}
