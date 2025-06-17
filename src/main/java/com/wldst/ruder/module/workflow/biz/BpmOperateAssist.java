package com.wldst.ruder.module.workflow.biz;

import bsh.EvalError;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.BeanShellService;
import com.wldst.ruder.module.workflow.beans.*;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.exceptions.WorkFlowException;
import com.wldst.ruder.module.workflow.formula.BpmExecutorFormulaParse;
import com.wldst.ruder.module.workflow.formula.FormulaParseUtil;
import com.wldst.ruder.module.workflow.inherit.BpmTaskBizExcute;
import com.wldst.ruder.module.workflow.inherit.SubWorkflowTrigger;
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.module.workflow.util.TextUtil;
import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.module.workflow.util.WorkflowUtil;
import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.LoggerTool;
import com.wldst.ruder.util.MapTool;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程任务推动辅助操作
 *
 * @author wldst
 */
@Component
public class BpmOperateAssist extends BpmDo {
    // 日志对象
    private static final Logger logger = LoggerFactory.getLogger(BpmOperateAssist.class);
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudNeo4jService crudService;
    @Autowired
    private  BeanShellService bss;
    @Autowired
    private BpmInstance bpmi;
    @Autowired
    private BpmInstanceManagerService bizWfInstanceManager;
    @Autowired
    private BpmInstanceStateTransfer bist;


    /**
     * 对业务流程进行操作。
     *
     * @param opdata              包含流程操作信息的JSONObject对象，需要包含flowId和executeComment等字段。
     * @param currentUserId   当前操作用户的ID。
     * @param wfdecisionAgree 用户对当前任务的决策意见，同意或拒绝等。
     * @return 如果流程操作成功执行，则返回true；否则返回false。
     * @throws EvalError              如果表达式评估出错。
     * @throws InstantiationException 如果类无法实例化。
     * @throws IllegalAccessException 如果没有权限访问类的成员。
     * @throws ClassNotFoundException 如果类未找到。
     */
    public Boolean operateFlow(JSONObject opdata, Long currentUserId, String wfdecisionAgree)
            throws EvalError, InstantiationException, IllegalAccessException, ClassNotFoundException {
        // 获取流程ID和执行意见
        String flowId = MapTool.string(opdata, "flowId");
        String executeComment = MapTool.string(opdata, "executeComment");

        // 根据流程ID获取流程信息
        Map<String, Object> workflow = bpmi.getFlowi(NumberUtil.parseLong(flowId, 0));
        Map<String, Object> nextTask =null;
        if (workflow != null) {

            // 获取下一个任务节点和当前任务节点
            Long nextTaskId = longValue(opdata, "nextTaskId");
            if(nextTaskId !=null){
                nextTask = bpmi.getWfTaskByID(nextTaskId);
            }else{
                nextTask = bpmi.findNextNormalNode(workflow);
            }

            Map<String, Object> currentTask = bpmi.getNowNode(workflow);

            // 根据当前任务和决策意见，获取决策结果
            Map<String, Object> decision = BpmDo.wfTaskDecision(currentTask, wfdecisionAgree);

            // 准备流程运行数据
            Map<String, Object> actionParam = new HashMap<String, Object>();
            // 设置决策键、执行意见、流程ID等信息
            actionParam.put("decisionKey", wfdecisionAgree);
            actionParam.put("executeComment", TextUtil.nvl(executeComment));
            actionParam.put("workflowId", flowId);
            // 如果决策为同意，则记录当前任务进入时间
            if (wfdecisionAgree.equals(WFEConstants.WFDECISION_AGREE)) {
                actionParam.put("currTaskComeDatetime", Calendar.getInstance().getTimeInMillis());
            }

            // 如果存在下一个任务节点，则设置下一个任务ID
            if (nextTask != null) {
                actionParam.put("nextTaskId", BpmDo.id(nextTask));
            }
            // 设置决策名称
            if (decision != null && BpmDo.decisionViewName(decision) != null
                    && BpmDo.decisionViewName(decision).trim().length() > 0) {
                actionParam.put("decisionName", BpmDo.decisionViewName(decision));
            } else {
                actionParam.put("decisionName", WFEConstants.convertWfDecisionNameZh(wfdecisionAgree));
            }

            // 设置下一任务执行者ID、当前执行者ID和名称
            Long agentID = longValue(nextTask, "agentID");
            if(agentID !=null){
                actionParam.put(WfPc.nextTaskExecutorIDs,  agentID);
                actionParam.put(WfPc.currentExecutorID, currentUserId);
                actionParam.put(WfPc.currentExecutorName, string(opdata,"currentAgentName"));
            }else{
                actionParam.put(WfPc.nextTaskExecutorIDs,  MapTool.string(nextTask, "executorIDs"));
                actionParam.put(WfPc.currentExecutorID, currentUserId);
                actionParam.put(WfPc.currentExecutorName, adminService.getCurrentAccount());
            }
            // 执行流程动作，并返回结果
            return flowAction(workflow, actionParam);
        }
        // 如果流程信息为空，则返回false
        return false;
    }

    /**
     * 取回单据
     *
     * @param flowi 包含流程信息的Map对象
     * @param currentUserId 当前用户ID
     * @throws EvalError 计算表达式时发生错误
     * @throws InstantiationException 实例化异常
     * @throws IllegalAccessException 无权访问异常
     * @throws ClassNotFoundException 类未找到异常
     * 本方法用于将流程中的任务取回，只能由当前任务的执行人员执行。如果尝试取回的任务是起始节点或非执行人员的任务，则会抛出异常。
     * 方法主要逻辑包括验证是否可以取回以及更新流程和任务状态。
     */
    public void reverse(Map<String, Object> flowi, Long currentUserId)
            throws EvalError, InstantiationException, IllegalAccessException, ClassNotFoundException {
        // 获取当前节点信息
        Map<String, Object> currentNode = bpmi.getNowNode(flowi);
        // 判断节点类型
        String taskType = nodeType(currentNode);
        // 如果当前节点是起始节点，则抛出异常，因为起始节点无需取回
        if (NODE_TYPE_START.equals(taskType)) {
            throw new WorkFlowException("已是新建节点，无需取回");
        }
        Boolean isInExcuteUsers = false;
        // 查询前一个任务节点信息
        List<Map<String, Object>> query = crudService
                .cypher("MATCH(n)-[:nextStep]->(m:BpmNode) where id(m)=" + BpmDo.id(currentNode) + " return n");
        if (query != null && !query.isEmpty()) {
            Map<String, Object> preTask = query.get(0);
            taskType = nodeType(preTask);
            // 判断前一个任务节点类型并进行相应处理
            if (NODE_TYPE_NORMAL.equals(taskType)) {
                // 检查当前用户是否为前一任务的执行人员之一
                List<Map<String, Object>> listMapObject = MapTool.listMapObject(preTask, "nodeUserList");

                if(listMapObject==null){
                    String roleID = string(preTask, "roleID");
                    if(roleID!=null && roleID.trim().length()>0){
                        List<Map<String, Object>> listMapObject2 = crudService.cypher("MATCH(n:User)-[:HAS_ROLE]->(m:Role) where id(m)=" + roleID + " return n");
                        if(listMapObject2!=null && !listMapObject2.isEmpty()){
                            listMapObject = listMapObject2;
                        }
                    }
                }
                if(listMapObject!=null) {
                    for (Map<String, Object> ui : listMapObject) {
                        if (currentUserId.equals(MapTool.id(ui))) {
                            isInExcuteUsers = true;
                            break;
                        }
                    }
                }
                
                if(!isInExcuteUsers) {
                    List<Map<String, Object>> queryx = crudService
                            .cypher("(m:BpmNode)-->(e:BpmTaskExecute) where id(m)=" + BpmDo.id(preTask)
                                    + " return e");
                    for(Map<String, Object> ei: queryx) {
                	 if (currentUserId.equals(MapTool.string(ei,"userId"))) {
                             isInExcuteUsers = true;
                             break;
                         }
                    }
                }
                
                
                // 如果当前用户是执行人员，则进行任务取回操作
                if (isInExcuteUsers) {
                    List<Map<String, Object>> queryx = crudService
                            .cypher("(m:BpmNode)-->(e:BpmTaskExecute)-->(u:User) where id(m)=" + BpmDo.id(preTask)
                                    + " and id(u)=" + currentUserId + " return u");
                    if (queryx != null && !queryx.isEmpty()) {
                        isInExcuteUsers = true;
                        flowi.put(NOW_TASK_IDS, id(preTask));
                        preTask.put(TASK_STATUS, WFTASK_STATUS_READY);
                        crudService.update(MapTool.copyWithKeys(preTask, "id," + TASK_STATUS));
                        crudService.update(MapTool.copyWithKeys(flowi, "id," + NOW_TASK_IDS));
                    } else {
                        isInExcuteUsers = false;
                    }
                }
                if (!isInExcuteUsers) {
                    throw new WorkFlowException("当前用户不是前一任务的执行人员，无法取回");
                }
            } else if (NODE_TYPE_START.equals(taskType)) {
                // 如果前一个任务是起始节点，则将流程重置为初始状态
                flowi.put(NOW_TASK_IDS, null);
                flowi.put(WF_STATUS, WFSTATUS_INIT);
                crudService.update(MapTool.copyWithKeys(flowi, "id," + NOW_TASK_IDS + "," + WF_STATUS));
            }
        }

        // 清理与当前节点相关的执行信息，即删除相关的任务执行实例和关联关系
        crudService.execute(
                "match (m:BpmNode)-[r]->(e:BpmTaskExecute)-[r2]-(u:User) where id(m)=" + BpmDo.id(currentNode) + " delete r,r2,e");
    }


