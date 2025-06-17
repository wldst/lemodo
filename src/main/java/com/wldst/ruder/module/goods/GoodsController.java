package com.wldst.ruder.module.goods;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import com.wldst.ruder.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.crud.service.ObjectService;
import com.wldst.ruder.domain.GoodsDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.ws.client.RuderWsClient;

/**
 * 主要实现商品的序列化，传递，反序列化Neo4j
 * 
 * @author wldst
 *
 */
@RestController
@RequestMapping("${server.context}/goods")
public class GoodsController extends GoodsDomain {
    final static Logger logger = LoggerFactory.getLogger(GoodsController.class);


    private CrudNeo4jService neo4jService;

    private CrudUserNeo4jService userNeo4j;

    private UserAdminService adminService;

    private Neo4jOptByUser optByUserSevice;

    private GoodsService goodsService;

    private RuderWsClient wsClient;

    private RestApi restApi;

    private ObjectService objectService;

    private Map<String, Set<Long>> setMap = new HashMap<>();
    private Set<String> relSet = new HashSet<>();
    private Map<String, List<Map<String, Object>>> listMap = new HashMap<>();

    public GoodsController(CrudNeo4jService neo4jService, CrudUserNeo4jService userNeo4j, UserAdminService adminService, Neo4jOptByUser optByUserSevice, GoodsService goodsService, RuderWsClient wsClient, RestApi restApi, ObjectService objectService){
        this.neo4jService=neo4jService;
        this.userNeo4j=userNeo4j;
        this.adminService=adminService;
        this.optByUserSevice=optByUserSevice;
        this.goodsService=goodsService;
        this.wsClient=wsClient;
        this.restApi=restApi;
        this.objectService=objectService;
    }

