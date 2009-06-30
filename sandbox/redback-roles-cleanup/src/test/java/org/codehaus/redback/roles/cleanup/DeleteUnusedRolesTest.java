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
import java.util.List;
import java.util.prefs.InvalidPreferencesFormatException;

import org.codehaus.redback.roles.cleanup.DeleteUnusedRoles;

import junit.framework.TestCase;

/**
 * Test for DeleteUnusedRolesTest
 * 
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class DeleteUnusedRolesTest 
    extends TestCase
{   
    private String basedir;
    
    private String getBasedir()
    {
        basedir = System.getProperty( "basedir" );
        if ( basedir == null )
        {
            basedir = new File( "" ).getAbsolutePath();
        }

        return basedir;
    }
    
    public void testGetRepositories()
        throws Exception
    {
        File testFile = new File( getBasedir(), "target/test-classes/archiva.xml" );
        
        List<String> repos = DeleteUnusedRoles.getRepositories( testFile.getAbsolutePath() );
        
        assertEquals( 4, repos.size() );
        
        assertTrue( repos.contains( "snapshots" ) );
        assertTrue( repos.contains( "releases" ) );
        assertTrue( repos.contains( "sandbox" ) );
        assertTrue( repos.contains( "test-repo" ) );
    }
    
    public void testGetRepositoriesOldFormat()
        throws Exception
    {
        File testFile = new File( getBasedir(), "target/test-classes/old-archiva.xml" );
        
        List<String> repos = DeleteUnusedRoles.getRepositories( testFile.getAbsolutePath() );
        
        assertEquals( 4, repos.size() );
        
        assertTrue( repos.contains( "snapshots" ) );
        assertTrue( repos.contains( "releases" ) );
        assertTrue( repos.contains( "sandbox" ) );
        assertTrue( repos.contains( "central" ) );
    }
    
    public void testGetRepositoriesInvalidFormat()
        throws Exception
    {
        File testFile = new File( getBasedir(), "target/test-classes/invalid-archiva.xml" );
        
        try
        {
            DeleteUnusedRoles.getRepositories( testFile.getAbsolutePath() );
            fail( "An InvalidPreferencesFormatException should have been thrown." );
        }
        catch ( InvalidPreferencesFormatException e )
        {
            assertTrue( true );
        }
    }
    
    public void testGetRepositoriesConfigFileDoesNotExist()
        throws Exception
    {
        File testFile = new File( getBasedir(), "target/test-classes/nonexisting-archiva.xml" );
        
        try
        {
            DeleteUnusedRoles.getRepositories( testFile.getAbsolutePath() );
            fail( "An IOException should have been thrown." );
        }
        catch ( IOException e )
        {
            assertTrue( true );
        }
    }
    
    public void testGetRepositoriesConfigFilePathIsEmpty()
        throws Exception
    {   
        try
        {
            DeleteUnusedRoles.getRepositories( "" );
            fail( "An IllegalArgumentException should have been thrown." );
        }
        catch ( IllegalArgumentException e )
        {
            assertTrue( true );
        }
    }
}
