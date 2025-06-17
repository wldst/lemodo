package com.wldst.ruder.module.fun;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.domain.CypherDomain;
import com.wldst.ruder.util.PageObject;

/**
 * 数据操作功能
 *
 * @author deeplearn96
 */
public class Neo4jOptCypher extends CypherDomain{


    /**
     * 生成删除语句
     *
     * @param props
     * @param label
     * @return
     */
    public static String delObj(Map<String, Object> props, String label){
        if(props==null||props.isEmpty()){
            return "match (n : "+label+")  DETACH DELETE  n";
        }else{
            StringBuilder sb=propString(props);
            return "match (n : "+label+"{"+sb.toString()+"}) delete n";
        }
    }

    public static String delObj(String label){
        return delObj(null, label);
    }

    public static String delRelaOf(Map<String, Object> props, String label){
        if(props==null||props.isEmpty()){
            return "match (n : "+label+")-[r]->(b) delete r";
        }else{
            StringBuilder sb=propString(props);
            return "match (n : "+label+"{"+sb.toString()+"})-[r]->(b) delete r";
        }
    }

    public static String delRelbOf(String label){
        return delRelbOf(null, label);
    }

    public static String delRelaOf(String label){
        return delRelaOf(null, label);
    }

    public static String delRel(String startId, String endId){
        return "match (b)-[r]->(n) where id(b)="+startId+" and id(n)="+endId+"  DETACH DELETE  r";
    }

    public static String delRel(String rel, Long startId, Long endId){
        if(endId==null){
            return "match (b)-[r:"+rel+"]->(n) where id(b)="+startId+"  DETACH DELETE  r";
        }
        return "match (b)-[r:"+rel+"]->(n) where id(b)="+startId+" and id(n)="+endId+"  DETACH DELETE  r";
    }

    public static String queryRel(String rel, Long startId, Long endId){
        if(endId==null){
            return "match (b)-[r:"+rel+"]->(n) where id(b)="+startId+" return r";
        }
        return "match (b)-[r:"+rel+"]->(n) where id(b)="+startId+" and id(n)="+endId+" return r";
    }

    public static String queryRel(String rlabel, Long startId, Long endId, Map<String, Object> param){
        StringBuilder sb=new StringBuilder();
        sb.append("MATCH (a)-[r:"+rlabel+relProp(param)+"]->(b)");
        sb.append("WHERE id(a) = "+startId+" AND id(b) = "+endId+" return r");
        return sb.toString();
    }

    public static String delRelbOf(Map<String, Object> props, String label){
        if(props==null||props.isEmpty()){
            return "match (b)-[r]->(n : "+label+") delete r";
        }else{
            StringBuilder sb=propString(props);
            return "match (b)-[r]->(n : "+label+"{"+sb.toString()+"})  DETACH DELETE  r";
        }
    }

    public static String delById(String id, String label){
        if(id.contains(",")){
            return "match (n : "+label+") where id(n) in ["+id+"]  DETACH DELETE  n";
        }else{
            return "match (n : "+label+") where id(n) ="+id+"  DETACH DELETE  n";
        }

    }

    public static String delById(Long id, String label){
        return "match (n : "+label+") where id(n) ="+id+"=  DETACH DELETE  n";
    }

    /**
     * 获取某个实体的状态列表
     *
     * @param startId
     * @return
     */
    public static String getStatusList(Long startId){
        return getFieldSelectList(startId, "stateStep");
    }

    public static String getStatusList(String poLabel){
        return getFieldSelectList(poLabel, "stateStep");
    }

    public static String initStatus(String poLabel){
        return getFieldSelectList(poLabel, "stateStep")+" limit 1 ";
    }

    /**
     * 根据实体Id获取所关联的 Label为：endlabel的相关数据code,name，供关联字段提取。
     *
     * @param startId
     * @param endlabel
     * @return
     */
    public static String getFieldSelectList(Long startId, String endlabel){
        return "MATCH (po:"+META_DATA+")-[*1..3]->(e:"+endlabel+") where id(po)="+startId+" return distinct e.code,e.name";
    }

    /**
     * 根据实体Label获取所关联的 ID为：endlabel的相关数据code,name，供关联字段提取。
     *
     * @param endlabel
     * @return
     */
    public static String getFieldSelectList(String label, String endlabel){
        return "MATCH (po:"+META_DATA+"{label:\""+label+"\"})-[*1..3]->(e:"+endlabel+") return distinct e.code,e.name,e.value";
    }

