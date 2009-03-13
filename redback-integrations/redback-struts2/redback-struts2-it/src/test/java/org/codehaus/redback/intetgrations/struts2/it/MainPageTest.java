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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

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

    @BeforeClass
    public void createSeleniumInstance()
    {
        // todo make browser, URL, port configurable
        selenium =
            new DefaultSelenium( "localhost", 4444, System.getProperty( "selenium.browser", "*firefox" ),
                                 "http://localhost:8080" );
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
    }
    
    @Test ( dependsOnMethods = { "createUser1", "logout" } )
    public void loginForcedPasswordChange()
    {
        selenium.open("/main.action");
        selenium.click("link=Login.");
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type("loginForm_username", "user1");
        selenium.type("loginForm_password", "user1");
        selenium.click("loginForm__login");
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        // TODO: should be on the page to change the password but we aren't due to current bug
    }

    @Test
    public void homePage()
    {
        selenium.open( "/" );
        assert selenium.getHtmlSource().indexOf( "<h4>This is the example mainpage</h4>" ) >= 0;
    }
    
    @Test ( dependsOnMethods = { "logout" }, description="REDBACK-207", groups="disabled" )
    public void logoutWhenAlreadyLoggedOut()
    {
        logout();
        // TODO: assert result - current bug throws an NPE but it isn't propogated here
    }

    @AfterClass
    public void shutdownSelenium()
    {
        selenium.stop();
    }
}
