<%--
  ~ Copyright 2005-2006 The Codehaus.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ taglib prefix="ww" uri="/webwork"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>


<html>
<ww:i18n name="org.codehaus.plexus.redback.xwork.default">
<head>
  <title><ww:text name="role.page.title"/></title>
  <script type="text/javascript" language="javascript">
    <!--
      // Compare two options within a list by VALUES
      function compareOptionValues(a, b)
      {
        // Radix 10: for numeric values
        // Radix 36: for alphanumeric values
        var sA = parseInt( a.value, 36 );
        var sB = parseInt( b.value, 36 );
        return sA - sB;
      }

      // Compare two options within a list by TEXT
      function compareOptionText(a, b)
      {
        // Radix 10: for numeric values
        // Radix 36: for alphanumeric values
        var sA = parseInt( a.text, 36 );
        var sB = parseInt( b.text, 36 );
        return sA - sB;
      }

      // Dual list move function
      function moveDualList( srcList, destList, moveAll )
      {
        // Do nothing if nothing is selected
        if (  ( srcList.selectedIndex == -1 ) && ( moveAll == false )   )
        {
          return;
        }

        newDestList = new Array( destList.options.length );

        var len = 0;

        for( len = 0; len < destList.options.length; len++ )
        {
          if ( destList.options[ len ] != null )
          {
            newDestList[ len ] = new Option( destList.options[ len ].text, destList.options[ len ].value, destList.options[ len ].defaultSelected, destList.options[ len ].selected );
          }
        }

        for( var i = 0; i < srcList.options.length; i++ )
        {
          if ( srcList.options[i] != null && ( srcList.options[i].selected == true || moveAll ) )
          {
             // Statements to perform if option is selected

             // Incorporate into new list
             newDestList[ len ] = new Option( srcList.options[i].text, srcList.options[i].value, srcList.options[i].defaultSelected, srcList.options[i].selected );
             len++;
          }
        }

        // Sort out the new destination list
        newDestList.sort( compareOptionValues );   // BY VALUES
        //newDestList.sort( compareOptionText );   // BY TEXT

        // Populate the destination with the items from the new array
        for ( var j = 0; j < newDestList.length; j++ )
        {
          if ( newDestList[ j ] != null )
          {
            destList.options[ j ] = newDestList[ j ];
          }
        }

        // Erase source list selected elements
        for( var i = srcList.options.length - 1; i >= 0; i-- )
        {
          if ( srcList.options[i] != null && ( srcList.options[i].selected == true || moveAll ) )
          {
             // Erase Source
             //srcList.options[i].value = "";
             //srcList.options[i].text  = "";
             srcList.options[i]       = null;
          }
        }
      } // End of moveDualList()

      function copy( srcList, destList )
      {
        newDestList = new Array( destList.options.length );

        var len = 0;

        for( len = 0; len < destList.options.length; len++ )
        {
          if ( destList.options[ len ] != null )
          {
            newDestList[ len ] = new Option( destList.options[ len ].text, destList.options[ len ].value, true, true );
          }
        }

        for( var i = 0; i < srcList.options.length; i++ )
        {
          if ( srcList.options[i] != null )
          {
             // Statements to perform if option is selected

             // Incorporate into new list
             newDestList[ len ] = new Option( srcList.options[i].text, srcList.options[i].value, true, true );
             len++;
          }
        }

        // Sort out the new destination list
        newDestList.sort( compareOptionValues );   // BY VALUES
        //newDestList.sort( compareOptionText );   // BY TEXT

        // Populate the destination with the items from the new array
        for ( var j = 0; j < newDestList.length; j++ )
        {
          if ( newDestList[ j ] != null )
          {
            destList.options[ j ] = newDestList[ j ];
          }
        }
      }

      function send()
      {
        copy(document.roleselect.usersSelect, document.rolesave.usersSelect);
        //document.rolesave.submit();
      }
    //-->
  </script>
</head>

<body>

  <%@ include file="/WEB-INF/jsp/redback/include/formValidationResults.jsp" %>

  <h2><ww:text name="role"/></h2>

  <ww:actionerror/>

  <form action="rolesave.action" name="rolesave">
    <input type="hidden" name="name" value="${name}"/>
    <input type="hidden" name="usersList"/>
    <div class="axial">
      <table border="1" cellspacing="2" cellpadding="3" width="100%">
        <ww:label label="%{getText('name')}" name="name"/>
        <ww:textfield label="%{getText('description')}" name="description"/>
      </table>
    </div>
    <div class="functnbar3">
        <input type="submit" value="Save"/>
    </div>
  </form>

  <h3><ww:text name="role.model.child.roles"/></h3>
  <c:if test="${empty childRoleNames}">
    <ww:text name="role.edit.no.childrole.defined"/>
  </c:if>
  <c:if test="${!empty childRoleNames}">
    <ul>
    <ww:iterator id="childRoleName" value="childRoleNames">
      <ww:url id="roleeditUrl" action="roleedit" includeParams="false">
        <ww:param name="name">${childRoleName}</ww:param>
      </ww:url>
      <li><ww:a href="%{roleeditUrl}">${childRoleName}</ww:a></li>
    </ww:iterator>
    </ul>
  </c:if>

  <h3><ww:text name="permissions"/></h3>
  <c:if test="${empty permissions}">
    <ww:text name="role.create.no.permissions.defined"/>
  </c:if>
  <c:if test="${!empty permissions}">
    <ec:table var="permission"
        items="permissions"
        cellspacing="2"
        cellpadding="3"
        showExports="flase"
        showPagination="false"
        showTitle="false"
        showStatusBar="false"
        filterable="false">
      <ec:row>
        <ec:column property="name" title="Name"/>
        <ec:column property="operation.name" title="Operation"/>
        <ec:column property="resource.identifier" title="Resource"/>
      </ec:row>
    </ec:table>
  </c:if>

  <h3><ww:text name="role.edit.section.users"/></h3>

  <form name="roleusers">
    <input type="hidden" name="name" value="${name}"/>
  <table>
    <tr>
      <td>
        <select size="20" multiple="multiple" id="allUsers" name="availableUsers">
          <ww:iterator id="user" value="allUsers">
            <option value="${user.username}">${user.fullName} - ${user.username}</option>
          </ww:iterator>
        </select>
      </td>
      <td>
        <input type="submit" value="--&gt;" onclick="this.form.action='roleusersadd.action'; this.form.submit();"/>
        <br/>
        <input type="submit" value="&lt;--" onclick="this.form.action='roleusersremove.action'; this.form.submit();"/>
      </td>
      <td>
        <c:if test="${!empty users}">
          <select size="20" multiple="multiple" id="users" name="currentUsers">
            <ww:iterator id="user" value="users">
              <option value="${user.username}">${user.fullName} - ${user.username}</option>
            </ww:iterator>
          </select>
        </c:if>
        <c:if test="${empty users}">
          <select size="20" multiple="multiple" id="users" name="usersList"/>
        </c:if>
      </td>
    </tr>
  </table>
  </form>

</body>
</ww:i18n>
</html>
