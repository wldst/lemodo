package com.wldst.ruder.module.cluster.handle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;
import com.qliu6.FileOperate;
import com.qliu6.OSUtil;
import com.qliu6.Q6Properties;
import com.qliu6.VBSUtils;
import com.qliu6.exception.Q6Exception;
import com.wldst.ruder.module.cluster.socket.TCPSend;
import com.wldst.ruder.module.cluster.util.FileUtils;
import com.wldst.ruder.module.cluster.util.ZipUtils;
import com.wldst.ruder.constant.UpdaterConstants;


/**
 * Copyright C 刘强个人
 * FavoritesHandle
 * 设计目标：
 * 收集版本信息：其实这个可以在注销的时候，
 * 
 * @author liuqiang
 * @describe
 * @version 2015-2-4 下午9:44:17
 */
public class WinApsDownFavoritesHandle implements IStarHandle {
	private static Logger logger = LoggerFactory
			.getLogger(WinApsDownFavoritesHandle.class);

	public void handle(JSONObject jsonObject) {
		if (jsonObject == null
				|| !jsonObject.get(UpdaterConstants.CMD).equals(
						UpdaterConstants.FAVORITES)) {
			return;
		}
		String ieFavoritesFile = "ieSyncfavorites.ini";
		File file = new File(Q6Properties.getConfDir() + ieFavoritesFile);
		if (!file.exists()) {
			return;
		}

		String update = Q6Properties.getInstance().getProp("updated",
				ieFavoritesFile);

		if (update == null || !update.equalsIgnoreCase("1")) {
			LoggerTool.info(logger,"no down ");
			return;
		}
		Q6Properties.getInstance().setProperty("ieSyncfavorites.ini",
				"updated", "2");

		String target = (String) jsonObject
				.get(UpdaterConstants.VERSION_TARGETS);
		String winUser = (String) jsonObject.get(UpdaterConstants.USER_WIN_ID);
		String targetF = "/runtime/Favorites.zip";

		try {
			File favoritesFile = compressWindowIEFavorites(winUser);
			FileUtils.copyFile(favoritesFile, new File(targetF));
		} catch (Q6Exception e) {
			LoggerTool.error(logger,"压缩服务器端收藏夹出错", e);
			e.printStackTrace();
		}
		JSONObject json = new JSONObject();
		json.put(UpdaterConstants.CMD, UpdaterConstants.FAVORITES);
		Integer port = Q6Properties
				.getIntUpdateProp(UpdaterConstants.HTTP_LOCAL_PORT);

		try {
			json.put("myip", OSUtil.getIP());
		} catch (Q6Exception e) {
			e.printStackTrace();
		}
		json.put(UpdaterConstants.HTTP_LOCAL_PORT, port);
		json.put(UpdaterConstants.VERSION_TARGETS, target);
		TCPSend.send(json, jsonObject);
	}

	public File compressWindowIEFavorites(String winUser) throws Q6Exception {
		String targetFile = VBSUtils.getSpecialFolder(VBSUtils.SF_FAVORITES);
		int usersidx = targetFile.indexOf("Administrator");
		targetFile = targetFile.substring(0, usersidx);
		String myFavorivte = targetFile + winUser + File.separator
				+ "Favorites";
		FileOutputStream fileOutputStream;
		File file=new File(myFavorivte);
		try {
		    fileOutputStream = new FileOutputStream(myFavorivte + ".zip");
		    ZipUtils.toZip(myFavorivte, fileOutputStream,true);
		} catch (FileNotFoundException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		
//		ZipCompressorByAnt zip = new ZipCompressorByAnt(myFavorivte + ".zip");
//		zip.zip(myFavorivte);
		return new File(myFavorivte + ".zip");
	}

	public void copyWindowIEFavorites(String winUser) throws Q6Exception {
		String targetFile = VBSUtils.getSpecialFolder(VBSUtils.SF_FAVORITES);
		int usersidx = targetFile.indexOf("Administrator");
		targetFile = targetFile.substring(0, usersidx);
		String myFavorivte = targetFile + winUser + File.separator
				+ "Favorites";
		String localFile = Q6Properties.getRuntimeDir() + "Favorites";
		FileOperate.createFolder(localFile);
		File localf = new File(localFile);
		FileUtils.copyFilesRecusively(new File(myFavorivte), localf);
	}
}
