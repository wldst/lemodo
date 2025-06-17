package com.wldst.ruder.crud.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.util.LoggerTool;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.util.CommonUtil;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.MapTool;

/**
 * 选项卡服务
 *
 * @author wldst
 */

@Service
public class TabListShowService extends MapTool {
    @Autowired
    private CrudNeo4jService neo4jService;
    final static Logger logger = LoggerFactory.getLogger(TabListShowService.class);
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private HtmlShowService showService;
    @Autowired
    private ViewService vService;

    /**
     * 获取表格数据，带上关系属性
     * @param defi
     * @param table
     * @return
     */
    @ServiceLog(description = "添加表格表头")
    private String addTableHead2(Map<String, Object> defi, StringBuilder table) {
        if (defi == null) {
            return null;
        }
        StringBuilder colGroup = new StringBuilder();
        colGroup.append("<colgroup>");
        StringBuilder thead = new StringBuilder();
        thead.append("<thead><tr>");
        Map<String, String> colNameMap = colName(defi);

        String[] shortShows = splitValue(defi, "shortShow");
        String[] headers = headers(defi);
        String[] columns = columns(defi);
        colGroup.append("<col width=\"25\">");
        colGroup.append("<col >");
        thead.append("<th>处理</th>");
        thead.append("<th>关系属性</th>");
        StringBuilder sb = new StringBuilder();
        if (shortShows != null) {
            for (String hi : shortShows) {
                colGroup.append("<col>");
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(colNameMap.get(hi));
                thead.append("<th>" + colNameMap.get(hi) + "</th>");
            }
        } else {
            for (String hi : headers) {
                if (hi.contains("名称") || hi.contains("表头") || hi.contains("代码") || hi.contains("内容") || hi.contains("标签") || hi.contains("字段") || hi.contains("内容") || hi.contains("标题")) {
                    // colGroup.append("<col width=\"" + (hi.length() * 25) + "\">");
                    colGroup.append("<col >");
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(hi);
                    thead.append("<th>" + hi + "</th>");
                }
            }
        }


        thead.append("<th>操作</th>");
        colGroup.append("<col width='50'>");

        colGroup.append("\n</colgroup>");
        thead.append("</tr></thead>");
        table.append(colGroup.toString());
        table.append(thead.toString());
        return sb.toString();
    }

    @ServiceLog(description = "添加表格表头")
    private String addTableHead(Map<String, Object> defi, StringBuilder table) {
        if (defi == null) {
            return null;
        }
        StringBuilder colGroup = new StringBuilder();
        colGroup.append("<colgroup>");
        StringBuilder thead = new StringBuilder();
        thead.append("<thead><tr>");
        Map<String, String> colNameMap = colName(defi);

        String[] shortShows = splitValue(defi, "shortShow");
        String[] headers = headers(defi);
        String[] columns = columns(defi);
        colGroup.append("<col width=\"25\">");
        colGroup.append("<col >");
        thead.append("<th>属性名</th>");
        thead.append("<th>处理</th>");
        StringBuilder sb = new StringBuilder();
        if (shortShows != null) {
            for (String hi : shortShows) {
                colGroup.append("<col>");
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(colNameMap.get(hi));
                thead.append("<th>" + colNameMap.get(hi) + "</th>");
            }
        } else {
            for (String hi : headers) {
                if (hi.contains("名称") || hi.contains("表头") || hi.contains("代码") || hi.contains("内容") || hi.contains("标签") || hi.contains("字段") || hi.contains("内容") || hi.contains("标题")) {
                    // colGroup.append("<col width=\"" + (hi.length() * 25) + "\">");
                    colGroup.append("<col >");
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(hi);
                    thead.append("<th>" + hi + "</th>");
                }
            }
        }


        thead.append("<th>操作</th>");
        colGroup.append("<col width='50'>");

        colGroup.append("\n</colgroup>");
        thead.append("</tr></thead>");
        table.append(colGroup.toString());
        table.append(thead.toString());
        return sb.toString();
    }

    @ServiceLog(description = "添加表格Body")
    private void addTbody(Map<String, Object> defi, List<Map<String, Object>> dataMapList, StringBuilder table, String relation, String addTableHead) {
        if (defi == null || defi.get("columns") == null) {
            return;
        }
        StringBuilder tBody = new StringBuilder();
        tBody.append("<tbody>");
        int n = 1;
        for (Map<String, Object> datai : dataMapList) {
            // Object label = defi.get(LABEL);
            Object dataId = datai.get(NODE_ID);
            tBody.append("<tr id=" + relation + dataId);
            //读取权限
//	    Map<String, Object> copyWithKeys = copyWithKeys(datai,"id,name");

            tBody.append("  >");
            String[] columns = columns(defi);
            tBody.append("<td><a class='layui-btn  layui-btn-primary layui-btn-xs'  onclick=openManage('编辑"+dataId+"','" + LemodoApplication.MODULE_NAME+"/layui/"+label(defi)+"/"+id(datai)+"/detail') >修改</a> </td>");

            Object prop = datai.get("prop");
            if (prop != null) {
                tBody.append("<td>" + prop + "</td>");
            } else {
                String p = n + ":";
                Object codeObj = datai.get("code");
                if (codeObj != null) {
                    p += codeObj;
                }
                tBody.append("<td>" + p + "</td>");
            }

            String[] split = addTableHead.split(",");

            String[] shortShows = splitValue(defi, "shortShow");
            if (shortShows != null) {
                for (String hi : shortShows) {
                    String value = datai.get(hi) == null ? "" : datai.get(hi).toString();
                    tBody.append("<td>" + value + "</td>");
                }
            } else {
                String[] headers = headers(defi);
                for (int i = 0; i < columns.length; i++) {
                    String ci = columns[i];
                    String hi = headers[i];
                    for (String thi : split) {
                        if (hi.equals(thi)) {
                            Object obj = datai.get(ci);

                            if (obj != null) {
                                String valueOf = String.valueOf(obj);
                                if (ci.equals("header")) {
                                    String substring = valueOf;
                                    // if(valueOf.length()>10) {
                                    // substring = valueOf.substring(0,10);
                                    // }
                                    tBody.append("<td>" + substring + "</td>");
                                } else {
                                    tBody.append("<td>" + valueOf + "</td>");
                                }
                            } else {
                                tBody.append("<td>无</td>");
                            }
                        }
                    }

                }
            }

            n++;

            tBody.append("<td><a class=\"layui-btn  layui-btn-primary layui-btn-xs del-rel\"  onclick=\"delRel('" + relation + "','" + dataId + "')\" >删除</a>");
//            tBody.append("、<a class=\"layui-btn  layui-btn-primary layui-btn-xs\"  onclick=\"openManage('"+name(datai)+"','" + LemodoApplication.MODULE_NAME+"/layui/"+label(defi)+"/"+id(datai)+"/detail')\" >编辑</a>");
            tBody.append("</tr>");
        }
        tBody.append("</tbody>");
        table.append(tBody.toString());
    }

