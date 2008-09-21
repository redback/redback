package org.codehaus.plexus.redback.xwork.filter;

/*
 * Copyright 2005-2006 The Codehaus.
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

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * PlexusServletFilter
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 */
public abstract class PlexusServletFilter
    implements Filter
{
    private ApplicationContext applicationContext;

    private Logger logger;

    public void destroy()
    {
        // Do nothing here.
    }

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public Object lookup( String role )
        throws ServletException
    {
        Object o = getApplicationContext().getBean(PlexusToSpringUtils.buildSpringId(role));
        if (o == null)
        {
            throw new ServletException( "Unable to lookup plexus component '" + role + "'.");
        }
        return o;
    }

    public Object lookup( String role, String hint )
        throws ServletException
    {
        Object o = getApplicationContext().getBean(PlexusToSpringUtils.buildSpringId(role, hint));
        if (o == null)
        {
            throw new ServletException( "Unable to lookup plexus component '" + role + "'.");
        }
        return o;
    }

    public void init( FilterConfig filterConfig )
        throws ServletException
    {
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());

        LoggerManager loggerManager = (LoggerManager) getApplicationContext().getBean(PlexusToSpringUtils.buildSpringId(LoggerManager.ROLE));
        logger = loggerManager.getLoggerForComponent( this.getClass().getName() );
        if (logger == null)
        {
            throw new ServletException( "Unable to lookup Logger from plexus.");
        }
    }

    public Logger getLogger()
    {
        return logger;
    }
}
