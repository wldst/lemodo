package com.wldst.ruder.module.ws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jakarta.websocket.Session;

/**
 * 读取线程结果信息
 * 
 * @author wldst
 *
 */
public class LinuxReaderThread extends Thread {

    private BufferedReader reader;
    private Session session;

    public LinuxReaderThread(InputStream in, Session session) {
	this.reader = new BufferedReader(new InputStreamReader(in));
	this.session = session;

    }

    @Override
    public void run() {
	String line;
	try {
	    while ((line = reader.readLine()) != null) {
		// 将实时日志�?�过WebSocket发�?�给客户端，给每�?行添加一个HTML换行
		session.getBasicRemote().sendText(line + "<br>");
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}