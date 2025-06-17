<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../../tag.inc" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK">
<title>Insert title here</title>
<jsp:include flush="false"  page="../../../header.jsp"></jsp:include>
<script type="text/javascript">
function gridEditFormatter(value, rec, index) {  
	return '<a href="javascript:void(0)" onclick="addEditTemplate('+index+');" >�༭</a> <a href="javascript:void(0)" onclick="editTemplate('+index+');" >ģ�����</a>  <a href="javascript:void(0)" onclick="editTemplate1('+index+');" >��������</a>';
} 

function gridWfInsCntFormatter(value, rec, index) {  
	return '<a href="javascript:void(0)" onclick="initWfInstanceList('+index+');" >��'+value+'��</a>';
} 

// datagrid�̶���
var frozenColumns = [ 
					  { field : 'opt', title : 'ģ��༭', align : 'center', width : 170, formatter : gridEditFormatter } 
					];

// datagrid�ǹ̶���
var columns = [
				{ field : 'TEMPLATENAME', title : '����ģ����', align : 'left', width : 300 },
				{ field : 'TEMPLATEMARK', title : '����ģ���ʶ', align : 'left', width : 300 },
				{ field : 'WFINS_CNT', title : '����ʵ����', align : 'right', width : 150, formatter : gridWfInsCntFormatter } ,
				{ field : 'TEMPLATEDESCRIPT', title : '��������', align : 'left', width : 200 }
			  ];

$(function() {
	// ��ѯ����
	var params = {};
	// ��ʼ����ѯ�б�
	initDataGrid('grid-workflow-list','${contextPath}/wfDesignForm/initWfTemplateList.so',params,frozenColumns,columns,false,'ID',false);

});


// ��ť�����ѯ
function query() {
	// ����datagrid��ѯ��������
	var queryParams = $('#grid-workflow-list').datagrid('options').queryParams;  
    queryParams.srchName = $('#srchName').val(); 
    queryParams.srchMark = $('#srchMark').val();  
	$("#grid-workflow-list").datagrid("reload");
}

//������������
function addTemplate(){
	var editUri = "${contextPath}/wfDesignForm/initWfTemplateAddPage.so?mode=new";
	popup_window({url:editUri,width:500,height:200,datagrid_id:"grid-workflow-list",data:window,model:true});
}

//�����༭��������
function addEditTemplate(_idx){
	var data = $("#grid-workflow-list").datagrid("getRows")[_idx];
	var templateMark = data.TEMPLATEMARK;
	var editUri = "${contextPath}/wfDesignForm/initWfTemplateAddPage.so?mode=edit&templateMark="+templateMark;
	popup_window({url:editUri,width:500,height:200,datagrid_id:"grid-workflow-list",data:window,model:true});
}
  

// �����༭����
function editTemplate(_idx) {
	var data = $("#grid-workflow-list").datagrid("getRows")[_idx];
	var templateMark = data.TEMPLATEMARK;
	
	var editUri = "${contextPath}/wfDesignForm/initWfTemplateEditPage.so?templateMark=" + templateMark+"&mode=edit";
	popup_window({url:editUri,width:1600,height:800,datagrid_id:"grid-workflow-list",data:window,model:true});
}
//������������
function editTemplate1(_idx) {
	var data = $("#grid-workflow-list").datagrid("getRows")[_idx];
	var templateMark = data.TEMPLATEMARK;
	
	 var editUri = "${contextPath}/wfTemplateForm/initTemplateDetail.so?templateMark=" + templateMark;
	 popup_window({url:editUri,width:1600,height:800,datagrid_id:"grid-workflow-list",data:window,model:true});
	      
 }

//��ʼ������ʵ��һ������
function initWfInstanceList(_idx){
   	var data = $("#grid-workflow-list").datagrid("getRows")[_idx];
	var templateMark = data.TEMPLATEMARK;
  	var listUri = "${contextPath}/wfMonitorForm/toWfInsList.so?wfTempMark=" + templateMark;
  	popup_window({url:listUri,width:1100,height:500,datagrid_id:"grid-workflow-list",data:window,model:true});
} 

function refreshAllWfTemplate() {
	if (confirm("�Ƿ�ˢ������ģ�建�棿")){
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
				 <LEGEND>��ѯ����</LEGEND>
					<table border="0" width="100%" cellpadding="0" cellspacing="0"  id="search_div" name="search_div">
						<tr>              
							<td width="8%" valign="center" class="label" >
								ģ�����ƣ�
							</td>
							<td width="17%" valign="center" class="tdinput" >
								<input type="text" class="input_text" id="srchName" name="srchName" 
								value=''>
							</td>
							<td width="8%" valign="center" class="label" >
								ģ���ʶ��
							</td>
							<td width="17%" valign="center" class="tdinput" >
								<input type="text" class="input_text" id="srchMark" name="srchMark" 
								value=''>
							</td>         
							<td width="8%" valign="center" class="tdinput" >
								<input id="btnAdd" class="input_btn" type="button" value="�½�����ģ��" onclick="addTemplate();"/>               
								<input id="btnQuery" class="input_btn" type="button" value="��ѯ" onclick="query();"/>               
								<input id="btnRefresh" class="input_btn" type="button" value="ˢ�����̻���" onclick="refreshAllWfTemplate();"/>               
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