    /**
     * 根据Id查找获取，某个节点到指定类别
     * 节点所有可达节点。
     *
     * @param startId
     * @param endlabel
     * @return
     */
    public static String getPathEnds(Long startId, String endlabel){
        //,properties(e) AS properties
        return "MATCH (s)-[*1..3]->(e:"+endlabel+") where id(s)="+startId+" return distinct e";
    }

    public static String getPathEnds(String startlabel, Long startId, String endlabel){
        //,properties(e) AS properties
        return "MATCH (s:"+startlabel+")-[*1..3]->(e:"+endlabel+") where id(s)="+startId+" return distinct e";
    }

    public static String getNoDirectionEnds(String startlabel, Long startId, String endlabel){
        //,properties(e) AS properties
        return "MATCH (s:"+startlabel+")-[*1..3]->(e:"+endlabel+") where id(s)="+startId+" return distinct e";
    }

    /**
     * 关系查询
     *
     * @param aLabel
     * @param rlabel
     * @param bLabel
     * @param keys
     * @return
     * @author liuqiang
     * @date 2019年9月18日 下午6:53:52
     * @version V1.0
     */
    public static String relationBs(String aLabel, String rlabel, String bLabel, String keys[]){
        StringBuilder sb=new StringBuilder();
        sb.append("match(a");
        if(aLabel==null){
            sb.append(":"+aLabel);
        }
        sb.append(")-[r");
        if(rlabel==null){
            sb.append(":"+rlabel);
        }
        sb.append("]->(b");
        if(bLabel!=null){
            sb.append(":"+bLabel);
        }
        sb.append(") return ");
        sb.append(returnAColumn("b", keys));
        return sb.toString();
    }

    /**
     * 关系查询
     *
     * @param rlabel
     * @return
     * @author liuqiang
     * @date 2019年9月18日 下午6:53:52
     * @version V1.0
     */
    public static String relationAB(String rlabel){
        StringBuilder sb=new StringBuilder();
        sb.append("match(a");
        sb.append(")-[r");
        if(rlabel==null){
            sb.append(":"+rlabel);
        }
        sb.append("]->(b");
        sb.append(") return a,b");
        return sb.toString();
    }

    public static String createRelation(String rlabel, Long startId, List<Long> endIds){
        StringBuilder sb=new StringBuilder();
        sb.append("MATCH (a),(b)");
        sb.append("WHERE id(a) = "+startId+" AND id(b) IN ["
                +StringUtils.collectionToCommaDelimitedString(endIds)+"] ");
        sb.append("CREATE (a)-[r:"+rlabel+" ]->(b)");

        return sb.toString();
    }

    public static String createRelation(String rlabel, Long startId, Long endId){
        StringBuilder sb=new StringBuilder();
        sb.append("MATCH (a),(b)");
        sb.append("WHERE id(a) = "+startId+" AND id(b) = "+endId+" ");
        sb.append("CREATE (a)-[r:"+rlabel+"{name:\""+rlabel+"\"} ]->(b)");

        return sb.toString();
    }

    public static String createRelation(String rlabel, String name, Long startId, Long endId){
        StringBuilder sb=new StringBuilder();
        sb.append(" MATCH (a),(b)");
        sb.append(" WHERE id(a) = "+startId+" AND id(b) = "+endId+" ");
        sb.append(" CREATE (a)-[r:"+rlabel);
        if(name!=null&&!"".equals(name)){
            sb.append("{name:\""+name+"\"}");
        }else{
            sb.append("{name:\""+rlabel+"\"}");
        }

        sb.append(" ]->(b)");

        return sb.toString();
    }

    public static String existRelation(String rlabel, Long startId, Long endId){
        StringBuilder sb=new StringBuilder();
        sb.append("MATCH (a),(b) ");
        sb.append("WHERE id(a) = "+startId+" AND id(b) = "+endId+" ");
        sb.append("return exists((a)-[:"+rlabel+" ] -> (b)) AS relExist");

        return sb.toString();
    }

    public static String existRelation(String rlabel, Long startId, Long endId, Map<String, Object> param){
        StringBuilder sb=new StringBuilder();
        sb.append("MATCH (a),(b) ");
        sb.append("WHERE id(a) = "+startId+" AND id(b) = "+endId+" ");
        sb.append("return exists((a)-[:"+rlabel+relProp(param)+" ] -> (b)) AS relExist");
        return sb.toString();
    }

