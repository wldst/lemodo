package com.wldst.ruder.constant;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.util.PageObject;

/**
 * 数据操作功能
 * @author deeplearn96
 *
 */
public class Neo4jOpt extends CruderConstant {
    private static final String MATCH_N = "match (n ";
    private static final String MATCH_N2 = MATCH_N + ": ";
	/**
	 * 生成删除语句
	 * @param props
	 * @param label
	 * @return
	 */
	public static String delObj(Map<String,Object> props,String label) {
		if(props==null||props.isEmpty()) {
	    return MATCH_N2 + label + ") delete n";
		}else {
			StringBuilder sb = propString(props);
	    return MATCH_N2 + label + "{" + sb.toString() + "}) delete n";
		}
	}
	/**
	  * 关系查询
	  *  
	  * @author liuqiang
	  * @date 2019年9月18日 下午6:53:52
	  * @version V1.0
	  * @param aLabel
	  * @param rlabel
	  * @param bLabel
	  * @param ret
	  * @param keys
	  * @return
	 */
	public static String relationBs(String aLabel,String rlabel,String bLabel,String keys[]) {
		StringBuilder sb = new StringBuilder();
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
		sb.append(returnAColumn("b",keys));
		return sb.toString();
	}
	
	/**
	  * 关系查询
	  *  
	  * @author liuqiang
	  * @date 2019年9月18日 下午6:53:52
	  * @version V1.0
	  * @param aLabel
	  * @param rlabel
	  * @param bLabel
	  * @param ret
	  * @param keys
	  * @return
	 */
	public static String relationAB(String rlabel) {
		StringBuilder sb = new StringBuilder();
		sb.append("match(a");
		sb.append(")-[r");
		if(rlabel==null){
			sb.append(":"+rlabel);
		}
		sb.append("]->(b");
		sb.append(") return a,b");
		return sb.toString();
	}
	
	public static String createRelation(String rlabel,Long startId,List<Long> endIds) {
		StringBuilder sb = new StringBuilder();
		sb.append("MATCH (a),(b)");
		sb.append("WHERE id(a) = '"+startId+"' AND id(b) IN ["+StringUtils.collectionToCommaDelimitedString(endIds)+"] ");
		 sb.append("CREATE (a)-[r:"+rlabel+" ]->(b)");
		
		return sb.toString();
	}
	
	
	
	public static String relationBs(String rlabel,String keys[]) {
		return relationBs(null,rlabel,null,keys);
	}
	
	
	
	/**
	 * 生成查询语句
	 * @param props
	 * @param label
	 * @return
	 */
	public static String queryObj(JSONObject props,String label,String[] keySet) {
		if(props==null||props.isEmpty()) {
			if(keySet!=null&&keySet.length>1) {
		return MATCH_N2 + label + ") return " + returnColumn(keySet);
			}	
	    return MATCH_N2 + label + ") return n";
		}else {
			StringBuilder sb = propString(props);
			if(keySet!=null&&keySet.length>1) {
		return MATCH_N2 + label + "{" + sb.toString() + "}) return " + returnColumn(keySet);
			}			
	    return MATCH_N2 + label + "{" + sb.toString() + "}) return n";
		}
	}
	
	/**
	 * updateCypher
	 * @param props
	 * @param label
	 * @param keys
	 * @return
	 */
	public static String update(Map<String,Object> props,String label,String[] keys) {
		StringBuilder sb = keyString(props,keys);
		StringBuilder setProp = setProp(props,keys);
	return MATCH_N2 + label + "{" + sb.toString() + "}) " + setProp.toString() + " return "
		+ returnColumn(props.keySet());
	}
	/**
	 * 查询单个节点
	  *  
	  * @author liuqiang
	  * @date 2019年9月19日 下午1:37:05
	  * @version V1.0
	  * @param props
	  * @param label
	  * @param keys
	  * @return
	 */
	public static String findNodeMapBy(Map<String,Object> props,String label,String[] keys) {
		StringBuilder sb = keyString(props,keys);
	return MATCH_N2 + label + "{" + sb.toString() + "})  return " + returnColumn(props.keySet());
	}
	
	public static String findNodeBy(Map<String,Object> props,String label,String[] keys) {
		StringBuilder sb = keyString(props,keys);
	return MATCH_N2 + label + "{" + sb.toString() + "})  return n";
	}
	
