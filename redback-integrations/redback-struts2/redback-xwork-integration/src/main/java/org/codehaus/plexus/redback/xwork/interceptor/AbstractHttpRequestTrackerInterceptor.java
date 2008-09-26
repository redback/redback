package org.codehaus.plexus.redback.xwork.interceptor;

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

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import java.util.Map;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.springframework.context.ApplicationContextAware;

public abstract class AbstractHttpRequestTrackerInterceptor
    extends AbstractLogEnabled
    implements Interceptor, ApplicationContextAware
{
    public static final String TRACKER_NAME = ActionInvocationTracker.ROLE + ":name";
    
    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException
    {
        this.applicationContext = applicationContext;
    }

    protected ApplicationContext getApplicationContext()
    {
        return applicationContext;
    }
    
    protected abstract String getTrackerName();

    protected synchronized ActionInvocationTracker addActionInvocation( ActionInvocation invocation )
        throws ComponentLookupException
    {
        Map sessionMap = invocation.getInvocationContext().getSession();
        
        ActionInvocationTracker tracker = (ActionInvocationTracker) sessionMap.get( ActionInvocationTracker.ROLE );

        if ( tracker == null )
        {
            final String beanName = PlexusToSpringUtils.buildSpringId(ActionInvocationTracker.ROLE, getTrackerName());
            //noinspection deprecation
            tracker = (ActionInvocationTracker) getApplicationContext().getBean(beanName);
            sessionMap.put( ActionInvocationTracker.ROLE, tracker );
        }

        tracker.addActionInvocation( invocation );

        return tracker;
    }
}
