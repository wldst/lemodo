<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%
	// 跟模块isc的上下文路径（/isc)
	String rootPath = request.getContextPath();
%>
<%@ include file="../../tag.inc" %>

<%@page import="java.util.List"%>
<%@page import="java.util.Map"%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK">
<title>待办事项</title>

<link href="../../pmis-common/style/wf-do.css" rel="stylesheet" type="text/css">
<jsp:include flush="false"  page="../../header.jsp"></jsp:include>
<script src="../../pmis-common/jscript/workflow/workflow-do.js" type="text/javascript"></script>

<script language="javascript"> 
window.onunload=function() {
	try {
		if (window.dialogArguments && window.dialogArguments.dialogParentWindow) {
			window.dialogArguments.dialogParentWindow.displayWFDoCount();
		}
	} catch(e){}
}

$(function() {
	initLeftList();
});

function reflushPage(){
	$.messager.loading('加载中， 请稍后...');
	initLeftList();
	$.messager.loaded(100);
}

function initLeftList() {
	var url = '${contextPath}/wfDoForm/queryWorkflowDoList.so';
	 $.ajax({ url: url, type: 'POST', data: {}, dataType: 'json', 
       	beforeSend: function () {},
       	success: function(result){
    		$('#button-wrapper').empty();
        	if (result && result.length > 0){
            	$(result).each(function(idx,e){
            		var url = getWFurl(e.TEMPLATEMARK);
            		if (url){
            			var spanCom = getSpanCom2(e.TEMPLATEMARK,e.TEMPLATENAME,e.WFCOUNT);
    					$(spanCom).appendTo($('#button-wrapper'));
            		}
                	});
        	} else {
        		var spanCom = getSpanCom2('','暂无待办事项','0');
        		$(spanCom).appendTo($('#button-wrapper'));
        	}
       	},
       	complete: function(e, xhr, settings) {}
       });
}

function getSpanCom2(tempateMark,templatename,wfcount) {
	var funcClick = "";
	if (wfcount > 0) funcClick = "showTab(this)";
	return '<div href="#" class="a-btn" style="cursor: hand;" onclick="'+funcClick+'" '
		+ ' id="'+tempateMark+'" title="'+templatename+'">'
		+ ' <span class="a-btn-text">'+templatename+'</span> '
		+ ' <span class="a-btn-slide-text">待办项：'+wfcount+'个</span>'
		+ ' </div>';
}
function showTab(obj) {
	showWFundoPanel("divWFundo","${contextPath}", obj.title, obj.id);
}

</script>
</head>
<body>
		<div class="easyui-layout" fit="true">
			<div region="west" border="false" style="width: 275px;overflow-y: scroll;" split="true" 
				data-options="tools:[{ iconCls:'icon-reload',handler:reflushPage }]" 
				title="流程待办事项">
				<div class="container">
		            <div class="content">
						<div class="button-wrapper" id="button-wrapper"></div>
		            </div>
		        </div>
			</div>
			<div region="center" border="false">
				<div class="easyui-tabs" fit="true" id="divWFundo">   
			    </div>
			</div>
		</div>
</body>
</html>