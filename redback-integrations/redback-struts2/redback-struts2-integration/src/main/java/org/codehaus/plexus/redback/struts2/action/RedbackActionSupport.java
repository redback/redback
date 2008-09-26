package org.codehaus.plexus.redback.struts2.action;

import com.opensymphony.xwork2.ActionSupport;
import java.util.Map;
import org.apache.struts2.interceptor.SessionAware;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

/**
 *
 * @author <a href="mailto:james@atlassian.com">James William Dumay</a>
 */
public abstract class RedbackActionSupport
    extends ActionSupport
    implements LogEnabled, SessionAware
{
    protected Map session;

    private Logger logger;

    public void setSession( Map map )
    {
        //noinspection AssignmentToCollectionOrArrayFieldFromParameter
        this.session = map;
    }

    public void enableLogging( Logger logger )
    {
        this.logger = logger;
    }

    protected Logger getLogger()
    {
        return logger;
    }
}
