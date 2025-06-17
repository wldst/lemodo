package com.wldst.ruder.module.bs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.wldst.ruder.crud.service.RelationService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.crud.service.ObjectService;
import com.wldst.ruder.domain.VoiceOperateDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.impl.AddRelation;
import com.wldst.ruder.module.bs.impl.RelationIn;
import com.wldst.ruder.module.bs.impl.RelationOut;
import com.wldst.ruder.module.bs.impl.SaveNode;
import com.wldst.ruder.module.voice.LfasrService;
import com.wldst.ruder.module.voice.VoiceRecorder;
import com.wldst.ruder.util.CrudUtil;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * 脚本
 */
@Component
public class ShellOperator extends VoiceOperateDomain{
    final static Logger logger=LoggerFactory.getLogger(ShellOperator.class);
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private CrudUserNeo4jService neo4jUService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private RelationService relationService;

    private Map<Long, VoiceRecorder> voiceRecordMap=new HashMap<>();
    private Map<String, Map<String, Object>> context=new HashMap<>();
    List<String> stackQuit=Arrays.asList("退出", "返回", "返回上一级");// 唤醒词
    List<String> operateStack=Arrays.asList("操作", "使用", "进入", "得到");// 唤醒词

    List<String> getUseWords=Arrays.asList("操作", "使用", "进入", "打开", "获取", "得到", "获取最新的", "关于", "拿到", "找到");
    // 修改前缀
    List<String> newUpdate=Arrays.asList("把", "将", "被", "修改", "更新", "update");

    List<String> updates=Arrays.asList("保存", "更新", "update", "save");
    List<String> auth=Arrays.asList("给", "将");
    List<String> authAdd=Arrays.asList("添加", "增加", "授予", "授权");
    // 新的关系
    List<String> newRelation=Arrays.asList("创建关系", "新增关系", "添加关系", "添加联系", "新建关系", "有关系", "更新关系");
    // 新建节点
    List<String> newNode=Arrays.asList("创建", "新增", "新建", "添加","new");

    List<String> manageNode=Arrays.asList("管理", "处理", "列表", "查询");

    // 所属
    List<String> ownWords=Arrays.asList("的", "地", "得", "所属", "隶属的");
    // 动词读取关系
    List<String> actionWords=Arrays.asList("做", "干", "读", "听", "说", "学", "想", "写", "完成", "接龙");
    // 谓词
    List<String> kEqualv=Arrays.asList("是", "有", "等于", "叫", "为", "=");
    // 获取关系，修改关系属性
    List<String> relProp=Arrays.asList("关系属性");
    List<String> relName=Arrays.asList("朋友", "孩子", "父亲", "上级", "下级", "后序", "前序");
    // 是某某关系
    List<String> isRel=Arrays.asList("是", "在", "一起", "一同", "俩", "两个");
    List<String> andRel=Arrays.asList("和", "跟", "与", "、", " AND ", " and ", " && ");
    List<String> between=Arrays.asList("之间的");
    // 删除信息
    List<String> removes=Arrays.asList("删除", "去除", "注销", "清理", "清除", "处理掉", "delete", "remove");
    List<String> relationDel=Arrays.asList("禁止", "删除", "注销", "清除", "去掉", "去除", "delete", "remove", "del");


    public RelationOut relOut(String shape, Long shapeNodeId, String outRLabel){
        return new RelationOut(neo4jService, outRLabel, shapeNodeId, shape);
    }

    public RelationIn relIn(String sgroup, Long nodeId, String inRLabel){
        return new RelationIn(neo4jService, inRLabel, nodeId, sgroup);
    }

    public SaveNode save(String targetLabel){
        SaveNode save=new SaveNode(neo4jService, targetLabel);
        save.setCrudUtil(crudUtil);
        return save;
    }

    public SaveNode create(String targetLabel){
        return save(targetLabel);
    }

    public Long getId(String label, String key, String value){
        return neo4jService.getNodeId(key, value, label);
    }

    @ServiceLog(description = "获取节点")
    public Map<String, Object> getNode(String label, String key, String value){
        return neo4jService.getAttMapBy(key, value, label);
    }

    public void addRel(String relLabel, Long nodeId, Long moduleId){
        AddRelation mm=new AddRelation(neo4jService, relLabel, nodeId, moduleId);
        mm.execute();
    }

    public void addRel(String relLabel, String relName, Long nodeId, Long moduleId){
        AddRelation mm=new AddRelation(neo4jService, relLabel, nodeId, moduleId, relName);
        mm.execute();
    }

    public void addRel(String relLabel, Long startNodeId, Map<String, Object> endNode){
        AddRelation ar=new AddRelation(neo4jService, relLabel, startNodeId, id(endNode));
        ar.execute();
    }

    public void addRel(String relLabel, Map<String, Object> savedApp, Long nodeId){
        AddRelation ar=new AddRelation(neo4jService, relLabel, id(savedApp), nodeId);
        ar.execute();
    }

    public void addRel(String relLabel, Map<String, Object> savedModule, Map<String, Object> savedApp){
        if(savedModule==null){
            return;
        }
        AddRelation ar=new AddRelation(neo4jService, relLabel, id(savedApp), id(savedModule));
        ar.execute();
    }

    public String getMyDesktopName(){
        String myName=adminService.getCurrentAccount();
        String myDesktopName="deskTop_"+myName;
        return myDesktopName;
    }

    public void startRecord(Map<String, Object> voiceInfo){
        Long recordId=id(voiceInfo);
        VoiceRecorder vr=voiceRecordMap.get(recordId);
        if(vr==null){
            vr=new VoiceRecorder();
            String filePath=string(voiceInfo, "filePath");
            vr.setPathname(filePath);
            vr.setRun(false);
            voiceRecordMap.put(recordId, vr);
        }
        vr.captureAudio();
    }

    public void stopRecord(Map<String, Object> voiceInfo){
        Long recordId=id(voiceInfo);
        VoiceRecorder vr=voiceRecordMap.get(recordId);
        if(vr!=null){
            vr.closeCaptureAudio();
            // 将文件复制到系统中
            String pathname=vr.getPathname();
            File recordFile=new File(pathname);
            Map<String, Object> recordFileInfo=neo4jUService.recordFileInfo(recordFile);
            Long fileId=id(recordFileInfo);
            voiceInfo.put("fileId", fileId);

            String voice2Text=voiceFileToText(pathname);
            voiceInfo.put("text", voice2Text);
            neo4jUService.update(voiceInfo);
        }
        if(voiceRecordMap.containsKey(recordId)){
            voiceRecordMap.remove(recordId);
        }
    }

    /**
     * 另存为BeanShell调用按钮
     *
     * @param vo
     * @return
     */
    @ServiceLog(description = "另存为BeanShell调用按钮")
    public Map<String, Object> beanShellBtn(JSONObject vo){
        //2 自定业务数据
        Map<String, Object> bizValue=new HashMap();
//	id,code,name,icon,Html,dropDownItem,JavaScript,btnAcitive,des
//	ID,编码,名称,图标,html内容,下拉按钮,JavaScript脚本,激活逻辑,描述
        bizValue.put("icon", "&#xe64d");
        String html=" <a class=\"layui-btn layui-btn-warning layui-btn-xs\" lay-event=\"${BtnCode}\" >上架商品</a>";

        html=replaceBtnCode(vo, html);
        bizValue.put("Html", html);

        String javascript="""
                               function ${BtnCode}(po){
                                     if(po.id==undefined){
                              		 return;
                              	 }
                              	var genurl ="/${MODULE_NAME}/bs/BeanShell/${Code}";
                              	 $.ajax({
                              	      type: "post",
                              	      url: genurl,
                              	        dataType : "json",
                              	        contentType : "application/json;charset=UTF-8", 
                              	        data: JSON.stringify(po),
                              	        success: function (d) {
                                                 layer.alert(d.msg, {icon: 1})
                              	      	},
                              	 	error:function (d) {
                              		        layer.alert(d.msg, {icon: 5})
                              		    }
                              	    });
                              }
                """;

        javascript=replaceBtnCode(vo, javascript);

        javascript=javascript.replace("${MODULE_NAME}", LemodoApplication.MODULE_NAME);
        javascript=javascript.replace("${Code}", code(vo));
        bizValue.put("JavaScript", javascript);

        String activeBtn="""
                                     if(obj.event === '${BtnCode}'){
                                        ${BtnCode}(obj.data);
                                     }
                """;
        activeBtn=replaceBtnCode(vo, activeBtn);
        bizValue.put("btnAcitive", activeBtn);
        return bizValue;
    }

