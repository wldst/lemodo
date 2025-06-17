package com.wldst.ruder.module.cluster.update;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.module.cluster.Version;
import com.wldst.ruder.module.cluster.socket.ServerTcpListener;

/**
 * 
 * Copyright 
 * Updator
 * 设计目标：windows应用服务器文件更新功能，受收藏夹同步功能驱动。
 * 
 * 
 * @author liuqiang
 * @describe
 * @version 2015-1-30 下午10:41:45
 */
public class APSServerUpdator implements Runnable {
	private static Logger logger = LoggerFactory
			.getLogger(APSServerUpdator.class);

	private static ExecutorService executorService = Executors
			.newFixedThreadPool(4);
	private static Version tempVersion = new Version(Version.getVersion());
	private static String newestVersion = null;
	private boolean runFlag = true;
	private receiveThread th =null;
	private static ServerTcpListener tcpLister;
	private static ElementalHttpServer httpServer;

	/**
	* 设定服务线程运行标志值
	* @param runFlag
	*/
	public synchronized void setRunFlag(boolean runFlag) {
		this.runFlag = runFlag;
	}

	/**
	* 取得服务线程运行标志值
	* @param void
	*/
	private synchronized boolean getRunFlag() {
		return runFlag;
	}

	public static void main(String args[]) throws Exception {
		new APSServerUpdator().waitForUpdate();
	}

	public static void recordNewerVersion(String versionInfoReplay) {
		newestVersion = versionInfoReplay;
	}

	private void waitForUpdate() {
		th = new receiveThread();
		th.start();
	}

	private class receiveThread extends Thread {
		@Override
		public void run() {
			update();
		}
	}
	public void stop() {
		th.interrupt();

		while (th.isAlive()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
			}
		}

		th = null;
	}

	public static void update() {
		LoggerTool.debug(logger,"staring fileserver");
		launchFileserver();
		LoggerTool.debug(logger,"staring tcp");
		tcpLister = new ServerTcpListener();
		executorService.execute(tcpLister);
	}

	private static void launchFileserver() {
		httpServer = new ElementalHttpServer();
		executorService.execute(httpServer);
	}

	public static String getLeastVersion() {
		return newestVersion;
	}

	public static Version getTempVersion() {
		return tempVersion;
	}

	public static void setTempVersion(Version tempVersion) {
		APSServerUpdator.tempVersion = tempVersion;
	}

	@Override
	public void run() {
		new APSServerUpdator().waitForUpdate();
	}

}
