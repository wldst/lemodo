package com.wldst.ruder.module.fun.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.module.bs.BeanShellService;
import com.wldst.ruder.util.ModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.SceneDomain;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.fun.service.RunAppService;
import com.wldst.ruder.module.fun.service.SceneManager;

import bsh.EvalError;
import bsh.Interpreter;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 操作控制器：读取相应场景下的数据
 *
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/scene")
public class SceneController extends SceneDomain {

    private CrudUserNeo4jService neo4jService;
    @Autowired
    private HtmlShowService showService;
    private UserAdminService adminService;
    @Autowired
    private RunAppService ras;
    private BeanShellService bss;

    private Map<Long, SceneManager> sceneStack = new HashMap<>();

    @Autowired
    public SceneController(CrudUserNeo4jService neo4jService, UserAdminService adminService, BeanShellService bss){
        this.neo4jService=neo4jService;
        this.adminService=adminService;
        this.bss=bss;
    }

    @RequestMapping(value = "/all", method = {RequestMethod.GET, RequestMethod.POST})
    public String sceneList(Model model, HttpServletRequest request) throws Exception {

        List<Map<String, Object>> scenes = neo4jService.listAllByLabel(SCENE);
        Map<String, Object> ctypei = newMap();
        ctypei.put(NAME, "场景");
        ctypei.put(CODE, "sceneSelect");
        String poSelectType = showService.poSelectType(ctypei, scenes);
        model.addAttribute("sceneList", poSelectType);

        // List<String> codesList = new ArrayList<>(scenes.size());
        StringBuilder htmls = new StringBuilder();
        StringBuilder toolFun = new StringBuilder();

        for (Map<String, Object> mi : scenes) {
            String querySceneBtn = "MATCH(s:Scene)-[r]->(b:layTableToolOpt) where s.code='" + code(mi) + "' return b";
            List<Map<String, Object>> btns = neo4jService.cypher(querySceneBtn);
            // codesList.add(code(mi));
            String si = "<fieldset class=\"layui-elem-field layui-field-title\" style=\"margin-top: 30px;\">\n"
                    + "	    <legend >" + name(mi) + "</legend>\n" + "	  </fieldset>";

            String sceneiBtn = showService.sceneiBtn(toolFun, btns, mi);
            htmls.append(si + sceneiBtn);
        }
        model.addAttribute("toolFun", toolFun.toString());
        model.addAttribute("opt", htmls.toString());
        model.addAttribute(NAME, "场景按钮组");
        // 获取场景数据，以及对应的按钮数据。
        // String querySceneBtn = "MATCH(s:Scene)-[r]->(b:layTableToolOpt) where s.code
        // IN ['"+String.join("','", codesList) +"'] return b";

        return "sceneOperate";
    }

    @RequestMapping(value = "/pre", method = {RequestMethod.GET, RequestMethod.POST})
    public String pre(Model model, HttpServletRequest request) throws Exception {
        String scene = goBack();
        Map<String, Object> sceneMap = neo4jService.getAttMapBy(CODE, scene, SCENE);

        ModelUtil.setKeyValue(model, sceneMap);
        // 获取场景数据，以及对应的按钮数据。
        String querySceneBtn = "MATCH(s:Scene)-[r]->(b:layTableToolOpt) where s.code='" + scene + "' return b";
        List<Map<String, Object>> query = neo4jService.cypher(querySceneBtn);

        showService.sceneBtn(model, query);
        // 获取场景描述：直接问问题，点击按钮
        // 获取关联场景

        // 场景数据加载：内容逻辑
        readContent(model, scene);
        // 获取元数据ID
        return "operation";
    }

    public String goBack() {
        SceneManager sceneManager = getSceneManager();
        String scene = sceneManager.toRight();
        return scene;
    }

