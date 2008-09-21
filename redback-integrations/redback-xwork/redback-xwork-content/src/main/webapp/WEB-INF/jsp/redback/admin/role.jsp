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

<%@ taglib prefix="ww" uri="/struts-tags"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.extremecomponents.org" prefix="ec" %>

<html>
<ww:i18n name="org.codehaus.plexus.redback.xwork.default">
<head>
  <title><ww:text name="role.page.title"/></title>
</head>

<body>

  <%@ include file="/WEB-INF/jsp/redback/include/formValidationResults.jsp" %>

  <h2><ww:text name="role"/></h2>

  <div class="axial">
    <table border="1" cellspacing="2" cellpadding="3" width="100%">
      <ww:label label="%{getText('name')}" name="name"/>
      <ww:label label="%{getText('description')}" name="description"/>
    </table>
  </div>

  <div class="functnbar3">
    <form action="roleedit.action">
      <input type="hidden" name="name" value="${name}"/>
      <input type="submit" value="Edit"/>
    </form>
  </div>

  <h3><ww:text name="role.model.parent.roles"/></h3>
  <c:if test="${empty parentRoleNames}">
    <ww:text name="role.edit.no.parent.defined"/>
  </c:if>
  <c:if test="${!empty parentRoleNames}">
    <ul>
    <ww:iterator id="parentRoleName" value="parentRoleNames">
      <ww:url id="roleUrl" action="role" includeParams="none">
        <ww:param name="name">${parentRoleName}</ww:param>
      </ww:url>
      <li><ww:a href="%{roleUrl}">${parentRoleName}</ww:a></li>
    </ww:iterator>
    </ul>
  </c:if>

  <h3><ww:text name="role.model.child.roles"/></h3>
  <c:if test="${empty childRoleNames}">
    <ww:text name="role.edit.no.childrole.defined"/>
  </c:if>
  <c:if test="${!empty childRoleNames}">
    <ul>
    <ww:iterator id="childRoleName" value="childRoleNames">
      <ww:url id="roleUrl" action="role" includeParams="none">
        <ww:param name="name">${childRoleName}</ww:param>
      </ww:url>
      <li><ww:a href="%{roleUrl}">${childRoleName}</ww:a></li>
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
  <c:if test="${!empty parentUsers}">
    <h4><ww:text name="role.edit.users.defined.in.parent.roles"/></h4>
    <ul>
      <ww:iterator id="user" value="parentUsers">
        <ww:url id="usereditUrl" action="useredit" includeParams="none">
          <ww:param name="username">${user.username}</ww:param>
        </ww:url>
        <li><ww:a href="%{usereditUrl}">${user.fullName} (${user.username} - ${user.email})</ww:a></li>
      </ww:iterator>
    </ul>
  </c:if>
  <h4><ww:text name="role.edit.users.defined.in.current.role"/></h4>
  <c:if test="${empty users}">
    <ww:text name="role.edit.no.user.defined"/>
  </c:if>
  <c:if test="${!empty users}">
    <ul>
      <ww:iterator id="user" value="users">
        <ww:url id="usereditUrl" action="useredit" includeParams="none">
          <ww:param name="username">${user.username}</ww:param>
        </ww:url>
        <li><ww:a href="%{usereditUrl}">${user.fullName} (${user.username} - ${user.email})</ww:a></li>
      </ww:iterator>
    </ul>
  </c:if>

</body>
</ww:i18n>
</html>
