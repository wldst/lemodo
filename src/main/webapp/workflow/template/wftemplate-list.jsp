<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../../tag.inc" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK">
<title>Insert title here</title>
<jsp:include flush="false"  page="../../../header.jsp"></jsp:include>

<script type="text/javascript">
	function gridEditFormatter(value, rec, index) {  
		return '<a href="javascript:void(0)" onclick="editTemplate('+index+');" >编辑</a>';
	} 

	function gridWfInsCntFormatter(value, rec, index) {  
		return '<a href="javascript:void(0)" onclick="initWfInstanceList('+index+');" >【'+value+'】</a>';
	} 
	
	// datagrid固定列
	var frozenColumns = [ 
						  { field : 'ID', checkbox : true },
						  { field : 'opt', title : '模板编辑', align : 'center', width : 80, formatter : gridEditFormatter } 
						];

	// datagrid非固定列
	var columns = [
					{ field : 'TEMPLATENAME', title : '流程模板名', align : 'left', width : 300 },
					{ field : 'TEMPLATEMARK', title : '流程模板标识', align : 'left', width : 300 },
					{ field : 'WFINS_CNT', title : '流程实例数', align : 'right', width : 150, formatter : gridWfInsCntFormatter } ,
					{ field : 'TEMPLATEDESCRIPT', title : '流程描述', align : 'left', width : 200 }
				  ];

	$(function() {
		// 查询参数
		var params = {};
		// 初始化查询列表
		initDataGrid('grid-workflow-list','${contextPath}/wfTemplateForm/initTemplateList.so',params,frozenColumns,columns,false,'ID',false);

	});

		
	
	// 按钮点击查询
	function query() {
		// 设置datagrid查询条件参数
		var queryParams = $('#grid-workflow-list').datagrid('options').queryParams;  
        queryParams.srchName = $('#srchName').val(); 
        queryParams.srchMark = $('#srchMark').val();  

        // 根据查询条件重新加载datagrid，并返回第一页数据
		$("#grid-workflow-list").datagrid("reload");
	}



	// 弹出新增界面
	function editTemplate(_idx) {
		var data = $("#grid-workflow-list").datagrid("getRows")[_idx];
		var templateMark = data.TEMPLATEMARK;
		
		 var editUri = "${contextPath}/wfTemplateForm/initTemplateDetail.so?templateMark=" + templateMark;
		 //popup_window({url:editUri,width:1100,height:500,datagrid_id:"grid-workflow-list",data:window,model:true});
		 window.open(editUri,"流程编辑","width=1100;height=500");     
	 }


   //初始化流程实例一栏画面
   function initWfInstanceList(_idx){
	   var data = $("#grid-workflow-list").datagrid("getRows")[_idx];
		var templateMark = data.TEMPLATEMARK;
     var listUri = "${contextPath}/wfMonitorForm/toWfInsList.so?wfTempMark=" + templateMark;
     popup_window({url:listUri,width:1100,height:500,datagrid_id:"grid-workflow-list",data:window,model:true});
   }  


   	function refreshAllWfTemplate() {
		 if (confirm("是否刷新流程模板缓存？")){
		$.ajax({
			url: '${contextPath}/wfTemplateForm/refreshAllWfTemplate.so', 
			type: 'POST', 
			data: '', 
			dataType: 'json', 
			success: function(result){
				alert(result.message);
				window.close();
			}
		});
		 }
	}
 

</script>
</head>
<body>
	<div class="easyui-layout" fit="true" style="width:100%;">
		<div region="north" border="false" split="false" style="overflow: hidden; border-bottom-width:thin;width: 100%;"> 
			<FIELDSET>
				 <LEGEND>查询条件</LEGEND>
					<table border="0" width="100%" cellpadding="0" cellspacing="0"  id="search_div" name="search_div">
						<tr>              
							<td width="8%" valign="center" class="label" nowrap>
								模板名称：
							</td>
							<td width="17%" valign="center" class="tdinput" nowrap>
								<input type="text" class="input_text" id="srchName" name="srchName" 
								value=''>
							</td>
							<td width="8%" valign="center" class="label" nowrap>
								模板标识：
							</td>
							<td width="17%" valign="center" class="tdinput" nowrap>
								<input type="text" class="input_text" id="srchMark" name="srchMark" 
								value=''>
							</td>         
							<td width="8%" valign="center" class="tdinput" nowrap>
							<input id="btnQuery" class="input_btn" type="button" value="查询" onclick="query();"/>               
							</td>    
							<td width="8%" valign="center" class="tdinput" nowrap>
							<input id="btnRefresh" class="input_btn" type="button" value="刷新流程缓存" onclick="refreshAllWfTemplate();"/>               
							</td>    
						</tr>
					</table>
			  </FIELDSET>				    
		</div>
		<div region="center" border="false">
			<table id="grid-workflow-list" fit="true"></table>
		</div>
	</div>


</body>
</html>
	