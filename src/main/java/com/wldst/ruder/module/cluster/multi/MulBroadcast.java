package com.wldst.ruder.module.cluster.multi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;


import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;
import com.qliu6.OSUtil;
import com.qliu6.Q6Properties;
import com.qliu6.SystemConstants;
import com.qliu6.exception.Q6Exception;
import com.wldst.ruder.constant.UpdaterConstants;
import com.wldst.ruder.module.cluster.Version;

public class MulBroadcast {
	private static Logger logger = LoggerFactory.getLogger(MulBroadcast.class);

	InetAddress address;
	MulticastSocket socket;
	int port;

	public MulBroadcast() {
		try {
			address = InetAddress.getByName("231.9.8.7");
			port = Q6Properties
					.getIntUpdateProp(UpdaterConstants.MULTCAST_PORT);
			socket = new MulticastSocket();
			socket.setTimeToLive(60);
			socket.joinGroup(address);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void noticeVersionInfo() {

		JSONObject versionquery = new JSONObject();
		versionquery.put("version", Version.getVersion());

		String java = OSUtil.excuteAndRetString("java -version").trim();
		if (java != null) {
			versionquery.put(SystemConstants.ENV_JAVA, java);
		}
		String osEnv = OSUtil.excuteAndRetString("uname -a").trim();
		
		versionquery.put(SystemConstants.ENV_OS, osEnv);
		String updateRequirealue = Q6Properties.getUpdatePropValue(UpdaterConstants.ENV_REQUIRE);
		if(updateRequirealue.toLowerCase().contains("false")){
//			versionquery.put(UpdaterConstants.STAR_JAVA_ENVIRENMENT, StarJAVAEnvironment.trans(java));
		}
		versionquery.put(	UpdaterConstants.ENV_REQUIRE,updateRequirealue);

		
		try {
			versionquery.put("myip", OSUtil.getIP());
			versionquery.put("myips", OSUtil.getIPList());
			versionquery.put(UpdaterConstants.CMD, UpdaterConstants.VERSION);

			versionquery.put(
					UpdaterConstants.VERSION_TARGETS,
					Q6Properties.getUpdatePropValue(
							UpdaterConstants.VERSION_TARGETS));
		} catch (Q6Exception e1) {
			e1.printStackTrace();
		}
		versionquery
				.put(UpdaterConstants.HTTP_LOCAL_PORT, Q6Properties
						.getIntUpdateProp(UpdaterConstants.HTTP_LOCAL_PORT));
		versionquery.put(UpdaterConstants.TCP_PORT,
				Q6Properties.getIntUpdateProp(UpdaterConstants.TCP_PORT));

		String msg = versionquery.toString().trim();
		try {
			broadcastMsg(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void broadcastMsg(String msg) throws Exception {

		byte[] data = msg.getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length, address,
				port);
		try {
			socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			socket.close();
		}
		LoggerTool.debug(logger,"消息已发送：" + msg);

	}

	public static void main(String[] args) {
		new MulBroadcast().noticeVersionInfo();
	}

}
