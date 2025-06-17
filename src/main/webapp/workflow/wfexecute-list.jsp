
<%
	/**
	 *  流程实例履历一览画面
	 *  2007-03-27
	 *  yangyn
	 */
%>
<%@ page language="java" contentType="text/html; charset=GBK"
	pageEncoding="GBK"%>
<%@ include file="../../tag.inc"%>
<%@ page import="java.util.List"%>

<%
System.out.println("加载成果");
	String bizDataID = request.getParameter("bizDataID");
	String bizTabName = request.getParameter("bizTabName");
	String templateMark = request.getParameter("templateMark");

	BpmInstance workflow = (BpmInstance) request.getAttribute("workflow");
	SimpleTask currentTask = (SimpleTask) request
			.getAttribute("currentTask");
	String currEmpID = (String)request.getAttribute("currEmpID");
	String taskComeDatetime = (String)request.getAttribute("taskComeDatetime");

	List historyList = null;
	if (workflow != null) {
		historyList = workflow.getWfHistoryList();
	}

	Boolean performFlag = (Boolean) request.getAttribute("performFlag");
	Boolean callbackFlag = (Boolean) request
			.getAttribute("callbackFlag");
	Boolean turnbackFlag = (Boolean) request
			.getAttribute("turnbackFlag");
	Boolean forwardFlag = (Boolean) request.getAttribute("forwardFlag");
	Boolean reloopFlag = (Boolean) request.getAttribute("reloopFlag");
	Boolean agreeFlag = (Boolean) request.getAttribute("agreeFlag");
	Boolean disagreeFlag = (Boolean) request
			.getAttribute("disagreeFlag");

	String performName = TextUtil.nvl((String) request
			.getAttribute("performName"), "提交");
	String callbackName = TextUtil.nvl((String) request
			.getAttribute("callbackName"), "回收");
	String turnbackName = TextUtil.nvl((String) request
			.getAttribute("turnbackName"), "打回");
	String forwardName = TextUtil.nvl((String) request
			.getAttribute("forwardName"), "转办");
	String reloopName = TextUtil.nvl((String) request
			.getAttribute("reloopName"), "跳转");
	String agreeName = TextUtil.nvl((String) request
			.getAttribute("agreeName"), "同意");
	String disagreeName = TextUtil.nvl((String) request
			.getAttribute("disagreeName"), "不同意");
	String contextPath = TextUtil.nvl((String) request.getAttribute("contextPath"));
	System.out.println("pathStr:"+contextPath);
%>

<html>
<head>
<title>流程实例履历一览画面</title>
<jsp:include flush="false" page="../../header.jsp"></jsp:include>

<script type="text/javascript">
$(function() {
	mergeCellsByField("tcltasktab","taskName,taskStatus");
	mergeCellsByField("yspTaskTab","taskName");
});

</script>
</head>
<script language="javascript">
   //刷新
   function refreshPage(){
	   art.dialog.close();
	   /*
	  var url = '${contextPath}/wfHistoryForm/initExecute.so';
      document.wfHistoryListForm.action = url;
      document.wfHistoryListForm.target = "_self";
      document.wfHistoryListForm.submit();
	      */
   }


function fmtCause(value,rec,index){
	/*
	if((value!=null&&value.length<15)||value==""||value==null){
		if(value==null) {
			return  "<span style='color:black;' title=''></span>";
		} else {
			return  "<span style='color:black;' title='"+value+"'>"+value+"</span>";
		}
	} else {
		return  "<span style='color:black;' title='"+value+"'>"+value.substr(0,15)+"..."+"</span>";
	}
	*/
	return  "<textarea cols='32' rows='3' name='executeComment' style='word-wrap:break-word;background:transparent;border-style:none; ' readonly='true'>"+value+"</textarea>";
	
}

