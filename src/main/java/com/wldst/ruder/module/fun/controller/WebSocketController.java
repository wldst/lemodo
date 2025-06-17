package com.wldst.ruder.module.fun.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.controller.BaseLayuiController;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.util.CrudUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * @author wldst
 *
 */
@Controller
@RequestMapping("${server.context}/ws")
public class WebSocketController extends BaseLayuiController {
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private HtmlShowService showService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private Neo4jOptByUser optByUserSevice;
    @Autowired
    private UserAdminService adminService;

    @RequestMapping(value = "/square", method = { RequestMethod.GET, RequestMethod.POST })
    public String square(Model model, String table, HttpServletRequest request) throws Exception {
	Long currentUserId = adminService.getCurrentPasswordId();
	String currentUserName = adminService.getCurrentAccount();
	
	model.addAttribute("userId", currentUserId);
	model.addAttribute("userName", adminService.getCurrentName());
	return "websocket/square";
    }
    
    @RequestMapping(value = "/chatRoom", method = { RequestMethod.GET, RequestMethod.POST })
    public String po(Model model, String table, HttpServletRequest request) throws Exception {
	return "websocket/chatRoom";
    }
    
    @RequestMapping(value = "/upload", method = { RequestMethod.GET, RequestMethod.POST })
    public String upload(Model model, String table, HttpServletRequest request) throws Exception {
	return "websocket/upload";
    }

}
