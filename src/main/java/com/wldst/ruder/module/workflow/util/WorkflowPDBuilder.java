package com.wldst.ruder.module.workflow.util;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.module.workflow.beans.History;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.template.beans.TemplateTask;
import com.wldst.ruder.module.workflow.template.beans.Toutput;
import com.wldst.ruder.module.workflow.template.beans.TbTask;
import com.wldst.ruder.module.workflow.template.beans.Tend;
import com.wldst.ruder.module.workflow.template.beans.Tshrink;
import com.wldst.ruder.module.workflow.template.beans.Tsimple;
import com.wldst.ruder.module.workflow.template.beans.Tstart;
import com.wldst.ruder.module.workflow.template.beans.Tshape;
import com.wldst.ruder.module.workflow.template.beans.Twf;
import com.wldst.ruder.util.MapTool;

/**
 * 流程VML或JSon等定义生成常用方法定义
 * 
 * @author wldst
 */
public class WorkflowPDBuilder {
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(WorkflowPDBuilder.class);

    // 流程VML定义生成常用方法定义静态实例对象
    private static WorkflowPDBuilder instance;

    /**
     * 屏蔽默认构造函数
     */
    private WorkflowPDBuilder() {

    }

    /**
     * 获取流程VML定义生成常用方法静态实例对象
     * 
     * @return 流程常用方法
     */
    public synchronized static WorkflowPDBuilder getInstance() {
	if (instance == null) {
	    instance = new WorkflowPDBuilder();
	}
	return instance;
    }

    /**
     * 生成流程模板信息VML代码
     * 
     * @param tempWf 流程模板
     * @return VML代码
     * @throws Exception
     */
    public String buildWfTemplateVML(Map<String, Object> tempWf) throws CrudBaseException {
	if (tempWf == null) {
	    throw new CrudBaseException("不能生成图形履历信息,模板文件对象为空");
	}
	StringBuffer retBuffer = new StringBuffer();
	// 绘制开始任务图形
	Map<String, Object> tempStartTask = Twf.getStartTask(tempWf);
	retBuffer.append(this.buildStartTaskVML2(tempStartTask));

	// 绘制结束任务图形
	Map<String, Object> tempEndTask =  Twf.getEndTask(tempWf);
	retBuffer.append(this.buildEndTaskVML2(tempEndTask));

	// 绘制普通任务图形
	List<Map<String, Object>> simpleTaskList =  Twf.getSimpleTasks(tempWf);
	if (simpleTaskList != null && simpleTaskList.size() > 0) {
	    int listSize = simpleTaskList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempSimpleTask = simpleTaskList.get(i);
		retBuffer.append(this.buildSimpleTaskVML(tempSimpleTask));
	    }
	}

