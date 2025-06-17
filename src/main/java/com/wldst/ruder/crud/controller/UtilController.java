package com.wldst.ruder.crud.controller;

import static com.wldst.ruder.constant.Msg.EXECUTE_SUCCESS;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.util.ReadXml;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;
 
/**
 * 读取数据
 * @author wldst
 *
 */
@Controller
@ResponseBody
@RequestMapping("${server.context}/util")
public class UtilController {
	@Autowired
	private CrudNeo4jService neo4jService;
	
    @RequestMapping(value = "/refresh", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public WrappedResult po(Model model,@RequestBody JSONObject vo,HttpServletRequest request)throws Exception{
    	String file = vo.getString("file");
    	if(file==null) {
    		Map<String, Object> paramMap =  (Map<String, Object>) JSON.parse(vo.getString("params"));
        	file = String.valueOf(paramMap.get("file"));
    	}
    	
		if(file==null) {
    		return ResultWrapper.wrapResult(false, null, null, "file参数必填");
    	}
    	List<Map<String, Object>> readSvgIconFont = ReadXml.readSvgIconFont(file);
    	for(Map<String, Object> priMap:readSvgIconFont) {
    		neo4jService.saveByBody(priMap, "iconFont");
    	}
    	
    	return ResultWrapper.wrapResult(false, null, null, EXECUTE_SUCCESS);
    }
    /**
     * 导入svg文件
     * @param model
     * @param vo
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/svgFile", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public WrappedResult svgFile(Model model,@RequestBody JSONObject vo,HttpServletRequest request)throws Exception{
    	String file = vo.getString("file");
    	if(file==null) {
    		Map<String, Object> paramMap =  (Map<String, Object>) JSON.parse(vo.getString("params"));
        	file = String.valueOf(paramMap.get("file"));
    	}
    	
		if(file==null) {
    		return ResultWrapper.wrapResult(false, null, null, "file参数必填");
    	}
    	List<Map<String, Object>> readSvgIconFont = ReadXml.readSvgIconFont(file);
    	for(Map<String, Object> priMap:readSvgIconFont) {
    		neo4jService.saveByBody(priMap, "iconFont");
    	}
    	
    	return ResultWrapper.wrapResult(false, null, null, EXECUTE_SUCCESS);
    }
   
}
