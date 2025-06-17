package com.wldst.ruder.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import com.wldst.ruder.domain.CrudSystem;

/**
 * 工具类。
 * 
 * @author deeplearn96
 *
 */
public class NeoUtil extends CrudSystem {
    // private static GraphDatabaseService graphDb;
    private static Map<String, GraphDatabaseService> dbSpace = new HashMap<>();
    final static Logger logger = LoggerFactory.getLogger(NeoUtil.class);


    public static void childrenWithColumns(Node findNode, Map<String, Object> rootMap, String[] columns) {
	List<Map<String, Object>> childList = new ArrayList<>();

	Iterable<Relationship> relationships = findNode.getRelationships(Direction.OUTGOING,
		RelationshipType.withName(REL_TYPE_CHILDREN), RelationshipType.withName(REL_TYPE_CHILDRENS));
	relationships.forEach(p -> {
	    Node endNode = p.getEndNode();
	    Map<String, Object> properties = endNode.getProperties(columns);
	    if (!properties.containsKey(NODE_ID)) {
		properties.put(NODE_ID, endNode.getId());
	    }
	    childrenWithColumns(endNode, properties, columns);
	    childList.add(properties);
	});

	if (!childList.isEmpty()) {
	    rootMap.put(REL_TYPE_CHILDREN, childList);
	}
    }

    public static   void children(Node findNode, Map<String, Object> rootMap) {
	List<Map<String, Object>> childList = new ArrayList<>();

	Iterable<Relationship> relationships = findNode.getRelationships(Direction.OUTGOING,
		RelationshipType.withName(REL_TYPE_CHILDREN), RelationshipType.withName(REL_TYPE_CHILDRENS));
	relationships.forEach(p -> {
	    Node endNode = p.getEndNode();
	    Map<String, Object> properties = endNode.getProperties(NODE_ID, "name");
	    if (!properties.containsKey(NODE_ID)) {
		properties.put(NODE_ID, endNode.getId());
	    }
	    children(endNode, properties);
	    childList.add(properties);
	});

	if (!childList.isEmpty()) {
	    rootMap.put(REL_TYPE_CHILDREN, childList);
	}
    }

    public static   void childList(Node findNode, Map<String, Object> rootMap) {
	List<Map<String, Object>> childList = getChildListByNode(findNode);

	if (!childList.isEmpty()) {
	    rootMap.put(REL_TYPE_CHILDREN, childList);
	}
    }

