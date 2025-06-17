package com.wldst.ruder.module.cluster.handle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.wldst.ruder.util.LoggerTool;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.qliu6.FileOperate;
import com.qliu6.OSUtil;
import com.qliu6.Q6Properties;
import com.qliu6.SystemConstants;
import com.qliu6.exception.Q6Exception;
import com.qliu6.util.GZip;
import com.wldst.ruder.module.cluster.FileHttpGets;
import com.wldst.ruder.module.cluster.Version;
import com.wldst.ruder.module.cluster.socket.TCPSend;
import com.wldst.ruder.module.cluster.update.Updator;
import com.wldst.ruder.constant.UpdaterConstants;

/**
 * Copyright C 刘强个人 VersionHandle 设计目标：版本处理，处理报文，将接受到的数据报
 * 收集版本信息：将版本信息存到版本类中。并存放KEY。
 * 
 * @author liuqiang
 * @describe
 * @version 2015-2-4 下午9:44:17
 */
public class VersionHandle implements IStarHandle {
    private static int versionServed = 0;
    private static Logger logger = LoggerFactory.getLogger(VersionHandle.class);
    private static HashSet<String> ipSet = new HashSet<String>();

    /**
     * @param args env:true;env.java:OpenJDK7,32;env.os:32,debian
     */
    public static void handle(DatagramPacket packet) {
	String str = new String(packet.getData(), 0, packet.getLength());
	JSONObject destJson = JSON.parseObject(str);
	VersionHandle hand = new VersionHandle();
	hand.handle(destJson);
    }

    public void handle(JSONObject destJson) {
	if (!versionNeededBeenHandle(destJson)) {
	    LoggerTool.debug(logger,"信息：" + destJson.toString());
	    return;
	}
	boolean check = false;
	try {
	    while (!check) {
		check = Q6Properties.lockcheck2("versionHandle.lock");

		try {
		    Thread.sleep(1000);
		} catch (InterruptedException e) {
		    LoggerTool.error(logger,e.getMessage(), e);
		}
	    }

	    Version tempvNow = Updator.getTempVersion();
	    String host = (String) destJson.get("myip");
	    if (!ipSet.contains(host)) {
		ipSet.add(host);
	    }
	    String localhost;
	    try {
		localhost = OSUtil.getIP();
		List<String> ips = OSUtil.getIPList();
		if (host.equalsIgnoreCase(localhost) || ips.contains(host)) {
		    return;
		}
	    } catch (Q6Exception e) {
		LoggerTool.error(logger,e.getMessage(), e);
	    }

	    LoggerTool.debug(logger,"信息：" + destJson.toString());
	    String queryVersion = (String) destJson.get(UpdaterConstants.VERSION);
	    Updator.recordNewerVersion(queryVersion);
	    int compareRes = Updator.getTempVersion().compare(queryVersion);
	    LoggerTool.info(logger,"compare version ret:" + compareRes);
	    if (compareRes > 0) {
		LoggerTool.info(logger,"当前版本信息：" + tempvNow.toString());
		Updator.getTempVersion().setVersion(queryVersion);
		LoggerTool.info(logger,"接受到新版本信息：" + destJson.toString());
		Updator.getTempVersion().setVersionInfo(destJson);
		handleVersionUpdate(destJson);
	    } else if (compareRes < 0) {
		if (versionServed < 10) {
		    versionServed++;
		    // 如果当前的版本但与发送过来的版本广告，则回发一个自己的版本信息给目标客户端
		    LoggerTool.info(logger,"当前的版本比发送过来的版本高，则反馈自己的版本信息：" + tempvNow);
		    versionreply(myversion(), destJson);
		}
	    }
	    Q6Properties.unlock("versionHandle.lock");
	} catch (FileNotFoundException e1) {
	    LoggerTool.error(logger,e1.getMessage(), e1);
	} catch (IOException e1) {
	    LoggerTool.error(logger,e1.getMessage(), e1);
	}
    }