    private String replaceBtnCode(JSONObject vo, String activeBtn){
        activeBtn=activeBtn.replace("${BtnCode}", code(vo)+"Btn");
        return activeBtn;
    }

    public String voiceFileToText(String pathname){
        LfasrService transText=new LfasrService(pathname);
        String voice2Text="";
        try{
            voice2Text=transText.voice2Text();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return voice2Text;
    }

    /**
     * 自定义句式，句式识别
     *
     * @param voiceInfo
     */
    public void parseText(Map<String, Object> voiceInfo){
        String commandText=string(voiceInfo, "text");
        parseAndExcute(commandText);
    }

    public String formatCmd(String commandText){
        JSONArray parseArray=JSON.parseArray(commandText);
        StringBuilder cmds=new StringBuilder();
        for(Object oi : parseArray.toArray()){
            JSONObject joi=(JSONObject) oi;
            String string=joi.getString("onebest");
            cmds.append(string);
        }
        try{
            String string=clearVoiceWord(cmds.toString());
            return string;
        }catch(Exception e){
            e.printStackTrace();
            return "会话异常";
        }
    }

    /**
     * 替换字段所有匹配
     *
     * @param label 标签
     * @param field 字段
     * @param from  源
     * @param to    目标
     */
    @ServiceLog(description = "替换字段所有匹配")
    public void replaceAll(String label, String field, String from, String to){
        String cypher="Match(n:"+label+") where n."+field+" CONTAINS '"+from+"' return n";
        List<Map<String, Object>> query=neo4jService.cypher(cypher);
        for(Map<String, Object> mi : query){
            String fieldContent=string(mi, field);
            String newValue=fieldContent.replaceAll(from, to);
            if(!newValue.equals(fieldContent)){
                Map<String, Object> copyWithKeys=copyWithKeys(mi, field);
                copyWithKeys.put(field, newValue);
                neo4jService.update(copyWithKeys, id(mi));
            }
        }
    }

    /**
     * 替换单个字段的内容
     *
     * @param label 标签
     * @param field 字段
     * @param from  源
     * @param to    目标
     */
    @ServiceLog(description = "替换单个字段的内容")
    public void replace(String label, String field, String from, String to){
        String cypher="Match(n:"+label+") return n";
        List<Map<String, Object>> query=neo4jService.cypher(cypher);
        if(query==null){
            return;
        }
        for(Map<String, Object> mi : query){
            String fieldContent=string(mi, field);
            if(fieldContent==null||"".equals(fieldContent)||"null".equals(fieldContent)){
                continue;
            }
            // from=from.replaceAll("\\.","\\\\.");
            String newValue=fieldContent.replace(from, to);
            if(!newValue.equals(fieldContent)){
                Map<String, Object> copyWithKeys=copyWithKeys(mi, field);
                copyWithKeys.put(field, newValue);
                neo4jService.update(copyWithKeys, id(mi));
            }
        }
    }

    @ServiceLog(description = "替换单个字段的内容")
    public void replaceValue(String label, String field, String from, String to){
        if(from.equals(to)){
            return;
        }
        String cypher="Match(n:"+label+"{\""+field+"\":\""+from+"\"}) return n";
        List<Map<String, Object>> query=neo4jService.cypher(cypher);
        if(query==null){
            return;
        }
        for(Map<String, Object> mi : query){
            if(!from.equals(to)){
                Map<String, Object> copyWithKeys=copyWithKeys(mi, field);
                copyWithKeys.put(field, to);
                neo4jService.update(copyWithKeys, id(mi));
            }
        }
    }

    @ServiceLog(description = "解析并执行语音识别句子")
    public Map<String, Object> parseAndExcute(String commandText){
        Map<String, Object> parseAndexcute=new HashMap<>();
        if(commandText.contains("{")&&commandText.contains(":")&&commandText.contains("}")){
            JSONArray parseArray=JSON.parseArray(commandText);
            StringBuilder cmds=new StringBuilder();
            for(Object oi : parseArray.toArray()){
                JSONObject joi=(JSONObject) oi;
                String string=joi.getString("onebest");
                cmds.append(string);
            }
            try{
                Long currentUserId=adminService.getCurrentPasswordId();
                parseAndexcute(cmds.toString(), currentUserId+"");
            }catch(Exception e){
                e.printStackTrace();
                parseAndexcute.put("msg", "会话异常");
                parseAndexcute.put("status", "error");
                parseAndexcute.put("error", e.getMessage());
                return parseAndexcute;
            }
        }else{
            try{
                parseAndexcute=parseAndexcute(commandText, adminService.getCurrentPasswordId()+"");
                return parseAndexcute;
            }catch(Exception e){
                parseAndexcute.put("msg", "会话异常");
                parseAndexcute.put("status", "error");
                parseAndexcute.put("error", e.getMessage());
                return parseAndexcute;
            }
        }
        return null;
    }


    @ServiceLog(description = "解析以管理开头的句子")
    public List<Map<String, Object>> handleStartManage(String msg, Map<String, Object> context){
        List<Map<String, Object>> data=new ArrayList<>();
        String prefix="管理";
        if(msg.startsWith(prefix)){// 根据角色权限，账号权限，来确定打开范围
            handleManage(msg, META_DATA, data, prefix);
        }
        return data;
    }

    @ServiceLog(description = "解析以《执行》开头的句子")
    public List<Map<String, Object>> handleStartExcute(String msg, Map<String, Object> context){
        List<Map<String, Object>> data=new ArrayList<>();
        String prefix="执行";
        if(msg.startsWith(prefix)){// 执行脚本，根据名称或者code来查找脚本
            handleExecute(msg, data, prefix);
        }
        return data;
    }

    /**
     * 获取某些对象的某些资源
     *
     * @param sb
     * @param startQuery
     * @param datas
     * @param endName
     * @param endMeta
     */
    @ServiceLog(description = "获取某些对象的某些资源")
    public void getSrOfSb(StringBuilder sb, String startQuery, List<Map<String, Object>> datas, String endName,
                          Map<String, Object> endMeta){
        String queryData;
        Boolean hasPermission=endName.contains("权限");
        queryData="MATCH (m:"+label(endMeta)+") return distinct m";
        List<Map<String, Object>> ends=neo4jUService.cypher(queryData);

        if(hasPermission){
            for(Map<String, Object> ei : ends){
                List<Map<String, Object>> authOfRi=new ArrayList<>();
                queryData="MATCH (n)-[r:HAS_PERMISSION]-(m:"+META_DATA+")  where n.name='"+startQuery
                        +"' and r.code='"+code(ei)+"' return distinct m ";
                List<Map<String, Object>> auths=neo4jUService.cypher(queryData);
                if(auths!=null&&!auths.isEmpty()){
                    authOfRi.addAll(auths);
                }

                queryData="MATCH (n)-[r:HAS_PERMISSION]->(ro:Role)-[r1:HAS_PERMISSION]->(m:"+META_DATA
                        +")  where n.name='"+startQuery+"' and r1.code=\""+code(ei)+"\"  return distinct m ";
                List<Map<String, Object>> auth2s=neo4jUService.cypher(queryData);
                if(auth2s!=null&&!auth2s.isEmpty()){
                    authOfRi.addAll(auth2s);
                }
                if(authOfRi.size()>0){
                    for(Map<String, Object> ai : authOfRi){
                        ai.put("HAS_PERMISSION", name(ei));
                        datas.add(ai);
                    }
                }
            }
            if(datas.size()>0){
                seeEndNodeAuth(sb, datas);
            }
        }else{
            for(Map<String, Object> ri : ends){
                List<Map<String, Object>> itsPropRel=new ArrayList<>();
                queryData="MATCH (n)-[r]-(m:"+META_DATA+")  where n.name='"+startQuery+"' and r.code='"
                        +code(ri)+"' return distinct m ";
                List<Map<String, Object>> auths=neo4jUService.cypher(queryData);
                if(auths!=null&&!auths.isEmpty()){
                    itsPropRel.addAll(auths);
                }

                queryData="MATCH (n)-[r]->(ro:Role)-[r1]->(m:"+META_DATA+")  where n.name='"+startQuery
                        +"' and r1.code=\""+code(ri)+"\"  return distinct m ";
                List<Map<String, Object>> auth2s=neo4jUService.cypher(queryData);
                if(auth2s!=null&&!auth2s.isEmpty()){
                    itsPropRel.addAll(auth2s);
                }
                if(itsPropRel.size()>0){
                    for(Map<String, Object> ai : itsPropRel){
                        ai.put("HAS_", name(ri));
                        datas.add(ai);
                    }
                }
            }
            if(datas.size()>0){
                seeEndNodePropRel(sb, datas);
            }
        }
    }

    @ServiceLog(description = "查询关节终点资源")
    public void queryRelationEnds(StringBuilder sb, String startQuery, List<Map<String, Object>> datas, String endName,
                                  Map<String, Object> endMeta){
        String queryData;
        Boolean hasPermission=endName.contains("权限");
        queryData="MATCH (m:"+label(endMeta)+") return distinct m";
        List<Map<String, Object>> ends=neo4jUService.cypher(queryData);

        if(hasPermission){
            for(Map<String, Object> ei : ends){
                List<Map<String, Object>> authOfRi=new ArrayList<>();
                queryData="MATCH (n)-[r:HAS_PERMISSION]-(m:"+META_DATA+")  where n.name='"+startQuery
                        +"' and r.code='"+code(ei)+"' return distinct m ";
                List<Map<String, Object>> auths=neo4jUService.cypher(queryData);
                if(auths!=null&&!auths.isEmpty()){
                    authOfRi.addAll(auths);
                }

                queryData="MATCH (n)-[r:HAS_PERMISSION]->(ro:Role)-[r1:HAS_PERMISSION]->(m:"+META_DATA
                        +")  where n.name='"+startQuery+"' and r1.code=\""+code(ei)+"\"  return distinct m ";
                List<Map<String, Object>> auth2s=neo4jUService.cypher(queryData);
                if(auth2s!=null&&!auth2s.isEmpty()){
                    authOfRi.addAll(auth2s);
                }
                if(authOfRi.size()>0){
                    for(Map<String, Object> ai : authOfRi){
                        ai.put("HAS_PERMISSION", name(ei));
                        datas.add(ai);
                    }
                }
            }
            if(datas.size()>0){
                seeEndNodeAuth(sb, datas);
            }
        }else{
            for(Map<String, Object> ri : ends){
                List<Map<String, Object>> itsPropRel=new ArrayList<>();
                queryData="MATCH (n)-[r]-(m:"+META_DATA+")  where n.name='"+startQuery+"' and r.code='"
                        +code(ri)+"' return distinct m ";
                List<Map<String, Object>> auths=neo4jUService.cypher(queryData);
                if(auths!=null&&!auths.isEmpty()){
                    itsPropRel.addAll(auths);
                }

                queryData="MATCH (n)-[r]->(ro:Role)-[r1]->(m:"+META_DATA+")  where n.name='"+startQuery
                        +"' and r1.code=\""+code(ri)+"\"  return distinct m ";
                List<Map<String, Object>> auth2s=neo4jUService.cypher(queryData);
                if(auth2s!=null&&!auth2s.isEmpty()){
                    itsPropRel.addAll(auth2s);
                }
                if(itsPropRel.size()>0){
                    for(Map<String, Object> ai : itsPropRel){
                        ai.put("HAS_", name(ri));
                        datas.add(ai);
                    }
                }
            }
            if(datas.size()>0){
                seeEndNodePropRel(sb, datas);
            }
        }
    }


    public void collectData(StringBuilder sb, List<Map<String, Object>> datas, String relQuery){
        List<Map<String, Object>> datasi=neo4jUService.cypher(relQuery);
        if(datasi!=null&&!datasi.isEmpty()){
            datas.addAll(datasi);
            seeEndNode(sb, datasi);
        }
    }

    public String replaceQueryWord(String por){
        if(por.startsWith("哪些")){

            por=por.replaceFirst("哪些", "");
        }
        return por;
    }

    /**
     * 根据名称获取元数据信息
     *
     * @param metaName
     * @return
     */
    @ServiceLog(description = "获取某些对象的某些资源")
    public List<Map<String, Object>> getMetaDataByName(String metaName){
        List<Map<String, Object>> ownerMetas;

        String getMetaInfo=" MATCH (m:MetaData) where  m.name='"+metaName+"'  return distinct m";
        ownerMetas=neo4jUService.cypher(getMetaInfo);
        if(ownerMetas!=null&&!ownerMetas.isEmpty()){
            return ownerMetas;
        }
        getMetaInfo="MATCH (n) WHERE n.name = '"+metaName+"' unwind labels(n) AS x return x";
        ownerMetas=neo4jUService.cypher(getMetaInfo);
        List<Object> obs=new ArrayList<>();
        for(Map<String, Object> omi : ownerMetas){
            String labeli=string(omi, "x");
            obs.add(labeli);
        }

        if(!obs.isEmpty()){
            getMetaInfo=" MATCH (m:MetaData) where  m.label in ("+joinStr(obs)+" ) return distinct m";
            ownerMetas=neo4jUService.cypher(getMetaInfo);
        }

        return ownerMetas;
    }

    public Map<String, String> getMetaData(){
        String getMetaInfo=" MATCH (m:MetaData) where  m.label='"+META_DATA+"'  return distinct m";
        List<Map<String, Object>> ownerMetas=neo4jService.cypher(getMetaInfo);
        if(ownerMetas!=null&&!ownerMetas.isEmpty()){
            return nameColumn(ownerMetas.get(0));
        }
        return null;
    }

    public List<String> getRelationQuery(){
        List<String> query=new ArrayList<>();
        query.add("可以访问哪些");
        query.add("能访问哪些");
        query.add("有哪些");
        query.add("有什么");
        query.add("有多少");
        query.add("是什么");
        query.add("是多少");
        query.add("是哪些");
        query.add("是");
        query.add("有");
        return query;
    }

    public void seeEndNodeAuth(StringBuilder sb, List<Map<String, Object>> datas){
        if(datas!=null&&!datas.isEmpty()){
            StringBuilder sbx=new StringBuilder();
            for(Map<String, Object> di : datas){
                if(sbx.length()>0){
                    sbx.append("、");
                }
                sbx.append(neo4jUService.seeNode(di)+"的"+string(di, "HAS_PERMISSION"));
            }
            if(sbx.length()>0){
                sb.append(sbx.toString());
            }
        }
    }

    public void seeEndNodePropRel(StringBuilder sb, List<Map<String, Object>> datas){
        if(datas!=null&&!datas.isEmpty()){
            StringBuilder sbx=new StringBuilder();
            for(Map<String, Object> di : datas){
                if(sbx.length()>0){
                    sbx.append("、");
                }
                sbx.append(neo4jUService.seeNode(di)+"的"+string(di, "HAS_"));
            }
            if(sbx.length()>0){
                sb.append(sbx.toString());
            }
        }
    }

    public void seeEndNode(StringBuilder sb, List<Map<String, Object>> datas){
        if(datas!=null&&!datas.isEmpty()){
            StringBuilder sbx=new StringBuilder();
            for(Map<String, Object> di : datas){
                if(sbx.length()>0){
                    sbx.append("、");
                }
                sbx.append(neo4jUService.seeNode(di));
            }
            if(sbx.length()>0){
                sb.append(sbx.toString());
            }
        }
    }

    public String getABPath(String msg){
        Long idOfStart=null;
        Long idOfEnd=null;

        boolean useAnd=false;
        for(String qie : andRel){
            if(msg.contains(qie)){
                String[] resourceAuth=msg.split(qie);
                idOfStart=getIdOfData(resourceAuth[0]);
                idOfEnd=getIdOfData(resourceAuth[1]);
                useAnd=true;
            }
        }
        if(useAnd){
            return adminService.showPathInfo(idOfStart, idOfEnd);
        }
        return null;
    }


    public void handleExecute(String msg, List<Map<String, Object>> data, String prefix){
        String obj=msg.replaceFirst(prefix, "");
        boolean useAnd=false;
        for(String qie : andRel){
            if(obj.contains(qie)){
                String[] scripts=msg.split(qie);
                for(String si : scripts){
                    Map<String, Object> script=getOneData(si);
                    if(script!=null){
                        data.add(script);
                    }
                }
                useAnd=true;
            }
        }
        if(!useAnd){
            Map<String, Object> data2=getOneData(obj);
            if(data2!=null){
                data.add(data2);
            }
        }

        for(Map<String, Object> di : data){
            String string=string(di, "Content");
            Interpreter in=new Interpreter();
            try{
                in.set("so", this);
                // 得有一个文档说明：
                in.set("repo", neo4jService);
                in.eval(string);
            }catch(EvalError e){
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

    public void handleManage(String msg, String labelOf, List<Map<String, Object>> data, String prefix){
        String obj=msg.replaceFirst(prefix, "");
        boolean useAnd=false;
        for(String qie : andRel){
            if(obj.contains(qie)){
                String[] resourceAuth=msg.split(qie);
                for(String ri : resourceAuth){
                    Map<String, Object> mdData=getData(ri, labelOf);

                    if(mdData!=null){
                        if(META_DATA.equals(labelOf)){
                            mdData.put("url", LemodoApplication.MODULE_NAME+"/md/"+label(mdData));
                        }
                        data.add(mdData);
                    }
                }
                useAnd=true;
            }
        }
        if(!useAnd){
            Map<String, Object> data2=getData(obj, labelOf);
            if(data2!=null){
                if(META_DATA.equals(labelOf)){
                    data2.put("url", LemodoApplication.MODULE_NAME+"/md/"+label(data2));
                }
                data.add(data2);
            }
        }
    }

    public void handleOpen(String msg, String labelOf, List<Map<String, Object>> data){
        boolean useAnd=false;
        for(String qie : andRel){
            if(msg.contains(qie)){
                String[] resourceAuth=msg.split(qie);
                for(String ri : resourceAuth){
                    Map<String, Object> mdData=getData(ri, labelOf);
                    processOpenData(labelOf, data, mdData);
                }
                useAnd=true;
            }
        }
        if(!useAnd){
            Map<String, Object> data2=getData(msg, labelOf);
            processOpenData(labelOf, data, data2);
        }
    }

    public void processOpenData(String labelOf, List<Map<String, Object>> data, Map<String, Object> data2){
        if(data2!=null){
            if(url(data2)==null){
                // 判断权限，只读和修改权限

                data2.put("url",
                        LemodoApplication.MODULE_NAME+"/layui/"+label(data2)+"/"+id(data2)+"/detail");

            }
            if(META_DATA.equals(labelOf)){
                data2.put("url", LemodoApplication.MODULE_NAME+"/md/"+label(data2));
            }
            data.add(data2);
        }
    }

    public void deleteAuthRel(Long startId, Long endId, String auth){
        String relCode;
        Map<String, Object> authMap=neo4jUService.getAttMapBy(NAME, auth, "permission");
        relCode=code(authMap);
        String cypher=" MATCH(s)-[r:"+relCode+"{name:\""+auth+"\"}]->(e)  where id(s)="+startId
                +" and id(e)="+endId+" delete r";
        neo4jService.execute(cypher);
    }

    public void deleteRel(Long startId, Long endId){
        String cypher=" MATCH(s)-[r]->(e)  where id(s)="+startId+" and id(e)="+endId+" delete r";
        neo4jService.execute(cypher);
    }

    public void deleteRel(Long startId, Long endId, String name){
        String cypher=" MATCH(s)-[r]->(e)  where id(s)="+startId+" and id(e)="+endId+" delete r";
        if(name!=null&&!"".equals(name.trim())){
            cypher=" MATCH(s)-[r]->(e)  where id(s)="+startId+" and r.name=\""+name+"\" and id(e)="+endId+" delete r";
        }
        neo4jService.execute(cypher);
    }


    public void createRel(Long startId, Long endId, String auth){
        Map<String, Object> authMap=neo4jUService.getAttMapBy(NAME, auth, "RelationDefine");
        if(authMap==null){
            return;
        }
        String relCode=string(authMap, "reLabel");
//	neo4jService.relate(startId,"HAS_PERMISSION",auth, endId);
        String cypher="MATCH (s),(e) where id(s)="+startId+" and id(e)="+endId
                +" create (s)-[:HAS_PERMISSION{name:\""+auth+"\",code:\""+relCode+"\"}]->(e)";
        neo4jService.execute(cypher);
    }

    public void delRel(Long startId, Long endId, String auth){
        Map<String, Object> authMap=neo4jUService.getAttMapBy(NAME, auth, "RelationDefine");
        if(authMap==null){
            return;
        }
        String relCode=string(authMap, "reLabel");
        String cypher="MATCH (s)-[r:"+relCode+"{name:\""+auth+"\"}]->(e) where id(s)="+startId
                +" and id(e)="+endId+" delete r";
        neo4jService.execute(cypher);
    }

    /**
     * 给开始节点和结束节点添加权限关系
     *
     * @param startId
     * @param endId
     * @param auth
     */
    public void addAuthRel(Long startId, Long endId, String auth){
        String relCode;
        Map<String, Object> authMap=neo4jUService.getAttMapBy(NAME, auth, "permission");
        if(authMap!=null){
            relCode=code(authMap);
//	    neo4jService.relate(startId,"HAS_PERMISSION",auth, endId);
            String cypher="MATCH (s),(e) where id(s)="+startId+" and id(e)="+endId
                    +" create (s)-[:HAS_PERMISSION{code:\""+relCode+"\",name:\""+auth+"\"}]->(e)";
            neo4jService.execute(cypher);
            return;
        }else{
            Map<String, Object> metaDataById=neo4jUService.getMetaDataById(endId);
            String code2=code(metaDataById);
            StringBuilder cypher=new StringBuilder(
                    "MATCH (s),(e) where id(s)="+startId+" and id(e)="+endId+" create (s)-[:HAS_PERMISSION{");
            if(code2!=null){
                cypher.append("code:\""+code2+"\",");
            }
            cypher.append("name:\""+name(metaDataById)+"\"}]->(e)");
            neo4jService.execute(cypher.toString());
//	    neo4jService.relate(startId,"HAS_PERMISSION",auth, endId);
        }

    }

    public Long getIdOfMd(String resource){
        Long startId=null;
        List<Map<String, Object>> metaDataBy=neo4jUService.getMetaDataBy(resource);
        if(metaDataBy.size()>=1){
            startId=id(metaDataBy.get(0));
        }
        return startId;
    }

    public String getLabelOfMd(String resource){
        String labelData=null;
        List<Map<String, Object>> metaDataBy=neo4jUService.getMetaDataBy(resource);
        if(metaDataBy.size()==1){
            labelData=label(metaDataBy.get(0));
        }
        return labelData;
    }

    public String showJsonString(String msgx){
        try{
            Map<String, Object> data=neo4jUService.getPropMapByNodeId(Long.valueOf(msgx));
            return JSON.toJSONString(data);
        }catch(Exception e){

        }

        Map<String, Object> attMapBy=neo4jUService.getAttMapBy(NAME, msgx, META_DATA);
        if(attMapBy!=null){
            return JSON.toJSONString(attMapBy);
        }

        Map<String, Object> attMapBy2=neo4jUService.getAttMapBy(CODE, msgx, META_DATA);
        if(attMapBy2!=null){
            return JSON.toJSONString(attMapBy);
        }

        List<Map<String, Object>> attMapBy3=neo4jUService.queryDataBy(msgx);
        if(attMapBy3!=null){
            return JSON.toJSONString(attMapBy3);
        }


        return "没有找到元数据"+msgx;

    }


    public Long getIdOfRoleOrUser(String resource){
        Long startId=null;
        List<Map<String, Object>> metaDataBy=neo4jUService.getDataBy("Role", resource);
        if(!metaDataBy.isEmpty()&&metaDataBy.size()>0){
            startId=id(metaDataBy.get(0));
        }else{
            metaDataBy=neo4jUService.getDataBy("User", resource);
            if(!metaDataBy.isEmpty()&&metaDataBy.size()>0){
                startId=id(metaDataBy.get(0));
            }
        }
        return startId;
    }

    public Map<String, Object> getData(String name, String labelOf){
        List<Map<String, Object>> metaDataBy=neo4jUService.getDataBy(labelOf, name);
        if(metaDataBy.isEmpty()||metaDataBy.size()<1){
            return null;
        }
        return metaDataBy.get(0);
    }

    public List<Map<String, Object>> queryData(String name, String labelOf){
        List<Map<String, Object>> metaDataBy=neo4jUService.getDataBy(labelOf, name);
        if(metaDataBy.isEmpty()||metaDataBy.size()<1){
            return null;
        }
        return metaDataBy;
    }

    public Long getIdOfData(String name, String labelOf){
        return id(getData(name, labelOf));
    }


    public Map<String, Object> getOneData(String name){
        List<Map<String, Object>> metaDataBy=neo4jUService.getDataBy(name);
        if(metaDataBy.isEmpty()||metaDataBy.size()<1||metaDataBy.size()>1){
            return null;
        }
        return metaDataBy.get(0);
    }

    public Long getIdOfData(String resource){
        return id(getOneData(resource));
    }

    /**
     * 默认增删改查
     *
     * @param message
     */
    public Map<String, Object> parseAndexcute(String message, String sessionId){
        // 替换掉声音助词
        String msg=clearVoiceWord(message);
        // huoq唤醒词：
        // 默认的唤醒词
        if(msg.length()<=10){
            // 获取默认数据：
            boolean use=false;
            List<String> xx=new ArrayList<>();
            xx.addAll(getUseWords);
            xx.addAll(stackQuit);
            xx.addAll(operateStack);
            xx.addAll(newUpdate);
            xx.addAll(newRelation);
            xx.addAll(newNode);
            xx.addAll(andRel);
            xx.addAll(removes);
            xx.addAll(ownWords);
            xx.addAll(kEqualv);
            xx.addAll(relProp);
            xx.addAll(relName);
            xx.addAll(isRel);
            xx.addAll(manageNode);

            for(String ni : xx){
                if(msg.startsWith(ni)){
                    use=true;
                }
            }
            if(!use){
                Map<String, Object> parseNoReservedWord=parseNoReservedWord(sessionId, msg);
                if(parseNoReservedWord!=null){
                    return parseNoReservedWord;
                }
            }

        }

        Map<String, Object> operateMeta=enterMeta(sessionId, msg);
        if(operateMeta!=null){
            return operateMeta;
        }
        Map<String, Object> manage=manage(sessionId, msg);
        if(manage!=null){
            return manage;
        }
        Map<String, Object> operateObject=useIt(sessionId, msg);
        if(operateObject!=null){
            return operateObject;
        }

        Map<String, Object> handleStartWithTa=handleStartWithTa(sessionId, msg);
        if(handleStartWithTa!=null){
            return handleStartWithTa;
        }
        for(String ni : removes){
            Map<String, Object> delObject=deleteOne(msg, ni, sessionId);
            if(delObject!=null){
                return delObject;
            }
        }
        //
        for(String hi : relProp){
            addRelationProp(msg, hi, sessionId);
        }
        return singleSentence(msg, sessionId);
    }

    /**
     * 解析没有使用关键字的句子
     *
     * @param sessionId
     * @param msg
     * @return
     */
    private Map<String, Object> parseNoReservedWord(String sessionId, String msg){
        Map<String, Object> myContext=getMyContext(sessionId);
        Map<String, Object> data=new HashMap<>();

        // 当前节点为空，或者当前节点名称有
        Map<String, Object> metaMap=mapObject(myContext, OPERATE_META);
        if(metaMap==null){
            return newFromMeta(sessionId, msg);
        }else{// 当前已有对象，则获取当前的标签，查询当前元数据的实例数据
            String operateLabel=getOperateLabel(sessionId);
            Map<String, Object> objectNode=getNode(operateLabel, "name", msg.trim());
            if(objectNode!=null){// 在当前的元数据下查询到实例数据，精确查找实例数据
                setMayConextProp(OPERATE_OBJECT, objectNode, sessionId);

                Map<String, Object> objectShowCol=neo4jUService.onlyShowCol(objectNode, operateLabel);
                if(objectShowCol==null){
                    try{
                        crudUtil.simplification(objectNode);
                        deSensitive(operateLabel, objectNode);
                        Map<String, String> colHeader=neo4jUService.getColHeadById(id(objectNode));
                        objectShowCol=visualData(colHeader, objectNode);
                    }catch(DefineException e){
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if(label(objectShowCol)==null){
                    objectShowCol.put(LABEL, operateLabel);
                }
                neo4jUService.visulRelation(objectShowCol);
                StringBuilder sb=new StringBuilder();
                nodeDataRelView(sb, objectShowCol);
                data.put("data", sb.toString());
                return data;
            }else{// 当前的元数据中，没有精确的实例数据
                // 重新走一遍查询，去掉当前：元数据，实例数据，标签
                clearMetaObject(myContext);
                return newFromMeta(sessionId, msg);
            }
        }
    }

    private Map<String, Object> parseObjectWord(String sessionId, String msg){
        Map<String, Object> myContext=getMyContext(sessionId);
        Map<String, Object> data=new HashMap<>();

        // 当前节点为空，或者当前节点名称有
        Map<String, Object> metaMap=mapObject(myContext, OPERATE_META);
        if(metaMap==null){
            return newFromMeta(sessionId, msg);
        }else{// 当前已有对象，则获取当前的标签，查询当前元数据的实例数据
            String operateLabel=getOperateLabel(sessionId);
            Map<String, Object> objectNode=getNode(operateLabel, "name", msg.trim());
            if(objectNode!=null){// 在当前的元数据下查询到实例数据，精确查找实例数据
                setMayConextProp(OPERATE_OBJECT, objectNode, sessionId);

                Map<String, Object> objectShowCol=neo4jUService.onlyShowCol(objectNode, operateLabel);
                if(objectShowCol==null){
                    try{
                        crudUtil.simplification(objectNode);
                        deSensitive(operateLabel, objectNode);
                        Map<String, String> colHeader=neo4jUService.getColHeadById(id(objectNode));
                        objectShowCol=visualData(colHeader, objectNode);
                    }catch(DefineException e){
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                if(label(objectShowCol)==null){
                    objectShowCol.put(LABEL, operateLabel);
                }
                neo4jUService.visulRelation(objectShowCol);
                StringBuilder sb=new StringBuilder();
                nodeDataRelView(sb, objectShowCol);
                data.put("data", sb.toString());
                return data;
            }else{// 当前的元数据中，没有精确的实例数据
                // 重新走一遍查询，去掉当前：元数据，实例数据，标签
                clearMetaObject(myContext);
                return newFromMeta(sessionId, msg);
            }
        }
    }

    /**
     * 根据实例数据，返回元数据
     *
     * @param di
     * @return
     */
    public String metaInfoByData(Map<String, Object> di){
        Map<String, Object> metaData=neo4jUService.getAttMapBy(LABEL, label(di), META_DATA);
        return metaDataView(metaData);
    }

    /**
     * 可视化元数据，链接元数据
     *
     * @param metaData
     * @return
     */
    public String metaDataView(Map<String, Object> metaData){
        String metai="<a href=\"javascript:;\" onclick=\"window.open('"+LemodoApplication.MODULE_NAME+"/layui/MetaData/documentRead?id="
                +id(metaData)+"')\">"+name(metaData)+"</a>";
        return metai;
    }

    private Map<String, Object> newFromMeta(String sessionId, String msg){
        // 全局查询元数据
        Map<String, Object> metaNode=getNode(META_DATA, "name", msg.trim());
        if(metaNode!=null){// 精确找到元数据
            String label=label(metaNode);
            getInto(label, sessionId);
            setMayConextProp(OPERATE_META, metaNode, sessionId);
        }else{// 没有精确的元数据
            // 查询所有名称或者编码与查询字段匹配的所有节点。
            String queryByNameOrCode="Match(n) where n.name  CONTAINS '"+msg+"' OR n.code  CONTAINS '"+msg
                    +"' return n";
            List<Map<String, Object>> queryList=neo4jService.cypher(queryByNameOrCode);
            Map<String, Object> data=new HashMap<>();
            // crudUtil.simplifiList(queryList);
            neo4jUService.visualRelList(queryList);
            StringBuilder sb=new StringBuilder();
            for(Map<String, Object> di : queryList){
                if(sb.length()>0){
                    sb.append("\n");
                }
                nodeDataRelView(sb, di);
            }
            data.put("data", sb.toString());
            return data;
        }
        // 只显示设置过的可视化字段
        Map<String, Object> metaShowCol=neo4jUService.onlyShowCol(metaNode, label(metaNode));
        if(metaShowCol!=null){
            neo4jUService.visulRelation(metaShowCol);
            return metaShowCol;
        }
        // 没有设置显示的数据，默认的数据显示功能。
        crudUtil.simplification(metaNode);
        neo4jUService.visulRelation(metaNode);

        return metaNode;
    }

    public void nodeDataRelView(StringBuilder sb, Map<String, Object> di){
        metaAndDataView(sb, di);
        String string=string(di, "出关系");
        if(string!=null){
            sb.append("\n"+name(di)+"->"+string);
        }
        String r=string(di, "入关系");
        if(r!=null){
            sb.append("\n"+r+"->"+name(di));
        }
    }

    /**
     * 根据实例数据读取实例数据，元数据，
     *
     * @param sb
     * @param di
     */
    public void metaAndDataView(StringBuilder sb, Map<String, Object> di){
        String label2=label(di);
        if(label2!=null){
            sb.append(metaInfoByData(di));// 元数据链接
            sb.append(":");
            String nodeName;
            if(label2.equals(LABEL_FIELD)){
                nodeName="【"+string(di, FIELD)+"】";
            }else{
                String name2=name(di);
                if(label2.equals(META_DATA)){
                    nodeName="【"+label2+"】";
                    if(name2!=null||"null".equals(name2)){
                        nodeName=nodeName+name2;
                    }
                }else{
                    nodeName=neo4jUService.showNode(di, label2);
                }
            }

            sb.append("<a href=\"javascript:;\" onclick=\"window.open('"+LemodoApplication.MODULE_NAME+"/layui/"+label2+"/documentRead?id="
                    +id(di)+"')\">"+nodeName+"</a>");// 实例数据连接
        }
    }

    /**
     * 清理对象和元数据，pop
     *
     * @param myContext
     */
    private void clearMetaObject(Map<String, Object> myContext){
        myContext.remove(OPERATE_META);
        myContext.remove(OPERATE_OBJECT);
        myContext.remove(OPERATE_LABEL);

    }

    private void deSensitive(String label, Map<String, Object> di) throws DefineException{
        String[] sensitiveColumn=crudUtil.getSensitiveColumn(label);
        if(sensitiveColumn!=null&&sensitiveColumn.length>0){
            for(String si : sensitiveColumn){
                String string=string(di, si);
                String dsValue=string.substring(0, 1);
                di.put(si, dsValue+"***");
            }
        }
    }

    private Map<String, Object> visualData(Map<String, String> colHeader, Map<String, Object> di)
            throws DefineException{
        Map<String, Object> copy=copy(di);
        for(Entry<String, String> si : colHeader.entrySet()){
            if(di.containsKey(si.getKey())){
                copy.put(si.getValue(), di.get(si.getKey()));
            }

        }
        return copy;
    }

    /**
     * 简化字段
     *
     * @throws DefineException
     */

    private Map<String, Object> handleStartWithTa(String sessionId, String string){
        if(getOperateLabel(sessionId)!=null&&getOperateObject(sessionId)!=null
                ||getOperateRelation(sessionId)!=null){
            List<String> heSheIt=Arrays.asList("他", "她", "它");
            for(String hi : heSheIt){
                // 新增关系 xx and xx is xxRel

                for(String ni : andRel){
                    addTaRealtion(string, hi+ni, sessionId);
                }
                // 新增关系 xx 是 bb de xxRel
                for(String ni : kEqualv){
                    addIsTaDeRealtion(string, hi+ni, sessionId);
                }
                // 新增属性 o的xxx是bbb
                for(String ni : ownWords){
                    Map<String, Object> addTaDeProp=addTaDeProp(string, hi+ni, sessionId);
                    if(addTaDeProp!=null){
                        return addTaDeProp;
                    }
                }
            }
        }

        return null;
    }

    public Map<String, Object> multiParseAndexcute(String string2, String sessionId){
        // 替换掉声音助词
        String string=clearVoiceWord(string2);
        // huoq唤醒词：
//	neo4jService.listDataByLabel("w");

        Map<String, Object> operateMeta=enterMeta(sessionId, string);
        if(operateMeta!=null){
            return operateMeta;
        }

        Map<String, Object> operateObject=useIt(sessionId, string);
        if(operateObject!=null){
            return operateObject;
        }
        handleStartWithTa(sessionId, string);
        List<String> relProp=Arrays.asList("关系属性");
        for(String hi : relProp){
            addRelationProp(string, hi, sessionId);
        }
        return singleSentence(string, sessionId);
    }

    private Map<String, Object> enterMeta(String sessionId, String string){
        Map<String, Object> operateMeta=null;

        for(String ni : getUseWords){
            operateMeta=useSomething(string, ni, sessionId);
            if(operateMeta!=null){
                return operateMeta;
            }
        }
        return operateMeta;
    }

    private Map<String, Object> manage(String sessionId, String string){
        Map<String, Object> operateMeta=new HashMap<>();

        for(String ni : manageNode){
            operateMeta=manageSomething(string, ni, sessionId);
            if(operateMeta!=null){
                return operateMeta;
            }
        }
        return operateMeta;
    }

    private Map<String, Object> useIt(String sessionId, String string){
        Map<String, Object> operateObject=null;
        for(String ni : getUseWords){
            operateObject=useObject(string, ni, sessionId);
            if(operateObject!=null){
                return operateObject;
            }
        }
        return null;
    }

    /**
     * 批量操作，同一种操作，CUD
     *
     * @param string
     * @param sessionId
     * @return
     */
    private Map<String, Object> singleSentence(String string, String sessionId){
        List<Map<String, Object>> dataCreate=new ArrayList<>();
        for(String ni : newNode){
            List<Map<String, Object>> createSomething=createSomething(string, ni, sessionId);
            dataCreate.addAll(createSomething);
        }
        if(dataCreate!=null&&!dataCreate.isEmpty()){
            Map<String, Object> data=new HashMap<>();
            data.put("data", dataCreate);
            return data;
        }
        List<Map<String, Object>> updateCreate=new ArrayList<>();

        for(String ni : newUpdate){
            List<Map<String, Object>> updates=updateSomething(string, ni);
            updateCreate.addAll(updates);
        }
        if(updateCreate!=null&&!updateCreate.isEmpty()){
            Map<String, Object> data=new HashMap<>();
            data.put("data", dataCreate);
            return data;
        }

        for(String ni : newRelation){
            addSomeRealtion(string, ni, sessionId);
        }

        for(String ni : removes){
            deleteSomething(string, ni, sessionId);
        }
        return null;
    }

    private String clearVoiceWord(String string2){
        String replaceAll=string2.replaceAll("嗯", "");
        List<String> wcSet=Arrays.asList("诶", "呃", "乌", "阿", "偌", "得", "叱", "吓", "吁", "呔", "呐", "呜", "呀", "呵", "哎",
                "咄", "咍", "呣", "呶", "呸", "呦", "哈", "咳", "哑", "咦", "哟", "咨", "啊", "唉", "唗", "哦", "哼", "唦", "喏", "啧", "嗏",
                "喝", "嗟", "喂", "喔", "嗄", "嗳", "嗤", "嘟", "嗨", "嗐", "嗯", "嘘", "嘿", "噢", "嘻", "噫", "嚄", "嚯", "於", "欸", "恶",
                "究竟", "终究", "他妈的", "日你的妈");
        for(String ni : wcSet){
            if(string2.indexOf("ni")>-1){
                replaceAll=replaceAll.replaceAll(ni, "");
            }
        }

        return replaceAll;
    }

    /**
     * 创建相关的事情
     *
     * @param string
     * @param newNode
     */
    private List<Map<String, Object>> createSomething(String string, String newCreate, String sessionId){
        string=clearFuhao(string);
        List<Map<String, Object>> createObjects=new ArrayList<>();
        if(string.indexOf(newCreate)>-1){
            String[] create=string.split(newCreate);
            for(String createObject : create){
                if(createObject!=null&&!"".equals(createObject.trim())&&!createObject.startsWith("关系")){
                    Map<String, Object> createOne=createOne(createObject, sessionId);
                    if(createOne!=null){
                        createObjects.add(createOne);
                    }
                }
            }
        }
        if(createObjects.size()==1){
            setMayConextProp(OPERATE_OBJECT, createObjects.get(0), sessionId);
        }

        return createObjects;
    }

    /**
     * 操作：元数据（用户，项目）。 更新某个东西 do（将，把）xxx的sss修改为（成）bb
     *
     * @param string
     * @param updateWord
     */
    private List<Map<String, Object>> updateSomething(String string, String updateWord){
        string=clearFuhao(string);
        List<Map<String, Object>> updateObjects=new ArrayList<>();
        if(string.indexOf(updateWord)>-1){
            String[] updates=string.split(updateWord);
            String operatObject=updates[0];
            if(operatObject!=null&&operatObject.startsWith("操作")){
                String label=operatObject.replace("操作", "");
                if(label.length()>1){
                    for(String updateObject : updates){
                        if(updateObject!=null&&!"".equals(updateObject.trim())&&!updateObject.startsWith("关系")){
                            List<Map<String, Object>> updateOne=updateOne(updateObject, label);
                            updateObjects.addAll(updateOne);
                        }
                    }
                }
            }
        }
        return updateObjects;
    }

    /**
     * @param string
     * @param useWordi
     */
    private Map<String, Object> useSomething(String string, String useWordi, String sessionId){
        string=clearFuhao(string);
        if(string.indexOf(useWordi)>-1){
            if(string!=null&&string.startsWith(useWordi)){
                String[] split=string.split(useWordi);
                if(split.length>2){// 进入操作，进入操作
                    for(String si : split){

                    }
                }else{
                    String metaName=string.replace(useWordi, "");
                    if(metaName.length()>1){
                        Map<String, Object> metaNode=getNode(META_DATA, "name", metaName);
                        if(metaNode!=null){
                            String label=label(metaNode);
                            getInto(label, sessionId);
                            setMayConextProp(OPERATE_META, metaNode, sessionId);
                        }
                        return metaNode;
                    }
                }
            }
        }
        return null;
    }

    private Map<String, Object> manageSomething(String string, String manageWordi, String sessionId){
        string=clearFuhao(string);
        if(string.indexOf(manageWordi)>-1){
            if(string!=null&&string.startsWith(manageWordi)){
                String[] split=string.split(manageWordi);
                if(split.length>2){// 进入操作，进入操作

                }else{
                    String metaName=string.replace(manageWordi, "");
                    if(metaName.length()>1){
                        String queryByNameOrCode="Match(n:MetaData) where n.name  CONTAINS '"+metaName
                                +"' OR n.code  CONTAINS '"+metaName+"'  OR n.label  CONTAINS '"+metaName
                                +"' return n";
                        List<Map<String, Object>> queryList=neo4jService.cypher(queryByNameOrCode);
                        List<String> manageHref=new ArrayList<>();
                        if(queryList!=null&&!queryList.isEmpty()){
                            setMayConextProp(OPERATE_META_LIST, queryList, sessionId);
                            for(Map<String, Object> mi : queryList){

                                String xxi="<a href=\"javascript:;\" onclick=\"window.open('"+LemodoApplication.MODULE_NAME+"/po/"+label(mi)
                                        +"')\"> 【"+name(mi)+"】</a>";
                                if(manageHref.size()>0){
                                    manageHref.add("、"+xxi);
                                }else{
                                    manageHref.add(xxi);
                                }
                            }
                        }
                        Map<String, Object> data=new HashMap<>();
                        data.put("data", manageHref);
                        return data;
                    }
                }
            }
        }
        return null;
    }

    private Map<String, Object> useObject(String string, String useWordi, String sessionId){
        string=clearFuhao(string);
        if(string.indexOf(useWordi)>-1){
            if(string!=null&&string.startsWith(useWordi)){
                String objectName=string.replace(useWordi, "");
                String trimName=objectName.trim();
                if(trimName.length()>1){
                    Map<String, Object> objectNode=getNode(getOperateLabel(sessionId), "name", trimName);
                    if(objectNode!=null){
                        setMayConextProp(OPERATE_OBJECT, objectNode, sessionId);
                    }
                    return objectNode;
                }
            }
        }
        return null;
    }

    private void getInto(String label, String sessionId){
        Map<String, Object> myContext=getMyContext(sessionId);
        myContext.put(OPERATE_LABEL, label);
    }

    private void setMayConextProp(String key, Object value, String sessionId){
        getMyContext(sessionId).put(key, value);
    }

    private Map<String, Object> getMyContext(String sessionId){
        Map<String, Object> myContext=context.get(sessionId);
        if(myContext==null){
            myContext=new HashMap<>();
            context.put(sessionId, myContext);
        }
        return myContext;
    }

    private String getMyKey(){
        String currentUserName=adminService.getCurrentAccount();
        String currentUserId=adminService.getCurrentPasswordId()+"";
        String userkey=currentUserId+"-"+currentUserName;
        return userkey;
    }

    private String getOperateLabel(String sessionId){
        return string(getMyContext(sessionId), OPERATE_LABEL);
    }

    private String getColumnByHeader(String headeri, String sessionId){
        Map<String, Object> metaMap=mapObject(getMyContext(sessionId), OPERATE_META);
        return getColByHeader(metaMap, headeri);
    }

    private Map<String, Object> getOperateObject(String sessionId){
        return mapObject(getMyContext(sessionId), OPERATE_OBJECT);
    }

    private Map<String, Object> getOperateRelation(String sessionId){
        return mapObject(getMyContext(sessionId), OPERATE_RELATION);
    }

    private void addSomeRealtion(String string, String newRelWord, String sessionId){
        string=clearFuhao(string);
        if(string.indexOf(newRelWord)>-1){
            String[] create=string.split(newRelWord);
            for(String createObject : create){
                if(createObject!=null&&!"".equals(createObject.trim())&&createObject.startsWith("关系")){
                    addOneRelation(createObject, sessionId);
                }
            }
        }
    }

    private void addTaRealtion(String string, String newRelWord, String sessionId){
        string=clearFuhao(string);
        if(string.indexOf(newRelWord)>-1&&string.startsWith(newRelWord)){
            String[] create=string.split(newRelWord);
            if(create[1]!=null&&!"".equals(create[1].trim())){
                addTaAndXIsRelation(create[1], sessionId);
            }
        }
    }

    private void addIsTaDeRealtion(String string, String newRelWord, String sessionId){
        string=clearFuhao(string);
        if(string.indexOf(newRelWord)>-1&&string.startsWith(newRelWord)){
            String[] relOne=string.split(newRelWord);
            if(relOne[1]!=null&&!"".equals(relOne[1].trim())){
                isTadeXRelation(relOne[1], sessionId);
            }
        }
    }

    private Map<String, Object> addTaDeProp(String string, String newRelWord, String sessionId){
        string=clearFuhao(string);
        if(string.indexOf(newRelWord)>-1&&string.startsWith(newRelWord)){
            String[] prop=string.split(newRelWord);
            if(prop[1]!=null&&!"".equals(prop[1].trim())){
                return addTadeProp(prop[1], sessionId);
            }
        }
        return null;
    }

    private void addRelationProp(String string, String newRelWord, String sessionId){
        string=clearFuhao(string);
        if(string.indexOf(newRelWord)>-1&&string.startsWith(newRelWord)){
            String[] prop=string.split(newRelWord);
            if(prop[1]!=null&&!"".equals(prop[1].trim())){
                addRelDeProp(prop[1], sessionId);
            }
        }
    }

    private void addRelDeProp(String itIs, String sessionId){
        String key="";
        String value="";

        for(String ui : kEqualv){
            if(itIs.indexOf(ui)>-1){
                String[] prop=itIs.split(ui);
                key=prop[0].trim();
                value=prop[1].trim();

                Map<String, Object> nodeMap=getOperateRelation(sessionId);
                if(nodeMap!=null){
                    Map<String, Object> data=new HashMap<>();
                    data.put(key, value);
                    Long startId=id(nodeMap);
                    neo4jService.saveRelById(startId, nodeMap);
                }
            }
        }

    }

    private Map<String, Object> addTadeProp(String itIs, String sessionId){
        String key="";
        String value="";
        Map<String, Object> startNode=getOperateObject(sessionId);

        for(String ui : kEqualv){
            if(itIs.indexOf(ui)>-1){
                String[] prop=itIs.split(ui);
                key=prop[0].trim();
                // 判断可以是否是关系，如朋友，父亲，上级，下级，后续，前序。
                Map<String, Object> endNode=null;
                if(relName.contains(key)){
                    // 添加关系,同类中找
                    endNode=getNode(getOperateLabel(sessionId), NAME, value);
                }else{// 跨元数据关系
                    Map<String, Object> node=getNode(META_DATA, NAME, key);
                    if(node!=null){
                        endNode=getNode(label(node), NAME, value);
                    }
                }
                if(endNode!=null){
                    relationService.addRel(key, id(startNode), id(endNode));
                    return new HashMap<>();
                }

                value=prop[1].trim();

                if(startNode!=null){
                    String columnKey=getColumnByHeader(key, sessionId);
                    startNode.put(columnKey, value);
                    neo4jService.saveById(string(startNode, ID), startNode);
                    return startNode;
                }
            }
        }
        return null;

    }

    /**
     * 是xxx的朋友
     *
     * @param itIs
     * @param sessionId
     */
    private void isTadeXRelation(String itIs, String sessionId){
        String endName="";
        String relName="";

        for(String ui : ownWords){
            if(itIs.indexOf(ui)>-1){
                String[] prop=itIs.split(ui);
                endName=prop[0].trim();
                relName=prop[1].trim();

                addRelateInCurrentMeta(sessionId, endName, relName);
            }
        }

    }

    /**
     * 在当前元数据中添加关系
     *
     * @param sessionId
     * @param endName
     * @param relName
     */
    private void addRelateInCurrentMeta(String sessionId, String endName, String relName){
        Map<String, Object> endNode=getNode(getOperateLabel(sessionId), NAME, endName);

        Map<String, Object> startNode=getOperateObject(sessionId);
        if(endNode!=null&&startNode!=null){
            Map<String, Object> data=new HashMap<>();
            data.put(NAME, relName);
            Long endId=id(endNode);
            Long startId=id(startNode);
            relationService.addRel(relName, startId, endId, data);
            setMayConextProp(OPERATE_RELATION, data, sessionId);
        }
    }

    /**
     * ta和tb是朋友
     *
     * @param itIs
     * @param sessionId
     */
    private void addTaAndXIsRelation(String itIs, String sessionId){
        String endName="";
        String relName="";
        for(String ui : isRel){
            if(itIs.indexOf(ui)>-1){
                String[] prop=itIs.split(ui);
                endName=prop[0].trim();
                relName=prop[1].trim();

                Map<String, Object> endNode=getNode(getOperateLabel(sessionId), "name", endName);

                Map<String, Object> startNode=getOperateObject(sessionId);
                if(endNode!=null&&startNode!=null){
                    Map<String, Object> data=new HashMap<>();
                    data.put(NAME, relName);
                    relationService.addRel(relName, id(startNode), id(endNode), data);
                }
            }
        }

    }

    /**
     * xx的relation是bb xx是bb的relation
     * <p>
     * 操作关系：同类关系，不同元数据关系。 关系属性。 给用户xx添加一个账号。账号是名称全拼。
     * <p>
     * 方向关系：出关系，入关系。 开始节点是sss叫sp,ta的属性x是dd 结束节点是eee叫ep,ta的属性x是dd 关系属性： 开始时间，结束时间，状态
     * 如何写脚本识别？
     *
     * @param createObject
     */
    private void addOneRelation(String createObject, String sessionId){
        String[] subject=createObject.split("的");
        String so=subject[1];
        String si=subject[1];
        String relationName="";
        String relEndObject="";
        for(String ki : kEqualv){
            if(si.indexOf(ki)>-1){
                String[] prop=si.split(ki);
                relationName=prop[0].trim();
                relEndObject=prop[1].trim();
                // relEndObject = splitByPronoun(relEndObject, sessionId);

                Map<String, Object> node=getNode(META_DATA, "name", relationName);
                if(node!=null){
                    String label=label(node);
                    Map<String, Object> data=new HashMap<>();
                    data.put(NAME, relEndObject);
                    data.put(LABEL, label);
                    Node saveByBody=neo4jService.saveByBody(data, label);
                    break;
                }
            }
        }

    }

    /**
     * 属性中文名是叫为等于xx的xx
     *
     * @param createObject
     */
    private Map<String, Object> recognitDifferntObject(String createObject){
        String[] subject=createObject.split("的");
        String so=subject[1];
        String metaName=subject[1];
        String key="";
        String value="";
        for(String ni : kEqualv){
            if(so.indexOf("ni")>-1){
                String[] prop=so.split(ni);
                key=prop[0].trim();
                value=prop[1].trim();

                Map<String, Object> metaNode=getNode(META_DATA, "name", metaName);

                if(metaNode!=null){
                    String label=label(metaNode);
                    Map<String, String> nameColumn=nameColumn(metaNode);
                    String keyCode=nameColumn.get(key);
                    return getNode(label, keyCode, value);
                }
            }
        }
        return null;

    }

    /**
     * xxx的属性kkkk修改为vvv
     *
     * @param createObject
     * @param metaName
     */
    private List<Map<String, Object>> updateOne(String createObject, String metaName){
        String[] subject=createObject.split("的");
        String objectName=subject[0];
        String propName=subject[1];
        propName=clearFuhao(propName);
        List<String> wcSet=Arrays.asList("修改为", "修改成", "改为", "改成", "变更为", "变更成", "更新成", "更新为", "刷新成", "刷新为");
        String key=null;
        String value=null;
        for(String ni : wcSet){
            if(propName.indexOf(ni)>-1){
                String[] prop=propName.split(ni);
                key=prop[0].trim();
                value=prop[1].trim();
            }
        }
        List<Map<String, Object>> retUpdate=new ArrayList<>();
        Map<String, Object> metaNode=getNode(META_DATA, "name", metaName);
        if(metaNode!=null){
            String label=label(metaNode);
            Map<String, Object> objectNode=getNode(label, "name", objectName);
            if(objectNode!=null){
                Long id=id(objectNode);
                Map<String, Object> data=new HashMap<>();
                String colByHeader=getColByHeader(metaNode, key);
                data.put(colByHeader, value);
                neo4jService.update(data, id);
                retUpdate.add(data);
            }
        }
        return retUpdate;
    }

    private Map<String, Object> createOne(String createObject, String sessionId){
        String propName=getPropName(createObject);
        propName=clearFuhao(propName);
        String metaName="";
        String name="";
        Map<String, Object> node=null;
        if(propName.indexOf("叫")>-1){
            String[] prop=propName.split("叫");
            metaName=prop[0].trim();
            name=prop[1].trim();
            name=splitByPronoun(name, sessionId);
            node=getNode(META_DATA, "name", metaName);
        }else{

            String operateLabel=getOperateLabel(sessionId);
            Map<String, Object> data=new HashMap<>();
            data.put(NAME, propName);
            data.put(LABEL, operateLabel);
            Node saveByBody=neo4jService.saveByBody(data, operateLabel);
            return data;
        }
        if(node!=null){
            String label=label(node);
            Map<String, Object> data=new HashMap<>();
            data.put(NAME, name);
            data.put(LABEL, label);
            Node saveByBody=neo4jService.saveByBody(data, label);
            return data;
        }
        return null;
    }

    private String getPropName(String createObject){
        if(createObject.indexOf("个")<0){// 没有两次
            return createObject;
        }
        String[] subject=createObject.split("个");
        String propName=subject[1];
        return propName;
    }

    private String clearFuhao(String propName){
        propName=propName.replaceAll(",", "");
        propName=propName.replaceAll("，", "");
        propName=propName.replaceAll("、", "");
        propName=propName.replaceAll("。", "");
        propName=propName.replaceAll("<div><br></div>", "");
        propName=propName.replaceAll("</pre>", "");

        return propName;
    }

    private Map<String, Object> deleteOne(String string, String delKey, String sessionId){
        string=clearFuhao(string);
        if(string.indexOf(delKey)>-1){
            String[] dels=string.split(delKey);
            String delObject=dels[1];
            String objName="";
            if(delObject.indexOf("个")>-1){
                String[] subject=delObject.split("个");
                objName=subject[1];
            }else{
                objName=delObject;
            }

            String name=objName;
            String label=getOperateLabel(sessionId);
            Map<String, Object> data=new HashMap<>();
            data.put(NAME, name);
            neo4jService.removeNodeByPropAndLabel(data, label);
            if(string(data, ID)!=null){
                return data;
            }
        }
        return null;
    }

    private void deleteSomething(String string, String delKey, String sessionId){
        string=clearFuhao(string);
        if(string.indexOf(delKey)>-1){
            String[] create=string.split(delKey);
            String createObject=create[1];
            String propName="";
            if(createObject.indexOf("个")>-1){
                String[] subject=createObject.split("一个");
                propName=subject[1];
            }else{
                propName=createObject;
            }

            String metaName="";
            String name="";
            if(propName.indexOf("叫")>-1){
                String[] prop=propName.split("叫");
                metaName=prop[0].trim();
                name=prop[1].trim();
                name=splitByPronoun(name, sessionId);
            }else{
                metaName=splitByPronoun(propName, sessionId);
            }

            Map<String, Object> node=getNode(META_DATA, "name", metaName);
            String label=label(node);
            Map<String, Object> data=new HashMap<>();
            data.put(NAME, name);
            data.put(LABEL, label);
            neo4jService.removeNodeByPropAndLabel(data, label);
        }
    }

    private String splitByPronoun(String name, String sessionId){
        if(name.indexOf("他")>-1){
            String[] split=name.split("他");
            name=split[0];
            parseAndexcute(split[1], sessionId);
        }
        if(name.indexOf("它")>-1){
            String[] split=name.split("它");
            name=split[0];
            parseAndexcute(split[1], sessionId);
        }
        if(name.indexOf("她")>-1){
            String[] split=name.split("她");
            name=split[0];
            parseAndexcute(split[1], sessionId);
        }
        return name;
    }

}
