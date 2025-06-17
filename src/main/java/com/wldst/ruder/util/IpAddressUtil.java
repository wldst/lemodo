package com.wldst.ruder.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

public class IpAddressUtil {
    /**
     * 获取Ip地址
     * @param request
     * @return
     */
    public static String getIpAddress(HttpServletRequest request) {
        String Xip = request.getHeader("X-Real-IP");
        String XFor = request.getHeader("X-Forwarded-For");
        if(StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)){
            //多次反向代理后会有多个ip值，第一个ip才是真实ip
            int index = XFor.indexOf(",");
            if(index != -1){
                return XFor.substring(0,index);
            }else{
                return XFor;
            }
        }
        XFor = Xip;
        if(StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)){
            return XFor;
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
            XFor = request.getRemoteAddr();
        }
        return XFor;
    }
    
    public static String getIp() {
	Set<String> ipSet = new HashSet<>();
	Enumeration<NetworkInterface> netInterfaces = null;
	try {
	        netInterfaces = NetworkInterface.getNetworkInterfaces();
	        while (netInterfaces.hasMoreElements()) {
	                NetworkInterface ni = netInterfaces.nextElement();
	                Enumeration<InetAddress> ips = ni.getInetAddresses();
	                while (ips.hasMoreElements()) {
	                     InetAddress nextElement = ips.nextElement();
	                    if (nextElement != null && nextElement instanceof Inet4Address) { // IPV4
	                	ipSet.add(nextElement.getHostAddress());
	                    }
	                       
	                }
	        }
	} catch (Exception e) {
	        e.printStackTrace();
	}
	ipSet.remove("127.0.0.1");
	return String.valueOf(ipSet.toArray()[0]);
    }
}

