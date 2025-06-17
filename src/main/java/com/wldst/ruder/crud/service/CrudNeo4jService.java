package com.wldst.ruder.crud.service;


import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import bsh.EvalError;
import com.wldst.ruder.domain.*;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.bs.BeanShellService;
import com.wldst.ruder.util.LoggerTool;
import com.wldst.ruder.util.ModelUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.fun.service.DataCacheManager;
import com.wldst.ruder.module.workflow.biz.BpmInstanceManagerService;
import com.wldst.ruder.module.workflow.constant.BpmDo;
import com.wldst.ruder.module.workflow.util.WFEConstants;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.StringGet;

import jakarta.servlet.http.HttpServletRequest;

import static com.wldst.ruder.domain.AuthDomain.ONLINE_USER;
import static com.wldst.ruder.domain.AuthDomain.onlineSeMap;
import static com.wldst.ruder.domain.FileDomain.FILE_STORE_PATH;
import static com.wldst.ruder.domain.FileDomain.UI_PUBLISH_PATH;
import static com.wldst.ruder.domain.LayUIDomain.is_po;
import static com.wldst.ruder.domain.SystemDomain.*;

@Service
public class CrudNeo4jService extends Neo4jService{

    final static Logger debugLog=LoggerFactory.getLogger("debugLogger");
    final static Logger logger=LoggerFactory.getLogger(CrudNeo4jService.class);
    final static Logger resultLog=LoggerFactory.getLogger("reportsLogger");

    private CrudNeo4jDriver driver;

    private Neo4jOptByUser optByUserSevice;

    protected BeanShellService bss;

    private RuleDomain rule;


    private static DataCacheManager dcm=new DataCacheManager();


    private static String printPrefix="\n================cypher: ==============\n";


    /**
     * Neo4JDriver creates and inserts the query to Neo4j instance
     */
    @Autowired
    public CrudNeo4jService(CrudNeo4jDriver driver, @Lazy Neo4jOptByUser optByUserSevice, RuleDomain rule, BeanShellService bss){
        super(driver, optByUserSevice, rule, bss);
        this.driver=driver;
        this.optByUserSevice=optByUserSevice;
        this.rule=rule;
        this.bss=bss;
    }


    private void logException(Exception e){
        LoggerTool.debug(logger, "Excetion : ", e);
        debugLog.debug("Excetion : ", e);
    }

    private void logDownInfo(){
        LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
        debugLog.debug("Driver or Session is down, check the configuration");
    }

    public Relationship addRelation(Node start, Node end, String relationLabel){
        if(start==null||end==null){
            return null;
        }
        operateLog("创建关系"+relationLabel,
                "startId="+start.getId()+"["+relationLabel+":"+relationLabel+"]->endId="+end.getId(),
                relationLabel);

        return driver.createRelation(start, end, relationLabel);
    }


    public void addRels(String label, String relation, String startId, String endId){
        Node start=findBy(NODE_ID, startId, label);
        List<Node> endNodes=new ArrayList<>();
        for(String endIdi : endId.split(",")){
            endNodes.add(findBy(NODE_ID, endIdi, label));
        }
        addRelations(start, endNodes, relation);
    }

    public void addRels(String label, String relation, Long startId, String endId){
        Node start=getNodeById(startId);
        List<Node> endNodes=new ArrayList<>();
        for(String endIdi : endId.split(",")){
            endNodes.add(findBy(NODE_ID, endIdi, label));
        }
        addRelations(start, endNodes, relation);
    }

    public Relationship addRel(String relation, String startId, String endId){
        if("null".equals(startId)||"null".equals(endId)){
            return null;
        }
        return addRelation(getNodeById(startId), getNodeById(endId), relation);
    }

    public void addRel(String relation, String startId, String endId, String relName){
        if("null".equals(startId)||"null".equals(endId)){
            return;
        }
        Node endNode=getNodeById(endId);

        Node startNode=getNodeById(startId);
        if(endNode==null||startNode==null){
            return;
        }
        relatei(startNode, endNode, relation, relName);
    }

    public void addRel(String relation, Long startId, Long endId, String relName){
        Node endNode=getNodeById(endId);

        Node startNode=getNodeById(startId);
        if(endNode==null||startNode==null){
            return;
        }
        relatei(startNode, endNode, relation, relName);
    }

    public Relationship addRel(String relation, String startId, String endId, Map<String, Object> props){
        return addRelation(getNodeById(startId), getNodeById(endId), relation, props);
    }

    public void addRel(String relation, String startId, String label, String valueField, String endValue, String relName){
        Node findBy=findBy(valueField, endValue, label);
        if(findBy!=null){
            relatei(getNodeById(startId), findBy, relation, relName);
        }
    }

    //    public void relate(String relation, Long startId, Long endId, Map<String, Object> relpropMap) {
//	relate(getNodeById(startId), getNodeById(endId), relation, relpropMap);
//    }
    public void relateList(String relation, Long startId, List<Long> endIds){
        relateList(relation, startId, endIds, null);
    }

    public void relateList(String relation, Long startId, List<Long> endIds, Map<String, Object> relpropMap){
        for(Long endId : endIds){
            addRelation(getNodeById(startId), getNodeById(endId), relation, relpropMap);
        }
    }

    public void addRel(String relation, Long startId, Long endId, Map<String, Object> relpropMap){
        addRelation(getNodeById(startId), getNodeById(endId), relation, relpropMap);
    }

    public Relationship relatei(Node start, Node end, String relationLabel, String name){
        Map<String, Object> param=new HashMap<>();
        param.put(NAME, name);
        if(name==null){
            param.put(NAME, relationLabel);
        }
        return driver.createRelation(start, end, relationLabel, param);
    }

//    public Relationship relate(Node start, Node end, String relationLabel, Map<String, Object> propMap) {
//   	return driver.createRelation(start, end, relationLabel, propMap);
//       }

    public void addRel(String relation, Long startId, Long endId){
        String createRelation=Neo4jOptCypher.createRelation(relation, startId, endId);
        execute(createRelation);
//	addRelation(getNodeById(startId), getNodeById(endId), relation);
    }

    public void addRelByCypher(Long startId, Long endId, String relation){
        String query=" create (n)-[r:"+relation+"{name:\""+relation+"\"}]->(m)  where id(n)="+startId+" and id(m)="+endId+" return r";
        execute(query);
    }

    public List<Map<String, Object>> hasPermission(String relation, Long startId){
        String query="MATCH (n)-[:HAS_PERMISSION]->(m:"+relation+") where id(n)="+startId+"   return m";
        return queryByCypher(query);
    }

    public Boolean hasPermission(Long startId, Long endId){
        String query="MATCH (n)-[r:HAS_PERMISSION]->(m) where id(n)="+startId+" and id(m)="+endId+"  return r";
        return !queryByCypher(query).isEmpty();
    }

    public void relate(String relation, Long startId, Long endId){
        addRelation(getNodeById(startId), getNodeById(endId), relation, null);
//	String query = "MATCH (m),(n) where id(n)="+startId+" and id(m)="+endId+" create (n)-[r:"+relation+"{\"name\":\""+relation+"\"}]->(m) return r";
//	execute(query);
    }

    public void relate(String relation, String name, Long startId, Long endId){
        String query="MATCH (m),(n) where id(n)="+startId+" and id(m)="+endId+" create (n)-[r:"+relation+"{name:\""+name+"\"}]->(m) return r";
        execute(query);
    }

    public void clear(String relation, Long startId, Long endId, Map<String, Object> props){
        String mapString=mapString(props);
        String cypher="MATCH (s)-[r:"+relation
                +"{"+mapString+"}]->(e)  where id(s)="+startId+" and id(e)="+endId+" return r";
        List<Map<String, Object>> query=cypher(cypher);
        if(query!=null&&query.size()>1){
            for(Map<String, Object> qi : query){
                execute(cypher);
            }
        }
    }

    public void removeNodeById(Long id){
        driver.removeById(id);
    }

    public void removeNodeList(List<Long> dirty){
        if(!dirty.isEmpty()){
            operateLog("删除节点", String.join(",", String.valueOf(dirty)), "");
            for(Long di : dirty){
                removeNodeById(di);
            }
        }
    }

    public void removeById(Long id){
        driver.removeById(id);
        operateLog("删除节点", id+"", "");

    }

    /**
     * 添加某个实例关系时，先看看是否有相关关系定义，如没有则定义类关系。
     *
     * @param relProp
     */
    public void validateParts(String startLabel, String endLabel, Map<String, Object> relProp){
        Node startPO=findBy(NODE_LABEL, startLabel, META_DATA);
        Node endPO=findBy(NODE_LABEL, endLabel, META_DATA);
        if(startLabel.equals(META_DATA)){
            return;
        }
        String relLabel=String.valueOf(relProp.get(LABEL));
        StringBuilder relationExist=Neo4jOptCypher.relationExist(relLabel, startPO.getId(), endPO.getId());

        List<Map<String, Object>> query=cypher(relationExist.toString());

        if(query.isEmpty()){
            addRelation(startPO, endPO, relLabel, relProp);
        }
    }

    /**
     * 创建关系，并附加关系属性
     *
     * @param start
     * @param end
     * @param relationLabel
     * @param propMap
     */
    public Relationship addRelation(Node start, Node end, String relationLabel, Map<String, Object> propMap){
        if(start==null||end==null){
            return null;
        }
        operateLog("创建关系", "startId="+start.getId()+"-["+relationLabel+"]->endId="+end.getId(),
                relationLabel);

        return driver.createRelation(start, end, relationLabel, propMap);
    }

    public void addRelations(Node start, List<Node> ends, String relationLabel){
        driver.createRelations(start, ends, relationLabel);
    }

//    public void addRelation(Node start, Node end, String relationLabel, String relationName) {
//	Map<String, Object> propMap = new HashMap<>();
//	propMap.put("name", relationName);
//	driver.createRelation(start, end, relationLabel, propMap);
//	operateLog("创建关系", "startId=" + start.getId() + "-[" + relationLabel + "]->endId=" + end.getId(),
//		relationLabel);
//    }

