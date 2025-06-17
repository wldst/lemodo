package com.wldst.ruder.module.cluster.handle;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson2.JSONObject;
import com.qliu6.OSUtil;
import com.qliu6.Q6Properties;
import com.wldst.ruder.constant.UpdaterConstants;

/**
 * Copyright C 刘强个人
 * VersionHandle
 * 设计目标：版本处理，处理报文，将接受到的数据报
 * 收集版本信息：将版本信息存到版本类中。并存放KEY。
 * 
 * @author liuqiang
 * @describe
 * @version 2015-2-4 下午9:44:17
 */
public class RestartHandle implements IStarHandle {
	private static Logger logger = LoggerFactory.getLogger(RestartHandle.class);

	public void handle(JSONObject jsonObject) {
		String test =(String) jsonObject.get(UpdaterConstants.CMD);
		if(test.toLowerCase().equals(UpdaterConstants.RETART_CMD)){
			
			String stopcmd = Q6Properties.getUpdatePropValue(UpdaterConstants.OVD_CLIENT_STOP_CMD);
			String startcmd = Q6Properties.getUpdatePropValue(UpdaterConstants.OVD_CLIENT_START_CMD);
			OSUtil.runCmd(stopcmd);
			LoggerTool.error(logger,"kill OVDNativeClient.jar ");
			try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			OSUtil.runCmd(startcmd);
			LoggerTool.debug(logger,"started ovd client");
		}
	}

}
