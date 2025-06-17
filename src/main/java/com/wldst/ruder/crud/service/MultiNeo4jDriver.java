package com.wldst.ruder.crud.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.util.LoggerTool;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.domain.CrudSystem;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.util.CommonUtil;
import com.wldst.ruder.util.JSONMapUtil;

/**
 * neo4j本地开发
 * 
 * @author deeplearn96
 *
 */
@Component
public class MultiNeo4jDriver extends CrudSystem{
	@Autowired
    private UserAdminService adminService;
//	private static GraphDatabaseService graphDb;
	private static Map<String,GraphDatabaseService> dbSpace = new HashMap<>();
	final static Logger logger = LoggerFactory.getLogger(MultiNeo4jDriver.class);

	private static void createCheckObjectUseRule(MultiNeo4jDriver d) {
		String query = "create(n:CheckObject{name:\"招投标\"}),(m:CheckRule{name:\"客户名称不能为空\",code:\"test2\"})  create (n)-[r:using_rule]->(m)";
		d.queryData(query);
		query = "create(n:CheckObject{name:\"招投标\"}),(m:CheckRule{name:\"地址不能为空\",code:\"test2\"})  create (n)-[r:using_rule]->(m)";
		query = "create(n:CheckObject{name:\"招投标\"}),(m:CheckRule{name:\"联系方式不能为空\",code:\"test2\"})  create (n)-[r:using_rule]->(m)";
		d.queryData(query);
	}

	private static void getRelation(MultiNeo4jDriver d) {
		// String query = "match (n:closeth{id:\"1\"})-[r]->(m) return r,m SKIP 0 Limit
		// 10 ";
		String query = "match (n:User)-[r]->(m) return r,m SKIP 0 Limit 20  ";
		// query="match (n:CheckRule) return n.name ,n.tableName SKIP 0 Limit 10 ";
		JSONArray relations = d.getRelation(query);

		System.out.println(relations.toJSONString());
	}

	public static void main(String[] args) {
		// createNode();
		// 查询数据库
		// query();
		// queryRelation();
		// registerShutdownHook();
		// useIndex();
		// show()
		MultiNeo4jDriver d = new MultiNeo4jDriver();
		// d.useIndex();
		// createCheckObjectUseRule(d);
		// queryRelation(d);
		getRelation(d);
		/*
		 * String createIndex = "create INDEX ON :Domain(label)";
		 */
	}

