<%
/**
 *  流程任务提交画面
 *  2007-03-27
 *  yangyn
 */
%>
<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../tag.inc" %>
<%@ page import="java.util.List" %>

<%
String bizDataID = request.getParameter("bizDataID");
   String bizTabName = request.getParameter("bizTabName");
   String templateMark = request.getParameter("templateMark");
   String rootPath = (String)request.getAttribute("contextPath");
   String taskComeDatetime = request.getParameter("taskComeDatetime");

   BpmInstance workflow = (BpmInstance)request.getAttribute("workflow");
   SimpleTask nextTask = (SimpleTask)request.getAttribute("nextTask");
   SimpleTask currentTask = (SimpleTask)request.getAttribute("currentTask");
   
   Decision performDecision = null;
   if (currentTask != null){
      performDecision = currentTask.getWfTaskDecision(WFEConstants.WFDECISION_PERFORM);
   }
   int decisionExecuteType = WFEConstants.DECISION_EXEC_EMP;
   if (performDecision != null){
      decisionExecuteType = performDecision.getExecuteType();
   }
   
   String executorSelectTitle = "";
   if (nextTask != null){
      executorSelectTitle = "【" + nextTask.getTaskName() + "】任务执行人选择设定";
   }
   
   int maxEmpNum = -1;
   if (nextTask != null){
      TaskProperties nextProp = nextTask.getTaskProperty();
      if (nextProp != null){
         maxEmpNum = nextProp.getMaxExecutorNum();
      }
   }
%>
<html>
<head>
<title>流程任务提交画面</title>
<jsp:include flush="false"  page="../../header.jsp"></jsp:include>
</head>
<script language="javascript">   
   //增加选择的执行人
   function addSelectItem(itemVal){      
      var itemValueArray;
      var objValue;
      var itemName;
      var itemValue;
      objValue = itemVal;//obj.itemEmpValue;
      itemValueArray = objValue.split("#");
      
      itemName = itemValueArray[1];
      itemValue = itemValueArray[0];
      
      var selectItemObj = document.getElementById("selectedItem");
      var optionObj = document.createElement("OPTION");
      optionObj.text = itemName;
      optionObj.value = itemValue;
      
      //检验是否已经选择了该记录
      for ( var i=0;i< selectItemObj.options.length;i++ )
      {
         if ( selectItemObj.options[i].value == itemValue )
         {
              return false;
         }
      }
      
	  if(1 == <%=maxEmpNum%>){
	  		removeAll();
	  }else if(-1 != <%=maxEmpNum%> && selectItemObj.length >= <%=maxEmpNum%>){
	  		alert("下一任务最多只能有<%=maxEmpNum%>人执行!");
	  		return false;
	  }
      selectItemObj.add( optionObj );
   }   
   
   //删除所有
   function removeAll()
   {
      var selectItemObj = document.getElementById("selectedItem");
      for (i=selectItemObj.options.length - 1;i >= 0 ; i-- )
      {
          selectItemObj.options.remove(0);
      }
   }
   
   //删除选中项目
   function removeSelectedItem(){
      var selectItemObj = document.getElementById("selectedItem");
      for(j = selectItemObj.options.length - 1; j >= 0; j--){
			if (selectItemObj.options(j).selected == true){
				selectItemObj.remove(j);
			}
		}
   }
   
   //选中按钮单击
   function imgSelectClick(){
      var obj = new Object();
      var hiddenClickExecutorObj = executorListFrame.document.getElementById("hiddenClickExecutor");
      if (hiddenClickExecutorObj == null || hiddenClickExecutorObj.value == ""){
         return;
      }
      obj.itemValue = hiddenClickExecutorObj.value;
      addSelectItem(obj);
      executorListFrame.scanItem();
   }
   
   //任务提交
   function performAction(){
      var performUri = "${contextPath}/agdev/workflow/wfdecision-perform.do?method=perform";
      
      <%
         if (nextTask == null){
      %>
      if (confirm("当前为流程最后一个任务，是否结束该流程？")){
		  execWf();
	}
      <%
         }else{
            if (decisionExecuteType == WFEConstants.DECISION_EXEC_EMP){               
      %>
         var selectItemObj = document.getElementById("selectedItem");
         if (selectItemObj.options.length <= 0){
            alert("没有选择下一任务执行人,请选择！");
            return;
         }else{
            var executorIDs = "";
            for(i =0; i < selectItemObj.options.length; i++){
               if (executorIDs == ""){
                  executorIDs = selectItemObj.options[i].value;
               }else{
                  executorIDs = executorIDs + "#" + selectItemObj.options[i].value;
               }            
			  
            }
           
            document.all.hiddenTaskExecutorIDs.value = executorIDs;
         }
      <%
         }
      %>   
         if (confirm("是否执行当前流程操作动作？")){
            execWf();
         }
      <%
         }
      %>
   }

	  function execWf()
	  {
		  var params=$("#wfDecisionPerformForm").serialize();
	        $.ajax({
		        url: '${contextPath}/wfPerformForm/perform.so', 
	        	type: 'POST', 
	        	data: params, 
	        	dataType: 'json', 
	        	beforeSend: function () {
					$("#BTN_SAVE").attr({"disabled":true});
		        },
	        	success: function(result){
					alert(result.message);
				    if (result.success) { // 操作成功
			    		// 刷新数据列表
						window.returnValue = result.success;
				    	art.dialog.close();
				    } 
	        	},
	        	complete: function(e, xhr, settings) {
					$("#BTN_SAVE").attr({"disabled":false});
	        	}
	        });
	  }
