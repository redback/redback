package org.codehaus.plexus.redback.struts2.interceptor;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @plexus.component role="com.opensymphony.xwork2.interceptor.Interceptor"
 *      role-hint="redbackXSSParameterCheckInterceptor"
 */
public class XSSParameterCheckInterceptor
    implements Interceptor
{
    private Logger log = LoggerFactory.getLogger( XSSParameterCheckInterceptor.class );

    private static final String SCRIPT_TAG_KEYWORD = "<script>";

    private static final String JAVASCRIPT_KEYWORD = "javascript";

    private static final String POSSIBLE_XSS_ATTACK = "possible-xss-attack";

    public void destroy()
    {
    }

    public void init()
    {
    }

    public String intercept( ActionInvocation actionInvocation )
        throws Exception
    {
        Map<String, Object> params = actionInvocation.getInvocationContext().getParameters();

        for ( String key : params.keySet() )
        {
            Object value = params.get( key );

            if ( value != null )
            {
                if ( value instanceof String )
                {
                    String strValue = cleanseString( (String) value );
                    if ( containsBannedXSSKeywords( key, strValue ) )
                    {
                        return POSSIBLE_XSS_ATTACK;
                    }
                }
                else if ( value instanceof String[] )
                {
                    String[] val = (String[]) value;
                    for ( int i = 0; i < val.length; i++ )
                    {
                        String strValue = cleanseString( val[i] );
                        if ( containsBannedXSSKeywords( key, strValue ) )
                        {
                            return POSSIBLE_XSS_ATTACK;
                        }
                    }
                }
            }
        }

        return actionInvocation.invoke();
    }

    private boolean containsBannedXSSKeywords( String key, String strValue )
    {
        if ( StringUtils.containsIgnoreCase( strValue, SCRIPT_TAG_KEYWORD ) )
        {
            log.warn( "Possible XSS attack detected! '" + SCRIPT_TAG_KEYWORD + "' was found in the " +
                "request parameter '" + key + "' of your action." );
            return true;
        }
        else if( StringUtils.containsIgnoreCase( strValue, JAVASCRIPT_KEYWORD ) )
        {
            log.warn( "Possible XSS attack detected! '" + JAVASCRIPT_KEYWORD + "' was found in the " +
                    "request parameter '" + key + "' of your action." );
            return true;
        }
        return false;
    }

    private String cleanseString( String str )
    {
        String cleansed = new String( str );

        cleansed = StringUtils.replace( cleansed, "&lt;", "<" );
        cleansed = StringUtils.replace( cleansed, "&gt;", ">" );
        cleansed = StringUtils.deleteWhitespace( cleansed );
        cleansed = StringUtils.remove( cleansed, "%20" );
        cleansed = StringUtils.lowerCase( cleansed );

        return cleansed;
    }
}
