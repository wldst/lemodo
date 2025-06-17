package com.wldst.ruder.module.workflow.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.workflow.biz.BpmInstanceManagerService;
import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.formula.ConditionFormulaParse;
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.module.workflow.util.TextUtil;
import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.MapTool;

import bsh.EvalError;

/**
 * 流程实例对象操作类
 *
 * @author wldstO
 */
@Component
public class BpmInstance extends BpmDo {
    // 日志对象
    private static final Logger logger = LoggerFactory.getLogger(BpmInstance.class);

    private CrudNeo4jService neo4jService;

    private BpmInstanceManagerService bizWfInstanceManager;
    // 流程实例对象ID

    // 关联业务数据ID
    public static String bizDataID = "bizDataId";

    // 关联业务表名
    public static String bizTableName = "bizTableName";

    // 流程实例名称
    public static String workflowName = "workflowName";

    // 流程实例描述
    public static String workflowDescript = "workflowDescript";

    // 流程实例所关联的模板ID
    public static String wfTemplateID = "wfTemplateId";

    // 流程实例状态
    public static String wfStatus = "wfStatus";

    // 是否是被其他流程实例所触发
    public static String triggerSubWfFlag = "triggerSubWfFlag";

    // 触发本子流程实例的流程实例ID
    public static String triggerWfInsID = "triggerWfInsId";

    // 触发本子流程实例的流程任务ID
    public static String triggerTaskID = "triggerTaskId";

    // 流程实例创建人ID
    public static String wfCreateEmpID = "wfCreateEmpId";

    // 流程实例创建时间
    public static String wfCreateDatetime = "wfCreateDatetime";

    // 当前任务ID串

    // 对应流程模板唯一标识
    public static String templateMark = "templateMark";
    @Autowired
    public BpmInstance(@Lazy CrudNeo4jService neo4jService, @Lazy BpmInstanceManagerService bizWfInstanceManager){
        this.neo4jService=neo4jService;
        this.bizWfInstanceManager=bizWfInstanceManager;
    }


    public void commit(String flowId, String executeComment, Long currentUserId,String executorIDs) {
        // 获取流程实例信息
        long wfInstanceID = NumberUtil.parseLong(flowId, 0);
        commit(wfInstanceID, executeComment, currentUserId,executorIDs);
    }

    /**
     *
     * @param wfInstanceID
     * @param executeComment
     * @param currentUserId
     * @param executorIDs
     */
    public void commit(long wfInstanceID, String executeComment, Long currentUserId,String executorIDs) {
        Map<String, Object> workflow = getFlowi(wfInstanceID);
        // 初始化流程状态
        workflow.put("wfStatus", WFEConstants.WFSTATUS_INIT);
        // 更新流程实例状态
        neo4jService.update(workflow, id(workflow));
//        crudService.updateByKey(workflow,"wfStatus");
        // 设置执行评论
        workflow.put("executorIDs",currentUserId);
        workflow.put("executeComment", executeComment);
        // 启动流程实例
        bizWfInstanceManager.runFlow(workflow, currentUserId);
    }

    /**
     * 获取指定任务所对应的履历信息列表
     *
     * @param wfTaskID 指定任务ID
     * @return 履历信息列表
     */
    public static List<Map<String, Object>> getWfHistoryList(long wfTaskID, Map<String, Object> data) {
        List<Map<String, Object>> retList = null;
        List<Map<String, Object>> wfHistoryList = wfHistoryList(data);
        if (wfHistoryList != null) {
            retList = new ArrayList<>();
            for (Map<String, Object> history : wfHistoryList) {
                if (MapTool.longValue(history, "wfTaskID") == wfTaskID) {
                    retList.add(history);
                }
            }
        }
        return retList;
    }


    public Map<String, Object> getFlowi(Long wfInstanceID) {
        return neo4jService.getOneMapById(wfInstanceID);
    }
    
    public Map<String, Object> getFlowiByBizId(Long bizId) {
	
	    Map<String, Object> query = neo4jService.getOne("Match(m:BpmGraphInstance) where m.bizDataId=" + Long.valueOf(bizId) + " return id(m) AS id");
	    Long flowid = id(query);
	    Map<String, Object> flowi = getFlowi(flowid);
	    return flowi;
    }

    public List<Map<String, Object>> getFlowExcute(Long wfInstanceID, Long taskId) {
        String query = "MATCH  (e:BpmTaskExecute),(n:BpmNode) where e.instanceID=" + wfInstanceID + " and e.taskID=id(n) and e.taskID <>"+taskId+" return distinct e.taskID AS id,n.title order by e.taskID desc";
        return neo4jService.cypher(query);
    }

    public List<Map<String, Object>> getTaskExcute(Long wfInstanceID, Long taskId) {
        String query = "MATCH  (e:BpmTaskExecute) where e.instanceID=" + wfInstanceID + " and e.taskID=" + taskId + " return e";
        return neo4jService.cypher(query);
    }

    /**
     * 返回流称履历信息列表
     *
     * @return 流称履历信息列表
     */
    public static List<Map<String, Object>> getWfHistoryList(Map<String, Object> data) {
        List<Map<String, Object>> retList = null;
        List<Map<String, Object>> wfHistoryList = wfHistoryList(data);
        if (wfHistoryList != null) {
            retList = new ArrayList<>();
            int listSize = wfHistoryList.size();
            for (int i = 0; i < listSize; i++) {
                retList.add(wfHistoryList.get(i));
            }
            Collections.sort(retList, new HistoryMapComparator());
        }
        return retList;
    }