</script>
<body scroll="no">
<form name="wfDecisionPerformForm" id="wfDecisionPerformForm" method="POST"> 
<input type="hidden" name="hiddenTaskExecutorIDs" id="hiddenTaskExecutorIDs">    
<input type="hidden" name="bizDataID" id="bizDataID" value="<%=TextUtil.nvl(bizDataID)%>">
<input type="hidden" name="bizTabName" id="bizTabName" value="<%=TextUtil.nvl(bizTabName)%>">
<input type="hidden" name="templateMark" id="templateMark" value="<%=TextUtil.nvl(templateMark)%>">
<input type="hidden" name="taskComeDatetime" id="taskComeDatetime" value="<%=taskComeDatetime%>">

<table width="100%" border="0" cellpadding="0" cellspacing="0" class="toolbar" id="toolbar_toolbar_null" name="toolbar">
   
	<%
		if (nextTask != null && decisionExecuteType == WFEConstants.DECISION_EXEC_EMP){
			%>
		         <tr>
		      		<td valign="top" style="padding-left:0px">
				         <FIELDSET>
				         <LEGEND><%=executorSelectTitle%></LEGEND>
				         <table width="100%" cellpadding="2" cellspacing="2" 
				               class="addtable" align="center" >
				            <tr>
				               <td width="55%" height="100" style="padding-left:5px;padding-right:5px;">
				                  <table width="100%" height="100%" align="center" border="0" class="addtable">
				                     <tr>
				                        <td align="center" valign="top" >
				                           <%
				                              String taskExecutorListPageUri = rootPath + "/wfPerformForm/initExecutorList.so";                              
				                              taskExecutorListPageUri = taskExecutorListPageUri + "?bizDataID=" + bizDataID;
				                              taskExecutorListPageUri = taskExecutorListPageUri + "&bizTabName=" + bizTabName;
				                              taskExecutorListPageUri = taskExecutorListPageUri+ "&templateMark=" + templateMark;
				                           %>
				                           <iframe height="100%" width="100%" frameborder="0" 
				                               scrolling="no" 
				                               src="<%=taskExecutorListPageUri%>" 
				                               name="executorListFrame"></iframe>
				                        </td>
				                     </tr>
				                  </table>
				               </td>
				               <td width="2%">
							   <!--input id="btnNext" name="btnNext" class="input_btn" type="button" value=">"	onclick="imgSelectClick();" /-->
							   <br>
							   <input id="btnPrv" name="btnPrv" class="input_btn" type="button" value="<"	onclick="removeSelectedItem();" />
				               </td>
				               <td width="33%" height="100" style="padding-left:5px;padding-right:5px;">
				                   <table width="100%" height="100%" align="center" border="0" class="addtable">
				                     <tr>
				                        <td align="center" valign="top" >
				                           <select name="selectedItem" id="selectedItem" size="12"  valign="top"
				                              style="width:100%;height:100%px; border-style: none; border-width: 0px;" onDblClick="removeSelectedItem();"> 
				                           </select>                                 
				                        </td>
				                     </tr>
				                  </table>
				               </td>
				            </tr>
				         </table>
				         </FIELDSET> 
		         	</td>
		         </tr>
         		<%
            }
         %>
	<tr>
         <td>
	         <FIELDSET>
	         <LEGEND>执行意见</LEGEND>
	         <table width="100%" cellpadding="2" cellspacing="2" class="addtable" align="center">
	            <tr >
	               <td class="label" nowrap width="15%">执行意见：</td>
	               <td class="tdinput" nowrap width="85%"> 
	                  <textarea cols="60" rows="5" name="executeComment"></textarea>
	               </td>
	            </tr>
	         </table>
	         </FIELDSET>
      </td>
   </tr>
   <tr align="center">
      <td height="45" nowrap colspan="2">         
         <input type="button" name="BTN_SAVE" id="BTN_SAVE" value="确认执行" class="input_button" onclick="performAction();">
         <input type="button" name="BTN_CLOSE"  id="BTN_CLOSE" value="取消" class="input_button" onclick="art.dialog.close();">
      </td>
   </tr>
</table>   
</form>
<iframe name="operFrame" width="0" height="0"></iframe> 
</body>
</html>