<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../../tag.inc" %>
<html>
<head>
<title>Ԥ��������ģ��༭</title>
<jsp:include flush="false"  page="../../../header.jsp"></jsp:include>
</head>

<script type='text/javascript' src='${contextPath}/dwr/interface/WfDWRTemplateOperate.js'></script>  
<script type='text/javascript' src='${contextPath}/dwr/engine.js'></script>  
<script type='text/javascript' src='${contextPath}/dwr/util.js'></script>  
<script langauge="javascript"> 
//var endObj = parent.window.dialogArguments;
var endObj = art.dialog.data('data');

//��ʼ����������ֵ
function initTaskValues(){
   document.all.name.value = endObj.wfTaskName;
   document.all.innerTaskID.value = endObj.wfTaskInnerID;
   document.all.description.value = endObj.descript;
}

//������������ֵ
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
      ���̽�������༭
      </td>
   </tr>
</table>
<div id="tt" class="easyui-tabs"   style="width:700px;height:360px;">  
       <div title="������Ϣ" style="padding:20px;" >  
		<table width="98%" cellpadding="2" cellspacing="2" class="addtable" align="center">
		   <tr >
               <td class="label"  width="15%">�������ƣ�</td>
               <td class="tdinput"  width="35%">
                  <input type="text" class="input_text" name="name" id="name" 
                     value="">
               </td>                         
            </tr>
            <tr >
               <td class="label"  width="15%">�����ڲ�InnerID��</td>
               <td class="tdinput"  width="35%">
                  <input type="text" class="input_text" name="innerTaskID" id="innerTaskID" 
                     value="">
               </td>                         
            </tr>
            <tr >
               <td class="label"  width="15%">ǰ������ڵ�InnerID��</td>
               <td class="tdinput"  width="35%">
                  <input type="text" class="input_text" name="next" id="next" 
                     value="">
               </td>                         
            </tr>
            <tr >
               <td class="label"  width="15%">ƥ������ڵ�InnerID��</td>
               <td class="tdinput"  width="35%">
                  <input type="text" class="input_text" name="pairNode" id="pairNode"
                     value="">
               </td>                         
            </tr>
            <tr >
               <td class="label"  width="15%">����������</td>
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
         <input type="button" name="BTN_SAVE" id="BTN_SAVE" value="ȷ  ��" 
            class="input_button" onclick="saveTaskValues();">
         <input type="button" name="BTN_CLOSE" id="BTN_CLOSE" value="��  ��" 
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