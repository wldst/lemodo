package com.wldst.ruder.crud.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.util.*;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.constant.RuleConstants;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.crud.service.ObjectService;
import com.wldst.ruder.crud.service.TabListShowService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.state.service.StateService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 专注于关系操作，增加，删除，修改，查询。
 *
 * @author wldst
 */
@RestController
@ResponseBody
@RequestMapping("${server.context}/relation/{po}")
@CrossOrigin
public class RelationController extends RuleConstants{
    private static Logger logger=LoggerFactory.getLogger(RelationController.class);
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private RelationService cypherService;
    @Autowired
    private Neo4jOptByUser optByUserSevice;
    @Autowired
    private StateService statusService;
    @Autowired
    private TabListShowService tabService;

    @Autowired
    private HtmlShowService htmlShowService;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private RelationService relationService;

    @Autowired
    @Qualifier("smPersister")
    private StateMachinePersister<Long, Long, String> smPersister;

    public List<Map<String, Object>> loadRules(String name){
        Map<String, Object> bodyMap=new HashMap<>();
        bodyMap.put("ruleKey", name);
        return loadRules(bodyMap);
    }

    public List<Map<String, Object>> loadRules(Map<String, Object> body){
        if(body==null){
            body=new HashMap<>(); // 请求body
        }
        String label="Rule";
        String[] columns;
        try{
            columns=crudUtil.getMdColumns(label);
            String query=Neo4jOptCypher.safeQueryObj(body, label, columns);
            return neo4jService.query(query, body);
        }catch(DefineException e){
            LoggerTool.error(logger, "loadRules", e);
        }
        return null;
    }

    /**
     * 补全主键
     *
     * @param po
     * @return
     */
    private void completePK(Map<String, Object> po){
        Object columnsStr=po.get(COLUMNS);
        Object headerObject=po.get(HEADER);

        String header=null;
        String headerValid=null;
        if(headerObject!=null){
            header=String.valueOf(headerObject);
            String[] hds=crudUtil.getColumns(header);
            headerValid=String.join(",", hds);
        }

        if(columnsStr!=null){
            String column=String.valueOf(columnsStr);
            String[] columns=crudUtil.getColumns(column);
            String columnValid=String.join(",", columns);
            Set<String> cSet=new HashSet<>();
            for(String ci : columns){
                cSet.add(ci);
            }
            if(!cSet.contains(NODE_ID)){
                column=NODE_ID+","+columnValid;
                header="编码,"+headerValid;
            }
            po.put(COLUMNS, column);
        }
        po.put(HEADER, header);
    }

    private String completePK(Map<String, Object> po, String crudColumns, String crudHeader){
        if(po==null){
            return ID;
        }
        Object key=po.get(NODE_ID);
        Object columnsStr=po.get(crudColumns);

        Object headerObject=po.get(crudHeader);

        String crudKey=null;
        String header=null;
        String headerValid=null;
        if(headerObject!=null){
            header=String.valueOf(headerObject);
            String[] hds=crudUtil.getColumns(header);
            headerValid=String.join(",", hds);
        }

        if(columnsStr!=null){
            String column=String.valueOf(columnsStr);
            String[] columns=crudUtil.getColumns(column);
            String columnValid=String.join(",", columns);
            if(key!=null){
                crudKey=String.valueOf(key);
                Set<String> cSet=new HashSet<>();
                for(String ci : columns){
                    cSet.add(ci);
                }
                if(!cSet.contains(crudKey)&&StringUtils.isNotBlank(crudKey)){
                    column=crudKey+","+columnValid;
                    header="编码,"+headerValid;
                }
            }else{
                if(columns.length>0){
                    crudKey=columns[0];
                }
            }
            po.put(crudColumns, column);
        }
        po.put(crudHeader, header);
        return crudKey;
    }