    /**
     * 实现任务的回退功能。
     * 回退任务时，根据提供的任务ID和工作流决策信息，更新工作流状态，将任务指向前一个任务节点。
     *
     * @param vo 包含回退所需信息的JSONObject对象，如flowId（流程ID），turnBackTaskID（回退的任务ID），executeComment（执行评论）。
     * @param currentUserId 当前操作用户的ID。
     * @param wfdecision 工作流决策标识，用于确定回退逻辑。
     * @throws EvalError 如果表达式评估出错。
     * @throws InstantiationException 如果类无法实例化。
     * @throws IllegalAccessException 如果没有权限访问类的成员。
     * @throws ClassNotFoundException 如果类未找到。
     */
    public void turnback(JSONObject vo, Long currentUserId, String wfdecision)
            throws EvalError, InstantiationException, IllegalAccessException, ClassNotFoundException {
        // 提取VO对象中的流程ID和回退任务ID
        Long flowId = MapTool.longValue(vo, "flowId");
        String turnBackTaskID = MapTool.string(vo, "turnBackTaskID");
        // 如果没有指定回退任务ID，则直接返回
        if (turnBackTaskID == null) {
            return;
        }
        // 获取执行评论
        String executeComment = MapTool.string(vo, "executeComment");

        // 根据流程ID获取工作流信息
        Map<String, Object> workflow = bpmi.getFlowi(flowId);
        // 如果找到工作流信息，则进行回退逻辑处理
        if (workflow != null) {
            // 获取当前任务节点信息
            Map<String, Object> currentTask = bpmi.getNowNode(workflow);
            // 根据提供的工作流决策信息，获取决策结果
            Map<String, Object> decision = BpmDo.wfTaskDecision(currentTask, wfdecision);

            // 准备执行回退操作所需的数据
            Map<String, Object> runData = new HashMap<String, Object>();
            runData.put("currentExecutorId", currentUserId);
            runData.put("decisionKey", wfdecision);
            runData.put("executeComment", TextUtil.nvl(executeComment));
            runData.put("workflowId", BpmDo.id(workflow));
            runData.put("currTaskComeDatetime", MapTool.now());
            runData.put("nextTaskID", turnBackTaskID);
            // 如果决策结果存在且有名称，则使用决策名称，否则使用决策标识的中文名称
            if (decision != null && BpmDo.decisionViewName(decision) != null
                    && BpmDo.decisionViewName(decision).trim().length() > 0) {
                runData.put("decisionName", BpmDo.decisionViewName(decision));
            } else {
                runData.put("decisionName", WFEConstants.convertWfDecisionNameZh(wfdecision));
            }
            runData.put("nextTaskExecutorIDs", null);
            runData.put("currentExecutorID", currentUserId);
            runData.put("currentExecutorName", adminService.getCurrentAccount());

            // 执行回退操作
            flowAction(workflow, runData);
        }
    }


    /**
     * 循环任务
     *
     * @param vo
     * @param currentUserId
     * @param wfdecisionAgree
     * @throws EvalError
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    public void reloop(JSONObject vo, Long currentUserId, String wfdecisionAgree)
            throws EvalError, InstantiationException, IllegalAccessException, ClassNotFoundException {
        String flowId = MapTool.string(vo, "flowId");
        String bizTabName = MapTool.string(vo, "bizTabName");
        String templateMark = MapTool.string(vo, "templateMark");
        String reloopTaskID = MapTool.string(vo, "reloopTaskID");
        String executeComment = MapTool.string(vo, "executeComment");
        String taskComeDatetime = MapTool.string(vo, "taskComeDatetime");
        String[] executorIDs = MapTool.splitColumnValue(vo, "executorIDs");
        Map<String, Object> workflow = bpmi.getFlowi(Long.valueOf(flowId));


        if (workflow != null) {

            Map<String, Object> currentTask = bpmi.getNowNode(workflow);
            // Map<String, Object> nextTask = bpmi.findNextSimpleTask(workflow);

            Map<String, Object> decision = BpmDo.wfTaskDecision(currentTask, wfdecisionAgree);

            Map<String, Object> wfRuntimeData = new HashMap<String, Object>();
            wfRuntimeData.put("currentExecutorId", currentUserId);
            // wfeRunData.setCurRequest(this.getRequest(context));
            wfRuntimeData.put("decisionKey", wfdecisionAgree);
            wfRuntimeData.put("taskExecuteConfirm", TextUtil.nvl(executeComment));
            wfRuntimeData.put("workflowId", BpmDo.id(workflow));
            if (wfdecisionAgree.equals(WFEConstants.WFDECISION_AGREE)) {
                wfRuntimeData.put("currTaskComeDatetime", NumberUtil.parseLong(taskComeDatetime, 0));
            }

            // if (nextTask != null) {
            wfRuntimeData.put("nextTaskID", reloopTaskID);
            // }
            if (decision != null && BpmDo.decisionViewName(decision) != null
                    && BpmDo.decisionViewName(decision).trim().length() > 0) {
                wfRuntimeData.put("decisionName", BpmDo.decisionViewName(decision));
            } else {
                wfRuntimeData.put("decisionName", WFEConstants.convertWfDecisionNameZh(wfdecisionAgree));
            }
            wfRuntimeData.put("nextTaskExecutorIDs", null);
            wfRuntimeData.put("currentExecutorID", currentUserId);
            wfRuntimeData.put("currentExecutorName", adminService.getCurrentAccount());

            flowAction(workflow, wfRuntimeData);
        }
    }

    /**
     * 提交流程实例的执行操作。
     *
     * @param workflow 流程实例数据映射。
     * @param runData  流程运行时的数据映射。
     * @return 执行操作是否成功。
     * @throws EvalError              表达式计算错误。
     * @throws InstantiationException 实例化异常。
     * @throws IllegalAccessException 无权访问异常。
     * @throws ClassNotFoundException 类未找到异常。
     */
    public boolean commit(Map<String, Object> workflow, Map<String, Object> runData)
            throws EvalError, InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (runData == null || BpmDo.decisionKey(runData) == null
                || !BpmDo.decisionKey(runData).equals(WFEConstants.WFDECISION_COMMIT)) {
            throw new CrudBaseException("流程流转信息不对,不能进行相关操作");
        }
        boolean retFlag = false;
        Map<String, Object> currentTask = bpmi.getNowNode(workflow); // 获取当前任务节点
        Map<String, Object> nextTask = bpmi.findNextNormalNode(workflow); // 查找下一个正常任务节点

