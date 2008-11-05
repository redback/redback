package org.codehaus.redback.jsecurity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.codehaus.plexus.redback.policy.UserSecurityPolicy;
import org.codehaus.plexus.redback.rbac.Permission;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.UserAssignment;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authc.credential.CredentialsMatcher;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.subject.PrincipalCollection;

public class RedbackRealm extends AuthorizingRealm
{
    private static final String REDBACK_REALM_NAME = "redback-realm";

    private final UserManager userManager;

    private final RBACManager rbacManager;

    private final UserSecurityPolicy userSecurityPolicy;

    public RedbackRealm(UserManager userManager, RBACManager rbacManager, UserSecurityPolicy userSecurityPolicy) {
        this.userManager = userManager;
        this.rbacManager = rbacManager;
        this.userSecurityPolicy = userSecurityPolicy;
    }
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
    {
        String username = (String) principals.fromRealm(getName()).iterator().next();

        try
        {
            UserAssignment assignment = rbacManager.getUserAssignment(username);
            Set<String> roleNames = new HashSet<String>(assignment.getRoleNames());
            Set<String> permissions = new HashSet<String>();

            for (Iterator<Permission> it = rbacManager.getAssignedPermissions(username).iterator(); it.hasNext();)
            {
                Permission permission = it.next();
                permissions.add(permission.getName());
            }

            SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo(roleNames);
            authorizationInfo.setStringPermissions(permissions);

            return authorizationInfo;
        }
        catch (RbacManagerException e)
        {
            //TODO: add logging
        }
        
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token)
        throws AuthenticationException
    {
        if (token == null)
        {
            throw new AuthenticationException("AuthenticationToken cannot be null");
        }
        
        UsernamePasswordToken passwordToken = (UsernamePasswordToken)token;

        User user = null;
        try
        {
            user = userManager.findUser(passwordToken.getUsername());
        }
        catch (UserNotFoundException e)
        {
            //TODO: log this
        }

        if (user == null)
        {
            return null;
        }

        return new SimpleAuthenticationInfo(user.getUsername(), user.getEncodedPassword(), getName());
    }

    @Override
    public String getName()
    {
        return REDBACK_REALM_NAME;
    }

    @Override
    public CredentialsMatcher getCredentialsMatcher()
    {
        return new CredentialsMatcher()
        {
            public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info)
            {
                final String credentials = new String((char[])token.getCredentials());
                return userSecurityPolicy.getPasswordEncoder().encodePassword(credentials).equals((String)info.getCredentials());
            }
        };
    }
}
