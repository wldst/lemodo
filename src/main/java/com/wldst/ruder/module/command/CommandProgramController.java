package com.wldst.ruder.module.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.CommandDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.command.impl.CommandListImpl;
import com.wldst.ruder.module.command.impl.RelationCommand;
import com.wldst.ruder.module.command.impl.RelationInCommand;
import com.wldst.ruder.module.command.impl.RelationOutCommand;
import com.wldst.ruder.module.command.impl.SaveCommand;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.PageObject;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 命令编程
 */
@Controller
@RequestMapping("${server.context}/program")
public class CommandProgramController extends CommandDomain {
    private static Logger logger = LoggerFactory.getLogger(CommandProgramController.class);
    @Autowired
    private CrudNeo4jService neo4jService;

    @Autowired
    private CrudUtil crudUtil;

    /**
     * 新建图标,添加流程图自定义节点
     * 
     * @param model
     * @param instanceId
     * @param endLabel
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}/icon", method = { RequestMethod.GET, RequestMethod.POST })
    public String icon(Model model, @PathVariable("po") String poLabel, @PathVariable("endLabel") String endLabel,
	    @RequestBody JSONObject vo, HttpServletRequest request) throws Exception {
	Map<String, Object> viewMap = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
	if (viewMap == null) {
	    throw new DefineException(endLabel + "未定义！");
	}
	CommandList mc = new CommandListImpl();
	mc.add(new SaveCommand(neo4jService, "SVGIcon",crudUtil));
	SaveCommand save = new SaveCommand(neo4jService, "X6DomainNode",crudUtil);
	save.add(new RelationOutCommand(neo4jService, "shape", "X6Shape", endLabel));
	save.add(new RelationInCommand(neo4jService, "group", "X6ShapeGroup", endLabel));
	mc.add(save);

	mc.add(new RelationOutCommand(neo4jService, "shape", "X6Shape", endLabel));
	mc.add(new RelationInCommand(neo4jService, "group", "X6ShapeGroup", endLabel));
	mc.add(new RelationOutCommand(neo4jService, "", "X6DomainNode", endLabel));
	Map<String, Object> execute = mc.execute(vo);
	return "layui/one2Many";
    }

    /**
     * 动态解析功能，解析操作指令。 save:{map,label, tranKey:map{key:value}, bizValue:
     * map{key:value}, childCMD:[ relin:{rLabel,sprop,sLabel,childCMD},
     * relout:{rLabel,tProp,tLabel,childCMD}, ], } relin:{rLabel,sprop,sLabel}
     * relout:{rLabel,tProp,tLabel}
     * 
     * @param model
     * @param poLabel
     * @param endLabel
     * @param vo
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/execute/{po}/{commandId}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult parse(Model model, @PathVariable("po") String poLabel,
	    @PathVariable("commandId") String commandId, @RequestBody JSONObject vo, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> viewMap = neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
	if (viewMap == null) {
	    throw new DefineException(poLabel + "未定义！");
	}
	Long cmdId = null;

	try {
	    cmdId = Long.valueOf(commandId);
	} catch (Exception e) {
	    cmdId = neo4jService.getNodeId(CODE, commandId, poLabel);
	}

	Map<String, Object> nodeMapById = neo4jService.getNodeMapById(cmdId);

	CommandListImpl mc = new CommandListImpl();
	if (poLabel.equals(CMD_CRUD)) {
	    cmdVisit(nodeMapById, mc);
	}
	if (poLabel.equals(CMD_MARCO)) {
	    List<Map<String, Object>> oneRelationList = neo4jService.getOneRelationEndNodeList(cmdId, REL_TYPE_CHILDRENS);
	    for (Map<String, Object> cmdNodeInfo : oneRelationList) {
		Long cmdiId = id(cmdNodeInfo);
		List<Map<String, Object>> childCmds = neo4jService.getOneRelationEndNodeList(cmdiId, "childCMD");
		cmdNodeInfo.put("childCMD", childCmds);
		cmdVisit(cmdNodeInfo, mc);
	    }

	}

	Map<String, Object> execute = mc.execute(vo);
	return ResultWrapper.success(execute);
    }

    private void cmdVisit(Map<String, Object> cmdObject, CommandList mc) {
	String label = MapTool.string(cmdObject, "label");
	Map<String, Object> propMap = MapTool.mapObject(cmdObject, "prop");
	List<Map<String, Object>> childs = MapTool.listMapObject(cmdObject, "childCMD");
	switch (MapTool.string(cmdObject, OPERATOR)) {
	case "save":
	    SaveCommand saveCmd = new SaveCommand(neo4jService, label,crudUtil);
	    if (propMap != null && !propMap.isEmpty()) {
		saveCmd.setBizValue(propMap);
	    }
	    Map<String, String> tranKey = MapTool.stringMap(cmdObject, TRAN_KEY);
	    if (tranKey != null && !tranKey.isEmpty()) {
		saveCmd.setTransKey(tranKey);
	    }
	    handleChild(childs, saveCmd);
	    mc.add(saveCmd);
	    break;
	case "relation":
	    String rsLabel = MapTool.string(cmdObject, REL_NODE_LABEL);
	    String rELabel = MapTool.string(cmdObject, REL_NODE2_LABEL);
	    if (propMap != null && !propMap.isEmpty()) {
		Long snodeId = neo4jService.getNodeIdByPropAndLabel(propMap, rsLabel);
		Long enodeId = neo4jService.getNodeIdByPropAndLabel(propMap, rELabel);
		RelationCommand cmd2 = new RelationCommand(neo4jService, label,snodeId,rsLabel,enodeId, rELabel);
		handleChild(childs, cmd2);
		mc.add(cmd2);
	    }else {
		RelationCommand cmd2 = new RelationCommand(neo4jService, label, rsLabel,rELabel);
		handleChild(childs, cmd2);
		mc.add(cmd2);
	    }
	    break;
	case "relIn":
	    String sLabel = MapTool.string(cmdObject, REL_NODE_LABEL);
	    if (propMap != null && !propMap.isEmpty()) {
		Long nodeId = neo4jService.getNodeIdByPropAndLabel(propMap, sLabel);
		RelationInCommand cmd2 = new RelationInCommand(neo4jService, label, nodeId, sLabel);
		handleChild(childs, cmd2);
		mc.add(cmd2);
	    }else {
		RelationInCommand cmd2 = new RelationInCommand(neo4jService, label, "", sLabel);
		handleChild(childs, cmd2);
		mc.add(cmd2);
	    }
	    break;
	case "relOut":
	    String tLabel = MapTool.string(cmdObject, REL_NODE_LABEL);
	    if (propMap != null && !propMap.isEmpty()) {
		Long targetId = neo4jService.getNodeIdByPropAndLabel(propMap, tLabel);
		RelationOutCommand relIn = new RelationOutCommand(neo4jService, label, targetId, tLabel);
		handleChild(childs, relIn);
		mc.add(relIn);
	    }else {
		RelationInCommand cmd2 = new RelationInCommand(neo4jService, label, "", tLabel);
		handleChild(childs, cmd2);
		mc.add(cmd2);
	    }
	    break;
	}
    }

    private void handleChild(List<Map<String, Object>> childs, CommandList saveCmd) {
	if (childs != null && !childs.isEmpty()) {
	    for (Map<String, Object> ci : childs) {
		cmdVisit(ci, saveCmd);
	    }
	}
    }

    @RequestMapping(value = "/{po}/saveAndConnect/{endLabel}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult saveAndConnect(Model model, @PathVariable("po") String poLabel,
	    @PathVariable("endLabel") String endLabel, @RequestBody JSONObject vo, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> viewMap = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
	if (viewMap == null) {
	    throw new DefineException(endLabel + "未定义！");
	}
	Long nodeId = neo4jService.getNodeId(CODE, "group2", endLabel);
	Map<String, Object> dd = neo4jService.getAttMapBy(NAME, "自定义节点", "X6Shape");
 
	neo4jService.hasPath(poLabel, id(viewMap), endLabel, nodeId);
	CommandList mc = new CommandListImpl();

	SaveCommand save = new SaveCommand(neo4jService, "X6DomainNode",crudUtil);
	Map<String, Object> bizValue = new HashMap<>();
	bizValue.put("shape", "custom-image");
	save.setBizValue(bizValue);
	Map<String, String> tk = new HashMap<>();
	tk.put("label", "title");
	save.setTransKey(tk);
	save.add(new RelationOutCommand(neo4jService, "shape", id(dd), "X6Shape"));
	save.add(new RelationInCommand(neo4jService, "group", nodeId, endLabel));

	mc.add(save);
	Map<String, Object> execute = mc.execute(vo);
	return ResultWrapper.wrapResult(true, execute, null, SAVE_SUCCESS);
    }

    @RequestMapping(value = "/query", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult query(@RequestBody JSONObject vo) throws DefineException {
	PageObject page = crudUtil.validatePage(vo);

	String query = Neo4jOptCypher.queryByProps(vo, page);
	List<Map<String, Object>> dataList = neo4jService.cypher(query);
	page.setTotal(crudUtil.total(query,vo));
	return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }

}