    @ServiceLog(description = "添加表格Body,带关系的属性")
    private void addTbody2(Map<String, Object> defi, List<Map<String, Object>> dataMapList, StringBuilder table, String relation, String addTableHead) {
        if (defi == null || defi.get("columns") == null) {
            return;
        }
        StringBuilder tBody = new StringBuilder();
        tBody.append("<tbody>");
        int n = 1;
        for (Map<String, Object> datai : dataMapList) {
            // Object label = defi.get(LABEL);
            Object dataId = datai.get(NODE_ID);
            tBody.append("<tr id=" + relation + dataId);
            //读取权限
//	    Map<String, Object> copyWithKeys = copyWithKeys(datai,"id,name");

            tBody.append("  >");
            String[] columns = columns(defi);
            tBody.append("<td><a class='layui-btn  layui-btn-primary layui-btn-xs'  onclick=openManage('编辑"+dataId+"','" + LemodoApplication.MODULE_NAME+"/layui/"+label(defi)+"/"+id(datai)+"/detail') >修改</a> </td>");

            Object prop = datai.get("prop");
            Map<String, Object> stringObjectMap=mapObject(datai, RELATION_PROP);
            if (stringObjectMap != null) {
                StringBuilder sb = new StringBuilder();
                for(String key:stringObjectMap.keySet()){
                    if(key.equals("label")||key.equals("name")){
                        continue;
                    }
                    if(sb.length()>0){
                        sb.append(",");
                    }
                    sb.append(key+":"+stringObjectMap.get(key));
                }
                tBody.append("<td>" + sb.toString() + "</td>");
            } else {
                String p = n + ":";
                Object codeObj = datai.get("code");
                if (codeObj != null) {
                    p += codeObj;
                }
                tBody.append("<td>" + p + "</td>");
            }

            String[] split = addTableHead.split(",");

            String[] shortShows = splitValue(defi, "shortShow");
            if (shortShows != null) {
                for (String hi : shortShows) {
                    String value = datai.get(hi) == null ? "" : datai.get(hi).toString();
                    tBody.append("<td>" + value + "</td>");
                }
            } else {
                String[] headers = headers(defi);
                for (int i = 0; i < columns.length; i++) {
                    String ci = columns[i];
                    String hi = headers[i];
                    for (String thi : split) {
                        if (hi.equals(thi)) {
                            Object obj = datai.get(ci);

                            if (obj != null) {
                                String valueOf = String.valueOf(obj);
                                if (ci.equals("header")) {
                                    String substring = valueOf;
                                    // if(valueOf.length()>10) {
                                    // substring = valueOf.substring(0,10);
                                    // }
                                    tBody.append("<td>" + substring + "</td>");
                                } else {
                                    tBody.append("<td>" + valueOf + "</td>");
                                }
                            } else {
                                tBody.append("<td>无</td>");
                            }
                        }
                    }

                }
            }

            n++;

            tBody.append("<td><a class=\"layui-btn  layui-btn-primary layui-btn-xs del-rel\"  onclick=\"delRel('" + relation + "','" + dataId + "')\" >删除</a>");
//            tBody.append("、<a class=\"layui-btn  layui-btn-primary layui-btn-xs\"  onclick=\"openManage('"+name(datai)+"','" + LemodoApplication.MODULE_NAME+"/layui/"+label(defi)+"/"+id(datai)+"/detail')\" >编辑</a>");
            tBody.append("</tr>");
        }
        tBody.append("</tbody>");
        table.append(tBody.toString());
    }

    private void objectListMap(Map<String, List<Map<String, Object>>> dataListMap, String tabId, Map<String, Object> propMap) {
        List<Map<String, Object>> propMapList = dataListMap.get(tabId);
        if (propMapList == null) {
            propMapList = new ArrayList<>();
        }
        propMapList.add(propMap);
        dataListMap.put(tabId, propMapList);
    }

