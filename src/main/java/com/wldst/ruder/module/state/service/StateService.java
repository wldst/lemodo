package com.wldst.ruder.module.state.service;

import java.util.List;
import java.util.Map;

/**
 * 后台管理员Service
 * Created by macro on 2018/4/26.
 */
public interface StateService {
	/**
	 * 下一状态
	 * @param nodeId
	 * @return
	 */
    Map<String, Object> nextStatus(Long nodeId);
    /**
     * 当前状态
     * @param nodeId
     * @return
     */
    Map<String, Object> currentStatus(Long nodeId);
	public String statusCode(Map<String, Object> task);
	public String statusName(Map<String, Object> task);
	public Long statusId(Map<String, Object> task);
    /**
     * 返回上一状态
     * @param nodeId
     * @return
     */
    Map<String, Object> preStatus(Long nodeId);
    /**
     * 最后一个状态
     * @param nodeId
     * @return
     */
    Map<String, Object> lastStatus(Long nodeId);
    /**
     * 返回最初的状态
     * @param nodeId
     * @return
     */
    Map<String, Object> firstStatus(Long nodeId);
    /**
     * 设置状态
     * @param nodeId
     * @param statusId
     */
   void setStatus(Long nodeId,Long statusId);
   /**
    * 初始化状态
    * @param nodeId
    */
	void initStatus(Long nodeId);
	void updateStatus(Long nodeId,Map<String, Object> status);
	void updateStatus(Long nodeId,Long statusId);
	
	/**
	 * 判断是否有状态机
	 * @param label
	 * @return
	 */
	List<Map<String, Object>> listStatus(String label);

	void statusRefresh(String label, Map<String, Object> vo, long id2);
   
  
}
