<%
/**
 *  预定义流程模板普通任务基本信息画面
 *  2007-03-27
 *  yangyn
 */
%>
<%@ page contentType="text/html;charset=GBK" %>

<table width="98%" cellpadding="2" cellspacing="2" class="addtable" align="center">
   <tr >
      <td class="label" nowrap width="15%">任务名称：</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="name" id="name" 
            value="<%=abTempTask.getWfTaskName()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">任务内部InnerID：</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="innerTaskID" id="innerTaskID" 
            value="<%=abTempTask.getWfTaskInnerID()%>">
      </td>                         
   </tr>
   <%
      if (abTempTask.getWfTaskType() == WFEConstants.WFTASK_TYPE_END){
   %>
   <tr >
      <td class="label" nowrap width="15%">前续任务节点InnerID：</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="previous" id="previous"
            value="<%=((Tend)abTempTask).getPrevious()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">匹配任务节点InnerID：</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="pairNode" id="pairNode"
            value="<%=((Tend)abTempTask).getPairNode()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">任务描述：</td>
      <td class="tdinput" nowrap width="35%">
         <textarea name="description" id="description" cols="40" rows="5"><%=((Tend)abTempTask).getDescript()%></textarea>
      </td>                         
   </tr>
   <%
      }
   %>
   <%
      if (abTempTask.getWfTaskType() == WFEConstants.WFTASK_TYPE_START){
   %>
   <tr >
      <td class="label" nowrap width="15%">后续任务节点InnerID：</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="next" id="next" 
            value="<%=((Tstart)abTempTask).getNext()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">匹配任务节点InnerID：</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="pairNode" id="pairNode"
            value="<%=((Tstart)abTempTask).getPairNode()%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">任务描述：</td>
      <td class="tdinput" nowrap width="35%">
         <textarea name="description" id="description" cols="40" rows="5"><%=((Tstart)abTempTask).getDescript()%></textarea>
      </td>                         
   </tr>
   <%
      }
   %>
  
</table>