    @ServiceLog(description = "获取label对应的模块出关系数据,返回TabList")
    public Map<String, Object> modulePoList(String label) {
        Map<String, Object> dataMap = new HashMap<>();
        JSONObject vo = new JSONObject();
        vo.put(LABEL, label);
        List<Map<String, Object>> relationAndEndNodeDataList = neo4jService.getOutRelations(vo, MODULE);
        // 如何处理结束节点？获取map，获取节点定义。展现
        List<Map<String, String>> tabList = new ArrayList<>();
        //主数据
        Map<String, String> mainTabInfo = null;
        List<Map<String, String>> mainDataList = new ArrayList<>();

        Map<String, Map<String, String>> tabMap = new HashMap<>();
//	Map<String, List<Map<String, Object>>> nodeDefinePropListMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> nodeDataListMap = new HashMap<>();
//	Map<String, List<Map<String, Object>>> nodeLabelListMap = new HashMap<>();

        dataMap.put("existRelation", getAlreadyExistRelation(relationAndEndNodeDataList));
        dataMap.put("relationEnd", getEndNode(relationAndEndNodeDataList));
        int vi = 0;
        for (Map<String, Object> ri : relationAndEndNodeDataList) {
            Map<String, String> tabiInfo = new HashMap<>();
            Map<String, Object> nodeMap = (Map<String, Object>) ri.get(RELATION_ENDNODE_PROP);
            String rTYpe = string(ri, RELATION_TYPE);
            List<String> endLabels = arrayList(ri, RELATION_ENDNODE_LABEL);
            Map<String, Object> mapObject = mapObject(ri, RELATION_PROP);


            String tabId = null;
            if (nodeMap.containsKey(NODE_LABEL)) {

                Object object = nodeMap.get(NODE_LABEL);
                if (object != null) {
                    tabId = label + "-Tab-" + object;
                    tabiInfo.put("tabId", tabId);
                    tabiInfo.put("tabTitle", String.valueOf(nodeMap.get(NODE_NAME)));
                    objectListMap(nodeDataListMap, tabId, nodeMap);
                } else {
                    String content = string(nodeMap, URL);
                    if (content != null && !content.trim().equals("")) {// 自带地址的资源
                        tabiInfo.put("tabContent", content);
                        tabiInfo.put("tabTitle", String.valueOf(nodeMap.get(NODE_NAME)));

                        tabId = label + "-Tab-" + code(nodeMap);
                    }
                }
            } else if (nodeMap.containsKey(NAVI_LABEL) && nodeMap.containsKey(TABLE_LABEL)) {
                // 导航类数据
                String naviLabel = string(nodeMap, NAVI_LABEL);
                String tableLabel = string(nodeMap, TABLE_LABEL);
                if (CommonUtil.isNumber(naviLabel)) {
                    naviLabel = neo4jService.getNodeLabelByNodeId(Long.valueOf(naviLabel));
                }

                if (CommonUtil.isNumber(tableLabel)) {
                    tableLabel = neo4jService.getNodeLabelByNodeId(Long.valueOf(tableLabel));
                }

                tabId = naviLabel + "-Tab-" + tableLabel;
                tabiInfo.put("tabId", tabId);
                tabiInfo.put("tabTitle", String.valueOf(nodeMap.get(NODE_NAME)));
                objectListMap(nodeDataListMap, tabId, nodeMap);
            } else if (endLabels.contains(VO) || nodeMap.containsKey(VO_COLUMN)) {
                if (endLabels.get(0) != null) {
                    tabId = label + "-Tab-" + rTYpe + vi;
                    tabiInfo.put("tabId", tabId);
                    tabiInfo.put("tabTitle", String.valueOf(nodeMap.get(NODE_NAME)));
                    objectListMap(nodeDataListMap, tabId, nodeMap);
                }
                vi++;
            }
            if (tabId != null) {
                mainTabInfo = tabListHandle(mapObject, tabiInfo, tabMap, tabId, tabList);
                if(mainTabInfo!=null&&mainDataList.isEmpty()){
                    mainDataList.add(mainTabInfo);
                }
            }
        }

        for (Entry<String, List<Map<String, Object>>> eni : nodeDataListMap.entrySet()) {
            Map<String, Object> defi = eni.getValue().get(0);
            String tabId = eni.getKey();
            Map<String, String> tabInfoMap = tabMap.get(tabId);
            String string = string(defi, COLUMNS);
            String voColumn = string(defi, VO_COLUMN);
            if (string == null) {
                // 导航类数据
                if (defi.containsKey(NAVI_LABEL) && defi.containsKey(TABLE_LABEL)) {
                    Long naviId = longValue(defi, NAVI_LABEL);

                    Long tableId = longValue(defi, TABLE_LABEL);
                    String tableLabel = label(neo4jService.getNodeMapById(tableId));

                    Map<String, Object> nodeMapById = neo4jService.getNodeMapById(naviId);
                    String naviLabel = label(nodeMapById);
                    if (naviLabel == null) {
                        tabInfoMap.put("tabContent", LemodoApplication.MODULE_NAME + "/manage/atreeb/" + naviId + "/" + tableId);
                        continue;
                    }
                    String string2 = string(nodeMapById, COLUMNS);
                    if (string2 == null) {
                        tabInfoMap.put("tabContent", LemodoApplication.MODULE_NAME + "/manage/atreeb/" + naviId + "/" + tableId);
                        continue;
                    }
                    String lowerCase = string2.toLowerCase();

                    if (lowerCase.indexOf(",parentid") > -1) {
                        tabInfoMap.put("tabContent", LemodoApplication.MODULE_NAME + "/manage/atreeb/" + naviLabel + "/" + tableLabel);
                    } else {
                        tabInfoMap.put("tabContent", LemodoApplication.MODULE_NAME + "/manage/abTable/" + naviLabel + "/" + tableLabel);
                    }

                }

            } else if (tabInfoMap.get("tabContent") == null) {
                if (voColumn != null) {
                    tabInfoMap.put("tabContent", LemodoApplication.MODULE_NAME + "/vo/" + defi.get(ID));
                } else if (string.toLowerCase().indexOf(",parentid") > 0 || string.toLowerCase().indexOf(",pid") > 0) {
                    tabInfoMap.put("tabContent", LemodoApplication.MODULE_NAME + "/manage/" + defi.get(LABEL) + "/tree");
                } else {
                    // MetaDataCRUD
                    tabInfoMap.put("tabContent", LemodoApplication.MODULE_NAME + "/md/" + defi.get(LABEL));
                }
            }
        }
        if (mainDataList != null&&!mainDataList.isEmpty()) {
            mainDataList.addAll(tabList);
            dataMap.put("tabList", mainDataList);
        }else {
            // 将Tab列表信息添加到返回的Map中
            dataMap.put("tabList", tabList);
        }
        return dataMap;
    }

