<%@ page language="java" contentType="text/html; charset=GBK"
	pageEncoding="GBK"%>
<%@ include file="../../../tag.inc"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK">
<title>��������ģ��</title>
<jsp:include flush="true" page="../../../header.jsp"></jsp:include>
<script type="text/javascript">
var obj = art.dialog.data('data');
$(function(){
	$.formValidator.initConfig({
		formID:"templateForm",
		theme:'ArrowSolidBox',
		inIframe:true,
		onError:function(msg,obj,errorlist){
              alert(msg);
        },
			submitAfterAjaxPrompt : '�����������첽��֤�����Ե�...'
		});
	$("#templateMark").formValidator().inputValidator({min:1,max:64,onError:"����ģ���ʶ����Ϊ��,��󳤶Ȳ�����64"}).regexValidator({regExp:"^[a-zA-Z_]{1,}$",onError:"ֻ��������ĸa-z��A-Z���»���"});
	$("#templateName").formValidator().inputValidator({min:1,max:60,onError:"����ģ�����Ʋ���Ϊ��,��󳤶Ȳ�����60"});
	$("#templateDescript").formValidator({empty:true}).inputValidator({max:200,onError:"����ģ��������󳤶Ȳ�����200"});

	if($("#mode").val() == "edit"){
		$("#templateMark").attr("readonly","readonly")
	}
	
});

//��������
function saveAdd(){
	if ($.formValidator.pageIsValid("1")){
		// ���л���
		var params=$("#templateForm").serialize();
        $.ajax({
	        url: '${contextPath}/wfDesignForm/saveWfTemplateMainInfo.so', 
        	type: 'POST', 
        	data: params, 
        	dataType: 'json', 
        	beforeSend: function () {
				$("#btnSave").attr({"disabled":true});
	        },
        	success: function(result){
			    alert(result.message);
			    if (result.success) { // �����ɹ�
			    	obj.query();
			    	window.close();
			    } 
        	},
        	complete: function(e, xhr, settings) {
				$("#btnSave").attr({"disabled":false});
        	}
        });
	}
}


</script>
</head>
<body>
<form action="" method="post" id="templateForm" name="templateForm">
	<div class="list_head_mainbg">
		<div class="list_head_left"></div>
		<div class="list_head_left_fontbg">����ģ����Ϣ</div>
		<div class="list_head_middle_space"></div>
		<div class="list_head_right">
			<input id="btnSave" class="input_btn" name="btnSave" type="button" value="����" onclick="saveAdd();" /> 
			<input id="btnClose" class="input_btn" name="btnClose" type="button" value="�ر�" onclick="window.close();" />
		</div>
	</div>
	<input type="hidden" id="mode" name="mode" value="${mode}">
	<input type="hidden" id="ID" name="ID" value="${templateObj.ID}">
	<input type="hidden" id="NEWESTVERFLAG" name="NEWESTVERFLAG" value="${templateObj.NEWESTVERFLAG}">
	<table border="0" cellpadding="0" cellspacing="0" class="addtable" style="width: 99%" >
		<tr>
			<td style="width: 20%" valign="top" class="label">ģ���ʶ��</td>
			<td valign="top" class="tdinput">
				<input type="text" id="templateMark" name="templateMark" value="${templateObj.TEMPLATEMARK}" style="width: 200px;"><font color="red">*</font>
			</td>
		</tr>
		<tr>
			<td valign="top" class="label">ģ�����ƣ�</td>
			<td valign="top" class="tdinput">
				<input type="text" id="templateName" name="templateName" value="${templateObj.TEMPLATENAME}" style="width: 200px;"><font color="red">*</font>
			</td>
		</tr>
		<tr>
			<td valign="top" class="label">ģ��������</td>
			<td valign="top" class="tdinput">
				<textarea id="templateDescript" name="templateDescript" rows="5" cols="50">${templateObj.TEMPLATEDESCRIPT}</textarea>
			</td>
		</tr>
	</table>
</form>
</body>
</html>
