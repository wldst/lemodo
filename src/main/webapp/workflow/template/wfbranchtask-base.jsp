<%
/**
 *  Ԥ��������ģ����ͨ���������Ϣ����
 *  2007-03-27
 *  yangyn
 */
%>
<%@ page contentType="text/html;charset=GBK" %>

<table width="98%" cellpadding="2" cellspacing="2" class="addtable" align="center">
   <tr >
      <td class="label" nowrap width="15%">�������ƣ�</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="name" id="name" 
            value="<%=templateBranchTask.getWfTaskName()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">�����ڲ�InnerID��</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="innerTaskID" id="innerTaskID" 
            value="<%=templateBranchTask.getWfTaskInnerID()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">ǰ������ڵ�InnerID��</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="previous" id="previous"
            value="<%=templateBranchTask.getInput()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">ƥ������ڵ�InnerID��</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="previous" id="previous"
            value="<%=templateBranchTask.getPairNode()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">����������</td>
      <td class="tdinput" nowrap width="35%">
         <textarea name="description" id="description" cols="40" rows="5"><%=templateBranchTask.getDescript()%></textarea>
      </td>                         
   </tr>
</table>