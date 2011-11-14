package org.codehaus.redback.rest.services.interceptors;
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.codehaus.plexus.redback.policy.PasswordRuleViolationException;
import org.codehaus.plexus.redback.policy.PasswordRuleViolations;
import org.codehaus.redback.rest.services.RedbackRestError;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Olivier Lamy
 * @since 1.4
 */
@Provider
@Service( "passwordRuleViolationExceptionMapper" )
public class PasswordRuleViolationExceptionMapper
    implements ExceptionMapper<PasswordRuleViolationException>
{
    public Response toResponse( PasswordRuleViolationException e )
    {
        RedbackRestError restError = new RedbackRestError();
        restError.setErrorMessage( e.getMessage() );
        List<String> errorKeys = new ArrayList<String>();
        for ( PasswordRuleViolations.MessageReference messageReference : e.getViolations().getViolations() )
        {
            errorKeys.add( messageReference.getKey() );
        }
        restError.setErrorKeys( errorKeys );
        Response.ResponseBuilder responseBuilder = Response.status( 500 ).entity( restError );
        return responseBuilder.build();
    }
}
