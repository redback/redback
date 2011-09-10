package org.codehaus.redback.rest.api.services;
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

import javax.ws.rs.Path;

/**
 * @author Olivier Lamy
 */
@Path( "/roleManagementService/" )
public interface RoleManagementService
{
    void createTemplatedRole( String templateId, String resource )
        throws RedbackServiceException;

    /**
     * removes a role corresponding to the role Id that was manufactured with the given resource
     * <p/>
     * it also removes any user assignments for that role
     *
     * @param templateId
     * @param resource
     * @throws Exception
     */
    void removeTemplatedRole( String templateId, String resource )
        throws RedbackServiceException;


    /**
     * allows for a role coming from a template to be renamed effectively swapping out the bits of it that
     * were labeled with the oldResource with the newResource
     * <p/>
     * it also manages any user assignments for that role
     *
     * @param templateId
     * @param oldResource
     * @param newResource
     * @throws Exception
     */
    void updateRole( String templateId, String oldResource, String newResource )
        throws RedbackServiceException;


    /**
     * Assigns the role indicated by the roleId to the given principal
     *
     * @param roleId
     * @param principal
     * @throws Exception
     */
    void assignRole( String roleId, String principal )
        throws RedbackServiceException;

    /**
     * Assigns the role indicated by the roleName to the given principal
     *
     * @param roleName
     * @param principal
     * @throws Exception
     */
    void assignRoleByName( String roleName, String principal )
        throws RedbackServiceException;

    /**
     * Assigns the templated role indicated by the templateId
     * <p/>
     * fails if the templated role has not been created
     *
     * @param templateId
     * @param resource
     * @param principal
     */
    void assignTemplatedRole( String templateId, String resource, String principal )
        throws RedbackServiceException;

    /**
     * Unassigns the role indicated by the role id from the given principal
     *
     * @param roleId
     * @param principal
     * @throws Exception
     */
    void unassignRole( String roleId, String principal )
        throws RedbackServiceException;

    /**
     * Unassigns the role indicated by the role name from the given principal
     *
     * @param roleName
     * @param principal
     * @throws Exception
     */
    void unassignRoleByName( String roleName, String principal )
        throws RedbackServiceException;

    /**
     * true of a role exists with the given roleId
     *
     * @param roleId
     * @return
     * @throws Exception
     */
    boolean roleExists( String roleId )
        throws RedbackServiceException;

    /**
     * true of a role exists with the given roleId
     *
     * @param templateId
     * @param resource
     * @return
     * @throws Exception
     */
    boolean templatedRoleExists( String templateId, String resource )
        throws RedbackServiceException;


    /**
     * Check a role template is complete in the RBAC store.
     *
     * @param templateID the templated role
     * @param resource   the resource to verify
     * @throws Exception
     */
    void verifyTemplatedRole( String templateID, String resource )
        throws RedbackServiceException;
}
