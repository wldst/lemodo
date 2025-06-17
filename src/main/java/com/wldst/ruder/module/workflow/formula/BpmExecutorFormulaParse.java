package com.wldst.ruder.module.workflow.formula;

import java.util.Map;

import com.wldst.ruder.module.bs.BeanShellService;
import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.BeanShellDomain;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.workflow.constant.BpmDo;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * 流程实例之执行人公式解析
 * 
 * @author wldst
 */
public class BpmExecutorFormulaParse extends BpmDo{
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(BpmExecutorFormulaParse.class);

    // 业务数据关联表名
    private String bizTableName;

    // 业务数据ID
    private long bizDataID;

    // 流程实例对象
    private Map<String, Object> workflow;
	private BeanShellService bss;

    private UserAdminService adminService;

    private BeanShellDomain bsDomain;


    // 关联业务数据对象
    private Map<String, Object> mainDataObject;

	private CrudNeo4jService repo;

    /**
     * 流程实例之执行人公式解析构造函数
     * 
     * @param bizTabName 关联业务数据表名
     * @param bizDataID  关联业务数据ID
     * @param workflow   对应的流程对象
     */
    public BpmExecutorFormulaParse(String bizTabName, long bizDataID, Map<String, Object> workflow, CrudNeo4jService repo, BeanShellService bss, UserAdminService adminService) {
	this.bizTableName = bizTabName;
	this.bizDataID = bizDataID;
	this.workflow = workflow;
        this.bss=bss;
        this.adminService=adminService;
        this.repo=repo;
    }

    /**
     * * 判断当前登录人是否某角色
     * 
     */
    public boolean isRole(String roleName) throws Exception {
	boolean retBoolean = false;
	try {
	    return  adminService.hasRole(adminService.getCurrentPasswordId(), roleName);
	} catch (Exception ex) {
	    LoggerTool.error(logger,"获取指定角色所分配的用户ID失败:", ex);
	}
	return retBoolean;
    }

    /**
     * 判断传入参数的人员是否某角色
     * 
     * @param roleName
     * @param empID
     * @return
     * @throws Exception
     */
    public boolean isRole(String roleName, String empID) throws Exception {
	boolean retBoolean = false;
	try {
	   return  adminService.hasRole(Long.valueOf(empID), roleName);
	} catch (Exception ex) {
	    LoggerTool.error(logger,"获取指定角色所分配的用户ID失败:", ex);
	}
	return retBoolean;
    }

    /**
     * 根据角色名称获取该角色所分配的用户ID数组
     * 
     * @param roleName 角色名称
     * @return 用户ID数组
     */
    public long[] getUsersByRoleName(String roleName) {
	long[] retArray = null;
	try {
	    return adminService.getUserListBy(roleName);	
	} catch (Exception ex) {
	    LoggerTool.error(logger,"获取指定角色所分配的用户ID失败:", ex);
	}
	return retArray;
    }

    /**
     * 根据角色名称获取该角色所分配的用户ID数组
     * 
     * @param roleNames 角色名称
     * @return 用户ID数组
     */
    public long[] getUsersByRoleNames(String... roleNames) {
	long[] retArray = null;
	try {
	    if (null != roleNames && roleNames.length > 0) {
		 return adminService.getUserListByRolesName(roleNames);
	    } else {
		return retArray; // 未传递参数直接返回空值
	    }
	} catch (Exception ex) {
	    LoggerTool.error(logger,"获取指定角色所分配的用户ID失败:", ex);
	}
	return retArray;
    }

    /**
     * 根据角色ID获取该角色所分配的用户ID数组
     * 
     * @param roleID 角色ID
     * @return 用户ID数组
     */
    public long[] getUsersByRoleID(long roleID) {
	long[] retArray = null;
	try {
	    return adminService.getUserListBy(roleID);	    
	} catch (Exception ex) {
	    LoggerTool.error(logger,"获取指定角色所分配的用户ID失败:", ex);
	}
	return retArray;
    }

    /**
     * 根据流程角色名编号
     * 
     * @param wfRoleCodes 流程角色名编号
     * @return 用户ID数组
     */
    public long[] getUsersByRoleCode(String... wfRoleCodes) {
	long[] retArray = null;
	try {
	    return adminService.getUserListByRolesCode(wfRoleCodes);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    LoggerTool.error(logger,"获取指定流程角色所分配的用户ID失败:", ex);
	}
	return retArray;
    }

