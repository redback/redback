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
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.redback.rbac.Permission;
import org.codehaus.plexus.redback.rbac.RBACManager;
import org.codehaus.plexus.redback.rbac.RbacManagerException;
import org.codehaus.plexus.redback.rbac.RbacObjectNotFoundException;
import org.codehaus.plexus.redback.rbac.Resource;
import org.codehaus.plexus.redback.rbac.Role;
import org.codehaus.plexus.redback.rbac.UserAssignment;
import org.codehaus.plexus.redback.rbac.jdo.JdoRole;
import org.codehaus.plexus.redback.role.RoleManager;
import org.codehaus.plexus.redback.role.model.ModelApplication;
import org.codehaus.plexus.redback.role.model.ModelRole;
import org.codehaus.plexus.redback.role.model.ModelTemplate;
import org.codehaus.plexus.redback.struts2.action.AbstractUserCredentialsAction;
import org.codehaus.plexus.redback.struts2.interceptor.SecureActionBundle;
import org.codehaus.plexus.redback.struts2.interceptor.SecureActionException;
import org.codehaus.plexus.redback.struts2.model.AdminEditUserCredentials;
import org.codehaus.plexus.redback.struts2.model.ApplicationRoleDetails;
import org.codehaus.plexus.redback.system.SecuritySession;
import org.codehaus.plexus.redback.system.SecuritySystemConstants;
import org.codehaus.plexus.redback.users.User;
import org.codehaus.plexus.redback.users.UserManager;
import org.codehaus.plexus.redback.users.UserNotFoundException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.redback.integration.role.RoleConstants;

/**
 * AssignmentsAction
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id$
 * @plexus.component role="com.opensymphony.xwork2.Action"
 * role-hint="redback-assignments"
 * instantiation-strategy="per-lookup"
 */
