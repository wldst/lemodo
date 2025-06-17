package com.wldst.ruder.module.fun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.domain.AuthDomain;
import com.wldst.ruder.domain.CypherDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.PageObject;

/**
 * 数据操作功能,根据用户角色来。
 *
 * @author deeplearn96
 */
@Service
public class Neo4jOptByUser extends CypherDomain{
    private UserAdminService adminService;
    private CrudUtil crudUtil;
    private CrudNeo4jService neo4jService;

    @Autowired
    public Neo4jOptByUser(@Lazy CrudUtil crudUtil, @Lazy CrudNeo4jService neo4jService, @Lazy UserAdminService adminService){
        this.neo4jService=neo4jService;
        this.crudUtil=crudUtil;
        this.adminService=adminService;
    }

    /**
     * 生成删除语句
     *
     * @param props
     * @param label
     * @return
     */
    public String delObj(Map<String, Object> props, String label){
        props=creatorInfo(props, label);
        StringBuilder sb=propString(props);
        return "match (n : "+label+"{"+sb.toString()+"}) delete n";
    }

    private Map<String, Object> creatorInfo(Map<String, Object> props, String label){
        if(props==null||props.isEmpty()){
            props=new HashMap<String, Object>();
        }
        //判断是否需要添加创建者信息
        List<Map<String, Object>> commonLabels=neo4jService.cypher("Match(n:ResourceType{code:'CommonResource'})-[r]->(m:"+META_DATA+") return m.label");
        if("syscode".equals(label)||commonLabels!=null&&!commonLabels.isEmpty()&&commonLabels.contains(label)){
            return props;
        }

        String currentAccount=adminService.getCurrentAccount();
        if(!currentAccount.equals("Server")){
            boolean notAuth=isNotAuth(label, currentAccount);
            if(adminService!=null&&!adminService.hasPermission(AuthDomain.CREATROR_AUTH)&&notAuth){
                if(!props.containsKey("creator")){

                    if(currentAccount!=null&&!"".equals(currentAccount.trim())){
                        props.put("creator", currentAccount);
                    }
                }
            }
        }

//	// if (props.containsKey("creator")) {
//	props.put("creator", adminService.getCurrentAccount());
//	// }
        return props;
    }

    private JSONObject creatorInfo(JSONObject props, String label){
        if(props==null||props.isEmpty()){
            props=new JSONObject();
        }
        //

        // String hasPath2EndNode = optByUserSevice.hasPath2EndNode(getCurrentUserId(),
        // nodeId);
        // 如果当前用户与此资源可达，则不用添加创建者信息。

        // 路径可达实现模式
        if(adminService!=null&&!adminService.hasPermission(AuthDomain.CREATROR_AUTH)){
            if(!props.containsKey("creator")){
                String currentAccount= adminService.getCurrentAccount();
                if(!currentAccount.equals("Server")){
                    props.put("creator", adminService.getCurrentAccount());
                }
            }
        }

        return props;
    }

    public String delObj(String label){
        return delObj(null, label);
    }

    public String delRelaOf(Map<String, Object> props, String label){
        props=creatorInfo(props, label);
        if(props==null||props.isEmpty()){
            return "match (n : "+label+")-[r]->(b) delete r";
        }else{
            StringBuilder sb=propString(props);
            return "match (n : "+label+"{"+sb.toString()+"})-[r]->(b) delete r";
        }
    }

    public String delRelbOf(String label){
        return delRelbOf(null, label);
    }

    public String delRelaOf(String label){
        return delRelaOf(null, label);
    }

    public String delRelbOf(Map<String, Object> props, String label){
        props=creatorInfo(props, label);
        if(props==null||props.isEmpty()){
            return "match (b)-[r]->(n : "+label+") delete r";
        }else{
            StringBuilder sb=propString(props);
            return "match (b)-[r]->(n : "+label+"{"+sb.toString()+"}) delete r";
        }
    }

    /**
     * 判断是否有 开始节点通往结束节点的路径存在
     *
     * @param startId
     * @param endId
     * @return
     */
    public String hasPath2EndNode(Long startId, Long endId){
        return "MATCH p = (a)-[*..3]->(b) where id(a)="+startId+" and id(b)="+endId
                +" return relationships(p)";
    }

    public String hasPermission(Long startId, Long endId){
        return "MATCH p = (a)-[r:HAS_PERMISSION*1..3]->(b) where id(a)="+startId+" and id(b)="+endId
                +" return relationships(p)";
    }

    public String getNodesOfPath(Long startId, Long endId){
//	return "MATCH p = (a)-[*2..5]->(b) where id(a)=" + startId + " and id(b)=" + endId
//		+ " return p"; nodes(p), relationships(p)
        return " MATCH (a),(b) where id(a)="+startId+" and id(b)="+endId
                +" with a,b MATCH p = (a)-[*1..3]->(b) "
                +" with p unwind nodes(p) as x return properties(x) AS nodeP";
    }

    public String getRelsOfPath(Long startId, Long endId){
//	return "MATCH p = (a)-[*2..5]->(b) where id(a)=" + startId + " and id(b)=" + endId
//		+ " return p"; nodes(p), relationships(p)
        return " MATCH (a),(b) where id(a)="+startId+" and id(b)="+endId
                +" with a,b MATCH p = (a)-[*1..3]->(b) "
                +" with p unwind relationships(p) as x return type(x) as r,x.name as rName, startNode(x) as s, endNode(x) as e";
    }

    public String delRel(Long startId, Long endId, String relType){
//	return "MATCH p = (a)-[*2..5]->(b) where id(a)=" + startId + " and id(b)=" + endId
//		+ " return p"; nodes(p), relationships(p)
        return " MATCH (a)-[r:"+relType+"]->(b) where id(a)="+startId+" and id(b)="+endId

                +"   delete r ";
    }


    public String queryPath(String startLabel, Long startId, String endLabel, Long endId){
        String cypher="MATCH p = (a";
        if(startLabel!=null&&!"".equals(startLabel)){
            cypher=cypher+":"+startLabel;
        }
        cypher=cypher+")-[*]->(b";
        if(endLabel!=null&&!"".equals(endLabel)){
            cypher=cypher+":"+endLabel;
        }
        cypher=cypher+") ";
        if(startId!=null||endId!=null){
            cypher=cypher+" where ";
            boolean hasOne=false;
            if(startId!=null){
                hasOne=true;
                cypher=cypher+" id(a)="+startId;
            }
            if(endId!=null){
                if(hasOne){
                    cypher=cypher+" and ";
                }
                cypher=cypher+" id(b)="+endId;
            }
        }
        cypher=cypher+" return p";
        return cypher;
    }

