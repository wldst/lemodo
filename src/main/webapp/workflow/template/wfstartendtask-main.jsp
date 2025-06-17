<%
/**
 *  预定义流程模板普通任务编辑画面
 *  2007-03-27
 *  yangyn
 */
%>
<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../../tag.inc" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.List" %>

<%
TemplateTask abTempTask = (TemplateTask)request.getAttribute("abTempTask");
      
   String templateMark = request.getParameter("templateMark");
   String innerTaskID = request.getParameter("innerTaskID");
%>
<html>
<head>
<title>预定义流程模板编辑</title>
<jsp:include flush="false"  page="../../../header.jsp"></jsp:include>
</head>

<script type='text/javascript' src='${contextPath}/dwr/interface/WfDWRTemplateOperate.js'></script>  
<script type='text/javascript' src='${contextPath}/dwr/engine.js'></script>  
<script type='text/javascript' src='${contextPath}/dwr/util.js'></script>  

<script langauge="javascript">
  
    //保存任务信息
	function saveTask() {
		// 序列化表单
		var params=$("#templateSimpleTaskEditForm").serialize();
		$.ajax({
			url: '${contextPath}/wfTemplateForm/saveTaskPage.so', 
			type: 'POST', 
			data: params, 
			dataType: 'json', 
			beforeSend: function () {
				$("#BTN_SAVE").attr({"disabled":true});
			},
			success: function(result){
				alert(result.message);
			},
			complete: function(e, xhr, settings) {
				$("#BTN_SAVE").attr({"disabled":false});
			}
		});
	}
</script>
<body scroll="no">
<form name="templateSimpleTaskEditForm" id="templateSimpleTaskEditForm" method="POST">
<input type="hidden" name="templateMark" id="templateMark" value="<%=TextUtil.nvl(templateMark)%>">
<input type="hidden" name="innerTaskID" id="innerTaskID" value="<%=TextUtil.nvl(innerTaskID)%>">
<input type="hidden" name="outputTaskID" id="outputTaskID" value="">

<table width="100%" border="0" cellpadding="2" cellspacing="2"> 
   <tr height="25" class="listtablelist_head_bgbg">
      <td align="center" class="table_title_text_bold">
      流程模板分支任务编辑
      </td>
   </tr>
</table>
  <div id="tt" class="easyui-tabs"   style="width:700px;height:360px;">  
       <div title="基本信息" style="padding:20px;" >  
		<table width="98%" cellpadding="2" cellspacing="2" class="addtable" align="center">
		   <tr >
			  <td class="label" nowrap width="15%">任务名称：</td>
			  <td class="tdinput" nowrap width="35%">
				 <input type="text" class="input_text" name="name" id="name" 
					value="<%=abTempTask.getWfTaskName()%>">
			  </td>                         
		   </tr>
		   <tr >
			  <td class="label" nowrap width="15%">任务内部InnerID：</td>
			  <td class="tdinput" nowrap width="35%">
				 <input type="text" class="input_text" name="innerTaskID" id="innerTaskID" 
					value="<%=abTempTask.getWfTaskInnerID()%>">
			  </td>                         
		   </tr>
		   <%
			  if (abTempTask.getWfTaskType() == WFEConstants.WFTASK_TYPE_END){
		   %>
		   <tr >
			  <td class="label" nowrap width="15%">前续任务节点InnerID：</td>
			  <td class="tdinput" nowrap width="35%">
				 <input type="text" class="input_text" name="previous" id="previous"
					value="<%=((Tend)abTempTask).getPrevious()%>">
			  </td>                         
		   </tr>
		   <tr >
			  <td class="label" nowrap width="15%">匹配任务节点InnerID：</td>
			  <td class="tdinput" nowrap width="35%">
				 <input type="text" class="input_text" name="pairNode" id="pairNode"
					value="<%=((Tend)abTempTask).getPairNode()%>">
			  </td>                         
		   </tr>
		   <tr >
			  <td class="label" nowrap width="15%">任务描述：</td>
			  <td class="tdinput" nowrap width="35%">
				 <textarea name="description" id="description" cols="40" rows="5"><%=((Tend)abTempTask).getDescript()%></textarea>
			  </td>                         
		   </tr>
		   <%
			  }
		   %>
		   <%
			  if (abTempTask.getWfTaskType() == WFEConstants.WFTASK_TYPE_START){
		   %>
		   <tr >
			  <td class="label" nowrap width="15%">后续任务节点InnerID：</td>
			  <td class="tdinput" nowrap width="35%">
				 <input type="text" class="input_text" name="next" id="next" 
					value="<%=((Tstart)abTempTask).getNext()%>">
			  </td>                         
		   </tr>
		   <tr >
			  <td class="label" nowrap width="15%">匹配任务节点InnerID：</td>
			  <td class="tdinput" nowrap width="35%">
				 <input type="text" class="input_text" name="pairNode" id="pairNode"
					value="<%=((Tstart)abTempTask).getPairNode()%>">
			  </td>                         
		   </tr>
		   <tr >
			  <td class="label" nowrap width="15%">任务描述：</td>
			  <td class="tdinput" nowrap width="35%">
				 <textarea name="description" id="description" cols="40" rows="5"><%=((Tstart)abTempTask).getDescript()%></textarea>
			  </td>                         
		   </tr>
		   <%
			  }
		   %>
		  
		</table> 
	</div>  
</div>  
  
<table width="98%" cellpadding="2" cellspacing="2" class="addtable" align="center">
   <tr align="center">
      <td height="45" nowrap colspan="4" class="table_button_text">                
         <input type="button" name="BTN_SAVE" id="BTN_SAVE" value="确  定" 
            class="main_button_style" onclick="saveTask();">
         <input type="button" name="BTN_CLOSE" id="BTN_CLOSE" value="关  闭" 
            class="main_button_style" onclick="window.close();">
      </td>
   </tr> 
</table> 
</form>   
<iframe name="operFrame" width="0" height="0"></iframe> 
</body>
</html>