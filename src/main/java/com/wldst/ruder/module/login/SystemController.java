package com.wldst.ruder.module.login;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.api.ResultCode;
import com.wldst.ruder.domain.AuthDomain;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.util.MapTool;

import jakarta.servlet.http.HttpServletRequest;

//import io.swagger.annotations.ApiOperation;

/**
 * 后台用户管理 Created by macro on 2018/4/26.
 */
@Controller
@RequestMapping("${server.context}/system/{plugin}")
public class SystemController extends AuthDomain {

    private UserAdminService adminService;
	@Autowired
    public SystemController(UserAdminService adminService) {
	this.adminService = adminService;
    }
    @Value("${server.port}")
    private String serverPort;
    
    // @ApiOperation(value = "用户注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    @ResponseBody
    public Result<Map<String, Object>> register(@PathVariable("plugin") String plugin,@RequestBody JSONObject umsAdminParam) {
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

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Result login(@PathVariable("plugin") String plugin,@Validated @RequestBody JSONObject umsAdminLoginParam, HttpServletRequest request) {
	String userName = umsAdminLoginParam.getString(USER_NAME);
	if (userName == null) {
	    userName = umsAdminLoginParam.getString(USER_EMAIL);
	}
	Result login = adminService.login(userName, umsAdminLoginParam.getString(PASSWORD));
	if (login.getCode() == ResultCode.SUCCESS.getCode()) {
	    request.getSession().setAttribute("loginName", userName);
	    return login.success("desktop/index", "登录成功");
	}
	return login.failed("登录失败！");
    }

    // @ApiOperation(value = "登录以后返回token")
    @RequestMapping(value = "/getToken", method = RequestMethod.POST)
    @ResponseBody
    public Result getToken(@PathVariable("plugin") String plugin,@Validated @RequestBody JSONObject umsAdminLoginParam) {
	return adminService.login(umsAdminLoginParam.getString("username"), umsAdminLoginParam.getString("password"));
    }

    // @ApiOperation(value = "获取当前登录用户信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    public Result getAdminInfo(@PathVariable("plugin") String plugin) {
	Map<String, Object> umsAdmin = adminService.getCurrentPassWord();
	Long userIdLong = Long.valueOf(String.valueOf(umsAdmin.get("id")));
	Map<String, Object> data = new HashMap<>();
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
    public Result<Map<String, Object>> getItem(@PathVariable("plugin") String plugin,@PathVariable Long id) {
	return Result.success(adminService.getItem(id));
    }

    // @ApiOperation("修改指定用户信息")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result update(@PathVariable("plugin") String plugin,@PathVariable Long id, @RequestBody Map<String, Object> admin) {
	int count = adminService.update(id, admin);
	if (count > 0) {
	    return Result.success(count);
	}
	return Result.failed();
    }

    // @ApiOperation("修改指定用户密码")
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    @ResponseBody
    public Result updatePassword(@PathVariable("plugin") String plugin,@RequestBody Map<String, Object> updatePasswordParam) {
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
    public Result delete(@PathVariable("plugin") String plugin,@PathVariable Long id) {
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
    public Result updateRole(@PathVariable("plugin") String plugin,@RequestParam("adminId") Long adminId,
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
    public Result<List<Map<String, Object>>> getRoleList(@PathVariable("plugin") String plugin,@PathVariable Long adminId) {
	List<Map<String, Object>> roleList = adminService.getRoleList(adminId);
	return Result.success(roleList);
    }

    // @ApiOperation("给用户分配+-权限")
    @RequestMapping(value = "/permission/update", method = RequestMethod.POST)
    @ResponseBody
    public Result updatePermission(@PathVariable("plugin") String plugin,@RequestParam Long adminId,
	    @RequestParam("permissionIds") List<Long> permissionIds) {
	int count = adminService.updatePermission(adminId, permissionIds);
	if (count > 0) {
	    return Result.success(count);
	}
	return Result.failed();
    }

    @RequestMapping(value = "/saveMySetting", method = RequestMethod.POST)
    @ResponseBody
    public Result saveMySetting(@PathVariable("plugin") String plugin,@RequestBody Map<String, Object> mySetting) {
	List<Long> mySetting2 = adminService.mySetting(mySetting);
	if (mySetting2.size() > 0) {
	    return Result.success(mySetting2.size());
	}
	return Result.failed();
    }

    // @ApiOperation("获取用户所有权限（包括+-权限）")
    @RequestMapping(value = "/permission/{adminId}", method = RequestMethod.GET)
    @ResponseBody
    public Result<List<Map<String, Object>>> getPermissionList(@PathVariable("plugin") String plugin,@PathVariable Long adminId) {
	List<Map<String, Object>> permissionList = adminService.getPermissionList(adminId);
	return Result.success(permissionList);
    }

    // @ApiOperation("根据用户名获取通用用户信息")
    @RequestMapping(value = "/loadByUsername", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> loadUserByUsername(@PathVariable("plugin") String plugin,@RequestParam String username) {
	Map<String, Object> userDTO = adminService.loadAccountByUsername(username);
	return userDTO;
    }

    @RequestMapping(value = "/getByName/{username}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getByName(@PathVariable("plugin") String plugin,@PathVariable String username) {
	Map<String, Object> userDTO = adminService.loadAccountByUsername(username);
	return userDTO;
    }

    // @ApiOperation("根据用户名获取通用用户信息")
    @RequestMapping(value = "/checkUser", method = RequestMethod.POST)
    @ResponseBody
    public Integer checkUser(@PathVariable("plugin") String plugin,@RequestParam String username) {
	Map<String, Object> userDTO = adminService.loadAccountByUsername(username);
	if (userDTO == null || userDTO.isEmpty()) {
	    return 1;
	}
	return 0;
    }
    
}
