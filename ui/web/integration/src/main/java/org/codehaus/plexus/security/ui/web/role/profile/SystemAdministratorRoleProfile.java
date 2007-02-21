package org.codehaus.plexus.security.ui.web.role.profile;

/*
 * Copyright 2005-2006 The Codehaus.
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

import org.codehaus.plexus.rbac.profile.AbstractRoleProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SystemAdministratorRoleProfile:
 *
 * @author: Jesse McConnell <jesse@codehaus.org>
 * @version: $ID:$
 * @plexus.component role="org.codehaus.plexus.rbac.profile.RoleProfile"
 * role-hint="system-administrator"
 */
public class SystemAdministratorRoleProfile
    extends AbstractRoleProfile
{
    public String getRoleName()
    {
        return RoleConstants.SYSTEM_ADMINISTRATOR_ROLE;
    }

    public List getOperations()
    {
        List operations = new ArrayList();
        operations.add( RoleConstants.CONFIGURATION_EDIT_OPERATION );
        operations.add( RoleConstants.USER_MANAGEMENT_RBAC_ADMIN_OPERATION );

        return operations;
    }

    public List getChildRoles()
    {
        return Collections.singletonList( RoleConstants.USER_ADMINISTRATOR_ROLE );
    }


    public boolean isAssignable()
    {
        return true;
    }


    public boolean isPermanent()
    {
        return true;
    }
}