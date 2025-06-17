package com.wldst.ruder.module.auth.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.constant.AuthConstant;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.domain.AuthDomain;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.engine.DroolsService;
import com.wldst.ruder.exception.AuthException;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.encode.EncodingException;
import com.wldst.ruder.module.encode.PwdPasswordEncoder;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.goods.GoodsService;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.LoggerTool;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.PageObject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.Map.Entry;

@Service
public class UserAdminServiceImpl extends AuthDomain implements UserAdminService {
    private static Logger logger = LoggerFactory.getLogger(UserAdminServiceImpl.class);

	@Autowired
	private RelationService relationService;
    @Autowired
    private CrudUserNeo4jService neo4jService;
    @Autowired
    private Neo4jOptByUser optByUserSevice;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private DroolsService drools;
    @Autowired
    private RuleDomain rule;
    @Autowired
    private GoodsService goodsService;

    @Override
    public Map<String, Object> getAccountByUsername(String username) {
	return neo4jService.getAttMapBy(USER_NAME, username, USER_ACCOUNT);
    }

    @Override
    public Map<String, Object> register(Map<String, Object> umsAdminParam) {
	// TODO Auto-generated method stub
	Map<String, Object> copy = copy(umsAdminParam);
	copy.remove("password");
	List<Map<String, Object>> queryBy = neo4jService.queryBy(copy, USER_ACCOUNT);
	if(queryBy!=null&&!queryBy.isEmpty()) {	     
	    Map<String, Object> map = queryBy.get(0);
	    map.put("exist", true);
	    return map;
	}
	Node account = neo4jService.saveByBody(umsAdminParam, USER_ACCOUNT);
	umsAdminParam.remove("password");
	umsAdminParam.put("name", string(umsAdminParam, "username"));
	Node user = neo4jService.saveByBody(umsAdminParam, USER);
	relationService.addRel("account", user.getId(), account.getId());
	relationService.addRel("role", user.getId(), neo4jService.getNodeId(CODE, "user", "Role"));

	// 创建角色，创建默认桌面
	return neo4jService.getPropMapByNode(account);
    }

    @Override
    public Map<String, Object> registerPhoneUser(String phone) {
	// TODO Auto-generated method stub
	if (phone == null || "".equals(phone)) {
	    return null;
	}
	Map<String, Object> umsAdminParam = new HashMap<>();
	umsAdminParam.put("username", phone);

	Node userAccount = neo4jService.findBy("username", phone, USER_ACCOUNT);
	if (userAccount != null) {
	    return null;
	}
	String encodePwd = bcryptPasswordEncoder.encode(neo4jService.getSettingBy("phonePassword"));
	umsAdminParam.put(PASSWORD, encodePwd);
	Node account = neo4jService.saveByBody(umsAdminParam, USER_ACCOUNT);

	umsAdminParam.remove("password");
	umsAdminParam.put("phone", phone);
	umsAdminParam.put("name", string(umsAdminParam, "username"));
	Node user = neo4jService.saveByBody(umsAdminParam, USER);
	relationService.addRel("account", user.getId(), account.getId());
	relationService.addRel("role", user.getId(), neo4jService.getNodeId(CODE, "phoneUser", "Role"));

	// 创建角色，创建默认桌面
	return neo4jService.getPropMapByNode(account);
    }

