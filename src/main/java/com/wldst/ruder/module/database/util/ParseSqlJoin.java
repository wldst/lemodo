package com.wldst.ruder.module.database.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

/**
 * Document解析XML
 * 
 * @author ouyangjun
 */
public class ParseSqlJoin {

    // 递归方法
    public static Set<String> parseSql(String textContent) {
	Set<String> reSet = new HashSet<>();
	    String trim = textContent.replaceAll("\t", "").replaceAll("\n", "").trim();
	    if (trim.length() < 7) {
		return reSet;
	    }
	    if (!trim.contains(".")) {
		return reSet;
	    }
	    String upperCase = textContent.toUpperCase();

	    upperCase = upperCase.replaceAll("\\. ", "\\.");
	    upperCase = upperCase.replaceAll(" \\.", "\\.");
	    upperCase = clearJoin(upperCase, "\\n");
	    upperCase = clearJoin(upperCase, "\\t");
	    upperCase = joinTableColumn(upperCase, " ");
	    if (upperCase.indexOf(" ON ") < 0 && upperCase.indexOf(" FROM ") < 0 && upperCase.indexOf(" WHERE ") < 0) {
		return reSet;
	    }

	    if (upperCase.indexOf("=") < 0) {
		return reSet;
	    }
	    String[] splitEq = upperCase.split("=");
	    if (splitEq.length == 2 && !hasRelation(splitEq[1])) {
		return reSet;
	    }
	    System.out.println("parsing SQL:"+upperCase);

	    Map<String, String> asMap = new HashMap<>();
	    List<Map<String, String>> relationList = new ArrayList<>();
	    parseAllJoin(upperCase, asMap, relationList);
	    
	    parseRelFromSql(reSet, upperCase, asMap);
	    getRelSet(reSet, asMap, relationList);
	    return reSet;
    }

    private static String[] parseRelFromSql(Set<String> reSet, String upperCase, Map<String, String> asMap) {
	String[] sqlArray = upperCase.split("SELECT");
	// 多个Select的语句
	if (sqlArray.length > 2) {
	    List<String> tempList = new ArrayList<>();
	    for (String sqli : sqlArray) {
		if (sqli.length() < 4) {
		    continue;
		}
		// VISU_DEF_ITEM WHERE ITEMID IN (
		if (sqli.indexOf(" WHERE ") > 0 && sqli.trim().endsWith(" IN (")) {
		    String[] whereSplit = sqli.split(" WHERE ");
		    String[] tables = whereSplit[0].split(" FROM ");
		    String tableInSql = tables[1].trim();
		    String columnInSql = whereSplit[1].trim();
		    boolean oneTable = tableInSql.split(" ").length == 1;
		    boolean oneColum = columnInSql.split(" ").length == 1;

		    if (!oneTable) {
			String[] split = tableInSql.split(",");
			for (String ti : split) {
			    String[] tl = ti.split(" ");
			    if (tl.length > 1) {
				asMap.put(tl[1], tl[0]);
			    }
			}
		    }

		    if (sqli.indexOf(".") < 0) {
			if (oneTable && oneColum) {
			    tempList.add(tableInSql + "." + columnInSql);
			}
		    } else {
			if (oneColum) {
			    tempList.add(columnInSql);
			}
		    }
		}

		if (!tempList.isEmpty()) {
		    boolean endsSubQuery = false;
		    if (!asMap.isEmpty()) {
			Set<String> keySet = asMap.keySet();
			for (String ti : keySet) {
			    endsSubQuery = upperCase.endsWith(") " + ti);
			    if (endsSubQuery) {
				break;
			    }
			}
		    }
		}

		if (sqli.contains(" FROM ")) {
		    if (sqli.indexOf(".") > 0) {
			handleMultiSelectSql(reSet, sqli, asMap);
		    }
		}
	    }
	} else {
	    handleSelectSql(reSet, upperCase);
	}
	return sqlArray;
    }

    private static void parseAllJoin(String upperCase, Map<String, String> asMap,
	    List<Map<String, String>> relationList) {
	parseJoin(relationList, upperCase, asMap, " LEFT JOIN ");
	parseJoin(relationList, upperCase, asMap, " INNER JOIN ");
	parseJoin(relationList, upperCase, asMap, " RIGHT JOIN ");
	parseInnerJoin(relationList, upperCase, asMap);
    }

