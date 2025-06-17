<%
/**
 *  预定义流程模板普通任务编辑画面
 *  2007-03-27
 *  yangyn
 */
%>
<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../../tag.inc" %>
<%@ page import="java.util.List" %>


<%
TbTask templateBranchTask = (TbTask)request.getAttribute("templateBranchTask");
   Twf templateWF = (Twf)session.getAttribute("TEMPLATEWF$$EDIT");
   List branchList = templateBranchTask.getBranchOutputList();
   String templateMark = request.getParameter("templateMark");
   String innerTaskID = request.getParameter("innerTaskID");
%>
<html>
<head>
<title>预定义流程模板编辑</title>
<jsp:include flush="false"  page="../../../header.jsp"></jsp:include>
</head>

<script type='text/javascript' src='${contextPath}/dwr/interface/WfDWRTemplateOperate.js'></script>  
<script type='text/javascript' src='${contextPath}/dwr/engine.js'></script>  
<script type='text/javascript' src='${contextPath}/dwr/util.js'></script>  

<script langauge="javascript">

   // 测试编辑
	function fillBranchOutCondition(outputTaskID){
		 document.all.outputTaskID.value = outputTaskID;
		 var params = {innerTaskID:"<%=innerTaskID%>",outputTaskID:outputTaskID};
		var url = '"${contextPath}/wfTemplateForm/getBranchOutputCondition.so?';
		 $.ajax({ url: url, type: 'POST', data: params, dataType: 'json', 
	        	beforeSend: function () {
		        },
	        	success: function(result){
		    		// 将json对象绑定到表单（表单赋值）
		    		$('#executorEditScript').attr('value',result);
	        	}
	        });
		
	}

   
   
   //保存分支条件信息
   function saveBranchOutputCondition(condition){
      var outputTaskID = document.all.outputTaskID.value; 
      if (outputTaskID != ""){    
		var params=$("#templateSimpleTaskEditForm").serialize();
		$.ajax({
			url: '${contextPath}/wfTemplateForm/saveBranchOutputCondition.so', 
			type: 'POST', 
			data: params, 
			dataType: 'json', 
			beforeSend: function () {
				$("#BTN_SAVE").attr({"disabled":true});
			},
			success: function(result){
				
				callBack(result);
			},
			complete: function(e, xhr, settings) {
				$("#BTN_SAVE").attr({"disabled":false});
			}
		});

      }
   }

   function callBack(result)
   { 
	    var outputTaskID = document.all.outputTaskID.value; 
     var outputTaskCondition = outputTaskID + '_condition';
	 if (outputTaskID != ""){
		if (result != '' && result.length > 10){
			 document.all(outputTaskCondition).innerText = result.substring(0,10) + " ..."; 
		}else{
			document.all(outputTaskCondition).innerText = result; 
		 }
	}

   }

      
   //设置行的背景颜色
   function setTrBGColor(trObj){
      var trObjArray = document.templateSimpleTaskEditForm.getElementsByTagName("TR");

      if (trObjArray != null && parseInt(trObjArray.length) > 0){
         for(i = 0; i < trObjArray.length; i++){
            if (trObjArray[i].trname != null && trObjArray[i].trname == "condition_tr"){
               trObjArray[i].style.backgroundColor = "";
            }
         }
      }
       
      if (trObj != null){
         trObj.style.backgroundColor = "#3366aa";
      }
   }
      
   
         //保存任务信息
	function saveTask() {
		// 序列化表单
		var params=$("#templateSimpleTaskEditForm").serialize();
		$.ajax({
			url: '${contextPath}/wfTemplateForm/saveTaskPage.so', 
			type: 'POST', 
			data: params, 
			dataType: 'json', 
			beforeSend: function () {
				$("#BTN_SAVE").attr({"disabled":true});
			},
			success: function(result){
				alert(result.message);
				window.close();
			},
			complete: function(e, xhr, settings) {
				$("#BTN_SAVE").attr({"disabled":false});
			}
		});
	}
</script>
<body scroll="no">
<form name="templateSimpleTaskEditForm" id="templateSimpleTaskEditForm" method="POST">
<input type="hidden" name="templateMark" id="templateMark" value="<%=TextUtil.nvl(templateMark)%>">
<input type="hidden" name="innerTaskID" id="innerTaskID" value="<%=TextUtil.nvl(innerTaskID)%>">
<input type="hidden" name="outputTaskID" id="outputTaskID" value="">

