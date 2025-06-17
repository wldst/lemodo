package com.wldst.ruder.module.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
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
import com.wldst.ruder.annotation.ControllerLog;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.constant.RuleConstants;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.ParseExcuteSentence;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.manage.AdminUserController;
import com.wldst.ruder.module.parse.ParseExcuteSentence2;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.PageObject;
import com.wldst.ruder.util.RestApi;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("${server.context}/ui/")
public class UserInterfaceController extends RuleConstants {
    @Autowired
    private CrudUtil crudUtil;
    private static Logger logger = LoggerFactory.getLogger(UserInterfaceController.class);
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private ParseExcuteSentence2 pes2;
    @Autowired
    private UserAdminService admin;
    @Autowired
    private RuleDomain rule;
    @Autowired
    private Neo4jOptByUser optByUserSevice;
    @Autowired
    private RestApi restApi;
     
    
    
    
    
    /**
     * 查询有权限的数据
     * @param vo
     * @return
     */
    @RequestMapping(value = "/call/{apiId}", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public WrappedResult callInterface(@PathVariable String apiId, @RequestBody JSONObject vo) {
	Map<String, Object> oneMapById = neo4jService.getOneMapById(Long.valueOf(apiId));
    	String url = (String) oneMapById.get("url");
        logger.info("callInterface:{},params{}",url, vo);
        logger.info("callInterface:{},dbparams{}",url, JSON.toJSONString(mapObject(oneMapById,"params")));
    Object postForObject = restApi.postForObject(url(oneMapById),mapObject(oneMapById,"params"),Object.class);
        logger.info("callInterface:{},postForObject{}",url, postForObject);
//	Map<String, Object> data2 = restApi.data(postForObject);
	return ResultWrapper.wrapResult(true, postForObject, null, QUERY_SUCCESS);
    }
    /**
     * 清理参数
     * @param vo
     * @param columns
     * @return
     */
    private Map<String, Object> validParam(JSONObject vo, String[] columns) {
   	Map<String, Object> param = new HashMap<>();
   	for (String ci : columns) {
   	    Object value2 = vo.get(ci);
   	    if (value2 != null&&!"".equals(value2)&&!"null".equals(value2)) {
   		param.put(ci, value2);
   	    }
   	}
   	return param;
    }
     

}
