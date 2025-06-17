package com.wldst.ruder.module.parse;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.crud.service.*;
import com.wldst.ruder.domain.ConfigDomain;
import com.wldst.ruder.domain.FileDomain;
import com.wldst.ruder.domain.SystemDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 管道管理，页面控制器 Created by on 2018/5/28.
 */
@Controller
@RequestMapping("${server.context}/pipline")
public class PipeLineController extends MapTool {
    private static Logger logger = LoggerFactory.getLogger(PipeLineController.class);
    @Autowired
    private CrudNeo4jService neo4jService;
	@Autowired
	private CrudUserNeo4jService userDataService;

	@Autowired
	private RelationService relationService;

    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private RelationService cypherService;
	@Autowired
	private HtmlShowService showService;

    @RequestMapping(value = "/setting/{key}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Result<String> setting(HttpServletResponse response,@PathVariable("key") String key, Model model,
	    HttpServletRequest request) throws Exception {
		String keyValue =  neo4jService.getBySysCode(key);
		return Result.success(keyValue);
    }

	@RequestMapping(value = "/{po}/setting", method = { RequestMethod.GET })
	public String editForm(Model model, @PathVariable("po") String label,HttpServletRequest request) throws Exception {
		Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
		if (po == null || po.isEmpty()) {
			try {
				po = neo4jService.getPropLabelByNodeId(Long.valueOf(label));
				if (po == null) {
					throw new DefineException(label + "未定义！");
				}
			} catch (Exception e) {
				throw new DefineException(label + "未定义！");
			}
		}
		ModelUtil.setKeyValue(model, po);
		showService.configForm(model, po, true);
		return "layui/pipeline";
	}


	@RequestMapping(value = "/{po}/configItem", method = { RequestMethod.GET })
	@ResponseBody
	public Result configItem(Model model, @PathVariable("po") String label,HttpServletRequest request) throws Exception {
		//先去检查配置中有无此配置
	  String validLabel=  label.trim();
	    logger.info("+++++++++++++++==============="+validLabel);
		List<Map<String, Object>> dataBy=userDataService.getDataBy(ConfigDomain.CONFIGURATION, validLabel);
		Map<String, Object> po =null;
		if(dataBy!=null&!dataBy.isEmpty()){
			po =dataBy.get(0);
		}
		try {
		    if (po == null || po.isEmpty()){
			po =neo4jService.getAttMapBy(LABEL, validLabel, META_DATA);
		}
		if (po == null || po.isEmpty()) {
				po = neo4jService.getPropLabelByNodeId(Long.valueOf(validLabel));
		}
		if (po == null) {
			throw new DefineException(validLabel + "未定义！");
		}
		}catch (Exception e) {
		    logger.info("+++++++++++++++==============="+e.getMessage());
		    throw new DefineException(validLabel + "未定义！");
		}
		
		ModelUtil.setKeyValue(model, po);
		List<Map<String, Object>> config=showService.getItemList(id(po));
		Map<String,Object> configMap=new HashMap<>();
		for(Map<String, Object> ci:config){
			configMap.put(code(ci),value(ci));
		}
		return Result.success(configMap);
	}

	@RequestMapping(value = "/{po}/saveSetting", method = { RequestMethod.GET, RequestMethod.POST})
	@ResponseBody
	public Result saveSetting(Model model, @PathVariable("po") String label,@RequestBody JSONObject vo,HttpServletRequest request) throws Exception {
		Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
		if (po == null || po.isEmpty()) {
			try {
				po = neo4jService.getPropMapBy(label);
				if (po == null) {
					throw new DefineException(label + "未定义！");
				}
			} catch (Exception e) {
				throw new DefineException(label + "未定义！");
			}
		}

		List<Map<String, Object>> config= new ArrayList<>();
		//id,name,value,code,comment
		List<Map<String, Object>> settings=neo4jService.cypher("MATCH(n)-[r:item]->(s:Help) where id(n)="+id(vo)+" return s");
		if(settings!=null&&!settings.isEmpty()){
			config.addAll(settings);
		}

		for(String ki : vo.keySet()){
			for(Map<String, Object> si : config){
				String value=value(si);
				if(code(si).equals(ki)&&value!=null&&!value.equals(string(vo,ki).trim())||id(si).equals(ki)){
					si.put("value", vo.get(ki));
					neo4jService.update(si, id(si));
				}
			}
		}
		return WrappedResult.successMsg("保存成功");
	}
    
    @RequestMapping(value = "/refreshSetting/{key}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public Result<String> refreshSetting(HttpServletResponse response,@PathVariable("key") String key, Model model,
	    HttpServletRequest request) throws Exception {
	String value = neo4jService.refreshSetting(key);
	return Result.success(value);
    }

}
