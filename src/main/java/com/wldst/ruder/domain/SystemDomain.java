package com.wldst.ruder.domain;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.RelationService;

/**
 * 系统设置相关的常量
 * 
 * @author wldst
 *
 */
public class SystemDomain extends StatusDomain {
    public static final String SETTING = "Settings";
    public static final String MY_SETTING = "MySetting";
    protected static final String BACKGROUND_IMG = "BackGroundImg";


    public static final String VALUE = "value";
    public static final String PLUGIN_PATH = "plugin.path";
    public static final String INIT_DATA = "file.data";
    public static final String PROT_DATA = "port.data";
    public static final String SYSTEM_LABEL = "system";
    public static final String STATE = "STATE";

    public static final String VERSION_LOG = "versionLog";
    public static final String LOGIN_LOG ="LoginLog";
    public static final String OPERATE_LOG ="operateLog";

    public static final String LABEL_NOTICE ="Notice";
    
    public static final String CMD_NOTICE ="notice";
    public static final String CMD ="cmd";
    public static final String CMD_REPORT ="report";
    public static final String CMD_REPORT_DATA ="reportData";
    public static final String CMD_DATA_LIST ="dataList";
    public static final String CMD_UP_DATA_LIST ="upDataList";
    
    public static final String CMD_PUSH ="push";
    
    public final static String HOST = "host";
    public final static String URI = "uri";
    public final static String PORT = "port";

    public final static String MICRO_SERVICE = "MicroService";
    
    @SuppressWarnings("preview")
    public static ExecutorService getExecutorService() {
	 return  Executors.newFixedThreadPool(10);
////             .newVirtualThreadExecutor();
//	 return Executors.newVirtualThreadPerTaskExecutor();
    }
    
    
    public static String host(Map<String, Object> mapData) {
	    return string(mapData, HOST);
    }
    public static String uri(Map<String, Object> mapData) {
	    return string(mapData, URI);
    }
    public static Integer port(Map<String, Object> mapData) {
	    return integer(mapData, PORT);
    }
    public static String portStr(Map<String, Object> mapData) {
	    return string(mapData, PORT);
    }
    

}