    private static void parseInnerJoin(List<Map<String, String>> relationList, String upperCase,
	    Map<String, String> asMap) {
	String regex = " FROM ";
	if (upperCase.contains(regex)) {
	    String[] splitFrom = upperCase.split(regex);
	    Stack<String> tempList = new Stack<>();
	    Set<String> allKnowSet = new HashSet<>();

	    for (String lfi : splitFrom) {
		if (!lfi.contains(".")) {
		    continue;
		}
		String tablei = "";
		// getTableAs
		if (lfi.indexOf(" WHERE ") > 0) {
		    tablei = lfi.substring(0, lfi.indexOf(" WHERE "));
		    parseTableAs(asMap, tablei);
		}
		// getTableTelation
		if (lfi.indexOf(" AND ") > 0 && !lfi.contains(" WHERE ")) {
		    findRelationFromCondition(relationList, lfi);
		}

		// endSubSelect
		endSubSelect(relationList, asMap, tempList, allKnowSet, lfi, tablei);
		// 获取t.x 集合。
		getLabelFromColumns(allKnowSet, lfi);

		// getTableAs From from before SubSelect
		// startSubSelect
		if (lfi.contains("SELECT ") && lfi.trim().indexOf("SELECT") > 1) {
		    String[] split = lfi.split("SELECT ");
		    String beforeSelect = split[0];
		    if (beforeSelect.length() > 3 && !beforeSelect.equals("(")) {
			String[] ta = beforeSelect.split(",");
			for (String tai : ta) {
			    tableAs(asMap, tai);
			}
		    }
		    if (beforeSelect.contains(" WHERE ")) {
			tablei = lfi.substring(0, lfi.indexOf(" WHERE "));
			parseTableAs(asMap, tablei);
		    }
		    tempList.push(split[1]);
		}
		// getTableRelation
		if (lfi.contains("=")) {
		    getRelFromWhere(relationList, lfi);
		}

	    }
	}
    }

    private static void getLabelFromColumns(Set<String> allKnowSet, String lfi) {
	if (lfi.contains("SELECT ")) {
	    Set<String> labSet = new HashSet<>();
	    String[] selectSplit = lfi.trim().split("SELECT ");
	    for (String si : selectSplit) {
		if (si.length() > 6) {
		    if (si.contains(" WHERE ")) {
			continue;
		    }
		    String[] split = si.split(",");
		    for (String ci : split) {
			if (ci.contains(".")) {
			    labSet.add(ci.split("\\.")[0].trim());
			}
		    }
		}
	    }
	    allKnowSet.addAll(labSet);
	}
    }

    private static void endSubSelect(List<Map<String, String>> relationList, Map<String, String> asMap,
	    Stack<String> tempList, Set<String> allKnowSet, String lfi, String tablei) {
	if (!tempList.isEmpty()) {
	    if (!asMap.isEmpty()) {
		boolean endsSubQuery = false;
		Set<String> keySet = asMap.keySet();
		allKnowSet.addAll(keySet);
		String splitorString = null;
		for (String ti : allKnowSet) {
		    splitorString = ti;
		    endsSubQuery = lfi.contains(") " + ti + " ");
		    if (endsSubQuery) {
			break;
		    }
		}
		if (endsSubQuery) {
		    String pop = tempList.pop();
		    String[] split = lfi.split("\\) " + splitorString + " ");
		    // 获取所有关系信息
		    for (String sqli : split) {
			// 获取 外部关系
			getRelFromWhere(relationList, sqli);
		    }
		    // 匹配关系字段和内部字段找到内部表。建立新的关系。
		    Map<String, String> outRMap = null;
		    String relColumni = null;
		    for (Map<String, String> ri : relationList) {
			String value = ri.get("start");
			if (value.startsWith(splitorString)) {
			    outRMap = ri;
			    relColumni = value.split("\\.")[1];
			    break;
			}
		    }
		    // 遍历内部sql的字段
		    for (String ki : pop.split(",")) {
			if (relColumni == null || ki.trim().isEmpty()) {
			    continue;
			}
			if (ki.endsWith(relColumni) || ki.equals(relColumni)) {
			    if (ki.contains(".")) {
				String[] split2 = ki.split("\\.");
				// String string = asMap.get(split2[0]);
				outRMap.put("start", split2[0] + "." + split2[1]);
			    } else {
				String[] split2 = tablei.split("\\)");
				for (Entry<String, String> ei : asMap.entrySet()) {
				    if (ei.getValue().equals(split2[0].trim())) {
					outRMap.put("start", ei.getKey() + "." + relColumni);
				    }
				}

			    }

			}
		    }

		}
	    }
	}
    }

