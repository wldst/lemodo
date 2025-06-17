package com.wldst.ruder.module.cluster.socket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class  UDPServer {
	public static void main(String args[]) throws Exception {
		DatagramSocket socket = new DatagramSocket(18796);
		byte buf[] = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		socket.receive(packet);
		System.out.println("Received from:" + packet.getSocketAddress());
		System.out.println("Data is:"
				+ new String(packet.getData(), 0, packet.getLength()));
	}
}
