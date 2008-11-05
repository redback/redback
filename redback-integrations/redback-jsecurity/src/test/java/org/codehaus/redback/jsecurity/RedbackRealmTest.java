package org.codehaus.redback.jsecurity;

import org.codehaus.plexus.redback.policy.UserSecurityPolicy;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.UserAssignment;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.mgt.DefaultSecurityManager;
import org.jsecurity.subject.Subject;

public class RedbackRealmTest extends PlexusInSpringTestCase
{
    private DefaultSecurityManager securityManager;
    private RedbackRealm realm;
    private UserManager userManager;
    private RBACManager rbacManager;
    private UserSecurityPolicy userSecurityPolicy;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        securityManager = new DefaultSecurityManager();
        userManager = (UserManager)lookup(UserManager.ROLE, "memory");
        rbacManager = (RBACManager)lookup(RBACManager.ROLE, "memory");
        userSecurityPolicy = (UserSecurityPolicy)lookup(userSecurityPolicy.ROLE);

        realm = new RedbackRealm(userManager, rbacManager, userSecurityPolicy);
        securityManager.setRealm(realm);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        securityManager.destroy();
        securityManager = null;
        realm = null;
    }

    public void testBasic() throws Exception
    {
        User user = userManager.createUser("test1", "John Tester", "jtester@redback.codehaus.org");
        user.setPassword("password1");
        userManager.addUser(user);

        assertEquals(1, userManager.getUsers().size());
        
        rbacManager.createRole("role1");
        rbacManager.createRole("role2");

        UserAssignment assignment = rbacManager.createUserAssignment(user.getUsername());
        assignment.addRoleName("role1");
        rbacManager.saveUserAssignment(assignment);

        Subject subject = securityManager.login(new UsernamePasswordToken("test1", "password1"));
        assertTrue(subject.isAuthenticated());
        assertTrue(subject.hasRole("role1"));
        assertFalse(subject.hasRole("role2"));
    }
}
