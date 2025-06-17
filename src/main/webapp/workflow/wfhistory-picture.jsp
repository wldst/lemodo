<%@ page contentType="text/html;charset=GBK" %>
<%@ include file="/includes/jsp-header.jsp"%>

<%
   String templateID = request.getParameter("templateID");   
   
   String historyVML = (String)request.getAttribute("historyVML");
%>
<html xmlns:v="urn:schemas-microsoft-com:vml">
<head>
  <title>动态创建VML</title>
</head>
<STYLE>
 v\:* { BEHAVIOR: url(#default#VML) }
</STYLE>
<v:shapetype id="branchNode" coordsize="2 2"> <!--三角形 向右 分支-->
    <v:path v="m 0,0 l 0,0,2,1,0,2 x e" />
</v:shapetype>

<v:shapetype id="shrinkNode" coordsize="2 2"> <!--三角形 向左 收缩-->
    <v:path v="m 0,1 l 0,1,2,2,2,0 x e" />
</v:shapetype>

<script language="javascript">
   
</script>
<BODY>
<v:group ID="workflowGroup" style="WIDTH:2000px;HEIGHT:500px;" coordsize = "2000,500">
   <%=historyVML%>                   
</v:group>
</BODY>
</HTML>