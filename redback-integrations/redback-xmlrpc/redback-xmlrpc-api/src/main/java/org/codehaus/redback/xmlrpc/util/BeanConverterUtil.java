package org.codehaus.redback.xmlrpc.util;

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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.redback.xmlrpc.bean.User;

public class BeanConverterUtil
{
    public static Map<String, String> toMap( User user )
    {
        Map<String, String> userMap = new HashMap<String, String>();

        userMap.put( User.KEY_USERNAME, user.getUsername() );
        userMap.put( User.KEY_FULLNAME, user.getFullname() );
        userMap.put( User.KEY_EMAIL, user.getEmail() );
        userMap.put( User.KEY_VALIDATED, "" + user.isValidated() );
        userMap.put( User.KEY_LOCKED, "" + user.isLocked() );

        return userMap;
    }

    public static User toUser( Map<String, String> userMap )
    {
        User user = new User();

        user.setUsername( userMap.get( User.KEY_USERNAME ) );
        user.setFullname( userMap.get( User.KEY_FULLNAME ) );
        user.setEmail( userMap.get( User.KEY_EMAIL ) );
        user.setValidated( Boolean.parseBoolean( userMap.get( User.KEY_VALIDATED ) ) );
        user.setLocked( Boolean.parseBoolean( userMap.get( User.KEY_LOCKED ) ) );

        return user;
    }
}
