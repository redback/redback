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

<html>
<ww:i18n name="org.codehaus.plexus.redback.xwork.default">
<head>
  <title><ww:text name="role.page.title"/></title>
</head>

<body>

  <%@ include file="/WEB-INF/jsp/redback/include/formValidationResults.jsp" %>

  <!-- h2><ww:text name="role.edit.section.title"/></h2 -->

  <div class="axial">
    <table border="1" cellspacing="2" cellpadding="3" width="100%">
      <ww:label label="%{getText('name')}" name="name"/>
      <ww:label label="%{getText('description')}" name="description"/>
    </table>
  </div>

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

  <h3><ww:text name="role.edit.section.permissions"/></h3>
  <c:if test="${empty permissions}">
    <ww:text name="role.create.no.permissions.defined"/>
  </c:if>
  <c:if test="${!empty permissions}">
    <ul>
    <ww:iterator id="permission" value="permissions">
      <li>P[${permission.name}] (${permission.operation.name}, ${permission.resource.identifier})</li>
    </ww:iterator>
    </ul>
  </c:if>
  

  <h3><ww:text name="role.edit.section.users"/></h3>
  <c:if test="${empty users}">
    <ww:text name="role.edit.no.user.defined"/>
  </c:if>
  <c:if test="${!empty users}">
    <ul>
      <ww:iterator id="user" value="users">
        <ww:url id="usereditUrl" action="useredit" includeParams="false">
          <ww:param name="username">${user.username}</ww:param>
        </ww:url>
        <li><ww:a href="%{usereditUrl}">${user.username} (${user.email})</ww:a></li>
      </ww:iterator>
    </ul>
  </c:if>

</body>
</ww:i18n>
</html>