    /**
     * 判断两节点是否可达
     *
     * @param startLabel
     * @param startId
     * @param endLabel
     * @param endId
     * @return
     */
    public String isReachAble(String startLabel, Long startId, String endLabel, Long endId){
        StringBuffer sBuffer=new StringBuffer();
        sBuffer.append("MATCH p = (a");
        if(startLabel!=null){
            sBuffer.append(":"+startLabel);
        }
        sBuffer.append(")-[*]->(b");
        if(endLabel!=null){
            sBuffer.append(": "+endLabel);
        }
        sBuffer.append(") ");
        if(startId!=null||endId!=null){
            sBuffer.append("where ");
            boolean startIdNotnull=false;
            if(startId!=null){
                startIdNotnull=true;
                sBuffer.append("id(a)="+startId);
            }

            if(endId!=null){
                if(startIdNotnull){
                    sBuffer.append(" and ");
                }
                sBuffer.append(" id(b)="+endId);
            }
        }

        sBuffer.append(" return p");

        return sBuffer.toString();

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
    public String relationBs(String aLabel, String rlabel, String bLabel, String keys[]){
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
    public String relationAB(String rlabel){
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

    public String relationBs(String rlabel, String keys[]){
        return relationBs(null, rlabel, null, keys);
    }

    /**
     * 生成查询语句
     *
     * @param props
     * @param label
     * @return
     */
    public String queryObj(Map<String, Object> props, String label, String[] keySet){
        props=creatorInfo(props, label);
        if(props==null||props.isEmpty()){
            if(keySet!=null&&keySet.length>1){
                return "match (n : "+label+") return "+returnColumn("n", keySet);
            }
            return "match (n : "+label+") return n";
        }else{
            StringBuilder sb=propString(props);
            if(keySet!=null&&keySet.length>1){
                return "match (n : "+label+"{"+sb.toString()+"}) return "+returnColumn("n", keySet);
            }
            return "match (n : "+label+"{"+sb.toString()+"}) return n";
        }
    }

    /**
     * 参数化查询
     * @param props
     * @param label
     * @param keySet
     * @return
     */
    public String safeQueryObj(Map<String, Object> props, String label, String[] keySet){
        props=creatorInfo(props, label);
        if(props==null||props.isEmpty()){
            if(keySet!=null&&keySet.length>1){
                return "match (n : "+label+") return "+returnColumn("n", keySet);
            }
            return "match (n : "+label+") return n";
        }else{
            StringBuilder sb=safePropString(props);
            if(keySet!=null&&keySet.length>1){
                return "match (n : "+label+"{"+sb.toString()+"}) return "+returnColumn("n", keySet);
            }
            return "match (n : "+label+"{"+sb.toString()+"}) return n";
        }
    }

    public String query(Map<String, Object> props, String label, String[] keySet){
        if(props==null||props.isEmpty()){
            if(keySet!=null&&keySet.length>1){
                return "match (n : "+label+") return "+returnColumn("n", keySet);
            }
            return "match (n : "+label+") return n";
        }else{
            StringBuilder sb=propString(props);
            if(keySet!=null&&keySet.length>1){
                return "match (n : "+label+"{"+sb.toString()+"}) return "+returnColumn("n", keySet);
            }
            return "match (n : "+label+"{"+sb.toString()+"}) return n";
        }
    }

    /**
     * updateCypher
     *
     * @param props
     * @param label
     * @param keys
     * @return
     */
    public String update(Map<String, Object> props, String label, String[] keys){
        StringBuilder sb=keyString(props, keys);
        StringBuilder setProp=setProp(props, keys);
        return "match (n : "+label+"{"+sb.toString()+"}) "+setProp.toString()+" return "
                +returnColumn(props.keySet());
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
    public String findNodeMapBy(Map<String, Object> props, String label, String[] keys){
        props=creatorInfo(props, label);
        StringBuilder sb=keyString(props, keys);
        return "match (n : "+label+"{"+sb.toString()+"})  return "+returnColumn(props.keySet());
    }

    public String findNodeBy(Map<String, Object> props, String label, String[] keys){
        props=creatorInfo(props, label);
        StringBuilder sb=keyString(props, keys);
        return "match (n : "+label+"{"+sb.toString()+"})  return n";
    }

    public String queryObj(Map<String, Object> props, String label, String[] keySet, PageObject page){
        return queryObj(props, label, keySet, page,null);
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
    public String queryObj(Map<String, Object> props, String label, String[] keySet, PageObject page,String orderBy){

        props=creatorInfo(props, label);
        if(orderBy!=null){
            if(orderBy.contains(",")){
               String orderByArr[]=orderBy.split(",");
                if((props==null||props.isEmpty())&&(keySet==null||keySet.length<1)){
                    return "match (n : "+label+") return n ORDER BY n."+orderByArr[0]+" "+orderByArr[1];
                }else{
                    StringBuilder ret=listAllObject(props, label, keySet);
                    String[] orders = ret.toString().split("ORDER BY");
                    StringBuilder by= new StringBuilder();
                    by.append(orders[0]+" ORDER BY n."+orderByArr[0]+" "+orderByArr[1]);
                    if(page!=null){
                        String skipPage=pageSkip(page);
                        by.append(skipPage);
                    }

                    return by.toString();
                }
            }else {
                if((props==null||props.isEmpty())&&(keySet==null||keySet.length<1)){
                    return "match (n : "+label+") return n ORDER BY n."+orderBy+" DESC";
                }else{
                    StringBuilder ret=listAllObject(props, label, keySet);
                    String[] orders = ret.toString().split("ORDER BY");
                    StringBuilder by= new StringBuilder();
                    by.append(orders[0]+" ORDER BY n."+orderBy+" DESC");
                    return by.toString();
                }
            }
        }
        if((props==null||props.isEmpty())&&(keySet==null||keySet.length<1)){
            return "match (n : "+label+") return n ORDER BY n.createTime DESC, n.updateTime DESC";
        }else{
            StringBuilder ret=listAllObject(props, label, keySet);
            if(page!=null){
                String skipPage=pageSkip(page);
                ret.append(skipPage);
            }

            return ret.toString();
        }

    }

    public String getObj(Map<String, Object> props, String label, String[] keySet, PageObject page){

        props=creatorInfo(props, label);
        if((props==null||props.isEmpty())&&(keySet==null||keySet.length<1)){
            return "match (n : "+label+") return n ORDER BY n.createTime DESC, n.updateTime DESC";
        }else{
            StringBuilder ret=getObject(props, label, keySet);
            if(page!=null){
                String skipPage=pageSkip(page);
                ret.append(skipPage);
            }

            return ret.toString();
        }
    }

    public String searchObj(Map<String, Object> props, String label, String[] keySet, PageObject page, String serchCols, String queryText){
        if((props==null||props.isEmpty())&&(keySet==null||keySet.length<1)){
            StringBuilder ret=new StringBuilder();
                     ret.append("match (n : "+label+") return n");
            appendOrderby(keySet, ret);
            return ret.toString();
        }else if(serchCols==null&&(keySet!=null&&keySet.length>1)){
            StringBuilder sb=new StringBuilder();
            for(String ki : keySet){
                if(!ki.equals(ID)&&!ki.toLowerCase().endsWith(ID)){
                    if(sb.length()>1){
                        sb.append(",");
                    }
                    sb.append(ki);
                }
            }
            StringBuilder ret=searchObject(props, label, keySet, sb.toString(), queryText);

            if(page!=null){
                String skipPage=pageSkip(page);
                ret.append(skipPage);
            }

            return ret.toString();

//	    return "match (n : " + label + ") return n ORDER BY n.createTime DESC, n.updateTime DESC";
        }else{
            StringBuilder ret=searchObject(props, label, keySet, serchCols, queryText);
           if(page!=null){
                String skipPage=pageSkip(page);
                ret.append(skipPage);
            }

            return ret.toString();
        }
    }

    public String queryAuthObj(Map<String, Object> props, String label, String[] keySet, PageObject page){

        if((props==null||props.isEmpty())&&(keySet==null||keySet.length<1)){
            return "MATCH (a:User)-[*1..2]-(rm:"+label+") where id(a)="+adminService.getCurrentUserId()+" return distinct rm";
        }else{
            StringBuilder ret=listAuthObject(props, label, keySet);
            if(page!=null){
                String skipPage=pageSkip(page);
                ret.append(skipPage);
            }

            return ret.toString();
        }
    }

    public String query(JSONObject props, String label, String[] keySet, PageObject page){
        String skipPage=pageSkip(page);
        if((props==null||props.isEmpty())&&(keySet==null||keySet.length<1)){
            return "match (n : "+label+") return n";
        }else{
            StringBuilder ret=listAllObject(props, label, keySet);
            ret.append(skipPage);
            return ret.toString();
        }
    }

    /**
     * match (obj:"+label+"),(f:Field),(m:MetaData)" f.objectId=id(obj) and
     * f.type=id(m)
     *
     * @param props
     * @param label
     * @param action
     * @param keySet
     * @return
     */
    public String moreQuery(JSONObject props, String label, String action, String[] keySet){
        return more(props, label, action, keySet);
    }

    public String moreQuery(JSONObject props){
        return more(props);
    }

    public String morePageQuery(JSONObject props, String label, String action, String[] keySet, PageObject page){
        String skipPage=pageSkip(page);
        return morePage(props, label, action, keySet, skipPage);
    }

    public String morePageQuery(JSONObject props, String label, String[] keySet, PageObject page){
        String skipPage=pageSkip(page);
        return morePage(props, skipPage);
    }

    private String morePage(JSONObject props, String label, String action, String[] keySet, String skipPage){
        if((props==null||props.isEmpty())&&(keySet==null||keySet.length<1)){
            return "match (n : "+label+") return n";
        }else{
            Long currentUserId=adminService.getCurrentPasswordId();
            StringBuilder sb=new StringBuilder();
            String isDoAction=MapTool.lowCaseStr(props, action);
            props.remove(action);// 动作不计入查询功能。
            if(isDoAction.equals("true")||isDoAction.equals("false")){
                String moreObj="n";
                if("Notice".equals(label)){
                    sb.append(" match (n:"+label+"),(p:Password) ");
                }else{
                    sb.append(" match (n:"+label+")-[*1..3]->(p:Password)  ");
                }

                if(isDoAction.equals("true")){
                    // (m:"+label+")-[:Team]->(t:Team)-[:member]->(u:User)-[:account]-(p:Password)
                    sb.append(" where  id(p)="+currentUserId+" "+"        and exists((p)-[:"+action
                            +"]->(n))  ");
                    appendWhereReturn(props, keySet, sb, moreObj);
                }
                if(isDoAction.equals("false")){
                    sb.append(" where id(p)="+currentUserId+" ");
                    sb.append("       and not exists((p)-[:"+action+"]->(n))  ");
                    appendWhereReturn(props, keySet, sb, moreObj);
                }
            }
            if(skipPage!=null&&!skipPage.isEmpty()){
                sb.append(skipPage);
            }
            return sb.toString();
        }
    }

    private String morePage(JSONObject props, String skipPage){
        String moreObj="n";
        Long currentUserId=adminService.getCurrentPasswordId();
        if((props==null||props.isEmpty())){
            StringBuilder sb=new StringBuilder();
            sb.append(" match ("+moreObj+")-[*2..4]-(p:Password)  ");
            sb.append(" where id(p)="+currentUserId+" ");
            sb.append("       and not exists((p)-->("+moreObj+"))  return  distinct(labels("+moreObj+"))  AS labels");
            if(skipPage!=null&&!skipPage.isEmpty()){
                sb.append(skipPage);
            }
            return sb.toString();
        }else{
            StringBuilder sb=new StringBuilder();
            sb.append(" match ("+moreObj+")-[*2..4]-(p:Password)  ");
            sb.append(" where id(p)="+currentUserId+" ");
            sb.append("       and not exists((p)-->("+moreObj+"))  return distinct(labels("+moreObj+"))  AS labels");
            appendWhere(props, sb, moreObj);
            if(skipPage!=null&&!skipPage.isEmpty()){
                sb.append(skipPage);
            }
            return sb.toString();
        }
    }

    public String moreDataPage(JSONObject props, PageObject page, Set<String> labels){
        String skipPage=pageSkip(page);
        String moreObj="n";
        Long currentUserId=adminService.getCurrentPasswordId();
        if((props==null||props.isEmpty())){
            StringBuilder sb=new StringBuilder();
            sb.append(" match ("+moreObj+")-[*2..4]-(p:Password)  ");
            sb.append(" where id(p)="+currentUserId+" ");
            sb.append("       and not exists((p)-->("+moreObj+"))  and  any( label in labels("+moreObj+") where label in ['"+String.join("','", labels)+"']) ");
            sb.append(" return "+moreObj);
            if(skipPage!=null&&!skipPage.isEmpty()){
                sb.append(skipPage);
            }
            return sb.toString();
        }else{
            StringBuilder sb=new StringBuilder();
            sb.append(" match ("+moreObj+")-[*2..4]-(p:Password)  ");
            sb.append(" where id(p)="+currentUserId+" ");
            appendWhere(props, sb, moreObj);
            sb.append("       and not exists((p)-->("+moreObj+"))  and any( label in labels("+moreObj+") where label in ['"+String.join("','", labels)+"']) ");
            sb.append(" return "+moreObj);
            if(skipPage!=null&&!skipPage.isEmpty()){
                sb.append(skipPage);
            }
            return sb.toString();
        }
    }

    public String moreDataCount(JSONObject props, Set<String> labels){
        String moreObj="n";
        Long currentUserId=adminService.getCurrentPasswordId();
        if((props==null||props.isEmpty())){
            StringBuilder sb=new StringBuilder();
            sb.append(" match ("+moreObj+")-[*2..4]-(p:Password)  ");
            sb.append(" where id(p)="+currentUserId+" ");
            sb.append("       and not exists((p)-->("+moreObj+"))  and  any( label in labels("+moreObj+") where label in ['"+String.join("','", labels)+"']) ");
            sb.append(" return labels(n) As mLabel,count(distinct("+moreObj+")) AS more");

            return sb.toString();
        }else{
            StringBuilder sb=new StringBuilder();
            sb.append(" match ("+moreObj+")-[*2..4]-(p:Password)  ");
            sb.append(" where id(p)="+currentUserId+" ");
            appendWhere(props, sb, moreObj);
            sb.append("       and not exists((p)-->("+moreObj+"))  and any( label in labels("+moreObj+") where label in ['"+String.join("','", labels)+"']) ");
            sb.append(" return labels(n) As mLabel,count("+moreObj+") AS more");
            return sb.toString();
        }
    }

    public String moreMetaDataPage(PageObject page, Set<String> labels){
        String skipPage=pageSkip(page);
        String moreObj="n";
        Long currentUserId=adminService.getCurrentPasswordId();
        StringBuilder sb=new StringBuilder();
        sb.append(" match ("+moreObj+":"+META_DATA+")-[*2..4]-(p:Password)  ");
        sb.append(" where id(p)="+currentUserId+" ");
        sb.append("         and "+moreObj+".label  in ['"+String.join("','", labels)+"'] ");
        sb.append(" return distinct("+moreObj+")  as "+moreObj);
        if(skipPage!=null&&!skipPage.isEmpty()){
            sb.append(skipPage);
        }
        return sb.toString();
    }

    private String more(JSONObject props, String label, String action, String[] keySet){
        return morePage(props, label, action, keySet, null);
    }

    private String more(JSONObject props){
        return morePage(props, null);
    }

    private void appendWhereReturn(JSONObject props, String[] keySet, StringBuilder sb, String moreObj){
        if(props!=null&&!props.isEmpty()){
            StringBuilder sbw=whereSearchString(moreObj, props, keySet);
            if(sbw.length()>1&&isNeedAnd(sb)){
                sb.append(" and ");
            }
            sb.append(sbw.toString()+" ");
        }
        returnKeySet(moreObj, keySet, sb);
    }

    private void appendWhereReturn(JSONObject props, StringBuilder sb, String moreObj){
        appendWhere(props, sb, moreObj);
        sb.append(" return "+moreObj);
    }

    private void appendWhere(JSONObject props, StringBuilder sb, String moreObj){
        if(props!=null&&!props.isEmpty()){
            StringBuilder sbw=whereSearchString(moreObj, props, null);

            if(sbw.length()>1&&isNeedAnd(sb)){
                sb.append(" and ");
            }
            sb.append(sbw.toString()+" ");
        }
    }

    public String cypherPage(String cypher, PageObject page){
        String skipPage=pageSkip(page);
        return cypher+skipPage;
    }

    public String cqueryObj(JSONObject props, String label, String[] keySet, PageObject page){
        String skipPage=pageSkip(page);
        props=creatorInfo(props, label);
        if((props==null||props.isEmpty())&&(keySet==null||keySet.length<1)){
            return "match (n : "+label+") return n";
        }else{
            StringBuilder ret=listAllObject2(props, label, keySet);
            ret.append(skipPage);
            return ret.toString();
        }
    }

    public String safeNormalQueryObj(JSONObject props, String label, String[] keySet, PageObject page){
        String skipPage=pageSkip(page);
        // props = creatorInfo(props, label);
        if((props==null||props.isEmpty())&&(keySet==null||keySet.length<1)){
            return "match (n : "+label+") return n";
        }else{
            StringBuilder ret=listAllObject(props, label, keySet);
            ret.append(skipPage);
            return ret.toString();
        }
    }

    public String getTopOne(JSONObject props, String label, String[] keySet){
        String skipPage=topOne();
        StringBuilder ret=listAllObject(props, label, keySet);
        ret.append(skipPage);
        return ret.toString();
    }

    public String getTopOneByCql(String cypher){
        String skipPage=topOne();
        return cypher+skipPage;
    }

    public String appendPage(PageObject page, StringBuilder ret){
        String skipPage=pageSkip(page);
        ret.append(skipPage);
        return ret.toString();
    }

    public String appendPage(PageObject page, String ret){
        String skipPage=pageSkip(page);

        return ret+skipPage;
    }

    public String voListByProps(Map<String, Object> po, JSONObject props, PageObject page){
        String skipPage=pageSkip(page);
        StringBuilder ret=new StringBuilder();
        String object=(String) po.get("cypher");
        String[] split=object.split("return ");
        ret.append(split[0]);
        String trim=split[0].trim();
        StringBuilder voWhereSearchString=voWhereSearchString(props);
        if(!voWhereSearchString.toString().trim().isEmpty()){
            if(trim.indexOf("where")>0){
                if(trim.length()>1&&isNeedAnd(trim)){
                    ret.append(" and ");
                }
            }else{
                ret.append(" where ");
            }
            ret.append(voWhereSearchString);
        }

        ret.append(" return ");
        ret.append(split[1]);
        ret.append(skipPage);
        return ret.toString();
    }

    /**
     * 根据关系实例查询数据列表
     *
     * @param props
     * @param label
     * @param keySet
     * @param page
     * @return
     */
    public String queryByRelInstance(JSONObject props, String label, String relLabel, String[] keySet, PageObject page,
                                     String instanceLabel, String instanceId){
        String skipPage=pageSkip(page);
        StringBuilder ret=relationEndList(relLabel, instanceLabel, instanceId, label, keySet);
        ret.append(skipPage);
        return ret.toString();
    }

    public StringBuilder listAllObject(Map<String, Object> props, String label, String[] keySet){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n:"+label+")");
        if(props!=null&&!props.isEmpty()){
            StringBuilder sb=whereSearchString(props, keySet, label);
            if(sb.length()>1){
                ret.append(" where "+sb.toString()+" ");
            }
        }

        returnKeySet(keySet, ret);
        appendOrderby(keySet, ret);

        return ret;
    }

    public StringBuilder getObject(Map<String, Object> props, String label, String[] keySet){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n:"+label+")");
        if(props!=null&&!props.isEmpty()){
            StringBuilder sb=whereString(props, keySet, label);
            if(sb.length()>1){
                ret.append(" where "+sb.toString()+" ");
            }
        }

        returnKeySet(keySet, ret);
        appendOrderby(keySet, ret);

        return ret;
    }



    public StringBuilder safeListAllObject(Map<String, Object> props, String label, String[] keySet){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n:"+label+")");
        if(props!=null&&!props.isEmpty()){
            StringBuilder sb=whereSearchString(props, keySet, label);
            if(sb.length()>1){
                ret.append(" where "+sb.toString()+" ");
            }
        }

        returnKeySet(keySet, ret);
        appendOrderby(keySet, ret);

        return ret;
    }



    public StringBuilder searchObject(Map<String, Object> props, String label, String[] keySet, String serchColumns,
                                      String queryText){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n:"+label+")");
        if(serchColumns!=null&&serchColumns.trim().length()>0){
            if(props!=null&&!props.isEmpty()||queryText!=null&&queryText.trim().length()>0){
                StringBuilder sb=whereMoreSearchString(props, keySet, label, serchColumns, queryText);
                if(sb.length()>1){
                    ret.append(" where "+sb.toString()+" ");
                }
            }
        }else{
            if(props!=null&&!props.isEmpty()){
                StringBuilder sb=whereSearchString(props, keySet, label);
                if(sb.length()>1){
                    ret.append(" where "+sb.toString()+" ");
                }
            }
        }

        returnKeySet(keySet, ret);
        appendOrderby(keySet, ret);
        return ret;
    }

    public StringBuilder listAuthObject(Map<String, Object> props, String label, String[] keySet){
        StringBuilder ret=new StringBuilder();

        ret.append("MATCH (a:User)-[*1..2]-(n:"+label+") where id(a)="+adminService.getCurrentUserId());
        if(props!=null&&!props.isEmpty()){
            StringBuilder sb=whereSearchString(props, keySet, label);
            if(sb.length()>1){
                ret.append(" AND "+sb.toString()+" ");
            }
        }

        returnKeySet(keySet, ret);
        appendOrderby(keySet, ret);
        return ret;
    }

    public StringBuilder moreList(JSONObject props, String label, String[] keySet){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n:"+label+")");
        if(props!=null&&!props.isEmpty()){
            StringBuilder sb=whereSearchString(props, keySet);
            if(sb.length()>1){
                ret.append(" where "+sb.toString()+" ");
            }
        }

        returnKeySet(keySet, ret);
        appendOrderby(keySet, ret);
        return ret;
    }

