package com.wldst.ruder.util;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;

import com.wldst.ruder.domain.FileDomain;

public class FileClient extends FileDomain {
    private ClientSocket cs = null;
    private String sendMessage = "Windows";

    public FileClient(String ip,int port) {
	try {
	    if (createConnection(ip, port)) {
		sendMessage();
		getMessage();
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    private boolean createConnection(String ip, int port) {
	cs = new ClientSocket(ip,port);
	try {
	    cs.createConnection();
	    System.out.print("连接服务器成功!" + "\n");
	    return true;
	} catch (Exception e) {
	    System.out.print("连接服务器失败!" + "\n");
	    return false;
	}
    }

    private void sendMessage() {
	if (cs == null)
	    return;
	try {
	    cs.sendMessage(sendMessage);
	    cs.sendMessage("testLong");
	} catch (Exception e) {
	    System.out.print("发送消息失败!" + "\n");
	}
	
    }

    private void getMessage() {
	if (cs == null)
	    return;
	DataInputStream inputStream = null;
	// 本地保存路径，文件名会自动从服务器端继承而来。
	String savePath = "D:\\360Downloads\\";
	try {
	    inputStream = cs.getMessageStream();
	    savePath += inputStream.readUTF();
	} catch (Exception e) {
	    System.out.print("接收消息缓存错误\n");
	    return;
	}

	try (DataOutputStream fileOut = new DataOutputStream(
		new BufferedOutputStream(new BufferedOutputStream(new FileOutputStream(savePath))));) {

	    int bufferSize = 8192;
	    byte[] buf = new byte[bufferSize];
	    int passedlen = 0;
	    long len = 0;
	    len = inputStream.readLong();
	    System.out.println("文件的长度为:" + len + "\n");
	    System.out.println("开始接收文件!" + "\n");
	    while (true) {
		int read = 0;
		if (inputStream != null) {
		    read = inputStream.read(buf);
		}
		passedlen += read;
		if (read == -1) {
		    break;
		}
		// 下面进度条本为图形界面的prograssBar做的，这里如果是打文件，可能会重复打印出一些相同的百分比
		System.out.println("文件接收了" + (passedlen * 100 / len) + "%\n");
		fileOut.write(buf, 0, read);
	    }
	    System.out.println("接收完成，文件存为" + savePath + "\n");
	    fileOut.close();
	} catch (Exception e) {
	    System.out.println("接收消息错误" + "\n");
	    return;
	}
    }

    public static void main(String arg[]) {
	new FileClient("localhost", 8821);
    }
}
