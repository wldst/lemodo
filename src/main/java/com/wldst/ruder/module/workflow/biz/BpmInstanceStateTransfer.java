package com.wldst.ruder.module.workflow.biz;

import bsh.EvalError;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.BeanShellService;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.formula.BpmExecutorFormulaParse;
import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.MapTool;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 流程实例状态转换业务实现
 */
@Component
public class BpmInstanceStateTransfer extends BpmDo {
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(BpmInstanceStateTransfer.class);

    // 流程实例基本信息之数据库操作
    // 流程任务基本信息数据库操作
    // 执行人数据库操作
    // 流程任务关系数据库操作
    @Autowired
    private BpmInstance bpmi;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudNeo4jService crudService;
    @Autowired
    private BeanShellService bss;
    @Autowired
    private RelationService relationService;
    public boolean run(Map<String, Object> flowi, Long currentUserId) throws EvalError {
        boolean retFlag = false;
        BpmExecutorFormulaParse parse = null;

        if (flowi == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (BpmDo.wfStatus(flowi) != WFEConstants.WFSTATUS_INIT
                && BpmDo.wfStatus(flowi) != WFEConstants.WFSTATUS_SUSPEND
                && BpmDo.wfStatus(flowi) != WFEConstants.WFSTATUS_PAUSE) {
            throw new CrudBaseException("流程实例状态不为初始、挂起、暂停,不能进入运行状态");
        }

        Long bizId = bizId(flowi);
        String bizLabel = docType(flowi);
        relateBizInfo(flowi, bizId, bizLabel);
        parse = new BpmExecutorFormulaParse(bizLabel, bizId, flowi,crudService,bss, adminService);
        flowi.put("wfStatus", WFEConstants.WFSTATUS_RUN);

        String nowTaskIDs = BpmInstance.nowTaskIDs(flowi);
        if (nowTaskIDs == null || nowTaskIDs.trim().length() <= 1) {// 如果流程没有当前任务,表示流程刚创建
            List<Map<String, Object>> addExecutorList = null;
            Map<String, Object> nextSimpleTask = bpmi.findNextNormalNode(flowi);
            Map<String, Object> sTask = BpmInstance.startTask(flowi);
            if (nextSimpleTask != null) {
                Long taskId = BpmDo.id(nextSimpleTask);
                updateNowTask(flowi, nextSimpleTask);
                nextSimpleTask.put("taskStatus", WFEConstants.WFTASK_STATUS_READY);
                //
                Long nowTimeLong = DateTool.nowLong();
                List<Map<String, Object>> nodeUserList = MapTool.listMapObject(nextSimpleTask, "nodeUserList");

                addExecutorList = new ArrayList<>();
                sTask.put("taskStatus", WFEConstants.WFTASK_STATUS_END);
                recordCommit(flowi, currentUserId, sTask, nowTimeLong);
                Set<Long> executorIDSet = splitValueLongSet(flowi, "executorIDs");


                if (nodeUserList != null && !nodeUserList.isEmpty()) {
                    for (Map<String, Object> ui : nodeUserList) {
                        if (!executorIDSet.contains(MapTool.longValue(ui, "userId"))) {
                            continue;
                        }
                        Map<String, Object> executor = new HashMap<>();
                        executor.put("taskID", id(nextSimpleTask));
                        executor.put("instanceID", id(flowi));
                        executor.put("executorStatus", WFEConstants.WF_EXEC_USERSTATE_NONE);
                        executor.put("executorDecision", null);

                        executor.put(BpmDo.TASK_COME_TIME, nowTimeLong);
                        executor.put("createTime", nowTimeLong);
                        executor.put("name", MapTool.string(ui, "username"));
                        executor.put("account", MapTool.string(ui, "account"));
                        executor.put("executorID", MapTool.string(ui, "userId"));
                        Node addNew = crudService.addNew(executor, "BpmTaskExecute");
                        long excuteId = addNew.getId();
                        Long uId = MapTool.longValue(ui, "userId");

                        addExcuteRelation(flowi, nextSimpleTask, excuteId, uId);
                        addExecutorList.add(executor);
                    }
                } else {
                    //用户为空，则去角色中查询用户信息
                    nodeUserList = crudService.cypher("Match (n:Role)<-[r:HAS_ROLE]-(u) where id(n)=" + longValue(nextSimpleTask, "roleID") + " return u");
                    for (Map<String, Object> ui : nodeUserList) {
                        Long userId = id(ui);
                        if (!executorIDSet.contains(userId)) {
                            continue;
                        }
                        Map<String, Object> executor = new HashMap<>();
                        executor.put("taskID", id(nextSimpleTask));
                        executor.put("instanceID", BpmDo.id(flowi));
                        executor.put("executorStatus", WFEConstants.WF_EXEC_USERSTATE_NONE);
                        executor.put("executorDecision", null);

                        executor.put(BpmDo.TASK_COME_TIME, nowTimeLong);
                        executor.put("createTime", nowTimeLong);
                        executor.put("name", MapTool.string(ui, "username"));
                        executor.put("account", MapTool.string(ui, "account"));
                        executor.put("executorID", userId);
                        Node addNew = crudService.addNew(executor, "BpmTaskExecute");
                        long excuteId = addNew.getId();
                        addExcuteRelation(flowi, nextSimpleTask, excuteId, userId);
                        addExecutorList.add(executor);
                    }
                }
            }

            // 保存流程信息
            crudService.update(sTask);
            crudService.update(nextSimpleTask);
        } else {// 流程非刚创建
            Map<String, Object> nextNormalNode = bpmi.findNextNormalNode(flowi);
            if (nextNormalNode != null) {
                updateNowTask(flowi, nextNormalNode);
                nextNormalNode.put("taskStatus", WFEConstants.WFTASK_STATUS_READY);
                crudService.update(nextNormalNode);
            } else {
                updateNowTask(flowi, null);
            }
        }
        crudService.update(flowi, id(flowi));
//        crudService.updateByKey(flowi,"wfStatus,nowTaskIDs,nowTaskName");
        return true;
    }

    /**
     * 历史记录以及添加相关关系
     *
     * @param flowi
     * @param currentUserId
     * @param sTask
     * @param nowTimeLong
     */
    private void recordCommit(Map<String, Object> flowi, Long currentUserId, Map<String, Object> sTask,
                              Long nowTimeLong) {
        Map<String, Object> hi = new HashMap<>();
        hi.put("taskID", id(sTask));
        hi.put("instanceID", BpmDo.id(flowi));
        hi.put("executorStatus", WFEConstants.WF_EXEC_USERSTATE_NONE);
        hi.put("decision", "提交");
        hi.put("executorID", currentUserId);
        hi.put("createTime", nowTimeLong);
        hi.put("opinion", MapTool.string(flowi, "executeComment"));
        crudService.addNew(hi, BpmDo.BPM_HISTORY);

        Long hiId = MapTool.id(hi);
        Long wfId = MapTool.id(flowi);
        Long currentNodeId = MapTool.id(sTask);
        relationService.addRel("history", wfId, hiId);
        relationService.addRel("user", hiId, currentUserId);
        relationService.addRel("node", hiId, currentNodeId);
    }

    public void addExcuteRelation(Map<String, Object> flowi, Map<String, Object> nextSimpleTask, long excuteId,
                                  Long uId) {
//	appendRelation(excuteId, "BpmTaskExecute", BpmDo.id(flowi), "BpmGraphInstance", "execute", "审批情况");
        appendRelation(excuteId, "BpmTaskExecute", id(nextSimpleTask), "BpmNode", "execute", "检查者");
        appendRelation(uId, "User", excuteId, "BpmTaskExecute", "executor", "执行者");
    }

    /**
     * 更新流程的当前任务信息。
     * 这个方法用于根据提供的下一节点信息，更新流程实例中当前任务的相关字段。
     * 如果下一节点存在，则更新当前任务名称和ID，并重置相关关系。
     * 如果下一节点不存在，表示流程结束，将流程状态更新为结束状态，并保存更新。
     *
     * @param flowi    当前流程实例的信息，作为一个Map存储。
     * @param nextNode 下一节点的信息，作为一个Map存储，包含节点标题和其它必要信息。
     */
    public void updateNowTask(Map<String, Object> flowi, Map<String, Object> nextNode) {
        // 尝试从nextNode中获取任务ID
        Long taskId = id(nextNode);
        if (taskId != null) {
            // 如果taskId不为空，更新flowi中的当前任务名称和ID
            flowi.put("nowTaskName", string(nextNode, "title"));
            flowi.put(NOW_TASK_IDS, taskId);
            // 重置任务与流程实例的关系
            resetRelation(taskId, "BpmNode", BpmDo.id(flowi), "BpmGraphInstance", "nowTask", "当前任务");
        } else {
            // 如果taskId为空，表示流程到达结束状态，更新流程状态并保存
            flowi.put("wfStatus", WFEConstants.WFSTATUS_END);
        }
        crudService.update(flowi, id(flowi));
//        neo4jService.save(flowi, "BpmGraphInstance");
    }


    private void relateBizInfo(Map<String, Object> flowi, Long bizId, String bizLabel) {
        flowi.put("bizId", bizId);
        resetRelation(bizId, bizLabel, BpmDo.id(flowi), "BpmGraphInstance", "bizData", "业务数据");
    }

    /**
     * 重置关系
     *
     * @param endId
     * @param endLabel
     * @param startId
     * @param startLabel
     * @param relLabel
     * @param relationName
     */
    private void resetRelation(Long endId, String endLabel, Long startId, String startLabel, String relLabel,
                               String relationName) {
        crudService.delRelation(startId, relLabel);
        if (endId == null) {
            return;
        }
        appendRelation(endId, endLabel, startId, startLabel, relLabel, relationName);
    }

    public void appendRelationList(List<Long> endIds, String endLabel, Long startId, String startLabel, String relLabel,
                                   String relationName) {
        for (Long ei : endIds) {
            appendRelation(ei, endLabel, startId, startLabel, relLabel, relationName);
        }

    }

    /**
     * 附加关系
     *
     * @param endId
     * @param endLabel
     * @param startId
     * @param startLabel
     * @param relLabel
     * @param relationName
     */
    public void appendRelation(Long endId, String endLabel, Long startId, String startLabel, String relLabel,
                               String relationName) {
        List<Map<String, Object>> relationDefineList = crudService.queryRelationDefine("startLabel", startLabel);

        if (relationDefineList == null || relationDefineList.isEmpty()) {
            crudService.saveRelationDefine(endLabel, relLabel, startLabel, relationName);
        } else {
            boolean containRel = false;
            for (Map<String, Object> ri : relationDefineList) {
                if (endLabel.equals(endLabel(ri)) && relLabel.equals(relLabel(ri))) {
                    containRel = true;
                }
            }
            if (!containRel) {
                crudService.saveRelationDefine(endLabel, relLabel, startLabel, relationName);
            }
        }
        crudService.relate(relLabel, relationName, startId, endId);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     */
    public boolean pause(Map<String, Object> workflow) {
        boolean retFlag = false;

        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (BpmDo.wfStatus(workflow) != WFEConstants.WFSTATUS_RUN) {
            throw new CrudBaseException("流程实例状态不为运行,不能进入暂停状态");
        }

        workflow.put("wfStatus", WFEConstants.WFSTATUS_PAUSE);

        crudService.update(workflow);
        retFlag = true;
        return retFlag;
    }

    public boolean terminate(Map<String, Object> workflow) {
        boolean retFlag = false;

        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (BpmDo.wfStatus(workflow) != WFEConstants.WFSTATUS_RUN
                && BpmDo.wfStatus(workflow) != WFEConstants.WFSTATUS_INIT
                && BpmDo.wfStatus(workflow) != WFEConstants.WFSTATUS_PAUSE
                && BpmDo.wfStatus(workflow) != WFEConstants.WFSTATUS_SUSPEND) {
            throw new CrudBaseException("流程实例状态不为运行、初始、挂起、暂停,不能进入终止状态");
        }

        workflow.put("wfStatus", WFEConstants.WFSTATUS_TERMINATE);

        crudService.update(workflow);
        retFlag = true;
        return retFlag;
    }

    public boolean suspend(Map<String, Object> workflow) {
        boolean retFlag = false;

        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (BpmDo.wfStatus(workflow) != WFEConstants.WFSTATUS_RUN) {
            throw new CrudBaseException("流程实例状态不为运行,不能进入挂起状态");
        }

        workflow.put("wfStatus", WFEConstants.WFSTATUS_SUSPEND);

        crudService.update(workflow);
        retFlag = true;
        return retFlag;
    }

    public boolean finish(Map<String, Object> workflow) {
        boolean retFlag = false;

        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (BpmDo.wfStatus(workflow) != WFEConstants.WFSTATUS_RUN) {
            throw new CrudBaseException("流程实例状态不为运行,不能进入结束状态");
        }

        workflow.put("wfStatus", WFEConstants.WFSTATUS_END);

        crudService.update(workflow);
        retFlag = true;
        return retFlag;
    }

    public boolean init(Map<String, Object> workflow) {
        boolean retFlag = false;

        if (workflow == null) {
            throw new CrudBaseException("流程实例为空,不能进行相关操作");
        }
        if (BpmDo.wfStatus(workflow) != WFEConstants.WFSTATUS_RUN
                && BpmDo.wfStatus(workflow) != WFEConstants.WFSTATUS_SUSPEND) {
            throw new CrudBaseException("流程实例状态不为运行、挂起,不能进入初始状态");
        }

        workflow.put("wfStatus", WFEConstants.WFSTATUS_INIT);

        crudService.update(workflow);
        retFlag = true;
        return retFlag;
    }
}
