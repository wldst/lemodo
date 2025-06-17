package com.wldst.ruder.module.workflow.biz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.thoughtworks.xstream.core.ReferenceByIdMarshaller.IDGenerator;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.beans.Decision;
import com.wldst.ruder.module.workflow.beans.TaskExtendInfo;
import com.wldst.ruder.module.workflow.beans.SimpleTask;
import com.wldst.ruder.module.workflow.beans.BpmTask;
import com.wldst.ruder.module.workflow.beans.TaskProperties;
import com.wldst.ruder.module.workflow.beans.TaskRelation;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.template.beans.TbTask;
import com.wldst.ruder.module.workflow.template.beans.Tend;
import com.wldst.ruder.module.workflow.template.beans.Toutput;
import com.wldst.ruder.module.workflow.template.beans.Tshrink;
import com.wldst.ruder.module.workflow.template.beans.Tsimple;
import com.wldst.ruder.module.workflow.template.beans.TtaskDecision;
import com.wldst.ruder.module.workflow.template.beans.TtaskExecutor;
import com.wldst.ruder.module.workflow.template.beans.TtaskProp;
import com.wldst.ruder.module.workflow.template.beans.Twf;
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.module.workflow.util.WFEConstants;

/**
 * 流程引擎业务辅助类<br>
 * 将流程引擎中大多数业务代码在改类中实现<br>
 * 避免对外提供的接口中业务代码过多
 *
 * @author wldst
 */
@Component
public class BpmEngineAssist extends BpmDo{
    // 日志对象
    private static Logger logger=LoggerFactory.getLogger(BpmEngineAssist.class);
    @Autowired
    private CrudNeo4jService crudService;
    // 流程实例基本信息
    // 流程任务基本信息
    // 流程实例之任务决策信息
    // 流程任务实例之属性
    // 流程任务之间的关系
    // 流程任务之扩展信息
    // 流程任实例执行人信息
    // 流程实例之历史履历信息

    public boolean deleteWfInstanceDB(long wfInstanceID){
        boolean retFlag=false;
        if(wfInstanceID<=0){
            throw new CrudBaseException("流程实例主键数据不正确,不能进行相关操作");
        }

        // this.daoWfInsHistory.deleteWfAll(wfInstanceID);
        // this.daoWfTaskDecision.deleteWfAll(wfInstanceID);
        // this.daoWfTaskExecutor.deleteWfAll(wfInstanceID);
        // this.daoWfTaskRelation.deleteWfAll(wfInstanceID);
        // this.daoWfTaskProperties.deleteWfAll(wfInstanceID);
        // this.daoWfTaskExtendsInfo.deleteWfAll(wfInstanceID);
        // this.daoWfTaskBaseInfo.deleteWfAll(wfInstanceID);
        // this.daoWfInstanceBaseInfo.delete(wfInstanceID);

        retFlag=true;
        return retFlag;
    }

    /**
     * 创建流程实例，创建流程
     *
     * @param wfTemplate
     * @param bizDataID
     * @param bizTableName
     * @param triggerFlowID
     * @param triggerTaskID
     * @param currentUserID
     * @return
     */
    public Map<String, Object> createWorkFlowInstance(Map<String, Object> wfTemplate, long bizDataID, String bizTableName,
                                                      long triggerFlowID, long triggerTaskID, long currentUserID){
        if(wfTemplate==null){
            throw new CrudBaseException("流程模板未定义,不能进行相关操作");
        }
        Map<String, Object> wfInstance=new HashMap<>();

        BpmInstance.setBizDataID(bizDataID, wfInstance);
        BpmInstance.setBizTableName(bizTableName, wfInstance);
        BpmInstance.setWfTemplateID(Twf.getID(wfTemplate), wfInstance);
        BpmInstance.setTriggerWfInsID(triggerFlowID, wfInstance);
        BpmInstance.setTriggerTaskID(triggerTaskID, wfInstance);
        if(triggerFlowID>0&&triggerTaskID>0){
            BpmInstance.setTriggerSubWfFlag(WFEConstants.DB_BOOLEAN_TRUE, wfInstance);
        }else{
            BpmInstance.setTriggerSubWfFlag(WFEConstants.DB_BOOLEAN_FALSE, wfInstance);
        }
        BpmInstance.setWfCreateDatetime(System.currentTimeMillis(), wfInstance);
        BpmInstance.setWfCreateEmpID(currentUserID, wfInstance);
        BpmInstance.setWfStatus(WFEConstants.WFSTATUS_INIT, wfInstance);
        BpmInstance.setWorkflowDescript(Twf.getWfDescript(wfTemplate), wfInstance);
        BpmInstance.setWorkflowName(Twf.getWfName(wfTemplate), wfInstance);

        this.createStartTask(wfInstance, wfTemplate);
        this.createEndTask(wfInstance, wfTemplate);
        this.createSimpleTask(wfInstance, wfTemplate);
        this.createBranchTask(wfInstance, wfTemplate);
        this.createShrinkTask(wfInstance, wfTemplate);

//	this.arrangeWorkflow(wfInstance, wfTemplate);

        this.saveWfInstanceDB(wfInstance);
        return wfInstance;
    }