    /**
     * 导出商品
     * 
     * @param vo
     * @return
     * @throws DefineException
     */
    @ResponseBody
    @RequestMapping(value = "/export", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult export(@RequestBody JSONObject vo) throws DefineException {
	Long goodsId = vo.getLong(ID);
	Map<String, Set<Long>> relationEndIdMap = new HashMap<>();
	Set<Long> allIdSet = new HashSet<>();
	getExportGraphData(goodsId, relationEndIdMap, allIdSet);
	Object nodeName = neo4jService.getPropValueByNodeId(goodsId, NAME);
	List<String> aaList = new ArrayList<>();
	List<Map<String, Object>> reldataList = new ArrayList<>();
	exportNodeData(nodeName, aaList, allIdSet);

	for (Long endi : allIdSet) {// 每个节点之间的关系数据
	    List<Map<String, Object>> reldata = neo4jService.outRelationDatas(endi);
	    reldataList.addAll(reldata);
	}

	if (!reldataList.isEmpty()) {
	    Map<String, Object> fileMap = new HashMap<>();
	    fileMap.put(NAME, nodeName + "_关系数据.json");
	    String jsonString = JSONUtils.toJSONString(reldataList);
	    String pathname = userNeo4j.goodsPersistSave(jsonString, fileMap);
	    aaList.add(pathname);
	}
	// 建立一个压缩包，囊括所有数据文件，关系文件。
	return ResultWrapper.wrapResult(true, aaList, null, QUERY_SUCCESS);
    }

    /**
     * 备份系统数据，作为系统初始化或者升级数据
     *
     * @return
     * @throws DefineException
     */
    @ResponseBody
    @RequestMapping(value = "/backup", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @Scheduled(cron = "0 0 1 ? * MON-FRI")
    public WrappedResult backup() throws DefineException {
	// 查看操作日志
	// 上次备份id
	String topOne = optByUserSevice.getTopOne(null, "BackLog", "id,operateLogId".split(","));
	List<Map<String, Object>> query = neo4jService.cypher(topOne);
	Long lastBakId = null;
	if (query != null && !query.isEmpty()) {
	    lastBakId = longValue(query.get(0), "operateLogId");
	}

	// 最新的操作日志ID
	String oLog = optByUserSevice.getTopOneByCql(
		" match(n:operateLog) where n.targetLabel <>'[LoginLog]' and n.targetLabel<>'[OnlineUser]' and n.targetLabel <>'LoginLog' and n.targetLabel<>'OnlineUser' return id(n) as id order by n.time desc");
	List<Map<String, Object>> opLog = neo4jService.cypher(oLog);
	Long latestOpLogId = null;

	if (opLog != null && !opLog.isEmpty()) {
	    latestOpLogId = id(opLog.get(0));
	}
	boolean dontBackup = lastBakId != null && latestOpLogId != null && latestOpLogId == lastBakId;

	if (dontBackup) {// 不需要备份，则退出
	    return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
	}

	// 获取数据产品数据，获取系统数据。排除非产品，非系统数据。
	Set<Long> allMetaIdSet = new HashSet<>();
	Map<Long, Map<String, Object>> idMap = new HashMap<>();
	// 1、获取所有MetaData数据,
	List<Map<String, Object>> metaList = neo4jService.listDataByLabel(META_DATA);

	for (Map<String, Object> data : metaList) {
	    Long longValue = id(data);
	    idMap.put(longValue, data);
	    data.put(MARK_LABEL, META_DATA);
	    allMetaIdSet.add(longValue);
	}

	List<Map<String, Object>> relList = new ArrayList<>();
	List<Map<String, Object>> productData = new ArrayList<>();

	for (Long mdIdi : allMetaIdSet) {// 每个节点之间的关系数据
	    List<Map<String, Object>> metaReldata = neo4jService.outRelationDatas(mdIdi);
	    relList.addAll(metaReldata);
	    for (Map<String, Object> ri : metaReldata) {
		// 导出产品数据
		String relLabel = string(ri, RELATION_TYPE);
		if ("isproductResourceType".equals(relLabel)) {
		    String metaiLabel = null;
		    Map<String, Object> metaDatai = idMap.get(mdIdi);
		    if (metaDatai != null) {
			metaiLabel = label(metaDatai);
			if (!META_DATA.equals(metaiLabel)) {
			    // 2data
			    List<Map<String, Object>> dataList = neo4jService.listDataByLabel(metaiLabel);
			    markLabel(metaiLabel, dataList);
			    productData.addAll(dataList);
			    List<Map<String, Object>> collectProductDataRel = collectProductDataRel(relList, dataList);
			    if (!collectProductDataRel.isEmpty()) {
				relList.addAll(collectProductDataRel);
			    }
			}
		    }
		}
	    }
	}

	exportData(metaList, "元数据");
	exportData(relList, "关系数据");
	exportData(productData, "产品数据");
	// BackLog
	Map<String, Object> blog = new HashMap<>();
	blog.put("operateLogId", latestOpLogId);
	blog.put("time", DateUtil.nowTime());
	neo4jService.saveByBody(blog, "BackLog");
	// 建立一个压缩包，囊括所有数据文件，关系文件。
	return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/exportBaseMenu", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @Scheduled(cron = "0 0 1 ? * MON-FRI")
    public WrappedResult exportBase() throws DefineException {

	Long currentUserId = adminService.getCurrentUserId();
	// 基本套餐

	List<Map<String, Object>> baseMenu = neo4jService.listAttMapBy(CREATOR, currentUserId, "BaseMenu");
	// 获取数据产品数据，获取系统数据。排除非产品，非系统数据。
	Set<Long> metaIdSet = new HashSet<>();
	Set<Long> dataIdSet = new HashSet<>();
	setMap.put("dataIdSet", dataIdSet);
	setMap.put("metaIdSet", metaIdSet);

	Map<Long, Map<String, Object>> idMap = new HashMap<>();
	List<Map<String, Object>> relList = new ArrayList<>();
	List<Map<String, Object>> dataList = new ArrayList<>();
	List<Map<String, Object>> metaList = new ArrayList<>();

	listMap.put("relList", relList);
	listMap.put("dataList", dataList);
	listMap.put("metaList", metaList);

	String account = ownerDataInfo(currentUserId, idMap);

	// 打造一个拥有者,
	Map<String, Object> owner = newMap();
	owner.put(MARK_LABEL, "System");
	owner.put("owner", currentUserId);

	listMap.get("dataList").add(owner);

	// 给拥有者配置什么数据呢?
	// 各种关系需要进一步读取结束节点的元数据信息.

	// 1、获取所有MetaData数据,
	for (Map<String, Object> wi : baseMenu) {
	    Long subject = longValue(wi, "subjectId");
	    String subjectLabel = neo4jService.getNodeLabelByNodeId(subject);

	    Map<String, Object> wantData = neo4jService.getOneMapById(subject);
	    idMap.put(subject, wantData);
	    readInRelInfo(subject, idMap);

	    if (label(wi) != null || META_DATA.equals(subjectLabel)) {
		wantData.put(MARK_LABEL, META_DATA);
		metaIdSet.add(subject);
		metaList.add(wantData);
	    } else {
		wantData.put(MARK_LABEL, subjectLabel);
		addDataInfo(wantData);
		addMeta(idMap, subjectLabel, wantData);
	    }
	}

	Set<Long> relEndIdSet = new HashSet<>();
	// 处理数据节点
	for (Long dataIdi : dataIdSet) {//
	    Set<Long> relEndIdSeti = handleOutRelation(idMap, dataIdi);
	    relEndIdSet.addAll(relEndIdSeti);
	}

	// 是否需要考虑划域,
	for (Long eId : relEndIdSet) {
	    if (!dataIdSet.contains(eId)) {
		dataIdSet.add(eId);
		Map<String, Object> endData = idMap.get(eId);
		if (META_DATA.equals(label(endData))) {
		    metaIdSet.add(eId);
		    metaList.add(endData);
		} else {
		    Map<String, Object> dataiWithLabel = neo4jService.getPropLabelByNodeId(eId);

		    String endLabel = label(dataiWithLabel);
		    addMeta(idMap, endLabel, dataiWithLabel);
		    dataList.add(endData);
		}
	    }
	}

	// 处理元数据节点
	for (Long mdIdi : metaIdSet) {// 每个节点之间的关系数据
	    List<Map<String, Object>> metaReldata = neo4jService.outRelationDatas(mdIdi);
	    relList.addAll(metaReldata);
	    for (Map<String, Object> ri : metaReldata) {
		// 导出产品数据
		String relLabel = string(ri, RELATION_TYPE);
		if ("isproductResourceType".equals(relLabel)) {
		    String metaiLabel = null;
		    Map<String, Object> metaDatai = idMap.get(mdIdi);
		    if (metaDatai != null) {
			metaiLabel = label(metaDatai);
			if (!META_DATA.equals(metaiLabel)) {
			    // 2data
			    List<Map<String, Object>> allList = neo4jService.listDataByLabel(metaiLabel);
			    markLabel(metaiLabel, allList);
			    dataList.addAll(allList);
			    List<Map<String, Object>> collectProductDataRel = collectProductDataRel(relList, dataList);
			    if (!collectProductDataRel.isEmpty()) {
				relList.addAll(collectProductDataRel);
			    }
			}
		    }
		}
	    }
	}

	exportData(metaList, "元数据");
	exportData(relList, "关系数据");
	exportData(dataList, "产品数据");

	// 建立一个压缩包，囊括所有数据文件，关系文件。
	return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }

    @ResponseBody
    @RequestMapping(value = "/exportIWant", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @Scheduled(cron = "0 0 1 ? * MON-FRI")
    public WrappedResult exportIWant() throws DefineException {
	Long cuId = adminService.getCurrentUserId();

	List<Map<String, Object>> wants = neo4jService.listAttMapBy(CREATOR, cuId, "IWant");
	// 获取数据产品数据，获取系统数据。排除非产品，非系统数据。
	Map<Long, Map<String, Object>> idMap = new HashMap<>();
	List<Map<String, Object>> relList = new ArrayList<>();
	List<Map<String, Object>> dataList = new ArrayList<>();
	List<Map<String, Object>> metaList = new ArrayList<>();
	listMap.put("relList", relList);
	listMap.put("dataList", dataList);
	listMap.put("metaList", metaList);
	Set<Long> metaIdSet = new HashSet<>();
	Set<Long> dataIdSet = new HashSet<>();
	Set<Long> relIdSet = new HashSet<>();
	setMap.put("dataIdSet", dataIdSet);
	setMap.put("metaIdSet", metaIdSet);
	setMap.put("relIdSet", relIdSet);
	// 获取用户信息

	String account = ownerDataInfo(cuId, idMap);
	//系统
	getMenu(idMap, "SystemMenu");
	
	// 基本套餐
	getMenu(idMap, "BaseMenu");
	//会员信息
	memberInfo(cuId, dataList);
	// 1、获取所有MetaData数据,
	for (Map<String, Object> wi : wants) {
	    Long subject = longValue(wi, "subjectId");
	    processOutNode(idMap, subject);
	}
	// 是否需要考虑划域,
	Set<Long> cdSet = copySet(setMap.get("dataIdSet"));
	for (Long di : cdSet) {
	    processOutNode(idMap, di);
	}

	Set<Long> copyMetaIdSet = copySet(setMap.get("metaIdSet"));
	// 处理元数据节点
	for (Long mdIdi : copyMetaIdSet) {// 每个节点之间的关系数据
	    processOutNode(idMap, mdIdi);
	    List<Map<String, Object>> metaReldata = neo4jService.outRelationDatas(mdIdi);

	    relList.addAll(metaReldata);
	    for (Map<String, Object> ri : metaReldata) {
		// 导出产品数据
		String relLabel = string(ri, RELATION_TYPE);
		if ("isproductResourceType".equals(relLabel)) {
		    String metaiLabel = null;
		    Map<String, Object> metaDatai = idMap.get(mdIdi);
		    if (metaDatai != null) {
			metaiLabel = label(metaDatai);
			if (!META_DATA.equals(metaiLabel)) {
			    // 2data
			    List<Map<String, Object>> allDataOfLabel = neo4jService.listDataByLabel(metaiLabel);
			    markLabel(metaiLabel, allDataOfLabel);
			    for (Map<String, Object> di : allDataOfLabel) {
				addData(di);
			    }

			    List<Map<String, Object>> collectProductDataRel = collectProductDataRel(relList,
				    allDataOfLabel);
			    if (!collectProductDataRel.isEmpty()) {
				relList.addAll(collectProductDataRel);
			    }
			}
		    }
		}
	    }
	}
	Set<Long> cdSet1 = copySet(setMap.get("dataIdSet"));
	// 检查父节点是否存在,不存在则要继续导出到根.整棵树导出
	Set<String> trees = new HashSet<>();
	for (Long dataId : cdSet1) {
	    Map<String, Object> dataiWithLabel = neo4jService.getPropLabelByNodeId(dataId);
	    String dataLabel = label(dataiWithLabel);
	    Long parentId = parentId(dataiWithLabel);
	    //
	    if (parentId != null) {
		if (!trees.contains(dataLabel)) {
		    trees.add(dataLabel);
		    List<Map<String, Object>> treeAllData = neo4jService.listAllByLabel(dataLabel);
		    if (treeAllData == null || treeAllData.isEmpty()) {
			continue;
		    }
		    Map<Long, Map<String, Object>> idMapTi = new HashMap<>();
		    for (Map<String, Object> ti : treeAllData) {
			idMapTi.put(id(ti), ti);
		    }
		    for (Map<String, Object> ti : treeAllData) {
			Map<String, Object> dataTreeNode = idMap.get(id(ti));
			if (dataTreeNode == null) {
			    processOutNode(idMap, id(ti));
			    addData(ti);
			}

			Long tParentId = parentId(ti);

			if (tParentId != null) {
			    if (idMap.get(tParentId) == null) {// 父节点未记录
				Map<String, Object> parentData = idMapTi.get(tParentId);
				if (parentData == null) {
				    processOutNode(idMap, tParentId);
				}
			    }
			}

		    }
		}
	    }
	}
	// 检查元数据是否缺失
	for (Long dataId : cdSet1) {
	    Map<String, Object> dataiWithLabel = neo4jService.getPropLabelByNodeId(dataId);
	    String dataLabel = label(dataiWithLabel);
	    addMeta(idMap, dataLabel, idMap.get(dataId));
	}
	// 元数据字段关系处理.
	updateCreator(listMap.get("metaList"), account);
	updateCreator(listMap.get("dataList"), account);
	exportData(listMap.get("metaList"), "元数据");
	exportData(listMap.get("relList"), "关系数据");
	exportData(listMap.get("dataList"), "产品数据");

	// 建立一个压缩包，囊括所有数据文件，关系文件。
	return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }

    public void free(Long cuId, List<Map<String, Object>> dataList) {	
	Map<String, Object> member = goodsService.freeMember(cuId);
	dataList.add(member);
    }

    
    
    public void memberInfo(Long cuId, List<Map<String, Object>> dataList) {
	Map<String, Object> buyInfo = neo4jService.getAttMapBy("userId", String.valueOf(cuId), "BuyRecord");
	if(buyInfo==null) {
	    free(cuId,dataList);
	    return;
	}
	Map<String, Object> memberInfo = copy(buyInfo);
	memberInfo.remove(ID);
	memberInfo.put(CREATOR,"system");
	dataList.add(memberInfo);
    }

    public void getMenu(Map<Long, Map<String, Object>> idMap, String menu) {
	Map<String, Object> attMapBy = neo4jService.getAttMapBy(CODE, menu, "Scene");
	Long baseMenuId = id(attMapBy);
	processNode(idMap, baseMenuId);
    }

    public void handelMoreSet(Map<String, Object> moreSet) {
	updateSet(setMap.get("metaIdSet"), moreSet, "mdIdSet");
	updateSet(setMap.get("dataIdSet"), moreSet, "dataIdSet");
    }

    public String ownerDataInfo(Long cuId, Map<Long, Map<String, Object>> idMap) {
	List<Map<String, Object>> dataList = listMap.get("dataList");
	// processNodeAndUpdateSet(idMap, cuId);
	processNode(idMap, cuId);
	Map<String, Object> user = idMap.get(cuId);
	// 打造一个拥有者,
	Map<String, Object> owner = newMap();
	owner.put(MARK_LABEL, "System");
	String account = string(user, "username");
	owner.put("owner", account);

	dataList.add(owner);
	Long accountId = adminService.getCurrentPasswordId();
	Long userId = adminService.getCurrentUserId();
	processNode(idMap, accountId);
	processNode(idMap, userId);
	// 获取桌面相关信息
	String myDesktopName = "deskTop_" + account;
	Map<String, Object> myDesktop = neo4jService.getAttMapBy("menuid", myDesktopName, DESKTOP);

	List<Map<String, Object>> otherInfo = new ArrayList<>();
	otherInfo.add(myDesktop);
	List<Map<String, Object>> desktops = userNeo4j.getPathEnds(accountId, DESKTOP);
	otherInfo.addAll(desktops);
	List<Map<String, Object>> apps = userNeo4j.getPathEnds(accountId, DeskTop_APP);
	otherInfo.addAll(apps);
	List<Map<String, Object>> roles = adminService.getRoleList(accountId);
	otherInfo.addAll(roles);

	List<Map<String, Object>> roles2 = adminService.getRoleList(userId);
	otherInfo.addAll(roles2);

	List<Map<String, Object>> menus = adminService.getMenuList(accountId);
	otherInfo.addAll(menus);
	List<Map<String, Object>> menus2 = adminService.getMenuList(userId);
	otherInfo.addAll(menus2);
	Map<String, Object> startupMenu = neo4jService.getAttMapBy(NAME, "opening", DESKTOP);
	otherInfo.add(startupMenu);

	for (Map<String, Object> adi : otherInfo) {
	    if (adi == null) {
		continue;
	    }
	    processOutNode(idMap, id(adi));
	}
	return account;
    }

//    public void processNodeAndUpdateSet(Map<Long, Map<String, Object>> idMap, Long currentPasswordId) {
//	processNode(idMap, currentPasswordId);
//	// Map<String, Object> idSetMapx = processNode(idMap, currentPasswordId);
//	// handelMoreSet(idSetMapx);
//    }

    public void updateSet(Set<Long> dataIdSet, Map<String, Object> idSetMap, String dataSetKey) {
	Set<Long> idSet = longSet(idSetMap, dataSetKey);
	if (idSet != null && !idSet.isEmpty()) {
	    if (idSet.size() != dataIdSet.size()) {
		dataIdSet.addAll(idSet);
		idSetMap.remove(dataSetKey);
	    }
	}
    }

    public void handleRelation(Map<Long, Map<String, Object>> idMap, Long baseMenuId) {
	readInRelInfo(baseMenuId, idMap);
	readOutRelInfo(baseMenuId, idMap);
    }

    public void addData(Map<String, Object> di) {
	addDataInfo(di);
    }

    public void updateCreator(List<Map<String, Object>> metaList, String account) {
	for (Map<String, Object> mi : metaList) {
	    if (mi == null) {
		continue;
	    }
	    mi.put(CREATOR, account);
	}
    }

    public void processNode(Map<Long, Map<String, Object>> idMap, Long dataId) {
	if (dataId == null) {
	    return;
	}
	validMetaAndData(idMap, dataId);
	handleRelation(idMap, dataId);
	
    }

    public void processOutNode(Map<Long, Map<String, Object>> idMap, Long dataId) {

	// Set<Long> copySet = copySet(dataIdSet);
	// Set<Long> copyMetaSet = copySet(metaIdSet);
	// Map<String, Object> dataRt = newMap();
	validMetaAndData(idMap, dataId);
	readOutRelInfo(dataId, idMap);
	// readOutRelInfo(dataId, copyMetaSet, copySet, idMap, relList, metaList,
	// dataList);
	// if (copyMetaSet.size() != metaIdSet.size()) {
	// dataRt.put("mdIdSet", copyMetaSet);
	// }
	// if (copySet.size() != dataIdSet.size()) {
	// dataRt.put("dataIdSet", copySet);
	// }
	// return dataRt;
    }

    // public Map<String, Object> processInNode(Map<Long, Map<String, Object>>
    // idMap, Long dataId) {
    public void processInNode(Map<Long, Map<String, Object>> idMap, Long dataId) {
	// Set<Long> dataIdSet = setMap.get("dataIdSet");
	// Set<Long> metaIdSet = setMap.get("metaIdSet");
	// Set<Long> copySet = copySet(dataIdSet);
	// Set<Long> copyMetaSet = copySet(metaIdSet);
	// Map<String, Object> dataRt = newMap();
	validMetaAndData(idMap, dataId);
	readInRelInfo(dataId, idMap);
	// readInRelInfo(dataId, copyMetaSet, copySet, idMap, relList, metaList,
	// dataList);
	// if (copyMetaSet.size() != metaIdSet.size()) {
	// dataRt.put("mdIdSet", copyMetaSet);
	// }
	// if (copySet.size() != dataIdSet.size()) {
	// dataRt.put("dataIdSet", copySet);
	// }
	// return dataRt;
    }

    public void readInRelInfo(Long dataId, Map<Long, Map<String, Object>> idMap) {
	Set<Long> relStartIdSeti = handleInRelation(idMap, dataId);
	for (Long startId : relStartIdSeti) {
	    if (idMap.containsKey(startId)||
		dataId.equals(startId) || 
		setMap.get("dataIdSet").contains(startId) ||
		setMap.get("metaIdSet").contains(startId)) {
		continue;
	    }
		processInNode(idMap, startId);
	}
    }

    /**
     * // String dataLabel = neo4jService.getNodeLabelByNodeId(dataId); //
     * addMeta(allMetaIdSet, metaList, dataLabel); // Map<String, Object> dataMap =
     * neo4jService.getOneMapById(dataId); // dataMap.put(MARK_LABEL,dataLabel); //
     * addDataInfo(allDataIdSet, productData, dataId, dataMap);
     *
     * @param idMap
     * @param dataId
     */
    public void validMetaAndData(Map<Long, Map<String, Object>> idMap, Long dataId) {
	// List<Map<String, Object>> dataList = listMap.get("dataList");
	List<Map<String, Object>> metaList = listMap.get("metaList");
	Set<Long> metaIdSet = setMap.get("metaIdSet");
	// Set<Long> dataIdSet = setMap.get("dataIdSet");
	// 获取节点的标签
	String nodeLabel = neo4jService.getNodeLabelByNodeId(dataId);
	Map<String, Object> nodeData = idMap.get(dataId);
	if (nodeData == null) {
	    nodeData = neo4jService.getOneMapById(dataId);
	    if (nodeData != null) {
		idMap.put(dataId, nodeData);
	    } else {
		LoggerTool.info(logger,"dataId=" + dataId + " node not exist");
		return;
	    }
	}

	if (META_DATA.equals(nodeLabel)) {
	    // 如果是元数据,则加入元数据信息
	    if (!metaIdSet.contains(dataId)) {
		nodeData.put(MARK_LABEL, META_DATA);
		metaIdSet.add(dataId);
		metaList.add(nodeData);
		handleFieldInfo(idMap, nodeLabel, nodeData);
	    }
	} else {// 是数据节点,则加入数据节点信息以及对应的元数据信息
	    nodeData.put(MARK_LABEL, nodeLabel);
	    addDataInfo(nodeData);
	    addMeta(idMap, nodeLabel, nodeData);
	}
    }

	/**
	 * 处理字段信息的方法
	 * 该方法主要用于处理节点的字段信息，将自定义字段和关联字段的值提取并处理
	 *
	 * @param idMap 用于存储处理后的数据的地图，键为节点ID，值为该节点的字段信息
	 * @param nodeLabel 节点标签，用于获取字段信息
	 * @param nodeData 节点数据，包含节点的各种属性和值
	 */
	public void handleFieldInfo(Map<Long, Map<String, Object>> idMap, String nodeLabel, Map<String, Object> nodeData){
	    // 获取节点标签对应的字段信息
	    List<Map<String, Object>> fieldInfo=objectService.getFieldInfo(nodeLabel);
	    // 如果字段信息不为空，则进一步处理
	    if(fieldInfo!=null&&!fieldInfo.isEmpty()){
	        String key="field";
	        for(Map<String, Object> fi : fieldInfo){
	            String field=string(fi, key);// 获取字段
	            // 获取自定义字段的值.关联字段处理
	            Long value=null;
	            // 关联字段处理
	            String type=type(fi);
	            if(type==null){
	                continue;
	            }
	            try{
	                value=longValue(nodeData, field);
	                if(value==null){
	                    continue;
	                }
	            }catch(Exception e){
	                // TODO: handle exception
	                // 获取字段取值信息
	                String valueField=string(fi, VALUE_FIELD);
	                value=neo4jService.getNodeId(valueField, string(nodeData, field), type);
	            }

	            if(type!=null&&value!=null){
	                Map<String, Object> meta=neo4jService.getAttMapBy(LABEL, type, META_DATA);

	                if(meta!=null){
	                    // 验证元数据和数据的有效性
	                    validMetaAndData(idMap, value);
	                }
	            }

	        }
	    }else{
	        // 如果节点标签没有对应的字段信息，则记录日志
	        LoggerTool.info(logger, "nodeLabel no field info:"+nodeLabel);
	    }
	}

	/**
	 * 根据数据ID读取并处理外出关系信息
	 * @param dataId 数据ID
	 * @param idMap 存储节点ID与数据映射的Map
	 */
	public void readOutRelInfo(Long dataId, Map<Long, Map<String, Object>> idMap){
	    // 处理外出关系，获取关系终点ID集合
	    Set<Long> relEndIdSet=handleOutRelation(idMap, dataId);
	    for(Long endId : relEndIdSet){
	        // 跳过特定条件的节点，避免重复处理
	        if(dataId.equals(endId)||setMap.get("dataIdSet").contains(endId)
	                ||setMap.get("metaIdSet").contains(endId)){
	            continue;
	        }
	        // 处理外出节点
	        processOutNode(idMap, endId);
	    }
	}

	/**
	 * 添加数据信息到列表和集合中，避免重复添加
	 * @param data 数据信息Map
	 */
	public void addDataInfo(Map<String, Object> data){
	    // 获取数据列表和ID集合
	    List<Map<String, Object>> dataList=listMap.get("dataList");
	    Set<Long> dataIdSet=setMap.get("dataIdSet");
	    Long dataId=id(data);
	    // 如果数据ID集合不为空且不包含当前数据ID，则添加数据
	    if(dataIdSet!=null&&!dataIdSet.contains(dataId)){
	        dataIdSet.add(dataId);
	        dataList.add(data);
	    }
	}

	/**
	 * 添加元数据信息，如果元数据不存在则进行添加和处理
	 * @param idMap 存储节点ID与数据映射的Map
	 * @param nodeLabel 节点标签
	 * @param nodeData 节点数据
	 */
	public void addMeta(Map<Long, Map<String, Object>> idMap, String nodeLabel, Map<String, Object> nodeData){
	    // 尝试获取元数据
	    Map<String, Object> meta=neo4jService.getAttMapBy(LABEL, nodeLabel, META_DATA);
	    if(meta==null){
	        // 如果元数据不存在，记录日志并返回
	        LoggerTool.info(logger, "metaLabel="+nodeLabel+" is not exists META_DATA");
	        return;
	    }
	    // 获取元数据ID集合
	    Set<Long> metaIdSet=setMap.get("metaIdSet");
	    // 如果元数据ID集合不包含当前元数据ID，则更新元数据并处理字段信息
	    if(!metaIdSet.contains(id(meta))){
	        updateMetaData(meta, metaIdSet);
	        handleFieldInfo(idMap, nodeLabel, nodeData);
	    }
	}

	/**
	 * 更新元数据信息，包括添加元数据和标记
	 * @param meta 元数据Map
	 * @param metaIdSet 元数据ID集合
	 */
	public void updateMetaData(Map<String, Object> meta, Set<Long> metaIdSet){
	    // 获取元数据列表
	    List<Map<String, Object>> metaList=listMap.get("metaList");
	    // 添加元数据ID到集合，并将元数据添加到列表
	    metaIdSet.add(id(meta));
	    meta.put(MARK_LABEL, META_DATA);
	    metaList.add(meta);
	}

	/**
	 * 处理外出关系，收集关系终点节点信息
	 * @param idMap 存储节点ID与数据映射的Map
	 * @param dataIdi 数据ID
	 * @return 关系终点ID集合
	 */
	public Set<Long> handleOutRelation(Map<Long, Map<String, Object>> idMap, Long dataIdi){
	    Set<Long> relEndIdSet=new HashSet<>();
	    // 获取外出关系数据列表
	    List<Map<String, Object>> relAndData=neo4jService.outRelationDatas(dataIdi);
		if (relAndData == null || relAndData.isEmpty()) {
			return relEndIdSet;
		}
	    for(Map<String, Object> ri : relAndData){
	        // 获取关系终点ID和数据
	        Long relEndId=longValue(ri, RELATION_END_ID);
	        Map<String, Object> endData=neo4jService.getPropLabelByNodeId(relEndId);
	        List<String> endLabels=listStr(ri, RELATION_ENDNODE_LABEL);

	        // 如果ID映射中不存在当前终点ID，则根据标签信息更新终点数据
	        if(idMap.get(relEndId)==null){
	            if(endLabels==null||endLabels.isEmpty()){
	                endData.put(MARK_LABEL, label(endData));
	            }else{
	                endData.put(MARK_LABEL, endLabels.get(0));
	            }
	            idMap.put(relEndId, endData);
	        }
	        // 添加关系信息并收集终点ID
	        addRi(relEndId, ri, dataIdi);
	        relEndIdSet.add(relEndId);
	    }
	    return relEndIdSet;
	}

	public Set<Long> handleInRelation(Map<Long, Map<String, Object>> idMap, Long dataIdi){
		Set<Long> relStartIdSet=new HashSet<>();
		List<Map<String, Object>> relAndData=neo4jService.inRelationDatas(dataIdi);
		if (relAndData == null || relAndData.isEmpty()) {
			return relStartIdSet;
		}
		for(Map<String, Object> ri : relAndData){

			Map<String, Object> startNode=mapObject(ri, RELATION_STARTNODE_PROP);
			Long relStartId=id(startNode);
			List<String> startLabels=listStr(ri, RELATION_STARTNODE_LABEL);
			Map<String, Object> nodeMapById=neo4jService.getPropLabelByNodeId(relStartId);
			if(idMap.get(relStartId)==null){
				nodeMapById.put(MARK_LABEL, startLabels.get(0));
				idMap.put(relStartId, nodeMapById);
			}
			addRi(dataIdi, ri, relStartId);
			relStartIdSet.add(relStartId);
		}
		return relStartIdSet;
	}

	public void addRi(Long relEndId, Map<String, Object> ri, Long relStartId){
		List<Map<String, Object>> relList=listMap.get("relList");
		String key=string(ri, RELATION_START_ID)+string(ri, RELATION_TYPE)+string(ri, RELATION_PROP)
				+string(ri, RELATION_END_ID);
		if(!relSet.contains(key)){
			Map<String, Object> riCopy=copy(ri);
			riCopy.remove(RELATION_STARTNODE_PROP);
			riCopy.remove(RELATION_ENDNODE_PROP);
			riCopy.put(RELATION_START_ID, relStartId);
			riCopy.put(RELATION_END_ID, relEndId);
			relSet.add(key);
			relList.add(riCopy);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/exportInit", method = {RequestMethod.POST,
			RequestMethod.GET}, produces = "application/json;charset=UTF-8")
	public WrappedResult exportInit() throws DefineException{

		// 获取数据产品数据，获取系统数据。排除非产品，非系统数据。
		Set<Long> allMetaIdSet=new HashSet<>();
		Map<Long, Map<String, Object>> idMap=new HashMap<>();
		// 1、获取所有MetaData数据,
		List<Map<String, Object>> metaList=neo4jService.listDataByLabel(META_DATA);

		for(Map<String, Object> data : metaList){
			data.put(MARK_LABEL, META_DATA);
			Long mtId=id(data);
			idMap.put(mtId, data);
			allMetaIdSet.add(mtId);
		}

		List<Map<String, Object>> relList=new ArrayList<>();
		List<Map<String, Object>> nonSenseData=new ArrayList<>();

		List<Map<String, Object>> sensitives=neo4jService.listDataByLabel("sensitiveData");
		Set<String> sensitiveLables=new HashSet<>();
		for(Map<String, Object> si : sensitives){
			String labels=string(si, "metaLabel");

			String[] split=labels.split(",");
			if(split.length>1){
				for(String si2 : split){
					sensitiveLables.add(si2);
				}
			}else{
				sensitiveLables.add(labels);
			}
		}
		LoggerTool.info(logger, "=============================");
		for(Long mdIdi : allMetaIdSet){// 每个节点之间的关系数据
			List<Map<String, Object>> metaReldata=neo4jService.outRelationDatas(mdIdi);
			relList.addAll(metaReldata);
			// 导出产品数据
			String metaiLabel=null;
			Map<String, Object> metaDatai=idMap.get(mdIdi);
			if(metaDatai!=null){
				metaiLabel=label(metaDatai);
				LoggerTool.info(logger, "==============metaiLabel==============="+metaiLabel);
				if(!sensitiveLables.contains(metaiLabel)){
					LoggerTool.info(logger, "==============非敏感数据==============="+metaiLabel);
					List<Map<String, Object>> dataList=neo4jService.listDataByLabel(metaiLabel);
					markLabel(metaiLabel, dataList);
					if(!metaiLabel.equals(META_DATA)){
						nonSenseData.addAll(dataList);
					}

					LoggerTool.info(logger, "==============非敏感数据="+metaiLabel+"=nonSenseData.size============="
							+nonSenseData.size());

					List<Map<String, Object>> collectProductDataRel=collectProductDataRel(relList, dataList);
					if(!collectProductDataRel.isEmpty()){
						LoggerTool.info(logger, "==============关系数据="+metaiLabel+"=relList.size============="
								+nonSenseData.size());

						relList.addAll(collectProductDataRel);
					}
				}
			}
		}
		LoggerTool.info(logger, "=============================");

		exportData(metaList, "元数据");
		exportData(relList, "关系数据");
		exportData(nonSenseData, "非敏感数据");

		// 建立一个压缩包，囊括所有数据文件，关系文件。
		return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
	}

    /**
     * 定时清理脏数据
     * 
     * @return
     * @throws DefineException
     */
    /*@ResponseBody
    @RequestMapping(value = "/clear", method = { RequestMethod.POST,
        RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @Scheduled(cron = "0 0 5 ? * MON-FRI")
    public WrappedResult clearDirty() throws DefineException {
    
    String filePath = neo4jService.getPathBy(FILE_STORE_PATH);
    if (filePath == null) {
        return ResultWrapper.failed("FilePath is null");
    }
    File file = new File(filePath);
    if (file.exists()) {
        String[] myFiles = file.list();
        Set<String> fileSet = new HashSet<>(myFiles.length);
        fileSet.addAll(List.of(myFiles));
        String query = Neo4jOptCypher.queryObj(FILE, "id,fileStoreName".split(","));
        List<Map<String, Object>> query2 = neo4jService.cypher(query);
        List<Long> dirty = new ArrayList<>();
        for (Map<String, Object> fi : query2) {
    	Long nodeId = id(fi);
    	if (!fileSet.contains(nodeId)) {
    	    dirty.add(nodeId);
    	}
        }
        neo4jService.removeNodeList(dirty);
    }
    
    // 建立一个压缩包，囊括所有数据文件，关系文件。
    return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }*/

    private List<Map<String, Object>> collectProductDataRel(List<Map<String, Object>> reldataList,
	    List<Map<String, Object>> pdList) {
	List<Map<String, Object>> manageList = new ArrayList<>();
	for (Map<String, Object> producti : pdList) {
	    Long longValue = id(producti);
	    List<Map<String, Object>> pdRelation = neo4jService.outRelationDatas(longValue);
	    if (!pdRelation.isEmpty()) {
		reldataList.addAll(pdRelation);
	    }
	    handleManagedNode(reldataList, manageList, producti);
	}
	return manageList;
    }

    /**
     * 处理管理数据
     * 
     * @param reldataList
     * @param manageList
     * @param producti
     */
    private void handleManagedNode(List<Map<String, Object>> reldataList, List<Map<String, Object>> manageList,
	    Map<String, Object> producti) {
	String nodeLabel = neo4jService.getNodeLabelByNodeId(id(producti));

	String manageLabel = string(producti, MANAGE_LABEL);
	if (manageLabel != null && !manageLabel.equals(nodeLabel)) {
	    List<Map<String, Object>> m2List = neo4jService.listDataByLabel(manageLabel);
	    markLabel(manageLabel, m2List);
	    manageList.addAll(m2List);
	    for (Map<String, Object> mi : m2List) {
		Long miId = id(mi);
		List<Map<String, Object>> outRelationDatas = neo4jService.outRelationDatas(miId);
		if (outRelationDatas != null && !outRelationDatas.isEmpty()) {
		    reldataList.addAll(outRelationDatas);
		}
	    }
	}
    }

    private void markLabel(String nodeLabel, List<Map<String, Object>> pdList) {
	for (Map<String, Object> pi : pdList) {
	    pi.put(MARK_LABEL, nodeLabel);
	}
    }

    private void exportData(List<Map<String, Object>> reldataList, String data) {
	if (!reldataList.isEmpty()) {
	    Map<String, Object> fileMap = new HashMap<>();
	    String fileName = DateUtil.nowTime() + "_" + data + ".json";
	    fileMap.put(NAME, fileName);
	    LoggerTool.info(logger,fileName);
	    StringBuilder sb = new StringBuilder();

	    for (Map<String, Object> di : reldataList) {
		if (sb.length() < 1) {
		    sb.append("[");
		} else {
		    sb.append(",");
		}
		sb.append(JSON.toJSONString(di));
	    }
	    sb.append("]");
	    userNeo4j.goodsPersistSave(sb.toString(), fileMap);
	}
    }

    private void exportNodeData(Object nodeName, List<String> aaList, Set<Long> endsNode) {
	List<Map<String, Object>> nodeList = new ArrayList<>(endsNode.size());
	for (Long endi : endsNode) {
	    Map<String, Object> propMapByNodeId = neo4jService.getPropMapByNodeId(endi);
	    String nodeLabeli = neo4jService.getNodeLabelByNodeId(endi);
	    if (nodeLabeli.equals(META_DATA)) {
		propMapByNodeId.put(META_DATA, true);
	    } else {
		if (propMapByNodeId.containsKey(LABEL) && !nodeLabeli.equals(propMapByNodeId.get(LABEL))) {
		    propMapByNodeId.put(MANAGE_LABEL, nodeLabeli);
		} else {
		    propMapByNodeId.put(LABEL, nodeLabeli);
		}
	    }
	    nodeList.add(propMapByNodeId);
	}

	String jsonString = JSONUtils.toJSONString(nodeList);
	Map<String, Object> fileMap = new HashMap<>();
	fileMap.put(NAME, nodeName);
	String pathname = userNeo4j.goodsPersistSave(jsonString, fileMap);
	// System.out.println(jsonString);
	aaList.add(pathname);
    }

    private String exportNodeData(Set<Long> endsNode) {
	List<Map<String, Object>> nodeList = new ArrayList<>(endsNode.size());
	for (Long endi : endsNode) {
	    Map<String, Object> propMapByNodeId = neo4jService.getPropMapByNodeId(endi);
	    String nodeLabeli = neo4jService.getNodeLabelByNodeId(endi);
	    if (nodeLabeli.equals(META_DATA)) {
		propMapByNodeId.put(META_DATA, true);
	    } else {
		if (propMapByNodeId.containsKey(LABEL) && !nodeLabeli.equals(propMapByNodeId.get(LABEL))) {
		    propMapByNodeId.put(MANAGE_LABEL, nodeLabeli);
		} else {
		    propMapByNodeId.put(LABEL, nodeLabeli);
		}

	    }
	    nodeList.add(propMapByNodeId);
	}

	return JSONUtils.toJSONString(nodeList);
    }

    /**
     * 根据商品收集数据，导出数据，和依赖数据。递归找出关系网中的节点
     * 
     * @param goodsId
     * @param relationDataMap
     */
    private void getExportGraphData(Long goodsId, Map<String, Set<Long>> relationDataMap, Set<Long> allIdSet) {
	List<Map<String, Object>> outRelations = neo4jService.outRelationDatas(goodsId);
	if (outRelations == null || outRelations.isEmpty()) {
	    return;
	}
	for (Map<String, Object> ori : outRelations) {
	    // Map<String, Object> relMap = (Map<String, Object>) ori.get(RELATION_PROP);
	    Long endNodeId = longValue(ori, RELATION_END_ID);
	    String relType = (String) ori.get(RELATION_TYPE);
	    Set<Long> relationEndNodeSet = relationDataMap.get(relType);
	    if (relationEndNodeSet == null) {
		relationEndNodeSet = new HashSet<>();
	    }

	    if (!relationEndNodeSet.contains(endNodeId)) {
		relationEndNodeSet.add(endNodeId);
		relationDataMap.put(relType, relationEndNodeSet);
		if (!allIdSet.contains(endNodeId)) {
		    allIdSet.add(endNodeId);
		    getExportGraphData(endNodeId, relationDataMap, allIdSet);
		}
	    }
	    // System.out.println(relType + mapString(relMap) +
	    // mapString(endPo));
	}
    }

    /**
     * 导入功能，需要添加索引
     * 
     * @param vo
     * @return
     * @throws DefineException
     */
    @RequestMapping(value = "/import", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult importQuery(@RequestBody JSONObject vo) throws DefineException {
	String goodsId = vo.getString(ID);
	String path = (String) neo4jService.getValueByNodeIdAndAttKey(goodsId, FILE_STORE_NAME);
	String importFileData = FileOpt.getImportFileData(path);
	JSONArray parseObject = JSON.parseArray(importFileData);
	/*
	 * for(JSONObject datai:) {
	 * 
	 * }
	 */
	return ResultWrapper.wrapResult(true, parseObject, null, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/service", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult service(@RequestBody JSONObject vo) throws DefineException {
	String goodsId = vo.getString(ID);
	goodsService.useGoods(goodsId);
	return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/share/{id}", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult share(@PathVariable("id") String id, @RequestBody JSONObject vo) throws DefineException {
	if (id == null) {
	    throw new DefineException(id + "对象不存在，分析异常！");
	}
	Map<String, Object> propMapByNodeId = neo4jService.getPropMapByNodeId(Long.valueOf(id));
	if (propMapByNodeId == null) {
	    throw new DefineException(id + "对象不存在，分析异常！");
	}
	if (wsClient != null) {
	    wsClient.sentCmd(propMapByNodeId, DATA);
	} else {
	    Map<String, Object> share = restApi.sent(propMapByNodeId, DATA);
	    return ResultWrapper.wrapResult(true, share, null, QUERY_SUCCESS);
	}

	return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }

    /**
     * 加入购物车
     * 
     * @param id
     * @param vo
     * @return
     * @throws DefineException
     */
    @RequestMapping(value = "/addCart/{id}", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult addCart(@PathVariable("id") String id, @RequestBody JSONObject vo) throws DefineException {
	if (id == null) {
	    throw new DefineException(id + "对象不存在！");
	}
	Map<String, Object> propMapByNodeId = neo4jService.getPropMapByNodeId(Long.valueOf(id));
	if (propMapByNodeId == null) {
	    throw new DefineException(id + "对象不存在！");
	}
	Map<String, Object> data = newMap();
	// 我要清单：
	// data.put("owner", adminService.getCurrentUserId());
	data.put("subjectId", id);
	data.put("userId", adminService.getCurrentUserId());

	neo4jService.save(data, "IWant");

	return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }

    /**
     * 加入购物车
     * 
     * @param id
     * @param vo
     * @return
     * @throws DefineException
     */
    @RequestMapping(value = "/iWant/{id}", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult iWant(@PathVariable("id") String id, @RequestBody JSONObject vo) throws DefineException {
	if (id == null) {
	    throw new DefineException(id + "对象不存在！");
	}
	Map<String, Object> propMapByNodeId = neo4jService.getPropMapByNodeId(Long.valueOf(id));
	if (propMapByNodeId == null) {
	    throw new DefineException(id + "对象不存在！");
	}
	Map<String, Object> data = newMap();
	// 我要清单：
	// data.put("owner", adminService.getCurrentUserId());
	data.put("subjectId", id);
	if (name(vo) != null) {
	    data.put(NAME, name(vo));
	}

	if (code(vo) != null) {
	    data.put(CODE, code(vo));
	}
	if (label(vo) != null) {
	    data.put(LABEL, label(vo));
	}
	data.put("creator", adminService.getCurrentUserId());

	neo4jService.save(data, "IWant");

	return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/file", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult goodsFile(@RequestBody JSONObject vo) throws DefineException {
	Long goodsId = vo.getLong(ID);
	Map<String, Set<Long>> relationEndIdMap = new HashMap<>();
	Set<Long> allIdSet = new HashSet<>();
	getExportGraphData(goodsId, relationEndIdMap, allIdSet);
	Object nodeName = neo4jService.getPropValueByNodeId(goodsId, NAME);
	String path = userNeo4j.getPathBy(FILE_STORE_PATH);
	String zipFIleName = path + nodeName + FILE_TYPE_ZIP;

	try (OutputStream outputStream = new FileOutputStream(zipFIleName);
		// 获取压缩文件输出流
		ZipOutputStream out = new ZipOutputStream(outputStream);) {
	    List<Map<String, Object>> reldataList = new ArrayList<>();
	    String exportNodeData = exportNodeData(allIdSet);
	    ZipFile.zipFile(out, exportNodeData.getBytes(), nodeName + "数据.json");
	    for (Long endi : allIdSet) {// 每个节点之间的关系数据
		List<Map<String, Object>> reldata = neo4jService.outRelationDatas(endi);
		reldataList.addAll(reldata);
	    }

	    if (!reldataList.isEmpty()) {
		String jsonString = JSONUtils.toJSONString(reldataList);
		ZipFile.zipFile(out, jsonString.getBytes(), nodeName + "_关系数据.json");
	    }
	    out.closeEntry();
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	String pathname = userNeo4j.fileMetaSave(new File(zipFIleName), vo, FILE_TYPE_ZIP);
	// 建立一个压缩包，囊括所有数据文件，关系文件。
	return ResultWrapper.wrapResult(true, pathname, null, QUERY_SUCCESS);
    }
}