    /**
     * 根据任务内部ID获取流程实例中的任务实例信息
     *
     * @param innerTaskID 任务的内部ID
     * @param data        包含流程数据的相关信息
     * @return 返回与任务内部ID对应的任务实例信息，如果找不到则返回null
     */
    public static Map<String, Object> getWfTaskByInnerID(String innerTaskID, Map<String, Object> data) {
        Map<String, Object> retTask = null;
        // 根据提供的数据查找内部ID的映射
        Map<String, Object> innerIDMapings = innerIDMapings(data);
        if (innerIDMapings != null) {
            // 尝试从映射中获取任务ID，并转换为长整型
            String taskID = (String) innerIDMapings.get(innerTaskID);
            long taskIDLong = NumberUtil.parseLong(taskID, 0);
            // 任务ID大于0时进行查找
            if (taskIDLong > 0) {
                // 遍历简单任务、分支任务、收缩任务映射，尝试获取对应任务实例
                Map<String, Map<String, Object>> simpleTaskMap = simpleTaskMap(data);
                if (simpleTaskMap != null) {
                    retTask = simpleTaskMap.get(taskID);
                }
                if (retTask == null) {
                    Map<String, Map<String, Object>> branchTaskMap = branchTaskMap(data);
                    if (branchTaskMap != null) {
                        retTask = branchTaskMap.get(taskID);
                    }
                }
                if (retTask == null) {
                    Map<String, Map<String, Object>> shrinkTaskMap = shrinkTaskMap(data);
                    if (shrinkTaskMap != null) {
                        retTask = shrinkTaskMap.get(taskID);
                    }
                }
                // 如果以上都未找到，则尝试匹配开始任务和结束任务
                if (retTask == null) {
                    Map<String, Object> startTask = startTask(data);
                    if (id(startTask) == taskIDLong) {
                        retTask = startTask;
                    }
                }
                if (retTask == null) {
                    Map<String, Object> endTask = endTask(data);
                    if (id(endTask) == taskIDLong) {
                        retTask = endTask;
                    }
                }
            }
        }
        return retTask;
    }


    /**
     * 根据流程任务实例ID得到流程任务名
     *
     * @param wfTaskID 流程任务实例ID
     * @return 流程任务名
     */
    public String getWfTaskName(long wfTaskID) {
        Map<String, Object> abTask = getWfTaskByID(wfTaskID);
        if (abTask != null) {
            return taskName(abTask);
        } else {
            return null;
        }
    }

    /**
     * 根据任务实例ID得到任务实例对象
     *
     * @param wfTaskID 任务实例ID
     * @return 任务实例对象
     */
    public Map<String, Object> getWfTaskByID(long wfTaskID) {
        Map<String, Object> retTask = null;
        if (wfTaskID > 0) {
            retTask = neo4jService.getOneMapById(wfTaskID);
        }

        return retTask;
    }


    public static Map<String, Object> startTask(Map<String, Object> data) {
        return MapTool.mapObject(data, "startTask");
    }

    public static void startTask(Map<String, Object> data, Map<String, Object> task) {
        data.put("startTask", task);
    }

    public static void endTask(Map<String, Object> data, Map<String, Object> task) {
        data.put("endTask", task);
    }

    public static Map<String, Object> endTask(Map<String, Object> data) {
        return MapTool.mapObject(data, "endTask");
    }

    /**
     * 获取所有简单任务类型的扩展信息列表。
     *
     * @param data 包含任务查询条件的数据映射。
     * @return 返回一个包含所有简单任务扩展信息的列表。如果不存在简单任务类型，返回空列表。
     */
    public static List<Map<String, Object>> getAllWfTaskExtendsList(Map<String, Object> data) {
        // 初始化返回列表
        List<Map<String, Object>> retList = null;
        // 获取所有任务列表
        List<Map<String, Object>> tempList = getAllWfTaskList(data);
        // 遍历任务列表
        if (tempList != null && tempList.size() > 0) {
            int listSize = tempList.size();
            for (int i = 0; i < listSize; i++) {
                Map<String, Object> abTask = tempList.get(i);
                // 判断任务类型是否为简单任务
                if (abTask != null && taskType(abTask) == WFTASK_TYPE_SIMPLE) {
                    // 如果返回列表为空，则初始化
                    if (retList == null) {
                        retList = new ArrayList<>();
                    }
                    // 获取当前任务的扩展信息列表
                    List<Map<String, Object>> extendsList = extendsInfo(abTask);
                    // 将当前任务的扩展信息添加到返回列表中
                    if (extendsList != null && extendsList.size() > 0) {
                        int size = extendsList.size();
                        for (int j = 0; j < size; j++) {
                            retList.add(extendsList.get(j));
                        }
                    }
                }
            }
        }
        // 返回简单任务的扩展信息列表
        return retList;
    }