    /**
     * 根据流程角色编码和组织机构ID获取配置的用户ID数据
     * 
     * @param wfRoleCode 流程角色编码
     * @param orgId      组织机构ID
     * @return 用户ID数组
     */
    public long[] getUsersByRoleCodeAndOrgId(String wfRoleCode, String orgId) {
	long[] retArray = null;
	try {
	     return adminService.getUserListBy(wfRoleCode,orgId);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    LoggerTool.error(logger,"获取指定流程角色所分配的用户ID失败:", ex);
	}
	return retArray;
    }

    /**
     * 根据角色ID和组织机构ID获取分配的用户ID数组
     * 
     * @param roleID           角色ID
     * @param orgID            组织机构ID
     * @param includeChildFlag 是否检查下级单位
     * @return 用户ID数组
     */

    public long[] getUsersByRoleID(long roleID, long orgID, boolean includeChildFlag) {
	long[] retArray = null;
	try {
	   
	} catch (Exception ex) {
	    LoggerTool.error(logger,"获取指定角色所分配的用户ID失败:", ex);
	}
	return retArray;
    }
    
    public String get(String label,String id,String fieldName) {
	String retStr = null;
	try {
	    if (this.mainDataObject == null) {
		StringBuffer sqlBuffer = new StringBuffer();
		 
		 
	    }

	    if (this.mainDataObject != null) {
		Object obj = this.mainDataObject.get(fieldName);
		if (null != obj) {
		    retStr = obj.toString();
		} else {
		    LoggerTool.error(logger,"获取流程关联业务主表信息失败:");
		}
	    }
	} catch (Exception ex) {
	    LoggerTool.error(logger,"获取流程关联业务主表信息失败:", ex);
	}
	return retStr;
    }

    /**
     * 获取关联业务主表数据指定字段的值
     * 
     * @param fieldName 指定字段名
     * @return 字段的值
     */
    public String getMainTableFieldValue(String primaryKey, String fieldName) {
	String retStr = null;
	try {
	   
	    if (this.mainDataObject != null) {
		Object obj = this.mainDataObject.get(fieldName);
		if (null != obj) {
		    retStr = obj.toString();
		} else {
		    LoggerTool.error(logger,"获取流程关联业务主表信息失败:");
		}
	    }else {
		
	    }
	} catch (Exception ex) {
	    LoggerTool.error(logger,"获取流程关联业务主表信息失败:", ex);
	}
	return retStr;
    }

    /**
     * 解析执行人公式，执行人脚本
     * 
     * @param executorFormula 执行人公式
     * @return 解析后的执行人信息
     * @throws Exception
     */
    public long[] parseExecutorFormula(String executorFormula) throws Exception {
	if (logger.isDebugEnabled()) {
	    LoggerTool.debug(logger,"now we enter XmlWorkFlowFormulaParse#parseExecutorFormula");
	}
	long[] retArray = null;
	try {
	    if (executorFormula != null && executorFormula.trim().length() > 0) {
		Interpreter bsh = new Interpreter();
		StringBuffer codeBuffer = new StringBuffer();
		codeBuffer.append(executorFormula);
		bsh.set("parse", this);
		bsh.eval(codeBuffer.toString());
		retArray = (long[]) bsh.get("returnValue");
	    }
	} catch (Exception ex) {
	    LoggerTool.error(logger,"人员公式错误,解析执行失败:", ex);
	    throw new Exception("人员公式错误,解析执行失败:", ex);
	}
	return retArray;
    }
    /**
     * 自定的脚本，查询执行人
     * @param shellName
     * @return
     */
    public Object excuteBeanShell(String shellName) {
	Map<String, Object> beanShell = repo.getAttMapBy(NAME, shellName, "BeanShell");
	Interpreter in = new Interpreter();
	try {
	    in.set("bizId", bizDataID);
	    in.set("label", bizTableName);
		bss.init(in);
	    in.setStrictJava(true);
	    String string = string(beanShell, bsDomain.BS_SCRIPT);
	    in.eval(string);
	    Object returnValue = in.get("returnValue");
	    return returnValue;
	} catch (EvalError e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return null;
    }
    
    
}
