package com.wldst.ruder.crud.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.CrudSystem;
import com.wldst.ruder.util.CommonUtil;
import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.JSONMapUtil;

/**
 * neo4j本地开发
 *
 * @author deeplearn96
 */
@Component
public class CrudNeo4jDriver extends CrudSystem{
    // private static GraphDatabaseService graphDb;
    private static Map<String, GraphDatabaseService> dbSpace=new HashMap<>();
    GraphDatabaseService graphDb=null;
    private static ExecutorService exec=getExecutorService();

    final static Logger logger=LoggerFactory.getLogger(CrudNeo4jDriver.class);

    private static void createCheckObjectUseRule(CrudNeo4jDriver d){
        String query="create(n:CheckObject{name:\"招投标\"}),(m:CheckRule{name:\"客户名称不能为空\",code:\"test2\"})  create (n)-[r:using_rule]->(m)";
        d.queryData(query);
        query="create(n:CheckObject{name:\"招投标\"}),(m:CheckRule{name:\"地址不能为空\",code:\"test2\"})  create (n)-[r:using_rule]->(m)";
        query="create(n:CheckObject{name:\"招投标\"}),(m:CheckRule{name:\"联系方式不能为空\",code:\"test2\"})  create (n)-[r:using_rule]->(m)";
        d.queryData(query);
    }

    private static void getRelation(CrudNeo4jDriver d){
        // String query = "match (n:closeth{id:\"1\"})-[r]->(m) return r,m SKIP 0 Limit
        // 10 ";
        String query="match (n:User)-[r]->(m) return r,m SKIP 0 Limit 20  ";
        // query="match (n:CheckRule) return n.name ,n.tableName SKIP 0 Limit 10 ";
        JSONArray relations=d.getRelation(query);

        System.out.println(relations.toJSONString());
    }

    public static void main(String[] args){
        // createNode();
        // 查询数据库
        // query();
        // queryRelation();
        // registerShutdownHook();
        // useIndex();
        // show()
        CrudNeo4jDriver d=new CrudNeo4jDriver();
        // d.useIndex();
        // createCheckObjectUseRule(d);
        // queryRelation(d);
        getRelation(d);
        /*
         * String createIndex = "create INDEX ON :Domain(label)"; d.query(createIndex);
         */
    }

