package org.codehaus.plexus.redback.struts2.filter;

import org.codehaus.plexus.spring.PlexusInSpringTestCase;

import javax.servlet.http.HttpServletResponse;

import static org.easymock.EasyMock.createMock;

/**
 * 
 */
public class RedbackCSRFResponseWrapperTest
    extends PlexusInSpringTestCase
{
    private RedbackCSRFResponseWrapper wrapper;

    private static final String newNonce = "3nc0d3dn0nc3";

    public void setUp()
        throws Exception
    {
        HttpServletResponse response = createMock( HttpServletResponse.class );

        wrapper = new RedbackCSRFResponseWrapperStub( response, newNonce );
    }

    public void testAddNonceToUrlCSRFNonceParamPresentAsLastParam()
        throws Exception
    {
        String url = "/redback?csrf_nonce=3x1st1ngn0nc3";

        String encodedURL = wrapper.addNonce( url );

        assertEquals( "/redback?csrf_nonce=" + newNonce, encodedURL );
    }

    public void testAddNonceToUrlCSRFNonceParamPresentAsFirstParam()
        throws Exception
    {
        String url = "/redback?csrf_nonce=3x1st1ngn0nc3&another_param=test";

        String encodedURL = wrapper.addNonce( url );

        assertEquals( "/redback?another_param=test&csrf_nonce=" + newNonce, encodedURL );
    }

    public void testAddNonceToUrlCSRFNonceParamPresentInBetweenParams()
        throws Exception
    {
        String url = "/redback?param1=test&csrf_nonce=3x1st1ngn0nc3&another_param=test";

        String encodedURL = wrapper.addNonce( url );

        assertEquals( "/redback?param1=test&another_param=test&csrf_nonce=" + newNonce, encodedURL );
    }

    public void testAddNonceToUrlMultipleCSRFNonceParamPresent()
        throws Exception
    {
        String url = "/redback?csrf_nonce=3x1st1ngn0nc3&another_param=test&csrf_nonce=an0th3r3x1st1ngn0nc3";

        String encodedURL = wrapper.addNonce( url );

        assertEquals( "/redback?another_param=test&csrf_nonce=" + newNonce, encodedURL );   
    }

    private class RedbackCSRFResponseWrapperStub
        extends RedbackCSRFResponseWrapper
    {
        public RedbackCSRFResponseWrapperStub( HttpServletResponse response, String nonce )
        {
            super( response );
            this.nonce = nonce;
        }
    }

}
