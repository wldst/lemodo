<%
/**
 *  Ԥ��������ģ����ͨ���������Ϣ����
 *  2007-03-27
 *  yangyn
 */
%>
<%@ page contentType="text/html;charset=GBK" %>

<%
   List inputTaskList = templateShrinkTask.getInputTaskIDList();
%>
<table width="98%" cellpadding="2" cellspacing="2" class="addtable" align="center">
   <tr >
      <td class="label" nowrap width="15%">�������ƣ�</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="name" id="name" 
            value="<%=templateShrinkTask.getWfTaskName()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">�����ڲ�InnerID��</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="innerTaskID" id="innerTaskID" 
            value="<%=templateShrinkTask.getWfTaskInnerID()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">��������ڵ�InnerID��</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="next" id="next"
            value="<%=templateShrinkTask.getOutput()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">ƥ������ڵ�InnerID��</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="pairNode" id="pairNode"
            value="<%=templateShrinkTask.getPairNode()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">����������</td>
      <td class="tdinput" nowrap width="35%">
         <textarea name="description" id="description" cols="40" rows="5"><%=templateShrinkTask.getDescript()%></textarea>
      </td>                         
   </tr>
</table>

<table width="100%" border="0" type="grid" 
   cellpadding="0" cellspacing="0" name="list_details" 
   id="list_details">
   <tr height="22" align="center" class="tablelist_head_bg"> 
      <td nowrap class="tablelist_head_leftline" width="1%">���</td>
      <td nowrap class="tablelist_head_middleandrightline" width="10%">��֧����InnerID</td>
      <td nowrap class="tablelist_head_middleandrightline" width="10%">��ְ������</td>
   </tr>
   <%
      if (inputTaskList != null && inputTaskList.size() > 0){
         int listSize = inputTaskList.size();
         for(int i = 0; i < listSize; i++){
            String inputTaskID = (String)inputTaskList.get(i);
   %>
   <tr align="center" style="cursor:hand">
       <td class="tablelist_list_leftline">
         <%=i+1%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=inputTaskID%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=templateWF.getTemplateTaskByInnerID(inputTaskID).getWfTaskName()%>
      </td>
   </tr>
   <%
         }
      }
   %>
</table>