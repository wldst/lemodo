package com.wldst.ruder.constant;

public class WechatConstants {
	//微信推送的agentId start
	/**
	 * 公司战报
	 */
	public static final String WECHAT_AGENT_ID_GSZB = "1000004";
	/**
	 * 票务管理
	 */
	public static final String WECHAT_AGENT_ID_PWGL = "5";
	/**
	 * 通知公告
	 */
	public static final String WECHAT_AGENT_ID_TZGG = "7";
	/**
	 * 众包创新
	 */
	public static final String WECHAT_AGENT_ID_ZBCX = "13";
	/**
	 * 待办提醒
	 */
	public static final String WECHAT_AGENT_ID_DBTX = "14";
	/**
	 * 员工考勤
	 */
	public static final String WECHAT_AGENT_ID_YGKQ = "2";
	
	/**
	 * 销售管理
	 */
	public static final String WECHAT_AGENT_ID_XSGL = "1000008";
	/**
	 * 商机管理
	 */
	public static final String WECHAT_AGENT_ID_SJGL = "1000014";
	//微信推送的agentId end
	
	//微信推送消息类型 start
	/**
	 * 全员推送
	 */
	public static final String WECHAT_PUSH_TYPE_ALL = "1";
	
	/**
	 * 角色推送
	 */
	public static final String WECHAT_PUSH_TYPE_ROLE = "2";
	
	/**
	 * 自定义推送
	 */
	public static final String WECHAT_PUSH_TYPE_CUSTOM = "3";
	//微信推送消息类型 end
	
	//微信推送角色start
	/**
	 * 销售管理全员
	 */
	public static final String MESSAGEROLE1 = "MESSAGEROLE1";
	
	/**
	 * 商机机会类型倒退消息推送角色
	 */
	public static final String MESSAGEROLE2 = "MESSAGEROLE2";
	
	/*机机会类型倒退消息推送 生产管理部主任*/
	public static final String MESSAGE_SCGLBZR="MESSAGE_SCGLBZR" ;
	
	/**
	 * 微信推送创建者(无session情况使用)
	 */
	public static final String WECHAT_CREATOR = "300000000076433264";
	
	/**
	 * 签单消息体
	 */
	public static final String WECHATMESSAGE_SIGN = "2019年度冲刺快讯：恭喜ASCRIPTION_DEPT（销售归属SALE_REGION，区域销售经理SALE_MAJOR），完成BUSINESS_OPP_NAME落单，金额MONEY万。";
	
	/**
	 * 产值消息体
	 */
	public static final String WECHATMESSAGE_CZ = "2019年度冲刺快讯：恭喜NAMES，确认ASCRIPTION_DEPT CONTRACT_NAME，进度PROCESS%，新增产值MONEY万。";
	
	/**
	 * 票务待办消息体
	 */
	public static final String WECHATMESSAGE_TICKETTODO = "您有一条【NAME】的票务申请到达，请及时审批";

	
	/**
	 * 转正通知消息链接
	 */
	public static final String WECHATMESSAGE_TURNOVER = "您好，恭喜您自#{dateOff}起<a href=\'http://vvv:9001/mpmis/messageDetail/toTurnoverPage?applyId=#{applyId}\'>转正</a>！";
	
	/**
	 * 党员调动企业微信通知
	 */
	public static final String WECHATMESSAGE_TRANSFER = "党群部您好，现有#{emName}(工号#{emNo})已办完调动手续，自#{date}起从#{oldDept}部门调动至#{newDept}部门，请备案，谢谢！。";
	
	/**
	 * 票务通知申请人员
	 */
	public static final String WECHATMESSAGE_TICKETEMP = "您的票务申请已到达【NAME】，如未及时审批，请联系！";
}