	// 绘制收缩任务图形
	List<Map<String, Object>> templateShrinkList = Twf.getShrinTaskList(tempWf);
	if (templateShrinkList != null && templateShrinkList.size() > 0) {
	    int listSize = templateShrinkList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempShrinkTask = templateShrinkList.get(i);
		retBuffer.append(this.buildShrinkTaskVML2(tempShrinkTask));
	    }
	}

	// 绘制分支任务图形
	List<Map<String, Object>> templateBranchList = Twf.getBranchTask(tempWf);
	if (templateBranchList != null && templateBranchList.size() > 0) {
	    int listSize = templateBranchList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempBranchTask = templateBranchList.get(i);
		retBuffer.append(this.buildBranchTaskVML2(tempBranchTask));
	    }
	}

	// 绘制任务之间的关系连线图形
	retBuffer.append(this.buildArrowLine(tempWf, tempStartTask));
	if (simpleTaskList != null && simpleTaskList.size() > 0) {
	    int listSize = simpleTaskList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempSimpleTask = simpleTaskList.get(i);
		retBuffer.append(this.buildArrowLine(tempWf, tempSimpleTask));
	    }
	}

	if (templateShrinkList != null && templateShrinkList.size() > 0) {
	    int listSize = templateShrinkList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempShrinkTask = templateShrinkList.get(i);
		retBuffer.append(this.buildArrowLine(tempWf, tempShrinkTask));
	    }
	}
	if (templateBranchList != null && templateBranchList.size() > 0) {
	    int listSize = templateBranchList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempBranchTask = templateBranchList.get(i);
		retBuffer.append(this.buildArrowLine(tempWf, tempBranchTask));
	    }
	}
	return retBuffer.toString();
    }

    /**
     * 生成流程实例的VML图形履历信息
     * 
     * @param tempWf 流程模板信息
     * @param wf     流程实例对象
     * @return VML代码
     * @throws CrudBaseException
     */
    public String buildHistoryVML(Map<String, Object> tempWf, Map<String, Object> wf) throws CrudBaseException {
	if (tempWf == null || wf == null) {
	    throw new CrudBaseException("不能生成图形履历信息,模板文件或流程实例对象为空");
	}
	StringBuffer retBuffer = new StringBuffer();

	// 绘制开始任务图形
	Map<String, Object> tempStartTask = Twf.getStartTask(tempWf);
	retBuffer.append(this.buildStartTaskVML(tempStartTask));

	// 绘制结束任务图形
	Map<String, Object> tempEndTask =  Twf.getEndTask(tempWf);
	retBuffer.append(this.buildEndTaskVML(tempEndTask));

	// 绘制普通任务图形
	List<Map<String, Object>> simpleTaskList =  Twf.getSimpleTasks(tempWf);
	if (simpleTaskList != null && simpleTaskList.size() > 0) {
	    int listSize = simpleTaskList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempSimpleTask = simpleTaskList.get(i);
		retBuffer.append(this.buildSimpleTaskVML(tempSimpleTask, wf));
	    }
	}

	// 绘制收缩任务图形
	List<Map<String, Object>> templateShrinkList =  Twf.getShrinTaskList(tempWf);
	if (templateShrinkList != null && templateShrinkList.size() > 0) {
	    int listSize = templateShrinkList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempShrinkTask = templateShrinkList.get(i);
		retBuffer.append(this.buildShrinkTaskVML(tempShrinkTask));
	    }
	}

	// 绘制分支任务图形
	List<Map<String, Object>> templateBranchList =  Twf.getBranchTask(tempWf);
	if (templateBranchList != null && templateBranchList.size() > 0) {
	    int listSize = templateBranchList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempBranchTask = (Map<String, Object>) templateBranchList.get(i);
		retBuffer.append(this.buildBranchTaskVML(tempBranchTask));
	    }
	}

	// 绘制任务之间的关系连线图形
	retBuffer.append(this.buildArrowLine(tempWf, tempStartTask));
	if (simpleTaskList != null && simpleTaskList.size() > 0) {
	    int listSize = simpleTaskList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempSimpleTask = simpleTaskList.get(i);
		retBuffer.append(this.buildArrowLine(tempWf, tempSimpleTask));
	    }
	}

	if (templateShrinkList != null && templateShrinkList.size() > 0) {
	    int listSize = templateShrinkList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempShrinkTask = templateShrinkList.get(i);
		retBuffer.append(this.buildArrowLine(tempWf, tempShrinkTask));
	    }
	}
	if (templateBranchList != null && templateBranchList.size() > 0) {
	    int listSize = templateBranchList.size();
	    for (int i = 0; i < listSize; i++) {
		Map<String, Object> tempBranchTask = templateBranchList.get(i);
		retBuffer.append(this.buildArrowLine(tempWf, tempBranchTask));
	    }
	}
	return retBuffer.toString();
    }

    /**
     * 生成开始任务图形
     * 
     * @param tempStartTask 开始任务模板信息
     * @return vml代码
     * @throws CrudBaseException
     */
    private String buildStartTaskVML(Map<String, Object> tempStartTask) throws CrudBaseException {
	Map<String, Object> shape = BpmDo.shape(tempStartTask);

	StringBuffer retBuffer = new StringBuffer();
	retBuffer.append("<v:oval ");
	retBuffer.append(" onDblClick=\"" + "\"");
	retBuffer.append(" title=\"任务名称：开始任务\"");
	retBuffer.append(" style='cursor:hand;POSITION:absolute;Z-INDEX:2;LEFT:");
	appendShape(tempStartTask, retBuffer);
	retBuffer.append(";' strokeweight='2px' fillcolor=\"black\">\n");
	retBuffer.append("</v:oval>\n");
	return retBuffer.toString();
    }

    /**
     * 生成开始任务图形
     * 
     * @param tempStartTask 开始任务模板信息
     * @return vml代码
     * @throws CrudBaseException
     */
    private String buildStartTaskVML2(Map<String, Object> tempStartTask) throws CrudBaseException {
	String taskInnerId = BpmDo.taskInnerId(tempStartTask);
	StringBuffer retBuffer = new StringBuffer();
	retBuffer.append("<v:oval ");
	retBuffer.append(" onDblClick=\"popup_task_page('");
	retBuffer.append(taskInnerId + "')\"");
	retBuffer.append(" title=\"任务名称：开始任务 ；\n");
	retBuffer.append("内部ID：" + taskInnerId);
	retBuffer.append("\"");
	retBuffer.append(" style='cursor:hand;POSITION:absolute;Z-INDEX:2;LEFT:");
	appendShape(tempStartTask, retBuffer);
	retBuffer.append(";' strokeweight='2px' fillcolor=\"black\">\n");
	retBuffer.append("</v:oval>\n");
	return retBuffer.toString();
    }

    /**
     * 生成结束任务图形VML
     * 
     * @param tempEndTask 结束任务模板信息
     * @return VML代码
     * @throws CrudBaseException
     */
    private String buildEndTaskVML(Map<String, Object> tempEndTask) throws CrudBaseException {
	Map<String, Object> shape = BpmDo.shape(tempEndTask);

	StringBuffer buildBuffer = new StringBuffer();
	buildBuffer.append("<v:oval ");
	buildBuffer.append(" title=\"任务名称：结束任务；\"");
	buildBuffer.append(" onDblClick=\"" + "\"");
	buildBuffer.append(" style='cursor:hand;POSITION:absolute;Z-INDEX:2;LEFT:");
	appendShape(tempEndTask, buildBuffer);
	buildBuffer.append(";' strokeweight='2px' fillcolor=\"black\">\n");
	buildBuffer.append("</v:oval>\n");

	return buildBuffer.toString();
    }

    /**
     * 生成结束任务图形VML
     * 
     * @param tempEndTask 结束任务模板信息
     * @return VML代码
     * @throws CrudBaseException
     */
    private String buildEndTaskVML2(Map<String, Object> tempEndTask) throws CrudBaseException {
	StringBuffer buildBuffer = new StringBuffer();
	buildBuffer.append("<v:oval ");
	buildBuffer.append(" title=\"任务名称：结束任务；\n");
	buildBuffer.append("内部ID：" + BpmDo.taskInnerId(tempEndTask));
	buildBuffer.append("\"");
	buildBuffer.append(" onDblClick=\"popup_task_page('");
	buildBuffer.append(BpmDo.taskInnerId(tempEndTask) + "')\"");
	buildBuffer.append(" style='cursor:hand;POSITION:absolute;Z-INDEX:2;LEFT:");
	appendShape(tempEndTask, buildBuffer);
	buildBuffer.append(";' strokeweight='2px' fillcolor=\"black\">\n");
	buildBuffer.append("</v:oval>\n");

	return buildBuffer.toString();
    }

    private void appendShape(Map<String, Object> tempEndTask, StringBuffer buildBuffer) {
	Map<String, Object> shape = BpmDo.shape(tempEndTask);
	buildBuffer.append(BpmDo.nodeX(shape));
	buildBuffer.append(";TOP:");
	buildBuffer.append(BpmDo.nodeY(shape));
	buildBuffer.append(";width:");
	buildBuffer.append(BpmDo.nodeW(shape) / 2);
	buildBuffer.append(";height:");
	buildBuffer.append(BpmDo.nodeH(shape) / 2);
    }

    /**
     * 生成普通任务VML代码
     * 
     * @param tempSimpleTask 普通任务模板信息
     * @return VML代码
     * @throws CrudBaseException
     */
    private String buildSimpleTaskVML(Map<String, Object> tempSimpleTask) throws CrudBaseException {
	String taskInnerId = BpmDo.taskInnerId(tempSimpleTask);
	StringBuffer buildBuffer = new StringBuffer();
	buildBuffer.append("<v:rect ");
	buildBuffer.append(" onDblClick=\"popup_task_page('");
	buildBuffer.append(taskInnerId + "')\"");

	String taskName = BpmDo.taskName(tempSimpleTask);
	buildBuffer.append(" title=\"任务名称：" + taskName + "\n");
	buildBuffer.append("内部ID：" + taskInnerId);
	buildBuffer.append("\"");

	buildBuffer.append(" style='cursor:hand;POSITION:absolute;Z-INDEX:2;LEFT:");

	appendSimpleShape(tempSimpleTask, buildBuffer);

	buildBuffer.append(";' strokeweight='2px' fillcolor=\"yellow\">\n");

	buildBuffer.append("<v:TextBox inset='5pt,5pt,5pt,5pt' style='font-size:10pt;'>");
	buildBuffer.append(taskName);
	buildBuffer.append("</v:TextBox>\n");
	buildBuffer.append("</v:rect>\n");

	return buildBuffer.toString();
    }

    private void appendSimpleShape(Map<String, Object> tempSimpleTask, StringBuffer buildBuffer) {
	Map<String, Object> shape = BpmDo.shape(tempSimpleTask);
	;
	buildBuffer.append(BpmDo.nodeX(shape));
	buildBuffer.append(";TOP:");
	buildBuffer.append(BpmDo.nodeY(shape));
	buildBuffer.append(";width:");
	buildBuffer.append(BpmDo.nodeW(shape));
	buildBuffer.append(";height:");
	buildBuffer.append(String.valueOf(BpmDo.nodeH(shape)));
    }

    /**
     * 生成普通任务VML代码
     * 
     * @param tempSimpleTask 普通任务模板信息
     * @return VML代码
     * @throws CrudBaseException
     */
    private String buildSimpleTaskVML(Map<String,Object> tempSimpleTask,  Map<String, Object> workflow) throws CrudBaseException {
	Map<String,Object> abTask = BpmInstance.getWfTaskByInnerID(BpmDo.taskInnerId(tempSimpleTask), workflow);
	String taskName = BpmDo.taskName(tempSimpleTask);
	StringBuffer buildBuffer = new StringBuffer();
	buildBuffer.append("<v:rect ");
	buildBuffer.append(" onDblClick=\"\"");
	if (abTask != null) {
	    List<Map<String,Object>> historyList = BpmInstance.getWfHistoryList(BpmDo.id(abTask), workflow);
	    buildBuffer.append(" title=\"任务名称：" + taskName + "\n");
	    // 增加流程任务履历信息
	    if (historyList != null && historyList.size() > 0) {
		int listSize = historyList.size();
		for (int i = 0; i < listSize; i++) {
		    Map<String,Object> history =  historyList.get(i);

		    buildBuffer.append("执 行 人：" + MapTool.string(history,"historyCreateEmpName"));
		    buildBuffer.append("\n");
		    buildBuffer.append("执行时间：" + DateUtils.formatDate(new Date(MapTool.longValue(history,"historyCreateDatetime"))));
		    buildBuffer.append("\n");
		    buildBuffer.append("执行动作：" + MapTool.string(history,"wfTaskDecisionNameZh"));
		    buildBuffer.append("\n");
		    buildBuffer.append("执行意见：" + MapTool.string(history,"wfExecuteHistory"));
		}
	    }
	    buildBuffer.append("\"");
	} else {
	    buildBuffer.append(" title=\"任务名称：" + taskName + "\"");
	}
	buildBuffer.append(" style='cursor:hand;POSITION:absolute;Z-INDEX:2;LEFT:");

	appendSimpleShape(tempSimpleTask, buildBuffer);
	if (abTask != null && BpmDo.taskStatus(abTask) == WFEConstants.WFTASK_STATUS_END) {
	    buildBuffer.append(";' strokeweight='2px' fillcolor=\"red\">\n");
	} else if (abTask != null && BpmDo.taskStatus(abTask) == WFEConstants.WFTASK_STATUS_READY) {
	    buildBuffer.append(";' strokeweight='2px' fillcolor=\"gray\">\n");
	} else {
	    buildBuffer.append(";' strokeweight='2px' fillcolor=\"yellow\">\n");
	}
	buildBuffer.append("<v:TextBox inset='5pt,5pt,5pt,5pt' style='font-size:10pt;'>");
	
	buildBuffer.append(taskName);
	buildBuffer.append("</v:TextBox>\n");
	buildBuffer.append("</v:rect>\n");

	return buildBuffer.toString();
    }

    /**
     * 生成收缩节点图形VML代码
     * 
     * @param tempShrinkTask 收缩节点模板信息
     * @return VML代码
     * @throws CrudBaseException
     */
    private String buildShrinkTaskVML2(Map<String, Object> tempShrinkTask) throws CrudBaseException {
	Map<String, Object> shape = BpmDo.shape(tempShrinkTask);

	StringBuffer buildBuffer = new StringBuffer();
	buildBuffer.append("<v:shape type=\"#shrinkNode\"");
	buildBuffer.append(" title=\"任务名称：收缩任务 ；\n");
	buildBuffer.append("内部ID：" + BpmDo.taskInnerId(tempShrinkTask));
	buildBuffer.append("\"");
	buildBuffer.append(" onDblClick=\"popup_task_page('");
	buildBuffer.append(BpmDo.taskInnerId(tempShrinkTask) + "')\"");
	buildBuffer.append(" style=\"cursor:hand;position:relative;");
	shapeInfo(shape, buildBuffer);
	buildBuffer.append("\" fillcolor=\"blue\"/>");
	return buildBuffer.toString();
    }

    /**
     * 生成收缩节点图形VML代码
     * 
     * @param tempShrinkTask 收缩节点模板信息
     * @return VML代码
     * @throws CrudBaseException
     */
    private String buildShrinkTaskVML(Map<String, Object> tempShrinkTask) throws CrudBaseException {
	Map<String, Object> shape = BpmDo.shape(tempShrinkTask);

	StringBuffer buildBuffer = new StringBuffer();
	buildBuffer.append("<v:shape type=\"#shrinkNode\"");
	buildBuffer.append(" title=\"任务名称：收缩任务\"");
	buildBuffer.append(" onDblClick=\"\"");
	buildBuffer.append(" style=\"cursor:hand;position:relative;");
	shapeInfo(shape, buildBuffer);
	buildBuffer.append("\" fillcolor=\"blue\"/>");
	return buildBuffer.toString();
    }

    /**
     * 生成分支任务图形VML代码
     * 
     * @param tempBranchTask 分支任务模板信息
     * @return VML代码
     * @throws CrudBaseException
     */
    private String buildBranchTaskVML(Map<String, Object> tempBranchTask) throws CrudBaseException {
	Map<String, Object> shape = BpmDo.shape(tempBranchTask);
	StringBuffer buildBuffer = new StringBuffer();
	buildBuffer.append("<v:shape type=\"#branchNode\"");
	buildBuffer.append(" title=\"任务名称：分支任务\"");
	buildBuffer.append(" onDblClick=\"\"");
	buildBuffer.append(" style=\"cursor:hand;position:relative;");
	shapeInfo(shape, buildBuffer);
	buildBuffer.append("\" fillcolor=\"blue\"/>");
	return buildBuffer.toString();
    }

    /**
     * 生成分支任务图形VML代码<br>
     * 含js代码
     * 
     * @param tempBranchTask 分支任务模板信息
     * @return VML代码
     * @throws CrudBaseException
     */
    private String buildBranchTaskVML2(Map<String, Object> tempBranchTask) throws CrudBaseException {
	Map<String, Object> shape = BpmDo.shape(tempBranchTask);
	StringBuffer buildBuffer = new StringBuffer();
	buildBuffer.append("<v:shape type=\"#branchNode\"");
	buildBuffer.append(" title=\"任务名称：" + BpmDo.taskName(tempBranchTask));
	buildBuffer.append("\n内部ID：" + BpmDo.taskInnerId(tempBranchTask));
	buildBuffer.append("\"");
	buildBuffer.append(" onDblClick=\"popup_task_page('");
	buildBuffer.append(BpmDo.taskInnerId(tempBranchTask) + "')\"");
	buildBuffer.append(" style=\"cursor:hand;position:relative;");
	shapeInfo(shape, buildBuffer);
	buildBuffer.append("\" fillcolor=\"blue\"/>");
	return buildBuffer.toString();
    }

    private void shapeInfo(Map<String, Object> shape, StringBuffer buildBuffer) {
	buildBuffer.append("width:");
	buildBuffer.append(BpmDo.nodeW(shape) / 2);
	buildBuffer.append(";height:");
	buildBuffer.append(BpmDo.nodeW(shape) / 2);
	buildBuffer.append(";");
	buildBuffer.append("left:" + BpmDo.nodeX(shape));
	buildBuffer.append(";top:" + BpmDo.nodeY(shape));
    }
    
    private void shapeInfo2(Map<String, Object> shape, StringBuffer buildBuffer) {
	buildBuffer.append("width:");
	buildBuffer.append(BpmDo.nodeW(shape));
	buildBuffer.append(";height:");
	buildBuffer.append(BpmDo.nodeW(shape));
	buildBuffer.append(";");
	buildBuffer.append("left:" + BpmDo.nodeX(shape));
	buildBuffer.append(";top:" + BpmDo.nodeY(shape));
    }

    /**
     * 根据传入的任务，绘制任务所关联的箭头图形VML
     * 
     * @param tempWf     流程模板信息
     * @param abTempTask 流程模板任务
     * @return VML代码
     * @throws CrudBaseException
     */
    private String buildArrowLine(Map<String, Object> tempWf, Map<String, Object> abTempTask) throws CrudBaseException {
	Map<String, Object> fromShape = BpmDo.shape(abTempTask);

	StringBuffer buildBuffer = new StringBuffer();
	Integer taskType = BpmDo.taskType(abTempTask);
	
	if (taskType  ==BpmDo.WFTASK_TYPE_BRANCH) {// 分支任务
	    List<Map<String, Object>> outputList = BpmDo.outs(abTempTask);
	    if (outputList != null && outputList.size() > 0) {
		int listSize = outputList.size();
		for (int i = 0; i < listSize; i++) {
		    Map<String, Object> output =  outputList.get(i);
		    Map<String, Object> nextTempTask = Twf.getByInnerID(BpmDo.output(output),tempWf);
		    if (nextTempTask != null) {
			arrowLineToNextTask(fromShape, buildBuffer, nextTempTask);
		    }
		}
	    }
	}else {
	    Map<String, Object> nextTempTask = Twf.getByInnerID(BpmDo.output(abTempTask),tempWf);
	    if (nextTempTask != null) {
		arrowLineToNextTask(fromShape, buildBuffer, nextTempTask);
	    }
	}
	
	return buildBuffer.toString();
    }
    /**
     * int fromXIndex = fromShape.getNodeX() + fromShape.getNodeW() / 2;
	    int fromYIndex = fromShape.getNodeY() + fromShape.getNodeW() / 4;
	    int endXIndex = toShape.getNodeX();
	    int endYIndex = toShape.getNodeY() + toShape.getNodeH() / 2;
     * 指向下一个任务的箭头
     * @param fromShape
     * @param buildBuffer
     * @param nextTempTask
     */
    private void arrowLineToNextTask(Map<String, Object> fromShape, StringBuffer buildBuffer,
	    Map<String, Object> nextTempTask) {
	Map<String, Object> toShape = BpmDo.shape(nextTempTask);
	
	int fromXIndex = BpmDo.fromXW2(fromShape);
	int fromYIndex = BpmDo.fromYW4(fromShape);
	int endXIndex = BpmDo.nodeX(toShape);
	int endYIndex = BpmDo.toY(toShape);	    
	if (BpmDo.taskType(toShape) ==BpmDo.WFTASK_TYPE_END) {
	    endYIndex = BpmDo.toEndY(toShape);
	}
	buildBuffer.append(this.arrowLine(fromXIndex, fromYIndex, endXIndex, endYIndex));
    }

    /**
     * 绘制箭头图形VML代码
     * 
     * @param fromXIndex 起始X坐标
     * @param fromYIndex 其实Y坐标
     * @param endXIndex  结束X坐标
     * @param endYIndex  结束Y坐标
     * @return VML代码
     */
    private String arrowLine(int fromXIndex, int fromYIndex, int endXIndex, int endYIndex) {
	StringBuffer retBuffer = new StringBuffer();
	retBuffer.append("<v:line style=\"position:relative\"");
	retBuffer.append(" from=\"" + fromXIndex + "," + fromYIndex + "\"");
	retBuffer.append(" to=\"" + endXIndex + "," + endYIndex + "\">\n");
	retBuffer.append("<v:stroke EndArrow=\"Classic\"/>\n");
	retBuffer.append("</v:line>\n");
	return retBuffer.toString();
    }

    /**
     * 根据流程模板信息生成流程JSON串
     * 
     * @param wfTemplate 流程模板信息
     * @return JSON串
     * @throws CrudBaseException
     */
    public String buildWfTemplateJson(Map<String, Object> wfTemplate) throws CrudBaseException {
	String retStr = null;
	JSONObject tempObj = null;
	try {
	    if (wfTemplate != null) {
		tempObj = new JSONObject();
		tempObj.put("id",  Twf.getID(wfTemplate));
		tempObj.put("name",  Twf.getWfName(wfTemplate));
		tempObj.put("count", Twf.getMaxNo(wfTemplate));

		// 开始任务处理
		Map<String, Object> startTask = Twf.getStartTask(wfTemplate);
		JSONArray nodes = tempObj.getJSONArray("nodes");

		if (startTask != null) {
		    Map<String, Object> startTaskShape = startTask(startTask, nodes);
		    lineTask(wfTemplate, tempObj, startTask, startTaskShape);
		}

		// 结束任务处理
		Map<String, Object> endTask = Twf.getEndTask(wfTemplate);
		if (endTask != null) {
		    endTask(nodes, endTask);
		}

		// 普通任务处理
		List<Map<String, Object>> simpleTaskList =  Twf.getSimpleTasks(wfTemplate);
		if (simpleTaskList != null && simpleTaskList.size() > 0) {
		    for (Map<String, Object> simpleTask : simpleTaskList) {
			Map<String, Object> sts = simpleTask(nodes, simpleTask);
			simpleTaskLine(wfTemplate, tempObj, simpleTask, sts);
		    }
		}

		// 分支任务处理
		List<Map<String, Object>> branchTaskList = Twf.getBranchTask(wfTemplate);;
		if (branchTaskList != null && branchTaskList.size() > 0) {
		    for (Map<String, Object> branchTask : branchTaskList) {
			branchTask(nodes, branchTask);
			List<Map<String, Object>> outList = BpmDo.outs(branchTask);
			if (outList != null && outList.size() > 0) {
			    int outListSize = outList.size();
			    for (int j = 0; j < outListSize; j++) {
				Map<String, Object> output = outList.get(j);

				JSONObject line = new JSONObject();
				line.put("id", "line_" + TextUtil.substring(BpmDo.taskInnerId(branchTask), "-"));
				line.put("name", "line_" + TextUtil.substring(BpmDo.taskInnerId(branchTask), "-"));
				line.put("type", "line");
				line.put("shape", "line");
				line.put("from", "branch_" + TextUtil.substring(BpmDo.taskInnerId(branchTask), "-"));

				Map<String, Object> nextTask =  Twf.getByInnerID(BpmDo.output(output),wfTemplate);
				BpmDo.lineTo(line, nextTask);
				from2Shape(BpmDo.shape(branchTask), line, nextTask);
				line.put("polydot", new JSONArray());
				line.put("number", 0);
				line.put("property", new JSONArray());
				nodes.add(line);
			    }
			}
		    }
		}

		// 收缩任务处理
		List<Map<String, Object>> shrinkTaskList =  Twf.getShrinTaskList(wfTemplate);
		if (shrinkTaskList != null && shrinkTaskList.size() > 0) {
		    int listSize = shrinkTaskList.size();
		    for (int i = 0; i < listSize; i++) {
			Map<String, Object> shrinkTask = shrinkTaskList.get(i);
			shrinkTask(nodes, shrinkTask);
			shrinkLines(wfTemplate, tempObj, shrinkTask);
		    }
		}
	    }

	    if (tempObj != null) {
		retStr = tempObj.toString();
	    }
	} catch (Exception ex) {
	    logger.error("生成流程模板的JSon串错误:", ex);
	    throw new CrudBaseException("生成流程模板的JSon串错误:", ex);
	}
	return retStr;
    }

    private void shrinkLines(Map<String, Object> tWorkflow, JSONObject tempObj, Map<String, Object> shrinkTask) {
	JSONObject line = new JSONObject();
	line.put("id", "line_" + TextUtil.substring(BpmDo.taskInnerId(shrinkTask), "-"));
	line.put("name", "line_" + TextUtil.substring(BpmDo.taskInnerId(shrinkTask), "-"));
	line.put("type", "line");
	line.put("shape", "line");
	line.put("from", "shrink_" + TextUtil.substring(BpmDo.taskInnerId(shrinkTask), "-"));

	Map<String, Object> nextTask = Twf.getByInnerID(BpmDo.output(shrinkTask),tWorkflow);
	if (nextTask != null) {
	    BpmDo.lineTo(line, nextTask);
	    from2Shape(BpmDo.shape(shrinkTask), line, nextTask);
	} else {
	    logger.error("nextTask is null:");
	}

	line.put("polydot", new JSONArray());
	line.put("number", 0);
	line.put("property", new JSONArray());
	tempObj.getJSONArray("lines").add(line);
    }

    private void shrinkTask(JSONArray nodes, Map<String, Object> shrinkTask) {
	JSONObject jo = new JSONObject();
	copyBaseInfo(shrinkTask, jo);

	jo.put("id", "shrink_" + TextUtil.substring(BpmDo.taskInnerId(shrinkTask), "-"));
	jo.put("type", "Shrink");
	jo.put("shape", "shape");
	jo.put("number", -NumberUtil.parseInt(BpmDo.taskInnerId(shrinkTask), 0));
	copyShape(shrinkTask, jo);
	jo.put("property", "null");

	nodes.add(jo);
    }

    private void branchTask(JSONArray nodes, Map<String, Object> branchTask) {
	JSONObject jo = new JSONObject();
	jo.put("id", "branch_" + TextUtil.substring(BpmDo.taskInnerId(branchTask), "-"));

	copyBaseInfo(branchTask, jo);

	jo.put("type", "Branch");
	jo.put("shape", "shape");
	jo.put("number", -NumberUtil.parseInt(BpmDo.taskInnerId(branchTask), 0));
	copyShape(branchTask, jo);
	jo.put("property", "null");
	nodes.add(jo);
    }

    private void copyBaseInfo(Map<String, Object> branchTask, JSONObject jsonBtask) {
	jsonBtask.put("wfTaskName", MapTool.name(branchTask));
	jsonBtask.put("wfTaskInnerID", BpmDo.taskInnerId(branchTask));
	jsonBtask.put("descript", BpmDo.descript(branchTask));
	jsonBtask.put("name", MapTool.name(branchTask));
    }

    private Map<String, Object> copyShape(Map<String, Object> branchTask, JSONObject jsonBtask) {
	Map<String, Object> shape = BpmDo.shape(branchTask);
	jsonBtask.put("left", BpmDo.nodeX(shape));
	jsonBtask.put("top", BpmDo.nodeY(shape));
	jsonBtask.put("width", BpmDo.nodeW(shape));
	jsonBtask.put("height", BpmDo.nodeH(shape));
	return shape;
    }

    private void simpleTaskLine(Map<String, Object> tWorkflow, JSONObject tempObj, Map<String, Object> simpleTask,
	    Map<String, Object> sts) {
	JSONObject line = new JSONObject();
	String stInnerId = BpmDo.taskInnerId(simpleTask);
	String startTaskInnerId = TextUtil.substring(stInnerId, "-");

	line.put("id", "line_" + startTaskInnerId);
	line.put("name", "line_" + startTaskInnerId);
	line.put("type", "line");
	line.put("shape", "line");
	line.put("from", "node_" + startTaskInnerId);

	Map<String, Object> nextTask = Twf.getByInnerID(BpmDo.next(simpleTask),tWorkflow);
	BpmDo.lineTo(line, nextTask);
	from2Shape(sts, line, nextTask);
	line.put("polydot", new JSONArray());
	line.put("number", 0);
	line.put("property", new JSONArray());
	tempObj.getJSONArray("lines").add(line);
    }

    private Map<String, Object> simpleTask(JSONArray nodes, Map<String, Object> simpleTask) {
	JSONObject json = new JSONObject();

	json.put("id", "node_" + TextUtil.substring(BpmDo.taskInnerId(simpleTask), "-"));
	json.put("type", "node");
	json.put("shape", "rect");
	json.put("number", -NumberUtil.parseInt(BpmDo.taskInnerId(simpleTask), 0));
	copyBaseInfo(simpleTask, json);
	Map<String, Object> copyShape = copyShape(simpleTask, json);
	json.put("property", "null");
	nodes.add(json);
	return copyShape;
    }

    private void endTask(JSONArray nodes, Map<String, Object> endTask) {
	JSONObject jsonEtask = new JSONObject();
	jsonEtask.put("id", "control_" + TextUtil.substring(BpmDo.taskInnerId(endTask), "-"));

	jsonEtask.put("type", "end");
	jsonEtask.put("shape", "oval");
	jsonEtask.put("number", -NumberUtil.parseInt(BpmDo.taskInnerId(endTask), 0));
	copyBaseInfo(endTask, jsonEtask);
	copyShape(endTask, jsonEtask);
	jsonEtask.put("property", "null");
	nodes.add(jsonEtask);
    }

    private void lineTask(Map<String, Object> tWorkflow, JSONObject tempObj, Map<String, Object> startTask,
	    Map<String, Object> startTaskShape) {
	JSONObject line = new JSONObject();
	line.put("id", "line_" + TextUtil.substring(BpmDo.taskInnerId(startTask), "-"));
	line.put("name", "line_" + TextUtil.substring(BpmDo.taskInnerId(startTask), "-"));
	line.put("type", "line");
	line.put("shape", "line");
	line.put("from", "control_" + TextUtil.substring(BpmDo.taskInnerId(startTask), "-"));

	Map<String, Object> nextTask =  Twf.getByInnerID(BpmDo.next(startTask),tWorkflow);

	Integer taskType = BpmDo.taskType(nextTask);
	if (taskType == BpmDo.WFTASK_TYPE_BRANCH) {
	    line.put("to", "branch_" + TextUtil.substring(BpmDo.taskInnerId(nextTask), "-"));
	}
	if (taskType == BpmDo.WFTASK_TYPE_SIMPLE) {
	    line.put("to", "node_" + TextUtil.substring(BpmDo.taskInnerId(nextTask), "-"));
	}

	from2Shape(startTaskShape, line, nextTask);
	line.put("polydot", new JSONArray());
	line.put("number", 0);
	line.put("property", new JSONArray());
	tempObj.getJSONArray("lines").add(line);
    }

    private void from2Shape(Map<String, Object> startTaskShape, JSONObject lineTask, Map<String, Object> nextTask) {
	lineTask.put("fromx", BpmDo.fromX(startTaskShape));
	lineTask.put("fromy", BpmDo.fromY(startTaskShape));
	Map<String, Object> toShape = BpmDo.shape(nextTask);
	lineTask.put("tox", BpmDo.nodeX(toShape));
	lineTask.put("toy", BpmDo.toY(toShape));
    }

    private Map<String, Object> startTask(Map<String, Object> startTask, JSONArray nodes) {
	JSONObject jsonStask = new JSONObject();
	copyBaseInfo(startTask, jsonStask);
	jsonStask.put("id", "control_" + TextUtil.substring(BpmDo.taskInnerId(startTask), "-"));
	jsonStask.put("type", "start");
	jsonStask.put("shape", "oval");
	jsonStask.put("number", -NumberUtil.parseInt(BpmDo.taskInnerId(startTask), 0));
	Map<String, Object> startTaskShape = copyShape(startTask, jsonStask);
	jsonStask.put("property", "null");
	nodes.add(jsonStask);
	return startTaskShape;
    }
}
