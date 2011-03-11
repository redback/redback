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

    private static final String SCRIPT_KEYWORD = "<script>";

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
                    if ( StringUtils.containsIgnoreCase( (String) value, SCRIPT_KEYWORD ) )
                    {
                        log.warn(
                            "Possible XSS attack detected! A '" + SCRIPT_KEYWORD + "' tag was found in the request " +
                                "parameter '" + key + "' of your action." );
                        return POSSIBLE_XSS_ATTACK;
                    }
                }
                else if ( value instanceof String[] )
                {
                    String[] val = (String[]) value;
                    for ( int i = 0; i < val.length; i++ )
                    {
                        if ( StringUtils.containsIgnoreCase( val[i], SCRIPT_KEYWORD ) )
                        {
                            log.warn( "Possible XSS attack detected! A '" + SCRIPT_KEYWORD +
                                "' tag was found in the request " + "parameter '" + key + "' of your action." );
                            return POSSIBLE_XSS_ATTACK;
                        }
                    }
                }
            }
        }

        return actionInvocation.invoke();
    }
}