    @Override
    public Result login(String username, String password) {
	// TODO Auto-generated method stub
	boolean expression = StringUtils.isEmpty(username) || StringUtils.isEmpty(password);
	if (expression) {
	    return Result.failed("用户名或密码不能为空！");
	}
	// Asserts.check(!expression, "用户名或密码不能为空！");
	Map<String, String> params = new HashMap<>();
	params.put("client_id", AuthConstant.ADMIN_CLIENT_ID);
	params.put("client_secret", "123456");
	params.put("grant_type", PASSWORD);
	params.put(USER_NAME, username);
	params.put(PASSWORD, password);

	// String hashPass = bcryptPasswordEncoder.encode(pass);
	Map<String, Object> passwordInfo = loadAccountByUsername(username);
	String dbPwd =  string(passwordInfo,PASSWORD);
	try {
	    if (passwordInfo == null || dbPwd == null) {
			//查询
			passwordInfo = neo4jService.getAttMapBy(USER_ID, username.toUpperCase(), "PmisPassword");
			if(passwordInfo==null){
				return Result.failed("密码或用户名错误");
			}
			dbPwd = (String) passwordInfo.get(PASSWORD);
			if (!PwdPasswordEncoder.getBspEncoder().isPasswordValid(dbPwd, password)) {
				return Result.failed("密码或用户名错误");
			}
			logSuccess(username);
	    } else {
//		dbPwd = string(passwordInfo, PASSWORD);
			if (bcryptPasswordEncoder.matches(password, dbPwd)) {
				logSuccess(username);
			} else {
				return Result.failed("密码或用户名错误");
			}
	    }
	} catch (DataAccessException | EncodingException e) {
	    // TODO Auto-generated catch block
	    LoggerTool.error(logger,e.getMessage(), e);
	}
	if (passwordInfo == null) {
	    return Result.failed();
	} else {
	    onlineSeMap.put(username, passwordInfo);
	}
	return Result.success(true);
    }

    public void logSuccess(String username) {
		if(rule.getAdminService()==null) {
		rule.setAdminService(this);
		}
		if(rule.getDrools()==null) {
		 rule.setDrools(drools);
		}

		neo4jService.insertLoginLog(username);
    }

    public Result logout(String userId) {
	String cypher = " match(n:" + ONLINE_USER + ") where n.userId='" + userId + "' delete n";
	neo4jService.execute(cypher);
	endSession(userId);
	return Result.success(true);
    }
    
    public void endSession(String userName) {
   	List<Map<String, Object>> existSession = neo4jService.listDataBy("userName", userName,"Session");
   	if(existSession!=null&&!existSession.isEmpty()) {
   	 for(Map<String, Object> si: existSession) {
    	    String string = string(si,"sessionId");
    	    HttpSession httpSession = AuthDomain.sessionMap.get(string);
    	    try {
    		if(httpSession.isNew()) {
    		 httpSession.invalidate();
    		}
    	    }catch (Exception e) {
    		AuthDomain.sessionMap.remove(string);
 	    }
    	}
   	}
   	
   	neo4jService.delete("userName", userName,"Session");
       }

    @Override
    public Map<String, Object> getItem(Long id) {
	// TODO Auto-generated method stub
	return neo4jService.getPropMapByNodeId(id);
    }

