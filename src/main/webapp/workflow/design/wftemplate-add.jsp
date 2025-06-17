<%@ page language="java" contentType="text/html; charset=GBK"
	pageEncoding="GBK"%>
<%@ include file="../../../tag.inc"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK">
<title>新增流程模版</title>
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
			submitAfterAjaxPrompt : '有数据正在异步验证，请稍等...'
		});
	$("#templateMark").formValidator().inputValidator({min:1,max:64,onError:"流程模版标识不能为空,最大长度不超过64"}).regexValidator({regExp:"^[a-zA-Z_]{1,}$",onError:"只能输入字母a-z或A-Z或下划线"});
	$("#templateName").formValidator().inputValidator({min:1,max:60,onError:"流程模版名称不能为空,最大长度不超过60"});
	$("#templateDescript").formValidator({empty:true}).inputValidator({max:200,onError:"流程模版描述最大长度不超过200"});

	if($("#mode").val() == "edit"){
		$("#templateMark").attr("readonly","readonly")
	}
	
});

//保存数据
function saveAdd(){
	if ($.formValidator.pageIsValid("1")){
		// 序列化表单
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
			    if (result.success) { // 操作成功
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
		<div class="list_head_left_fontbg">流程模版信息</div>
		<div class="list_head_middle_space"></div>
		<div class="list_head_right">
			<input id="btnSave" class="input_btn" name="btnSave" type="button" value="保存" onclick="saveAdd();" /> 
			<input id="btnClose" class="input_btn" name="btnClose" type="button" value="关闭" onclick="window.close();" />
		</div>
	</div>
	<input type="hidden" id="mode" name="mode" value="${mode}">
	<input type="hidden" id="ID" name="ID" value="${templateObj.ID}">
	<input type="hidden" id="NEWESTVERFLAG" name="NEWESTVERFLAG" value="${templateObj.NEWESTVERFLAG}">
	<table border="0" cellpadding="0" cellspacing="0" class="addtable" style="width: 99%" >
		<tr>
			<td style="width: 20%" valign="top" class="label">模版标识：</td>
			<td valign="top" class="tdinput">
				<input type="text" id="templateMark" name="templateMark" value="${templateObj.TEMPLATEMARK}" style="width: 200px;"><font color="red">*</font>
			</td>
		</tr>
		<tr>
			<td valign="top" class="label">模版名称：</td>
			<td valign="top" class="tdinput">
				<input type="text" id="templateName" name="templateName" value="${templateObj.TEMPLATENAME}" style="width: 200px;"><font color="red">*</font>
			</td>
		</tr>
		<tr>
			<td valign="top" class="label">模版描述：</td>
			<td valign="top" class="tdinput">
				<textarea id="templateDescript" name="templateDescript" rows="5" cols="50">${templateObj.TEMPLATEDESCRIPT}</textarea>
			</td>
		</tr>
	</table>
</form>
</body>
</html>
