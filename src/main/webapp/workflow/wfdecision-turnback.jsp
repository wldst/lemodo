<%
/**
 *  流程任务打回画面
 *  2007-03-27
 *  yangyn
 */
%>
<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../tag.inc" %>
<%@ page import="java.util.List" %>

<%
String bizDataID = request.getParameter("bizDataID");
   String bizTabName = request.getParameter("bizTabName");
   String templateMark = request.getParameter("templateMark");
   String taskComeDatetime = request.getParameter("taskComeDatetime");
  
   
   SimpleTask currentTask = (SimpleTask)request.getAttribute("currentTask");
   List completedTask = (List)request.getAttribute("completedTask");
   int count = 0; 
   if(null != completedTask){
   		count = completedTask.size();
   }
%>
<html>
<head>
<title>流程任务打回画面</title>
<jsp:include flush="false"  page="../../header.jsp"></jsp:include>
</head>

<script language="javascript">
   //流程动作执行
   function performAction(){
     	  	// 序列化表单
	//	var count =getSelectedCheckBox(document.wfDecisionAgreeForm,"executorIDs");
		//if (count > 0){
			var params=$("#wfDecisionTurnbackForm").serialize();
	        $.ajax({
		        url: '${contextPath}/wfTurnbackForm/turnback.so', 
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
						art.dialog.close();
				    } 
					
					
	        	},
	        	complete: function(e, xhr, settings) {
					$("#BTN_SAVE").attr({"disabled":false});
	        	}
	        });
	//	} else {
			
		//	alert("请选择选择执行人！");
	//	}
   }         
</script>
<body scroll="no">
<form name="wfDecisionTurnbackForm" id="wfDecisionTurnbackForm" method="POST"> 
<input type="hidden" name="bizDataID" id="bizDataID" value="<%=TextUtil.nvl(bizDataID)%>">
<input type="hidden" name="bizTabName" id="bizTabName" value="<%=TextUtil.nvl(bizTabName)%>">
<input type="hidden" name="templateMark" id="templateMark" value="<%=TextUtil.nvl(templateMark)%>">
<input type="hidden" name="taskComeDatetime" id="taskComeDatetime" value="<%=taskComeDatetime%>">

<table width="100%" cellpadding="2" cellspacing="2" class="addtable" align="center">
   <tr >
      <td class="label" nowrap width="15%">任务选择：</td>
      <td class="tdinput" nowrap width="85%"> 
         <%
          if (completedTask != null && count > 0){
          %><select name="turnBackTaskID"><%
          for(int i = 0; i < count; i++){
                   		SimpleTask task = (SimpleTask)completedTask.get(i);
          %><option value="<%=task.getID() %>"><%=task.getTaskName()%></option><%
	         	}
			 %></select><%
            }else{
            	%>无完成任务节点<%
            }
         %>
      </td>
   </tr>
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
   <%
   		if(count > 0){
   			%>
   				<tr >
			      <td class="label" nowrap width="15%">执行意见：</td>
			      <td class="tdinput" nowrap width="85%"> 
			         <textarea cols="60" rows="5" name="executeComment"></textarea>
			      </td>
			   </tr>
   			<%
   		}
   %>
   
   <tr align="center">
      <td height="45" nowrap colspan="2">
      	 <%
		   	   if(count > 0){
		   			%>
		   				<input type="button" name="BTN_SAVE" value="确认执行" class="input_button" onclick="performAction();">
		   			<%
		   		}
		   %>
         <input type="button" name="BTN_CLOSE" value="取消" class="input_button" onclick="art.dialog.close();">
      </td>
   </tr>
</table>      
</form>
<iframe name="operFrame" width="0" height="0"></iframe>
</body>
</html>