    @RequestMapping(value = "/next", method = {RequestMethod.GET, RequestMethod.POST})
    public String next(Model model, HttpServletRequest request) throws Exception {
        String scene = goNext();
        Map<String, Object> sceneMap = neo4jService.getAttMapBy(CODE, scene, SCENE);
        ModelUtil.setKeyValue(model, sceneMap);
        // 获取场景数据，以及对应的按钮数据。
        String querySceneBtn = "MATCH(s:Scene)-[r]->(b:layTableToolOpt) where s.code='" + scene + "' return b";
        List<Map<String, Object>> query = neo4jService.cypher(querySceneBtn);

        showService.sceneBtn(model, query);

        // 获取场景描述：直接问问题，点击按钮
        // 获取关联场景

        // 场景数据加载：内容逻辑
        readContent(model, scene);
        // 获取元数据ID
        return "operation";
    }

    @RequestMapping(value = "/returnHomePage", method = {RequestMethod.GET, RequestMethod.POST})
    public String homePage(Model model, HttpServletRequest request) throws Exception {
        String scene = goToHomePage();

        Map<String, Object> sceneMap = neo4jService.getAttMapBy(CODE, scene, SCENE);
        ModelUtil.setKeyValue(model, sceneMap);
        // 获取场景数据，以及对应的按钮数据。
        String querySceneBtn = "MATCH(s:Scene)-[r]->(b:layTableToolOpt) where s.code='" + scene + "' return b";
        List<Map<String, Object>> query = neo4jService.cypher(querySceneBtn);

        showService.sceneBtn(model, query);

        // 获取场景描述：直接问问题，点击按钮
        // 获取关联场景

        // 场景数据加载：内容逻辑
        readContent(model, scene);
        // 获取元数据ID
        return "operation";
    }

    public String goNext() {
        SceneManager sceneManager = getSceneManager();
        String scene = sceneManager.toLeft();
        return scene;
    }

    public String goToHomePage() {
        SceneManager sceneManager = getSceneManager();
        return sceneManager.goToHomePage();
    }

    /**
     * 回退，前进，返回主页。
     *
     * @param model
     * @param scene
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{scene}", method = {RequestMethod.GET, RequestMethod.POST})
    public String oneScene(Model model, @PathVariable("scene") String scene, HttpServletRequest request)
            throws Exception {
        gotoScene(scene);
        Map<String, Object> sceneMap = neo4jService.getAttMapBy(CODE, scene, SCENE);
        ModelUtil.setKeyValue(model, sceneMap);

        // 获取场景数据，以及对应的按钮数据。
        String querySceneBtn = "MATCH(s:Scene)-[r]->(b:layTableToolOpt) where s.code='" + scene + "' return b";
        List<Map<String, Object>> query = neo4jService.cypher(querySceneBtn);

        showService.sceneBtn(model, query);

        // 获取场景描述：直接问问题，点击按钮
        // 获取关联场景

        // 场景数据加载：内容逻辑
        readContent(model, scene);
        // 获取元数据ID
        return "operation";
    }

    @RequestMapping(value = "/{scene}/{preScene}", method = {RequestMethod.GET, RequestMethod.POST})
    public String oneSceneWithParent(Model model, @PathVariable("scene") String scene,
                                     @PathVariable("preScene") String preScene, HttpServletRequest request) throws Exception {
        Map<String, Object> sceneMap = neo4jService.getAttMapBy(CODE, scene, SCENE);
//	Map<String, Object> preSceneMap = neo4jService.getAttMapBy(CODE, preScene, SCENE);
        SceneManager sceneManager = getSceneManager();
        String prex = sceneManager.preScene();
        if (prex == null || !prex.equals(preScene)) {
            gotoScene(preScene);
        }
        gotoScene(scene);
        ModelUtil.setKeyValue(model, sceneMap);
        // 获取场景数据，以及对应的按钮数据。
        String querySceneBtn = "MATCH(s:Scene)-[r]->(b:layTableToolOpt) where s.code='" + scene + "' return b";
        List<Map<String, Object>> query = neo4jService.cypher(querySceneBtn);

        showService.sceneBtn(model, query);

        // 获取场景描述：直接问问题，点击按钮
        // 获取关联场景

        // 场景数据加载：内容逻辑
        readContent(model, scene);

        return "operation";
    }

    public void gotoScene(String scene) {
        SceneManager sceneManager = getSceneManager();
        sceneManager.goToScene(scene);
    }

    public void containModule(String scene, StringBuilder sb) {

        String queryModuleBtn = "MATCH(s:Scene)-[r]->(b:module) where s.code='" + scene
                + "' return b order by b.createTime desc";
        List<Map<String, Object>> module = neo4jService.cypher(queryModuleBtn);
        if (module == null || module.isEmpty()) {
            return;
        }
        sb.append("""
                <fieldset class="layui-elem-field layui-field-title" style="margin-top: 30px;">
                  <legend>模块</legend>
                </fieldset>
                	    		""");
        int i = 0;
        for (Map<String, Object> mi : module) {

            if (url(mi) == null || "null".equals(url(mi) == null)) {
                continue;
            }
            if (i > 0) {
                sb.append(" 、");
            }
            sb.append(" <a href='javascript:;' onclick=\"openManage('模块:" + name(mi) + "','" + LemodoApplication.MODULE_NAME + "/module/" + label(mi)
                    + "')\">" + name(mi) + "</a>");
            i++;
        }
    }

    /**
     * 添加权限，只能读取自己有权限的元数据
     *
     * @param scene
     * @param sb
     */
    public void containMetaData(String scene, StringBuilder sb) {
        String queryMetaBtn = "MATCH(s:Scene)-[r]->(b:MetaData) where s.code='" + scene
                + "' return b order by b.createTime,b.updateTime desc";
        List<Map<String, Object>> mds = neo4jService.cypher(queryMetaBtn);
        if (mds != null && !mds.isEmpty()) {
            sb.append("""
                    <legend>管理（增删改查）</legend>
                        		""");
            int i = 0;
            for (Map<String, Object> mdi : mds) {

                if (label(mdi) == null || "null".equals(label(mdi) == null)) {
                    continue;
                }
                if (i > 0) {
                    sb.append(" 、");
                }
                sb.append(" <a href='javascript:;' onclick=\"openManage('管理:" + name(mdi) + "','" + LemodoApplication.MODULE_NAME + "/md/" + label(mdi)
                        + "')\">" + name(mdi) + "</a>");
                i++;
            }
        }
    }