    /**
     * 获取所有任务关系信息的列表。
     * <p>此方法通过传入包含任务数据的Map，检索并返回所有任务及其关系信息的列表。首先，它会调用{@code getAllWfTaskList}方法获取所有任务的列表，然后对每个任务，调用{@code relationTaskList}方法来获取该任务的关系信息，并将其添加到返回列表中。</p>
     *
     * @param data 包含任务数据的Map对象。此参数用于初始化任务列表的查询。
     * @return 返回一个包含所有任务关系信息的List。如果没有任何任务或任务没有关系信息，则返回空列表。
     */
    public static List<Map<String, Object>> getAllWfTaskRelationList(Map<String, Object> data) {
        List<Map<String, Object>> retList = null; // 初始化返回列表为空
        List<Map<String, Object>> tempList = getAllWfTaskList(data); // 获取所有任务列表
        if (tempList != null && tempList.size() > 0) { // 检查任务列表是否非空且不为空
            int listSize = tempList.size(); // 获取任务列表的大小
            for (int i = 0; i < listSize; i++) { // 遍历每个任务
                Map<String, Object> abTask = tempList.get(i);
                if (retList == null) { // 如果返回列表为空，则初始化它
                    retList = new ArrayList<>();
                }
                List<Map<String, Object>> relationList = relationTaskList(abTask); // 获取当前任务的关系信息列表

                if (relationList != null && relationList.size() > 0) { // 检查关系信息列表是否非空且不为空
                    int size = relationList.size(); // 获取关系信息列表的大小
                    for (int j = 0; j < size; j++) { // 遍历每个关系信息，并添加到返回列表中
                        retList.add(relationList.get(j));
                    }
                }
            }
        }
        return retList; // 返回任务关系信息列表
    }


    public static List<Map<String, Object>> relationTaskList(Map<String, Object> abTask) {
        return MapTool.listMapObject(abTask, "relationTaskList");
    }

    /**
     * 获取已完成的简单任务节点
     *
     * @return
     */
    /**
     * 获取已完成的普通节点信息列表。
     *
     * @param data 包含任务相关数据的Map对象。
     * @return 返回已完成的普通节点信息列表，列表中的每个节点信息包括执行者等详细信息。
     */
    public List<Map<String, Object>> getCompletedNormalNode(Map<String, Object> data) {
        List<Map<String, Object>> reList = null;
        Map<String, Object> param = new HashMap<>();
        param.put(INSTANCEID, id(data)); // 根据传入的数据获取实例ID
        param.put(TASK_STATUS, WFTASK_STATUS_END); // 设置任务状态为结束

        // 查询条件设置，查询类型为简单任务的普通节点（注释掉的部分）
        List<Map<String, Object>> taskList = neo4jService.queryBy(param, BpmDo.BPM_NODE); // 根据条件查询任务节点信息列表
        if (null != taskList) {
            reList = new ArrayList<>();
            for (int i = 0; i < taskList.size(); i++) {
                Map<String, Object> task = taskList.get(i);
                Integer taskStatus = taskStatus(task); // 获取任务状态
                String nodeType = nodeType(task); // 获取节点类型

                if (nodeType != null && taskStatus != null) { // 确保节点类型和任务状态不为空
                    // 筛选出状态为完成的普通节点
                    if (NODE_TYPE_NORMAL.equals(nodeType)) {
                        // 获取节点操作人列表，并格式化为字符串列表
                        List<Map<String, Object>> listMapObject = MapTool.listMapObject(task, "nodeUserList");
                        List<String> userInfo = new ArrayList<>(listMapObject.size());
                        for (Map<String, Object> nodeUseri : listMapObject) {
                            userInfo.add(MapTool.string(nodeUseri, "organName") + "-" + MapTool.string(nodeUseri, "username"));
                        }
                        // 将操作人列表用逗号连接并存入任务信息中
                        task.put("executors", String.join(",", userInfo));
                    }
                    reList.add(task); // 将符合条件的节点信息添加到结果列表中
                }
            }
        }
        return reList; // 返回筛选后的节点信息列表
    }

    /**
     * 获取流程中所有普通任务的属性信息列表。
     *
     * @param data 包含流程相关数据的Map对象
     * @return 返回一个包含所有普通任务属性信息的List集合，每个任务的属性信息以Map形式表示。
     */
    public static List<Map<String, Object>> getAllWfTaskDecisionList(Map<String, Object> data) {
        List<Map<String, Object>> retList = null; // 将用于存储查询结果的列表初始化为null
        List<Map<String, Object>> tempList = getAllWfTaskList(data); // 获取所有任务的列表
        if (tempList != null && tempList.size() > 0) { // 检查列表是否非空且包含元素
            int listSize = tempList.size(); // 获取列表大小
            for (int i = 0; i < listSize; i++) { // 遍历所有任务
                Map<String, Object> abTask = tempList.get(i); // 获取当前任务
                Integer taskType = BpmDo.taskType(abTask); // 获取任务类型
                if (taskType != null && taskType == BpmDo.WFTASK_TYPE_SIMPLE) { // 检查任务是否为普通类型
                    if (retList == null) { // 如果结果列表为空，则初始化
                        retList = new ArrayList<>();
                    }
                    List<Map<String, Object>> decisionList = SimpleTask.getTaskDecisionList(abTask); // 获取当前任务的决策信息列表
                    if (decisionList != null && decisionList.size() > 0) { // 检查决策信息列表是否非空且包含元素
                        int size = decisionList.size(); // 获取决策信息列表大小
                        for (int j = 0; j < size; j++) { // 遍历决策信息列表，并将其添加到结果列表中
                            retList.add(decisionList.get(j));
                        }
                    }
                }
            }
        }
        return retList; // 返回结果列表
    }