    public void returnKeySet(String[] keySet, StringBuilder ret){
        returnKeySet("n", keySet, ret);
    }

    private void returnKeySet(String obj, String[] keySet, StringBuilder ret){
        if(keySet!=null&&keySet.length>1){
            List<String> ks=new ArrayList<>();
            for(String keyi : keySet){
                ks.add(keyi);
            }
            if(!ks.contains("createTime")){
                ks.add("createTime");
            }
            ret.append(" WITH DISTINCT "+obj+"   return  "+returnColumn(obj, ks));
        }else{
            ret.append(" WITH DISTINCT "+obj+" return  "+obj+" ");
        }
    }

    public StringBuilder listAllObject2(JSONObject props, String label, String[] keySet){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n:"+label+")");
        if(props!=null&&!props.isEmpty()){
            StringBuilder sb=whereSearchString2(props, keySet);
            if(sb.length()>1){
                ret.append(" where "+sb.toString()+" ");
            }
        }

        if(keySet!=null&&keySet.length>1){
            ret.append(" return "+returnColumn2("n", keySet));
        }else{
            ret.append(" return n ");
        }
        ret.append(" n.updateTime DESC,n.createTime DESC ");
        return ret;
    }

    public String getAllObject(String label, String[] keySet){
        return getAllObject(null, label, keySet).toString();
    }

    public StringBuilder getAllObject(JSONObject props, String label, String[] keySet){
        StringBuilder ret=new StringBuilder();
        ret.append("match (n:"+label+")");
        if(props!=null){
            props=creatorInfo(props, label);
            if(!props.isEmpty()){
                StringBuilder sb=whereString(props);
                if(sb.length()>1){
                    ret.append(" where "+sb.toString()+" ");
                }
            }
        }

        if(keySet!=null&&keySet.length>0){
            ret.append(" return "+returnColumn("n", keySet));
        }else{
            ret.append(" return n ");
        }
        return ret;
    }

    /**
     * 获取label类型的子节点
     *
     * @param props
     * @param label
     * @param keySet
     * @return
     */
    public StringBuilder getLabelChild(PageObject page, JSONObject props, String label, String endLabel,
                                       String[] keySet){
        StringBuilder ret=new StringBuilder();
        ret.append("match (a:"+label+")-[r]-(b:"+endLabel+")");
        props=creatorInfo(props, label);
        if(!props.isEmpty()){
            StringBuilder sb=whereString(props, "a");
            if(sb.length()>1){
                ret.append(" where "+sb.toString()+" ");
            }
        }

        if(keySet!=null&&keySet.length>0){
            ret.append(" return "+returnColumn("b", keySet));
        }else{
            ret.append(" return b ");
        }
        if(page!=null){
            appendPage(page, ret);
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
    public String relationsPage(JSONObject start, String label, JSONObject endNode, PageObject page){
        String skipPage=pageSkip(page);
        StringBuilder ret=relations(start, label, endNode);
        ret.append(skipPage);
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
    public StringBuilder relations(JSONObject start, String label, JSONObject endNode){
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
    public String allRelation(String label, JSONObject start, PageObject page){
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
    public StringBuilder relationBs(JSONObject start, String label, JSONObject endNode){
        StringBuilder ret=relationMatch(start, label, endNode);
        ret.append(" return m");
        return ret;
    }

    public StringBuilder addRelation(JSONObject start, String label, JSONObject endNode){
        StringBuilder ret=relationCreate(start, label, endNode);
        ret.append(" return r");
        return ret;
    }

    public StringBuilder addRelation(JSONObject start, JSONObject relation, JSONObject endNode){
        StringBuilder ret=relationCreate(start, relation, endNode);
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
    public StringBuilder delRelations(Map<String, Object> startNode, String label, Map<String, Object> endNode){
        StringBuilder ret=relationMatch(startNode, label, endNode);
        ret.append(" delete r");
        return ret;
    }

    private StringBuilder relationMatch(Map<String, Object> start, String label, Map<String, Object> endNode){
        StringBuilder ret=new StringBuilder();
        ret.append("match ");
        relationConditionBody(start, label, endNode, ret);
        return ret;
    }

    private StringBuilder relationMatchAll(JSONObject start, String label){
        StringBuilder ret=new StringBuilder();
        ret.append("match ");
        allRelationOfStart(start, label, ret);
        return ret;
    }

    private void allRelationOfStart(JSONObject start, String label, StringBuilder ret){
        ret.append("(n");
        startLabelAndProp(start, label, ret);
        ret.append(")-[r]->(m)");
    }

    private void startLabelAndProp(JSONObject node, String label, StringBuilder ret){
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

    private StringBuilder relationMatch(JSONObject start, JSONObject label, JSONObject endNode){
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
    private StringBuilder relationCreate(JSONObject start, String label, JSONObject endNode){
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
    private void relationConditionBody(Map<String, Object> start, String label, Map<String, Object> endNode,
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
    private StringBuilder relationEndList(String relLabel, String startLabel, String startId, String endLabel,
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
        return ret;
    }

    public StringBuilder relationExist(String relLabel, Long startId, Long endId){
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
    private StringBuilder relationCreate(JSONObject start, JSONObject relation, JSONObject endNode){
        StringBuilder ret=new StringBuilder();
        ret.append("create ");
        relationConditionBody(start, relation, endNode, ret);
        return ret;
    }

    private void relationConditionBody(JSONObject start, JSONObject relation, JSONObject endNode, StringBuilder ret){
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
    private void validateLabelAndProp(Map<String, Object> node, StringBuilder ret){
        if((node!=null&&!node.isEmpty())){
            String aLabel=String.valueOf(node.get(LABEL));
            if(aLabel!=null&&!aLabel.equals("null")){
                // node.remove(LABEL);
                ret.append(":"+aLabel);
            }
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
    public String pageSkip(PageObject page){
        int limit=page.getPageSize();// 每页条数
        int skip=(page.getPageNum()-1)*limit;// 第几页
        String skipPage=" SKIP "+skip+" Limit "+limit+" ";
        return skipPage;
    }

    private String topOne(){
        int limit=1;// 每页条数
        int skip=0;// 第几页
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
    public String queryObj(JSONObject props, String label){
        return queryObj(props, label, null);
    }

    public String query(JSONObject props, String label){
        return query(props, label, null);
    }

    private String returnColumn(String nodeSymble, String[] keySet){
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
                sbRet.append(nodeSymble+"."+key+" as "+key);
            }
        }
        return sbRet.toString();
    }

    private String returnColumn(String nodeSymble, List<String> keySet){
        StringBuilder sbRet=new StringBuilder();
        Boolean idUsed=false;
        for(String key : keySet){
            if(sbRet.length()>1){
                sbRet.append(",");
            }
            if(NODE_ID.equalsIgnoreCase(key)&&!idUsed){
                sbRet.append("id("+nodeSymble+") as "+key);
                idUsed=true;
            }

            if(!NODE_ID.equalsIgnoreCase(key)){
                sbRet.append(nodeSymble+"."+key+" as "+key);
            }
        }
        return sbRet.toString();
    }

    private String returnColumn2(String nodeSymble, String[] keySet){
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
                sbRet.append(nodeSymble+"."+key+" as "+key);
            }
        }
        sbRet.append(","+nodeSymble+".createTime, "+nodeSymble+".updateTime ");

        return sbRet.toString();
    }

    private String returnColumn(Set<String> keySet){
        StringBuilder sbRet=new StringBuilder();

        for(String key : keySet){
            if(sbRet.length()>1){
                sbRet.append(",");
            }
            sbRet.append("n."+key+"");
        }
        return sbRet.toString();
    }

    private String returnAColumn(String label, String[] keySet){
        StringBuilder sbRet=new StringBuilder();
        for(String key : keySet){
            if(sbRet.length()>1){
                sbRet.append(",");
            }
            sbRet.append(label+"."+key+" ");
        }
        return sbRet.toString();
    }

    private String returnABColumn(String[] keySet){
        StringBuilder sbRet=new StringBuilder();
        for(String key : keySet){
            if(sbRet.length()>1){
                sbRet.append(",");
            }
            sbRet.append(key+" ");
        }
        return sbRet.toString();
    }

    public StringBuilder keyString(Map<String, Object> props, String[] keys){
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

    private StringBuilder setProp(Map<String, Object> props, String[] keys){
        StringBuilder sb=new StringBuilder();
        String join=String.join(",", keys);
        for(String key : props.keySet()){
            if(!join.contains(key)){
                Object value=props.get(key);
                if(value!=null){
                    if(sb.length()<1){
                        sb.append(" SET  ");
                    }else{
                        sb.append(" , ");
                    }
                    sb.append(" SET n."+key+"='"+String.valueOf(value)+"' ");
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
    private StringBuilder propString(Map<String, Object> props){
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

    private StringBuilder safePropString(Map<String, Object> props){
        StringBuilder sb=new StringBuilder();
        if(props.containsKey("id")){
            Object value=props.get("id");
            sb.append("id: {id} ");
            return sb;
        }
        for(Entry<String, Object> entryi : props.entrySet()){
            Object value=entryi.getValue();
            if(value!=null&&Strings.isNotEmpty(String.valueOf(value))){
                if(sb.length()>1){
                    sb.append(",");
                }
                sb.append(entryi.getKey()+":{"+entryi.getKey()+"} ");
            }
        }

        return sb;
    }

    /**
     * 模糊查询
     *
     * @param props
     * @return
     */
    private StringBuilder whereSearchString(Map<String, Object> props){
        StringBuilder sb=new StringBuilder();
        for(Entry<String, Object> entryi : props.entrySet()){
            Object value=entryi.getValue();
            String valueStr=String.valueOf(value);
            if(value!=null&&Strings.isNotBlank(valueStr)){
                if(sb.length()>1&&isNeedAnd(sb)){
                    sb.append(" and ");
                }
                String key=entryi.getKey();

                if(key.equals("startTime")){
                    Long startLong=DateTool.dateStrToLong(valueStr);
                    sb.append(" n."+key+">="+startLong);
                    continue;
                }
                if(key.equals("endTime")){
                    Long endLong=DateTool.dateStrToLong(valueStr);
                    sb.append(" n."+key+"<="+endLong);
                    continue;
                }
                if(needEqual(key)){
                    if(value instanceof Long lv){
                        sb.append("n."+key+"="+lv);
                        continue;
                    }
                    if(value instanceof String sv){
                        sb.append("n."+key+"='"+sv+"'");
                        continue;
                    }
                    sb.append("n."+key+"='"+valueStr+"'");

                }else{
                    sb.append("n."+key+" CONTAINS '"+valueStr+"'");
                }

            }
        }

        return sb;
    }

    private StringBuilder whereSearchString(Map<String, Object> props, String[] keySet){
        return whereSearchString("n", props, keySet);
    }

    private StringBuilder whereSearchString(Map<String, Object> props, String[] keySet, String label){
        return whereSearchString("n", props, keySet, label);
    }
    private StringBuilder whereString(Map<String, Object> props, String[] keySet, String label){
        return whereString("n", props, keySet, label);
    }

    private StringBuilder whereMoreSearchString(Map<String, Object> props, String[] keySet, String label, String searchColumns, String queryText){
        return whereMoreSearchString("n", props, keySet, label, searchColumns, queryText);
    }

    private StringBuilder whereSearchString(String obj, Map<String, Object> props, String[] columnSet){
        return whereSearchString(obj, props, columnSet, null);
    }

    private StringBuilder whereSearchString(String obj, Map<String, Object> props){
        return whereSearchString(obj, props, null, null);
    }


    public String queryWith(String query, String vari, String label, Map<String, Object> vo){
        if(vo.isEmpty()||label==null||vari==null){
            return query;
        }
        //查询条件
        String[] columns;
        try{
            columns=crudUtil.getMdColumns(label);
            StringBuilder q1=whereSearchString("b", vo, columns, label);
            String[] split=null;
            if(query.contains(" where ")){
                split=query.split(" where ");
            }

            if(query.contains(" WHERE ")){
                split=query.split(" WHERE ");
            }
            if(split.length==2&&q1.length()>1){
                return split[0]+" WHERE "+q1.toString()+" AND "+split[1];
            }
        }catch(DefineException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "";
    }

    public StringBuilder whereMoreSearchString(String obj, Map<String, Object> props, String[] columnSet, String label, String searchColumns, String queryText){
        StringBuilder sb=new StringBuilder();
        Set<String> ks=new HashSet<>();
        if(columnSet!=null){
            for(String keyi : columnSet){
                ks.add(keyi);
            }
            if(!ks.contains("startTime")&&!ks.contains("endTime")){

            }
            String id=string(props, ID);
            if(props.containsKey(ID)&&id!=null&&!"".equals(id)){
                return sb.append("id("+obj+")="+id);
            }
            if(queryText!=null&&!queryText.equals("")&&!queryText.trim().equals("")){
                StringBuilder searchStr=new StringBuilder();
                Boolean hasSerachCol=false;
                for(String ci : searchColumns.split(",")){
                    if(!props.keySet().contains(ci)){
                        hasSerachCol=true;
                        if(searchStr.length()>0){
                            searchStr.append(" OR ");
                        }
                        if(ci.endsWith("ID")){
                            searchStr.append(obj+"."+ci+"="+queryText+" ");
                        }else{
                            searchStr.append(obj+"."+ci+" CONTAINS '"+queryText+"'");
                        }

                    }
                }
                if(hasSerachCol){
                    if(sb.length()>0){
                        sb.append(" and (");
                    }else{
                        sb.append("(");
                    }
                    sb.append(searchStr.toString());
                    sb.append(")");
                }
            }
            for(Entry<String, Object> entryi : props.entrySet()){
                Object value=entryi.getValue();
                String key=entryi.getKey();
                String valueStr=String.valueOf(value);
                if(key==null||null==value||"".equals(valueStr.trim())){
                    continue;
                }
                boolean needAppendAnd=isNeedAnd(sb);
                if(sb.length()>1&&
                        needAppendAnd){
                    sb.append(" and ");
                }
                if(key.toLowerCase().endsWith("id")){
                    try{
                        //字段是ID的情况，兼容字段的值为Long和String两种类型
                        Long someId=Long.valueOf(valueStr);
                        sb.append("("+obj+"."+key+"="+someId+" OR "+obj+"."+key+"='"+someId+"')");
                        continue;
                    }catch(Exception e){
                    }
                }
                if(!ks.contains(key)){
                    if(key.equals("creator")){

                        if(isForbidden(label, valueStr)||isNotAuth(label, valueStr)){
                            sb.append(obj+"."+key+"='"+valueStr+"'");
                        }
                    }
                    if(key.equals("updator")){
                        if(isForbidden(label, valueStr)||isNotAuth(label, valueStr)){
                            sb.append(obj+"."+key+"='"+valueStr+"'");
                        }
                    }
                    continue;
                }

                if(value!=null&&Strings.isNotBlank(valueStr)&&!"选择".equals(valueStr)){
                    if(sb.length()>1&&isNeedAnd(sb)){
                        sb.append(" and ");
                    }
                    for(String keyi : columnSet){
                        if(!keyi.equals(key)&&keyi.equalsIgnoreCase(key)){
                            key=keyi;
                        }
                    }
                    if(key.equals("startTime")){
                        if(ks.contains("startTime")){
                            Long startLong=DateTool.dateStrToLong(valueStr);
                            if(ks.contains(key)){
                                sb.append(" "+obj+"."+key+">="+startLong);
                            }else{
                                sb.append(" "+obj+".createTime >="+startLong);
                            }

                            continue;
                        }else{
                            //查找时间类型的字段
                        }
                    }


                    if(key.equals("endTime")){
                        if(ks.contains("endTime")){
                            Long endLong=DateTool.dateStrToLong(valueStr);

                            if(ks.contains(key)){
                                sb.append(" "+obj+"."+key+"<="+endLong);
                            }else{
                                sb.append(" "+obj+".createTime <="+endLong);
                            }

                            continue;
                        }else{
                            //查找时间类型的字段
                        }
                    }

                    boolean b=value instanceof Long;
                    try{
                        Long valueOf=Long.valueOf(valueStr);
                        b=valueOf>0&&b;
                    }catch(Exception e){
                        b=false;
                    }

                    String lowerCase=valueStr.toLowerCase();
                    if(value instanceof Boolean||lowerCase.equals("true")||lowerCase.equals("false")){
                        // sb.append("n." + key + "='" + valueStr + "'");
                        if(lowerCase.equals("true")){
                            sb.append("("+obj+"."+key+"='"+valueStr+"' OR "+obj+"."+key+"="+value
                                    +")");
                        }else if(lowerCase.equals("false")){
                            // sb.append(" n." + key + "<> true");
                            sb.append("not ("+obj+"."+key+"= 'true' OR "+obj+"."+key+"=\"true\" OR "
                                    +obj+"."+key+"=true)");
                        }
                    }else if(needEqual(key)||b){
                        if(b){
                            sb.append(obj+"."+key+"="+Long.valueOf(valueStr));
                            continue;
                        }
                        if(value instanceof Long lv){
                            sb.append(obj+"."+key+"="+lv);
                            continue;
                        }
                        if(value instanceof String sv){
                            sb.append(obj+"."+key+"='"+sv+"'");
                            continue;
                        }
                        sb.append(obj+"."+key+"='"+valueStr+"'");

                    }else{
                        sb.append(obj+"."+key+" CONTAINS '"+valueStr+"'");
                    }

                }
            }
        }

        return sb;
    }

    public StringBuilder safeWhereSearchString(String obj, Map<String, Object> props, String[] columnSet, String label){
        StringBuilder sb=new StringBuilder();
        Set<String> ks=new HashSet<>();
        if(columnSet!=null){
            for(String keyi : columnSet){
                ks.add(keyi);
            }
            String id=string(props, ID);
            if(props.containsKey(ID)&&id!=null&&!"".equals(id)){
                return sb.append("id("+obj+")="+id);
            }
            String searchVal=searchVal(props);
            if(searchVal!=null){
                props.remove(SEARCH_VAL);
                StringBuilder searchStr=new StringBuilder();
                Boolean hasSerachCol=false;
                for(String ci : columnSet){
                    boolean b=ci.equals(NAME)||ci.equals(TITLE)||ci.equals(CONTENT)||ci.equals(CODE)
                            ||ci.equals("desc");
                    if(!props.keySet().contains(ci)&&b){
                        hasSerachCol=true;
                        if(searchStr.length()>0){
                            searchStr.append(" OR ");
                        }
                        searchStr.append(obj+"."+ci+" CONTAINS '"+searchVal+"'");
                    }
                }
                if(hasSerachCol){
                    if(sb.length()>0){
                        sb.append(" and (");
                    }else{
                        sb.append("(");
                    }
                    sb.append(searchStr.toString());
                    sb.append(")");
                }
            }
            for(Entry<String, Object> entryi : props.entrySet()){
                Object value=entryi.getValue();
                String key=entryi.getKey();
                String valueStr=String.valueOf(value);
                if(null==value||"".equals(valueStr.trim())){
                    continue;
                }
                boolean needAppendAnd=isNeedAnd(sb);
                if(sb.length()>1&&
                        needAppendAnd){
                    sb.append(" and ");
                }
                if(key.toLowerCase().endsWith("id")){
                    try{
                        //字段是ID的情况，兼容字段的值为Long和String两种类型
                        Long someId=Long.valueOf(valueStr);
                        sb.append("("+obj+"."+key+"="+someId+" OR "+obj+"."+key+"='"+someId+"')");
                        continue;
                    }catch(Exception e){
                    }
                }
                if(!ks.contains(key)){
                    if(key.equals("creator")){

                        if(isForbidden(label, valueStr)||isNotAuth(label, valueStr)){
                            sb.append(obj+"."+key+"='"+valueStr+"'");
                        }
                    }
                    if(key.equals("updator")){
                        if(isForbidden(label, valueStr)||isNotAuth(label, valueStr)){
                            sb.append(obj+"."+key+"='"+valueStr+"'");
                        }
                    }
                    continue;
                }

                if(value!=null&&Strings.isNotBlank(valueStr)&&!"选择".equals(valueStr)){
                    if(sb.length()>1&&isNeedAnd(sb)){
                        sb.append(" and ");
                    }
                    for(String keyi : columnSet){
                        if(!keyi.equals(key)&&keyi.equalsIgnoreCase(key)){
                            key=keyi;
                        }
                    }
                    if(key.equals("startTime")){
                        Long startLong=DateTool.dateStrToLong(valueStr);
                        if(ks.contains(key)){
                            sb.append(" "+obj+"."+key+">="+startLong);
                        }else{
                            sb.append(" "+obj+".createTime >="+startLong);
                        }

                        continue;
                    }

                    if(key.equals("endTime")){
                        Long endLong=DateTool.dateStrToLong(valueStr);

                        if(ks.contains(key)){
                            sb.append(" "+obj+"."+key+"<="+endLong);
                        }else{
                            sb.append(" "+obj+".createTime <="+endLong);
                        }

                        continue;
                    }

                    boolean b=value instanceof Long;
                    try{
                        Long valueOf=Long.valueOf(valueStr);
                        b=valueOf>0&&b;
                    }catch(Exception e){
                        b=false;
                    }

                    String lowerCase=valueStr.toLowerCase();
                    if(value instanceof Boolean||lowerCase.equals("true")||lowerCase.equals("false")){
                        // sb.append("n." + key + "='" + valueStr + "'");
                        if(lowerCase.equals("true")){
                            sb.append("("+obj+"."+key+"='"+valueStr+"' OR "+obj+"."+key+"="+value
                                    +")");
                        }else if(lowerCase.equals("false")){
                            // sb.append(" n." + key + "<> true");
                            sb.append("not ("+obj+"."+key+"= 'true' OR "+obj+"."+key+"=\"true\" OR "
                                    +obj+"."+key+"=true)");
                        }
                    }else if(needEqual(key)||b){
                        if(b){
                            sb.append(obj+"."+key+"="+Long.valueOf(valueStr));
                            continue;
                        }
                        if(value instanceof Long lv){
                            sb.append(obj+"."+key+"="+lv);
                            continue;
                        }
                        if(value instanceof String sv){
                            sb.append(obj+"."+key+"='"+sv+"'");
                            continue;
                        }
                        sb.append(obj+"."+key+"='"+valueStr+"'");

                    }else{
                        sb.append(obj+"."+key+" CONTAINS '"+valueStr+"'");
                    }

                }
            }
        }

        return sb;
    }

    public StringBuilder whereString(String obj, Map<String, Object> props, String[] columnSet, String label){
        StringBuilder sb=new StringBuilder();
        Set<String> ks=new HashSet<>();
        if(columnSet!=null){
            for(String keyi : columnSet){
                ks.add(keyi);
            }
            String id=string(props, ID);
            if(props.containsKey(ID)&&id!=null&&!"".equals(id)){
                return sb.append("id("+obj+")="+id);
            }

            for(Entry<String, Object> entryi : props.entrySet()){
                Object value=entryi.getValue();
                String key=entryi.getKey();
                boolean needAppendAnd=isNeedAnd(sb);
                if(sb.length()>1&&
                        needAppendAnd){
                    sb.append(" and ");
                }
                if(value instanceof Integer intValue){
                    sb.append(obj+"."+key+"="+intValue+" ");
                    continue;
                }
                String valueStr=String.valueOf(value);
                if(null==value||"".equals(valueStr.trim())){
                    continue;
                }


                if(key.toLowerCase().endsWith("id")){
                    try{
                        //字段是ID的情况，兼容字段的值为Long和String两种类型
                        Long someId=Long.valueOf(valueStr);
                        sb.append(" (" +obj+"."+key+"="+someId+" OR "+obj+"."+key+"='"+someId+"') ");
                        continue;
                    }catch(Exception e){
                        sb.append(obj+"."+key+"=\""+valueStr+"\"");
                        continue;
                    }
                }


                if(value!=null&&Strings.isNotBlank(valueStr)&&!"选择".equals(valueStr)){
                    if(sb.length()>1&&isNeedAnd(sb)){
                        sb.append(" and ");
                    }
                    for(String keyi : columnSet){
                        if(!keyi.equals(key)&&keyi.equalsIgnoreCase(key)){
                            key=keyi;
                        }
                    }

                    boolean b=value instanceof Long;
                    try{
                        Long valueOf=Long.valueOf(valueStr);
                        b=valueOf>0&&b;
                    }catch(Exception e){
                        b=false;
                    }

                    String lowerCase=valueStr.toLowerCase();
                    if(value instanceof Boolean||lowerCase.equals("true")||lowerCase.equals("false")){
                        // sb.append("n." + key + "='" + valueStr + "'");
                        if(lowerCase.equals("true")){
                            sb.append("("+obj+"."+key+"='"+valueStr+"' OR "+obj+"."+key+"="+value
                                    +")");
                        }else if(lowerCase.equals("false")){
                            // sb.append(" n." + key + "<> true");
                            sb.append("not ("+obj+"."+key+"= 'true' OR "+obj+"."+key+"=\"true\" OR "
                                    +obj+"."+key+"=true)");
                        }
                    }else {
                        if(b){
                            sb.append(obj+"."+key+"="+b+" ");
                        }else{
                            sb.append(obj+"."+key+"=\""+valueStr+"\" ");
                        }
                    }
                }
            }
        }

        return sb;
    }

    public StringBuilder whereSearchString(String obj, Map<String, Object> props, String[] columnSet, String label){
        StringBuilder sb=new StringBuilder();
        Set<String> ks=new HashSet<>();
        if(columnSet!=null){
            for(String keyi : columnSet){
                ks.add(keyi);
            }
            String id=string(props, ID);
            if(props.containsKey(ID)&&id!=null&&!"".equals(id)){
                return sb.append("id("+obj+")="+id);
            }
            String searchVal=searchVal(props);
            if(searchVal!=null){
                props.remove(SEARCH_VAL);
                StringBuilder searchStr=new StringBuilder();
                Boolean hasSearchCol=false;
                for(String ci : columnSet){
                    boolean b=ci.equals(NAME)||ci.equals(TITLE)||ci.equals(CONTENT)||ci.equals(CODE)
                            ||ci.equals("desc");
                    if(!props.keySet().contains(ci)&&b){
                        hasSearchCol=true;
                        if(searchStr.length()>0){
                            searchStr.append(" OR ");
                        }
                        if(searchVal.toString().equals("true")||searchVal.toString().equals("false")){
                            searchStr.append(obj+"."+ci+" = "+searchVal);
                        }
                        searchStr.append(obj+"."+ci+" CONTAINS '"+searchVal+"'");
                    }
                }
                if(hasSearchCol){
                    if(sb.length()>0){
                        sb.append(" and (");
                    }else{
                        sb.append("(");
                    }
                    sb.append(searchStr.toString());
                    sb.append(")");
                }
            }
            for(Entry<String, Object> entryi : props.entrySet()){
                Object value=entryi.getValue();
                String key=entryi.getKey();
                boolean needAppendAnd=isNeedAnd(sb);
                if(sb.length()>1&&
                        needAppendAnd){
                    sb.append(" and ");
                }
                if(value instanceof Integer intValue){
//                    sb.append(obj+"."+key+"="+intValue+" ");
                    sb.append("("+obj+"."+key+"="+intValue+" OR "+obj+"."+key+"='"+intValue+"')");
                    continue;
                }
                String valueStr=String.valueOf(value);
                if(null==value||"".equals(valueStr.trim())){
                    continue;
                }


                if(key.toLowerCase().endsWith("id")){
                    try{
                        //字段是ID的情况，兼容字段的值为Long和String两种类型
                        Long someId=Long.valueOf(valueStr);
                        sb.append(" (" +obj+"."+key+"="+someId+" OR "+obj+"."+key+"='"+someId+"') ");
                        continue;
                    }catch(Exception e){
                        sb.append(obj+"."+key+"=\""+valueStr+"\"");
                        continue;
                    }
                }
                if(!ks.contains(key)){
                    if(key.equals("creator")){

                        if(isForbidden(label, valueStr)||isNotAuth(label, valueStr)){
                            sb.append(obj+"."+key+"='"+valueStr+"' ");
                        }
                    }
                    if(key.equals("updator")){
                        if(isForbidden(label, valueStr)||isNotAuth(label, valueStr)){
                            sb.append(obj+"."+key+"='"+valueStr+"' ");
                        }
                    }
                    continue;
                }

                if(value!=null&&Strings.isNotBlank(valueStr)&&!"选择".equals(valueStr)){
                    if(sb.length()>1&&isNeedAnd(sb)){
                        sb.append(" and ");
                    }
                    for(String keyi : columnSet){
                        if(!keyi.equals(key)&&keyi.equalsIgnoreCase(key)){
                            key=keyi;
                        }
                    }
                    if(key.equals("startTime")){
                        Long startLong=DateTool.dateStrToLong(valueStr);
                        if(ks.contains(key)){
                            sb.append(" "+obj+"."+key+">"+startLong+" ");
                        }else{
                            sb.append(" "+obj+".createTime >="+startLong+" ");
                        }

                        continue;
                    }

                    if(key.equals("endTime")){
                        Long endLong=DateTool.dateStrToLong(valueStr);

                        if(ks.contains(key)){
                            sb.append(" "+obj+"."+key+"<="+endLong+" ");
                        }else{
                            sb.append(" "+obj+".createTime <="+endLong+" ");
                        }

                        continue;
                    }

                    boolean b=value instanceof Long;
                    try{
                        Long valueOf=Long.valueOf(valueStr);
                        b=valueOf>0&&b;
                    }catch(Exception e){
                        b=false;
                    }

                    String lowerCase=valueStr.toLowerCase();
                    if(value instanceof Boolean||lowerCase.equals("true")||lowerCase.equals("false")){
                        // sb.append("n." + key + "='" + valueStr + "'");
                        if(lowerCase.equals("true")){
                            sb.append("("+obj+"."+key+"='"+valueStr+"' OR "+obj+"."+key+"="+value
                                    +")");
                        }else if(lowerCase.equals("false")){
                            // sb.append(" n." + key + "<> true");
                            sb.append("not ("+obj+"."+key+"= 'true' OR "+obj+"."+key+"=\"true\" OR "
                                    +obj+"."+key+"=true)");
                        }
                    }else if(needEqual(key)){
                        if(b){
                            sb.append("("+obj+"."+key+"="+valueStr+" OR "+obj+"."+key+"='"+valueStr+"')");
                        }else{
                            sb.append(obj+"."+key+"=\""+valueStr+"\" ");
                        }

                    }else{
                        sb.append(obj+"."+key+" CONTAINS '"+valueStr+"' ");
                    }

                }
            }
        }

        return sb;
    }


    private boolean isNotAuth(String label, String valueStr){
        String roleAuth="MATCH(n:User{username:\""+valueStr
                +"\"}) return exists((n)--(:Role)-[:dataAuth]->(:MetaData{label:\""+label+"\"})) AS isAuth";
        String userAuth="MATCH(n:User{username:\""+valueStr
                +"\"}) return exists((n)-[:dataAuth]->(:MetaData{label:\""+label+"\"})) AS isAuth";
        String roleAuthOk=neo4jService.getOne(roleAuth, "isAuth");
        String userAuthOk=neo4jService.getOne(userAuth, "isAuth");
        boolean roleNotAuth=roleAuthOk==null||!Boolean.valueOf(roleAuthOk);
        boolean userNotAuth=userAuthOk==null||!Boolean.valueOf(userAuthOk);

        boolean notAuth=roleNotAuth&&userNotAuth;
        return notAuth;
    }

    private boolean isForbidden(String label, String valueStr){
        String roleAuth="MATCH(n:User{username:\""+valueStr
                +"\"}) return exists((n)-[:HAS_PERMISSION]-(:Role)<-[:dataForbidden]-(:MetaData{label:\""+label
                +"\"})) AS isAuth";
        String userAuth="MATCH(n:User{username:\""+valueStr
                +"\"}) return exists((n)<-[:dataForbidden]-(:MetaData{label:\""+label+"\"})) AS isAuth";
        String roleAuthOk=neo4jService.getOne(roleAuth, "isAuth");
        String userAuthOk=neo4jService.getOne(userAuth, "isAuth");
        boolean roleForbidden=roleAuthOk!=null&&Boolean.valueOf(roleAuthOk);
        boolean userForbidden=userAuthOk!=null&&Boolean.valueOf(userAuthOk);

        boolean notAuth=roleForbidden||userForbidden;
        return notAuth;
    }

    private StringBuilder whereSearchString2(Map<String, Object> props, String[] keySet){
        StringBuilder sb=new StringBuilder();
        Set<String> ks=new HashSet<>();
        for(String keyi : keySet){
            ks.add(keyi);
        }
        for(Entry<String, Object> entryi : props.entrySet()){
            Object value=entryi.getValue();
            String valueStr=String.valueOf(value);
            if(value!=null&&Strings.isNotBlank(valueStr)&&!"选择".equals(valueStr)){
                if(sb.length()>1&&isNeedAnd(sb)){
                    sb.append(" and ");
                }
                String key=entryi.getKey();
                for(String keyi : keySet){
                    if(!keyi.equals(key)&&keyi.equalsIgnoreCase(key)){
                        key=keyi;
                    }
                }
                if(ks.contains(key)){
                    if(key.equals("startTime")){
                        Long startLong=DateTool.dateStrToLong(valueStr);
                        sb.append(" n."+key+">="+startLong);
                        continue;
                    }
                    if(key.equals("endTime")){
                        Long endLong=DateTool.dateStrToLong(valueStr);
                        sb.append(" n."+key+"<="+endLong);
                        continue;
                    }
                }else{
                    if(key.equals("startTime")){
                        Long startLong=DateTool.dateStrToLong(valueStr);
                        sb.append(" n.createTime >="+startLong);
                        continue;
                    }
                    if(key.equals("endTime")){
                        Long endLong=DateTool.dateStrToLong(valueStr);
                        sb.append(" n.createTime <="+endLong);
                        continue;
                    }
                }

                boolean b=value instanceof Long;
                try{
                    Long valueOf=Long.valueOf(valueStr);
                    b=valueOf>0;
                }catch(Exception e){
                    b=false;
                }

                if(needEqual(key)||b){
                    if(value instanceof Long lv){
                        sb.append("n."+key+"="+lv);
                        continue;
                    }
                    if(value instanceof String sv){
                        sb.append("n."+key+"='"+sv+"'");
                        continue;
                    }
                    sb.append("n."+key+"='"+valueStr+"'");

                }else{
                    sb.append("n."+key+" CONTAINS '"+valueStr+"'");
                }

            }
        }

        return sb;
    }

    private StringBuilder voWhereSearchString(Map<String, Object> props){
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
                if(needEqual(key)){
                    sb.append("a."+key+"='"+String.valueOf(value)+"'");
                }else{
                    sb.append("a."+key+" CONTAINS '"+String.valueOf(value)+"'");
                }

            }
        }

        return sb;
    }

    private static boolean needEqual(String key){

        if(key.equals("startTime")){
            return true;
        }
        if(key.equals("endTime")){
            return true;
        }
        boolean equalKey=key.endsWith("Id")||key.equals("creator")||key.equals("updator");
        return equalKey;
    }

    /**
     * 精确查询
     *
     * @param props
     * @return
     */
    private StringBuilder whereString(Map<String, Object> props){
        StringBuilder sb=new StringBuilder();
        for(Entry<String, Object> entryi : props.entrySet()){
            Object value=entryi.getValue();
            if(value!=null&&Strings.isNotBlank(String.valueOf(value))){
                if(sb.length()>1&&isNeedAnd(sb)){
                    sb.append(" and ");
                }
                sb.append("n."+entryi.getKey()+"='"+String.valueOf(value)+"'");
            }
        }

        return sb;
    }

    private StringBuilder whereString(Map<String, Object> props, String belong){
        StringBuilder sb=new StringBuilder();
        for(Entry<String, Object> entryi : props.entrySet()){
            Object value=entryi.getValue();
            if(value!=null&&Strings.isNotBlank(String.valueOf(value))){
                if(sb.length()>1&&isNeedAnd(sb)){
                    sb.append(" and ");
                }
                String key2=entryi.getKey();

                if(ID.equals(key2)){
                    sb.append("id("+belong+") ="+String.valueOf(value));
                }else{
                    sb.append(belong+"\\."+key2+"='"+String.valueOf(value)+"'");
                }

            }
        }

        return sb;
    }

    public UserAdminService getAdminService(){
        return adminService;
    }

    public void setAdminService(UserAdminService adminService){
        this.adminService=adminService;
    }

}
