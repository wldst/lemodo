
<table width="98%" cellpadding="2" cellspacing="2" class="addtable"
	align="center">
	<tr>
		<td class="label" nowrap width="15%"></td>
		<td class="tdinput" nowrap width="35%"><input type="text"
			class="input_text" name="name" id="name"
			value="<%=templateSimpleTask.getWfTaskName()%>"></td>
	</tr>
	<tr>
		<td class="label" nowrap width="15%"></td>
		<td class="tdinput" nowrap width="35%"><input type="text"
			class="input_text" name="innerTaskID" id="innerTaskID"
			value="<%=templateSimpleTask.getWfTaskInnerID()%>"></td>
	</tr>
	<tr>
		<td class="label" nowrap width="15%"></td>
		<td class="tdinput" nowrap width="35%"><input type="text"
			class="input_text" name="next" id="next"
			value="<%=templateSimpleTask.getNext()%>"></td>
	</tr>
	<tr>
		<td class="label" nowrap width="15%"></td>
		<td class="tdinput" nowrap width="35%"><input type="text"
			class="input_text" name="previous" id="previous"
			value="<%=templateSimpleTask.getPrevious()%>"></td>
	</tr>
	<tr>
		<td class="label" nowrap width="15%"></td>
		<td class="tdinput" nowrap width="35%"><textarea
			name="description" id="description" cols="40" rows="5"><%=templateSimpleTask.getDescript()%></textarea>
		</td>
	</tr>
</table>