    /**
     * 添加权限，只能读取自己有权限的资源
     *
     * @param scene
     * @param sb
     */
    public void containResource(String scene, StringBuilder sb) {
        String queryResourceBtn = "MATCH(s:Scene)-[r]->(b:resource) where s.code='" + scene + "' return b";
        List<Map<String, Object>> rscs = neo4jService.cypher(queryResourceBtn);
        if (rscs != null && !rscs.isEmpty()) {
            sb.append("""

                    	    	<fieldset class="layui-elem-field layui-field-title" style="margin-top: 30px;">
                      <legend>资源</legend>
                    </fieldset>
                    	    		    		""");
            int i = 0;
            for (Map<String, Object> ri : rscs) {

                if (url(ri) == null || "null".equals(url(ri) == null)) {
                    continue;
                }
                if (i > 0) {
                    sb.append(" 、");
                }
                String url = showService.validUrlPrefix(ri);

                sb.append(" <a href='javascript:;' onclick=\"openManage('资源:" + name(ri) + "','" + url + "')\">"
                        + name(ri) + "</a>");
                i++;
            }
        }
    }

    /**
     * 所有数据对象
     *
     * @param scene
     */
    public String containData(String scene) {
        StringBuilder sb = new StringBuilder();
        String queryResourceBtn = "MATCH(s:Scene)-[r]->(b) where s.code='" + scene + "' "
                + " and NOT (labels(b) IN ['" + META_DATA + "', 'resource', 'App', 'module']) return b";
        List<Map<String, Object>> rscs = neo4jService.cypher(queryResourceBtn);
        if (rscs != null && !rscs.isEmpty()) {
            sb.append("""
                    <legend> 数据</legend>
                    	    		    		""");
            int i = 0;
            for (Map<String, Object> ri : rscs) {
                if (i > 0) {
                    sb.append(" 、");
                }
                String link = LemodoApplication.MODULE_NAME + "/layui/" + label(ri) + "/" + id(ri) + "/documentRel";
//		 String seeNode = neo4jService.seeNode(ri);
                sb.append(" <a href='javascript:;' onclick=\"openManage('数据节点:" + name(ri) + "','" + link + "')\">"
                        + name(ri) + "</a>");
                i++;
            }
        }
        return sb.toString();
    }

