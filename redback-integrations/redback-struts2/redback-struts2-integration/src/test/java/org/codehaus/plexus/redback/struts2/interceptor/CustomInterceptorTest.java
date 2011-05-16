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

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.inject.Named;


/**
 *
 * @author <a href='mailto:rahul.thakur.xdev@gmail.com'>Rahul Thakur</a>
 * @version $Id: CustomInterceptorTest.java 5687 2007-02-14 00:11:28Z brett $
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath*:/META-INF/spring-context.xml", "classpath*:/spring-context.xml" } )
public class CustomInterceptorTest
    extends TestCase
{

    @Inject @Named(value = "testCustomInterceptor")
    MockCustomInterceptor component;

    /**
     *
     * @throws Exception on errors
     */
    @Test
    public void testLookup()
        throws Exception
    {
        assertNotNull( component );
    }
}
