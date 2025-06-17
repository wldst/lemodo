package com.wldst.ruder.module.cluster.handle;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;

import com.wldst.ruder.util.LoggerTool;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.qliu6.FileOperate;
import com.qliu6.Q6Properties;
import com.qliu6.VBSUtils;
import com.qliu6.util.AppendFile;
import com.wldst.ruder.module.cluster.FileHttpGets;
import com.wldst.ruder.constant.UpdaterConstants;

/**
 * Copyright C 刘强个人 FavoritesHandle 设计目标： 收集版本信息：
 * 
 * @author liuqiang
 * @describe
 * @version 2015-2-4 下午9:44:17
 */
public class WinApsUpFavoritesHandle implements IStarHandle {
    private static Logger logger = LoggerFactory.getLogger(WinApsUpFavoritesHandle.class);

    /**
     * @param args
     */
    public static void handle(DatagramPacket packet) {
	String str = new String(packet.getData(), 0, packet.getLength());
	JSONObject destJson = JSON.parseObject(str);
	WinApsUpFavoritesHandle fh = new WinApsUpFavoritesHandle();
	fh.handle(destJson);
    }

    public void handle(JSONObject jsonObject) {
	if (jsonObject == null || !jsonObject.get(UpdaterConstants.CMD).equals(UpdaterConstants.FAVORITES)) {
	    return;
	}
	LoggerTool.info(logger,"handleFavorites:" + jsonObject.toString());
	try {
	    FileHttpGets.getFiles2LocalDir(jsonObject);
	} catch (UnknownHostException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (HttpException e) {
	    e.printStackTrace();
	}
	/*String ieFavoritesFile = "ieSyncfavorites.ini";
	File file = new File(Q6Properties.getConfDir() + ieFavoritesFile);
	if (file.exists()) {
		String update = Q6Properties.getInstance().getProp("updated",
				ieFavoritesFile);
		if (update == null || update.equalsIgnoreCase("1")) {
			LoggerTool.info(logger,"no up ");
			return;
		}
	}*/
	updateUp(jsonObject);
    }

    private void updateUp(JSONObject jsonObject) {
	System.out.println("OK UPDATEUP");
	String target = (String) jsonObject.get(UpdaterConstants.VERSION_TARGETS);
	String winUser = (String) jsonObject.get(UpdaterConstants.USER_WIN_ID);
	String user = (String) jsonObject.get(UpdaterConstants.USER);
	if (target.contains(",")) {
	    String ts[] = target.split(",");
	    for (int i = 0; i < ts.length; i++) {
		String ti = ts[i];
		if (!ti.contains("zip")) {
		    FileOperate.moveFile(ti, Q6Properties.getCurrentDir());
		} else {
		    unzipFile(ti, winUser);
		}
	    }
	} else if (target.endsWith("zip")) {
	    unzipFile(target, winUser);
	}
	String userfile = Q6Properties.getCurrentDir() + "loginuser" + File.separator + user;
	FileOperate.createFolder(userfile);
	AppendFile.apendContent(userfile, System.nanoTime() + "|||" + winUser);
    }

    private static void unzipFile(String target, String winUser) {
	String filedir = Q6Properties.getCurrentDir() + target;
	String targetFile = VBSUtils.getSpecialFolder(VBSUtils.SF_FAVORITES);

	LoggerTool.info(logger,targetFile + "------------" + target + "\n" + winUser);
	int usersidx = targetFile.indexOf("Administrator");
	if (usersidx > 0) {
	    targetFile = targetFile.substring(0, usersidx);
	} else {
	    targetFile = "C:\\Users\\";
	}
	String myFavorivte = targetFile + winUser + File.separator + "Favorites";
	LoggerTool.info(logger,myFavorivte);
//	ZipCompressorByAnt.unzip(filedir, myFavorivte);
    }
}