        if (currentTask != null) {
            // 更新当前任务的执行人信息
            Map<String, Object> updateExecutor = null;
            Long taskCurentUserID = MapTool.longValue(runData, "currentExecutorID");
            List<Map<String, Object>> taskExcuteList = bpmi.getTaskExcute(MapTool.id(workflow),
                    MapTool.id(currentTask));

            for (Map<String, Object> ei : taskExcuteList) {
                Long userId = MapTool.longValue(ei, "executorID");
                Long passwordIdBy = crudService.getPasswordIdBy(userId);
                if (taskCurentUserID.equals(passwordIdBy) || taskCurentUserID.equals(userId)) {
                    updateExecutor = ei;
                }
            }

            if (updateExecutor != null) {
                updateExecutor.put(BpmDo.EXECUTOR_STATUS, WFEConstants.WF_EXEC_USERSTATE_EXECED);
                updateExecutor.put(BpmDo.EXECUTOR_DECISION, BpmDo.decisionKey(runData));
                crudService.saveById(BpmDo.id(updateExecutor), updateExecutor);
            }

            // 记录当前任务的历史履历
            addHistory(workflow, runData, currentTask);

            // 判断任务执行人完成比例
            Map<String, Object> taskProperty = BpmDo.taskProperty(currentTask);

            if (null != taskProperty) {
                // 设置最多的环节执行人
                boolean flag = checkMaxExecutor(taskExcuteList, taskProperty);
                if (flag) {
                    return true;
                }
            }

            currentTask.put("taskStatus", WFEConstants.WFTASK_STATUS_END);
            crudService.saveById(BpmDo.id(currentTask), currentTask);

            // 执行当前任务关联的业务逻辑
            String bizClassName = BpmDo.invokeClass(taskProperty);
            if (bizClassName != null && bizClassName.trim().length() > 0) {
                BpmTaskBizExcute bizExecute = (BpmTaskBizExcute) Class.forName(bizClassName).newInstance();
                if (bizExecute != null) {
                    bizExecute.performBizExecute(workflow, runData);
                }
            }

            // 触发子流程的执行
            String subClassName = BpmDo.exeSubFlowClass(taskProperty);
            if (subClassName != null && subClassName.trim().length() > 0) {
                SubWorkflowTrigger trigger = (SubWorkflowTrigger) Class.forName(subClassName).newInstance();
                if (trigger != null) {
                    Map<String, Object> subWf = trigger.triggerSubWorkflow(workflow, currentTask, runData);
                    if (subWf != null) {
                        BpmInstance.setTriggerSubWfFlag(WFEConstants.DB_BOOLEAN_TRUE, subWf);
                        BpmInstance.setTriggerTaskID(BpmDo.id(currentTask), subWf);
                        BpmInstance.setTriggerWfInsID(BpmDo.id(workflow), subWf);
                        crudService.saveByBody(subWf, "Workflow");
                        bizWfInstanceManager.runFlow(subWf, adminService.getCurrentUserId());
                    }
                    if (BpmDo.waitExecSubFlow(taskProperty) == WFEConstants.DB_BOOLEAN_TRUE) {
                        workflow.put("wfStatus", WFEConstants.WFSTATUS_SUSPEND);
                        crudService.saveById(BpmDo.id(workflow), workflow);
                    }
                }
            }

            if (nextTask != null) {
                // 处理下一个任务的执行人信息
                nextTask.put("taskStatus", WFEConstants.WFTASK_STATUS_READY);
                int autoExecuteFlag = BpmDo.taskAutoExecute(taskProperty);
                workflow.put(NOW_TASK_IDS, String.valueOf(BpmDo.id(nextTask)));
                if (autoExecuteFlag == WFEConstants.DB_BOOLEAN_TRUE) {
                    this.autoExecutePerform(workflow, nextTask, runData);
                } else {
                    Map<String, Object> taskDecision = SimpleTask.getWfTaskDecisionByName(BpmDo.decisionKey(runData),
                            currentTask);
                    long[] nextExecutorIDArray = getNextTaskExecutors(workflow, BpmDo.executeType(taskDecision),
                            runData);
                    if (nextExecutorIDArray != null && nextExecutorIDArray.length > 0) {
                        for (int i = 0; i < nextExecutorIDArray.length; i++) {
                            addExecutor(nextTask, nextExecutorIDArray[i]);
                        }
                    }
                    crudService.update(nextTask);
                }
            } else {
                // 流程实例结束运行
                BpmDo.clear(workflow, NOW_TASK_IDS);
                workflow.put("wfStatus", WFEConstants.WFSTATUS_END);

                if (BpmDo.triggerSubWfFlag(workflow) == WFEConstants.DB_BOOLEAN_TRUE) {
                    long triggerWorkflowID = BpmDo.triggerWfInsId(workflow);
                    Map<String, Object> triggerWorkflow = bpmi.getFlowi(triggerWorkflowID);
                    bizWfInstanceManager.runFlow(triggerWorkflow, adminService.getCurrentPasswordId());
                }
            }
            crudService.update(workflow);
        }
        retFlag = true;
        return retFlag;
    }


    private void addExecutor(Map<String, Object> nextTask, long nextExecutorId) {
        Map<String, Object> nextExecutor = new HashMap<>();
        nextExecutor.put("executorID", nextExecutorId);
        nextExecutor.put("instanceID", BpmDo.wfInstanceId(nextTask));
        nextExecutor.put("taskID", BpmDo.id(nextTask));
        nextExecutor.put("taskComeDateTime",Calendar.getInstance().getTimeInMillis());
        crudService.saveByBody(nextExecutor, "BpmTaskExecute");
        // list
        nextTask.put("taskExecutor", nextExecutor);
    }

    /**
     * 根据任务决策名和运行数据获取下一任务的执行人信息
     *
     * @param workflow 当前工作流的详细信息，包含任务节点等信息。
     * @param decisionStyle 决策样式，用于区分不同的决策方式（如是/否决策）。
     * @param runData 运行时的数据，包含了流程当前的状态和用户的选择等信息。
     * @return 返回一个long数组，包含下一任务的执行人的ID。
     * @throws EvalError 如果在执行表达式时发生错误，则抛出此异常。
     */
    private long[] getNextTaskExecutors(Map<String, Object> workflow, int decisionStyle, Map<String, Object> runData)
            throws EvalError {
        long[] nextExecutorIDArray = null;
        // 查找工作流中的下一个正常节点
        Map<String, Object> nextTask = bpmi.findNextNormalNode(workflow);
        if (WFEConstants.DECISION_EXEC_YESNO == decisionStyle) {
            // 对于是/否决策，直接获取下一任务的所有执行人
            BpmExecutorFormulaParse executorParse = null;
            executorParse = new BpmExecutorFormulaParse(BpmInstance.getBizTableName(workflow),
                    BpmInstance.getBizDataID(workflow), workflow,crudService, bss,adminService);
            List<Map<String, Object>> extendsList = BpmDo.extendsInfo(nextTask);
            // 解析执行人公式，获取执行人ID数组
            nextExecutorIDArray = FormulaParseUtil.parseExecutorFormula(executorParse, extendsList);
        } else {
            // 获取通过页面选择的执行人ID数组
            nextExecutorIDArray = BpmDo.nextTaskExecutorIDs(runData);
        }
        // 打印执行人数量，用于调试
        if (null != nextExecutorIDArray) {
            System.out.println("nextExecutorIDArray: " + nextExecutorIDArray.length);
        }
        return nextExecutorIDArray;
    }


    /**
     * 重新循环执行流程,跳转到
     *
     * @param workflow 流程实例数据
     * @param runData 流程运行数据
     * @return boolean 执行成功返回true，失败返回false
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws EvalError
     */
    public boolean reloop(Map<String, Object> workflow, Map<String, Object> runData)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, EvalError {
        boolean retFlag = false;
        // 检查流程实例和运行数据的有效性
        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (runData == null || BpmDo.decisionKey(runData) == null
                || !BpmDo.decisionKey(runData).equals(WFEConstants.WFDECISION_RELOOP)
                || BpmDo.nextTaskId(runData) <= 0) {
            throw new CrudBaseException("流程流转信息不对,不能进行相关操作");
        }

        Map<String, Object> currentTask = bpmi.getNowNode(workflow);
        Map<String, Object> reloopTask = bpmi.getWfTaskByID(BpmDo.nextTaskId(runData));
        List<Map<String, Object>> executorList = null;
        // 清理当前任务的执行人
        if (currentTask != null) {
            executorList = BpmDo.taskExecutorList(currentTask);
            if (executorList != null && executorList.size() > 0) {
                int listSize = executorList.size();
                for (int i = 0; i < listSize; i++) {
                    Map<String, Object> delExecutor = executorList.get(i);
                    crudService.removeById(BpmDo.id(delExecutor));
                }
                BpmDo.removeAllExecutor(currentTask);
            }
            currentTask.put("taskStatus", WFEConstants.WFTASK_STATUS_READY);
            crudService.update(currentTask);
        }

        // 清理回退任务上的执行人
        List<Map<String, Object>> turnbackTaskList = WorkflowUtil.getInstance().getExecutedWFSimpleTaskList(workflow,
                currentTask, reloopTask);
        if (turnbackTaskList != null && turnbackTaskList.size() > 0) {
            int listSize = turnbackTaskList.size();
            for (int i = 0; i < listSize; i++) {
                Map<String, Object> turnbackTask = turnbackTaskList.get(i);
                if (BpmDo.id(turnbackTask) != BpmDo.id(reloopTask)) {
                    List<Map<String, Object>> executorList1 = BpmDo.taskExecutorList(turnbackTask);
                    if (executorList1 != null && executorList1.size() > 0) {
                        int listSize2 = executorList1.size();
                        for (int j = 0; j < listSize2; j++) {
                            Map<String, Object> delExecutor = executorList1.get(j);

                            crudService.removeById(BpmDo.id(delExecutor));
                        }
                        BpmDo.removeAllExecutor(turnbackTask);
                    }
                    turnbackTask.put("taskStatus", WFEConstants.WFTASK_STATUS_READY);
                    crudService.update(turnbackTask);
                }
            }
        }

        // 记录当前任务的执行记录
        if (BpmDo.currentExecutorId(runData) > 0) {
            addHistory(workflow, runData, currentTask);
        }

        // 检查并处理任务执行人数量限制
        Map<String, Object> taskProperty = BpmDo.taskProperty(currentTask);
        if (null != taskProperty) {
            boolean flag = checkMaxExecutor(executorList, taskProperty);
            if (flag) {
                return true;
            }
        }

        // 执行业务逻辑
        String bizClassName = BpmDo.invokeClass(taskProperty);
        if (bizClassName != null && bizClassName.trim().length() > 0) {
            BpmTaskBizExcute bizExecute = (BpmTaskBizExcute) Class.forName(bizClassName).newInstance();
            if (bizExecute != null) {
                bizExecute.reloopBizExecute(workflow, runData);
            }
        }

        // 处理跳转到的任务
        if (reloopTask != null) {
            Map<String, Object> tpCurrent = BpmDo.taskProperty(reloopTask);
            workflow.put(NOW_TASK_IDS, String.valueOf(BpmDo.id(reloopTask)));
            reloopTask.put("taskStatus", WFTASK_STATUS_READY);

            int autoExecuteFlag = BpmDo.taskAutoExecute(tpCurrent);
            if (autoExecuteFlag == WFEConstants.DB_BOOLEAN_TRUE) {
                this.autoExecutePerform(workflow, reloopTask, runData);
            } else {
                List<Map<String, Object>> executorList2 = BpmDo.taskExecutorList(reloopTask);
                if (executorList2 != null && executorList2.size() > 0) {
                    int listSize = executorList2.size();
                    for (int i = 0; i < listSize; i++) {
                        Map<String, Object> updateExecutor = executorList2.get(i);

                        updateExecutor.put(BpmDo.EXECUTOR_DECISION, WFEConstants.WFDECISION_NONE);
                        updateExecutor.put(BpmDo.EXECUTOR_STATUS, WFEConstants.WF_EXEC_USERSTATE_NONE);

                        crudService.update(updateExecutor);
                    }
                }
                crudService.update(reloopTask);
            }

            crudService.update(workflow);
        }
        retFlag = true;
        return retFlag;
    }

    /**
     * 自动执行任务推动流程。
     * 该方法负责处理自动执行的任务，更新任务状态，执行相关的业务逻辑，并推动流程继续进行。
     *
     * @param workflow 流程实例，包含当前流程的相关信息。
     * @param autoTask 自动执行任务的相关信息。
     * @param runData  流程推动的数据。
     * @return 执行结果，成功返回true，失败返回false。
     * @throws ClassNotFoundException 当尝试加载类时未找到该类。
     * @throws IllegalAccessException 当试图访问或修改字段，而当前上下文没有足够的访问权限时抛出。
     * @throws InstantiationException 当试图实例化一个类时，出现了与实例化有关的异常。
     * @throws EvalError 当执行JavaScript表达式时出现错误。
     */
    private boolean autoExecutePerform(Map<String, Object> workflow, Map<String, Object> autoTask,
                                       Map<String, Object> runData)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException, EvalError {
        boolean retFlag = false;
        // 获取任务属性
        Map<String, Object> taskProperty = BpmDo.taskProperty(autoTask);
        // 检查是否为自动执行任务
        if (autoTask != null && BpmDo.taskAutoExecute(taskProperty) == WFEConstants.DB_BOOLEAN_TRUE) {

            // 更新任务状态为结束
            autoTask.put("taskStatus", WFEConstants.WFTASK_STATUS_END);
            crudService.update(autoTask);

            // 执行自动任务的业务逻辑
            String bizClassName = BpmDo.invokeClass(taskProperty);
            if (bizClassName != null && bizClassName.trim().length() > 0) {
                BpmTaskBizExcute bizExecute = (BpmTaskBizExcute) Class.forName(bizClassName).newInstance();
                if (bizExecute != null) {
                    bizExecute.performBizExecute(workflow, runData);
                }
            }

            // 触发子流程
            String subClassName = BpmDo.exeSubFlowClass(taskProperty);
            if (subClassName != null && subClassName.trim().length() > 0) {
                SubWorkflowTrigger trigger = (SubWorkflowTrigger) Class.forName(subClassName).newInstance();
                if (trigger != null) {
                    Map<String, Object> subWf = trigger.triggerSubWorkflow(workflow, autoTask, runData);

                    if (subWf != null) {
                        // 更新流程标志位和任务ID
                        BpmInstance.setTriggerSubWfFlag(WFEConstants.DB_BOOLEAN_TRUE, subWf);
                        BpmInstance.setTriggerTaskID(BpmDo.id(autoTask), subWf);
                        BpmInstance.setTriggerWfInsID(BpmDo.id(workflow), subWf);

                        // 更新子流程信息并启动子流程
                        crudService.update(subWf);
                        bizWfInstanceManager.runFlow(subWf, adminService.getCurrentUserId());
                    }
                }
            }

            // 检查是否存在循环任务节点
            Map<String, Object> reloopTask = BpmInstance.getReloopTask(BpmDo.taskInnerId(autoTask), workflow);
            if (reloopTask != null) {
                // 处理循环任务逻辑
                Map<String, Object> reloopRunDate = new HashMap<String, Object>();
                WfPc.setCurrentExecutorID(0l, reloopRunDate);
                WfPc.setDecisionKey(WFEConstants.WFDECISION_RELOOP, reloopRunDate);
                WfPc.setTaskExecuteConfirm("", reloopRunDate);
                WfPc.setWorkflowID(BpmDo.id(workflow), reloopRunDate);
                WfPc.setCurrentExecutorName("", reloopRunDate);
                WfPc.setNextTaskID(BpmDo.id(reloopTask), reloopRunDate);
                WfPc.setNextTaskExecutorIDs(null, reloopRunDate);
                WfPc.setDecisionName(WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_RELOOP),
                        reloopRunDate);
                this.reloop(workflow, reloopRunDate);
            } else {
                // 处理非循环任务逻辑，包括更新任务关系、判断下一个任务并执行
                Map<String, Object> nextTask = bpmi.findNextNormalNode(workflow);
                if (nextTask != null) {
                    // 根据下一个任务的属性执行相应的逻辑
                    nextTask.put("taskStatus", WFEConstants.WFTASK_STATUS_READY);
                    Map<String, Object> taskPropertyNext = BpmDo.taskProperty(nextTask);
                    int autoExecuteFlag = BpmDo.taskAutoExecute(taskPropertyNext);

                    workflow.put(NOW_TASK_IDS, BpmDo.id(nextTask));

                    if (autoExecuteFlag == WFEConstants.DB_BOOLEAN_TRUE) {
                        // 如果是自动执行任务，则递归调用本方法执行自动任务逻辑
                        this.autoExecutePerform(workflow, nextTask, runData);
                    } else {
                        // 如果是手动执行任务，则处理任务分配逻辑
                        long[] nextExecutorIDArray = WfPc.getNextTaskExecutorIDs(runData);

                        if (nextExecutorIDArray != null && nextExecutorIDArray.length > 0) {
                            for (int i = 0; i < nextExecutorIDArray.length; i++) {
                                addExecutor(nextTask, nextExecutorIDArray[i]);
                            }
                        } else {
                            List<Map<String, Object>> extendsList = BpmDo.extendsInfo(nextTask);
                            BpmExecutorFormulaParse parse = new BpmExecutorFormulaParse(
                                    BpmInstance.getBizTableName(workflow), BpmInstance.getBizDataID(workflow),
                                    workflow,crudService,bss, adminService);

                            long[] executorArray = FormulaParseUtil.parseExecutorFormula(parse, extendsList);

                            for (int i = 0; i < executorArray.length; i++) {
                                addExecutor(nextTask, executorArray[i]);
                            }
                        }
                        crudService.update(nextTask);
                    }
                } else {
                    // 流程结束逻辑处理
                    BpmDo.clear(workflow, NOW_TASK_IDS);
                    workflow.put("wfStatus", WFEConstants.WFSTATUS_END);

                    if (BpmDo.triggerSubWfFlag(workflow) == WFEConstants.DB_BOOLEAN_TRUE) {
                        // 如果存在触发的子流程，则启动子流程
                        long triggerWorkflowID = BpmDo.triggerWfInsId(workflow);
                        Map<String, Object> triggerWorkflow = bizWfInstanceManager.getBpmi(triggerWorkflowID);
                        bizWfInstanceManager.runFlow(triggerWorkflow, adminService.getCurrentPasswordId());
                    }
                }
                crudService.update(workflow);
            }
        }

        retFlag = true;
        return retFlag;
    }

    /**
     * 解除流程中的任务关系为真的
     *
     * @param workflow 流程实例，包含流程的相关信息。
     * @param currentTask 当前任务，表示当前处理的任务节点。
     * @param nextTask 结束任务，表示要解除关系的目标任务节点。
     * @return 返回解除操作的执行结果，成功为true，失败为false。
     * @throws CrudBaseException 如果流程实例为空，则抛出异常。
     */
    private boolean releaseRelation(Map<String, Object> workflow, Map<String, Object> currentTask,
                                    Map<String, Object> nextTask) {
        boolean retFlag = false; // 默认返回值为false
        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }

        long loopEndTaskID = 0; // 循环结束任务的ID，用于判断是否到达循环的结束节点
        if (nextTask != null) {
            loopEndTaskID = BpmDo.id(nextTask); // 获取结束任务的ID
        }
        Map<String, Object> tempABTask = null; // 临时存储任务关系的Map

        if (currentTask != null) {
            tempABTask = currentTask; // 初始化临时任务关系
            Map<String, Object> nextRelation = BpmDo.realNextRelation(BpmDo.realNextRelation(currentTask));
            // 遍历当前任务到结束任务之间的所有任务关系，将它们的realFlowRoadFlag设置为false
            while (nextRelation != null && BpmDo.id(tempABTask) != loopEndTaskID) {
                nextRelation.put("realFlowRoadFlag", WFEConstants.DB_BOOLEAN_FALSE);
                crudService.update(nextRelation); // 更新任务关系

                Map<String, Object> abTask = bpmi.getWfTaskByID(TaskRelation.getRelationTaskID(nextRelation));
                if (abTask != null) {
                    Map<String, Object> preRelation = BpmDo.realPreRel(abTask); // 获取当前任务的前置任务关系
                    if (preRelation != null) {
                        preRelation.put("realFlowRoadFlag", WFEConstants.DB_BOOLEAN_FALSE);
                        crudService.update(preRelation); // 更新前置任务关系
                    }
                    nextRelation = BpmDo.realNextRelation(abTask); // 获取当前任务的后置任务关系
                }
                tempABTask = abTask; // 更新当前任务为后置任务
            }
        }
        return retFlag; // 返回执行结果
    }



    /**
     * 同意流程处理。
     * 对于给定的流程实例和运行数据，执行同意操作，这包括更新当前任务执行状态、记录历史履历、触发后续业务逻辑等。
     *
     * @param workflow 流程实例数据，包含流程的相关信息。
     * @param runData 运行时数据，包含当前任务的执行信息。
     * @return boolean 返回操作是否成功。true表示操作成功，false表示操作失败。
     * @throws WorkFlowException 流程引擎内部异常。
     * @throws CrudBaseException 数据操作异常。
     */
    public boolean agree(Map<String, Object> workflow, Map<String, Object> runData) {

        // 检查流程实例和运行数据的有效性
        if (workflow == null) {
            LoggerTool.error(logger,"执行agree流程实例为空,不能进行相关操作");
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (runData == null || BpmDo.decisionKey(runData) == null
                || !BpmDo.decisionKey(runData).equals(WFEConstants.WFDECISION_AGREE)) {
            throw new CrudBaseException("流程流转信息不对,不能进行相关操作");
        }
        boolean retFlag = false;

        try {
            // 获取当前节点和下一个正常节点信息
            Map<String, Object> currentNode = bpmi.getNowNode(workflow);
            Map<String, Object> nextNode = bpmi.findNextNormalNode(workflow);

            if (currentNode != null) {
                // 更新当前任务执行人状态
                Map<String, Object> updateExecutor = null;
                Long taskCurentUserID = MapTool.longValue(runData, "currentExecutorID");
                List<Map<String, Object>> nodeUserList = bpmi.getTaskExcute(MapTool.id(workflow),
                        MapTool.id(currentNode));
                if (nodeUserList != null && nodeUserList.size() > 0) {
                    // 找到当前任务执行人并更新状态
                    for (Map<String, Object> ei : nodeUserList) {
                        Long longValue = MapTool.longValue(ei, "executorID");
                        if (taskCurentUserID.equals(longValue)) {
                            updateExecutor = ei;break;
                        }
                    }
                    if (updateExecutor != null) {
                        updateExecutor.put("executorStatus", WFEConstants.WF_EXEC_USERSTATE_EXECED);
                        updateExecutor.put("executorDecision", BpmDo.decisionKey(runData));
                        crudService.update(updateExecutor);

                        // 判断任务执行人完成比例，更新当前节点状态并触发后续逻辑
                        Map<String, Object> taskProperty = BpmDo.taskProperty(currentNode);
                        if (null != taskProperty) {
                            // 设置了最多的环节执行人
                            boolean flag = checkMaxExecutor(nodeUserList, taskProperty);
                            if (flag) {
                                return true;
                            }
                        }

                        currentNode.put("taskStatus", WFEConstants.WFTASK_STATUS_END);
                        crudService.update(currentNode);
                        // 执行相关的业务逻辑类
                        triggerBizClass(workflow, runData, taskProperty);

                        // 自动触发子流程
                        subFlowTrigger(workflow, runData, taskProperty, currentNode);

                        // 处理下一个任务
                        handleNextTask(workflow, runData, nextNode, nodeUserList);
                        // 记录当前任务的历史履历信息
                        addHistory(workflow, runData, currentNode);

                        retFlag = true;
                    }
                }else{
                    Long agentID = longValue(currentNode, "agentID");
                    if(agentID !=null){
                        //BpmTaskExecute
                        updateExecutor = crudService.getNodeMapById(agentID);
                    }
                    if (updateExecutor != null) {
                        // 判断任务执行人完成比例，更新当前节点状态并触发后续逻辑
                        Map<String, Object> taskProperty = BpmDo.taskProperty(currentNode);
                        if (null != taskProperty) {
                            // 设置了最多的环节执行人
                            boolean flag = checkMaxExecutor(nodeUserList, taskProperty);
                            if (flag) {
                                return true;
                            }
                        }

                        currentNode.put("taskStatus", WFEConstants.WFTASK_STATUS_END);
                        crudService.update(currentNode);
                        // 执行相关的业务逻辑类
                        triggerBizClass(workflow, runData, taskProperty);

                        // 自动触发子流程
                        subFlowTrigger(workflow, runData, taskProperty, currentNode);

                        // 处理下一个任务
                        handleNextTask(workflow, runData, nextNode, nodeUserList);
                        // 记录当前任务的历史履历信息
                        addHistory(workflow, runData, currentNode);


                        retFlag = true;
                    }
                }
     


            }

        } catch (WorkFlowException e) {
            LoggerTool.error(logger,"agree同意失败", e);
            throw new WorkFlowException(e.getMessage(), e);
        } catch (CrudBaseException ex) {
            LoggerTool.error(logger,"agree同意失败", ex);
            throw new CrudBaseException(ex.getMessage());
        } catch (Exception ex) {
            LoggerTool.error(logger,"agree同意失败", ex);
            throw new CrudBaseException("同意失败：" + ex);
        }
        return retFlag;
    }

    private static void triggerBizClass(Map<String, Object> workflow, Map<String, Object> runData, Map<String, Object> taskProperty) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String bizClassName = BpmDo.invokeClass(taskProperty);
        if (bizClassName != null && bizClassName.trim().length() > 0) {
            BpmTaskBizExcute bizExecute = (BpmTaskBizExcute) Class.forName(bizClassName).newInstance();
            if (bizExecute != null) {
                bizExecute.agreeBizExecute(workflow, runData);
            }
        }
    }

    private void handleNextTask(Map<String, Object> workflow, Map<String, Object> runData, Map<String, Object> nextNode, List<Map<String, Object>> nodeUserList) throws InstantiationException, IllegalAccessException, ClassNotFoundException, EvalError {
        if (nextNode != null) {
            // 自动执行任务逻辑
            nextNode.put("taskStatus", WFEConstants.WFTASK_STATUS_READY);
            if ("Y".equals(MapTool.string(nextNode, "isAutoNode"))) {
                this.autoExecutePerform(workflow, nextNode, runData);
            } else {
                // 非自动执行任务逻辑
                long[] nextExecutorIDArray = WfPc.getNextTaskExecutorIDs(runData);
                if (nextExecutorIDArray == null && nextExecutorIDArray.length <1) {
                    // 未设置下一步执行人,根据角色获取相关执行人，默认取当前任务的第一个执行人


                }

                if (nextExecutorIDArray != null && nextExecutorIDArray.length > 0) {
                    for (int i = 0; i < nextExecutorIDArray.length; i++) {
                        Map<String, Object> executor = new HashMap<>();
                        executor.put("taskID", MapTool.id(nextNode));
                        executor.put("instanceID", BpmDo.id(workflow));
                        executor.put("executorStatus", WFEConstants.WF_EXEC_USERSTATE_NONE);
                        executor.put("executorDecision", null);
                        executor.put(BpmDo.TASK_COME_TIME, DateTool.nowLong());
                        executor.put("createTime", DateTool.nowLong());
                        long userId = nextExecutorIDArray[i];
                        executor.put("executorID", userId);
                        Node taskExecute = crudService.addNew(executor, "BpmTaskExecute");
                        long excuteId = taskExecute.getId();
                        bist.addExcuteRelation(workflow, nextNode, excuteId, userId);
                    }
                }else {
                    if (nodeUserList == null && nodeUserList.size() <1) {
                        // 根据角色找执行人
//                        crudService.cypher(nextNode, "BpmTaskExecute");

                    }
                    LoggerTool.error(logger,"agree同意失败:没有找到下一个节点执行人");
                    throw new WorkFlowException("agree同意失败:没有找到下一个节点执行人");
                }
                crudService.update(nextNode);
            }

            bist.updateNowTask(workflow, nextNode);
        } else {
            // 流程实例结束逻辑
            BpmDo.clear(workflow, NOW_TASK_IDS);
            workflow.put("wfStatus", WFEConstants.WFSTATUS_END);
            crudService.update(workflow);
            if (BpmDo.triggerSubWfFlag(workflow) == WFEConstants.DB_BOOLEAN_TRUE) {
                long triggerWorkflowID = BpmDo.triggerWfInsId(workflow);
                Map<String, Object> triggerWorkflow = bizWfInstanceManager.getBpmi(triggerWorkflowID);
                bizWfInstanceManager.runFlow(triggerWorkflow, adminService.getCurrentUserId());
            }
        }
    }

    private void subFlowTrigger(Map<String, Object> workflow, Map<String, Object> runData, Map<String, Object> taskProperty, Map<String, Object> currentNode) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String subClassName = BpmDo.exeSubFlowClass(taskProperty);
        if (subClassName != null && subClassName.trim().length() > 0) {
            SubWorkflowTrigger trigger = (SubWorkflowTrigger) Class.forName(subClassName).newInstance();
            if (trigger != null) {
                Map<String, Object> subWf = trigger.triggerSubWorkflow(workflow, currentNode, runData);

                if (subWf != null) {
                    BpmInstance.setTriggerSubWfFlag(WFEConstants.DB_BOOLEAN_TRUE, subWf);
                    BpmInstance.setTriggerTaskID(BpmDo.id(currentNode), subWf);
                    BpmInstance.setTriggerWfInsID(BpmDo.id(workflow), subWf);

                    crudService.update(subWf);
                    bizWfInstanceManager.runFlow(subWf, adminService.getCurrentUserId());
                }
                if (TaskProperties.getWaitExecSubFlow(taskProperty) == WFEConstants.DB_BOOLEAN_TRUE) {
                    workflow.put("wfStatus", WFEConstants.WFSTATUS_SUSPEND);
                    crudService.update(workflow);
                }
            }
        }
    }


    private void addHistory(Map<String, Object> flowi, Map<String, Object> runData, Map<String, Object> currentNode) {
        Map<String, Object> addHistory = addHistory(runData, currentNode);
        Long hi = id(addHistory);
        Long wfId = id(flowi);
        Long currentNodeId = id(currentNode);
        Long userId = longValue(runData, "currentExecutorID");
        crudService.relate("history","执行历史", wfId, hi);
        crudService.relate("user","用户", hi, userId);
        crudService.relate("node","当前节点", hi, currentNodeId);
    }

    /**
     * 处理不同意的流程操作。
     *
     * @param workflow 流程实例数据
     * @param runData 流程运行数据
     * @return boolean 操作是否成功
     * @throws EvalError 表达式计算错误
     * @throws InstantiationException 实例化异常
     * @throws IllegalAccessException 无权访问异常
     * @throws ClassNotFoundException 类未找到异常
     */
    public boolean disagree(Map<String, Object> workflow, Map<String, Object> runData)
            throws EvalError, InstantiationException, IllegalAccessException, ClassNotFoundException {
        // 检查流程实例和运行数据的有效性
        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (runData == null || BpmDo.decisionKey(runData) == null
                || !BpmDo.decisionKey(runData).equals(WFEConstants.WFDECISION_DISAGREE)) {
            throw new CrudBaseException("流程流转信息不对,不能进行相关操作");
        }

        boolean retFlag = false;
        Map<String, Object> currentTask = bpmi.getNowNode(workflow); // 获取当前任务
        Long nextTaskId = longValue(runData, "nextTaskId");
        Map<String, Object> nextTask =null;
        if(nextTaskId !=null){
            nextTask = bpmi.getWfTaskByID(nextTaskId);
        }else{
            nextTask = bpmi.findNextNormalNode(workflow);
        }

        if (currentTask != null) {
            Map<String, Object> updateExecutor = null;
            Long taskCurentUserID = MapTool.longValue(runData, "currentExecutorID");
            List<Map<String, Object>> taskExcuteList = bpmi.getTaskExcute(MapTool.id(workflow),
                    MapTool.id(currentTask));

            // 更新当前任务的执行状态
            for (Map<String, Object> ei : taskExcuteList) {
                Long longValue = MapTool.longValue(ei, "executorID");
                Long passwordIdBy = crudService.getPasswordIdBy(longValue);
                if (taskCurentUserID.equals(passwordIdBy)) {
                    updateExecutor = ei;
                }
            }

            if (updateExecutor != null) {
                updateExecutor.put("executorStatus", WFEConstants.WF_EXEC_USERSTATE_EXECED);
                updateExecutor.put("executorDecision", BpmDo.decisionKey(runData));
                crudService.update(updateExecutor);
            }

            // 记录当前任务的历史信息
            addHistory(workflow, runData, currentTask);

            // 判断任务执行人是否达到最大数量
            Map<String, Object> taskProperty = BpmDo.taskProperty(currentTask);
            Map<String, Object> taskProperties = taskProperty;
            if (null != taskProperties) {
                boolean flag = checkMaxExecutor(taskExcuteList, taskProperties);
                if (flag) {
                    return true;
                }
            }

            // 更新当前任务状态为结束
            currentTask.put("taskStatus", WFEConstants.WFTASK_STATUS_END);
            crudService.update(currentTask);

            // 执行业务逻辑和子流程触发
            String bizClassName = BpmDo.invokeClass(taskProperty);
            if (bizClassName != null && bizClassName.trim().length() > 0) {
                BpmTaskBizExcute bizExecute = (BpmTaskBizExcute) Class.forName(bizClassName).newInstance();
                if (bizExecute != null) {
                    bizExecute.disagreeBizExecute(workflow, runData);
                }
            }
            subFlowTrigger(workflow, runData, taskProperty, nextTask);

            // 更新任务间的关系
//            this.updateRelation(workflow, currentTask, nextTask);

            // 处理下一个任务
            if (nextTask != null) {
                nextTask.put("taskStatus", WFEConstants.WFTASK_STATUS_READY);
                Map<String, Object> taskPropOfNext = BpmDo.taskProperty(nextTask);

                if (taskPropOfNext!=null &&TaskProperties.getTaskAutoExecute(taskPropOfNext) == WFEConstants.DB_BOOLEAN_TRUE) {
                    this.autoExecutePerform(workflow, nextTask, runData);
                } else {
                    long[] nextExecutorIDArray =splitLong(runData,"nextTaskExecutorIDs");
                    if (nextExecutorIDArray != null && nextExecutorIDArray.length > 0) {
                        for (int i = 0; i < nextExecutorIDArray.length; i++) {
                            addExecutor(nextTask, nextExecutorIDArray[i]);
                        }
                    }
                    crudService.update(nextTask);
                }
                workflow.put(NOW_TASK_NAME,string(nextTask,"title"));
                workflow.put(NOW_TASK_IDS, BpmDo.id(nextTask));
            } else {
                // 流程结束相关的处理
                BpmDo.clear(workflow, NOW_TASK_IDS);
                workflow.put("wfStatus", WFEConstants.WFSTATUS_END);

                if (BpmDo.triggerSubWfFlag(workflow) == WFEConstants.DB_BOOLEAN_TRUE) {
                    long triggerWorkflowID = BpmDo.triggerWfInsId(workflow);
                    Map<String, Object> triggerWorkflow = bizWfInstanceManager.getBpmi(triggerWorkflowID);
                    bizWfInstanceManager.runFlow(triggerWorkflow, adminService.getCurrentPasswordId());
                }
            }
            crudService.update(workflow);
            crudService.delRelation(id(workflow),"nowTask");
            crudService.relate("nowTask",id(workflow),id(nextTask));
        }
        retFlag = true;
        return retFlag;
    }

    /**
     * Map<String, Object> history = new HashMap<>();
     * history.setID(IDGenerator.nextLongId());
     * history.setWfInstanceID(currentTask.getWfInstanceID());
     * history.setWfTaskID(currentTask.getID );
     * history.setWfExecuteHistory(BpmDo.taskExecuteConfirm(runData));
     * history.setWfTaskDecision(BpmDo.decisionKey(runData));
     * history.setHistoryCreateEmpID(BpmDo.currentExecutorId(runData));
     * history.setTaskComeDatetime(runData.getCurrTaskComeDatetime());
     * history.setHistoryCreateEmpName(BpmDo.currentExecutorName(runData));
     * history.setWfTaskDecisionNameZh(BpmDo.decisionName(runData));
     * history.setHistoryCreateDatetime(System.currentTimeMillis());
     *
     * @param runData
     * @param currentTask
     * @return
     */
    private Map<String, Object> addHistory(Map<String, Object> runData, Map<String, Object> currentTask) {
        Map<String, Object> history = new HashMap<>();

        history.put("instanceID", MapTool.longValue(runData, "workflowId"));
        history.put("taskID", BpmDo.id(currentTask));

        history.put("wfExecuteHistory", BpmDo.taskExecuteConfirm(runData));
        history.put("decision", BpmDo.decisionKey(runData));
        history.put("executorID", MapTool.longValue(runData, "currentExecutorID"));
        history.put("decisionName", BpmDo.decisionName(runData));
        history.put("createTime", System.currentTimeMillis());
        history.put("taskComeDatetime", BpmDo.taskComeTime(runData));
        history.put("opinion", MapTool.string(runData, "executeComment"));
        crudService.addNew(history, "BpmHistory");
        return history;
    }

    public boolean accept(Map<String, Object> workflow, Map<String, Object> runData) {
        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (runData == null) {
            throw new CrudBaseException("流程流转信息不对,不能进行相关操作");
        }
        boolean retFlag = false;

        Map<String, Object> currentTask = bpmi.getNowNode(workflow);
        if (currentTask != null && BpmDo.taskStatus(currentTask) == WFEConstants.WFTASK_STATUS_WAIT) {
            Map<String, Object> updateExecutor = null;
            Long taskCurentUserID = MapTool.longValue(runData, "currentExecutorID");
            List<Map<String, Object>> taskExcuteList = bpmi.getTaskExcute(MapTool.id(workflow),
                    MapTool.id(currentTask));

            for (Map<String, Object> ei : taskExcuteList) {
                Long longValue = MapTool.longValue(ei, "executorID");
                Long passwordIdBy = crudService.getPasswordIdBy(longValue);
                if (taskCurentUserID.equals(passwordIdBy)) {
                    updateExecutor = ei;
                }
            }

            if (updateExecutor != null) {
                updateExecutor.put(BpmDo.EXECUTOR_STATUS, WFEConstants.WF_EXEC_USERSTATE_NONE);
                updateExecutor.put(BpmDo.EXECUTOR_DECISION, WFEConstants.WFDECISION_NONE);

                currentTask.put("taskStatus", WFEConstants.WFTASK_STATUS_READY);
                crudService.update(currentTask);
                crudService.update(updateExecutor);
            }
        }
        retFlag = true;
        return retFlag;
    }

    public boolean turnback(Map<String, Object> workflow, Map<String, Object> runData)
            throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (runData == null || BpmDo.decisionKey(runData) == null
                || !BpmDo.decisionKey(runData).equals(WFEConstants.WFDECISION_TURNBACK)
                || WfPc.getNextTaskID(runData) <= 0) {
            throw new CrudBaseException("流程流转信息不对,不能进行相关操作");
        }

        boolean retFlag = false;

        Map<String, Object> currentTask = bpmi.getNowNode(workflow);

        Map<String, Object> turnBackTask = bpmi.getWfTaskByID(MapTool.longValue(runData, "nextTaskID"));
        // 处理打回任务
        if (turnBackTask != null) {

            // 更新到指定任务，同时处理打回任务与当前任务间已完成的简单任务
            // 清理路径数据
            Long currentId = id(currentTask);
            String querySteps = "match p=(m:BpmNode)-[:nextStep*1..20]->(mi:BpmNode)" + " where id(m)="
                    + id(turnBackTask) + " and id(mi)=" + currentId + " return nodes(p)  ";
            List<Map<String, Object>> ps = crudService.cypher(querySteps);

            for (Map<String, Object> pi : ps) {
                List<Node> nodes = MapTool.listNode(pi, "nodes(p)");
                for (Node ni : nodes) {
                    if (!currentId.equals(ni.getId()) && !id(turnBackTask).equals(ni.getId())) {
                        turnBackTask(crudService.getNodeMapById(ni.getId()));
                    }
                }
            }

            String queryExecuteList = "match (m:BpmNode)-[r]->(e:BpmTaskExecute) where id(m)=" + BpmDo.id(turnBackTask)
                    + " return e";
            List<Map<String, Object>> executorList = crudService.cypher(queryExecuteList);
            if (executorList != null && executorList.size() > 0) {
                int listSize = executorList.size();
                for (int i = 0; i < listSize; i++) {
                    Map<String, Object> updateExecutor = executorList.get(i);
                    updateExecutor.put(EXECUTOR_DECISION, WFDECISION_NONE);
                    updateExecutor.put(EXECUTOR_STATUS, WF_EXEC_USERSTATE_NONE);
                    updateExecutor.put("taskComeDatetime", System.currentTimeMillis());
                    crudService.update(updateExecutor);
                }
            }
            turnBackTask.put("taskStatus", WFEConstants.WFTASK_STATUS_READY);
            crudService.update(turnBackTask);
            workflow.put(NOW_TASK_IDS, id(turnBackTask));
            crudService.update(workflow);
        }

        // + " and all(x in nodes(p) where x.taskStatus="+WFTASK_STATUS_END+" ) "
        // + " foreach(t in nodes(p)| match(t)-[r1]->(e:BpmTaskExecute) delete r1,e)"
        // + " foreach(t in nodes(p)| SET (t.) )"

        // 处理当前任务
        long taskComeDatetime = 0;
        if (currentTask != null) {
            currentTask.put("taskStatus", WFEConstants.WFTASK_STATUS_WAIT);
            turnBackTask(currentTask);
        }
        // 处理任务履历信息
        addHistory(workflow, runData, currentTask);
        // this.releaseRelation(workflow, turnBackTask, currentTask);

        String bizClassName = BpmDo.invokeClass(BpmDo.taskProperty(currentTask));
        if (bizClassName != null && bizClassName.trim().length() > 0) {
            BpmTaskBizExcute bizExecute = (BpmTaskBizExcute) Class.forName(bizClassName).newInstance();
            if (bizExecute != null) {
                bizExecute.turnbackBizExecute(workflow, runData);
            }
        }
        retFlag = true;
        return retFlag;
    }

    /**
     * 将任务状态重置，并清除相关的执行信息。
     * 该方法主要用于处理正常类型的任务，将任务状态重置为等待状态，并从数据库中更新该任务的状态。
     * 对于简单任务，还会清除其执行人和关联信息。
     *
     * @param task 一个包含任务信息的Map对象。
     */
    private void turnBackTask(Map<String, Object> task) {
        if (null != task) {
            Map<String, Object> stask = null;
            // 判断任务类型，如果是正常类型的任务，则进行状态重置和数据库更新
            if (NODE_TYPE_NORMAL.equals(nodeType(task))) {
                stask = task;
                // 清理任务状态并更新数据库
                stask.put("taskStatus", WFEConstants.WFTASK_STATUS_WAIT);
                crudService.update(stask);
            }
            // 处理简单任务，清除执行人和关联信息
            Long id2 = id(stask);
            if (id2 != null) {
                // 尝试删除任务执行人和关联信息，如果失败则只删除关联信息
                try {
                    crudService.execute("match (m:BpmNode)-[r]->(e:BpmTaskExecute),(e)-[r1]-() where id(m)=" + id2
                            + " delete r1,e");
                } catch (Exception e) {
                    crudService.execute("match (m:BpmNode)-[r]->(e:BpmTaskExecute) where id(m)=" + id2 + " delete r");
                }
            }
        }
    }


    /**
     * 执行撤回操作，用于处理流程的撤回操作。
     * 
     * @param workflow 流程实例数据，包含流程的相关信息。
     * @param runData 流程运行时的数据，用于判断当前的流程状态。
     * @return boolean 操作成功返回true，失败抛出异常。
     * @throws CrudBaseException 如果流程实例为空或流程流转信息不正确时，抛出异常。
     */
    public boolean callback(Map<String, Object> workflow, Map<String, Object> runData) {
        // 检查流程实例和流程流转信息的合法性
        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (null == runData || null == BpmDo.decisionKey(runData)
                || !WFEConstants.WFDECISION_CALLBACK.equalsIgnoreCase(BpmDo.decisionKey(runData))) {
            throw new CrudBaseException("流程流转信息不对,不能进行相关操作");
        }
    
        boolean retFlag = false;
        Map<String, Object> currentTask = bpmi.getNowNode(workflow); // 获取当前任务节点
        Map<String, Object> previewTask = null; // 初始化前置任务节点
        if (currentTask != null) {
            previewTask = bpmi.getPreviewWfTask(BpmDo.id(currentTask));
            
            // 清理当前任务的执行人信息，并更新任务状态为等待
            List<Map<String, Object>> executorList = BpmDo.taskExecutorList(currentTask);
            if (executorList != null && executorList.size() > 0) {
                for (Map<String, Object> delExecutor : executorList) {
                    crudService.removeById(BpmDo.id(delExecutor));
                }
                BpmDo.removeAllExecutor(currentTask);
            }
            currentTask.put("taskStatus", WFEConstants.WFTASK_STATUS_WAIT);
            crudService.update(currentTask);
    
            // 如果存在前置任务，处理前置任务的执行人状态和流程状态
            if (previewTask != null) {
                executorList = BpmDo.taskExecutorList(previewTask);
                if (executorList != null && executorList.size() > 0) {
                    for (Map<String, Object> updateExecutor : executorList) {
                        updateExecutor.put(BpmDo.EXECUTOR_DECISION, WFEConstants.WFDECISION_NONE);
                        updateExecutor.put(BpmDo.EXECUTOR_STATUS, WFEConstants.WF_EXEC_USERSTATE_NONE);
                        crudService.update(updateExecutor);
                    }
                }
    
                // 更新前置任务和当前任务的流程状态标识，并更新任务状态
                Map<String, Object> realNext = BpmDo.realNextRelation(previewTask);
                if (null != realNext && BpmDo.realFlowRoadFlag(realNext) == WFEConstants.DB_BOOLEAN_TRUE) {
                    realNext.put("realFlowRoadFlag", WFEConstants.DB_BOOLEAN_FALSE);
                    crudService.update(realNext);
                }
                Map<String, Object> currentTaskRelation = BpmDo.realPreRel(currentTask);
                if (null != currentTaskRelation
                        && BpmDo.realFlowRoadFlag(currentTaskRelation) == WFEConstants.DB_BOOLEAN_TRUE) {
                    currentTaskRelation.put("realFlowRoadFlag", WFEConstants.DB_BOOLEAN_FALSE);
                    crudService.update(currentTaskRelation);
                }
    
                previewTask.put("taskStatus", WFEConstants.WFTASK_STATUS_READY);
                crudService.update(previewTask);
    
                // 记录当前操作，更新流程状态和任务状态
                addHistory(workflow, runData, currentTask);
                workflow.put(NOW_TASK_IDS, BpmDo.id(previewTask));
                crudService.update(workflow);
                crudService.update(previewTask);
            }
        }
        retFlag = true;
        return retFlag;
    }

    /**
     * 向前推进流程。
     *
     * @param workflow 流程实例数据，包含流程的当前状态等信息。
     * @param runData 流程运行时的数据，用于决定流程如何推进。
     * @return 返回一个布尔值，表示流程是否成功向前推进。
     * @throws CrudBaseException 如果流程实例为空、流程流转信息不正确，或者无法找到当前任务等情况下，抛出此异常。
     */
    public boolean forward(Map<String, Object> workflow, Map<String, Object> runData) {
        // 检查流程实例和运行数据的有效性
        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (runData == null || BpmDo.decisionKey(runData) == null
                || !BpmDo.decisionKey(runData).equals(WFEConstants.WFDECISION_FORWARD)) {
            throw new CrudBaseException("流程流转信息不对,不能进行相关操作");
        }

        boolean retFlag = false;
        // 获取当前任务信息
        Map<String, Object> currentTask = bpmi.getNowNode(workflow);

        if (currentTask != null) {
            // 更新当前任务的执行人状态
            Map<String, Object> updateExecutor = null;
            List<Map<String, Object>> executorList = BpmDo.taskExecutorList(currentTask);
            Long taskCurentUserID = MapTool.longValue(runData, "currentExecutorID");
            List<Map<String, Object>> taskExcuteList = bpmi.getTaskExcute(MapTool.id(workflow),
                    MapTool.id(currentTask));

            // 查找当前操作用户的执行信息
            for (Map<String, Object> ei : taskExcuteList) {
                Long longValue = MapTool.longValue(ei, "executorID");
                Long passwordIdBy = crudService.getPasswordIdBy(longValue);
                if (taskCurentUserID.equals(passwordIdBy)) {
                    updateExecutor = ei;
                }
            }
            if (updateExecutor != null) {
                // 更新执行人状态和决策信息
                updateExecutor.put(BpmDo.EXECUTOR_STATUS, WFEConstants.WF_EXEC_USERSTATE_EXECED);
                updateExecutor.put(BpmDo.EXECUTOR_DECISION, BpmDo.decisionKey(runData));
                crudService.update(updateExecutor);
            }

            // 添加下一个任务的执行人信息
            long[] nextExecutorIDArray = BpmDo.nextTaskExecutorIDs(runData);
            if (nextExecutorIDArray != null && nextExecutorIDArray.length > 0) {
                for (long ni : nextExecutorIDArray) {
                    addExecutor(currentTask, ni);
                }
            }

            // 记录当前任务的历史履历信息
            addHistory(workflow, runData, currentTask);
            crudService.update(currentTask);
        }
        retFlag = true;
        return retFlag;
    }

    /**
     * 判断任务执行次数是否达到最大执行数要求。
     * 此函数考虑了两种情况：所有任务必须执行和仅执行一定比例或数量的任务。
     *
     * @param wfTaskExecutor 任务执行者列表，包含每个任务的执行信息。
     * @param properties 任务属性，包含执行次数、最大执行数和最大执行比例等信息。
     * @return boolean 如果当前执行次数未达到最大执行要求，则返回true；否则返回false。
     */
    private boolean checkMaxExecutor(List<Map<String, Object>> wfTaskExecutor, Map<String, Object> properties) {
        boolean flag = false;
        if (null != wfTaskExecutor && wfTaskExecutor.size() > 0) {
            // 计算当前执行数量和需执行数量
            int executorNum = wfTaskExecutor.size();
            int needRun = 1;
            if (TaskProperties.getAllExecute(properties) == WFEConstants.DB_BOOLEAN_TRUE) {
                // 所有任务必须执行
                needRun = executorNum;
            } else {
                // 根据最大执行数和执行比例计算需执行数量
                int maxNum = TaskProperties.getMaxExecutorNum(properties);
                int runNum = (executorNum >= maxNum) ? maxNum : executorNum;
                needRun = Math.round(runNum * TaskProperties.getMaxExecutorPercent(properties));
            }
            // 保证需执行数量至少为1
            needRun = (1 >= needRun) ? 1 : needRun;

            // 统计已执行的数量
            int runed = 0;
            for (int i = 0; i < executorNum; i++) {
                Map<String, Object> run = wfTaskExecutor.get(i);
                if (BpmTaskExecute.getExecutorStatus(run) == WFEConstants.DB_BOOLEAN_TRUE) {
                    runed++;
                }
            }
            // 判断是否达到需执行数量要求
            flag = (runed < needRun) ? true : false;
        }
        return flag;
    }


    /**
     * 处理工作流动作的函数。
     *
     * @param workflow    包含工作流信息的Map对象。
     * @param runtimeData 包含运行时数据的Map对象。
     * @return retFlag 执行结果标志，true表示成功，false表示失败。
     * @throws EvalError              如果表达式评估出错。
     * @throws InstantiationException 如果实例化异常发生。
     * @throws IllegalAccessException 如果访问权限不允许。
     * @throws ClassNotFoundException 如果类未找到。
     */
    public boolean flowAction(Map<String, Object> workflow, Map<String, Object> runtimeData)
            throws EvalError, InstantiationException, IllegalAccessException, ClassNotFoundException {
        boolean retFlag = false;
        if (runtimeData != null) {
            // 检查当前执行人是否接受任务，未接受则自动接受
            boolean acceptFlag = bpmi.isAccept(MapTool.longValue(runtimeData,WfPc.currentExecutorID), workflow);
            if (!acceptFlag) {
                accept(workflow, runtimeData);
            }

            // 根据运行时数据决定工作流的下一步动作
            String decisionKey = BpmDo.decisionKey(runtimeData);
            // 执行提交动作
            if (decisionKey.equals(WFEConstants.WFDECISION_COMMIT)) {
                commit(workflow, runtimeData);
            }
            // 执行打回动作
            if (decisionKey.equals(WFEConstants.WFDECISION_TURNBACK)) {
                turnback(workflow, runtimeData);
            }
            // 执行同意动作
            if (decisionKey.equals(WFEConstants.WFDECISION_AGREE)) {
                agree(workflow, runtimeData);
            }
            // 执行不同意动作
            if (decisionKey.equals(WFEConstants.WFDECISION_DISAGREE)) {
                disagree(workflow, runtimeData);
            }
            // 执行跳转动作
            if (decisionKey.equals(WFEConstants.WFDECISION_RELOOP)) {
                reloop(workflow, runtimeData);
            }
            // 执行撤回动作
            if (decisionKey.equals(WFEConstants.WFDECISION_CALLBACK)) {
                callback(workflow, runtimeData);
            }
        }
        retFlag = true;
        return retFlag;
    }

    /**
     * 自动审批
     * <p>
     * 传入参数（业务主表ID，流程模板Mark，业务表表名）
     *
     */
    public void autoAgree(String flowId) throws Exception {
        Long currentUserId = adminService.getCurrentPasswordId();
        Map<String, Object> workflow = bpmi.getFlowi(Long.valueOf(flowId));

        if (workflow != null) {
            Map<String, Object> currentTask = bpmi.getNowNode(workflow);
            List<Map<String, Object>> executorList = BpmDo.taskExecutorList(currentTask);
            Map<String, Object> executor = null;
            for (Map<String, Object> bean : executorList) {
                if (currentUserId.equals(BpmDo.executorId(bean))) {
                    executor = bean;
                }
            }
            if (executor == null) {
                throw new WorkFlowException("没有权限操作该数据。");
            }
            Map<String, Object> decision = BpmDo.wfTaskDecision(currentTask, WFEConstants.WFDECISION_AGREE);

            Map<String, Object> wfRuntimeData = new HashMap<String, Object>();
            wfRuntimeData.put("decisionKey", WFEConstants.WFDECISION_AGREE);
            wfRuntimeData.put("workflowId", BpmDo.id(workflow));
            wfRuntimeData.put("currTaskComeDatetime", BpmDo.taskComeTime(executor));

            if (decision != null && BpmDo.decisionViewName(decision) != null
                    && BpmDo.decisionViewName(decision).trim().length() > 0) {
                wfRuntimeData.put(BpmDo.DECISION_NAME, BpmDo.decisionViewName(decision));
            } else {
                wfRuntimeData.put(BpmDo.DECISION_NAME,
                        WFEConstants.convertWfDecisionNameZh(WFEConstants.WFDECISION_AGREE));
            }

            wfRuntimeData.put(BpmDo.CURRENT_EXECUTOR_ID, currentUserId);
            wfRuntimeData.put(BpmDo.CURRENT_EXECUTOR_NAME, adminService.getCurrentAccount());

            // 获取下个流程节点的审批人
            BpmExecutorFormulaParse executorParse = null;
            executorParse = new BpmExecutorFormulaParse(BpmInstance.getBizTableName(workflow),
                    BpmInstance.getBizDataID(workflow), workflow,crudService,bss, adminService);
            // 判断执行人是否为空
            // 如果不为空，将执行人 放置wfeRunData里
            Map<String, Object> nextTask = null;
            Map<String, Object> nextNormalNode = bpmi.findNextNormalNode(workflow);
            if (nextNormalNode != null) {
                nextTask = nextNormalNode;
                long[] nextExecutorIDArray = null;
                //解析角色數據
//		nextExecutorIDArray = FormulaParseUtil.parseExecutorFormula(executorParse, extendsList);
                wfRuntimeData.put("nextTaskExecutorIDs", nextExecutorIDArray);
            }
            // 测试报错情况
            // else{
            // throw new WorkFlowException("测试。");
            // }
            this.flowAction(workflow, wfRuntimeData);
        }
    }
}