    /**
     * 生成属性Tab列表信息
     *
     * @param label 标签名称
     * @param vo    输入的JSON对象，用于查询条件
     * @return 返回包含Tab列表信息的Map对象
     */
    public Map<String, Object> tabList(String label, JSONObject vo) {
        // 初始化数据映射
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, Object>> relationAndEndNodeDataList = null;
//        // 构造查询语句
//        String queryOneStepRel = "Match(s:"+label+")-[r]-(e) where id(s)="+id(vo)+" return r,e";
//        // 执行查询
//        List<Map<String, Object>> query = neo4jService.query(queryOneStepRel );
        try {
            // 尝试获取外出关系数据
            relationAndEndNodeDataList = neo4jService.getOutRelations(vo, label);
            // 如果外出关系数据为空，则尝试根据标签ID获取外出关系数据
            if (relationAndEndNodeDataList == null) {
                Long longValue = Long.valueOf(label);
                boolean isId = longValue != null;
                if (isId) {
                    relationAndEndNodeDataList = neo4jService.getOutgoings(label);
                    label = neo4jService.getNodeLabelByNodeId(longValue);
                }
            }
        } catch (NumberFormatException e) {
            // 记录异常信息
            LoggerTool.error(logger,e.getMessage(), e);
        }

        // 初始化Tab列表相关数据结构
        List<Map<String, String>> tabList = new ArrayList<>();
        Map<String, Map<String, String>> tabMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> nodeDefinePropListMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> nodeDataListMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> nodeLabelListMap = new HashMap<>();
        //mainData
        //主数据
        Map<String, String> mainTabInfo = null;
        List<Map<String, String>> mainDataList = new ArrayList<>();

        // 处理查询结果，生成Tab列表信息
        if (relationAndEndNodeDataList != null) {
            // 处理已存在的关系
            dataMap.put("existRelation", getAlreadyExistRelation(relationAndEndNodeDataList));
            // 获取关系端点信息
            Map<String, Map<String, Object>> endNode = getEndNode(relationAndEndNodeDataList);
            dataMap.put("relationEnd", endNode);

            // 遍历关系数据，生成每个Tab的信息
            for (Map<String, Object> ri : relationAndEndNodeDataList) {
                // 初始化Tab信息映射
                Map<String, String> tabiInfo = new HashMap<>();

                Object relType = ri.get(RELATION_TYPE);
                if (relType == null) {
                    relType = ri.get(RELATION_ENDNODE_LABEL);
                }
                // 构造Tab ID
                String tabId = label + "-Tab-" + relType;

                Map<String, Object> propMap = (Map<String, Object>) ri.get(RELATION_ENDNODE_PROP);
                List<String> labels = (List<String>) ri.get(RELATION_ENDNODE_LABEL);
                List<Map<String, Object>> tabNodeDefineList = null;

                // 获取端点名称和关系名称
                String endName = name(endNode.get(relType));
                String relationName = getRelationName(ri);
                if (!getRelationName(ri).endsWith(endName)) {
                    relationName = getRelationName(ri) + "-" + endName;
                }

                // 遍历标签，处理每个标签的属性信息
                for (String lai : labels) {
                    lai = lai.replaceAll("\\[", "").replaceAll("\\]", "");
                    Map<String, Object> endiMetaData = neo4jService.getAttMapBy(LABEL, lai, META_DATA);

                    tabId = tabId + "-" + lai;
                    tabiInfo.put("tabId", tabId);
                    tabNodeDefineList = nodeLabelListMap.get(tabId);
                    if (tabNodeDefineList == null) {
                        // 保存关系定义
                        neo4jService.saveRelationDefine(lai, String.valueOf(relType), label, relationName);
                        tabNodeDefineList = new ArrayList<>();
                    }
                    // 添加端点属性定义到列表中
                    if (!tabNodeDefineList.contains(endiMetaData)) {
                        tabNodeDefineList.add(endiMetaData);
                    }

                }
                // 处理关系属性
                Map<String, Object> mapObject = mapObject(ri, RELATION_PROP);
                propMap.put("prop", mapObject.get("prop"));
                propMap.put(RELATION_PROP, mapObject);
                // 添加节点数据到列表
                objectListMap(nodeDataListMap, tabId, propMap);

                // 设置Tab标题
                tabiInfo.put("tabTitle", relationName);

                // 更新Tab定义和数据列表映射
                nodeDefinePropListMap.put(tabId, tabNodeDefineList);
                nodeLabelListMap.put(tabId, tabNodeDefineList);
                mainTabInfo = tabListHandle(mapObject, tabiInfo, tabMap, tabId, tabList);
                if(mainTabInfo!=null&&mainDataList.isEmpty()){
                    mainDataList.add(mainTabInfo);
                }
            }

        }

        // 处理额外的查询结果，用于补充Tab列表信息
        //需要添加权限，谁才有查看权限
//        if(query!=null&&!query.isEmpty()){
//            dataMap.put("existRelation", getAlreadyExistRelation2(query));
//            Map<String, Map<String, Object>> endNode = getEndNode2(query);
//			Object relationEnd = dataMap.get("relationEnd");
//			if(relationEnd!=null){
//				Map<String, Map<String, Object>> relMap = (Map<String, Map<String, Object>>) relationEnd;
//				for(Entry<String,Map<String, Object>> entry:relMap.entrySet()){
//					if(!relMap.containsKey(entry.getKey())){
//						relMap.put(entry.getKey(),entry.getValue());
//					}
//				}
//				dataMap.put("relationEnd", relMap);
//			}else{
//				dataMap.put("relationEnd", endNode);
//			}
//            List<Map<String, Object>> tabNodeDefineList = new ArrayList<>();
//
//            for(Map<String, Object> map:query) {
//                Map<String, Object> r = mapObject(map,"r");
//                Map<String, Object>  data=copy(map);
//
//                String rType = label(r);
//                String eLabel = label(map);
//				data.remove("r");
//				data.remove(rType);
//                if(rType==null){
//                    rType=eLabel;
//                }
//                String tabId = label + "-Tab-" + rType+"-"+eLabel;
//                tabNodeDefineList = nodeLabelListMap.get(tabId);
//                Map<String, String> tabInfoMap = tabMap.get(tabId);
//				Map<String, Object> md = neo4jService.getAttMapBy(LABEL, eLabel, META_DATA);
//				String relationName=name(r);
//				if(name(md)!=null){
//					if(!name(r).endsWith(name(md))) {
//						relationName = name(r)+"-"+name(md);
//					}
//				}
//                if(tabInfoMap==null) {
//                    tabInfoMap = new HashMap<>();
//					tabInfoMap.put("tabTitle", relationName);
//					tabInfoMap.put("tabId", tabId);
//                }
//
//                tabList.add(tabInfoMap);
//
//                // 添加节点数据到列表
//                objectListMap(nodeDataListMap, tabId, data);
//
//                tabNodeDefineList = nodeLabelListMap.get(tabId);
//                if (tabNodeDefineList == null) {
//                    tabNodeDefineList = new ArrayList<>();
//                }
//
//				// 添加端点属性定义到列表中
//				if (!tabNodeDefineList.contains(md)) {
//					tabNodeDefineList.add(md);
//				}
//                nodeDefinePropListMap.put(tabId, tabNodeDefineList);
//                nodeLabelListMap.put(tabId, tabNodeDefineList);
//                // 如果Tab信息映射中不存在当前Tab，则添加到映射和列表中
//                if (!tabMap.containsKey(tabId)) {
//                    tabMap.put(tabId, tabInfoMap);
//                    tabList.add(tabInfoMap);
//                }
//            }
//        }

        Set<String> used = new HashSet<>();
        // 处理每个Tab的内容数据
        for (Entry<String, List<Map<String, Object>>> eni : nodeDefinePropListMap.entrySet()) {
            List<Map<String, Object>> tabNodeDefine = eni.getValue();
            String tabId = eni.getKey();
            if (!used.contains(tabId)) {
                used.add(tabId);
                List<Map<String, Object>> tabdataList = nodeDataListMap.get(tabId);
                if (tabdataList.size() > 0) {
                    // 生成Tab的内容数据列表
                    tabContentDataList2(tabMap, tabNodeDefine, tabId, tabdataList);
                }
            }

        }
        if (mainDataList != null&&!mainDataList.isEmpty()) {
            mainDataList.addAll(tabList);
            dataMap.put("tabList", mainDataList);
        } else {
            // 将Tab列表信息添加到返回的Map中
            dataMap.put("tabList", tabList);
        }

        return dataMap;
    }

