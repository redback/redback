package org.codehaus.plexus.redback.struts2.model;

import org.codehaus.plexus.redback.rbac.Role;
import org.codehaus.plexus.redback.role.model.ModelRole;
import org.codehaus.plexus.redback.role.model.ModelTemplate;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jesse
 * Date: Nov 11, 2007
 * Time: 10:49:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationRoleDetails
{
    private String name;

    private String description;

    // to be filled with resources garnered from created applicationTemplates
    private Set resources = new HashSet();

    // ModelTemplate
    private List applicationTemplates;

    // ModelRole
    private List applicationRoles;

    // all created roles in the system
    private List roles;

    private List effectivelyAssignedRoles;

    private List allAssignedRoles;

    private List assignedRoles = new ArrayList();

    private List availableRoles = new ArrayList();

    private List tableHeader;

    private List table;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set getResources() {
        return resources;
    }

    public List getApplicationTemplates() {
        return applicationTemplates;
    }

    public void setApplicationTemplates(List applicationTemplates) {
        this.applicationTemplates = applicationTemplates;
    }

    public List getRoles() {
        return roles;
    }

    public void setRoles(List roles) {
        this.roles = roles;
    }

    public List getAssignedRoles() {
        process();
        return assignedRoles;
    }

    public void setAssignedRoles(List assignedRoles) {
        this.assignedRoles = assignedRoles;
    }

    public List getEffectivelyAssignedRoles() {
        return effectivelyAssignedRoles;
    }

    public void setEffectivelyAssignedRoles(List effectivelyAssignedRoles) {
        this.effectivelyAssignedRoles = effectivelyAssignedRoles;
    }

    public List getAllAssignedRoles() {
        return allAssignedRoles;
    }

    public void setAllAssignedRoles(List allAssignedRoles) {
        this.allAssignedRoles = allAssignedRoles;
    }

    public List getAvailableRoles() {
        process();
        return availableRoles;
    }

    public void setAvailableRoles(List availableRoles) {
        this.availableRoles = availableRoles;
    }

    public List getApplicationRoles() {
        return applicationRoles;
    }

    public void setApplicationRoles(List applicationRoles) {
        this.applicationRoles = applicationRoles;
    }

    public List getTableHeader()
    {
        process();

        return tableHeader;
    }

    public List getTable()
    {
        process();

        return table;
    }


    private boolean prepared;

    private void process()
    {
        if ( !prepared )
        {
            gatherResources();

            computeRoles();

            computeTable();

            prepared = true;
        }
    }

    private void computeRoles()
    {
        for ( Iterator i = applicationRoles.iterator(); i.hasNext(); )
        {            
            ModelRole role = (ModelRole)i.next();

            if ( isAssigned( role.getName() ) )
            {
                if ( role.isAssignable() )
                {
                    assignedRoles.add( role.getName() );
                }
            }
            else if ( isEffectivelyAssigned( role.getName() ) )
            {
                // nothing
            }
            else
            {
                if ( role.isAssignable() )
                {
                    availableRoles.add( role.getName() );                    
                }
            }
        }
        
        Collections.sort( assignedRoles, String.CASE_INSENSITIVE_ORDER );
        Collections.sort( availableRoles, String.CASE_INSENSITIVE_ORDER );
    }

    private void gatherResources()
    {
        for ( Iterator i = applicationTemplates.iterator(); i.hasNext(); )
        {
            ModelTemplate template = (ModelTemplate)i.next();

            for ( Iterator k = roles.iterator(); k.hasNext(); )
            {
                Role role = (Role)k.next();

                if ( role.getName().startsWith( template.getNamePrefix() ) )
                {
                    resources.add( role.getName().substring( role.getName().indexOf( template.getDelimiter() ) + template.getDelimiter().length() ) );
                }
            }
        }
    }

    private void computeTable()
    {
        table = new LinkedList();

        String delimiter;

        tableHeader = new LinkedList();
        // the top row is the list of applicationTemplates, empty in the first column
        //tableHeader.add("");

        for ( Iterator i = applicationTemplates.iterator(); i.hasNext(); )
        {
            ModelTemplate template = (ModelTemplate)i.next();
            tableHeader.add( template );
        }

        List<String> resourcesList = new ArrayList<String>( resources );
        Collections.sort( resourcesList, String.CASE_INSENSITIVE_ORDER );
        
        for ( Iterator k = resourcesList.iterator(); k.hasNext(); )
        {
            String resource = (String)k.next();
            LinkedList tableRow = new LinkedList();

            RoleTableCell resourceCell = new RoleTableCell();
            resourceCell.setName( resource );
            resourceCell.setLabel( true );
            tableRow.add( resourceCell );


            for ( Iterator i = tableHeader.iterator(); i.hasNext(); )
            {
                ModelTemplate template = (ModelTemplate)i.next();

                RoleTableCell cell = new RoleTableCell();

                cell.setName( template.getNamePrefix() + template.getDelimiter() + resource );
                cell.setEffectivelyAssigned( isEffectivelyAssigned( cell.getName() ) );
                cell.setAssigned( isAssigned( cell.getName() ) );
                cell.setLabel( false );

                tableRow.add( cell );
            }

            table.add( tableRow );
        }
    }


    private boolean isEffectivelyAssigned( String roleName )
    {
        for ( Iterator i = effectivelyAssignedRoles.iterator(); i.hasNext(); )
        {
            Role effectiveRole = (Role)i.next();
            {
                if ( roleName.equals( effectiveRole.getName() ) )
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isAssigned( String roleName )
    {
        for ( Iterator i = allAssignedRoles.iterator(); i.hasNext(); )
        {
            Role assignedRole = (Role)i.next();

            if ( roleName.equals( assignedRole.getName() ) )
            {
                return true;
            }
        }
        return false;
    }


    public class RoleTableCell
    {
        private String name;

        private boolean effectivelyAssigned;

        private boolean assigned;

        private boolean label;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isEffectivelyAssigned() {
            return effectivelyAssigned;
        }

        public void setEffectivelyAssigned(boolean effectivelyAssigned) {
            this.effectivelyAssigned = effectivelyAssigned;
        }

        public boolean isAssigned() {
            return assigned;
        }

        public void setAssigned(boolean assigned) {
            this.assigned = assigned;
        }

        public boolean isLabel() {
            return label;
        }

        public void setLabel(boolean label) {
            this.label = label;
        }
    }
}
