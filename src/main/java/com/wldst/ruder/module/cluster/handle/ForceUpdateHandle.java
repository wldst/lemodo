package com.wldst.ruder.module.cluster.handle;

import java.io.IOException;
import java.net.UnknownHostException;


import com.wldst.ruder.util.LoggerTool;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.module.cluster.FileHttpGets;
import com.wldst.ruder.constant.UpdaterConstants;

/**
 * Copyright C 刘强个人
 * VersionHandle
 * 设计目标：強制更新处理,
 * 检查是否有强制更新的内容,在原有的版本更新基础上进行.
 * 
 * @author liuqiang
 * @describe
 * @version 2015-2-4 下午9:44:17
 */
public class ForceUpdateHandle implements IStarHandle {
	private static Logger logger = LoggerFactory
			.getLogger(ForceUpdateHandle.class);

	public void handle(JSONObject jsonObject) {
		if (jsonObject == null
				|| !jsonObject.get(UpdaterConstants.CMD).equals(
						UpdaterConstants.FILE_TRANSPORT)) {
			return;
		}
		LoggerTool.debug(logger,jsonObject.toString());
		try {
			FileHttpGets.getFiles2LocalDir(jsonObject);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (HttpException e) {
			e.printStackTrace();
		}
	}

}
