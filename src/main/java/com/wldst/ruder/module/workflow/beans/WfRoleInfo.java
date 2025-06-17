package com.wldst.ruder.module.workflow.beans;

import java.util.HashMap;
import java.util.Map;

import com.wldst.ruder.util.MapTool;

public class WfRoleInfo {
	//流程角色ID
	private String ID;
	
	//流程角色名
	private String WFRoleName;
	
	//流程角色编号
	private String RoleNameCode;
	
	//备注信息
	private String ReMark;

	public String getID(Map<String, Object> data) {
		return MapTool.string(data,ID);
	}

	public void setID(String id, Map<String, Object> data) {
		data.put(ID,id);
	}

	public String getWFRoleName(Map<String, Object> data) {
		return MapTool.string(data,WFRoleName);
	}

	public void setWFRoleName(String roleName, Map<String, Object> data) {
		data.put(WFRoleName,roleName);
	}

	public String getRoleNameCode(Map<String, Object> data) {
		return MapTool.string(data,RoleNameCode);
	}

	public void setRoleNameCode(String roleNameCode, Map<String, Object> data) {
		data.put(RoleNameCode,roleNameCode);
	}

	public String getReMark() {
		return ReMark;
	}

	public void setReMark(String reMark, Map<String, Object> data) {
		data.put(ReMark,reMark);
	}
	
		
	public Map<String, Object> data(String id, String roleName, String roleNameCode,String reMark) {
	    Map<String, Object> data = new HashMap<>();
		data.put(ID,id);
		data.put(WFRoleName,roleName);
		data.put(RoleNameCode ,roleNameCode);
		data.put(ReMark,reMark);
		return data;
	}	
}