    /**
     * 保存流程实例信息到数据库中
     *
     * @param workflow 流程实例对象
     * @throws CrudBaseException
     */
    private void saveWfInstanceDB(Map<String, Object> workflow){
        if(workflow==null){
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        crudService.saveByBody(workflow, "BpmInstance");
        crudService.save(BpmInstance.getAllWfTaskList(workflow), "BpmTask");
        crudService.save(BpmInstance.getAllWfTaskPropertyList(workflow), "WfProperty");
        crudService.save(BpmInstance.getAllWfTaskDecisionList(workflow), "WfTaskDecision");
        crudService.save(BpmInstance.getAllWfTaskRelationList(workflow), "WfTaskRelation");
        crudService.save(BpmInstance.getAllWfTaskExtendsList(workflow), "WfTaskExtendsInfo");
    }

    /**
     * 根据流程模板信息生成流程开始任务实例
     *
     * @param instance   流程实例对象
     * @param wfTemplate 流程模板 @
     */
    private void createStartTask(Map<String, Object> instance, Map<String, Object> wfTemplate){
        if(wfTemplate==null){
            throw new CrudBaseException("流程模板未定义,不能进行相关操作");
        }
        Map<String, Object> tempStartTask=Twf.getStartTask(wfTemplate);
        createTask(instance, tempStartTask, WFEConstants.WFTASK_TYPE_START);
    }

    /**
     * 根据流程模板信息生成流程结束任务实例
     *
     * @param instance   流程实例对象
     * @param templateWF 流程模板 @
     */
    private void createEndTask(Map<String, Object> instance, Map<String, Object> templateWF){
        if(templateWF==null){
            throw new CrudBaseException("流程模板未定义,不能进行相关操作");
        }
        Map<String, Object> tempEndTask=Twf.getEndTask(templateWF);
        createTask(instance, tempEndTask, WFEConstants.WFTASK_TYPE_END);
    }

    private void createTask(Map<String, Object> workflow, Map<String, Object> tempEndTask, int taskType){
        Map<String, Object> newTask=copyTaskInfo(workflow, tempEndTask, taskType);
        BpmInstance.addWorkflowTask(newTask, workflow);
    }

    private Map<String, Object> copyTaskInfo(Map<String, Object> workflow, Map<String, Object> tempEndTask,
                                             int taskType){
        Map<String, Object> newTask=new HashMap<>();
        BpmTask.setInstanceID(BpmInstance.id(workflow), newTask);
        BpmTask.setInnerTaskID(BpmInstance.taskInnerId(workflow), newTask);
        BpmTask.setDescript(Tend.getDescript(tempEndTask), newTask);
        BpmTask.setName(Tend.getWfTaskName(tempEndTask), newTask);
        BpmTask.setTaskStatus(WFEConstants.WFTASK_STATUS_WAIT, newTask);
        BpmTask.setTaskType(taskType, newTask);
        return newTask;
    }

    /**
     * 根据流程模板信息创建流程普通任务实例信息<br>
     * 并将其填充到流程实例中
     *
     * @param workflow   流程实例
     * @param templateWF 流程模本定义信息 @
     */
    private void createSimpleTask(Map<String, Object> workflow, Map<String, Object> templateWF){
        if(templateWF==null){
            throw new CrudBaseException("流程模板未定义,不能进行相关操作");
        }

        List<Map<String, Object>> tempSimpleTaskList=Twf.getSimpleTasks(templateWF);

        if(tempSimpleTaskList!=null&&tempSimpleTaskList.size()>0){
            for(Map<String, Object> tempSimpleTask : tempSimpleTaskList){
                Map<String, Object> simpleTask=copyTaskInfo(workflow, tempSimpleTask,
                        WFEConstants.WFTASK_TYPE_SIMPLE);

                Map<String, Object> taskWithProperty=fillSimpleTaskProperty(tempSimpleTask);
                long wfInstanceID=BpmTask.getInstanceID(simpleTask);
                long taskId=BpmTask.getID(simpleTask);
                SimpleTask.setTaskProperty(taskWithProperty, simpleTask);

                fillSimpleTaskDecision(simpleTask, tempSimpleTask);

                List<Map<String, Object>> executorList=BpmTask.taskExecutorList(tempSimpleTask);
                if(executorList!=null){
                    int executorSize=executorList.size();
                    for(int j=0; j<executorSize; j++){
                        Map<String, Object> tempExecutor=executorList.get(j);
                        Map<String, Object> extendInfo=new HashMap<>();

                        // extendInfo.setID(IDGenerator.nextLongId());
                        TaskExtendInfo.setWfInstanceID(wfInstanceID, extendInfo);
                        TaskExtendInfo.setWfTaskID(taskId, extendInfo);
                        TaskExtendInfo.setInnerTaskID(BpmTask.getInnerTaskID(simpleTask), extendInfo);
                        TaskExtendInfo.setExecutorCondition(TtaskExecutor.getExpression(tempExecutor), extendInfo);

                        SimpleTask.addTaskExtendsInfo(extendInfo, simpleTask);
                    }
                }

                BpmInstance.addWorkflowTask(simpleTask, workflow);
            }
        }
    }

    /**
     * 填充普通任务决策信息
     *
     * @param simpleTask     普通任务
     * @param tempSimpleTask 普通任务模板信息 @
     */
    private void fillSimpleTaskDecision(Map<String, Object> simpleTask, Map<String, Object> tempSimpleTask){
        if(tempSimpleTask==null){
            throw new CrudBaseException("流程普通任务模板定义为空,不能进行相关操作");
        }

        List<Map<String, Object>> decisionList=Twf.taskDecisionList(tempSimpleTask);

        if(decisionList!=null&&decisionList.size()>0){
            for(Map<String, Object> tempDecision : decisionList){

                Map<String, Object> decision=new HashMap<>();
                Decision.setInstanceID(SimpleTask.getInstanceID(simpleTask), decision);
                Decision.setTaskID(SimpleTask.getID(simpleTask), decision);
                decision.put(Decision.decisionDescript, Decision.getDescript(tempDecision));
                Decision.setName(Decision.taskName(tempDecision), decision);
                Decision.setViewName(Decision.decisionViewName(tempDecision), decision);
                Decision.setExecuteType(Decision.getExecuteType(tempDecision), decision);
                Decision.setOrderNO(Decision.getOrderNO(tempDecision), decision);

                SimpleTask.addTaskDecision(decision, simpleTask);
            }
        }
    }

    /**
     * 填充流程任务属性
     *
     * @param tempSimpleTask 普通任务模板定义信息
     * @return 流程任务属性 @
     */
    private Map<String, Object> fillSimpleTaskProperty(Map<String, Object> tempSimpleTask){
        if(tempSimpleTask==null){
            throw new CrudBaseException("流程普通任务模板定义为空,不能进行相关操作");
        }
        Map<String, Object> taskProperty=new HashMap<>();

        return taskProperty;
    }

    /**
     * 创建流程分支任务信息
     *
     * @param workflow   流程实例对象
     * @param templateWF 流程定义模板 @
     */
    private void createBranchTask(Map<String, Object> workflow, Map<String, Object> templateWF){
        if(templateWF==null){
            throw new CrudBaseException("流程模板未定义,不能进行相关操作");
        }

        List<Map<String, Object>> branchList=Twf.getBranchTask(templateWF);
        if(branchList!=null&&branchList.size()>0){
            for(Map<String, Object> bi : branchList){
                Map<String, Object> branchTask=new HashMap<>();
                BpmTask.setInstanceID(BpmInstance.id(workflow), branchTask);
                BpmTask.setInnerTaskID(BpmTask.taskInnerId(bi), branchTask);
                BpmTask.setName(BpmTask.taskName(bi), branchTask);
                BpmTask.setDescript(BpmTask.descript(bi), branchTask);
                BpmTask.setTaskStatus(WFEConstants.WFTASK_STATUS_WAIT, branchTask);
                BpmTask.setTaskType(WFEConstants.WFTASK_TYPE_BRANCH, branchTask);
//		BpmTask.setPairTaskInnerID(TbTask.getPairNode(bi), branchTask);

                BpmInstance.addWorkflowTask(branchTask, workflow);
            }
        }
    }

    /**
     * 创建流程收缩任务信息
     *
     * @param workflow   流程实例对象
     * @param templateWF 流程定义模板 @
     */
    private void createShrinkTask(Map<String, Object> workflow, Map<String, Object> templateWF){
        if(templateWF==null){
            throw new CrudBaseException("流程模板未定义,不能进行相关操作");
        }

        List<Map<String, Object>> shrinkList=Twf.getShrinTaskList(templateWF);
        if(shrinkList!=null&&shrinkList.size()>0){
            for(Map<String, Object> si : shrinkList){
                int wftaskType=WFEConstants.WFTASK_TYPE_SHRINK;
                createTask(workflow, si, wftaskType);
            }
        }
    }


}
