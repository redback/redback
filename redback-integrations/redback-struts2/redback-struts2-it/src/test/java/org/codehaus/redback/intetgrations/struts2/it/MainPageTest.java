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
}
