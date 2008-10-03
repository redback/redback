package org.codehaus.plexus.redback.xwork.mail;

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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.redback.configuration.UserConfiguration;
import org.codehaus.plexus.redback.keys.AuthenticationKey;
import org.codehaus.plexus.redback.policy.UserSecurityPolicy;
import org.codehaus.plexus.redback.policy.UserValidationSettings;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * Mailer
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * @plexus.component role="org.codehaus.plexus.redback.xwork.mail.Mailer"
 */
public class MailerImpl
    extends AbstractLogEnabled
    implements Mailer
{
    /**
     * @plexus.requirement role-hint="velocity"
     */
    private MailGenerator generator;

    /**
     * @plexus.requirement role="mailSender"
     */
    private JavaMailSender javaMailSender;    
    
    /**
     * @plexus.requirement
     */
    private SecuritySystem securitySystem;

    /**
     * @plexus.requirement
     */
    private UserConfiguration config;

    public void sendAccountValidationEmail( Collection recipients, AuthenticationKey authkey, String baseUrl )
    {
        String content = generator.generateMail( "newAccountValidationEmail", authkey, baseUrl );

        UserSecurityPolicy policy = securitySystem.getPolicy();
        UserValidationSettings validation = policy.getUserValidationSettings();
        sendMessage( recipients, validation.getEmailSubject(), content );
    }

    public void sendPasswordResetEmail( Collection recipients, AuthenticationKey authkey, String baseUrl )
    {
        String content = generator.generateMail( "passwordResetEmail", authkey, baseUrl );

        UserSecurityPolicy policy = securitySystem.getPolicy();
        UserValidationSettings validation = policy.getUserValidationSettings();
        sendMessage( recipients, validation.getEmailSubject(), content );
    }

    public void sendMessage( Collection recipients, String subject, String content )
    {
        if ( recipients.isEmpty() )
        {
            getLogger().warn( "Mail Not Sent - No mail recipients for email. subject [" + subject + "]" );
            return;
        }

        String fromAddress = config.getString( "email.from.address" );
        String fromName = config.getString( "email.from.name" );

        if ( StringUtils.isEmpty( fromAddress ) )
        {
            fromAddress = System.getProperty( "user.name" ) + "@localhost";
        }

        

        // TODO: Allow for configurable message headers.

        try
        {
            
            MimeMessage message = javaMailSender.createMimeMessage();
            
            message.setSubject( subject );
            message.setText( content );

            InternetAddress from = new InternetAddress( fromAddress, fromName );

            message.setFrom( from );

            Iterator it = recipients.iterator();
            List<Address> tos = new ArrayList<Address>();
            while ( it.hasNext() )
            {
                String mailbox = (String) it.next();

                
                InternetAddress to = new InternetAddress( mailbox.trim() );

                tos.add( to );                
            }

            message.setRecipients(Message.RecipientType.TO, tos.toArray(new Address[tos.size()]));


            getLogger().debug( content );

            javaMailSender.send( message );
        }
        catch ( AddressException e )
        {
            getLogger().error( "Unable to send message, subject [" + subject + "]", e );
        }
        catch ( MessagingException e )
        {
            getLogger().error( "Unable to send message, subject [" + subject + "]", e );
        }       
        catch ( UnsupportedEncodingException e )
        {
            getLogger().error( "Unable to send message, subject [" + subject + "]", e );
        }                
    }
}
