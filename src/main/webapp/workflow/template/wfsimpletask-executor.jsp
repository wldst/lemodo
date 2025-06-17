
<%@ page contentType="text/html;charset=GBK" %>

<%
List executorList = null;
   if (templateSimpleTask!=null){
      executorList = templateSimpleTask.getTemplateTaskExecutorList();
   }
   TtaskExecutor executor = null;
   
   if (executorList != null && executorList.size() > 0){
      executor = (TtaskExecutor)executorList.get(0);
   }
%>
<FIELDSET>
<LEGEND>执行人公式详细</LEGEND>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
   <tr>
      <td>
         <textarea name="executorEditScript" id="executorEditScript" cols="73" rows="20"><%=executor!=null?executor.getExpression():""%></textarea>
      </td>
   </tr>
</table>
</FIELDSET>