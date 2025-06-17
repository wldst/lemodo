package com.wldst.ruder.module;

import static com.wldst.ruder.constant.Msg.QUERY_FAILED;
import static com.wldst.ruder.constant.Msg.QUERY_SUCCESS;

import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wldst.ruder.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.unbescape.html.HtmlEscape;
import org.w3c.dom.Document;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.database.util.XmlParse;
import com.wldst.ruder.exception.DefineException;

/**
 * Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/graph")
public class MxGraphController extends MapTool {
    @Autowired
    private CrudNeo4jService neo4jService;

    @Autowired
    private CrudUtil crudUtil;

    @RequestMapping(value = "/editor", method = { RequestMethod.GET, RequestMethod.POST })
    public String graph() throws Exception {
	return "graph/editor";
    }

    @RequestMapping(value = "/po", method = { RequestMethod.GET, RequestMethod.POST })
    public String po() throws Exception {
	return "graph/po";
    }
    
    @RequestMapping(value = "/contextMenu/{page}/{label}", method = { RequestMethod.GET, RequestMethod.POST })
    public String monitor(Model model,
	    @PathVariable("page") String page,
	    @PathVariable("label") String label) throws Exception {
	 Map<String, Object> po = null;
	if(label.startsWith("-cd-module-")) {
	    label=label.replace("-cd-module-", "");
	    po = neo4jService.getAttMapBy(LABEL, label, MODULE);
	}else if(label.startsWith("-cd-manage-")) {
	    label=label.replace("-cd-manage-", "");
	    po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	}else if(StringGet.isNumeric(label)) {
	    po = neo4jService.getPropLabelByNodeId(StringGet.getLong(label));
	}else {
	    po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	}
		
	
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	ModelUtil.setKeyValue(model, po);
	
	return "graph/"+page;
    }
    /**
     * 获取双向关系
     * @param label
     * @param relation
     * @param vo
     * @return
     * @throws DefineException
     */
    @ResponseBody
    @RequestMapping(value = "/getRelation/{nodeId}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Map<String, Object> getBiRelation(
	    @PathVariable("nodeId") String nodeId) throws DefineException {
	Map<String, Object> map = new HashMap<>();
	Long id = Long.valueOf(nodeId);
	Map<String, Object> startNodeMap = neo4jService.getLablePropBy(nodeId);
	if(startNodeMap==null||startNodeMap.isEmpty()) {
	    return  map;
	}
	map.put("me", startNodeMap);
	List<Map<String, Object>> ins =neo4jService.allInRelations(id);
	List<Map<String, Object>> outs = neo4jService.allOutRelations(id);
	List<Map<String, Object>> irelations =new ArrayList<>(ins.size());
	List<Map<String, Object>> orelations =new ArrayList<>(outs.size());
	Object centerId = startNodeMap.get(ID);
//	Map<Long,Map<String, Object>> inMap = new HashMap<>();
//	Map<Long,Map<String, Object>> outMap = new HashMap<>();
	
	//聚合同链接关系
	for(Map<String, Object> nii: ins) {
	    Map<String, Object> startObject = MapTool.mapObject(nii, RELATION_STARTNODE_PROP);
	    Long inId =id(startObject);
	    boolean notM2 = !META_DATA.equals(startObject.get(LABEL))&&!MODULE.equals(startObject.get(LABEL));
//	    System.out.print(","+inId);
//	    if(!inMap.containsKey(inId)) {
		if(notM2&&!inId.equals(centerId)) {
			irelations.add(nii);
		}
//		inMap.put(inId, nii);
//	    }else {
//		Map<String, Object> map2 = inMap.get(inId);
//		map2.put(LABEL, map2.get(LABEL)+","+startObject.get(LABEL));
//		
//		map2.put(LABEL, map2.get(LABEL)+","+startObject.get(LABEL));
//		startObject.get(NAME);
//		
//	    }
	}
//	System.out.print("\n");
	//聚合同链接关系
	for(Map<String, Object> noi: outs) {
	    Map<String, Object> endObject = MapTool.mapObject(noi, RELATION_ENDNODE_PROP);
	    Long outId =id(endObject);
//	    System.out.print(","+outId);
	    List<String> object2 = MapTool.arrayList(noi, RELATION_ENDNODE_LABEL);
	    Object label = endObject.get(LABEL);
	    if(label==null) {
		endObject.put(LABEL, object2.get(0));
	    }
	    boolean notM2 = !META_DATA.equals(label)&&!MODULE.equals(label);
	    if(notM2&&!outId.equals(centerId)) {
		orelations.add(noi);
	    }
	}
	map.put("outs",orelations);
	map.put("ins",irelations);
	return map;
    }

    @RequestMapping(value = "/struct/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    public String struct(Model model, @PathVariable("id") String id) throws Exception {
	model.addAttribute("id", id);
	model.addAttribute("getData", "getStructData(\""+LemodoApplication.MODULE_NAME+"graph/structData/" + id + "\",graph)");
	return "graph/domainStruct";
    }
    @RequestMapping(value = "/structObject/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    public String structObject(Model model, @PathVariable("id") String id) throws Exception {
	model.addAttribute("id", id);
	model.addAttribute("getData", "getStructData(\""+LemodoApplication.MODULE_NAME+"graph/structObject/" + id + "\",graph)");
	return "graph/objectStruct";
    }

    @RequestMapping(value = "/tree/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    public String tree(Model model, @PathVariable("id") String id) throws Exception {
	model.addAttribute("id", id);
	model.addAttribute("getData", "getTreeData(\""+LemodoApplication.MODULE_NAME+"graph/treeData/" + id + "\",graph)");
	return "graph/domainTree";
    }

    @RequestMapping(value = "/structData/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult structData(@PathVariable("id") String id) throws Exception {

	Map<String, Object> startNodeMap = neo4jService.getLablePropBy(id);
	if (startNodeMap == null || startNodeMap.isEmpty()) {
	    return ResultWrapper.wrapResult(true, null, null, QUERY_FAILED);
	}
	String startLabel = String.valueOf(startNodeMap.get(LABEL));
	if (startLabel.contains(",")) {
	    startLabel = startLabel.split(",")[0];
	}
	List<String> ignoreList = new ArrayList<>();
	ignoreList.add("btn");
	ignoreList.add(COPY_RELATION);
	List<Map<String, Object>> outRelationsnList = neo4jService.getByIdIgnoreRels(id, startLabel, ignoreList);
	outRelationsnList.addAll(neo4jService.getNodeIdById(id, startLabel, ignoreList));
	Map<String, List<Map<String, Object>>> relEnds = new HashMap<>();
	Map<String, String> relTypeNameMap = neo4jService.relSet(outRelationsnList, relEnds);
	for (Map<String, Object> reli : outRelationsnList) {
	    Map<String, Object> endPaMap = new HashMap<>();
	    endPaMap.put("id", reli.get("eId"));
	    String endLabeli = String.valueOf(reli.get(LABEL));
	    String relLabeli = String.valueOf(reli.get("rType"));
	    if (relLabeli == null || relLabeli.equals("null")) {
		continue;
	    }
	    Map<String, Object> childNodeMap = neo4jService.loadByIdWithLabel(endLabeli,
		    String.valueOf(reli.get("eId")));
	    if (!childNodeMap.containsKey(NODE_LABEL)
		    && endLabeli.indexOf(String.valueOf(childNodeMap.get(NODE_LABEL))) < 0) {
		childNodeMap.put(NODE_LABEL, endLabeli);
	    } else {
		childNodeMap.put(NODE_LABEL, NodeLabelUtil.firstValidate(String.valueOf(childNodeMap.get(NODE_LABEL))));
	    }
	    reli.put("endNode", childNodeMap);
	}
	Map<String, Object> dataMap = new HashMap<>();
	dataMap.put("startNode", startNodeMap);
	dataMap.put("relations", relEnds);
	dataMap.put("relName", relTypeNameMap);
	return ResultWrapper.wrapResult(true, dataMap, null, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/treeData/{id}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult treeData(@PathVariable("id") String id) {
	if (id == null || id.equals("null")) {
	    Map<String, Object> startNodeMap = neo4jService.getLablePropBy(id);
	}
	Map<String, Object> startNodeMap = neo4jService.getLablePropBy(id);
	if (startNodeMap == null) {
	    return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
	}
	String startLabel = String.valueOf(startNodeMap.get(LABEL));
	List<String> relationLabelList = new ArrayList<>();
	relationLabelList.add("child");
	relationLabelList.add(REL_TYPE_CHILDREN);
	relationLabelList.add(REL_TYPE_CHILDRENS);
	Map<String, String> relTypeNameMap = new HashMap<>();
	Set<String> usedIdSet = new HashSet<>();
	List<Map<String, Object>> partTree = neo4jService.getSubTree(usedIdSet, startLabel, id, relationLabelList,
		relTypeNameMap);
	Map<String, List<Map<String, Object>>> relEnds = neo4jService.relEndList(partTree);
	Map<String, Object> dataMap = new HashMap<>();
	dataMap.put("startNode", startNodeMap);
	dataMap.put("relations", relEnds);
	dataMap.put("relName", relTypeNameMap);
	return ResultWrapper.wrapResult(true, dataMap, null, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/define/{po}", method = { RequestMethod.GET, RequestMethod.POST })
    public String instance(Model model, @PathVariable("po") String label, HttpServletRequest request) throws Exception {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	ModelUtil.setKeyValue(model, po);
	return "graph/domainDefine";
    }

    @RequestMapping(value = "/info/{po}", method = { RequestMethod.GET, RequestMethod.POST })
    public String info(Model model, @PathVariable("po") String label, HttpServletRequest request) throws Exception {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	ModelUtil.setKeyValue(model, po);
	return "graph/domainDetail";
    }

    @RequestMapping(value = "/windows", method = { RequestMethod.GET, RequestMethod.POST })
    public String windows() throws Exception {
	return "graph/windows";
    }

    @RequestMapping(value = "/context", method = { RequestMethod.GET, RequestMethod.POST })
    public String contexticons() throws Exception {
	return "graph/contexticons";
    }

    @RequestMapping(value = "/toolbar", method = { RequestMethod.GET, RequestMethod.POST })
    public String toolbar() throws Exception {
	return "graph/dynamictoolbar";
    }

    @RequestMapping(value = "/save2")
    @ResponseBody
    protected void save2(HttpServletResponse response, HttpServletRequest request, @RequestParam String graphXml)
	    throws Exception {
	String xml = URLDecoder.decode(graphXml, "UTF-8");
	System.out.println(xml);

	Document current = new XmlParse(xml).getDocument();
	String graphId = "6ed10c4036f245b8bf78e1141d85e23b";// current.getDocumentElement().getAttribute("id");
	System.out.println("graphId:" + graphId);
	// transService.deleteGraphById(graphId);
	List<Map<String, Object>> nodeMapList = DocumentUtil.parseDocument(current);
	for (Map<String, Object> trans : nodeMapList) {
	    System.out.println(trans);
	    // transService.insert(trans);
	}
	try {
	    PrintWriter out = response.getWriter();
	    String result = "songyan";
	    out.write(result);
	    out.flush();
	    out.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new Exception(e.getMessage());
	}
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/open")
    protected void open(HttpServletRequest request) throws Exception {
	// 获取图的id
	String graphId = request.getParameter("graphId");
	System.out.println("graphId::" + graphId);
	// 获取节点对象
	List<Map<String, Object>> nodeList = null;
	// nodeList = transService.getTransListByGraphId(graphId);
	List<Map<String, Object>> newNodeList = new ArrayList<Map<String, Object>>();
	for (Map<String, Object> node : nodeList) {
	    Map nodeMap = node;
	    String nodeId = (String) node.get("nodeId");
	    // 查询子节点
	    List<Map<String, Object>> childNodeList = null;
	    // getChildNodes(graphId,nodeId);
	    if (childNodeList != null && childNodeList.size() != 0) {
		nodeMap.put("child", childNodeList);
		newNodeList.add(nodeMap);
	    }
	}

	System.out.println(newNodeList);
	// 获取图的document对象
	Document document = DocumentUtil.getDocument(newNodeList, graphId);

	// 获取xml
	String graphXml = DocumentUtil.getXmlStrByDocument(document);
	System.out.println(graphXml);
	// JSONUtils.responseXml(StringEscapeHelper.encode(graphXml));
    }
}
