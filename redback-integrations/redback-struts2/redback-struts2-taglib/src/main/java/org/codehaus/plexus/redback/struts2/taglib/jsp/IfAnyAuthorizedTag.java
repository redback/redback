package org.codehaus.plexus.redback.struts2.taglib.jsp;

/*
 * Copyright 2005 The Codehaus.
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

import com.opensymphony.xwork2.ActionContext;

import org.codehaus.plexus.redback.authorization.AuthorizationException;
import org.codehaus.plexus.redback.system.SecuritySession;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.system.SecuritySystemConstants;

import java.util.StringTokenizer;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.ConditionalTagSupport;
import org.codehaus.plexus.spring.PlexusToSpringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * IfAnyAuthorizedTag:
 *
 * @author Jesse McConnell <jesse@codehaus.org>
 * @version $Id$
 */
public class IfAnyAuthorizedTag
    extends ConditionalTagSupport
{
    /**
     * comma delimited list of permissions to check
     */
    private String permissions;

    private String resource;

    public void setPermissions( String permissions )
    {
        this.permissions = permissions;
    }

    public void setResource( String resource )
    {
        this.resource = resource;
    }

    protected boolean condition()
        throws JspTagException
    {
        ApplicationContext applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(pageContext.getServletContext());

        ActionContext context = ActionContext.getContext();

        if ( context.getApplication() == null )
        {
            return false;
        }

        SecuritySession securitySession =
            (SecuritySession) context.getSession().get( SecuritySystemConstants.SECURITY_SESSION_KEY );

        try
        {
            final SecuritySystem securitySystem = (SecuritySystem) applicationContext.getBean(PlexusToSpringUtils.buildSpringId(SecuritySystem.ROLE));
            if (securitySystem == null)
            {
                throw new JspTagException( "unable to locate the security system");
            }

            StringTokenizer strtok = new StringTokenizer( permissions, "," );

            while ( strtok.hasMoreTokens() )
            {
                String permission = strtok.nextToken().trim();

                if ( securitySystem.isAuthorized( securitySession, permission, resource ) )
                {
                    return true;
                }
            }
        }
        catch ( AuthorizationException ae )
        {
            throw new JspTagException( "error with authorization", ae );
        }

        return false;
    }
}