    private static Map<String, String> tabListHandle(Map<String, Object> mapObject,  Map<String, String> tabiInfo, Map<String, Map<String, String>> tabMap, String tabId, List<Map<String, String>> tabList) {
        Map<String, String> mainTabInfo=null;
        Boolean mainData = bool(mapObject, "mainData");
        if (mainData) {
            mainTabInfo = tabiInfo;
        }

        // 如果Tab信息映射中不存在当前Tab，则添加到映射和列表中
        if (!tabMap.containsKey(tabId)) {
            tabMap.put(tabId, tabiInfo);
            if (!mainData) {
                tabList.add(tabiInfo);
            }
        }
        return mainTabInfo;
    }

    /**
     * @param label
     * @param vo
     * @return
     */
    @ServiceLog(description = "生成TabList")
    public Map<String, Object> readTabList(String label, JSONObject vo) {
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, Object>> relationAndEndNodeDataList = null;
        try {
            relationAndEndNodeDataList = neo4jService.getOutRelations(vo, label);
            if (relationAndEndNodeDataList == null) {
                Long longValue = Long.valueOf(label);
                boolean isId = longValue != null;
                if (isId) {
                    relationAndEndNodeDataList = neo4jService.getOutgoings(label);
                    label = neo4jService.getNodeLabelByNodeId(longValue);
                }
            }
        } catch (NumberFormatException e) {
            LoggerTool.error(logger,e.getMessage(), e);
        }

        // 如何处理结束节点？获取map，获取节点定义。展现
        List<Map<String, String>> tabList = new ArrayList<>();
        //主数据
        Map<String, String> mainTabInfo = null;
        List<Map<String, String>> mainDataList = new ArrayList<>();

        Map<String, Map<String, String>> tabMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> nodeDefinePropListMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> nodeDataListMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> nodeLabelListMap = new HashMap<>();
        if (relationAndEndNodeDataList != null) {
            dataMap.put("relationEnd", getEndNode(relationAndEndNodeDataList));
            for (Map<String, Object> ri : relationAndEndNodeDataList) {
                Map<String, String> tabiInfo = new HashMap<>();

                Object relType = ri.get(RELATION_TYPE);
                String tabId = label + "-Tab-" + relType;

                Map<String, Object> propMap = (Map<String, Object>) ri.get(RELATION_ENDNODE_PROP);
                List<String> labels = (List<String>) ri.get(RELATION_ENDNODE_LABEL);
                List<Map<String, Object>> tabNodeDefineList = null;

                String relationName = getRelationName(ri);
                for (String lai : labels) {
                    lai = lai.replaceAll("\\[", "").replaceAll("\\]", "");
                    Map<String, Object> endiPo = neo4jService.getAttMapBy(LABEL, lai, META_DATA);

                    tabId = tabId + "-" + lai;
                    tabiInfo.put("tabId", tabId);
                    tabNodeDefineList = nodeLabelListMap.get(tabId);
                    if (tabNodeDefineList == null) {
                        neo4jService.saveRelationDefine(lai, String.valueOf(relType), label, relationName);
                        tabNodeDefineList = new ArrayList<>();
                    }
                    if (!tabNodeDefineList.contains(endiPo)) {
                        tabNodeDefineList.add(endiPo);
                    }

                }
                Map<String, Object> mapObject = mapObject(ri, RELATION_PROP);
                if(mapObject!=null &&!mapObject.isEmpty()){
                    propMap.put("prop", mapObject.get("prop"));
                    propMap.put(RELATION_PROP, mapObject);
                }

                objectListMap(nodeDataListMap, tabId, propMap);

                tabiInfo.put("tabTitle", relationName);

                nodeDefinePropListMap.put(tabId, tabNodeDefineList);
                nodeLabelListMap.put(tabId, tabNodeDefineList);
                if (!tabMap.containsKey(tabId)) {
                    tabMap.put(tabId, tabiInfo);
                    tabList.add(tabiInfo);
                }
            }
        }

        for (Entry<String, List<Map<String, Object>>> eni : nodeDefinePropListMap.entrySet()) {
            List<Map<String, Object>> tabNodeDefine = eni.getValue();
            String tabId = eni.getKey();
            List<Map<String, Object>> tabdataList = nodeDataListMap.get(tabId);
            if (tabdataList.size() > 0) {
                tabContentDataList(tabMap, tabNodeDefine, tabId, tabdataList);
            }
        }
        if (mainTabInfo != null) {
            mainDataList.add(mainTabInfo);
            mainDataList.addAll(tabList);
            dataMap.put("tabList", mainDataList);
        } else {
            // 将Tab列表信息添加到返回的Map中
            dataMap.put("tabList", tabList);
        }
        return dataMap;
    }

