package com.wldst.ruder.module.manage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.util.LoggerTool;
import com.wldst.ruder.util.ModelUtil;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSON;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.domain.UserSpaceDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.fun.Neo4jOptCypher;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 首页
 *
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}")
public class WelcomeController extends UserSpaceDomain {
    private static Logger logger = LoggerFactory.getLogger(WelcomeController.class);
    @Autowired
    private CrudUserNeo4jService userDataService;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private RuleDomain rule;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private HtmlShowService showService;

    @GetMapping("/")
    public String index() {
        return "layui/login";
    }

    @RequestMapping(value = "/admin", method = {RequestMethod.GET, RequestMethod.POST})
    public String admin(Model model, String table, HttpServletRequest request) throws Exception {
        model.addAttribute("adminTitle", "管理系统自定义系统");
        return "admin";
    }

    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
    public String login(Model model, String table, HttpServletRequest request) throws Exception {
        model.addAttribute("adminTitle", "管理系统自定义系统");
        return "layui/login";
    }

    @RequestMapping(value = "/register", method = {RequestMethod.GET, RequestMethod.POST})
    public String register(Model model, String table, HttpServletRequest request) throws Exception {
        model.addAttribute("adminTitle", "管理系统自定义系统");
        return "layui/register";
    }

    /**
     * 处理管理员信息请求
     *
     * @param model 用于添加模型属性的对象
     * @param table 请求参数中的表名
     * @param request HTTP请求对象，用于获取会话信息
     * @return 返回包含管理员信息的结果对象
     * @throws Exception 可能抛出的异常
     */
    @RequestMapping(value = "/admin/info", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public Result adminInfo(Model model, String table, HttpServletRequest request) throws Exception {
        // 创建一个映射对象用于存储管理员信息
        Map<String, Object> dataMap = new HashMap<>();
        // 获取当前请求的会话
        HttpSession mysession = request.getSession();
        // 从会话中获取登录名称属性
        String attribute = String.valueOf(mysession.getAttribute("loginName"));
        // 从在线会话映射中获取当前用户的会话信息
        Map<String, Object> sessionMap = onlineSeMap.get(attribute);
        // 将当前会话添加到会话映射中
        sessionMap.put(mysession.getId(), mysession);
        // 如果会话映射为空，则返回失败结果，提示用户登录
        if (sessionMap == null) {
            return Result.failed("请登录");
        }
        // 从会话映射中获取用户ID
        Object obj = sessionMap.get(ID);
        // 获取用户数据列表
        List<Map<String, Object>> appList = userDataService.listDataByLabel("App");

        // 获取当前管理员账户名
        String myName = adminService.getCurrentAccount();
        // 初始化管理员标志为false
        boolean isAdmin = false;
        // 如果用户ID不为空
        if (obj != null) {
            Long userId = Long.valueOf(String.valueOf(obj));

            // 获取用户的权限列表并存入会话映射
            List<Map<String, Object>> permissionList = adminService.myPermissionList();
            sessionMap.put("permission", permissionList);
            // 获取用户的角色列表
            List<Map<String, Object>> roleList = adminService.getRoleList(userId);

            // 遍历角色列表，检查是否有管理员角色
            if (!roleList.isEmpty()) {
                for (Map<String, Object> ri : roleList) {
                    Map<String, Object> mapObject = mapObject(ri, RELATION_ENDNODE_PROP);
                    String string = code(mapObject);
                    if (string == null) {
                        continue;
                    }
                    String lowerCaseRoleCode = string.toLowerCase();
                    if (lowerCaseRoleCode.endsWith("admin")) {
                        isAdmin = true;
                    }
                }
            }

            // 将角色列表添加到数据映射中
            dataMap.put("roles", roleList);
            // 获取用户的菜单列表并添加到数据映射中
            List<Map<String, Object>> menuList = adminService.getMenuList(userId);
            dataMap.put("menus", menuList);
        }

        // 如果不是管理员，添加默认APP
        if (!isAdmin) {
            List<Map<String, Object>> defaultApp = adminService.getDefaultApp();
            Set<String> appIdList = collectAppId(defaultApp);
        }
        // 创建一个映射对象用于存储APP信息
        Map<String, Map<String, Object>> appMap = new HashMap<>();

        // 遍历APP列表，添加到APP映射中
        for (Map<String, Object> appi : appList) {
            appi.put("width", "");
            appi.put("height", "");
            if (appi.containsKey("appid")) {
                String object = (String) appi.get("appid");
                appMap.put(object, appi);
            }
        }
        // 将会话映射的内容添加到数据映射中
        dataMap.putAll(sessionMap);
        // 将APP映射添加到数据映射中
        dataMap.put("apps", appMap);
        // 添加管理员账户名到模型中
        model.addAttribute("myName", myName);
        // 根据用户，获取当前用户的桌面信息并添加到模型中
        model.addAttribute("desktpData", JSON.toJSON(dataMap));
        // 获取用户的设置列表并添加到模型中
        List<Map<String, Object>> mySettingList = adminService.mySettingList();
        if (mySettingList != null) {
            model.addAttribute(MY_SETTING, JSON.toJSON(mySettingList));
        }
        // 返回成功结果，包含数据映射
        return Result.success(dataMap);
    }

    @RequestMapping(value = "/desktop", method = {RequestMethod.GET, RequestMethod.POST})
    public String desktop(Model model, String table, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> dataMap = new HashMap<>();
        String attribute = String.valueOf(request.getSession().getAttribute("loginName"));
        Map<String, Object> userPasswordInfo = onlineSeMap.get(attribute);
        if (userPasswordInfo == null) {
            return "layui/login";
        }
        Object pwdId = userPasswordInfo.get(ID);
        List<Map<String, Object>> appList = userDataService.listDataByLabel("App");

        String myName = adminService.getCurrentAccount();
        String cnName = adminService.getCurrentName();
        String myDesktopName = "deskTop_" + myName;
        List<Map<String, Object>> accessibleDeskTops = null;
        List<Map<String, Object>> accessibleAppList = null;

        boolean isAdmin = false;
        boolean isContainMyDesktop = false;
        boolean onlyPhoneUser = false;
        String roleCode = null;
        boolean oneRole = false;
        if (pwdId != null) {
            Long userPwdId = Long.valueOf(String.valueOf(pwdId));

            accessibleDeskTops = userDataService.getPathEnds(USER_ACCOUNT, userPwdId, DESKTOP);
            accessibleAppList = userDataService.getPathEnds(USER_ACCOUNT, userPwdId, DeskTop_APP);

            List<Map<String, Object>> permissionList = adminService.myPermissionList();
            List<Map<String, Object>> roleList = adminService.getRoleList(userPwdId);

            Map<String, Object> buyInfo = adminService.getBuyInfo();
            Long longValue = longValue(buyInfo, "memberId");
            if (buyInfo != null && longValue != null) {
                Map<String, Object> memberData = neo4jService.getNodeMapById(longValue);
//	    Long nodeId = neo4jService.getNodeId("name", "免费会员", "Member");
                String memberName = name(memberData);
                if ("免费会员".equals(memberName)) {
                    //提醒免费试用情况
                    neo4jService.execute("MATCH (a:Password)-[r]->(n:Notice) where a.username='" + myName + "' and n.name='原创提醒' delete r");
                }
                if ("终身会员".equals(memberName)) {
                    //提醒免费试用情况
//		neo4jService.execute("MATCH (a:Password)-[r]->(n:Notice) where a.username='"+myName+"' and n.name='原创提醒' delete r");
                }
            } else {
                neo4jService.execute("MATCH (a:Password)-[r]->(n:Notice) where a.username='" + myName + "' and n.name='原创提醒' delete r");
            }


            userPasswordInfo.put("permission", permissionList);

            if (!roleList.isEmpty()) {
                model.addAttribute("role", roleList);
                Map<String, Object> anObject = roleList.get(0);
                roleCode = code(anObject);
                LoggerTool.info(logger, "==========rolecode==================" + roleCode);
                oneRole = roleList.size() == 1;
                onlyPhoneUser = oneRole && ROLE_USER_PHONE.equals(roleCode);
                model.addAttribute("onlyPhoneUser", onlyPhoneUser);

                for (Map<String, Object> ri : roleList) {
                    String string = code(ri);
                    if (string == null) {
                        continue;
                    }
                    String lowerCaseRoleCode = string.toLowerCase();
                    if (lowerCaseRoleCode.endsWith("admin")) {
                        isAdmin = true;
                        break;
                    }
                }
            }


            userPasswordInfo.put("role", roleList);
            List<Map<String, Object>> menuList = adminService.getMenuList(userPwdId);
            userPasswordInfo.put("menu", menuList);

            isContainMyDesktop = isContainMyDesktop(accessibleDeskTops, myDesktopName);
            //第一次登陆系统，还没有桌面，则创建相应的桌面
            if (accessibleDeskTops == null || accessibleDeskTops.isEmpty() || !isContainMyDesktop) {
                Map<String, Object> priMap = new HashMap<>();
                priMap.put("menuid", myDesktopName);
                priMap.put(NAME, myName + "的桌面");
                Node deskTopNodei = userDataService.saveByBody(priMap, DESKTOP);
                if (onlyPhoneUser) {
                    userDataService.initDesktopApp(deskTopNodei, ROLE_USER_PHONE);
                } else {
                    userDataService.initAppOfDesktop(deskTopNodei);
                }

                String createRelation = Neo4jOptCypher.createRelation(DESKTOP, "默认桌面", userPwdId, deskTopNodei.getId());
                userDataService.execute(createRelation);
                accessibleDeskTops = userDataService.getPathEnds(userPwdId, DESKTOP);
                accessibleAppList = userDataService.getPathEnds(userPwdId, DeskTop_APP);
            } else if (accessibleAppList.isEmpty()) {
                accessibleAppList = userDataService.getPathEnds(userPwdId, DeskTop_APP);
            }

            if (!onlyPhoneUser) {
                //根据人员权限获取开始菜单

                Map<String, Object> startupMenu = userDataService.getAttMapBy(NAME, "opening", DESKTOP);
                accessibleDeskTops.add(startupMenu);
            }
        }

        if (accessibleDeskTops != null) {
            for (Map<String, Object> mi : accessibleDeskTops) {
                rule.formateQueryField(mi);
            }
        }
        // myDeskTopList = neo4jService.listDataBy("creator", myName, "Desktop");

        // if (myName.equals("liuqiang")) {
        // myDeskTopList.addAll(deskTopList);
        // }

        if (!isContainMyDesktop) {
            Map<String, Object> myDesktop = userDataService.getAttMapBy("menuid", myDesktopName, DESKTOP);
            accessibleDeskTops.add(myDesktop);
        }


        List<Map<String, Object>> distinctDeskTops = dinstinctDesktop(accessibleDeskTops);

        appendApp(distinctDeskTops);
        Map<String, Object> myDesktop = getMyDesktop(myDesktopName, distinctDeskTops);

        if (!isAdmin && !onlyPhoneUser) {// 添加默认APP
            List<Map<String, Object>> defaultApp = adminService.getDefaultApp();
            Set<String> appIdList = collectAppId(defaultApp);
            List<String> appObj = arrayList(myDesktop, "app");
            if (appObj == null) {//添加默认应用
                myDesktop.put("app", appIdList);
            } else {
                appIdList.addAll(appObj);
                myDesktop.put("app", appIdList);
            }
        }
        //将可访问的App都放到当前用户的桌面上
//	if (myDesktop != null && !myDesktop.containsKey("app")) {
//	    Set<String> appIdSet = new HashSet<String>();
//	    for (Map<String, Object> ai : accessibleAppList) {
//		collectAppId(appIdSet, ai);
//	    }
//	    setDesktopApp(appIdSet, myDesktop);
//	}
        Map<String, Map<String, Object>> appMap = new HashMap<>();

        // if(!myAppList.isEmpty()) {
        // appList=myAppList;
        // }
        if (appList != null && !appList.isEmpty()) {
            for (Map<String, Object> appi : appList) {
                appi.put("width", "");
                appi.put("height", "");
                if (appi.containsKey("appid")) {
                    String object = (String) appi.get("appid");
                    appMap.put(object, appi);
                }
            }
        }

        dataMap.put("menu", distinctDeskTops);
        for (String ki : appMap.keySet()) {
            //context前缀替换与维护，桌面应用，模块，资源，菜单，右键菜单
            Map<String, Object> app = appMap.get(ki);
//		String ux = url(app);
            String ux = showService.validUrlPrefix(app);
            if (ux != null && ux.startsWith("/cd")&&!LemodoApplication.MODULE_NAME.equals("/cd")) {
                Map<String, Object> copa = copy(app);
                app.put("url", LemodoApplication.MODULE_NAME + ux.substring(3));
                copa.put("url", "${MODULE_NAME}" + ux.substring(3));
                userDataService.saveByBody(copa, "App");
            }
            if (ux != null && ux.startsWith("${MODULE_NAME}")) {
                app.put("url", LemodoApplication.MODULE_NAME + ux.substring(14));
            }
        }

        dataMap.put("apps", appMap);
        model.addAttribute("myName", cnName);
        model.addAttribute("userId", myName);
        //右键读取，去哪里读？
        model.addAttribute("contextMenu", JSON.toJSON(dataMap));

        model.addAttribute("rightMenu", JSON.toJSON(dataMap));

        // 根据用户，获取当前用户的桌面信息
        model.addAttribute("desktpData", JSON.toJSON(dataMap));
        List<Map<String, Object>> mySettingList = adminService.mySettingList();
        if (mySettingList != null) {
            model.addAttribute(MY_SETTING, JSON.toJSON(mySettingList));
        }

        String homePage = "Match(n:UIPlugin)-[r:homePage]->(ro:Role) where ro.code=\"" + roleCode + "\" return n.code";
        List<Map<String, Object>> query = userDataService.cypher(homePage);
        //读取主页
        if (onlyPhoneUser && query != null && !query.isEmpty()) {
            //读取主页
            response.sendRedirect("/lemodo/" + code(query.get(0)) + "/index.html");
//	    return "desktop/phoneUser";
        }
        return "desktop/index";
    }

    private List<Map<String, Object>> dinstinctDesktop(List<Map<String, Object>> accessibleDeskTops) {
        Set<String> desktopSet = new HashSet<>();
        List<Map<String, Object>> distinctDeskTops = new ArrayList<>(accessibleDeskTops.size());
        for (Map<String, Object> di : accessibleDeskTops) {
            String nameAndMenuId = name(di) + string(di, MENU_ID);
            if (!desktopSet.contains(nameAndMenuId)) {
                distinctDeskTops.add(di);
                desktopSet.add(nameAndMenuId);
            }
        }
        return distinctDeskTops;
    }

    private Set<String> collectAppId(List<Map<String, Object>> defaultApp) {
        Set<String> appIdList = new HashSet<String>();
        for (Map<String, Object> ai : defaultApp) {
            Map<String, Object> appiMap = mapObject(ai, "properties");
            collectAppId(appIdList, appiMap);
        }
        return appIdList;
    }

    private Map<String, Object> getMyDesktop(String myDesktopName, List<Map<String, Object>> myDeskTopList) {
        Map<String, Object> myDesktop = null;
        for (Map<String, Object> deski : myDeskTopList) {
            String string = string(deski, "menuid");
            if (myDesktopName.equals(string)) {
                myDesktop = deski;
                break;
            }
        }
        return myDesktop;
    }

    private void collectAppId(Set<String> appIdList, Map<String, Object> appiMap) {
        if (appiMap == null) {
            return;
        }
        String string = string(appiMap, "appid");
        if (string != null) {
            // myAppList.add(appiMap);
            appIdList.add(string);
        }
    }

    private void setDesktopApp(Set<String> appIdList, Map<String, Object> deski) {
        List<String> object = arrayList(deski, "app");
        if (object != null) {
            appIdList.addAll(object);
        }
        deski.put("app", appIdList);
    }

    private boolean isContainMyDesktop(List<Map<String, Object>> myDeskTopList, String myDesktopName) {
        boolean hasMyDesktop = false;
        if (myDeskTopList != null && !myDeskTopList.isEmpty()) {
            for (Map<String, Object> myi : myDeskTopList) {
                String deskName = string(myi, "menuid");
                if (deskName.equals(myDesktopName)) {
                    hasMyDesktop = true;
                }
            }
        }
        return hasMyDesktop;
    }

    /**
     * 获取拥有的桌面所包含的应用
     *
     * @param deskTopList
     */
    private void appendApp(List<Map<String, Object>> deskTopList) {
        if (deskTopList == null || deskTopList.isEmpty()) {
            return;
        }
        for (Map<String, Object> deski : deskTopList) {
            if (deski == null) {
                continue;
            }
            String desktopHasApp = "match (a:Desktop)-[r0]->(e0:App) where id(a)= " + deski.get("id")
                    + " return distinct e0.appid";
            List<Map<String, Object>> query = userDataService.cypher(desktopHasApp);
            List<String> appIdList = new ArrayList<String>(query.size());

            for (Map<String, Object> dMap : query) {
                String appId = (String) dMap.get("e0.appid");
                if(!appIdList.contains(appId)){
                    appIdList.add(appId);
                }
            }
            if (!appIdList.isEmpty()) {
                deski.put("app", appIdList);
            }
        }
    }

    // 映射到 "/domain/{di}" 的请求，支持 GET 和 POST 方法，产生 JSON 数据，字符集为 UTF-8
    @RequestMapping(value = "/domain/{di}", method = {RequestMethod.GET,
            RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String domain(Model model, @PathVariable("di") String di, HttpServletRequest request) throws Exception {
        // 添加属性到模型，用于显示管理系统标题
        model.addAttribute("adminTitle", "管理系统自定义系统");
        // 根据领域获取菜单
        Map<String, String> data = new HashMap<>();
        data.put("di", di);
        // 定义需要查询的菜单属性
        String[] columns = {NODE_ID, NODE_NAME, "href"};
        // 获取菜单树，只包含指定的列
        Map<String, Object> tree = userDataService.getWholeTreeWithColumn("Menu", columns);
        // 创建字符串构建器，用于拼接 HTML 代码
        StringBuilder sb = new StringBuilder();
        // 获取菜单树的第一级子菜单列表
        List<Map<String, Object>> child1List = (List<Map<String, Object>>) tree.get(REL_TYPE_CHILDREN);
        if (child1List != null && !child1List.isEmpty()) {
            // 标记是否为第一个父菜单
            Boolean firstParent = true;
            // 遍历第一级子菜单
            for (Map<String, Object> ci : child1List) {
                Object childrensObject = ci.get(REL_TYPE_CHILDREN);
                // 如果当前菜单项有子菜单，则进行拼接
                if (childrensObject != null) {
                    sb.append("<li class=\"layui-nav-item");
                    if (firstParent) {
                        sb.append(" layui-nav-itemed ");
                    }
                    sb.append(" \"><a href=\"javascript:;\">" + ci.get("name") + "</a>");
                    firstParent = false;
                    sb.append("<dl class=\"layui-nav-child\">");
                    List<Map<String, Object>> child2List = (List<Map<String, Object>>) childrensObject;
                    // 遍历第二级子菜单
                    for (Map<String, Object> c2i : child2List) {
                        List<Map<String, Object>> outRelations = userDataService.getRelationOneList(c2i, "Menu", "module");
                        if (outRelations != null && !outRelations.isEmpty()) {
                            Map<String, Object> module = (Map<String, Object>) outRelations.get(0)
                                    .get("endNodeProperties");
                            addDDModule(sb, module);
                        } else {
                            adDD(sb, c2i);
                        }
                    }
                    sb.append("</dl>");
                } else {
                    addLi(sb, ci);
                }
                sb.append("</li>");
            }
        }
        // 返回拼接好的 HTML 字符串
        return sb.toString();
    }

    private void addDDModule(StringBuilder sb, Map<String, Object> data) {
        sb.append("<dd><a href=\"javascript:;\" onclick=\"openDomain(" + data.get("id") + ",'" + data.get("name")
                + "','" + LemodoApplication.MODULE_NAME + "/module/" + data.get(LABEL) + "Div')\">" + data.get("name") + "</a></dd>");
    }

    private void adDD(StringBuilder sb, Map<String, Object> data) {
        sb.append("<dd><a href=\"javascript:;\" onclick=\"openDomain(" + data.get("id") + ",'" + data.get("name")
                + "','" + data.get("href") + "')\">" + data.get("name") + "</a></dd>");
    }

    private void addLi(StringBuilder sb, Map<String, Object> ci) {
        sb.append("<li class=\"layui-nav-item\"><a href=\"javascript:;\"  onclick=\"openDomain(" + ci.get("id") + ",'"
                + ci.get("name") + "','" + ci.get("href") + "')\">" + ci.get("name") + "</a>");
    }


    /**
     *
     *
     * @param model
     * @param table
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/admin2", method = {RequestMethod.GET, RequestMethod.POST})
    public String admin2(Model model, String table, HttpServletRequest request) throws Exception {
        // 添加属性adminTitle到模型，用于显示页面标题
        model.addAttribute("adminTitle", "管理系统自定义系统");

        // 获取标记为"Domain"的数据列表，并添加到模型中
        List<Map<String, Object>> listDataByLabel = userDataService.listDataByLabel("Domain");
        model.addAttribute("domains", listDataByLabel);

        // 定义列数组，用于获取和显示菜单树
        String[] columns = {NODE_ID, NODE_NAME, "href", PARENT_ID};

        // 获取整个菜单树结构
        Map<String, Object> tree = userDataService.getWholeTree("Menu");
        StringBuilder sb = new StringBuilder();

        // 获取菜单树的第一级子节点列表
        List<Map<String, Object>> child1List = (List<Map<String, Object>>) tree.get(REL_TYPE_CHILDREN);

        // 检查第一级子节点列表是否非空，如果非空则进行树结构的刷新
        if (child1List != null && !child1List.isEmpty()) {
            Boolean treeChange = false;
            treeChange = refreshTree("Menu", tree, child1List, treeChange);

            // 如果树结构发生变化，重新获取包含指定列的树结构
            if (treeChange) {
                tree = userDataService.getWholeTreeWithColumn("Menu", columns);
                child1List = (List<Map<String, Object>>) tree.get(REL_TYPE_CHILDREN);
            }
        } else {
            // 如果第一级子节点列表为空，记录日志信息
            LoggerTool.info(logger, "====child1List.isEmpty============");
        }

        // 再次检查第一级子节点列表是否非空，如果非空则生成左侧菜单HTML
        if (child1List != null && !child1List.isEmpty()) {
            Boolean firstParent = true;
            for (Map<String, Object> ci : child1List) {
                Object childrensObject = ci.get(REL_TYPE_CHILDREN);
                if (childrensObject != null) {
                    // 为每个子节点生成<li>元素和对应的子菜单
                    sb.append("<li class=\"layui-nav-item");
                    if (firstParent) {
                        sb.append(" layui-nav-itemed ");
                    }
                    sb.append(" \"><a href=\"javascript:;\">" + ci.get("name") + "</a>");
                    firstParent = false;
                    sb.append("<dl class=\"layui-nav-child\">");

                    List<Map<String, Object>> child2List = (List<Map<String, Object>>) childrensObject;
                    for (Map<String, Object> c2i : child2List) {
                        List<Map<String, Object>> outRelations = userDataService.getRelationOneList(c2i, "Menu", "module");
                        if (outRelations != null && !outRelations.isEmpty()) {
                            // 如果存在模块关系，添加模块到子菜单
                            Map<String, Object> module = (Map<String, Object>) outRelations.get(0)
                                    .get("endNodeProperties");
                            addDDModule(sb, module);
                        } else {
                            // 如果不存在模块关系，直接添加菜单项
                            adDD(sb, c2i);
                        }
                    }
                    sb.append("</dl>");
                } else {
                    // 如果子节点没有子菜单，直接添加菜单项
                    addLi(sb, ci);
                }
                sb.append("</li>");
            }
        }

        // 将生成的左侧菜单HTML添加到模型中
        model.addAttribute("menuLeft", sb.toString());

        // 返回admin2视图
        return "admin2";
    }

    private Boolean refreshTree(String label, Map<String, Object> tree, List<Map<String, Object>> child1List, Boolean treeChange) {
        LoggerTool.info(logger, "=====id(tree)=" + id(tree) + "=========refreshTree ====\n====treeChange=" + treeChange + "======" + mapString(tree) + "==========");
        for (Map<String, Object> ci : child1List) {
            Object childrensObject = ci.get(REL_TYPE_CHILDREN);
            Long parentIdData = parentId(ci);
            Long nowParentId = id(tree);
            if (nowParentId != null && parentIdData != null && !nowParentId.equals(parentIdData)) {
                treeChange = true;
                userDataService.execute("match (n:" + label + ")  where id(n)=" + id(ci) + " set n.parentId=" + nowParentId);
            }
            if (childrensObject != null) {
                List<Map<String, Object>> child2List = (List<Map<String, Object>>) childrensObject;
                refreshTree(label, ci, child2List, treeChange);
            }
        }
        return treeChange;
    }

    @RequestMapping(value = "/editor", method = {RequestMethod.GET, RequestMethod.POST})
    public String editor(Model model, String table, HttpServletRequest request) throws Exception {
        return "mxgraph/editor";
    }

    @RequestMapping(value = "/card", method = {RequestMethod.GET, RequestMethod.POST})
    public String card(Model model, String table, HttpServletRequest request) throws Exception {
        return "layui/design/index";
    }

    @RequestMapping(value = "/home", method = {RequestMethod.GET, RequestMethod.POST})
    public String home(Model model, String table, HttpServletRequest request) throws Exception {
        return "layui/home";
    }

    /**
     * 上传
     *
     * @param model
     * @param label
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/fileUpload", method = {RequestMethod.GET, RequestMethod.POST})
    public String fileUpload(Model model, @PathVariable("po") String label, HttpServletRequest request)
            throws Exception {
        Map<String, Object> po = userDataService.getAttMapBy(LABEL, label, META_DATA);
        if (po == null || po.isEmpty()) {
            throw new DefineException(label + "未定义！");
        }
        ModelUtil.setKeyValue(model, po);
//	st.createHtml("layui/viewDefine", model.asMap());
        return "layui/viewDefine";
    }


    @RequestMapping(value = "/notice", method = {RequestMethod.GET, RequestMethod.POST})
    public String notice(Model model, HttpServletRequest request) throws Exception {
        return "layui/notice/index";
    }
}
