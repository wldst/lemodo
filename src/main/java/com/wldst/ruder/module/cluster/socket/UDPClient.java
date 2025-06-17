package com.wldst.ruder.module.cluster.socket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class UDPClient {
	public static void main(String args[]) throws Exception {
		DatagramSocket socket = new DatagramSocket();
		
		String str = "hello,fuxin!新哥";
		sendSocket(socket, str);
	}

	private static void sendSocket(DatagramSocket socket, String str)
			throws UnknownHostException, IOException {
		byte buf[] = str.getBytes();
		InetAddress address = InetAddress.getByName("localhost");
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address,
				18796);
		socket.send(packet);
	}
}