    public static String createRelation(String rlabel, Long startId, Long endId, Map<String, Object> param){
        StringBuilder sb=new StringBuilder();
        sb.append("MATCH (a),(b)");
        sb.append("WHERE id(a) = "+startId+" AND id(b) = "+endId+" ");
        sb.append("CREATE (a)-[r:"+rlabel+relProp(param)+"]->(b)");

        return sb.toString();
    }

    public static String createRelation(String rlabel, String rName, String startId, String endId){
        String[] starts=startId.split(",");
        String[] ends=endId.split(",");
        if(starts.length>=1||ends.length>=1){
            StringBuilder sb=new StringBuilder();
            for(String si : starts){
                for(String ei : ends){
                    String createRelation=createRelation(rlabel, rName, Long.valueOf(si), Long.valueOf(ei));
                    sb.append(createRelation);
                }
            }
            return sb.toString();
        }
        return createRelation(rlabel, rName, Long.valueOf(startId), Long.valueOf(endId));
    }

    public static String createRelation(String rlabel, String startId, String endId){
        String[] starts=startId.split(",");
        String[] ends=endId.split(",");
        if(starts.length>=1||ends.length>=1){
            StringBuilder sb=new StringBuilder();
            for(String si : starts){
                for(String ei : ends){
                    String createRelation=createRelation(rlabel, Long.valueOf(si), Long.valueOf(ei));
                    sb.append(createRelation);
                }
            }
            return sb.toString();
        }
        return createRelation(rlabel, Long.valueOf(startId), Long.valueOf(endId));
    }

    public static String createRelation(String rlabel, Long startId, String endId){
        return createRelation(rlabel, startId, Long.valueOf(endId));
    }

    public static String createRelation(String rlabel, String startId, Long endId){
        return createRelation(rlabel, startId, Long.valueOf(endId));
    }

    public static String relationBs(String rlabel, String keys[]){
        return relationBs(null, rlabel, null, keys);
    }

    /**
     * 生成查询语句
     *
     * @param props
     * @param label
     * @return
     */
    public static String queryObj(Map<String, Object> props, String label, String[] keySet){

        if(props==null||props.isEmpty()){
            return listAllData(label, keySet);
        }else{
            StringBuilder sb=propString(props);
            if(keySet!=null&&keySet.length>1){
                StringBuilder ret= new StringBuilder();
                ret.append("match (n : "+label+"{"+sb.toString()+"}) return "+returnColumn("n", keySet));
                appendOrderby(keySet, ret);
                return ret.toString();
            }
            return "match (n : "+label+"{"+sb.toString()+"}) return n";
        }
    }

    public static String safeQueryObj(Map<String, Object> props, String label, String[] keySet){

        if(props==null||props.isEmpty()){
            return listAllData(label, keySet);
        }else{
            StringBuilder sb=safePropString(props);
            if(keySet!=null&&keySet.length>1){
                StringBuilder ret= new StringBuilder();
                ret.append("match (n : "+label+"{"+sb.toString()+"}) return "+returnColumn("n", keySet));
                appendOrderby(keySet, ret);
                return ret.toString();
            }
            return "match (n : "+label+"{"+sb.toString()+"}) return n";
        }
    }

    public static String listAllData(String label, String[] keySet){
        if(keySet!=null&&keySet.length>1){
            StringBuilder ret= new StringBuilder();
            ret.append("match (n : "+label+") return "+returnColumn("n", keySet));
            appendOrderby(keySet, ret);
            return ret.toString();
        }
        return "match (n : "+label+") return n";
    }

    public static String queryObj(String label, String[] keySet){
        return queryObj(null, label, keySet);
    }

    /**
     * updateCypher
     *
     * @param props
     * @param label
     * @param keys
     * @return
     */
    public static String update(Map<String, Object> props, String label, String[] keys){
        StringBuilder setProp=setProp(props, keys);
        if(label!=null){
            return "match (n : "+label+") "+setProp.toString()+" where id(n)="+id(props);
        }else{
            return "match (n) "+setProp.toString()+" where id(n)="+id(props);
        }
    }

    /**
     * 查询单个节点
     *
     * @param props
     * @param label
     * @param keys
     * @return
     * @author liuqiang
     * @date 2019年9月19日 下午1:37:05
     * @version V1.0
     */
    public static String findNodeMapBy(Map<String, Object> props, String label, String[] keys){
        StringBuilder sb=keyString(props, keys);
        return "match (n : "+label+"{"+sb.toString()+"})  return "+returnColumn(props.keySet());
    }

