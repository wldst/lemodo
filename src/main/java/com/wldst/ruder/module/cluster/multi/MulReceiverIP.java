package com.wldst.ruder.module.cluster.multi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qliu6.Q6Properties;
import com.wldst.ruder.module.cluster.handle.RecieHandle;
import com.wldst.ruder.constant.UpdaterConstants;

public class MulReceiverIP implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(MulReceiverIP.class);
	
	int port;
	InetAddress group;
	MulticastSocket socket; // socket sends and receives the packet.
	DatagramPacket packet;
	byte[] buf = new byte[1024];// If the message is longer than the packet's
								// length, the message is truncated.

	public MulReceiverIP() throws Exception {
		try {
			port = Q6Properties
					.getIntUpdateProp(UpdaterConstants.MULTCAST_PORT);
			socket = new MulticastSocket(port);

			group = InetAddress.getByName("231.9.8.7");
			if (!group.isMulticastAddress()) {// 检测该地址是否是多播地址
				throw new Exception("地址不是多播地址");
			}
			socket.joinGroup(group); // 加入广播组,加入group后,socket发送的数据报,可以被加入到group中的成员接收到。
			packet = new DatagramPacket(buf, buf.length);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				if(packet==null&&socket==null)
					return;
					socket.receive(packet);
							
			} catch (IOException e) {
				e.printStackTrace();
			}
			RecieHandle.handle(packet);
		}
	}
}
