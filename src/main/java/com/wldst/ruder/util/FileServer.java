package com.wldst.ruder.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
    private int port = 8821;

    void start() {

	try (ServerSocket sockets = new ServerSocket(port);) {

	    while (true) {
		// 选择进行传输的文件
		
		// public Socket accept() throws
		// IOException侦听并接受到此套接字的连接。此方法在进行连接之前一直阻塞。
		Socket socket = sockets.accept();
		String filePath = "D:\\问题清单20200117.xls";
		File fi = new File(filePath);
		System.out.println("文件长度:" + (int) fi.length());
		System.out.println("建立socket链接");
		try (DataInputStream dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			DataOutputStream outps = new DataOutputStream(socket.getOutputStream());
		) {
		    System.out.println(dis.readUTF(dis));
		    // byte[] readAllBytes = dis.readAllBytes();
		    // String mssString = new String(readAllBytes);
		    // System.out.println(mssString);
		    // 将文件名及长度传给客户端。这里要真正适用所有平台，例如中文名的处理，还需要加工，具体可以参见Think In Java 4th里有现成的代码。
		    outps.writeUTF(fi.getName());
		    outps.flush();
		    outps.writeLong(fi.length());
		    outps.flush();
		    int bufferSize = 8192;
		    byte[] buf = new byte[bufferSize];
		    DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath)));
		    while (true) {
			int read = 0;
			if (fis != null) {
			    read = fis.read(buf);
			}
			if (read == -1) {
			    break;
			}
			outps.write(buf, 0, read);
		    }
		    outps.flush();
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static void main(String arg[]) {
	new FileServer().start();
    }
}
