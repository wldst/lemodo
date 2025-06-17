package com.wldst.ruder.domain;

public class Constants {

	public final static String APP_CODE = "";// 获取系统所需码值

	public final static Integer PAGE_MAX_SIZE = 100;// 不分页的情况下，限制查询吐出数量,最多查询100条

	public final static String SESSION_TOKEN_PREFIX = "ERP:TOKEN:";// 会话key前缀

	public final static String REDIS_PREFIX_USERINFO = "ERP-USERINFO-";// 存放在Redis中系统用户前缀
	public final static String REDIS_PREFIX_ISC_ORG = "ISC-DEPT-";// Isc中部门前缀
	public final static String REDIS_PREFIX_EXECUTE_DT = "STATISTICAL-EXECUTEDT-";// Isc中部门前缀

	public final static String RESULT_SUCCESS_CODE = "1";// 服务器server端返回成功
	public final static String RESULT_FAILED_CODE = "-1";// 服务器server端返回失败
	public final static String RESULT_SUCCESS_STATUS = "SUCCESS";// 成功请求标识
	public final static String RESULT_FAILED_STATUS = "FAILED";// 失败请求标识

	public final static String DICT_INFO_PREFIX = "dict_info"; // Redis配置:字典类信息前缀
	public final static String DICT_INIT_STATUS_KEY = "dict_init"; // Redis配置:字典类初始化状态key
	public final static String DICT_INIT_STATUS_OK = "1"; // Redis配置:字典类初始化状态：已完成
	public final static String DICT_INIT_STATUS_RUNNING = "2"; // Redis配置:字典类初始化状态：正在进行
	public final static String DICT_INIT_STATUS_FIELD = "3"; // Redis配置:字典类初始化状态：失败

	public final static String QUARTZ_GROUP_NAME = "Task_Group"; // 定时任务组
	public final static String QUARTZ_KEY = "T_ERP_"; // 定时任务id前缀

	public final static String SYSTEM_CHARSET = "UTF-8";// 全局字符编码
    /**
     * 前后端传递的token名称
     */
	public final static String SESSION_TOKEN_NAME = "token";
	//用户状态-正常
	public final static String USER_STATUS_NORMAL = "1";
	
	 /**
     * excel类型 .xls
     */
	public final static int EXCEL_TYPE_XLS = 1;

    /**
     * excel类型 .xlsx
     */
	public final static int EXCEL_TYPE_XLSX = 2;
}