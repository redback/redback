package org.codehaus.redback.rest.services.interceptors;

import org.apache.cxf.message.Message;
import org.codehaus.plexus.redback.authorization.RedbackAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: olamy
 * Date: 11/08/11
 * Time: 12:09
 * To change this template use File | Settings | File Templates.
 */
public class AbstractInterceptor
{

    private Logger log = LoggerFactory.getLogger( getClass() );

    public HttpServletRequest getHttpServletRequest( Message message )
    {
        // FIXME use a constant from cxf
        return (HttpServletRequest) message.get( "HTTP.REQUEST" );
    }

    public HttpServletResponse getHttpServletResponse( Message message )
    {
        // FIXME use a constant from cxf
        return (HttpServletResponse) message.get( "HTTP.RESPONSE" );
    }

    public RedbackAuthorization getRedbackAuthorization( Message message )
    {
        // FIXME provide a patch to cxf to have it as a constant in Message
        Method method = ( (Method) message.get( "org.apache.cxf.resource.method" ) );
        log.debug( " method name " + method.getName() );
        RedbackAuthorization redbackAuthorization = method.getAnnotation( RedbackAuthorization.class );
        return redbackAuthorization;
    }
}
