package org.codehaus.redback.jsecurity;

import java.util.Collection;
import org.jsecurity.authz.AuthorizationInfo;
import org.jsecurity.authz.Permission;

public class RedbackAuthorizationInfo implements AuthorizationInfo
{
    public Collection<Permission> getObjectPermissions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<String> getRoles() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Collection<String> getStringPermissions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