    /**
     * 得到所有普通任务的属性对象列表
     *
     * @return 属性对象列表
     */
    public static List<Map<String, Object>> getAllWfTaskPropertyList(Map<String, Object> data) {
        List<Map<String, Object>> retList = null;
        List<Map<String, Object>> tempList = getAllWfTaskList(data);
        if (tempList != null && tempList.size() > 0) {
            int listSize = tempList.size();
            for (int i = 0; i < listSize; i++) {
                Map<String, Object> abTask = tempList.get(i);
                Integer taskType = BpmDo.taskType(abTask);
                if (taskType != null && taskType == BpmDo.WFTASK_TYPE_SIMPLE) {
                    if (retList == null) {
                        retList = new ArrayList<>();
                    }
                    retList.add(SimpleTask.getTaskProperty(abTask));
                }
            }
        }
        return retList;
    }

    /**
     * 获取流程所有任务信息
     *
     * @param data 包含流程相关数据的Map对象
     * @return 任务信息列表，列表中每个元素都是一个包含任务信息的Map对象
     */
    public static List<Map<String, Object>> getAllWfTaskList(Map<String, Object> data) {
        List<Map<String, Object>> retList = new ArrayList<>();

        // 添加开始任务和结束任务到列表
        retList.add(startTask(data));
        retList.add(endTask(data));

        // 如果simpleTaskMap不为空，则将它的值添加到列表中
        if (simpleTaskMap(data) != null) {
            retList.addAll(simpleTaskMap(data).values());
        }
        // 如果branchTaskMap不为空，则将它的值添加到列表中
        if (branchTaskMap(data) != null) {
            retList.addAll(branchTaskMap(data).values());
        }
        // 如果shrinkTaskMap不为空，则将它的值添加到列表中
        if (shrinkTaskMap(data) != null) {
            retList.addAll(shrinkTaskMap(data).values());
        }
        return retList;
    }


    public static Map<String, Map<String, Object>> shrinkTaskMap(Map<String, Object> data) {
        return MapTool.mapKeyMap(data, "shrinkTaskMap");
    }

    public static Map<String, Map<String, Object>> branchTaskMap(Map<String, Object> data) {
        return MapTool.mapKeyMap(data, "branchTaskMap");
    }

    public static Map<String, Map<String, Object>> simpleTaskMap(Map<String, Object> data) {
        return MapTool.mapKeyMap(data, "simpleTaskMap");
    }

    /**
     * 获取指定任务节点的上一个普通任务
     *
     * @param currentTaskID 指定的任务节点ID，类型为long。
     * @return 返回上一个普通任务的信息，以Map<String, Object>的形式返回。如果不存在上一个普通任务，则返回null。
     */
    public Map<String, Object> getPreviewWfTask(long currentTaskID) {
        Map<String, Object> retTask = null; // 准备返回的任务信息，默认为null
        Map<String, Object> tempTask = getWfTaskByID(currentTaskID); // 通过ID获取当前任务节点信息

        Map<String, Object> tempPreTask = null;
        // 判断当前任务节点信息是否获取成功
        if (tempTask != null) {
            Map<String, Object> preRelation = BpmDo.realPreRel(tempTask); // 获取当前任务的前置关系信息
            // 判断前置关系信息是否获取成功
            if (preRelation != null) {
                tempPreTask = getWfTaskByID(BpmDo.relTaskId(preRelation)); // 通过前置关系ID获取前置任务节点信息
            }

            // 判断前置任务是否为普通任务（简单任务）
            if (tempPreTask != null) {
                if (taskType(tempPreTask) == WFEConstants.WFTASK_TYPE_SIMPLE) {
                    retTask = tempPreTask; // 如果前置任务是普通任务，则直接返回该任务信息
                } else {
                    // 如果前置任务不是普通任务，则递归获取其上一个普通任务
                    retTask = getPreviewWfTask(id(tempPreTask));
                }
            }
        }
        return retTask; // 返回上一个普通任务的信息，或null
    }


    /**
     * 增加流程历史履历信息到流程实例中
     *
     * @param history 流程历史履历信息
     * @throws Exception
     */
    public static void addWfHistory(Map<String, Object> history, Map<String, Object> data) throws CrudBaseException {
        if (history == null) {
            throw new CrudBaseException("需要添加的历史信息为空,不能进行添加");
        }
        List<Map<String, Object>> wfHistoryList = wfHistoryList(data);
        if (wfHistoryList == null) {
            wfHistoryList = new ArrayList<>();
        }
        wfHistoryList.add(history);
    }

