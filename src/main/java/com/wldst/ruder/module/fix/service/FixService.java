package com.wldst.ruder.module.fix.service;

import cn.hutool.extra.pinyin.PinyinUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.fun.service.RuleService;
import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.LoggerTool;
import com.wldst.ruder.util.MapTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

@Service
public class FixService extends MapTool{
    private static Logger logger=LoggerFactory.getLogger(FixService.class);
    private CrudNeo4jService neo4jService;

    private CrudUserNeo4jService neo4jUService;

    private HtmlShowService htmlShowService;
    @Autowired
    private Neo4jOptByUser optByUserSevice;

    @Autowired
    private RuleService ruleService;

    @Autowired
    public FixService(@Lazy CrudNeo4jService neo4jService,
                      @Lazy CrudUserNeo4jService neo4jUService,
                      HtmlShowService htmlShowService){
        this.neo4jService=neo4jService;
        this.neo4jUService=neo4jUService;
        this.htmlShowService=htmlShowService;
    }

    /**
     * 修复日期时间字段的格式问题
     * 此方法从dateTimeField方法获取日期时间字段的映射，然后构建查询以查找可能包含不规范格式的节点
     * 对于每个标签和字段集合，它会检查字段中是否包含特定的日期时间格式字符
     * 如果发现不规范的格式，将通过调用DateTool.dateFieldLong方法尝试修正这些字段的值
     */
    public void fixDateTime(){
        // 获取需要修复的日期时间字段映射
        Map<String, Set<String>> dateTimeField=getDateTimeField();
        // 遍历每个标签及其对应的日期时间字段集合
        for(Entry<String, Set<String>> entry: dateTimeField.entrySet()){
            // 获取当前处理的标签
            String label = entry.getKey();
            // 将字段集合转换为字符串数组，用于后续处理
            String[] columns=new String[entry.getValue().size()+1];
            columns = entry.getValue().toArray(columns);

            // 构建匹配特定标签节点的查询语句
            StringBuilder ret=new StringBuilder();
            ret.append("match (n:"+label+")");
            // 构建where子句，用于检查日期时间字段中是否包含不规范的格式字符
            StringBuilder sb=new StringBuilder();
            for(String ci: columns){
                if(sb.length()>0){
                    sb.append(" or ");
                }
                sb.append(" n."+ci+" contains('-') OR  n."+ci+" contains('/')  OR  n."+ci+" contains(' ') OR  n."+ci+" contains(':')");
            }
            if(sb.length()>1){
                ret.append(" where "+sb.toString()+" ");
            }
            // 在字段数组末尾添加ID，以便后续处理
            columns[entry.getValue().size()]=ID;
            // 调用服务方法获取需要修正的节点的关键字集合
            optByUserSevice.returnKeySet(columns, ret);

            // 执行查询，获取需要修复的节点数据
            List<Map<String, Object>> query1=neo4jService.query(ret.toString(), newMap());
            // 遍历查询结果，对每个节点进行修复
            for(Map<String, Object> qi: query1){
                Map<String, Object> copy=copy(qi);
                // 对每个日期时间字段调用DateTool.dateFieldLong方法进行修复
                for(String ci:entry.getValue()){
                    DateTool.dateFieldLong(copy, qi.get(ci), ci);
                }
                // 保存修复后的节点数据
                neo4jService.save(copy);
            }
        }
    }

    public Map<String, Set<String>> getDateTimeField(){
        Map<String, Set<String>> labelTimeField=new HashMap<>();
        List<Map<String, Object>> field2=htmlShowService.getTimeField();
        if(field2==null||field2.isEmpty()){
            return labelTimeField;
        }
//id,field,objectId,poId,type,isPo,valueField,showType
        Map<String, Object> ddMap=new HashMap<>();
        for(Map<String, Object> mi : field2){
            String field=string(mi, "field");
            String label=string(mi, "poId");
            Set<String> tf=labelTimeField.get(label);
            if(tf==null){
                tf=new HashSet<>();
            }
            tf.add(field);
            labelTimeField.put(label, tf);
        }

        return labelTimeField;
    }