    /**
     * tab页的内容table，DataList
     *
     * @param tabMap
     * @param tabNodeDefine
     * @param tabId
     * @param tabdataList
     */
    @ServiceLog(description = "生成TabContentDataList")
    private void tabContentDataList(Map<String, Map<String, String>> tabMap, List<Map<String, Object>> tabNodeDefine, String tabId, List<Map<String, Object>> tabdataList) {
        StringBuilder content = new StringBuilder();
        if (tabNodeDefine == null) {
            return;
        }
        for (Map<String, Object> defi : tabNodeDefine) {
            StringBuilder table = new StringBuilder();
            table.append("<table class=\"layui-table\" id=\"tab" + tabId + "\" lay-filter=\"tab" + tabId + "\">");
            String addTableHead = addTableHead(defi, table);
            addTbody(defi, tabdataList, table, tabId.split("-")[2], addTableHead);
            table.append("\n</table>");
            content.append(table.toString());
        }
        Map<String, String> tabInfoMap = tabMap.get(tabId);
        tabInfoMap.put("tabContent", content.toString());
    }
    @ServiceLog(description = "生成TabContentDataList，包含关系属性")
    private void tabContentDataList2(Map<String, Map<String, String>> tabMap, List<Map<String, Object>> tabNodeDefine, String tabId, List<Map<String, Object>> tabdataList) {
        StringBuilder content = new StringBuilder();
        if (tabNodeDefine == null) {
            return;
        }
        for (Map<String, Object> defi : tabNodeDefine) {
            StringBuilder table = new StringBuilder();
            table.append("<table class=\"layui-table\" id=\"tab" + tabId + "\" lay-filter=\"tab" + tabId + "\">");
            String addTableHead = addTableHead2(defi, table);
            addTbody2(defi, tabdataList, table, tabId.split("-")[2], addTableHead);
            table.append("\n</table>");
            content.append(table.toString());
        }
        Map<String, String> tabInfoMap = tabMap.get(tabId);
        tabInfoMap.put("tabContent", content.toString());
    }

    /**
     * 获取实例的关系详细信息列表
     *
     * @param startLabel
     * @param vo
     * @return
     */
    public Map<String, Object> detailTabList(String startLabel, JSONObject vo) {
        Map<String, Object> dataMap = new HashMap<>();

        if (startLabel.equals(META_DATA)) {
            startLabel = vo.getString(LABEL);
        }
        List<Map<String, Object>> parts = neo4jService.getOutRelations(vo, startLabel);
//主数据

        /*
         * RELATION_ENDNODE_LABEL; RELATION_ENDNODE_PROP; RELATION_TYPE; RELATION_PROP;
         */
        Set<String> partLabel = new HashSet<>();
        for (Map<String, Object> nodei : parts) {
            Map<String, Object> nodeMap = (Map<String, Object>) nodei.get(RELATION_ENDNODE_PROP);
            Map<String, Object> mapObject = mapObject(nodei, RELATION_PROP);
            Object object = nodeMap.get(LABEL);
            if (object != null) {
                partLabel.add((String) object);
            }
        }
        List<Map<String, String>> tabList = getInstanceTabListWithData(startLabel, vo, partLabel);
//        List<Map<String, String>> poTabListWithData = getPoTabListWithData(startLabel, vo, partLabel, parts, tabList);
//	clearDuplicate(poTabListWithData, tabList);

            // 将Tab列表信息添加到返回的Map中
            dataMap.put("tabList", tabList);
        return dataMap;
    }

    /**
     * 只展现实例数据，多维则Tab展现
     *
     * @param startLabel
     * @param vo
     * @return
     */
    public Map<String, Object> instanceDataTabList(String startLabel, JSONObject vo) {
        Map<String, Object> dataMap = new HashMap<>();
        List<Map<String, Object>> parts = neo4jService.getOutRelations(vo, startLabel);
        /*
         * RELATION_ENDNODE_LABEL; RELATION_ENDNODE_PROP; RELATION_TYPE; RELATION_PROP;
         */
        Set<String> partLabel = new HashSet<>();
        for (Map<String, Object> nodei : parts) {
            Map<String, Object> nodeMap = (Map<String, Object>) nodei.get(RELATION_ENDNODE_PROP);
            partLabel.add((String) nodeMap.get(LABEL));
        }
        List<Map<String, String>> tabList = getInstanceTabListWithData(startLabel, vo, partLabel);
            // 将Tab列表信息添加到返回的Map中
            dataMap.put("tabList", tabList);
        return dataMap;
    }

    /**
     * 获取PO关系
     *
     * @param startLabel
     * @param vo
     * @param partLabel
     * @return
     */
    private List<Map<String, String>> getPoTabListWithData(String startLabel, JSONObject vo, Set<String> partLabel, List<Map<String, Object>> parts, List<Map<String, String>> instatTList) {
        Set<String> ilabelSet = new HashSet<>();
        for (Map<String, String> iti : instatTList) {
            ilabelSet.add(iti.get("tabId"));
        }
        Set<String> newPartLabel = new HashSet<>();
        // PO关系
        // 如何处理结束节点？获取map，获取节点定义。展现
        List<Map<String, String>> tabList = new ArrayList<>();
        Map<String, Map<String, String>> tabMap = new HashMap<>();
        Map<String, Map<String, Object>> nodeDefinePropMap = new HashMap<>();
        Map<String, List<Map<String, Object>>> nodeLabelListMap = new HashMap<>();
        // 实例关系不为空则收集关系定义
        if (parts != null && !parts.isEmpty()) {
            for (Map<String, Object> ri : parts) {

                Map<String, String> object = (Map<String, String>) ri.get(RELATION_ENDNODE_PROP);
                String endPoLabel = object.get(NODE_LABEL);
                /*
                 * if(META_DATA.equals(endPoLabel)) {//关系，不允许有PO-META_DATA continue; }
                 */
                /*
                 * if("module".equals(endPoLabel)) {//关系，不允许有PO-META_DATA continue; }
                 */

                Object relType = ri.get(RELATION_TYPE);
                if (relType == null) {
                    continue;
                }
                Map<String, Object> repMap = (Map<String, Object>) ri.get(RELATION_PROP);
                if ("null".equals(relType)) {
                    relType = repMap.get(LABEL);
                }
                String tabPrefix = startLabel + "-Tab-";
                String tabId = tabPrefix + relType;
                if (relType.equals(REL_TYPE_CHILDREN)) {
                    if (tabMap.containsKey(tabId) || tabMap.containsKey(tabId + "s")) {
                        continue;
                    }
                }
                if (relType.equals(REL_TYPE_CHILDRENS)) {
                    if (tabMap.containsKey(tabId) || tabMap.containsKey(tabPrefix + REL_TYPE_CHILDREN)) {
                        continue;
                    }
                }
                if (ilabelSet.contains(tabId)) {// 实例覆盖PO关系
                    continue;
                }
                String relationName = getRelationName(ri);
                if (null == relationName || "null".equals(relationName)) {
                    continue;
                }
                Map<String, String> tabiInfo = new HashMap<>();

                tabiInfo.put("tabId", tabId);

                tabiInfo.put("tabTitle", relationName);

                List<Map<String, Object>> tabNodeDefineList = nodeLabelListMap.get(tabId);

                if (tabNodeDefineList == null) {
                    tabNodeDefineList = new ArrayList<>();
                }

                Map<String, Object> endiPo = neo4jService.getAttMapBy(LABEL, endPoLabel, META_DATA);
                if (!partLabel.contains(endPoLabel)) {
                    newPartLabel.add(endPoLabel);
                    Node endNode = neo4jService.findBy(LABEL, endPoLabel, META_DATA);
                    Node startNode = neo4jService.findBy(LABEL, startLabel, META_DATA);
                    neo4jService.addRelation(startNode, endNode, String.valueOf(relType), repMap);
                }
                nodeDefinePropMap.put(tabId, endiPo);
                neo4jService.saveRelationDefine(endPoLabel, String.valueOf(relType), startLabel, relationName);

                if (!tabMap.containsKey(tabId)) {
                    tabMap.put(tabId, tabiInfo);
                    tabList.add(tabiInfo);
                }
            }
            // 添加Tab内容
            for (Entry<String, Map<String, Object>> eni : nodeDefinePropMap.entrySet()) {
                String tabId = eni.getKey();
                Map<String, Object> value = eni.getValue();
                if (value == null || tabId.endsWith("null")) {
                    continue;
                }

                Object endLabel = value.get(LABEL);
                StringBuilder content = new StringBuilder();

                Map<String, String> tabInfoMap = tabMap.get(tabId);
                String relLabel = tabId.split("\\-")[2];
                content.append("<iframe data-frameid='");
                content.append(tabId + "' scrolling='auto' frameborder='0' src='" + LemodoApplication.MODULE_NAME + "/layui/" + startLabel + "/" + vo.getString(NODE_ID) + "/" + relLabel + "/" + endLabel);
                content.append("'></iframe>");

                tabInfoMap.put("tabContent", content.toString());
            }
        }
        return tabList;
    }