    /**
     * 将任务实例对象添加到流程实例中。
     * 该方法根据任务的类型，将任务添加到相应的任务集合中（如开始任务、结束任务、简单任务、分支任务、缩小任务），
     * 并更新流程实例内的任务映射关系。
     *
     * @param task       需要添加的任务实例，为包含任务信息的Map对象。
     * @param wfInstance 流程实例的数据，为包含流程相关信息的Map对象。
     * @throws CrudBaseException 如果提供的任务实例为空，则抛出异常。
     */
    public static void addWorkflowTask(Map<String, Object> task, Map<String, Object> wfInstance) throws CrudBaseException {
        // 检查任务实例是否为空
        if (task == null) {
            throw new CrudBaseException("需要添加的任务为空,不能添加到流程实例中");
        }

        // 获取任务类型
        int taskType = taskType(task);
        String taskId = MapTool.string(task, "id");
        switch (taskType) {
            // 处理开始任务
            case WFEConstants.WFTASK_TYPE_START:
                startTask(wfInstance, task);
                break;
            // 处理结束任务
            case WFEConstants.WFTASK_TYPE_END:
                endTask(wfInstance, task);
                break;
            // 处理简单任务
            case WFEConstants.WFTASK_TYPE_SIMPLE:
                // 将简单任务添加到简单任务集合中
                Map<String, Map<String, Object>> simpleTaskMap = simpleTaskMap(wfInstance);
                if (simpleTaskMap == null) {
                    simpleTaskMap = new HashMap<>();
                }
                simpleTaskMap.put(taskId, task);
                break;
            // 处理分支任务
            case WFEConstants.WFTASK_TYPE_BRANCH:
                // 将分支任务添加到分支任务集合中
                Map<String, Map<String, Object>> branchTaskMap = branchTaskMap(wfInstance);
                if (branchTaskMap == null) {
                    branchTaskMap = new HashMap<>();
                }
                branchTaskMap.put(taskId, task);
                break;
            // 处理缩小任务
            case WFEConstants.WFTASK_TYPE_SHRINK:
                // 将缩小任务添加到缩小任务集合中
                Map<String, Map<String, Object>> shrinkTaskMap = shrinkTaskMap(wfInstance);
                if (shrinkTaskMap == null) {
                    shrinkTaskMap = new HashMap<>();
                }
                shrinkTaskMap.put(taskId, task);
                break;
        }
        // 更新内部任务ID映射
        Map<String, Object> innerIDMapings = innerIDMapings(wfInstance);
        if (innerIDMapings == null) {
            innerIDMapings = new HashMap<>();
        }
        innerIDMapings.put(MapTool.string(task, "innerTaskId"), taskId);
    }


    public static Map<String, Object> innerIDMapings(Map<String, Object> data) {
        return MapTool.mapObject(data, "innerIDMapings");
    }

    /**
     * 判断流程当前任务是否已经被指定人员所接收
     *
     * @param userID   指定人员ID，即要判断是否接收任务的人员的ID
     * @param workFlow 当前流程的信息，包含流程的各种状态和节点等信息
     * @return 返回true：表示已经接收任务；返回false：表示未接收任务
     * @throws CrudBaseException 抛出该异常表示在查询流程信息时发生错误
     */
    public boolean isAccept(long userID, Map<String, Object> workFlow) throws CrudBaseException {
        boolean retFlag = false; // 默认未接收任务
        Map<String, Object> currentTask = getNowNode(workFlow); // 获取当前流程节点信息
        if (currentTask != null) { // 当前流程节点不为空则继续判断
            // 获取当前任务的执行者列表
            List<Map<String, Object>> taskExcuteList = getTaskExcute(MapTool.id(workFlow), MapTool.id(currentTask));
            Map<String, Object> taskExecutor = null;
            for (Map<String, Object> ei : taskExcuteList) { // 遍历执行者列表
                Long longValue = MapTool.longValue(ei, "executorID");
                // 判断执行者ID是否与指定人员ID匹配
                if (userID == longValue || neo4jService.getPasswordIdBy(longValue)!=null&&userID == neo4jService.getPasswordIdBy(longValue)) {
                    taskExecutor = ei;
                    // 判断任务执行者状态和任务状态是否表示任务已被接收但未完成
                    if (MapTool.integer(taskExecutor, "executorStatus") == WFEConstants.WF_EXEC_USERSTATE_NONE
                            && taskStatus(currentTask) == WFEConstants.WFTASK_STATUS_READY) {
                        retFlag = true; // 标记为已接收任务
                    }
                    break; // 找到匹配的执行者后跳出循环
                }
            }
        }
        return retFlag;
    }

    /**
     * 获取当前流程正需要执行的普通任务实例
     *
     * @param flowi 包含流程信息的Map对象
     * @return 返回当前需要执行的普通任务实例的Map，如果没有则返回null
     * @throws CrudBaseException 如果操作失败，抛出基础CRUD异常
     */
    public Map<String, Object> getNowNode(Map<String, Object> flowi) throws CrudBaseException {
        Map<String, Object> retTask = null;

        // 获取当前任务ID字符串
        String nowTaskIDs = nowTaskIDs(flowi);
        // 如果当前任务ID为空或仅包含空白字符，则直接返回null
        if (nowTaskIDs == null || nowTaskIDs.trim().length() <= 0) {
            return null;
        }

        // 将任务ID字符串转换为长整型数组
        long[] nowTaskIDArray = TextUtil.splitLongArray(nowTaskIDs, ",");
        if (nowTaskIDArray != null && nowTaskIDArray.length > 0) {
            // 通过第一个任务ID获取任务实例
            Map<String, Object> abTask = getWfTaskByID(nowTaskIDArray[0]);
            if (abTask != null) {
                retTask = abTask;
            }
        }
        return retTask;
    }


    public static String nowTaskIDs(Map<String, Object> data) {
        return MapTool.string(data, NOW_TASK_IDS);
    }

