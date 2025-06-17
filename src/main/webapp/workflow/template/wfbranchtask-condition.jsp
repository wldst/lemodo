
<%@ page contentType="text/html;charset=GBK" %>

<%
   List branchList = templateBranchTask.getBranchOutputList();
%>
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
      onDblClick='setTrBGColor(this);getBranchOutputCondition("<%=output.getOutputTaskID()%>");' 
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