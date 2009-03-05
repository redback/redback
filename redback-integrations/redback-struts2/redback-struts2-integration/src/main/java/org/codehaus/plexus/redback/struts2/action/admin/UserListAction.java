package org.codehaus.plexus.redback.struts2.action.admin;

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.struts2.ServletActionContext;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.RbacObjectNotFoundException;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.redback.rbac.Role;
import org.codehaus.plexus.redback.rbac.UserAssignment;
import org.codehaus.plexus.redback.struts2.action.AbstractSecurityAction;
import org.codehaus.plexus.redback.system.SecuritySystem;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserQuery;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.redback.integration.interceptor.SecureActionBundle;
import org.codehaus.redback.integration.interceptor.SecureActionException;
import org.codehaus.redback.integration.reports.ReportManager;
import org.codehaus.redback.integration.role.RoleConstants;
import org.extremecomponents.table.context.Context;
import org.extremecomponents.table.context.HttpServletRequestContext;
import org.extremecomponents.table.limit.FilterSet;
import org.extremecomponents.table.limit.Limit;
import org.extremecomponents.table.limit.LimitFactory;
import org.extremecomponents.table.limit.TableLimit;
import org.extremecomponents.table.limit.TableLimitFactory;

/**
 * UserListAction
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action"
 * role-hint="redback-admin-user-list"
 * instantiation-strategy="per-lookup"
 */
public class UserListAction
    extends AbstractSecurityAction
{
    // ------------------------------------------------------------------
    // Plexus Component Requirements
    // ------------------------------------------------------------------

    /**
     * @plexus.requirement
     */
    private SecuritySystem securitySystem;

    /**
     * @plexus.requirement role-hint="cached"
     */
    private RBACManager rbac;

    /**
     * @plexus.requirement
     */
    private ReportManager reportManager;

    // ------------------------------------------------------------------
    // Action Parameters
    // ------------------------------------------------------------------

    private List users;

    private List roles;

    private String roleName;

    // ------------------------------------------------------------------
    // Action Entry Points - (aka Names)
    // ------------------------------------------------------------------

    public String show()
    {
        try
        {
            roles = rbac.getAllRoles();
        }
        catch ( RbacManagerException e )
        {
            roles = Collections.EMPTY_LIST;
        }

        if ( StringUtils.isEmpty( roleName ) )
        {
            users = findUsersWithFilter();
        }
        else
        {
            try
            {
                Role target = rbac.getRole( roleName );
                Set targetRoleNames = new HashSet();

                for ( int i = 0; i < roles.size(); i++ )
                {
                    Role r = (Role) roles.get( i );
                    if ( rbac.getEffectiveRoles( r ).contains( target ) )
                    {
                        targetRoleNames.add( r.getName() );
                    }
                }

                users = findUsers( targetRoleNames );
            }
            catch ( RbacObjectNotFoundException e )
            {
                users = Collections.EMPTY_LIST;
            }
            catch ( RbacManagerException e )
            {
                users = Collections.EMPTY_LIST;
            }
        }

        if ( users == null )
        {
            users = Collections.EMPTY_LIST;
        }

        return INPUT;
    }

    public SecureActionBundle initSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( RoleConstants.USER_MANAGEMENT_USER_LIST_OPERATION, Resource.GLOBAL );
        bundle.addRequiredAuthorization( RoleConstants.USER_MANAGEMENT_USER_ROLE_OPERATION, Resource.GLOBAL );
        return bundle;
    }

    private List findUsers( Collection roleNames )
    {
        List usernames = getUsernamesForRoles( roleNames );
        List users = findUsersWithFilter();
        List filteredUsers = new ArrayList();

        for ( Iterator i = users.iterator(); i.hasNext(); )
        {
            User user = (User) i.next();
            if ( usernames.contains( user.getUsername() ) )
            {
                filteredUsers.add( user );
            }
        }

        return filteredUsers;
    }
    
    private List findUsersWithFilter()
    {
        Context context = new HttpServletRequestContext(ServletActionContext.getRequest());
        LimitFactory limitFactory = new TableLimitFactory(context);
        Limit limit = new TableLimit(limitFactory);
        FilterSet filterSet = limit.getFilterSet();            

        UserQuery query = getUserManager().createUserQuery();
        if (filterSet.getFilter("username")!=null) {
            query.setUsername(filterSet.getFilter("username").getValue());
        }
        if (filterSet.getFilter("fullName")!=null) {
            query.setFullName(filterSet.getFilter("fullName").getValue());
        }
        if (filterSet.getFilter("email")!=null) {
            query.setEmail(filterSet.getFilter("email").getValue());
        }
        return getUserManager().findUsersByQuery(query);
    }

    private List getUsernamesForRoles( Collection roleNames )
    {
        Set usernames = new HashSet();

        try
        {
            List userAssignments = rbac.getUserAssignmentsForRoles( roleNames );

            if ( userAssignments != null )
            {
                for ( int i = 0; i < userAssignments.size(); i++ )
                {
                    usernames.add( ( (UserAssignment) userAssignments.get( i ) ).getPrincipal() );
                }
            }
        }
        catch ( RbacManagerException e )
        {
            getLogger().warn( "Unable to get user assignments for roles " + roleNames, e );
        }

        return new ArrayList( usernames );
    }

    private UserManager getUserManager()
    {
        return securitySystem.getUserManager();
    }

    // ------------------------------------------------------------------
    // Parameter Accessor Methods
    // ------------------------------------------------------------------

    public List getUsers()
    {
        return users;
    }

    public void setUsers( List users )
    {
        this.users = users;
    }

    public String getRoleName()
    {
        if ( StringUtils.isEmpty( roleName ) )
        {
            return "Any";
        }
        return roleName;
    }

    public void setRoleName( String roleName )
    {
        this.roleName = roleName;
    }

    public List getRoles()
    {
        return roles;
    }

    public Map getReportMap()
    {
        return reportManager.getReportMap();
    }
}
