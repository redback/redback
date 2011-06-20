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

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Note: dependencies are for interdependent tests (as in, won't pass if this doesn't), not for sequencing the UI.
 * Always ensure that the test adequately prepares its own state
 */
public abstract class AbstractSeleniumTestCase
{
    protected static final String PAGE_TIMEOUT = "30000";

    protected static final String ADMIN_USERNAME = "admin";

    protected static final String ADMIN_PASSWORD = "admin1";

    protected static Selenium selenium;

    protected static String baseUrl;

    protected void doLogin( String username, String password )
    {
        selenium.deleteAllVisibleCookies();
        selenium.open( "/security/login.action" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "loginForm_username", username );
        selenium.type( "loginForm_password", password );
        selenium.click( "loginForm__login" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
    }

    @BeforeSuite
    public void createSeleniumInstance()
    {
        baseUrl = "http://localhost:" + System.getProperty( "jetty.port", "8080" );
        selenium =
            new DefaultSelenium( "localhost", Integer.valueOf( System.getProperty( "selenium.port", "4444" ) ),
                                 System.getProperty( "selenium.browser", "*firefox" ), baseUrl );
        selenium.start();
    }

    @AfterSuite
    public void shutdownSelenium()
    {
        selenium.stop();
    }

}
