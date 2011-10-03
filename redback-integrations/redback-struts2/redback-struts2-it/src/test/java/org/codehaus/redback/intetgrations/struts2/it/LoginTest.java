package org.codehaus.redback.intetgrations.struts2.it;

/*
 * Copyright 2009 The Codehaus.
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

import au.com.bytecode.opencsv.CSVReader;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @todo more assertions
 */
public class LoginTest
    extends AbstractSeleniumTestCase
{
    @Test
    public void login()
    {
        selenium.deleteAllVisibleCookies();

        selenium.open( "/main.action" );
        selenium.click( "loginLink" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "loginForm_username", ADMIN_USERNAME );
        selenium.type( "loginForm_password", ADMIN_PASSWORD );
        selenium.click( "loginSubmit" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        // TODO: assertion
    }

    @Test
    public void createUser1()
    {
        doLogin( ADMIN_USERNAME, ADMIN_PASSWORD );

        selenium.open( "/security/userlist.action" );
        selenium.click( "id=userCreateButton" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "userCreateForm_user_username", "user1" );
        selenium.type( "userCreateForm_user_fullName", "User" );
        selenium.type( "userCreateForm_user_email", "user@localhost" );
        selenium.type( "userCreateForm_user_password", "user1" );
        selenium.type( "userCreateForm_user_confirmPassword", "user1" );
        selenium.click( "id=userCreateSubmit" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.click( "addRolesToUser_submitRolesButton" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    @Test( dependsOnMethods = { "login", "createUser1" } )
    public void loginForcedPasswordChange()
    {
        doLogin( "user1", "user1" );
        assert selenium.getTitle().endsWith( "Change Password" );

        selenium.type( "passwordForm_existingPassword", "user1" );
        selenium.type( "passwordForm_newPassword", "user2" );
        selenium.type( "passwordForm_newPasswordConfirm", "user2" );
        selenium.click( "passwordForm__submit" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    @Test( dependsOnMethods = { "login" } )
    public void logout()
    {
        doLogin( ADMIN_USERNAME, ADMIN_PASSWORD );
        selenium.open( "/security/logout.action" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        String s = selenium.getHtmlSource();
        assert s.indexOf( "<h4>This is the example mainpage</h4>" ) >= 0;
    }

    @Test( dependsOnMethods = { "logout" }, description = "REDBACK-207" )
    public void logoutWhenAlreadyLoggedOut()
    {
        selenium.deleteAllVisibleCookies();
        logout();
    }

    @SuppressWarnings( "unchecked" )
    @Test( dependsOnMethods = "login", description = "REDBACK-43" )
    public void checkLastLoginDateAndAccountDate()
        throws IOException, SAXException, ParseException
    {
        Calendar cal = Calendar.getInstance();
        cal.set( Calendar.MILLISECOND, 0 );
        Date date = cal.getTime();

        doLogin( ADMIN_USERNAME, ADMIN_PASSWORD );

        String value = selenium.getCookieByName( "rbkSignon" );
        WebConversation wc = new WebConversation();
        WebRequest req =
            new GetMethodWebRequest( baseUrl + "/security/report!generate.action?reportId=userlist&reportType=csv" );
        wc.putCookie( "rbkSignon", value );
        WebResponse resp = wc.getResponse( req );

        CSVReader reader = new CSVReader( new InputStreamReader( resp.getInputStream() ) );
        List<String[]> rows = reader.readAll();
        String[] headers = rows.get( 0 );
        int lastLoggedInIndex = Arrays.asList( headers ).indexOf( "Date Last Logged In" );
        assert lastLoggedInIndex >= 0;
        int dateCreatedIndex = Arrays.asList( headers ).indexOf( "Date Created" );
        assert dateCreatedIndex >= 0;
        int usernameIndex = Arrays.asList( headers ).indexOf( "User Name" );
        assert usernameIndex >= 0;

        DateFormat fmt = new SimpleDateFormat( "EEE MMM dd HH:mm:ss zzz yyyy" );
        fmt.setLenient( true );

        rows = rows.subList( 1, rows.size() );
        for ( String[] row : rows )
        {
            if ( row[usernameIndex].equals( ADMIN_USERNAME ) )
            {
                assert row[lastLoggedInIndex] != null;
                Date parsedDate = fmt.parse( row[lastLoggedInIndex] );
                assert !parsedDate.before( date );
            }

            assert row[dateCreatedIndex] != null;
            assert fmt.parse( row[dateCreatedIndex] ) != null;
        }
    }   
}
