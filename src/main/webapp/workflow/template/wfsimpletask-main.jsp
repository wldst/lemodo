<%
/**
 *  Ԥ��������ģ����ͨ����༭����
 *  2007-03-27
 *  yangyn
 */
%>
<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../../tag.inc" %>
<%@ page import="java.util.List" %>


<%
Tsimple templateSimpleTask = (Tsimple)request.getAttribute("templateSimpleTask");
	List executorList = null;
	if (templateSimpleTask!=null){
	   executorList = templateSimpleTask.getTemplateTaskExecutorList();
	}
	TtaskExecutor executor = null;
	
	if (executorList != null && executorList.size() > 0){
	   executor = (TtaskExecutor)executorList.get(0);
	}
   String templateMark = request.getParameter("templateMark");
   String innerTaskID = request.getParameter("innerTaskID");
%>
<html>
<head>
<title>Ԥ��������ģ��༭</title>
<jsp:include flush="false"  page="../../../header.jsp"></jsp:include>

</head>
<script langauge="javascript">
    

   //������ͨ������Ϣ
	function saveSimpleTask() {
		// ���л���
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
				window.close();
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

<table width="100%" border="0" cellpadding="2" cellspacing="2"> 
   <tr height="25" class="listtablelist_head_bgbg">
      <td align="center" class="table_title_text_bold">
      ����ģ����ͨ����༭
      </td>
   </tr>
   </table>
 	  <div id="tt" class="easyui-tabs"   style="width:600px;height:360px;">  
        <div title="������Ϣ" style="padding:20px;" >  
			<table width="98%" cellpadding="2" cellspacing="2" class="addtable"
				align="center">
				<tr>
					<td class="label" nowrap width="15%">�������ƣ�</td>
					<td class="tdinput" nowrap width="35%"><input type="text"
						class="input_text" name="name" id="name"
						value="<%=templateSimpleTask.getWfTaskName()%>"></td>
				</tr>
				<tr>
					<td class="label" nowrap width="15%">�����ڲ�InnerID��</td>
					<td class="tdinput" nowrap width="35%"><input type="text"
						class="input_text" name="innerTaskID" id="innerTaskID"
						value="<%=templateSimpleTask.getWfTaskInnerID()%>"></td>
				</tr>
				<tr>
					<td class="label" nowrap width="15%">��������ڵ�InnerID��</td>
					<td class="tdinput" nowrap width="35%"><input type="text"
						class="input_text" name="next" id="next"
						value="<%=templateSimpleTask.getNext()%>"></td>
				</tr>
				<tr>
					<td class="label" nowrap width="15%">ǰ������ڵ�InnerID��</td>
					<td class="tdinput" nowrap width="35%"><input type="text"
						class="input_text" name="previous" id="previous"
						value="<%=templateSimpleTask.getPrevious()%>"></td>
				</tr>
				<tr>
					<td class="label" nowrap width="15%">����������</td>
					<td class="tdinput" nowrap width="35%"><textarea
						name="description" id="description" cols="40" rows="5"><%=templateSimpleTask.getDescript()%></textarea>
					</td>
				</tr>
			</table>        
		</div>  
        <div title="��������" style="padding:20px;">  
            <table width="98%" cellpadding="2" cellspacing="2" class="addtable" align="center">
			   <tr >
			      <td class="label" nowrap width="15%">�Ƿ���Ϊѭ����ʼ�ڵ㣺</td>
			      <td class="tdinput" nowrap width="35%">
			         <select name="<%=WFEConstants.TASK_PROPERTY_FOUR%>">
			            <option value="<%=WFEConstants.DB_BOOLEAN_FALSE%>" 
			               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FOUR) != null ? (TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FOUR).getValue())?"":"selected"):""%>>��</option>
			            <option value="<%=WFEConstants.DB_BOOLEAN_TRUE%>" 
			               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FOUR) != null ? (TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FOUR).getValue())?"selected":""):""%>>��</option>
			         </select>
			      </td>                         
			   </tr>
			   <tr >
			      <td class="label" nowrap width="15%">ѭ����������ڵ㣺</td>
			      <td class="tdinput" nowrap width="35%">
			         <input type="text" class="input_text" name="<%=WFEConstants.TASK_PROPERTY_ONE%>" 
			         id="<%=WFEConstants.TASK_PROPERTY_ONE%>" 
			         value="<%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_ONE) != null ? TextUtil.nvl(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_ONE).getValue()) : ""%>">
			      </td>                         
			   </tr>
			   <tr >
			      <td class="label" nowrap width="15%">����ִ���˱���ִ�У�</td>
			      <td class="tdinput" nowrap width="35%">
			        <select name="<%=WFEConstants.TASK_PROPERTY_TWO%>">
			            <option value="<%=WFEConstants.DB_BOOLEAN_FALSE%>" 
			               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TWO) != null ? (TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TWO).getValue())?"":"selected"):""%>>��</option>
			            <option value="<%=WFEConstants.DB_BOOLEAN_TRUE%>" 
			               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TWO) != null ? (TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TWO).getValue())?"selected":""):""%>>��</option>
			         </select>
			      </td>                         
			   </tr>
			   <tr >
			      <td class="label" nowrap width="15%">���Ļ���ִ���ˣ�</td>
			      <td class="tdinput" nowrap width="35%">
			         <input type="text" class="input_text" name="<%=WFEConstants.TASK_PROPERTY_THREE%>" 
			         id="<%=WFEConstants.TASK_PROPERTY_THREE%>" 
			         value="<%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_THREE)!=null?(TextUtil.nvl(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_THREE).getValue())):""%>">
			      </td>                         
			   </tr>
			   <tr >
			      <td class="label" nowrap width="15%">�����Ƿ��Զ�ִ�У�</td>
			      <td class="tdinput" nowrap width="35%">
			        <select name="<%=WFEConstants.TASK_PROPERTY_SIX%>">
			            <option value="<%=WFEConstants.DB_BOOLEAN_FALSE%>" 
			               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_SIX)!= null ? (TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_SIX).getValue())?"":"selected"):""%>>��</option>
			            <option value="<%=WFEConstants.DB_BOOLEAN_TRUE%>" 
			               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_SIX)!= null ? (TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_SIX).getValue())?"selected":""):""%>>��</option>
			         </select>
			      </td>                         
			   </tr>
			   <tr >
			      <td class="label" nowrap width="15%">�Ƿ�ȴ�������ִ�У�</td>
			      <td class="tdinput" nowrap width="35%">
			        <select name="<%=WFEConstants.TASK_PROPERTY_FIVE%>">
			            <option value="<%=WFEConstants.DB_BOOLEAN_FALSE%>" 
			               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FIVE)!=null?(TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FIVE).getValue())?"":"selected"):""%>>��</option>
			            <option value="<%=WFEConstants.DB_BOOLEAN_TRUE%>" 
			               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FIVE)!=null?(TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FIVE).getValue())?"selected":""):""%>>��</option>
			         </select>
			      </td>                         
			   </tr>
			   <tr >
			      <td class="label" nowrap width="15%">�����̴���class��</td>
			      <td class="tdinput" nowrap width="35%">
			         <input type="text" class="input_text" name="<%=WFEConstants.TASK_PROPERTY_SEVEN%>" 
			         id="<%=WFEConstants.TASK_PROPERTY_SEVEN%>" 
			         value="<%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_SEVEN)!=null?(TextUtil.nvl(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_SEVEN).getValue())):""%>">
			      </td>                         
			   </tr>
			   <tr >
			      <td class="label" nowrap width="15%">��������ʱ�Զ�����class��</td>
			      <td class="tdinput" nowrap width="35%">
			         <input type="text" class="input_text" 
			         name="<%=WFEConstants.TASK_PROPERTY_EIGHT%>" 
			         id="<%=WFEConstants.TASK_PROPERTY_EIGHT%>" 
			         value="<%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_EIGHT)!=null?(TextUtil.nvl(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_EIGHT).getValue())):""%>">
			      </td>                         
			   </tr>
			   <tr >
			      <td class="label" nowrap width="15%">ҵ�񴥷�URI��</td>
			      <td class="tdinput" nowrap width="35%">
			         <input type="text" class="input_text" 
			         name="<%=WFEConstants.TASK_PROPERTY_NINE%>" 
			         id="<%=WFEConstants.TASK_PROPERTY_NINE%>" 
			         value="<%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_NINE)!=null?(TextUtil.nvl(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_NINE).getValue())):""%>">
			      </td>                         
			   </tr>
			   <tr >
			      <td class="label" nowrap width="15%">ҵ�񴥷���ʽ��</td>
			      <td class="tdinput" nowrap width="35%">
			         <select name="<%=WFEConstants.TASK_PROPERTY_TEN%>">
			            <option value="<%=WFEConstants.BIZTYPE_PAGE%>" 
			               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TEN) != null?(NumberUtil.parseInt(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TEN).getValue(),0) == WFEConstants.BIZTYPE_PAGE?"selected":""):"" %>>
			               <%=WFEConstants.convertBizTypeZh(WFEConstants.BIZTYPE_PAGE)%></option>
			            <option value="<%=WFEConstants.BIZTYPE_POPUP%>"  
			               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TEN) != null?(NumberUtil.parseInt(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TEN).getValue(),0) == WFEConstants.BIZTYPE_POPUP?"selected":""):"" %>>
			               <%=WFEConstants.convertBizTypeZh(WFEConstants.BIZTYPE_POPUP)%></option>
			         </select>         
			      </td>                         
			   </tr>
			</table> 
        </div>  
        <div title="�������" style="padding:20px;">  
            <table width="100%" border="0" type="grid" 
			   cellpadding="0" cellspacing="0" name="list_details" 
			   id="list_details">
			   <tr height="22" align="center"  class="tablelist_head_bg"> 
			      <td nowrap class="tablelist_head_leftline" width="1%">
			         <input type="checkbox" name="allSelected" 
			            onclick="selectAllCheckBox(document.templateSimpleTaskEditForm,document.all.allSelected,'selectDecision');">
			      </td>
			      <td nowrap class="tablelist_head_middleandrightline" width="1%">���</td>
			      <td nowrap class="tablelist_head_middleandrightline" width="10%">������</td>
			      <td nowrap class="tablelist_head_middleandrightline" width="10%">���߱�ʶ</td>
			      <td nowrap class="tablelist_head_middleandrightline" width="10%">��ʾ��</td>
			      <td nowrap class="tablelist_head_middleandrightline" width="10%">ִ�з�ʽ</td>
			      <td nowrap class="tablelist_head_middleandrightline" width="10%">�����</td>
			   </tr>
			   
			   <%
			   			   // �ύ����
			   			   			      TtaskDecision performDecision = null;
			   			   			      if (templateSimpleTask!=null){
			   			   			         performDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_PERFORM);
			   			   			      }
			   			   %>
			   <tr align="center" style="cursor:hand">
			      <td class="tablelist_list_leftline">
			         <input type="checkbox" name="selectDecision" 
			            value="<%=WFEConstants.WFDECISION_PERFORM%>"
			            <%=performDecision != null?"checked":""%>>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         1
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_PERFORM)%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.WFDECISION_PERFORM%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_PERFORM%>_decisionViewName" 
			            value='<%=performDecision != null?performDecision.getViewName():""%>'>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <select name="<%=WFEConstants.WFDECISION_PERFORM%>_execType">
			            <option value="<%=WFEConstants.DECISION_EXEC_YESNO%>"
			               <%=performDecision!=null&&NumberUtil.parseInt(performDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_YESNO?"selected":""%>>
			               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%></option>
			            <option value="<%=WFEConstants.DECISION_EXEC_EMP%>"
			               <%=performDecision!=null&&NumberUtil.parseInt(performDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_EMP?"selected":""%>>
			               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_EMP)%></option>
			         </select>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_PERFORM%>_decisionOrderID" 
			            value='<%=performDecision != null?performDecision.getOrderID():""%>'>
			      </td>
			   </tr>
			   
			   <%
			   			   // ��ز�����
			   			   			      TtaskDecision turnbackDecision = null;
			   			   			      if (templateSimpleTask!=null){
			   			   			         turnbackDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_TURNBACK);
			   			   			      }
			   			   %>
			   <tr align="center" style="cursor:hand">
			      <td class="tablelist_list_leftline">
			         <input type="checkbox" name="selectDecision" 
			            value="<%=WFEConstants.WFDECISION_TURNBACK%>"
			            <%=turnbackDecision != null?"checked":""%>>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         2
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_TURNBACK)%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.WFDECISION_TURNBACK%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_TURNBACK%>_decisionViewName" 
			            value='<%=turnbackDecision != null? turnbackDecision.getViewName():""%>'>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <select name="<%=WFEConstants.WFDECISION_TURNBACK%>_execType">
			               <option value="<%=WFEConstants.DECISION_EXEC_YESNO%>"
				               <%=turnbackDecision!=null&&NumberUtil.parseInt(turnbackDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_YESNO?"selected":""%>>
				               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%></option>
				         <option value="<%=WFEConstants.DECISION_EXEC_EMP%>"
				               <%=turnbackDecision!=null&&NumberUtil.parseInt(turnbackDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_EMP?"selected":""%>>
				               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_EMP)%></option>
			         </select>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_TURNBACK%>_decisionOrderID" 
			            value='<%=turnbackDecision != null? turnbackDecision.getOrderID():""%>'>
			      </td>
			   </tr>
			   
			   <%
			   			   // ת�����
			   			   			      TtaskDecision forwardDecision =  null;
			   			   			      if (templateSimpleTask!=null){
			   			   			         forwardDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_FORWARD);
			   			   			      }
			   			   %>
			   <tr align="center" style="cursor:hand">
			      <td class="tablelist_list_leftline">
			         <input type="checkbox" name="selectDecision" 
			            value="<%=WFEConstants.WFDECISION_FORWARD%>"
			            <%=forwardDecision != null?"checked":""%>>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         3
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_FORWARD)%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.WFDECISION_FORWARD%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_FORWARD%>_decisionViewName" 
			            value='<%=forwardDecision != null? forwardDecision.getViewName():""%>'>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <select name="<%=WFEConstants.WFDECISION_FORWARD%>_execType">
			            <option value="<%=WFEConstants.DECISION_EXEC_YESNO%>"
			               <%=forwardDecision!=null&&NumberUtil.parseInt(forwardDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_YESNO?"selected":""%>>
			               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%></option>
			            <option value="<%=WFEConstants.DECISION_EXEC_EMP%>"
			               <%=forwardDecision!=null&&NumberUtil.parseInt(forwardDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_EMP?"selected":""%>>
			               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_EMP)%></option>
			         </select>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_FORWARD%>_decisionOrderID" 
			            value='<%=forwardDecision != null? forwardDecision.getOrderID():""%>'>
			      </td>
			   </tr>
			   
			   <%
			   			   // ѭ����ת����
			   			   			      TtaskDecision reloopDecision = null;
			   			   			      if (templateSimpleTask!=null){
			   			   			         reloopDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_RELOOP);
			   			   			      }
			   			   %>
			   <tr align="center" style="cursor:hand">
			      <td class="tablelist_list_leftline">
			         <input type="checkbox" name="selectDecision" 
			            value="<%=WFEConstants.WFDECISION_RELOOP%>"
			            <%=reloopDecision != null? "checked":""%>>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         4
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_RELOOP)%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.WFDECISION_RELOOP%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_RELOOP%>_decisionViewName" 
			            value='<%=reloopDecision != null? reloopDecision.getViewName():""%>'>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <select name="<%=WFEConstants.WFDECISION_RELOOP%>_execType">
			            <option value="<%=WFEConstants.DECISION_EXEC_YESNO%>"
			               <%=reloopDecision!=null&&NumberUtil.parseInt(reloopDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_YESNO?"selected":""%>>
			               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%></option>
			            <option value="<%=WFEConstants.DECISION_EXEC_EMP%>"
			               <%=reloopDecision!=null&&NumberUtil.parseInt(reloopDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_EMP?"selected":""%>>
			               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_EMP)%></option>
			         </select>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_RELOOP%>_decisionOrderID" 
			            value='<%=reloopDecision != null? reloopDecision.getOrderID():""%>'>
			      </td>
			   </tr>
			   
			   <%
			   			   // ���ղ���
			   			   			      TtaskDecision callbackDecision = null;
			   			   			      if (templateSimpleTask!=null){
			   			   			         callbackDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_CALLBACK);
			   			   			      }
			   			   %>
			   <tr align="center" style="cursor:hand">
			      <td class="tablelist_list_leftline">
			         <input type="checkbox" name="selectDecision" 
			            value="<%=WFEConstants.WFDECISION_CALLBACK%>"
			            <%=callbackDecision != null?"checked":""%>>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         5
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_CALLBACK)%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.WFDECISION_CALLBACK%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_CALLBACK%>_decisionViewName" 
			            value='<%=callbackDecision != null?callbackDecision.getViewName():""%>'>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			      	 <!-- modify aaron_ye at 2010-07-09 -->
			         <input type="input" style="width:80px" name="<%=WFEConstants.WFDECISION_CALLBACK%>_execType_readonly" value="<%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%>" disabled/>
			         <input type="hidden" name="<%=WFEConstants.WFDECISION_CALLBACK%>_execType" value="<%=WFEConstants.DECISION_EXEC_YESNO%>" />
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_CALLBACK%>_decisionOrderID" 
			            value='<%=callbackDecision != null?callbackDecision.getOrderID():""%>'>
			      </td>
			   </tr>
			   
			   
			   
			   <%
			   			   			   			   // ͬ�����
			   			   			   			   			      TtaskDecision agreeDecision = null;      
			   			   			   			   			      if (templateSimpleTask!=null){
			   			   			   			   			         agreeDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_AGREE);
			   			   			   			   			      }
			   			   			   			   %>
			   <tr align="center" style="cursor:hand">
			      <td class="tablelist_list_leftline">
			         <input type="checkbox" name="selectDecision" 
			            value="<%=WFEConstants.WFDECISION_AGREE%>"
			            <%=agreeDecision != null?"checked":""%>>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         6
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_AGREE)%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.WFDECISION_AGREE%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_AGREE%>_decisionViewName" 
			            value='<%=agreeDecision != null?agreeDecision.getViewName():""%>'>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			      	 <!-- modify aaron_ye at 2010-07-09 -->
			         <input type="input" style="width:80px" name="<%=WFEConstants.WFDECISION_AGREE%>_execType_readonly" value="<%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%>" disabled/>
			         <input type="hidden" name="<%=WFEConstants.WFDECISION_AGREE%>_execType" value="<%=WFEConstants.DECISION_EXEC_YESNO%>" />
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_AGREE%>_decisionOrderID" 
			            value='<%=agreeDecision != null?agreeDecision.getOrderID():""%>'>
			      </td>
			   </tr>
			   
			   <%
			   			   // ��ͬ��
			   			   			      TtaskDecision disagreeDecision = null;
			   			   			      if (templateSimpleTask!=null){
			   			   			         disagreeDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_DISAGREE);
			   			   			      }
			   			   %>
			   <tr align="center" style="cursor:hand">
			      <td class="tablelist_list_leftline">
			         <input type="checkbox" name="selectDecision" 
			            value="<%=WFEConstants.WFDECISION_DISAGREE%>"
			            <%=disagreeDecision != null?"checked":""%>>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         7
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_DISAGREE)%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <%=WFEConstants.WFDECISION_DISAGREE%>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_DISAGREE%>_decisionViewName" 
			            value='<%=disagreeDecision != null?disagreeDecision.getViewName():""%>'>
			      </td>
			      <td class="tablelist_list_middleandrightline">
			      	 <input type="input" style="width:80px" name="<%=WFEConstants.WFDECISION_DISAGREE%>_execType_readonly" value="<%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%>" disabled/>
			          <input type="hidden" name="<%=WFEConstants.WFDECISION_DISAGREE%>_execType" value="<%=WFEConstants.DECISION_EXEC_YESNO%>" />
			      </td>
			      <td class="tablelist_list_middleandrightline">
			         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_DISAGREE%>_decisionOrderID" 
			            value='<%=disagreeDecision != null?disagreeDecision.getOrderID():""%>'>
			      </td>
			   </tr>
			</table>
        </div>  
        <div title="ִ�����趨" style="padding:20px;">  
            <FIELDSET>
			<LEGEND>ִ���˹�ʽ��ϸ</LEGEND>
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
			   <tr>
			      <td>
			         <textarea name="executorEditScript" id="executorEditScript" cols="73" rows="20"><%=executor!=null?executor.getExpression():""%></textarea>
			      </td>
			   </tr>
			</table>
			</FIELDSET>
        </div>  
    </div>  
<table width="98%" cellpadding="2" cellspacing="2" class="addtable" align="center">
   <tr align="center">
      <td height="40" nowrap colspan="4" class="table_button_text">                   
         <input type="button" name="BTN_SAVE" id="BTN_SAVE" value="ȷ  ��" 
            class="main_button_style" onclick="saveSimpleTask();">
         <input type="button" name="BTN_CLOSE" id="BTN_CLOSE" value="��  ��" 
            class="main_button_style" onclick="window.close();">
      </td>
   </tr> 
</table> 
</form>   
<iframe name="operFrame" width="0" height="0"></iframe> 
</body>
</html>