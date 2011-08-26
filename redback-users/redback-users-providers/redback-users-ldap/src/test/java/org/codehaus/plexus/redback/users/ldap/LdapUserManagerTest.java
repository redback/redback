package org.codehaus.plexus.redback.users.ldap;

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

import org.codehaus.plexus.redback.common.ldap.connection.LdapConnection;
import org.codehaus.plexus.redback.common.ldap.connection.LdapConnectionFactory;
import org.codehaus.plexus.redback.policy.PasswordEncoder;
import org.codehaus.plexus.redback.policy.encoders.SHA1PasswordEncoder;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.plexus.redback.users.ldap.service.LdapCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.codehaus.plexus.apacheds.ApacheDs;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;

import java.util.List;


/**
 * LdapUserManagerTest 
 *
 * @author <a href="mailto:jesse@codehaus.org">Jesse McConnell</a>
 * @version $Id$
 */  

public class LdapUserManagerTest
    extends PlexusInSpringTestCase
{
    
    protected Logger log = LoggerFactory.getLogger( getClass() );
    
    private UserManager userManager;

    private ApacheDs apacheDs;

    private String suffix;

    private PasswordEncoder passwordEncoder;

    private LdapConnectionFactory connectionFactory;

    private LdapCacheService ldapCacheService;

    public void testFoo()
        throws Exception
    {

    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        ldapCacheService = (LdapCacheService) lookup( LdapCacheService.class.getName() );

        passwordEncoder = new SHA1PasswordEncoder();

        apacheDs = (ApacheDs) lookup( ApacheDs.ROLE, "test" );

        suffix = apacheDs.addSimplePartition( "test", new String[] { "redback", "plexus", "codehaus", "org" } )
            .getSuffix();

        log.info( "DN Suffix: " + suffix );

        apacheDs.startServer();
        
        clearManyUsers();

        makeUsers();

        userManager = (UserManager) lookup( UserManager.ROLE, "ldap" );

        connectionFactory = (LdapConnectionFactory) lookup( LdapConnectionFactory.ROLE, "configurable" );
    }

    protected void tearDown()
        throws Exception
    {
        // clear cache
        ldapCacheService.removeAllUsers();

        InitialDirContext context = apacheDs.getAdminContext();

        context.unbind( createDn( "jesse" ) );

        context.unbind( createDn( "joakim" ) );

        apacheDs.stopServer();

        super.tearDown();
    }

    private void makeUsers()
        throws Exception
    {
        InitialDirContext context = apacheDs.getAdminContext();

        String cn = "jesse";
        bindUserObject( context, cn, createDn( cn ) );
        assertExist( context, createDn( cn ), "cn", cn );

        cn = "joakim";
        bindUserObject( context, cn, createDn( cn ) );
        assertExist( context, createDn( cn ), "cn", cn );

    }

    public void testConnection()
        throws Exception
    {
        assertNotNull( connectionFactory );

        LdapConnection connection = null; 
        try
        {
        connection = connectionFactory.getConnection();

        assertNotNull( connection );

        DirContext context = connection.getDirContext();

        assertNotNull( context );
        } finally {
            connection.close();
        }
    }

    public void testDirectUsersExistence()
        throws Exception
    {
        LdapConnection connection = null; 
        try
        {
        connection = connectionFactory.getConnection();

        DirContext context = connection.getDirContext();

        assertExist( context, createDn( "jesse" ), "cn", "jesse" );
        assertExist( context, createDn( "joakim" ), "cn", "joakim" );
        } finally {
            connection.close();
        }
        
    }

    public void testUserManager()
        throws Exception
    {
        assertNotNull( userManager );

        //assertNull( ldapCacheService.getUser( "jesse" ) );

        assertTrue( userManager.userExists( "jesse" ) );

        //assertNotNull( ldapCacheService.getUser( "jesse" ) );

        List<User> users = userManager.getUsers();

        assertNotNull( users );

        assertEquals( 2, users.size() );

        User jesse = userManager.findUser( "jesse" );

        assertNotNull( jesse );

        assertEquals( "jesse", jesse.getPrincipal().toString() );
        assertEquals( "jesse@apache.org", jesse.getEmail() );
        assertEquals( "foo", jesse.getFullName() );
        System.out.println( "=====>"+jesse.getEncodedPassword());
        System.out.println( "=====>"+passwordEncoder.encodePassword( "foo" ));
        assertTrue( passwordEncoder.isPasswordValid( jesse.getEncodedPassword(), "foo" ) );

    }

    public void testUserNotFoundException()
        throws Exception
    {
        try
        {
            userManager.findUser( "foo bar" );
            fail( "not a UserNotFoundException with an unknown user" );
        }
        catch ( UserNotFoundException e )
        {
            // cool it works !
        }
    }
    
    public void testWithManyUsers()
        throws Exception
    {
        makeManyUsers();
        
        assertNotNull( userManager );

        assertTrue( userManager.userExists( "user10" ) );

        List<User> users = userManager.getUsers();

        assertNotNull( users );

        assertEquals( 10002, users.size() );

        User user10 = userManager.findUser( "user10" );

        assertNotNull( user10 );
    }
    
    private void makeManyUsers()
        throws Exception
    {
        InitialDirContext context = apacheDs.getAdminContext();
        
        for ( int i = 0 ; i < 10000 ; i++ )
        {    
            String cn = "user"+i;
            bindUserObject( context, cn, createDn( cn ) );
        }
    
    }
    
    private void clearManyUsers()
        throws Exception
    {
        InitialDirContext context = apacheDs.getAdminContext();
        
        for ( int i = 0 ; i < 10000 ; i++ )
        {    
            String cn = "user"+i;
            try
            {
                context.unbind( createDn( cn ) );
            }
            catch ( NamingException e )
            {
                // OK lets try with next one
            }
        }
    
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
        attributes.put( "mail", cn+"@apache.org" );
        attributes.put( "userPassword", passwordEncoder.encodePassword( "foo" ) );
        attributes.put( "givenName", "foo" );
        context.createSubcontext( dn, attributes );
    }

    private String createDn( String cn )
    {
        return "cn=" + cn + "," + suffix;
    }

    private void assertExist( DirContext context, String dn, String attribute, String value )
        throws NamingException
    {
        SearchControls ctls = new SearchControls();

        ctls.setDerefLinkFlag( true );
        ctls.setSearchScope( SearchControls.ONELEVEL_SCOPE );
        ctls.setReturningAttributes( new String[] { "*" } );

        BasicAttributes matchingAttributes = new BasicAttributes();
        matchingAttributes.put( attribute, value );
        BasicAttribute objectClass = new BasicAttribute( "objectClass" );
        objectClass.add( "inetOrgPerson" );
        matchingAttributes.put( objectClass );

        NamingEnumeration<SearchResult> results = context.search( suffix, matchingAttributes );
        // NamingEnumeration<SearchResult> results = context.search( suffix, "(" + attribute + "=" + value + ")", ctls
        // );

        assertTrue( results.hasMoreElements() );
        SearchResult result = results.nextElement();
        Attributes attrs = result.getAttributes();
        Attribute testAttr = attrs.get( attribute );
        assertEquals( value, testAttr.get() );

    }

}
