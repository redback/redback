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

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class MainPageTest
    extends AbstractSeleniumTestCase
{

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

    @Test
    public void homePage()
    {
        selenium.open( "/" );
        assert selenium.getHtmlSource().indexOf( "<h4>This is the example mainpage</h4>" ) >= 0;
    }
}
