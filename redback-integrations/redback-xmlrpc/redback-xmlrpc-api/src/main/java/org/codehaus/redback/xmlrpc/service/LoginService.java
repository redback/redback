package org.codehaus.redback.xmlrpc.service;

import com.atlassian.xmlrpc.ServiceObject;

@ServiceObject("LoginService")
public interface LoginService
{
    public Boolean ping()
        throws Exception;

    public int addAuthenticationKey( String key, String principal, String purpose, int expirationMinutes )
        throws Exception;

    public int removeFromCache( String username )
        throws Exception;
}