    /**
     * 跟新某个功能的struct字段，收集一个功能的元数据依赖信息。在部署实时将其放到update目录下。
     * 或者进行推送。多少个系统，每个系统拥有哪些功能。
     * @param data
     */
    public void fixStruct(Map<String, Object> data){
        Long id=id(data);
        String funName=name(data);
        Map<String, Object> copy=newMap();
        String functionConfig="FunctionConfig";
        copy.put("label", functionConfig);
        copy.put(NAME, funName);
        copy.put(STATUS,"1");
        copy.put(CODE, PinyinUtil.getPinyin(funName));
        List<Map<String, Object>> depenencyMetaData =neo4jService.cypher("match (n:FunctionConfig)-[r:struct]->(m:MetaData) where id(n)="+id+" return distinct m");
        if(!depenencyMetaData.isEmpty()){
            copy.put(ID, id);
        }
        JSONArray metaList=new JSONArray();
        Set<String> labelSet=new HashSet<>();
        for(Map<String, Object> mi: depenencyMetaData){
            if(!labelSet.contains(label(mi))){
                metaList.add(JSON.toJSON(mi));
                labelSet.add(label(mi));
            }
        }


        //元数据的关系按钮，字段配置，数据，状态机配置。状态机步骤，流程，流程节点。流程历史？
        List<Map<String,Object>> otherInfo=new ArrayList<>();
        Set<String> otherLabelSet=new HashSet<>();

        for(Map<String, Object> mdi: depenencyMetaData){
            if(otherLabelSet.contains(label(mdi))){
                continue;
            }else{
                otherLabelSet.add(label(mdi));
            }
            List<Map<String, Object>> fields=addRelateData("Field", mdi, otherInfo);
            //判断字段是否依赖其他元数据
            for(Map<String, Object> fi: fields){
                String type=type(fi);
                if(type!=null){
                    List<Map<String, Object>> typeMeta=neo4jService.cypher("match (m:MetaData{label:\""+type+"\"})   return m");
                    metaList.add(JSON.toJSON(typeMeta));
                }
            }

            addRelateData("layTableToolOpt",mdi, otherInfo);
            List<Map<String, Object>> stateMachine=addRelateData("stateMachine", mdi, otherInfo);
            //判断状态机是否依赖其他元数据：状态机
            for(Map<String, Object> smi: stateMachine){
                addRelateData("stateStep", smi, otherInfo);
            }
            addRelateData("BpmGraph", mdi, otherInfo);
        }

        if(metaList.isEmpty()&&otherInfo.isEmpty()){
            neo4jService.save(copy, functionConfig);
            return;
        }
        if(!otherInfo.isEmpty()){
            for(Map<String, Object> oi: otherInfo){
                if(!labelSet.contains(label(oi))){
                    metaList.add(oi);
                    labelSet.add(label(oi));
                }
            }
        }

        copy.put("content", metaList.toJSONString());
        if(copy.isEmpty()||copy.size()<2){
            return;
        }
        neo4jService.save(copy, functionConfig);
    }

       /**
     * 添加关联数据
     * 该方法用于从 Neo4j 数据库中查询特定类型的关联数据，并将这些数据添加到一个列表中
     * @param endLabel 关联数据的标签
     * @param mi 元数据对象
     * @param otherInfo 其他信息列表
     * @return 关联数据列表
     */
    private List<Map<String, Object>> addRelateData(String endLabel,Map<String, Object> mi, List<Map<String,Object>> otherInfo){
        List<Map<String, Object>> relateInfo=neo4jService.cypher("match (n:MetaData)-[r]->(m:"+endLabel+") where id(n)="+id(mi)+" return m");
        for(Map<String, Object> fi: relateInfo){
            otherInfo.add(fi);
        }
        return relateInfo;
    }

}
