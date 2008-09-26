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

<html>
<ww:i18n name="org.codehaus.plexus.redback.struts2.default">
<head>
  <title><ww:text name="user.delete.page.title"/></title>
</head>

<body>

<h2><ww:text name="user.delete.section.title"/></h2>

<ww:form action="userdelete!submit" namespace="/security">
  <p>
    <ww:text name="user.delete.message"/>:
  </p>
  <p>
  	<ww:text name="username"/>: <ww:property value="user.username"/><br/>
  	<ww:text name="full.name"/>: <ww:property value="user.fullName"/><br/>
  	<ww:text name="email"/>: <ww:property value="user.email"/><br/>
  </p>
  <ww:hidden label="Username" name="username" />
  <ww:submit value="%{getText('user.delete')}" />
  <ww:submit value="%{getText('cancel')}" action="userdelete!cancel"/>
</ww:form>

</body>
</ww:i18n>
</html>
