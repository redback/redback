package org.codehaus.plexus.redback.struts2.interceptor;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import org.codehaus.plexus.spring.PlexusInSpringTestCase;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.expectLastCall;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class XSSParameterCheckInterceptorTest
    extends PlexusInSpringTestCase
{
    private Interceptor xssInterceptor;

    private ActionInvocation actionInvocation;

    private ActionContext actionContext;

    public void setUp()
        throws Exception
    {
        xssInterceptor = new XSSParameterCheckInterceptor();
        actionInvocation = createMock( ActionInvocation.class );
        actionContext = org.easymock.classextension.EasyMock.createMock( ActionContext.class );
    }

    public void testNoScriptFoundInRequestParams()
        throws Exception
    {
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put( "name", "notascript" );

        expect( actionInvocation.getInvocationContext() ).andReturn( actionContext );
        expect( actionContext.getParameters() ).andReturn( requestParams );
        expect( actionInvocation.invoke() ).andReturn( "success" );

        replay( actionInvocation );
        org.easymock.classextension.EasyMock.replay( actionContext );

        xssInterceptor.intercept( actionInvocation );

        verify( actionInvocation );
        org.easymock.classextension.EasyMock.verify( actionContext );
    }

    public void testScriptFoundInRequestParams()
        throws Exception
    {
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put( "name", "value<script>alert(xss)</script>" );

        expect( actionInvocation.getInvocationContext() ).andReturn( actionContext );
        expect( actionContext.getParameters() ).andReturn( requestParams );

        replay( actionInvocation );
        org.easymock.classextension.EasyMock.replay( actionContext );

        String result = xssInterceptor.intercept( actionInvocation );
        assertEquals( "possible-xss-attack", result );

        verify( actionInvocation );
        org.easymock.classextension.EasyMock.verify( actionContext );
    }

    public void testScriptCasing()
        throws Exception
    {
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put( "name", "value<SCRIPT>alert(xss)</SCRIPT>" );

        expect( actionInvocation.getInvocationContext() ).andReturn( actionContext );
        expect( actionContext.getParameters() ).andReturn( requestParams );

        replay( actionInvocation );
        org.easymock.classextension.EasyMock.replay( actionContext );

        String result = xssInterceptor.intercept( actionInvocation );
        assertEquals( "possible-xss-attack", result );

        verify( actionInvocation );
        org.easymock.classextension.EasyMock.verify( actionContext );
    }

    public void testScriptTagHasWhiteSpace()
        throws Exception
    {
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put( "name", "value<script >alert(xss)</script>" );

        expect( actionInvocation.getInvocationContext() ).andReturn( actionContext );
        expect( actionContext.getParameters() ).andReturn( requestParams );

        replay( actionInvocation );
        org.easymock.classextension.EasyMock.replay( actionContext );

        String result = xssInterceptor.intercept( actionInvocation );
        assertEquals( "possible-xss-attack", result );

        verify( actionInvocation );
        org.easymock.classextension.EasyMock.verify( actionContext );
    }

    public void testScriptTagIsEscaped()
        throws Exception
    {
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put( "name", "value&lt;script&gt;alert(xss)</script>" );

        expect( actionInvocation.getInvocationContext() ).andReturn( actionContext );
        expect( actionContext.getParameters() ).andReturn( requestParams );

        replay( actionInvocation );
        org.easymock.classextension.EasyMock.replay( actionContext );

        String result = xssInterceptor.intercept( actionInvocation );
        assertEquals( "possible-xss-attack", result );

        verify( actionInvocation );
        org.easymock.classextension.EasyMock.verify( actionContext );
    }

    public void testJavascriptCodeInsteadOfTag()
        throws Exception
    {
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put( "name", "value<img onerror=\"javascript:alert('xss')\" src=\"/images/archiva.png\"/>" );

        expect( actionInvocation.getInvocationContext() ).andReturn( actionContext );
        expect( actionContext.getParameters() ).andReturn( requestParams );

        replay( actionInvocation );
        org.easymock.classextension.EasyMock.replay( actionContext );

        String result = xssInterceptor.intercept( actionInvocation );
        assertEquals( "possible-xss-attack", result );

        verify( actionInvocation );
        org.easymock.classextension.EasyMock.verify( actionContext );
    }

}
