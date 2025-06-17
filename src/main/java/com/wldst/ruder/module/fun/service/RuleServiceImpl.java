package com.wldst.ruder.module.fun.service;

import java.util.*;
import java.util.Map.Entry;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.wldst.ruder.constant.RuleConstants;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.util.DateUtil;
import com.wldst.ruder.util.ValidateUtil;

@Service
public class RuleServiceImpl extends RuleConstants implements RuleService {
    private static Logger logger = LoggerFactory.getLogger(RuleServiceImpl.class);
    private CrudNeo4jService neo4jService;

    private CrudUserNeo4jService neo4jUService;

    private HtmlShowService htmlShowService;

	@Autowired
    public RuleServiceImpl(@Lazy CrudNeo4jService neo4jService, @Lazy CrudUserNeo4jService neo4jUService, HtmlShowService htmlShowService){
        this.neo4jService=neo4jService;
        this.neo4jUService=neo4jUService;
        this.htmlShowService=htmlShowService;
    }


    @Override
    public Boolean isDuplicate(Map<String, Object> map, String key) {
	String keyValue = string(map, key);

	Long id = id(map);
	String label = string(map, LABEL + META_DATA);
	if (label == null) {
	    return false;
	}
	String headeri = neo4jService.getHeaderOf(key, label);

	String queryDuplicat = "match (n:" + label + ")";
	String uniqueCondition = "  n." + key + "=\"" + keyValue + "\"";
	if (id != null) {
	    String idNotEqual = "id(n)<>" + id;
	    queryDuplicat = queryDuplicat + " where " + idNotEqual + " and " + uniqueCondition
		    + " return count(n) as countNu";
	} else {
	    queryDuplicat = queryDuplicat + " where  " + uniqueCondition + " return count(n) as countNu";
	}
	List<Map<String, Object>> query = neo4jService.cypher(queryDuplicat);
	if (query == null) {
	    return false;
	}
	boolean b = integer(query.get(0), "countNu") > 0;
	if (b) {
	    map.put(VALID_RESULT, false);
	    map.put(VALIDATE_MSG, headeri + "已存在！");
	}
	return !b;
    }

    @Override
    public Boolean validField(Map<String, Object> map) {
	String label = label(map);

	String getValidKeySet = "match (vf:" + VALID_FIELD + ") where vf.poId='" + label
		+ "' return vf.field,vf.type,vf.regex";
	List<Map<String, Object>> inputValidate = neo4jService.cypher(getValidKeySet);
	Map<String, Set<String>> validFieldSet = new HashMap<>();
	if (inputValidate != null && !inputValidate.isEmpty()) {
	    for (Map<String, Object> oi : inputValidate) {
		String key = string(oi, "type");
		String value = string(oi, "field");
		String regex = string(oi, "regex");
		Set<String> set = validFieldSet.get(key);
		if (set == null) {
		    set = new HashSet<>();
		}
		set.add(value);
		validFieldSet.put(key, set);
	    }
	}

	Set<String> keySet = map.keySet();
	// 校验规则字段集合
	for (String vkey : validFieldSet.keySet()) {
	    Set<String> set = validFieldSet.get(vkey);
	    if (vkey.equals("unique")) {
		for (String key : keySet) {
		    // 获取需要校验重复的信息。
		    if (set.contains(key) && !isDuplicate(map, key)) {
			return true;
		    }
		}
	    }
	    if (vkey.equalsIgnoreCase("notNull")) {
		for (String key : keySet) {
		    // 获取需要校验重复的信息。
		    if (set.contains(key) && !isNull(map, key)) {
			return true;
		    }
		}
	    }
	    if (vkey.equalsIgnoreCase("url")) {
		for (String key : keySet) {
		    // 获取需要校验重复的信息。
		    if (set.contains(key) && isURL(map, key)) {
			return true;
		    }
		}
	    }
	    if (vkey.equalsIgnoreCase("date")) {
		for (String key : keySet) {
		    // 获取需要校验重复的信息。
		    if (set.contains(key) && isDate(map, key)) {
			return true;
		    }
		}
	    }

	}

	return false;
    }