    private WrappedResult validate(String label, PageObject page, Map<String, Object> po){
        String[] splitColumnValue=columns(po);
        String[] headers=headers(po);
        List<Map<String, Object>> fieldInfo=objectService.getFieldInfo(label);
        Map<String, Map<String, Object>> fieldInfoMap=new HashMap<>();
        if(fieldInfo!=null&&!fieldInfo.isEmpty()){
            for(Map<String, Object> fi : fieldInfo){
                fieldInfoMap.put(string(fi, "field"), fi);
            }
        }

        List<Map<String, Object>> fieldsInfo=new ArrayList<>(splitColumnValue.length);

        for(int i=0; i<splitColumnValue.length; i++){
            String columni=splitColumnValue[i];
            String hi=headers[i];
            Map<String, Object> fi=new HashMap<>();
            fi.put("name", columni);
            fi.put("header", hi);
            fi.put("description", hi);
            String string=string(fi, TYPE);
            if(null==string){
                fi.put("type", "string");
            }else{
                fi.put("type", string);
            }

            fieldsInfo.add(fi);
        }

        return ResultWrapper.wrapResult(true, po, page, QUERY_SUCCESS);
    }


    @RequestMapping(value = "/query/{relLabel}/{endLabel}/{instanceId}", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult queryRelEnds(@PathVariable("po") String label,
                                      @PathVariable("endLabel") String endLabel, @PathVariable("relLabel") String relLabel,
                                      @PathVariable("instanceId") String instanceId, @RequestBody JSONObject vo) throws DefineException{
        PageObject page=crudUtil.validatePage(vo);
        String[] columns=crudUtil.getMdColumns(endLabel);
        if(columns==null||columns.length<=0){
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        String query=Neo4jOptCypher.queryByRelInstance(vo, endLabel, relLabel, columns, page, label, instanceId);
        List<Map<String, Object>> query2=neo4jService.cypher(query);
        page.setTotal(crudUtil.total(query, vo));
        return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/children", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult queryChild(@PathVariable("po") String label, @RequestBody JSONObject vo)
            throws DefineException{
        PageObject page=crudUtil.validatePage(vo);
        String[] columns=crudUtil.getMdColumns(label);
        if(columns==null||columns.length<=0){
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        Map<String, Object> nextOneLevelChildren=neo4jService.nextOneLevelChildren(label, vo);
        return ResultWrapper.wrapResult(true, nextOneLevelChildren, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/childrenList", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult childrenList(@PathVariable("po") String label, @RequestBody JSONObject vo)
            throws DefineException{
        PageObject page=crudUtil.validatePage(vo);
        String[] columns=crudUtil.getMdColumns(label);
        if(columns==null||columns.length<=0){
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        List<Map<String, Object>> chidList=neo4jService.chidlList(label, vo);
        if(chidList!=null&&!chidList.isEmpty()){
            page.setPageSize(chidList.size());
            page.setTotal(chidList.size());
        }

        return ResultWrapper.wrapResult(true, chidList, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/childList/{endLabel}", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult childList(@PathVariable("po") String label, @PathVariable("endLabel") String endLabel,
                                   @RequestBody JSONObject vo) throws DefineException{
        PageObject page=crudUtil.validatePage(vo);
        if(vo.isEmpty()){
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        String[] columns=crudUtil.getMdColumns(endLabel);
        if(columns==null||columns.length<=0){
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        JSONObject jo=new JSONObject();
        jo.put("id", vo.getString("id"));
        StringBuilder labelChild=optByUserSevice.getLabelChild(page, jo, label, endLabel, columns);

        String query=labelChild.toString();
        List<Map<String, Object>> chidList=neo4jService.cypher(query);
        if(chidList!=null&&!chidList.isEmpty()){
            page.setTotal(crudUtil.total(query));
        }else{
            page.setTotal(0);
        }

        return ResultWrapper.wrapResult(true, chidList, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/getEndId/{relation}", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public List<Long> getEndId(@PathVariable("po") String label, @PathVariable("relation") String relation,
                               @RequestBody JSONObject vo) throws DefineException{
        return neo4jService.endNodeIdList(label, relation, vo);
    }

    @RequestMapping(value = "/rel/{relation}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult relations(@PathVariable("po") String label, @PathVariable("relation") String relation,
                                   @RequestBody JSONObject vo) throws DefineException{
        PageObject page=crudUtil.validatePage(vo);

        String query=crudUtil.relationQuery(label, relation, vo, page);
        page.setTotal(crudUtil.total(query));
        JSONArray query2=neo4jService.relation(query);
        return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
    }
    @RequestMapping(value = "/outRels", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult outRels(@PathVariable("po") String label,
                                   @RequestBody JSONObject vo) throws DefineException{
        PageObject page=crudUtil.validatePage(vo);
        List<Map<String, Object>> relationQuery = relationService.relationQuery(label, vo);
	return ResultWrapper.wrapResult(true, relationQuery, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/oneRel/{endLabel}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult oneRel(@PathVariable("po") String label, @PathVariable("endLabel") String endLabel,
                                @RequestBody JSONObject vo) throws DefineException{
        String query=crudUtil.oneEndRelationQuery(label, endLabel, vo);
        JSONArray query2=neo4jService.relationOne(query);
        return ResultWrapper.wrapResult(true, query2, null, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/oneRelPage/{endLabel}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult oneRelPage(@PathVariable("po") String label, @PathVariable("endLabel") String endLabel,
                                    @RequestBody JSONObject vo) throws DefineException{
        PageObject page=crudUtil.validatePage(vo);

        String query=crudUtil.oneEndRelationQuery(label, endLabel, vo, page);
        page.setTotal(crudUtil.total(query, vo));
        JSONArray query2=neo4jService.relationOne(query);
        return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/rel/{relation}/del", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult relationsDel(@PathVariable("po") String label, @PathVariable("relation") String relation,
                                      @RequestBody JSONObject vo) throws DefineException{
        boolean delRelation=neo4jService.delRelation(vo.getString("startId"), vo.getString("endId"), label, relation);
        return ResultWrapper.ret(true, delRelation, DELETE_SUCCESS);
    }

    @RequestMapping(value = "/rel/{relation}/save", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult relSave(@PathVariable("po") String label, @PathVariable("relation") String relation,
                                 @RequestBody JSONObject vo) throws DefineException{
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null||po.isEmpty()){
            throw new DefineException(label+"未定义！");
        }
        Map<String, Object> relationDef=neo4jService.getAttMapBy(LABEL, label, REALTION);
        if(relationDef==null||relationDef.isEmpty()){
            return ResultWrapper.wrapResult(true, null, null, "关系未定义！");
        }

        String endLabel=String.valueOf(relationDef.get("End"));
        // String startKey = String.valueOf(po.get(CRUD_KEY));

        Map<String, Object> endDomain=neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
        // String endKey = String.valueOf(endDomain.get(CRUD_KEY));
        Node start=null;
        if(!vo.containsKey("relations")){
            return ResultWrapper.wrapResult(true, null, null, "没有关系数据可保存！");
        }
        JSONArray relations=vo.getJSONArray("relations");
        for(int i=0; i<relations.size(); i++){
            JSONObject ri=relations.getJSONObject(i);
            String value=ri.getString("start");
            String endValue=ri.getString("end");
            start=neo4jService.findBy(NODE_ID, value, endLabel);
            List<Node> endNodes=new ArrayList<>();
            for(String eki : endValue.split(",")){
                endNodes.add(neo4jService.findBy(NODE_ID, eki, endLabel));
            }
            neo4jService.addRelations(start, endNodes, relation);
        }

        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    @RequestMapping(value = "/saveRel", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult saveRel(@PathVariable("po") String label, @RequestBody JSONObject vo){
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        Node start=null;
        if(!vo.containsKey("relations")){
            return ResultWrapper.wrapResult(true, null, null, "没有关系数据可保存！");
        }
        JSONArray relations=vo.getJSONArray("relations");
        for(int i=0; i<relations.size(); i++){
            JSONObject ri=relations.getJSONObject(i);
            String value=ri.getString("start");
            String endValue=ri.getString("end");
            relationService.addRel(REL_TYPE_CHILDREN, value, endValue);
        }

        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    @RequestMapping(value = "/addRel", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult addRel(@PathVariable("po") String label, @RequestBody JSONObject vo){
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null||po.isEmpty()){
            return ResultWrapper.wrapResult(true, null, null, label+"元数据缺失！");
        }
        String startIds=vo.getString("start");
        String endIds=vo.getString("end");
        String rel=vo.getString("rel");
        String relName=vo.getString("relName");

        String lowerCase=rel.toLowerCase();
        if(rel.length()>10||lowerCase.indexOf("create")>0||lowerCase.indexOf("delete")>0||lowerCase.indexOf("remove")>0){
            return ResultWrapper.wrapResult(true, null, null, rel+"敏感！");
        }
        try{
            if(relName!=null){
                cypherService.addRel(rel, relName, startIds, endIds);
            }else{
                cypherService.addRel(rel, startIds, endIds);
            }

        }catch(NumberFormatException e){
            LoggerTool.error(logger, "add rel exception", e);
        }
        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }


    @RequestMapping(value = "/saveRelById/{objId}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult saveRelByObjId(@PathVariable("po") String label, @PathVariable("objId") String objId){
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        relationService.addRel("is"+label, id(po), Long.valueOf(objId));
        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    /**
     * 保存父级关系，比如组织机构，用户。在保存用户基本信息后，保存用户所属组织机构的关系。
     *
     * @param label
     * @param vo
     * @return
     */
    @RequestMapping(value = "/saveParentRel", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult saveParentRel(@PathVariable("po") String label, @RequestBody JSONObject vo){

        String relAId=string(vo, REL_START_ID);
        String relALabel=string(vo, REL_START_LABEL);
        String endId=string(vo, "endId");

        relationService.addRel("belong"+relALabel, endId, relAId);
        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    @RequestMapping(value = "/saveResourceType/{objId}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult saveResourceType(@PathVariable("po") String label, @PathVariable("objId") String objId) throws DefineException{
        Map<String, Object> po=neo4jService.getAttMapBy(CODE, label, RESOURCE);
        if(po==null||po.isEmpty()){
            throw new DefineException(label+"未定义！");
        }
        relationService.addRel("is"+label+"Type", Long.valueOf(objId), id(po));
        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }


    private boolean isComplicated(Map<String, Object> vo){
        boolean isComplicatedData=false;
        for(Object vi : vo.values()){
            if(vi instanceof Map||vi instanceof List){
                isComplicatedData=true;
            }
        }
        return isComplicatedData;
    }

    private Map<String, Object> clearComplicated(Map<String, Object> vo){
        List<String> keys=new ArrayList<>();
        for(Entry<String, Object> vi : vo.entrySet()){
            if(vi.getValue() instanceof Map||vi.getValue() instanceof List){
                keys.add(vi.getKey());
            }
        }
        for(String ki : keys){
            vo.remove(ki);
        }
        return vo;
    }

    /**
     * 保存关系数据，保存结构数据
     *
     * @param label
     * @param relType 关系类型
     * @param vo
     * @return
     */
    private Long saveRelData(String label, String relType, Map<String, Object> vo){
        Map<String, Object> copyData=copy(vo);

        Long mainId=neo4jService.saveByBody(vo, label).getId();
        vo.put(ID, mainId);
        handelProp(label, relType, copyData, mainId);
        return mainId;
    }

    private void handelProp(String label, String relType, Map<String, Object> copyData,
                            Long mainId){
        List<Map<String, Object>> propInfoList=queryMetaRelDefine(label, relType);

        Map<String, String> propLabelNameMap=new HashMap<>();
        Map<String, List<Object>> propIds=new HashMap<>();
        //逐个处理属性
        for(Map<String, Object> pi : propInfoList){
            String piLabel=label(pi);
            String piName=name(pi);
            String propKey=string(pi, "prop");
            propLabelNameMap.put(piLabel, piName);
            Object voPropValue=copyData.get(propKey);
            if(voPropValue!=null){//属性值不为空，处理单个属性
                List<Object> labelIdList=new ArrayList<>();
                if(voPropValue instanceof List vps&&!vps.isEmpty()){
                    List<Map<String, Object>> voPropValues=listMapObject(copyData, propKey);
                    copyData.remove(propKey);
                    List<Long> savedIdList=new ArrayList<>(voPropValues.size());
                    for(Map<String, Object> propValuei : voPropValues){
                        savedIdList.add(saveProp(label, piLabel, relType, propValuei));
                    }
                    for(Long si : savedIdList){
                        labelIdList.add(si);
                    }
                    propIds.put(piLabel+"-"+propKey, labelIdList);
                }
                if(voPropValue instanceof Map){
                    Map<String, Object> nodei=mapObject(copyData, propKey);
                    copyData.remove(propKey);
                    labelIdList.add(saveProp(label, piLabel, relType, nodei));
                    propIds.put(piLabel+"-"+propKey, labelIdList);
                }
            }else{
                noPropVar(label, copyData, propIds, piLabel);
            }
        }
        clearComplicated(copyData);

        if(mainId!=null){
            for(String ki : propIds.keySet()){//建立实例数据与属性数据关系
                List<Object> list=propIds.get(ki);
                if(list!=null&&!list.isEmpty()){
                    String[] split=ki.split("-");
                    String kLabel=split[0];
                    String createRel=" match(n:"+label+"),(m:"+kLabel+") where id(n)= "+mainId
                            +" and id(m) IN ["+StringGet.join(",", list)+"] CREATE UNIQUE (n)-[r:"+relType+"{name:\""
                            +propLabelNameMap.get(kLabel)+"\",prop:\""+split[1]+"\"}]->(m)";
                    neo4jService.execute(createRel);
                }
            }
            //兼容数据，关系建立
            for(String li : propLabelNameMap.keySet()){
                List<Object> list=propIds.get(li);
                if(list!=null&&!list.isEmpty()){
                    String createRel=" match(n:"+label+"),(m:"+li+") where id(n)= "+mainId
                            +" and id(m) IN ["+StringGet.join(",", list)+"] CREATE UNIQUE (n)-[r:"+li+relType+"{name:\""
                            +propLabelNameMap.get(li)+","+"\"}]->(m)";
                    neo4jService.execute(createRel);
                }
            }
        }
    }

    private List<Map<String, Object>> queryMetaRelDefine(String label, String relType){
        String query="match(n:"+META_DATA+")-[r:"+relType+"]->(d:"+META_DATA+") where n.label='"+label
                +"' return d.name AS name,d.label AS label,d.columns AS columns,r.prop AS prop,r.name AS rName";
        //获取属性对象定义信息列表
        List<Map<String, Object>> propInfoList=neo4jService.cypher(query);
        return propInfoList;
    }

    private Long saveStruct(String label, Map<String, Object> vo){
        return saveRelData(label, "struct", vo);
    }

    /**
     * 递归保存复杂结构数据
     *
     * @param label
     * @param miLabel
     * @param relLabel
     * @param nodei
     * @return
     */
    private Long saveProp(String label, String miLabel, String relLabel, Map<String, Object> nodei){
        Node saveByBody=null;
        if(isComplicated(nodei)){
            return saveRelData(miLabel, relLabel, nodei);
        }else{
            saveByBody=neo4jService.addNew(nodei, miLabel);
            return saveByBody.getId();
        }
    }

    /**
     * 兼容复杂的属性类型，不规范的属性，找不到属性类型，进行尝试处理属性数据
     *
     * @param label
     * @param copyData
     * @param labelIdMap
     * @param miLabel
     */
    private void noPropVar(String label, Map<String, Object> copyData, Map<String, List<Object>> labelIdMap,
                           String miLabel){
        Boolean handleLabel=false;
        if(miLabel.startsWith(label)||miLabel.endsWith(label)){
            String fixLabel=StringGet.fixLabel(miLabel, label);
            handleLabel=handleStructLabel(label, copyData, labelIdMap, miLabel, fixLabel);
        }

        if(handleLabel==null||handleLabel!=true){
            String firstLow=StringGet.firstLow(miLabel);
            handleLabel=handleStructLabel(label, copyData, labelIdMap, miLabel, firstLow);
        }
        if(handleLabel==null||handleLabel!=true){
            String lowerCase=miLabel.toLowerCase();
            handleLabel=handleStructLabel(label, copyData, labelIdMap, miLabel, lowerCase);
        }
        if(handleLabel==null||handleLabel!=true){
            String connectLabel=StringGet.firstLow(label)+miLabel;
            handleLabel=handleStructLabel(label, copyData, labelIdMap, miLabel, connectLabel);
        }
    }

    private Boolean handleStructLabel(String label, Map<String, Object> vo, Map<String, List<Object>> labelIdMap,
                                      String miLabel, String alias){
        List<Object> labelIdList=labelIdMap.get(miLabel);
        String key2=alias+"List";
        String key=alias+"Info";
        if(vo.containsKey(key2)){
            List<Map<String, Object>> nodeList=listMapObject(vo, key2);
            vo.remove(key2);
            List<Long> savedIdList=new ArrayList<>(nodeList.size());
            for(Map<String, Object> pi : nodeList){
                Node saveByBody=null;
                if(isComplicated(pi)){
                    savedIdList.add(saveStruct(miLabel, pi));
                }else{
                    saveByBody=neo4jService.saveByBody(pi, label, false);
                    savedIdList.add(saveByBody.getId());
                }
            }

            if(labelIdList==null){
                labelIdList=new ArrayList<>(savedIdList.size());
            }
            for(Long si : savedIdList){
                labelIdList.add(si);
            }
            labelIdMap.put(miLabel, labelIdList);
            return true;
        }
        if(vo.containsKey(key)){
            if(!key.equals(label+"Info")){
                Long longValue=saveObjInfo(miLabel, vo);
                if(labelIdList==null){
                    labelIdList=new ArrayList<>();
                }
                if(longValue!=null){
                    vo.remove(key);
                    labelIdList.add(longValue);
                    labelIdMap.put(miLabel, labelIdList);
                }
            }

            return true;
        }
        return null;
    }

    private Long saveObjInfo(String label, Map<String, Object> vo){
        String firstLowLabel=StringGet.firstLow(label);
        Map<String, Object> mainInfo=mapObject(vo, firstLowLabel+"Info");
        if(mainInfo!=null){
            Node saveByBody=neo4jService.saveByBody(mainInfo, label);
            return saveByBody.getId();
        }else{
            return neo4jService.saveByBody(vo, label).getId();
        }
    }

    /**
     * 定义类的元数据没有关系和状态。
     *
     * @param label
     * @param vo
     * @param savedNode
     */
    private void handleRelationAndInitStatus(String label, JSONObject vo, Node savedNode, Map<String, Object> po){
        Map<String, Object> currentStatus=statusService.currentStatus(savedNode.getId());
        List<Map<String, Object>> listStatus=statusService.listStatus(label);
        if(listStatus!=null&&!listStatus.isEmpty()&&currentStatus==null){
            statusService.initStatus(savedNode.getId());
        }
        if(!LABEL_FIELD.equals(label)){
            List<Map<String, Object>> field2=htmlShowService.getField(label);
            for(Map<String, Object> mi : field2){
                Object object=mi.get("isPo");
                // 字段是实体关系的，保存数据时，建立关系。
                if(object!=null&&!"".equals(object)&&Boolean.valueOf(String.valueOf(object))){
                    String metaData=string(mi, "type");
                    String field=string(mi, "field");
                    String value=vo.getString(field);

                    String valueField=string(mi, "valueField");
                    if(valueField==null){
                        return;
                    }
                    if(value!=null&&!"".equals(value.trim())){
                        if(value.indexOf("选择")>-1){
                            continue;
                        }
                        String headerByCol=getHeaderByCol(po, field);
                        if(valueField.equals(ID)){
                            relationService.addRel(field, String.valueOf(savedNode.getId()), value, headerByCol);
                        }else{
                            neo4jService.addRel(field, String.valueOf(savedNode.getId()), metaData, valueField,
                                    value, headerByCol);
                        }
                    }
                }
            }
        }

        parentIdRelation(label, vo, savedNode);
    }

    /**
     * 子节点关系保存
     *
     * @param label
     * @param vo
     * @param saveByKey
     */
    private void parentIdRelation(String label, JSONObject vo, Node saveByKey){
        String string=vo.getString(COLUMN_PARENT);
        if(vo.containsKey(COLUMN_PARENT)&&string!=null&&!string.trim().isEmpty()){
            if(!vo.containsKey(NODE_ID)){
                relationService.addRel(REL_TYPE_CHILDREN, vo.getString("parentId"), String.valueOf(saveByKey.getId()));
            }else{
                Boolean existBoolean=false;
                List<Map<String, Object>> relationOneList=neo4jService.getOneRelationList(vo, label,
                        REL_TYPE_CHILDREN);
                if(relationOneList!=null&&!relationOneList.isEmpty()){
                    for(Map<String, Object> rei : relationOneList){
                        Map<String, Object> object=(Map<String, Object>) rei.get(RELATION_ENDNODE_PROP);
                        Object relId=rei.get("id");
                        if(relId!=null&&relId.equals(string)){
                            existBoolean=true;
                        }
                    }
                }
                if(!existBoolean){
                    relationService.addRel(REL_TYPE_CHILDREN, vo.getString("parentId"), String.valueOf(saveByKey.getId()));
                }
            }
        }
    }

    @RequestMapping(value = "/copy", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult copy(@PathVariable("po") String label, @RequestBody JSONObject vo){
        if(!vo.containsKey("params")||vo.isEmpty()||!crudUtil.isColumnsNotEmpty(vo)){
            return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
        }

        Object object=vo.get("params");
        Map<String, Object> paramObject=(Map<String, Object>) JSON.parse(String.valueOf(object));

        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        crudUtil.clearColumnOrHeader(vo);
        // if (!vo.containsKey(CRUD_KEY)) {
        // vo.put(CRUD_KEY, NODE_ID);
        // }
        List<Map<String, Object>> outRelationsnList=new ArrayList<>();
        List<Map<String, Object>> relationOneList=neo4jService.getOneRelationList(po, label, COPY_RELATION);
        if(relationOneList!=null&&!relationOneList.isEmpty()){
            List<String> copyLables=new ArrayList<>();
            for(Map<String, Object> reli : relationOneList){
                Map<String, Object> relPropMap=(Map<String, Object>) reli.get(RELATION_PROP);
                Map<String, Object> propMap=(Map<String, Object>) reli.get(RELATION_ENDNODE_PROP);
                Object object2=propMap.get(NODE_LABEL);
                if(object2!=null&&!COPY_RELATION.equals(object2)){
                    String endLabel=(String) object2;
                    copyLables.add(endLabel);
                }
            }
            outRelationsnList=neo4jService.getSomeRelationEndNodeId(paramObject, label, copyLables);
        }

        // String crudKey = completePK(po, CRUD_KEY, COLUMNS, HEADER);
        completePK(po);
        Node nodeCopy=neo4jService.copy(paramObject, label, NODE_ID);
        for(Map<String, Object> reli : outRelationsnList){
            Map<String, Object> endPaMap=new HashMap<>();
            endPaMap.put("id", reli.get("eId"));
            String endLabeli=String.valueOf(reli.get(LABEL));
            Node endNodeCopy=neo4jService.copy(endPaMap, endLabeli, NODE_ID);
            String relLabeli=String.valueOf(reli.get("rLabel"));
            if(relLabeli==null||relLabeli.equals("null")){
                continue;
            }
            Map<String, Object> relMap=new HashMap<>();
            relMap.put("name", reli.get("rName"));
            relMap.put(LABEL, relLabeli);
            neo4jService.addRelation(nodeCopy, endNodeCopy, relLabeli, relMap);
        }

        return ResultWrapper.wrapResult(true, nodeCopy.getId(), null, SAVE_SUCCESS);
    }


    @RequestMapping(value = "/save/{relLabel}/{startLabel}/{startId}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult saveInstance(@PathVariable("po") String label, @PathVariable("relLabel") String relLabel,
                                      @PathVariable("startLabel") String startLabel, @PathVariable("startId") String startId,
                                      @RequestBody JSONObject vo) throws DefineException{
        if(vo.isEmpty()||!crudUtil.isColumnsNotEmpty(vo)){
            return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
        }

        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null||po.isEmpty()){
            throw new DefineException(label+"未定义！");
        }
        Map<String, Object> startPo=neo4jService.getAttMapBy(LABEL, startLabel, META_DATA);

        String relationName=neo4jService.getRelationName(startLabel, META_DATA, relLabel);
        crudUtil.clearColumnOrHeader(vo);
        // if (!vo.containsKey(CRUD_KEY)) {
        // vo.put(CRUD_KEY, NODE_ID);
        // }
        // String crudKey = completePK(po, CRUD_KEY, COLUMNS, HEADER);
        completePK(po);
        Node endNode=neo4jService.saveByKey(vo, label, NODE_ID);

        Node startNode=neo4jService.findBy(NODE_ID, startId, startLabel);

        neo4jService.saveRelationDefine(label, relLabel, startLabel, relationName);

        relationService.addRel(relLabel, relationName, startNode.getId(), endNode.getId());
        return ResultWrapper.wrapResult(true, endNode.getId(), null, SAVE_SUCCESS);
    }

    /**
     * 获取某个类的字段列表
     *
     * @param label
     * @return
     */
    @RequestMapping(value = "/fieldList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult fieldList(@PathVariable("po") String label){
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null){
            po=neo4jService.getPropMapBy(label);
        }
        if(po==null){
            ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
        }
        String retColumns=String.valueOf(po.get("columns"));
        String header=String.valueOf(po.get("header"));
        String[] columnArray=retColumns.split(",");
        String[] headers=StringGet.split(header);
        List<Map<String, String>> filesList=new ArrayList<>(headers.length);
        for(int i=0; i<headers.length; i++){
            Map<String, String> e=new HashMap<>();
            e.put("code", columnArray[i]);
            e.put("name", headers[i]);
            filesList.add(e);
        }
        return ResultWrapper.wrapResult(true, filesList, null, SAVE_SUCCESS);
    }


    @RequestMapping(value = "/getRelation/{relation}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public List<Map<String, Object>> getRelation(@PathVariable("po") String label,
                                                 @PathVariable("relation") String relation, @RequestBody JSONObject vo) throws DefineException{
        PageObject page=crudUtil.validatePage(vo);
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        List<Map<String, Object>> oneOutRelation=neo4jService.getOutRelationList(vo, label, relation);
        return oneOutRelation;
    }

    @RequestMapping(value = "/tabList", method = {RequestMethod.GET, RequestMethod.POST})
    public WrappedResult tabList(Model model, @PathVariable("po") String label, @RequestBody JSONObject vo,
                                 HttpServletRequest request) throws Exception{
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null||po.isEmpty()){
            throw new DefineException(label+"未定义！");
        }

        Map<String, Object> tabList=tabService.tabList(label, vo);
        // page.setTotal(crudUtil.total(query));
        return ResultWrapper.wrapResult(true, tabList, null, QUERY_SUCCESS);
    }


    @RequestMapping(value = "/treeData", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public WrappedResult treeData(Model model, @PathVariable("po") String poLabel, HttpServletRequest request)
            throws Exception{

        Map<String, Object> endPo=neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
        Map<String, Object> tree=neo4jService.getWholeTree(poLabel);
        JSONArray zNodesList=new JSONArray();
        zNodesList.add(tree);
        return ResultWrapper.wrapResult(true, zNodesList, null, UPDATE_SUCCESS);
    }

}