    private static void queryRelation(CrudNeo4jDriver d){
        String query="match(n)-[r:using_rule]->(m) return n,m";
        query="match (n)-[using_rule]->(m) return n,m SKIP 0 Limit 10 ";
        // query="match (n:CheckRule) return n.name ,n.tableName SKIP 0 Limit 10 ";
        JSONArray relations=d.relationData(query);

        System.out.println(relations.toJSONString());
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb){
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run(){
                graphDb.shutdown();
            }
        });
    }

    @Value("${neo4j.db.path}")
    private String dbPath="..\\neo4j";

    @Value("${neo4j.log.path}")
    private String dbLogPath="..\\neo4jlog";

    private void childrenWithColumns(Node rootNode, Map<String, Object> rootMap, String[] columns){
        List<Map<String, Object>> childList=new ArrayList<>();

        Iterable<Relationship> relationships=rootNode.getRelationships(Direction.OUTGOING,
                RelationshipType.withName(REL_TYPE_CHILDREN), RelationshipType.withName(REL_TYPE_CHILDRENS));
        relationships.forEach(p->{
            Node ciNode=p.getEndNode();
            Map<String, Object> properties=ciNode.getProperties(columns);
            if(!properties.containsKey(NODE_ID)){
                properties.put(NODE_ID, ciNode.getId());
            }
            childrenWithColumns(ciNode, properties, columns);
            childList.add(properties);
        });

        if(!childList.isEmpty()){
            rootMap.put(REL_TYPE_CHILDREN, childList);
        }
    }

    private void subRelation(Map<String, Object> ti, Map<String, Object> treeInfo, String[] columns){
        List<Map<String, Object>> childList=new ArrayList<>();
        String parentIdField=string(treeInfo, "parentIdField");
        Node parentNode=graphDb.getNodeById(id(ti));
        getOutRel(treeInfo, columns, childList, parentIdField, parentNode, "children");
//	getOutRel(treeInfo, columns, childList, parentIdField, parentNode, "childrens");
//	getOutRel(treeInfo, columns, childList, parentIdField, parentNode, "child");
//	getOutRel(treeInfo, columns, childList, parentIdField, parentNode, "sub");

        if(!childList.isEmpty()){

            List<Map<String, Object>> listMapObject=listMapObject(ti, REL_TYPE_CHILDREN);
            if(listMapObject==null||listMapObject.isEmpty()){
                ti.put(REL_TYPE_CHILDREN, childList);
            }else{
                Set<Long> dataId=new HashSet<>();
                for(Map<String, Object> di : listMapObject){
                    dataId.add(id(di));
                }
                for(Map<String, Object> dx : childList){
                    if(dataId.contains(id(dx))){
                        listMapObject.add(dx);
                    }
                }
            }
        }
    }

    public void getOutRel(Map<String, Object> treeInfo, String[] columns, List<Map<String, Object>> childList,
                          String parentIdField, Node parentNode, String chilrelName){
        Iterable<Relationship> relationships=parentNode.getRelationships(Direction.OUTGOING,
                RelationshipType.withName(chilrelName));
        relationships.forEach(p->{
            Node ciNode=p.getEndNode();
            Map<String, Object> properties=ciNode.getProperties(columns);
            if(!properties.containsKey(NODE_ID)){
                properties.put(NODE_ID, ciNode.getId());
            }
//	    subRelation(properties,treeInfo, columns);
            if(parentIdField==null){
                properties.put(PARENT_ID, parentNode.getId());
            }else{
                properties.put(parentIdField, parentNode.getId());
            }

            childList.add(properties);
        });
    }

    private void childrenWithColumns(Map<String, Object> qi, String parentIdField, String[] columns){
        List<Map<String, Object>> childList=new ArrayList<>();
        Node parentNode=graphDb.getNodeById(id(qi));
        Iterable<Relationship> relationships=parentNode.getRelationships(Direction.OUTGOING,
                RelationshipType.withName(REL_TYPE_CHILDREN), RelationshipType.withName(REL_TYPE_CHILDRENS));
        relationships.forEach(p->{
            Node ciNode=p.getEndNode();
            Map<String, Object> properties=ciNode.getProperties(columns);
            if(!properties.containsKey(NODE_ID)){
                properties.put(NODE_ID, ciNode.getId());
            }
            childrenWithColumns(properties, parentIdField, columns);

            properties.put(parentIdField, parentNode.getId());
            childList.add(properties);
        });

        if(!childList.isEmpty()){
            qi.put(REL_TYPE_CHILDREN, childList);
        }
    }

    private void children(String label, Map<String, Object> rootMap, Set<Long> childIds){
        LoggerTool.info(logger, "=======children==========={}={}=", label, mapString(rootMap));
        List<Map<String, Object>> childList=new ArrayList<>();
        String query="match(n:"+label+") where n.parentId="+id(rootMap)+" or n.parentId=\""+id(rootMap)+"\" return n ";
        List<Map<String, Object>> childrens=queryData(query);
        LoggerTool.info(logger, "=======childrens="+childrens.size()+"========={}=", query);

//	if (childrens == null || childrens.isEmpty()) {
//	    query = "match(n:" + label + ") where n.parentId=" + id(rootMap) + " return n ";
//	    LoggerTool.info(logger,"=======children=is null=========={}=", query);
//	    childrens = queryData(query);
//	    if (childrens == null || childrens.isEmpty()) {
//		childrens = new ArrayList<>();
//	    }
//	}
        Set<Long> cIds=new HashSet<>();
        for(Map<String, Object> ci : childrens){
            cIds.add(id(ci));
        }
        // 兼容查询关系数据
        collectChild(label, REL_TYPE_CHILDREN, rootMap, childrens, cIds);
        collectChild(label, REL_TYPE_CHILDRENS, rootMap, childrens, cIds);
        collectChild(label, "child", rootMap, childrens, cIds);

        LoggerTool.info(logger, "=======childIds.size="+cIds.size()+"=========={}={}=");
        for(Map<String, Object> ci : childrens){
            Long cIDi=id(ci);
            if(!childIds.contains(cIDi)){
                childIds.add(cIDi);
                children(label, ci, childIds);
                childList.add(ci);
            }
        }

        if(!childList.isEmpty()){
            rootMap.put(REL_TYPE_CHILDREN, childList);
        }
    }

    /**
     * 下级数据
     *
     * @param label
     * @param treeNode
     */
    private void subData(String label, Map<String, Object> treeInfo, Map<String, Object> treeNode, Set<String> childIdSet){
        String parentIdField=string(treeInfo, "parentIdField");
        LoggerTool.info(logger, "=======children==========={}={}===========", label, mapString(treeNode));
        List<Map<String, Object>> childList=new ArrayList<>();
        Long upLevelId=id(treeNode);
        String query="match(n:"+label+") where n."+parentIdField+"="+upLevelId+" or n."+parentIdField+"=\""+upLevelId+"\" return n ";
        List<Map<String, Object>> childrens=queryData(query);
        LoggerTool.info(logger, "=======childrens="+childrens.size()+"========={}=============", query);

        Set<Long> childIds=new HashSet<>();
        for(Map<String, Object> ci : childrens){
            if(id(ci)==longValue(ci, parentIdField)){
                continue;
            }
            childIds.add(id(ci));
        }
        // 兼容查询关系数据
        String treeRelCode=code(treeInfo);
        collectChild(label, treeRelCode, treeNode, childrens, childIds);

        LoggerTool.info(logger, "=======childIds.size="+childIds.size()+"=========={}={}==============");
        for(Map<String, Object> ci : childrens){
            if(childIdSet.contains(id(ci))){
                subData(label, treeInfo, ci, childIdSet);
                childList.add(ci);
            }
        }

        if(!childList.isEmpty()){
            List<Map<String, Object>> listMapObject=listMapObject(treeNode, REL_TYPE_CHILDREN);
            if(listMapObject==null||listMapObject.isEmpty()){
                treeNode.put(REL_TYPE_CHILDREN, childList);
            }else{
                Set<Long> dataId=new HashSet<>();
                for(Map<String, Object> di : listMapObject){
                    dataId.add(id(di));
                }
                for(Map<String, Object> dx : childList){
                    if(!dataId.contains(id(dx))){
                        listMapObject.add(dx);
                    }
                }
            }
        }


    }

    private void collectChild(String label, String childrenLabel, Map<String, Object> rootMap,
                              List<Map<String, Object>> childrens, Set<Long> childIds){
        String childRel="match(n:"+label+")<-[:"+childrenLabel+"]-(m:"+label+") where id(m)=\""
                +id(rootMap)+"\" return n ";
        LoggerTool.info(logger, "=======collectChild=========={}==========", childRel);
        List<Map<String, Object>> childrenRels=queryData(childRel);

        childRel="match(n:"+label+")<-[:"+childrenLabel+"]-(m:"+label+") where id(m)="
                +id(rootMap)+" return n ";
        if(childrenRels==null||childrenRels.isEmpty()){
            childrenRels=queryData(childRel);
        }else{
            childrenRels.addAll(queryData(childRel));
        }
        if(childrenRels!=null&&!childrenRels.isEmpty()){
            LoggerTool.info(logger, "=======childrenRels=========={}=====", childrenRels.size());

            for(Map<String, Object> cri : childrenRels){
                if(!childIds.contains(id(cri))){
                    childrens.add(cri);
                    childIds.add(id(cri));
                }
            }
        }
    }

    private void childList(Node findNode, Map<String, Object> rootMap){
        List<Map<String, Object>> childList=getChildListByNode(findNode);

        if(!childList.isEmpty()){
            rootMap.put(REL_TYPE_CHILDREN, childList);
        }
    }

    private List<Map<String, Object>> getChildListByNode(Node findNode){
        List<Map<String, Object>> childList=new ArrayList<>();

        Iterable<Relationship> relationships=findNode.getRelationships(Direction.OUTGOING,
                RelationshipType.withName(REL_TYPE_CHILDREN), RelationshipType.withName(REL_TYPE_CHILDRENS));
        relationships.forEach(p->{
            Node endNode=p.getEndNode();
            Map<String, Object> properties=endNode.getProperties(NODE_ID, "name");
            childList.add(properties);
        });
        return childList;
    }

    private List<Map<String, Object>> getChildListByNode(Node findNode, String endLabel){
        List<Map<String, Object>> childList=new ArrayList<>();

        Iterable<Relationship> relationships=findNode.getRelationships(Direction.OUTGOING,
                RelationshipType.withName(REL_TYPE_CHILDREN), RelationshipType.withName(REL_TYPE_CHILDRENS));
        relationships.forEach(p->{
            Node endNode=p.getEndNode();
            Map<String, Object> properties=endNode.getProperties(NODE_ID, "name");
            childList.add(properties);
        });
        return childList;
    }

    private List<Map<String, Object>> getChildPropListByNode(Node findNode){
        List<Map<String, Object>> childList=new ArrayList<>();

        Iterable<Relationship> relationships=findNode.getRelationships(Direction.OUTGOING,
                RelationshipType.withName(REL_TYPE_CHILDREN), RelationshipType.withName(REL_TYPE_CHILDRENS));
        relationships.forEach(p->{
            Node endNode=p.getEndNode();
            Map<String, Object> properties=endNode.getAllProperties();
            properties.put("id", endNode.getId());
            childList.add(properties);
        });
        return childList;
    }

    public List<Long> getEndNodeId(String label, String rLabel, Map<String, Object> queryMap){
        Node queryNode=queryNode(queryMap, label);
        return getEndNodeIdList(label, rLabel, queryNode.getId());
    }

    public List<Long> getEndNodeIdList(String label, String rLabel, Object object){
        Node findNode=findNode(NODE_ID, String.valueOf(object), label);
        if(findNode==null){
            return null;
        }
        return vtResult(()->{
            try(Transaction tx=getInstance().beginTx()){
                List<Long> childList=getChildNodeIdByNode(findNode, rLabel);
                tx.success();
                tx.close();
                return childList;
            }
        });
    }

    public List<Long> getChildNodeIdByNode(Node findNode, String label){
        List<Long> childIdList=new ArrayList<>();

        Iterable<Relationship> relationships=findNode.getRelationships(Direction.OUTGOING,
                RelationshipType.withName(label));
        relationships.forEach(p->{
            Node endNode=p.getEndNode();
            childIdList.add(endNode.getId());
        });
        return childIdList;
    }

    public Node createNode(Map<String, Object> props, Label label, String pkey){
        System.out.println(label.name()+"的Id："+pkey+"属性:"+JSON.toJSONString(props));
        // 开启事务
        Node node=null;
        props.put(VERSION_DATA, 1);
        GraphDatabaseService instance=getInstance();
        try(Transaction tx=instance.beginTx()){
            // Perform DB operations
            node=instance.createNode(label);

            Object object=props.get(NODE_ID);
            long id=node.getId();
            if(StringUtils.isBlank(String.valueOf(object))||object==null){
                node.setProperty(NODE_ID, id);
                if(props.containsKey(NODE_ID)){
                    props.remove(NODE_ID);
                }
            }
            for(String key : props.keySet()){
                Object value2=props.get(key);
                if(value2!=null&&!"null".equals(value2)&&!"".equals(value2)){
                    setValue(node, key, value2);
                }else{
                    if(node.getAllProperties().containsKey(key)){
                        Object property=node.getProperty(key);
                        if(property!=null){
                            node.removeProperty(key);
                        }
                    }

                }

            }
            props.put(NODE_ID, id);
            tx.success();
        }
        return node;
    }

    public Node createNode(Map<String, Object> props, String label){
        if(label==null){
            label=label(props);
        }
        if(label==null){
            return null;
        }
        return createNode(props, Label.label(label));
    }

    /**
     * @param props
     * @param label
     * @return
     */
    public Node createNode(Map<String, Object> props, Label label){
        String labelName=label.name();
        if(label==null||labelName.equals("")){
            String string=string(props, LABEL);
            if(string==null||"".equals(string)){
                return null;
            }
            label=Label.label(string);
        }
        GraphDatabaseService instance=getInstance();
        if(props.isEmpty()){
            return null;
        }
        Map<String, Object> validateMap=clearNonStringValue(clearEmptyValue(props));

        Node node=null;

        // 开启事务
        try(Transaction tx=instance.beginTx()){
            // Perform DB operations
            node=instance.createNode(label);
            if(props.containsKey(NODE_ID)){
                props.remove(NODE_ID);
            }
            validateMap.put(VERSION_DATA, 1);
            long id=node.getId();
            props.put(NODE_ID, id);
            node.setProperty(NODE_ID, id);
            for(String key : validateMap.keySet()){
                Object value=validateMap.get(key);
                if(value!=null){
                    setValue(node, key, value);
                }
            }
            tx.success();
        }
        return node;
    }

    public Relationship createRelation(Node sNode, Node eNode, String rt, String name){
        GraphDatabaseService instance=getInstance();
        Relationship createRelationshipTo=null;
        // 开启事务
        try(Transaction tx=instance.beginTx()){
            createRelationshipTo=sNode.createRelationshipTo(eNode, RelationshipType.withName(rt));
            createRelationshipTo.setProperty(NAME, name);
            createRelationshipTo.setProperty("creatTime", DateTool.now());
            tx.success();
        }
        return createRelationshipTo;
    }

    public Relationship createRelation(Node sNode, Node eNode, String rt){
        GraphDatabaseService instance=getInstance();
        Relationship createRelationshipTo=null;
        // 开启事务
        try(Transaction tx=instance.beginTx()){

            Iterable<Relationship> relationships=sNode.getRelationships(RelationshipType.withName(rt),
                    Direction.OUTGOING);
            Map<String, String> nameSet=new HashMap<>();
            Object relationName=null;
            Iterator<Relationship> iterator=relationships.iterator();
            if(iterator.hasNext()){
                while(iterator.hasNext()){
                    Relationship ri=iterator.next();
                    long endNodeId=ri.getEndNodeId();
                    if(eNode.getId()==endNodeId){
                        return ri;
                    }
                    Map<String, Object> allProperties=ri.getAllProperties();
                    relationName=allProperties.get(NAME);
                    Object object=allProperties.get(LABEL);
                    if(relationName!=null&&object!=null){
                        nameSet.put(String.valueOf(object), String.valueOf(relationName));
                    }
                }
                if(!nameSet.isEmpty()){
                    relationName=nameSet.get(rt);
                }

                if(nameSet.isEmpty()){
                    // Perform DB operations
                    Object labelB=eNode.getLabels().iterator().next();
                    if(labelB!=null){
                        Node findNode=findNode(LABEL, String.valueOf(labelB), META_DATA);
                        if(findNode!=null){
                            relationName=findNode.getProperty(NAME);
                        }
                    }
                }
            }

            if(rt.startsWith("child")){
                relationName="子"+relationName;
            }
            createRelationshipTo=sNode.createRelationshipTo(eNode, RelationshipType.withName(rt));
            if(relationName==null){
                createRelationshipTo.setProperty(NAME, rt);
            }else{
                createRelationshipTo.setProperty(NAME, relationName);
            }

            createRelationshipTo.setProperty("creatTime", DateTool.now());
            // createRelationshipTo.setProperty(key, value);
            tx.success();
        }
        return createRelationshipTo;
    }

    public void createRelation(Long node, Long nodeB, String rt, Map<String, Object> propMap){


    }

    public Relationship createRelation(Node node, Node nodeB, String rt, Map<String, Object> propMap){
        return vtResult(()->{
            Relationship createRelationshipTo=null;
            if(node!=null&&null!=nodeB){
                GraphDatabaseService instance=getInstance();

                // 开启事务
                try(Transaction tx=instance.beginTx()){
                    // Perform DB operations
                    Iterable<Relationship> relationships=node.getRelationships(RelationshipType.withName(rt),
                            Direction.OUTGOING);
                    Iterator<Relationship> iterator=relationships.iterator();
                    while(iterator.hasNext()){
                        Relationship next=iterator.next();
                        if(next!=null){
                            long endNodeId=next.getEndNodeId();
                            if(nodeB.getId()==endNodeId){
                                updateRelProp(propMap, next);
                                return next;
                            }
                        }

                    }
                    createRelationshipTo=node.createRelationshipTo(nodeB, RelationshipType.withName(rt));
                    LoggerTool.info(logger, "create realtion:"+rt+"=="+name(propMap));
                    updateRelProp(propMap, createRelationshipTo);

                    tx.success();
                }
            }
            return createRelationshipTo;
        });

    }

    public void updateRelProp(Map<String, Object> propMap, Relationship createRelationshipTo){
        if(propMap!=null&&!propMap.isEmpty()&&createRelationshipTo!=null){
            for(Entry<String, Object> ei : propMap.entrySet()){
                if(ei.getValue()!=null){
                    createRelationshipTo.setProperty(ei.getKey(), ei.getValue());
                }
            }
            propMap.put(ID, createRelationshipTo.getId());
        }
    }

    public Node createRelations(Node node, List<Node> nodeBList, String rt){
        return vtResult(()->{
            GraphDatabaseService instance=getInstance();
            // 开启事务
            try(Transaction tx=instance.beginTx()){
                // Perform DB operations
                for(Node ni : nodeBList){
                    node.createRelationshipTo(ni, RelationshipType.withName(rt));
                }
                tx.success();
            }
            return node;
        });
    }

    /**
     * 关联开始和结束节点，并设置关系属性
     *
     * @param node
     * @param nodeBList
     * @param rt
     * @param relProp
     * @return
     */
    public Node createRelations(Node node, List<Node> nodeBList, String rt, Map<String, Object> relProp){
        return vtResult(()->{
            GraphDatabaseService instance=getInstance();
            // 开启事务
            try(Transaction tx=instance.beginTx()){
                // Perform DB operations
                for(Node ni : nodeBList){
                    Relationship createRelationshipTo=node.createRelationshipTo(ni, RelationshipType.withName(rt));
                    for(Entry<String, Object> ei : relProp.entrySet()){
                        createRelationshipTo.setProperty(ei.getKey(), ei.getValue());
                    }
                }
                tx.success();
            }
            return node;
        });
    }

    public JSONObject findJSONNode(String key, String value, String label){
        Node findNode=findNode(key, value, label);
        return propertiesJSON(findNode);
    }

    public Long getNodeId(String key, String value, String label){
        Node findNode=findNode(key, value, label);
        if(findNode==null){
            return null;
        }
        return findNode.getId();
    }

    public Node findNode(String key, String value, String label){
        if(label==null||key==null||value==null){
            return null;
        }
        GraphDatabaseService instance=getInstance();
        Node findNode=null;
        try(Transaction tx=instance.beginTx()){
            ResourceIterator<Node> findNodes=instance.findNodes(Label.label(label), key, value);
            if(findNodes.hasNext()){
                findNode=findNodes.next();
            }
            if(findNode==null){
                if(NODE_ID.equalsIgnoreCase(key)&&CommonUtil.isNumber(value)){
                    findNode=instance.getNodeById(Long.parseLong(value));
                }
            }
            tx.success();
        }
        return findNode;
    }

    public List<Map<String, Object>> queryBy(String key, String value, String label){
        if(label==null||key==null||value==null){
            return null;
        }

        return vtResult(()->{
            GraphDatabaseService instance=getInstance();
            List<Map<String, Object>> nodeMapList=new ArrayList<>();

            try(Transaction tx=instance.beginTx()){
                Node findNode=null;
                ResourceIterator<Node> findNodes=instance.findNodes(Label.label(label), key, value);
                while(findNodes.hasNext()){
                    findNode=findNodes.next();
                    if(findNode==null){
                        if(NODE_ID.equalsIgnoreCase(key)&&CommonUtil.isNumber(value)){
                            long parseLong=Long.parseLong(value);
                            findNode=instance.getNodeById(parseLong);
                        }
                    }
                    if(findNode!=null){
                        nodeMapList.add(findNode.getAllProperties());
                    }
                }
                tx.success();
            }
            return nodeMapList;
        });
    }

    public List<Map<String, Object>> queryBy(String key, Long value, String label){
        if(label==null||key==null||value==null){
            return null;
        }

        return vtResult(()->{
            GraphDatabaseService instance=getInstance();
            List<Map<String, Object>> nodeMapList=new ArrayList<>();

            try(Transaction tx=instance.beginTx()){
                Node findNode=null;
                ResourceIterator<Node> findNodes=instance.findNodes(Label.label(label), key, value);
                while(findNodes.hasNext()){
                    findNode=findNodes.next();
                    if(findNode==null){
                        if(NODE_ID.equalsIgnoreCase(key)){
                            findNode=instance.getNodeById(value);
                        }
                    }
                    if(findNode!=null){
                        nodeMapList.add(findNode.getAllProperties());
                    }
                }
                tx.success();
            }
            return nodeMapList;
        });
    }

    public Map<String, Object> findNodeAttMap(String key, String value, String label){
        Node findNode=findNode(key, value, label);
        if(findNode==null){
            return null;
        }
        return getNodeProperties(findNode);
    }

    public Node findNodeByName(String name, String label){
        return vtResult(()->{
            GraphDatabaseService instance=getInstance();
            Node findNode=null;
            try(Transaction tx=instance.beginTx()){
                ResourceIterator<Node> findNodes=instance.findNodes(Label.label(label), "name", name);
                if(findNodes.hasNext()){
                    findNode=findNodes.next();
                }
                tx.success();
            }
            return findNode;
        });
    }

    /**
     * c查找第一级树节点
     *
     * @param label
     * @return
     */
    public Map<String, Object> getTreeRootMap(String label){
        Node findNode=findNode("isRoot", "true", label);
        if(findNode==null){
            return null;
        }
        return vtResult(()->{
            Map<String, Object> rootMap=null;
            try(Transaction tx=getInstance().beginTx()){
                rootMap=findNode.getProperties(NODE_ID, "name");
                rootMap.put("open", true);
                childList(findNode, rootMap);
                tx.success();
            }
            return rootMap;
        });
    }

    public Map<String, Object> getALevelChildren(String label, Map<String, Object> queryMap){
        Object object=queryMap.get(NODE_ID);
        Node findNode=findNode(NODE_ID, String.valueOf(object), label);
        if(findNode==null){
            return null;
        }
        return vtResult(()->{
            Map<String, Object> rootMap=null;
            try(Transaction tx=getInstance().beginTx()){
                rootMap=findNode.getProperties(NODE_ID, "name");
                rootMap.put("open", true);
                childList(findNode, rootMap);
                tx.success();
            }
            return rootMap;
        });
    }

    @ServiceLog(description = "根据参数查询节点，并返回当前节点的子节点")
    public List<Map<String, Object>> getChildrenList(String label, Map<String, Object> queryMap){
        Object object=queryMap.get(NODE_ID);
        Node findNode=findNode(NODE_ID, String.valueOf(object), label);
        if(findNode==null){
            return null;
        }
        return vtResult(()->{
            try(Transaction tx=getInstance().beginTx()){
                List<Map<String, Object>> childList=getChildListByNode(findNode);
                tx.success();
                return childList;
            }
        });

    }

    public List<Map<String, Object>> getChildrenList(String label, String endLabel, Map<String, Object> queryMap){
        Object object=queryMap.get(NODE_ID);
        Node findNode=findNode(NODE_ID, String.valueOf(object), label);
        if(findNode==null){
            return null;
        }
        try(Transaction tx=getInstance().beginTx()){
            List<Map<String, Object>> childList=getChildListByNode(findNode);
            tx.success();
            return childList;
        }
    }

    public List<Map<String, Object>> childrenList(String label, Map<String, Object> queryMap){
        Object object=queryMap.get(NODE_ID);
        Node findNode=findNode(NODE_ID, String.valueOf(object), label);
        if(findNode==null){
            return null;
        }
        try(Transaction tx=getInstance().beginTx()){
            List<Map<String, Object>> childList=getChildPropListByNode(findNode);
            tx.success();
            return childList;
        }
    }

    /**
     * @return
     */
    public GraphDatabaseService getInstance(){
        if(graphDb==null){
            graphDb=new GraphDatabaseFactory().newEmbeddedDatabase(new File(dbPath));
            dbSpace.put("default", graphDb);
        }
        return graphDb;
    }

    public GraphDatabaseService getInstance(String dbId){
        if(dbSpace.containsKey(dbId)){
            return dbSpace.get(dbId);
        }else{
            Map<String, String> dataMap=new HashMap<>();
            List<Map<String, Object>> listData=listData(CRUD_DS_LABEL);

            for(Map<String, Object> dsi : listData){
                dataMap.put(String.valueOf(dsi.get(CRUD_DS_NAME)), String.valueOf(dsi.get(CRUD_DS_PATH)));
            }
            if(dataMap.containsKey(dbId)){
                GraphDatabaseService newEmbeddedDatabase=new GraphDatabaseFactory()
                        .newEmbeddedDatabase(new File(dataMap.get(dbId)));
                dbSpace.put(dbId, newEmbeddedDatabase);
                return newEmbeddedDatabase;
            }else{
                return null;
            }
        }
    }

    public Node getNodeById(Long id){
        Callable<Node> cal=()->{
            GraphDatabaseService instance=getInstance();
            Node nodeById=null;
            try(Transaction tx=instance.beginTx()){
                try{
                    nodeById=instance.getNodeById(id);
                }catch(Exception e){
                    nodeById=null;
                }

                tx.success();
            }
            return nodeById;
        };
        return vtResult(cal);
    }

    public Relationship getRelationById(Long id){
        Callable<Relationship> cal=()->{
            GraphDatabaseService instance=getInstance();
            Relationship relById=null;
            try(Transaction tx=instance.beginTx()){
                try{
                    relById=instance.getRelationshipById(id);
                }catch(Exception e){
                    relById=null;
                }

                tx.success();
            }
            return relById;
        };
        return vtResult(cal);
    }

    public String getNodeLabelById(Long id){
        Callable<String> cal=()->{
            GraphDatabaseService instance=getInstance();
            String label=null;
            // 开启事务
            try(Transaction tx=instance.beginTx()){
                Node node=instance.getNodeById(id);
                if(node==null){
                    return null;
                }
                Iterator<Label> iterator=node.getLabels().iterator();
                label=iterator.next().name();
                tx.success();
            }
            return label;
        };
        return vtResult(cal);
    }

    public JSONObject propertiesJSON(Node node){
        Callable<JSONObject> cal=()->{
            GraphDatabaseService instance=getInstance();
            JSONObject jsonObjectFrom=null;
            // 开启事务
            try(Transaction tx=instance.beginTx()){
                // Perform DB operations
                jsonObjectFrom=JSONMapUtil.jsonObject(node.getAllProperties());
                tx.success();
            }
            return jsonObjectFrom;
        };
        return vtResult(cal);
    }

    public Map<String, Object> getNodePropertiesById(Long id){
        if(id==null){
            return null;
        }
        Callable<Map<String, Object>> cal=()->{
            GraphDatabaseService instance=getInstance();
            Map<String, Object> nodeMap=null;
            // 开启事务
            try(Transaction tx=instance.beginTx()){
                Node node=instance.getNodeById(id);
                // Perform DB operations
                if(node==null){
                    return nodeMap;
                }
                nodeMap=node.getAllProperties();
                Iterable<Label> labels=node.getLabels();

                Iterator<Label> iterator=labels.iterator();
                List<String> ls=new ArrayList<>();
                while(iterator.hasNext()){
                    Label x=iterator.next();
                    ls.add(x.name());
                }

                nodeMap.put("Mark-label", ls);
                nodeMap.put("id", node.getId());
                tx.success();
            }
            return nodeMap;
        };
        return vtResult(cal);
    }

    public void changeLabelById(Long id, String label){
        Callable<Map<String, Object>> cal=()->{
            GraphDatabaseService instance=getInstance();
            Map<String, Object> nodeMap=null;
            // 开启事务
            try(Transaction tx=instance.beginTx()){
                Node node=instance.getNodeById(id);
                Label next=node.getLabels().iterator().next();
                node.removeLabel(next);
                node.addLabel(Label.label(label));

                tx.success();
            }
            return nodeMap;
        };
        vtResult(cal);
    }

    public Map<String, Object> getLabelAndPropertiesById(Long id){
        Callable<Map<String, Object>> cal=()->{
            GraphDatabaseService instance=getInstance();
            Map<String, Object> nodeMap=null;
            // 开启事务
            try(Transaction tx=instance.beginTx()){
                Node node=instance.getNodeById(id);
                // Perform DB operations
                if(node==null){
                    return nodeMap;
                }
                nodeMap=node.getAllProperties();
                nodeMap.put("id", node.getId());
                Iterator<Label> iterator=node.getLabels().iterator();
                List<String> laList=new ArrayList<>();
                while(iterator.hasNext()){
                    laList.add(iterator.next().name());
                }
                if(label(nodeMap)==null){
                    if(laList.size()==1&&!META_DATA.equals(laList.get(0))){
                        nodeMap.put(LABEL, laList.get(0));
                    }
                }
                tx.success();
            }
            return nodeMap;
        };
        return vtResult(cal);
    }

    public Map<String, Object> getNodePropertiesWithLabelById(Long id){
        Callable<Map<String, Object>> cal=()->{
            GraphDatabaseService instance=getInstance();
            Map<String, Object> nodeMap=null;
            // 开启事务
            try(Transaction tx=instance.beginTx()){
                Node node=instance.getNodeById(id);
                // Perform DB operations
                nodeMap=node.getAllProperties();
                nodeMap.put("id", node.getId());
                Iterator<Label> iterator=node.getLabels().iterator();
                List<String> labList=new ArrayList<>();
                while(iterator.hasNext()){
                    labList.add(iterator.next().name());
                }
                String join=String.join(",", labList);
                nodeMap.put(LABEL, join);
                tx.success();
            }
            return nodeMap;
        };
        return vtResult(cal);
    }

    /**
     * 获取节点的属性值
     *
     * @param id
     * @param key
     * @return
     */
    public Object getNodePropValueById(Long id, String key){
        Callable<Object> cal=()->{
            GraphDatabaseService instance=getInstance();
            Object value=null;
            // 开启事务
            try(Transaction tx=instance.beginTx()){
                Node node=instance.getNodeById(id);
                if(key.equals(LABEL)){
                    value=node.getLabels().iterator().next().name();
                }else{
                    value=node.getProperty(key);
                }
                tx.success();
            }
            return value;
        };
        return vtResult(cal);
    }

    public Map<String, Object> getNodeProperties(Node node){
        Callable<Map<String, Object>> cal=()->{
            GraphDatabaseService instance=getInstance();
            Map<String, Object> nodeMap=null;
            // 开启事务
            try(Transaction tx=instance.beginTx()){
                // Perform DB operations
                nodeMap=node.getAllProperties();
                if(!nodeMap.containsKey(NODE_ID)){
                    node.setProperty(NODE_ID, node.getId());
                    nodeMap.put(NODE_ID, node.getId());
                }

                tx.success();
            }
            return nodeMap;
        };
        return vtResult(cal);
    }

    /**
     * 获取所有的关系
     *
     * @param propMap
     * @param label
     * @return
     */
    public List<Map<String, Object>> getAllOutgoings(Map<String, Object> propMap, String label){
        Node startNode=queryNode(propMap, label);
        return getOutingRelationOf(startNode);
    }

    public List<Map<String, Object>> getOutgoings(String nodeId){
        Node startNode=getNodeById(Long.valueOf(nodeId));
        return getOutingRelationOf(startNode);
    }

    public List<Map<String, Object>> getOutgoings(Long nodeId){
        Node startNode=getNodeById(nodeId);
        return getOutingRelationOf(startNode);
    }

    private List<Map<String, Object>> getOutingRelationOf(Node startNode){
        List<Map<String, Object>> relationshipList=new ArrayList<>();
        if(startNode==null){
            return relationshipList;
        }
        try(Transaction tx=getInstance().beginTx()){
            Iterable<Relationship> relationships=startNode.getRelationships(Direction.OUTGOING);
            handleRelationData(relationshipList, relationships);
            tx.success();
        }
        return relationshipList;
    }

    private List<Map<String, Object>> getIningOf(Node startNode){
        List<Map<String, Object>> relationshipList=new ArrayList<>();
        if(startNode==null){
            return relationshipList;
        }
        return vtResult(()->{
            try(Transaction tx=getInstance().beginTx()){
                Iterable<Relationship> relationships=startNode.getRelationships(Direction.INCOMING);
                handleRelationData(relationshipList, relationships);
                tx.success();
            }
            return relationshipList;
        });
    }

    public List<Map<String, Object>> getAllDefineRelationList(Map<String, Object> propMap, String label){
        Node startNode=queryNode(propMap, label);

        List<Map<String, Object>> relationshipList=new ArrayList<>();
        if(startNode==null){
            return relationshipList;
        }
        try(Transaction tx=getInstance().beginTx()){
            Iterable<Relationship> relationships=startNode.getRelationships(Direction.OUTGOING);
            handleDefineRelation(relationshipList, relationships);
            tx.success();
        }
        return relationshipList;
    }

    public List<Map<String, Object>> allOutRelation(String nodeId){
        return allOutRelation(getNodeById(Long.valueOf(nodeId)));
    }

    public List<Map<String, Object>> allOutRelation(Long nodeId){
        return allOutRelation(getNodeById(nodeId));
    }

    public List<Map<String, Object>> allOutRelationData(Long nodeId){
        Node nodeById=getNodeById(nodeId);
        if(nodeById==null){
            return new ArrayList<>();
        }
        return allOutRelationData(nodeById);
    }

    public List<Map<String, Object>> allInRelation(Long nodeId){
        return allInRelation(getNodeById(nodeId));
    }

    public List<Map<String, Object>> allInRelation(Node startNode){
        List<Map<String, Object>> relationshipList=new ArrayList<>();
        try(Transaction tx=getInstance().beginTx()){
            Iterable<Relationship> relationships=startNode.getRelationships(Direction.INCOMING);
            handleDefineRelation(relationshipList, relationships);
            tx.success();
        }
        return relationshipList;
    }

    public List<Map<String, Object>> allOutRelation(Node startNode){
        List<Map<String, Object>> relationshipList=new ArrayList<>();
        try(Transaction tx=getInstance().beginTx()){
            Iterable<Relationship> relationships=startNode.getRelationships(Direction.OUTGOING);
            handleDefineRelation(relationshipList, relationships);
            tx.success();
        }
        return relationshipList;
    }

    public List<Map<String, Object>> allOutRelationData(Node startNode){
        List<Map<String, Object>> relationshipList=new ArrayList<>();
        try(Transaction tx=getInstance().beginTx()){
            Iterable<Relationship> relationships=startNode.getRelationships(Direction.OUTGOING);
            onlyRelationData(relationshipList, relationships);
            tx.success();
        }
        return relationshipList;
    }

    public String getRelationName(Map<String, Object> propMap, String label, String reLabel){
        Node startNode=queryNode(propMap, label);

        if(startNode==null){
            return "未定义";
        }
        String name=null;
        try(Transaction tx=getInstance().beginTx()){
            Iterable<Relationship> relationships=startNode.getRelationships(Direction.OUTGOING);
            List<String> handleRelationName=handleRelationName(relationships, reLabel);
            if(handleRelationName!=null&&!handleRelationName.isEmpty()){
                name=handleRelationName.get(0);
            }
            tx.success();
        }
        return name;
    }

    public List<String> getOutgoingsLabel(Map<String, Object> propMap, String label){
        Node startNode=queryNode(propMap, label);

        List<String> relationshipList=new ArrayList<>();
        if(startNode==null){
            return relationshipList;
        }
        Callable<List<String>> cal=()->{
            List<String> data=new ArrayList<>();
            try(Transaction tx=getInstance().beginTx()){
                Iterable<Relationship> relationships=startNode.getRelationships(Direction.OUTGOING);
                relationships.forEach(p->data.add(p.getType().name()));
                tx.success();
            }
            return data;
        };
        return vtResult(cal);
    }

    /**
     * 获取某一类关系
     *
     * @param propMap
     * @param label
     * @param relationLabel
     * @return
     */
    public List<Map<String, Object>> getOneTypeOutgoings(Map<String, Object> propMap, String label,
                                                         String relationLabel){
        Node startNode=queryNode(propMap, label);

        return getOneRelationOf(relationLabel, startNode);
    }

    public List<Map<String, Object>> getOneTypeOutgoings(Long startId, String relationLabel){
        Node startNode=getNodeById(startId);
        return getOneRelationOf(relationLabel, startNode);
    }

    private List<Map<String, Object>> getOneRelationOf(String relationLabel, Node startNode){
        List<Map<String, Object>> relationshipList=new ArrayList<>();
        if(startNode==null){
            return relationshipList;
        }

        Callable<List<Map<String, Object>>> cal=()->{
            try(Transaction tx=getInstance().beginTx()){
                Iterable<Relationship> relationships=startNode
                        .getRelationships(RelationshipType.withName(relationLabel), Direction.OUTGOING);
                handleRelationData(relationshipList, relationships);
                tx.success();
            }
            return relationshipList;
        };
        return vtResult(cal);
    }

    public List<Long> getEndIdsOf(String relationLabel, Long startId){
        List<Long> relationshipList=new ArrayList<>();
        if(startId==null){
            return relationshipList;
        }
        List<Map<String, Object>> queryData=getEndNodesBy(relationLabel, startId);
        return ids(queryData);
    }

    public List<String> getEndIdsOf(String relationLabel, Long startId, String endLabel){
        StringBuilder sb=new StringBuilder();
        sb.append("match(s)-[r:"+relationLabel+"]->(e) where id(s)="+startId+" return id(e) AS id");
        List<Map<String, Object>> queryData=queryData(sb.toString());
        return idStrList(queryData);
    }

    /**
     * 获取关系结束节点数据列表
     *
     * @param relationLabel
     * @param startId
     * @return
     */
    public List<Map<String, Object>> getEndNodesBy(String relationLabel, Long startId){
        StringBuilder sb=new StringBuilder();
        sb.append("match(s)-[r:"+relationLabel+"]->(e) where id(s)="+startId+" return e");
        List<Map<String, Object>> queryData=queryData(sb.toString());
        return queryData;
    }

    /**
     * 获取一个关系的数据。
     *
     * @param propMap
     * @param startLabel
     * @param endlabel
     * @return
     */
    public List<Map<String, Object>> getOneDataList(Map<String, Object> propMap, String startLabel, String endlabel){
        Node startNode=queryNode(propMap, startLabel);

        List<Map<String, Object>> relationshipList=new ArrayList<>();
        if(startNode==null){
            return relationshipList;
        }
        Callable<List<Map<String, Object>>> cal=()->{
            try(Transaction tx=getInstance().beginTx()){
                Iterable<Relationship> relationships=startNode.getRelationships(Direction.OUTGOING);
                getRelationData(relationshipList, relationships, endlabel);
                tx.success();
            }
            return relationshipList;
        };
        return vtResult(cal);
    }

    /**
     * 获取所有入关系
     *
     * @param propMap
     * @param label
     * @return
     */
    public List<Map<String, Object>> getIncomings(Map<String, Object> propMap, String label){
        Node startNode=queryNode(propMap, label);

        List<Map<String, Object>> relationshipList=new ArrayList<>();
        if(startNode==null){
            return relationshipList;
        }
        try(Transaction tx=getInstance().beginTx()){
            Iterable<Relationship> relationships=startNode.getRelationships(Direction.INCOMING);
            handleRelationData(relationshipList, relationships);
            tx.success();
        }
        return relationshipList;
    }

    public List<Map<String, Object>> getOutcomings(Map<String, Object> propMap, String label){
        Node startNode=queryNode(propMap, label);

        List<Map<String, Object>> relationshipList=new ArrayList<>();
        try(Transaction tx=getInstance().beginTx()){
            Iterable<Relationship> relationships=startNode.getRelationships(Direction.OUTGOING);
            handleRelationData(relationshipList, relationships);
            tx.success();
        }
        return relationshipList;
    }

    private void getRelationData(List<Map<String, Object>> relationshipList, Iterable<Relationship> relationships,
                                 String endLabel){
        Iterator<Relationship> iterator=relationships.iterator();
        Boolean findEnd=false;
        while(iterator.hasNext()&&!findEnd){
            Relationship ri=iterator.next();
            Map<String, Object> reMap=new HashMap<>();
            endNodeInfoOfRelation(ri, reMap);
            Object object=reMap.get(RELATION_ENDNODE_LABEL);
            if(null!=object){
                List<String> labelList=(List<String>) object;
                if(labelList.contains(endLabel)){
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
    private void handleRelationData(List<Map<String, Object>> relationshipList, Iterable<Relationship> relationships){
        Iterator<Relationship> iterator=relationships.iterator();
        while(iterator.hasNext()){
            Relationship ri=iterator.next();
            Map<String, Object> reMap=new HashMap<>();
            relationInfo(ri, reMap);
            endNodeInfoOfRelation(ri, reMap);
            relationshipList.add(reMap);
        }
    }

    private List<String> handleRelationName(Iterable<Relationship> relationships, String reLabel){
        Iterator<Relationship> iterator=relationships.iterator();
        List<String> nameList=new ArrayList<>();
        while(iterator.hasNext()){
            Relationship ri=iterator.next();
            Object relationName=relationName(ri, reLabel);
            if(relationName!=null){
                nameList.add(String.valueOf(relationName));
            }
        }
        return nameList;
    }

    private void handleDefineRelation(List<Map<String, Object>> relationshipList,
                                      Iterable<Relationship> relationships){
        Iterator<Relationship> iterator=relationships.iterator();
        while(iterator.hasNext()){
            Relationship ri=iterator.next();
            Map<String, Object> reMap=new HashMap<>();
            relationInfo(ri, reMap);
            endNodeInfoOfRelation(ri, reMap);
            startNodeInfoOfRelation(ri, reMap);
            relationshipList.add(reMap);
        }
    }

    private void onlyRelationData(List<Map<String, Object>> relationshipList, Iterable<Relationship> relationships){
        Iterator<Relationship> iterator=relationships.iterator();
        while(iterator.hasNext()){
            Relationship ri=iterator.next();
            relationshipList.add(relationData(ri));
        }
    }

    private Object relationName(Relationship ri, String reLabel){
        Map<String, Object> riProperties=ri.getAllProperties();
        RelationshipType type=ri.getType();
        if(reLabel.equals(type.name())){
            return riProperties.get("name");
        }
        return null;
    }

    private void relationInfo(Relationship ri, Map<String, Object> reMap){
        Map<String, Object> riProperties=ri.getAllProperties();
        RelationshipType type=ri.getType();
        reMap.put(RELATION_TYPE, type.name());
        reMap.put(RELATION_PROP, riProperties);
    }

    private Map<String, Object> relationData(Relationship ri){
        Map<String, Object> reMap=new HashMap<>();
        Map<String, Object> riProperties=ri.getAllProperties();
        RelationshipType type=ri.getType();
        reMap.put(RELATION_TYPE, type.name());
        reMap.put(RELATION_PROP, riProperties);
        reMap.put(RELATION_START_ID, ri.getStartNodeId());
        reMap.put(RELATION_END_ID, ri.getEndNodeId());
        return reMap;
    }

    public void endNodeInfoOfRelation(Relationship ri, Map<String, Object> reMap){
        Node endNode=ri.getEndNode();
        Map<String, Object> endNodeProperties=endNode.getAllProperties();
        if(!endNodeProperties.containsKey("id")){
            endNodeProperties.put("id", endNode.getId());
        }
        Iterable<Label> labels=endNode.getLabels();
        List<String> labelList=new ArrayList<>();
        labels.forEach(p->labelList.add(p.name()));
        reMap.put(RELATION_ENDNODE_LABEL, labelList);
        reMap.put(RELATION_ENDNODE_PROP, endNodeProperties);
    }

    public void startNodeInfoOfRelation(Relationship ri, Map<String, Object> reMap){
        Node endNode=ri.getStartNode();
        Map<String, Object> startNodeProperties=endNode.getAllProperties();
        if(!startNodeProperties.containsKey("id")){
            startNodeProperties.put("id", endNode.getId());
        }
        Iterable<Label> labels=endNode.getLabels();
        List<String> labelList=new ArrayList<>();
        labels.forEach(p->labelList.add(p.name()));
        reMap.put(RELATION_STARTNODE_LABEL, labelList);
        reMap.put(RELATION_STARTNODE_PROP, startNodeProperties);
    }

    public Map<String, Object> getRelEndNodePropties(Map<String, Object> reMap){
        return (Map<String, Object>) reMap.get(RELATION_ENDNODE_PROP);
    }

    public List<String> getRelEndNodeLabel(Map<String, Object> reMap){
        return (List<String>) reMap.get(RELATION_ENDNODE_LABEL);
    }

    public List<Map<String, Object>> getTheRelation(Map<String, Object> propMap, String label, String relationLabel){
        Node startNode=queryNode(propMap, label);
        Callable<List<Map<String, Object>>> cal=()->{
            List<Map<String, Object>> relationshipList=new ArrayList<>();
            try(Transaction tx=getInstance().beginTx()){
                Iterable<Relationship> relationship=startNode
                        .getRelationships(RelationshipType.withName(relationLabel), Direction.OUTGOING);
                Iterator<Relationship> iterator=relationship.iterator();
                while(iterator.hasNext()){
                    Relationship ri=iterator.next();
                    // Map<String, Object> reMap = new HashMap<>();
                    Node endNode=ri.getEndNode();
                    Map<String, Object> endNodeProperties=endNode.getAllProperties();
                    relationshipList.add(endNodeProperties);
                }
                tx.success();
            }
            return relationshipList;
        };
        return vtResult(cal);
    }

    public Boolean delRelation(String startId, String endId, String startLabel, String relationLabel){
        Callable<Boolean> cal=()->{
            Node startDefine=findNode(LABEL, startLabel, META_DATA);
            Boolean delBoolean=false;
            try(Transaction tx=getInstance().beginTx()){// 清理Po定义关系
                Iterable<Relationship> relationship=startDefine
                        .getRelationships(RelationshipType.withName(relationLabel));
                Iterator<Relationship> iterator=relationship.iterator();
                delBoolean=removeRelations(endId, iterator);
                tx.success();
            }
            if(startId!=null){// 清理实例关系数据
                Node startNode=findNode(NODE_ID, startId, startLabel);
                try(Transaction tx=getInstance().beginTx()){
                    Iterable<Relationship> relationship=startNode
                            .getRelationships(RelationshipType.withName(relationLabel));
                    Iterator<Relationship> iterator=relationship.iterator();
                    delBoolean=removeRelations(endId, iterator);
                    String jsonString=JSON.toJSONString(startNode.getAllProperties());
                    tx.success();
                }
            }

            return delBoolean;
        };

        return vtResult(cal);
    }

    public Boolean delRelation(String startId, String endId, String relationLabel){

        Node startNode=getNodeById(Long.valueOf(startId));
        // Node endNode = getNodeById(Long.valueOf(endId));
        Boolean delBoolean=false;
        try(Transaction tx=getInstance().beginTx()){// 清理Po定义关系
            Iterable<Relationship> relationship=null;

            if(relationLabel!=null){
                relationship=startNode.getRelationships(RelationshipType.withName(relationLabel));
            }else{
                relationship=startNode.getRelationships();
            }

            Iterator<Relationship> iterator=relationship.iterator();
            delBoolean=removeRelations(endId, iterator);
            String jsonString=JSON.toJSONString(startNode.getAllProperties());

            tx.success();
        }
        if(startId!=null){// 清理实例关系数据
            try(Transaction tx=getInstance().beginTx()){
                Iterable<Relationship> relationship=startNode
                        .getRelationships(RelationshipType.withName(relationLabel));
                Iterator<Relationship> iterator=relationship.iterator();
                delBoolean=removeRelations(endId, iterator);
                String jsonString=JSON.toJSONString(startNode.getAllProperties());
                Iterable<Label> labels=startNode.getLabels();
                tx.success();
            }
        }

        return delBoolean;
    }

    /**
     * 删除关系
     *
     * @param startId
     * @param relationLabel
     * @return
     */
    public Boolean delRelation(Long startId, String relationLabel){
        if(startId==null){
            return false;
        }
        if(relationLabel==null||"".equals(relationLabel)){
            return false;
        }
        Node startNode=getNodeById(startId);
        if(startNode==null){
            return false;
        }
        excuteCypher("match(n)-[r:"+relationLabel+"]->(m) where id(n)="+startId+" delete r");
        return true;
    }

    public Boolean delRelation(Long startId, Long endId, String relationLabel){
        if(startId==null){
            return false;
        }
        if(endId==null){
            return false;
        }
        if(relationLabel==null||"".equals(relationLabel)){
            return false;
        }
        Node startNode=getNodeById(startId);
        if(startNode==null){
            return false;
        }
        Node endNode=getNodeById(endId);
        if(endNode==null){
            return false;
        }
        List<Map<String, Object>> maps=queryCypher("match(n)-[r]-(m) where id(n)="+startId+" and id(m)="+endId+" return properties(r) as relation");
        if(maps.size()>0){
//            for(Map<String, Object> map:maps){
//                System.out.println(map);
//            }
            excuteCypher("match(n)-[r:"+relationLabel+"]->(m) where id(n)="+startId+" and id(m)="+endId+"  DETACH  delete r");
        }
        return true;
    }


    private Boolean removeRelations(String endId, Iterator<Relationship> iterator){
        Relationship ri=null;
        Boolean delBoolean=false;
        while(iterator.hasNext()){
            ri=iterator.next();
            if(endId!=null){
                Node endNode=ri.getEndNode();
                if(endId.equals(String.valueOf(endNode.getId()))){
                    delBoolean=true;
                    break;
                }else{
                    Map<String, Object> endNodeProperties=endNode.getAllProperties();
                    if(endId.equals(endNodeProperties.get(NODE_ID))){
                        delBoolean=true;
                    }
                }
            }else{
                delBoolean=true;
            }
            if(delBoolean&&ri!=null){
                ri.delete();
                ri=null;
            }
        }
        if(delBoolean&&ri!=null){
            ri.delete();
        }

        return delBoolean;
    }

    public JSONArray getRelation(String query){
        JSONArray relationTree=new JSONArray();
        List<Map<String, Object>> relations=queryData(query);
        Map<Long, JSONObject> retMap=new HashMap<>();
        Map<Long, Set<Long>> relationMap=new HashMap<>();
        for(Map<String, Object> ri : relations){
            Node m=(Node) ri.get("m");
            Node n=(Node) ri.get("r");
            long startId=n.getId();
            long endId=m.getId();
            if(!relationMap.containsKey(startId)){
                Set<Long> objects=new HashSet<>();
                objects.add(endId);
                relationMap.put(startId, objects);
            }else{
                relationMap.get(startId).add(endId);
            }
            retMap.put(startId, propertiesJSON(n));
            retMap.put(endId, propertiesJSON(m));
        }
        for(Entry<Long, Set<Long>> ei : relationMap.entrySet()){
            Long pid=ei.getKey();
            JSONObject jsonObject=retMap.get(pid);
            JSONArray childs=new JSONArray();
            Set<Long> value=ei.getValue();
            for(Long cid : value){
                childs.add(retMap.get(cid));
            }
            jsonObject.put("childs", childs);
            relationTree.add(jsonObject);
        }
        return relationTree;
    }

    public Map<String, Object> getWholeTree(String label){
        LoggerTool.info(logger, "=======getWholeTree==========={}==", label);
        Node findNode=findNode(IS_TREE_ROOT, "true", label);
        if(findNode==null){
            LoggerTool.info(logger, "=======findNode is null ===============");
            return null;
        }
        Set<Long> childIds=new HashSet<>();

        return vtResult(()->{
            Map<String, Object> data=null;
            try(Transaction tx=getInstance().beginTx()){
                data=findNode.getProperties(NODE_ID, "name");
                data.put("open", true);
                data.put(ID, findNode.getId());
                children(label, data, childIds);
                tx.success();
            }
            return data;
        });
    }

    public Map<String, Object> getWholeTree(String label, String rootName){
        LoggerTool.info(logger, "=======getWholeTree==========={}={}=", label, rootName);
        Map<String, Object> rootMap=null;
        Node findNode=findNode(IS_TREE_ROOT, "true", label);
        if(findNode==null){

            List<Map<String, Object>> topNode=queryCypher(
                    "match (a:"+label+") where a.parentId is null return id(a) AS id,a.name AS name");
            childNode(label, topNode);

            if(topNode.size()==1){
                rootMap=topNode.get(0);
            }else{
                rootMap=new HashMap<>();
                rootMap.put(ID, "0");
                rootMap.put(NAME, rootName);
                rootMap.put(REL_TYPE_CHILDREN, topNode);
            }
            rootMap.put("open", true);
            return rootMap;
        }
        Set<Long> childIds=new HashSet<>();
        return vtResult(()->{
            Map<String, Object> data=null;
            try(Transaction tx=getInstance().beginTx()){
                data=findNode.getProperties(NODE_ID, "name");
                data.put("open", true);
                children(label, data, childIds);
                tx.success();
            }
            return data;
        });
    }

    private List<Map<String, Object>> childNode(String label, List<Map<String, Object>> topNode){
        List<Map<String, Object>> nodes=new ArrayList<>(topNode.size());
        for(Map<String, Object> ci : topNode){
            Map<String, Object> newCi=new HashMap<>();
            List<Map<String, Object>> child=queryCypher("match (a:"+label+") where a.parentId =\""
                    +string(ci, ID)+"\" return id(a) AS id,a.name AS name");
            if(child!=null&&!child.isEmpty()){
                List<Map<String, Object>> childNode=childNode(label, child);
                newCi.put(REL_TYPE_CHILDREN, childNode);
            }
            newCi.putAll(ci);
            nodes.add(newCi);
        }
        return nodes;
    }

    public Map<String, Object> getWholeTreeWithColumn(String label, String[] columns){
        Node rootNode=findNode("isRoot", "true", label);
        if(rootNode==null){
            return null;
        }
        return vtResult(()->{
            Map<String, Object> rootMap=null;
            try(Transaction tx=getInstance().beginTx()){
                rootMap=rootNode.getProperties(columns);
                rootMap.put("open", true);
                childrenWithColumns(rootNode, rootMap, columns);
                tx.success();
            }
            return rootMap;
        });
    }


    public Map<String, Object> getTreeByDefine(String label, Map<String, Object> one2, String[] columns){

        String parentIdField="parentId";
        if(one2!=null){
            String parent=string(one2, "parentIdField");
            if(parent==null){
                parentIdField=string(one2, "parentField");
            }else{
                parentIdField=parent;
            }
            Node rootNode=findNode("isRoot", "true", label);
            if(rootNode==null){
                List<Map<String, Object>> oneLevelChild=queryData("Match(n:"+label+") where n."+parentIdField+" is null or n."+parentIdField+"='null' or n."+parentIdField+"='' return n");
                if(oneLevelChild==null||oneLevelChild.isEmpty()){
                    return null;
                }
                subTree(label, one2, columns, oneLevelChild);
                Map<String, Object> topLevel=newMap();
                topLevel.put(REL_TYPE_CHILDREN, oneLevelChild);
                return topLevel;

            }
            return vtResult(()->{
                Map<String, Object> rootMap=null;
                try(Transaction tx=getInstance().beginTx()){
                    rootMap=rootNode.getProperties(columns);
                    rootMap.put("open", true);
//			childrenWithColumns(rootMap,parentIdField, columns);
                    subTreeOf(label, one2, columns, rootMap);
                    tx.success();
                }
                return rootMap;
            });
        }else{
            Node rootNode=findNode("isRoot", "true", label);
            if(rootNode==null){
                List<Map<String, Object>> oneLevelChild=queryData("Match(n:"+label+") where  n."+parentIdField+" is null or n."+parentIdField+"='null' or n."+parentIdField+"='' return n");
                if(oneLevelChild==null||oneLevelChild.isEmpty()){
                    return null;
                }
                subTree(label, one2, columns, oneLevelChild);
                Map<String, Object> topLevel=newMap();
                topLevel.put(REL_TYPE_CHILDREN, oneLevelChild);
                return topLevel;
            }
            return vtResult(()->{
                Map<String, Object> rootMap=null;
                try(Transaction tx=getInstance().beginTx()){
                    rootMap=rootNode.getProperties(columns);
                    rootMap.put("open", true);
//			childrenWithColumns(rootMap,parentIdField, columns);
                    subTreeOf(label, one2, columns, rootMap);
                    tx.success();
                }
                return rootMap;
            });
        }

    }

    public void subTree(String label, Map<String, Object> treeInfo, String[] columns,
                        List<Map<String, Object>> oneLevelChild){
        for(Map<String, Object> qi : oneLevelChild){
            subTreeOf(label, treeInfo, columns, qi);
//	    if(childs!=null&&!childs.isEmpty()) {
//		for(Map<String, Object> ci:childs) {
//		  //查关系
//		    subRelation(qi,treeInfo, columns);
//		    subData(label,treeInfo, ci);
//		    }
//	    }


        }
    }

    public void subTreeOf(String label, Map<String, Object> treeInfo, String[] columns, Map<String, Object> qi){
        try(Transaction tx=getInstance().beginTx()){
            qi.put("open", true);
            // 查关系
            subRelation(qi, treeInfo, columns);
            // 查字段
            Set<String> childIds=new HashSet<>();
            subData(label, treeInfo, qi, childIds);
            tx.success();
        }
    }

    public void excuteCypher(String query){
        exec.submit(()->{
            GraphDatabaseService instance=getInstance();
            Map<String, Object> parameters=new HashMap<String, Object>();
            try(Transaction beginTx=instance.beginTx()){
                Result result=instance.execute(query, parameters);
                while(result.hasNext()){
                    Map<String, Object> row=result.next();
                    for(String key : result.columns()){
                        System.out.printf("%s = %s%n", key, row.get(key));
                    }
                }

                beginTx.success();
            }
        });

    }

    public List<Map<String, Object>> excuteCypher(String query, Map<String, Object> parameters){
        return vtResult(()->{
            GraphDatabaseService instance=getInstance();
            List<Map<String, Object>> data=new ArrayList<>();
            try(Transaction beginTx=instance.beginTx()){
                Result result=instance.execute(query, parameters);
                while(result.hasNext()){
                    Map<String, Object> row=result.next();
                    if(!row.isEmpty()){
                        data.add(row);
                    }
                }
                beginTx.success();
            }
            return data;
        });
    }

    public List<Map<String, Object>> queryCypher(String query, Map<String, Object> param){
        // Callable<List<Map<String, Object>>> cal=;
        return vtResult(()->{
            GraphDatabaseService instance=getInstance();
            List<Map<String, Object>> data=new ArrayList<>();
            try(Transaction beginTx=instance.beginTx()){
                Result result=instance.execute(query, param);
                while(result.hasNext()){
                    Map<String, Object> row=result.next();
                    if(!row.isEmpty()){
                        data.add(row);
                    }
                }
                beginTx.success();
                beginTx.close();
            }
            return data;
        });
    }

    public List<Map<String, Object>> queryCypher(String query){
        // Callable<List<Map<String, Object>>> cal=;
        return vtResult(()->{
            GraphDatabaseService instance=getInstance();
            List<Map<String, Object>> data=new ArrayList<>();
            try(Transaction beginTx=instance.beginTx()){
                Result result=instance.execute(query);
                while(result.hasNext()){
                    Map<String, Object> row=result.next();
                    if(!row.isEmpty()){
                        data.add(row);
                    }
                }
                beginTx.success();
                beginTx.close();
            }
            return data;
        });
    }

    public List<Map<String, Object>> queryCypherPah(String query){
        // Callable<List<Map<String, Object>>> cal=;
        return vtResult(()->{
            GraphDatabaseService instance=getInstance();
            List<Map<String, Object>> data=new ArrayList<>();
            try(Transaction beginTx=instance.beginTx()){
                Result result=instance.execute(query);
                while(result.hasNext()){
                    Map<String, Object> row=result.next();
                    if(!row.isEmpty()){
                        data.add(row);
                    }
                }
                beginTx.success();
            }
            return data;
        });
    }

    public List<Map<String, Object>> queryData(String query){
        return vtResult(()->{
            GraphDatabaseService instance=getInstance();
            List<Map<String, Object>> data=new ArrayList<>();
            try(Transaction beginTx=instance.beginTx()){
                Result result=instance.execute(query);
                while(result.hasNext()){
                    Map<String, Object> rowi=new HashMap<>();
                    Map<String, Object> row=result.next();
                    int i=0;
                    for(Entry<String, Object> ei : row.entrySet()){
                        i++;
                        String key=ei.getKey();
                        Object value=ei.getValue();
                        if(value instanceof Node retNode){
                            Map<String, Object> allProperties=retNode.getAllProperties();
                            Iterable<Label> labels=retNode.getLabels();
                            List<String> labeList=new ArrayList<>();
                            boolean isMetaData=false;
                            for(Label li : labels){
                                if(META_DATA.equals(li.name())){
                                    isMetaData=true;
                                }
                                labeList.add(li.name());
                            }

                            if(!isMetaData&&!allProperties.containsKey(LABEL)){
                                allProperties.put(LABEL, String.join(",", labeList));
                            }

                            rowi.putAll(allProperties);
                        }else if(value instanceof Relationship retNode){

                            Map<String, Object> allProperties=retNode.getAllProperties();
                            String relName=retNode.getType().name();
                            List<String> labeList=new ArrayList<>();
                            boolean isMetaData=false;

                            if(!isMetaData&&!allProperties.containsKey(LABEL)){
                                allProperties.put(LABEL, String.join(",", labeList));
                            }
                            rowi.put(relName, jsonString(allProperties));
                            rowi.put(key, jsonString(allProperties));
                        }else{
                            String ki=key.replaceAll("[a-zA-Z]+\\W?\\.", "");
                            if(ki.equalsIgnoreCase("id(n)")){
                                ki=NODE_ID;
                            }
                            rowi.put(ki, value);
                        }
                    }
                    if(!rowi.isEmpty()){
                        data.add(rowi);
                    }
                }
                beginTx.success();
            }catch(Exception e){
                LoggerTool.error(logger, e.getMessage()+"\n"+query, e);
            }
            return data;
        });
    }

    public List<Map<String, Object>> voQueryData(String query){
        GraphDatabaseService instance=getInstance();
        List<Map<String, Object>> data=new ArrayList<>();
        try(Transaction beginTx=instance.beginTx()){
            Result result=instance.execute(query);
            while(result.hasNext()){
                Map<String, Object> rowi=new HashMap<>();
                Map<String, Object> row=result.next();

                for(Entry<String, Object> ei : row.entrySet()){
                    String key=ei.getKey();
                    Object value=ei.getValue();
                    if(value instanceof Node){
                        Node retNode=(Node) value;
                        Map<String, Object> allProperties=retNode.getAllProperties();
                        Iterable<Label> labels=retNode.getLabels();
                        List<String> labeList=new ArrayList<>();
                        for(Label li : labels){
                            labeList.add(li.name());
                        }
                        allProperties.put(LABEL, String.join(",", labeList));
                        data.add(allProperties);
                    }else{
                        String ki=key.replaceAll("[a-zA-Z]+\\W?\\.", "");
                        if(ki.equalsIgnoreCase("id(n)")){
                            ki=NODE_ID;
                            rowi.put(ki, value);
                        }
                        rowi.put(key, value);
                    }
                }
                if(!rowi.isEmpty()){
                    data.add(rowi);
                }
            }
            beginTx.success();
        }
        return data;
    }

    public List<Map<String, Object>> listData(String label){
        GraphDatabaseService instance=getInstance();
        List<Map<String, Object>> data=new ArrayList<>();
        try(Transaction beginTx=instance.beginTx()){
            ResourceIterator<Node> result=instance.findNodes(Label.label(label));
            while(result.hasNext()){
                Map<String, Object> rowi=new HashMap<>();
                Node row=result.next();
                // 节点数据
                rowi.putAll(row.getAllProperties());
                if(!rowi.isEmpty()){
                    rowi.put(ID, row.getId());
                    data.add(rowi);
                }
            }
            beginTx.success();
        }
        return data;
    }

    public List<Map<String, Object>> listAllData(String label){
        GraphDatabaseService instance=getInstance();
        List<Map<String, Object>> data=new ArrayList<>();
        try(Transaction beginTx=instance.beginTx()){
            ResourceIterator<Node> result=instance.findNodes(Label.label(label));
            while(result.hasNext()){
                Map<String, Object> rowi=new HashMap<>();
                Node row=result.next();
                // 节点数据
                rowi.putAll(row.getAllProperties());
                // 所有关系数据
                Iterable<RelationshipType> relationshipTypes=row.getRelationshipTypes();
                for(RelationshipType rti : relationshipTypes){
                    List<Map<String, Object>> meList=new ArrayList<>();
                    Iterable<Relationship> relationships=row.getRelationships(rti);
                    for(Relationship ri : relationships){
                        meList.add(ri.getEndNode().getAllProperties());
                    }
                    rowi.put(rti.name(), meList);
                }
                if(!rowi.isEmpty()){
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
    public Node queryNode(Map<String, Object> propMap, String label){
        GraphDatabaseService instance=getInstance();
        Node findNode=null;
        try(Transaction tx=instance.beginTx()){

            Map<String, Object> validPropMap=clearNonStringValue(clearEmptyValue(propMap));
            if(validPropMap.isEmpty()){
                return findNode;
            }

            ResourceIterator<Node> findNodes=instance.findNodes(Label.label(label), validPropMap);
            if(findNodes!=null&&findNodes.hasNext()){
                findNode=findNodes.next();
            }else{
                if(validPropMap.containsKey(NODE_ID)){
                    Long nodeId=Long.parseLong(String.valueOf(validPropMap.get(NODE_ID)));

                    Node nodeBy=instance.getNodeById(nodeId);
                    validPropMap.remove(NODE_ID);
                    if(nodeBy!=null){
                        findNode=nodeBy;
                    }else{
                        ResourceIterator<Node> noIdNodes=instance.findNodes(Label.label(label), validPropMap);
                        while(noIdNodes.hasNext()){// 删除老数据
                            findNode=noIdNodes.next();
                            Map<String, Object> allProperties=findNode.getAllProperties();
                            Long obj=Long.parseLong(String.valueOf(allProperties.get(NODE_ID)));
                            if(!nodeId.equals(obj)){
                                findNode.setProperty(NODE_ID, obj);
                            }
                        }
                    }
                }else{
                    ResourceIterator<Node> noIdNodes=instance.findNodes(Label.label(label), validPropMap);
                    if(noIdNodes.hasNext()){
                        findNode=noIdNodes.next();
                    }
                }
            }
            tx.success();
        }
        return findNode;
    }

    /**
     * 多条件查询节点
     *
     * @param propMap
     * @param label
     * @return
     */
    public List<Map<String, Object>> query(Map<String, Object> propMap, String label){
        List<Map<String, Object>> list=new ArrayList<>();
        Map<String, Object> validPropMap=clearNonStringValue(clearEmptyValue(propMap));
        if(validPropMap.isEmpty()){
            return list;
        }

        Callable<List<Map<String, Object>>> callabel=()->{
            List<Map<String, Object>> data=new ArrayList<>();
            GraphDatabaseService instance=getInstance();
            try(Transaction tx=instance.beginTx()){
                ResourceIterator<Node> findNodes=instance.findNodes(Label.label(label), validPropMap);
                if(findNodes!=null&&findNodes.hasNext()){
                    while(findNodes.hasNext()){//
                        Map<String, Object> allProperties=findNodes.next().getAllProperties();
                        data.add(allProperties);
                    }
                }else{
                    if(validPropMap.containsKey(NODE_ID)){
                        Long nodeId=Long.parseLong(String.valueOf(validPropMap.get(NODE_ID)));

                        Node nodeBy=instance.getNodeById(nodeId);
                        validPropMap.remove(NODE_ID);
                        if(nodeBy!=null){
                            Map<String, Object> allProperties=nodeBy.getAllProperties();
                            data.add(allProperties);
                        }else{
                            ResourceIterator<Node> noIdNodes=instance.findNodes(Label.label(label), validPropMap);
                            while(noIdNodes.hasNext()){//
                                Map<String, Object> allProperties=noIdNodes.next().getAllProperties();
                                data.add(allProperties);
                            }
                        }
                    }else{
                        ResourceIterator<Node> noIdNodes=instance.findNodes(Label.label(label), validPropMap);
                        while(noIdNodes.hasNext()){//
                            Map<String, Object> allProperties=noIdNodes.next().getAllProperties();
                            data.add(allProperties);
                        }
                    }
                }
                tx.success();
            }
            return data;
        };
        list=vtResult(callabel);
        return list;
    }

    public <T> T vtResult(Callable<T> callabel){
        Future<T> submit=exec.submit(callabel);
        T list=null;
        try{
            list=submit.get();
        }catch(InterruptedException|ExecutionException e){
            LoggerTool.error(logger, e.getMessage(), e);
        }
        return list;
    }

    public <T> void vt(Callable<T> callabel){
        Future<T> submit=exec.submit(callabel);
        try{
            submit.get();
        }catch(InterruptedException|ExecutionException e){
            LoggerTool.error(logger, e.getMessage(), e);
        }
    }

    /**
     * 删除与当前节点所有关系数据后，再删除节点
     *
     * @param propMap
     * @param label
     * @return
     */
    public Node removeNode(Map<String, Object> propMap, String label){
        GraphDatabaseService instance=getInstance();
        Node findNode=null;
        try(Transaction tx=instance.beginTx()){
            Long nodeId=longValue(propMap, NODE_ID);
            if(nodeId!=null&&nodeId>0){
                findNode=instance.getNodeById(nodeId);
                if(findNode!=null){
                    deleteRelAndNode(findNode);
                }
                if(propMap.containsKey(NODE_ID)){
                    propMap.remove(NODE_ID);
                }
                ResourceIterator<Node> noIdNodes=instance.findNodes(Label.label(label),
                        clearNonStringValue(clearEmptyValue(propMap)));
                while(noIdNodes.hasNext()){// 删除老数据
                    findNode=noIdNodes.next();
                    if(findNode.getId()==nodeId){
                        findNode=deleteRelAndNode(findNode);
                    }else{
                        Map<String, Object> allProperties=findNode.getAllProperties();
                        String valueOf=String.valueOf(allProperties.get(NODE_ID));
                        if(CommonUtil.isNumber(valueOf)){
                            Long obj=Long.parseLong(valueOf);
                            if(nodeId.equals(obj)){
                                findNode=deleteRelAndNode(findNode);
                            }
                        }
                    }
                }
            }
            tx.success();
        }
        return findNode;
    }

    public void removeById(String id){
        Long nodeId=Long.parseLong(id);
        removeById(nodeId);
    }

    public Node removeById(Long nodeId){
        Node nodeById=null;
        try(Transaction tx=getInstance().beginTx()){
            nodeById=getInstance().getNodeById(nodeId);
            deleteRelAndNode(nodeById);

            tx.success();
        }
        return nodeById;
    }

    private Node deleteRelAndNode(Node findNode){
        for(Relationship rsi : findNode.getRelationships()){
            rsi.delete();
        }
        findNode.delete();
        // findNode = null;
        return findNode;
    }

    public Node queryNode(String query){
        Callable<Node> call=()->{
            GraphDatabaseService instance=getInstance();
            Node node=null;
            try(Transaction beginTx=instance.beginTx()){
                Result result=instance.execute(query);
                if(result.hasNext()){
                    node=(Node) result.next();
                }
                beginTx.success();
            }
            return node;
        };
        return vtResult(call);
    }

    /**
     * 关系数据，包含开始结束节点的属性数据
     *
     * @param query
     * @return
     */
    public JSONArray relationData(String query){
        JSONArray relationTree=new JSONArray();
        List<Map<String, Object>> relations=queryData(query);
        Map<Long, JSONObject> retMap=new HashMap<>();
        Map<Long, Set<Long>> relationMap=new HashMap<>();
        for(Map<String, Object> ri : relations){
            Node m=(Node) ri.get("m");
            Node n=(Node) ri.get("n");
            long startId=n.getId();
            long endId=m.getId();
            if(!relationMap.containsKey(startId)){
                Set<Long> objects=new HashSet<>();
                objects.add(endId);
                relationMap.put(startId, objects);
            }else{
                relationMap.get(startId).add(endId);
            }
            retMap.put(startId, propertiesJSON(n));
            retMap.put(endId, propertiesJSON(m));
        }
        for(Entry<Long, Set<Long>> ei : relationMap.entrySet()){
            Long pid=ei.getKey();
            JSONObject jsonObject=retMap.get(pid);
            JSONArray childs=new JSONArray();
            Set<Long> value=ei.getValue();
            for(Long cid : value){
                childs.add(retMap.get(cid));
            }
            jsonObject.put("childs", childs);
            relationTree.add(jsonObject);
        }
        return relationTree;
    }

    public JSONArray relationOne(String query){
        JSONArray relationTree=new JSONArray();
        List<Map<String, Object>> relations=queryCypher(query);
        Map<Long, JSONObject> retMap=new HashMap<>();
        Map<Long, Set<Long>> relationMap=new HashMap<>();
        for(Map<String, Object> ri : relations){
            Node m=(Node) ri.get("m");
            Node n=(Node) ri.get("n");
            long startId=n.getId();
            long endId=m.getId();
            if(!relationMap.containsKey(startId)){
                Set<Long> objects=new HashSet<>();
                objects.add(endId);
                relationMap.put(startId, objects);
            }else{
                relationMap.get(startId).add(endId);
            }
            if(!retMap.containsKey(startId)){
                retMap.put(startId, propertiesJSON(n));
            }
            if(!retMap.containsKey(endId)){
                retMap.put(endId, propertiesJSON(m));
            }
        }
        for(Entry<Long, Set<Long>> ei : relationMap.entrySet()){
            Long pid=ei.getKey();
            JSONObject jsonObject=retMap.get(pid);
            JSONArray childs=new JSONArray();
            Set<Long> value=ei.getValue();
            for(Long cid : value){
                childs.add(retMap.get(cid));
            }
            jsonObject.put(REL_END, childs);
            relationTree.add(jsonObject);
        }
        return relationTree;
    }

    public List<Map<String, Object>> queryByCypher(String query){
        List<Map<String, Object>> relations=queryCypher(query);
        return relations;
    }

    public List<Map<String, Object>> queryByCypher(String query, Map<String, Object> param){
        List<Map<String, Object>> relations=queryCypher(query, param);
        return relations;
    }

    public void registerShutdownHook(){
        registerShutdownHook(getInstance());
    }

    private void show(){
        GraphDatabaseService instance=getInstance();
        try(Transaction tx=instance.beginTx()){
            // Node
            ResourceIterable<Node> nIt=instance.getAllNodes();
            nIt.forEach((node)->showNodes(node));
            // commit transaction
            tx.success();
        }
        // Stop the database
        instance.shutdown();
    }

    private void showNodes(Node node){
        System.out.print(node.getId()+"-id;");
        node.getLabels().forEach((label)->System.out.print(label+"-label;"));
        node.getAllProperties().entrySet().forEach((entity)->{
            System.out.print(entity.getKey()+"=");
            System.out.println(entity.getValue()+",");
        });
        node.getRelationships().forEach((rel)->System.out.println(rel));
    }

    // public Node updateNode(Map<String, Object> props, Label label) {
    // GraphDatabaseService instance = getInstance();
    // Node node = null;
    // System.out.println("Database Load!");
    //
    // // 开启事务
    // try (Transaction tx = instance.beginTx()) {
    // // Perform DB operations
    // node = instance.createNode(label);
    // updateNodeData(props, node);
    // tx.success();
    // }
    // return node;
    // }

    public void updateBatch(String cypher, Map<String, Object> props){
        GraphDatabaseService instance=getInstance();
        Map<String, Object> validaMap=clearNonStringValue(clearEmptyValue(props));
        // 开启事务
        try(Transaction tx=instance.beginTx()){
            // Perform DB operations
            instance.execute(cypher, validaMap);

            tx.success();
        }
    }

    public Node updateNode(Map<String, Object> props, Node node, Boolean createLog){
        GraphDatabaseService instance=getInstance();
        // 开启事务
        try(Transaction tx=instance.beginTx()){
            // Perform DB operations
            updateNodeData(props, node);
            tx.success();
        }
        return node;
    }


    public void updateRelation(Map<String, Object> props, Relationship relNode, Boolean createLog){
        GraphDatabaseService instance=getInstance();
        // 开启事务
        try(Transaction tx=instance.beginTx()){
            // Perform DB operations
            updateRelData(props, relNode, createLog);
            tx.success();
        }
    }

    public void updateNode(Map<String, Object> props, Node node){
        updateNode(props, node, false);
    }

    public void updateNode(Map<String, Object> props, Long node){
        updateNode(props, getNodeById(node));
    }

    public void updateRelation(Map<String, Object> props, Relationship node){
        updateRelation(props, node);
    }


    private void updateNodeData(Map<String, Object> newData, Node node){
        if(node==null){
            LoggerTool.error(logger, "node is null========================="+mapString(newData));
            return;
        }
        Map<String, Object> validaMap=clearNonStringValue(clearEmptyValue(newData));
        Map<String, Object> allProperties=node.getAllProperties();
        Integer version2=version(allProperties);
        if(version2==null){
            version2=0;
        }
        version2++;
        newData.put(VERSION_DATA, version2);
        for(Entry<String, Object> ei : newData.entrySet()){
            Object newV=ei.getValue();
            String newK=ei.getKey();
            boolean empty=newV==null||String.valueOf(newV).trim().equals("")
                    ||String.valueOf(newV).toLowerCase().trim().equals("null");
            if(empty&&!newK.equals("creator")&&!newK.equals("updator")){
                node.removeProperty(newK);
            }
        }
        for(String key : validaMap.keySet()){
            Object value=validaMap.get(key);
            if(value!=null&&key!=null){
                if(key.startsWith("n\\.")){
                    key=key.replace("n\\.", "");
                }
                String nodeId=String.valueOf(node.getId());
                if(NODE_ID.equalsIgnoreCase(key)&&!value.equals(nodeId)){
                    value=nodeId;
                }
                Object oldValue=allProperties.get(key);
                if(oldValue==null||!value.equals(oldValue)){
                    setValue(node, key, value);
                }
            }
        }
    }

    public void setValue(Node node, String key, Object value){
        if(value instanceof String l){
            node.setProperty(key, l);
        }else if(value instanceof Long l){
            node.setProperty(key, l);
        }else if(value instanceof Integer l){
            node.setProperty(key, l);
        }else if(value instanceof String l){
            node.setProperty(key, l);
        }else if(value instanceof BigDecimal l){
            node.setProperty(key, l);
        }else if(value instanceof Double l){
            node.setProperty(key, l);
        }else{
            node.setProperty(key, String.valueOf(value));
        }
    }

    public void setValue(Relationship node, String key, Object value){
        if(value instanceof String l){
            node.setProperty(key, l);
        }else if(value instanceof Long l){
            node.setProperty(key, l);
        }else if(value instanceof Integer l){
            node.setProperty(key, l);
        }else if(value instanceof BigDecimal l){
            node.setProperty(key, l);
        }else if(value instanceof Double l){
            node.setProperty(key, l);
        }else{
            node.setProperty(key, String.valueOf(value));
        }
    }


    private void updateRelData(Map<String, Object> props, Relationship node, Boolean createLog){
        Map<String, Object> validaMap=clearNonStringValue(clearEmptyValue(props));
        Map<String, Object> allProperties=node.getAllProperties();
        String jsonString=JSON.toJSONString(allProperties);
        Boolean updated=false;
        for(String key : validaMap.keySet()){
            Object value=validaMap.get(key);
            if(value!=null){
                if(key.startsWith("n\\.")){
                    key=key.replace("n\\.", "");
                }
                String nodeId=String.valueOf(node.getId());
                if(NODE_ID.equalsIgnoreCase(key)&&!value.equals(nodeId)){
                    value=nodeId;
                }
                Object oldValue=allProperties.get(key);
                if(!value.equals(oldValue)){
                    setValue(node, key, value);
                    updated=true;
                }
            }
        }
    }

    public void useIndex(){
        GraphDatabaseService instance=getInstance();

        try(Transaction tx=instance.beginTx()){
            Index<Node> index=instance.index().forNodes("nodes");

            Node node1=instance.createNode();
            String name="歌手 1";
            node1.setProperty("name", name);
            index.add(node1, "name", name);
            node1.setProperty("gender", "男");
            Object result=index.get("name", "歌手 1").getSingle().getProperty("gender");
            System.out.println(result); // 输出为“男”
            tx.success();
        }

    }

    public Boolean hasIndex(String label, String field){
        GraphDatabaseService instance=getInstance();
        try(Transaction tx=instance.beginTx()){
            Iterable<IndexDefinition> indexes=instance.schema().getIndexes(Label.label(label));
            for(IndexDefinition idi : indexes){
                Iterable<String> propertyKeys=idi.getPropertyKeys();
                for(String pki : propertyKeys){
                    if(field.equals(pki)){
                        return true;
                    }
                }
            }
            tx.success();
        }
        return false;
    }

    public Boolean hasConstraint(String label, String field){
        GraphDatabaseService instance=getInstance();
        try(Transaction tx=instance.beginTx()){
            Iterable<ConstraintDefinition> indexes=instance.schema().getConstraints(Label.label(label));
            for(ConstraintDefinition idi : indexes){
                Iterable<String> propertyKeys=idi.getPropertyKeys();
                for(String pki : propertyKeys){
                    if(pki.indexOf(field)>0){
                        return true;
                    }
                }
            }
            tx.success();
        }
        return false;
    }

    public Map<String, Object> clearEmptyValue(Map<String, Object> props){

        Map<String, Object> propMap=new HashMap<String, Object>();
        if(props==null){
            return propMap;
        }
        for(Entry<String, Object> eni : props.entrySet()){
            if(StringUtils.isNotBlank(String.valueOf(eni.getValue()))){
                propMap.put(eni.getKey(), eni.getValue());
            }
        }
        return propMap;
    }

    public Map<String, Object> clearNonStringValue(Map<String, Object> props){
        Map<String, Object> propMap=new HashMap<String, Object>();
        for(Entry<String, Object> eni : props.entrySet()){
            Object value=eni.getValue();
            if(value instanceof java.util.Date){
                propMap.put(eni.getKey(), DateTool.format((Date) value, "yyyy-MM-dd hh:MM:ss"));
                continue;
            }
            if(value instanceof String||value instanceof Integer||value instanceof Double
                    ||!String.valueOf(value).isBlank()){
                if(value instanceof List li){
                    if(!li.isEmpty()){
                        propMap.put(eni.getKey(), String.valueOf(value));
                    }else{
                        propMap.put(eni.getKey(), "");
                    }
                }else{
                    propMap.put(eni.getKey(), value);
                }
            }
        }
        return propMap;
    }
}
