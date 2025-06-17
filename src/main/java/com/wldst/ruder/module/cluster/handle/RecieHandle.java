package com.wldst.ruder.module.cluster.handle;

import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.qliu6.Q6Properties;
import com.qliu6.SystemProperties;
import com.wldst.ruder.constant.UpdaterConstants;

public class RecieHandle implements IStarHandle {
	private static RecieHandle handleInstance;
	private List<IStarHandle> regsiterHandles = new ArrayList<IStarHandle>();

	public List<IStarHandle> register(IStarHandle handle) {
		regsiterHandles.add(handle);
		return regsiterHandles;
	}

	public List<IStarHandle> unregister(IStarHandle handle) {
		regsiterHandles.remove(handle);
		return regsiterHandles;
	}

	public void handle(JSONObject destJson) {
		if (regsiterHandles.size() > 0) {
			for (IStarHandle hi : regsiterHandles) {
				hi.handle(destJson);
			}
		}
	}
	
	public static void handle(DatagramPacket packet){
		String message = new String(packet.getData(), 0, packet.getLength());// very
		JSONObject jsonObject = JSON.parseObject(message);
		getRecieveHandle().handle(jsonObject);
	}
	public static void handle(String message){
		JSONObject jsonObject = JSON.parseObject(message);
		getRecieveHandle().handle(jsonObject);
	}
	
	private static RecieHandle getRecieveHandle(){
		if(handleInstance==null){
			handleInstance = new RecieHandle();
			String configFile = SystemProperties
					.getParamByKey(UpdaterConstants.UPDATE_PROP_FILE);
			if (Q6Properties.isOpen(UpdaterConstants.UPDATE_VERSION_HANDLE_OPEN,
					configFile)) {
				handleInstance.register(new VersionHandle());
			}
			if (Q6Properties.isOpen(UpdaterConstants.IE_FAVORITES_HANDLE_OPEN,
					configFile)) {
				handleInstance.register(new WinApsUpFavoritesHandle());
				handleInstance.register(new WinApsDownFavoritesHandle());
			}
			if (Q6Properties.isOpen(UpdaterConstants.UPDATE_FILE_HANDLE_OPEN,
					configFile)) {
				handleInstance.register(new FileHandle());
			}
			if (Q6Properties.isOpen(UpdaterConstants.IE_FAVORITES_LOCAL_OPEN,
					configFile)) {
				handleInstance.register(new LocalFavoritesHandle());
			}
			if (Q6Properties.isOpen(UpdaterConstants.RETART_SWITCH,
					configFile)) {
				handleInstance.register(new RestartHandle());
			}
		}		
		return handleInstance;
	}

}