    @Override
    public Boolean isOutOfRange(Map<String, Object> map, String key) {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public Boolean isDate(Map<String, Object> map, String key) {
	if (!map.containsKey(key) || map.get(key) == null) {
	    return true;
	}

	return ValidateUtil.checkDate(string(map, key));
    }

    @Override
    public Boolean isNull(Map<String, Object> map, String key) {
	if (!map.containsKey(key) || map.get(key) == null) {
	    map.put(VALID_RESULT, false);
	    Object object = map.get(VALIDATE_MSG);
	    String value = key + "为空！";
	    if (object != null) {
		map.put(key, string(map, VALIDATE_MSG) + "\n" + value);
	    } else {
		map.put(VALIDATE_MSG, value);
	    }
	    return true;
	}
	return false;
    }

    @Override
    public Boolean isEmail(Map<String, Object> map, String key) {
	if (!map.containsKey(key) || map.get(key) == null) {
	    return true;
	}

	return ValidateUtil.isMobile(string(map, key));
    }

    @Override
    public Boolean isNumber(Map<String, Object> map, String key) {
	if (!map.containsKey(key) || map.get(key) == null) {
	    return true;
	}
	return false;
    }

    @Override
    public Boolean isPoneNumber(Map<String, Object> map, String key) {
	// TODO Auto-generated method stub
	if (!map.containsKey(key) || map.get(key) == null) {
	    return true;
	}

	String phone = string(map, key);
	return ValidateUtil.isPhone(phone) || ValidateUtil.isMobile(phone);
    }

    @Override
    public Boolean isIdCard(Map<String, Object> map, String key) {
	if (!map.containsKey(key) || map.get(key) == null) {
	    return true;
	}
	return ValidateUtil.isIdCard(string(map, key));
    }

    @Override
    public Boolean isURL(Map<String, Object> map, String key) {
	if (!map.containsKey(key) || map.get(key) == null) {
	    return true;
	}
	return ValidateUtil.isURL(string(map, key));
    }

    @Override
    public Map<String, Object> formateQueryField(Map<String, Object> mapx) {
		Map<String,Object> map = copy(mapx);
	String label = neo4jService.label(id(map));
	List<Map<String, Object>> field2 = htmlShowService.getField(label);
	if(field2==null||field2.isEmpty()) {
	    return map;
	}
	Set<String> timeField = new HashSet<>();
	Map<String, Object> formatMap = new HashMap<>();
	for (Map<String, Object> mi : field2) {
	    String field = string(mi, "field");
	    if(field==null||"null".equals(field)) {
		continue;
	    }
	    String formatSwitch = string(mi, "formateQuery");
	    if ("false".equals(formatSwitch)) {
		continue;
	    }
	    LoggerTool.info(logger,"formateQueryField label:{},field:{},",label,field);
	    Object object = mi.get("isPo");
	    if (object != null && !"".equals(object) && Boolean.valueOf(String.valueOf(object))) {
		formatePoWithName(map, label, field);
	    } else {
		//状态机的状态步骤，获取状态中文名称
		if(STATUS.equals(field)) {
			String status2 = status(map);
			if(status2!=null) {
			    String statusName = neo4jService.getStatusName(status2);
				if(statusName!=null) {
					formatMap.put(field, statusName);
				}
			}
		}
		String showType = string(mi, "showType");
		if (showType != null) {
		    String lowerCase = showType.toLowerCase();
		    if (lowerCase.contains("upload")) {
			formateFileOrImg(map, mi, field, lowerCase);
		    }
			if(lowerCase.equals("datetime")||lowerCase.equals("date")||lowerCase.equals("time")){
				timeField.add(field);
			}
		}
	    }
	}

	formatMap.putAll(map);
	for (Entry<String, Object> ei : map.entrySet()) {
		if(timeField.contains(ei.getKey())){
			formateDateTime(formatMap, ei);
		}
	}
	return formatMap;
    }

    private void formatePoWithName(Map<String, Object> map, String label, String field) {
	String relateValue = string(map, field);
	if (relateValue != null && !"".equals(relateValue.trim())) {
	    if(ValidateUtil.isNum(relateValue)) {
		Map<String, Object> propMapBy = neo4jService.getPropMapBy(relateValue);
		    // 可视化问题处理。选择第一个字段。
		    map.put(field, neo4jUService.seeNodeText(propMapBy));
		    map.put(field+COLUMN_FORMAT, neo4jUService.seeNodeText(propMapBy));
	    }else {
		//状态机的状态步骤，获取状态中文名称
		if(STATUS.equals(field)) {
		    map.put(field, neo4jService.getStatusName(relateValue));
		}
	    }
	    
	}
	
    }

    private void formateDateTime(Map<String, Object> ddMap, Entry<String, Object> ei) {
	    Object value2 = ei.getValue();
		String key =ei.getKey();
		formateDateField(ddMap,  value2, key);
	}

	public void formateDateField(Map<String, Object> ddMap, Object value2, String key){
		if (value2== null || value2.equals("")) {
			return;
		}
		if (value2 instanceof Long l) {
		 dateLongString(ddMap, key, l);
		} else if (value2 instanceof String s) {
			if (s.endsWith("天")) {
				return;
			}
			if (s.indexOf("年") > 0 || s.indexOf("月") > 0 || s.indexOf("日") > 0) {
				dateLongString(ddMap, key, DateUtil.getDateByCn(s).getTime());
			} else {
				if (s.indexOf("-") < 0 && s.indexOf(":") < 0 && s.indexOf("/") < 0 && !ValidateUtil.isChineseCharacter(s)) {
					Long l = Long.valueOf(s);
					dateLongString(ddMap, key, l);
				}
				if (s.indexOf("/") > 0 && s.indexOf(":") < 0) {
					dateLongString(ddMap, key, DateUtil.strToDate2(s).getTime());
				}
				if (s.indexOf("-") > 0 && s.indexOf(":") < 0) {
					dateLongString(ddMap, key, DateUtil.strToDate(s).getTime());
				}
				if (s.indexOf(":") > 0) {
					dateLongString(ddMap, key, DateUtil.strToDateLong(s).getTime());
				}
			}
		}
	}



	public void dateLongString(Map<String, Object> ddMap, String key, Long l) {
	String dateToStrLong;
	Calendar instance = Calendar.getInstance();
	instance.setTimeInMillis(l);
	dateToStrLong = DateUtil.dateToStrLong(instance.getTime());
	ddMap.put(key, dateToStrLong);
	ddMap.put(key+COLUMN_FORMAT, dateToStrLong);
    }
    
    

    private void formateFileOrImg(Map<String, Object> map, Map<String, Object> mi, String field, String lowerCase) {
	if (lowerCase.contains("image") || lowerCase.contains("file")) {
	    String idString = string(map, field);

	    if (idString != null && !idString.trim().isBlank()) {
		if (!idString.contains(",")) {
		    try {

			Long.valueOf(idString);
		    } catch (Exception e) {
			return;
		    }
		}
		String[] split = idString.split(",");

		if ("on".equals(string(mi, "showImage"))) {
		    String downLoadPath = LemodoApplication.MODULE_NAME+"/file/show/";
		    if (split.length > 1) {
			StringBuilder sb = new StringBuilder();

			for (String idi : split) {
			    // Map<String, Object> propMapBy = neo4jService.getPropMapBy(idi);
			    // String fileName = name(di);
			    String[] split2 = idi.split("/");

			    if (split2.length > 0) {
				idString = split2[split2.length - 1];
				sb.append(downLoadPath + idi + ",");
			    } else {
				sb.append(idi + ",");
			    }
			}
			map.put(field, sb.toString());
		    } else {
			// Map<String, Object> propMapBy = neo4jService.getPropMapBy(idString);
			// String fileName = name(di);
			String[] split2 = idString.split("/");

			if (split2.length > 0) {
			    idString = split2[split2.length - 1];
			}
			map.put(field, downLoadPath + idString);
		    }
		} else {
		    if (split.length > 1) {
			StringBuilder sb = new StringBuilder();

			for (String idi : split) {
			    Map<String, Object> propMapBy = neo4jService.getPropMapBy(idi);
			    String fileName = name(propMapBy);
			    sb.append("<button id=\"downLoad\" class=\"layui-btn layui-btn-xs\" onclick=\"downLoad("
				    + idi + ")\">" + fileName + "</button>");
			}
			map.put(field, sb.toString());
		    } else {
			Map<String, Object> propMapBy = neo4jService.getPropMapBy(idString);
			if (propMapBy != null) {
			    String fileName = name(propMapBy);
			    map.put(field,
				    "<button id=\"downLoad\" class=\"layui-btn layui-btn-xs\" onclick=\"downLoad("
					    + idString + ")\">" + fileName + "</button>");
			}

		    }
		}

	    }

	}
    }

    @Override
    public void clearFieldInfo(Map<String, Object> map) {
	String labelPo = null;
	if (map.containsKey("poId")) {
	    labelPo = string(map, "poId");
	} else if (map.containsKey(LABEL)) {
	    labelPo = label(map);
	}
	if (labelPo != null && !"".equals(labelPo.trim())) {
	    htmlShowService.clearFieldInfo(labelPo);
	}
    }

}
