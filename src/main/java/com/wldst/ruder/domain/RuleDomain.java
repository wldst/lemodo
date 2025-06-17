package com.wldst.ruder.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wldst.ruder.constant.RuleConstants;
import com.wldst.ruder.crud.service.CrudNeo4jDriver;
import com.wldst.ruder.engine.DroolsService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.fun.service.DataCacheManager;

@Component
public class RuleDomain extends RuleConstants {
    final static Logger logger = LoggerFactory.getLogger(RuleDomain.class);

    protected static List<Map<String, Object>> globalRuleInfo;
    @Autowired
    private CrudNeo4jDriver driver;
    @Autowired
    private DroolsService drools;
    private UserAdminService adminService;
    private static DataCacheManager dcm = new DataCacheManager();

    public List<Map<String, Object>> getGlobalRuleInfo() {
	StringBuilder queryEndId = new StringBuilder();
	queryEndId.append("match(n:Rule)");
	queryEndId.append(" where n.global=\"on\"");
	queryEndId.append(" return id(n) AS id,n.rulekey as ruleKey,n.content AS content");
	String query = queryEndId.toString();
	return cacheQuery(query);
    }

    public List<Map<String, Object>> getRuleInfo(Map<String, Object> dataMap, String label) {
	StringBuilder queryEndId = new StringBuilder();
	queryEndId.append("match p= (n");
	if (label != null) {
	    queryEndId.append(":" + label);
	}
	queryEndId.append(")-[r]->(m:Rule)");
	if (dataMap.containsKey("id") && dataMap.get("id") != null
		&& !String.valueOf(dataMap.get("id")).trim().equals("")) {
	    queryEndId.append(" where id(n)=" + dataMap.get("id") + "");
	}
	queryEndId.append(" return distinct(id(m)) AS id,m.rulekey as ruleKey,m.content AS content");
	String query=queryEndId.toString();
	return cacheQuery(query);
    }

    public List<Map<String, Object>> getRuleInfoById(Map<String, Object> dataMap) {
	return getRuleInfo(dataMap, null);
    }

    public List<Map<String, Object>> getRuleInfoByLabel(String label) {
	return getLabelRuleInfo(label);
    }

    public List<Map<String, Object>> getLabelRuleInfo(String label) {
	if (label == null) {
	    return null;
	}
	StringBuilder queryEndId = new StringBuilder();
	queryEndId.append("match(n:" + META_DATA + ")");
	queryEndId.append(")-[r]->(m:Rule)");
	queryEndId.append(" where n.label=" + label);

	queryEndId.append(" return id(m) AS id,m.rulekey as ruleKey,m.content AS content");
	String query=queryEndId.toString();
	return cacheQuery(query);
    }

    public List<Map<String, Object>> cacheQuery(String query) {
	Object data2 = dcm.getData(query);
	if(null!=data2) {;
	return (List<Map<String, Object>>) data2;
	}
	List<Map<String, Object>> queryData = driver.queryData(query);
	dcm.putData(query,queryData);
	return queryData;
    }

    public List<Map<String, Object>> getQueryRuleInfo() {
	StringBuilder queryEndId = new StringBuilder();
	queryEndId.append(" match   (n:Rule)-[r]->(m:ruleScope{code:'query'}) ");
	queryEndId.append(" return id(n) AS id,n.rulekey as ruleKey,n.content AS content");
	String query=queryEndId.toString();
	return cacheQuery(query);
    }

    public void formateQueryField(Map<String, Object> vo) {
	validAdminService();
	List<Map<String, Object>> ruleInfo = getQueryRuleInfo();
	if (ruleInfo != null && !ruleInfo.isEmpty() && drools != null) {
	    drools.execute(ruleInfo, vo);
	}
    }

    public void formateQueryField(List<Map<String, Object>> voList) {
	validAdminService();
	List<Map<String, Object>> ruleInfo = getQueryRuleInfo();
	if (ruleInfo != null && !ruleInfo.isEmpty()) {
		KieSession kieSession=drools.initSession(ruleInfo);
		for (Map<String, Object> voi : voList) {
			drools.execute(kieSession,voi);
	    }
	}
    }

    public void refreshGlobalRule() {
	globalRuleInfo = null;
    }

    private void excuteRule(Map<String, Object> vo, List<Map<String, Object>> globalRuleInfo,
	    List<Map<String, Object>> ruleInfo) {
	if (ruleInfo == null) {
	    ruleInfo = new ArrayList<>();
	}
	Map<String, Map<String, Object>> myRule = new HashMap<>();
	for (Map<String, Object> glo : ruleInfo) {
	    String object = String.valueOf(glo.get(NODE_ID));
	    myRule.put(object, glo);
	}
	if (globalRuleInfo != null && !globalRuleInfo.isEmpty()) {
	    for (Map<String, Object> glo : globalRuleInfo) {
		// if("154280".equals(String.valueOf(glo.get(NODE_ID)))) {
		// continue;
		// }
		if (!myRule.containsKey(String.valueOf(glo.get(NODE_ID)))) {
		    ruleInfo.add(glo);
		}
	    }
	}
	if (ruleInfo != null && !ruleInfo.isEmpty()) {
	    validAdminService();
	    drools.execute(ruleInfo, vo);
	}
    }

    public void validRule(String label, Map<String, Object> vo) {
	if (globalRuleInfo == null) {
	    globalRuleInfo = getGlobalRuleInfo();
	}

	List<Map<String, Object>> ruleInfo = getRuleInfo(vo, label);
	if (label != null) {
	    if (!vo.containsKey(LABEL)) {
		vo.put(LABEL, label);
		excuteRule(vo, globalRuleInfo, ruleInfo);
		vo.remove(LABEL);
	    } else {
		excuteRule(vo, globalRuleInfo, ruleInfo);
	    }

	} else {
	    excuteRule(vo, globalRuleInfo, ruleInfo);
	}
    }

    public void validMyRule(String label, Map<String, Object> vo, Map<String, Object> po) {
	List<Map<String, Object>> ruleInfo = getRuleInfoById(po);
	excuteRule(vo, null, ruleInfo);
    }

    public void validRule(String label, Map<String, Object> vo, Map<String, Object> po) {
	if (globalRuleInfo == null) {
	    globalRuleInfo = getGlobalRuleInfo();
	}
	List<Map<String, Object>> ruleInfo = getRuleInfoById(po);
	if (label != null) {
	    vo.put(LABEL + META_DATA, label);
	    excuteRule(vo, globalRuleInfo, ruleInfo);
	    vo.remove(LABEL + META_DATA);
	} else {
	    excuteRule(vo, globalRuleInfo, ruleInfo);
	}
    }

    public void validAdminService() {
	if (drools != null && drools.getAdminService() == null) {
	    drools.setAdminService(adminService);
	}
    }

    public UserAdminService getAdminService() {
	return adminService;
    }

    public void setAdminService(UserAdminService adminService) {
	this.adminService = adminService;
    }

    public DroolsService getDrools() {
	return drools;
    }

    public void setDrools(DroolsService drools) {
	this.drools = drools;
    }
}
