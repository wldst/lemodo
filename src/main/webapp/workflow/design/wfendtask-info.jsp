<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../../tag.inc" %>
<html>
<head>
<title>预定义流程模板编辑</title>
<jsp:include flush="false"  page="../../../header.jsp"></jsp:include>
</head>

<script type='text/javascript' src='${contextPath}/dwr/interface/WfDWRTemplateOperate.js'></script>  
<script type='text/javascript' src='${contextPath}/dwr/engine.js'></script>  
<script type='text/javascript' src='${contextPath}/dwr/util.js'></script>  
<script langauge="javascript"> 
//var endObj = parent.window.dialogArguments;
var endObj = art.dialog.data('data');

//初始化任务属性值
function initTaskValues(){
   document.all.name.value = endObj.wfTaskName;
   document.all.innerTaskID.value = endObj.wfTaskInnerID;
   document.all.description.value = endObj.descript;
}

//保存任务属性值
function saveTaskValues(){
   endObj.wfTaskName = document.all.name.value;
   endObj.wfTaskInnerID = document.all.innerTaskID.value;
   endObj.descript = document.all.description.value;
   window.close();
}
</script>
<body scroll="no">
<form name="wfStartTaskForm" method="POST">   
<table width="100%" border="0" cellpadding="2" cellspacing="2"> 
   <tr height="25" class="listtablelist_head_bgbg">
      <td align="center" class="table_title_text_bold">
      流程结束任务编辑
      </td>
   </tr>
</table>
<div id="tt" class="easyui-tabs"   style="width:700px;height:360px;">  
       <div title="基本信息" style="padding:20px;" >  
		<table width="98%" cellpadding="2" cellspacing="2" class="addtable" align="center">
		   <tr >
               <td class="label"  width="15%">任务名称：</td>
               <td class="tdinput"  width="35%">
                  <input type="text" class="input_text" name="name" id="name" 
                     value="">
               </td>                         
            </tr>
            <tr >
               <td class="label"  width="15%">任务内部InnerID：</td>
               <td class="tdinput"  width="35%">
                  <input type="text" class="input_text" name="innerTaskID" id="innerTaskID" 
                     value="">
               </td>                         
            </tr>
            <tr >
               <td class="label"  width="15%">前序任务节点InnerID：</td>
               <td class="tdinput"  width="35%">
                  <input type="text" class="input_text" name="next" id="next" 
                     value="">
               </td>                         
            </tr>
            <tr >
               <td class="label"  width="15%">匹配任务节点InnerID：</td>
               <td class="tdinput"  width="35%">
                  <input type="text" class="input_text" name="pairNode" id="pairNode"
                     value="">
               </td>                         
            </tr>
            <tr >
               <td class="label"  width="15%">任务描述：</td>
               <td class="tdinput"  width="35%">
                  <textarea name="description" id="description" cols="40" rows="5"></textarea>
               </td>                         
            </tr>
		</table> 
	</div>  
</div>

<table width="99%" cellpadding="2" cellspacing="2" class="addtable" align="center">
   <tr align="center">
      <td height="45"  colspan="4">                  
         <input type="button" name="BTN_SAVE" id="BTN_SAVE" value="确  定" 
            class="input_button" onclick="saveTaskValues();">
         <input type="button" name="BTN_CLOSE" id="BTN_CLOSE" value="关  闭" 
            class="input_button" onclick="window.close();">
      </td>
   </tr> 
</table>
</form>
<script language="javascript">
   initTaskValues();   
</script>
</body>
</html>   