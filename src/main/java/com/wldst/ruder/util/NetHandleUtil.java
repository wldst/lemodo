package com.wldst.ruder.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * 网络操作工具类
 * 
 * @author qliu1
 *
 */
public class NetHandleUtil {

    /**
     * 获取本机本地ip地址
     * 
     * @return
     */
    public static String getLocalIpAddress() {
	InetAddress address;
	String hostAddress = null;
	if (isWindowsOS()) {

	    try {
		address = InetAddress.getLocalHost();
		hostAddress = address.getHostAddress();// 192.168.0.121
	    } catch (UnknownHostException e) {
		e.printStackTrace();
		hostAddress = null;
	    }
	} else {
	    try {
		return getLinuxLocalIp();
	    } catch (SocketException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}
	return hostAddress;
    }

    /**
     * 判断操作系统是否是Windows
     *
     * @return
     */
    public static boolean isWindowsOS() {
	boolean isWindowsOS = false;
	String osName = System.getProperty("os.name");
	if (osName.toLowerCase().indexOf("windows") > -1) {
	    isWindowsOS = true;
	}
	return isWindowsOS;
    }

    /**
     * 获取本地Host名称
     */
    public static String getLocalHostName() throws UnknownHostException {
	return InetAddress.getLocalHost().getHostName();
    }

    /**
     * 获取Linux下的IP地址
     *
     * @return IP地址
     * @throws SocketException
     */
    private static String getLinuxLocalIp() throws SocketException {
	String ip = "";
	try {
	    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
		NetworkInterface intf = en.nextElement();
		String name = intf.getName();
		if (!name.contains("docker") && !name.contains("lo")) {
		    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
			InetAddress inetAddress = enumIpAddr.nextElement();
			if (!inetAddress.isLoopbackAddress()) {
			    String ipaddress = inetAddress.getHostAddress().toString();
			    if (!ipaddress.contains("::") && !ipaddress.contains("0:0:")
				    && !ipaddress.contains("fe80")) {
				ip = ipaddress;
			    }
			}
		    }
		}
	    }
	} catch (SocketException ex) {
	    System.out.println("获取ip地址异常");
	    ip = "127.0.0.1";
	    ex.printStackTrace();
	}
	return ip;
    }
    
     

    public static void main(String args[]) {
	System.out.println(getMacAddress()+"----"+getLocalIpAddress());
    }

    public static String getMacAddress() {
	StringBuilder sb = new StringBuilder();
        Enumeration<NetworkInterface> allNetInterfaces;
	try {
	    allNetInterfaces = NetworkInterface.getNetworkInterfaces();
	    byte[] mac = null;
	        while (allNetInterfaces.hasMoreElements()) {
	            NetworkInterface netInterface = allNetInterfaces.nextElement();
	            if (netInterface.isLoopback() || netInterface.isVirtual() || netInterface.isPointToPoint() || !netInterface.isUp()) {
	                continue;
	            } else {
	                mac = netInterface.getHardwareAddress();
	                if (mac != null) {
	                    if(sb.length()>0) {
	                	    sb.append(",");
	                	}
	                    for (int i = 0; i < mac.length; i++) {
	                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
	                    }
	                }
	            }
	        }
	} catch (SocketException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
        
        return sb.toString();
    }
}
