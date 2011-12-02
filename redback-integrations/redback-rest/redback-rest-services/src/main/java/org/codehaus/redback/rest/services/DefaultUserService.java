package org.codehaus.redback.rest.services;

/*
 * Copyright 2011 The Codehaus.
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

import net.sf.ehcache.CacheManager;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.cache.Cache;
import org.codehaus.plexus.redback.authentication.TokenBasedAuthenticationDataSource;
import org.codehaus.plexus.redback.configuration.UserConfiguration;
import org.codehaus.plexus.redback.keys.AuthenticationKey;
import org.codehaus.plexus.redback.keys.KeyManagerException;
import org.codehaus.plexus.redback.keys.KeyNotFoundException;
import org.codehaus.plexus.redback.policy.UserSecurityPolicy;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.RbacObjectNotFoundException;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.RoleManagerException;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.redback.integration.mail.Mailer;
import org.codehaus.redback.integration.security.role.RedbackRoleConstants;
import org.codehaus.redback.rest.api.model.ErrorMessage;
import org.codehaus.redback.rest.api.model.Operation;
import org.codehaus.redback.rest.api.model.Permission;
import org.codehaus.redback.rest.api.model.Resource;
import org.codehaus.redback.rest.api.model.User;
import org.codehaus.redback.rest.api.services.RedbackServiceException;
import org.codehaus.redback.rest.api.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service( "userService#rest" )
public class DefaultUserService
    implements UserService
{

    private Logger log = LoggerFactory.getLogger( getClass() );

    private static final String VALID_USERNAME_CHARS = "[a-zA-Z_0-9\\-.@]*";

    private UserManager userManager;

    private SecuritySystem securitySystem;

    @Inject
    private UserConfiguration config;

    @Inject
    private RoleManager roleManager;

    /**
     * cache used for user assignments
     */
    @Inject
    @Named( value = "cache#userAssignments" )
    private Cache userAssignmentsCache;

    /**
     * cache used for user permissions
     */
    @Inject
    @Named( value = "cache#userPermissions" )
    private Cache userPermissionsCache;

    /**
     * Cache used for users
     */
    @Inject
    @Named( value = "cache#users" )
    private Cache usersCache;

    @Inject
    private Mailer mailer;

    @Inject
    @Named( value = "rBACManager#cached" )
    private RBACManager rbacManager;

    @Context
    private HttpServletRequest httpServletRequest;

    @Inject
    public DefaultUserService( @Named( value = "userManager#cached" ) UserManager userManager,
                               SecuritySystem securitySystem )
    {
        this.userManager = userManager;
        this.securitySystem = securitySystem;
    }


    public Boolean createUser( User user )
        throws RedbackServiceException
    {

        try
        {
            org.codehaus.plexus.redback.users.User u = userManager.findUser( user.getUsername() );
            if ( u != null )
            {
                throw new RedbackServiceException(
                    new ErrorMessage( "user " + user.getUsername() + " already exists" ) );
            }
        }
        catch ( UserNotFoundException e )
        {
            //ignore we just want to prevent non human readable error message from backend :-)
            log.debug( "user {} not exists", user.getUsername() );
        }
        org.codehaus.plexus.redback.users.User u =
            userManager.createUser( user.getUsername(), user.getFullName(), user.getEmail() );
        u.setPassword( user.getPassword() );
        u.setLocked( user.isLocked() );
        u.setPasswordChangeRequired( user.isPasswordChangeRequired() );
        u.setPermanent( user.isPermanent() );
        u.setValidated( user.isValidated() );
        u = userManager.addUser( u );
        if ( !user.isPasswordChangeRequired() )
        {
            u.setPasswordChangeRequired( false );
            try
            {
                u = userManager.updateUser( u );
                log.debug( "user {} created", u.getUsername() );
            }
            catch ( UserNotFoundException e )
            {
                throw new RedbackServiceException( e.getMessage() );
            }
        }
        try
        {
            roleManager.assignRole( RedbackRoleConstants.REGISTERED_USER_ROLE_ID, u.getPrincipal().toString() );
        }
        catch ( RoleManagerException rpe )
        {
            log.error( "RoleProfile Error: " + rpe.getMessage(), rpe );
            throw new RedbackServiceException( new ErrorMessage( "assign.role.failure", null ) );
        }
        return Boolean.TRUE;
    }

    public Boolean deleteUser( String username )
        throws RedbackServiceException
    {
        try
        {
            userManager.deleteUser( username );
            return Boolean.TRUE;
        }
        catch ( UserNotFoundException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
    }


    public User getUser( String username )
        throws RedbackServiceException
    {
        try
        {
            org.codehaus.plexus.redback.users.User user = userManager.findUser( username );
            return getSimpleUser( user );
        }
        catch ( UserNotFoundException e )
        {
            return null;
        }
    }

    public List<User> getUsers()
        throws RedbackServiceException
    {
        List<org.codehaus.plexus.redback.users.User> users = userManager.getUsers();
        List<User> simpleUsers = new ArrayList<User>();

        for ( org.codehaus.plexus.redback.users.User user : users )
        {
            simpleUsers.add( getSimpleUser( user ) );
        }

        return simpleUsers;
    }

    public Boolean updateUser( User user )
        throws RedbackServiceException
    {
        try
        {
            org.codehaus.plexus.redback.users.User rawUser = userManager.findUser( user.getUsername() );
            rawUser.setFullName( user.getFullName() );
            rawUser.setEmail( user.getEmail() );
            rawUser.setValidated( user.isValidated() );
            rawUser.setLocked( user.isLocked() );
            rawUser.setPassword( user.getPassword() );
            rawUser.setPasswordChangeRequired( user.isPasswordChangeRequired() );
            rawUser.setPermanent( user.isPermanent() );

            userManager.updateUser( rawUser );
            return Boolean.TRUE;
        }
        catch ( UserNotFoundException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
    }

    public int removeFromCache( String userName )
        throws RedbackServiceException
    {
        if ( userAssignmentsCache != null )
        {
            userAssignmentsCache.remove( userName );
        }
        if ( userPermissionsCache != null )
        {
            userPermissionsCache.remove( userName );
        }
        if ( usersCache != null )
        {
            usersCache.remove( userName );
        }

        CacheManager cacheManager = CacheManager.getInstance();
        String[] caches = cacheManager.getCacheNames();
        for ( String cacheName : caches )
        {
            if ( StringUtils.startsWith( cacheName, "org.codehaus.plexus.redback.rbac.jdo" ) )
            {
                cacheManager.getCache( cacheName ).removeAll();
            }
        }

        return 0;
    }

    public User getGuestUser()
        throws RedbackServiceException
    {
        try
        {
            org.codehaus.plexus.redback.users.User user = userManager.getGuestUser();
            return getSimpleUser( user );
        }
        catch ( Exception e )
        {
            return null;
        }
    }

    public User createGuestUser()
        throws RedbackServiceException
    {
        User u = getGuestUser();
        if ( u != null )
        {
            return u;
        }
        // temporary disable policy during guest creation as no password !
        try
        {
            securitySystem.getPolicy().setEnabled( false );
            org.codehaus.plexus.redback.users.User user = userManager.createGuestUser();
            user.setPasswordChangeRequired( false );
            user = userManager.updateUser( user, false );
            roleManager.assignRole( "guest", user.getPrincipal().toString() );
            return getSimpleUser( user );
        }
        catch ( RoleManagerException e )
        {
            log.error( e.getMessage(), e );
            throw new RedbackServiceException( e.getMessage() );
        }
        catch ( UserNotFoundException e )
        {
            // olamy I wonder how this can happen :-)
            log.error( e.getMessage(), e );
            throw new RedbackServiceException( e.getMessage() );
        }
        finally
        {

            if ( !securitySystem.getPolicy().isEnabled() )
            {
                securitySystem.getPolicy().setEnabled( true );
            }
        }
    }

    public Boolean ping()
        throws RedbackServiceException
    {
        return Boolean.TRUE;
    }

    private User getSimpleUser( org.codehaus.plexus.redback.users.User user )
    {
        if ( user == null )
        {
            return null;
        }
        return new User( user );
    }

    public Boolean createAdminUser( User adminUser )
        throws RedbackServiceException
    {
        if ( isAdminUserExists() )
        {
            return Boolean.FALSE;
        }

        org.codehaus.plexus.redback.users.User user =
            userManager.createUser( RedbackRoleConstants.ADMINISTRATOR_ACCOUNT_NAME, adminUser.getFullName(),
                                    adminUser.getEmail() );
        user.setPassword( adminUser.getPassword() );

        user.setLocked( false );
        user.setPasswordChangeRequired( false );
        user.setPermanent( true );
        user.setValidated( true );

        userManager.addUser( user );

        try
        {
            roleManager.assignRole( "system-administrator", user.getPrincipal().toString() );
        }
        catch ( RoleManagerException e )
        {
            throw new RedbackServiceException( e.getMessage() );
        }
        /*
        UserAssignment userAssignment = rbacManager.createUserAssignment( RoleConstants.ADMINISTRATOR_ACCOUNT_NAME );
        userAssignment.setRoleNames( Collections.singletonList( RoleConstants.USER_ADMINISTRATOR_ROLE ) );
        rbacManager.saveUserAssignment( userAssignment );
        */
        return Boolean.TRUE;
    }

    public Boolean isAdminUserExists()
        throws RedbackServiceException
    {
        try
        {
            userManager.findUser( config.getString( "redback.default.admin" ) );
            return Boolean.TRUE;
        }
        catch ( UserNotFoundException e )
        {
            // ignore
        }
        return Boolean.FALSE;
    }

    public String registerUser( User user )
        throws RedbackServiceException
    {
        if ( user == null )
        {
            throw new RedbackServiceException( new ErrorMessage( "invalid.user.credentials", null ) );

        }

        UserSecurityPolicy securityPolicy = securitySystem.getPolicy();

        boolean emailValidationRequired = securityPolicy.getUserValidationSettings().isEmailValidationRequired();

        if ( emailValidationRequired )
        {
            validateCredentialsLoose( user );
        }
        else
        {
            validateCredentialsStrict( user );
        }

        // NOTE: Do not perform Password Rules Validation Here.

        if ( userManager.userExists( user.getUsername() ) )
        {
            throw new RedbackServiceException(
                new ErrorMessage( "user.already.exists", new String[]{ user.getUsername() } ) );
        }

        org.codehaus.plexus.redback.users.User u =
            userManager.createUser( user.getUsername(), user.getFullName(), user.getEmail() );
        u.setPassword( user.getPassword() );
        u.setValidated( false );
        u.setLocked( false );

        try
        {
            roleManager.assignRole( RedbackRoleConstants.REGISTERED_USER_ROLE_ID, u.getPrincipal().toString() );
        }
        catch ( RoleManagerException rpe )
        {
            log.error( "RoleProfile Error: " + rpe.getMessage(), rpe );
            throw new RedbackServiceException( new ErrorMessage( "assign.role.failure", null ) );
        }

        if ( emailValidationRequired )
        {
            u.setLocked( true );

            try
            {
                AuthenticationKey authkey =
                    securitySystem.getKeyManager().createKey( u.getPrincipal().toString(), "New User Email Validation",
                                                              securityPolicy.getUserValidationSettings().getEmailValidationTimeout() );

                mailer.sendAccountValidationEmail( Arrays.asList( u.getEmail() ), authkey, getBaseUrl() );

                securityPolicy.setEnabled( false );
                userManager.addUser( u );
                return authkey.getKey();

            }
            catch ( KeyManagerException e )
            {
                log.error( "Unable to register a new user.", e );
                throw new RedbackServiceException( new ErrorMessage( "cannot.register.user", null ) );
            }
            finally
            {
                securityPolicy.setEnabled( true );
            }
        }
        else
        {
            userManager.addUser( u );
            return "-1";
        }

        // FIXME log this event
        /*
        AuditEvent event = new AuditEvent( getText( "log.account.create" ) );
        event.setAffectedUser( username );
        event.log();
        */

    }

    public Boolean validateUserFromKey( String key )
        throws RedbackServiceException
    {
        try
        {
            AuthenticationKey authkey = securitySystem.getKeyManager().findKey( key );

            org.codehaus.plexus.redback.users.User user =
                securitySystem.getUserManager().findUser( authkey.getForPrincipal() );

            user.setValidated( true );
            user.setLocked( false );
            user.setPasswordChangeRequired( true );
            user.setEncodedPassword( "" );

            TokenBasedAuthenticationDataSource authsource = new TokenBasedAuthenticationDataSource();
            authsource.setPrincipal( user.getPrincipal().toString() );
            authsource.setToken( authkey.getKey() );
            authsource.setEnforcePasswordChange( false );

            securitySystem.getUserManager().updateUser( user );

            log.info( "account validated for user {}", user.getUsername() );

            return Boolean.TRUE;
        }
        catch ( KeyNotFoundException e )
        {
            log.info( "Invalid key requested: {}", key );
            RedbackServiceException exception = new RedbackServiceException( "Invalid key requested" );
            exception.addErrorMessage( new ErrorMessage( "cannot.find.key" ) );
            throw exception;
        }
        catch ( KeyManagerException e )
        {
            RedbackServiceException exception = new RedbackServiceException( e.getMessage() );
            exception.addErrorMessage( new ErrorMessage( "cannot.find.key.at.the.momment" ) );
            throw exception;

        }
        catch ( UserNotFoundException e )
        {
            RedbackServiceException exception = new RedbackServiceException( e.getMessage() );
            exception.addErrorMessage( new ErrorMessage( "cannot.find.user" ) );
            throw exception;
        }
    }

    public Collection<Permission> getCurrentUserPermissions()
        throws RedbackServiceException
    {
        RedbackRequestInformation redbackRequestInformation = RedbackAuthenticationThreadLocal.get();
        String userName = UserManager.GUEST_USERNAME;
        if ( redbackRequestInformation != null && redbackRequestInformation.getUser() != null )
        {
            userName = redbackRequestInformation.getUser().getUsername();
        }

        return getUserPermissions( userName );
    }

    public Collection<Operation> getCurrentUserOperations()
        throws RedbackServiceException
    {
        RedbackRequestInformation redbackRequestInformation = RedbackAuthenticationThreadLocal.get();
        String userName = UserManager.GUEST_USERNAME;
        if ( redbackRequestInformation != null && redbackRequestInformation.getUser() != null )
        {
            userName = redbackRequestInformation.getUser().getUsername();
        }

        return getUserOperations( userName );
    }

    public Collection<Operation> getUserOperations( String userName )
        throws RedbackServiceException
    {
        Collection<Permission> permissions = getUserPermissions( userName );
        List<Operation> operations = new ArrayList<Operation>( permissions.size() );
        for ( Permission permission : permissions )
        {
            if ( permission.getOperation() != null )
            {
                Operation operation = new Operation();
                operation.setName( permission.getOperation().getName() );
                operations.add( operation );
            }
        }
        return operations;
    }

    public Collection<Permission> getUserPermissions( String userName )
        throws RedbackServiceException
    {
        try
        {
            Set<org.codehaus.plexus.redback.rbac.Permission> permissions =
                rbacManager.getAssignedPermissions( userName );
            // FIXME return guest permissions !!
            List<Permission> userPermissions = new ArrayList<Permission>( permissions.size() );
            for ( org.codehaus.plexus.redback.rbac.Permission p : permissions )
            {
                Permission permission = new Permission();
                permission.setName( p.getName() );

                if ( p.getOperation() != null )
                {
                    Operation operation = new Operation();
                    operation.setName( p.getOperation().getName() );
                    permission.setOperation( operation );
                }

                if ( p.getResource() != null )
                {
                    Resource resource = new Resource();
                    resource.setIdentifier( p.getResource().getIdentifier() );
                    resource.setPattern( p.getResource().isPattern() );
                    permission.setResource( resource );
                }

                userPermissions.add( permission );
            }
            return userPermissions;
        }
        catch ( RbacObjectNotFoundException e )
        {
            log.error( e.getMessage(), e );
            throw new RedbackServiceException( e.getMessage() );
        }
        catch ( RbacManagerException e )
        {
            log.error( e.getMessage(), e );
            throw new RedbackServiceException( e.getMessage() );
        }
    }

    public void validateCredentialsLoose( User user )
        throws RedbackServiceException
    {
        RedbackServiceException redbackServiceException =
            new RedbackServiceException( "issues during validating user" );
        if ( org.codehaus.plexus.util.StringUtils.isEmpty( user.getUsername() ) )
        {
            redbackServiceException.addErrorMessage( new ErrorMessage( "username.required", null ) );
        }
        else
        {
            if ( !user.getUsername().matches( VALID_USERNAME_CHARS ) )
            {
                redbackServiceException.addErrorMessage( new ErrorMessage( "username.invalid.characters", null ) );
            }
        }

        if ( org.codehaus.plexus.util.StringUtils.isEmpty( user.getFullName() ) )
        {
            redbackServiceException.addErrorMessage( new ErrorMessage( "fullName.required", null ) );
        }

        if ( org.codehaus.plexus.util.StringUtils.isEmpty( user.getEmail() ) )
        {
            redbackServiceException.addErrorMessage( new ErrorMessage( "email.required", null ) );
        }

        if ( !org.codehaus.plexus.util.StringUtils.equals( user.getPassword(), user.getConfirmPassword() ) )
        {
            redbackServiceException.addErrorMessage( new ErrorMessage( "passwords.does.not.match", null ) );
        }

        try
        {
            if ( !org.codehaus.plexus.util.StringUtils.isEmpty( user.getEmail() ) )
            {
                new InternetAddress( user.getEmail(), true );
            }
        }
        catch ( AddressException e )
        {
            redbackServiceException.addErrorMessage( new ErrorMessage( "email.invalid", null ) );
        }
        if ( !redbackServiceException.getErrorMessages().isEmpty() )
        {
            throw redbackServiceException;
        }
    }

    public void validateCredentialsStrict( User user )
        throws RedbackServiceException
    {
        validateCredentialsLoose( user );

        org.codehaus.plexus.redback.users.User tmpuser =
            userManager.createUser( user.getUsername(), user.getFullName(), user.getEmail() );

        user.setPassword( user.getPassword() );

        securitySystem.getPolicy().validatePassword( tmpuser );

        if ( ( org.codehaus.plexus.util.StringUtils.isEmpty( user.getPassword() ) ) )
        {
            throw new RedbackServiceException( new ErrorMessage( "password.required", null ) );
        }
    }

    private String getBaseUrl()
    {
        if ( httpServletRequest != null )
        {
            if ( httpServletRequest != null )
            {
                return httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName() + (
                    httpServletRequest.getServerPort() == 80
                        ? ""
                        : ":" + httpServletRequest.getServerPort() ) + httpServletRequest.getContextPath();
            }
        }
        return null;
    }
}