    /**
     * 保存关系定义
     *
     * @param endLabel
     * @param relLabel
     * @param startLabel
     * @param relationName
     */
    public Map<String, Object> saveRelationDefine(String endLabel, String relLabel, String startLabel,
                                                  String relationName){
        Map<String, Object> priMap=new HashMap<>();
        priMap.put("reLabel", relLabel);
        priMap.put("startLabel", startLabel);
        priMap.put("endLabel", endLabel);
        priMap.put("name", relationName);
        saveByBody(priMap, RELATION_DEFINE);
        return priMap;
    }

//    public void addRelations(Node start, List<Node> ends, String relationLabel, Map<String, Object> relProp) {
//		relationService.addRel();
////	optByUserSevice.addRelation(null, null, null);
////	driver.createRelations(start, ends, relationLabel, relProp);
//    }

    /*
     * Close Neo4j Connection
     */
    public void closeDriverSession(){
        driver.registerShutdownHook();
    }

    /**
     * 创建索引
     *
     * @param label
     * @param key
     * @author liuqiang
     * @date 2019年9月20日 下午2:38:35
     * @version V1.0
     */
    private void createIndexOn(String label, String key){
        String createIndex="create INDEX ON :"+label+"("+key+")";
        driver.excuteCypher(createIndex);
    }

    /**
     * 不重复唯一性处理
     *
     * @param label
     * @param key
     * @author liuqiang
     * @date 2019年9月20日 下午2:40:30
     * @version V1.0
     */
    private void createUniqueOn(String label, String key){
        if(!driver.hasConstraint(label, key)){
            String createUnique="create constraint ON (s:"+label+") assert s."+key+" is unique ";
            driver.excuteCypher(createUnique);
        }
    }

    /**
     * 删除索引
     *
     * @param label
     * @param key
     * @author liuqiang
     * @date 2019年9月20日 下午2:42:03
     * @version V1.0
     */
    private void dropIndexOn(String label, String key){
        String dropIndex="drop INDEX ON :"+label+"("+key+")";
        driver.excuteCypher(dropIndex);
    }

    /**
     * 不重复唯一性处理
     *
     * @param label
     * @param key
     * @author liuqiang
     * @date 2019年9月20日 下午2:40:30
     * @version V1.0
     */
    private void dropUniqueOn(String label, String key){
        String dropUnique="drop constraint ON (s:"+label+") assert s."+key+" is unique ";
        driver.excuteCypher(dropUnique);
    }

