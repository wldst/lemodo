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
public class LinuxCommander extends Thread {

    private BufferedReader reader;
    private Session session;
    private String divId;

    public LinuxCommander(InputStream in, Session session) {
	this.reader = new BufferedReader(new InputStreamReader(in));
	this.session = session;

    }

    @Override
    public void run() {
	String line;
	try {
	    while ((line = reader.readLine()) != null) {

	    }
	    session.getBasicRemote().sendText(line);
	} catch (IOException e) {
	    e.printStackTrace();
	}

    }

    public String getDivId() {
	return divId;
    }

    public void setDivId(String divId) {
	this.divId = divId;
    }
}