function disableControll(flag) {
	$('#performButton').attr({'disabled':flag});
	$('#agreeButton').attr({'disabled':flag});
	$('#disagreeButton').attr({'disabled':flag});
	$('#callbackButton').attr({'disabled':flag});
	$('#forwardButton').attr({'disabled':flag});
	$('#reloopButton').attr({'disabled':flag});
	$('#turnbackButton').attr({'disabled':flag});
}
</script>
<body>
<div id="p" class="easyui-panel" title="当前流程信息" style="height:170px; width: 700px;" data-options="tools:'#tt'">
<table id="tcltasktab" class="easyui-datagrid" data-options="nowrap:false">
				<thead> 
				<tr height="22" align="center" class="tablelist_head_bg">
					<th data-options="field:'taskName',width:120,align:'center',resizable:false">任务名</th>
					<th data-options="field:'taskStatus',width:60,align:'center',resizable:false">任务状态</th>
					<th data-options="field:'itemid2',width:60,align:'center',resizable:false">执行人</th>
					<th data-options="field:'itemid3',width:130,align:'center',resizable:false">到达时间</th>
					<th data-options="field:'itemid4',width:60,align:'center',resizable:false">执行状态</th>
					<th data-options="field:'itemid6',width:180,align:'center',resizable:false,formatter:fmtCause">执行意见</th>
				</tr>
				</thead>
				<tbody>
				<%
				if (currentTask != null) {
														List executorList = currentTask.getTaskExecutorList();
														BpmTaskExecute currentExecutor = null;
														if (executorList != null && executorList.size() > 0) {
															int count = executorList.size();
															for (int i = 0; i < count; i++) {
																currentExecutor = (BpmTaskExecute) executorList.get(i);
															String colorStr = "";
															if(currentExecutor.getExecutorStatus() == 0)
															{
																colorStr = "color='red'";
															}
															System.out.println("cc:"+colorStr);
				%>
				<tr align="center" style="cursor: hand">
					<td>
						<%=currentTask.getTaskName()%>
					</td>
					<td>
						<%=WFEConstants.convertWfTaskStatusZh(currentTask.getTaskStatus())%>
					</td>
					<%
						if (currentExecutor != null) {
					%>
					<td>
						<%=WfExecutorFactory.getExecutorNameByExecutorId(String.valueOf(currentExecutor.getExecutorID()))%>
					</td>
					<td>
						<%=DateUtil.formatDate(currentExecutor.getTaskComeDatetime())%>
					</td>
					<td>
						<font <%=colorStr%>><%=WFEConstants.convertUserExecStateZh(currentExecutor.getExecutorStatus())%></font>
					</td>
					<%
						} else {
					%>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<%
						}
					%>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<%
							}
						}
					} else {
				%>
				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<%
					}
				%>
				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
						
			</tbody>
			</table>
</div>

<div id="p1" class="easyui-panel" title="流程履历信息一览" style="width: 700px;height: 280px;overflow: hidden" >
        <table id="yspTaskTab" class="easyui-datagrid" fit="true">
			<thead>
			<tr>
				<th data-options="field:'itemid',width:40,align:'center',resizable:false">序号</th>
				<th data-options="field:'taskName',width:180,align:'center',resizable:false">历史任务名</th>
				<th data-options="field:'itemid2',width:70,align:'center',resizable:false">执行人</th>
				<th data-options="field:'itemid6',width:120,align:'center',resizable:false">办理时间</th>
				<th data-options="field:'itemid4',width:70,align:'center',resizable:false">执行动作</th>
				<th data-options="field:'itemid5',width:200,align:'center',resizable:false">执行意见</th>
			</tr>
			</thead>
			<tbody>
			<%
			if (workflow != null && historyList != null
																		&& historyList.size() > 0) {
																	int listSize = historyList.size();
																	for (int i = 0; i < listSize; i++) {
																		History history = (History) historyList.get(i);
																		BpmTask abTask = workflow.getWfTaskByID(history
																				.getWfTaskID());
			%>
			<tr align="center" >
				<td><%=i + 1%></td>
				<td><%=TextUtil.nvl(workflow.getWfTaskName(history.getWfTaskID()))%></td>
				<td><%=history.getHistoryCreateEmpName()%></td>
				<td><%=DateUtil.formatDate(history.getHistoryCreateDatetime())%></td>
				<td><%=history.getWfTaskDecisionNameZh()%></td>
				<td>
				<span style="color:black;" title="<%=TextUtil.nvl(history.getWfExecuteHistory())%>" >
				<% 
				 String str = TextUtil.nvl(history.getWfExecuteHistory());
				 int len = str.length();
				 int temp = 0;
				 if(len<=10){
					 out.print(str);
				 }else{
					 while(len>15){
						 int tempend = temp +15;
						 out.print(str.substring(temp,tempend)+"<br>");
						 len= len -15;
						 temp = tempend ;
					 }
					 out.print(str.substring(temp,len+temp)+"<br>");
				 }
				%>
				</span>
				</td>
		    </tr>
			<%
				}
				}
			%>
			</tbody>
		</table>
</div>

