<%
/**
 *  ����������Ϣͼ����ʾ
 *  2007-03-27
 *  yangyn
 */
%>
<%@ page contentType="text/html;charset=GBK" %>
<%@ include file="/includes/jsp-header.jsp"%>

<%
   String templateMark = request.getParameter("templateMark");   
   
   String templateWFPicVML = (String)request.getAttribute("templateWFPicVML");
%>
<html xmlns:v="urn:schemas-microsoft-com:vml">
<head>
  <title>��̬����VML</title>
</head>
<STYLE>
 v\:* { BEHAVIOR: url(#default#VML) }
</STYLE>
<v:shapetype id="branchNode" coordsize="2 2"> <!--������ ���� ��֧-->
    <v:path v="m 0,0 l 0,0,2,1,0,2 x e" />
</v:shapetype>

<v:shapetype id="shrinkNode" coordsize="2 2"> <!--������ ���� ����-->
    <v:path v="m 0,1 l 0,1,2,2,2,0 x e" />
</v:shapetype>

<script language="javascript">
   //��������༭����
   function popup_task_page(taskInnerID){
      var popupUri = conText + "/agdev/workflow/wftemplate-page.do?method=initTaskPage";
      popupUri = popupUri + "&innerTaskID=" + taskInnerID;
      popupUri = popupUri + "&templateMark=<%=templateMark%>";
      
      var executeFlag = popup_window("<%=context%>","��������༭",popupUri,550,580,true);      
   }
</script>
<BODY>
<v:group ID="workflowGroup" style="WIDTH:2000px;HEIGHT:500px;" coordsize = "2000,500">
   <%=templateWFPicVML%>                   
</v:group>
</BODY>
</HTML>