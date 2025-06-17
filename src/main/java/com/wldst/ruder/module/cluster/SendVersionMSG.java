package com.wldst.ruder.module.cluster;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;
import com.qliu6.OSUtil;


public class SendVersionMSG {
	private static Logger logger = LoggerFactory
			.getLogger(SendVersionMSG.class);

	public static void main(String args[]) {
		noticeVersionInfo();
	}

	public static void noticeVersionInfo() {
		JSONObject versionquery = new JSONObject();
		versionquery.put("version", Version.getVersion());
		try {
			versionquery.put("myip", OSUtil.getIP());
			versionquery.put("myips", OSUtil.getIPList());
//			versionquery.put(UpdaterConstants.UDP_PORT, port);
			String msg = versionquery.toString();

			broadcastMsg(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void broadcastMsg(String msg) throws Exception {
		DatagramSocket dgSocket = new DatagramSocket();
		byte b[] = msg.getBytes();
		Integer port = 0;//UpdaterConstants.UDP_PORT;
		DatagramPacket dgPacket = new DatagramPacket(b, b.length,
				InetAddress.getByName("255.255.255.255"), port);
		dgSocket.send(dgPacket);
		dgSocket.close();
		LoggerTool.info(logger,"send \n" + msg + "send multicast message is ok.");
	}

}
