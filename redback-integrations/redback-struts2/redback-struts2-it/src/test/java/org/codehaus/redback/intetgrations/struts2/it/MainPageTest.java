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

    @Test
    public void testHomePage()
    {
        selenium.open( "/" );
        assert selenium.getHtmlSource().indexOf( "<h4>This is the example mainpage</h4>" ) >= 0;
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

    @AfterClass
    public void shutdownSelenium()
    {
        selenium.stop();
    }
}
