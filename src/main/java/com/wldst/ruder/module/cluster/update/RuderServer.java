package com.wldst.ruder.module.cluster.update;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.module.cluster.ReciveMsg;
import com.wldst.ruder.module.cluster.SendVersionMSG;
import com.wldst.ruder.module.cluster.Version;
import com.wldst.ruder.module.cluster.multi.MulBroadcast;
import com.wldst.ruder.module.cluster.multi.MulReceiverIP;

/**
 * 
 * Copyright C 
 * Updator
 * 设计目标：自动更新，在系统启动后，自动查找其他和客户端。
 * 1、比较是否有最新版本。设计一个版本系统，默认版本为空的，只要版本不为空，这是高版本。会同步。
 * 2、升级策略：使用多播进行自动更新。总是最新版本的客户端发送多播信号。其他客户端视情况自动更新。
 * 3、根据多播，各个客户端自动更新当前在线用户。
 * 广播，查询元数据，同步元数据
 * 
 * @author liuqiang
 * @describe
 * @version 2015-1-30 下午10:41:45
 */
public class RuderServer implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(RuderServer.class);

	private static ExecutorService executorService = Executors
			.newFixedThreadPool(10);
	private static Version tempVersion = new Version(Version.getVersion());
	private static String newestVersion = null;

	public static void main(String args[]) throws Exception {

		new RuderServer().waitForUpdate();
	}

	public static void recordNewerVersion(String versionInfoReplay) {
		newestVersion = versionInfoReplay;
	}

	public void waitForUpdate() {
		receiveThread th = new receiveThread();
		th.start();
	}

	private class receiveThread extends Thread {
		@Override
		public void run() {
			update();
		}
	}

	public static void update() {
		// 发送广播，等待接受数据，收集版本信息。
		multiwork();
	}

	private static void broadcast() {
		ReciveMsg.startRecieve();
		SendVersionMSG.noticeVersionInfo();
	}

	private static void multiwork() {
		try {
			executorService.execute(new MulReceiverIP());
		} catch (Exception e) {
			e.printStackTrace();
		}
		new MulBroadcast().noticeVersionInfo();
	}

	public static String getLeastVersion() {
		return newestVersion;
	}

	public static Version getTempVersion() {
		return tempVersion;
	}

	public static void setTempVersion(Version tempVersion) {
		RuderServer.tempVersion = tempVersion;
	}

	@Override
	public void run() {
		new RuderServer().waitForUpdate();
	}

}
