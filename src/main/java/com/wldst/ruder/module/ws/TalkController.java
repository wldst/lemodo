package com.wldst.ruder.module.ws;

import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("/ws/talk/view")
public class TalkController {
    @RequestMapping(value = "/talk", method = { RequestMethod.GET, RequestMethod.POST })
    public String talk(Model model, String table, HttpServletRequest request) throws Exception {
	Set<String> hostList = new HashSet<>();
	String serverName = request.getServerName();
	String hosti = serverName + ":" + LogInfo.get("serverPort");
	hostList.add(hosti);
	model.addAttribute("hosts", String.join(",", hostList));
	model.addAttribute("server", hosti);
	return "talk";
    }

    @RequestMapping(value = "/opt", method = RequestMethod.GET)
    public String monitor(Model model, String operate, HttpServletRequest request) throws Exception {
	model.addAttribute("server", request.getServerName() + ":" + LogInfo.get("serverPort"));
	return "tailLog";
    }

}
