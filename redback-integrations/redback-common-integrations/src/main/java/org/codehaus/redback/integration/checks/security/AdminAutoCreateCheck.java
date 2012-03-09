package org.codehaus.redback.integration.checks.security;
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

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.redback.authentication.PasswordBasedAuthenticationDataSource;
import org.codehaus.plexus.redback.configuration.UserConfiguration;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.Role;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.plexus.redback.system.SecuritySession;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.system.check.EnvironmentCheck;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author Olivier Lamy
 * @since 1.5
 */
@Service( "AdminAutoCreateCheck" )
public class AdminAutoCreateCheck
    implements EnvironmentCheck
{

    private Logger log = LoggerFactory.getLogger( getClass() );

    public static final String FORCE_ADMIN_FILE_PATH = "redback.admin.creation.file";

    public static final String ADMIN_FULL_NAME_KEY = "redback.admin.fullname";

    public static final String ADMIN_EMAIL_KEY = "redback.admin.email";

    public static final String ADMIN_PASSWORD_KEY = "redback.admin.password";

    @Inject
    @Named( value = "userManager#configurable" )
    private UserManager userManager;

    @Inject
    private UserConfiguration config;

    @Inject
    protected SecuritySystem securitySystem;

    @Inject
    private RoleManager roleManager;

    @Inject
    @Named( value = "rBACManager#cached" )
    private RBACManager rbacManager;

    public void validateEnvironment( List<String> violations )
    {
        try
        {
            User user = userManager.findUser( getAdminUid() );
            if ( user == null )
            {
                useForceAdminCreationFile();
            }


        }
        catch ( UserNotFoundException e )
        {
            useForceAdminCreationFile();
        }
    }

    private void checkAdminKarma( User u )
    {
        try
        {
            Collection<Role> roles = rbacManager.getEffectivelyAssignedRoles( getAdminUid() );
            boolean adminRole = false;
            for ( Role role : roles )
            {
                if ( StringUtils.equals( "system-administrator", role.getName() ) )
                {
                    adminRole = true;
                }
            }
            if ( !adminRole )
            {
                assignAdminRole( u );
            }
        }
        catch ( RbacManagerException e )
        {
            log.warn( "fail to checkAdminKarma {}", e, e.getMessage() );
        }
        catch ( RoleManagerException e )
        {
            log.warn( "fail to assignAdmin role {}", e, e.getMessage() );
        }
    }

    private void useForceAdminCreationFile()
    {
        try
        {
            String forceAdminFilePath = System.getProperty( FORCE_ADMIN_FILE_PATH );
            if ( StringUtils.isBlank( forceAdminFilePath ) )
            {
                log.info( FORCE_ADMIN_FILE_PATH + " system props is empty don't use an auto creation admin " );
                return;
            }
            File file = new File( forceAdminFilePath );
            if ( !file.exists() )
            {
                log.warn( "file set in sysprops " + FORCE_ADMIN_FILE_PATH + " not exists skip admin auto creation" );
                return;
            }
            log.debug( "user {} not found try auto creation" );
            Properties properties = new Properties();
            FileInputStream fis = new FileInputStream( file );
            try
            {
                properties.load( fis );
            }
            catch ( Exception e )
            {
                log.warn( "error loading properties from file " + forceAdminFilePath + " skip admin auto creation" );
                return;
            }
            finally
            {
                IOUtil.close( fis );
            }

            // ensure we have all properties
            String password = properties.getProperty( ADMIN_PASSWORD_KEY );
            String email = properties.getProperty( ADMIN_EMAIL_KEY );
            String fullName = properties.getProperty( ADMIN_FULL_NAME_KEY );

            if ( StringUtils.isBlank( password ) )
            {
                log.warn( "property " + ADMIN_PASSWORD_KEY + " not set skip auto admin creation" );
                return;
            }

            if ( StringUtils.isBlank( email ) )
            {
                log.warn( "property " + ADMIN_EMAIL_KEY + " not set skip auto admin creation" );
                return;
            }

            if ( StringUtils.isBlank( fullName ) )
            {
                log.warn( "property " + ADMIN_FULL_NAME_KEY + " not set skip auto admin creation" );
                return;
            }

            User u = userManager.createUser( getAdminUid(), fullName, email );

            u.setPassword( password );
            u.setLocked( false );
            u.setPasswordChangeRequired( false );
            u.setPermanent( true );
            u.setValidated( true );

            u = userManager.addUser( u );
            u.setPassword( password );

            PasswordBasedAuthenticationDataSource authdatasource = new PasswordBasedAuthenticationDataSource();
            authdatasource.setPrincipal( u.getUsername() );
            authdatasource.setPassword( u.getPassword() );
            SecuritySession securitySession = securitySystem.authenticate( authdatasource );
            if ( securitySession.getAuthenticationResult().isAuthenticated() )
            {
                // good add various tokens.
                u = securitySession.getUser();
                u.setLastLoginDate( new Date() );
                securitySystem.getUserManager().updateUser( u );
            }
            assignAdminRole( u );

        }
        catch ( Exception e )
        {
            log.warn( "failed to automatically create an admin account " + e.getMessage(), e );
        }
    }

    private void assignAdminRole( User user )
        throws RoleManagerException
    {
        roleManager.assignRole( "system-administrator", user.getPrincipal().toString() );
    }

    private String getAdminUid()
    {
        return config.getString( "redback.default.admin" );
    }
}
