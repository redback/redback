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
    @Test( description = "REDBACK-262" )
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

    @Test( description = "REDBACK-188" )
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

    @Test( description = "REDBACK-166", enabled = false )
    public void testFeedbackAfterUserEdit()
        throws Exception
    {
        selenium.open( "/security/userlist.action" );
        selenium.click( "link=user1" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        selenium.type( "userEditForm_user_fullName", "User Edited" );
        selenium.click( "userEditForm__submit" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        // Not yet implemented. Since we redirect to the userlist, it is difficult to pass the success message there.
        assert selenium.isTextPresent( "User details for 'user1' successfully changed" );
    }

    @Test( description = "REDBACK-157", dependsOnMethods = "loginForcedPasswordChange" )
    public void testForceChangePassword()
        throws Exception
    {
        doLogin( ADMIN_USERNAME, ADMIN_PASSWORD );

        selenium.open( "/security/userlist.action" );
        assert selenium.getTitle().contains( "[Admin] User List" );

        selenium.click( "link=user1" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        assert selenium.getTitle().contains( "[Admin] User Edit" );

        selenium.type( "userEditForm_user_password", "user0" );
        selenium.type( "userEditForm_user_confirmPassword", "user0" );
        selenium.click( "userEditForm_user_passwordChangeRequired" );
        selenium.click( "userEditForm__submit" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        assert selenium.getTitle().contains( "[Admin] User Edit - Confirm Administrator Password" );

        selenium.type( "userEditForm_userAdminPassword", ADMIN_PASSWORD );
        selenium.click( "userEditForm__confirmAdminPassword" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        //assert selenium.getTitle().contains( "[Admin] User List" );

        selenium.click( "link=Logout" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );

        doLogin( "user1", "user0" );

        assert selenium.getTitle().contains( "Change Password" );
        selenium.type( "passwordForm_existingPassword", "user0" );
        selenium.type( "passwordForm_newPassword", "user3" );
        selenium.type( "passwordForm_newPasswordConfirm", "user3" );
        selenium.click( "passwordForm__submit" );
        selenium.waitForPageToLoad( PAGE_TIMEOUT );
        assert selenium.isTextPresent( "Password successfully changed" );
    }
}
