<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../../tag.inc" %>

<html xmlns:v="urn:schemas-microsoft-com:vml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GBK">

<%
String templateMark = request.getParameter("templateMark");   

   Twf templateWF = (Twf)session.getAttribute("TEMPLATEWF$$EDIT");
   String templateWFPicVML = (String)request.getAttribute("templateWFPicVML");
   System.out.println(templateMark);
%>
<html xmlns:v="urn:schemas-microsoft-com:vml">
<head>
  <title>动态创建VML</title>
<jsp:include flush="false"  page="../../../header.jsp"></jsp:include>
</head>
<STYLE>
 v\:* { BEHAVIOR: url(#default#VML) }
</STYLE>
<v:shapetype id="branchNode" coordsize="2 2"> <!--三角形 向右 分支-->
    <v:path v="m 0,0 l 0,0,2,1,0,2 x e" />
</v:shapetype>

<v:shapetype id="shrinkNode" coordsize="2 2"> <!--三角形 向左 收缩-->
    <v:path v="m 0,1 l 0,1,2,2,2,0 x e" />
</v:shapetype>

<script language="javascript">

 //保存流程模板信息
	function saveTemplateWF() {
		 if (confirm("是否保存流程模板信息？")){
		// 序列化表单
		var params=$("#templateWorkflowForm").serialize();
		$.ajax({
			url: '${contextPath}/wfTemplateForm/saveWfTemplate.so', 
			type: 'POST', 
			data: params, 
			dataType: 'json', 
			beforeSend: function () {
				$("#btnSave").attr({"disabled":true});
			},
			success: function(result){
				alert(result.message);
				window.close();
			},
			complete: function(e, xhr, settings) {
				$("#btnSave").attr({"disabled":false});
			}
		});
		 }
	}
 

      //弹出任务编辑画面
   function popup_task_page(taskInnerID){
      var popupUri =  "${contextPath}/wfTemplateForm/initTaskPage.so?";
      popupUri = popupUri + "innerTaskID=" + taskInnerID;
      popupUri = popupUri + "&templateMark=<%=templateMark%>";
      popup_window({url:popupUri,width:700,height:450,data:window,model:true});
  }

</script>
<body scroll="auto">
	<div class="easyui-layout" fit="true">
  <form action="" method="post" id="templateWorkflowForm" name="templateWorkflowForm" >
			<div region="north" border="false">
				 <table width="100%" cellpadding="2" cellspacing="2"  align="center">
					<tr >
					   <td class="label" nowrap width="15%">名称：</td>
					   <td class="tdinput" nowrap width="35%">
						  <input type="text" class="input_text" name="templateName" 
							 value="<%=TextUtil.nvl(templateWF.getWfName())%>">
					   </td>  
					   <td class="label" nowrap width="15%">模板标识：</td>
					   <td class="tdinput" nowrap width="35%">
						  <input type="text" class="input_text" name="templateMark" 
							 value="<%=TextUtil.nvl(templateWF.getWfMark())%>" readonly>
					   </td>        
					</tr>
					<tr >
					   <td class="label" nowrap width="15%">模板描述：</td>
					   <td class="tdinput" nowrap width="35%" colspan="3">
						  <textarea name="templateDescript" id="templateDescript" cols="85" rows="5"><%=TextUtil.nvl(templateWF.getWfDescript())%></textarea>                       
					   </td>                        
					</tr>
				 </table>

			</div>
			<div region="center" border="false" split="false" > 
				<v:group ID="workflowGroup" style="WIDTH:2000px;HEIGHT:500px;" coordsize = "2000,500">
				   <%=templateWFPicVML%> 
				</v:group>
			</div>
			<div region="south" border="false" split="false"  > 
					<input id="btnSave" class="input_btn" name="btnSave" type="button" value="保存" onclick="saveTemplateWF();" /> 
					<input id="btnClose" class="input_btn" type="button" value="关闭" onclick="window.close();" />
			</div>
</form>

	</div>

</body>
</html>