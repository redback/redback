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
import java.util.Iterator;
import java.util.List;

import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.redback.rbac.Role;
import org.codehaus.plexus.redback.struts2.action.AbstractSecurityAction;
import org.codehaus.plexus.redback.struts2.interceptor.SecureActionBundle;
import org.codehaus.plexus.redback.struts2.interceptor.SecureActionException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.redback.integration.model.SimplePermission;
import org.codehaus.redback.integration.role.RoleConstants;

/**
 * RoleCreateAction
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action"
 * role-hint="redback-role-create"
 * instantiation-strategy="per-lookup"
 */
public class RoleCreateAction
    extends AbstractSecurityAction
{
    // ------------------------------------------------------------------
    // Plexus Component Requirements
    // ------------------------------------------------------------------

    /**
     * @plexus.requirement role-hint="cached"
     */
    private RBACManager manager;

    // ------------------------------------------------------------------
    // Action Parameters
    // ------------------------------------------------------------------

    private String principal;

    private String roleName;

    private String description;

    private List permissions;

    private List childRoles;

    private SimplePermission addpermission;

    private String submitMode;

    // ------------------------------------------------------------------
    // Action Entry Points - (aka Names)
    // ------------------------------------------------------------------

    public String show()
    {
        if ( permissions == null )
        {
            permissions = new ArrayList();
        }

        if ( childRoles == null )
        {
            childRoles = new ArrayList();
        }

        if ( addpermission == null )
        {
            addpermission = new SimplePermission();
        }

        return INPUT;
    }

    public String addpermission()
    {
        if ( addpermission == null )
        {
            addActionError( getText( "cannot.add.null.permission" ) );
            return ERROR;
        }

        if ( permissions == null )
        {
            permissions = new ArrayList();
        }

        permissions.add( addpermission );

        addpermission = new SimplePermission();

        return INPUT;
    }

    public String submit()
    {
        if ( StringUtils.equals( getSubmitMode(), "addPermission" ) )
        {
            return addpermission();
        }

        if ( StringUtils.isEmpty( roleName ) )
        {
            addActionError( getText( "cannot.add.empty.role" ) );
            return ERROR;
        }

        try
        {
            Role _role;
            if ( manager.roleExists( roleName ) )
            {
                _role = manager.getRole( roleName );
            }
            else
            {
                _role = manager.createRole( roleName );
            }

            _role.setDescription( description );
            _role.setChildRoleNames( childRoles );

            List _permissionList = new ArrayList();
            Iterator it = permissions.iterator();
            while ( it.hasNext() )
            {
                SimplePermission perm = (SimplePermission) it.next();
                _permissionList.add(
                    manager.createPermission( perm.getName(), perm.getOperationName(), perm.getResourceIdentifier() ) );
            }

            _role.setPermissions( _permissionList );

            manager.saveRole( _role );

            List list = new ArrayList();
            list.add( roleName );
            addActionMessage( getText( "save.role.success", list ) );
        }
        catch ( RbacManagerException e )
        {
            List list = new ArrayList();
            list.add( roleName );
            list.add( e.getMessage() );
            addActionError( getText( "cannot.get.role", list ) );
            return ERROR;
        }

        return SUCCESS;
    }

    // ------------------------------------------------------------------
    // Parameter Accessor Methods
    // ------------------------------------------------------------------

    public String getPrincipal()
    {
        return principal;
    }

    public void setPrincipal( String principal )
    {
        this.principal = principal;
    }

    public SimplePermission getAddpermission()
    {
        return addpermission;
    }

    public void setAddpermission( SimplePermission addpermission )
    {
        this.addpermission = addpermission;
    }

    public String getSubmitMode()
    {
        return submitMode;
    }

    public void setSubmitMode( String submitMode )
    {
        this.submitMode = submitMode;
    }

    public SecureActionBundle initSecureActionBundle()
        throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( RoleConstants.USER_MANAGEMENT_RBAC_ADMIN_OPERATION, Resource.GLOBAL );
        return bundle;
    }

}