    public void containDesktopApp(String scene, StringBuilder sb) {
        String queryResourceBtn = "MATCH(s:Scene)-[r]->(b:App) where s.code='" + scene + "' return b";
        List<Map<String, Object>> rscs = neo4jService.cypher(queryResourceBtn);
        if (rscs != null && !rscs.isEmpty()) {
            sb.append("""
                    	    		 <fieldset class="layui-elem-field layui-field-title" style="margin-top: 30px;">
                      <legend>桌面应用</legend>
                    </fieldset>
                    	    		    		""");
            int i = 0;
            for (Map<String, Object> ri : rscs) {
                if (i > 0) {
                    sb.append(" 、");
                }
                if (url(ri) == null || "null".equals(url(ri) == null)) {
                    continue;
                }
                String url = showService.validUrlPrefix(ri);
                sb.append(" <a href='javascript:;' onclick=\"openManage('应用：" + name(ri) + "','" + url + "')\">"
                        + name(ri) + "</a>");
                i++;
            }
        }
    }

    /**
     * 包含打开程序任务,则打开
     *
     * @param scene
     * @param sb
     */
    public void containOpenTask(String scene, StringBuilder sb) {
        String queryOpenTask = "MATCH(s:Scene)-[r]->(b:OpenTask) where s.code='" + scene + "' return b";
        List<Map<String, Object>> openTasks = neo4jService.cypher(queryOpenTask);
        ras.runApp(openTasks);
    }

    public void containProgram(String scene, StringBuilder sb) {
        String queryOpenTask = "MATCH(s:Scene)-[r]->(b:LocalProgram) where s.code='" + scene + "' return b";
        List<Map<String, Object>> openTasks = neo4jService.cypher(queryOpenTask);
        if (openTasks.size() > 0) {
            ras.runApp(String.join(",", idStrList(openTasks)));
        }
    }

    /**
     * 如果有路径,则打开
     *
     * @param scene
     * @param sb
     */
    public void containOpenDir(String scene, StringBuilder sb) {
        String queryOpenTask = "MATCH(s:Scene)-[r]->(d:dir) where s.code='" + scene + "' return d";
        List<Map<String, Object>> openDirs = neo4jService.cypher(queryOpenTask);
        ras.explore(openDirs);
    }

    public void containOpenFile(String scene, StringBuilder sb) {
        String queryOpenTask = "MATCH(s:Scene)-[r]->(d:File) where s.code='" + scene + "' return d";
        List<Map<String, Object>> openDirs = neo4jService.cypher(queryOpenTask);
        ras.explore(openDirs);
    }


    public void navigateScene(Model model, Map<String, Object> homePage, String scene) {

        if (homePage == null) {
//            return;
	    homePage= newMap();
	    homePage.put(NAME, "主页");
	    homePage.put(CODE, "homePage");
        }
        SceneManager sceneManager = getSceneManager();
        String previousScene = sceneManager.preScene();
        if (previousScene == null) {
            gotoScene("homePage");
        }
        if (previousScene != null) {
            Map<String, Object> preScene = neo4jService.getAttMapBy(CODE, previousScene, SCENE);
            if (preScene != null) {
                boolean equals = code(preScene).equals("homePage");
                if (!equals) {
                    preScene.put(NAME + "x", "上一个：" + name(preScene));
                    model.addAttribute("pre", addPreLink(preScene));
                }

            }
        }
        if(!scene.equals("homePage")){
            homePage.put(NAME + "x", "返回主页");
            model.addAttribute("homePage", returnHomePageLink(homePage));
        }


        String nextScene = sceneManager.nextScene();
        if (nextScene != null) {
            Map<String, Object> next = neo4jService.getAttMapBy(CODE, nextScene, SCENE);
            if (next != null) {
                next.put(NAME + "x", "下一个：" + name(next));
                model.addAttribute("next", addNextLink(next));
            }
        }
    }

    public SceneManager getSceneManager() {
        SceneManager sceneManager = sceneStack.get(adminService.getCurrentUserId());
        if (sceneManager == null) {
            sceneManager = new SceneManager();
            sceneStack.put(adminService.getCurrentUserId(), sceneManager);
        }
        return sceneManager;
    }