<div id="tt">
       <%
			if (performFlag != null && performFlag.booleanValue()) {
				String btnScript = "javascript: $('#search_div').attr({'disabled':true}); disableControll(true); if (wfdecision_popup_perform(";
				btnScript = btnScript + "'" + contextPath + "',";
				btnScript = btnScript + "'" + bizDataID + "',";
				btnScript = btnScript + "'" + currEmpID + "',";
				btnScript = btnScript + "'" + taskComeDatetime + "',";
				btnScript = btnScript + "'" + bizTabName + "',";
				btnScript = btnScript + "'" + templateMark + "')){";
				btnScript = btnScript + "refreshPage();";
				btnScript = btnScript + "}else{$('#search_div').attr({'disabled':false});}";
		%> <input id="performButton" name="performButton"
			class="input_btn" type="button" value="<%=performName%>"
			onclick="<%=btnScript%>" /> <%
 	}
 	if (agreeFlag != null && agreeFlag.booleanValue()) {
 		String btnScript = "javascript: $('#search_div').attr({'disabled':true}); disableControll(true); if (wfdecision_popup_agree(";
 		btnScript = btnScript + "'" + contextPath + "',";
 		btnScript = btnScript + "'" + bizDataID + "',";
 		btnScript = btnScript + "'" + currEmpID + "',";
 		btnScript = btnScript + "'" + taskComeDatetime + "',";
 		btnScript = btnScript + "'" + bizTabName + "',";
 		btnScript = btnScript + "'" + templateMark + "')){";
 		btnScript = btnScript + "refreshPage();";
 		btnScript = btnScript + "}else{$('#search_div').attr({'disabled':false});}";
 %> <input id="agreeButton" name="agreeButton" class="input_btn"
			type="button" value="<%=agreeName%>" onclick="<%=btnScript%>" /> <%
 	}
 	if (disagreeFlag != null && disagreeFlag.booleanValue()) {
 		String btnScript = "javascript: $('#search_div').attr({'disabled':true}); disableControll(true); if (wfdecision_popup_disagree(";
 		btnScript = btnScript + "'" + contextPath + "',";
 		btnScript = btnScript + "'" + bizDataID + "',";
 		btnScript = btnScript + "'" + currEmpID + "',";
 		btnScript = btnScript + "'" + taskComeDatetime + "',";
 		btnScript = btnScript + "'" + bizTabName + "',";
 		btnScript = btnScript + "'" + templateMark + "')){";
 		btnScript = btnScript + "refreshPage();";
 		btnScript = btnScript + "}else{$('#search_div').attr({'disabled':false});}";
 %> <input id="disagreeButton" name="disagreeButton"
			class="input_btn" type="button" value="<%=disagreeName%>"
			onclick="<%=btnScript%>" /> <%
 	}
 	if (callbackFlag != null && callbackFlag.booleanValue()) {
 		String btnScript = "javascript: wfdecision_popup_callback(";
 		btnScript = btnScript + "'" + contextPath + "',";
 		btnScript = btnScript + "'" + bizDataID + "',";
 		btnScript = btnScript + "'" + currEmpID + "',";
 		btnScript = btnScript + "'" + taskComeDatetime + "',";
 		btnScript = btnScript + "'" + bizTabName + "',";
 		btnScript = btnScript + "'" + templateMark + "');";
 		System.out.println(btnScript);
 %> <input id="callbackButton" name="callbackButton"
			class="input_btn" type="button" value="<%=callbackName%>"
			onclick="<%=btnScript%>" /> <%
 	}
 	if (forwardFlag != null && forwardFlag.booleanValue()) {
 		String btnScript = "javascript: $('#search_div').attr({'disabled':true}); disableControll(true); if (wfdecision_popup_forward(";
 		btnScript = btnScript + "'" + contextPath + "',";
 		btnScript = btnScript + "'" + bizDataID + "',";
 		btnScript = btnScript + "'" + currEmpID + "',";
 		btnScript = btnScript + "'" + taskComeDatetime + "',";
 		btnScript = btnScript + "'" + bizTabName + "',";
 		btnScript = btnScript + "'" + templateMark + "')){";
 		btnScript = btnScript + "refreshPage();";
 		btnScript = btnScript + "}else{$('#search_div').attr({'disabled':false});}";
 %> <input id="forwardButton" name="forwardButton"
			class="input_btn" type="button" value="<%=forwardName%>"
			onclick="<%=btnScript%>" /> <%
 	}
 	if (reloopFlag != null && reloopFlag.booleanValue()) {
 		String btnScript = "javascript: $('#search_div').attr({'disabled':true}); disableControll(true); if (wfdecision_popup_reloop(";
 		btnScript = btnScript + "'" + contextPath + "',";
 		btnScript = btnScript + "'" + bizDataID + "',";
 		btnScript = btnScript + "'" + currEmpID + "',";
 		btnScript = btnScript + "'" + taskComeDatetime + "',";
 		btnScript = btnScript + "'" + bizTabName + "',";
 		btnScript = btnScript + "'" + templateMark + "')){";
 		btnScript = btnScript + "refreshPage();";
 		btnScript = btnScript + "}else{$('#search_div').attr({'disabled':false});}";
 %> <input id="reloopButton" name="reloopButton"
			class="input_btn" type="button" value="<%=reloopName%>"
			onclick="<%=btnScript%>" /> <%
 	}
 	if (turnbackFlag != null && turnbackFlag.booleanValue()) {
 		String btnScript = "javascript: $('#search_div').attr({'disabled':true}); disableControll(true); if (wfdecision_popup_turnback(";
 		btnScript = btnScript + "'" + contextPath + "',";
 		btnScript = btnScript + "'" + bizDataID + "',";
 		btnScript = btnScript + "'" + currEmpID + "',";
 		btnScript = btnScript + "'" + taskComeDatetime + "',";
 		btnScript = btnScript + "'" + bizTabName + "',";
 		btnScript = btnScript + "'" + templateMark + "')){";
 		btnScript = btnScript + "refreshPage();";
 		btnScript = btnScript + "}else{$('#search_div').attr({'disabled':false});}";
 %> <input id="turnbackButton" name="turnbackButton"
			class="input_btn" type="button" value="<%=turnbackName%>"
			onclick="<%=btnScript%>" /> <%
 	}
 %> 
</div>

</body>

</html>