    /**
     * 获取实例关系  TabList
     *
     * @param startLabel
     * @param vo
     * @param endLabelSet
     * @return
     */
    private List<Map<String, String>> getInstanceTabListWithData(String startLabel, JSONObject vo, Set<String> endLabelSet) {

        Set<String> newPartLabel = new HashSet<>();
        // 实例关系
        List<Map<String, Object>> relationAndEndNodeDataList = neo4jService.getOutRelations(vo, startLabel);
        // 如何处理结束节点？获取map，获取节点定义。展现
        List<Map<String, String>> tabList = new ArrayList<>();
        //主数据
        Map<String, String> mainTabInfo = null;
        List<Map<String, String>> mainDataList = new ArrayList<>();
        Map<String, Map<String, String>> tabMap = new HashMap<>();
        Map<String, Map<String, Object>> nodeDefinePropListMap = new HashMap<>();
        Map<String, Map<String, Object>> relDefineMap = new HashMap<>();

        // 实例关系不为空则收集关系定义
        if (relationAndEndNodeDataList != null && !relationAndEndNodeDataList.isEmpty()) {
            for (Map<String, Object> ri : relationAndEndNodeDataList) {
                Object relType = ri.get(RELATION_TYPE);

                Map<String, String> tabiInfo = new HashMap<>();

                String tabId = startLabel + "-Tab-" + relType;
                tabiInfo.put("tabId", tabId);
                String relationName = getRelationName(ri);
                tabiInfo.put("tabTitle", relationName);
                Map<String, String> object = (Map<String, String>) ri.get(RELATION_ENDNODE_PROP);

                List<String> endLabels = (List<String>) ri.get(RELATION_ENDNODE_LABEL);
                String endPoLabel = endLabels.get(0);
                if (!relDefineMap.containsKey(tabId)) {
                    Map<String, Object> saveRelationDefine = neo4jService.saveRelationDefine(endPoLabel, String.valueOf(relType), startLabel, relationName);
                    relDefineMap.put(tabId, saveRelationDefine);
                }

                Map<String, Object> endiPo = neo4jService.getAttMapBy(LABEL, endPoLabel, META_DATA);
                if (endiPo == null) {
                    continue;
                }
                if (!endLabelSet.contains(endPoLabel)) {
                    newPartLabel.add(endPoLabel);
                    Node endNode = neo4jService.findBy(LABEL, endPoLabel, META_DATA);
                    Node startNode = neo4jService.findBy(LABEL, startLabel, META_DATA);
                    String relName = String.valueOf(relType);
                    Map<String, Object> relProp = (Map<String, Object>) ri.get(RELATION_PROP);
                    if (relationName != null) {
                        relName = String.valueOf(relationName);
                    }
                    mainTabInfo = tabListHandle(relProp, tabiInfo, tabMap, tabId, tabList);
                    if(mainTabInfo!=null&&mainDataList.isEmpty()){
                        mainDataList.add(mainTabInfo);
                    }
                    neo4jService.addRelation(startNode, endNode, String.valueOf(relType), relProp);
                }
                if (!nodeDefinePropListMap.containsKey(tabId)) {
                    nodeDefinePropListMap.put(tabId, endiPo);
                }

                if (!tabMap.containsKey(tabId)) {
                    tabMap.put(tabId, tabiInfo);
                    tabList.add(tabiInfo);
                }
            }
            // 添加Tab内容
            for (Entry<String, Map<String, Object>> eni : nodeDefinePropListMap.entrySet()) {
                String tabId = eni.getKey();
                Map<String, Object> value = eni.getValue();
                if (value == null) {
                    continue;
                }
                Object endLabel = value.get(LABEL);
                StringBuilder content = new StringBuilder();

                Map<String, String> tabInfoMap = tabMap.get(tabId);
                String relLabel = tabId.split("\\-")[2];
                content.append("<iframe data-frameid='");
                content.append(tabId + "' scrolling='auto' frameborder='0' src='" + LemodoApplication.MODULE_NAME + "/layui/" + startLabel + "/" + vo.getString(NODE_ID) + "/" + relLabel + "/" + endLabel);
                content.append("'></iframe>");
                tabInfoMap.put("tabContent", content.toString());
            }
        }

        if (mainDataList != null&&!mainDataList.isEmpty()) {
            mainDataList.addAll(tabList);
            return mainDataList;
        }

        return tabList;
    }

