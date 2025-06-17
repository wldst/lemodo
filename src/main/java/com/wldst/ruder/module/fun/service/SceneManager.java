package com.wldst.ruder.module.fun.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.SceneDomain;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.util.DateUtil;
import com.wldst.ruder.util.MapTool;  
  

public class SceneManager extends SceneDomain{
    final static Logger logger = LoggerFactory.getLogger(SceneManager.class);
    private Stack<String> leftScenes;
    private Stack<String> rightScenes;
    private String currentScene;

    private CrudUserNeo4jService neo4jUserService;
    private CrudNeo4jService neo4jService;
    private UserAdminService adminService;
    
    public SceneManager() {
	leftScenes = new Stack<>();
	rightScenes = new Stack<>();
	currentScene = "homePage";
	leftScenes.push(currentScene);
	if(SpringContextUtil.getApplicationContext()!=null) {
	    neo4jUserService=(CrudUserNeo4jService) SpringContextUtil.getBean(CrudUserNeo4jService.class);
	    neo4jService=(CrudNeo4jService) SpringContextUtil.getBean(CrudNeo4jService.class);
	    adminService=(UserAdminService) SpringContextUtil.getBean(UserAdminService.class);
	}
	
    }

    public void goToScene(String scene) {
	currentScene = scene;
	if (neo4jService != null) {
	    String nowDateTime = saveSceneUseInfo(scene);
//	    scene,inTime,user,num
	    refreshSceneUseCount(scene, nowDateTime);
//	    neo4jUserService.geton
//	    neo4jService.getNodeByPropAndLabel(m, scene);
	    
	    
//	    Long sceneId = neo4jService.getNodeId(CODE, scene, SCENE);
	   
//	    relationService.addRel("used", adminService.getCurrentUserId(), sceneId, m);
	}
	
	if("homePage".equals(scene)) {
	    goToHomePage();
	    return;
	}
	if (leftScenes.isEmpty()) {
	    leftScenes.push(currentScene);
	} else {
	    String peek = leftScenes.peek();
	    if (peek == null || peek != null && !scene.equals(peek)) {
		leftScenes.push(currentScene);
	    }
	}
	LoggerTool.info(logger,"goToScene =="+scene+" leftScenes="+String.valueOf(leftScenes)+"rightScenes="+String.valueOf(rightScenes));

    }

	public void refreshSceneUseCount(String scene, String nowDateTime) {
		Map<String, Object> sc = MapTool.newMap();
		sc.put("scene", scene);
		sc.put("user", adminService.getCurrentUserId());
		List<Map<String, Object>> count = neo4jService.queryBy(sc, SCENE_USE_COUNT);
		if (count == null || count.isEmpty()) {
			sc.put("num", 1);
			sc.put("inTime", nowDateTime);
			neo4jService.saveByBody(sc, SCENE_USE_COUNT);
		} else {
			Map<String, Object> ci = count.get(0);
			ci.put("num", integer(ci, "num") + 1);
			ci.put("inTime", nowDateTime);
			ci.put("scene", scene);
			neo4jService.saveByBody(ci, SCENE_USE_COUNT);
		}
	}

    public String saveSceneUseInfo(String scene) {
	Map<String, Object> m = MapTool.newMap();
	String nowDateTime = DateUtil.nowDateTime();
	m.put("inTime", nowDateTime);
	m.put("path", scenePath()); 
	m.put("scene",scene);
	neo4jService.saveByBody(m, SCENE_USE_INFO);
	return nowDateTime;
    }

    public void rightReceived(String scene) {
	if (rightScenes.isEmpty()&&!scene.equals("homePage")) {
	    rightScenes.push(scene);
	} else {
	    String peek = rightScenes.peek();
	    if (peek == null || peek != null && !scene.equals(peek)) {
		rightScenes.push(scene);
	    }
	}
	LoggerTool.info(logger,"backToScene "+scene+" leftScenes="+String.valueOf(leftScenes)+"rightScenes="+String.valueOf(rightScenes));

    }

    public String toRight() {
	if (leftScenes.size() <= 1) {
	    return leftScenes.peek(); // 已经在主页，无法再返回上一步
	}	
	rightReceived(leftScenes.pop());
	String currentScene = leftScenes.peek();
	LoggerTool.info(logger," after toRight ,currentScene= "+currentScene+" leftScenes="+String.valueOf(leftScenes)+"rightScenes="+String.valueOf(rightScenes));
	return currentScene;
    }

    public String toLeft() {
	if (rightScenes.size() < 1) {
	    return null; // 已经在主页，无法再返回上一步
	}
	goToScene(rightScenes.pop());
	LoggerTool.info(logger," after toLeft ,currentScene="+currentScene+" leftScenes="+String.valueOf(leftScenes)+"rightScenes="+String.valueOf(rightScenes));

	return currentScene;
    }

    public String nextScene() {
	if (rightScenes.isEmpty()) {
	    return null; // 栈为空，没有下一个场景
	}
	String currentScene=rightScenes.peek();
	LoggerTool.info(logger,"nextScene "+currentScene+" leftScenes="+String.valueOf(leftScenes)+"rightScenes="+String.valueOf(rightScenes));

	return currentScene;
    }

    public String preScene() {
	toRight();
	if (leftScenes.isEmpty()) {
	    return null; // 栈为空，没有下一个场景
	}
	String pre = leftScenes.peek();
	toLeft();
	LoggerTool.info(logger,"preScene "+pre+" leftScenes="+String.valueOf(leftScenes)+"rightScenes="+String.valueOf(rightScenes));
	return pre;
    }

    public String goToHomePage() {
	leftScenes.clear();
	rightScenes.clear();
	currentScene = "homePage";
	leftScenes.push(currentScene);
	return currentScene;
    }

    public static void main(String[] args) {
	SceneManager sceneManager = new SceneManager();
	sceneManager.goToScene("test1");
	sceneManager.goToScene("test2");
	sceneManager.goToScene("test3");
	String join = sceneManager.scenePath();
	System.out.println("当前场景：" + join ); // 输出：场景3
	System.out.println("当前场景：" + sceneManager.nextScene()); // 输出：场景3
	System.out.println("上一步场景：" + sceneManager.toRight()); // 输出：场景2
	System.out.println("下一步场景：" + sceneManager.nextScene()); // 输出：场景3
	System.out.println("返回主页场景"); // 输出：无输出
	sceneManager.goToHomePage();
	System.out.println("当前场景：" + sceneManager.nextScene()); // 输出：主页
    }

    public String scenePath() {
	Object[] array = leftScenes.toArray();
	List<String> scenes = new ArrayList<>(array.length);
	for(Object ai: array ) {
	    scenes.add(String.valueOf(ai));
	}
	String join = String.join("/", scenes);
	return join;
    }

    
    public UserAdminService getAdminService() {
        return adminService;
    }

    public void setAdminService(UserAdminService adminService) {
        this.adminService = adminService;
    }
    
    
    
}