package com.wldst.ruder.domain;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.wldst.ruder.module.state.service.StateService;
import com.wldst.ruder.util.NodeTool;

/**
 * 状态相关
 * 
 * @author wldst
 *
 */
public class StatusDomain extends NodeTool {
	public static final String STATUS = "status";
	public static final String STATE_MACHINE = "stateMachine";
    public static final String STATE_STEP = "stateStep";

    protected static final String STATUS_ON = "1";
    protected static final String STATUS_OFF = "0";

    protected static final String STATUS_OPEN = "0";
    protected static final String STATUS_CLOSE = "0";
    public static String status(Map<String, Object> mapData) {
	return string(mapData, STATUS);
    }

}
