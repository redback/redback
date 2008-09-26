/**
 *
 */
package org.codehaus.plexus.redback.struts2.interceptor;

/*
 * Copyright 2006-2007 The Codehaus Foundation.
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

import com.opensymphony.xwork2.interceptor.Interceptor;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;


/**
 * Test for {@link PlexusObjectFactory} when it attempts to lookup Custom Xwork interceptors.
 *
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id: CustomInterceptorTest.java 5687 2007-02-14 00:11:28Z brett $
 */
public class CustomInterceptorTest
    extends PlexusInSpringTestCase
{

    /**
     * Tests a plain Interceptor lookup that <em>does not</em> use the {@link PlexusObjectFactory}
     *
     * @throws Exception on errors
     */
    public void testLookup()
        throws Exception
    {
        MockCustomInterceptor component =
            (MockCustomInterceptor) lookup( Interceptor.class.getName(), "testCustomInterceptor" );
        assertNotNull( component );
    }
}