    public void addOneLink(Map<String, Object> scene, StringBuilder sb) {
        if (scene != null && !scene.isEmpty()) {
            sb.append(" <a href='javascript:;' onclick=\"openManage('场景：" + name(scene)
                    + "','" + LemodoApplication.MODULE_NAME + "/scene/" + code(scene)
                    + "')\">" + string(scene, NAME + "x") + "</a>");
        }
    }

    public String returnHomePageLink(Map<String, Object> scene) {
        StringBuilder sb = new StringBuilder();
        if (scene != null && !scene.isEmpty()) {
            sb.append(" <a href='javascript:;' onclick=\"openManage('" + name(scene)
                    + "','" + LemodoApplication.MODULE_NAME + "/scene/returnHomePage')\">" + string(scene, NAME + "x") + "</a>");
        }
        return sb.toString();
    }

    public String addPreLink(Map<String, Object> scene) {
        StringBuilder sb = new StringBuilder();
        if (scene != null && !scene.isEmpty()) {
            sb.append(" <a href='javascript:;' onclick=\"openManage('场景：" + name(scene)
                    + "','" + LemodoApplication.MODULE_NAME + "/scene/pre')\">" + string(scene, NAME + "x") + "</a>");
        }
        return sb.toString();
    }

    public String addNextLink(Map<String, Object> scene) {
        StringBuilder sb = new StringBuilder();
        if (scene != null && !scene.isEmpty()) {
            sb.append(" <a href='javascript:;' onclick=\"openManage('场景：" + name(scene)
                    + "','" + LemodoApplication.MODULE_NAME + "/scene/next')\">" + string(scene, NAME + "x") + "</a>");
        }
        return sb.toString();
    }

    /**
     * 添加权限，只能读取自己有权限的场景
     */
    public Map<String, Object> relateScene(String scene, StringBuilder sb) {
        Map<String, Object> hp = null;

        String queryRelateSceneBtn = "MATCH(s:Scene{code:'" + scene + "'})-[r]->(b:Scene) where b.code<>'" + scene + "'  AND EXISTS((b)<-[:HAS_PERMISSION]-(:Role)<-[:HAS_ROLE]-(:User {username: '" + adminService.getCurrentUserName() + "'})) return b,r order by b.createTime desc";
//	+ "' and b.code!='"+scene
        List<Map<String, Object>> scenes = neo4jService.cypher(queryRelateSceneBtn);
        if (scenes != null && !scenes.isEmpty()) {
            sb.append("""
                    <legend>场景</legend>
                    	    		""");
            int i = 0;
            for (Map<String, Object> mdi : scenes) {
                if (name(mdi).equals("主页") || code(mdi).equalsIgnoreCase("HomePage")) {
                    hp = mdi;
                    continue;
                }
                if (i > 0) {
                    sb.append(" 、");
                }
                sb.append(" <a href='javascript:;' onclick=\"openManage('场景：" + name(mdi) + "','" + LemodoApplication.MODULE_NAME + "/scene/"
                        + code(mdi) + "/" + scene + "')\">" + name(mdi) + "</a>");
                i++;
            }
        }
        return hp;
    }

