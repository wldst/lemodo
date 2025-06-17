package com.wldst.ruder.crud.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.module.bs.BeanShellService;
import com.wldst.ruder.util.LoggerTool;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.constant.RuleConstants;
import com.wldst.ruder.domain.DomainBuffer;
import com.wldst.ruder.domain.EmailDomain;
import com.wldst.ruder.engine.DroolsService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.util.FileOpt;
import com.wldst.ruder.util.MapTool;

import static com.wldst.ruder.domain.FileDomain.*;
import static com.wldst.ruder.domain.UserSpaceDomain.*;

@Service
public class CrudUserNeo4jService extends Neo4jService{

    final static Logger debugLog=LoggerFactory.getLogger("debugLogger");
    final static Logger logger=LoggerFactory.getLogger(CrudUserNeo4jService.class);
    final static Logger resultLog=LoggerFactory.getLogger("reportsLogger");
    @Autowired
    private CrudNeo4jDriver myDriver;
    private UserAdminService adminService;
    @Autowired
    private DroolsService drools;
    private static List<Map<String, Object>> globalRuleInfo;
    private Neo4jOptByUser optByUserSevice;

    /**
     * Neo4JDriver creates and inserts the query to Neo4j instance
     *
     * @param driver
     * @param optByUserSevice
     * @param rule
     * @param bss
     */
    @Autowired
    public CrudUserNeo4jService(@Lazy CrudNeo4jDriver driver, @Lazy Neo4jOptByUser optByUserSevice, RuleDomain rule, @Lazy BeanShellService bss, @Lazy UserAdminService adminService){
        super(driver, optByUserSevice, rule, bss);
        this.adminService=adminService;
        this.optByUserSevice=optByUserSevice;
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

    public void initAppOfDesktop(Node deskTopNode){
        // 初始化个人空间：元数据，桌面，
        String queryAppId="MATCH(n:"+SPACE+")-[r:"+APP_REL_LABEL+"]->(m:App) where n.code='"+SPACE_MY
                +"' return id(m) AS id";
        List<Map<String, Object>> queryByCypher=queryByCypher(queryAppId);
        List<Long> endIds=collectAppId(queryByCypher);

        if(endIds!=null&&!endIds.isEmpty()){
            String addDesktopApp=Neo4jOptCypher.createRelation(APP_REL_LABEL, deskTopNode.getId(), endIds);
            execute(addDesktopApp);
        }
    }

    public void initDesktopApp(Node deskTopNode, String userRole){

        // 初始化个人空间：元数据，桌面，
        String queryAppId="MATCH(n:"+SPACE+")-[r:"+APP_REL_LABEL+"]->(m:App) where n.code='"+userRole
                +"' return id(m) AS id";
        List<Map<String, Object>> queryByCypher=queryByCypher(queryAppId);
        List<Long> endIds=collectAppId(queryByCypher);
        if(endIds!=null&&!endIds.isEmpty()){
            String addDesktopApp=Neo4jOptCypher.createRelation(APP_REL_LABEL, deskTopNode.getId(), endIds);
            execute(addDesktopApp);
        }
    }

    private List<Long> collectAppId(List<Map<String, Object>> defaultApp){
        Set<Long> appIdList=new HashSet<Long>();
        for(Map<String, Object> ai : defaultApp){
            appIdList.add(MapTool.id(ai));
        }
        List<Long> data=new ArrayList<>(appIdList.size());
        for(Long id : appIdList){
            data.add(id);
        }
        return data;
    }

    /**
     * 初始化插件
     *
     * @param saveByBody
     */
    public void init(Node saveByBody){
        Map<String, Object> userSpace=new HashMap<>();
        String initData=getPathBy(INIT_DATA);
        String bySysCode=getPathBy(PLUGIN_PATH);
        // 导入基本元数据,导入PO

        // 导入初始化结构数据，导入App，desk。按钮，字段。

        // 初始化桌面数据：元数据定义，文件上传。桌面，App数据。导出数据

        // System.getProperties().getProperty(key);
        userSpace.put(CODE, SPACE_MY);
        // 初始化个人空间：元数据，桌面，
        List<Long> endIds=getEndIdOf(userSpace, SPACE, APP_REL_LABEL);
        String createRelation=Neo4jOptCypher.createRelation(APP_REL_LABEL, saveByBody.getId(), endIds);
        execute(createRelation);
    }

    /*
     * Close Neo4j Connection
     */
    public void closeDriverSession(){
        myDriver.registerShutdownHook();
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
        myDriver.excuteCypher(createIndex);
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
        if(!myDriver.hasConstraint(label, key)){
            String createUnique="create constraint ON (s:"+label+") assert s."+key+" is unique ";
            myDriver.excuteCypher(createUnique);
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
        myDriver.excuteCypher(dropIndex);
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
        myDriver.excuteCypher(dropUnique);
    }

    /**
     * Inserting AST to Neo4J instance through logger file
     *
     * @param cmd
     */
    public void execute(String cmd){
        if(isNeo4jConnectionUp()){
            // Insert query on Neo4j graph DB
            myDriver.excuteCypher(cmd);
            LoggerTool.info(logger, "exucute CMD: "+cmd);
        }else{
            logDownInfo();
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
                findNode=myDriver.findNode(key, value, label);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return findNode;
    }

    public List<Map<String, Object>> listDataBy(String key, String value, String label){
        List<Map<String, Object>> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=myDriver.queryBy(key, value, label);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return findNode;
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
                findNode=myDriver.findNodeByName(name, label);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return findNode;
    }

    public List<Map<String, Object>> getMetaDataBy(String name){
        StringBuilder queryBy=new StringBuilder("match(n:MetaData) ");
        if(name!=null&&!"".equals(name)){
            queryBy.append("  where (n.code=\""+name+"\" OR n.name=\""+name+"\"  OR n.label= \""+name+"\") ");
        }
        queryBy.append(" return n ");

        return cypher(queryBy.toString());
    }

    public List<Map<String, Object>> getMetaDataByLabel(String label){
        StringBuilder queryBy=new StringBuilder("match(n:MetaData) ");
        if(label!=null&&!"".equals(label)){
            queryBy.append("  where n.label=\""+label+"\"  ");
        }
        queryBy.append(" return n ");

        return cypher(queryBy.toString());
    }

    public List<Map<String, Object>> getDataBy(String label, String name){
        StringBuilder queryBy=new StringBuilder("match(n:"+label+") ");
        if(name!=null&&!"".equals(name)){
            try{
                if(Long.valueOf(name)!=null){
                    queryBy.append("  where (id(n)="+name+" OR n.name=\""+name+"\" OR n.code="+name+") ");
                }
            }catch(Exception e){
                queryBy.append("  where (n.code=\""+name+"\" OR n.name=\""+name+"\" ) ");
            }
        }
        queryBy.append(" return n ");
        return cypher(queryBy.toString());
    }

    public List<Map<String, Object>> getDataBy(List<String> labels, String name){
        if(labels.size()>1){
            StringBuilder queryBy=new StringBuilder();
            for(String li : labels){
                if(queryBy.length()>1){
                    queryBy.append(" union all ");
                }
                queryBy.append("match(n:"+li+") ");
                if(name!=null&&!"".equals(name)){
                    queryBy.append("  where (n.code=\""+name+"\" OR n.name=\""+name+"\") ");
                }
                queryBy.append(" return n ");
            }

            return cypher(queryBy.toString());

        }else{
            StringBuilder queryBy=new StringBuilder("match(n:"+labels.get(0)+") ");
            if(name!=null&&!"".equals(name)){
                queryBy.append("  where (n.code=\""+name+"\" OR n.name=\""+name+"\") ");
            }
            queryBy.append(" return n ");
            return cypher(queryBy.toString());
        }

    }

    public List<Map<String, Object>> getDataBy(String name){
        StringBuilder queryBy=new StringBuilder("match(n) ");
        if(name!=null&&!"".equals(name)){
            queryBy.append("  where ( n.code=\""+name+"\" OR n.name=\""+name+"\" ");
            try{
                if(Long.valueOf(name)!=null){
                    queryBy.append("   OR id(n)="+name);
                }
            }catch(Exception e){
//			LoggerTool.error(logger,e.getMessage(),e);
            }
            queryBy.append(")");
        }
        queryBy.append(" return distinct n ");

        return cypher(queryBy.toString());
    }

    public List<Map<String, Object>> queryDataBy(String name){
        StringBuilder queryBy=new StringBuilder("match(n) ");
        if(name!=null&&!"".equals(name)){
            queryBy.append("  where (n.code  CONTAINS '"+name+"' OR n.name CONTAINS  '"+name+"') ");
        }
        queryBy.append(" return distinct n ");

        return cypher(queryBy.toString());
    }

    public Long getIdBy(String name){
        StringBuilder queryBy=new StringBuilder("match(n) ");
        if(name!=null&&!"".equals(name)){
            queryBy.append("  where (n.code=\""+name+"\" OR n.name=\""+name+"\") ");
        }
        queryBy.append(" return distinct id(n) as id ");

        List<Map<String, Object>> query=cypher(queryBy.toString());
        if(query!=null&&query.size()>0){
            return id(query.get(0));
        }
        return null;
    }


    public Node getNodeById(String nodeId){
        return getNodeById(Long.valueOf(nodeId));
    }

    public Node getNodeById(Long valueOf){
        Node findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=myDriver.getNodeById(valueOf);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return findNode;
    }

    public Map<String, Object> visualById(Long valueOf){
        Map<String, Object> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=myDriver.getNodePropertiesById(valueOf);
                visulRelation(findNode);
            }catch(Exception e){
                logException(e);
            }
        }else{

        }
        return findNode;
    }

    public void visualRelList(List<Map<String, Object>> dis){
        for(Map<String, Object> di : dis){
            visulRelation(di);
        }
    }

    /**
     * 可视化节点的关系数据
     *
     * @param findNode
     */
    public void visulRelation(Map<String, Object> findNode){
        if(findNode!=null){
            // UNWIND labels(m) as x
            String relationNodeName="Match(n)-[r]->(m)   ";
            Long nodeId=id(findNode);
            String name=name(findNode);
            if(nodeId!=null){
                relationNodeName=relationNodeName+" where  id(n)="+nodeId
                        +" and  ['MetaData'] <> labels(m)";
            }

            relationNodeName=relationNodeName+" return id(m) as mId,labels(m) AS mLabel,type(r) as rName,m";
            List<Map<String, Object>> relations=cypher(relationNodeName);
            if(relations!=null&&!relations.isEmpty()){
                StringBuilder rels=null;
                Map<String, StringBuilder> dis=new HashMap<>();
                // 'MetaData' not in labels(m)
                for(Map<String, Object> di : relations){
                    String relName=string(di, "rName");
                    if(!dis.containsKey(relName)){
                        rels=new StringBuilder();
                        dis.put(relName, rels);
                    }else{
                        rels=dis.get(relName);
                    }

                    // String reli = "<a href=\"[(${MODULE_NAME})]/layui/" + oneLabel(di, "mLabel")
                    // + "/documentRead?id="+string(di,"mId")+"\">" + name(di) + "</a>";
                    String oneLabel=oneLabel(di, "mLabel");
                    String nodeName;
                    if(oneLabel.equals(LABEL_FIELD)){
                        nodeName="【"+string(di, FIELD)+"】";
                    }else{
                        String name2=name(di);
                        if(oneLabel.equals(META_DATA)){
                            nodeName="【"+label(di)+"】";
                            if(name2!=null||"null".equals(name2)){
                                nodeName=nodeName+name2;
                            }
                        }else{
                            nodeName=showNode(di, oneLabel);
                        }
                    }

                    String reli="<a href=\"javascript:;\" onclick=\"window.open('"+LemodoApplication.MODULE_NAME+"/layui/"+oneLabel
                            +"/documentRead?id="+string(di, "mId")+"')\">"+nodeName+"</a>";
                    if(rels.length()>1){
                        rels.append("、");
                    }
                    rels.append(reli);
                }
                StringBuilder orel=new StringBuilder();
                for(Entry<String, StringBuilder> ei : dis.entrySet()){
                    if(orel.length()>1){
                        orel.append("\n");
                    }
                    orel.append(ei.getKey()+"("+ei.getValue().toString()+";)\n");
                }

                findNode.put("出关系", orel.toString());
            }else{
                outRelation(findNode, nodeId);
            }
            // UNWIND labels(m) as x
            String relationInName="Match(n)<-[r]-(m) ";
            if(nodeId!=null){
                relationInName=relationInName+" where  id(n)="+nodeId
                        +" and ['MetaData'] <> labels(m)";
            }

            relationInName=relationInName+" return id(m) as mId,labels(m) AS mLabel,type(r) as rName,m";
            List<Map<String, Object>> inRelations=cypher(relationInName);
            if(inRelations!=null&&!inRelations.isEmpty()){
                StringBuilder rels=null;
                Map<String, StringBuilder> dis=new HashMap<>();
                // 'MetaData' not in labels(m)
                for(Map<String, Object> di : inRelations){
                    String relName=string(di, "rName");
                    if(!dis.containsKey(relName)){
                        rels=new StringBuilder();
                        dis.put(relName, rels);
                    }else{
                        rels=dis.get(relName);
                    }

                    // String reli = "<a href=\"[(${MODULE_NAME})]/layui/" + oneLabel(di, "mLabel")
                    // + "/documentRead?id="+string(di,"mId")+"\">" + name(di) + "</a>";
                    String oneLabel=oneLabel(di, "mLabel");
                    String nodeName;
                    if(oneLabel.equals(LABEL_FIELD)){
                        nodeName="【"+string(di, FIELD)+"】";
                    }else{
                        String name2=name(di);
                        if(oneLabel.equals(META_DATA)){
                            nodeName="【"+label(di)+"】";
                            if(name2!=null||"null".equals(name2)){
                                nodeName=nodeName+name2;
                            }
                        }else{
                            nodeName=showNode(di, oneLabel);
                        }
                    }

                    String reli="<a href=\"javascript:;\" onclick=\"window.open('"+LemodoApplication.MODULE_NAME+"/layui/"+oneLabel
                            +"/documentRead?id="+string(di, "mId")+"')\">"+nodeName+"</a>";
                    if(rels.length()>1){
                        rels.append("、");
                    }
                    rels.append(reli);
                }
                StringBuilder orel=new StringBuilder();
                for(Entry<String, StringBuilder> ei : dis.entrySet()){
                    if(orel.length()>1){
                        orel.append("\n");
                    }
                    orel.append(ei.getKey()+"("+ei.getValue().toString()+";)\n");
                }

                findNode.put("入关系", orel.toString());
            }else{
                inRelation(findNode, nodeId);
            }
        }
    }

    /**
     * 查看节点数据的可视化文本
     *
     * @param mapData
     * @return
     */
    public String seeNodeText(Map<String, Object> mapData){
        //本就有name字段，且值不为空的直接返回
        String nameStr=name(mapData);
        if(nameStr!=null){
            return nameStr;
        }
        String metaLabel=label(id(mapData));
        Map<String, Object> metaData=getAttMapBy(LABEL, metaLabel, META_DATA);
        String shortCol=string(metaData, "shortShow");
        //缩写字段
        if(shortCol!=null){
            return string(mapData, shortCol);
        }
        //默认使用第一个字段
        String[] columns2=columns(metaData);
        if(columns2==null){
            return null;
        }
        return string(mapData, columns2[1]);
    }

    public String seeNode(Map<String, Object> mapData){
        String nameStr=name(mapData);
        if(nameStr!=null){
            return linkNode(nameStr, mapData);
        }

        if(mapData.containsKey("relProps")){//关系数据
            Map<String, Object> relProps=mapObject(mapData, "relProps");
            Set<String> relTypes=stringSet(mapData, "relType");
            Map<String, Object> endNodeProperties=mapObject(mapData, "endNodeProperties");
            String name2=name(endNodeProperties);
            if(name2!=null){
                if(!relTypes.contains("")){
                    return linkNode(name2, mapData);
                }else{

                }
            }
            return showNode(endNodeProperties);
        }
        return showNode(mapData);
    }

    public String showNode(Map<String, Object> mapData){
        String nodeLabelByNodeId=label(id(mapData));
        Map<String, Object> metaData=getAttMapBy(LABEL, nodeLabelByNodeId, META_DATA);
        String shortCol=string(metaData, "shortShow");

        if(shortCol!=null){
            return linkNode(string(mapData, shortCol), mapData);
        }
        String[] columns2=columns(metaData);
        if(columns2==null){
            return null;
        }
        String string=string(mapData, columns2[1]);
        return linkNode(string, mapData);
    }

    /**
     * 将某个节点的数据链接化
     *
     * @param name
     * @param mapData
     * @return
     */
    public String linkNode(String name, Map<String, Object> mapData){
        String nameStr=null;
        if(name==null){
            nameStr=name(mapData);
        }else{
            nameStr=name;
        }


        String nodeLabelByNodeId=getNodeLabelByNodeId(id(mapData));
        String link=LemodoApplication.MODULE_NAME+"/layui/"+nodeLabelByNodeId+"/"+id(mapData)+"/documentRel";

        if(nameStr!=null){
            return "<a href=\"javascript:;\" onclick=openNode('"+nameStr+"','"+link+"')>"+nameStr+"</a>";
        }
        Map<String, Object> metaData=getAttMapBy(LABEL, nodeLabelByNodeId, META_DATA);


        String shortCol=string(metaData, "shortShow");

        if(shortCol!=null){
            String title=string(mapData, shortCol);
            return "<a href=\"javascript:;\" onclick=openNode('"+title+"','"+link+"')>"+title+"</a>";
        }
        String[] columns2=columns(metaData);
        if(columns2==null){
            return null;
        }
        String title=string(mapData, columns2[1]);
        return "<a href=\"javascript:;\" onclick=openNode('"+title+"','"+link+"')>"+title+"</a>";
    }

    public String showNode(Map<String, Object> di, String oneLabel){
        String nodeName;

        String name2=name(di);
        Map<String, Object> metaData=getAttMapBy(LABEL, oneLabel, META_DATA);
        String[] shortCols=splitValue(metaData, "shortShow");
        String[] showCols=splitValue(metaData, "show");
        StringBuilder sb=new StringBuilder();
        if(shortCols!=null){
            for(String si : shortCols){
                String string=string(di, si);
                if(string==null){
                    continue;
                }
                if(sb.length()>0){
                    sb.append(",");
                }

                sb.append(string);
            }
        }

        if(sb.length()<1){
            if(showCols!=null){
                for(String si : showCols){
                    String string=string(di, si);
                    if(string==null){
                        continue;
                    }
                    if(sb.length()>0){
                        sb.append(",");
                    }
                    sb.append(string);
                }
            }
        }

        if(sb.length()>1){
            nodeName="【"+sb.toString()+"】";
        }else{
            nodeName="【"+name2+"】";
        }
        return nodeName;
    }

    public Map<String, Object> onlyShowCol(Map<String, Object> di, String oneLabel){
        Map<String, Object> copy=null;
        Map<String, Object> metaData=getAttMapBy(LABEL, oneLabel, META_DATA);
        String[] showCols=splitValue(metaData, "show");
        // String metaName = name(metaData);
        StringBuilder sb=new StringBuilder();
        if(sb.length()<1){
            if(showCols!=null){
                for(String si : showCols){
                    if(sb.length()>0){
                        sb.append(",");
                    }
                    sb.append(string(di, si));
                }
                copy=copyWithKeys(di, showCols);
            }
        }
        if(sb.length()<1){
            return null;
        }
        if(!copy.containsKey(ID)){
            copy.put(ID, id(di));
        }
        String name2=name(di);
        if(name2!=null&&!copy.containsKey(NAME)){
            copy.put(NAME, name2);
        }
        // copy.put("metaName", metaName);
        return copy;

    }

    private void inRelation(Map<String, Object> findNode, Long nodeId){
        List<Map<String, Object>> allInRelations=allInRelations(nodeId);
        if(allInRelations==null||allInRelations.isEmpty()){
            return;
        }
        StringBuilder rels=null;
        Map<String, StringBuilder> dis=new HashMap<>();
        for(Map<String, Object> di : allInRelations){
            Map<String, Object> startObject=mapObject(di, "startNodeProperties");
            String startName=name(startObject);
            String startLabel=label(startObject);

            if(startLabel==null){
                Set<String> stringSet=stringSet(di, "startLabels");
                String[] els=new String[stringSet.size()];
                stringSet.toArray(els);
                startLabel=els[0];

                Map<String, Object> metaData=getAttMapBy("label", startLabel, META_DATA);
                if(startName==null){
                    if(startLabel.equals(LABEL_FIELD)){
                        startName="【自定义"+name(metaData)+"】"+string(startObject, FIELD);
                    }else{
                        startName="【"+name(metaData)+"】"+string(startObject, "content");
                    }

                }else{
                    startName="【"+name(metaData)+"】"+startName;
                }
            }
            Map<String, Object> end=mapObject(di, "endNodeProperties");
            String endName=name(end);
            String relName=string(di, "relType");
            String endLabel=label(end);
            if(endLabel==null){
                Set<String> stringSet=stringSet(di, "endLabels");
                String[] els=new String[stringSet.size()];
                stringSet.toArray(els);
                endLabel=els[0];
            }
            Map<String, Object> relProp=mapObject(di, "relProps");
            if(relProp!=null&&!relProp.isEmpty()){
                relName=name(relProp);
                if(relName==null){
                    relName=label(relProp);
                }
            }

            if(!dis.containsKey(relName)){
                rels=new StringBuilder();
                dis.put(relName, rels);
            }else{
                rels=dis.get(relName);
            }
            if(rels.length()>1){
                rels.append("、");
            }
            String reli="<a href=\"javascript:;\" onclick=\"window.open('"+LemodoApplication.MODULE_NAME+"/layui/"+startLabel
                    +"/documentRead?id="+id(startObject)+"')\">"+startName+"</a>";
            rels.append(reli);
        }

        StringBuilder orel=new StringBuilder();
        for(Entry<String, StringBuilder> ei : dis.entrySet()){
            if(orel.length()>1){
                orel.append("\n");
            }
            orel.append(ei.getKey()+"("+ei.getValue().toString()+";)\n");
        }

        if(orel.length()>0){
            findNode.put("入关系", orel.toString());
        }
    }

    private void outRelation(Map<String, Object> findNode, Long nodeId){
        List<Map<String, Object>> outRelations=outRelations(nodeId);
        StringBuilder rels=null;
        Map<String, StringBuilder> dis=new HashMap<>();
        for(Map<String, Object> di : outRelations){
            String startName=name(mapObject(di, "startNodeProperties"));
            Map<String, Object> end=mapObject(di, "endNodeProperties");
            Map<String, Object> relProp=mapObject(di, "relProps");
            String relName=null;

            if(relProp!=null&&!relProp.isEmpty()){
                relName=name(relProp);
                if(relName==null){
                    string(di, "relType");
                }
                if(relName==null){
                    relName=label(relProp);
                }
            }

            if(!dis.containsKey(relName)){
                rels=new StringBuilder();
                dis.put(relName, rels);
            }else{
                rels=dis.get(relName);
            }

            String endName=name(end);
            if(endName==null){
                endName=mapString(end);
            }
            String endLabel=label(end);
            if(endLabel==null){
                Set<String> stringSet=stringSet(di, "endLabels");
                String[] els=new String[stringSet.size()];
                stringSet.toArray(els);
                endLabel=els[0];
            }

            if(rels.length()>0){
                rels.append("\n");
            }
            String reli="<a href=\"javascript:;\" onclick=\"window.open('"+LemodoApplication.MODULE_NAME+"/layui/"+endLabel+"/documentRead?id="
                    +id(end)+"')\">"+endName+"</a>";
            if(rels.length()>1){
                rels.append("、");
            }
            rels.append(reli);
        }

        StringBuilder orel=new StringBuilder();
        for(Entry<String, StringBuilder> ei : dis.entrySet()){
            if(orel.length()>1){
                orel.append("\n");
            }
            String value=ei.getValue().toString();
            if(value.indexOf("、")>0){
                orel.append(ei.getKey()+":"+value+";\n");
            }else{
                orel.append(ei.getKey()+":"+value+"、");
            }

        }

        if(orel.length()>0){
            findNode.put("出关系", orel.toString());
        }
    }

    private void logException(Exception e){
        LoggerTool.debug(logger, "Excetion : ", e);
        debugLog.debug("Excetion : ", e);
    }

    public Map<String, Object> getNodeMapById(Long id){
        Map<String, Object> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=myDriver.getNodePropertiesById(id);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return findNode;
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
        Map<String, Object> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=myDriver.findNodeAttMap(key, value, label);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return findNode;
    }

    public Map<String, Object> getPropMapByNode(Node node){
        return getPropMapByNodeId(node.getId());
    }

    public Map<String, Object> getPropMapBy(String nodeId){
        return getPropMapByNodeId(Long.valueOf(nodeId));
    }

    public Map<String, Object> getLablePropBy(String nodeId){
        return getPropLabelByNodeId(Long.valueOf(nodeId));
    }

    /**
     * 根据定义节点的ID获取节点的Label
     *
     * @param nodeId
     * @return
     */
    public String getLabelByNodeId(Long nodeId){
        return String.valueOf(getPropValueByNodeId(nodeId, LABEL));
    }

    public Map<String, Object> getMetaDataById(Long nodeId){
        return getAttMapBy(LABEL, getLabelByNodeId(nodeId), META_DATA);
    }

    public Map<String, String> getColHeadById(Long nodeId){
        return colName(getMetaDataById(nodeId));
    }

    public Map<String, String> getNameColById(Long nodeId){
        return nameColumn(getMetaDataById(nodeId));
    }

    /**
     * 根据id和属性名称，获取属性值
     *
     * @param nodeId
     * @param key
     * @return value
     */
    public Object getPropValueByNodeId(Long nodeId, String key){
        if(isNeo4jConnectionUp()){
            try{
                return myDriver.getNodePropValueById(nodeId, key);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return null;
    }

    private void logDownInfo(){
        LoggerTool.debug(logger, "Driver or Session is down, check the configuration");
        debugLog.debug("Driver or Session is down, check the configuration");
    }

    public Map<String, Object> getPropMapByNodeId(Long nodeId){
        Map<String, Object> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=myDriver.getNodePropertiesById(nodeId);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return findNode;
    }

    public Map<String, Object> getPropLabelByNodeId(Long nodeId){
        Map<String, Object> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=myDriver.getLabelAndPropertiesById(nodeId);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
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
    public Node getNodeByPropAndLabel(JSONObject props, String label){
        Node findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=myDriver.queryNode(props, label);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
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
        return myDriver.getAllOutgoings(props, label);
    }

    public List<Map<String, Object>> getOutRelations(Map<String, Object> props, String label){
        return myDriver.getAllOutgoings(props, label);
    }

    public List<Map<String, Object>> queryRelationDefine(String key, String label){
        return myDriver.queryBy(key, label, RELATION_DEFINE);
    }

    public List<Map<String, Object>> getSomeRelationEndNodeId(Map<String, Object> props, String label,
                                                              List<String> endLabel){
        StringBuilder queryEndId=new StringBuilder();
        queryEndId.append("match(n:"+label+")-[r]->(m) where id(n)="+props.get("id"));
        queryEndId.append(" and any(label in labels(m) WHERE label in ['"+String.join("','", endLabel)+"'])");
        queryEndId.append(" return id(m) AS eId,r.name AS rName,r.label AS rLabel,labels(m) AS label");
        return myDriver.queryData(queryEndId.toString());
    }

    public List<Map<String, Object>> getEndNodeIdByIdIgnoreRrelation(String id, String label, String rLabel){
        List<String> relationLabelList=new ArrayList<String>();
        if(rLabel!=null){
            relationLabelList.add(rLabel);
        }
        return getEndNodeIdById(id, label, relationLabelList);
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
        return myDriver.queryData(queryEndId.toString());
    }

    public List<Map<String, Object>> getRuleInfoById(Map<String, Object> dataMap){
        return getRuleInfo(dataMap, null);
    }

    public List<Map<String, Object>> getRuleInfo(Map<String, Object> dataMap, String label){
        StringBuilder queryEndId=new StringBuilder();
        queryEndId.append("match p= (n");
        if(label!=null){
            queryEndId.append(":"+label);
        }
        queryEndId.append(")-[r]->(m:Rule)");
        if(dataMap.containsKey("id")&&dataMap.get("id")!=null
                &&!String.valueOf(dataMap.get("id")).trim().equals("")){
            queryEndId
                    .append(" where id(n)="+dataMap.get("id")+" AND NONE (x IN nodes(p) WHERE x.global = \"on\")");
        }
        queryEndId.append(" return distinct id(m) AS id,m.rulekey as ruleKey,m.content AS content");
        return myDriver.queryData(queryEndId.toString());
    }

    public List<Map<String, Object>> getGlobalRuleInfo(){
        StringBuilder queryEndId=new StringBuilder();
        queryEndId.append("match(n:Rule)");
        queryEndId.append(" where n.global=\"on\"");
        queryEndId.append(" return id(n) AS id,n.rulekey as ruleKey,n.content AS content");
        return myDriver.queryData(queryEndId.toString());
    }

    public List<Map<String, Object>> getEndNodeIdById(String id, String label, List<String> rLabel){
        StringBuilder queryEndId=new StringBuilder();
        queryEndId.append("match(n:"+label+")-[r]->(m) where id(n)="+id);
        for(String ri : rLabel){
            queryEndId.append("  and not ((n)-[r:"+ri+"]->(m))");
        }
        if(rLabel.contains("btn")){
            queryEndId.append(" and not ((n)-[r]->(m:layTableToolOpt))");
        }
        queryEndId.append(" return id(m) AS eId,r.name AS rName,r.label AS rLabel,type(r) AS rType,labels(m) AS label");
        return myDriver.queryData(queryEndId.toString());
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
        return myDriver.queryData(queryEndId.toString());
    }

    public List<Map<String, Object>> getRelationOneList(Map<String, Object> props, String label, String rLabel){
        return myDriver.getOneTypeOutgoings(props, label, rLabel);
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
        return myDriver.getOneTypeOutgoings(props, label, rLabel);
    }

    public List<Long> getEndIdOf(Map<String, Object> props, String label, String rLabel){
        return myDriver.getEndNodeId(label, rLabel, props);
    }

    /**
     * 获取某个节点的额一种关系数据
     *
     * @param startId
     * @param rLabel
     * @return
     */
    public List<Map<String, Object>> getRelationDataOf(Long startId, String rLabel){
        return myDriver.getOneTypeOutgoings(startId, rLabel);
    }

    public List<Map<String, Object>> getChildrens(Map<String, Object> props, String label){
        List<Map<String, Object>> oneTypeOutgoings=myDriver.getOneTypeOutgoings(props, label, REL_TYPE_CHILDREN);
        if(oneTypeOutgoings.isEmpty()){
            oneTypeOutgoings=myDriver.getOneTypeOutgoings(props, label, REL_TYPE_CHILDRENS);
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
        return myDriver.getAllDefineRelationList(props, label);
    }

    public String getRelationName(String startLabel, String label, String reLabel){
        Map<String, Object> props=new HashMap<>();
        props.put(LABEL, startLabel);
        return myDriver.getRelationName(props, label, reLabel);
    }

    /**
     * 获取所有入关系数据,关系属性和关系节点数据
     *
     * @param props
     * @param label
     * @return
     */
    public List<Map<String, Object>> getInRelations(Map<String, Object> props, String label){
        return myDriver.getIncomings(props, label);
    }

    /**
     * 获取指定label的出关系
     *
     * @param props
     * @param label
     * @param relationLabel
     * @return
     */
    public List<Map<String, Object>> getOutRelationList(Map<String, Object> props, String label, String relationLabel){
        return myDriver.getTheRelation(props, label, relationLabel);

    }

    /**
     * 获取出关系的所有标签
     *
     * @param props
     * @param label
     * @return
     */
    public List<String> getOutRLabelList(Map<String, Object> props, String label){
        return myDriver.getOutgoingsLabel(props, label);

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
                return myDriver.getTreeRootMap(label);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return null;
    }

    public Map<String, Object> nextOneLevelChildren(String label, Map<String, Object> queryMap){
        if(isNeo4jConnectionUp()){
            try{
                return myDriver.getALevelChildren(label, queryMap);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
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
                return myDriver.getChildrenList(label, queryMap);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return null;
    }

    public List<Map<String, Object>> chidrenlList(String label, Map<String, Object> queryMap){
        if(isNeo4jConnectionUp()){
            try{
                return myDriver.childrenList(label, queryMap);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return null;
    }

    public List<Map<String, Object>> listAllByLabel(String label){
        return listDataByLabel(label);
    }

    /**
     * 清晰定义：每一个接口，不模糊与混乱。
     *
     * @param label
     * @return
     */
    public List<Map<String, Object>> listDataByLabel(String label){
        Map<String, Object> md=getAttMapBy(LABEL, label, META_DATA);
        if(md==null||md.isEmpty()){
            return null;
        }
        String query=optByUserSevice.listAllObject(null, label, columns(md)).toString();

        List<Map<String, Object>> dataList=cypher(query);

        return dataList;
    }

    public List<Long> endNodeIdList(String label, String rLabel, Map<String, Object> queryMap){
        if(isNeo4jConnectionUp()){
            try{
                return myDriver.getEndNodeId(label, rLabel, queryMap);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return null;
    }

    public Map<String, Object> getWholeTree(String label){
        Map<String, Object> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=myDriver.getWholeTree(label);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return findNode;
    }

    public Map<String, Object> getWholeTreeWithColumn(String label, String[] columns){
        Map<String, Object> findNode=null;
        if(isNeo4jConnectionUp()){
            try{
                findNode=myDriver.getWholeTreeWithColumn(label, columns);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return findNode;
    }

    /*
     * Check Neo4j Connection
     */
    public boolean isNeo4jConnectionUp(){
        return myDriver.getInstance()!=null;
    }

    public List<Map<String, Object>> voQuery(String query){
        List<Map<String, Object>> queryData=null;
        if(isNeo4jConnectionUp()){
            try{
                // Insert query on Neo4j graph DB
                queryData=myDriver.voQueryData(query);
                LoggerTool.info(logger, "execute: "+query);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return queryData;
    }

    public JSONArray relation(String query){
        JSONArray queryData=null;
        if(isNeo4jConnectionUp()){
            try{
                // Insert query on Neo4j graph DB
                queryData=myDriver.relationData(query);
                LoggerTool.info(logger, "Insertion Query: "+query);
            }catch(Exception e){
                logException(e);
            }
        }else{
            logDownInfo();
        }
        return queryData;
    }

    /**
     * 根据前端数据，创建实体
     *
     * @param <T>
     * @param priMap
     * @param t
     * @return
     */
    public <T> Node save(Map<String, Object> priMap, T t){
        Node createNode=null;
        if(priMap.containsKey("name")){
            Node findNode=myDriver.findNodeByName(String.valueOf(priMap.get("name")), t.getClass().getSimpleName());
            if(findNode!=null){
                createNode=findNode;
            }
        }else{
            createNode=myDriver.createNode(priMap, Label.label(t.getClass().getSimpleName()));
        }
        return createNode;
    }

    public void validRule(String label, Map<String, Object> vo){
        if(vo==null){
            return;
        }
        if(globalRuleInfo==null){
            globalRuleInfo=getGlobalRuleInfo();
        }
        Map<String, Object> copy=copy(vo);
        if(copy==null){
            return;
        }
        List<Map<String, Object>> ruleInfo=getRuleInfo(vo, label);
        if(label!=null){

            copy.put(LABEL, label);
            excuteRule(copy, globalRuleInfo, ruleInfo);
            if(!META_DATA.equals(label)){
                vo.remove(LABEL);
            }

        }else{
            excuteRule(copy, globalRuleInfo, ruleInfo);
        }
    }

    public void validRule(String label, Map<String, Object> vo, Map<String, Object> po){
        if(globalRuleInfo==null){
            globalRuleInfo=getGlobalRuleInfo();
        }

        List<Map<String, Object>> ruleInfo=getRuleInfoById(po);
        excuteRule(vo, globalRuleInfo, ruleInfo);
    }

    public void refreshGlobalRule(){
        globalRuleInfo=null;
    }

    private void excuteRule(Map<String, Object> vo, List<Map<String, Object>> globalRuleInfo,
                            List<Map<String, Object>> ruleInfo){
        if(ruleInfo==null){
            ruleInfo=new ArrayList<>();
        }
        if(globalRuleInfo!=null&&!globalRuleInfo.isEmpty()){
            for(Map<String, Object> gi : globalRuleInfo){
                if(!ruleInfo.contains(gi)){
                    ruleInfo.add(gi);
                }
            }

        }
        if(ruleInfo!=null&&!ruleInfo.isEmpty()){
            validAdminService();
            drools.execute(ruleInfo, vo);
        }
    }

    private void validAdminService(){
        if(drools.getAdminService()==null){
            drools.setAdminService(adminService);
        }
    }

    /**
     * 根据Id保存属性
     *
     * @param key
     * @return
     */
    public void saveById(String id, String key, String value){
        if(StringUtils.isNotBlank(key)){
            Map<String, Object> priMap=new HashMap<>();
            priMap.put(key, value);
            saveById(id, priMap);
        }
    }

    @ServiceLog(description = "update node prop ")
    public void updateBy(Long id, String key, String value){

//	Map<String, Object> priMap = new HashMap<>();
//	    priMap.put(key, value);
//	    update(id, priMap);
        execute("MATCH(n)  where id(n)="+id+" set n."+key+"="+value);
    }

    public void saveById(String id, Map<String, Object> priMap){
        if(!priMap.isEmpty()){
            if(!priMap.containsKey(NODE_ID)){
                priMap.put(NODE_ID, id);
            }
            Long valueOf=Long.valueOf(id);
            validRule(null, priMap);
            update(valueOf, priMap);
        }
    }

    public void update(Long id, Map<String, Object> body){
        Node queryNode=myDriver.getNodeById(id);
        myDriver.updateNode(body, queryNode);
    }

    public void update(Node queryNode, Map<String, Object> body){
        myDriver.updateNode(body, queryNode);
    }

    public void update(Map<String, Object> body){
        Long id=id(body);
        if(id!=null){
            Node queryNode=myDriver.getNodeById(id);
            myDriver.updateNode(body, queryNode);
        }

    }

    public Node copy(Map<String, Object> priMap, String label, String key){
        Object idObject=priMap.get("id");
        Map<String, Object> nodePropertiesById=myDriver.getNodePropertiesById(Long.valueOf(String.valueOf(idObject)));
        copy(nodePropertiesById, "name");
        copy(nodePropertiesById, "code");
        nodePropertiesById.remove("id");

        return myDriver.createNode(nodePropertiesById, Label.label(label));
    }

    public void changeLabelById(Long id, String label){
        myDriver.changeLabelById(id, label);

    }


    private void copy(Map<String, Object> priMap, String key2){
        if(priMap.containsKey(key2)){
            priMap.put(key2, priMap.get(key2)+"(副本)");
        }
    }

    public Node saveByBody(Map<String, Object> priMap, String label){
        Node createNode=null;
        Node queryNode=null;
        String dataLabel=label(priMap);
        if(META_DATA.equals(label)){
            queryNode=myDriver.findNode(NODE_LABEL, dataLabel, label);
        }else if(RELATION_DEFINE.equals(label)){
            Map<String, Object> ppMap=new HashMap<>();
            ppMap.putAll(priMap);
            ppMap.remove("name");
            queryNode=myDriver.queryNode(ppMap, label);
        }else if(EmailDomain.EMAIL.equals(label)){
            Map<String, Object> ppMap=new HashMap<>();
            ppMap.putAll(priMap);
            ppMap.remove("name");
            ppMap.remove(EmailDomain.EMAIL_CONTENT);
            queryNode=myDriver.queryNode(ppMap, label);
        }else{
            Map<String, Object> nameMap=new HashMap<>();
            for(Entry<String, Object> eni : priMap.entrySet()){
                String key=eni.getKey();
                Object value=eni.getValue();
                if((key.indexOf("Name")>=0||key.indexOf("name")>=0)&&value!=null
                        &&!"".equals(String.valueOf(value).trim())){
                    nameMap.put(key, value);
                }
            }
            if(!nameMap.isEmpty()){
                queryNode=myDriver.queryNode(nameMap, label);
            }else{
                queryNode=myDriver.queryNode(priMap, label);
            }
        }

        if(queryNode!=null){
            LoggerTool.info(logger, "query use exist node"+queryNode.getId());
            createNode=queryNode;
            Boolean update=false;
            Map<String, Object> nodeProperties=myDriver.getNodeProperties(queryNode);
            for(Entry<String, Object> eni : priMap.entrySet()){
                String attKey=eni.getKey();
                Object attValue=eni.getValue();
                Boolean valueNotEqual=!nodeProperties.containsKey(attKey)||attValue!=null
                        &&(!nodeProperties.containsValue(attValue)||!attValue.equals(nodeProperties.get(attKey)));
                if(valueNotEqual){
                    update=true;
                }
            }
            if(update){
                validRule(label, priMap);
                LoggerTool.info(logger, "update node"+queryNode.getId());
                myDriver.updateNode(priMap, queryNode);
            }

        }else{
            validRule(label, priMap);
            LoggerTool.info(logger, "\ncreate "+label+"Node"+priMap);
            createNode=myDriver.createNode(priMap, Label.label(label));
        }
        return createNode;
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
            Node queryNode=myDriver.queryNode(findeNode);
            if(queryNode!=null){
                myDriver.updateNode(priMap, queryNode);
                return queryNode;
            }else{
                createNode=myDriver.createNode(priMap, Label.label(lname));
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
        return myDriver.getNodeProperties(saveNode);
    }

    public Node update(Map<String, Object> priMap, String lname, String[] keys){
        String update=Neo4jOptCypher.update(priMap, lname, keys);
        myDriver.queryData(update);
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
        if(usedIdSet.contains(id)){
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

    public String getBySysCode(String key){

        return getSettingBy(key);
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
            if("staticFile".equals(key)){
                path=runPath+"static";
            }
            if(UI_PUBLISH_PATH.equals(key)){
                path=runPath+"page";
            }
        }else{
            if(path.contains("../")){
                path=path.replaceAll("../", "");
                path=runPath+path;
            }
        }
        if(!path.endsWith(File.separator)){
            return path+File.separator;
        }
        return path;
    }


    public String multiPartFileSave(MultipartFile file, Map<String, Object> fileMap){
        String fileName=file.getOriginalFilename();

        String pathname=saveFileInfo(file, fileMap, fileName);
        return pathname;
    }

    public String saveFileInfo(MultipartFile file, Map<String, Object> fileMap, String fileName){
        String filePath=getPathBy(FILE_STORE_PATH);
        fileMap.put(NAME, fileName);
        fileMap.put(FILE_SIZE, String.valueOf(file.getSize()));
        fileMap.put(FILE_TYPE, file.getContentType());

        Node saveByBody=addNew(fileMap, FILE);
        long id2=saveByBody.getId();
        fileMap.put(ID, id2);
        String pathname=filePath+id2;
        fileMap.put(FILE_STORE_NAME, pathname);
        return pathname;
    }

    public String fileSave(File file, Map<String, Object> fileMap){
        String fileName=file.getName();
        String filePath=getPathBy(FILE_STORE_PATH);

        fileMap.put(NAME, fileName);
        // fileMap.put(FILE_SIZE, String.valueOf(file.));
        fileMap.put(FILE_TYPE, fileName.split("\\.")[1]);

        Node saveByBody=saveByBody(fileMap, FILE);
        long id2=saveByBody.getId();
        fileMap.put(ID, id2);
        String pathname=filePath+id2;
        fileMap.put(FILE_STORE_NAME, pathname);
        return pathname;
    }

    public Map<String, Object> recordFileInfo(File file){
        Map<String, Object> fileMap=new HashMap<>();
        String pathname=fileSave(file, fileMap);
        File dest=new File(pathname);
        if(!dest.exists()){
            dest.mkdirs();
        }

        update(fileMap);
        return fileMap;
    }

    public String fileMetaSave(File file, Map<String, Object> fileMap, String fileType){
        String fileName=file.getName();
        String filePath=file.getAbsolutePath();
        try(FileInputStream fis=new FileInputStream(file);){
            fileMap.put(FILE_SIZE, String.valueOf(fis.available()));
        }catch(FileNotFoundException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }catch(IOException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        fileMap.put(NAME, fileName);

        fileMap.put(FILE_TYPE, fileType);

        Node saveByBody=saveByBody(fileMap, FILE);
        long id2=saveByBody.getId();

        String pathname=filePath+File.separator+id2;
        fileMap.put(FILE_STORE_NAME, pathname);
        return pathname;
    }

    public String goodsPersistSave(String file, Map<String, Object> fileMap){
        String filePath=getBySysCode(FILE_STORE_PATH);
        fileMap.put(FILE_SIZE, String.valueOf(file.getBytes().length));
        fileMap.put(FILE_TYPE, FILE_TYPE_JSON);

        Node saveByBody=saveByBody(fileMap, FILE);
        long id2=saveByBody.getId();

        String pathname=filePath+File.separator+id2+"."+FILE_TYPE_JSON;
        fileMap.put(FILE_STORE_NAME, pathname);
        FileOpt.writeFile(file, pathname);
        update(saveByBody, fileMap);
        return pathname;
    }

    /**
     * 获取开始节点Id为StartId的，终点Label为endLabel的路径终点的数据
     *
     * @param startId
     * @param endLabel
     * @return
     */
    public List<Map<String, Object>> getPathEnds(Long startId, String endLabel){
        String query=Neo4jOptCypher.getPathEnds(startId, endLabel);
        List<Map<String, Object>> pathEnds=cypher(query);
        return pathEnds;
    }

    public List<Map<String, Object>> getPathEnds(String startlabel, Long startId, String endLabel){
        String query=Neo4jOptCypher.getPathEnds(startlabel, startId, endLabel);
        List<Map<String, Object>> pathEnds=cypher(query);
        return pathEnds;
    }

}
