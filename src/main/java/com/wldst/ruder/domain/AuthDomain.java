package com.wldst.ruder.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.exception.AuthException;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;

import jakarta.servlet.http.HttpSession;

public class AuthDomain extends SystemDomain {
    protected static final String USER_NAME = "username";
    protected static final String USER_ID = "userid";
    protected static final String USER_EMAIL = "userEmail";
    protected static final String PASSWORD = "password";
    protected static final String MENUS = "menus";
    protected static final String ROLES = "roles";
    protected static final String RESOURCES = "resources";

    protected static final String roleRelation = "role";
    protected static final String ORG = "Organization";
    protected static final String TEAM = "Team";
    protected static final String permissionRel = "Permission";
    public final static String HAS_PERMISSION="HAS_PERMISSION";
    protected static final String menuRel = "Menu";
    protected static final String resourceRel = "Resource";
    protected static final String passwordLabel = "password";
    protected static final String USER_ACCOUNT = "Password";
    protected static final String USER = "User";
    public static final String CREATROR_AUTH = "creatorAuth";
    protected static final String ACCOUNT = "account";
    public static final String ONLINE_USER = "OnlineUser";
    public static final String LABEL_CLIENT = "Client";
    public static final String LABEL_CLIENT_LOG = "ClientLog";
    // 拥有者相关变量
    protected static final String OWNER = "Owner";
    protected static final String SETTINGS = "Settings";

    protected static final String MAC_ADDRESS = "macAddress";
    protected static final String IP_ADDRESS = "ipAddress";
    protected static final String HOST_NAME = "hostName";
    protected static final String CLIENT_PORT = "port";
    protected static final String CLIENT_HTTPS_PORT = "httpsPort";

    protected static final String USER_PHONE = "phone";
    protected static final String ROLE_USER_PHONE = "phoneUser";
    protected static final String SESSION = "Session";
    
    // @Autowired
    // protected RestApi authServicex;


    protected BCryptPasswordEncoder bcryptPasswordEncoder = new BCryptPasswordEncoder();
    public static final Map<String, Map<String, Object>> onlineSeMap = new HashMap<>();
    public static final Map<String, HttpSession> sessionMap = new HashMap<>();
     
}
