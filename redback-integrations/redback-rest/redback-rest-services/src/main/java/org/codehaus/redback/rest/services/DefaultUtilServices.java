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

        // load redback user api messages
        try
        {

            // load default first then requested locale
            loadResource( properties, "org/codehaus/plexus/redback/users/messages", null );
            loadResource( properties, "org/codehaus/plexus/redback/users/messages", locale );

        }
        catch ( IOException e )
        {
            log.warn( "skip error loading properties {}", "org/codehaus/plexus/redback/users/messages" );
        }

        try
        {

            // load default first then requested locale
            loadResource( properties, "org/codehaus/redback/i18n/default", null );
            loadResource( properties, "org/codehaus/redback/i18n/default", locale );

        }
        catch ( IOException e )
        {
            log.warn( "skip error loading properties {}", "org/codehaus/redback/i18n/default" );
        }

        StringBuilder output = new StringBuilder();

        for ( Map.Entry<Object, Object> entry : properties.entrySet() )
        {
            output.append( (String) entry.getKey() ).append( '=' ).append( (String) entry.getValue() );
            output.append( '\n' );
        }

        return output.toString();
    }

    public Properties getI18nProperties( String locale )
        throws RedbackServiceException
    {
        try
        {
            Properties properties = new Properties();
            // load default first then requested locale
            loadResource( properties, "org/codehaus/plexus/redback/users/messages", null );
            loadResource( properties, "org/codehaus/plexus/redback/users/messages", locale );
            return properties;
        }
        catch ( IOException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
    }

    private void loadResource( final Properties finalProperties, String resourceName, String locale )
        throws IOException
    {
        InputStream is = null;
        Properties properties = new Properties();
        try
        {
            if ( StringUtils.isNotEmpty( locale ) )
            {
                resourceName = resourceName + "_" + locale;
            }
            resourceName = resourceName + ".properties";
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream( resourceName.toString() );
            if ( is != null )
            {
                properties.load( is );
                finalProperties.putAll( properties );
            }
            else
            {
                if ( !StringUtils.equalsIgnoreCase( locale, "en" ) )
                {
                    log.info( "cannot load resource {}", resourceName );
                }
            }
        }
        finally
        {
            IOUtils.closeQuietly( is );
        }

    }


}
