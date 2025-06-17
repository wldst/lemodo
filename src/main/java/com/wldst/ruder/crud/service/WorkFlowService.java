package com.wldst.ruder.crud.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.LoggerTool;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.workflow.biz.BpmInstanceManagerService;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.util.WFEConstants;

@Service
public class WorkFlowService extends BpmDo {
	final static Logger logger = LoggerFactory.getLogger(RelationService.class);
	@Autowired
	private CrudNeo4jService crudNeo4jSevice;
	@Autowired
	private RelationService relationService;
	@Autowired
	private UserAdminService adminService;
	@Autowired
	private BpmInstanceManagerService bizWfInstanceManager;

	/**
	 * 创建流程
	 *
	 * @param labelId
	 * @param bizDataId
	 * @return
	 */
	@ServiceLog(description = "流程创建判断")
	public boolean createFlow(String label, Long labelId, Long bizDataId) {
//	String cypher2 = "match (n:MetaData)-[r]->(e:BpmGraph) where id(n)="+labelId+" return props(e)";
//	List<Map<String, Object>> count = neo4jService.queryByCypher(cypher2);
		String cypher2 = "match (n:MetaData)-[r:HAS_FLOW]->(e:BpmGraph) where id(n)=" + labelId + " return e";
		List<Map<String, Object>> flows = crudNeo4jSevice.cypher(cypher2);
		//判断是否启动流程？添加关系属性，添加属性。

		if (flows != null && flows.size() > 0) {
			LoggerTool.info(logger,"有流程需要创建，进入创建流程逻辑");
			//启动流程：
			boolean runing = false;
			Map<String, Object> flow = flows.get(0);

			if (flow != null && !flow.isEmpty()) {
				String cypher = "match (n:" + label + ")-[r:HAS_FLOW]->(e:BpmGraphInstance) where id(n)=" + bizDataId + " return e";
				List<Map<String, Object>> instances = crudNeo4jSevice.cypher(cypher);

				if (instances == null || instances.isEmpty()) {
					//新建流程：
					LoggerTool.info(logger,"创建流程" + labelId + ":" + bizDataId);
					flow.remove(ID);
					flow.put(LABEL, "BpmGraphInstance");
					flow.put(BpmDo.NOW_TASK_IDS, "");
					flow.put("wfStatus", WFEConstants.WFSTATUS_INIT);
					flow.put(BpmDo.bizDataId, bizDataId);
					flow.put("createEmpID", adminService.getCurrentUserId());
					Node save = crudNeo4jSevice.addNew(flow, "BpmGraphInstance");
					long instanceId = save.getId();
					relationService.addRel("HAS_FLOW", "有流程实例", bizDataId, instanceId);
					//解析流程，并创建实例
					LoggerTool.info(logger,jsonString(flow));

					List<Map<String, Object>> cellList = listMapObject(flow, "cellList");
					//更新实例数据中的Cell中的数据的ID数据
					for (Map<String, Object> ci : cellList) {
						ci.put(BpmDo.INSTANCEID, instanceId);
					}
					flow.put("cellList", cellList);
					//添加流程节点实例数据
					List<Long> cellIds = crudNeo4jSevice.save(cellList, "BpmNode");
					crudNeo4jSevice.update(flow);


					List<Map<String, Object>> conditionList = listMapObject(flow, "conditionList");
//		   List<Long> conditionIds = neo4jService.save(conditionList, "BpmCondition");
					Map<String, Long> vIdNodeId = new HashMap<>();

					for (Map<String, Object> nodei : cellList) {
						vIdNodeId.put(string(nodei, "vid"), id(nodei));
					}

					for (Map<String, Object> ci : conditionList) {
						Map<String, Object> copyWithKeys = copyWithKeys(flow, "logicalExp,conditionPSM");
						copyWithKeys.put(NAME, "下一步");
						relationService.addRel("nextStep", vIdNodeId.get(string(ci, "fromId")),
								vIdNodeId.get(string(ci, "toId")), copyWithKeys);

					}

					relationService.addRel("task", "流程节点", instanceId, cellIds);
					for (Map<String, Object> ti : cellList) {
						if (ti.get("nodeType").equals("Start")) {
							flow.put("startTask", ti);
							break;
						}
					}

					LoggerTool.info(logger,jsonString(flow));


					if (runing) {
						bizWfInstanceManager.runFlow(flow, adminService.getCurrentUserId());
					}

					return true;
				} else {
					for (Map<String, Object> flowi : instances) {
						if (WFEConstants.WFSTATUS_INIT == integer(flowi, "wfStatus") && "".equals(string(flowi, WFEConstants.NOW_TASK_IDS))) {
							bizWfInstanceManager.runFlow(flow, adminService.getCurrentUserId());
						}
					}
				}
			}
		}

		return false;
	}

}
