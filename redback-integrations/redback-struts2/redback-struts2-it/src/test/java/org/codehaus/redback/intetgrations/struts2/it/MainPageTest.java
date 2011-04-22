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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVReader;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;

/**
 * @todo more assertions
 * @todo dependencies are too complicated, should be grouped. Bear in mind what happens if one fails.
 */
public class MainPageTest
    extends AbstractSeleniumTestCase
{

    @Test
    public void createUser1()
    {
        homePage();
        loginAdmin();
        selenium.click( "link=userlist" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.click( "usercreate_0" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "userCreateForm_user_username", "user1" );
        selenium.type( "userCreateForm_user_fullName", "User" );
        selenium.type( "userCreateForm_user_email", "user@localhost" );
        selenium.type( "userCreateForm_user_password", "user1" );
        selenium.type( "userCreateForm_user_confirmPassword", "user1" );
        selenium.click( "userCreateForm_0" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.click( "addRolesToUser_submitRolesButton" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    @Test( dependsOnMethods = { "createUser1" } )
    public void loginForcedPasswordChange()
    {
        selenium.deleteAllVisibleCookies();

        selenium.open( "http://localhost:" + System.getProperty( "jetty.port", "8080" ) );
        selenium.click( "link=Login." );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        selenium.type( "loginForm_username", "user1" );
        selenium.type( "loginForm_password", "user1" );
        selenium.click( "loginForm__login" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        assert selenium.getTitle().endsWith( "Change Password" );

        selenium.type( "passwordForm_existingPassword", "user1" );
        selenium.type( "passwordForm_newPassword", "user2" );
        selenium.type( "passwordForm_newPasswordConfirm", "user2" );
        selenium.click( "passwordForm__submit" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    @Test( description = "REDBACK-207", groups = { "disabled" } )
    public void logoutWhenAlreadyLoggedOut()
    {
        logout();
        selenium.deleteAllVisibleCookies();
        logout();
    }

    @SuppressWarnings( "unchecked" )
    @Test( description = "REDBACK-43" )
    public void checkLastLoginDateAndAccountDate()
        throws MalformedURLException, IOException, SAXException, ParseException
    {
        Date date = new Date();

        selenium.deleteAllVisibleCookies();
        selenium.open( "/" );
        loginAdmin();

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
            if ( row[usernameIndex].equals( "admin" ) )
            {
                assert row[lastLoggedInIndex] != null;
                assert !fmt.parse( row[lastLoggedInIndex] ).before( date );
            }

            assert row[dateCreatedIndex] != null;
            assert fmt.parse( row[dateCreatedIndex] ) != null;
        }
    }

}
