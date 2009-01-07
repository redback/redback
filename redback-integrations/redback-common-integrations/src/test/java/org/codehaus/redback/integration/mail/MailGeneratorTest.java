package org.codehaus.redback.integration.mail;

/*
* Copyright 2005-2006 The Codehaus.
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

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import net.sf.ehcache.CacheManager;

import org.codehaus.plexus.jdo.DefaultConfigurableJdoFactory;
import org.codehaus.plexus.jdo.JdoFactory;
import org.codehaus.plexus.redback.keys.AuthenticationKey;
import org.codehaus.plexus.redback.keys.KeyManager;
import org.codehaus.plexus.redback.keys.KeyManagerException;
import org.codehaus.plexus.redback.policy.UserSecurityPolicy;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;
import org.codehaus.redback.integration.mail.MailGenerator;
import org.jpox.SchemaTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the Mailer class.
 */
public class MailGeneratorTest
    extends PlexusInSpringTestCase
{
    private MailGenerator generator;

    private UserSecurityPolicy policy;

    private KeyManager keyManager;
    
    private Logger log = LoggerFactory.getLogger( getClass() );

    protected void setUp()
        throws Exception
    {
        CacheManager.getInstance().clearAll();
        CacheManager.getInstance().removalAll();
        CacheManager.getInstance().shutdown();
        super.setUp();

        DefaultConfigurableJdoFactory jdoFactory = (DefaultConfigurableJdoFactory) lookup( JdoFactory.ROLE, "users" );

        jdoFactory.setPassword( "" );

        jdoFactory.setProperty( "org.jpox.transactionIsolation", "READ_COMMITTED" ); //$NON-NLS-1$ //$NON-NLS-2$

        jdoFactory.setProperty( "org.jpox.poid.transactionIsolation", "READ_COMMITTED" ); //$NON-NLS-1$ //$NON-NLS-2$

        jdoFactory.setProperty( "org.jpox.autoCreateSchema", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
        
        Properties properties = jdoFactory.getProperties();

        for ( Iterator it = properties.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();

            System.setProperty( (String) entry.getKey(), (String) entry.getValue() );
        }

        SchemaTool.createSchemaTables( new URL[] { getClass()
            .getResource( "/org/codehaus/plexus/redback/keys/jdo/package.jdo" ) }, new URL[] {}, null, false, null ); //$NON-NLS-1$

        PersistenceManagerFactory pmf = jdoFactory.getPersistenceManagerFactory();

        assertNotNull( pmf );

        PersistenceManager pm = pmf.getPersistenceManager();

        pm.close();        
        
        generator = (MailGenerator) lookup( MailGenerator.ROLE, "velocity" );

        policy = (UserSecurityPolicy) lookup( UserSecurityPolicy.ROLE );

        keyManager = (KeyManager) lookup( KeyManager.ROLE, "memory" );
    }

    public void testGeneratePasswordResetMail()
        throws KeyManagerException
    {
        AuthenticationKey authkey = keyManager.createKey( "username", "Password Reset Request",
                                                          policy.getUserValidationSettings().getEmailValidationTimeout() );

        String content = generator.generateMail( "passwordResetEmail", authkey, "baseUrl" );

        assertNotNull( content );
        assertTrue( content.indexOf( '$' ) == -1 ); // make sure everything is properly populate
    }

    public void testGenerateAccountValidationMail()
        throws KeyManagerException
    {
        AuthenticationKey authkey = keyManager.createKey( "username", "New User Email Validation",
                                                          policy.getUserValidationSettings().getEmailValidationTimeout() );

        String content = generator.generateMail( "newAccountValidationEmail", authkey, "baseUrl" );

        assertNotNull( content );
        assertTrue( content.indexOf( '$' ) == -1 ); // make sure everything is properly populate
    }

    public void testGenerateAccountValidationMailCustomUrl()
        throws Exception
    {
        AuthenticationKey authkey = keyManager.createKey( "username", "New User Email Validation",
                                                          policy.getUserValidationSettings().getEmailValidationTimeout() );

        generator = (MailGenerator) lookup( MailGenerator.ROLE, "custom-url" );
        String content = generator.generateMail( "newAccountValidationEmail", authkey, "baseUrl" );

        assertNotNull( content );
        assertTrue( content.indexOf( "baseUrl" ) == -1 ); // make sure everything is properly populate
        assertTrue( content.indexOf( "MY_APPLICATION_URL/security" ) > 0 ); // make sure everything is properly populate
    }

    public void testGeneratePasswordResetMailCustomUrl()
        throws Exception
    {
        AuthenticationKey authkey = keyManager.createKey( "username", "Password Reset Request",
                                                          policy.getUserValidationSettings().getEmailValidationTimeout() );

        generator = (MailGenerator) lookup( MailGenerator.ROLE, "custom-url" );
        String content = generator.generateMail( "passwordResetEmail", authkey, "baseUrl" );

        assertNotNull( content );
        
        log.info( "mail content " + content );
        
        assertTrue( content.indexOf( "baseUrl" ) == -1 ); // make sure everything is properly populate
        assertTrue( content.indexOf( "MY_APPLICATION_URL/security" ) > 0 ); // make sure everything is properly populate
    }
}
