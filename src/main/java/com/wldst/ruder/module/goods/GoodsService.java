package com.wldst.ruder.module.goods;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.module.schedule.BeanShellScheduler;
import com.wldst.ruder.util.*;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.crud.service.CrudNeo4jDriver;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.GoodsDomain;
import com.wldst.ruder.module.state.sys.SystemStates;
import com.wldst.ruder.module.ws.client.RuderWsClient;

@Component
public class GoodsService extends GoodsDomain {

    @Value(value = "${file.data}")
    private String initData;
    @Value(value = "${file.update}")
    private String updateData;
    @Value(value = "${server.port}")
    private String serverPort;
    @Value(value = "${server.root.host}")
    private String rootHost;
    @Value(value = "${http.port}")
    private String port;
    private String goodsRemind = "原创不易，如对此软件感兴趣，\n联系方式：15828264059，\n微信号：w15828264059，\nQQ号：1721903353,\nQQ群：276174968";

    private CrudNeo4jDriver driver;
    private CrudNeo4jService neo4jService;
    private RestApi rest;
    private RuderWsClient wsClient;
    private BeanShellScheduler bss;
    @Autowired
    private RelationService relationService;
    private String sysOwner;
    private static Logger logger = LoggerFactory.getLogger(GoodsService.class);

    public GoodsService(CrudNeo4jDriver driver, CrudNeo4jService neo4jService, RestApi rest, RuderWsClient wsClient, BeanShellScheduler bss){
        this.driver=driver;
        this.neo4jService=neo4jService;
        this.rest=rest;
        this.wsClient=wsClient;
        this.bss=bss;
    }


