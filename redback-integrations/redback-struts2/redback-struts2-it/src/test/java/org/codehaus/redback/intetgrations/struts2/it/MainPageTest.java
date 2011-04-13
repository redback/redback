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

import com.thoughtworks.selenium.DefaultSelenium;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class MainPageTest
    extends AbstractSeleniumTestCase
{
    @BeforeSuite
    public void createSeleniumInstance()
    {
        baseUrl = "http://localhost:" + System.getProperty( "jetty.port", "8080" );
        selenium =
            new DefaultSelenium( "localhost", Integer.valueOf( System.getProperty( "selenium.server", "4444" ) ),
                                 System.getProperty( "selenium.browser", "*firefox" ), baseUrl );
        selenium.start();
    }

    @AfterSuite
    public void shutdownSelenium()
    {
        selenium.stop();
    }

    @BeforeSuite(dependsOnMethods = "createSeleniumInstance")
    public void createAdminPage()
    {
        selenium.open( "/security/addadmin.action" );
        selenium.type( "adminCreateForm_user_fullName", "Admin User" );
        selenium.type( "adminCreateForm_user_email", "admin@localhost" );
        selenium.type( "adminCreateForm_user_password", ADMIN_PASSWORD );
        selenium.type( "adminCreateForm_user_confirmPassword", ADMIN_PASSWORD );
        selenium.click( "adminCreateForm_0" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    // start - REDBACK-274 tests

    @Test( dependsOnMethods = { "loginForcedPasswordChange" } )
    public void csrfCreateUser()
    {
        selenium.deleteAllVisibleCookies();
        homePage();
        doLogin( ADMIN_USERNAME, ADMIN_PASSWORD );
        selenium.open( "/security/usercreate!submit.action?user.username=tester123" +
            "&user.fullName=test&user.email=test%40test.com&user.password=abc&user.confirmPassword=abc" );
        assertTrue( selenium.isTextPresent( "Security Alert - Invalid Token Found" ) );
        assertTrue( selenium.isTextPresent( "Possible CSRF attack detected! Invalid token found in the request." ) );
    }

    @Test( dependsOnMethods = { "csrfCreateUser" } )
    public void csrfDeleteUser()
    {
        selenium.open( "/main.action" );
        selenium.open( "/security/userdelete!submit.action?username=test" );
        assertTrue( selenium.isTextPresent( "Security Alert - Invalid Token Found" ) );
        assertTrue( selenium.isTextPresent( "Possible CSRF attack detected! Invalid token found in the request." ) );
    }

    @Test( dependsOnMethods = { "csrfDeleteUser" } )
    public void csrfAddRolesToUser()
    {
        selenium.open( "/main.action" );
        selenium.open( "/security/addRolesToUser.action?principal=test&" +
            "addRolesButton=true&__checkbox_addNDSelectedRoles=Guest&__checkbox_addNDSelectedRoles=Registered+User&addNDSelectedRoles=" +
            "System+Administrator&__checkbox_addNDSelectedRoles=System+Administrator&__checkbox_addNDSelectedRoles=" +
            "User+Administrator&__checkbox_addNDSelectedRoles=Global+Repository+Manager&__checkbox_addNDSelectedRoles=Global+Repository+Observer&submitRolesButton=Submit" );
        assertTrue( selenium.isTextPresent( "Security Alert - Invalid Token Found" ) );
        assertTrue( selenium.isTextPresent( "Possible CSRF attack detected! Invalid token found in the request." ) );
    }

    // end - REDBACK-274 tests

    // start - REDBACK-276 tests

    @Test( dependsOnMethods = { "csrfAddRolesToUser" } )
    public void createUserInvalidCharsInUsername()
    {
        selenium.open( "/main.action" );
        selenium.click( "link=userlist" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.click( "usercreate_0" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "userCreateForm_user_username", "user1<script/>" );
        selenium.type( "userCreateForm_user_fullName", "User" );
        selenium.type( "userCreateForm_user_email", "user@localhost" );
        selenium.type( "userCreateForm_user_password", "user1" );
        selenium.type( "userCreateForm_user_confirmPassword", "user1" );
        selenium.click( "userCreateForm_0" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        assertTrue( selenium.isTextPresent( "Invalid characters found in Username." ) );
    }

    @Test( dependsOnMethods = { "createUserInvalidCharsInUsername" } )
    public void createUserInvalidCharsInFullname()
    {
        selenium.open( "/main.action" );
        selenium.click( "link=userlist" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.click( "usercreate_0" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "userCreateForm_user_username", "user2" );
        selenium.type( "userCreateForm_user_fullName", "User()" );
        selenium.type( "userCreateForm_user_email", "user@localhost" );
        selenium.type( "userCreateForm_user_password", "user1" );
        selenium.type( "userCreateForm_user_confirmPassword", "user1" );
        selenium.click( "userCreateForm_0" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        assertTrue( selenium.isTextPresent( "Invalid characters found in Full Name." ) );
    }

    @Test( dependsOnMethods = { "createUserInvalidCharsInFullname" } )
    public void XSSUserEditAction()
    {
        selenium.open( "/main.action" );
        selenium.open( "http://localhost:" + System.getProperty( "jetty.port", "8080" ) + "/security/useredit.action?username=test%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E" );
       
        // html should have been escaped!
        assertFalse( selenium.isAlertPresent() );
        assertTrue( selenium.isTextPresent( "User 'test&lt;script&gt;alert(&apos;xss&apos;)&lt;/script&gt;' does not exist." ) );
    }

    @Test( dependsOnMethods = { "XSSUserEditAction" } )
    public void XSSUserListAction()
    {
        selenium.open( "/main.action" );
        selenium.open( "http://localhost:" + System.getProperty( "jetty.port", "8080" ) + "/security/userlist!show.action?roleName=test%3Cscript%3Ealert%28%27xss%27%29%3C/script%3E" );

        // html should have been escaped!
        assertFalse( selenium.isAlertPresent() );
        assertTrue( selenium.isTextPresent( "test<script>alert('xss')</script>" ) );
        assertTrue( selenium.isTextPresent( "There were no results found." ) );
    }

    @Test( dependsOnMethods = { "XSSUserListAction" } )
    public void XSSRoleEditAction()
    {
        selenium.open( "/main.action" );
        selenium.open( "http://localhost:" + System.getProperty( "jetty.port", "8080" ) + "/security/roleedit.action?name=%22%3E%3Cscript%3Ealert%28%27xss%27%29%3C%2Fscript%3E" );

        // html should have been escaped!
        assertFalse( selenium.isAlertPresent() );
        assertTrue( selenium.isTextPresent( "&quot;&gt;&lt;script&gt;alert(&apos;xss&apos;)&lt;/script&gt;" ) );
    }
    
    // end - REDBACK-276 tests

    @Test
    public void homePage()
    {
        selenium.open( "/" );
        assert selenium.getHtmlSource().indexOf( "<h4>This is the example mainpage</h4>" ) >= 0;
    }

}