    /**
     * 获取流程实例中下一个可执行的普通任务。
     * <p>此方法首先判断流程实例的状态是否为运行中，若不是，则抛出异常。
     * 如果当前流程实例中没有可执行的任务，会尝试寻找并启动首个任务。
     * 如果找到可执行的普通任务，则返回该任务的详细信息。</p>
     *
     * @param flowi 包含流程实例信息的Map对象
     * @return 如果找到下一个可执行的普通任务，则返回其信息的Map对象；否则返回null
     * @throws EvalError         如果计算条件表达式时发生错误
     * @throws CrudBaseException 如果流程实例状态不为运行状态
     */
    public Map<String, Object> findNextNormalNode(Map<String, Object> flowi) throws EvalError {
        // 检查流程实例状态是否为运行中
        if (wfStatus(flowi) != WFEConstants.WFSTATUS_RUN) {
            throw new CrudBaseException("流程实例不为运行状态,不能获取下一个普通任务实例");
        }

        Map<String, Object> retTask = null;
        ConditionFormulaParse parse = new ConditionFormulaParse(flowi, neo4jService);

        // 判断当前是否有任务可执行
        String nowTaskIDs = nowTaskIDs(flowi);
        if (nowTaskIDs == null || nowTaskIDs.trim().length() <= 0) {
            // 尝试启动首个任务
            Map<String, Object> sTask = startTask(flowi);
            if (sTask == null) {
                // 获取流程开始节点信息
                Map<String, Object> startNode = getStartNode(flowi);
                Long startId = id(startNode);
                if (startNode != null && startId != null) {
                    sTask = startNode;
                    flowi.put("currentTask", startNode);
                    flowi.put("startTask", startNode);
                    flowi.put(NOW_TASK_IDS, startId);
                }
            } else {
                flowi.put("currentTask", sTask);
                flowi.put(NOW_TASK_IDS, id(sTask));
            }
            // 判断并获取下一个普通任务
            retTask = judgeNextNormalNode(parse, sTask, flowi);
        } else {
            // 当前有任务可执行，获取并判断下一个任务
            long[] nowTaskArray = TextUtil.splitLongArray(nowTaskIDs, ",");
            if (nowTaskArray != null && nowTaskArray.length > 0) {
                long nowTaskId = nowTaskArray[0];

                // 获取当前任务的后续任务列表
                List<Map<String, Object>> nexts = neo4jService.getOneRelationList(nowTaskId, "nextStep");

                if (nexts != null && nexts.size() > 0) {
                    Map<String, Object> map = null;
                    boolean conditionOk = false;
                    for (Map<String, Object> ni : nexts) {
                        // 遍历后续任务，寻找满足条件的任务
                        Map<String, Object> condition = MapTool.mapObject(ni, RELATION_PROP);
                        String beanShell = MapTool.string(condition, "logicalExp");
                        if (beanShell != null && parse.parseConidtionFormula(beanShell)) {
                            conditionOk = true;
                            map = ni;
                            break;
                        }
                    }
                    // 如果没有满足条件的任务，则选择第一个任务
                    if (!conditionOk) {
                        map = nexts.get(0);
                    }

                    // 判断并获取下一个普通任务
                    retTask = judgeNextNormalNode(parse, MapTool.mapObject(map, RELATION_ENDNODE_PROP), flowi);
                }
            }
        }
        return retTask;
    }


    private Map<String, Object> getStartNode(Map<String, Object> flowi) {
        Map<String, Object> startNode = neo4jService.getOne("Match (m:BpmNode{nodeType:\"Start\"}) where m.instanceID=" + id(flowi) + " return m");
        return startNode;
    }

    /**
     * 判断给定的任务是否为当前可执行的下一个任务<br>
     * 如果不是则递归向下查找,直到找到可执行的普通任务,或者找到流程结束
     *
     * @param parse    条件解析公式实例对象，用于解析任务执行的条件
     * @param taskData 需要判断的任务数据，包含任务的各种信息
     * @param flowi    当前流程实例的信息，包含流程的当前状态等
     * @return 返回下一个可执行的任务数据，如果没有可执行的任务则返回null
     * @throws EvalError         当解析条件时发生错误时抛出
     * @throws CrudBaseException 当进行数据库操作时发生错误时抛出
     */
    public Map<String, Object> judgeNextNormalNode(ConditionFormulaParse parse, Map<String, Object> taskData,
                                                   Map<String, Object> flowi) throws EvalError {
        Map<String, Object> retTask = null;
        String taskType = nodeType(taskData); // 获取当前任务的类型

        switch (taskType) {
            case WFEConstants.NODE_TYPE_NORMAL:
                retTask = taskData; // 如果是普通任务，则直接返回该任务
                break;
            case WFEConstants.NODE_TYPE_START:
                // 如果是开始任务，则计算并返回下一个任务
                retTask = computeNext(parse, taskData, flowi);
//                // 查找当前任务的结束节点，尝试找到下一个简单任务
//                List<Long> endIdsOf = crudService.getEndIdsOf("nextStep", MapTool.id(taskData));
//                if (endIdsOf != null && endIdsOf.size() > 0) {
//                    // 找到满足条件的分支
//                    Long relation = endIdsOf.get(0);
//                    retTask = judgeNextSimpleTask(parse,crudService.getPropMapByNodeId(relation), flowi);
//                }
                break;
            case WFEConstants.NODE_TYPE_GATEWAY:
                // 如果是网关任务，则计算并返回下一个任务
                retTask = computeNext(parse, taskData, flowi);
                break;
            case WFEConstants.NODE_TYPE_END:
                // 如果是结束任务，则不返回任何任务
                retTask = null;
                break;
        }
        return retTask;
    }


