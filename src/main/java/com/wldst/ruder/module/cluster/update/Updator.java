package com.wldst.ruder.module.cluster.update;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qliu6.Q6Properties;
import com.wldst.ruder.module.cluster.ReciveMsg;
import com.wldst.ruder.module.cluster.SendVersionMSG;
import com.wldst.ruder.module.cluster.Version;
import com.wldst.ruder.module.cluster.multi.MulBroadcast;
import com.wldst.ruder.module.cluster.multi.MulReceiverIP;
import com.wldst.ruder.module.cluster.socket.ServerTcpListener;

/**
 * 
 * Copyright C 
 * Updator
 * 设计目标：自动更新，在系统启动后，自动查找其他和客户端。
 * 1、比较是否有最新版本。
 * 2、升级策略：使用多播进行自动更新。总是最新版本的客户端发送多播信号。其他客户端视情况自动更新。
 * 3、根据多播，各个客户端自动更新当前在线用户。
 * 4、客户端如何升级，如何进行更新文件？更新配置。通过多播信号决定是否更新初始化数据更新。
 * 5、升级排队策略，TCP了。MapnewVersion：version-hostset。socket。
 * 6、2015年04月20日09:52:42，自动更新规则：JDK 环境一致，才更新。
 * 7
 * 
 * @author liuqiang
 * @describe
 * @version 2015-1-30 下午10:41:45
 */
public class Updator implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(Updator.class);

	private static ExecutorService executorService = Executors
			.newFixedThreadPool(10);
	private static Version tempVersion = new Version(Version.getVersion());
	private static String newestVersion = null;
	private receiveThread th =null;
	public static void main(String args[]) throws Exception {
		new Updator().waitForUpdate();
	}

	public static void recordNewerVersion(String versionInfoReplay) {
		newestVersion = versionInfoReplay;
	}

	public void waitForUpdate() {
		try {
			Q6Properties.lockcheck("updater.lock");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		th = new receiveThread();
		th.start();
	}
	
	public void stop() {
		//OVDClientChecker.stop();
		executorService.shutdownNow();
		executorService.shutdown();
		th.interrupt();
		while (th.isAlive()) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
			}
		}

		th = null;
	}

	private class receiveThread extends Thread {
		@Override
		public void run() {
			update();
		}
	}

	public static void update() {
		LoggerTool.error(logger,"staring fileserver");
		launchFileserver();
		LoggerTool.debug(logger,"staring tcp");
		executorService.execute(new ServerTcpListener());
		// 发送广播，等待接受数据，收集版本信息。
		// broadcast();
		LoggerTool.debug(logger,"staring UDP");
		multiwork();
		// check OVDNativeClient.jar是否运行
		//OVDClientChecker.start();
	}

	private static void broadcast() {
		ReciveMsg.startRecieve();
		SendVersionMSG.noticeVersionInfo();
	}

	private static void multiwork() {
		try {
			executorService.execute(new MulReceiverIP());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new MulBroadcast().noticeVersionInfo();
	}

	private static void launchFileserver() {
		executorService.execute(new ElementalHttpServer());
	}

	public static String getLeastVersion() {
		return newestVersion;
	}

	public static Version getTempVersion() {
		return tempVersion;
	}

	public static void setTempVersion(Version tempVersion) {
		Updator.tempVersion = tempVersion;
	}

	@Override
	public void run() {
		new Updator().waitForUpdate();
	}

}
