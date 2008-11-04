package org.codehaus.redback.jsecurity;

import java.util.List;
import org.codehaus.plexus.redback.authorization.AuthorizationException;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.UserAssignment;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.jsecurity.authc.AuthenticationException;
import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.authc.AuthenticationToken;
import org.jsecurity.authc.SimpleAuthenticationInfo;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.SimpleAuthorizationInfo;
import org.jsecurity.realm.AuthorizingRealm;
import org.jsecurity.subject.PrincipalCollection;

public class RedbackRealm extends AuthorizingRealm
{
    private static final String REDBACK_REALM_NAME = "redback-realm";

    private final UserManager userManager;

    private final RBACManager rbackManager;

    public RedbackRealm(UserManager userManager, RBACManager rbackManager) {
        this.userManager = userManager;
        this.rbackManager = rbackManager;
    }
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals)
    {
        String username = (String) principals.fromRealm(getName()).iterator().next();

        UserAssignment assignment = rbackManager.getUserAssignment(username);
        assignment.getRoleNames()

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private List<String> getRoleNames(User user)
    {
        userManager.
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

        return new SimpleAuthenticationInfo(user.getUsername(), user.getPassword(), getName());
    }

    @Override
    public String getName() {
        return REDBACK_REALM_NAME;
    }
}
