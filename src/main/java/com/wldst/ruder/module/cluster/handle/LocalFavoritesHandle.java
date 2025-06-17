package com.wldst.ruder.module.cluster.handle;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.wldst.ruder.util.LoggerTool;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;
import com.qliu6.Q6Properties;
import com.qliu6.exception.Q6Exception;
import com.wldst.ruder.module.cluster.FileHttpGets;
import com.wldst.ruder.constant.UpdaterConstants;


/**
 * Copyright C 刘强个人
 * FavoritesHandle
 * 设计目标：
 * 收集版本信息：
 * 
 * @author liuqiang
 * @describe
 * @version 2015-2-4 下午9:44:17
 */
public class LocalFavoritesHandle implements IStarHandle {
	private static Logger logger = LoggerFactory
			.getLogger(LocalFavoritesHandle.class);
	private static List<String> userFavorites;

	public static List<String> getFavoriteUsersList() {
		if (userFavorites == null) {
			return new ArrayList<String>();
		}
		return userFavorites;
	}

	public void handle(JSONObject jsonObject) {
		if (jsonObject == null
				|| !jsonObject.get(UpdaterConstants.CMD).equals(
						UpdaterConstants.FAVORITES)) {
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

		String target = (String) jsonObject
				.get(UpdaterConstants.VERSION_TARGETS);
		try {
			if (target.contains(",")) {
				String ts[] = target.split(",");
				for (int i = 0; i < ts.length; i++) {
					String ti = ts[i];
					if (ti.contains("zip")) {
						copyFavoritesToWinDiskC(ti);
					}
				}
			} else if (target.endsWith("zip")) {
				copyFavoritesToWinDiskC(target);
			}
		} catch (Q6Exception e) {
			LoggerTool.error(logger,"压缩收藏夹出错", e);
			e.printStackTrace();
		}
	}

	private static void unzipFile(String target, String myFavorivte) {
		String filedir = Q6Properties.getCurrentDir() + target;
		LoggerTool.info(logger,myFavorivte);
//		ZipCompressorByAnt.unzip(filedir, myFavorivte);
	}

	public void copyFavoritesToWinDiskC(String ti) throws Q6Exception {
		String usersdir = "/media/sda2/Users/";
		String usersdir2 = "/media/C/Users/";
		String usersdir3 = "/media/c/Users/";
		File userdirfile2 = new File(usersdir2);
		File userdirfile3 = new File(usersdir3);

		File userdirfile = new File(usersdir);
		if (!userdirfile.exists() && !userdirfile2.exists()
				&& !userdirfile3.exists()) {
			throw new Q6Exception("检查本地磁盘C是否挂载到操作系统上了");
		}
		String[] usersf = userdirfile.list();

		if (usersf != null && usersf.length > 0) {
			for (String useridir : getFavoriteUsersList()) {
				String winUserFavoritesDir = usersdir + useridir + "/Favorites";
				File favoritesFile = new File(winUserFavoritesDir);
				for (File fi : favoritesFile.listFiles()) {
					fi.delete();
				}
				if (favoritesFile.exists()
						&& favoritesFile.listFiles().length > 0) {
					unzipFile(ti, useridir);
				}
			}
		}
	}
}