<table width="100%" border="0" cellpadding="2" cellspacing="2"> 
   <tr height="25" class="listtablelist_head_bgbg">
      <td align="center" class="table_title_text_bold">
      流程模板分支任务编辑
      </td>
   </tr>
   </table>
    <div id="tt" class="easyui-tabs"   style="width:700px;height:360px;">  
        <div title="基本信息" style="padding:20px;" >  
			<table width="98%" cellpadding="2" cellspacing="2" class="addtable" align="center">
			   <tr >
				  <td class="label" nowrap width="15%">任务名称：</td>
				  <td class="tdinput" nowrap width="35%">
					 <input type="text" class="input_text" name="name" id="name" 
						value="<%=templateBranchTask.getWfTaskName()%>">
				  </td>                         
			   </tr>
			   <tr >
				  <td class="label" nowrap width="15%">任务内部InnerID：</td>
				  <td class="tdinput" nowrap width="35%">
					 <input type="text" class="input_text" name="innerTaskID" id="innerTaskID" 
						value="<%=templateBranchTask.getWfTaskInnerID()%>">
				  </td>                         
			   </tr>
			   <tr >
				  <td class="label" nowrap width="15%">前续任务节点InnerID：</td>
				  <td class="tdinput" nowrap width="35%">
					 <input type="text" class="input_text" name="previous" id="previous"
						value="<%=templateBranchTask.getInput()%>">
				  </td>                         
			   </tr>
			   <tr >
				  <td class="label" nowrap width="15%">匹配任务节点InnerID：</td>
				  <td class="tdinput" nowrap width="35%">
					 <input type="text" class="input_text" name="previous" id="previous"
						value="<%=templateBranchTask.getPairNode()%>">
				  </td>                         
			   </tr>
			   <tr >
				  <td class="label" nowrap width="15%">任务描述：</td>
				  <td class="tdinput" nowrap width="35%">
					 <textarea name="description" id="description" cols="40" rows="5"><%=templateBranchTask.getDescript()%></textarea>
				  </td>                         
			   </tr>
			</table>
		</div>  
        <div title="分支条件" style="padding:20px;">  
			<table width="100%" border="0" type="grid" 
			   cellpadding="0" cellspacing="0" name="list_details" 
			   id="list_details">
			   <tr height="22" align="center" class="tablelist_head_bg"> 
				  <td nowrap class="tablelist_head_leftline" width="1%">序号</td>
				  <td nowrap class="tablelist_head_middleandrightline" width="10%">分支任务InnerID</td>
				  <td nowrap class="tablelist_head_middleandrightline" width="10%">任职任务名</td>
				  <td nowrap class="tablelist_head_middleandrightline" width="10%">条件</td>
			   </tr>
			   <%
			   if (branchList != null && branchList.size() > 0){
			   					 int listSize = branchList.size();
			   					 for(int i = 0; i < listSize; i++){
			   						Toutput output = (Toutput)branchList.get(i);
			   %>
			   <tr align="center" style="cursor:hand"
				  onClick='fillBranchOutCondition("<%=output.getOutputTaskID()%>");' 
				  trname="condition_tr">
				  <td class="tablelist_list_leftline">
					 <%=i+1%>
				  </td>
				  <td class="tablelist_list_middleandrightline">
					 <%=output.getOutputTaskID()%>
				  </td>
				  <td class="tablelist_list_middleandrightline">
					 <%=templateWF.getTemplateTaskByInnerID(output.getOutputTaskID()).getWfTaskName()%>
				  </td>
				  <td class="tablelist_list_middleandrightline" id="<%=output.getOutputTaskID()%>_condition" title="<%=output.getCondition()%>">
					 <%=TextUtil.nvl(TextUtil.subString(output.getCondition(),16))%>&nbsp;
				  </td>
			   </tr>
			   <%
					 }
				  }
			   %>
			</table>

			<FIELDSET>
			<LEGEND>分支条件公式详细</LEGEND>
			<table width="100%" border="0" cellpadding="0" cellspacing="0">
			   <tr>
				  <td>
					 <textarea name="executorEditScript" id="executorEditScript" 
						cols="74" rows="10" onchange="saveBranchOutputCondition(this.value);"></textarea>
				  </td>
			   </tr>
			</table>
			</FIELDSET>
        </div>  
    </div>  


<table width="98%" cellpadding="2" cellspacing="2" class="addtable" align="center">
   <tr align="center">
      <td height="45" nowrap colspan="4" class="table_button_text">                   
         <input type="button" name="BTN_SAVE" id="BTN_SAVE" value="确  定" 
            class="main_button_style" onclick="saveTask();">
         <input type="button" name="BTN_CLOSE" id="BTN_CLOSE" value="关  闭" 
            class="main_button_style" onclick="window.close();">
      </td>
   </tr> 
</table> 
</form>   
<iframe name="operFrame" width="0" height="0"></iframe> 
</body>
</html>