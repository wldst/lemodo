package com.wldst.ruder.module.bs;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.VoiceOperateDomain;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.impl.AddRelation;
import com.wldst.ruder.module.bs.impl.SaveNode;
import com.wldst.ruder.module.voice.LfasrService;
import com.wldst.ruder.util.CrudUtil;

/**
 * 领域对象，基本操作功能脚本
 */
@Component
public class DomainOperator extends VoiceOperateDomain {
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
    
    public String label(Long id) {
	return neo4jService.label(id);
    }
    
    public String getLabel(Map<String,Object> vo) {
	return neo4jService.label(id(vo));
    }

    public Map<String, Object> getNode(String label, String key, String value) {
	return neo4jService.getAttMapBy(key, value, label);
    }    

    public void addRel(String relLabel, Long nodeId, Long moduleId) {
	AddRelation mm = new AddRelation(neo4jService, relLabel, nodeId, moduleId);
	mm.execute();
    }

    public void addRel(String relLabel, Long startNodeId, Map<String, Object> endNode) {
	AddRelation ar = new AddRelation(neo4jService, relLabel, startNodeId, id(endNode));
	ar.execute();
    }

    public void addRel(String relLabel, Map<String, Object> savedApp, Long nodeId) {
	AddRelation ar = new AddRelation(neo4jService, relLabel, id(savedApp), nodeId);
	ar.execute();
    }

    public void addRel(String relLabel, Map<String, Object> savedModule, Map<String, Object> savedApp) {
	if(savedModule==null) {
	    return;
	}
	AddRelation ar = new AddRelation(neo4jService, relLabel, id(savedApp), id(savedModule));
	ar.execute();
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


   

 

    private void getInto(String label, String sessionId) {
	Map<String, Object> myContext = getMyContext(sessionId);
	myContext.put(OPERATE_LABEL, label);
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

     


}
