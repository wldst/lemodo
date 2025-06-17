<%
/**
 *  流程模板编辑
 *  2006-02-28
 *  yangyn
 */
%>
<%@ page contentType="text/html;charset=GBK" %>
<%@ include file="/includes/jsp-header.jsp"%>

<%
Twf templateWF = (Twf)request.getAttribute("templateWF");
   String mode = request.getParameter("mode");
   String editWorkflowTemplateJSON = (String)request.getAttribute("editWorkflowTemplateJSON");
%>
<html>
<head>
<title>流程模板编辑</title>
</head>

<script language="javascript">
   //返回
   function backList(){
      window.location.href = "<%=context%>/agdev/workflow/wftemplate-design.do?method=initWfTemplateList";
   }  
   
   //保存流程模板信息
   function saveTemplateWF(){
      var groupObj = templatePicFrame.document.getElementById('group');
      var Love=groupObj.getAttribute('bindClass');
      document.all.wfTemplateContent.value = Love.toJson();
      if (confirm("是否保存流程信息？")){
         var saveUri = "<%=context%>/agdev/workflow/wftemplate-design.do?method=saveWfTemplate";
         document.templateWorkflowForm.action = saveUri;
         document.templateWorkflowForm.target = "operFrame";
         document.templateWorkflowForm.submit();
      }   
   }       
</script>
<body scroll="no">

<form name="templateWorkflowForm" id="templateWorkflowForm" method="POST">
<input type="hidden" name="wfTemplateContent" id="wfTemplateContent" value='<%=TextUtil.nvl(editWorkflowTemplateJSON)%>'>
<input type="hidden" name="mode" id="mode" value="<%=TextUtil.nvl(mode)%>">
<input type="hidden" name="ID" id="ID" value='<%=templateWF!=null?String.valueOf(templateWF.getID()):""%>'>
     
<div class="list_head_mainbg">
   <div class="tablelist_head_bg_left"></div>
   <div class="list_head_left_fontbg">您当前的位置：开发管理--流程模板设计</div>
   <div class="list_head_middle_space"></div>
   <div class="list_head_right"></div>
   <agdev:toolbar name="toolbar">
      <agdev:button id="refreshButton" name="refreshButton" caption="保存" 
   		link="javascript:saveTemplateWF();" 
   		image='<%=context + "/images/toolbar/save.gif"%>'/>	      	
   	<agdev:button id="refreshButton" name="refreshButton" caption="返回" 
   		link="javascript:backList();" 
   		image='<%=context + "/images/toolbar/back.gif"%>'/>              
   </agdev:toolbar>
   <div class="clear"></div>
</div>

<table width="100%" border="0" cellpadding="0" cellspacing="0">
   <tr>
      <td valign="top" style="padding-left:0px">
         <table width="100%" cellpadding="0" cellspacing="0" class="addtable" align="center">
            <tr >
               <td class="label" nowrap width="15%">名称：</td>
               <td class="tdinput" nowrap width="35%">
                  <input type="text" class="input_text" name="templateName" 
                     value='<%=templateWF!=null?templateWF.getWfName():""%>'>
               </td>  
               <td class="label" nowrap width="15%">模板标识：</td>
               <td class="tdinput" nowrap width="35%">
                  <input type="text" class="input_text" name="templateMark" 
                      value='<%=templateWF!=null?templateWF.getWfMark():""%>'>
               </td>        
            </tr>
            <tr >
               <td class="label" nowrap width="15%">模板描述：</td>
               <td class="tdinput" nowrap width="35%" colspan="3">
                  <textarea name="templateDescript" id="templateDescript" cols="85" rows="3"><%=templateWF!=null?templateWF.getWfDescript():""%></textarea>                       
               </td>                        
            </tr>
         </table>
      </td>
   </tr>
   <tr>
      <td width="100%" style="height:expression(document.body.clientHeight-this.offsetTop-20)">
         <table width="100%" height="100%" align="center" 
            border="0" cellpadding="0" cellspacing="0">
            <tr>
               <td align="center" valign="top" >
                  <%
                     String picUri = context + "/agdev/workflow/design/wftemplate-design.jsp";                                       
                  %>
                  <iframe height="100%" width="100%"
                      border="0" frameborder="0" 
                      scrolling="yes" 
                      src="<%=picUri%>" 
                      name="templatePicFrame"></iframe>
               </td>
            </tr>
         </table>
      </td>
   </tr>
</table> 
</form>  
<iframe name="operFrame" width="0" height="0"></iframe>
</body>
</html>