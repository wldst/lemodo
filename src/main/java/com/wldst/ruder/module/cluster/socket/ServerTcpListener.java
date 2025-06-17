package com.wldst.ruder.module.cluster.socket;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qliu6.Q6Properties;
import com.wldst.ruder.module.cluster.handle.RecieHandle;
import com.wldst.ruder.constant.UpdaterConstants;

public class ServerTcpListener implements Runnable {
	private static Logger logger = LoggerFactory
			.getLogger(ServerTcpListener.class);
	@Override
	public void run() {
		try {
			launchTCPServer();
		} catch (IOException e) {
			if (e instanceof BindException) {
				LoggerTool.error(logger,e.getMessage(), e);
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public static void main(String[] args) {
		new Thread(new ServerTcpListener()).start();
	}


	public void launchTCPServer() throws IOException {
		int port = Q6Properties.getIntUpdateProp(UpdaterConstants.TCP_PORT);
		LoggerTool.info(logger,"Tcp server listen on " + port);
		final ServerSocket server;
		server = new ServerSocket(port);

		// server.bind(new InetSocketAddress(OSUtil.getIP(), port));

		Thread th = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Socket socket = server.accept();
						LoggerTool.info(logger,"有链接");
						recieve(socket);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		th.run();
	}

	public void recieve(Socket socket) throws IOException {
		byte[] inputByte = null;
		int length = 0;
		DataInputStream din = null;
		try {
			din = new DataInputStream(socket.getInputStream());

			inputByte = new byte[1024];
			StringBuilder sb = new StringBuilder();
			LoggerTool.info(logger,"开始接收数据...");
			while (true) {
				if (din != null) {
					length = din.read(inputByte, 0, inputByte.length);
				}
				if (length == -1) {
					break;
				}
				sb.append(new String(inputByte, "UTF-8"));
			}
			
			String msg = sb.toString();
			RecieHandle.handle(msg);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (din != null)
				din.close();
			if (socket != null)
				socket.close();
		}
	}

	public static void receiveFile(Socket socket) throws IOException {
		byte[] inputByte = null;
		int length = 0;
		DataInputStream din = null;
		FileOutputStream fout = null;
		try {
			din = new DataInputStream(socket.getInputStream());

			String utf = Q6Properties.getCurrentDir() + din.readUTF();
			fout = new FileOutputStream(new File(utf));
			inputByte = new byte[1024];
			LoggerTool.debug(logger,"开始接收数据...");
			while (true) {
			  if (din != null) {
				length = din.read(inputByte, 0, inputByte.length);
			  }
			  if (length == -1) {
			    break;
			  }
			  LoggerTool.debug(logger,"recieve Length:"+length);
			  fout.write(inputByte, 0, length);
			  fout.flush();
			}
			LoggerTool.debug(logger,"完成接收");
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (fout != null)
				fout.close();
			if (din != null)
				din.close();
			if (socket != null)
				socket.close();
		}
	}

}
