package com.wldst.ruder.module.voice;

import java.util.HashMap;
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
import com.wldst.ruder.domain.BeanShellDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.bs.ShellOperator;
import com.wldst.ruder.module.command.CommandList;
import com.wldst.ruder.module.command.impl.CommandListImpl;
import com.wldst.ruder.module.command.impl.RelationInCommand;
import com.wldst.ruder.module.command.impl.RelationOutCommand;
import com.wldst.ruder.module.command.impl.SaveCommand;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import bsh.Interpreter;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 脚本
 */
@Controller
@RequestMapping("${server.context}/voice/")
public class VoiceController extends BeanShellDomain {
    private static Logger logger = LoggerFactory.getLogger(VoiceController.class);
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private ShellOperator so;
    @Autowired
    private CrudUtil crudUtil;

    /**
     * 动态解析功能，解析操作指令。  
     * 
     * 语音控制建模：
     * 录音-开始-结束->记录文件->保存语音识别内容。记录相关信息。
     * 解析文本内容->执行脚本。执行相关指令。
     * 
     * 我想新建一个项目，名称是休息休息。
     * 写日志，内容是：休息休息。
     * 用户1和用户2他们是什么关系。
     * 
     * 调用语音合成。
     * 
     * 
     * 
     * @param model
     * @param poLabel
     * @param endLabel
     * @param vo
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}/{shellName}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult shell(Model model, @PathVariable("po") String poLabel,
	    @PathVariable("shellName") String shellName, 
	    @RequestBody JSONObject vo, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> shellMap = neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
	if (shellMap == null) {
	    throw new DefineException(poLabel + "未定义！");
	}
	
	Map<String, Object> attMapBy = neo4jService.getAttMapBy(CODE, shellName,poLabel);
	Interpreter in = new Interpreter(); 
	in.set("vo", vo);
	in.set("label", MapTool.string(vo,LABEL));
	in.set("so", so);
//	in.setStrictJava(true);
	String string = MapTool.string(attMapBy, BS_SCRIPT);
	in.eval(string);
	//获取元数据ID
//	Long nodeId = so.getId(META_DATA,LABEL, label);
//	  
//	SaveNode saveModule = so.save("module");
//	SaveNode saveApp = so.save("App");
//	
//	Map<String, Object> bizValue = new HashMap<>();
//	bizValue.put("iconbg","#16990c");
//	bizValue.put("icon","&#xe64d");
//	bizValue.put("url","[(${MODULE_NAME})]/module/{label}");
//	saveApp.setBizValue(bizValue);
//	
//	Map<String, String> tk = new HashMap<>();
//	tk.put("label","appid");
//	saveApp.setTransKey(tk);
//	
//	Map<String, Object> savedModule = saveModule.execute(vo);
//	Map<String, Object> savedApp = saveApp.execute(vo);
//	
//	so.addRel("appModule",savedModule, savedApp); 
//	so.addRel("moduleMeta",nodeId, savedModule);
	
	Object returnValue = in.get("returnValue");
	return ResultWrapper.success(returnValue);
    }
    

    @RequestMapping(value = "/{po}/saveAndConnect/{endLabel}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult saveAndConnect(Model model, 
	    @PathVariable("po") String poLabel,
	    @PathVariable("endLabel") String endLabel, 
	    @RequestBody JSONObject vo, 
	    HttpServletRequest request)
	    throws Exception {
	Map<String, Object> viewMap = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
	if (viewMap == null) {
	    throw new DefineException(endLabel + "未定义！");
	}
	Long nodeId = neo4jService.getNodeId(CODE, "group2", endLabel);
	Long shapeNodeId =   neo4jService.getNodeId(NAME, "自定义节点", "X6Shape"); 
	
//	Long longValue = MapTool.longValue(viewMap, ID);
//	neo4jService.hasPath(poLabel, longValue, endLabel, nodeId);
	CommandList mc = new CommandListImpl();

	SaveCommand save = new SaveCommand(neo4jService, "X6DomainNode",crudUtil);
	Map<String, Object> bizValue = new HashMap<>();
	bizValue.put("shape", "custom-image");
	save.setBizValue(bizValue);
	Map<String, String> tk = new HashMap<>();
	tk.put("label", "title");
	save.setTransKey(tk);
	save.add(new RelationOutCommand(neo4jService, "shape", shapeNodeId, "X6Shape"));
	save.add(new RelationInCommand(neo4jService, "group", nodeId, endLabel));

	mc.add(save);
	Map<String, Object> execute = mc.execute(vo);
	return ResultWrapper.wrapResult(true, execute, null, SAVE_SUCCESS);
    }

}
