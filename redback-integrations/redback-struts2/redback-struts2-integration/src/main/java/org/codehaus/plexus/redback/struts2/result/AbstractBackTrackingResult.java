package org.codehaus.plexus.redback.struts2.result;

/*
 * Copyright 2006-2007 The Codehaus Foundation.
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.struts2.dispatcher.ServletActionRedirectResult;
import org.codehaus.plexus.redback.struts2.interceptor.ActionInvocationTracker;
import org.codehaus.plexus.redback.struts2.interceptor.SavedActionInvocation;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.config.entities.ResultConfig;

@SuppressWarnings("serial")
public class AbstractBackTrackingResult
    extends ServletActionRedirectResult
{
    public static final int PREVIOUS = 1;

    public static final int CURRENT = 2;
    
    protected boolean setupBackTrackPrevious( ActionInvocation invocation )
    {
        return setupBackTrack( invocation, PREVIOUS );
    }

    protected boolean setupBackTrackCurrent( ActionInvocation invocation )
    {
        return setupBackTrack( invocation, CURRENT );
    }

    @SuppressWarnings("unchecked")
    protected boolean setupBackTrack( ActionInvocation invocation, int order )
    {
        Map session = invocation.getInvocationContext().getSession();
        ActionInvocationTracker tracker = (ActionInvocationTracker) session.get( ActionInvocationTracker.ROLE );

        if ( tracker != null && tracker.isBackTracked() )
        {
            SavedActionInvocation savedInvocation;

            if ( order == PREVIOUS )
            {
                savedInvocation = tracker.getPrevious();
            }
            else
            {
                savedInvocation = tracker.getCurrent();
            }

            if ( savedInvocation != null )
            {
                setNamespace( savedInvocation.getNamespace() );
                setActionName( savedInvocation.getActionName() );
                setMethod( savedInvocation.getMethodName() );
                invocation.getInvocationContext().getParameters().clear();
                invocation.getInvocationContext().getParameters().putAll( savedInvocation.getParametersMap() );
                
                // hack for REDBACK-188
                String resultCode = invocation.getResultCode();
                if( resultCode != null )
                {
	            	ResultConfig resultConfig = invocation.getProxy().getConfig().getResults().get( resultCode );
	            	resultConfig.getParams().clear();	            	
	            	Map<String, String> filteredMap = new HashMap<String, String>();
	            	Set<String> keys = savedInvocation.getParametersMap().keySet();
	            	
	            	for( String key : keys )
	            	{	
	            		String value = savedInvocation.getParametersMap().get(key)[0];
	            		filteredMap.put( key , value);
	            	}
	            	
	            	resultConfig.getParams().putAll( filteredMap );
                }
                
                tracker.unsetBackTrack();
            }

            return true;
        }

        return false;
    }
}