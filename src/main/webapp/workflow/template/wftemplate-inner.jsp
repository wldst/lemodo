<%
/**
 *  快捷开发平台普通内部执行画面
 *  2007-10-09
 *  yangyn
 */
%>
<%@ page contentType="text/html;charset=GBK" %>
<%@ include file="/includes/jsp-header.jsp"%>

<%
   Boolean executeFlagObj = (Boolean)request.getAttribute("executeFlag");
   boolean executeFlag = false;
   if (executeFlagObj != null){
      executeFlag = executeFlagObj.booleanValue();
   }
%>
<html>
<head>
<title>流程执行画面</title>
</head>
<script language="javascript">
   function remindScript(){
      if (parent.document.all.BTN_SAVE != null){
         parent.document.all.BTN_SAVE.disabled=false;
      }
      if (parent.document.all.BTN_CLOSE != null){
         parent.document.all.BTN_CLOSE.disabled=false;
      }
      <%
      if (executeFlag){         
      %>
      window.returnValue = "true";
      window.close();
      <%
         }
      %>
   }
</script>
<body scroll="no">   
<agdev:message nextLine="true" remindType="<%=AgdevConstants.WEB_MSG_REMINDTYPE_ALERT%>"
   executeScript="remindScript();"/>   
</body>
</html>