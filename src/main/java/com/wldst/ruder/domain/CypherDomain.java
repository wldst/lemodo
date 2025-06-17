package com.wldst.ruder.domain;

public class CypherDomain extends SystemDomain {
    public static boolean isNeedAnd(StringBuilder sb) {
	String cypher = sb.toString();
	return isNeedAnd(cypher);
    }

    public static boolean isNeedAnd(String cypher) {
	boolean needAppendAnd = !cypher.trim().endsWith(" and")&& 
				!cypher.trim().endsWith(" where")&&
				!cypher.trim().endsWith(" WHERE")&&
				!cypher.trim().endsWith(" AND");
	return needAppendAnd;
    }

	public static void appendOrderby(String[] keySet, StringBuilder ret){
		StringBuilder orderBy = new StringBuilder();
		for(String ki: keySet){
			if(ki.equals("updateTime")){
				if(orderBy.length()>1){
					orderBy.append(",");
				}
				orderBy.append("  n.createTime DESC ");
			}else  if(ki.equals("createTime")){
				if(orderBy.length()>1){
					orderBy.append(",");
				}
				orderBy.append(" n.updateTime DESC");
			} else  if(ki.equals("time")){
				if(orderBy.length()>1){
					orderBy.append(",");
				}
				orderBy.append(" n.time DESC");
			}
		}

		if(orderBy.length()>1){
			ret.append(" ORDER BY "+orderBy.toString());
		}
	}
}
