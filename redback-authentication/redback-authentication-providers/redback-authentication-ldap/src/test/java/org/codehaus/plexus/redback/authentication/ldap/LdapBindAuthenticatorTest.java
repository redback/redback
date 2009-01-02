package org.codehaus.plexus.redback.authentication.ldap;

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

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import org.codehaus.plexus.apacheds.ApacheDs;
import org.codehaus.plexus.redback.authentication.AuthenticationResult;
import org.codehaus.plexus.redback.authentication.Authenticator;
import org.codehaus.plexus.redback.authentication.PasswordBasedAuthenticationDataSource;
import org.codehaus.plexus.redback.policy.PasswordEncoder;
import org.codehaus.plexus.redback.policy.encoders.SHA1PasswordEncoder;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapBindAuthenticatorTest
    extends PlexusInSpringTestCase
{
    
    protected Logger log = LoggerFactory.getLogger( getClass() );

    private ApacheDs apacheDs;

    private String suffix;

    private PasswordEncoder passwordEncoder;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        passwordEncoder = new SHA1PasswordEncoder();

        apacheDs = (ApacheDs) lookup( ApacheDs.ROLE, "test" );

        suffix = apacheDs.addSimplePartition( "test", new String[] { "redback", "plexus", "codehaus", "org" } )
            .getSuffix();

        log.info( "DN Suffix: " + suffix );

        apacheDs.startServer();
       
        makeUsers();

    }

    protected void tearDown()
        throws Exception
    {

        InitialDirContext context = apacheDs.getAdminContext();

        context.unbind( createDn( "jesse" ) );

        context.unbind( createDn( "joakim" ) );

        apacheDs.stopServer();

        super.tearDown();
    }

    public void testAuthentication()
        throws Exception
    {
        LdapBindAuthenticator authnr = (LdapBindAuthenticator) lookup( Authenticator.ROLE, "ldap" );
        PasswordBasedAuthenticationDataSource authDs = new PasswordBasedAuthenticationDataSource();
        authDs.setPrincipal( "jesse" );
        authDs.setPassword( passwordEncoder.encodePassword( "foo" ) );
        AuthenticationResult result = authnr.authenticate( authDs );
        assertTrue( result.isAuthenticated() );
    }

    private void makeUsers()
        throws Exception
    {
        InitialDirContext context = apacheDs.getAdminContext();

        String cn = "jesse";
        bindUserObject( context, cn, createDn( cn ) );

        cn = "joakim";
        bindUserObject( context, cn, createDn( cn ) );

    }

    private void bindUserObject( DirContext context, String cn, String dn )
        throws Exception
    {
        Attributes attributes = new BasicAttributes( true );
        BasicAttribute objectClass = new BasicAttribute( "objectClass" );
        objectClass.add( "top" );
        objectClass.add( "inetOrgPerson" );
        objectClass.add( "person" );
        objectClass.add( "organizationalperson" );
        attributes.put( objectClass );
        attributes.put( "cn", cn );
        attributes.put( "sn", "foo" );
        attributes.put( "mail", "foo" );
        attributes.put( "userPassword", passwordEncoder.encodePassword( "foo" ) );
        attributes.put( "givenName", "foo" );
        context.createSubcontext( dn, attributes );
    }

    private String createDn( String cn )
    {
        return "cn=" + cn + "," + suffix;
    }
}
