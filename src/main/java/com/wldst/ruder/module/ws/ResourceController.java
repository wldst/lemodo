package com.wldst.ruder.module.ws;

import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/resource")
@RestController
public class ResourceController {
    @Value("${server.port}")
    private String serverPort;

    @RequestMapping(value = "/server", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public String resource(String table, HttpServletRequest request) throws Exception {
	Set<String> hostList = new HashSet<>();
	Set<String> servers = new HashSet<>();
	String serverName = request.getServerName();
	for (String hi : hostList) {
	    servers.add(hi + ":" + LogInfo.get("serverPort"));
	}
	return String.join(",", servers);
    }

    @RequestMapping(value = "/portHosts", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public String portHosts(String table, HttpServletRequest request) throws Exception {
	Set<String> hostList = new HashSet<>();
	String serverName = request.getServerName();
	StringBuilder sb = new StringBuilder();
	sb.append(LogInfo.get("serverPort") + ":");
	sb.append(String.join(",", hostList));
	return sb.toString();
    }

}
