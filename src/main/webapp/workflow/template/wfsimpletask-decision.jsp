<%@ page contentType="text/html;charset=GBK" %>
<table width="100%" border="0" type="grid" 
   cellpadding="0" cellspacing="0" name="list_details" 
   id="list_details">
   <tr height="22" align="center"  class="tablelist_head_bg"> 
      <td nowrap class="tablelist_head_leftline" width="1%">
         <input type="checkbox" name="allSelected" 
            onclick="selectAllCheckBox(document.templateSimpleTaskEditForm,document.all.allSelected,'selectDecision');">
      </td>
      <td nowrap class="tablelist_head_middleandrightline" width="1%">序号</td>
      <td nowrap class="tablelist_head_middleandrightline" width="10%">决策名</td>
      <td nowrap class="tablelist_head_middleandrightline" width="10%">决策标识</td>
      <td nowrap class="tablelist_head_middleandrightline" width="10%">显示名</td>
      <td nowrap class="tablelist_head_middleandrightline" width="10%">执行方式</td>
      <td nowrap class="tablelist_head_middleandrightline" width="10%">排序号</td>
   </tr>
   
   <%
      // 提交操作
            TtaskDecision performDecision = null;
            if (templateSimpleTask!=null){
               performDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_PERFORM);
            }
      %>
   <tr align="center" style="cursor:hand">
      <td class="tablelist_list_leftline">
         <input type="checkbox" name="selectDecision" 
            value="<%=WFEConstants.WFDECISION_PERFORM%>"
            <%=performDecision != null?"checked":""%>>
      </td>
      <td class="tablelist_list_middleandrightline">
         1
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_PERFORM)%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.WFDECISION_PERFORM%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_PERFORM%>_decisionViewName" 
            value='<%=performDecision != null?performDecision.getViewName():""%>'>
      </td>
      <td class="tablelist_list_middleandrightline">
         <select name="<%=WFEConstants.WFDECISION_PERFORM%>_execType">
            <option value="<%=WFEConstants.DECISION_EXEC_YESNO%>"
               <%=performDecision!=null&&NumberUtil.parseInt(performDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_YESNO?"selected":""%>>
               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%></option>
            <option value="<%=WFEConstants.DECISION_EXEC_EMP%>"
               <%=performDecision!=null&&NumberUtil.parseInt(performDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_EMP?"selected":""%>>
               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_EMP)%></option>
         </select>
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_PERFORM%>_decisionOrderID" 
            value='<%=performDecision != null?performDecision.getOrderID():""%>'>
      </td>
   </tr>
   
   <%
      // 打回操作。
            TtaskDecision turnbackDecision = null;
            if (templateSimpleTask!=null){
               turnbackDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_TURNBACK);
            }
      %>
   <tr align="center" style="cursor:hand">
      <td class="tablelist_list_leftline">
         <input type="checkbox" name="selectDecision" 
            value="<%=WFEConstants.WFDECISION_TURNBACK%>"
            <%=turnbackDecision != null?"checked":""%>>
      </td>
      <td class="tablelist_list_middleandrightline">
         2
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_TURNBACK)%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.WFDECISION_TURNBACK%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_TURNBACK%>_decisionViewName" 
            value='<%=turnbackDecision != null? turnbackDecision.getViewName():""%>'>
      </td>
      <td class="tablelist_list_middleandrightline">
         <select name="<%=WFEConstants.WFDECISION_TURNBACK%>_execType">
               <option value="<%=WFEConstants.DECISION_EXEC_YESNO%>"
	               <%=turnbackDecision!=null&&NumberUtil.parseInt(turnbackDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_YESNO?"selected":""%>>
	               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%></option>
	         <option value="<%=WFEConstants.DECISION_EXEC_EMP%>"
	               <%=turnbackDecision!=null&&NumberUtil.parseInt(turnbackDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_EMP?"selected":""%>>
	               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_EMP)%></option>
         </select>
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_TURNBACK%>_decisionOrderID" 
            value='<%=turnbackDecision != null? turnbackDecision.getOrderID():""%>'>
      </td>
   </tr>
   
   <%
      // 转办操作
            TtaskDecision forwardDecision =  null;
            if (templateSimpleTask!=null){
               forwardDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_FORWARD);
            }
      %>
   <tr align="center" style="cursor:hand">
      <td class="tablelist_list_leftline">
         <input type="checkbox" name="selectDecision" 
            value="<%=WFEConstants.WFDECISION_FORWARD%>"
            <%=forwardDecision != null?"checked":""%>>
      </td>
      <td class="tablelist_list_middleandrightline">
         3
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_FORWARD)%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.WFDECISION_FORWARD%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_FORWARD%>_decisionViewName" 
            value='<%=forwardDecision != null? forwardDecision.getViewName():""%>'>
      </td>
      <td class="tablelist_list_middleandrightline">
         <select name="<%=WFEConstants.WFDECISION_FORWARD%>_execType">
            <option value="<%=WFEConstants.DECISION_EXEC_YESNO%>"
               <%=forwardDecision!=null&&NumberUtil.parseInt(forwardDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_YESNO?"selected":""%>>
               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%></option>
            <option value="<%=WFEConstants.DECISION_EXEC_EMP%>"
               <%=forwardDecision!=null&&NumberUtil.parseInt(forwardDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_EMP?"selected":""%>>
               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_EMP)%></option>
         </select>
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_FORWARD%>_decisionOrderID" 
            value='<%=forwardDecision != null? forwardDecision.getOrderID():""%>'>
      </td>
   </tr>
   
   <%
      // 循环跳转操作
            TtaskDecision reloopDecision = null;
            if (templateSimpleTask!=null){
               reloopDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_RELOOP);
            }
      %>
   <tr align="center" style="cursor:hand">
      <td class="tablelist_list_leftline">
         <input type="checkbox" name="selectDecision" 
            value="<%=WFEConstants.WFDECISION_RELOOP%>"
            <%=reloopDecision != null? "checked":""%>>
      </td>
      <td class="tablelist_list_middleandrightline">
         4
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_RELOOP)%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.WFDECISION_RELOOP%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_RELOOP%>_decisionViewName" 
            value='<%=reloopDecision != null? reloopDecision.getViewName():""%>'>
      </td>
      <td class="tablelist_list_middleandrightline">
         <select name="<%=WFEConstants.WFDECISION_RELOOP%>_execType">
            <option value="<%=WFEConstants.DECISION_EXEC_YESNO%>"
               <%=reloopDecision!=null&&NumberUtil.parseInt(reloopDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_YESNO?"selected":""%>>
               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%></option>
            <option value="<%=WFEConstants.DECISION_EXEC_EMP%>"
               <%=reloopDecision!=null&&NumberUtil.parseInt(reloopDecision.getExecuteType(),0)==WFEConstants.DECISION_EXEC_EMP?"selected":""%>>
               <%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_EMP)%></option>
         </select>
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_RELOOP%>_decisionOrderID" 
            value='<%=reloopDecision != null? reloopDecision.getOrderID():""%>'>
      </td>
   </tr>
   
   <%
      // 回收操作
            TtaskDecision callbackDecision = null;
            if (templateSimpleTask!=null){
               callbackDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_CALLBACK);
            }
      %>
   <tr align="center" style="cursor:hand">
      <td class="tablelist_list_leftline">
         <input type="checkbox" name="selectDecision" 
            value="<%=WFEConstants.WFDECISION_CALLBACK%>"
            <%=callbackDecision != null?"checked":""%>>
      </td>
      <td class="tablelist_list_middleandrightline">
         5
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_CALLBACK)%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.WFDECISION_CALLBACK%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_CALLBACK%>_decisionViewName" 
            value='<%=callbackDecision != null?callbackDecision.getViewName():""%>'>
      </td>
      <td class="tablelist_list_middleandrightline">
      	 <!-- modify aaron_ye at 2010-07-09 -->
         <input type="input" style="width:80px" name="<%=WFEConstants.WFDECISION_CALLBACK%>_execType_readonly" value="<%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%>" disabled/>
         <input type="hidden" name="<%=WFEConstants.WFDECISION_CALLBACK%>_execType" value="<%=WFEConstants.DECISION_EXEC_YESNO%>" />
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_CALLBACK%>_decisionOrderID" 
            value='<%=callbackDecision != null?callbackDecision.getOrderID():""%>'>
      </td>
   </tr>
   
   
   
   <%
            // 同意操作
                  TtaskDecision agreeDecision = null;      
                  if (templateSimpleTask!=null){
                     agreeDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_AGREE);
                  }
            %>
   <tr align="center" style="cursor:hand">
      <td class="tablelist_list_leftline">
         <input type="checkbox" name="selectDecision" 
            value="<%=WFEConstants.WFDECISION_AGREE%>"
            <%=agreeDecision != null?"checked":""%>>
      </td>
      <td class="tablelist_list_middleandrightline">
         6
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_AGREE)%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.WFDECISION_AGREE%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_AGREE%>_decisionViewName" 
            value='<%=agreeDecision != null?agreeDecision.getViewName():""%>'>
      </td>
      <td class="tablelist_list_middleandrightline">
      	 <!-- modify aaron_ye at 2010-07-09 -->
         <input type="input" style="width:80px" name="<%=WFEConstants.WFDECISION_AGREE%>_execType_readonly" value="<%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%>" disabled/>
         <input type="hidden" name="<%=WFEConstants.WFDECISION_AGREE%>_execType" value="<%=WFEConstants.DECISION_EXEC_YESNO%>" />
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_AGREE%>_decisionOrderID" 
            value='<%=agreeDecision != null?agreeDecision.getOrderID():""%>'>
      </td>
   </tr>
   
   <%
      // 不同意
            TtaskDecision disagreeDecision = null;
            if (templateSimpleTask!=null){
               disagreeDecision = templateSimpleTask.getTaskDecision(WFEConstants.WFDECISION_DISAGREE);
            }
      %>
   <tr align="center" style="cursor:hand">
      <td class="tablelist_list_leftline">
         <input type="checkbox" name="selectDecision" 
            value="<%=WFEConstants.WFDECISION_DISAGREE%>"
            <%=disagreeDecision != null?"checked":""%>>
      </td>
      <td class="tablelist_list_middleandrightline">
         7
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_DISAGREE)%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <%=WFEConstants.WFDECISION_DISAGREE%>
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_DISAGREE%>_decisionViewName" 
            value='<%=disagreeDecision != null?disagreeDecision.getViewName():""%>'>
      </td>
      <td class="tablelist_list_middleandrightline">
      	 <input type="input" style="width:80px" name="<%=WFEConstants.WFDECISION_DISAGREE%>_execType_readonly" value="<%=WFEConstants.convertDecisionExecTypeZh(WFEConstants.DECISION_EXEC_YESNO)%>" disabled/>
          <input type="hidden" name="<%=WFEConstants.WFDECISION_DISAGREE%>_execType" value="<%=WFEConstants.DECISION_EXEC_YESNO%>" />
      </td>
      <td class="tablelist_list_middleandrightline">
         <input type="text" class="input_text" name="<%=WFEConstants.WFDECISION_DISAGREE%>_decisionOrderID" 
            value='<%=disagreeDecision != null?disagreeDecision.getOrderID():""%>'>
      </td>
   </tr>
   
   
</table>