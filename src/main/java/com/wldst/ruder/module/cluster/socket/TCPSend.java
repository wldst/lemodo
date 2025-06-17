package com.wldst.ruder.module.cluster.socket;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.List;


import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;
import com.qliu6.Q6Properties;
import com.wldst.ruder.constant.UpdaterConstants;

public class TCPSend {
	private static Logger logger = LoggerFactory.getLogger(TCPSend.class);

	public static void main(String[] args) {
		String infoSrcIP = "127.0.0.1";
		int tcpIP = 33456;
		String sendedfile = "E:\\TU\\DSCF0320.JPG";
		send(infoSrcIP, tcpIP, sendedfile);
	}

	public static void send(String infoSrcIP, int tcpIP, String sendedfile) {
		int length = 0;
		byte[] sendByte = null;
		Socket socket = null;
		DataOutputStream dout = null;
		FileInputStream fin = null;
		try {
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress(infoSrcIP, tcpIP),
						10 * 1000);
				dout = new DataOutputStream(socket.getOutputStream());

				File file = new File(sendedfile);
				fin = new FileInputStream(file);
				sendByte = new byte[1024];
				dout.writeUTF(file.getName());
				while ((length = fin.read(sendByte, 0, sendByte.length)) > 0) {
					dout.write(sendByte, 0, length);
					dout.flush();
				}
			} catch (Exception e) {

			} finally {
				if (dout != null)
					dout.close();
				if (fin != null)
					fin.close();
				if (socket != null)
					socket.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void send(JSONObject myMsg, HashSet<String> winapsHashSet) {

		for (String ipi : winapsHashSet) {
			Integer tcpPort = Q6Properties
					.getIntUpdateProp(UpdaterConstants.TCP_PORT);
			sendTCP(myMsg, ipi, tcpPort);
		}
	}

	public static void send(JSONObject myMsg, JSONObject destjson) {
		List<String> infoSrcIPs = (List<String>) destjson
				.get(UpdaterConstants.MYIP_LIST);
		for (String ipi : infoSrcIPs) {
			Integer tcpPort = destjson.getInteger(UpdaterConstants.TCP_PORT);
			sendTCP(myMsg, ipi, tcpPort);
		}
	}

	public static void sendTCP(JSONObject json, String ip, Integer tcpPort) {
		try {
			Socket socket = null;
			DataOutputStream dout = null;
			String msg = json.toString();
			try {
				socket = new Socket();
				socket.connect(new InetSocketAddress(ip, tcpPort), 10 * 1000);
				dout = new DataOutputStream(socket.getOutputStream());
				dout.writeBytes(msg.trim());
			} catch (Exception e) {
				LoggerTool.error(logger,"发送数据异常", e);
			} finally {
				if (dout != null) {
					dout.close();
				}
				if (socket != null)
					socket.close();
			}
			LoggerTool.debug(logger,"send" + msg + "\n to " + ip + ":" + tcpPort);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
