package org.codehaus.redback.xmlrpc.client;

/*
 * Copyright 2009 The Codehaus.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.codehaus.redback.xmlrpc.bean.Operation;
import org.codehaus.redback.xmlrpc.bean.Permission;
import org.codehaus.redback.xmlrpc.bean.Resource;
import org.codehaus.redback.xmlrpc.bean.Role;
import org.codehaus.redback.xmlrpc.bean.User;
import org.codehaus.redback.xmlrpc.service.OperationService;
import org.codehaus.redback.xmlrpc.service.PermissionService;
import org.codehaus.redback.xmlrpc.service.ResourceService;
import org.codehaus.redback.xmlrpc.service.RoleService;
import org.codehaus.redback.xmlrpc.service.UserService;
import org.codehaus.redback.xmlrpc.util.BeanConverterUtil;

public class CommandLineClient
{
    private static final String COMMAND_BLANK = "blank";

    private static final String COMMAND_EXIT = "exit";

    private static final String COMMAND_BIND = "bind";

    private static final String COMMAND_PING = "ping";

    private static final String COMMAND_HELP = "help";

    private static final String COMMAND_USER_CREATE = "createuser";

    private static final String COMMAND_USER_GETALL = "getusers";

    private static final String COMMAND_USER_GET = "getuser";

    private static final String COMMAND_USER_DELETE = "deleteuser";

    private static final String COMMAND_USER_UPDATE = "updateuser";

    private static final String COMMAND_ROLE_CREATE = "createrole";

    private static final String COMMAND_ROLE_DELETE = "deleterole";

    private static final String COMMAND_ROLE_GET = "getrole";

    private static final String COMMAND_ROLE_GETALL = "getroles";

    private static final String COMMAND_ROLE_ADD_CHILDROLE = "addchildrole";

    private static final String COMMAND_ROLE_GET_CHILDROLES = "getchildroles";

    private static final String COMMAND_ROLE_GET_ROLEPERMISSIONS = "getrolepermissions";

    private static final String COMMAND_PERMISSION_CREATE = "createpermission";

    private static final String COMMAND_PERMISSION_DELETE = "deletepermission";

    private static final String COMMAND_PERMISSION_GET = "getpermission";

    private static final String COMMAND_PERMISSION_GETALL = "getpermissions";

    private static final String COMMAND_OPERATION_CREATE = "createoperation";

    private static final String COMMAND_OPERATION_DELETE = "deleteoperation";

    private static final String COMMAND_OPERATION_GET = "getoperation";

    private static final String COMMAND_OPERATION_GETALL = "getoperations";

    private static final String COMMAND_RESOURCE_CREATE = "createresource";

    private static final String COMMAND_RESOURCE_DELETE = "deleteresource";

    private static final String COMMAND_RESOURCE_GET = "getresource";

    private static final String COMMAND_RESOURCE_GETALL = "getresources";

    private static List<String> COMMANDS = initCommands();

    private static List<String> initCommands()
    {
        List<String> commands = new ArrayList<String>();
        commands.add( COMMAND_BLANK );
        commands.add( COMMAND_EXIT );
        commands.add( COMMAND_BIND );
        commands.add( COMMAND_PING );
        commands.add( COMMAND_HELP );

        commands.add( COMMAND_USER_CREATE );
        commands.add( COMMAND_USER_UPDATE );
        commands.add( COMMAND_USER_DELETE );
        commands.add( COMMAND_USER_GET );
        commands.add( COMMAND_USER_GETALL );

        commands.add( COMMAND_ROLE_CREATE );
        commands.add( COMMAND_ROLE_DELETE );
        commands.add( COMMAND_ROLE_GET );
        commands.add( COMMAND_ROLE_GETALL );
        commands.add( COMMAND_ROLE_ADD_CHILDROLE );
        commands.add( COMMAND_ROLE_GET_CHILDROLES );
        commands.add( COMMAND_ROLE_GET_ROLEPERMISSIONS );

        commands.add( COMMAND_PERMISSION_CREATE );
        commands.add( COMMAND_PERMISSION_DELETE );
        commands.add( COMMAND_PERMISSION_GET );
        commands.add( COMMAND_PERMISSION_GETALL );

        commands.add( COMMAND_OPERATION_CREATE );
        commands.add( COMMAND_OPERATION_DELETE );
        commands.add( COMMAND_OPERATION_GET );
        commands.add( COMMAND_OPERATION_GETALL );

        commands.add( COMMAND_RESOURCE_CREATE );
        commands.add( COMMAND_RESOURCE_DELETE );
        commands.add( COMMAND_RESOURCE_GET );
        commands.add( COMMAND_RESOURCE_GETALL );

        return commands;
    }

    private String bindUrl;

    private String bindUsername;

    private String bindPassword;

    private boolean isBound;

    private ServiceClient userService = new UserServiceClient();

    private ServiceClient roleService = new RoleServiceClient();

    private ServiceClient permissionService = new PermissionServiceClient();

    private ServiceClient operationService = new OperationServiceClient();

    private ServiceClient resourceService = new ResourceServiceClient();

    private List<ServiceClient> services;

    public CommandLineClient()
    {
        init();
    }

    public void init()
    {
        this.bindUrl = "";
        this.bindUsername = "";
        this.bindPassword = "";
        this.isBound = false;

        userService = new UserServiceClient();
        roleService = new RoleServiceClient();

        this.services = new ArrayList<ServiceClient>();
        this.services.add( userService );
        this.services.add( roleService );
        this.services.add( permissionService );
        this.services.add( operationService );
        this.services.add( resourceService );
    }

    public void parseArgs( String[] args )
    {
        String ARG_PREFIX = "--";
        String key = "";
        String value = "";

        for ( String arg : args )
        {
            if ( arg.startsWith( ARG_PREFIX ) )
            {
                key = arg.substring( ARG_PREFIX.length(), arg.indexOf( '=' ) );
                value = arg.substring( arg.indexOf( '=' ) + 1 );
            }

            if ( key != null && value != null )
            {
                if ( key.equals( "url" ) )
                {
                    bindUrl = value.trim();
                }

                if ( key.equals( "username" ) )
                {
                    bindUsername = value.trim();
                }

                if ( key.equals( "password" ) )
                {
                    bindPassword = value.trim();
                }
            }
        }
    }

    public void bind()
    {
        if ( bindUrl.trim().equals( "" ) )
        {
            log( "Usage: \"> bind url [username [password]]\"" );
        }
        else
        {
            log( "Url: " + bindUrl );
            log( "Username: " + bindUsername );
            log( "Password: " + bindPassword );

            try
            {
                for ( ServiceClient service : services )
                {
                    service.bind( bindUrl, bindUsername, bindPassword );
                }
                isBound = true;
                log( "Bind ok." );
            }
            catch ( Exception e )
            {
                isBound = false;
                log( "Bind failed." );
                e.printStackTrace();
            }
        }
    }

    private Map<String, List<String>> processInputLine( String inputLine )
    {
        Map<String, List<String>> command = new HashMap<String, List<String>>();

        if ( inputLine.trim().equals( "" ) )
        {
            command.put( COMMAND_BLANK, new ArrayList<String>() );
        }
        else
        {
            StringTokenizer st = new StringTokenizer( inputLine );
            String commandKey = st.nextToken().trim();
            List<String> parameters = new ArrayList<String>();
            while ( st.hasMoreTokens() )
            {
                parameters.add( st.nextToken() );
            }

            command.put( commandKey, parameters );
        }

        return command;
    }

    private void displayPrompt()
    {
        if ( isBound )
        {
            System.out.print( "[" + ( !bindUsername.equals( "" ) ? bindUsername + "@" : "" ) + bindUrl + "]> " );
        }
        else
        {
            System.out.print( "[not-bound]> " );
        }
    }

    private void log( String text )
    {
        System.out.println( text );
    }

    public void run()
        throws Exception
    {
        BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
        do
        {
            displayPrompt();
            String inputLine = in.readLine();
            Map<String, List<String>> command = processInputLine( inputLine );

            if ( command.containsKey( COMMAND_BLANK ) )
            {
            }

            else if ( command.containsKey( COMMAND_BIND ) )
            {
                init();

                if ( command.get( COMMAND_BIND ).size() > 0 )
                {
                    bindUrl = command.get( COMMAND_BIND ).get( 0 );

                    if ( command.get( COMMAND_BIND ).size() > 1 )
                    {
                        bindUsername = command.get( COMMAND_BIND ).get( 1 );

                        if ( command.get( COMMAND_BIND ).size() > 2 )
                        {
                            bindPassword = command.get( COMMAND_BIND ).get( 2 );
                        }
                    }
                }

                bind();
            }

            else if ( command.containsKey( COMMAND_PING ) )
            {
                if ( isBound )
                {
                    for ( ServiceClient service : services )
                    {
                        log( "Ping..." + ( service.ping() ? "ok." : "failed!" ) );
                    }
                }
                else
                {
                    log( "Not bound to service yet." );
                }
            }

            else if ( command.containsKey( COMMAND_HELP ) )
            {
                String commandList = "";

                for ( String commandName : COMMANDS )
                {
                    commandList += commandName + ", ";
                }
                log( "Available commands:" );
                log( commandList );
            }

            else if ( command.containsKey( COMMAND_EXIT ) )
            {
                log( "Exit." );
                break;
            }

            else if ( command.containsKey( COMMAND_USER_CREATE ) )
            {
                String username = "";
                String fullname = "";
                String email = "";

                if ( command.get( COMMAND_USER_CREATE ).size() > 0 )
                {
                    username = command.get( COMMAND_USER_CREATE ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Username: " );
                    username = in.readLine();
                }

                System.out.print( "Full Name: " );
                fullname = in.readLine();

                System.out.print( "Email: " );
                email = in.readLine();

                ( (UserService) userService ).createUser( username, fullname, email );

                log( "Successfully created user '" + username + "'" );
            }

            else if ( command.containsKey( COMMAND_USER_UPDATE ) )
            {
                String username = "";
                String fullname = "";
                String email = "";
                String isValidated = "";
                String isLocked = "";

                if ( command.get( COMMAND_USER_UPDATE ).size() > 0 )
                {
                    username = command.get( COMMAND_USER_UPDATE ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Username: " );
                    username = in.readLine();
                }

                System.out.print( "Full Name: " );
                fullname = in.readLine();

                System.out.print( "Email: " );
                email = in.readLine();

                System.out.print( "Validated (y/n)? " );
                isValidated = in.readLine();

                System.out.print( "Locked (y/n)? " );
                isLocked = in.readLine();

                User user = new User( username, fullname, email, ( isValidated.equals( "y" ) ? true : false ),
                                      ( isLocked.equals( "y" ) ? true : false ) );
                ( (UserService) userService ).updateUser( BeanConverterUtil.toMap( user ) );

                log( "User('" + user.getUsername() + "', '" + user.getFullname() + "', '" + user.getEmail() + "', "
                    + ( user.isValidated() ? "VALIDATED" : "NOT VALIDATED" ) + ", "
                    + ( user.isLocked() ? "LOCKED" : "NOT LOCKED" ) + ")" );
            }

            else if ( command.containsKey( COMMAND_USER_DELETE ) )
            {
                String username = "";

                if ( command.get( COMMAND_USER_DELETE ).size() > 0 )
                {
                    username = command.get( COMMAND_USER_DELETE ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Username: " );
                    username = in.readLine();
                }

                ( (UserService) userService ).deleteUser( username );

                log( "Successfully deleted '" + username + "'" );
            }

            else if ( command.containsKey( COMMAND_USER_GET ) )
            {
                String username = "";

                if ( command.get( COMMAND_USER_GET ).size() > 0 )
                {
                    username = command.get( COMMAND_USER_GET ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Username: " );
                    username = in.readLine();
                }

                User user = ( (UserService) userService ).getUser( username );

                log( "User('" + user.getUsername() + "', '" + user.getFullname() + "', '" + user.getEmail() + "', "
                    + ( user.isValidated() ? "VALIDATED" : "NOT VALIDATED" ) + ", "
                    + ( user.isLocked() ? "LOCKED" : "NOT LOCKED" ) + ")" );
            }

            else if ( command.containsKey( COMMAND_USER_GETALL ) )
            {
                List<User> users = ( (UserService) userService ).getUsers();

                for ( User user : users )
                {
                    log( "User('" + user.getUsername() + "', '" + user.getFullname() + "', '" + user.getEmail() + "', "
                        + ( user.isValidated() ? "VALIDATED" : "NOT VALIDATED" ) + ", "
                        + ( user.isLocked() ? "LOCKED" : "NOT LOCKED" ) + ")" );
                }

                log( "Found " + users.size() + " user" + ( users.size() > 1 ? "s" : "" ) + "." );
            }

            else if ( command.containsKey( COMMAND_ROLE_CREATE ) )
            {
                String rolename = "";

                if ( command.get( COMMAND_ROLE_CREATE ).size() > 0 )
                {
                    rolename = command.get( COMMAND_ROLE_CREATE ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Role Name: " );
                    rolename = in.readLine();
                }

                ( (RoleService) roleService ).createRole( rolename );

                log( "Successfully created role '" + rolename + "'" );
            }

            else if ( command.containsKey( COMMAND_ROLE_DELETE ) )
            {
                String rolename = "";

                if ( command.get( COMMAND_ROLE_DELETE ).size() > 0 )
                {
                    rolename = command.get( COMMAND_ROLE_DELETE ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Role Name: " );
                    rolename = in.readLine();
                }

                ( (RoleService) roleService ).removeRole( rolename );

                log( "Successfully deleted role '" + rolename + "'" );
            }

            else if ( command.containsKey( COMMAND_ROLE_GET ) )
            {
                String rolename = "";

                if ( command.get( COMMAND_ROLE_GET ).size() > 0 )
                {
                    rolename = command.get( COMMAND_ROLE_GET ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Role Name: " );
                    rolename = in.readLine();
                }

                Role role = ( (RoleService) roleService ).getRole( rolename );

                log( "Role('" + role.getRoleName() + "', '" + role.getDescription() + "', "
                    + ( role.isAssignable() ? "ASSIGNABLE" : "NOT ASSIGNABLE" ) + ", "
                    + ( role.isPermanent() ? "PERMANENT" : "NOT PERMANENT" ) + ")" );
            }

            else if ( command.containsKey( COMMAND_ROLE_GETALL ) )
            {
                List<Role> roles = ( (RoleService) roleService ).getRoles();

                for ( Role role : roles )
                {
                    log( "Role('" + role.getRoleName() + "', '" + role.getDescription() + "', "
                        + ( role.isAssignable() ? "ASSIGNABLE" : "NOT ASSIGNABLE" ) + ", "
                        + ( role.isPermanent() ? "PERMANENT" : "NOT PERMANENT" ) + ")" );
                }

                log( "Found " + roles.size() + " role" + ( roles.size() > 1 ? "s" : "" ) + "." );
            }

            else if ( command.containsKey( COMMAND_ROLE_ADD_CHILDROLE ) )
            {
                String roleName = "";
                String childRoleName = "";

                if ( command.get( COMMAND_ROLE_ADD_CHILDROLE ).size() > 0 )
                {
                    roleName = command.get( COMMAND_ROLE_ADD_CHILDROLE ).get( 0 ).trim();

                    if ( command.get( COMMAND_ROLE_ADD_CHILDROLE ).size() > 1 )
                    {
                        childRoleName = command.get( COMMAND_ROLE_ADD_CHILDROLE ).get( 1 ).trim();
                    }
                    else
                    {
                        System.out.print( "Child Role Name: " );
                        childRoleName = in.readLine();
                    }
                }
                else
                {
                    System.out.print( "Role Name: " );
                    roleName = in.readLine();

                    System.out.print( "Child Role Name: " );
                    childRoleName = in.readLine();
                }

                ( (RoleService) roleService ).addChildRole( roleName, childRoleName );

                log( "Successfully added child role '" + childRoleName + "' to role '" + roleName + "'" );
            }

            else if ( command.containsKey( COMMAND_ROLE_GET_CHILDROLES ) )
            {
                String rolename = "";

                if ( command.get( COMMAND_ROLE_GET_CHILDROLES ).size() > 0 )
                {
                    rolename = command.get( COMMAND_ROLE_GET_CHILDROLES ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Role Name: " );
                    rolename = in.readLine();
                }

                List<String> childRoleNames = ( (RoleService) roleService ).getChildRoles( rolename );
                String childRoleNamesString = "";

                for ( String childRoleName : childRoleNames )
                {
                    childRoleNamesString += childRoleName + ", ";
                }

                log( "Role: " + rolename );
                log( "Child Roles=[" + childRoleNamesString + "]" );
                log( "Found " + childRoleNames.size() + " role" + ( childRoleNames.size() > 1 ? "s" : "" ) + "." );
            }

            else if ( command.containsKey( COMMAND_ROLE_GET_ROLEPERMISSIONS ) )
            {
                String rolename = "";

                if ( command.get( COMMAND_ROLE_GET_ROLEPERMISSIONS ).size() > 0 )
                {
                    rolename = command.get( COMMAND_ROLE_GET_ROLEPERMISSIONS ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Role Name: " );
                    rolename = in.readLine();
                }

                List<String> permissionNames = ( (RoleService) roleService ).getChildRoles( rolename );
                String permissionNamesString = "";

                for ( String permissionName : permissionNames )
                {
                    permissionNamesString += permissionName + ", ";
                }

                log( "Role: " + rolename );
                log( "Permissions=[" + permissionNamesString + "]" );
                log( "Found " + permissionNames.size() + " role" + ( permissionNames.size() > 1 ? "s" : "" ) + "." );
            }

            else if ( command.containsKey( COMMAND_PERMISSION_CREATE ) )
            {
                String permissionName = "";
                String operationName = "";
                String resourceId = "";

                if ( command.get( COMMAND_PERMISSION_CREATE ).size() > 0 )
                {
                    permissionName = command.get( COMMAND_PERMISSION_CREATE ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Permission Name: " );
                    permissionName = in.readLine();
                }

                System.out.print( "Operation Name: " );
                operationName = in.readLine();

                System.out.print( "Resource Id: " );
                resourceId = in.readLine();

                ( (PermissionService) permissionService ).createPermission( permissionName, operationName, resourceId );

                log( "Successfully created permission '" + permissionName + "'" );
            }

            else if ( command.containsKey( COMMAND_PERMISSION_DELETE ) )
            {
                String permissionName = "";

                if ( command.get( COMMAND_PERMISSION_DELETE ).size() > 0 )
                {
                    permissionName = command.get( COMMAND_PERMISSION_DELETE ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Permission Name: " );
                    permissionName = in.readLine();
                }

                ( (PermissionService) permissionService ).removePermission( permissionName );

                log( "Successfully deleted permission '" + permissionName + "'" );
            }

            else if ( command.containsKey( COMMAND_PERMISSION_GET ) )
            {
                String permissionName = "";

                if ( command.get( COMMAND_PERMISSION_GET ).size() > 0 )
                {
                    permissionName = command.get( COMMAND_PERMISSION_GET ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Permission Name: " );
                    permissionName = in.readLine();
                }

                Permission permission = ( (PermissionService) permissionService ).getPermission( permissionName );

                log( "Permission('" + permission.getName() + "', '" + permission.getDescription() + "', '"
                    + permission.getOperation() + "', '" + permission.getResource() + "')" );
            }

            else if ( command.containsKey( COMMAND_PERMISSION_GETALL ) )
            {
                List<Permission> permissions = ( (PermissionService) permissionService ).getPermissions();

                for ( Permission permission : permissions )
                {
                    log( "Permission('" + permission.getName() + "', '" + permission.getDescription() + "', '"
                        + permission.getOperation() + "', '" + permission.getResource() + "')" );
                }

                log( "Found " + permissions.size() + " permission" + ( permissions.size() > 1 ? "s" : "" ) + "." );
            }

            else if ( command.containsKey( COMMAND_OPERATION_CREATE ) )
            {
                String operationName = "";

                if ( command.get( COMMAND_OPERATION_CREATE ).size() > 0 )
                {
                    operationName = command.get( COMMAND_OPERATION_CREATE ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Operation Name: " );
                    operationName = in.readLine();
                }

                ( (OperationService) operationService ).createOperation( operationName );

                log( "Successfully created operation '" + operationName + "'" );
            }

            else if ( command.containsKey( COMMAND_OPERATION_DELETE ) )
            {
                String operationName = "";

                if ( command.get( COMMAND_OPERATION_DELETE ).size() > 0 )
                {
                    operationName = command.get( COMMAND_OPERATION_DELETE ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Operation Name: " );
                    operationName = in.readLine();
                }

                ( (OperationService) operationService ).removeOperation( operationName );

                log( "Successfully deleted operation '" + operationName + "'" );
            }

            else if ( command.containsKey( COMMAND_OPERATION_GET ) )
            {
                String operationName = "";

                if ( command.get( COMMAND_OPERATION_GET ).size() > 0 )
                {
                    operationName = command.get( COMMAND_OPERATION_GET ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Operation Name: " );
                    operationName = in.readLine();
                }

                Operation operation = ( (OperationService) operationService ).getOperation( operationName );

                log( "Operation('" + operation.getName() + "', '" + operation.getDescription() + "', "
                    + ( operation.isPermanent() ? "PERMANENT" : "NOT PERMANENT" ) + ")" );
            }

            else if ( command.containsKey( COMMAND_OPERATION_GETALL ) )
            {
                List<Operation> operations = ( (OperationService) operationService ).getOperations();

                for ( Operation operation : operations )
                {
                    log( "Operation('" + operation.getName() + "', '" + operation.getDescription() + "', "
                        + ( operation.isPermanent() ? "PERMANENT" : "NOT PERMANENT" ) + ")" );
                }

                log( "Found " + operations.size() + " operation" + ( operations.size() > 1 ? "s" : "" ) + "." );
            }

            else if ( command.containsKey( COMMAND_RESOURCE_CREATE ) )
            {
                String resourceId = "";

                if ( command.get( COMMAND_RESOURCE_CREATE ).size() > 0 )
                {
                    resourceId = command.get( COMMAND_RESOURCE_CREATE ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Resource Id: " );
                    resourceId = in.readLine();
                }

                ( (ResourceService) resourceService ).createResource( resourceId );

                log( "Successfully created resource '" + resourceId + "'" );
            }

            else if ( command.containsKey( COMMAND_RESOURCE_DELETE ) )
            {
                String resourceId = "";

                if ( command.get( COMMAND_RESOURCE_DELETE ).size() > 0 )
                {
                    resourceId = command.get( COMMAND_RESOURCE_DELETE ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Resource Id: " );
                    resourceId = in.readLine();
                }

                ( (ResourceService) resourceService ).removeResource( resourceId );

                log( "Successfully deleted resource '" + resourceId + "'" );
            }

            else if ( command.containsKey( COMMAND_RESOURCE_GET ) )
            {
                String resourceId = "";

                if ( command.get( COMMAND_RESOURCE_DELETE ).size() > 0 )
                {
                    resourceId = command.get( COMMAND_RESOURCE_DELETE ).get( 0 ).trim();
                }
                else
                {
                    System.out.print( "Resource Id: " );
                    resourceId = in.readLine();
                }

                Resource resource = ( (ResourceService) resourceService ).getResource( resourceId );

                log( "Resource('" + resource.getIdentifier() + "', "
                    + ( resource.isPattern() ? "PATTERN" : "NOT A PATTERN" ) + ", "
                    + ( resource.isPermanent() ? "PERMANENT" : "NOT PERMANENT" ) + ")" );
            }

            else if ( command.containsKey( COMMAND_RESOURCE_GETALL ) )
            {
                List<Resource> resources = ( (ResourceService) resourceService ).getResources();

                for ( Resource resource : resources )
                {
                    log( "Resource('" + resource.getIdentifier() + "', "
                        + ( resource.isPattern() ? "PATTERN" : "NOT A PATTERN" ) + ", "
                        + ( resource.isPermanent() ? "PERMANENT" : "NOT PERMANENT" ) + ")" );
                }

                log( "Found " + resources.size() + " resource" + ( resources.size() > 1 ? "s" : "" ) + "." );
            }

            else
            {
                log( command.keySet().iterator().next() + ": command not found." );
            }
        }
        while ( true );
    }

    public static void main( String[] args )
        throws Exception
    {
        CommandLineClient commandLineClient = new CommandLineClient();
        commandLineClient.parseArgs( args );
        commandLineClient.bind();
        commandLineClient.run();
    }
}