    private Boolean versionNeededBeenHandle(JSONObject destJson) {
	if (destJson == null || !destJson.get(UpdaterConstants.CMD).equals(UpdaterConstants.VERSION)) {
	    return false;
	}

	List<String> infoSrcIPs = (List<String>) destJson.get(UpdaterConstants.MYIP_LIST);
	for (String ipi : infoSrcIPs) {
	    try {
		if (ipi.equalsIgnoreCase(OSUtil.getIP())) {
		    return false;
		}
	    } catch (Q6Exception e) {
		LoggerTool.error(logger,"self ip return with error");
	    }
	}

	Object envObject = destJson.get(UpdaterConstants.ENV_REQUIRE);
	if (envObject == null) {
	    LoggerTool.debug(logger,infoSrcIPs + "no environment args,so no version handle!");
	    return false;
	}
	String envString = (String) envObject;
	String envs[] = envString.split(";");
	// destJson.get(key)
	// infoSrcIPs
	if (envs[0].equalsIgnoreCase("true")) {
	    if (!environmentTotalySame(destJson, infoSrcIPs, envs)) {
		LoggerTool.error(logger,"TotalySame:false");
		return false;
	    }
	} else {
	    if (!environmentContionSame(destJson, infoSrcIPs, envs)) {
		LoggerTool.error(logger,"ContionSame:false");
		return false;
	    }
	}
	return true;
    }

    /**
     * 
     * @param destJson
     * @param infoSrcIPs
     * @param envs       length=1,return true, >2,条件判断
     * @return
     */
    private Boolean environmentContionSame(JSONObject destJson, List<String> infoSrcIPs, String[] envs) {
	LoggerTool.error(logger,"environmentContionSame:in======" + envs[0]);
	for (String evi : envs) {
	    if (evi.contains(SystemConstants.ENV_JAVA)) {
		Object javaEnv = destJson.get(SystemConstants.ENV_JAVA);
		if (javaEnv == null) {
		    LoggerTool.error(logger,infoSrcIPs + "no java environment args,so no version handle!");
		    return false;
		}
		String java = OSUtil.excuteAndRetString("java -version").trim();
		String javaEnvString = (String) javaEnv;
		if (javaEnvString.contains(":")) {
		    String args[] = javaEnvString.split(":");
		    for (String condi : args) {
			if (!java.contains(condi)) {
			    return false;
			}
		    }
		    LoggerTool.error(logger,infoSrcIPs + "java environment different ,so don't handle version!");
		    return false;
		}
	    }
	    if (evi.contains(SystemConstants.ENV_OS)) {
		Object osEnv = destJson.get(SystemConstants.ENV_OS);
		if (osEnv == null) {
		    LoggerTool.error(logger,infoSrcIPs + "no OS environment args,so no version handle!");
		    return false;
		}
		String localOs = OSUtil.excuteAndRetString("uname -a").trim();

		String incomeVersionOSEnv = (String) osEnv;
		if (incomeVersionOSEnv.contains(":")) {
		    String args[] = incomeVersionOSEnv.split(":");
		    for (String condi : args) {
			if (!localOs.contains(condi)) {
			    LoggerTool.error(logger,infoSrcIPs + "java environment different ,so don't handle version!");
			    return false;
			}
		    }
		}
	    }
	}
	return true;
    }

    private Boolean environmentTotalySame(JSONObject destJson, List<String> infoSrcIPs, String[] envs) {
	Object javaEnv = destJson.get(SystemConstants.ENV_JAVA);
	if (javaEnv == null) {
	    LoggerTool.debug(logger,infoSrcIPs + "no java environment args,so no version handle!");
	    return false;
	}
	String java = OSUtil.excuteAndRetString("java -version").trim();
	String javaEnvString = (String) javaEnv;
	LoggerTool.info(logger,"localJava\n\n" + java);
	LoggerTool.info(logger,"incomeJava\n\n" + javaEnvString);

	if (!java.equalsIgnoreCase(javaEnvString.trim())) {
	    LoggerTool.debug(logger,infoSrcIPs + "java environment different ,so don't handle version!");
	    return false;
	}

	Object osEnv = destJson.get(SystemConstants.ENV_OS);
	if (osEnv == null) {
	    LoggerTool.debug(logger,infoSrcIPs + "no OS environment args,so no version handle!");
	    return false;
	}

	String localOs = OSUtil.excuteAndRetString("uname -a").trim();

	String osEnvString = (String) osEnv;

	LoggerTool.info(logger,"localOS\n\n" + localOs);
	LoggerTool.info(logger,"incomeOS\n\n" + osEnvString);
	if (!localOs.equalsIgnoreCase(osEnvString.trim())) {
	    LoggerTool.info(logger,"os equal");
	    LoggerTool.debug(logger,infoSrcIPs + "java environment different ,so don't handle version!");
	    return false;
	}
	return true;
    }

