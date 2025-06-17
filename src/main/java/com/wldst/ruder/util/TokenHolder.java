package com.wldst.ruder.util;

/**
 * 主要存放会话token
 * 
 * @author erpit
 * @version 20201117
 */
public class TokenHolder {
	/**
	 * 会话token
	 */
	private static final ThreadLocal<String> SESSION_TOKEN = new ThreadLocal<String>();

	/**
	 * 前后端交互消息
	 */
	private static final ThreadLocal<MsgContext> MSG_CONTEXT = new ThreadLocal<MsgContext>();

	/**
	 * 存放token
	 * 
	 * @param token 会话令牌
	 */
	public static void set(String token) {
		SESSION_TOKEN.set(token);
	}

	/**
	 * 存放消息
	 * 
	 * @param msgContext 消息对象
	 */
	public static void set(MsgContext msgContext) {
		MSG_CONTEXT.set(msgContext);
	}

	/**
	 * 
	 * 删除
	 */
	public static void remove() {
		SESSION_TOKEN.remove();
		MSG_CONTEXT.remove();
	}

	/**
	 * 
	 * 获取token
	 * 
	 * @return token
	 */
	public static String get() {
		return SESSION_TOKEN.get();
	}

	/**
	 * 
	 * 获取消息
	 * 
	 * @return 消息
	 */
	public static MsgContext getMsgContext() {
		return MSG_CONTEXT.get();
	}
}