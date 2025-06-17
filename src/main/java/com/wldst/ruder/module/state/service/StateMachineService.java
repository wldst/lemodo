package com.wldst.ruder.module.state.service;

import java.util.List;
import java.util.Map;

/**
 * 后台管理员Service
 * Created by macro on 2018/4/26.
 */
public interface StateMachineService {
	/**
	 *  当前对象的状态机
	 * @param nodeId
	 * @return
	 */
    Map<String, Object> myStateMachie(Long nodeId);
    /**
     *   当前对象所属对象对应的状态机的事务。
     * @param nodeId
     * @return
     */
    List<Map<String, Object>> transactions(Long nodeId);
    /**
     *    history status
     * @param nodeId
     * @return
     */
    List<Map<String, Object>> history(Long nodeId);
    
    /**
     * 事件处理
     * @param eventId
     */
    void event(Long eventId,Long objId);
  
}