    private static JSONObject myversion() {
	JSONObject myVersionInfo = new JSONObject();

	myVersionInfo.put(UpdaterConstants.VERSION, Q6Properties.getInstance().getUpdateProp(UpdaterConstants.VERSION));
	myVersionInfo.put(UpdaterConstants.CMD, UpdaterConstants.VERSION);
	myVersionInfo.put(UpdaterConstants.HTTP_LOCAL_PORT,
		Q6Properties.getInstance().getUpdateProp(UpdaterConstants.HTTP_LOCAL_PORT));
	myVersionInfo.put(UpdaterConstants.TCP_PORT,
		Q6Properties.getInstance().getUpdateProp(UpdaterConstants.TCP_PORT));
	myVersionInfo.put(UpdaterConstants.UDP_PORT,
		Q6Properties.getInstance().getUpdateProp(UpdaterConstants.UDP_PORT));

	String targets = Q6Properties.getInstance().getUpdateProp(UpdaterConstants.VERSION_TARGETS);
	myVersionInfo.put(UpdaterConstants.VERSION_TARGETS, targets);
	try {
	    myVersionInfo.put(UpdaterConstants.MYIP, OSUtil.getIP());
	    myVersionInfo.put(UpdaterConstants.MYIP_LIST, OSUtil.getIPList());
	} catch (Q6Exception e) {
	    e.printStackTrace();
	}
	return myVersionInfo;
    }

    public static void handleVersionUpdate(JSONObject jsonObject) {
	// 下载文件并执行相关更新动作
	// get文件。
	// 执行更新动作，备份本地文件，替换本地文件。重启并登陆？
	LoggerTool.debug(logger,"处理版本更新");

	String updateTime = String.valueOf(System.nanoTime());
	String bakpre = "/usr/q6/ovdbak";
	File ff = new File("/usr/q6/");
	List<Long> tis = new ArrayList<Long>();
	for (String bakdir : ff.list()) {
	    if (bakdir.startsWith(bakpre)) {
		String time = bakdir.substring(bakpre.length());
		try {
		    Long xxLong = new Long(time);
		} catch (Exception e) {
		    String rmbak = "/usr/q6/ovdbak" + time;
		    FileOperate.delFile(rmbak);
		    continue;
		}
		tis.add(new Long(time));
	    }
	}

	if (tis.size() > 3) {
	    Collections.sort(tis);
	    for (int i = 0; i < tis.size() - 3; i++) {
		String rmbak = "/usr/q6/ovdbak" + tis.get(i);
		FileOperate.delFile(rmbak);
	    }
	}
	FileOperate.backUpFolder("/usr/q6/ovd", bakpre + updateTime + "/", bakpre);
	Object pathObject = jsonObject.get(UpdaterConstants.UPDATE_PATH);
	try {
	    if (pathObject != null) {
		FileOperate.createFolder(Q6Properties.getCurrentDir() + (String) pathObject);
		FileHttpGets.getFiles2SpecialDir(jsonObject, (String) pathObject);
	    } else {
		FileHttpGets.getFiles2LocalDir(jsonObject);
	    }
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (HttpException e) {
	    e.printStackTrace();
	}
	LoggerTool.info(logger,"checkSum un implemnets");
	if (pathObject != null) {
	    String targets[] = ((String) jsonObject.get(UpdaterConstants.VERSION_TARGETS)).split(",");
	    for (int k = 0; k < targets.length; k++) {
		String filedir = Q6Properties.getCurrentDir() + (String) pathObject;
		if (filedir.startsWith("/")) {
		    filedir += targets[k].substring(1);
		} else {
		    filedir += targets[k];
		}

		if (filedir.contains("zip")) {
		    // ZipCompressorByAnt.unzip(filedir,
		    // Q6Properties.getCurrentDir());
		} else if (filedir.contains("tar.gz")) {
		    GZip.unTargzFile(filedir, Q6Properties.getCurrentDir());
		} else if (filedir.contains("conf")) {
		    String confs[] = filedir.split("conf/");
		    String targetFile = Q6Properties.getConfDir() + confs[1];
		    FileOperate.copyFile(filedir, targetFile);
		} else {
		    String targetFile = Q6Properties.getCurrentDir() + targets[k];
		    FileOperate.copyFile(filedir, targetFile);
		}
	    }
	}
	// 记录更新情况。
    }

    private static void versionreply(JSONObject srcJsonObject, JSONObject destJosnJson) {
	TCPSend.send(srcJsonObject, destJosnJson);
    }
}