    /**
     * 常用场景统计信息
     *
     * @param scene
     * @param sb
     * @return
     */
    public Map<String, Object> statisticScene(String scene, StringBuilder sb) {
        Map<String, Object> hp = null;
//	String queryUseScene = " MATCH (u:User)-[r:used]->(s:" + SCENE + ") " + "where id(u)="
//		+ adminService.getCurrentUserId() + " RETURN distinct s.name,s.code,r.inTime order by r.inTime desc Limit 9";
//	

        String preUse = " MATCH (n:" + SCENE_USE_COUNT + "), (s:" + SCENE + ") where n.user="
                + adminService.getCurrentUserId() + " and n.scene=s.code  RETURN  s.code,s.name,n.scene,n.inTime,n.num order by n.inTime desc Limit 9";
//	MATCH (n:SceneUseCount) ,(s:Scene) where n.user=4793 and n.scene=s.code
//		RETURN n.num,n.inTime,n.user,n.scene,s.name order by n.num,n.inTime desc Limit 9
//	 
        List<Map<String, Object>> scenes = neo4jService.cypher(preUse);
        if (scenes != null && !scenes.isEmpty()) {
            sb.append("""
                    <legend>最近使用</legend>
                    	    		""");
            int i = 0;
            for (Map<String, Object> si : scenes) {
                if (i > 0) {
                    sb.append(" 、");
                }
                if (!scene.equals(code(si))) {
                    sb.append(" <a href='javascript:;' onclick=\"openManage('场景：" + name(si) + "','" + LemodoApplication.MODULE_NAME + "/scene/" + code(si)
                            + "/" + scene + "')\">" + name(si) + "</a>");
                } else {
                    sb.append(" <label style=\"color:green\"> " + name(si) + "</label> ");
                }

                i++;
            }
        }

        String queryUseCount = " MATCH (n:" + SCENE_USE_COUNT + "), (s:" + SCENE + ") where n.user="
                + adminService.getCurrentUserId() + " and n.scene=s.code  RETURN  s.code,s.name,n.scene,n.inTime,n.num order by n.num desc Limit 9";
//	MATCH (n:SceneUseCount) ,(s:Scene) where n.user=4793 and n.scene=s.code
//		RETURN n.num,n.inTime,n.user,n.scene,s.name order by n.num,n.inTime desc Limit 9
//	 
        List<Map<String, Object>> scenesMost = neo4jService.cypher(queryUseCount);
        if (scenesMost != null && !scenesMost.isEmpty()) {
            sb.append("""
                    <legend>最多使用</legend>
                    	    		""");
            int i = 0;
            for (Map<String, Object> si : scenesMost) {
                if (i > 0) {
                    sb.append(" 、");
                }

                if (!scene.equals(code(si))) {
                    sb.append(" <a href='javascript:;' onclick=\"openManage('场景：" + name(si) + "','" + LemodoApplication.MODULE_NAME + "/scene/" + code(si)
                            + "/" + scene + "')\">" + name(si) + "</a>");
                } else {
                    sb.append(" <label style=\"color:green\"> " + name(si) + "</label> ");
                }

//		sb.append(" <a href='javascript:;' onclick=\"openManage('场景：" + name(si) + "','"+LemodoApplication.MODULE_NAME+"/scene/" + code(si)
//			+ "/" + scene + "')\">" + name(si) +  "</a>");
                i++;
            }
        }
        return hp;
    }

    public void readContent(Model model, String scene) throws EvalError {
        readBeanShell(model, scene);

        // 获取链接:资源，模块：/cd/module/，元数据管理。/cd/md/
        StringBuilder sbx = new StringBuilder();
        statisticScene(scene, sbx);
        Map<String, Object> homePage = relateScene(scene, sbx);

        navigateScene(model, homePage, scene);

        containModule(scene, sbx);
        containMetaData(scene, sbx);
        containResource(scene, sbx);
        containDesktopApp(scene, sbx);
        containOpenTask(scene, sbx);
        containProgram(scene, sbx);
        containOpenDir(scene, sbx);
        containOpenFile(scene, sbx);
        model.addAttribute("links", sbx.toString());
        //other Data
        model.addAttribute("dataObjects", containData(scene));

    }

    private void readBeanShell(Model model, String scene) throws EvalError{
        String queryContentBtn = "MATCH(s:Scene)-[r:content]->(b:BeanShell) where s.code='" +scene+ "' return b";
        StringBuilder sb = new StringBuilder();
        Interpreter in = new Interpreter();
        bss.init(in);
        List<Map<String, Object>> beanShells = neo4jService.cypher(queryContentBtn);
        // in.setStrictJava(true);
        if (!beanShells.isEmpty()) {
            for (Map<String, Object> beanShell : beanShells) {
                String string = string(beanShell, BS_SCRIPT);
                in.eval(string);
                Object returnValue = in.get("returnValue");
                if (!sb.isEmpty()) {
                    sb.append("\n");
                }
                sb.append(String.valueOf(returnValue));
            }

            model.addAttribute("content", sb.toString());
        }
    }

}
