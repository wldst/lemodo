
<%@ page contentType="text/html;charset=GBK" %>

<%
   
%>
<table width="98%" cellpadding="2" cellspacing="2" class="addtable" align="center">
   <tr >
      <td class="label" nowrap width="15%">是否作为循环开始节点：</td>
      <td class="tdinput" nowrap width="35%">
         <select name="<%=WFEConstants.TASK_PROPERTY_FOUR%>">
            <option value="<%=AgdevConstants.DB_BOOLEAN_FALSE%>" 
               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FOUR) != null ? (TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FOUR).getValue())?"":"selected"):""%>>否</option>
            <option value="<%=AgdevConstants.DB_BOOLEAN_TRUE%>" 
               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FOUR) != null ? (TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FOUR).getValue())?"selected":""):""%>>是</option>
         </select>
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">循环结束任务节点：</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="<%=WFEConstants.TASK_PROPERTY_ONE%>" 
         id="<%=WFEConstants.TASK_PROPERTY_ONE%>" 
         value="<%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_ONE) != null ? TextUtil.nvl(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_ONE).getValue()) : ""%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">所有执行人必须执行：</td>
      <td class="tdinput" nowrap width="35%">
        <select name="<%=WFEConstants.TASK_PROPERTY_TWO%>">
            <option value="<%=AgdevConstants.DB_BOOLEAN_FALSE%>" 
               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TWO) != null ? (TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TWO).getValue())?"":"selected"):""%>>否</option>
            <option value="<%=AgdevConstants.DB_BOOLEAN_TRUE%>" 
               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TWO) != null ? (TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TWO).getValue())?"selected":""):""%>>是</option>
         </select>
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">最多的环节执行人：</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="<%=WFEConstants.TASK_PROPERTY_THREE%>" 
         id="<%=WFEConstants.TASK_PROPERTY_THREE%>" 
         value="<%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_THREE)!=null?(TextUtil.nvl(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_THREE).getValue())):""%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">任务是否自动执行：</td>
      <td class="tdinput" nowrap width="35%">
        <select name="<%=WFEConstants.TASK_PROPERTY_SIX%>">
            <option value="<%=AgdevConstants.DB_BOOLEAN_FALSE%>" 
               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_SIX)!= null ? (TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_SIX).getValue())?"":"selected"):""%>>否</option>
            <option value="<%=AgdevConstants.DB_BOOLEAN_TRUE%>" 
               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_SIX)!= null ? (TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_SIX).getValue())?"selected":""):""%>>是</option>
         </select>
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">是否等待子流程执行：</td>
      <td class="tdinput" nowrap width="35%">
        <select name="<%=WFEConstants.TASK_PROPERTY_FIVE%>">
            <option value="<%=AgdevConstants.DB_BOOLEAN_FALSE%>" 
               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FIVE)!=null?(TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FIVE).getValue())?"":"selected"):""%>>否</option>
            <option value="<%=AgdevConstants.DB_BOOLEAN_TRUE%>" 
               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FIVE)!=null?(TextUtil.toBoolean(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_FIVE).getValue())?"selected":""):""%>>是</option>
         </select>
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">子流程触发class：</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" name="<%=WFEConstants.TASK_PROPERTY_SEVEN%>" 
         id="<%=WFEConstants.TASK_PROPERTY_SEVEN%>" 
         value="<%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_SEVEN)!=null?(TextUtil.nvl(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_SEVEN).getValue())):""%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">任务运行时自动触发class：</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" 
         name="<%=WFEConstants.TASK_PROPERTY_EIGHT%>" 
         id="<%=WFEConstants.TASK_PROPERTY_EIGHT%>" 
         value="<%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_EIGHT)!=null?(TextUtil.nvl(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_EIGHT).getValue())):""%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">业务触发URI：</td>
      <td class="tdinput" nowrap width="35%">
         <input type="text" class="input_text" 
         name="<%=WFEConstants.TASK_PROPERTY_NINE%>" 
         id="<%=WFEConstants.TASK_PROPERTY_NINE%>" 
         value="<%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_NINE)!=null?(TextUtil.nvl(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_NINE).getValue())):""%>">
      </td>                         
   </tr>
   <tr >
      <td class="label" nowrap width="15%">业务触发方式：</td>
      <td class="tdinput" nowrap width="35%">
         <select name="<%=WFEConstants.TASK_PROPERTY_TEN%>">
            <option value="<%=WFEConstants.BIZTYPE_PAGE%>" 
               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TEN) != null?(NumberUtil.parseInt(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TEN).getValue(),0) == WFEConstants.BIZTYPE_PAGE?"selected":""):"" %>>
               <%=WFEConstants.convertBizTypeZh(WFEConstants.BIZTYPE_PAGE)%></option>
            <option value="<%=WFEConstants.BIZTYPE_POPUP%>"  
               <%=templateSimpleTask!=null&&templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TEN) != null?(NumberUtil.parseInt(templateSimpleTask.getTaskProperty(WFEConstants.TASK_PROPERTY_TEN).getValue(),0) == WFEConstants.BIZTYPE_POPUP?"selected":""):"" %>>
               <%=WFEConstants.convertBizTypeZh(WFEConstants.BIZTYPE_POPUP)%></option>
         </select>         
      </td>                         
   </tr>
</table>