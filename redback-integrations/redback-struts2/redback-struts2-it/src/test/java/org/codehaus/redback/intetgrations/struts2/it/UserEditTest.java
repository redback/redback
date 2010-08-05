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

import org.testng.annotations.Test;

/**
 * @todo more assertions
 */
public class UserEditTest
    extends AbstractSeleniumTestCase
{

    @Test( dependsOnMethods = { "login" } )
    public void createUser1()
    {
        doLogin( ADMIN_USERNAME, ADMIN_PASSWORD );

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

    @Test( dependsOnMethods = { "login" }, description = "REDBACK-262" )
    public void testUserListUrlLength()
        throws Exception
    {
        doLogin( ADMIN_USERNAME, ADMIN_PASSWORD );

        selenium.open( "/security/userlist.action" );

        selenium.deleteAllVisibleCookies();

        selenium.type( "ec_f_username", "admin" );
        selenium.keyPress( "ec_f_username", "13" );

        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        assert selenium.getTitle().contains( "Security Alert - Action Requires Authentication" );

        selenium.click( "link=Login" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "loginForm_username", "admin" );
        selenium.type( "loginForm_password", "admin1" );
        selenium.click( "loginForm__login" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        assert selenium.getTitle().contains( "[Admin] User List" );
        assert ( selenium.getLocation().length() < 256 );
        
        selenium.click( "link=admin" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        
        assert selenium.getTitle().contains( "[Admin] User Edit" );
        assert ( selenium.getLocation().length() < 256 );
        assert selenium.getLocation().endsWith( "username=admin" );
    }

    @Test( dependsOnMethods = { "login" }, description = "REDBACK-188" )
    public void testUserEdit()
        throws Exception
    {
        doLogin( ADMIN_USERNAME, ADMIN_PASSWORD );

        selenium.open( "/security/userlist.action" );

        assert selenium.getTitle().contains( "[Admin] User List" );

        selenium.deleteAllVisibleCookies();

        selenium.click( "link=admin" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        selenium.click( "link=Login" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "loginForm_username", "admin" );
        selenium.type( "loginForm_password", "admin1" );
        selenium.click( "loginForm__login" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        assert selenium.getTitle().contains( "[Admin] User Edit" );
        assert selenium.getLocation().endsWith( "username=admin" );
    }

}
