package org.codehaus.redback.roles.cleanup;

/*
 * Copyright 2009 The Apache Software Foundation.
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

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.InvalidPreferencesFormatException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Deletes unused resources, roles and related data from the Redback users database.
 * 
 * <p>
 * <b>How it works:</b><br/>
 *  1. The resources from Archiva (repositories) or Continuum (project groups) or both will be retrieved. <br/>
 *  2. The resources from SECURITY_RESOURCES table of users database will be retrieved and checked against the 
 *  retrieved resources from Archiva and Continuum. <br/>
 *  3. Constraints for cascade deletion of resources and roles will be set in the Redback users db tables. <br/>
 *  4. The resource will be deleted if it doesn't exist in Archiva or/and Continuum resources list. <br/>
 *  5. The set constraints from step 3 will be removed.
 * </p>
 *  
 * To use this, execute the following command with the specified parameters below:
 * 
 * <p>
 * <i>java -jar redback-roles-cleanup-${version}.jar [PARAMETERS]</i>
 * </p>
 * 
 * <p>
 * <b>Parameters:</b><br/>
 * 1. Application name - specify archiva, continuum or all<br/>
 * 2. JDBC driver class name <br/>
 * 3. Redback users database connection URL <br/>
 * 4. Redback users database username <br/>
 * 5. Redback users database password <br/>
 * 6. Path to Archiva configuration file (if application is continuum, just set this value to "") <br/> 
 * 7. Continuum builds database connection URL <br/>
 * 8. Continuum builds database username <br/>
 * 9. Continuum builds database password <br/>
 * 10. JDBC driver class name for Continuum (set this parameter only if Continuum database is using a different DBMS)
 * </p>
 * 
 * <p>
 * <b>Notes:</b><br/>
 * - Make sure that you add the JDBC driver in the classpath either by setting -cp [JDBC driver jar] when executing
 * the redback roles cleanup utility jar or if that doesn't work, put the JDBC driver jar in ${JAVA_HOME}/jre/lib/ext <br/>
 * - When Archiva and Continuum are configured to share their own database, make sure to specify <i>all</i> for
 * the application name parameter or else you might accidentally delete used resource roles from the other application.
 * </p>
 * 
 * <p>
 * <b>Sample Usages:</b>
 * <br/>
 * 
 * 1. Users database is used by Continuum only: <br/>
 * 
 * <p>
 * <i>java -jar redback-roles-cleanup-${version}.jar continuum com.mysql.jdbc.Driver 
 *         jdbc:mysql://localhost:3306/users usersdbUsername usersdbPassword "" 
 *         jdbc:mysql://localhost:3306/continuum continuumdbUsername continuumDbPassword
 * </i>
 * </p>
 *
 * 2. Users database is used by Archiva only: <br/>
 * 
 * <p>
 * <i>java -jar redback-roles-cleanup-${version}.jar archiva com.mysql.jdbc.Driver 
 *         jdbc:mysql://localhost:3306/users usersdbUsername usersdbPassword /path/to/archiva.xml
 * </i>
 * </p>
 * 
 * 3. Archiva and Continuum are sharing one users database: <br/>
 * 
 * <p>
 * <i>java -jar redback-roles-cleanup-${version}.jar all com.mysql.jdbc.Driver 
 *         jdbc:mysql://localhost:3306/users usersdbUsername usersdbPassword /path/to/archiva.xml 
 *         jdbc:mysql://localhost:3306/continuum continuumdbUsername continuumDbPassword
 * </i>
 * </p>
 *  
 * </p>
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class DeleteUnusedRoles
{
    private static Connection continuumConn;

    private static Connection usersConn;

    /**
     * Main method.
     * 
     * @param args
     * @throws SQLException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws XPathExpressionException
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws InvalidPreferencesFormatException
     * @throws IllegalArgumentException
     */
    public static void main( String[] args )
        throws SQLException, IOException, SAXException, ParserConfigurationException, XPathExpressionException,
        ClassNotFoundException, InstantiationException, IllegalAccessException, InvalidPreferencesFormatException,
        IllegalArgumentException
    {
        String application = args[0];
        List<String> activeResources = new ArrayList<String>();

        Class.forName( args[1] );

        if ( application.equals( "archiva" ) )
        {
            String archivaConfigFile = args[5];
            activeResources = getRepositories( archivaConfigFile );
        }
        else if ( application.equals( "continuum" ) )
        {
            if ( args.length == 10 )
            {
                Class.forName( args[9] );
            }
            activeResources = getProjectGroups( args[6], args[7], args[8] );
        }
        else if ( application.equals( "all" ) )
        {
            String archivaConfigFile = args[5];
            activeResources = getRepositories( archivaConfigFile );

            if ( args.length == 10 )
            {
                Class.forName( args[9] );
            }
            activeResources.addAll( getProjectGroups( args[6], args[7], args[8] ) );
        }
        else
        {
            System.out.println( "Application '" + application + "' is not recognized." );
            return;
        }

        try
        {
            System.out.println( "Connecting to Users database.." );

            usersConn = DriverManager.getConnection( args[2], args[3], args[4] );

            System.out.println( "Adding cascade delete constraints.." );

            addCascadeDeleteConstraints();

            System.out.println( "Removing unusued '" + application + "' resource roles.." );

            List<String> resourcesToBeDeleted = new ArrayList<String>();
            Statement stmt = usersConn.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY );
            ResultSet result = stmt.executeQuery( "SELECT IDENTIFIER from SECURITY_RESOURCES" );

            System.out.println( "============= Resources to be deleted ==============" );
            while ( result.next() )
            {
                String resource = result.getString( 1 );
                if ( !activeResources.contains( resource ) && !resource.equals( "*" ) &&
                    !resource.equals( "${username}" ) )
                { 
                    System.out.println( resource );
                    resourcesToBeDeleted.add( resource );
                }
            }

            PreparedStatement deleteRoles = null;
            for ( String resource : resourcesToBeDeleted )
            {
                deleteRoles = usersConn.prepareStatement( "DELETE from SECURITY_RESOURCES where IDENTIFIER = ?" );
                deleteRoles.setString( 1, resource );
                deleteRoles.execute();
            }
            deleteRoles.clearBatch();

            for ( String resource : resourcesToBeDeleted )
            {
                deleteRoles =
                    usersConn.prepareStatement( "DELETE from SECURITY_ROLES where NAME LIKE '% - " + resource + "'" );
                deleteRoles.execute();
            }
            deleteRoles.clearBatch();

            System.out.println( "Removing cascade delete constraints.." );

            removeCascadeDeleteConstraints();

            System.out.println( "Unused roles successfully deleted." );
        }
        catch ( SQLException e )
        {
            e.printStackTrace();
        }
        finally
        {
            usersConn.close();
        }
    }

    protected static List<String> getRepositories( String archivaConfigFile )
        throws IOException, SAXException, ParserConfigurationException, XPathExpressionException,
        InvalidPreferencesFormatException, IllegalArgumentException
    {
        System.out.println( "Retrieving active repositories from '" + archivaConfigFile + "'" );

        if ( archivaConfigFile == null || archivaConfigFile.equals( "" ) )
        {
            throw new IllegalArgumentException( "Path to Archiva config file cannot be null or blank." );
        }

        if ( !new File( archivaConfigFile ).exists() )
        {
            throw new IOException( "Archiva config file does not exist." );
        }

        List<String> repositories = new ArrayList<String>();

        Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( archivaConfigFile );
        XPath xPath = XPathFactory.newInstance().newXPath();

        String expression = "/configuration/managedRepositories/managedRepository";

        XPathExpression xPathExpression = xPath.compile( expression );
        NodeList rootNode = (NodeList) xPathExpression.evaluate( xmlDocument, XPathConstants.NODESET );

        // try the old version of archiva config file
        if ( rootNode == null || rootNode.getLength() == 0 )
        {
            expression = "/configuration/repositories/repository";
            xPathExpression = xPath.compile( expression );
            rootNode = (NodeList) xPathExpression.evaluate( xmlDocument, XPathConstants.NODESET );
        }

        if ( rootNode == null || rootNode.getLength() == 0 )
        {
            throw new InvalidPreferencesFormatException( "Invalid Archiva configuration file format." );
        }

        for ( int index = 0; index < rootNode.getLength(); index++ )
        {
            Node aNode = rootNode.item( index );
            NodeList childNodes = aNode.getChildNodes();
            for ( int i = 0; i < childNodes.getLength(); i++ )
            {
                Node childNode = childNodes.item( i );
                if ( childNode.getNodeName().equals( "id" ) )
                {
                    String repositoryName = childNode.getTextContent();
                    repositories.add( repositoryName );
                }
            }
        }

        return repositories;
    }

    protected static List<String> getProjectGroups( String jdbcUrl, String username, String password )
        throws SQLException
    {
        List<String> projectGroups = new ArrayList<String>();

        try
        {
            System.out.println( "Connecting to Continuum database.." );

            continuumConn = DriverManager.getConnection( jdbcUrl, username, password );

            System.out.println( "Retrieving active Project Groups from Continuum database.." );

            Statement stmt =
                continuumConn.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY );
            ResultSet result = stmt.executeQuery( "SELECT NAME from PROJECTGROUP" );

            while ( result.next() )
            {
                projectGroups.add( result.getString( 1 ) );
            }
        }
        finally
        {
            continuumConn.close();
        }

        return projectGroups;
    }

    private static void addCascadeDeleteConstraints()
        throws SQLException
    {
        PreparedStatement addConstraint =
            usersConn.prepareStatement( "alter table SECURITY_PERMISSIONS add constraint SECURITY_PERMISSIONS_FK01_WITH_CASCADE_DELETE foreign key (RESOURCE_IDENTIFIER_OID) references SECURITY_RESOURCES (IDENTIFIER) ON DELETE CASCADE" );
        addConstraint.execute();
        addConstraint.clearBatch();

        addConstraint =
            usersConn.prepareStatement( "alter table SECURITY_ROLE_PERMISSION_MAP add constraint SECURITY_ROLE_PERMISSION_MAP_FK01_WITH_CASCADE_DELETE foreign key (NAME_EID) references SECURITY_PERMISSIONS (NAME) ON DELETE CASCADE" );
        addConstraint.execute();
        addConstraint.clearBatch();

        addConstraint =
            usersConn.prepareStatement( "alter table SECURITY_ROLE_PERMISSION_MAP add constraint SECURITY_ROLE_PERMISSION_MAP_FK02_WITH_CASCADE_DELETE foreign key (NAME_OID) references SECURITY_ROLES  (NAME) ON DELETE CASCADE" );
        addConstraint.execute();
        addConstraint.clearBatch();

        addConstraint =
            usersConn.prepareStatement( "alter table SECURITY_ROLE_CHILDROLE_MAP add constraint SECURITY_ROLE_CHILDROLE_MAP_FK01_WITH_CASCADE_DELETE foreign key (NAME_OID) references SECURITY_ROLES (NAME) ON DELETE CASCADE" );
        addConstraint.execute();
        addConstraint.clearBatch();

        addConstraint =
            usersConn.prepareStatement( "alter table SECURITY_USERASSIGNMENT_ROLENAMES add constraint SECURITY_USERASSIGNMENT_ROLENAMES_FK01_WITH_CASCADE_DELETE foreign key (STRING_ELE) references SECURITY_ROLES (NAME) ON DELETE CASCADE" );
        addConstraint.execute();
        addConstraint.clearBatch();
    }

    private static void removeCascadeDeleteConstraints()
        throws SQLException
    {
        PreparedStatement deleteConstraint =
            usersConn.prepareStatement( "alter table SECURITY_PERMISSIONS drop FOREIGN KEY SECURITY_PERMISSIONS_FK01_WITH_CASCADE_DELETE" );
        deleteConstraint.execute();
        deleteConstraint.clearBatch();

        deleteConstraint =
            usersConn.prepareStatement( "alter table SECURITY_ROLE_PERMISSION_MAP drop FOREIGN KEY SECURITY_ROLE_PERMISSION_MAP_FK01_WITH_CASCADE_DELETE" );
        deleteConstraint.execute();
        deleteConstraint.clearBatch();

        deleteConstraint =
            usersConn.prepareStatement( "alter table SECURITY_ROLE_PERMISSION_MAP drop FOREIGN KEY SECURITY_ROLE_PERMISSION_MAP_FK02_WITH_CASCADE_DELETE" );
        deleteConstraint.execute();
        deleteConstraint.clearBatch();

        deleteConstraint =
            usersConn.prepareStatement( "alter table SECURITY_ROLE_CHILDROLE_MAP drop FOREIGN KEY SECURITY_ROLE_CHILDROLE_MAP_FK01_WITH_CASCADE_DELETE" );
        deleteConstraint.execute();
        deleteConstraint.clearBatch();

        deleteConstraint =
            usersConn.prepareStatement( "alter table SECURITY_USERASSIGNMENT_ROLENAMES drop FOREIGN KEY SECURITY_USERASSIGNMENT_ROLENAMES_FK01_WITH_CASCADE_DELETE" );
        deleteConstraint.execute();
        deleteConstraint.clearBatch();
    }
}
