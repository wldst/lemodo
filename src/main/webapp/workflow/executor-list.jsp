<%@ page language="java" contentType="text/html; charset=GBK" pageEncoding="GBK"%>
<%@ include file="../../tag.inc" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>

<%
		  System.out.println("dd:");

   List xmlWFTaskExecutorList = (List)request.getAttribute("xmlWFTaskExecutorList");
  // String paginateHtmlScript = (String)request.getAttribute("paginateHtmlScript");
%>
<html>
<head>
<title>流程任务可选执行人一览画面</title>
<jsp:include flush="false"  page="../../header.jsp"></jsp:include>
</head>
<script language="javascript">
   //单击可选人员
   function clickXmlTaskExecutor(obj){
	   alert("si");
      scanItem();
      var objValue;

      objValue = obj.itemEmpValue;
      document.all.hiddenClickExecutor.value = objValue;
      obj.style.backgroundColor = "#3366aa";
   }
   
   //扫描已选项,将已选项的背景颜色改变为已选中状态
   function scanItem()
   {
     var selectItemObj = document.getElementById("hiddenClickExecutor");
     var itemObj = document.all.itemEmpTR;
     
     if ( itemObj == null)
     {
        return false;
     }
     //清除已设置颜色
     clearAllTRColor( itemObj );
     document.all.hiddenClickExecutor.value = "";    
   }
   
   //清除所有TR背景颜色
   function clearAllTRColor( obj )
   {
      var itemObj = obj;
      if ( itemObj.length == undefined )
      {
         itemObj.style.backgroundColor = "";
      }
      else
      {
         for ( var i = 0; i< itemObj.length ; i++ )
           {
              itemObj[i].style.backgroundColor = "";
           }
      }
   }


function dd(rowIndex, rowData){
				var itemVal = rowData.ID+"#"+rowData.EMPNAME;
				parent.addSelectItem(itemVal);
				
			}


</script>
<body scroll="no">
<div class="easyui-layout" fit="true" style="width: 100%;">
<form name="executorListForm" method="post"> 
<div region="center" border="false">
<input type="hidden" id="flagName" name="flagName" value="0">   
<input type="hidden" id="hiddenClickExecutor" name="hiddenClickExecutor" value="">

<table id="executorTab" class="easyui-datagrid" fit="true" data-options="onDblClickRow:dd">
	<thead> 
	<tr>
		<th data-options="field:'itemid',width:40,align:'center',resizable:false">序号</th>
		<th data-options="field:'EMPNAME',width:140,align:'center',resizable:false">名称</th>
		<th data-options="field:'ID',width:140,align:'center',resizable:false, hidden:true">ID</th>
	</tr>
	</thead>
	<tbody>
	   <%
  
      if (xmlWFTaskExecutorList != null && xmlWFTaskExecutorList.size() > 0){
         int listSize = xmlWFTaskExecutorList.size();
		  System.out.println("dd:"+listSize);
         for(int i = 0; i < listSize; i++){
            Map tempUser = (Map)xmlWFTaskExecutorList.get(i);
			System.out.println(tempUser);
   %>
 	<tr align="center" style="cursor: hand" onDblClick="parent.addSelectItem(this);" 
	onclick="clickXmlTaskExecutor(this);" id="itemEmpTR"  >
      <td>
         <%=i+1%></td>
      <td>
         <%=(String)tempUser.get("EMPNAME")%></td>
      <td>
         <%=(String)tempUser.get("ID")%></td>
   </tr>
   <%
         }
      }
   %>

	</tbody>
</table>

</div>
</form>

</div>

</body>
</html>