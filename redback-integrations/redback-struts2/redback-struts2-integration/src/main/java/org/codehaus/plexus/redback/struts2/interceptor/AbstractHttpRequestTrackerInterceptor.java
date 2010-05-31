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

import java.util.Map;

import org.apache.struts2.StrutsException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public abstract class AbstractHttpRequestTrackerInterceptor
    extends AbstractInterceptor
{
    public static final String TRACKER_NAME = ActionInvocationTracker.ROLE + ":name";
    
    protected Logger logger = LoggerFactory.getLogger( getClass() );
    
    protected abstract String getTrackerName();

    @Override
    public void init()
    {
        super.init();
        logger.info( this.getClass().getName() + " initialized!" );
    }
    
    @SuppressWarnings("unchecked")
    protected synchronized ActionInvocationTracker addActionInvocation( ActionInvocation invocation )
        throws ComponentLookupException
    {
        Map<String, Object> sessionMap = invocation.getInvocationContext().getSession();
        
        ApplicationContext applicationContext = (ApplicationContext) ActionContext.getContext().getApplication().get(
                    WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (applicationContext == null)
        {
            throw new StrutsException("Could not locate ApplicationContext");
        }
        
        ActionInvocationTracker tracker = (ActionInvocationTracker) sessionMap.get( ActionInvocationTracker.ROLE );

        if ( tracker == null )
        {
            final String beanName = PlexusToSpringUtils.buildSpringId(ActionInvocationTracker.ROLE, getTrackerName());
            //noinspection deprecation
            tracker = (ActionInvocationTracker) applicationContext.getBean(beanName);
            sessionMap.put( ActionInvocationTracker.ROLE, tracker );
        }

        tracker.addActionInvocation( invocation );

        return tracker;
    }
}