    public static String findNodeBy(Map<String, Object> props, String label, String[] keys){
        StringBuilder sb=keyString(props, keys);
        return "match (n : "+label+"{"+sb.toString()+"})  return n";
    }

    /**
     * 分页查询
     *
     * @param props
     * @param label
     * @param keySet
     * @param page
     * @return
     */
    public static String queryObj(JSONObject props, String label, String[] keySet, PageObject page){
        String skipPage=pageSkip(page);
        if((props==null||props.isEmpty())&&(keySet==null||keySet.length<1)){
            return "match (n : "+label+") return n ORDER BY n.updateTime DESC,n.createTime DESC";
        }else{
            StringBuilder ret=listAllObject(props, label, keySet);
            ret.append(skipPage);
            return ret.toString();
        }
    }

    public static String queryObj2(JSONObject props, String label, String[] keySet, PageObject page){
        String skipPage=pageSkip(page);
        if((props==null||props.isEmpty())&&(keySet==null||keySet.length<1)){
            return "match (n : "+label+") return n ORDER BY n.updateTime DESC,n.createTime DESC";
        }else{
            StringBuilder ret=listAllObject2(props, label, keySet);
            ret.append(skipPage);
            return ret.toString();
        }
    }

    /**
     * 根据属性查询
     *
     * @param props
     * @param page
     * @return
     */
    public static String queryByProps(JSONObject props, PageObject page){
        String skipPage=pageSkip(page);
        StringBuilder ret=new StringBuilder();
        ret.append("match (n) where ");
        ret.append(whereSearchString(props).toString());
        ret.append(" return n");
        ret.append(skipPage);
        return ret.toString();
    }

    /**
     * 从视图对象中获取Cypher，并追加查询条件。已创建时间排序。
     * cypher的变量：a、b、c、d
     *
     * @param vo
     * @param props
     * @param page
     * @return
     */
    public static String voListByProps(Map<String, Object> vo, Map<String,Object> props, PageObject page){
        if(vo==null){
            return null;
        }
        String skipPage=pageSkip(page);
        StringBuilder ret=new StringBuilder();
        String cypher=(String) vo.get("cypher");
        String[] split=cypher.split(" return ");
        ret.append(split[0]);
        String match=split[0].trim();
        //附加查询条件
        StringBuilder voWhereSearchString=voWhereSearchString(props);
        if(!voWhereSearchString.toString().trim().isEmpty()){
            if(match.indexOf(" where")>0){
                if(isNeedAnd(match)){
                    ret.append(" and ");
                }
            }else{
                ret.append(" where ");
            }
            ret.append(voWhereSearchString);
        }

        ret.append(" return ");
        ret.append(split[1]);
        ret.append(" order by a.createTime desc ");
        if(!"".equals(skipPage)){
            ret.append(skipPage);
        }

        return ret.toString();
    }

    /**
     * 根据关系实例查询数据列表
     *
     * @param props
     * @param endLabel
     * @param keySet
     * @param page
     * @return
     */
    public static String queryByRelInstance(JSONObject props, String endLabel, String relLabel, String[] keySet,
                                            PageObject page, String startLabel, String startId){
        String skipPage=pageSkip(page);
        StringBuilder ret=relationEndList(relLabel, startLabel, startId, endLabel, keySet);
        ret.append("");
        ret.append(skipPage);
        return ret.toString();
    }

    public static StringBuilder listAllObject(JSONObject props, String label, String[] keySet){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n:"+label+")");
        if(!props.isEmpty()){
            StringBuilder sb=whereSearchString(props);
            if(sb.length()>1){
                ret.append(" where "+sb.toString()+" ");
            }
        }

