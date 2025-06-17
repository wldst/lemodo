package com.wldst.ruder.util;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class HostUtil {
    public static String getHostName() throws UnknownHostException {
	InetAddress addr = InetAddress.getLocalHost();
	return addr.getHostName();// 获得本机名称
    }

    public static String[] getHostDiskKey() {
	String key = "Filesystem     1K      Used Available Use Mounted";
	String[] keys = key.split("\\s+");
	return keys;
    }

    public static void udpClient(String msg) {
	String host = "255.255.255.255";// 广播地址
	int port = 19602;// 广播的目的端口
	String message = msg;// 用于发送的字符串
	try {
	    InetAddress adds = InetAddress.getByName(host);
	    try (DatagramSocket ds = new DatagramSocket();) {
		DatagramPacket dp = new DatagramPacket(message.getBytes(), message.length(), adds, port);
		ds.send(dp);
		ds.close();
	    }
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	} catch (SocketException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    public static List<String> getLocalHostIpList() {
	List<String> ipList = new ArrayList<>();
	Enumeration<NetworkInterface> netInterfaces = null;
	try {
	    netInterfaces = NetworkInterface.getNetworkInterfaces();
	    while (netInterfaces.hasMoreElements()) {
		NetworkInterface ni = netInterfaces.nextElement();
		if (ni.getName().equals("lo")) {
		    continue;
		}
		Enumeration<InetAddress> ips = ni.getInetAddresses();
		while (ips.hasMoreElements()) {
		    String hostAddress = ips.nextElement().getHostAddress();
		    if (hostAddress.startsWith("127") || hostAddress.equalsIgnoreCase("localhost")) {
			continue;
		    }
		    ipList.add(hostAddress);
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return ipList;
    }
}