    public void init() {
        LoggerTool.debug(logger,"The Runner start to initialize ...");
        driver.getInstance();
        Boolean initedBoolean = false;
        Map<String, Object> systemMap = new HashMap<>();
        systemMap.put(NAME, SYSTEM_LABEL);
        Node queryNode = driver.queryNode(systemMap, SYSTEM_LABEL);
        if (queryNode != null) {
            Map<String, Object> nodeProperties = driver.getNodeProperties(queryNode);
            String status = string(nodeProperties, STATE);
            boolean initInt = false;
            if (ValidateUtil.isNum(status)) {
                initInt = SystemStates.INITED.getValue() == Integer.valueOf(status);
            }
            if (status != null && (SystemStates.INITED.equals(status) || initInt)) {
                initedBoolean = true;
            }
        }

        if (initedBoolean) {

            importData(updateData);
            if (NetHandleUtil.isWindowsOS()) {
                openBroswer();
            }
            // 启动后online
            try {
                String localHostName = NetHandleUtil.getLocalHostName();
                String localIpAddress = NetHandleUtil.getLocalIpAddress();
                Map<String, Object> body = new HashMap<>();
                body.put(HOST_NAME, localHostName);
                body.put(IP_ADDRESS, localIpAddress);
                body.put(MAC_ADDRESS, NetHandleUtil.getMacAddress());
                body.put(CLIENT_PORT, port);
                body.put(CLIENT_HTTPS_PORT, serverPort);
                body.put(CREATETIME, Calendar.getInstance().getTimeInMillis());
                List<Map<String, Object>> systemOwner = neo4jService.listAllByLabel(OWNER);

                Map<String, Object> attMapBy = neo4jService.getAttMapBy(CODE, "remindGoods", SETTINGS);
                if (attMapBy != null) {
                    String remindGoods = value(attMapBy);
                    if (remindGoods != null) {
                        goodsRemind = remindGoods;
                    }
                }

                if (systemOwner == null || systemOwner.isEmpty()) {
                    LoggerTool.info(logger,"\n" + goodsRemind, localHostName, localIpAddress);
                } else {
                    // 自动清除Owner信息，crud传送
                    Map<String, Object> map = systemOwner.get(0);
                    if (map == null) {
                        LoggerTool.debug(logger,"\n" + goodsRemind);
                    }
                }

                LoggerTool.info(logger,"localHostName:{}========localIpAddress:{}", localHostName, localIpAddress);
                if (!localIpAddress.equals(rootHost)) {
                    rest.online(body);
                    wsClient.sentCmd(body, "client");
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                ;
                LoggerTool.error(logger,e.getMessage(),e);
            }
            return;
        }
        LoggerTool.debug(logger," init neo4j ...");
        // 初始化数据
        importData(initData);
        if (NetHandleUtil.isWindowsOS()) {
            openBroswer();
        }
        systemMap.put(STATE, SystemStates.INITED.getValue());
        if (queryNode == null) {
            driver.createNode(systemMap, Label.label(SYSTEM_LABEL));
        } else {
            driver.updateNode(systemMap, queryNode);
        }
        try{
            bss.taskRefresh();
        }catch(SchedulerException e){
            throw new RuntimeException(e);
        }
    }

    private void openBroswer() {
        // String serverMode = neo4jUserService.getBySysCode("serverMode");
        // if (serverMode != null && serverMode.toLowerCase().equals("https")) {
        BareBonesBrowserLaunch.openURL("https://127.0.0.1:" + serverPort + LemodoApplication.MODULE_NAME + "/desktop");
        // } else {
        // BareBonesBrowserLaunch.openURL("http://localhost:" + port + LemodoApplication.MODULE_NAME+"/desktop");
        // }
    }

    /**
     * 导入初始化数据，同时也存在判断更新数据。
     *
     * @param initFile
     */
    private void importData(String initFile) {
        File inidir = new File(initFile);
        if (inidir.exists() && inidir.isDirectory()) {
            Map<Long, Long> idRefresh = new HashMap<>();
            Map<Long, Map<String, Object>> oldIdMapData = new HashMap<>();
            List<Map<String, Object>> reList = new ArrayList<>();
            List<Map<String, Object>> dataList = new ArrayList<>();
            List<Map<String, Object>> relateFieldList = new ArrayList<>();
            Set<String> savedLabels = new HashSet<>();
            // 主外键关系,对象ID,应该是元数据,元数据的当前
            Map<Long, String> objectFieldNeedUpdate = new HashMap<>();

            setSystemOwner(inidir);
            for (File fi : inidir.listFiles()) {
                if (!fi.getName().endsWith("json")) {
                    System.out.println(fi.getName());
                    continue;
                }
                String read = FileOpt.readUtf8(fi);
                JSONArray parseObject = JSON.parseArray(read);

                clearDuplicateData(parseObject);
                LoggerTool.info(logger,"fi==================" + fi.getName());
                for (Object joi : parseObject) {
                    if (joi == null) {
                        continue;
                    }
                    JSONObject datai = (JSONObject) joi;
                    boolean isRel = datai.get(RELATION_START_ID) != null && datai.get(RELATION_END_ID) != null;

                    if (!isRel) {
                        Long dataId = id(datai);
                        if (dataId != null) {
                            oldIdMapData.put(dataId, datai);
                            Label nodeLabel = null;
                            String markLabel = string(datai, MARK_LABEL);

                            if (markLabel != null) {
                                if (!"Field".equals(markLabel) && !META_DATA.equals(markLabel)) {
                                    dataList.add(datai);
                                } else {// collect relateField
                                    if ("Field".equals(markLabel) && bool(datai, "isPo")) {// 解析字段关联
                                        relateFieldList.add(datai);
                                        // 统计字段关联其他对象ID的情况
                                        collectObjectFieldNeedUpdate(objectFieldNeedUpdate, datai);
                                    }
                                }
                            } else {
                                LoggerTool.error(logger,"nodeLabel is null {}", mapString(datai));
                            }
                            // 解决冲突，低版本对高版本的影响，首先是元数据，再说数据。
                            if (META_DATA.equals(markLabel)) {
                                String label2 = label(datai);
								if(label2==null){
									continue;
								}

                                Map<String, Object> dbData = neo4jService.getAttMapBy(LABEL, label2, markLabel);
                                if (dbData != null) {
                                    if (!isNeedUpdate(idRefresh, datai, dbData)) {
                                        idRefresh.put(dataId, id(dbData));
                                        continue;
                                    }
                                }

                                if (!savedLabels.contains(label2)) {
                                    nodeLabel = Label.label(label2);
                                    saveMetaData(idRefresh, datai, dataId, nodeLabel, markLabel);
                                    savedLabels.add(label2);
                                } else {
                                    LoggerTool.info(logger,"metaData===had==added===label===========" + label2);
                                }
                            } else {
                                String label2 = label(datai);
                                Map<String, Object> nonMetaData = copyMap(datai);
                                nonMetaData.remove(ID);
                                nonMetaData.remove(MARK_LABEL);
                                if (label2 == null) {
                                    label2 = markLabel;
                                }
                                List<Map<String, Object>> dbDatas = neo4jService.queryBy(nonMetaData, markLabel);

                                if (dbDatas != null && !dbDatas.isEmpty()) {
                                    long[] nums = new long[dbDatas.size()];
                                    int i = 0;
                                    Map<Long, Map<String, Object>> dd = new HashMap<>();
                                    for (Map<String, Object> ei : dbDatas) {
                                        nums[i] = id(ei);
                                        i++;
                                    }
                                    Arrays.sort(nums); // 对数组进行排序
                                    long minId = nums[0]; // 获取最小值
                                    idRefresh.put(dataId, minId);
                                    for (int k = 1; k < nums.length; k++) {
                                        neo4jService.delete(nums[k]);
                                    }
                                    Map<String, Object> map = dd.get(minId);
                                    if (map != null && isNeedUpdate(idRefresh, datai, map)) {
                                        updateInfo(datai, sysOwner);
                                        neo4jService.update(datai, minId);
                                    }
                                } else {
                                    saveNodeData(idRefresh, datai, dataId, markLabel);
                                }

                            }
                        } else {
                            //没有ID的是造的数据
                            String markLabel = string(datai, MARK_LABEL);
                            dataList.add(datai);
                            saveNodeData(datai, markLabel);
                        }

                    } else {
                        reList.add(datai);
                    }
                }
            }

            // 更新字段定义：
            refreshFieldInfo(idRefresh, relateFieldList);
            // 更新parentId,ObjectId,各种关联的ID。这个改如何处理？查看字段是如何制作的？，
            // 替换数据中存在的各种ID为新版的ID
            refreshDataFk(idRefresh, dataList, objectFieldNeedUpdate);

            if (!reList.isEmpty()) {// 有明确关系的关系更新
                for (Map<String, Object> reMap : reList) {

                    Long startId = idRefresh.get(longValue(reMap, RELATION_START_ID));
                    Long endId = idRefresh.get(longValue(reMap, RELATION_END_ID));
                    // string(reMap, RELATION_TYPE);
                    if (startId == null || endId == null) {
                        LoggerTool.error(logger,"startId：{}，endId：{}====" + mapString(reMap), startId, endId);
                        Map<String, Object> startObject = mapObject(reMap, RELATION_STARTNODE_PROP);
                        Map<String, Object> endObject = mapObject(reMap, RELATION_ENDNODE_PROP);
                        startId = idRefresh.get(id(startObject));
                        endId = idRefresh.get(id(endObject));
                    }
                    LoggerTool.info(logger,"===create  relation======sid=" + startId + "=====eId=" + endId + "========"
                            + mapString(reMap));
                    if (endId != null && startId != null && !startId.equals(endId)) {
                        Map<String, Object> propMap = mapObject(reMap, RELATION_PROP);
                        relationService.validRelate(string(reMap, RELATION_TYPE), startId, endId, propMap);
                    }
                }
            }
            // 更新拥有者:
            // if(sysOwner!=null) {
            // neo4jService.query("MATCH(n) SET n.creator='"+sysOwner+"'" );
            //
            // }

        }
    }

    private void clearDuplicateData(JSONArray parseObject) {
        // Set<String>
        // for (Object joi : parseObject) {
        // JSONObject datai = (JSONObject) joi;
        // }
    }

    public void setSystemOwner(File inidir) {
        for (File fi : inidir.listFiles()) {
            if (!fi.getName().endsWith("json")) {
                System.out.println(fi.getName());
                continue;
            }
            if (sysOwner != null) {
                break;
            }
            String read = FileOpt.readUtf8(fi);
            JSONArray parseObject = JSON.parseArray(read);
            LoggerTool.info(logger,"fi==================" + fi.getName());
            for (Object joi : parseObject) {
                if (joi == null) {
                    continue;
                }
                JSONObject datai = (JSONObject) joi;
                if (datai != null && "System".equals(datai.get(MARK_LABEL))) {
                    String string = string(datai, "owner");
                    sysOwner = string;
                    break;
                }
            }
        }
    }

    public Boolean isNeedUpdate(Map<Long, Long> idRefresh, JSONObject datai, Map<String, Object> dbData) {
        if (dbData != null) {
            return false;
        }
        Integer dbVersion = version(dbData);
        Integer dataVersion = version(datai);
        if (dbVersion != null && dataVersion != null && dbVersion >= dataVersion) {
            return false;
        } else {
            if (datai.get(CREATETIME) != null && dbData.get(CREATETIME) != null) {
                Date c1 = date(datai, CREATETIME);
                Date c2 = date(dbData, CREATETIME);
                if (null != datai.get(UPDATETIME) && null != dbData.get(UPDATETIME)) {
                    Date u1 = date(datai, UPDATETIME);
                    Date u2 = date(dbData, UPDATETIME);
                    boolean updateBefore = u1 != null && u2 != null && u1.before(u2);
                    boolean createBefore = c1 != null && c2 != null && c1.before(c2);
                    if (updateBefore && createBefore) {
                        return false;
                    }
                }
            }

            boolean columnsEquals = columnsString(datai).equals(columnsString(dbData));
            boolean headerEquals = headersString(datai).equals(headersString(dbData));
            if (columnsEquals && headerEquals) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取要更新的外键信息
     *
     * @param fieldFKMap
     * @param datai
     */
    private void collectObjectFieldNeedUpdate(Map<Long, String> fieldFKMap, JSONObject datai) {
        String valueField = string(datai, "valueField");
        String field = string(datai, "field");
        if (valueField == null) {
            valueField = ID;
        }
        if (field == null) {
            return;
        }

        Long ojbectId = longValue(datai, "objectId");
        if (ojbectId == null) {
            return;
        }
        String typex = string(datai, "poId");

        String fkField = fieldFKMap.get(ojbectId);
        String relateField = field + "=>" + typex + "-" + valueField;
        if (fkField != null) {
            String[] split = fkField.split(",");
            boolean contain = false;
            for (String si : split) {
                if (si.equals(relateField)) {
                    contain = true;
                    break;
                }
            }
            if (!contain) {
                fieldFKMap.put(ojbectId, fkField + "," + relateField);
            }
        } else {
            fieldFKMap.put(ojbectId, relateField);
        }
    }

    /**
     * 保存元数据信息
     *
     * @param idMap 存储旧节点ID与新节点ID的映射
     * @param metai 节点的元数据信息
     * @param oldNodeId 旧节点的ID
     * @param nodeLabel 节点的标签
     * @param markLabel 用于查询的标记标签
     */
    private void saveMetaData(Map<Long, Long> idMap, JSONObject metai, Long oldNodeId, Label nodeLabel,
                              String markLabel) {

        // 检查标记标签是否有效
        if (markLabel != null && !"".equals(markLabel.trim())) {
            // 复制元数据并移除不必要的字段
            Map<String, Object> importData = copyMap(metai);
            importData.remove(ID);
            importData.remove(MARK_LABEL);

            // 根据标记标签查询数据库
            List<Map<String, Object>> query = driver.query(importData, markLabel);
            if (query != null && !query.isEmpty()) {
                // 处理查询结果，获取所有匹配记录的ID，并按ID排序
                long[] nums = new long[query.size()];
                int i = 0;
                for (Map<String, Object> ei : query) {
                    nums[i] = id(ei);
                    i++;
                }
                Arrays.sort(nums); // 对数组进行排序
                long minNum = nums[0]; // 获取最小值
                idMap.put(oldNodeId, minNum);
                // 删除除最小ID外的其他记录
                for (int k = 1; k < nums.length; k++) {
                    neo4jService.delete(nums[k]);
                }
                return;
            }
            // 如果没有查询到记录，创建新的节点
            creatInfo(importData, sysOwner);
            Node createNode = driver.createNode(importData, markLabel);
            if (createNode != null) {
                // 记录日志并更新ID映射
                LoggerTool.info(logger,"====save===" + nodeLabel.name() + "===newId==" + createNode.getId() + "========="
                        + importData);
                idMap.put(oldNodeId, createNode.getId());
            } else {
                // 如果创建节点失败，记录错误日志
                LoggerTool.error(logger,"createNode is null {}", mapString(metai));
            }
        } else {
            // 如果标记标签无效，记录错误日志
            LoggerTool.error(logger,"====nodeLabel is null==============" + mapString(metai));
        }
    }

    private void saveNodeData(Map<Long, Long> idMap, JSONObject datai, Long oldNodeId,
                              String markLabel) {
        if (markLabel != null && !"".equals(markLabel.trim())) {
            creatInfo(datai, sysOwner);
            Long newId = saveNodeData(datai, markLabel);
            idMap.put(oldNodeId, newId);
        } else {
            LoggerTool.error(logger,"====nodeLabel is null==============" + mapString(datai));
        }
    }

    public Map<String, Object> freeMember(Long cuId) {
        Map<String, Object> member = newMap();
        member.put("userId", cuId);
        member.put("memberId", neo4jService.getNodeId("name", "免费会员", "Member"));
        member.put("startTime", DateUtil.nowDateTime());
        member.put("MARK_LABEL", "BuyRecord");
        member.put(CREATOR, "system");
        return member;
    }

    public Map<String, Object> createFree(Long cuId) {
        Map<String, Object> member = newMap();
        member.put("userId", cuId);
        member.put("memberId", neo4jService.getNodeId("name", "免费会员", "Member"));
        member.put("startTime", DateUtil.nowDateTime());
        member.put("endTime", DateUtil.getNextMonth(DateUtil.nowDateTime()));
        member.put(CREATOR, "system");
        neo4jService.save(member, "BuyRecord");
        return member;
    }

    /**
     * 保存节点数据
     * 此方法负责将给定的JSON对象数据保存为图数据库中的一个节点如果给定的标记标签为null，则会根据数据生成一个
     * 在保存之前，会根据数据中的父ID（如果有）来调整要保存的数据结构最后，会创建一个新的节点并返回其ID
     *
     * @param datai 要保存的节点数据，封装在一个JSONObject中
     * @param markLabelx 节点的标记标签，用于图数据库中的分类，可以为null
     * @return 返回新创建节点的ID
     */
    private Long saveNodeData(JSONObject datai,
                                  String markLabelx) {
        // 初始化标记标签，如果未提供则根据节点数据生成
        String markLabel = markLabelx;
        // 拷贝节点数据以避免修改原始数据结构
        Map<String, Object> importData = copyMap(datai);
        if (markLabel == null) {
            markLabel = label(datai);
        }
        // 移除不需要保存到数据库中的字段
        importData.remove(ID);
        importData.remove(MARK_LABEL);
        if (datai.get(PARENT_ID) != null) {
            importData.remove(PARENT_ID);
        }
        // 查询数据库中已存在的相似节点数据
        List<Map<String, Object>> query = driver.query(importData, markLabel);
        if (query != null && !query.isEmpty()) {
            // 对查询结果中的节点ID进行排序
            long[] nums = new long[query.size()];
            int i = 0;
            for (Map<String, Object> ei : query) {
                nums[i] = id(ei);
                i++;
            }
            Arrays.sort(nums); // 对数组进行排序
            // 删除已存在的相似节点，为新节点的创建腾出空间
            for (int k = 0; k < nums.length; k++) {
                neo4jService.delete(nums[k]);
            }
        }
        // 如果节点有父节点，重新添加父节点ID到数据中
        if (datai.get(PARENT_ID) != null) {
            importData.put(PARENT_ID, datai.get(PARENT_ID));
        }
        // 创建新节点
        Node createNode = driver.createNode(importData, markLabel);
        return createNode.getId();
    }

    public void creatInfo(Map<String, Object> importData, String owner) {
        importData.put(CREATOR, owner);
        importData.put(CREATETIME, Calendar.getInstance().getTimeInMillis());
    }

    public void updateInfo(Map<String, Object> importData, String owner) {
        importData.put(UPDATOR, owner);
        importData.put(UPDATETIME, Calendar.getInstance().getTimeInMillis());
    }

    /**
     * //更新字段定义,更新自定义字段信息，涉及到关联对象的字段
     *
     * @param idMap
     * @param relateFieldList
     */
    private void refreshFieldInfo(Map<Long, Long> idMap, List<Map<String, Object>> relateFieldList) {
        for (Map<String, Object> fieldi : relateFieldList) {
            // 更新字段的所属数据节点
            Long oldDataId = longValue(fieldi, "objectId");
            if (oldDataId == null) {
                continue;
            }
            Long newId = idMap.get(oldDataId);
            if (newId == null) {
                LoggerTool.info(logger,"===={}====has no new node========", oldDataId);
                continue;
            }

            fieldi.put("objectId", newId);
            // updateNew Field
            Long newNode = idMap.get(id(fieldi));
            LoggerTool.info(logger,"===update Field==oldId=" + id(fieldi) + "=====newID=" + newNode + "============"
                    + mapString(fieldi));
            updateInfo(fieldi, sysOwner);
            driver.updateNode(fieldi, newNode);
        }
    }

    /**
     * //更新parentId,ObjectId,各种关联的ID。这个改如何处理？查看字段是如何制作的？， //替换数据中存在的各种ID为新版的ID
     *
     * @param idMap
     * @param dataList
     * @param fieldFKMap
     */
    private void refreshDataFk(Map<Long, Long> idMap, List<Map<String, Object>> dataList,
                               Map<Long, String> fieldFKMap) {
        for (Map<String, Object> di : dataList) {
            Long dataOldId = id(di);

            Long dataNewId = idMap.get(dataOldId);
            Long oldParentId = parentId(di);

            String labelx = neo4jService.getNodeLabelByNodeId(dataNewId);
            Map<String, Object> metai = neo4jService.getAttMapOf(labelx);

            String relateFields = fieldFKMap.get(id(metai));
            if (relateFields != null) {
                String[] fks = relateFields.split(",");
                for (String fki : fks) {
                    if (fki != null && !"".equals(fki.trim())) {
                        try {
                            String[] metaField = fki.split("=>");
                            String valueField = metaField[1];
                            String[] targetTypeField = valueField.split("-");
                            String ti = targetTypeField[0];
                            String fi = targetTypeField[1];
                            String fieldi = metaField[0];

                            if (fi.equals(ID)) {
                                Long fiValue = longValue(di, fieldi);
                                Long newId = idMap.get(fiValue);
                                if (newId == null) {
                                    LoggerTool.info(logger,"==newData=is=null===" + fki + "=" + fiValue + "=====newID============="
                                            + mapString(di));
                                } else {
                                    di.put(fieldi, newId);
                                    LoggerTool.info(logger,"==update saveById====" + fieldi + "=" + newId + "=====dataNewId="
                                            + dataNewId + "============" + mapString(di));
                                    updateInfo(di, sysOwner);
                                    neo4jService.update(di, dataNewId);
                                }
                            } else {
                                String dataFKi = string(di, fieldi);
                                Map<String, Object> fkObject = neo4jService.getAttMapBy(fi, dataFKi, ti);
                                di.put(fieldi, id(fkObject));
                                updateInfo(di, sysOwner);
                                neo4jService.update(di, dataNewId);
                            }

                        } catch (Exception e) {
                            continue;
                        }
                    }
                }
            }

            if (oldParentId != null) {
                if (relateFields == null || relateFields.indexOf(PARENT_ID) < 0) {
                    Long newParentId = idMap.get(oldParentId);
                    if (newParentId != null) {
                        di.put(PARENT_ID, newParentId);
                        LoggerTool.info(logger,"==update=oldParentId===" + oldParentId + "=====new parentid==" + newParentId
                                + "============" + mapString(di));
                        Long newNode = idMap.get(dataOldId);
                        updateInfo(di, sysOwner);
                        neo4jService.update(di, newNode);
                    } else {
                        LoggerTool.info(logger,"====oldParentId=" + oldParentId
                                + "====new parentid=is=null========newID=============" + mapString(di));
                    }
                }
            }
        }
    }

    /**
     * 使用
     * 根据货物ID获取存储路径，解压文件并解析为JSON数组，然后创建节点或关系
     *
     * @param goodsId 货物ID，用于获取货物的存储路径
     */
    public void useGoods(String goodsId) {
        // 根据货物ID获取文件存储路径
        String path = (String) neo4jService.getValueByNodeIdAndAttKey(goodsId, FILE_STORE_NAME);
        // 解压文件并解析为JSON数组列表
        List<JSONArray> unzip2JsonArrays = ZipFile.unzip2JsonArrays(path);
        // 存储节点ID与节点对象的映射
        Map<String, Node> idMap = new HashMap<>();
        // 存储关系数据
        List<Map<String, Object>> reList = new ArrayList<>();

        // 遍历解析后的JSON数组列表
        for (JSONArray parseObject : unzip2JsonArrays) {
            for (Object joi : parseObject) {
                JSONObject jo = (JSONObject) joi;
                // 判断当前对象是否为关系数据
                boolean isRel = jo.get(RELATION_START_ID) != null && jo.get(RELATION_END_ID) != null;

                if (!isRel) {
                    // 处理节点数据
                    String oldIdString = jo.getString(ID);
                    jo.remove(ID);
                    Node createNode = null;
                    Label nodeLabel = null;

                    // 根据节点类型创建标签
                    if (jo.getBooleanValue(META_DATA)) {
                        nodeLabel = Label.label(META_DATA);
                    } else {
                        String mLabel = jo.getString(MANAGE_LABEL);
                        if (mLabel != null && !mLabel.equals(jo.getString(LABEL))) {
                            nodeLabel = Label.label(jo.getString(MANAGE_LABEL));
                        } else {
                            nodeLabel = Label.label(jo.getString(LABEL));
                        }
                    }
                    // 创建节点
                    createNode = driver.createNode(jo, nodeLabel);

                    // 将节点ID与节点对象存储到映射中
                    idMap.put(oldIdString, createNode);
                } else {
                    // 将关系数据添加到列表中
                    reList.add(jo);
                }
            }
        }

        // 如果存在关系数据，则创建关系
        if (!reList.isEmpty()) {
            for (Map<String, Object> reMap : reList) {
                Node startId = idMap.get(string(reMap, RELATION_START_ID));
                Node endId = idMap.get(string(reMap, RELATION_END_ID));
                // string(reMap, RELATION_TYPE);
                Map<String, Object> propMap = (Map<String, Object>) reMap.get(RELATION_PROP);
                // 创建关系
                driver.createRelation(startId, endId, string(reMap, RELATION_TYPE), propMap);
            }
        }
    }

}
