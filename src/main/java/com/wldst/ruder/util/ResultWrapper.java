package com.wldst.ruder.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

public class ResultWrapper {
	/***
	 * 消息主键
	 */
	protected static final String MESSAGE_KEY = "message";
	/**
	 * 添加Model消息
	 * @param message
	 */
	public static void addMessage(Model model, String... messages) {
		StringBuilder sb = new StringBuilder();
		for (String message : messages){
			sb.append(message).append(messages.length>1?"<br/>":"");
		}
		model.addAttribute(MESSAGE_KEY, sb.toString());
	}
	
	/****
	 * 处理返回消息
	 * 
	 * @param successful 成功与否提示,true/false
	 * @param modelData 要推送到客户端展示的数据列表
	 * @param page 分页属性
	 * @param messages 校验提示信息
	 * @return
	 */
	public static <T> WrappedResult wrapResult(boolean successful, T modelData, PageObject page, String... messages) {
		StringBuilder sb = new StringBuilder();
		
		JSONObject json = new JSONObject();
		for (String message : messages){
			if(StringUtils.isNotEmpty(message)){
				sb.append(message).append(messages.length>1?"<br/>":"");
			}
		}
		if(modelData==null) {
			return WrappedResult.wrap(successful, null, page, sb.toString());
		}
		if (modelData instanceof Boolean) {
			return WrappedResult.wrap(successful, modelData, page, sb.toString());
		}
		if(modelData instanceof String[]) {
		    return WrappedResult.wrap(successful, modelData, page, sb.toString());
		}
		//若对象为List
		if(modelData instanceof List) {
			JSONArray newList = new JSONArray();
			List<?> modelData2 = (List<?>)modelData;
			if(!modelData2.isEmpty()) {
			    for(Object data : modelData2) {
				newList.add(JSONMapUtil.jsonObject(data));
			}
			}
			
			return WrappedResult.wrap(successful, newList, page, sb.toString());
		}else { //单体对象时
			return WrappedResult.wrap(successful, JSONMapUtil.jsonObject(modelData), page, sb.toString());
		}
	}
	public static <T> WrappedResult ret(boolean successful, T modelData, String... messages) {
		return wrapResult(successful, modelData,null,messages);
	}
	
	public static <T> WrappedResult error(String messages) {
		return wrapResult(false, null,null,messages);
	}
	public static <T> WrappedResult success(T modelData) {
		return wrapResult(true, modelData,null,"操作成功");
	}
	public static <T> WrappedResult failed(T modelData) {
		return wrapResult(true, modelData,null,"操作失败");
	}
}