    public static   List<Map<String, Object>> getChildListByNode(Node findNode) {
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

    public static  List<Map<String, Object>> getChildPropListByNode(Node findNode) {
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
   

    public static List<Long> getChildNodeIdByNode(Node findNode, String label) {
	List<Long> childIdList = new ArrayList<>();

	Iterable<Relationship> relationships = findNode.getRelationships(Direction.OUTGOING,
		RelationshipType.withName(label));
	relationships.forEach(p -> {
	    Node endNode = p.getEndNode();
	    childIdList.add(endNode.getId());
	});
	return childIdList;
    }

    public static void getRelationData(List<Map<String, Object>> relationshipList, Iterable<Relationship> relationships,
	    String endLabel) {
	Iterator<Relationship> iterator = relationships.iterator();
	Boolean findEnd = false;
	while (iterator.hasNext() && !findEnd) {
	    Relationship ri = iterator.next();
	    Map<String, Object> reMap = new HashMap<>();
	    endNodeInfoOfRelation(ri, reMap);
	    Object object = reMap.get(RELATION_ENDNODE_LABEL);
	    if (null != object) {
		List<String> labelList = (List<String>) object;
		if (labelList.contains(endLabel)) {
		    findEnd = true;
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
    public static void handleRelationData(List<Map<String, Object>> relationshipList, Iterable<Relationship> relationships) {
	Iterator<Relationship> iterator = relationships.iterator();
	while (iterator.hasNext()) {
	    Relationship ri = iterator.next();
	    Map<String, Object> reMap = new HashMap<>();
	    relationInfo(ri, reMap);
	    endNodeInfoOfRelation(ri, reMap);
	    relationshipList.add(reMap);
	}
    }

    public static List<String> handleRelationName(Iterable<Relationship> relationships, String reLabel) {
	Iterator<Relationship> iterator = relationships.iterator();
	List<String> nameList = new ArrayList<>();
	while (iterator.hasNext()) {
	    Relationship ri = iterator.next();
	    Object relationName = relationName(ri, reLabel);
	    if (relationName != null) {
		nameList.add(String.valueOf(relationName));
	    }
	}
	return nameList;
    }

    public static void handleDefineRelation(List<Map<String, Object>> relationshipList,
	    Iterable<Relationship> relationships) {
	Iterator<Relationship> iterator = relationships.iterator();
	while (iterator.hasNext()) {
	    Relationship ri = iterator.next();
	    Map<String, Object> reMap = new HashMap<>();
	    relationInfo(ri, reMap);
	    endNodeInfoOfRelation(ri, reMap);
	    relationshipList.add(reMap);
	}
    }

    public static void onlyRelationData(List<Map<String, Object>> relationshipList, Iterable<Relationship> relationships) {
	Iterator<Relationship> iterator = relationships.iterator();
	while (iterator.hasNext()) {
	    Relationship ri = iterator.next();
	    relationshipList.add(relationData(ri));
	}
    }

    public static Object relationName(Relationship ri, String reLabel) {
	Map<String, Object> riProperties = ri.getAllProperties();
	RelationshipType type = ri.getType();
	if (reLabel.equals(type.name())) {
	    return riProperties.get("name");
	}
	return null;
    }

    public static void relationInfo(Relationship ri, Map<String, Object> reMap) {
	Map<String, Object> riProperties = ri.getAllProperties();
	RelationshipType type = ri.getType();
	reMap.put(RELATION_TYPE, type.name());
	reMap.put(RELATION_PROP, riProperties);
    }

    public static Map<String, Object> relationData(Relationship ri) {
	Map<String, Object> reMap = new HashMap<>();
	Map<String, Object> riProperties = ri.getAllProperties();
	RelationshipType type = ri.getType();
	reMap.put(RELATION_TYPE, type.name());
	reMap.put(RELATION_PROP, riProperties);
	reMap.put(RELATION_START_ID, ri.getStartNodeId());
	reMap.put(RELATION_END_ID, ri.getEndNodeId());
	return reMap;
    }

    public static void endNodeInfoOfRelation(Relationship ri, Map<String, Object> reMap) {
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
    
    public static Map<String, Object> getRelEndNodePropties(Map<String, Object> reMap) {
	return (Map<String, Object>) reMap.get(RELATION_ENDNODE_PROP);
    }
    
    public static List<String> getRelEndNodeLabel(Map<String, Object> reMap) {
	return (List<String>) reMap.get(RELATION_ENDNODE_LABEL);
    }

    
    public Boolean handleIteration(String endId, Iterator<Relationship> iterator) {
	Relationship ri = null;
	Boolean delBoolean = false;
	while (iterator.hasNext()) {
	    ri = iterator.next();
	    if (endId != null) {
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
	    } else {
		delBoolean = true;
	    }
	    if (delBoolean && ri != null) {
		ri.delete();
		ri = null;
	    }
	}
	if (delBoolean && ri != null) {
	    ri.delete();
	}

	return delBoolean;
    }

    

    

    public Node deleteRelAndNode(Node findNode) {
	for (Relationship rsi : findNode.getRelationships()) {
	    rsi.delete();
	}
	findNode.delete();
	findNode = null;
	return findNode;
    }

    public void showNodes(Node node) {
	System.out.print(node.getId() + "-id;");
	node.getLabels().forEach((label) -> System.out.print(label + "-label;"));
	node.getAllProperties().entrySet().forEach((entity) -> {
	    System.out.print(entity.getKey() + "=");
	    System.out.println(entity.getValue() + ",");
	});
	node.getRelationships().forEach((rel) -> System.out.println(rel));
    }

    public Map<String, Object> clearEmptyValue(Map<String, Object> props) {
	Map<String, Object> propMap = new HashMap<String, Object>();
	for (Entry<String, Object> eni : props.entrySet()) {
	    if (StringUtils.isNotBlank(String.valueOf(eni.getValue()))) {
		propMap.put(eni.getKey(), eni.getValue());
	    }
	}
	return propMap;
    }

    public Map<String, Object> clearNonStringValue(Map<String, Object> props) {
	Map<String, Object> propMap = new HashMap<String, Object>();
	for (Entry<String, Object> eni : props.entrySet()) {
	    Object value = eni.getValue();
	    if (value instanceof java.util.Date) {
		propMap.put(eni.getKey(), DateTool.format((Date) value, "yyyy-MM-dd hh:MM:ss"));
		continue;
	    }
	    if (value instanceof String || value instanceof Integer || value instanceof Double
		    || !String.valueOf(value).isBlank()) {
		propMap.put(eni.getKey(), value);
	    }

	}
	return propMap;
    }
}
