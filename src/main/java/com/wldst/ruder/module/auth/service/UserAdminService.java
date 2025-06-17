package com.wldst.ruder.module.auth.service;

import java.util.List;
import java.util.Map;

import com.wldst.ruder.exception.AuthException;
import com.wldst.ruder.exception.DefineException;
import org.springframework.transaction.annotation.Transactional;

import com.wldst.ruder.api.Result;

/**
 * 后台管理员Service Created by macro on 2018/4/26.
 */
public interface UserAdminService {
    
    public void endSession(String userName);
    /**
     * 根据用户名获取后台管理员
     */
    Map<String, Object> getAccountByUsername(String username);
    /**
     * 显示路径信息
     * @param start
     * @param end
     * @return
     */
    public String showPathInfo(Long start, Long end);

    /**
     * 注册功能
     */
    Map<String, Object> register(Map<String, Object> umsAdminParam);
    /**
     * 注册手机用户
     * @return
     */
    Map<String, Object> registerPhoneUser(String phone);

    /**
     * 登录功能
     * 
     * @param username 用户名
     * @param password 密码
     * @return 调用认证中心返回结果
     */
    Result login(String username, String password);
    Result logout(String userId);
    
    
    /**
     * 根据用户id获取用户
     */
    Map<String, Object> getItem(Long id);

    /**
     * 根据用户名或昵称分页查询用户
     */
    List<Map<String, Object>> list(String keyword, Integer pageSize, Integer pageNum);

    /**
     * 修改指定用户信息
     */
    int update(Long id, Map<String, Object> admin);

    /**
     * 删除指定用户
     */
    int delete(Long id);

    /**
     * 修改用户角色关系
     */
    @Transactional
    int updateRole(Long adminId, List<Long> roleIds);

    /**
     * 获取用户对于角色
     */
    List<Map<String, Object>> getRoleList(Long adminId);
    List<Map<String, Object>> getOrgInfo(Long adminId);
    public List<Map<String, Object>> getTeamInfo(Long adminId);
     
    long[] getUserListBy(Long roleId);
    long[] getUserListByRoleCode(String roleCode);
    long[] getUserListBy(String roleCode,String orgId);
    long[] getUserListBy(String roleName);
    long[] getUserListByRolesName(String... roleName);
    long[] getUserListByRolesCode(String... roleCode);
    boolean hasRole(Long userId,String roleName);
    List<Map<String, Object>> getDefaultApp();

    /**
     * 获取指定用户的可访问资源
     */
    List<Map<String, Object>> getResourceList(Long adminId);

    /**
     * 获取指定用户菜单
     * 
     * @param adminId
     * @return
     */
    List<Map<String, Object>> getMenuList(Long adminId);

    /**
     * 修改用户的+-权限
     */
    @Transactional
    int updatePermission(Long adminId, List<Long> permissionIds);

    /**
     * 获取用户所有权限（包括角色权限和+-权限）
     */
    List<Map<String, Object>> getPermissionList(Long adminId);

    /**
     * 判断权限
     *
     * @param permissionCode
     * @return
     */
    Boolean hasPermission(String permissionCode);

    /**
     * 修改密码
     */
    int updatePassword(Map<String, Object> updatePasswordParam);

    /**
     * 获取用户信息
     */
    Map<String, Object> loadAccountByUsername(String username);
    
    Map<String, Object> loadUserByUsername(String username);

    /**
     * 获取当前登录后台用户
     */
    Map<String, Object> getCurrentPassWord();

    /**
     * 获取当前用户username
     * 
     * @return
     */
    String getCurrentAccount();

    /**
     * 获取当前用户ID
     * 
     * @return
     */
    Long getCurrentPasswordId();
    String getJSessionId(String userName);
    String getCurrentJSessionId();
    String getRequestSessionId();
    
    Long getCurrentUserId();

    /**
     * 获取用户对于角色
     */
    List<Map<String, Object>> myRoleList();

    /**
     * 获取指定用户的可访问资源
     */
    List<Map<String, Object>> myResourceList();

    /**
     * 获取指定用户菜单
     *
     * @return
     */
    List<Map<String, Object>> myMenuList();

    /**
     * 获取指定用户的权限列表
     * 
     * @return
     */
    List<Map<String, Object>> myPermissionList();
    
    List<Map<String, Object>> mySettingList();
    List<Long> mySetting(Map<String, Object> setting);

    String getCurrentNeo4jDsId(String userId);

    String getCurrentName();

    String getCurrentUserName();
    Map<String, Object> getCurrentUser();
    Map<String, Object> getBuyInfo();
    Boolean hasRight(String label, String account, String operate);
    public Map<String, Object> checkAuth(String label, String operate, String msg)
            throws DefineException, AuthException;

}
