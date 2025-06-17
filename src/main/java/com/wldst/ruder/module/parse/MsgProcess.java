package com.wldst.ruder.module.parse;

import java.util.Map;

import com.wldst.ruder.util.MapTool;
/**
 * 消息处理
 * @author wldst
 *
 */
public interface MsgProcess {
    Object process(String msg, Map<String, Object> context);
    public default Object doWithSelected(Map<String, Object> context) {
	 Integer integer = MapTool.integer(context, "which");
	Map<String, Object> map = MapTool.listMapObject(context, "selectList").get(integer);
	return map;
    }
}