    private void clearDuplicate(List<Map<String, String>> poTabListWithData, List<Map<String, String>> tabList) {
        for (Map<String, String> tabInfoi : tabList) {
            String tabId = tabInfoi.get("tabId");
            List<Map<String, String>> duplicateList = new ArrayList<>();

            for (Map<String, String> tabi : poTabListWithData) {
                if (tabId.equals(tabi.get("tabId"))) {
                    duplicateList.add(tabi);
                }
            }
            poTabListWithData.removeAll(duplicateList);
        }
        if (!poTabListWithData.isEmpty()) {
            tabList.addAll(poTabListWithData);
        }
    }

    /**
     * 获取关系节点数据，name，label
     *
     * @param relations
     * @return
     */
    private Map<String, Map<String, Object>> getEndNode(List<Map<String, Object>> relations) {
        Map<String, Map<String, Object>> relationEndNode = new HashMap<>();
        for (Map<String, Object> ri : relations) {
            Map<String, Object> endMap = new HashMap<>();
            List<String> labels = (List<String>) ri.get(RELATION_ENDNODE_LABEL);
            String label0 = labels.get(0).replaceAll("\\]", "").replaceAll("\\[", "");
            endMap.put(LABEL, label0);
            Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label0, META_DATA);
            if (po == null) {
                System.out.println("l:" + label0);
                endMap.put("name", label0);
            } else {
                endMap.put("name", String.valueOf(po.get("name")));
            }
            Map<String, String> mapObject = map(ri, RELATION_PROP);
            if (mapObject.containsKey("prop")) {
                endMap.put("prop", mapObject.get("prop"));
            }

            relationEndNode.put(String.valueOf(ri.get(RELATION_TYPE)), endMap);
        }
        return relationEndNode;
    }

    private Map<String, Map<String, Object>> getEndNode2(List<Map<String, Object>> relations) {
        Map<String, Map<String, Object>> relationEndNode = new HashMap<>();
        for (Map<String, Object> ri : relations) {
            Map<String, Object> endMap = new HashMap<>();
            Map<String, Object> r = mapObject(ri, "r");

            String endLabel = label(ri);

            endMap.put(LABEL, endLabel);
            Map<String, Object> po = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
            if (po == null) {
                System.out.println("l:" + endLabel);
                endMap.put("name", endLabel);
            } else {
                endMap.put("name", name(po));
            }
            Map<String, String> mapObject = map(ri, RELATION_PROP);
            if (mapObject != null && mapObject.containsKey("prop")) {
                endMap.put("prop", mapObject.get("prop"));
            }
            relationEndNode.put(label(r), endMap);
        }
        return relationEndNode;
    }

    /**
     * 获取已有关系数据
     *
     * @param relations
     * @return
     */
    /**
     * 获取已存在的关系列表，并将其转换为可供选择的选项格式
     *
     * @param relations 原始关系列表，每个关系包含关系类型等信息
     * @return 转换后的选择选项字符串，附带默认选择提示
     */
    public String getAlreadyExistRelation(List<Map<String, Object>> relations) {
        // 初始化用于存放转换后选择项的列表和临时关系映射
        List<Map<String, Object>> relationSelectOption = new ArrayList<>();
        Map<String, Map<String, Object>> relation = new HashMap<>();

        // 遍历原始关系列表，构造新的选择项格式
        for (Map<String, Object> ri : relations) {
            Map<String, Object> optionMap = new HashMap<>();
            // 提取关系ID和名称，并构造选项映射
            String relId = String.valueOf(ri.get(RELATION_TYPE));
            optionMap.put(NODE_CODE, relId);
            String relationName = getRelationName(ri);
            optionMap.put(NODE_NAME, relationName);
            // 将选项映射到关系ID
            relation.put(relId, optionMap);
        }

        // 将关系映射的值（即选项映射）转换为选择选项列表
        for (Entry<String, Map<String, Object>> ei : relation.entrySet()) {
            relationSelectOption.add(ei.getValue());
        }

        // 调用服务，将转换后的选择选项添加默认选择提示，并返回
        return showService.addSelectOption(relationSelectOption, "请选择关系");
    }

    public String getAlreadyExistRelation2(List<Map<String, Object>> relations) {
        List<Map<String, Object>> relationSelectOption = new ArrayList<>();
        Map<String, Map<String, Object>> relation = new HashMap<>();
        for (Map<String, Object> ri : relations) {
            Map<String, Object> optionMap = new HashMap<>();
            Map<String, Object> rid = mapObject(ri, "r");
            String relId = label(rid);
            optionMap.put(NODE_CODE, relId);
            String relationName = name(rid);
            optionMap.put(NODE_NAME, relationName);
            relation.put(relId, optionMap);
        }
        for (Entry<String, Map<String, Object>> ei : relation.entrySet()) {
            relationSelectOption.add(ei.getValue());
        }

        return showService.addSelectOption(relationSelectOption, "请选择关系");
    }

    public String getRelationInfo(List<Map<String, Object>> relations) {
        List<Map<String, Object>> relationSelectOption = new ArrayList<>();
        Map<String, Map<String, Object>> relation = new HashMap<>();
        for (Map<String, Object> ri : relations) {
            Map<String, Object> optionMap = new HashMap<>();
            String relLabel = String.valueOf(ri.get(RELATION_LABEL));
            optionMap.put(NODE_CODE, relLabel);
            optionMap.put(NODE_NAME, String.valueOf(ri.get(NODE_NAME)));
            optionMap.put(END_LABEL, String.valueOf(ri.get(END_LABEL)));
            relation.put(relLabel, optionMap);
        }
        for (Entry<String, Map<String, Object>> ei : relation.entrySet()) {
            relationSelectOption.add(ei.getValue());
        }

        return showService.addSelectOption(relationSelectOption, "请选择关系");
    }

    private String getRelationName(Map<String, Object> ri) {
        Map<String, Object> relMap = (Map<String, Object>) ri.get(RELATION_PROP);
        Object rName = relMap.get("name") == null ? relMap.get("relationName") : relMap.get("name");
        String relationName = String.valueOf(rName);
        return relationName;
    }

}