	/**
	 * 分页查询
	 * @param props
	 * @param label
	 * @param keySet
	 * @param page
	 * @return
	 */
    public static String queryObj(JSONObject props, String label, String[] keySet, PageObject page) {
	String skipPage = pageSkip(page);
	if ((null == props || props.isEmpty()) && (keySet == null || keySet.length < 1)) {
	    return MATCH_N2 + label + ") return n";
	} else {
	    StringBuilder ret = new StringBuilder();
	    ret.append("match (n:" + label);
	    if (props != null && !props.isEmpty()) {
		StringBuilder sb = propString(props);
		ret.append("{" + sb.toString() + "}");
	    }

	    if (keySet != null && keySet.length > 1) {
		ret.append(") return " + returnColumn(keySet));
	    } else {
		ret.append(") return n ");
	    }
	    ret.append(skipPage);
	    return ret.toString();
	}
    }
	
	/**
	  * 关系数据
	  *  
	  * @author liuqiang
	  * @date 2019年9月20日 下午4:29:55
	  * @version V1.0
	  * @param props
	  * @param label
	  * @param keySet
	  * @param page
	  * @return
	 */
	public static String relationsPage(JSONObject start,String label,JSONObject endNode,PageObject page) {
		String skipPage = pageSkip(page);	
		StringBuilder ret = new StringBuilder();			

		ret.append(MATCH_N);
		
		validateLabelAndProp(start, ret);
		ret.append(")-["+label+"]->(m");
		validateLabelAndProp(endNode, ret);
		ret.append(") return n,m");
		ret.append( skipPage );
		return ret.toString();
	}
	private static void validateLabelAndProp(JSONObject node, StringBuilder ret) {
		if((node!=null&&!node.isEmpty())) {
			String aLabel = node.getString(LABEL);
			if(aLabel!=null&&!aLabel.equals("null")){
				node.remove(LABEL);
				ret.append(":"+aLabel);
			}
			if(!node.isEmpty()){
				StringBuilder sb = propString(node);
				ret.append("{"+sb.toString()+"}");
			}
		}
	}
	
	/**
	 * 返回分页信息
	 * @param page
	 * @return
	 */
	private static String pageSkip(PageObject page) {
		int limit=page.getPageSize();//每页条数
		int skip=(page.getPageNum()-1)*limit;//第几页
		String skipPage = " SKIP "+skip+" Limit "+limit+" ";
		return skipPage;
	}
	
	
	
	/**
	 * 查询
	 * @param props
	 * @param label
	 * @return
	 */
	public static String queryObj(JSONObject props,String label) {
		return queryObj(props,label,null);
	}
	
	private static String returnColumn(String[] keySet) {
		StringBuilder sbRet = new StringBuilder();
		
		for(String key:keySet) {
			if(sbRet.length()>1) {
				sbRet.append(",");
			}
			sbRet.append("n."+key+" ");
		}
		return sbRet.toString();
	}
	
	private static String returnColumn(Set<String> keySet) {
		StringBuilder sbRet = new StringBuilder();
		
		for(String key:keySet) {
			if(sbRet.length()>1) {
				sbRet.append(",");
			}
			sbRet.append("n."+key+"");
		}
		return sbRet.toString();
	}
	
	private static String returnAColumn(String label,String[] keySet) {
		StringBuilder sbRet = new StringBuilder();
		for(String key:keySet) {
			if(sbRet.length()>1) {
				sbRet.append(",");
			}
			sbRet.append(label+"."+key+" ");
		}
		return sbRet.toString();
	}
	
	private static String returnABColumn(String[] keySet) {
		StringBuilder sbRet = new StringBuilder();
		for(String key:keySet) {
			if(sbRet.length()>1) {
				sbRet.append(",");
			}
			sbRet.append(key+" ");
		}
		return sbRet.toString();
	}
	
	public static StringBuilder keyString(Map<String, Object> props,String[] keys) {
		StringBuilder sb = new StringBuilder();
		for(String key:keys) {
			Object value = props.get(key);
			if(value!=null) {
				if(sb.length()>1) {
					sb.append(",");
				}
				sb.append(key+":\""+String.valueOf(value)+"\"");
			}
		}
		return sb;
	}
	private static StringBuilder setProp(Map<String, Object> props,String[] keys) {
		StringBuilder sb = new StringBuilder();
		String join = String.join(",", keys);
		for(String key:props.keySet()) {
			if(!join.contains(key)) {
				Object value = props.get(key);
				if(value!=null) {
				    if (sb.length() < 1) {
					sb.append(" SET  ");
				    }else {
					sb.append(" , ");
				    }
					sb.append(" SET n."+key+"='"+String.valueOf(value)+"' ");
				}
			}
		}
		return sb;
	}
	
	private static StringBuilder propString(Map<String,Object> props) {
		StringBuilder sb = new StringBuilder();
		for(Entry<String,Object> entryi:props.entrySet()) {
			Object value = entryi.getValue();
			if(value!=null) {
				if(sb.length()>1) {
					sb.append(",");
				}
				sb.append(entryi.getKey()+":\""+String.valueOf(value)+"\"");
			}
		}
		
		return sb;
	}
}
