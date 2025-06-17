package com.wldst.ruder.crud.controller;

import static com.wldst.ruder.constant.CruderConstant.LABEL;
import static com.wldst.ruder.constant.CruderConstant.MODULE;
import static com.wldst.ruder.constant.CruderConstant.META_DATA;
import static com.wldst.ruder.constant.Msg.QUERY_SUCCESS;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.crud.service.TabListShowService;
import com.wldst.ruder.exception.DefineException;

/**
 * 导入，导出工具设计。可导入视图，模块，实体，节点。
 * 导出：导出视图，模块，实体，节点。
 * 数据即功能，数据=功能+数据。数据=定义+数据。数据=定义+数据+属性+关系
 * 高级形态：数据=程序+数据
 * 定义容器：Container，单个容器只支持局部。全局容器：定义数据，定义图数据库。
 */
@Controller
@RequestMapping("${server.context}/migration")
public class FunctionImgrateController {
	@Autowired
	private CrudNeo4jService neo4jService;
	@Autowired
	private HtmlShowService showService;
	@Autowired
	private TabListShowService tabService;
	
	@RequestMapping(value = "/export/node/{node}", method = { RequestMethod.GET, RequestMethod.POST })
	public void exportNode(Model model, @PathVariable("node") String label,
			HttpServletRequest request,HttpServletResponse response) throws Exception {
		Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
		if (po == null || po.isEmpty()) {
			throw new DefineException(label + "未定义！");
		}
		List<Map<String, Object>> query2 = neo4jService.listDataByLabel(label);		
		String[] headers = MapTool.headers(po);
		
		
		List<Object[]> dataList = getDataList(po, query2);
		String object = (String) po.get(LABEL);
		ExportExcel ec = new ExportExcel(object+"_" + DateTool.format(new Date(),"yyyy-MM-dd HH:mm:ss") + CruderConstant.FILE_TYPE_XLS, "项目基本信息列表", headers,
				dataList, response);
		ec.export();
	}
	
	@RequestMapping(value = "/import/node", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public WrappedResult importNode(Model model, 
			@RequestParam("file") MultipartFile file,
			HttpServletRequest request,HttpServletResponse response) throws Exception {

	        String originalName = file.getOriginalFilename();
	        String importLabel = originalName.split("_")[0];
	        Map<String, Object> po = neo4jService.getAttMapBy(LABEL, importLabel, META_DATA);
			if (po == null || po.isEmpty()) {
//				neo4jService.saveByBody(priMap, label);
			}
	        
	        long fileSize = file.getSize();
	        String ext = originalName.substring(originalName.lastIndexOf(".") + 1, originalName.length());
	        String tmpName = genTmpFileName();
//	        String path = File.separator + category + File.separator + tmpName + "." + ext;
//	        try {
//	            File targetFile = new File(uploadBasePath +  path);
//	            if (!targetFile.getParentFile().exists()){
//	                targetFile.getParentFile().mkdirs();
//	            }
//	            file.transferTo(targetFile);
//	            return sysAccessoryService.upload(accessory);
//	        } catch (IOException e) {
//	            e.printStackTrace();
//	        }
	        return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
	}

	private String genTmpFileName() {
	    return DateTool.format(new Date(), "yyyyMMddHHmmssssss");
	}
	private List<Object[]> getDataList(Map<String, Object> po, List<Map<String, Object>> query2) {
		String columnStr = (String) po.get("columns");
		String[] columns = columnStr.split(",");
		List<Object[]> dataList = new ArrayList<Object[]>(query2.size()+1);
		dataList.add(columns);
		for(Map<String, Object> di:query2) {
			Object[] values=new Object[columns.length];
			for(int i=0;i<columns.length;i++) {
				values[i]=di.get(columns[i]);
			}
			dataList.add(values);
		}
		return dataList;
	}
	
	@RequestMapping(value = "/export/module/{module}", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public WrappedResult exportModule(Model model, @PathVariable("module") String label, @RequestBody JSONObject vo,
			HttpServletRequest request) throws Exception {
		Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, MODULE);
		if (po == null || po.isEmpty()) {
			throw new DefineException(label + "未定义！");
		}

		Map<String, Object> tabList = tabService.modulePoList(label);
		return ResultWrapper.wrapResult(true, tabList, null, QUERY_SUCCESS);
	}


	@RequestMapping(value = "/{module}", method = { RequestMethod.GET, RequestMethod.POST })
	public String exportView(Model model, @PathVariable("module") String label, HttpServletRequest request) throws Exception {
		Map<String, Object> module = neo4jService.getAttMapBy(LABEL, label, MODULE);
		if (module == null || module.isEmpty()) {
			throw new DefineException(label + "未定义！");
		}
		showService.module(model);
		ModelUtil.setKeyValue(model, module);
		return "layui/module";
	}

}
