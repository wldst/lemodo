<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../../tag.inc" %>


<%
   	String templateMark = request.getParameter("templateMark");   
   	String model = (String)request.getAttribute("mode");
   
%>
<html xmlns:v="urn:schemas-microsoft-com:vml">
<head>
  <title>动态创建VML</title>
<jsp:include flush="false"  page="../../../header.jsp"></jsp:include>
</head>
<style type="text/css">
v\:*{BEHAVIOR:url(#default#VML)}
body{margin:0,0,0,0;}
#group{position:absolute;top:0px;left:0px;height:100%;width:100%;z-index:-1000;}
#menu table{font-size:11px;background-image:url(../../pmis-common/images/wfdesign/menubg.gif);z-index:1000;height:100%;}
#menu td{white-space:nowrap;}
#movebar{font-size:11px;z-index:1000;cursor:move;height:30;width:8;}
#movebar div{height:30;width:8;background-image:url(../../pmis-common/images/wfdesign/grid-blue-split.gif);}
#menu img{vertical-align:middle;height:16px;}
#menu span{vertical-align:bottom;cursor:hand;padding-left:3px;padding-top:5px;padding-bottom:2px;padding-right:5;}
.x-window{}
.x-window-tc{background:transparent url(../../pmis-common/images/wfdesign/window/top-bottom.png) repeat-x 0 0;overflow:hidden;zoom:1;height:25px;padding-top:5px;font-size:12px;cursor:move;}
.x-window-tl{background:transparent url(../../pmis-common/images/wfdesign/window/left-corners.png) no-repeat 0 0;padding-left:6px;zoom:1;z-index:1;position:relative;}
.x-window-tr{background:transparent url(../../pmis-common/images/wfdesign/window/right-corners.png) no-repeat right 0;padding-right:6px;}
.x-window-bc{background:transparent url(../../pmis-common/images/wfdesign/window/top-bottom.png) repeat-x 0 bottom;zoom:1;text-align:center;height:30px;padding-top:4px;}
.x-window-bl{background:transparent url(../../pmis-common/images/wfdesign/window/left-corners.png) no-repeat 0 bottom;padding-left:6px;zoom:1;}
.x-window-br{background:transparent url(../../pmis-common/images/wfdesign/window/right-corners.png) no-repeat right bottom;padding-right:6px;zoom:1;}
.x-window-mc{border:1px solid #99bbe8;padding:0;margin:0;font:normal 11px tahoma,arial,helvetica,sans-serif;background:#dfe8f6;width:384px;height:231px;}
.x-window-ml{background:transparent url(../../pmis-common/images/wfdesign/window/left-right.png) repeat-y 0 0;padding-left:6px;zoom:1;}
.x-window-mr{background:transparent url(../../pmis-common/images/wfdesign/window/left-right.png) repeat-y right 0;padding-right:6px;zoom:1;}
.x-tool-close{overflow:hidden;width:15px;height:15px;float:right;cursor:pointer;background:transparent url(../../pmis-common/images/wfdesign/window/tool-sprites.gif) no-repeat;background-position:0 -0;margin-left:2px;}
.x-tab-panel-header{border:1px solid #8db2e3;padding-bottom:2px;border-top-width:0;border-left-width:0;border-right-width:0;height:26px;}
.x-tab-panel-body{overflow-y:auto;height:200px;display:none;}
.x-tab-panel-body-show{overflow-y:auto;height:200px;padding-top:3px;}
.x-tab-strip-top{float:left;width:100%;height:27px;padding-top:1px;background:url(../../pmis-common/images/wfdesign/window/tab-strip-bg.gif) #cedff5 repeat-x top;}
.x-tab-strip-wrap{width:100%;border-bottom:1px solid #8db2e3;}
.x-tab-strip-top ul{list-style:none;margin:0px;padding:0px;}
.x-tab-strip-top li{float:left;display:block;width:60px;padding-left:3px;text-align:center;font-size:11px;}
.x-tab-strip-top
.x-tab-left{background:transparent url(../../pmis-common/images/wfdesign/window/tabs-sprite.gif) no-repeat 0 -51px;padding-left:10px;}
.x-tab-strip-top
.x-tab-right{background:transparent url(../../pmis-common/images/wfdesign/window/tabs-sprite.gif) no-repeat right -351px;padding-right:10px;}
.x-tab-strip-top
.x-tab-middle{background:transparent url(../../pmis-common/images/wfdesign/window/tabs-sprite.gif) repeat-x 0 -201px;height:25px;overflow:hidden;padding-top:5px;cursor:hand;}
.x-tab-strip-top
.x-tab-strip-active
.x-tab-left{background:transparent url(../../pmis-common/images/wfdesign/window/tabs-sprite.gif) no-repeat 0 0px;padding-left:10px;}
.x-tab-strip-top
.x-tab-strip-active
.x-tab-right{background:transparent url(../../pmis-common/images/wfdesign/window/tabs-sprite.gif) no-repeat right -301px;padding-right:10px;}
.x-tab-strip-top
.x-tab-strip-active
.x-tab-middle{background:transparent url(../../pmis-common/images/wfdesign/window/tabs-sprite.gif) repeat-x 0 -151px;height:25px;overflow:hidden;padding-top:5px;font-weight:bold;cursor:hand;}
.x-form-field{font-family:tahoma,arial,helvetica,sans-serif;font-size:12px;font-size-adjust:none;font-stretch:normal;font-style:normal;font-variant:normal;font-weight:normal;line-height:normal;margin:0;background:#FFFFFF url(../../pmis-common/images/wfdesign/default/form/text-bg.gif) repeat-x scroll 0 0;border:1px solid #B5B8C8;padding:1px 3px;width:200;overflow:hidden;}
.btn{BORDER-RIGHT:#7b9ebd 1px solid;PADDING-RIGHT:2px;BORDER-TOP:#7b9ebd 1px solid;PADDING-LEFT:2px;FONT-SIZE:12px;FILTER:progid:DXImageTransform.Microsoft.Gradient(GradientType=0,StartColorStr=#ffffff,EndColorStr=#cecfde);BORDER-LEFT:#7b9ebd 1px solid;CURSOR:hand;COLOR:black;PADDING-TOP:2px;BORDER-BOTTOM:#7b9ebd 1px solid}
</style>

<script language="javascript" src="../../pmis-common/jscript/workflow/agdev-wf-design.js"></script>
<script language="javascript" src="../../pmis-common/jscript/workflow/agdev-util.js"></script>

<script type="text/javascript">
//弹出任务编辑画面
function popup_task_page(flowObj){
   if (flowObj != null){
      var type = flowObj.type;
      switch(type){
         case "start":
            doStartTask(flowObj);
            break;
         case "end":
            doEndTask(flowObj);
            break;
         case "node":
            doSimpleTask(flowObj);
            break;
         case "Branch":
            doBranchTask(flowObj);
            break;
         case "Shrink":
            doShrinkTask(flowObj);
            break;
      }
   }
}

//处理开始任务节点
function doStartTask(startObj){
   	var feacture = "dialogWidth:" + 550 + "px; dialogHeight:" + 500 + "px;";
 	feacture = feacture + "directories:no; localtion:no; menubar:no; status:no;";
 	feacture = feacture + "toolbar:no; scroll:no; resizeable:no; help:no";

   	var popupUri = "${contextPath}/pages/workflow/design/wfstarttask-info.jsp";
   	popup_window({url:popupUri,width:550,height:500,data:taskObj,model:true});
}

//处理结束任务节点
function doEndTask(endObj){
   var feacture = "dialogWidth:" + 550 + "px; dialogHeight:" + 500 + "px;";
 feacture = feacture + "directories:no; localtion:no; menubar:no; status:no;";
 feacture = feacture + "toolbar:no; scroll:no; resizeable:no; help:no";

   var popupUri = "${contextPath}/pages/workflow/design/wfendtask-info.jsp";
   popup_window({url:popupUri,width:550,height:500,data:taskObj,model:true});
}

//处理普通任务节点
function doSimpleTask(taskObj){
   var feacture = "dialogWidth:" + 550 + "px; dialogHeight:" + 500 + "px;";
 feacture = feacture + "directories:no; localtion:no; menubar:no; status:no;";
 feacture = feacture + "toolbar:no; scroll:no; resizeable:no; help:no";

   var popupUri = "${contextPath}/pages/workflow/design/wfsimpletask-info.jsp";
   popup_window({url:popupUri,width:550,height:500,data:taskObj,model:true});
}

//处理分支任务节点
function doBranchTask(taskObj){
   var feacture = "dialogWidth:" + 550 + "px; dialogHeight:" + 500 + "px;";
 feacture = feacture + "directories:no; localtion:no; menubar:no; status:no;";
 feacture = feacture + "toolbar:no; scroll:no; resizeable:no; help:no";

   var popupUri = "${contextPath}/pages/workflow/design/wfbranchtask-info.jsp";
   popup_window({url:popupUri,width:550,height:500,data:taskObj,model:true});
}

//处理收缩任务节点
function doShrinkTask(taskObj){
   	var feacture = "dialogWidth:" + 550 + "px; dialogHeight:" + 500 + "px;";
 	feacture = feacture + "directories:no; localtion:no; menubar:no; status:no;";
 	feacture = feacture + "toolbar:no; scroll:no; resizeable:no; help:no";

   	var popupUri = "${contextPath}/pages/workflow/design/wfshrinktask-info.jsp";
   	popup_window({url:popupUri,width:500,height:500,data:taskObj,model:true});
}

function saveTemplateWF() {
	var groupObj = document.getElementById('group');
    var Love=groupObj.getAttribute('bindClass');
	 if (confirm("是否保存流程模板信息？")){
		 $.ajax({
				url: '${contextPath}/wfDesignForm/saveWfTemplate.so', 
				type: 'POST', 
				data: {wfTemplateContent:Love.toJson(),mode:"edit"}, 
				dataType: 'json', 
				success: function(result){
					 alert(result.message);
					 window.close();
				}
			});
		
	 }
}
   
$(function(){
	var g = new Group();
	g.init();
	g.setGroupArea();
	var m = new Menu();
	m.init();
	var modeValue = $("#model").val();
	var jsonEdit = $("#editWorkflowTemplateJSON").val();
	if (modeValue == 'edit'){
	   var j = JSON.decode(jsonEdit);
	   if(j.count==0)return;
	   g.jsonTo(j);
	}
});

</script>

<body class='btype'  onload="document.getElementById('group').focus();">
	<input type="hidden" id="model" value="<%=model%>">
	<input type="hidden" id="editWorkflowTemplateJSON" value='${editWorkflowTemplateJSON}'>
	<div id="tool"></div>
	<div id="group" onselectstart="return false;"></div>
</body>
</html>