    /**
     * 计算下一个任务节点。
     * 根据当前任务数据和流程定义，解析条件表达式，确定流程的下一个节点。
     *
     * @param parse    条件公式解析对象，用于解析条件表达式和公式。
     * @param taskData 当前任务的数据。
     * @param flowi    流程实例信息，包含流程实例的相关数据。
     * @return 返回下一个任务节点的数据，如果没有符合条件的下一个节点，则返回null。
     * @throws EvalError 如果条件解析出错，则抛出异常。
     */
    private Map<String, Object> computeNext(ConditionFormulaParse parse, Map<String, Object> taskData,
                                            Map<String, Object> flowi) throws EvalError {
        Map<String, Object> retTask = null;
        // 处理网关类型的节点，获取后续所有可能的节点
        List<Map<String, Object>> nexts = neo4jService.getOneRelationList(MapTool.id(taskData), "nextStep");
        if (nexts != null && nexts.size() > 0) {
            Map<String, Object> map = null;
            // 遍历所有后续节点，查找满足条件的下一个节点
            boolean conditionOk = false;
            for (Map<String, Object> ni : nexts) {
                Map<String, Object> condition = MapTool.mapObject(ni, RELATION_PROP);
                // 解析逻辑表达式条件
                String logcialExp = MapTool.string(condition, "logicalExp");
                if (logcialExp != null && parse.parseConidtion(logcialExp)) {
                    conditionOk = true;
                    map = ni;
                    break;
                }
                // 解析PSM公式条件
                String psmShell = MapTool.string(condition, "conditionPSM");
                if (psmShell != null && parse.parseConidtionFormula(psmShell)) {
                    conditionOk = true;
                    map = ni;
                    break;
                }
            }
            // 如果没有满足条件的节点，则默认返回第一个节点
            if (!conditionOk) {
                map = nexts.get(0);
            }

            // 判断并返回下一个普通节点
            retTask = judgeNextNormalNode(parse, MapTool.mapObject(map, RELATION_ENDNODE_PROP), flowi);
        }
        return retTask;
    }

