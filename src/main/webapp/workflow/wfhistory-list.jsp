<%@ page contentType="text/html;charset=GBK" %>
<%@ include file="/includes/jsp-header.jsp"%>
<%@ page import="java.util.List"%>

<%
BpmInstance workflow = (BpmInstance)request.getAttribute("workflow");
   SimpleTask currentTask = workflow.getNowSimpleTask();
   List historyList = null;
   if (workflow != null){
      historyList = workflow.getWfHistoryList();
   }
%>
<html>
<head>
<title>����ʵ������һ������</title>
</head>
<script language="javascript">

</script>
<body scroll="auto">

<form name="wfHistoryListForm" method="post"> 
<div class="wholeheight">
   <div class="list_head_mainbg">
      <div class="tablelist_head_bg_left"></div>
      <div class="list_head_left_fontbg">����ǰ��λ�ã���������</div>
      <div class="list_head_middle_space"></div>
      <div class="list_head_right"></div>   
      <agdev:toolbar name="toolbar">
      	<agdev:button id="closeButton" name="closeButton" 
             caption="�ر�"
      		link="javascript:window.returnValue=true;window.close();" 
      		image='<%=context + "/images/toolbar/close.gif"%>'/>      		                       
       </agdev:toolbar>
       </div>
   <div class="clear"></div>
   <div class="list_maintank">
      <div class="list_maintank_tablearea">
			<table width="100%" border="0" type="grid" 
     		cellpadding="0" cellspacing="0" name="list_details" 
     		class="list_details">
			   <tr align="center">
			      <td class="text_bold" colspan="10" align="left">
			      ����������
			      </td>
			   </tr>            
			</table>
			<table width="100%" border="0" type="grid" 
       		cellpadding="0" cellspacing="0" name="list_details" 
       		class="list_details">
   		<tr height="22" align="center" class="tablelist_head_bg"> 
      	<td class="tablelist_head_leftline" nowrap colspan="1" align="right">��ǰ��������״̬��</td>
      	<td class="tablelist_head_middleandrightline" nowrap colspan="6" align="left">
         <%
            if (workflow != null){
         %>
         <font color="red"><%=WFEConstants.convertWFStateForExecuteZh(workflow.getWfStatus())%></font>
         <%
            }else{
         %> 
         <font color="red">����ʵ��������</font>
         <%
            }
         %>        
      </td>
   </tr>
   <tr height="22" align="center"  class="tablelist_head_bg"> 
      <td nowrap class="tablelist_head_leftline">������</td>
      <td nowrap class="tablelist_head_middleandrightline">����״̬</td>
      <td nowrap class="tablelist_head_middleandrightline">ִ����</td>
      <td nowrap class="tablelist_head_middleandrightline">ִ��״̬</td>
      <td nowrap class="tablelist_head_middleandrightline">ִ�ж���</td>
      <td nowrap class="tablelist_head_middleandrightline">ִ�����</td>
      <td nowrap class="tablelist_head_middleandrightline">����ʱ��</td>
   </tr>
   <%
   if (currentTask != null){
               List executorList = currentTask.getTaskExecutorList();
               BpmTaskExecute currentExecutor = null;
               if (executorList != null && executorList.size() > 0){
               	int count = executorList.size();
               	for(int i = 0; i < count; i++){
               		currentExecutor = (BpmTaskExecute)executorList.get(i);
      				String colorStr = "";
      				if(currentExecutor.getExecutorStatus() == 0)
      				{
      					colorStr = "color='red'";
      				}
      				System.out.println("cc:"+colorStr);
               		if(0 == i){
   %>
					   <tr align="center"   
					       style="cursor:hand">
					       
					      <td class="tablelist_list_leftline" rowspan="<%=count %>" nowrap>
					         <%=currentTask.getTaskName()%>
					      </td>
					      <td class="tablelist_list_middleandrightline" rowspan="<%=count %>" nowrap><%=WFEConstants.convertWfTaskStatusZh(currentTask.getTaskStatus())%></td>
					      <%
					         if (currentExecutor != null){
					      %>
					      <td class="tablelist_list_middleandrightline" nowrap><%=StruObjectFactory.getOrganNameByOrganId(String.valueOf(currentExecutor.getExecutorID()))%></td>
					      <td class="tablelist_list_middleandrightline" nowrap><font <%=colorStr%>><%=WFEConstants.convertUserExecStateZh(currentExecutor.getExecutorStatus())%></font></td>
					      <%
					         }else{
					      %>
					      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
					      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
					      <%
					         }
					      %>      
					      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
					      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
					      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
					   </tr>
				   <%
         		}else{
         			%>
					   <tr align="center"   
					       style="cursor:hand">
					      <%
					         if (currentExecutor != null){
					      %>
					      <td class="tablelist_list_middleandrightline" nowrap><%=StruObjectFactory.getOrganNameByOrganId(String.valueOf(currentExecutor.getExecutorID()))%></td>
					      <td class="tablelist_list_middleandrightline" nowrap><font <%=colorStr%>><%=WFEConstants.convertUserExecStateZh(currentExecutor.getExecutorStatus())%></font></td>
					      <%
					         }else{
					      %>
					      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
					      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
					      <%
					         }
					      %>      
					      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
					      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
					      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
					   </tr>
				   <%
         		
         		}
         	}
         }
      }else{
		   %>
			   <tr align="center" class="listeven" onmouseover="mouseOver(this)" onmouseout="mouseOut(this)"  style="cursor:hand">
			      <td class="tablelist_list_middleandrightline" nowrap>
			         &nbsp;
			      </td>
			      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
			      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
			      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
			      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
			      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
			      <td class="tablelist_list_middleandrightline" nowrap>&nbsp;</td>
			   </tr>
		   <%
      }
   %>
