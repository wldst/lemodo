package com.wldst.ruder.module.msg;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.controller.BaseLayuiController;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * @author wldst
 *
 */
@Controller
@RequestMapping("${server.context}/message")
public class MessageController extends BaseLayuiController {
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private HtmlShowService showService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private Neo4jOptByUser optByUserSevice;
    
    @RequestMapping(value = "/{po}/{id}/{operate}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult operate(Model model, 
	    @PathVariable("po") String label, 
	    @PathVariable("id") String id,
	    @PathVariable("operate") String operate,
	    HttpServletRequest request) throws Exception {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}	 
	if("getLog".equals(operate)) {
	    
	    
	}
	return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);	
    }
    

}