	private static void queryRelation(MultiNeo4jDriver d) {
		String query = "match(n)-[r:using_rule]->(m) return n,m";
		query = "match (n)-[using_rule]->(m) return n,m SKIP 0 Limit 10 ";
		// query="match (n:CheckRule) return n.name ,n.tableName SKIP 0 Limit 10 ";
		JSONArray relations = d.queryRelation(query);

		System.out.println(relations.toJSONString());
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

	@Value("${neo4j.db.path}")
	private String dbPath = "D:\\liuqiang\\data\\neo4j";
	
	@Value("${neo4j.log.path}")
	private String dbLogPath = "D:\\liuqiang\\data\\neo4jlog";
	
	

	private void childrenWithColumns(Node findNode, Map<String, Object> rootMap,String[] columns) {
		List<Map<String, Object>> childList = new ArrayList<>();

		Iterable<Relationship> relationships = findNode.getRelationships(Direction.OUTGOING,
				RelationshipType.withName(REL_TYPE_CHILDREN), RelationshipType.withName(REL_TYPE_CHILDRENS));
		relationships.forEach(p -> {
			Node endNode = p.getEndNode();
			Map<String, Object> properties = endNode.getProperties(columns);
			if(!properties.containsKey(NODE_ID)) {
				properties.put(NODE_ID, endNode.getId());
			}
			childrenWithColumns(endNode, properties,columns);
			childList.add(properties);
		});

		if (!childList.isEmpty()) {
			rootMap.put(REL_TYPE_CHILDREN, childList);
		}
	}
	
	private void children(Node findNode, Map<String, Object> rootMap) {
		List<Map<String, Object>> childList = new ArrayList<>();

		Iterable<Relationship> relationships = findNode.getRelationships(Direction.OUTGOING,
				RelationshipType.withName(REL_TYPE_CHILDREN), RelationshipType.withName(REL_TYPE_CHILDRENS));
		relationships.forEach(p -> {
			Node endNode = p.getEndNode();
			Map<String, Object> properties = endNode.getProperties(NODE_ID, "name");
			if(!properties.containsKey(NODE_ID)) {
				properties.put(NODE_ID, endNode.getId());
			}
			children(endNode, properties);
			childList.add(properties);
		});

		if (!childList.isEmpty()) {
			rootMap.put(REL_TYPE_CHILDREN, childList);
		}
	}

	private void childList(Node findNode, Map<String, Object> rootMap) {
		List<Map<String, Object>> childList = getChildListByNode(findNode);

		if (!childList.isEmpty()) {
			rootMap.put(REL_TYPE_CHILDREN, childList);
		}
	}

	private List<Map<String, Object>> getChildListByNode(Node findNode) {
		List<Map<String, Object>> childList = new ArrayList<>();

		Iterable<Relationship> relationships = findNode.getRelationships(Direction.OUTGOING,
				RelationshipType.withName(REL_TYPE_CHILDREN), RelationshipType.withName(REL_TYPE_CHILDRENS));
		relationships.forEach(p -> {
			Node endNode = p.getEndNode();
			Map<String, Object> properties = endNode.getProperties(NODE_ID, "name");
			childList.add(properties);
		});
		return childList;
	}
	
	private List<Map<String, Object>> getChildPropListByNode(Node findNode) {
		List<Map<String, Object>> childList = new ArrayList<>();

		Iterable<Relationship> relationships = findNode.getRelationships(Direction.OUTGOING,
				RelationshipType.withName(REL_TYPE_CHILDREN), RelationshipType.withName(REL_TYPE_CHILDRENS));
		relationships.forEach(p -> {
			Node endNode = p.getEndNode();
			Map<String, Object> properties = endNode.getAllProperties();
			properties.put("id", endNode.getId());
			childList.add(properties);
		});
		return childList;
	}

	public List<Long> getEndNodeId(String label, String rLabel, Map<String, Object> queryMap) {
		Object object = queryMap.get(NODE_ID);
		return getEndNodeIdList(label, rLabel, object);
	}

	public List<Long> getEndNodeIdList(String label, String rLabel, Object object) {
		Node findNode = findNode(NODE_ID, String.valueOf(object), label);
		if (findNode == null) {
			return null;
		}
		try (Transaction tx = getInstance().beginTx()) {
			List<Long> childList = getChildNodeIdByNode(findNode, rLabel);
			tx.success();
			return childList;
		}
	}

	public List<Long> getChildNodeIdByNode(Node findNode, String label) {
		List<Long> childIdList = new ArrayList<>();

		Iterable<Relationship> relationships = findNode.getRelationships(Direction.OUTGOING,
				RelationshipType.withName(label));
		relationships.forEach(p -> {
			Node endNode = p.getEndNode();
			childIdList.add(endNode.getId());
		});
		return childIdList;
	}

	public Node createNode(Map<String, Object> props, Label label, String pkey) {
		Node node = null;
		System.out.println(label.name() + "的Id：" + pkey + "属性:" + JSON.toJSONString(props));
		// 开启事务
		GraphDatabaseService instance = getInstance();
		try (Transaction tx = instance.beginTx()) {
			// Perform DB operations
			node = instance.createNode(label);
			Object object = props.get(NODE_ID);
			if (StringUtils.isBlank(String.valueOf(object))) {
				long id = node.getId();
				node.setProperty(NODE_ID, id);
				if (props.containsKey(NODE_ID)) {
					props.remove(NODE_ID);
				}
			}
			for (String key : props.keySet()) {
				node.setProperty(key, String.valueOf(props.get(key)));
			}
			tx.success();
		}
		return node;
	}

	/**
	 * 
	 * @param props
	 * @param label
	 * @return
	 */
	public Node createNode(Map<String, Object> props, Label label) {
		GraphDatabaseService instance = getInstance();
		if (props.isEmpty()) {
			return null;
		}
		Node node = null;
		Map<String, Object> validateMap = clearNonStringValue(clearEmptyValue(props));
		System.out.println("createNode:" + label + "!" + JSON.toJSONString(validateMap));
		// 开启事务
		try (Transaction tx = instance.beginTx()) {
			// Perform DB operations
			node = instance.createNode(label);
			if (props.containsKey(NODE_ID)) {
				props.remove(NODE_ID);
			}
			long id = node.getId();
			node.setProperty(NODE_ID, id);
			for (String key : validateMap.keySet()) {
				node.setProperty(key, String.valueOf(validateMap.get(key)));
			}
			tx.success();
		}
		return node;
	}

	public Node createRelation(Node node, Node nodeB, String rt) {
		GraphDatabaseService instance = getInstance();

		// 开启事务
		try (Transaction tx = instance.beginTx()) {
			
			Iterable<Relationship> relationships = node.getRelationships(RelationshipType.withName(rt), Direction.OUTGOING);
			Map<String,String> nameSet = new HashMap<>();
			Object relationName = null;
			Iterator<Relationship> iterator = relationships.iterator();
			while(iterator.hasNext()) {
				Relationship ri=iterator.next();
				Map<String, Object> allProperties = ri.getAllProperties();
				relationName = allProperties.get("name");
				Object object = allProperties.get(LABEL);
				if(relationName!=null&&object!=null) {
					nameSet.put(String.valueOf(object), String.valueOf(relationName));
				}
			}
			if(!nameSet.isEmpty()) {
				relationName=nameSet.get(rt);
			}
			
			if(nameSet.isEmpty()) {
				// Perform DB operations
				Object labelB = nodeB.getLabels().iterator().next();
				if(labelB!=null) {
					Node findNode = findNode(LABEL, String.valueOf(labelB), META_DATA);
					relationName = findNode.getProperty("name");
				}
			}
			Relationship createRelationshipTo = node.createRelationshipTo(nodeB, RelationshipType.withName(rt));
			if(rt.startsWith("child")) {
				relationName="子"+relationName;
			}
			createRelationshipTo.setProperty("name", relationName);
			// createRelationshipTo.setProperty(key, value);
			tx.success();
		}
		return node;
	}
	


	public Node createRelation(Node node, Node nodeB, String rt, Map<String, Object> propMap) {
		if(null!=nodeB) {
			GraphDatabaseService instance = getInstance();
			
			// 开启事务
			try (Transaction tx = instance.beginTx()) {
				// Perform DB operations
				Iterable<Relationship> relationships = node.getRelationships(RelationshipType.withName(rt), Direction.OUTGOING);
				Iterator<Relationship> iterator = relationships.iterator();
				while(iterator.hasNext()) {
					Relationship next = iterator.next();
					long endNodeId = next.getEndNodeId();
					if(nodeB.getId()==endNodeId) {
						return node;
					}
				}
				Relationship createRelationshipTo = node.createRelationshipTo(nodeB, RelationshipType.withName(rt));
				LoggerTool.info(logger,"create realtion:"+rt);
				for (Entry<String, Object> ei : propMap.entrySet()) {
					createRelationshipTo.setProperty(ei.getKey(), ei.getValue());
				}
				tx.success();
			}
		}
		return node;
		
	}

	public Node createRelations(Node node, List<Node> nodeBList, String rt) {
		GraphDatabaseService instance = getInstance();
		// 开启事务
		try (Transaction tx = instance.beginTx()) {
			// Perform DB operations
			for (Node ni : nodeBList) {
				node.createRelationshipTo(ni, RelationshipType.withName(rt));
			}
			tx.success();
		}
		return node;
	}
	/**
	 * 关联开始和结束节点，并设置关系属性
	 * @param node
	 * @param nodeBList
	 * @param rt
	 * @param relProp
	 * @return
	 */
	public Node createRelations(Node node, List<Node> nodeBList, String rt, Map<String, Object> relProp) {
		GraphDatabaseService instance = getInstance();
		// 开启事务
		try (Transaction tx = instance.beginTx()) {
			// Perform DB operations
			for (Node ni : nodeBList) {
				Relationship createRelationshipTo = node.createRelationshipTo(ni, RelationshipType.withName(rt));
				for (Entry<String, Object> ei : relProp.entrySet()) {
					createRelationshipTo.setProperty(ei.getKey(), ei.getValue());
				}
			}
			tx.success();
		}
		return node;
	}

	public JSONObject findJSONNode(String key, String value, String label) {
		Node findNode = findNode(key, value, label);
		return getNodeJSON(findNode);
	}

	public Node findNode(String key, String value, String label) {
		if(label==null||key==null||value==null) {
			return null;
		}
		GraphDatabaseService instance = getInstance();
				
		Node findNode = null;
		try (Transaction tx = instance.beginTx()) {

			ResourceIterator<Node> findNodes = instance.findNodes(Label.label(label), key, value);
			if (findNodes.hasNext()) {
				findNode = findNodes.next();
			}
			if (findNode == null) {
				if (NODE_ID.equalsIgnoreCase(key) && CommonUtil.isNumber(value)) {
					findNode = instance.getNodeById(Long.parseLong(value));
				}
			}
			// else {
			// HashMap<String, Object> propMap = new HashMap<>();
			// propMap.put(key, value);
			// findNode=createNode(propMap,Label.label(label));
			// }
			tx.success();
		}
		return findNode;
	}

	public List<Map<String, Object>> queryBy(String key, String value, String label) {
		if(label==null||key==null||value==null) {
			return null;
		}
		GraphDatabaseService instance = getInstance();	
		List<Map<String, Object>> nodeMapList = new ArrayList<>();
		
		try (Transaction tx = instance.beginTx()) {
			Node findNode = null;
			ResourceIterator<Node> findNodes = instance.findNodes(Label.label(label), key, value);
			while(findNodes.hasNext()) {
				findNode = findNodes.next();
				if (findNode == null) {
					if (NODE_ID.equalsIgnoreCase(key) && CommonUtil.isNumber(value)) {
						findNode = instance.getNodeById(Long.parseLong(value));
					}
				}
				if(findNode!=null) {
					nodeMapList.add(findNode.getAllProperties());
				}
			}
			tx.success();
		}
		return nodeMapList;
	}


	public Map<String, Object> findNodeAttMap(String key, String value, String label) {
		Node findNode = findNode(key, value, label);
		if (findNode == null) {
			return null;
		}
		return getNodeProperties(findNode);
	}

	public Node findNodeByName(String name, String label) {
		GraphDatabaseService instance = getInstance();
		Node findNode = null;
		try (Transaction tx = instance.beginTx()) {
			ResourceIterator<Node> findNodes = instance.findNodes(Label.label(label), "name", name);
			if (findNodes.hasNext()) {
				findNode = findNodes.next();
			}
			tx.success();
		}
		return findNode;
	}

	/**
	 * c查找第一级树节点
	 * 
	 * @param label
	 * @return
	 */
	public Map<String, Object> getTreeRootMap(String label) {
		Node findNode = findNode("isRoot", "true", label);
		if (findNode == null) {
			return null;
		}
		Map<String, Object> rootMap = null;
		try (Transaction tx = getInstance().beginTx()) {
			rootMap = findNode.getProperties(NODE_ID, "name");
			rootMap.put("open", true);
			childList(findNode, rootMap);
			tx.success();
		}
		return rootMap;
	}

	public Map<String, Object> getALevelChildren(String label, Map<String, Object> queryMap) {
		Object object = queryMap.get(NODE_ID);
		Node findNode = findNode(NODE_ID, String.valueOf(object), label);
		if (findNode == null) {
			return null;
		}
		Map<String, Object> rootMap = null;
		try (Transaction tx = getInstance().beginTx()) {
			rootMap = findNode.getProperties(NODE_ID, "name");
			rootMap.put("open", true);
			childList(findNode, rootMap);
			tx.success();
		}
		return rootMap;
	}

	public List<Map<String, Object>> getChildrenList(String label, Map<String, Object> queryMap) {
		Object object = queryMap.get(NODE_ID);
		Node findNode = findNode(NODE_ID, String.valueOf(object), label);
		if (findNode == null) {
			return null;
		}
		try (Transaction tx = getInstance().beginTx()) {
			List<Map<String, Object>> childList = getChildListByNode(findNode);
			tx.success();
			return childList;
		}
	}
	
	public List<Map<String, Object>> childrenList(String label, Map<String, Object> queryMap) {
		Object object = queryMap.get(NODE_ID);
		Node findNode = findNode(NODE_ID, String.valueOf(object), label);
		if (findNode == null) {
			return null;
		}
		try (Transaction tx = getInstance().beginTx()) {
			List<Map<String, Object>> childList = getChildPropListByNode(findNode);
			tx.success();
			return childList;
		}
	}

	/**
	 * 
	 * @return
	 */
	public GraphDatabaseService getInstance() {
		Long currentUserName = adminService.getCurrentPasswordId();
		
		GraphDatabaseService graphDb=null;
		if (currentUserName==null) {
			if(dbSpace.get("default")!=null) {
				return dbSpace.get("default");
			}
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(new File(dbPath));
			dbSpace.put("default", graphDb);
		}
		else {
			return getInstance(String.valueOf(currentUserName));
		}
		return graphDb;
	}
	
	public GraphDatabaseService getInstance(String dbId) {
		if(dbSpace.containsKey(dbId)) {
			return dbSpace.get(dbId);
		}else {
			Map<String, String> dataMap = new HashMap<>();
			List<Map<String, Object>> listData = listData(CRUD_DS_LABEL);
			
			for(Map<String, Object> dsi: listData) {
				dataMap.put(String.valueOf(dsi.get(CRUD_DS_NAME)), String.valueOf(dsi.get(CRUD_DS_PATH)));
			}
			if(dataMap.containsKey(dbId)) {
				GraphDatabaseService newEmbeddedDatabase = new GraphDatabaseFactory().newEmbeddedDatabase(new File(dataMap.get(dbId)));
				dbSpace.put(dbId, newEmbeddedDatabase);
				return newEmbeddedDatabase;
			}else {
				return null;
			}
		}
	}

	public Node getNodeById(Long id) {
		GraphDatabaseService instance = getInstance();
		Node nodeById = null;
		try (Transaction tx = instance.beginTx()) {
			nodeById = instance.getNodeById(id);
			tx.success();
		}
		return nodeById;
	}

	public JSONObject getNodeJSON(Node node) {
		GraphDatabaseService instance = getInstance();
		JSONObject jsonObjectFrom = null;
		// 开启事务
		try (Transaction tx = instance.beginTx()) {
			// Perform DB operations
			jsonObjectFrom = JSONMapUtil.jsonObject(node.getAllProperties());
			tx.success();
		}
		return jsonObjectFrom;
	}

	public Map<String, Object> getNodePropertiesById(Long id) {
		GraphDatabaseService instance = getInstance();
		Map<String, Object> nodeMap = null;
		// 开启事务
		try (Transaction tx = instance.beginTx()) {
			Node node = instance.getNodeById(id);
			// Perform DB operations
			nodeMap = node.getAllProperties();
			nodeMap.put("id", node.getId());
			tx.success();
		}
		return nodeMap;
	}
	public Map<String, Object> getNodeLabelPropertiesById(Long id) {
		GraphDatabaseService instance = getInstance();
		Map<String, Object> nodeMap = null;
		// 开启事务
		try (Transaction tx = instance.beginTx()) {
			Node node = instance.getNodeById(id);
			// Perform DB operations
			nodeMap = node.getAllProperties();
			nodeMap.put("id", node.getId());
			Iterator<Label> iterator = node.getLabels().iterator();
			List<String> laList = new ArrayList<>();
			while(iterator.hasNext()){
				laList.add(iterator.next().name());
			}
			nodeMap.put(LABEL, String.join(",", laList));
			tx.success();
		}
		return nodeMap;
	}
	public Map<String, Object> getNodePropertiesWithLabelById(Long id) {
		GraphDatabaseService instance = getInstance();
		Map<String, Object> nodeMap = null;
		// 开启事务
		try (Transaction tx = instance.beginTx()) {
			Node node = instance.getNodeById(id);
			// Perform DB operations
			nodeMap = node.getAllProperties();
			nodeMap.put("id", node.getId());
			Iterator<Label> iterator = node.getLabels().iterator();
			List<String> labList = new ArrayList<>();
			while(iterator.hasNext()) {
				labList.add(iterator.next().name());
			}
			String join = String.join(",", labList);
			nodeMap.put(LABEL, join);
			tx.success();
		}
		return nodeMap;
	}
	/**
	 * 获取节点的属性值
	 * @param id
	 * @param key
	 * @return
	 */
	public Object getNodePropValueById(Long id,String key) {
		GraphDatabaseService instance = getInstance();
		Object value = null;
		// 开启事务
		try (Transaction tx = instance.beginTx()) {
			Node node = instance.getNodeById(id);
			value = node.getProperty(key);
			tx.success();
		}
		return value;
	}

	public Map<String, Object> getNodeProperties(Node node) {
		GraphDatabaseService instance = getInstance();
		Map<String, Object> nodeMap = null;
		// 开启事务
		try (Transaction tx = instance.beginTx()) {
			// Perform DB operations
			nodeMap = node.getAllProperties();
			if (!nodeMap.containsKey(NODE_ID)) {
				node.setProperty(NODE_ID, node.getId());
				nodeMap.put(NODE_ID, node.getId());
			}

			tx.success();
		}
		return nodeMap;
	}

	/**
	 * 获取所有的关系
	 * 
	 * @param propMap
	 * @param label
	 * @return
	 */
	public List<Map<String, Object>> getAllOutgoings(Map<String, Object> propMap, String label) {
		Node startNode = queryNode(propMap, label);

		List<Map<String, Object>> relationshipList = new ArrayList<>();
		if (startNode == null) {
			return relationshipList;
		}
		try (Transaction tx = getInstance().beginTx()) {
			Iterable<Relationship> relationships = startNode.getRelationships(Direction.OUTGOING);
			handleRelationData(relationshipList, relationships);
			tx.success();
		}
		return relationshipList;
	}
	
	public List<Map<String, Object>> getAllDefineRelationList(Map<String, Object> propMap, String label) {
		Node startNode = queryNode(propMap, label);

		List<Map<String, Object>> relationshipList = new ArrayList<>();
		if (startNode == null) {
			return relationshipList;
		}
		try (Transaction tx = getInstance().beginTx()) {
			Iterable<Relationship> relationships = startNode.getRelationships(Direction.OUTGOING);
			handleDefineRelation(relationshipList, relationships);
			tx.success();
		}
		return relationshipList;
	}
	
	public String getRelationName(Map<String, Object> propMap, String label,String reLabel) {
		Node startNode = queryNode(propMap, label);

		if (startNode == null) {
			return "未定义";
		}
		String name=null;
		try (Transaction tx = getInstance().beginTx()) {
			Iterable<Relationship> relationships = startNode.getRelationships(Direction.OUTGOING);
			List<String> handleRelationName = handleRelationName(relationships,reLabel);
			if(handleRelationName!=null&&!handleRelationName.isEmpty()) {
				name=handleRelationName.get(0);
			}
			tx.success();
		}
		return name;
	}
	
	public List<String> getOutgoingsLabel(Map<String, Object> propMap, String label) {
		Node startNode = queryNode(propMap, label);

		List<String> relationshipList = new ArrayList<>();
		if (startNode == null) {
			return relationshipList;
		}
		try (Transaction tx = getInstance().beginTx()) {
			Iterable<Relationship> relationships = startNode.getRelationships(Direction.OUTGOING);
			relationships.forEach(p -> relationshipList.add(p.getType().name()));
			tx.success();
		}
		return relationshipList;
	}
	/**
	 * 获取某一类关系
	 * @param propMap
	 * @param label
	 * @param relationLabel
	 * @return
	 */
	public List<Map<String, Object>> getOneTypeOutgoings(Map<String, Object> propMap, String label,String relationLabel) {
		Node startNode = queryNode(propMap, label);

		return getOneRelationOf(relationLabel, startNode);
	}
	
	public List<Map<String, Object>> getOneTypeOutgoings(Long startId,String relationLabel) {
		Node startNode = getNodeById(startId);
		return getOneRelationOf(relationLabel, startNode);
	}

	private List<Map<String, Object>> getOneRelationOf(String relationLabel, Node startNode) {
		List<Map<String, Object>> relationshipList = new ArrayList<>();
		if (startNode == null) {
			return relationshipList;
		}
		try (Transaction tx = getInstance().beginTx()) {
			Iterable<Relationship> relationships = startNode.getRelationships(RelationshipType.withName(relationLabel), Direction.OUTGOING);
			handleRelationData(relationshipList, relationships);
			tx.success();
		}
		return relationshipList;
	}
	
	private List<Long> getEndIdOf(String relationLabel, Node startNode) {
		List<Long> endIdList = new ArrayList<>();
		if (startNode == null) {
			return endIdList;
		}
		try (Transaction tx = getInstance().beginTx()) {
			Iterable<Relationship> relationships = startNode.getRelationships(RelationshipType.withName(relationLabel), Direction.OUTGOING);
			getEndIdList(endIdList, relationships);
			tx.success();
		}
		return endIdList;
	}
	
	/**
	 * 获取一个关系的数据。
	 * @param propMap
	 * @param startLabel
	 * @param endlabel
	 * @return
	 */
	public List<Map<String, Object>> getOneDataList(Map<String, Object> propMap,String startLabel, String endlabel) {
		Node startNode = queryNode(propMap, startLabel);

		List<Map<String, Object>> relationshipList = new ArrayList<>();
		if (startNode == null) {
			return relationshipList;
		}
		try (Transaction tx = getInstance().beginTx()) {
			Iterable<Relationship> relationships = startNode.getRelationships(Direction.OUTGOING);
			getRelationData(relationshipList, relationships,endlabel);
			tx.success();
		}
		return relationshipList;
	}

	/**
	 * 获取所有入关系
	 * 
	 * @param propMap
	 * @param label
	 * @return
	 */
	public List<Map<String, Object>> getIncomings(Map<String, Object> propMap, String label) {
		Node startNode = queryNode(propMap, label);

		List<Map<String, Object>> relationshipList = new ArrayList<>();
		try (Transaction tx = getInstance().beginTx()) {
			Iterable<Relationship> relationships = startNode.getRelationships(Direction.INCOMING);
			handleRelationData(relationshipList, relationships);
			tx.success();
		}
		return relationshipList;
	}
	
	private void getRelationData(List<Map<String, Object>> relationshipList, Iterable<Relationship> relationships,String endLabel) {
		Iterator<Relationship> iterator = relationships.iterator();
		Boolean findEnd=false;
		while (iterator.hasNext()&&!findEnd) {
			Relationship ri = iterator.next();
			Map<String, Object> reMap = new HashMap<>();
			endNodeInfoOfRelation(ri, reMap);
			Object object = reMap.get(RELATION_ENDNODE_LABEL);
			if(null!=object) {
				List<String> labelList = (List<String>)object;
				if(labelList.contains(endLabel)) {
					findEnd=true;
				}
			}
			relationInfo(ri, reMap);
			
			relationshipList.add(reMap);
		}
	}

	/**
	 * 关系属性和关系另外一端的节点属性
	 * 
	 * @param relationshipList
	 * @param relationships
	 */
	private void handleRelationData(List<Map<String, Object>> relationshipList, Iterable<Relationship> relationships) {
		Iterator<Relationship> iterator = relationships.iterator();
		while (iterator.hasNext()) {
			Relationship ri = iterator.next();
			Map<String, Object> reMap = new HashMap<>();
			relationInfo(ri, reMap);
			endNodeInfoOfRelation(ri, reMap);
			relationshipList.add(reMap);
		}
	}
	
	private void getEndIdList(List<Long> endIdList, Iterable<Relationship> relationships) {
		Iterator<Relationship> iterator = relationships.iterator();
		while (iterator.hasNext()) {
			Relationship ri = iterator.next();
			Node endNode = ri.getEndNode();
			endIdList.add(endNode.getId());
		}
	}
	
	private List<String> handleRelationName(Iterable<Relationship> relationships,String reLabel) {
		Iterator<Relationship> iterator = relationships.iterator();
		List<String> nameList = new ArrayList<>();
		while (iterator.hasNext()) {
			Relationship ri = iterator.next();
			Object relationName = relationName(ri, reLabel);
			if(relationName!=null) {
				nameList.add(String.valueOf(relationName));
			}
		}
		return nameList;
	}
	
	private void handleDefineRelation(List<Map<String, Object>> relationshipList, Iterable<Relationship> relationships) {
		Iterator<Relationship> iterator = relationships.iterator();
		while (iterator.hasNext()) {
			Relationship ri = iterator.next();
			Map<String, Object> reMap = new HashMap<>();
			relationInfo(ri, reMap);
			endNodeInfoOfRelation(ri, reMap);
			relationshipList.add(reMap);
		}
	}
	
	private Object relationName(Relationship ri, String reLabel) {
		Map<String, Object> riProperties = ri.getAllProperties();
		RelationshipType type = ri.getType();
		if(reLabel.equals(type.name())) {
			return riProperties.get("name");
		}
		return null;
	}

	private void relationInfo(Relationship ri, Map<String, Object> reMap) {
		Map<String, Object> riProperties = ri.getAllProperties();
		RelationshipType type = ri.getType();
		reMap.put(RELATION_TYPE, type.name());
		reMap.put(RELATION_PROP, riProperties);
	}

	private void endNodeInfoOfRelation(Relationship ri, Map<String, Object> reMap) {
		Node endNode = ri.getEndNode();
		Map<String, Object> endNodeProperties = endNode.getAllProperties();
		if (!endNodeProperties.containsKey("id")) {
			endNodeProperties.put("id", endNode.getId());
		}
		Iterable<Label> labels = endNode.getLabels();
		List<String> labelList = new ArrayList<>();
		labels.forEach(p -> labelList.add(p.name()));
		reMap.put(RELATION_ENDNODE_LABEL, labelList);
		reMap.put(RELATION_ENDNODE_PROP, endNodeProperties);
	}

	public List<Map<String, Object>> getTheRelation(Map<String, Object> propMap, String label, String relationLabel) {
		Node startNode = queryNode(propMap, label);

		List<Map<String, Object>> relationshipList = new ArrayList<>();
		try (Transaction tx = getInstance().beginTx()) {
			Iterable<Relationship> relationship = startNode.getRelationships(RelationshipType.withName(relationLabel),
					Direction.OUTGOING);
			Iterator<Relationship> iterator = relationship.iterator();
			while (iterator.hasNext()) {
				Relationship ri = iterator.next();
//				Map<String, Object> reMap = new HashMap<>();
				Node endNode = ri.getEndNode();
				Map<String, Object> endNodeProperties = endNode.getAllProperties();
				relationshipList.add(endNodeProperties);
			}
			tx.success();
		}
		return relationshipList;
	}

	public Boolean delRelation(String startId, String endId, String startLabel, String relationLabel) {
		
		Node startDefine = findNode(LABEL, startLabel, META_DATA);
		Boolean delBoolean = false;
		try (Transaction tx = getInstance().beginTx()) {//清理Po定义关系
			Iterable<Relationship> relationship = startDefine.getRelationships(RelationshipType.withName(relationLabel));
			Iterator<Relationship> iterator = relationship.iterator();			
			delBoolean = handleIteration(endId, iterator);
			tx.success();
		}
		if(startId!=null) {//清理实例关系数据
			Node startNode = findNode(NODE_ID, startId, startLabel);
			try (Transaction tx = getInstance().beginTx()) {
				Iterable<Relationship> relationship = startNode.getRelationships(RelationshipType.withName(relationLabel));
				Iterator<Relationship> iterator = relationship.iterator();			
				delBoolean = handleIteration(endId, iterator);
				tx.success();
			}
		}
		
		return delBoolean;
	}

	private Boolean handleIteration(String endId, Iterator<Relationship> iterator
			) {
		Relationship ri = null;
		Boolean delBoolean=false;
		while (iterator.hasNext()) {
			ri = iterator.next();
			if(endId!=null) {
				Node endNode = ri.getEndNode();
				if (endId.equals(String.valueOf(endNode.getId()))) {
					delBoolean = true;
					break;
				} else {
					Map<String, Object> endNodeProperties = endNode.getAllProperties();
					if (endId.equals(endNodeProperties.get(NODE_ID))) {
						delBoolean = true;
					}
				}
			}else {
				delBoolean = true;
			}
			if (delBoolean && ri != null) {
				ri.delete();
				ri=null;
			}
		}
		if (delBoolean && ri != null) {
			ri.delete();
		}
		
		return delBoolean;
	}

	public JSONArray getRelation(String query) {
		JSONArray relationTree = new JSONArray();
		List<Map<String, Object>> relations = queryData(query);
		Map<Long, JSONObject> retMap = new HashMap<>();
		Map<Long, Set<Long>> relationMap = new HashMap<>();
		for (Map<String, Object> ri : relations) {
			Node m = (Node) ri.get("m");
			Node n = (Node) ri.get("r");
			long startId = n.getId();
			long endId = m.getId();
			if (!relationMap.containsKey(startId)) {
				Set<Long> objects = new HashSet<>();
				objects.add(endId);
				relationMap.put(startId, objects);
			} else {
				relationMap.get(startId).add(endId);
			}
			retMap.put(startId, getNodeJSON(n));
			retMap.put(endId, getNodeJSON(m));
		}
		for (Entry<Long, Set<Long>> ei : relationMap.entrySet()) {
			Long pid = ei.getKey();
			JSONObject jsonObject = retMap.get(pid);
			JSONArray childs = new JSONArray();
			Set<Long> value = ei.getValue();
			for (Long cid : value) {
				childs.add(retMap.get(cid));
			}
			jsonObject.put("childs", childs);
			relationTree.add(jsonObject);
		}
		return relationTree;
	}

	public Map<String, Object> getWholeTree(String label) {
		Node findNode = findNode("isRoot", "true", label);
		if (findNode == null) {
			return null;
		}
		Map<String, Object> rootMap = null;
		try (Transaction tx = getInstance().beginTx()) {
			rootMap = findNode.getProperties(NODE_ID, "name");
			rootMap.put("open", true);
			children(findNode, rootMap);
			tx.success();
		}
		return rootMap;
	}
	
	public Map<String, Object> getWholeTreeWithColumn(String label,String[] columns) {
		Node findNode = findNode("isRoot", "true", label);
		if (findNode == null) {
			return null;
		}
		Map<String, Object> rootMap = null;
		try (Transaction tx = getInstance().beginTx()) {
			rootMap = findNode.getProperties(columns);
			rootMap.put("open", true);
			childrenWithColumns(findNode, rootMap,columns);
			tx.success();
		}
		return rootMap;
	}

	private void query() {
		String query = "match (n:Node) return n";
		query(query);
	}

	public void query(String query) {
		GraphDatabaseService instance = getInstance();
		Map<String, Object> parameters = new HashMap<String, Object>();
		try (Transaction beginTx = instance.beginTx()) {
			Result result = instance.execute(query, parameters);
			while (result.hasNext()) {
				Map<String, Object> row = result.next();
				for (String key : result.columns()) {
					System.out.printf("%s = %s%n", key, row.get(key));
				}
			}
			beginTx.success();
		}

	}

	public List<Map<String, Object>> queryData(String query) {
		GraphDatabaseService instance = getInstance();
		List<Map<String, Object>> data = new ArrayList<>();
		try (Transaction beginTx = instance.beginTx()) {
			Result result = instance.execute(query);
			while (result.hasNext()) {
				Map<String, Object> rowi = new HashMap<>();
				Map<String, Object> row = result.next();

				for (Entry<String, Object> ei : row.entrySet()) {
					String key = ei.getKey();
					Object value = ei.getValue();
					if (value instanceof Node) {
						Node retNode = (Node) value;
						Map<String, Object> allProperties = retNode.getAllProperties();
						Iterable<Label> labels = retNode.getLabels();
						List<String> labeList = new ArrayList<>();
						for(Label li:labels) {
							labeList.add(li.name());
						}
						allProperties.put(LABEL, String.join(",", labeList));
						data.add(allProperties);
					} else {
						String ki = key.replaceAll("[a-zA-Z]+\\W?\\.", "");
						if (ki.equalsIgnoreCase("id(n)")) {
							ki = NODE_ID;
						}
						rowi.put(ki, value);
					}
				}
				if (!rowi.isEmpty()) {
					data.add(rowi);
				}
			}
			beginTx.success();
		}
		return data;
	}
	
	public List<Map<String, Object>> voQueryData(String query) {
		GraphDatabaseService instance = getInstance();
		List<Map<String, Object>> data = new ArrayList<>();
		try (Transaction beginTx = instance.beginTx()) {
			Result result = instance.execute(query);
			while (result.hasNext()) {
				Map<String, Object> rowi = new HashMap<>();
				Map<String, Object> row = result.next();

				for (Entry<String, Object> ei : row.entrySet()) {
					String key = ei.getKey();
					Object value = ei.getValue();
					if (value instanceof Node) {
						Node retNode = (Node) value;
						Map<String, Object> allProperties = retNode.getAllProperties();
						Iterable<Label> labels = retNode.getLabels();
						List<String> labeList = new ArrayList<>();
						for(Label li:labels) {
							labeList.add(li.name());
						}
						allProperties.put(LABEL, String.join(",", labeList));
						data.add(allProperties);
					} else {
						String ki = key.replaceAll("[a-zA-Z]+\\W?\\.", "");
						if (ki.equalsIgnoreCase("id(n)")) {
							ki = NODE_ID;
							rowi.put(ki, value);
						}
						rowi.put(key, value);
					}
				}
				if (!rowi.isEmpty()) {
					data.add(rowi);
				}
			}
			beginTx.success();
		}
		return data;
	}
	
	public List<Map<String, Object>> listData(String label) {
		GraphDatabaseService instance = getInstance();
		List<Map<String, Object>> data = new ArrayList<>();
		try (Transaction beginTx = instance.beginTx()) {
			ResourceIterator<Node> result = instance.findNodes(Label.label(label));
			while (result.hasNext()) {
				Map<String, Object> rowi = new HashMap<>();
				Node row = result.next();
				//节点数据
				rowi.putAll(row.getAllProperties());				
				if (!rowi.isEmpty()) {
					data.add(rowi);
				}
			}
			beginTx.success();
		}
		return data;
	}
	
	public List<Map<String, Object>> listAllData(String label) {
		GraphDatabaseService instance = getInstance();
		List<Map<String, Object>> data = new ArrayList<>();
		try (Transaction beginTx = instance.beginTx()) {
			ResourceIterator<Node> result = instance.findNodes(Label.label(label));
			while (result.hasNext()) {
				Map<String, Object> rowi = new HashMap<>();
				Node row = result.next();
				//节点数据
				rowi.putAll(row.getAllProperties());
				//所有关系数据
				Iterable<RelationshipType> relationshipTypes = row.getRelationshipTypes();
				for(RelationshipType rti: relationshipTypes) {
					List<Map<String, Object>> meList = new ArrayList<>();
					Iterable<Relationship> relationships = row.getRelationships(rti);
					for(Relationship ri: relationships) {
						meList.add(ri.getEndNode().getAllProperties());
					}
					rowi.put(rti.name(), meList);
				}
				if (!rowi.isEmpty()) {
					data.add(rowi);
				}
			}
			beginTx.success();
		}
		return data;
	}

	/**
	 * 多条件查询节点
	 * 
	 * @param propMap
	 * @param label
	 * @return
	 */
	public Node queryNode(Map<String, Object> propMap, String label) {
		GraphDatabaseService instance = getInstance();
		Node findNode = null;
		try (Transaction tx = instance.beginTx()) {

			Map<String, Object> validPropMap = clearNonStringValue(clearEmptyValue(propMap));
			ResourceIterator<Node> findNodes = instance.findNodes(Label.label(label), validPropMap);
			if (findNodes!=null&&findNodes.hasNext()) {
				findNode = findNodes.next();
			} else {
				if (validPropMap.containsKey(NODE_ID)) {
					Long nodeId = Long.parseLong(String.valueOf(validPropMap.get(NODE_ID)));
					
					Node nodeBy = instance.getNodeById(nodeId);
					validPropMap.remove(NODE_ID);
					if (nodeBy != null) {
						findNode = nodeBy;
					} else {
						ResourceIterator<Node> noIdNodes = instance.findNodes(Label.label(label), validPropMap);
						while (noIdNodes.hasNext()) {// 删除老数据
							findNode = noIdNodes.next();
							Map<String, Object> allProperties = findNode.getAllProperties();
							Long obj = Long.parseLong(String.valueOf(allProperties.get(NODE_ID)));
							if (!nodeId.equals(obj)) {
								findNode.setProperty(NODE_ID, obj);
							}
						}
					}
				} else {
					ResourceIterator<Node> noIdNodes = instance.findNodes(Label.label(label), validPropMap);
					if (noIdNodes.hasNext()) {
						findNode = noIdNodes.next();
					}
				}
			}
			tx.success();
		}
		return findNode;
	}

	/**
	 * 删除与当前节点所有关系数据后，再删除节点
	 * 
	 * @param propMap
	 * @param label
	 * @return
	 */
	public Node removeNode(Map<String, Object> propMap, String label) {
		GraphDatabaseService instance = getInstance();
		Node findNode = null;
		try (Transaction tx = instance.beginTx()) {
			ResourceIterator<Node> findNodes = instance.findNodes(Label.label(label), clearNonStringValue(clearEmptyValue(propMap)));
			if (findNodes.hasNext()) {
				findNode = findNodes.next();
				findNode = deleteRelAndNode(findNode);
			} else {
				Long nodeId = Long.parseLong(String.valueOf(propMap.get(NODE_ID)));
				if (propMap.containsKey(NODE_ID)) {
					propMap.remove(NODE_ID);
				}
				ResourceIterator<Node> noIdNodes = instance.findNodes(Label.label(label), clearNonStringValue(clearEmptyValue(propMap)));
				while (noIdNodes.hasNext()) {// 删除老数据
					findNode = noIdNodes.next();
					if (findNode.getId() == nodeId) {
						findNode = deleteRelAndNode(findNode);
					} else {
						Map<String, Object> allProperties = findNode.getAllProperties();
						String valueOf = String.valueOf(allProperties.get(NODE_ID));
						if (CommonUtil.isNumber(valueOf)) {
							Long obj = Long.parseLong(valueOf);
							if (nodeId.equals(obj)) {
								findNode = deleteRelAndNode(findNode);
							}
						}
					}
				}
			}
			tx.success();
		}
		return findNode;
	}
	
	public void removeById(String id) {
		Long nodeId = Long.parseLong(id);
		removeById(nodeId);
	}

	public void removeById(Long nodeId) {
		try (Transaction tx = getInstance().beginTx()) {
			Node nodeById = getInstance().getNodeById(nodeId);
			deleteRelAndNode(nodeById);
			tx.success();
		}
	}

	private Node deleteRelAndNode(Node findNode) {
		for (Relationship rsi : findNode.getRelationships()) {
			rsi.delete();
		}
		findNode.delete();
		findNode = null;
		return findNode;
	}

	public Node queryNode(String query) {
		GraphDatabaseService instance = getInstance();
		Node node = null;
		try (Transaction beginTx = instance.beginTx()) {
			Result result = instance.execute(query);
			if (result.hasNext()) {
				node = (Node) result.next();
			}
			beginTx.success();
		}
		return node;
	}

	public JSONArray queryRelation(String query) {
		JSONArray relationTree = new JSONArray();
		List<Map<String, Object>> relations = queryData(query);
		Map<Long, JSONObject> retMap = new HashMap<>();
		Map<Long, Set<Long>> relationMap = new HashMap<>();
		for (Map<String, Object> ri : relations) {
			Node m = (Node) ri.get("m");
			Node n = (Node) ri.get("n");
			long startId = n.getId();
			long endId = m.getId();
			if (!relationMap.containsKey(startId)) {
				Set<Long> objects = new HashSet<>();
				objects.add(endId);
				relationMap.put(startId, objects);
			} else {
				relationMap.get(startId).add(endId);
			}
			retMap.put(startId, getNodeJSON(n));
			retMap.put(endId, getNodeJSON(m));
		}
		for (Entry<Long, Set<Long>> ei : relationMap.entrySet()) {
			Long pid = ei.getKey();
			JSONObject jsonObject = retMap.get(pid);
			JSONArray childs = new JSONArray();
			Set<Long> value = ei.getValue();
			for (Long cid : value) {
				childs.add(retMap.get(cid));
			}
			jsonObject.put("childs", childs);
			relationTree.add(jsonObject);
		}
		return relationTree;
	}

	public void registerShutdownHook() {
		registerShutdownHook(getInstance());
	}

	private void show() {
		GraphDatabaseService instance = getInstance();
		try (Transaction tx = instance.beginTx()) {
			// Node
			ResourceIterable<Node> nIt = instance.getAllNodes();
			nIt.forEach((node) -> showNodes(node));
			// commit transaction
			tx.success();
		}
		// Stop the database
		instance.shutdown();
	}

	private void showNodes(Node node) {
		System.out.print(node.getId() + "-id;");
		node.getLabels().forEach((label) -> System.out.print(label + "-label;"));
		node.getAllProperties().entrySet().forEach((entity) -> {
			System.out.print(entity.getKey() + "=");
			System.out.println(entity.getValue() + ",");
		});
		node.getRelationships().forEach((rel) -> System.out.println(rel));
	}

	public Node updateNode(Map<String, Object> props, Label label) {
		GraphDatabaseService instance = getInstance();
		Node node = null;
		System.out.println("Database Load!");
		Map<String, Object> validaMap = clearNonStringValue(clearEmptyValue(props));
		// 开启事务
		try (Transaction tx = instance.beginTx()) {
			// Perform DB operations
			node = instance.createNode(label);
			for (String key : validaMap.keySet()) {
				node.setProperty(key, String.valueOf(validaMap.get(key)));
			}
			tx.success();
		}
		return node;
	}

	public Node updateNode(Map<String, Object> props, Node node) {
		GraphDatabaseService instance = getInstance();
		// 开启事务
		try (Transaction tx = instance.beginTx()) {
			// Perform DB operations
			Map<String, Object> validaMap = clearNonStringValue(clearEmptyValue(props));
			
			for (String key : validaMap.keySet()) {
				Object value = validaMap.get(key);
				if (value != null) {
					if (key.startsWith("n\\.")) {
						key = key.replace("n\\.", "");
					}
					String nodeId = String.valueOf(node.getId());
					if (NODE_ID.equalsIgnoreCase(key) && !value.equals(nodeId)) {
						value = nodeId;
					}
					node.setProperty(key, value);
				}
			}
			tx.success();
		}
		return node;
	}

	public void useIndex() {
		GraphDatabaseService instance = getInstance();

		try (Transaction tx = instance.beginTx()) {
			Index<Node> index = instance.index().forNodes("nodes");

			Node node1 = instance.createNode();
			String name = "歌手 1";
			node1.setProperty("name", name);
			index.add(node1, "name", name);
			node1.setProperty("gender", "男");
			Object result = index.get("name", "歌手 1").getSingle().getProperty("gender");
			System.out.println(result); // 输出为“男”
			tx.success();
		}

	}

	public Boolean hasIndex(String label, String field) {
		GraphDatabaseService instance = getInstance();
		try (Transaction tx = instance.beginTx()) {
			Iterable<IndexDefinition> indexes = instance.schema().getIndexes(Label.label(label));
			for (IndexDefinition idi : indexes) {
				Iterable<String> propertyKeys = idi.getPropertyKeys();
				for (String pki : propertyKeys) {
					if (field.equals(pki)) {
						return true;
					}
				}
			}
			tx.success();
		}
		return false;
	}

	public Boolean hasConstraint(String label, String field) {
		GraphDatabaseService instance = getInstance();
		try (Transaction tx = instance.beginTx()) {
			Iterable<ConstraintDefinition> indexes = instance.schema().getConstraints(Label.label(label));
			for (ConstraintDefinition idi : indexes) {
				Iterable<String> propertyKeys = idi.getPropertyKeys();
				for (String pki : propertyKeys) {
					if (pki.indexOf(field)>0) {
						return true;
					}
				}
			}
			tx.success();
		}
		return false;
	}
	
	private Map<String, Object> clearEmptyValue(Map<String, Object> props) {
		Map<String, Object> propMap = new HashMap<String, Object>();
		for (Entry<String, Object> eni : props.entrySet()) {
			if (StringUtils.isNotBlank(String.valueOf(eni.getValue()))) {
				propMap.put(eni.getKey(), eni.getValue());
			}
		}
		return propMap;
	}

	private Map<String, Object> clearNonStringValue(Map<String, Object> props) {
		Map<String, Object> propMap = new HashMap<String, Object>();
		for (Entry<String, Object> eni : props.entrySet()) {
			Object value = eni.getValue();
			if (value instanceof String||
				value instanceof Integer||
				value instanceof Double) {
				propMap.put(eni.getKey(), value);
			}
		}
		return propMap;
	}
}
