package com.wldst.ruder.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.wldst.ruder.constant.Constants;

/**
 * 定义前后端交互对象具体属性
 * 
 * @author erpit
 * @version 20201117
 */
public class MsgContext implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -7001172110984678477L;

	/**
	 * 服务代码
	 */
	private String code = Constants.RESULT_SUCCESS_CODE;

	/**
	 * 通知消息
	 */
	private String msg;

	/**
	 * 结果数据
	 */
	private Map<String, Object> result = new HashMap<String, Object>();

	public MsgContext() {
		this.setCode(Constants.RESULT_SUCCESS_CODE);
		TokenHolder.set(this);
	}

	/**
	 * 方便使用，添加返回结果
	 * 
	 * @param key   值名称
	 * @param value 具体值
	 */
	public void addResult(String key, Object value) {
		if (result == null) {
			result = new HashMap<String, Object>();
		}
		result.put(key, value);
	}

	/**
	 * 返回失败的结果（不带Msg）
	 */
	public static MsgContext failed() {
		MsgContext mc = new MsgContext();
		mc.setCode(Constants.RESULT_FAILED_CODE);
		return mc;
	}

	/**
	 * 返回失败的结果（带Msg）
	 */
	public static MsgContext failed(String msg) {
		MsgContext mc = new MsgContext();
		mc.setCode(Constants.RESULT_FAILED_CODE);
		mc.setMsg(msg);
		return mc;
	}

	/**
	 * 返回成功的结果（不带Msg）
	 */
	public static MsgContext success() {
		MsgContext mc = new MsgContext();
		mc.setCode(Constants.RESULT_SUCCESS_CODE);
		return mc;
	}

	/**
	 * 返回成功的结果（带Msg）
	 */
	public static MsgContext success(String msg) {
		MsgContext mc = new MsgContext();
		mc.setCode(Constants.RESULT_SUCCESS_CODE);
		mc.setMsg(msg);
		return mc;
	}

	/**
	 * 返回成功的结果（带Msg和ID）
	 */
	public static MsgContext success(String msg, String id) {
		MsgContext mc = new MsgContext();
		mc.setCode(Constants.RESULT_SUCCESS_CODE);
		mc.setMsg(msg);
		mc.addResult("id", id);
		return mc;
	}

	/**
	 * 返回成功的结果，没有提示信息，结果集对应的key将为page
	 * 
	 * @param pageValue 存储了结果集的分页对象
	 */
	public static MsgContext success(Object pageValue) {
		MsgContext mc = new MsgContext();
		mc.setCode(Constants.RESULT_SUCCESS_CODE);
		mc.addResult("page", pageValue);
		return mc;
	}

	/**
	 * 返回成功的结果，结果集对应的key将为page
	 * 
	 * @param msg       成功的提示信息
	 * @param pageValue 存储了结果集的分页对象
	 */
	public static MsgContext success(String msg, Object pageValue) {
		MsgContext mc = new MsgContext();
		mc.setCode(Constants.RESULT_SUCCESS_CODE);
		mc.setMsg(msg);
		mc.addResult("page", pageValue);
		return mc;
	}

	/**
	 * 返回成功的结果，没有提示信息，并将值放入指定的key之中
	 * 
	 * @param key   对象值对应的key
	 * @param value 对象
	 */
	public static MsgContext successNotMsg(String key, Object value) {
		MsgContext mc = new MsgContext();
		mc.setCode(Constants.RESULT_SUCCESS_CODE);
		mc.addResult(key, value);
		return mc;
	}

	/**
	 * 返回成功的结果，并将值放入指定的key之中
	 * 
	 * @param msg   成功的提示信息
	 * @param key   对象值对应的key
	 * @param value 对象
	 */
	public static MsgContext success(String msg, String key, Object value) {
		MsgContext mc = new MsgContext();
		mc.setCode(Constants.RESULT_SUCCESS_CODE);
		mc.setMsg(msg);
		mc.addResult(key, value);
		return mc;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Map<String, Object> getResult() {
		return result;
	}

	public void setResult(Map<String, Object> result) {
		this.result = result;
	}
}