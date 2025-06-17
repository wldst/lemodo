package com.wldst.ruder.module.workflow.inherit;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.constant.WechatConstants;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.util.DateUtil;

/**
 * 流程任务自动触发业务接口
 * 
* @author wldst
 */
public abstract class BpmTaskBizExcute {
	// 日志对象
	private static Logger logger = LoggerFactory.getLogger(BpmTaskBizExcute.class);
	CrudNeo4jService neo4jService;
	
	public BpmTaskBizExcute() {
	    //CruderService
	    neo4jService = (CrudNeo4jService) SpringContextUtil.getBean(CrudNeo4jService.class);
	}

	/**
	 * 任务推动之需要执行的业务方法
	 *
	 * @param workflow
	 *            当前流程实例
	 * @param runData
	 *            流程运行对象
	 * @throws CrudBaseException
	 */
	public void performBizExecute(Map<String,Object> workflow, Map<String,Object> runData)
			throws CrudBaseException {

	}

	/**
	 * 任务打回之需要执行的业务方法
	 *
	 * @param workflow
	 *            当前流程实例
	 * @param runData
	 *            流程运行对象
	 * @throws CrudBaseException
	 */
	public void turnbackBizExecute(Map<String,Object> workflow, Map<String,Object> runData)
			throws CrudBaseException {

	}

	/**
	 * 任务同意之需要执行的业务方法
	 *
	 * @param workflow
	 *            当前流程实例
	 * @param runData
	 *            流程运行对象
	 * @throws CrudBaseException
	 */
	public void agreeBizExecute(Map<String,Object> workflow, Map<String,Object> runData)
			throws CrudBaseException {

	}

	/**
	 * 任务不同意之需要执行的业务方法
	 *
	 * @param workflow
	 *            当前流程实例
	 * @param runData
	 *            流程运行对象
	 * @throws CrudBaseException
	 */
	public void disagreeBizExecute(Map<String,Object> workflow, Map<String,Object> runData)
			throws CrudBaseException {

	}

	/**
	 * 任务跳转之需要执行的业务方法
	 *
	 * @param workflow
	 *            当前流程实例
	 * @param runData
	 *            流程运行对象
	 * @throws CrudBaseException
	 */
	public void reloopBizExecute(Map<String,Object> workflow, Map<String,Object> runData)
			throws CrudBaseException {

	}

	
	/**
	 * 新增推送消息(全员推送)
	 * @param creatorId 创建者(18位ID)
	 * @param message 消息
	 * @param agentId 推送到微信的模块
	 * @param pushTime 推送时间(YYYY-MM-DD HH:mm)(YYYY-MM-DD HH:mm)
	 * 
	 */
	public void insertWechatMessage(String creatorId,String message
			, String agentId, String pushTime) {
		if(StringUtils.isBlank(pushTime)) {
			pushTime = DateUtil.nowTime("yyyy-MM-dd HH:mm");
		}
		try {
			//保存消息信息
			String messageId = insertWechatMessage(message, creatorId, WechatConstants.WECHAT_PUSH_TYPE_ALL, agentId, pushTime);
			insertWechatMessageEmp(messageId, "@all");
		} catch (Exception e) {
			throw new CrudBaseException("保存微信推送信息失败："+e.getMessage());
		}
	}
	public String insertWechatMessage(String message, String creatorId
		, String pushType, String agentId, String pushTime) {
	    Map<String,Object> data  = new HashMap<>();
	    data.put("message",message);
	    data.put("creatorId",creatorId);
	    data.put("pushType",pushType);
	    data.put("agentId",agentId);
	    data.put("pushTime",pushTime);
	    neo4jService.saveByBody(data, "WechatMessage");
		return pushTime;
	    
	}
	public String insertWechatMessageEmp(String messageId, String empNo) {
	    Map<String,Object> data  = new HashMap<>();
	    data.put("messageId",messageId);
	    data.put("empNo",empNo);
	    neo4jService.saveByBody(null, "MessageEmp");
	    return empNo;
	}

	/**
	 * 新增推送消息(自定义推送)
	 * @param creatorId 创建者(18位ID)
	 * @param message 消息
	 * @param agentId 推送到微信的模块
	 * @param pushType 推送策略
	 * @param empList 人员清单(pmis登录账号)
	 * @param pushTime 推送时间(YYYY-MM-DD HH:mm)
	 */
	public void insertWechatMessage(String creatorId,String message
			, String agentId,List<String> empList, String pushTime) {
		if(StringUtils.isBlank(pushTime)) {
			pushTime = DateUtil.nowTime("yyyy-MM-dd HH:mm");
		}
		try {
			//保存消息信息
			String messageId = insertWechatMessage(message, creatorId, WechatConstants.WECHAT_PUSH_TYPE_CUSTOM, agentId, pushTime);
			for(String empNo : empList) {
				insertWechatMessageEmp(messageId, empNo);
			}
		} catch (Exception e) {
			throw new CrudBaseException("保存微信推送信息失败："+e.getMessage());
		}
	}
	
	
	/**
	 * 元转换为万元
	 * @param money
	 * @return
	 */
	public String wanyuan(String yuan){
		if(StringUtils.isBlank(yuan)){
			return "0";
		}
		double d = this.objToDou(NumberUtil.parseDouble(yuan, 0)/10000);
		return String.valueOf(d);
	}
	
	/**
	 * 将Object转化为Double (null 转化为0)
	 * @param obj
	 * @return Double
	 */
	public Double objToDou(Object obj) {
		DecimalFormat df = new DecimalFormat("#.00");
		String str = "";
		if(obj == null  || "".equals(obj) || "null".equals(obj)){
			str = "0";
		}else{
			str = obj.toString();
		}
		str = df.format(Double.parseDouble(str));
		return Double.parseDouble(str);
	}
	
	
}
