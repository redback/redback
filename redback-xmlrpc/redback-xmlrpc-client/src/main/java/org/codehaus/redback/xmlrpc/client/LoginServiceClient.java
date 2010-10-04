package org.codehaus.redback.xmlrpc.client;

import java.net.URL;

import org.codehaus.redback.xmlrpc.service.LoginService;

import com.atlassian.xmlrpc.AuthenticationInfo;
import com.atlassian.xmlrpc.Binder;
import com.atlassian.xmlrpc.DefaultBinder;

public class LoginServiceClient
    implements LoginService, ServiceClient
{
    private LoginService loginService;

    public LoginServiceClient()
    {
    }

    public LoginServiceClient( String url )
        throws Exception
    {
        bind( url );
    }

    public LoginServiceClient( String url, String username, String password )
        throws Exception
    {
        bind( url, username, password );
    }

    public void bind( String url )
        throws Exception
    {
        bind( url, "", "" );
    }

    public void bind( String url, String username, String password )
        throws Exception
    {
        Binder binder = new DefaultBinder();

        loginService = binder.bind( LoginService.class, new URL( url ), new AuthenticationInfo( username, password ) );
    }

    public Boolean ping()
        throws Exception
    {
        return loginService.ping();
    }

    public int addKey( String key, String principal, String purpose, int expirationMinutes )
        throws Exception
    {
        return loginService.addKey( key, principal, purpose, expirationMinutes );
    }

    public int removeFromCache( String username )
        throws Exception
    {
        return loginService.removeFromCache( username );
    }
}