    @Override
    public List<Map<String, Object>> list(String keyword, Integer pageSize, Integer pageNum) {
	// TODO Auto-generated method stub

	PageObject page = new PageObject();
	page.setPageNum(pageNum);
	page.setPageSize(pageSize);
	List<Map<String, Object>> dataList = null;
	try {
	    String[] columns = crudUtil.getMdColumns(USER_ACCOUNT);
	    JSONObject vo = new JSONObject();
	    vo.put(NODE_NAME, keyword);
	    String query = Neo4jOptCypher.queryObj2(vo, USER_ACCOUNT, columns, page);
	    dataList = neo4jService.query(query,vo);
	} catch (DefineException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return dataList;
    }

    @Override
    public int update(Long id, Map<String, Object> admin) {
	neo4jService.update(admin, id);
	return 1;
    }

    @Override
    public int delete(Long id) {
	neo4jService.removeById(id);
	return 1;
    }

    @Override
    public int updateRole(Long adminId, List<Long> roleIds) {
	// TODO Auto-generated method stub
	String[] rolesLongs = new String[roleIds.size()];
	roleIds.toArray(rolesLongs);
	neo4jService.addRels(USER_ACCOUNT, roleRelation, adminId, String.join(",", rolesLongs));
	return 0;
    }

    @Override
    public List<Map<String, Object>> getRoleList(Long adminId) {
	String match = "MATCH (a:Password)-[*1..2]-(role:Role) where id(a)=" + adminId + " return distinct role";
	// return neo4jService.getRelationDataOf(adminId, roleRelation);
	return neo4jService.cypher(match);
    }
    

    @Override
    public List<Map<String, Object>> getOrgInfo(Long adminId) {
	return neo4jService.getRelationDataOf(adminId, "org");
    }

    @Override
    public List<Map<String, Object>> getTeamInfo(Long adminId) {
	return neo4jService.getRelationDataOf(adminId, "team");
    }

    @Override
    public List<Map<String, Object>> getDefaultApp() {
	String desktopHasApp = "match (a)-[r0:defaultApp]->(e0:App)" + " return  properties(e0) as properties";
	return neo4jService.cypher(desktopHasApp);
    }

    @Override
    public List<Map<String, Object>> getResourceList(Long adminId) {
	// TODO Auto-generated method stub
	return neo4jService.getRelationDataOf(adminId, resourceRel);
    }

    @Override
    public int updatePermission(Long adminId, List<Long> permissionIds) {
	String[] rolesLongs = new String[permissionIds.size()];
	permissionIds.toArray(rolesLongs);
	neo4jService.addRels(USER_ACCOUNT, permissionRel, adminId, String.join(",", rolesLongs));
	return 0;
    }

    @Override
    public List<Map<String, Object>> getPermissionList(Long adminId) {
	return neo4jService.getRelationDataOf(adminId, permissionRel);
    }

    @Override
    public int updatePassword(Map<String, Object> updatePasswordParam) {
	// TODO Auto-generated method stub
	Map<String, Object> user = loadAccountByUsername(string(updatePasswordParam,USER_NAME));
	Object object = user.get(NODE_ID);
	if (object == null) {
	    return 0;
	}
	String encode = bcryptPasswordEncoder.encode(String.valueOf(updatePasswordParam.get(PASSWORD)));
	updatePasswordParam.put(PASSWORD, encode);
	Node saveByBody = neo4jService.saveByBody(updatePasswordParam, PASSWORD);
	relationService.addRel(PASSWORD, id(user), saveByBody.getId());
	return 1;
    }

    @Override
    public Map<String, Object> loadAccountByUsername(String username) {
	return neo4jService.getAttMapBy(USER_NAME, username, USER_ACCOUNT);
    }
    
    @Override
    public Map<String, Object> loadUserByUsername(String username) {
	return neo4jService.getAttMapBy(USER_NAME, username, USER);
    }

    @Override
    public Map<String, Object> getCurrentPassWord() {
	String userStr = request.getHeader(AuthConstant.USER_TOKEN_HEADER);
	// Asserts.check(StringUtils.isEmpty(userStr),ResultCode.UNAUTHORIZED.getMessage());
	Map<String, String> userDto = null;
	if (userStr == null) {
	    HttpSession session = request.getSession();
	    Object attribute = session.getAttribute("loginName");
	    return loadAccountByUsername(String.valueOf(attribute));
	} else {
	    userDto = JSON.parseObject(userStr, Map.class);
	    Map<String, Object> admin = neo4jService.getPropMapBy(userDto.get(NODE_ID));
	    return admin;
	}
    }
    
    @Override
    public String getRequestSessionId() {
	HttpSession session = request.getSession();
	return session.getId();
    }

    @Override
    public Map<String, Object> getCurrentUser() {
	String userStr = request.getHeader(AuthConstant.USER_TOKEN_HEADER);
	// Asserts.check(StringUtils.isEmpty(userStr),ResultCode.UNAUTHORIZED.getMessage());
	Map<String, String> userDto = null;
	if (userStr == null) {
	    HttpSession session = request.getSession();
	    Object attribute = session.getAttribute("loginName");
	    return loadAccountByUsername(String.valueOf(attribute));
	} else {
	    userDto = JSON.parseObject(userStr, Map.class);
	    Map<String, Object> admin = neo4jService.getPropMapBy(userDto.get(NODE_ID));
	    return admin;
	}
    }
    
    @Override
    public List<Map<String, Object>> getMenuList(Long adminId) {
	String match = "MATCH (a:Password)-[*1..2]-(m:Menu) where id(a)=" + adminId + " return m";
	// return neo4jService.getRelationDataOf(adminId, roleRelation);
	return neo4jService.cypher(match);
	// return neo4jService.getRelationDataOf(adminId, menuRel);
    }

    @Override
    public String getCurrentAccount() {
		Map<String, Object> currentAdmin=null;
		try{
			currentAdmin=getCurrentPassWord();
		}catch(IllegalStateException e){
			return "";
		}

		if(currentAdmin==null||!currentAdmin.containsKey(USER_NAME)){
			return "";
		}
		Object object=currentAdmin.get(USER_NAME);
		return String.valueOf(object);
    }

    @Override
	public String getCurrentUserName(){
		Map<String, Object> account=null;
		try{
			account=getCurrentPassWord();
		}catch(IllegalStateException e){
			return "";
		}

		if(account==null||!account.containsKey(USER_NAME)){
			return "";
		}
		Object object=account.get(USER_NAME);
		return String.valueOf(object);
	}

    @Override
    public String getCurrentName() {
	Map<String, Object> currentAdmin = null;
	try {
	    currentAdmin = getCurrentPassWord();
	} catch (IllegalStateException e) {
	    return "";
	}
	String account = "username";
	Map<String, Object> user = neo4jService.getAttMapBy(account, string(currentAdmin, account), "User");

	if (user == null || !user.containsKey(NAME)) {
	    return "";
	}
	return name(user);
    }

    @Override
    public List<Map<String, Object>> myRoleList() {
	return getRoleList(getCurrentPasswordId());
    }

    @Override
    public List<Map<String, Object>> myResourceList() {
	return getResourceList(getCurrentPasswordId());
    }

    @Override
    public List<Map<String, Object>> myMenuList() {
	return getMenuList(getCurrentPasswordId());
    }

    @Override
    public Long getCurrentPasswordId() {
	Map<String, Object> currentAdmin = getCurrentPassWord();
	if (currentAdmin == null) {
	    return null;
	}
	Object object = currentAdmin.get(NODE_ID);
	if (object != null) {
	    return Long.valueOf(String.valueOf(object));
	}
	return null;
    }

    @Override
    public Long getCurrentUserId() {
	Map<String, Object> currentAdmin = getCurrentPassWord();
	if (currentAdmin == null) {
	    return 0L;
	}
	Long id2 = id(currentAdmin);
	if (id2 != null) {
	    List<Map<String, Object>> userIds = neo4jService
		    .queryByCypher("MATCH(n:User)-[r]->(m:Password) where id(m)=" + id2 + " return id(n) as id");
	    if (userIds != null && !userIds.isEmpty()) {
		return id(userIds.get(0));
	    }
	}
	return null;
    }

    @Override
    public List<Map<String, Object>> myPermissionList() {
	return getPermissionList(getCurrentPasswordId());
    }

    @Override
    public Boolean hasPermission(String permissionCode) {
	// neo4jService
	Long nodeId = neo4jService.getNodeId(CODE, permissionCode, "permission");
	optByUserSevice.setAdminService(this);
	if (hasPermission(getCurrentPasswordId(), nodeId)) {
	    return true;
	}
	if (hasPermission(getCurrentUserId(), nodeId)) {
	    return true;
	}
	return false;
    }

    public boolean isConnected(Long currentPasswordId, Long nodeId) {
	String accountPermission = optByUserSevice.hasPath2EndNode(currentPasswordId, nodeId);
	List<Map<String, Object>> apaths = neo4jService.cypher(accountPermission);
	if (apaths != null && !apaths.isEmpty()) {
	    // showPathInfo(currentPasswordId, nodeId);
	    return true;
	}
	return false;
    }
    
    public boolean hasPermission(Long currentPasswordId, Long nodeId) {
	String accountPermission = optByUserSevice.hasPermission(currentPasswordId, nodeId);
	List<Map<String, Object>> apaths = neo4jService.cypher(accountPermission);
	if (apaths != null && !apaths.isEmpty()) {
	    LoggerTool.info(logger,showPathInfo(currentPasswordId, nodeId));
	    return true;
	}
	return false;
    }

    public String showPathInfo(Long start, Long end) {
	String ret = null;
	String nodesP = optByUserSevice.getNodesOfPath(start, end);
	List<Map<String, Object>> pNodeList = neo4jService.queryByCypher(nodesP);

	String relsp = optByUserSevice.getRelsOfPath(start, end);
	List<Map<String, Object>> pRelList = neo4jService.queryByCypher(relsp);

	Map<Long, List<Map<String, Object>>> startRelMap = new HashMap<>();
	if (pRelList != null && !pRelList.isEmpty()) {
	    for (Map<String, Object> pi : pRelList) {
		long startId = node(pi, "s").getId();
		// Map<String, Object> nodeS = idNode.get(startId);
		List<Map<String, Object>> list = startRelMap.get(startId);
		if (list == null) {
		    list = new ArrayList<>();
		}
		list.add(pi);
		startRelMap.put(startId, list);
	    }
	}

	if (pRelList != null && !pRelList.isEmpty()) {
	    Set<String> used = new HashSet<>();
	    Map<Long, Map<String, Object>> idNode = mapIdNode(pNodeList);

	    // sb.append("查询从"+crudUserService.seeNode(idNode.get(start))+"到"+crudUserService.seeNode(idNode.get(end))+"的关系：\n");
	    StringBuilder sb = new StringBuilder();
	    showPath(sb,used, start, end, null, idNode, startRelMap);
	    ret = sb.toString();
	}
	return ret;
    }

    /**
     * 将结果集ListMap，映射一下，ID->Map
     * 
     * @param pNodeList
     * @return
     */
    public Map<Long, Map<String, Object>> mapIdNode(List<Map<String, Object>> pNodeList) {
	Map<Long, Map<String, Object>> idNode = new HashMap<>();
	// start-r-end,start-r-end
	for (Map<String, Object> mi : pNodeList) {
	    Map<String, Object> nodep = mapObject(mi, "nodeP");
	    Long id2 = id(nodep);
	    if (id2 == null) {
		continue;
	    }
	    idNode.put(id2, nodep);
	}
	return idNode;
    }
    /**
     * 获取两点之间的路径
     * @param prePath
     * @param used
     * @param start
     * @param end
     * @param current
     * @param idNode
     * @param startRelMap
     * @return
     */
    public Boolean showPath(StringBuilder prePath,Set<String> used, Long start, Long end, Long current,
	    Map<Long, Map<String, Object>> idNode, Map<Long, List<Map<String, Object>>> startRelMap) {
	StringBuilder sb = new StringBuilder();
	Map<String, Object> startNode = idNode.get(start);
	Map<String, Object> mapData = idNode.get(end);
	String seeEnd = neo4jService.seeNode(mapData);
	
	if (current == null) {
	    sb.append(neo4jService.seeNode(startNode));
	    current = start;
	    List<Map<String, Object>> list = startRelMap.get(start);
	    int i=0;
	    for (Map<String, Object> ri : list) {		
		long startId = node(ri, "s").getId();
		long endId = node(ri, "e").getId();
		Map<String, Object> nodeE = idNode.get(endId);
		String eName = neo4jService.seeNode(nodeE);

		String relName = string(ri, "r");
		String startEnd = startId + relName + endId;

		if (used.contains(startEnd)) {
		    continue;
		}
		
		used.add(startEnd);

		String relNamex = string(ri, "rName");
		if (relNamex != null) {
		    relName = relNamex;
		}

		List<Map<String, Object>> nextList = startRelMap.get(endId);
		StringBuilder subPath = new StringBuilder();
		if(i>0) {
		    subPath.append("\n"+neo4jService.seeNode(startNode));
		}
		i++;
		
		if (nextList == null || nextList.isEmpty()) {
		    subPath.append("【" + relName + "】->" + eName + "\n");
		} else {
		    subPath.append("【" + relName + "】->");
		    
		    if (end.equals(endId)) {
			subPath.append(eName+"\n");
		    }else {
			showPath(subPath,used, start, end, endId, idNode, startRelMap);
			
		    }
		}
		if (subPath.toString().contains(seeEnd)) {
		    sb.append(subPath.toString());
		}
	    }
	} else {
	    sb.append(neo4jService.seeNode(idNode.get(current)));
	    List<Map<String, Object>> currentOutRels = startRelMap.get(current);
	    int i=0;
	    for (Map<String, Object> ri : currentOutRels) {
		
		String eName = null;
		long startId = node(ri, "s").getId();

		long endId = node(ri, "e").getId();
		Map<String, Object> nodeE = idNode.get(endId);
		if(nodeE==null) {
		    continue;
		}
		eName = neo4jService.seeNode(nodeE);

		List<Map<String, Object>> nextList = startRelMap.get(endId);
		String relName = string(ri, "r");
		String startEnd = startId + relName + endId;

		if (used.contains(startEnd)) {
		    continue;
		}
		used.add(startEnd);

		String relNamex = string(ri, "rName");
		if (relNamex != null) {
		    relName = relNamex;
		}
		StringBuilder reliPath = new StringBuilder();
		if(i>0) {
		    reliPath.append("\n"+neo4jService.seeNode(startNode));
		}
		i++;
		if (nextList == null || nextList.isEmpty()) {
		    reliPath.append( "【"+relName + "】->" + eName + "\n");
		    if (end.equals(endId)) {
			sb.append(reliPath.toString());
		    }
		} else {
		    reliPath.append("【"+relName + "】->");
		  
		    if (end.equals(endId)) {
			reliPath.append(eName+"\n");
			sb.append(reliPath.toString());
		    }else {
			showPath(reliPath,used, start, end, endId, idNode, startRelMap);
		    }
		}
		if(reliPath.toString().contains(seeEnd)) {
		    sb.append(reliPath.toString());
		}
	    }
	}
	 if(sb.toString().contains(seeEnd)) {
		prePath.append(sb.toString());
		return true;
	    }
	return false;

    }

    @Override
    public List<Map<String, Object>> mySettingList() {
	List<Map<String, Object>> listAttMapBy = neo4jService.listAttMapBy(ACCOUNT, getCurrentAccount(), MY_SETTING);
	// neo4jService.cypher("match(n:Mysetting{}) where return ");
	return listAttMapBy;
    }

    public List<Long> mySetting(Map<String, Object> mySetting) {
	List<Long> seList = new ArrayList<>();
	for (Entry<String, Object> key : mySetting.entrySet()) {
	    Map<String, Object> miMap = new HashMap<>();
	    miMap.put(ACCOUNT, getCurrentAccount());
	    neo4jService.removeNodeByPropAndLabel(miMap, MY_SETTING);
	    miMap.put(CODE, key.getKey());
	    miMap.put(VALUE, key.getValue());
	    Node saveByBody = neo4jService.saveByBody(miMap, MY_SETTING);
	    seList.add(saveByBody.getId());
	}

	return seList;
    }

    @Override
    public long[] getUserListBy(Long roleId) {
	List<Map<String, Object>> userId = neo4jService
		.cypher("match (n:WorkFlowRole)-[r]-(u:User) where id(n)=" + roleId + " return id(u) as id");
	return getIds(userId);
    }

    private long[] getIds(List<Map<String, Object>> users) {
	long[] userIds = new long[users.size()];
	int i = 0;
	for (Map<String, Object> ui : users) {
	    userIds[i] = MapTool.longValue(ui, ID).longValue();
	    i++;
	}
	return userIds;
    }

    @Override
    public long[] getUserListBy(String roleName) {
	List<Map<String, Object>> userId = neo4jService
		.queryCache("match (n:WorkFlowRole)-[r]-(u:User) where n.name='" + roleName + "' return id(u) as id");
	return getIds(userId);
    }

    @Override
    public long[] getUserListByRolesName(String... roleNames) {
	String cypher = "match (n:WorkFlowRole)-[r]-(u:User) where n.name in ['" + String.join("','", roleNames)
		+ "'] return id(u) as id";
	List<Map<String, Object>> userId = neo4jService.queryCache(cypher);
	return getIds(userId);
    }

    @Override
    public long[] getUserListByRoleCode(String roleCode) {
	List<Map<String, Object>> userId = neo4jService
		.queryCache("match (n:WorkFlowRole)-[r]-(u:User) where n.code=" + roleCode + " return id(u) as id");
	return getIds(userId);
    }

    @Override
    public long[] getUserListByRolesCode(String... roleCode) {
	String cypher = "match (n:WorkFlowRole)-[r]-(u:User) where n.code in ['" + String.join("','", roleCode)
		+ "'] return id(u) as id";
	List<Map<String, Object>> userId = neo4jService.queryCache(cypher);
	return getIds(userId);
    }

    @Override
    public boolean hasRole(Long userId, String roleName) {
	String cypher = "match (n:User)-[r]-(wr:WorkFlowRole) where id(n)=" + userId + "   return count(wr) AS cr ";
	List<Map<String, Object>> userIds = neo4jService.cypher(cypher);
	if (userIds != null && !userIds.isEmpty()) {
	    Integer cr = MapTool.integer(userIds.get(0), "cr");
	    return cr > 0 ? true : false;
	}
	return false;
    }

    @Override
    public long[] getUserListBy(String roleCode, String orgId) {
	String cypher = "match (n:User)-[*1..3]-(wr:WorkFlowRole),(n:User)-[*1..3]-(o:Organization) where wr.code=\""
		+ roleCode + "\" and o.id=" + orgId + "    return id(n) AS id ";
	List<Map<String, Object>> userIds = neo4jService.cypher(cypher);
	if (userIds != null && !userIds.isEmpty()) {
	    long[] data = new long[userIds.size()];
	    for (int i = 0; i < userIds.size(); i++) {
		data[i] = id(userIds.get(i));
	    }
	    return data;
	}
	return null;
    }

    @Override
    public String getCurrentNeo4jDsId(String userId) {
	String cypher = "match (n:Account)-[*:2-5]-(nds:Neo4jDataSource) where id(n)=" + userId
		+ "   return id(nds) AS dsId ";
	List<Map<String, Object>> userIds = neo4jService.cypher(cypher);
	return MapTool.string(userIds.get(0), "dsId");
    }

    @Override
    public String getJSessionId(String userName) {
	String cypher = "match (n:Session) where n.userName='" + userName
		+ "'   return n.sessionId ";
	Map<String, Object> one = neo4jService.getOne(cypher);
	return string(one,"sessionId");
    }
    @Override
    public String getCurrentJSessionId() {
	String cypher = "match (n:Session) where n.userName='" + getCurrentUserName()
		+ "'   return n.sessionId ";
	Map<String, Object> one = neo4jService.getOne(cypher);
	return string(one,"sessionId");
    }

    @Override
    public Map<String, Object> getBuyInfo() {
	Long currentUserId = getCurrentUserId();
	List<Map<String, Object>> memberUse = neo4jService.listAttMapBy("userId", currentUserId, "BuyRecord");
	List<Map<String, Object>> listAttMapBy = neo4jService.listAttMapBy("userId", String.valueOf(currentUserId), "BuyRecord");
	if(listAttMapBy!=null&&!listAttMapBy.isEmpty()) {
	    memberUse.addAll(listAttMapBy);
	}
	Map<String, Object> buyInfo = null;
	if(memberUse==null||memberUse.isEmpty()) {
	   buyInfo = goodsService.createFree(currentUserId);
	}else {
	    for(Map<String, Object> mi: memberUse) {
//		String code2 = longValue(mi,"memberId");
		Map<String, Object> nodeMapById = neo4jService.getNodeMapById(longValue(mi,"memberId"));
		String code2=code(nodeMapById);
		if(code2!=null&&code2.equals("forever")) {
		    return mi;
		}
	    }
	    return memberUse.get(0);
	}
	return buyInfo;
    }

	@Override
	public Map<String, Object> checkAuth(String label, String operate, String msg)
			throws DefineException{
		Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
		if (md == null || md.isEmpty()) {
			throw new DefineException(label + "未定义！");
		}
		return md;
//
//		String currentAccount = getCurrentAccount();
//
//		if (hasPermission(AuthDomain.CREATROR_AUTH)) {
//			return md;
//		}
//		// adminService.getc
//		String metaCreator = creator(md);
//		if (metaCreator != null && !metaCreator.equals(currentAccount)) {
//			Boolean hasRight = hasRight(label, currentAccount, operate);
//			if (!hasRight) {
//			String lowerCase = operate.toLowerCase();
//			if(lowerCase.startsWith("save")||lowerCase.startsWith("update")||
//				lowerCase.startsWith("query")||
//				lowerCase.startsWith("delete")) {
//				md =checkAuth(label,"CRUDOperate",msg);
//			}else {
//				throw new AuthException("没有【" + name(md) + "】的" + msg + "权限！请联系管理员");
//			}
//			}
//		}
//		return md;
	}
	@Override
	public Boolean hasRight(String label, String account, String operate) {

		// 根据用户去查询权限 角色
		String cypher = " MATCH (u:User)-[r0:HAS_PERMISSION]->(role:Role)-[r1:HAS_PERMISSION{code:\"" + operate
				+ "\"}]->(n:MetaData) where u.username=\"" + account + "\" " + "   and n.label=\"" + label
				+ "\" return r1 ";

		List<Map<String, Object>> query = neo4jService.cypher(cypher);
		if (null == query || query.isEmpty()) {// 查询用户的角色所拥有的权限
			cypher = " MATCH (u:User)-[r:HAS_PERMISSION]->(n:MetaData) where u.username=\"" + account + "\" "
					+ " and r.code='" + operate + "'   and n.label=\"" + label + "\" return r ";
			query = neo4jService.cypher(cypher);
			if (null == query || query.isEmpty()) {
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
//	String vari = "hasAuth";

		// if(!hasRight) {//用户账号去查询角色
		// cypher = " Match
		// (p:Password{userid:'"+account+"'}),(n:MetaData{\"label\":\""+label+"\"})
		// return exists((p)-->(role:Role)-[r:"+operate+"]->(n)) AS "+vari;
		//
		// hasRight = neo4jService.queryBool(cypher,vari);
		// if(!hasRight) {
		// cypher = " Match
		// (p:Password{\"userid\":\""+account+"\"}),(n:MetaData{\"label\":\""+label+"\"})
		// return exists((p)-[r:"+operate+"]->(n)) AS "+vari;
		// hasRight= neo4jService.queryBool(cypher,vari);
		// }
		// }
	}

}
