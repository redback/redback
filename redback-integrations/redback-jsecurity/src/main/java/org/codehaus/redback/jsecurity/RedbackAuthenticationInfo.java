package org.codehaus.redback.jsecurity;

import org.jsecurity.authc.AuthenticationInfo;
import org.jsecurity.subject.PrincipalCollection;

public class RedbackAuthenticationInfo implements AuthenticationInfo
{
    public Object getCredentials() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PrincipalCollection getPrincipals() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
