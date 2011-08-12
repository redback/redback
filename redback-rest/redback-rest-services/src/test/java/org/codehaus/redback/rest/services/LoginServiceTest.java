package org.codehaus.redback.rest.services;

/*
 * Copyright 2011 The Codehaus.
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

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.codehaus.redback.rest.api.services.UserService;
import org.junit.Test;

/**
 * @author Olivier Lamy
 */
public class LoginServiceTest
    extends AbstractRestServicesTest
{
    @Test
    public void adminLoginTest()
        throws Exception
    {
        UserService userService =
            JAXRSClientFactory.create( "http://localhost:" + port + "/services/redbackServices", UserService.class );
        userService.ping();
    }
}
