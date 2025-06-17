<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../../tag.inc" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK">
<title>Insert title here</title>
<jsp:include flush="false"  page="../../../header.jsp"></jsp:include>

<script type="text/javascript">
	function gridEditFormatter(value, rec, index) {  
		return '<a href="javascript:void(0)" onclick="editTemplate('+index+');" >�༭</a>';
	} 

	function gridWfInsCntFormatter(value, rec, index) {  
		return '<a href="javascript:void(0)" onclick="initWfInstanceList('+index+');" >��'+value+'��</a>';
	} 
	
	// datagrid�̶���
	var frozenColumns = [ 
						  { field : 'ID', checkbox : true },
						  { field : 'opt', title : 'ģ��༭', align : 'center', width : 80, formatter : gridEditFormatter } 
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
		initDataGrid('grid-workflow-list','${contextPath}/wfTemplateForm/initTemplateList.so',params,frozenColumns,columns,false,'ID',false);

	});

		
	
	// ��ť�����ѯ
	function query() {
		// ����datagrid��ѯ��������
		var queryParams = $('#grid-workflow-list').datagrid('options').queryParams;  
        queryParams.srchName = $('#srchName').val(); 
        queryParams.srchMark = $('#srchMark').val();  

        // ���ݲ�ѯ�������¼���datagrid�������ص�һҳ����
		$("#grid-workflow-list").datagrid("reload");
	}



	// ������������
	function editTemplate(_idx) {
		var data = $("#grid-workflow-list").datagrid("getRows")[_idx];
		var templateMark = data.TEMPLATEMARK;
		
		 var editUri = "${contextPath}/wfTemplateForm/initTemplateDetail.so?templateMark=" + templateMark;
		 //popup_window({url:editUri,width:1100,height:500,datagrid_id:"grid-workflow-list",data:window,model:true});
		 window.open(editUri,"���̱༭","width=1100;height=500");     
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
							<td width="8%" valign="center" class="label" nowrap>
								ģ�����ƣ�
							</td>
							<td width="17%" valign="center" class="tdinput" nowrap>
								<input type="text" class="input_text" id="srchName" name="srchName" 
								value=''>
							</td>
							<td width="8%" valign="center" class="label" nowrap>
								ģ���ʶ��
							</td>
							<td width="17%" valign="center" class="tdinput" nowrap>
								<input type="text" class="input_text" id="srchMark" name="srchMark" 
								value=''>
							</td>         
							<td width="8%" valign="center" class="tdinput" nowrap>
							<input id="btnQuery" class="input_btn" type="button" value="��ѯ" onclick="query();"/>               
							</td>    
							<td width="8%" valign="center" class="tdinput" nowrap>
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
	