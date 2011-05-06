package org.codehaus.plexus.redback.configuration;

/*
 * Copyright 2001-2006 The Codehaus.
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

import java.io.File;
import java.util.List;

import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.evaluator.DefaultExpressionEvaluator;
import org.codehaus.plexus.evaluator.EvaluatorException;
import org.codehaus.plexus.evaluator.ExpressionEvaluator;
import org.codehaus.plexus.evaluator.sources.SystemPropertyExpressionSource;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.codehaus.redback.components.registry.Registry;
import org.codehaus.redback.components.registry.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ConfigurationFactory
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * 
 */
@Service("userConfiguration")
public class UserConfiguration
    implements Contextualizable, Initializable
{
    public static final String ROLE = UserConfiguration.class.getName();

    private static final String DEFAULT_CONFIG_RESOURCE = "org/codehaus/plexus/redback/config-defaults.properties";

    protected Logger log = LoggerFactory.getLogger( getClass() );
    
    /**
     * @plexus.configuration
     * @deprecated Please configure the Plexus registry instead
     */
    private List<String> configs;

    private Registry lookupRegistry;

    private static final String PREFIX = "org.codehaus.plexus.redback";

    private Registry registry;

    public void initialize()
        throws InitializationException
    {
        try
        {
            performLegacyInitialization();

            try
            {
                registry.addConfigurationFromResource( DEFAULT_CONFIG_RESOURCE, PREFIX );
            }
            catch ( RegistryException e )
            {
                // Ok, not found in context classloader; try the one in this jar.

                ClassLoader prevCl = Thread.currentThread().getContextClassLoader();
                try
                {

                    Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
                    registry.addConfigurationFromResource( DEFAULT_CONFIG_RESOURCE, PREFIX );
                }
                finally
                {
                    Thread.currentThread().setContextClassLoader( prevCl );
                }
            }
        }
        catch ( RegistryException e )
        {
            throw new InitializationException( e.getMessage(), e );
        }

        lookupRegistry = registry.getSubset( PREFIX );

        if ( log.isDebugEnabled() )
        {
            log.debug( lookupRegistry.dump() );
        }
    }

    private void performLegacyInitialization()
        throws InitializationException,
            RegistryException
    {
        ExpressionEvaluator evaluator = new DefaultExpressionEvaluator();
        evaluator.addExpressionSource( new SystemPropertyExpressionSource() );

        if ( configs != null )
        {
            if ( !configs.isEmpty() )
            {
                // TODO: plexus should be able to do this on it's own.
                log.warn(
                    "DEPRECATED: the <configs> elements is deprecated. Please configure the Plexus registry instead" );
            }
    
            for ( String configName : configs )
            {
                try
                {
                    configName = evaluator.expand( configName );
                }
                catch ( EvaluatorException e )
                {
                    log.warn( "Unable to resolve configuration name: " + e.getMessage(), e );
                }
                log.info(
                    "Attempting to find configuration [" + configName + "] (resolved to [" + configName + "])" );
    
                registry.addConfigurationFromFile( new File( configName ), PREFIX );
            }
        }
    }

    public String getString( String key )
    {
        return lookupRegistry.getString( key );
    }

    public String getString( String key, String defaultValue )
    {
        return lookupRegistry.getString( key, defaultValue );
    }

    public int getInt( String key )
    {
        return lookupRegistry.getInt( key );
    }

    public int getInt( String key, int defaultValue )
    {
        return lookupRegistry.getInt( key, defaultValue );
    }

    public boolean getBoolean( String key )
    {
        return lookupRegistry.getBoolean( key );
    }

    public boolean getBoolean( String key, boolean defaultValue )
    {
        return lookupRegistry.getBoolean( key, defaultValue );
    }

    @SuppressWarnings("unchecked")
    public List<String> getList( String key )
    {
        return lookupRegistry.getList( key );
    }

    public String getConcatenatedList( String key, String defaultValue )
    {
        String concatenatedList;
        List<String> list = getList( key );
        if ( !list.isEmpty() )
        {
            StringBuilder s = new StringBuilder();
            for ( String value : list )
            {
                if ( s.length() > 0 )
                {
                    s.append( "," );
                }
                s.append( value );
            }
            concatenatedList = s.toString();
        }
        else
        {
            concatenatedList = defaultValue;
        }

        return concatenatedList;
    }

    public void contextualize( Context context )
        throws ContextException
    {
        PlexusContainer container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
        try
        {
            // elsewhere, this can be a requirement, but we need this for backwards compatibility
            registry = (Registry) container.lookup( Registry.class.getName(), "commons-configuration" );
        }
        catch ( ComponentLookupException e )
        {
            throw new ContextException( e.getMessage(), e );
        }
    }

    /**
     * @deprecated
     * @return
     */
    public List<String> getConfigs()
    {
        return configs;
    }

    /**
     * @deprecated
     * @param configs
     */
    public void setConfigs( List<String> configs )
    {
        this.configs = configs;
    }

    public Registry getRegistry()
    {
        return registry;
    }

    public void setRegistry( Registry registry )
    {
        this.registry = registry;
    }
}
