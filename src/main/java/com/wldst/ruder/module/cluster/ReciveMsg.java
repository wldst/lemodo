package com.wldst.ruder.module.cluster;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.qliu6.Q6Properties;
import com.wldst.ruder.module.cluster.handle.RecieHandle;
import com.wldst.ruder.constant.UpdaterConstants;

public class ReciveMsg implements Runnable {
	private static ReceiveThread rth = null;

	public static void main(String args[]) throws Exception {
		new ReciveMsg().startRecieve();
	}

	public static void startRecieve() {
		rth = (new ReciveMsg()).new ReceiveThread();
		rth.start();
	}

	public static void stopRecieve() {
		if (rth != null) {
			try {
				rth.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			rth = null;
		}
	}

	class ReceiveThread extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					receiveIP();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		private void receiveIP() throws Exception {
			int port = Q6Properties.getIntUpdateProp(UpdaterConstants.UDP_PORT);
			DatagramSocket dgSocket = new DatagramSocket(port);
			byte[] by = new byte[1024];
			DatagramPacket packet = new DatagramPacket(by, by.length);
			dgSocket.receive(packet);
			RecieHandle.handle(packet);
			dgSocket.close();
		}
	}

	@Override
	public void run() {
		rth = (new ReciveMsg()).new ReceiveThread();
		rth.start();
	}

}