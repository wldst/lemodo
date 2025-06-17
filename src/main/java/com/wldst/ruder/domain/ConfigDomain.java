package com.wldst.ruder.domain;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.RelationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 系统设置相关的常量
 * 
 * @author wldst
 *
 */
public class ConfigDomain extends StatusDomain {

    public static final String CONFIGURATION = "Configuration";
    protected static final String MY_SETTING = "MySetting";
    protected static final String BACKGROUND_IMG = "BackGroundImg";

    

    

}
