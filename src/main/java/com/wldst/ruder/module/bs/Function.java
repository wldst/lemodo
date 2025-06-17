package com.wldst.ruder.module.bs;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.VoiceOperateDomain;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.impl.AddRelation;
import com.wldst.ruder.module.bs.impl.SaveNode;
import com.wldst.ruder.module.voice.LfasrService;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.MapTool;

/**
 * 通用功能
 */
@Component
public class Function extends MapTool {
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private CrudUserNeo4jService neo4jUService;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudUtil crudUtil;

    private Map<String, Map<String, Object>> context = new HashMap<>();
    
    public SaveNode save(String targetLabel) {
	SaveNode save = new SaveNode(neo4jService, targetLabel);
	save.setCrudUtil(crudUtil);
	return save;
    }

    public Long getId(String label, String key, String value) {
	return neo4jService.getNodeId(key, value, label);
    }

    public Map<String, Object> getNode(String label, String key, String value) {
	return neo4jService.getAttMapBy(key, value, label);
    }    

    public void addRel(String relLabel, Long nodeId, Long moduleId) {
	AddRelation mm = new AddRelation(neo4jService, relLabel, nodeId, moduleId);
	mm.execute();
    }

    public void addRel(String relLabel, Long startNodeId, Map<String, Object> endNode) {
	Long endId = id(endNode);
	AddRelation ar = new AddRelation(neo4jService, relLabel, startNodeId, endId);
	ar.execute();
    }

    public void addRel(String relLabel, Map<String, Object> savedApp, Long nodeId) {
	Long appId = id(savedApp);
	AddRelation ar = new AddRelation(neo4jService, relLabel, appId, nodeId);
	ar.execute();
    }

    public void addRel(String relLabel, Map<String, Object> savedModule, Map<String, Object> savedApp) {
	if(savedModule==null) {
	    return;
	}
	Long startId = id(savedApp);
	Long endId = id(savedModule);
	AddRelation ar = new AddRelation(neo4jService, relLabel, startId, endId);
	ar.execute();
    }
    
    public Integer count(String relLabel) {
	if(relLabel==null) {
	    return 0;
	}
	String query = "MATCH(n:"+relLabel+") return count(n) AS "+relLabel+"Count";
	String one = neo4jService.getOne(query,relLabel+"Count");
	return Integer.valueOf(one);
    }

    public String getMyDesktopName() {
	String myName = adminService.getCurrentAccount();
	String myDesktopName = "deskTop_" + myName;
	return myDesktopName;
    }

    

    public String voiceFileToText(String pathname) {
	LfasrService transText = new LfasrService(pathname);
	String voice2Text = "";
	try {
	    voice2Text = transText.voice2Text();
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	return voice2Text;
    }


   

 

   

    private void setMayConextProp(String key, Object value, String sessionId) {
	getMyContext(sessionId).put(key, value);
    }

    private Map<String, Object> getMyContext(String sessionId) {
	Map<String, Object> myContext = context.get(sessionId);
	if (myContext == null) {
	    myContext = new HashMap<>();
	    context.put(sessionId, myContext);
	}
	return myContext;
    }

    private String getMyKey() {
	String currentUserName = adminService.getCurrentAccount();
	String currentUserId = adminService.getCurrentPasswordId() + "";
	String userkey = currentUserId + "-" + currentUserName;
	return userkey;
    }

}
