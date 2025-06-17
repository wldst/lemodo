package com.wldst.ruder.module.manage;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.util.LoggerTool;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.api.ResultCode;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.AuthDomain;
import com.wldst.ruder.domain.DomainBuffer;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.MapTool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import io.swagger.annotations.ApiOperation;

/**
 * 后台用户管理 Created by macro on 2018/4/26.
 */
@Controller
@RequestMapping("${server.context}/adminctrl")
public class AdminUserController extends AuthDomain {
    private static Logger logger = LoggerFactory.getLogger(AdminUserController.class);
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private CrudUtil crudUtil;
    // @ApiOperation(value = "用户注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public Result<Map<String, Object>> register(@RequestBody JSONObject umsAdminParam) {
	String encodePwd = bcryptPasswordEncoder.encode(umsAdminParam.getString("password"));
	umsAdminParam.put("password", encodePwd);
	Map<String, Object> umsAdmin = adminService.register(umsAdminParam);
	if (umsAdmin == null) {
	    return Result.failed();
	}else  if(bool(umsAdmin,"exist")) {
	    return Result.fail(string(umsAdminParam, "username")+"已经存在");
	}

	umsAdmin.remove("password");
	return Result.success(umsAdmin);
    }
    
    @RequestMapping(value = "/modifyPassword", method = RequestMethod.POST)
    @ResponseBody
    public Result<String> modifyPassword(@RequestBody JSONObject umsAdminParam) {
	String encodePwd = bcryptPasswordEncoder.encode(umsAdminParam.getString("password"));
	umsAdminParam.put("password", encodePwd);
	int updatePassword = adminService.updatePassword(umsAdminParam);
	if (updatePassword == 0) {
	    return Result.failed();
	}
	if(updatePassword == 1) {
	    return Result.success(string(umsAdminParam, "username")+"修改密码成功");
	}
	return Result.success("");
    }
    
    

    @RequestMapping(value = "/login2", method = RequestMethod.POST)
    @ResponseBody
    public Result<Object> login2(Model model, @Validated @RequestBody JSONObject umsAdminLoginParam, HttpServletRequest request) {
	String userName = umsAdminLoginParam.getString(USER_NAME);
	if (userName == null) {
	    userName = umsAdminLoginParam.getString(USER_EMAIL);
	}
	//安全校验
	//跨站攻击
	Result login = adminService.login(userName, umsAdminLoginParam.getString(PASSWORD));
	if (login.getCode() == ResultCode.SUCCESS.getCode()) {
	    HttpSession mysession = request.getSession();
	    mysession.setAttribute("loginName", userName);
	    model.addAttribute("username", userName);
	    refreshSession(request, userName);
	    String id2 = mysession.getId();
	    sessionMap.put(id2, mysession);
	    Map<String, Object> currentUser = adminService.getCurrentUser();
	    currentUser.put("token",id2);
	    return login.success(currentUser);
	}
	return login.failed("登录失败！");
    }
    
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Result login(Model model, @Validated @RequestBody JSONObject umsAdminLoginParam, HttpServletRequest request) {
	String userName = umsAdminLoginParam.getString(USER_NAME);
	if (userName == null) {
	    userName = umsAdminLoginParam.getString(USER_EMAIL);
	}
	//安全校验
	//跨站攻击
	Result login = adminService.login(userName, umsAdminLoginParam.getString(PASSWORD));
	if (login.getCode() == ResultCode.SUCCESS.getCode()) {
	    HttpSession mysession = request.getSession();
	    mysession.setAttribute("loginName", userName);
	    model.addAttribute("username", userName);
	    refreshSession(request, userName);
	    sessionMap.put(mysession.getId(), mysession);
	    return login.success("desktop/index", "登录成功");
	}
	return login.failed("登录失败！");
    }

    public void refreshSessionId(HttpServletRequest request) {
	String userName =adminService.getCurrentUserName();
	refreshSession(request, userName);
    }
     
    public void refreshSession(HttpServletRequest request, String userName) {
	if(userName==null) {
	    return ;
	}
	LoggerTool.info(logger,"userName=={}",userName);
	Map<String,Object> data = newMap();
	data.put("userName", userName);
 //保存会话信息,保存之前，删除所有过期数据。
	adminService.endSession(userName);
	LoggerTool.info(logger,"after refreshSession userName=session={}",userName);
	data.put("sessionId", request.getSession().getId());
	data.put("createTime", DateTool.nowLong());
	data.put("accountId", adminService.getCurrentPasswordId());
	neo4jService.save(data, "Session");
	LoggerTool.info(logger,"after refreshSession userName=session={}",JSON.toJSONString(data));
	
    }

   
    

    // @ApiOperation(value = "登录以后返回token")
    @RequestMapping(value = "/getToken", method = RequestMethod.POST)
    @ResponseBody
    public Result getToken(@Validated @RequestBody JSONObject umsAdminLoginParam, HttpServletRequest request) {
	Result login = adminService.login(umsAdminLoginParam.getString("username"), umsAdminLoginParam.getString("password"));
	String id2 = request.getSession().getId();
	return login;
    }

    // @ApiOperation(value = "获取当前登录用户信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public Result getAdminInfo() {
	Map<String, Object> umsAdmin = adminService.getCurrentPassWord();
		Map<String, Object> data = new HashMap<>();
	if(umsAdmin==null||umsAdmin.isEmpty()){
		return Result.success(data);
	}
	Long userIdLong = Long.valueOf(String.valueOf(umsAdmin.get("id")));

	data.put(USER_NAME, umsAdmin.get(USER_NAME));
	data.put(MENUS, adminService.getMenuList(userIdLong));
	data.put("icon", umsAdmin.get("icon"));
	adminService.getOrgInfo(userIdLong);
	
	List<Map<String, Object>> roleList = adminService.getRoleList(userIdLong);
	if (roleList != null && !roleList.isEmpty()) {
	    List<String> roles = new ArrayList<>(roleList.size());
	    for (Map<String, Object> ri : roleList) {
		roles.add(String.valueOf(ri.get("name")));
	    }
	    data.put(ROLES, roles);
	}
	return Result.success(data);
    }
    
    @RequestMapping(value = "/menu", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Result menu() {
	Map<String, Object> umsAdmin = adminService.getCurrentPassWord();
	Long userIdLong = Long.valueOf(String.valueOf(umsAdmin.get("id")));
	  
	List<Map<String, Object>> menuList = adminService.getMenuList(userIdLong);
	 
	List<Map<String, Object>> listAllByLabel = neo4jService.listAllByLabel("PmisMenu");
	 
	return Result.success(listAllByLabel);
    }
    @RequestMapping(value = "/pmisMenu", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Result pmisMenu() {
//	Map<String, Object> umsAdmin = adminService.getCurrentPassWord();
//	Long userIdLong = Long.valueOf(String.valueOf(umsAdmin.get("id")));
//	  
//	List<Map<String, Object>> menuList = adminService.getMenuList(userIdLong);
//	 
//	Map<String, Object> tree = neo4jService.getWholeTree("PmisMenu");
		Map<String, Object> param = newMap();
		param.put("status", "use");
		List<Map<String, Object>> listAllByLabel = neo4jService.listChild("PmisMenu",param);
	
	return Result.success(listAllByLabel);
    }

    

    // @ApiOperation(value = "登出功能")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    @ResponseBody
    public Result logout(HttpServletRequest request) {
	String attribute = String.valueOf(request.getSession().getAttribute("loginName"));
	Map<String, Object> map = onlineSeMap.get(attribute);
	String string = MapTool.string(map, "userId");
	adminService.logout(string);
	onlineSeMap.remove(attribute);
	
	return Result.success(null);
    }

    /**
     * 注销登录
     *
     * @param request
     * @return
     */
    @RequestMapping("/loginout")
    public String loginOut(HttpServletRequest request) {
	String attribute = String.valueOf(request.getSession().getAttribute("loginName"));
	Map<String, Object> map = onlineSeMap.get(attribute);
	adminService.logout(MapTool.string(map, "username"));
	onlineSeMap.remove(attribute);
//	neo4jService.delete("userName", attribute,"Session");
//	request.getSession().invalidate();
	return "redirect:"+LemodoApplication.MODULE_NAME+"/login";
    }

    // @ApiOperation("根据用户名或姓名分页获取用户列表")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Result<List<Map<String, Object>>> list(
	    @RequestParam(value = "keyword", required = false) String keyword,
	    @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
	    @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
	List<Map<String, Object>> adminList = adminService.list(keyword, pageSize, pageNum);
	return Result.success(adminList);
    }

    // @ApiOperation("获取指定用户信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public Result<Map<String, Object>> getItem(@PathVariable Long id) {
	return Result.success(adminService.getItem(id));
    }

    // @ApiOperation("修改指定用户信息")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result update(@PathVariable Long id, @RequestBody Map<String, Object> admin) {
	int count = adminService.update(id, admin);
	if (count > 0) {
	    return Result.success(count);
	}
	return Result.failed();
    }

    // @ApiOperation("修改指定用户密码")
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @ResponseBody
    public Result updatePassword(@RequestBody Map<String, Object> updatePasswordParam) {
	int status = adminService.updatePassword(updatePasswordParam);
	if (status > 0) {
	    return Result.success(status);
	} else if (status == -1) {
	    return Result.failed("提交参数不合法");
	} else if (status == -2) {
	    return Result.failed("找不到该用户");
	} else if (status == -3) {
	    return Result.failed("旧密码错误");
	} else {
	    return Result.failed();
	}
    }
    

    // @ApiOperation("删除指定用户信息")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result delete(@PathVariable Long id) {
	int count = adminService.delete(id);
	if (count > 0) {
	    return Result.success(count);
	}
	return Result.failed();
    }

    // @ApiOperation("修改帐号状态")
    @RequestMapping(value = "/updateStatus/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result updateStatus(@PathVariable Long id, @RequestParam(value = "status") Integer status) {
	Map<String, Object> umsAdmin = new HashMap<String, Object>();
	umsAdmin.put("status", status);
	int count = adminService.update(id, umsAdmin);
	if (count > 0) {
	    return Result.success(count);
	}
	return Result.failed();
    }

    // @ApiOperation("给用户分配角色")
    @RequestMapping(value = "/role/update", method = RequestMethod.POST)
    @ResponseBody
    public Result updateRole(@RequestParam("adminId") Long adminId,
	    @RequestParam("roleIds") List<Long> roleIds) {
	int count = adminService.updateRole(adminId, roleIds);
	if (count >= 0) {
	    return Result.success(count);
	}
	return Result.failed();
    }

    // @ApiOperation("获取指定用户的角色")
    @RequestMapping(value = "/role/{adminId}", method = RequestMethod.GET)
    @ResponseBody
    public Result<List<Map<String, Object>>> getRoleList(@PathVariable Long adminId) {
	List<Map<String, Object>> roleList = adminService.getRoleList(adminId);
	return Result.success(roleList);
    }

    // @ApiOperation("给用户分配+-权限")
    @RequestMapping(value = "/permission/update", method = RequestMethod.POST)
    @ResponseBody
    public Result updatePermission(@RequestParam Long adminId,
	    @RequestParam("permissionIds") List<Long> permissionIds) {
	int count = adminService.updatePermission(adminId, permissionIds);
	if (count > 0) {
	    return Result.success(count);
	}
	return Result.failed();
    }

    @RequestMapping(value = "/saveMySetting", method = RequestMethod.POST)
    @ResponseBody
    public Result saveMySetting(@RequestBody Map<String, Object> mySetting) {
	List<Long> mySetting2 = adminService.mySetting(mySetting);
	if (mySetting2.size() > 0) {
	    return Result.success(mySetting2.size());
	}
	return Result.failed();
    }

    // @ApiOperation("获取用户所有权限（包括+-权限）")
    @RequestMapping(value = "/permission/{adminId}", method = RequestMethod.GET)
    @ResponseBody
    public Result<List<Map<String, Object>>> getPermissionList(@PathVariable Long adminId) {
	List<Map<String, Object>> permissionList = adminService.getPermissionList(adminId);
	return Result.success(permissionList);
    }
    
    @RequestMapping(value = "/clearSession", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public Result<String> clearSession() {
	List<Map<String, Object>> sessionList = neo4jService.listAllByLabel(SESSION);
	for(Map<String, Object> si: sessionList) {
	    Long longValue = longValue(si,"createTime");
	    if(DateTool.over30m(longValue)) {
		neo4jService.delete(id(si));
	    }
	}
	return Result.success();
    }
    
    @RequestMapping(value = "/showPath/{startId}/{endId}", method = RequestMethod.GET)
    @ResponseBody
    public Result<String> showPath(@PathVariable Long startId,@PathVariable Long endId) {
	 String showPathInfo = adminService.showPathInfo(startId, endId);
	return Result.success(showPathInfo);
    }

    // @ApiOperation("根据用户名获取通用用户信息")
    @RequestMapping(value = "/loadByUsername", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> loadUserByUsername(@RequestParam String username) {
	Map<String, Object> userDTO = adminService.loadAccountByUsername(username);
	return userDTO;
    }

    @RequestMapping(value = "/getByName/{username}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getByName(@PathVariable String username) {
	Map<String, Object> userDTO = adminService.loadAccountByUsername(username);
	return userDTO;
    }

    // @ApiOperation("根据用户名获取通用用户信息")
    @RequestMapping(value = "/checkUser", method = RequestMethod.POST)
    @ResponseBody
    public Integer checkUser(@RequestParam String username) {
	Map<String, Object> userDTO = adminService.loadAccountByUsername(username);
	if (userDTO == null || userDTO.isEmpty()) {
	    return 1;
	}
	return 0;
    }
    
    @RequestMapping(value = "/tailLog", method = RequestMethod.GET)
    public String database(Model model,HttpServletRequest request)throws Exception{
	model.addAttribute("server",request.getServerName()+":"+serverPort);
        return "/websocket/tailLog";
    }
    
    @RequestMapping(value = "/monitor", method = RequestMethod.GET)
    public String monitor(Model model,String table,HttpServletRequest request)throws Exception{
		/*
		 * List<String> databases =oisGenerator.showDataBases();
		 * model.addAttribute("list",databases);
		 */
        model.addAttribute("server",request.getServerName()+":"+serverPort);
        return "/websocket/tailLog";
    }
    
    @RequestMapping(value = "/refreshCache", method = { RequestMethod.GET })
    @ResponseBody
    public Result refreshCache(Model model, HttpServletRequest request)
	    throws Exception {
	DomainBuffer.clear();
	return Result.failed("刷新成功！");
    }
}
