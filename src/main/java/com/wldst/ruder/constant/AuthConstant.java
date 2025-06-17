package com.wldst.ruder.constant;

import com.wldst.ruder.util.MapTool;

/**
 * 权限相关常量定义
 * Created by macro on 2020/6/19.
 */
public class AuthConstant extends MapTool{

    /**
     * JWT存储权限前缀
     */
    public final static String AUTHORITY_PREFIX = "ROLE_";

    /**
     * JWT存储权限属性
     */
    public final static String AUTHORITY_CLAIM_NAME = "authorities";

    /**
     * 后台管理client_id
     */
    public final static String  ADMIN_CLIENT_ID = "admin-app";

    /**
     * 前台商城client_id
     */
    public final static String  PORTAL_CLIENT_ID = "portal-app";

    /**
     * 后台管理接口路径匹配
     */
    String ADMIN_URL_PATTERN = "/mall-admin/**";

    /**
     * Redis缓存权限规则key
     */
    String RESOURCE_ROLES_MAP_KEY = "auth:resourceRolesMap";

    /**
     * 认证信息Http请求头
     */
    public final static String JWT_TOKEN_HEADER = "Authorization";

    /**
     * JWT令牌前缀
     */
    public final static String JWT_TOKEN_PREFIX = "Bearer ";

    /**
     * 用户信息Http请求头
     */
    public final static String USER_TOKEN_HEADER = "user";
    
    
    
    public final static String HAS_PERMISSION="HAS_PERMISSION";

}