</table>

<br>
<table border="0" width="100%" cellpadding="0" cellspacing="0" class="listtable" style="word-break:keep-all;">
   <tr align="center">
      <td class="text_bold" colspan="10" align="left">
      ����������Ϣһ��
      </td>
   </tr>            
</table>

<table width="100%" border="0" type="grid" 
         cellpadding="0" cellspacing="0" name="list_details" 
         class="list_details">
   <tr height="22" align="center" class="tablelist_head_bg"> 
      <td nowrap class="tablelist_head_leftline" width="1%">���</td>
      <td nowrap class="tablelist_head_middleandrightline" width="15%">������</td>
      <td nowrap class="tablelist_head_middleandrightline" width="15%">ִ����</td>
      <td nowrap class="tablelist_head_middleandrightline" width="15%">ִ�ж���</td>
      <td nowrap class="tablelist_head_middleandrightline" width="30%">ִ�����</td>
      <td nowrap class="tablelist_head_middleandrightline">����ʱ��</td>
   </tr>
   <%
   if (workflow != null && historyList != null && historyList.size() > 0){
                     int listSize = historyList.size();
                     for(int i = 0; i < listSize; i++){
                        History history = (History)historyList.get(i);
                        BpmTask abTask = workflow.getWfTaskByID(history.getWfTaskID());
   %>
   <tr align="center"   
            style="cursor:hand">
      <td class="tablelist_list_leftline" nowrap><%=i+1%></td>
      <td class="tablelist_list_middleandrightline" nowrap><%=TextUtil.nvl(workflow.getWfTaskName(history.getWfTaskID()))%></td>      
      <td class="tablelist_list_middleandrightline" nowrap><%=history.getHistoryCreateEmpName()%></td>
      <td class="tablelist_list_middleandrightline" nowrap><%=history.getWfTaskDecisionNameZh()%></td>
      <td class="tablelist_list_middleandrightline"><textarea rows="5"  readonly="readonly"><%=TextUtil.nvl(history.getWfExecuteHistory())%></textarea></td>
      <td class="tablelist_list_middleandrightline" nowrap><%=DateUtil.formatDate(history.getHistoryCreateDatetime())%></td>
   </tr>
   <%
         }
      }
   %>
</table>   
</div> 
   <div class="clear"></div>
</div>
</form>  
<iframe name="operInnerFrame" width="0" height="0"></iframe>  
</body>
</html>