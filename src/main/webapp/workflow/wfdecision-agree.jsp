<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../tag.inc" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%
String bizDataID = request.getParameter("bizDataID");
   String bizTabName = request.getParameter("bizTabName");
   String templateMark = request.getParameter("templateMark");
   String currEmpID = request.getParameter("currEmpID");
   String taskComeDatetime = request.getParameter("taskComeDatetime");
   
   
   SimpleTask currentTask = (SimpleTask)request.getAttribute("currentTask");
   SimpleTask nextTask = (SimpleTask)request.getAttribute("nextTask");
   
   Decision performDecision = null;
   if (currentTask != null){
      performDecision = currentTask.getWfTaskDecision(WFEConstants.WFDECISION_PERFORM);
      //currentTask.getTaskProperty().getAllExecute();
   }
   int decisionExecuteType = WFEConstants.DECISION_EXEC_EMP;
   if (performDecision != null){
      decisionExecuteType = performDecision.getExecuteType();
   }
   
   int autoExecuteFlag = 0;
   boolean notCanSelect = false;
   if (nextTask != null){
      autoExecuteFlag = nextTask.getTaskProperty().getTaskAutoExecute();
      if(nextTask.getTaskProperty().getAllExecute()==1)
      {
      	notCanSelect = true;
      }
   }
   System.out.println("notCanSelect:"+notCanSelect);
   List xmlWFTaskExecutorList = (List)request.getAttribute("xmlWFTaskExecutorList");
%>
<html>
<head>
<title>流程任务审批</title>
<jsp:include flush="false"  page="../../header.jsp"></jsp:include>
</head>
<script language="javascript">
	//流程动作执行
	function performAction(){
		<%
		if (nextTask != null && decisionExecuteType == WFEConstants.DECISION_EXEC_EMP){            
			if (autoExecuteFlag != WFEConstants.DB_BOOLEAN_TRUE){
				%>
				var checkedCount = $(":checked").length;//getSelectedCheckBox(document.wfDecisionAgreeForm,"executorIDs");
				if (checkedCount <= 0){
					alert("没有选择执行人！");
					return;
				}
				<%
			}
		}
		%>
		var params=$("#wfDecisionAgreeForm").serialize();
		$.ajax({
			url: '${contextPath}/wfAgreeForm/agree.so', 
			type: 'POST', 
			data: params, 
			dataType: 'json', 
			beforeSend: function () {
				$("#BTN_SAVE").attr({"disabled":true});
			},
			success: function(result){
				alert(result.message);
				if (result.success) { // 操作成功
					// 刷新数据列表
					window.returnValue = result.success;
					//window.close();
				art.dialog.close();
				} 
			},
			complete: function(e, xhr, settings) {
				$("#BTN_SAVE").attr({"disabled":false});
			}
		});

	}         
</script>
<body scroll="no">
<form name="wfDecisionAgreeForm"  id="wfDecisionAgreeForm" method="POST">    
<input type="hidden" name="bizDataID" id="bizDataID" value="<%=TextUtil.nvl(bizDataID)%>">
<input type="hidden" name="bizTabName" id="bizTabName" value="<%=TextUtil.nvl(bizTabName)%>">
<input type="hidden" name="templateMark" id="templateMark" value="<%=TextUtil.nvl(templateMark)%>">
<input type="hidden" name="currEmpID" id="currEmpID" value="<%=currEmpID%>">
<input type="hidden" name="taskComeDatetime" id="taskComeDatetime" value="<%=taskComeDatetime%>">


<table width="100%" cellpadding="2" cellspacing="2" class="addtable" align="center">
	<tr >
      <td class="label" nowrap width="15%">当前任务：</td>
      <td class="tdinput" nowrap width="85%"> 
         <%
            if (currentTask != null){
         %>
         <%=currentTask.getTaskName()%>
         <%
            }
         %>
      </td>
   </tr>
   <tr >
      <td class="label" nowrap width="15%">下一任务：</td>
      <td class="tdinput" nowrap width="85%"> 
         <%
            if (nextTask != null){
         %>
         <%=nextTask.getTaskName()%>
         <%
            }
         %>
      </td>
   </tr>
   <%
      if (autoExecuteFlag != WFEConstants.DB_BOOLEAN_TRUE){
   %>
    <tr >
      <td class="label" nowrap width="15%">执 行 人：</td>
      <td class="tdinput" nowrap width="85%">
         <%
            if (xmlWFTaskExecutorList != null && xmlWFTaskExecutorList.size() > 0){
               int listSize = xmlWFTaskExecutorList.size();
               for(int i = 0; i < listSize; i++){
                  Map tempObj = (Map)xmlWFTaskExecutorList.get(i);
         %> 
       <input type="checkBox" name="executorIDs" value='<%=(String)tempObj.get("ID")%>' checked  readonly/><%=(String)tempObj.get("EMPNAME")%>
         <%
         if((i+1)%6==0){
        %>
        	 </br>
        <%

                  }
             }
        }
         %>
      </td>
   </tr>
   <%
      }
   %>
   <tr >
      <td class="label" nowrap width="15%">执行意见：</td>
      <td class="tdinput" nowrap width="85%"> 
         <textarea cols="60" rows="5" name="executeComment"></textarea>
      </td>
   </tr>
   <tr align="center">
      <td height="45" nowrap colspan="2">         
         <input type="button" name="BTN_SAVE" id="BTN_SAVE" value="确认执行" 
            class="input_button" onclick="performAction();">
         <input type="button" name="BTN_CLOSE" id="BTN_CLOSE" value="取消" 
            class="input_button" onclick="art.dialog.close();">
      </td>
   </tr>
</table>     
</form>

<iframe name="operFrame" width="0" height="0"></iframe> 
</body>
</html>