public class AssignmentsAction
    extends AbstractUserCredentialsAction
{
    // ------------------------------------------------------------------
    // Plexus Component Requirements
    // ------------------------------------------------------------------

    /**
     * @plexus.requirement role-hint="cached"
     */
    private RBACManager manager;

    /**
     * @plexus.requirement role-hint="default"
     */
    private RoleManager rmanager;

    // ------------------------------------------------------------------
    // Action Parameters
    // ------------------------------------------------------------------

    private String principal;

    private AdminEditUserCredentials user;

    /**
     * A List of {@link Role} objects.
     */
    private List assignedRoles;

    /**
     * A List of {@link Role} objects.
     */
    private List availableRoles;

    private List effectivelyAssignedRoles;

    /**
     * List of names (received from client) of dynamic roles to set/unset
     */
    private List addDSelectedRoles;

    /**
     * List of names (received from client) of nondynamic roles to set/unset
     */
    private List addNDSelectedRoles;

    private List nondynamicroles;

    private List dynamicroles;

    private List templates;

    private List NDRoles;

    private List DRoles;

    private List applicationRoleDetails = new ArrayList();

    // ------------------------------------------------------------------
    // Action Entry Points - (aka Names)
    // ------------------------------------------------------------------

    public List getApplicationRoleDetails()
    {
        return applicationRoleDetails;
    }

    public List getTemplates()
    {
        return templates;
    }

    
    public void setTemplates( List templates )
    {
        this.templates = templates;
    }
      
    /**
     * Display the edit user panel. <p/> This should consist of the Role details for the specified user. <p/> A table of
     * currently assigned roles. This table should have a column to remove the role from the user. This table should
     * also have a column of checkboxes that can be selected and then removed from the user. <p/> A table of roles that
     * can be assigned. This table should have a set of checkboxes that can be selected and then added to the user. <p/>
     * Duplicate role assignment needs to be taken care of.
     */
    public String show()
    {
        this.addNDSelectedRoles = new ArrayList();
        this.addDSelectedRoles = new ArrayList();
        
        if ( StringUtils.isEmpty( principal ) )
        {
            addActionError( getText( "rbac.edit.user.empty.principal" ) );
            return ERROR;
        }

        UserManager userManager = super.securitySystem.getUserManager();

        if ( !userManager.userExists( principal ) )
        {
            List list = new ArrayList();
            list.add( principal );
            addActionError( getText( "user.does.not.exist", list ) );
            return ERROR;
        }

        try
        {
            User u = userManager.findUser( principal );

            if ( u == null )
            {
                addActionError( getText( "cannot.operate.on.null.user" ) );
                return ERROR;
            }

            user = new AdminEditUserCredentials( u );
        }
        catch ( UserNotFoundException e )
        {
            List list = new ArrayList();
            list.add( principal );
            list.add( e.getMessage() );
            addActionError( getText( "user.not.found.exception", list ) );
            return ERROR;
        }

        try
        {
        for ( Iterator i = rmanager.getModel().getApplications().iterator(); i.hasNext(); )
        {
            ModelApplication application = (ModelApplication)i.next();

            ApplicationRoleDetails details = new ApplicationRoleDetails();

            details.setName( application.getId() );
            details.setDescription( application.getDescription() );
            details.setAllAssignedRoles( new ArrayList( manager.getAssignedRoles( principal ) ) );
            details.setEffectivelyAssignedRoles( new ArrayList( manager.getEffectivelyAssignedRoles( principal ) )  );
            details.setApplicationRoles( application.getRoles() );
            details.setApplicationTemplates( application.getTemplates() );
            details.setRoles( manager.getAllRoles() );

            applicationRoleDetails.add( details );
        }
        }
        catch ( RbacObjectNotFoundException re )
        {
           re.printStackTrace();
        }
        catch ( RbacManagerException rme )
        {
            rme.printStackTrace();
        }

        return SUCCESS;
    }

    /**
     * Display the edit user panel.
     * 
     * @return
     */
    public String edituser()
    {
        getLogger().info( "in edit user now" );
        
        try 
        { 
              UserAssignment assignment;
              
              if ( manager.userAssignmentExists( principal ) )
              { 
                  assignment = manager.getUserAssignment( principal ); 
              }
              else 
              { 
                  assignment = manager.createUserAssignment( principal );
              }
                
              List roles = new ArrayList();
              
              assignment.setRoleNames( roles );

            List allAssignedRoles = null;
            List allRoles = manager.getAllRoles();
            List applicationRoles = new ArrayList();
            List resourceRoles = new ArrayList();

            try
            {
                allAssignedRoles = new ArrayList( manager.getAssignedRoles( principal ) );

                for ( Iterator i = rmanager.getModel().getApplications().iterator(); i.hasNext(); )
                {
                    ModelApplication application = (ModelApplication) i.next();

                    applicationRoles.addAll( application.getRoles() );
                    resourceRoles.addAll( getResourceRoles( application.getTemplates(), allRoles ) );
                }
            }
            catch ( RbacObjectNotFoundException re )
            {

            }
            catch ( RbacManagerException rme )
            {

            }

            if ( allAssignedRoles != null )
            {
                for ( Iterator j = allAssignedRoles.iterator(); j.hasNext(); )
                {
                    Role assignedRole = (Role) j.next();

                    boolean found = checkRoleName( assignedRole.getName(), applicationRoles );

                    if ( !found )
                    {
                        found = checkRoleName( assignedRole.getName(), resourceRoles );
                    }

                    if ( !found )
                    {
                        assignment.addRoleName( assignedRole.getName() );
                    }
                }
            }
              
              if ( addNDSelectedRoles != null )
              {
                for ( Iterator i = addNDSelectedRoles.iterator(); i.hasNext(); )
                {
                  String r = ( String ) i.next();
                  getLogger().info( "-------- adding ND Role: " +  r );
                  assignment.addRoleName( r );
                }
              }
              if ( addDSelectedRoles != null )
              {
                for ( Iterator i = addDSelectedRoles.iterator(); i.hasNext(); )
                {
                  assignment.addRoleName( (String) i.next() );
                }
              }
              assignment = manager.saveUserAssignment( assignment );
              
              getLogger().info( "roles assigned = " + assignment.getRoleNames().size() );
                
        }
        catch ( RbacManagerException ne ) 
        {
              List list = new ArrayList(); 
              list.add( ne.getMessage() ); 
              addActionError( getText("error.removing.selected.roles", list ) ); 
              return ERROR; 
        }
        return SUCCESS;
    }

    private Set getResourceRoles( List applicationTemplates, List roles )
    {
        Set resources = new HashSet();

        for ( Iterator i = applicationTemplates.iterator(); i.hasNext(); )
        {
            ModelTemplate template = (ModelTemplate)i.next();

            for ( Iterator j = roles.iterator(); j.hasNext(); )
            {
                JdoRole role = (JdoRole) j.next();

                if ( role.getName().startsWith( template.getNamePrefix() ) )
                {
                    resources.add( role );
                }
            }
        }

        return resources;
    }

    private boolean checkRoleName( String roleName, List roles )
    {
        boolean found = false;

        for ( Iterator i = roles.iterator(); i.hasNext() && !found; )
        {
            Object obj = i.next();

            if ( obj instanceof ModelRole )
            {
                ModelRole role = (ModelRole) obj;

                if ( role.getName().equals( roleName ) )
                {
                    found = true;
                }
            }
            else if ( obj instanceof JdoRole )
            {
                JdoRole role = (JdoRole) obj;

                if ( role.getName().equals( roleName ) )
                {
                    found = true;
                }
            }
        }

        return found;
    }

    /**
     * this is a hack.
     * 
     * this is a hack around the requirements of putting RBAC constraits into the model.
     * 
     * this adds one very major restriction to this security system, that a role name must contain the identifiers of
     * the resource that is being constrained for adding and granting of roles, this is unacceptable in the long term
     * and we need to get the model refactored to include this RBAC concept
     * 
     * 
     * @param roleList
     * @return
     * @throws RbacManagerException
     */
    private List filterRolesForCurrentUserAccess( List roleList ) throws RbacManagerException
    {
        String currentUser =
            ( (SecuritySession) session.get( SecuritySystemConstants.SECURITY_SESSION_KEY ) ).getUser().getPrincipal().toString();

        List filteredRoleList = new ArrayList();

        Map assignedPermissionMap = manager.getAssignedPermissionMap( currentUser );
        List resourceGrants = new ArrayList();

        if ( assignedPermissionMap.containsKey( RoleConstants.USER_MANAGEMENT_ROLE_GRANT_OPERATION ) )
        {
            List roleGrantPermissions =
                (List) assignedPermissionMap.get( RoleConstants.USER_MANAGEMENT_ROLE_GRANT_OPERATION );

            for ( Iterator i = roleGrantPermissions.iterator(); i.hasNext(); )
            {
                Permission permission = (Permission) i.next();

                if ( permission.getResource().getIdentifier().equals( Resource.GLOBAL ) )
                {
                    // the current user has the rights to assign any given role
                    return roleList;
                }
                else
                {
                    resourceGrants.add( permission.getResource().getIdentifier() );
                }
            }
        }
        else
        {
            return Collections.EMPTY_LIST;
        }

        // we should have a list of resourceGrants now, this will provide us with the information necessary to restrict
        // the role list
        for ( Iterator i = roleList.iterator(); i.hasNext(); )
        {
            Role role = (Role) i.next();

            for ( Iterator j = resourceGrants.iterator(); j.hasNext(); )
            {
                String resourceIdentifier = (String) j.next();

                if ( role.getName().indexOf( resourceIdentifier ) != -1 )
                {
                    filteredRoleList.add( role );
                }
            }
        }

        return filteredRoleList;
    }
    
    // ------------------------------------------------------------------
    // Parameter Accessor Methods
    // ------------------------------------------------------------------

    public List getAssignedRoles()
    {
        return assignedRoles;
    }

    public void setAssignedRoles( List assignedRoles )
    {
        this.assignedRoles = assignedRoles;
    }

    public List getAvailableRoles()
    {
        return availableRoles;
    }

    public void setAvailableRoles( List availableRoles )
    {
        this.availableRoles = availableRoles;
    }

    public List getEffectivelyAssignedRoles()
    {
        return effectivelyAssignedRoles;
    }

    public void setEffectivelyAssignedRoles( List effectivelyAssignedRoles )
    {
        this.effectivelyAssignedRoles = effectivelyAssignedRoles;
    }

    public String getPrincipal()
    {
        return principal;
    }

    public void setPrincipal( String principal )
    {
        this.principal = principal;
    }

    public void setUsername( String username )
    {
        this.principal = username;
    }

    public AdminEditUserCredentials getUser()
    {
        return user;
    }

    public SecureActionBundle initSecureActionBundle() throws SecureActionException
    {
        SecureActionBundle bundle = new SecureActionBundle();
        bundle.setRequiresAuthentication( true );
        bundle.addRequiredAuthorization( RoleConstants.USER_MANAGEMENT_USER_EDIT_OPERATION, Resource.GLOBAL );
        bundle.addRequiredAuthorization( RoleConstants.USER_MANAGEMENT_RBAC_ADMIN_OPERATION, Resource.GLOBAL );
        bundle.addRequiredAuthorization( RoleConstants.USER_MANAGEMENT_ROLE_GRANT_OPERATION, Resource.GLOBAL );
        bundle.addRequiredAuthorization( RoleConstants.USER_MANAGEMENT_ROLE_DROP_OPERATION, Resource.GLOBAL );
        bundle.addRequiredAuthorization( RoleConstants.USER_MANAGEMENT_USER_ROLE_OPERATION, Resource.GLOBAL );

        return bundle;
    }

    public List getNondynamicroles()
    {
        return nondynamicroles;
    }

    public void setNondynamicroles( List nondynamicroles )
    {
        this.nondynamicroles = nondynamicroles;
    }

    public List getDynamicroles()
    {
        return dynamicroles;
    }

    public void setDynamicroles( List dynamicroles )
    {
        this.dynamicroles = dynamicroles;
    }

    public List getNDRoles()
    {
        return NDRoles;
    }

    public void setNDRoles( List roles )
    {
        NDRoles = roles;
    }

    public List getDRoles()
    {
        return DRoles;
    }

    public void setDRoles( List roles )
    {
        DRoles = roles;
    }

    public List getAddDSelectedRoles()
    {
        return addDSelectedRoles;
    }

    public void setAddDSelectedRoles( List addDSelectedRoles )
    {
        this.addDSelectedRoles = addDSelectedRoles;
    }

    public List getAddNDSelectedRoles()
    {
        return addNDSelectedRoles;
    }

    public void setAddNDSelectedRoles( List addNDSelectedRoles )
    {
        this.addNDSelectedRoles = addNDSelectedRoles;
    }
}