        if(keySet!=null&&keySet.length>1){
            ret.append(" return "+returnColumn("n", keySet));
        }else{
            ret.append(" return n ");
        }
        appendOrderby(keySet, ret);
        return ret;
    }

    /**
     * 获取所有对象，更安全的获取方式，参数查询Cypher生成
     * @param props
     * @param label
     * @param keySet
     * @return
     */
    public static StringBuilder listAllObject2(JSONObject props, String label, String[] keySet){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n:"+label+")");
        if(!props.isEmpty()){
            StringBuilder sb=whereSearchString(props);
            if(sb.length()>1){
                ret.append(" where "+sb.toString()+" ");
            }
        }

        if(keySet!=null&&keySet.length>1){
            ret.append(" return "+returnColumn("n", keySet));
        }else{
            ret.append(" return n ");
        }
        appendOrderby(keySet, ret);
        return ret;
    }

    public static StringBuilder getAllObject(Map<String, Object> props, String label, String[] keySet){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n:"+label+")");
        if(props!=null&&!props.isEmpty()){
            StringBuilder sb=whereString(props);
            if(sb.length()>1){
                ret.append(" where "+sb.toString()+" ");
            }
        }

        if(keySet!=null&&keySet.length>0){
            ret.append(" return "+returnColumn("n", keySet));
        }else{
            ret.append(" return n ");
        }
        appendOrderby(keySet, ret);
        return ret;
    }

    /**
     * 状态机
     *
     * @param props
     * @param label
     * @return
     */
    public static StringBuilder validStatus(JSONObject props, String label){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n:"+label+")");
        if(!props.isEmpty()){
            StringBuilder sb=whereString(props);
            if(sb.length()>1){
                ret.append(" where "+sb.toString()+" ");
            }
        }
        return ret;
    }

    /**
     * 关系数据
     *
     * @param label
     * @param page
     * @return
     * @author liuqiang
     * @date 2019年9月20日 下午4:29:55
     * @version V1.0
     */
    public static String relationsPage(JSONObject start, String label, JSONObject endNode, PageObject page){
        String skipPage=pageSkip(page);
        StringBuilder ret=relations(start, label, endNode);
        ret.append(skipPage);
        return ret.toString();
    }

    public static String relationsPage(JSONObject start, JSONObject endNode, PageObject page){
        StringBuilder ret=relations(start, null, endNode);
        if(page!=null){
            String skipPage=pageSkip(page);
            ret.append(skipPage);
        }
        return ret.toString();
    }

    /**
     * 查询所有关系实例
     *
     * @param start
     * @param label
     * @param endNode
     * @return
     */
    public static StringBuilder relations(JSONObject start, String label, JSONObject endNode){
        StringBuilder ret=relationMatch(start, label, endNode);
        ret.append(" return n,m");
        return ret;
    }

    /**
     * 根据开始节点查询所有关系数据
     *
     * @param label
     * @param start
     * @param page
     * @return
     */
    public static String allRelation(String label, JSONObject start, PageObject page){
        String skipPage=pageSkip(page);

        StringBuilder ret=relationMatchAll(start, label);
        ret.append(" return r,m");
        ret.append(skipPage);
        return ret.toString();
    }

    /**
     * 查询所有结束节点信息
     *
     * @param start
     * @param label
     * @param endNode
     * @return
     */
    public static StringBuilder relationBs(JSONObject start, String label, JSONObject endNode){
        StringBuilder ret=relationMatch(start, label, endNode);
        ret.append(" return m");
        return ret;
    }

    public static StringBuilder addRelation(JSONObject start, String label, JSONObject endNode){
        StringBuilder ret=relationAdd(start, label, endNode);
        ret.append(" return r");
        return ret;
    }

    public static StringBuilder addRelation(JSONObject start, JSONObject relation, JSONObject endNode){
        StringBuilder ret=relationAdd(start, relation, endNode);
        ret.append(" return r");
        return ret;
    }

    /**
     * 删除关系
     *
     * @param startNode
     * @param label
     * @param endNode
     * @return
     */
    public static StringBuilder delRelations(Map<String, Object> startNode, String label, Map<String, Object> endNode){
        StringBuilder ret=relationMatch(startNode, label, endNode);
        ret.append(" delete r");
        return ret;
    }

    private static StringBuilder relationMatch(Map<String, Object> start, String label, Map<String, Object> endNode){
        StringBuilder ret=new StringBuilder();
        ret.append("match ");
        relationConditionBody(start, label, endNode, ret);
        return ret;
    }

    private static StringBuilder relationMatchAll(JSONObject start, String label){
        StringBuilder ret=new StringBuilder();
        ret.append("match ");
        allRelationOfStart(start, label, ret);
        return ret;
    }

    private static void allRelationOfStart(JSONObject start, String label, StringBuilder ret){
        ret.append("(n");
        startLabelAndProp(start, label, ret);
        ret.append(")-[r]->(m)");
    }

    private static void startLabelAndProp(JSONObject node, String label, StringBuilder ret){
        if((node!=null&&!node.isEmpty())){
            if(label!=null&&!label.equals("null")){
                // node.remove(LABEL);
                ret.append(":"+label);
            }
            if(!node.isEmpty()){
                StringBuilder sb=propString(node);
                ret.append("{"+sb.toString()+"}");
            }
        }
    }

    private static StringBuilder relationMatch(JSONObject start, JSONObject label, JSONObject endNode){
        StringBuilder ret=new StringBuilder();
        ret.append("match ");
        relationConditionBody(start, label, endNode, ret);
        return ret;
    }

    /**
     * 创建关系语句
     *
     * @param start
     * @param label
     * @param endNode
     * @return
     */
    private static StringBuilder relationAdd(JSONObject start, String label, JSONObject endNode){
        StringBuilder ret=new StringBuilder();
        ret.append("create ");
        relationConditionBody(start, label, endNode, ret);
        return ret;
    }

    /**
     * 关系实体 (n:label{})-[r:label]->(m:label{})
     *
     * @param start
     * @param label
     * @param endNode
     * @param ret
     */
    private static void relationConditionBody(Map<String, Object> start, String label, Map<String, Object> endNode,
                                              StringBuilder ret){
        ret.append("(n");
        validateLabelAndProp(start, ret);
        ret.append(")-[r");
        if(label!=null&&!label.isEmpty()){
            ret.append(":"+label);
        }
        ret.append("]->(m");
        validateLabelAndProp(endNode, ret);
        ret.append(")");
    }

    /**
     * 关系终点列表查询
     *
     * @param startLabel
     * @param startId
     * @param endLabel
     * @param keySet
     * @return
     */
    private static StringBuilder relationEndList(String relLabel, String startLabel, String startId, String endLabel,
                                                 String[] keySet){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n");

        if(startLabel!=null&&!startLabel.equals("null")){
            // node.remove(LABEL);
            ret.append(":"+startLabel);
        }
        ret.append(")-[r");
        if(relLabel!=null&&!relLabel.equals("null")){
            // node.remove(LABEL);
            ret.append(":"+relLabel);
        }
        ret.append("]->(m");
        if(endLabel!=null&&!endLabel.equals("null")){
            // node.remove(LABEL);
            ret.append(":"+endLabel);
        }
        ret.append(")");

        if(!startId.isEmpty()){
            ret.append("where id(n)="+startId);
        }
        if(keySet!=null&&keySet.length>1){
            ret.append(" return "+returnColumn("m", keySet));
        }else{
            ret.append(" return n,m ");
        }
        ret.append(" order by id(m) ");
        return ret;
    }

    public static StringBuilder relationExist(String relLabel, Long startId, Long endId){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n:"+META_DATA+")-[r");
        if(relLabel!=null&&!relLabel.equals("null")){
            ret.append(":"+relLabel);
        }
        ret.append("]->(m:"+META_DATA+")");

        if(null!=startId&&null!=endId){
            ret.append("where id(n)="+startId);
            ret.append(" and id(m)="+endId);
        }
        ret.append(" return r ");
        return ret;
    }

    /**
     * 关系创建，开始节点，关系属性，结束节点
     *
     * @param start
     * @param relation
     * @param endNode
     * @return
     */
    private static StringBuilder relationAdd(JSONObject start, JSONObject relation, JSONObject endNode){
        StringBuilder ret=new StringBuilder();
        ret.append("create ");
        relationConditionBody(start, relation, endNode, ret);
        return ret;
    }

    private static void relationConditionBody(JSONObject start, JSONObject relation, JSONObject endNode,
                                              StringBuilder ret){
        ret.append("(n");
        validateLabelAndProp(start, ret);

        ret.append(")-[r:");

        validateLabelAndProp(relation, ret);

        ret.append("]->(m");
        validateLabelAndProp(endNode, ret);
        ret.append(")");
    }

    /**
     * 节点信息和条件 append :label{propString}
     *
     * @param node
     * @param ret
     */
    private static void validateLabelAndProp(Map<String, Object> node, StringBuilder ret){
        if((node!=null&&!node.isEmpty())){
            String aLabel=String.valueOf(node.get(LABEL));

            if(aLabel!=null&&!aLabel.equals("null")){
                // node.remove(LABEL);
                ret.append(":"+aLabel);
            }
            node.remove(LABEL);
            if(!node.isEmpty()){
                StringBuilder sb=propString(node);
                ret.append("{"+sb.toString()+"}");
            }
        }
    }

    /**
     * 返回分页信息
     *
     * @param page
     * @return
     */
    private static String pageSkip(PageObject page){
        if(page==null){
            return "";
        }
        int limit=page.getPageSize();// 每页条数
        int skip=(page.getPageNum()-1)*limit;// 第几页
        String skipPage=" SKIP "+skip+" Limit "+limit+" ";
        return skipPage;
    }

    /**
     * 查询
     *
     * @param props
     * @param label
     * @return
     */
    public static String queryObj(JSONObject props, String label){
        return queryObj(props, label, null);
    }

    public static String returnColumn(String nodeSymble, String[] keySet){
        StringBuilder sbRet=new StringBuilder();
        Boolean idUsed=false;
        for(String key : keySet){
            if(!NODE_ID.equalsIgnoreCase(key)&&sbRet.length()>1){
                sbRet.append(",");
            }
            if(NODE_ID.equalsIgnoreCase(key)&&!idUsed){
                sbRet.append("id("+nodeSymble+") as "+key);
                idUsed=true;
            }

            if(!NODE_ID.equalsIgnoreCase(key)){
                if(key.contains(" as ")){
                    sbRet.append(nodeSymble+"."+key);
                }else{
                    sbRet.append(nodeSymble+"."+key+" as "+key);
                }
            }
        }
        return sbRet.toString();
    }

    private static String returnColumn(Set<String> keySet){
        StringBuilder sbRet=new StringBuilder();

        for(String key : keySet){
            if(sbRet.length()>1){
                sbRet.append(",");
            }
            sbRet.append("n."+key+"");
        }
        return sbRet.toString();
    }

    private static String returnAColumn(String label, String[] keySet){
        StringBuilder sbRet=new StringBuilder();
        for(String key : keySet){
            if(sbRet.length()>1){
                sbRet.append(",");
            }
            sbRet.append(label+"."+key+" ");
        }
        return sbRet.toString();
    }

    private static String returnABColumn(String[] keySet){
        StringBuilder sbRet=new StringBuilder();
        for(String key : keySet){
            if(sbRet.length()>1){
                sbRet.append(",");
            }
            sbRet.append(key+" ");
        }
        return sbRet.toString();
    }

    public static StringBuilder keyString(Map<String, Object> props, String[] keys){
        StringBuilder sb=new StringBuilder();
        for(String key : keys){
            Object value=props.get(key);
            if(value!=null){
                if(sb.length()>1){
                    sb.append(",");
                }
                sb.append(key+":\""+String.valueOf(value)+"\"");
            }
        }
        return sb;
    }

    private static StringBuilder setProp(Map<String, Object> props, String[] keys){
        StringBuilder sb=new StringBuilder();
        String join=String.join(",", keys);
        for(String key : props.keySet()){
            if(join.contains(key)){
                Object value=props.get(key);
                if(value!=null){
                    if(sb.length()<1){
                        sb.append(" SET  ");
                    }else{
                        sb.append(" , ");
                    }
                    sb.append(" n."+key+"='"+String.valueOf(value)+"' ");
                }
            }
        }
        return sb;
    }

    /**
     * 属性 key:value, keyN:valueN,...., keyM:valueM
     *
     * @param props
     * @return
     */
    private static StringBuilder propString(Map<String, Object> props){
        StringBuilder sb=new StringBuilder();
        if(props.containsKey("id")){
            Object value=props.get("id");
            sb.append("id:\""+String.valueOf(value)+"\"");
            return sb;
        }
        for(Entry<String, Object> entryi : props.entrySet()){
            Object value=entryi.getValue();
            if(value!=null&&Strings.isNotEmpty(String.valueOf(value))){
                if(sb.length()>1){
                    sb.append(",");
                }
                sb.append(entryi.getKey()+":\""+String.valueOf(value)+"\"");
            }
        }

        return sb;
    }

    private static StringBuilder safePropString(Map<String, Object> props){
        StringBuilder sb=new StringBuilder();
        if(props.containsKey("id")){
            Object value=props.get("id");
            sb.append("id:$id ");
            return sb;
        }
        for(Entry<String, Object> entryi : props.entrySet()){
            Object value=entryi.getValue();
            if(value!=null&&Strings.isNotEmpty(String.valueOf(value))){
                if(sb.length()>1){
                    sb.append(",");
                }
                sb.append(entryi.getKey()+": $"+entryi.getKey()+" ");
            }
        }

        return sb;
    }

    public static String relProp(Map<String, Object> props){
        StringBuilder sb1=new StringBuilder();
        StringBuilder sb=new StringBuilder();
        for(Entry<String, Object> entryi : props.entrySet()){
            Object value=entryi.getValue();
            if(value!=null&&Strings.isNotEmpty(String.valueOf(value))){
                if(sb.length()>1){
                    sb.append(",");
                }
                sb.append(entryi.getKey()+":\""+String.valueOf(value)+"\"");
            }
        }
        sb1.append("{"+sb.toString()+"}");


        return sb1.toString();
    }

    /**
     * 模糊查询
     *
     * @param props
     * @return
     */
    private static StringBuilder whereSearchString(Map<String, Object> props){
        StringBuilder sb=new StringBuilder();
        for(Entry<String, Object> entryi : props.entrySet()){
            Object value=entryi.getValue();
            if(value!=null&&Strings.isNotBlank(String.valueOf(value))){
                if(sb.length()>1&&isNeedAnd(sb)){
                    sb.append(" and ");
                }
                String key=entryi.getKey();
                if(needEqual(key)){
                    sb.append("n."+key+"='"+String.valueOf(value)+"'");
                }else{
                    sb.append("n."+key+" CONTAINS '"+String.valueOf(value)+"'");
                }

            }
        }

        return sb;
    }

    /**
     * 安全的查询条件：参数化查询Cypher生成。
     * @param props
     * @return
     */
    private static StringBuilder safeWhereSearchString(Map<String, Object> props){
        StringBuilder sb=new StringBuilder();
        for(Entry<String, Object> entryi : props.entrySet()){
            Object value=entryi.getValue();
            if(value!=null&&Strings.isNotBlank(String.valueOf(value))){
                if(sb.length()>1&&isNeedAnd(sb)){
                    sb.append(" and ");
                }
                String key=entryi.getKey();
                if(needEqual(key)){
                    sb.append(" n."+key+"='"+String.valueOf(value)+"' ");
                }else{
                    sb.append(" n."+key+" CONTAINS '"+String.valueOf(value)+"' ");
                }
            }
        }

        return sb;
    }

    private static StringBuilder voWhereSearchString(Map<String, Object> props){
        StringBuilder sb=new StringBuilder();
        if(props==null||props.isEmpty()){
            return sb;
        }

        for(Entry<String, Object> entryi : props.entrySet()){
            Object value=entryi.getValue();
            if(value!=null&&Strings.isNotBlank(String.valueOf(value))){
                if(sb.length()>1&&isNeedAnd(sb)){
                    sb.append(" and ");
                }
                String key=entryi.getKey();

                if(key.contains(".")){
                    String keys[]=key.split("\\.");
                    if(keys[1].toLowerCase().equals("id")){
//                    sb.append("a."+key+"='"+String.valueOf(value)+"'");
                        sb.append("id("+keys[0]+")=" + String.valueOf(value));
                    }else{
                        sb.append(key+"='"+String.valueOf(value)+"'");
                    }
                }else{
                    if(needEqual(key)){
                        sb.append("a."+key+"='"+String.valueOf(value)+"'");
                    }else{
                        sb.append("a."+key+" CONTAINS '"+String.valueOf(value)+"'");
                    }
                }
            }
        }

        return sb;
    }

    /**
     * 精确查询
     *
     * @param key
     * @return
     */
    private static boolean needEqual(String key){
        key=key.toLowerCase();
        boolean equalKey=key.endsWith("id")||key.equals("creator")||key.equals("updator");
        return equalKey;
    }

    /**
     * 精确查询
     *
     * @param props
     * @return
     */
    public static StringBuilder whereString(Map<String, Object> props){
        StringBuilder sb=new StringBuilder();
        for(Entry<String, Object> entryi : props.entrySet()){
            Object value=entryi.getValue();
			if(value!=null){
				if(sb.length()>1&&isNeedAnd(sb)){
					sb.append(" and ");
				}
				if(value instanceof List && ((List<String>)value).size()>0){
					List<String> ids = ((List<String>)value).stream().map(String::valueOf).collect(Collectors.toList());
					sb.append("n."+entryi.getKey()+" in ['"+String.join("','",ids)+"']");
				}else if(Strings.isNotBlank(String.valueOf(value))){
					sb.append("n."+entryi.getKey()+"='"+String.valueOf(value)+"'");
				}
			}

        }

        return sb;
    }
}
