<%
/**
 *  ����ʵ��һ������
 *  2006-02-28
 *  yangyn
 */
%>
<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../../tag.inc" %>
<%@ page import="java.util.List" %>

<%
   List wfInstanceList = (List)request.getAttribute("wfInstanceList");
   String wfTempMark = request.getParameter("wfTempMark");
   String srcBizDataID = (String)request.getAttribute("srcBizDataID");
   String srchWfStatus = (String)request.getAttribute("srchWfStatus");
   String createEmployeeID = (String)request.getAttribute("createEmployeeID");      
%>
<html>
<head>
<title>����ʵ��һ������</title>
<jsp:include flush="false"  page="../../../header.jsp"></jsp:include>
</head>

<script language="javascript">

function gridDateFormatter(value, rec, index) {
	//alert(1315377877109);
	//alert(getSmpFormatDateByLong(new Date(parseFloat(value))));
		return getSmpFormatDateByLong(new Date(parseFloat(value)));
	} 

// datagrid�̶���
	var frozenColumns = [{ field : 'ID', checkbox : true }];

	// datagrid�ǹ̶���
	var columns = [	
					{ field : 'WORKFLOWNAME', title : '��������', align : 'center', width : 150 },
					{ field : 'TASKNAME', title : '��ǰ����', align : 'center', width : 80 },
					{ field : 'WFSTATUS', title : '����״̬', align : 'center', width : 80 } ,
					{ field : 'TEMPLATEMARK', title : '��Ӧ����ģ��', align : 'center', width : 200 },
					{ field : 'EMPNAME', title : '������', align : 'center', width : 60 },
					{ field : 'WFCREATEDATETIME', title : '����ʱ��', align : 'center', width : 100 , formatter : gridDateFormatter},
					{ field : 'TRIGGERSUBWFFLAG', title : '�Ƿ�������', align : 'center', width : 100 },
					{ field : 'TEMPLATEMARK', title : '��������', align : 'center', width : 300 }
				  ];


	$(function() {
		// ��ѯ����
		var params = {wfTempMark:"<%=wfTempMark%>"};
		// ��ʼ����ѯ�б�
		initDataGrid('grid-wfins-list','${contextPath}/wfMonitorForm/initInstanceList.so',params,frozenColumns,columns,false,'ID');

	});

	// ��ť�����ѯ
	function query() {
		// ����datagrid��ѯ��������
		var queryParams = $('#grid-wfins-list').datagrid('options').queryParams;  
        queryParams.wfTempMark = $('#wfTempMark').val(); 
        queryParams.srchWfStatus = $('#srchWfStatus').val();  
		 queryParams.createEmployeeID = $('#createEmployeeID').val();  

        // ���ݲ�ѯ�������¼���datagrid�������ص�һҳ����
		$("#grid-wfins-list").datagrid("reload");
	}
		
</script>
<body scroll="no">
<input type="hidden" id="flagName" name="flagName" value="0">   
<input type="hidden" name="wfTempMark" id="wfTempMark" value="<%=TextUtil.nvl(wfTempMark)%>">
<div class="easyui-layout" fit="true" style="width:100%;">
	<div region="north" border="false" split="false" > 
		<FIELDSET>
		  <LEGEND>��ѯ����</LEGEND>
		  <table border="0" width="100%" cellpadding="0" cellspacing="0" class="addtable" id="search_div" name="search_div">
			 <tr>              
				<td width="8%" valign="top" class="label" nowrap>
				   ����ҵ��ID��
				</td>
				<td width="17%" valign="top" class="tdinput" nowrap>
				   <input type="text" class="input_text" id="srcBizDataID" name="srcBizDataID" 
					  value='<%=TextUtil.nvl(srcBizDataID)%>'>
				</td>
				<td width="8%" valign="top" class="label" nowrap>
				   ����״̬��
				</td>
				<td width="10%" valign="top" class="tdinput" nowrap>
				   <%
					  int srchWfStatusInt = NumberUtil.parseInt(srchWfStatus,0);
				   %>
				   <select name="srchWfStatus" id="srchWfStatus" style="width:100px">
					  <option value="-1">&nbsp;</option>
					  <option value="<%=WFEConstants.WFSTATUS_INIT%>" <%=srchWfStatusInt==WFEConstants.WFSTATUS_INIT?"selected":""%>><%=WFEConstants.convertWfStatusZh(WFEConstants.WFSTATUS_INIT)%></option>
					  <option value="<%=WFEConstants.WFSTATUS_RUN%>" <%=srchWfStatusInt==WFEConstants.WFSTATUS_RUN?"selected":""%>><%=WFEConstants.convertWfStatusZh(WFEConstants.WFSTATUS_RUN)%></option>
					  <option value="<%=WFEConstants.WFSTATUS_PAUSE%>" <%=srchWfStatusInt==WFEConstants.WFSTATUS_PAUSE?"selected":""%>><%=WFEConstants.convertWfStatusZh(WFEConstants.WFSTATUS_PAUSE)%></option>
					  <option value="<%=WFEConstants.WFSTATUS_TERMINATE%>" <%=srchWfStatusInt==WFEConstants.WFSTATUS_TERMINATE?"selected":""%>><%=WFEConstants.convertWfStatusZh(WFEConstants.WFSTATUS_TERMINATE)%></option>
					  <option value="<%=WFEConstants.WFSTATUS_SUSPEND%>" <%=srchWfStatusInt==WFEConstants.WFSTATUS_SUSPEND?"selected":""%>><%=WFEConstants.convertWfStatusZh(WFEConstants.WFSTATUS_SUSPEND)%></option>
					  <option value="<%=WFEConstants.WFSTATUS_END%>" <%=srchWfStatusInt==WFEConstants.WFSTATUS_END?"selected":""%>><%=WFEConstants.convertWfStatusZh(WFEConstants.WFSTATUS_END)%></option>
				   </select>
				</td>
				<td width="8%" valign="top" class="label" nowrap>
				   �����ˣ�
				</td>
				<td width="17%" valign="top" class="tdinput" nowrap>
				   <pmis:employee valueName="createEmployeeID" displayName="createEmployeeName" multiple="false"></pmis:employee>
				</td>        
				<td width="10%" align="center" class="tdinput" nowrap>
					<input id="btnQuery"  type="button" value="��ѯ" onclick="query();"/>               
				</td>    
			 </tr>
		  </table>
		</FIELDSET>  
	</div>
	<div region="center" border="false">
		<table id="grid-wfins-list" fit="true"></table>
	</div>
</div>
</body>
</html>