    /**
     * 获取制定任务的循环开始任务
     *
     * @param currentInnerTaskID 指定任务内部ID
     * @return 循环开始任务信息
     * @throws CrudBaseException
     */
    public static Map<String, Object> getReloopTask(String currentInnerTaskID, Map<String, Object> data)
            throws CrudBaseException {
        Map<String, Object> retTask = null;
        if (currentInnerTaskID == null || currentInnerTaskID.trim().length() <= 0) {
            throw new CrudBaseException("获取指定任务对应的循环开始任务信息失败");
        }

        List<Map<String, Object>> tempTaskList = getAllWfTaskList(data);
        try {
            if (tempTaskList != null && tempTaskList.size() > 0) {
                int listSize = tempTaskList.size();
                for (int i = 0; i < listSize; i++) {
                    Map<String, Object> abTask = tempTaskList.get(i);

                    if (taskType(abTask) == WFEConstants.WFTASK_TYPE_SIMPLE) {
                        Map<String, Object> prop = MapTool.mapObject(abTask, "taskProperty");

                        if (MapTool.integer(prop, "loopStartTask") == WFEConstants.DB_BOOLEAN_TRUE) {
                            String endTasks = MapTool.string(prop, "reloopEndTaskInnerID");
                            if (endTasks != null && endTasks.trim().length() > 0) {
                                if (endTasks.indexOf(currentInnerTaskID) >= 0) {
                                    retTask = abTask;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (CrudBaseException ex) {
            LoggerTool.error(logger,"获取循环开始任务失败:", ex);
            throw new CrudBaseException("获取循环开始任务失败:", ex);
        }
        return retTask;
    }

    /**
     * 设置流程实例对象ID
     *
     * @param id 流程实例对象ID.
     */
    public static void setID(long id, Map<String, Object> data) {
        data.put(ID, id);
    }

    /**
     * 得到关联业务数据ID
     *
     * @return 关联业务数据ID.
     */
    public static long getBizDataID(Map<String, Object> data) {
        return MapTool.longValue(data, bizDataID);
    }

    /**
     * 设置关联业务数据ID
     *
     * @param bizDataId 关联业务数据ID.
     */
    public static void setBizDataID(long bizDataId, Map<String, Object> data) {
        data.put(bizDataID, bizDataId);
    }

    /**
     * 得到关联业务表名
     *
     * @return 关联业务表名.
     */
    public static String getBizTableName(Map<String, Object> data) {
        return MapTool.string(data, bizTableName);
    }

    /**
     * 设置关联业务表名
     *
     * @param bizName 关联业务表名.
     */
    public static void setBizTableName(String bizName, Map<String, Object> data) {
        data.put(bizTableName, bizName);
    }

    /**
     * 得到流程实例名称
     *
     * @return 流程实例名称.
     */
    public static String getWorkflowName(Map<String, Object> data) {
        return workflowName;
    }

    /**
     * 设置流程实例名称
     *
     * @param wfName 流程实例名称.
     */
    public static void setWorkflowName(String wfName, Map<String, Object> data) {
        data.put(workflowName, wfName);
    }

    /**
     * 得到流程实例描述
     *
     * @return 流程实例描述.
     */
    public static String getWorkflowDescript(Map<String, Object> data) {
        return workflowDescript;
    }

    /**
     * 设置流程实例描述
     *
     * @param workflowDescript 流程实例描述.
     */
    public static void setWorkflowDescript(String workflowDescript, Map<String, Object> data) {
        data.put("workflowDescript", workflowDescript);
    }

    /**
     * 得到流程实例所关联的模板ID
     *
     * @return 流程实例所关联的模板ID.
     */
    public static long getWfTemplateID(Map<String, Object> data) {
        return MapTool.longValue(data, wfTemplateID);
    }

    /**
     * 设置流程实例所关联的模板ID
     *
     * @param wfTemplateID 流程实例所关联的模板ID.
     */
    public static void setWfTemplateID(long wfTemplateID, Map<String, Object> data) {
        data.put("wfTemplateID", wfTemplateID);
    }

    /**
     * 得到流程实例状态
     *
     * @return 流程实例状态.
     */
    public static int getWfStatus(Map<String, Object> data) {
        return MapTool.integer(data, wfStatus);
    }

    /**
     * 设置流程实例状态
     *
     * @param wfStatus 流程实例状态.
     */
    public static void setWfStatus(int wfStatus, Map<String, Object> data) {
        data.put("wfStatus", wfStatus);
    }

    /**
     * 得到是否是被其他流程实例所触发
     *
     * @return 是否是被其他流程实例所触发.
     */
    public static int getTriggerSubWfFlag(Map<String, Object> data) {
        return MapTool.integer(data, triggerSubWfFlag);
    }

    /**
     * 设置是否是被其他流程实例所触发
     *
     * @param triggerSubWfFlag 是否是被其他流程实例所触发.
     */
    public static void setTriggerSubWfFlag(int triggerSubWfFlag, Map<String, Object> data) {
        data.put("triggerSubWfFlag", triggerSubWfFlag);
    }

    /**
     * 得到触发本子流程实例的流程实例ID
     *
     * @return 触发本子流程实例的流程实例ID.
     */
    public static long getTriggerWfInsID(Map<String, Object> data) {
        return MapTool.longValue(data, triggerWfInsID);
    }

    /**
     * 设置触发本子流程实例的流程实例ID
     *
     * @param triggerWfInsID 触发本子流程实例的流程实例ID.
     */
    public static void setTriggerWfInsID(long triggerWfInsID, Map<String, Object> data) {
        data.put("triggerWfInsID", triggerWfInsID);
    }

    /**
     * 得到触发本子流程实例的流程任务ID
     *
     * @return 触发本子流程实例的流程任务ID.
     */
    public static long getTriggerTaskID(Map<String, Object> data) {
        return MapTool.longValue(data, triggerTaskID);
    }

    /**
     * 设置触发本子流程实例的流程任务ID
     *
     * @param triggerTaskID 触发本子流程实例的流程任务ID.
     */
    public static void setTriggerTaskID(long triggerTaskID, Map<String, Object> data) {
        data.put("triggerTaskID", triggerTaskID);
    }

    /**
     * 得到流程实例创建人ID
     *
     * @return 流程实例创建人ID.
     */
    public static long getWfCreateEmpID(Map<String, Object> data) {
        return MapTool.longValue(data, wfCreateEmpID);
    }

    /**
     * 设置流程实例创建人ID
     *
     * @param wfCreateEmpID 流程实例创建人ID.
     */
    public static void setWfCreateEmpID(long wfCreateEmpID, Map<String, Object> data) {
        data.put("wfCreateEmpID", wfCreateEmpID);
    }

    /**
     * 得到流程实例创建时间
     *
     * @return 流程实例创建时间.
     */
    public static long getWfCreateDatetime(Map<String, Object> data) {
        return MapTool.longValue(data, wfCreateDatetime);
    }

    /**
     * 设置流程实例创建时间
     *
     * @param wfCreateDatetime 流程实例创建时间.
     */
    public static void setWfCreateDatetime(long wfCreateDatetime, Map<String, Object> data) {
        data.put("wfCreateDatetime", wfCreateDatetime);
    }

    /**
     * 得到当前任务ID串
     *
     * @return 当前任务ID串.
     */
    public static String getNowTaskIDs(Map<String, Object> data) {
        return MapTool.string(data, NOW_TASK_IDS);
    }

    /**
     * 设置当前任务ID串
     *
     * @param nowTaskIDs 当前任务ID串.
     */
    public static void setNowTaskIDs(String nowTaskIDs, Map<String, Object> data) {
        data.put(NOW_TASK_IDS, nowTaskIDs);
    }

    /**
     * 获取流程对应的模板信息唯一标识
     *
     * @return 模板信息唯一标识
     */
    public static String getTemplateMark(Map<String, Object> data) {
        return MapTool.string(data, templateMark);

    }

    /**
     * 设置流程实例对应的模板信息唯一标识
     *
     * @param templateMark 模板信息唯一标识
     */
    public static void setTemplateMark(String templateMark, Map<String, Object> data) {
        data.put("templateMark", templateMark);
    }
}

/**
 * 流程履历排序
 *
 * @author wldst
 */
class HistoryMapComparator implements Comparator {
    @Override
    public int compare(Object o1, Object o2) {
        Map<String, Object> e1 = (Map<String, Object>) o1;
        Map<String, Object> e2 = (Map<String, Object>) o2;
        Long orderIDone = MapTool.longValue(e1, "historyCreateDatetime");
        Long orderIDtwo = MapTool.longValue(e2, "historyCreateDatetime");
        return orderIDone.compareTo(orderIDtwo);
    }
}