    private static void getRelFromWhere(List<Map<String, String>> relationList, String lfi) {
	if (lfi.contains("WHERE ")) {
	    String[] splitWhere = lfi.split("WHERE ");
	    String whereCondition = splitWhere[1];
	    findRelationFromCondition(relationList, whereCondition);
	} else {
	    findRelationFromCondition(relationList, lfi);
	}
    }

    private static void findRelationFromCondition(List<Map<String, String>> relationList, String lfi) {
	String[] splitAnd = lfi.split(" AND ");
	for (String andi : splitAnd) {
	    String[] split = andi.split("=");
	    if (split.length < 2) {
		continue;
	    }
	    String value = split[1].trim().split(" ")[0];
	    if (hasRelation(andi)) {
		addRelation(relationList, split[0].trim(),value);
	    } else if (andi.indexOf("=") > 0) {
		if (andi.split(",").length > 1 && valueWrong(value)) {
		    addRelation(relationList, split[0].trim(),value);
		}

	    }
	}
    }

    private static void parseTableAs(Map<String, String> asMap, String tables) {
	if (tables.indexOf(",") > 0) {
	    String[] split = tables.split(",");
	    for (String ti : split) {
		tableAs(asMap, ti);
	    }
	} else {
	    tableAs(asMap, tables);
	}
    }

    private static void parseJoin(List<Map<String, String>> relationList, String upperCase, Map<String, String> asMap,
	    String regex) {
	if (upperCase.contains(regex)) {
	    String[] splitLeftJoin = upperCase.split(regex);
	    for (String lfi : splitLeftJoin) {
		if (lfi.indexOf(" ON ") > 0 && lfi.indexOf(" WHERE ") > 0) {
		    lfi = lfi.substring(0, lfi.indexOf(" WHERE "));
		}
		if (lfi.indexOf(" ON ") > 0 && lfi.indexOf(" AND ") > 0) {
		    lfi = lfi.substring(0, lfi.indexOf(" AND "));
		    String[] splitAnd = lfi.split(" AND ");
		    getEqualFromOther(relationList, splitAnd, asMap);
		}
		if (lfi.indexOf(" ON ") > 0) {
		    String[] splitOn = lfi.split(" ON ");
		    String express = splitOn[1];
		    if (express.contains("=")) {
			String[] split2 = express.split("=");
			addRelation(relationList, split2[0].trim(), split2[1].trim());
		    } else {
			tableAs(asMap, splitOn[0]);
		    }
		} else {
		    String tables = lfi.substring(lfi.lastIndexOf(" FROM ") + 6);
		    parseTableAs(asMap, tables);
		}

	    }
	}
    }

    private static void tableAs(Map<String, String> asMap, String ti) {
	String trim = ti.trim();
	if (trim.length() > 2 && trim.indexOf(" ") > 0) {
	    if (ti.contains(" WHERE ")) {
		String[] split = ti.split(" WHERE ");
		ti = split[0];
	    }

	    String[] split2 = trim.split(" ");
	    if (split2.length == 1 || 
		    split2.length == 3 || 
			    ti.indexOf("=") > 0||
		    ti.indexOf(")") > 0) {
		return;
	    }
	    asMap.put(split2[1], split2[0]);
	}
    }

