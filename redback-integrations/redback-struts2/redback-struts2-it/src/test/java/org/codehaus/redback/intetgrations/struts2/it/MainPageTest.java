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
{
    private static final String PAGE_TIMEOUT = "30000";

    private Selenium selenium;

    private String baseUrl;

    @BeforeClass
    public void createSeleniumInstance()
    {
        baseUrl = "http://localhost:" + System.getProperty( "jetty.port", "8080" );
        selenium =
            new DefaultSelenium( "localhost",
                                 Integer.valueOf( System.getProperty( "selenium.server", "4444" ) ).intValue(),
                                 System.getProperty( "selenium.browser", "*firefox" ), baseUrl );
        selenium.start();
    }

    @BeforeSuite
    public void createAdminPage()
    {
        createSeleniumInstance();

        try
        {
            selenium.open( "/security/addadmin.action" );
            selenium.type( "adminCreateForm_user_fullName", "Admin User" );
            selenium.type( "adminCreateForm_user_email", "admin@localhost" );
            selenium.type( "adminCreateForm_user_password", "admin1" );
            selenium.type( "adminCreateForm_user_confirmPassword", "admin1" );
            selenium.click( "adminCreateForm_0" );
            selenium.waitForPageToLoad( PAGE_TIMEOUT );
        }
        finally
        {
            shutdownSelenium();
        }
    }

    @Test( dependsOnMethods = { "homePage" } )
    public void loginAdmin()
    {
        selenium.open( "/main.action" );
        selenium.click( "link=Login." );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "loginForm_username", "admin" );
        selenium.type( "loginForm_password", "admin1" );
        selenium.click( "loginForm__login" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    @Test( dependsOnMethods = { "homePage", "loginAdmin" } )
    public void createUser1()
    {
        selenium.open( "/security/userlist.action" );
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
    public void logout()
    {
        selenium.open( "/security/logout.action" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        assert selenium.getHtmlSource().indexOf( "<h4>This is the example mainpage</h4>" ) >= 0;
    }

    @Test( dependsOnMethods = { "createUser1" } )
    public void loginForcedPasswordChange()
    {
        selenium.deleteAllVisibleCookies();

        selenium.open( "/main.action" );
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

    // REDBACK-274
    @Test( dependsOnMethods = { "loginForcedPasswordChange" } )
    public void csrfCreateUser()
    {
        selenium.deleteAllVisibleCookies();
        homePage();
        loginAdmin();
        selenium.open( "http://localhost:" + System.getProperty( "jetty.port", "8080" ) + "/security/usercreate!submit.action?user.username=tester123" +
            "&user.fullName=test&user.email=test%40test.com&user.password=abc&user.confirmPassword=abc" );
        assertTrue( selenium.isTextPresent( "Security Alert - Invalid Token Found" ) );
        assertTrue( selenium.isTextPresent( "Possible CSRF attack detected! Invalid token found in the request." ) );
    }

    // REDBACK-274
    @Test( dependsOnMethods = { "csrfCreateUser" } )
    public void csrfDeleteUser()
    {
        selenium.open( "/main.action" );
        selenium.open( "http://localhost:" + System.getProperty( "jetty.port", "8080" ) + "/security/userdelete!submit.action?username=test" );
        assertTrue( selenium.isTextPresent( "Security Alert - Invalid Token Found" ) );
        assertTrue( selenium.isTextPresent( "Possible CSRF attack detected! Invalid token found in the request." ) );
    }

    // REDBACK-274
    @Test( dependsOnMethods = { "csrfDeleteUser" } )
    public void csrfAddRolesToUser()
    {
        selenium.open( "/main.action" );
        selenium.open( "http://localhost:" + System.getProperty( "jetty.port", "8080" ) + "/security/addRolesToUser.action?principal=test&" +
            "addRolesButton=true&__checkbox_addNDSelectedRoles=Guest&__checkbox_addNDSelectedRoles=Registered+User&addNDSelectedRoles=" +
            "System+Administrator&__checkbox_addNDSelectedRoles=System+Administrator&__checkbox_addNDSelectedRoles=" +
            "User+Administrator&__checkbox_addNDSelectedRoles=Global+Repository+Manager&__checkbox_addNDSelectedRoles=Global+Repository+Observer&submitRolesButton=Submit" );
        assertTrue( selenium.isTextPresent( "Security Alert - Invalid Token Found" ) );
        assertTrue( selenium.isTextPresent( "Possible CSRF attack detected! Invalid token found in the request." ) );
    }

    @Test
    public void homePage()
    {
        selenium.open( "/" );
        assert selenium.getHtmlSource().indexOf( "<h4>This is the example mainpage</h4>" ) >= 0;
    }

    @Test( dependsOnMethods = { "logout" }, description = "REDBACK-207", groups = { "disabled" } )
    public void logoutWhenAlreadyLoggedOut()
    {
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

    @AfterClass
    public void shutdownSelenium()
    {
        selenium.stop();
    }
}
