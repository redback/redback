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
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Iterator;
import java.util.prefs.InvalidPreferencesFormatException;
import java.security.InvalidParameterException;

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
 *  3. The resource/role will be deleted in the tables (in order) SECURITY_USERASSIGNMENT_ROLENAMES,
 *  SECURITY_ROLE_CHILDROLE_MAP, SECURITY_ROLE_PERMISSION_MAP, SECURITY_PERMISSIONS, SECURITY_RESOURCES and SECURITY_ROLES
 *  if it doesn't exist in Archiva or/and Continuum resources list. <br/>
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
 * 2. JDBC driver class name for Redback users database <br/>
 * 3. Redback users database connection URL <br/>
 * 4. Redback users database username <br/>
 * 5. Redback users database password <br/>
 * 6. Path to Archiva configuration file (if application is continuum, just set this value to "") <br/>
 * 7. Path to Continuum database connection properties file <br/> 
 * </p>
 *
 * <p>
 * The Continuum database connection properties file is where the connection properties of all Continuum databases to be
 * queried are put. It should contain the following 4 key-value pairs for each database instance.
 * </p>
 *
 * <p>
 * continuum.db.driverClassName.${count} <br/>
 * continuum.db.url.${count} <br/>
 * continuum.db.username.${count} <br/>
 * continuum.db.password.${count} <br/>
 * <br/>
 * ${count} - index for each database instance. The set of keys above must have the same count as this would be used to
 * determine which properties are associated to the database instance. <br/>
 * </p>
 *
 * <p>
 * Example: <br/>
 *
 * # Continuum database instance 1 <br/>
 * continuum.db.driverClassName.1=com.mysql.jdbc.Driver <br/>
 * continuum.db.url.1=jdbc:mysql://localhost:3306/continuum  <br/>
 * continuum.db.username.1=myusername <br/>
 * continuum.db.password.1=mypassword <br/>
 *
 * # Continuum database instance 2 <br/>
 * continuum.db.driverClassName.2=com.mysql.jdbc.Driver <br/>
 * continuum.db.url.2=jdbc:mysql://example.com:3306/continuum <br/>
 * continuum.db.username.2=anotherusername <br/>
 * continuum.db.password.2=anotherpassword <br/>
 *
 * </p>
 *
 * <p>
 * <b>Notes:</b><br/>
 * - Make sure that you add the JDBC driver(s) in the classpath either by setting -cp [JDBC driver jar] when executing
 * the redback roles cleanup utility jar or if that doesn't work, put the JDBC driver jar in ${JAVA_HOME}/jre/lib/ext <br/>
 * - When Archiva and Continuum are configured to share their own database, make sure to specify <i>all</i> for
 * the application name parameter or else you might accidentally delete used resource roles from the other application.
 * - When connecting to a remote Continuum database, make sure that the server is configured to accept remote connections.
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
 *         /path/to/continuum.props
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
 *         /path/to/continuum.props
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

    private static final String KEY_PREFIX = "continuum.db";

    private static final String KEY_DRIVER_CLASSNAME = "driverClassName";

    private static final String KEY_DB_URL = "url";

    private static final String KEY_USERNAME = "username";

    private static final String KEY_PASSWORD = "password";

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
        List<String> loadedDriverClasses = new ArrayList<String>();

        if ( application.equals( "archiva" ) )
        {
            String archivaConfigFile = args[5];

            if( "".equals( archivaConfigFile ) )
            {
                throw new InvalidParameterException(
                    "Required paramater 'Archiva configuration file' should not be empty." );
            }

            activeResources = getRepositories( archivaConfigFile );
        }
        else if ( application.equals( "continuum" ) )
        {
            String props = args[6];

            if( "".equals( props ) )
            {
                throw new InvalidParameterException(
                    "Required paramater 'Continuum database connection properties file' should not be empty." );
            }

            loadContinuumResources( props, loadedDriverClasses, activeResources );

        }
        else if ( application.equals( "all" ) )
        {
            String archivaConfigFile = args[5];

            if( "".equals( archivaConfigFile ) )
            {
                throw new InvalidParameterException(
                    "Required paramater 'Archiva configuration file' should not be empty." );
            }

            String props = args[6];
            if( "".equals( props ) )
            {
                throw new InvalidParameterException(
                    "Required paramater 'Continuum database connection properties file' should not be empty." );
            }

            activeResources = getRepositories( archivaConfigFile );

            loadContinuumResources( props, loadedDriverClasses, activeResources );
        }
        else
        {
            System.out.println( "Application '" + application + "' is not recognized." );
            return;
        }

        if( !loadedDriverClasses.contains( args[1] ) )
        {
            Class.forName( args[1] );
            loadedDriverClasses.add( args[1] );
        }

        try
        {
            System.out.println( "Connecting to Users database.." );

            usersConn = DriverManager.getConnection( args[2], args[3], args[4] );

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
            // delete user assignments;
            for ( String resource : resourcesToBeDeleted )
            {
                deleteRoles = usersConn.prepareStatement( "DELETE from SECURITY_USERASSIGNMENT_ROLENAMES where STRING_ELE LIKE '% - " + resource + "'" );
                deleteRoles.execute();
            }

            clearSQL( deleteRoles );

         // delete child roles
            for ( String resource : resourcesToBeDeleted )
            {
                deleteRoles = usersConn.prepareStatement( "DELETE from SECURITY_ROLE_CHILDROLE_MAP where STRING_ELE LIKE '% - " + resource + "'" );
                deleteRoles.execute();
            }
            clearSQL( deleteRoles );


         // delete role-permissions map
            for ( String resource : resourcesToBeDeleted )
            {
                deleteRoles = usersConn.prepareStatement( "DELETE from SECURITY_ROLE_PERMISSION_MAP where NAME_OID LIKE '% - " + resource + "'" );
                deleteRoles.execute();
            }
            clearSQL( deleteRoles );

         // delete permissions
            for ( String resource : resourcesToBeDeleted )
            {
                deleteRoles = usersConn.prepareStatement( "DELETE from SECURITY_PERMISSIONS where RESOURCE_IDENTIFIER_OID = '" + resource + "' OR " +
                		"NAME LIKE '% - " + resource + "'" );
                deleteRoles.execute();
            }
            clearSQL( deleteRoles );

          // delete resources
            for ( String resource : resourcesToBeDeleted )
            {
                deleteRoles = usersConn.prepareStatement( "DELETE from SECURITY_RESOURCES where IDENTIFIER = ?" );
                deleteRoles.setString( 1, resource );
                deleteRoles.execute();
            }
            clearSQL( deleteRoles );

          // delete roles
            for ( String resource : resourcesToBeDeleted )
            {
                deleteRoles =
                    usersConn.prepareStatement( "DELETE from SECURITY_ROLES where NAME LIKE '% - " + resource + "'" );
                deleteRoles.execute();
            }
            clearSQL( deleteRoles );

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

    protected static void loadContinuumResources( String props, List<String> loadedDriverClasses,
                                               List<String> activeResources )
        throws IOException, ClassNotFoundException, SQLException
    {
        Properties dbConnectionProps = getProperties( props );
        Set keys = dbConnectionProps.keySet();

        for( Iterator iter = keys.iterator(); iter.hasNext(); )
        {
            String key = ( String ) iter.next();
            String[] parts = key.split( "\\." );

            if( parts.length != 4 )
            {
                System.out.println( "Not enough parts, skipping property.." );
                continue;
            }
            else
            {
                if( KEY_DRIVER_CLASSNAME.equals( parts[2] ) )
                {
                    String driverClassName = dbConnectionProps.getProperty( key );

                    if( !loadedDriverClasses.contains( driverClassName ) )
                    {
                        Class.forName( driverClassName );
                        loadedDriverClasses.add( driverClassName );
                    }

                    String num = parts[3];
                    String dbConnectionUrl = dbConnectionProps.getProperty( KEY_PREFIX + "." + KEY_DB_URL + "." + num );

                    if( dbConnectionUrl == null || "".equals( dbConnectionUrl ) )
                    {
                        continue;
                    }

                    String dbUsername = dbConnectionProps.getProperty( KEY_PREFIX + "." + KEY_USERNAME + "." + num );
                    String dbPassword = dbConnectionProps.getProperty( KEY_PREFIX + "." + KEY_PASSWORD + "." + num );

                    activeResources.addAll( getProjectGroups( dbConnectionUrl, dbUsername, dbPassword ) );
                }
            }
        }
    }

    private static void clearSQL( PreparedStatement deleteRoles )
        throws SQLException
    {
        if( deleteRoles != null )
        {
            deleteRoles.clearBatch();
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
            System.out.println( "\nConnecting to Continuum database '" + jdbcUrl + "'.." );

            continuumConn = DriverManager.getConnection( jdbcUrl, username, password );

            System.out.println( "Retrieving active Project Groups from Continuum database.." );

            Statement stmt =
                continuumConn.createStatement( ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY );
            ResultSet result = stmt.executeQuery( "SELECT NAME from PROJECTGROUP" );

            System.out.println( "Project Groups retrieved:" );
            while ( result.next() )
            {
                String pGroup = result.getString( 1 );
                projectGroups.add( pGroup );

                System.out.println( pGroup );
            }
        }
        finally
        {
            continuumConn.close();
        }

        return projectGroups;
    }

    private static Properties getProperties( String props )
        throws IOException
    {
        File propsFile = new File( props );

        Properties propsFromFile = new Properties();
        propsFromFile.load( new FileInputStream( propsFile) );

        return propsFromFile;
    }
}
