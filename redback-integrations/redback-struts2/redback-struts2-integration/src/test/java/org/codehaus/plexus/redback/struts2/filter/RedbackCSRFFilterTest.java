package org.codehaus.plexus.redback.struts2.filter;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.anyObject;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class RedbackCSRFFilterTest
    extends PlexusInSpringTestCase
{
    private RedbackCSRFFilter filter;

    private HttpServletRequest request;

    private HttpServletResponse response;

    public void setUp() throws Exception
    {
        filter = new RedbackCSRFFilterStub();

        Random randomizer = new SecureRandom();
        filter.setRandomizer( randomizer );

        request = createMock( HttpServletRequest.class );
        response = createMock( HttpServletResponse.class );
    }

    public void testDefaultValuesInInit()
        throws Exception
    {
        FilterConfig filterConfig = createMock( FilterConfig.class );

        expect( filterConfig.getInitParameter( RedbackCSRFFilter.PARAM_NONCE_CACHE_SIZE ) ).andReturn( null );
        expect( filterConfig.getInitParameter( RedbackCSRFFilter.PARAM_EXCLUDED_PATHS ) ).andReturn( null );
        expect( filterConfig.getInitParameter( RedbackCSRFFilter.PARAM_RANDOM_CLASS ) ).andReturn( null );

        replay( filterConfig );

        filter.init( filterConfig );
        
        assertEquals( 6, filter.getNonceCacheSize() );
        assertTrue( filter.getExcludedPaths().isEmpty() );
        assertEquals( "java.security.SecureRandom", filter.getRandomClass() );

        verify( filterConfig );
    }

    public void testInit()
        throws Exception
    {
        FilterConfig filterConfig = createMock( FilterConfig.class );

        expect( filterConfig.getInitParameter( RedbackCSRFFilter.PARAM_NONCE_CACHE_SIZE ) ).andReturn( "10" ).times( 2 );
        expect( filterConfig.getInitParameter( RedbackCSRFFilter.PARAM_EXCLUDED_PATHS ) )
            .andReturn( "/css/**,/images/**" ).times( 2 );
        expect( filterConfig.getInitParameter( RedbackCSRFFilter.PARAM_RANDOM_CLASS ) ).andReturn( "java.security.SecureRandom" );

        replay( filterConfig );

        filter.init( filterConfig );

        assertEquals( 10, filter.getNonceCacheSize() );
        assertFalse( filter.getExcludedPaths().isEmpty() );
        assertEquals( "java.security.SecureRandom", filter.getRandomClass() );

        verify( filterConfig );
    }

    public void testGenerateEncodedNonce()
        throws Exception
    {
        String encodedNonce = filter.generateEncodedNonce();
        String decodedNonce = filter.decodeNonce( encodedNonce );

        assertEquals( encodedNonce, Base64.encodeBase64String( decodedNonce.getBytes() ) );
    }

    public void testNonceNotFoundInCache()
        throws Exception
    {
        HttpSession session = createMock( HttpSession.class );

        LruCache<String> nonceCache = new LruCache<String>( 6 );
        
        expect( request.getMethod() ).andReturn( RedbackCSRFFilter.HTTP_GET_METHOD );
        expect( request.getSession( true ) ).andReturn( session );
        expect( session.getAttribute( RedbackCSRFFilter.CSRF_NONCE_SESSION_ATTR_NAME ) ).andReturn( nonceCache );
        expect( request.getParameter( RedbackCSRFFilter.CSRF_NONCE_REQUEST_PARAM ) ).andReturn( "r4nd0m3nc0d3dn0nc3" );
        response.sendError( HttpServletResponse.SC_FORBIDDEN );
        expectLastCall().once();

        recordOtherExpectedCallsToRequestObj();

        replay( request, session, response );

        filter.doFilter( request, response, null );

        // no nonce should have been added in the nonceCache
        assertEquals( 0, nonceCache.getSize() );

        verify( request, session, response );
    }

    public void testNonceFoundInCache()
        throws Exception
    {
        HttpSession session = createMock( HttpSession.class );
        FilterChain filterChain = createMock( FilterChain.class );

        String encodedNonce = "r4nd0m3nc0d3dn0nc3";
        LruCache<String> nonceCache = new LruCache<String>( 6 );
        nonceCache.add( new String( Base64.decodeBase64( encodedNonce ) ) );

        expect( request.getMethod() ).andReturn( RedbackCSRFFilter.HTTP_GET_METHOD );
        expect( request.getSession( true ) ).andReturn( session );
        expect( session.getAttribute( RedbackCSRFFilter.CSRF_NONCE_SESSION_ATTR_NAME ) ).andReturn( nonceCache );
        expect( request.getParameter( RedbackCSRFFilter.CSRF_NONCE_REQUEST_PARAM ) ).andReturn( encodedNonce );
        filterChain.doFilter( ( ServletRequest ) anyObject(), ( ServletResponse ) anyObject() );
        expectLastCall().once();

        recordOtherExpectedCallsToRequestObj();

        replay( request, session, filterChain );

        filter.doFilter( request, response, filterChain );

        assertTrue( nonceCache.contains( new String( Base64.decodeBase64( encodedNonce ) ) ) );

        // new nonce should have been added to the nonceCache
        assertEquals( 2, nonceCache.getSize() );

        verify( request, session, filterChain );
    }

    public void testNonceCacheIsNull()
        throws Exception
    {
        HttpSession session = new HttpSessionStub();
        FilterChain filterChain = createMock( FilterChain.class );

        String encodedNonce = "r4nd0m3nc0d3dn0nc3";

        expect( request.getMethod() ).andReturn( RedbackCSRFFilter.HTTP_GET_METHOD );
        expect( request.getSession( true ) ).andReturn( session );
        expect( request.getSession() ).andReturn( session );
        expect( request.getParameter( RedbackCSRFFilter.CSRF_NONCE_REQUEST_PARAM ) ).andReturn( encodedNonce );
        filterChain.doFilter( ( ServletRequest ) anyObject(), ( ServletResponse ) anyObject() );
        expectLastCall().once();

        recordOtherExpectedCallsToRequestObj();

        replay( request, filterChain );

        filter.doFilter( request, response, filterChain );

        LruCache<String> nonceCache = ( LruCache<String> )session.getAttribute( RedbackCSRFFilter.CSRF_NONCE_SESSION_ATTR_NAME );
        
        // new nonce should have been added to the nonceCache
        assertNotNull( nonceCache );
        assertEquals( 1, nonceCache.getSize() );

        verify( request, filterChain );
    }

    public void testRequestUrlInExcludedPathsExactMatch()
        throws Exception
    {
        HttpSession session = createMock( HttpSession.class );
        FilterChain filterChain = createMock( FilterChain.class );

        List<String> excludedPaths = new ArrayList<String>();
        excludedPaths.add( "/favicon.ico" );
        excludedPaths.add( "/images/hello.png" );
        excludedPaths.add( "/css/**" );

        filter.setExcludedPaths( excludedPaths );

        String encodedNonce = "r4nd0m3nc0d3dn0nc3";
        LruCache<String> nonceCache = new LruCache<String>( 6 );
        nonceCache.add( new String( Base64.decodeBase64( encodedNonce ) ) );

        expect( request.getMethod() ).andReturn( RedbackCSRFFilter.HTTP_GET_METHOD );
        expect( request.getServletPath() ).andReturn( "/favicon.ico" );
        expect( request.getPathInfo() ).andReturn( null );
        expect( request.getSession( true ) ).andReturn( session );
        expect( session.getAttribute( RedbackCSRFFilter.CSRF_NONCE_SESSION_ATTR_NAME ) ).andReturn( nonceCache );
        filterChain.doFilter( ( ServletRequest ) anyObject(), ( ServletResponse ) anyObject() );
        expectLastCall().once();

        replay( request, session, filterChain );

        filter.doFilter( request, response, filterChain );

        assertTrue( nonceCache.contains( new String( Base64.decodeBase64( encodedNonce ) ) ) );

        // new nonce should have been added to the cache
        assertEquals( 2, nonceCache.getSize() );

        verify( request, session, filterChain );
    }

    public void testRequestUrlInExcludedPathsNotExactMatchButInExtensionPattern()
        throws Exception
    {
        HttpSession session = createMock( HttpSession.class );
        FilterChain filterChain = createMock( FilterChain.class );

        List<String> excludedPaths = new ArrayList<String>();
        excludedPaths.add( "/favicon.ico" );
        excludedPaths.add( "/images/hello.png" );
        excludedPaths.add( "/css/**" );

        filter.setExcludedPaths( excludedPaths );

        String encodedNonce = "r4nd0m3nc0d3dn0nc3";
        LruCache<String> nonceCache = new LruCache<String>( 6 );
        nonceCache.add( new String( Base64.decodeBase64( encodedNonce ) ) );

        expect( request.getMethod() ).andReturn( RedbackCSRFFilter.HTTP_GET_METHOD );
        expect( request.getServletPath() ).andReturn( "/css/global.css" );
        expect( request.getPathInfo() ).andReturn( null );
        expect( request.getSession( true ) ).andReturn( session );
        expect( session.getAttribute( RedbackCSRFFilter.CSRF_NONCE_SESSION_ATTR_NAME ) ).andReturn( nonceCache );
        filterChain.doFilter( ( ServletRequest ) anyObject(), ( ServletResponse ) anyObject() );
        expectLastCall().once();

        replay( request, session, filterChain );

        filter.doFilter( request, response, filterChain );

        assertTrue( nonceCache.contains( new String( Base64.decodeBase64( encodedNonce ) ) ) );

        // new nonce should not have been added to the cache because the requested path
        // is a css file (see extensionPatterns variable)
        assertEquals( 1, nonceCache.getSize() );

        verify( request, session, filterChain );
    }

    public void testRequestUrlInExcludedPathsNotExactMatchButNotInExtensionPatternAndNotAllowedAll()
        throws Exception
    {
        HttpSession session = createMock( HttpSession.class );
        FilterChain filterChain = createMock( FilterChain.class );

        List<String> excludedPaths = new ArrayList<String>();
        excludedPaths.add( "/favicon.ico" );
        excludedPaths.add( "/css/**" );
        excludedPaths.add( "/test/**");

        filter.setExcludedPaths( excludedPaths );

        String encodedNonce = "r4nd0m3nc0d3dn0nc3";
        LruCache<String> nonceCache = new LruCache<String>( 6 );
        nonceCache.add( new String( Base64.decodeBase64( encodedNonce ) ) );

        expect( request.getMethod() ).andReturn( RedbackCSRFFilter.HTTP_GET_METHOD );
        expect( request.getServletPath() ).andReturn( "/test/test.ext" );
        expect( request.getPathInfo() ).andReturn( null );
        expect( request.getSession( true ) ).andReturn( session );
        expect( session.getAttribute( RedbackCSRFFilter.CSRF_NONCE_SESSION_ATTR_NAME ) ).andReturn( nonceCache );
        expect( request.getParameter( RedbackCSRFFilter.CSRF_NONCE_REQUEST_PARAM ) ).andReturn( encodedNonce );
        filterChain.doFilter( ( ServletRequest ) anyObject(), ( ServletResponse ) anyObject() );
        expectLastCall().once();

        expect( request.getScheme() ).andReturn( "http" );
        expect( request.getServerName() ).andReturn( "localhost" );
        expect( request.getServerPort() ).andReturn( 8080 ).times( 2 );
        expect( request.getContextPath() ).andReturn( "redback" );
        expect( request.getRequestURL() ).andReturn( new StringBuffer( "http://localhost:8080/redback/test/test.ext" ) );

        replay( request, session, filterChain );

        filter.doFilter( request, response, filterChain );

        assertTrue( nonceCache.contains( new String( Base64.decodeBase64( encodedNonce ) ) ) );

        // new nonce should have been added to the cache because the requested path
        // is not in the extensionPattern of files whose nonce should be cached
        assertEquals( 2, nonceCache.getSize() );

        verify( request, session, filterChain );
    }

    public void testRequestUrlInExcludedPathsNotExactMatchButNotInExtensionPatternAndAllowedAll()
        throws Exception
    {
        HttpSession session = createMock( HttpSession.class );
        FilterChain filterChain = createMock( FilterChain.class );

        List<String> excludedPaths = new ArrayList<String>();
        excludedPaths.add( "/favicon.ico" );
        excludedPaths.add( "/css/**" );
        excludedPaths.add( "//test/**");

        filter.setExcludedPaths( excludedPaths );

        String encodedNonce = "r4nd0m3nc0d3dn0nc3";
        LruCache<String> nonceCache = new LruCache<String>( 6 );
        nonceCache.add( new String( Base64.decodeBase64( encodedNonce ) ) );

        expect( request.getMethod() ).andReturn( RedbackCSRFFilter.HTTP_GET_METHOD );
        expect( request.getServletPath() ).andReturn( "/test/test.ext" );
        expect( request.getPathInfo() ).andReturn( null );
        expect( request.getSession( true ) ).andReturn( session );
        expect( session.getAttribute( RedbackCSRFFilter.CSRF_NONCE_SESSION_ATTR_NAME ) ).andReturn( nonceCache );
        filterChain.doFilter( ( ServletRequest ) anyObject(), ( ServletResponse ) anyObject() );
        expectLastCall().once();

        replay( request, session, filterChain );

        filter.doFilter( request, response, filterChain );

        assertTrue( nonceCache.contains( new String( Base64.decodeBase64( encodedNonce ) ) ) );

        // new nonce should not have been added to the cache because the requested path
        // is not in the extensionPattern of files whose nonce should be cached and the path was configured
        // to allow all (due to the two '/' at the beginning of '//test/**')
        assertEquals( 1, nonceCache.getSize() );

        verify( request, session, filterChain );
    }

    private void recordOtherExpectedCallsToRequestObj()
    {
        expect( request.getServletPath() ).andReturn( "/" );
        expect( request.getPathInfo() ).andReturn( null );
        expect( request.getScheme() ).andReturn( "http" );
        expect( request.getServerName() ).andReturn( "localhost" );
        expect( request.getServerPort() ).andReturn( 8080 ).times( 2 );
        expect( request.getContextPath() ).andReturn( "redback" );
        expect( request.getRequestURL() ).andReturn( new StringBuffer( "http://localhost:8080/redback/query.action" ) );

    }

    private class HttpSessionStub implements HttpSession
    {
        private Map<String, Object> attributes = new HashMap<String, Object>();

        public long getCreationTime()
        {
            return 0;  
        }

        public String getId()
        {
            return null;  
        }

        public long getLastAccessedTime()
        {
            return 0;  
        }

        public ServletContext getServletContext()
        {
            return null;  
        }

        public void setMaxInactiveInterval( int i )
        {
            
        }

        public int getMaxInactiveInterval()
        {
            return 0;  
        }

        public HttpSessionContext getSessionContext()
        {
            return null;  
        }

        public Object getAttribute( String key )
        {
            return attributes.get( key );
        }

        public Object getValue( String s )
        {
            return null;  
        }

        public Enumeration getAttributeNames()
        {
            return null;  
        }

        public String[] getValueNames()
        {
            return new String[0];  
        }

        public void setAttribute( String key, Object value )
        {
            this.attributes.put( key, value );
        }

        public void putValue( String s, Object o )
        {
            
        }

        public void removeAttribute( String s )
        {
            
        }

        public void removeValue( String s )
        {
            
        }

        public void invalidate()
        {
            
        }

        public boolean isNew()
        {
            return false;  
        }
    }

    private class RedbackCSRFFilterStub
        extends RedbackCSRFFilter
    {

    }
}