    private static void handleSelectSql(Set<String> reSet, String upperCase) {
	List<Map<String, String>> relationList = new ArrayList<>();

	if (upperCase.indexOf(" ON ") > 0
		&& (upperCase.trim().indexOf("SELECT") >= 0 || upperCase.indexOf(" FROM ") > 0)) {
	    String[] splitFrom = upperCase.split(" FROM ");
	    if (splitFrom.length < 2) {
		return;
	    }
	    Map<String, String> asMap = new HashMap<>();
	    String x = splitFrom[1];
	    if (x.contains(" AND ")) {
		String[] andCon = x.split(" AND ");
		x = andCon[0];
		getEqualFromOther(relationList, andCon, asMap);
	    }
	    if (x.contains(" WHERE ")) {
		String[] whereSplit = x.split(" WHERE ");
		x = whereSplit[0];
		if (x.indexOf(" ON ") < 0 || whereSplit.length > 1) {
		    String where = whereSplit[1];
		    findFromWhereCondition(relationList, where.split(" AND "), asMap);
		}
	    }

	    try {
		if (x.indexOf(" ON ") > 0) {
		    String[] onCondition = x.split(" ON ");
		    for (String xi : onCondition) {
			handleJoin(asMap, relationList, xi);
		    }
		    getRelSet(reSet, asMap, relationList);
		} else if (x.indexOf("=") < 0 && x.indexOf(",") > 0) {// inner join xx x,mm m where x.xi=m.mi
		    x = clearJoin(x, " ");
		    String[] labelTable = x.split(",");
		    for (String xi : labelTable) {
			tableAs(asMap, xi);
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }

	}
    }

    private static void handleMultiSelectSql(Set<String> reSet, String sql, Map<String, String> asMap) {
	List<Map<String, String>> relationList = new ArrayList<>();
	String[] splitFrom = sql.split(" FROM ");
	if (splitFrom.length < 2) {
	    return;
	}

	if (sql.indexOf(" ON ") > 0 && (sql.trim().indexOf("SELECT") >= 0 || sql.indexOf(" FROM ") > 0)) {

	    handleJoinOn(reSet, asMap, relationList, splitFrom);
	} else if (sql.indexOf(" FROM ") > 0) {
	    handleInnerJoin(asMap, relationList, splitFrom);
	}
    }

    private static void handleInnerJoin(Map<String, String> asMap, List<Map<String, String>> relationList,
	    String[] splitFrom) {
	String x = splitFrom[1];
	if (x.contains(" AND ")) {
	    String[] andCon = x.split(" AND ");
	    x = andCon[0];
	    getEqualFromOther(relationList, andCon, asMap);
	}
	if (x.contains(" WHERE ")) {
	    String[] whereSplit = x.split(" WHERE ");

	    x = whereSplit[0];
	    // VISU_APP_STROKE ) TMP , VISU_PERM_USER T1, VISU_PERM_ORGAN T2
	    if (!x.contains("=") && x.length() > 3) {
		String[] split = x.split(",");
		for (String ti : split) {
		    String[] tt = ti.split(" ");
		    if (tt.length > 2 && ti.indexOf(")") > 0) {
			putTableAs(asMap, tt[0], tt[2]);
		    }
		    if (tt.length == 2) {
			putTableAs(asMap, tt[0],tt[1]);
		    }
		}
	    }

	    if (x.indexOf(" ON ") < 0 || whereSplit.length > 1) {
		String where = whereSplit[1];
		findFromWhereCondition(relationList, where.split(" AND "), asMap);
	    }
	}
    }

    private static void handleJoinOn(Set<String> reSet, Map<String, String> asMap,
	    List<Map<String, String>> relationList, String[] splitFrom) {
	String x = splitFrom[1];
	if (x.contains(" AND ")) {
	    String[] andCon = x.split(" AND ");
	    x = andCon[0];
	    getEqualFromOther(relationList, andCon, asMap);
	}
	if (x.contains(" WHERE ")) {
	    String[] whereSplit = x.split(" WHERE ");
	    x = whereSplit[0];
	    if (x.indexOf(" ON ") < 0 || whereSplit.length > 1) {
		String where = whereSplit[1];
		findFromWhereCondition(relationList, where.split(" AND "), asMap);
	    }
	}

	try {
	    if (x.indexOf(" ON ") > 0) {
		String[] onCondition = x.split(" ON ");
		for (String xi : onCondition) {
		    handleJoin(asMap, relationList, xi);
		}
		getRelSet(reSet, asMap, relationList);
	    } else if (x.indexOf("=") < 0 && x.indexOf(",") > 0) {// inner join xx x,mm m where x.xi=m.mi
		x = clearJoin(x, " ");
		String[] labelTable = x.split(",");
		for (String xi : labelTable) {
		    tableAs(asMap, xi);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private static String joinTableColumn(String sql, String splitor) {
	String[] labels = sql.split(splitor);
	List<String> tables = new ArrayList<>();
	String temp = null;
	boolean hasStart = false;
	for (int i = 0, k = 0; i < labels.length; i++) {
	    String trim = labels[i].trim();
	    if (trim.length() > 0) {
		temp = trim;
		if (trim.startsWith("\\.")) {
		    tables.set(k, temp + trim);
		    continue;
		}
		if (trim.endsWith("\\.")) {
		    hasStart = true;
		    tables.add(temp);
		    k++;
		    continue;
		}
		if (hasStart) {
		    hasStart = false;
		    tables.set(k, tables.get(k) + temp);
		}
		tables.add(trim);
		k++;
	    }
	}

	String upperCase = String.join(" ", tables);
	return upperCase;
    }

    private static String clearJoin(String sql, String splitor) {
	String[] labels = sql.split(splitor);
	List<String> tokens = new ArrayList<>();
	trimTokens(labels, tokens);
	String upperCase = String.join(" ", tokens);
	return upperCase;
    }

    private static void trimTokens(String[] labels, List<String> tables) {
	for (String li : labels) {
	    if (li.trim().length() > 0) {
		tables.add(li.trim());
	    }
	}
    }

    private static void findFromWhereCondition(List<Map<String, String>> relationList, String[] whereCon,
	    Map<String, String> asMap) {
	for (int i = 0; i < whereCon.length; i++) {
	    String andCondition = whereCon[i];
	    if (andCondition.contains(" ON ")) {
		handleOnCondition(relationList, andCondition, asMap);
	    } else {
		if (hasRelation(andCondition)) {
		    String[] labels = andCondition.split(" ");
		    List<String> tokens = new ArrayList<>();
		    trimTokens(labels, tokens);

		    if (labels.length >= 2) {
			String start = tokens.get(0).trim();
			String end = tokens.get(1).trim();
			if (end.length() > 1) {
			    end= end.substring(1);
			} else {
			    end = tokens.get(2).trim();
			    if (end.startsWith("'") && end.endsWith("'") || end.equals("(")) {
				continue;
			    }
			}
			addRelation(relationList,start,end);
		    } else {
			String[] eq = tokens.get(0).split("=");
			addRelation(relationList,eq[0],eq[1]);
		    }
		}
	    }

	}
    }

    /**
     * 从条件中获取关系
     * 
     * @param relationList
     * @param andCon
     */
    private static void getEqualFromOther(List<Map<String, String>> relationList, String[] andCon,
	    Map<String, String> asMap) {
	for (int i = 1; i < andCon.length; i++) {
	    String andCondition = andCon[i];
	    if (andCondition.contains(" ON ")) {
		handleOnCondition(relationList, andCondition, asMap);
	    } else {
		if (hasRelation(andCondition)) {
		    String[] labels = andCondition.split(" ");
		    if (labels.length > 2) {
			List<String> tables = new ArrayList<>();
			trimTokens(labels, tables);
			String start = tables.get(0).trim();
			String end = tables.get(1).trim();
			if (end.length() > 1) {
			    end= end.substring(1);
			} else {
			    end = tables.get(2).trim();
			    if (end.startsWith("'") && end.endsWith("'") || end.equals("(")) {
				continue;
			    }
			}
			addRelation(relationList,start,end);
		    }
		}
	    }

	}
    }

    private static boolean hasRelation(String andCondition) {
	return andCondition.indexOf("=") > 0 && valueWrong(andCondition);
    }

    private static boolean valueWrong(String andCondition) {
	return andCondition.indexOf("'") < 0 && andCondition.indexOf("}") < 0 && andCondition.indexOf("(") < 0
		&& andCondition.indexOf("#{") < 0;
    }

    private static void handleOnCondition(List<Map<String, String>> relationList, String andCondition,
	    Map<String, String> asMap) {
	String[] split = andCondition.split(" ON ");

	String[] split2 = split[0].split(" JOIN ");
	if (split2 != null) {
	    handleJoin(relationList, split2[0], asMap);
	    if (split2.length > 1) {
		handleJoin(relationList, split2[1], asMap);
	    }
	}
	if (split.length > 1) {
	    handleJoin(relationList, split[1], asMap);
	}

    }

    private static void handleJoin(List<Map<String, String>> relationList, String split2, Map<String, String> asMap) {
	if (split2.indexOf(" ") < 0 && split2.contains("=") && split2.length() > 1) {
	    String[] eq = split2.split("=");
	    addRelation(relationList,eq[0],eq[1]);
	    return;
	}
	String upperCase = clearJoin(split2, " ");

	List<String> tables = tokens(upperCase);

	String start = tables.get(0).trim();

	if (tables.size() < 2) {
	    return;
	}
	String end = tables.get(1).trim();
	if (upperCase.contains("=")) {
	    if (end.length() > 1) {
		end=end.substring(1);
	    } else {
		end = tables.get(2).trim();
		if (end.startsWith("'") && end.endsWith("'")) {
		    return;
		}
	    }
	    addRelation(relationList,start,end);
	} else {
	    putTableAs(asMap, start, end);
	}

    }

    private static List<String> tokens(String upperCase) {
	List<String> tables = new ArrayList<>();
	String[] labels = upperCase.split(" ");
	trimTokens(labels, tables);
	return tables;
    }

    private static void handleJoin(Map<String, String> asMap, 
	    List<Map<String, String>> relationList, String xi) {
	Map<String, String> reLMap = new HashMap<>();

	if (xi.indexOf(" JOIN ") > 0) {
	    String[] join = xi.split(" JOIN ");
	    String trim = join[0].trim();
	    String[] labels = trim.split(" ");
	    List<String> tokens = new ArrayList<>();
	    trimTokens(labels, tokens);
	    String startTrim = tokens.get(0).trim();

	    String trim2 = null;
	    if (tokens.size() > 2) {
		trim2 = tokens.get(2).trim();
	    }

	    if (trim.contains("=")) {
		if (labels.length > 2) {
		    if (xi.indexOf("=") < 0 && tokens.size() < 4) {
			String endTrim = tokens.get(1).trim();
			putTableAs(asMap, startTrim, endTrim);
		    } else {
			if (tokens.size() <= 2 && startTrim.contains("=")) {
			    String[] eq = startTrim.split("=");
			    addRelation(relationList,eq[0],eq[1]);
			} else {
			    addRelation(relationList,startTrim,trim2);
			}
		    }
		}
	    } else {
		if (labels.length >= 2) {
		    if (tokens.size() < 4) {
			putTableAs(asMap,startTrim,tokens.get(1).trim());
		    } else {
			addRelation(relationList,startTrim,trim2);
		    }
		}
	    }

	    String[] lableTable = join[1].trim().split(" ");
	    putTableAs(asMap, lableTable[0].trim(), lableTable[1].trim());
//	    asMap.put(lableTable[1].trim(), lableTable[0].trim());
	    if (!reLMap.isEmpty()) {
		relationList.add(reLMap);
	    }
	} else {
	    String[] split = xi.trim().split(" ");
	    String trim2 = split[0].trim();
	    if (xi.indexOf("=") > 0 && split.length > 2) {
		String trim = split[2].trim();
		if (!trim.startsWith("'") && !trim.endsWith("'")) {
		    addRelation(relationList, trim2, trim);
		}
	    } else {
		if (split.length > 1) {
		    asMap.put(split[1].trim(), trim2);
		} else {
		    System.out.println(xi);
		}

	    }

	}
    }

    private static void addRelation(List<Map<String, String>> relationList, String trim2,
	    String trim) {
	if (trim.indexOf(".") < 0||trim2.indexOf(".")<0) {
	    return;
	}
	if (trim2.equals("(")||trim.equals("(")) {
	    return;
	}
	if (trim2.contains("(")||trim.contains("(")) {
	    return;
	}
	Map<String, String> reLMap = new HashMap<>();
	reLMap.put("start", trim2);
	reLMap.put("end", trim);
	relationList.add(reLMap);
    }

    private static void putTableAs(Map<String, String> asMap, 
	    String startTrim, String endTrim) {
	if(hasProblem(startTrim)||hasProblem(endTrim)) {
	    return;
	}
	
	asMap.put(endTrim, startTrim);
	
    }

    private static boolean hasProblem(String endTrim) {
	return endTrim.contains("#")||endTrim.contains("]")||
		endTrim.contains(" ")||endTrim.contains("[")||
		endTrim.contains(",")||endTrim.contains(":")||
		endTrim.contains("\\)")||
		endTrim.contains("\\(")||
		endTrim.contains("{")||
		endTrim.contains("=")||
		endTrim.contains("}");
    }

    /*private static void handleWhere(List<Map<String, String>> relationList, String xi, Map<String, String> reLMap) {
    if (xi.contains("WHERE")) {
        String[] joinCondition = xi.split("WHERE");
        String[] split2 = joinCondition[0].trim().split("=");
        reLMap.put("start", split2[0].trim());
        reLMap.put("end", split2[1].trim());
        if (!reLMap.isEmpty()) {
    	relationList.add(reLMap);
        }
    }
    }
    
    private static void handleAnd(List<Map<String, String>> relationList, String xi, Map<String, String> reLMap) {
    if (xi.contains("AND")) {
        String ci = xi.split("AND")[0];
        String[] tokens = ci.split(" ");
        if (tokens.length > 2) {
    	List<String> tables = new ArrayList<>();
    	for (String li : tokens) {
    	    if (li.trim().length() > 0) {
    		tables.add(li.trim());
    	    }
    	}
    	reLMap.put("start", tables.get(0));
    	reLMap.put("end", tables.get(1));
        }
        if (!reLMap.isEmpty()) {
    	relationList.add(reLMap);
        }
    }
    }*/

    private static void getRelSet(Set<String> reSet, Map<String, String> asMap,
	    List<Map<String, String>> relationList) {
	for (Map<String, String> reLMapi : relationList) {
	    if (reLMapi.isEmpty() || reLMapi.size() < 2) {
		continue;
	    }
	    String startTrim = reLMapi.get("start").trim();
	    String[] startTableColumn = startTrim.split("\\.");
	    if (startTableColumn.length < 2) {
		System.out.println("w:" + startTrim);
		continue;
	    }
	    String key = startTableColumn[0];
	    key = clearStartParentheses(key);
	    String colStart = clearEndParentheses(startTableColumn);
	    String tableStart = asMap.get(key);
	    tableStart = clearTable(tableStart);
	    String endTrim = reLMapi.get("end").trim();
	    String[] endTableColumn = endTrim.split("\\.");
	    String end = endTableColumn[0];
	    end = clearStartParentheses(end);
	    String tableEnd = asMap.get(end);
	    tableEnd = clearTable(tableEnd);
	    if (endTableColumn.length < 2) {
		System.out.println("w End:" + endTrim);
		continue;
	    }
	    String colEnd = clearEndParentheses(endTableColumn);
	    if (tableStart == null || tableEnd == null) {
		continue;
	    }
	    reSet.add(tableStart.trim() + "." + colStart + "=" + tableEnd.trim() + "." + colEnd);
	}
    }

    private static String clearTable(String tableStart) {
	if(tableStart==null) {
	    return null;
	}
	tableStart=tableStart.replaceAll("\\)", "");
	tableStart=tableStart.replaceAll("\\(", "");
	return tableStart;
    }

    private static String clearStartParentheses(String end) {
	if (end.contains("(")) {
	    String[] split = end.split("\\(");
	    if (split.length < 2) {
		end = split[0];
	    }
	    end = split[1];
	}
	return end;
    }

    private static String clearEndParentheses(String[] split3) {
	String trim = split3[1].trim();
	if (trim.contains(")")) {
	    trim = trim.split("\\)")[0];
	}
	return trim;
    }
}