    /**
     * Inserting AST to Neo4J instance through logger file
     *
     * @param cmd
     */
    @ServiceLog(description = "执行 Cypher")
    public void execute(String cmd){
        if(isNeo4jConnectionUp()){
            // Insert query on Neo4j graph DB
            driver.excuteCypher(cmd);
            LoggerTool.info(logger, printPrefix+"execute:"+cmd);
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
    }

    public void executeBatch(String cmd, Map<String, Object> params){
        if(isNeo4jConnectionUp()){
            // Insert query on Neo4j graph DB
            driver.updateBatch(cmd, params);
            LoggerTool.info(logger, "\n=======executeBatch: \n"+cmd);
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
    }

    /**
     * 根据名字查找对应对象
     *
     * @param key
     * @param label
     * @return
     * @author liuqiang
     * @date 2019年9月18日 下午5:55:34
     * @version V1.0
     */
    public Node findBy(String key, String value, String label){
        Node findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.findNode(key, value, label);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    public List<Map<String, Object>> listDataBy(String key, String value, String label){
        List<Map<String, Object>> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.queryBy(key, value, label);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    public List<Map<String, Object>> listDataByLabel(String label){
        return listDataByLabel(label, null);
    }

    @ServiceLog(description = "获取数据")
    public List<Map<String, Object>> listDataByLabel(String label, Map<String, Object> param){
        Map<String, Object> md=getAttMapBy(LABEL, label, META_DATA);
        if(md==null||md.isEmpty()){
            return null;
        }
        String query=optByUserSevice.listAllObject(param, label, columns(md)).toString();
        if(param==null){
            List<Map<String, Object>> dataList=cypher(query);
            return dataList;
        }
        List<Map<String, Object>> dataList=query(query,param);

        return dataList;
    }

    /**
     * 根据名字查找对应对象
     *
     * @param name
     * @param label
     * @return
     * @author liuqiang
     * @date 2019年9月18日 下午5:55:34
     * @version V1.0
     */
    public Node findByName(String name, String label){
        Node findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.findNodeByName(name, label);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    public Set<String> getSensitiveData(){
        List<Map<String, Object>> sensitives=listDataByLabel("sensitiveData");
        Set<String> sensitiveLables=new HashSet<>();
        for(Map<String, Object> si : sensitives){
            String labels=string(si, "metaLabel");

            String[] split=labels.split(",");
            if(split.length>1){
                for(String si2 : split){
                    sensitiveLables.add(si2);
                }
            }else{
                sensitiveLables.add(labels);
            }
        }
        return sensitiveLables;
    }

    public Node getNodeById(String nodeId){
        return getNodeById(Long.valueOf(nodeId));
    }

    public Node getNodeById(Long valueOf){
        Node findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.getNodeById(valueOf);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    public List<Map<String, Object>> getDataBy(String name) {
        return getDataBy(name,null);
    }

    public List<Map<String, Object>> searchMeataDataBy(String name) {
        return searchMetaDataBy(name,META_DATA);
    }
    public List<Map<String, Object>> searchMetaDataBy(String name,String label) {
        StringBuilder queryBy = new StringBuilder("match(n) ");
        if(label!=null){
            queryBy= new StringBuilder("match(n:"+label+") ");
        }
        if(name!=null&&!"".equals(name)) {
            queryBy.append("  where ( n.code contains(\""+name+"\") OR n.name contains(\""+name+"\")  OR n.label contains(\""+name+"\") ");
            try{
                if(Long.valueOf(name)!=null){
                    queryBy.append("   OR id(n)="+name );
                }
            }catch (Exception e){
//			LoggerTool.error(logger,e.getMessage(),e);
            }
            queryBy.append(")" );
        }
        queryBy.append(" return distinct n ");

        return cypher(queryBy.toString());
    }

    public List<Map<String, Object>> searchDataBy(String name) {
        StringBuilder queryBy = new StringBuilder("match(n) ");
        queryBy= new StringBuilder("match(n) ");
        if(name!=null&&!"".equals(name)) {
            queryBy.append("  where ( n.code contains(\""+name+"\") OR n.name contains(\""+name+"\")  ");
            queryBy.append(") and 'MetaData' not in labels(n)" );
        }
        queryBy.append(" return distinct n ");

        return cypher(queryBy.toString());
    }

    public List<Map<String, Object>> getDataBy(String name,String label) {
        StringBuilder queryBy = new StringBuilder("match(n) ");
        if(label!=null){
            queryBy= new StringBuilder("match(n:"+label+") ");
        }
        if(name!=null&&!"".equals(name)) {
            queryBy.append("  where ( n.code=\""+name+"\" OR n.name=\""+name+"\" ");
            try{
                if(Long.valueOf(name)!=null){
                    queryBy.append("   OR id(n)="+name );
                }
            }catch (Exception e){
//			LoggerTool.error(logger,e.getMessage(),e);
            }
            queryBy.append(")" );
        }
        queryBy.append(" return distinct n ");

        return cypher(queryBy.toString());
    }

    /**
     * 获取节点属性Map
     *
     * @param key
     * @param value
     * @param label
     * @return
     */
    public Map<String, Object> getAttMapBy(String key, String value, String label){
        String queryx="MATCH(n:"+label+") where n."+key+"='"+value+"' return n";
        Map<String, Object> findNode=null;
        try{
            List<Map<String, Object>> query=queryCache(queryx);
            if(query!=null&&!query.isEmpty()){
                findNode=query.get(0);
            }
            // driver.findNodeAttMap(key, value, label);
        }catch(Exception e){
            LoggerTool.debug(logger, "Excetion : ", e);
            debugLog.debug("Excetion : ", e);
        }

        return findNode;
    }

    public Map<String, Object> getAttMapOf(String label){
        return getAttMapBy(LABEL, label, META_DATA);
    }

    public String getValueOfFieldObject(Map<String, Object> hi, String fieldKey, String fieldName){
        String retStr=null;
        try{
            String projectId=string(hi, fieldKey);
            Map<String, Object> mainDataObject=getPropMapBy(projectId);
            if(mainDataObject!=null){
                Object valObj=mainDataObject.get(fieldName);
                if(null!=valObj){
                    retStr=valObj.toString();
                }else{
                    LoggerTool.error(logger, "获取流程关联业务信息失败:");
                }
            }
        }catch(Exception ex){
            LoggerTool.error(logger, "获取流程关联业务信息失败:", ex);
        }
        return retStr;
    }

    public List<Map<String, Object>> listAttMapBy(String key, String value, String label){
        List<Map<String, Object>> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.queryBy(key, value, label);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    public List<Map<String, Object>> listAttMapBy(String key, Object value, String label){
        Map<String, Object> prop=newMap();
        prop.put(key, value);
        return queryBy(prop, label);
    }

    public List<Map<String, Object>> queryBy(Map<String, Object> prop, String label){
        List<Map<String, Object>> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.query(prop, label);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    public List<Map<String, Object>> queryByCypher(String cypher){
        List<Map<String, Object>> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.queryByCypher(cypher);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    public List<Map<String, Object>> queryByCypher(String cypher, Map<String, Object> params){
        List<Map<String, Object>> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.queryByCypher(cypher,params);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }


    public Long getNodeId(String key, String value, String label){
        Long nodeId=null;
        if(isNeo4jConnectionUp()){
            try{
                nodeId=driver.getNodeId(key, value, label);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return nodeId;
    }

    public Map<String, Object> getPropMapByNode(Node node){
        return getPropMapByNodeId(node.getId());
    }

    public Map<String, Object> getPropMapBy(String nodeId){
        return getPropMapByNodeId(Long.valueOf(nodeId));
    }

    /**
     * 获取带label的属性Map
     *
     * @param nodeId
     * @return
     */
    public Map<String, Object> getLablePropBy(String nodeId){
        return getPropLabelByNodeId(Long.valueOf(nodeId));
    }

    /**
     * 根据定义节点的ID获取节点的Label
     *
     * @param nodeId
     * @return
     */
    public String label(Long nodeId){
        return String.valueOf(getPropValueByNodeId(nodeId, LABEL));
    }

    /**
     * 获取节点label
     *
     * @param nodeId
     * @return
     */
    public String getNodeLabelByNodeId(Long nodeId){
        if(nodeId==null){
            return null;
        }
        return driver.getNodeLabelById(nodeId);
    }

    public String getHeaderOf(String key, String label){
        String headeri=null;
        Map<String, Object> attMapBy=getAttMapBy(LABEL, label, META_DATA);
        String[] headers=headers(attMapBy);
        String[] columns=columns(attMapBy);
        for(int i=0; i<columns.length; i++){
            String ci=columns[i];
            if(ci.equals(key)||ci.equalsIgnoreCase(key)){
                headeri=headers[i];
            }
        }
        return headeri;
    }

    public String getColOf(String name, String label){
        String coli=null;
        Map<String, Object> attMapBy=getAttMapBy(LABEL, label, META_DATA);
        String[] headers=splitValue(attMapBy, HEADER);
        String[] columns=splitValue(attMapBy, COLUMNS);
        for(int i=0; i<headers.length; i++){
            String hi=headers[i];
            if(hi.equals(name)||hi.equalsIgnoreCase(name)){
                coli=columns[i];
            }
        }
        return coli;
    }

    public String queryBy(String key, String label){
        String headeri=null;
        Map<String, Object> attMapBy=getAttMapBy(LABEL, label, META_DATA);
        String[] headers=splitValue(attMapBy, HEADER);
        String[] columns=splitValue(attMapBy, COLUMNS);
        for(int i=0; i<columns.length; i++){
            String ci=columns[i];
            if(ci.equals(key)||ci.equalsIgnoreCase(key)){
                headeri=headers[i];
            }
        }
        return headeri;
    }

    /**
     * 根据id和属性名称，获取属性值
     *
     * @param nodeId
     * @param key
     * @return value
     */
    public Object getPropValueByNodeId(Long nodeId, String key){
        if(nodeId==null){
            return null;
        }
        if(isNeo4jConnectionUp()){
            try{
                return driver.getNodePropValueById(nodeId, key);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return null;
    }

    public Object getValueByNodeIdAndAttKey(String nodeId, String key){
        if(nodeId==null){
            return null;
        }
        Long valueOf=Long.valueOf(nodeId);
        return getPropValueByNodeId(valueOf, key);
    }

    public Map<String, Object> getPropMapByNodeId(Long nodeId){
        if(nodeId==null){
            return null;
        }
        Map<String, Object> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.getNodePropertiesById(nodeId);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    public Map<String, Object> getPropLabelByNodeId(Long nodeId){
        Map<String, Object> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.getLabelAndPropertiesById(nodeId);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    /**
     * 根据标签和属性查询节点
     *
     * @param props
     * @param label
     * @return
     */
    public Node getNodeByPropAndLabel(Map<String, Object> props, String label){
        Node findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.queryNode(props, label);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    public Long getNodeIdByPropAndLabel(Map<String, Object> props, String label){
        Long id=null;
        if(props.containsKey(ID)){
            id=id(props);
        }
        if(id==null){
            Node findNode=getNodeByPropAndLabel(props, label);
            if(findNode==null){
                return null;
            }
            id=findNode.getId();
        }
        return id;
    }

    public Node removeNodeByPropAndLabel(JSONObject props, String label){
        Node findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.removeNode(props, label);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    public void removeBy(String id, String label){
        if(id.contains(",")){
            String[] ids=id.split(",");
            for(String idi : ids){
                removeBy(Long.valueOf(idi), label);
            }
        }else{
            removeBy(Long.valueOf(id), label);
        }
    }

    public void removeBy(Long id, String label){
        try{
            String delRel="MATCH(n:"+label+")-[r]-(m) where id(n)="+id+" DETACH DELETE r ";
            execute(delRel);
            String cypher="MATCH(n:"+label+") where id(n)="+id+"  DETACH DELETE  n ";
            execute(cypher);
        }catch(Exception e){
            LoggerTool.debug(logger, "Excetion : ", e);
            debugLog.debug("Excetion : ", e);
        }
    }

    public void delete(String key, String value, String label){
        try{
            String delRel="MATCH(n:"+label+") where n."+key+"='"+value+"' DETACH DELETE n ";
            execute(delRel);
        }catch(Exception e){
            LoggerTool.debug(logger, "Excetion : ", e);
            debugLog.debug("Excetion : ", e);
        }
    }

    public void delete(Long id){
        try{
            execute("MATCH(n) where id(n)="+id+" DETACH DELETE n");
        }catch(Exception e){
            LoggerTool.debug(logger, "Excetion : ", e);
            debugLog.debug("Excetion : ", e);
        }
    }

    public void deleteCascade(Long id){
        try{
            execute("MATCH(n) where id(n)="+id+" DETACH delete n");
        }catch(Exception e){
            LoggerTool.debug(logger, "Excetion : ", e);
            debugLog.debug("Excetion : ", e);
        }
    }


    public Node removeNodeByPropAndLabel(Map<String, Object> props, String label){
        Node findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.removeNode(props, label);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    /**
     * 获取所有 出关系，关系属性和关系节点数据
     *
     * @param props
     * @param label
     * @return
     */
    public List<Map<String, Object>> getOutRelations(JSONObject props, String label){
        return driver.getAllOutgoings(props, label);
    }

    public List<Map<String, Object>> getOutgoings(String label){
        return driver.getOutgoings(label);
    }


    public List<Map<String, Object>> getOutRelations(Map<String, Object> props, String label){
        return driver.getAllOutgoings(props, label);
    }

    public List<Map<String, Object>> queryRelationDefine(String key, String label){
        return driver.queryBy(key, label, RELATION_DEFINE);
    }

    public List<Map<String, Object>> getSomeRelationEndNodeId(Map<String, Object> props, String label,
                                                              List<String> endLabel){
        StringBuilder queryEndId=new StringBuilder();
        queryEndId.append("match(n:"+label+")-[r]->(m) where id(n)="+props.get("id"));
        queryEndId.append(" and any(label in labels(m) WHERE label in ['"+String.join("','", endLabel)+"'])");
        queryEndId.append(" return id(m) AS eId,r.name AS rName,r.label AS rLabel,labels(m) AS label");
        return driver.queryData(queryEndId.toString());
    }

    public List<Map<String, Object>> getEndNodeIdByIdIgnoreRrelation(String id, String label, String rLabel){
        List<String> relationLabelList=new ArrayList<String>();
        if(rLabel!=null){
            relationLabelList.add(rLabel);
        }
        return getByIdIgnoreRels(id, label, relationLabelList);
    }

    /**
     * 获取规则数据
     *
     * @param id
     * @param label
     * @return
     */
    public List<Map<String, Object>> getRuleNodeById(String id, String label){
        StringBuilder queryEndId=new StringBuilder();
        queryEndId.append("match(n:"+label+")-[r]->(m:Rule) where id(n)="+id);

        queryEndId.append(" return id(m) AS eId,m.rulekey as ruleKey,m.content AS content");
        return driver.queryData(queryEndId.toString());
    }

    public void hasPath(String startLabel, Long startId, String endLabel, Long endId){
        String queryPath=optByUserSevice.queryPath(startLabel, startId, endLabel, endId);
        List<Map<String, Object>> paths=cypher(queryPath);

        if(paths!=null&&!paths.isEmpty()){
            for(Map<String, Object> pi : paths){
                System.out.println(mapString(pi));
            }
        }
    }

    public List<Map<String, Object>> getOneRelationList(Long nodeId, String relationLabel){
        return driver.getOneTypeOutgoings(nodeId, relationLabel);
    }

    public List<Map<String, Object>> getOneRelationEndNodeList(Long nodeId, String relationLabel){
        List<Map<String, Object>> oneTypeOutgoings=driver.getOneTypeOutgoings(nodeId, relationLabel);
        List<Map<String, Object>> ends=new ArrayList<>();
        for(Map<String, Object> ri : oneTypeOutgoings){
            Map<String, Object> cmdNodeInfo=mapObject(ri, RELATION_ENDNODE_PROP);
            ends.add(cmdNodeInfo);
        }
        return ends;
    }


    public List<Map<String, Object>> getStatusMachine(String label){
        if(label==null){
            return null;
        }
        StringBuilder queryEndId=new StringBuilder();
        queryEndId.append("match(n:"+META_DATA+")");
        queryEndId.append(")-[r]->(m:status)-[r2]->()");
        queryEndId.append(" where n.label="+label+" and m.status");

        queryEndId.append(" return id(m) AS id,m.rulekey as ruleKey,m.content AS content");
        return driver.queryData(queryEndId.toString());
    }


    public List<Map<String, Object>> getByIdIgnoreRels(String id, String label, String rLabel){
        List<String> rsList=new ArrayList<>();
        rsList.add(rLabel);
        return getByIdIgnoreRels(id, label, rsList);
    }

    public List<Map<String, Object>> getByIdIgnoreRels(String id, String label, List<String> rLabel){
        StringBuilder queryEndId=new StringBuilder();
        queryEndId.append("match(n:"+label+")-[r]->(m) where id(n)="+id);
        for(String ri : rLabel){
            queryEndId.append("  and not ((n)-[r:"+ri+"]->(m))");
        }
        if(rLabel.contains("btn")){
            queryEndId.append(" and not ((n)-[r]->(m:layTableToolOpt))");
        }
        queryEndId.append(" return id(m) AS eId,r.name AS rName,r.label AS rLabel,r.prop AS prop,type(r) AS rType,labels(m) AS label");
        return driver.queryData(queryEndId.toString());
    }

    /**
     * 获取没有方向的关系节点
     *
     * @param id
     * @param label
     * @param rLabel
     * @return
     */
    public List<Map<String, Object>> getNodeIdById(String id, String label, List<String> rLabel){
        StringBuilder queryEndId=new StringBuilder();
        queryEndId.append("match(n:"+label+")-[r]-(m) where id(n)="+id);
        for(String ri : rLabel){
            queryEndId.append("  and not ((n)-[r:"+ri+"]->(m))");
        }
        if(rLabel.contains("btn")){
            queryEndId.append(" and not ((n)-[r]->(m:layTableToolOpt))");
        }
        queryEndId.append(" return id(m) AS eId,r.name AS rName,r.label AS rLabel,r.prop AS prop,type(r) AS rType,labels(m) AS label");
        return driver.queryData(queryEndId.toString());
    }

    public Map<String, Object> getNodeMapById(String id){
        return getNodeMapById(Long.valueOf(id));
    }

    public Map<String, Object> getOneMapById(Long id){
        String cypher="MATCH(n) where id(n)="+id+" return n";
        return getOne(cypher);
    }

    public Map<String, Object> getOneMapById(Long id, String label){
        String cypher="MATCH(n) where id(n)="+id+" return n";
        return getOne(cypher);
    }

    public Map<String, Object> getNodeMapById(Long id){
        Map<String, Object> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.getNodePropertiesById(id);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return findNode;
    }

    public List<Map<String, Object>> getSomeRelEndNodeId(String id, String label, List<String> rLabel){
        StringBuilder queryEndId=new StringBuilder();
        queryEndId.append("match(n:"+label+")-[r]->(m) where id(n)="+id);
        if(rLabel.size()>1){
            queryEndId.append("  and type(r) in ['"+String.join("','", rLabel)+"']");

        }else{
            queryEndId.append("  and type(r)="+rLabel.get(0));
        }
        queryEndId.append(" return id(m) AS eId,r.name AS rName,r.label AS rLabel,type(r) AS rType,labels(m) AS label");
        return driver.queryData(queryEndId.toString());
    }

    public List<Map<String, Object>> currentStatus(Map<String, Object> props, String label, String rLabel){
        return driver.getOneTypeOutgoings(props, label, rLabel);
    }

    /**
     * 获取一种关系数据
     *
     * @param props
     * @param label
     * @param rLabel
     * @return
     */
    public List<Map<String, Object>> getOneRelationList(Map<String, Object> props, String label, String rLabel){
        return driver.getOneTypeOutgoings(props, label, rLabel);
    }

    public List<Long> getEndIdsOf(String relationLabel, Long startId){
        return driver.getEndIdsOf(relationLabel, startId);
    }

    public List<String> getEndIdsOf(String relationLabel, Long startId, String endLabel){
        return driver.getEndIdsOf(relationLabel, startId, endLabel);
    }

    /**
     * 获取关系终点节点
     *
     * @param relationLabel
     * @param startId
     * @return
     */
    public List<Map<String, Object>> getEndNodes(String relationLabel, Long startId, String endLabel){
        return driver.getEndNodesBy(relationLabel, startId);
    }

    public List<Map<String, String>> valueTitles(List<Map<String, Object>> listMap, String endLabel){
        List<Map<String, String>> ids=new ArrayList<>(listMap.size());

        Map<String, Object> metaData=getAttMapBy(LABEL, endLabel, META_DATA);
        String[] columns=columns(metaData);
        for(Map<String, Object> mi : listMap){
            Map<String, String> vi=new HashMap<>();
            vi.put("value", string(mi, ID));
            String name2=name(mi);
            if(name2!=null){
                vi.put("title", name2);
            }else{
                //获取可视化字段
                String[] show=show(metaData);
                if(show!=null&&show.length>0){
                    String showField=show[0];
                    vi.put("title", string(mi, showField));
                }else{
                    vi.put("title", string(mi, columns[1]));
                }
            }
            ids.add(vi);
        }
        return ids;
    }

    /**
     * 获取某个节点的一种关系数据
     *
     * @param startId
     * @param rLabel
     * @return
     */
    public List<Map<String, Object>> getRelationDataOf(Long startId, String rLabel){
        return driver.getOneTypeOutgoings(startId, rLabel);
    }

    public List<Map<String, Object>> getChildrens(Map<String, Object> props, String label){
        List<Map<String, Object>> oneTypeOutgoings=driver.getOneTypeOutgoings(props, label, REL_TYPE_CHILDREN);
        if(oneTypeOutgoings.isEmpty()){
            oneTypeOutgoings=driver.getOneTypeOutgoings(props, label, REL_TYPE_CHILDRENS);
        }
        return oneTypeOutgoings;
    }

    /**
     * 获取定义关系
     *
     * @param startLabel
     * @param label
     * @return
     */
    public List<Map<String, Object>> getDefineRelation(String startLabel, String label){
        Map<String, Object> props=new HashMap<>();
        props.put(LABEL, startLabel);
        return driver.getAllDefineRelationList(props, label);
    }

    public String getRelationName(String startLabel, String label, String reLabel){
        Map<String, Object> props=new HashMap<>();
        props.put(LABEL, startLabel);
        return driver.getRelationName(props, label, reLabel);
    }

    /**
     * 获取所有入关系数据,关系属性和关系节点数据
     *
     * @param props
     * @param label
     * @return
     */
    public List<Map<String, Object>> getInRelations(JSONObject props, String label){
        return driver.getIncomings(props, label);
    }

    public List<Map<String, Object>> allInRelations(Long id){
        return driver.allInRelation(id);
    }

    public List<Map<String, Object>> allOutRelations(Long id){
        return driver.getOutgoings(id);
    }

    /**
     * 获取指定label的出关系
     *
     * @param props
     * @param label
     * @param relationLabel
     * @return
     */
    public List<Map<String, Object>> getOutRelationList(JSONObject props, String label, String relationLabel){
        return driver.getTheRelation(props, label, relationLabel);

    }

    /**
     * 获取出关系的所有标签
     *
     * @param props
     * @param label
     * @return
     */
    public List<String> getOutRLabelList(Map<String, Object> props, String label){
        return driver.getOutgoingsLabel(props, label);

    }

    /**
     * 获取指定label的出关系
     *
     * @param label
     * @param relationLabel
     * @return
     */
    public boolean delRelation(String startId, String endId, String label, String relationLabel){
        Map<String, Object> data=new HashMap<>();
        data.put("startId", startId);
        data.put("endId", endId);
        data.put("relationLabel", relationLabel);
        operateLog("删除关系", data, relationLabel);
        return driver.delRelation(startId, endId, label, relationLabel);
    }

    public boolean delRelation(Long startId, Long endId, String relationLabel){


        return driver.delRelation(startId, endId, relationLabel);
    }

    public boolean delRelation(String startId, String endId, String relationLabel){
        Map<String, Object> data=new HashMap<>();
        data.put("startId", startId);
        data.put("endId", endId);
        data.put("relationLabel", relationLabel);
        operateLog("删除关系", data, relationLabel);
        return driver.delRelation(startId, endId, relationLabel);
    }

    public boolean delRelation(Long startId, String relationLabel){
        return driver.delRelation(startId, relationLabel);
    }

    /**
     * 删除Po定义关系
     *
     * @param label
     * @param relationLabel
     * @return
     */
    public boolean delRelation(String label, String relationLabel){
        Map<String, Object> data=new HashMap<>();
        data.put("label", label);
        data.put("relationLabel", relationLabel);
        operateLog("删除关系", data, label);
        return driver.delRelation(null, null, label, relationLabel);
    }

    public Map<String, Object> getThirdInterface(String id){
        String entityString="match (n:"+META_DATA+") -[r]->(e:thirdInterface) where id(n)="+id
                +" return e.save,e.update,e.queryUrl,e.delUrl";
        List<Map<String, Object>> endList=cypher(entityString);
        if(endList==null||endList.isEmpty()){
            return null;
        }
        return endList.get(0);
    }

    /**
     * getTree:获取单级
     *
     * @param label
     * @return
     */
    public Map<String, Object> getTree(String label){
        if(isNeo4jConnectionUp()){
            try{
                return driver.getTreeRootMap(label);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return null;
    }

    public Map<String, Object> nextOneLevelChildren(String label, Map<String, Object> queryMap){
        if(isNeo4jConnectionUp()){
            try{
                return driver.getALevelChildren(label, queryMap);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return null;
    }

    /**
     * 精确定义，必须含有ID，且值不为空
     *
     * @param label
     * @param queryMap
     * @return
     */
    public List<Map<String, Object>> chidlList(String label, Map<String, Object> queryMap){
        if(isNeo4jConnectionUp()){
            try{
                return driver.getChildrenList(label, queryMap);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return null;
    }

    public List<Map<String, Object>> chidlList(String label, String endLabel, Map<String, Object> queryMap){
        if(isNeo4jConnectionUp()){
            try{
                return driver.getChildrenList(label, queryMap);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return null;
    }

    public List<Map<String, Object>> chidrenlList(String label, Map<String, Object> queryMap, String parentIdField){
        StringBuilder childs=new StringBuilder("match(a:"+label+") ");
        if(queryMap.isEmpty()){
            Node findNode=driver.findNode(IS_TREE_ROOT, "true", label);
            if(findNode!=null){
                childs.append("where a.isRoot=\"true\" return a ");
            }else{
                childs.append("where a."+parentIdField+" is null OR a."+parentIdField+"='' OR a."+parentIdField+"='null' return a ");
            }
            return cypher(childs.toString());
        }else{
            Long parentId=parentId(queryMap, parentIdField);
            if(parentId==null){
                parentId=parentId(queryMap);
            }
            if(parentId==null){
                childs.append(" where a."+parentIdField+" is null  return a");
                List<Map<String, Object>> dd=cypher(childs.toString());
                return dd;
            }else{
                childs.append(" where a."+parentIdField+"="+parentId+" or a."+parentIdField+"='"+parentId+"'  return a");
                List<Map<String, Object>> dd=cypher(childs.toString());
                if(dd==null||dd.isEmpty()){
                    dd=cypher(childs.toString().split(" where ")[0]+" where  a."+parentIdField+"='"+parentId+"' return a");
                }
                return dd;
            }


        }
    }

    public void clearUpperKey(String label, Map<String, Object> queryMap, Set<String> columns){
        String string=string(queryMap, ID);

        String join=String.join(",a.", columns);
        String childs="match(A:"+label+") where id(A)="+string+" remove A."+join.toUpperCase();
        execute(childs);
    }

    public Map<String, Object> getUpperKeyData(String label, String id, Set<String> columns){

        String join=String.join(",a.", columns);
        String childs="match(A:"+label+") where id(A)="+id+" return A."+join.toUpperCase();
        return cypher(childs).get(0);
    }

    public List<Map<String, Object>> outRelations(String id){
        return outRelations(Long.valueOf(id));
    }

    public List<Map<String, Object>> outRelations(Long id){
        if(isNeo4jConnectionUp()){
            try{
                return driver.allOutRelation(id);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return null;
    }

    public List<Map<String, Object>> outRelationDatas(Long id){
        if(isNeo4jConnectionUp()){
            try{
                return driver.allOutRelationData(id);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return null;
    }

    public List<Map<String, Object>> inRelationDatas(Long id){
        if(isNeo4jConnectionUp()){
            try{
                return driver.allInRelation(id);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return null;
    }

    public List<Map<String, Object>> getAllByIds(long[] ids){
        String queryBy="match(n) where id(n) in ("+StringGet.joins(ids)+") return n";
        return cypher(queryBy);
    }


    public List<Map<String, Object>> pageOne(String endLabel, String name){
        StringBuilder queryBy=new StringBuilder("match(n:"+endLabel+") ");
        if(name!=null&&!"".equals(name)){
            queryBy.append("  where (n.code CONTAINS '"+name+".* OR n.name CONTAINS '"+name+"') ");
        }
        queryBy.append(" return n ");
        return cypher(queryBy.toString());
    }


    public List<Map<String, Object>> listAllByLabel(String label){
        return listDataByLabel(label);
    }

    public List<Map<String, Object>> listChild(String label, Map<String, Object> param){
        List<Map<String, Object>> listDataByLabel=listDataByLabel(label, param);
        return listWithChildx(listDataByLabel);
    }

    public List<Map<String, Object>> listWithChildx(List<Map<String, Object>> listAllByLabel){
        Map<String, List<Map<String, Object>>> pchild=new HashMap<>();
        Map<String, Map<String, Object>> idMap=new HashMap<>();
        List<Map<String, Object>> parent=new ArrayList<>();
        for(Map<String, Object> mi : listAllByLabel){
            String stringId=stringId(mi);
            idMap.put(stringId, mi);
            if(mi.containsKey("parentId")){
                String parentId=stringParentId(mi);
                if(parentId!=null&&!parentId.equals(stringId)){
                    List<Map<String, Object>> childs=null;
                    if(pchild.get(parentId)==null){
                        childs=new ArrayList<>();
                    }else{
                        childs=pchild.get(parentId);
                    }
                    childs.add(mi);
                    pchild.put(parentId, childs);
                }
            }
        }
        for(Entry<String, List<Map<String, Object>>> mi : pchild.entrySet()){
            String key2=mi.getKey();
            Map<String, Object> map=idMap.get(key2);
            if(map==null){
                continue;
            }
            List<Map<String, Object>> value2=mi.getValue();
            map.put("children", value2);
            parent.add(map);
        }
        return parent;

    }

    /**
     * 根据自定义字段信息和label获取名称化数据
     *
     * @param customFieldMap
     * @param label
     * @return
     */
    public List<Map<String, Object>> listNameLize(Map<String, Map<String, Object>> customFieldMap, String label){
        List<Map<String, Object>> query2=listAllByLabel(label);

        List<Map<String, Object>> tranData=new ArrayList<Map<String, Object>>();
        for(Map<String, Object> dataRowi : query2){
            Map<String, Object> mi=new HashMap<>();
            for(Entry<String, Object> eni : dataRowi.entrySet()){
                String tKey=eni.getKey();
                Object tValue=eni.getValue();
                if(customFieldMap.containsKey(tKey)){
                    String dataIdValue=dataIdValue(customFieldMap, tKey, String.valueOf(tValue));
                    if(dataIdValue!=null&&!"".equals(dataIdValue.trim())){
                        mi.put(tKey, dataIdValue);
                    }else{
                        mi.put(tKey, tValue);
                    }
                }else{
                    mi.put(tKey, tValue);
                }
            }
            tranData.add(mi);
        }
        return tranData;
    }

    /**
     * 转换关联类型的字段的值为ID
     *
     * @param customFieldMap
     * @param trimKey
     * @param colValuei
     * @return
     */
    public String col2ObjectId(Map<String, Map<String, Object>> customFieldMap, String trimKey, String colValuei){
        String value="";
        Map<String, Object> map=customFieldMap.get(trimKey);
        if(map!=null&&bool(map, is_po)){
            // getIdNameMap
            Map<String, String> mapData=new HashMap<>();
            String fieldTypeLabel=string(map, "type");
            List<Map<String, Object>> selectList=getPoIdNameList(fieldTypeLabel);
            for(Map<String, Object> si : selectList){
                mapData.put(string(si, NAME), string(si, ID));
            }
            value=mapData.get(colValuei);
        }
        return value;
    }

    /**
     * 转换关联类型的字段的值转换为name
     *
     * @param customFieldMap
     * @param trimKey
     * @param di
     * @return
     */
    public String dataIdValue(Map<String, Map<String, Object>> customFieldMap, String trimKey, String di){
        String value="";
        Map<String, Object> map=customFieldMap.get(trimKey);
        if(map!=null&&bool(map, is_po)){
            // getIdNameMap
            Map<String, String> mapData=new HashMap<>();
            String fieldTypeLabel=string(map, "type");
            List<Map<String, Object>> selectList=getPoIdNameList(fieldTypeLabel);
            for(Map<String, Object> si : selectList){
                mapData.put(string(si, ID), string(si, NAME));
            }
            value=mapData.get(di);
        }
        return value;
    }

    private List<Map<String, Object>> getPoIdNameList(String fieldTypeLabel){
        Object data2=dcm.getData(fieldTypeLabel);
        if(data2!=null){
            return (List<Map<String, Object>>) data2;
        }
        String[] columns=INTERFACE_COLUMN.split(",");
        String query=Neo4jOptCypher.listAllData(fieldTypeLabel, columns);
        List<Map<String, Object>> selectList=cypher(query);
        dcm.putData(fieldTypeLabel, selectList);
        return selectList;
    }


    public List<Long> endNodeIdList(String label, String rLabel, Map<String, Object> queryMap){
        if(isNeo4jConnectionUp()){
            try{
                return driver.getEndNodeId(label, rLabel, queryMap);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return null;
    }

    @ServiceLog(description = "获取整个tree")
    public Map<String, Object> getWholeTree(String label){
        Map<String, Object> findNode=null;
        Map<String, Object> meta=getAttMapOf(label);
        String columns=string(meta, COLUMNS);
        if(columns.indexOf(","+PARENT_ID)<0){
            return findNode;
        }
        if(isNeo4jConnectionUp()){
            try{
                if(columns.indexOf(IS_TREE_ROOT)>-1){
                    findNode=driver.getWholeTree(label);
                }else{
                    findNode=driver.getWholeTree(label, string(meta, NAME));
                }
            }catch(Exception e){
                e.printStackTrace();
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    public Map<String, Object> getWholeTreeWithColumn(String label, String[] columns){
        Map<String, Object> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.getWholeTreeWithColumn(label, columns);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    public Map<String, Object> getTreeByDefine(String label, Map<String, Object> one2, String[] columns){
        Map<String, Object> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=driver.getTreeByDefine(label, one2, columns);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return findNode;
    }

    /*
     * Check Neo4j Connection
     */
    public boolean isNeo4jConnectionUp(){
        return driver.getInstance()!=null;
    }

    public String getOne(String query, String column){
        return string(getOne(query), column);
    }

    public String getById(Long id, String column){
        String cypher="MATCH(n) where id(n)="+id+" return n."+column;
        List<Map<String, Object>> query=cypher(cypher);
        if(query!=null&&!query.isEmpty()
        ){
            return string(query.get(0), column);
        }
        return null;
    }

    public String getStatusName(String code){
        String cypher="MATCH(n:stateStep) where n.code='"+code+"' return n.name";
        List<Map<String, Object>> query=cypher(cypher);
        if(query!=null&&!query.isEmpty()
        ){
            return name(query.get(0));
        }
        return null;
    }

    public Map<String, Object> getOne(String query){
        List<Map<String, Object>> query2=cypher(query);
        if(query2!=null&&!query2.isEmpty()){
            return query2.get(0);
        }
        return null;
    }
    public Map<String, Object> getOne(String query,Map<String, Object> params){
        List<Map<String, Object>> query2=query(query,params);
        if(query2!=null&&!query2.isEmpty()){
            return query2.get(0);
        }
        return null;
    }

    public Boolean queryBool(String query, String key){
        List<Map<String, Object>> query2=cypher(query);
        if(query2!=null&&!query2.isEmpty()){
            return bool(query2.get(0), key);
        }
        return false;
    }

    public Long id(String query){
        List<Map<String, Object>> query2=cypher(query);
        if(query2!=null&&!query2.isEmpty()){
            Map<String, Object> map=query2.get(0);
            return id(map);
        }
        return null;
    }

    public Long getPasswordIdBy(Long userId){
        return id("Match(n:User)-[r:account]->(m:Password) where id(n)="+userId+" return id(m) AS id");
    }

    public List<Map<String, Object>> query(String query){
        return query(query, new HashMap<String, Object>());
    }

    public List<Map<String, Object>> query(String query, Map<String, Object> params){
        List<Map<String, Object>> queryData=null;
        if(isNeo4jConnectionUp()){
            // Insert query on Neo4j graph DB
            queryData=driver.excuteCypher(query, params);
            if(queryData==null){
                return null;
            }
            LoggerTool.info(logger, "\n execute: \n"+query+"\n==return======>>>>\n"+listMapString(queryData));
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return queryData;
    }

    public List<Map<String, Object>> queryCache(String query){
        Object data2=dcm.getData(query);
        if(data2!=null){
            return (List<Map<String, Object>>) data2;
        }
        List<Map<String, Object>> queryData=null;
        if(isNeo4jConnectionUp()){

            queryData=driver.queryData(query);
            dcm.putData(query, queryData);
            LoggerTool.info(logger, "\n execute: \n"+query+"\n==return======>>>>\n"+listMapString(queryData));
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return queryData;
    }

    public List<Map<String, Object>> voQuery(String query){
        List<Map<String, Object>> queryData=null;
        if(isNeo4jConnectionUp()){
            try{
                // Insert query on Neo4j graph DB
                queryData=driver.voQueryData(query);
                LoggerTool.info(logger, printPrefix+"voQuery:"+query);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return queryData;
    }

    public JSONArray relation(String query){
        JSONArray queryData=null;
        if(isNeo4jConnectionUp()){
            try{
                // Insert query on Neo4j graph DB
                queryData=driver.relationData(query);
                LoggerTool.info(logger, printPrefix+"relation:"+query);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return queryData;
    }

    public JSONArray relationOne(String query){
        JSONArray queryData=null;
        if(isNeo4jConnectionUp()){
            try{
                // Insert query on Neo4j graph DB
                queryData=driver.relationOne(query);
                LoggerTool.info(logger, printPrefix+"relationOne:"+query);
            }catch(Exception e){
                LoggerTool.debug(logger, "Excetion : ", e);
                debugLog.debug("Excetion : ", e);
            }
        }else{
            LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
            debugLog.debug("Driver or Session is down, check the configuration");
        }
        return queryData;
    }

    /**
     * 根据前端数据，创建实体
     *
     * @param priMap
     * @param label
     * @return
     */
    public Node save(Map<String, Object> priMap, String label){
        Node createNode=null;
        LoggerTool.info(logger, "save:{}", mapString(priMap));
        if(priMap.containsKey("name")){
            LoggerTool.info(logger, "containsKey(\"name\"):{}", mapString(priMap));
            Node findNode=driver.findNodeByName(name(priMap), label);
            if(findNode!=null){
                driver.updateNode(priMap, findNode);
                createNode=findNode;
            }else{
                createNode=driver.createNode(priMap, Label.label(label));
            }
        }else{
            Node findNode=driver.queryNode(priMap, label);
            if(findNode!=null){
                createNode=findNode;
            }else{
                createNode=driver.createNode(priMap, Label.label(label));
            }
        }
        if(id(priMap)==null){
            priMap.put("id", createNode.getId());
        }
        doBeanShellAfterSave(label, priMap);
        return createNode;
    }

    public <T> Node saveT(Map<String, Object> priMap, T t){
        Node createNode=null;
        if(priMap.containsKey("name")){
            Node findNode=driver.findNodeByName(String.valueOf(priMap.get("name")), t.getClass().getSimpleName());
            if(findNode!=null){
                createNode=findNode;
            }
        }else{
            createNode=driver.createNode(priMap, Label.label(t.getClass().getSimpleName()));
        }
        if(createNode==null){
            driver.createNode(priMap, Label.label(String.valueOf(t)));
        }
        return createNode;
    }

    /**
     * @param priMap
     * @param label
     * @param key
     * @return
     * @author liuqiang
     * @date 2019年9月20日 下午2:43:24
     * @version V1.0
     */
    public Node saveByKey(Map<String, Object> priMap, String label, String key){
        Node createNode=null;
        // validRule(label, priMap);
        if(StringUtils.isNotBlank(key)){
            String primaryKey=string(priMap, key);
            if(StringUtils.isNotBlank(primaryKey)&&!"null".equalsIgnoreCase(primaryKey)){
                Node queryNode=driver.findNode(key, primaryKey, label);
                if(queryNode!=null){
                    driver.updateNode(priMap, queryNode);
                    return queryNode;
                }else{
                    createNode=driver.createNode(priMap, Label.label(label));
                }
            }else{
                Node queryNode=null;
                if(META_DATA.equals(label)){
                    String object=String.valueOf(priMap.get(NODE_LABEL));
                    queryNode=driver.findNode(NODE_LABEL, object, label);
                }else{
                    Map<String, Object> copy=copy(priMap);
                    copy.remove(CREATETIME);
                    copy.remove(CREATOR);
                    driver.clearEmptyValue(copy);
                    queryNode=driver.queryNode(copy, label);
                }

                if(queryNode!=null){
                    createNode=queryNode;
                    Map<String, Object> nodeProperties=driver.getNodeProperties(queryNode);
                    boolean equal=true;
                    for(Entry<String, Object> ei : nodeProperties.entrySet()){
                        String key2=ei.getKey();
                        Object object=priMap.get(key2);
                        Object value=ei.getValue();
                        if(!NODE_ID.equals(key2)){
                            boolean b=object!=null&&value!=null;
                            if(!b||!object.equals(value)){
                                equal=false;
                            }
                        }
                    }
                    if(equal){
                        return queryNode;
                    }
                    driver.updateNode(priMap, queryNode);
                    return queryNode;
                }else{
                    createNode=driver.createNode(priMap, Label.label(label), key);
                    String jsonString=JSON.toJSONString(priMap);
                    operateLog("创建节点0", jsonString, label);
                }
            }

            // dropIndexOn(label, key);

            if(!driver.hasIndex(label, key)){
                createIndexOn(label, key);
                // createUniqueOn(label, key);
            }
        }else{
            createNode=driver.createNode(priMap, Label.label(label));
        }

        return createNode;
    }

    public String saveDataList(String jsonString){
        StringBuilder sb = new StringBuilder();
        if(StringUtils.isNotBlank(jsonString)){

            int labelIndex = jsonString.indexOf(":[");
            String  label = jsonString.substring(0, labelIndex);
            String  data = jsonString.substring(labelIndex+1);

//             判断是否是jsonArray
            if(data.startsWith("[")){
                JSONArray jsonArray = JSON.parseArray(data);
                for(int i=0;i<jsonArray.size();i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Node createNode=driver.createNode(jsonObject, Label.label(label));
                    if(sb.length()>0){
                        sb.append(",");
                    }
                    sb.append(createNode.getId());
                }
            }
        }
        return sb.toString();
    }

    public String saveByString(String jsonString){
        StringBuilder sb = new StringBuilder();
         if(StringUtils.isNotBlank(jsonString)){
//             判断是否是jsonArray
             if(jsonString.startsWith("[")){
                 JSONArray jsonArray = JSON.parseArray(jsonString);
                 for(int i=0;i<jsonArray.size();i++){
                     JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String keys = keyString(jsonObject);
                    if(!keys.startsWith("id,")&&!keys.contains("id,")){
                        keys="id,"+keys;
                    }
                     Map<String, Object> md = getAttMapBy("columns", keys, META_DATA);
                     if(md!=null&&md.size()>0&&columnsString(md).equals(keys)){
                         Node createNode=driver.createNode(jsonObject, Label.label(label(md)));
                         if(sb.length()>0){
                             sb.append(",");
                         }
                         sb.append(createNode.getId());
                     }
                 }
             }else{
                 JSONObject jsonObject = JSON.parseObject(jsonString);
                 Node createNode=driver.createNode(jsonObject, META_DATA);
                 sb.append(createNode.getId());
             }
         }
         return sb.toString();

    }


    public void updateByKey(Map<String, Object> flowi, String label, String keys){
        Map<String, Object> updateParam=MapTool.copyWithKeys(flowi, "id,"+keys);
        String[] keyArray=keys.split(",");
        String updateCypher=Neo4jOptCypher.update(updateParam, label, keyArray);
        execute(updateCypher);
    }

    public void updateByKey(Map<String, Object> flowi, String keys){
        updateByKey(flowi, null, keys);
    }

    /**
     * 根据Id保存属性
     *
     * @param key
     * @return
     */
    public void saveById(String id, String key, Object value){
        saveById(Long.valueOf(id), key, value);
    }

    public void saveById(Long id, String key, Object value){
        if(StringUtils.isNotBlank(key)){
            String cypher="MATCH (a)   where id(a)="+id+" set a."+key+"="+value;
            execute(cypher);
        }
    }

    public void saveById(String id, Map<String, Object> priMap){
        if(!priMap.isEmpty()){
            if(!priMap.containsKey(NODE_ID)){
                priMap.put(NODE_ID, id);
            }
            rule.validRule(null, priMap);
            Long valueOf=Long.valueOf(id);
            update(priMap, valueOf);
        }
    }

    @ServiceLog(description = "保存数据")
    public void save(Map<String, Object> priMap){
        saveById(id(priMap), priMap);
    }

    public void saveById(Long id, Map<String, Object> priMap){
        if(!priMap.isEmpty()){
            String label2=label(priMap);

            if(label2==null){
                //导入时专用
                label2=string(priMap, MARK_LABEL);
            }
            rule.validRule(label2, priMap);
            update(priMap, id);
        }
    }

    public void saveRelPropById(String id, Map<String, Object> priMap){
        Long valueOf=Long.valueOf(id);
        saveRelById(valueOf, priMap);
    }

    public void saveRelById(Long id, Map<String, Object> priMap){
        if(!priMap.isEmpty()&&id!=null){
            updateRel(priMap, id);
        }
    }

    public void update(Map<String, Object> body){
        Long id2=id(body);
        if(id2!=null){
            update(body, id2);
        }
    }

    public void update(Map<String, Object> body, Long id){
        Node queryNode=driver.getNodeById(id);
        String label=driver.getNodeLabelById(id);
        driver.updateNode(body, queryNode);
        doBeanShellAfterSave(label, body);
    }

    public void updateRel(Map<String, Object> body, Long id){
        Relationship queryrel=driver.getRelationById(id);
        driver.updateRelation(body, queryrel);
    }

    public Node copy(Map<String, Object> priMap, String label, String key){
        Object idObject=priMap.get("id");
        Map<String, Object> nodePropertiesById=driver.getNodePropertiesById(Long.valueOf(String.valueOf(idObject)));
        copy(nodePropertiesById, "name");
        copy(nodePropertiesById, "code");
        nodePropertiesById.remove("id");
        String jsonString=JSON.toJSONString(priMap);
        if(!label.equals(EmailDomain.EMAIL)&&!label.equals(OPERATE_LOG)){
            operateLog("复制节点"+label, jsonString, label);
        }
        System.out.println("复制节点:"+label+"!"+jsonString);

        return driver.createNode(nodePropertiesById, Label.label(label));
    }

    private void copy(Map<String, Object> priMap, String key2){
        if(priMap.containsKey(key2)){
            priMap.put(key2, priMap.get(key2)+"(副本)");
        }
    }

    /**
     * 添加登录记录
     *
     * @param username 用户名
     */
    public void insertLoginLog(String username){
        Map<String, Object> admin=rule.getAdminService().getAccountByUsername(username);
        if(admin==null)
            return;
        insertLoginLog(username, admin);

        onlineUserUpdate(username, admin);
    }

    private void insertLoginLog(String username, Map<String, Object> admin){
        Map<String, Object> loginLog=new HashMap<String, Object>();
        // loginLog.put("Rule", "");
        loginLog.put("adminId", admin.get("id"));
        ServletRequestAttributes attributes=(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request=attributes.getRequest();
        loginLog.put("ip", request.getRemoteAddr());
        loginLog.put("startTime", new Date());
        loginLog.put("creator", username);
        loginLog.put("userName", username);
        loginLog.put("userAccount", username);
        saveByBody(loginLog, LOGIN_LOG);
    }

    private void onlineUserUpdate(String username, Map<String, Object> admin){
        ServletRequestAttributes attributes=(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request=attributes.getRequest();
        Map<String, Object> onlineUser=new HashMap<String, Object>();
        onlineUser.put("ip", request.getRemoteAddr());
        onlineUser.put("startTime", new Date());
        onlineUser.put("userName", username);
        onlineUser.put("userAccount", username);
        onlineUser.put("userId", admin.get("id"));

        saveByBody(onlineUser, ONLINE_USER);
    }

    public void operateLog(String msg, Map<String, Object> data, Map<String, Object> oldData, String label){
        if(!label.equals(OPERATE_LOG)&&!label.equals(LOGIN_LOG)&&!label.equals(ONLINE_USER)){
            ServletRequestAttributes attributes=(ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            HttpServletRequest request=attributes.getRequest();

            Map<String, Object> logData=new HashMap<String, Object>();
            logData.put("ip", request.getRemoteAddr());
            logData.put("time", new Date());
            logData.put("description", msg);
            if(data!=null){
                String newData=jsonString(data);
                logData.put("content", newData);
            }

            if(oldData!=null){
                String oldDataStr=jsonString(oldData);
                logData.put("oldContent", oldDataStr);
            }
            if(label!=null){
                logData.put("targetLabel", label);
            }
            appendOperatorInfo(request, logData);

            saveByBody(logData, OPERATE_LOG);
        }
    }


    private void appendOperatorInfo(HttpServletRequest request, Map<String, Object> logData){
        Object loginName=request.getSession().getAttribute("loginName");
        if(loginName==null){
            return;
        }
        String attribute=String.valueOf(loginName);
        Map<String, Object> map=onlineSeMap.get(attribute);
        if(map!=null){
            logData.put("userId", map.get(ID));
            logData.put("userName", map.get(NAME));
        }
    }

    public void operateLog(String msg, Map<String, Object> data, String label){
        operateLog(msg, data, null, label);
    }

    public void operateLog(String msg, String data, String oldData, String label){
        if(!label.equals(OPERATE_LOG)&&!label.equals(LOGIN_LOG)&&!label.equals(ONLINE_USER)){
            RequestAttributes requestAttributes=RequestContextHolder.getRequestAttributes();
            if(requestAttributes==null){
                LoggerTool.info(logger, "operateLog:{},{},{},{}", msg, data, oldData, label);
                return;
            }
            ServletRequestAttributes attributes=(ServletRequestAttributes) requestAttributes;
            HttpServletRequest request=attributes.getRequest();

            String attribute=String.valueOf(request.getSession().getAttribute("loginName"));
            Map<String, Object> map=onlineSeMap.get(attribute);

            Map<String, Object> logData=new HashMap<String, Object>();
            logData.put("ip", request.getRemoteAddr());
            logData.put("time", new Date());
            logData.put("description", msg);
            if(data!=null){
                logData.put("content", data);
            }

            if(oldData!=null){
                logData.put("oldContent", oldData);
            }
            if(label!=null){
                logData.put("targetLabel", label);
            }
            if(map!=null){
                logData.put("userId", map.get(ID));
                logData.put("userName", map.get(NAME));
            }

            saveByBody(logData, OPERATE_LOG);
        }
    }

    public void operateLog(String msg, String data, String label){
        operateLog(msg, data, null, label);
    }

    public void updateBodyList(List<Map<String, Object>> nodes, String label){
        for(Map<String, Object> pi : nodes){
            saveByBody(pi, label);
        }
    }

    public Node saveByBody(Map<String, Object> priMap, String label){
        return saveByBody(priMap, label, false);
    }

    public List<Long> save(List<Map<String, Object>> nodes, String label){
        List<Long> ids=new ArrayList<>(nodes.size());
        for(Map<String, Object> pi : nodes){
            Node saveByBody=addNew(pi, label);
            ids.add(saveByBody.getId());
        }
        return ids;
    }

    public Node saveByBody(Map<String, Object> priMap, String label, Boolean logRecord){
        Long vId=null;
        try{
            vId=id(priMap);
        }catch(NumberFormatException e){
            priMap.put(label+ID, priMap.get("ID"));
            priMap.remove(priMap.get("ID"));
        }
        if(vId!=null&&vId<0){
            priMap.remove(ID);
        }

        Node queryNode=getIfExistNode(priMap, label);

        Node createNode=saveOrUpdate(priMap, label, logRecord, queryNode);
        priMap.put(ID, createNode.getId());
        if(vId!=null){
            priMap.put("vId", vId);
        }
        return createNode;
    }

    public List<Long> addList(List<Map<String, Object>> priMap, String label){
        List<Long> ids=new ArrayList<>(priMap.size());
        for(Map<String, Object> pi : priMap){
            ids.add(addNew(pi, label).getId());
        }
        return ids;
    }

    public Node addNew(Map<String, Object> priMap, String label){
        rule.validRule(label, priMap);
        Node createNode=driver.createNode(priMap, Label.label(label));
        priMap.put(ID, createNode.getId());
        return createNode;
    }

    public Node saveOrUpdate(Map<String, Object> priMap, String label, Boolean logRecord, Node queryNode){
        Node createNode;
        if(queryNode!=null){
            LoggerTool.info(logger, "query use exist node:"+queryNode.getId());
            createNode=queryNode;
            Boolean update=false;
            Map<String, Object> oldData=driver.getNodeProperties(queryNode);
            for(Entry<String, Object> eni : priMap.entrySet()){
                String newKey=eni.getKey();
                Object newValue=eni.getValue();
                Object oldValue=oldData.get(newKey);
                Boolean valueNotEqual=!oldData.containsKey(newKey)||newValue!=null
                        &&(!oldData.containsValue(newValue)||
                        !newValue.equals(oldValue));
                if(valueNotEqual){
                    update=true;
                }
            }
            if(update){
                if(logRecord){
                    rule.validRule(label, priMap);
                }
                LoggerTool.info(logger, "update node"+queryNode.getId());
                driver.updateNode(priMap, queryNode);
            }
        }else{
            LoggerTool.info(logger, "\ncreate "+label+"-Node"+priMap);
            if(logRecord&&!"BackLog".equals(label)){
                rule.validRule(label, priMap);
            }
            createNode=driver.createNode(priMap, Label.label(label));
            if(logRecord&&!label.equals("BackLog")&&!label.equals(OPERATE_LOG)&&!label.equals(LOGIN_LOG)
                    &&!label.equals(ONLINE_USER)){
                operateLog("创建日志节点", jsonString(priMap), label);
            }
        }
        return createNode;
    }

    /**
     * 保存前查询，除开某些字段来查找某些对象。
     *
     * @param label
     * @param poKeys
     * @param mi
     * @return
     */
    public Node saveByGetExceptPoKeys(String label, List<String> poKeys, Map<String, Object> mi){
        Node createNode=null;
        Node queryNode=null;
        if(poKeys!=null&&!poKeys.isEmpty()){
            Map<String, Object> copyMap=copyMap(mi);

            for(String ki : poKeys){
                copyMap.remove(ki);
            }
            LoggerTool.info(logger, "copyMap:{}", mapString(copyMap));
            LoggerTool.info(logger, "Map:{}", mapString(mi));
            queryNode=getIfExistNode(copyMap, label);
            createNode=saveOrUpdate(mi, label, false, queryNode);
        }else{
            createNode=addNew(mi, label);
        }
        mi.put(ID, createNode.getId());
        return createNode;
    }

    public Node getIfExistNode(Map<String, Object> priMap, String label){
        Node queryNode;
        if(META_DATA.equals(label)){
            String object=String.valueOf(priMap.get(NODE_LABEL));
            queryNode=driver.findNode(NODE_LABEL, object, label);
        }else if(RELATION_DEFINE.equals(label)||LABLE_TABLE.equals(label)){
            Map<String, Object> ppMap=new HashMap<>();
            ppMap.putAll(priMap);
            ppMap.remove("name");
            ppMap.remove(CREATETIME);
            ppMap.remove(UPDATETIME);
            queryNode=driver.queryNode(ppMap, label);
        }else{
            Map<String, Object> nameMap=new HashMap<>();
            for(Entry<String, Object> eni : priMap.entrySet()){
                String key=eni.getKey();
                Object value=eni.getValue();
                if((key.toLowerCase().indexOf("name")>=0)&&value!=null
                        &&!"".equals(String.valueOf(value).trim())){
                    nameMap.put(key, value);
                }
            }
            if(!nameMap.isEmpty()){
                queryNode=driver.queryNode(nameMap, label);
            }else{
                queryNode=driver.queryNode(priMap, label);
            }
        }
        return queryNode;
    }

    /**
     * 根据Key进行查询节点，存在则更新，否则新建节点，并返回节点
     *
     * @param priMap
     * @param lname
     * @param keys
     * @return
     * @author liuqiang
     * @date 2019年9月20日 上午9:21:09
     * @version V1.0
     */
    public Node saveByKey(Map<String, Object> priMap, String lname, String[] keys){
        Node createNode=null;
        if(keys!=null&&keys.length>0){
            String findeNode=Neo4jOptCypher.findNodeBy(priMap, lname, keys);
            Node queryNode=driver.queryNode(findeNode);
            if(queryNode!=null){
                driver.updateNode(priMap, queryNode);
                return queryNode;
            }else{
                createNode=driver.createNode(priMap, Label.label(lname));
            }
        }

        return createNode;
    }

    /**
     * 保存更新后，返回属性MAP
     *
     * @param priMap 属性列表
     * @param lname  标签名称
     * @return
     */
    public Map<String, Object> saveRetAttsMap(Map<String, Object> priMap, String lname, String[] keys){
        Node saveNode=saveByKey(priMap, lname, keys);
        return driver.getNodeProperties(saveNode);
    }

    public Node update(Map<String, Object> priMap, String lname, String[] keys){
        String update=Neo4jOptCypher.update(priMap, lname, keys);
        driver.queryData(update);
        return null;
    }

    /**
     * 递归获取子树
     *
     * @param label
     * @param id
     * @param relationLabelList
     * @param relTypeNameMap
     * @return
     */
    public List<Map<String, Object>> getSubTree(Set<String> usedIdSet, String label, String id,
                                                List<String> relationLabelList, Map<String, String> relTypeNameMap){
        LoggerTool.info(logger, "getSubTree:label:{"+label+"},id{"+id+"}");
        if(usedIdSet.contains(id)||relTypeNameMap==null){
            return null;
        }
        usedIdSet.add(id);
        List<Map<String, Object>> someRelationList=getSomeRelEndNodeId(id, label, relationLabelList);

        if(relTypeNameMap!=null&&relTypeNameMap.size()<relationLabelList.size()){
            relTypeNameMap.putAll(relNameMap(someRelationList));
        }
        Map<String, String> relTypeNameMapCopyMap=null;
        if(relTypeNameMap.size()<relationLabelList.size()){
            relTypeNameMapCopyMap=relTypeNameMap;
        }

        for(Map<String, Object> reli : someRelationList){
            Map<String, Object> endPaMap=new HashMap<>();
            endPaMap.put("id", reli.get("eId"));
            List<String> list=(List<String>) reli.get(LABEL);
            String endLabeli=list.get(0);

            String relLabeli=String.valueOf(reli.get("rType"));
            if(relLabeli==null||relLabeli.equals("null")){
                continue;
            }
            String endId=String.valueOf(reli.get("eId"));
            Map<String, Object> endNodeMap=loadByIdWithLabel(endLabeli, endId);
            List<Map<String, Object>> partTree=getSubTree(usedIdSet, endLabeli, endId, relationLabelList,
                    relTypeNameMapCopyMap);
            if(partTree!=null&&!partTree.isEmpty()){
                usedIdSet.add(endId);
                endNodeMap.put("relations", relEndList(partTree));
            }

            reli.put("endNode", endNodeMap);
        }
        return someRelationList;
    }

    public Map<String, Object> loadByIdWithLabel(String endLabeli, String endId){
        Map<String, Object> endNodeMap=getPropMapBy(endId);
        String strLabel=String.valueOf(endNodeMap.get(NODE_LABEL));
        if(!endNodeMap.containsKey(NODE_LABEL)||endLabeli.indexOf(strLabel)<0)
            ;
        {
            endNodeMap.put(NODE_LABEL, endLabeli);
        }
        return endNodeMap;
    }

    /**
     * 集合关系
     *
     * @param outRelationsnList
     * @param relEnds
     * @return
     */
    public Map<String, String> relSet(List<Map<String, Object>> outRelationsnList,
                                      Map<String, List<Map<String, Object>>> relEnds){
        relEnds.putAll(relEndList(outRelationsnList));
        return relNameMap(outRelationsnList);
    }

    private Map<String, String> relNameMap(List<Map<String, Object>> outRelationsnList){
        Map<String, String> relTypeNameMap=new HashMap<>();
        for(Map<String, Object> reli : outRelationsnList){
            if(relTypeNameMap.get(String.valueOf(reli.get("rType")))==null&&reli.get("rName")!=null){
                relTypeNameMap.put(String.valueOf(reli.get("rType")), String.valueOf(reli.get("rName")));
            }
        }
        return relTypeNameMap;
    }

    /**
     * 关系类型：关系终点列表 映射
     *
     * @param outRelationsnList
     * @return
     */
    public Map<String, List<Map<String, Object>>> relEndList(List<Map<String, Object>> outRelationsnList){
        Map<String, List<Map<String, Object>>> relEnds=new HashMap<>();
        for(Map<String, Object> reli : outRelationsnList){
            List<Map<String, Object>> list=relEnds.get(reli.get("rType"));
            if(list==null){
                list=new ArrayList<Map<String, Object>>();
                relEnds.put(String.valueOf(reli.get("rType")), list);
            }
            list.add(reli);
        }
        return relEnds;
    }


    public void addList(List<Map<String, Object>> addExecutorList, String label, Set<String> columns){
        Map<String, String> idMap=new HashMap<>();
        for(Map<String, Object> ai : addExecutorList){
            handleNodeData(label, columns, idMap, ai);
        }
    }

    /**
     * @param label
     * @param columns
     * @param idMap
     * @param ai
     */
    public void handleNodeData(String label, Set<String> columns, Map<String, String> idMap, Map<String, Object> ai){
        String stringId=MapTool.stringId(ai);

        Map<String, Object> a=new HashMap<>(ai.size());
        // 兼容数据
        if(ai.containsKey("raw")&&!columns.contains("raw")){
            Map<String, Object> mapObject=mapObject(ai, "raw");
            for(String ki : columns){
                if(ki.equals("id")){
                    continue;
                }
                a.put(ki, mapObject.get(ki));
            }

            for(Entry<String, Object> ei : ai.entrySet()){
                for(String ki : columns){
                    if(ki.equals("id")||ki.equals("raw")||ki.equals("children")){
                        continue;
                    }
                    if(ei.getKey().equals(ki.toUpperCase())){
                        a.put(ki, ei.getValue());
                    }
                }
            }
        }else{
            for(Entry<String, Object> ei : ai.entrySet()){
                for(String ki : columns){
                    if(ki.equals("id")||ki.equals("raw")||ki.equals("children")){
                        continue;
                    }
                    if(ei.getKey().equals(ki.toUpperCase())){
                        a.put(ki, ei.getValue());
                    }
                }
            }
        }
        String parentId=string(ai, PARENT_ID);
        if(idMap.get(parentId)!=null){
            a.put(PARENT_ID, idMap.get(parentId));
        }
        saveByBody(a, label);

        idMap.put(stringId, stringId(a));
        List<Map<String, Object>> listMapObject=listMapObject(ai, "children");

        if(listMapObject!=null&&!listMapObject.isEmpty()){
            for(Map<String, Object> ci : listMapObject){
                handleNodeData(label, columns, idMap, ci);
            }
        }
    }

    public void refreshList(List<Map<String, Object>> addExecutorList, String label, Set<String> columns){
        for(Map<String, Object> ai : addExecutorList){
            Map<String, Object> a=new HashMap<>();
            Object value2=ai.get(ID);
            if(ai.size()<2){
                Map<String, Object> upperKeyData=getUpperKeyData(label, String.valueOf(value2), columns);
                ai.putAll(upperKeyData);
            }

            for(Entry<String, Object> ei : ai.entrySet()){
                for(String ki : columns){
                    if(ki.equals("id")){
                        continue;
                    }
                    if(ei.getKey().equals(ki.toUpperCase())){
                        a.put(ki, ei.getValue());
                    }
                }
            }
            clearUpperKey(label, ai, columns);

            a.put(ID, value2);
            saveByBody(a, label);
        }
    }

    public String getPathBy(String key){
        String path=getBySysCode(key);
        String runPath=System.getProperty("user.dir")+File.separator;
        if(runPath.contains("workspace")){
            runPath=runPath+"src"+File.separator+"main"+File.separator+"resources"+File.separator;
        }

        if(path==null){
            if(FILE_STORE_PATH.equals(key)){
                path=runPath+"fileStore";
            }
            if(UI_PUBLISH_PATH.equals(key)){
                path=runPath+"page";
            }
        }
        if(!path.endsWith(File.separator)){
            return path+File.separator;
        }
        return path;
    }

    public String getBySysCode(String key){
        return getSettingBy(key);
    }

    public String refreshSetting(String key){
        DomainBuffer.clear(key);
        return getSettingBy(key);
    }

    public void refreshSetting(String key, String value){
        DomainBuffer.clear(key);
        DomainBuffer.put(key, value);
    }

    public String getSettingBy(String key){
        Map<String, String> bufferCode=DomainBuffer.getBufferCode();
        if(bufferCode.containsKey(key)){
            return bufferCode.get(key);
        }else{
            List<Map<String, Object>> dataBy=getDataBy(key, SETTING);
            if(dataBy ==null||dataBy.isEmpty()){
                return null;
            }
            Map<String, Object> attMapBy=dataBy.get(0);
            if(attMapBy==null||!attMapBy.containsKey(VALUE)){
                return null;
            }
            String valueOfCode=String.valueOf(attMapBy.get(VALUE));
            bufferCode.put(key, valueOfCode);
            return valueOfCode;
        }
    }

    public String getConfigBy(String key){
        Map<String, String> bufferCode=DomainBuffer.getBufferCode();
        if(bufferCode.containsKey(key)){
            return bufferCode.get(key);
        }else{
            Map<String, Object> attMapBy=getAttMapBy(CODE, key, SETTING);
            if(attMapBy==null||!attMapBy.containsKey(VALUE)){
                return null;
            }
            String valueOfCode=String.valueOf(attMapBy.get(VALUE));
            bufferCode.put(key, valueOfCode);
            return valueOfCode;
        }
    }

    public void refreshSetting(){
        DomainBuffer.clear();
    }

    public String getSessionIdBy(String userName){
        Map<String, String> bufferCode=DomainBuffer.getBufferCode();
        if(bufferCode.containsKey(userName)){
            return bufferCode.get(userName);
        }else{
            Map<String, Object> attMapBy=getAttMapBy("userName", userName, "Session");
            if(attMapBy==null||!attMapBy.containsKey("sessionId")){
                return null;
            }
            String valueOfCode=string(attMapBy, "sessionId");
            return valueOfCode;
        }
    }

    /**
     * 后续触发脚本动作
     *
     * @param label
     * @param vo
     */
    public void doBeanShellAfterSave(String label, Map<String, Object> vo){
        //获取保存后的脚本
        List<Map<String, Object>> afterSaveAction=cypher("MATCH(n:"+META_DATA+"{label:'"+label+"'})-[r:afterSave]->(m:BeanShell) return m");
        if(afterSaveAction!=null&&!afterSaveAction.isEmpty()){
            for(Map<String, Object> ai : afterSaveAction){
                try{
                    bss.runShell(stringId(ai), vo);
                }catch(EvalError e){
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public boolean isOn(String key){
        String settingBy=getSettingBy(key);
        boolean equals="on".equals(settingBy)||"true".equals(settingBy)||"1".equals(settingBy);
        return equals;
    }



}
