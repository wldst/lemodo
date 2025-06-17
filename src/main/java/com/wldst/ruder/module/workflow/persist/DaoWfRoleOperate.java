package com.wldst.ruder.module.workflow.persist;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.RowMapper;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.wldst.ruder.module.workflow.beans.WfRoleInfo;
import com.wldst.ruder.module.workflow.util.NumberUtil;

public class DaoWfRoleOperate{
	
	
	/**
	 * 生成流程编码数字部分
	 * @param sql
	 * @return 流程编码末尾数字部分
	 */
	private String returnCodeNum() {
		String sql = "select max(substr(A.rolenamecode,11,8)) as rolenamecode from PUB_WF_ROLEINFO A";
		String lastNum ="0";
//		String.valueOf(Long.parseLong(this.queryForMapWithSql
//				(sql).get("ROLENAMECODE").toString()) + 1);
		StringBuffer codeNum = new StringBuffer();
		int zeroLen = 8 - lastNum.length();
		for(int i = 0;i < zeroLen;i++) {
			codeNum.append("0");
		}
		codeNum.append(lastNum);
		return codeNum.toString();
	}
	/**
	 * 检查要保存流程角色编号是否已经存在
	 * @param roleCode
	 * @return
	 */
	public boolean checkRoleNameCodeIsExist(String roleCode) {
		String sql = " select * from pub_wf_roleinfo where ROLENAMECODE = ? ";
		List<Map<String,Object>> list = null;
		return list != null && list.size() > 0 ? true : false;
	}
	
	/**
	 * 流程角色查询分页
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public void queryWfRoleInfoPaged(Map<String,String> params) {
		String roleNameCode = params.get("srcRoleNameCode");
		String WFRoleName = params.get("srcWFRoleName");
		List<Object> args = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT * FROM PUB_WF_ROLEINFO")
		.append(" WHERE 1=1");
		//流程角色编号
		if (StringUtils.isNotBlank(roleNameCode)) {
			sql.append(" AND ROLENAMECODE LIKE ?");
			args.add("%" + roleNameCode + "%");
		}
		//流程角色名
		if (StringUtils.isNotBlank(WFRoleName)) {
			sql.append(" AND WFROLENAME LIKE ?");
			args.add("%" + WFRoleName + "%");
		}
		
		sql.append(" ORDER BY ROLENAMECODE");
		
		Long count = 0l;//queryCountForSql(sql.toString(), args.toArray());
		int pageNo = 1;
		int pageSize = 10;
//		Constants.SEARCH_PAGE_SIZE;

		// 获取当前查询页
//		String page = WebContextHolder.getRequest().getParameter("page");
//		if (StringUtils.isNotBlank(page)) {
//			pageNo = Integer.parseInt(page);
//		}

		// 获取每页显示记录数
		String rows = "";//WebContextHolder.getRequest().getParameter("rows");
		if (StringUtils.isNotBlank(rows)) {
			pageSize = Integer.parseInt(rows);
		}

//		PageFinder finder = new PageFinder(pageNo, pageSize, count.intValue());
		List<Map<String,Object>> datas = new ArrayList<>();
		if (count > 0) {
			if (null == args || args.toArray().length == 0) {
				datas = null;//this.queryForListWithSql(sql.toString(), pageNo,
//						pageSize);
			} else {
				datas = null;//this.queryForListWithSql(sql.toString(), args.toArray(),
//						pageNo, pageSize);
			}
		}
//		finder.setRows(datas);
//		return finder;
	}
	
	/**
	 * 删除流程角色人员关联关系
	 * @param ids
	 */
	public void deleteWfRoleAndUser(String[] ids) {
		String sql = "DELETE FROM PUB_ROLE_USER WHERE WFROLEID = ?";
		List<Object[]> args = new ArrayList<Object[]>();
		if (null != ids && ids.length > 0) {
			for (String id : ids) {
				args.add(new Object[]{id});
			}
//			this.batchUpdateWithSql(sql, args);
		}
	}
	
	/**
	 * 删除流程角色
	 * @param ids
	 */
	public void deleteWfRoleInfo(String[] ids) {
		String sql = "DELETE FROM PUB_WF_ROLEINFO WHERE ID = ?";
		List<Object[]> args = new ArrayList<Object[]>();
		if (null != ids && ids.length > 0) {
			for (String id : ids) {
				args.add(new Object[]{id});
			}
//			this.batchUpdateWithSql(sql, args);
		}
	}
	
	
	
	public void distributeWFRole(Object[] params) {
		StringBuffer createSql = new StringBuffer();
		createSql.append("INSERT INTO PUB_ROLE_USER(")
		.append("ID,WFROLEID,EMPID)")
		.append(" VALUES (?,?,?)");
//		this.updateWithSql(createSql.toString(), params);
	}
	public void distributeWFRole(List<Object[]> args) {
		StringBuffer createSql = new StringBuffer();
		createSql.append("INSERT INTO PUB_ROLE_USER(")
		.append("ID,WFROLEID,EMPID)")
		.append(" VALUES (?,?,?)");
//		this.batchUpdateWithSql(createSql.toString(), args);
	}
	
	public void deleteUserAndRoleByID(String[] ids) {
		String sql = "DELETE FROM PUB_ROLE_USER WHERE ID = ?";
		List<Object[]> args = new ArrayList<Object[]>();
		if (null != ids && ids.length > 0) {
			for (String id : ids) {
				args.add(new Object[]{id});
			}
//			this.batchUpdateWithSql(sql, args);
		}
	}
	
	
	/**
	 * 分页查询所有用户信息和人员编号
	 * @return List
	 */
	public void queryAllUserAndEmpId(Map<String, String> params) {
		StringBuffer sql = new StringBuffer();
		List<Object> args = new ArrayList<Object>();
		String wfRoleID = params.get("srcWFRoleID");
		args.add(wfRoleID);
		String userName = params.get("srcUserName");
		String orgName = params.get("srcOrgName");
		String empNo = params.get("srcEmpNo");
		String empType = params.get("empType");
		sql.append("SELECT DISTINCT A.ID,A.STATUS,A.EMPTYPE,A.EMPNO,A.NAME,C.ORGNAME,D.STATIONNAME FROM B_HR_HIM_EMPLOYEE A ")
		.append(" LEFT JOIN B_HR_HIM_ORGANDEMPLOYEE B ON B.EMPLOYEEID = A.ID ")
		.append(" LEFT JOIN B_HR_ORG_ORGANIZATION C ON B.ORGID = C.ID LEFT JOIN B_HR_HIM_STATION D ON D.ID = B.STATIONID WHERE 1 = 1")
		.append(" AND A.ID NOT IN (SELECT EMPID FROM PUB_ROLE_USER WHERE WFROLEID = ?) AND A.STATUS = 1 AND B.RELATIONTYPE = 1");
				
		if (StringUtils.isNotBlank(userName)) {
			sql.append(" AND A.NAME LIKE ?");
			args.add("%" + userName + "%");
		}
		if (StringUtils.isNotBlank(orgName)) {
			sql.append(" AND C.ID IN (");
			String[] orgNameArray = orgName.split(",");
			int orgNameLen = orgNameArray.length;
			int num = 0;
			for(String orgname:orgNameArray) {
				num++;
				if(num < orgNameLen) {
					sql.append("'" + orgname + "',");
				}else {
					sql.append("'" + orgname + "'");
				}				
			}
			sql.append(")");
		}
		if (StringUtils.isNotBlank(empNo)) {
			sql.append(" AND A.EMPNO LIKE ?");
			args.add("%" + empNo + "%");
		}
		if(StringUtils.isNotBlank(empType)){
			sql.append(" AND A.EMPTYPE = ? ");
			args.add(empType);
		}
//		
//		return this.pagedForSql(NumberUtil.parseInt(params.get("page"), 1), NumberUtil.parseInt(params.get("rows"), 20), sql.toString()
//				, args.toArray());
	}
	
	/**
	 * 分页查询所有用户信息和人员编号
	 * @return List
	 */
	public void queryUserByWFRoleID(Map<String,String> params) {
		StringBuffer sql = new StringBuffer();
		String wfRoleID = params.get("wfRoleID");
		String src_EmpId = params.get("src_EmpId");
		String src_OrgId = params.get("src_OrgId");
		String src_empType = params.get("src_empType");
		String src_wfRoleName = params.get("src_wfRoleName");
		List<Object> args = new ArrayList<Object>();
		sql.append("SELECT DISTINCT B.ID,A.STATUS,A.EMPTYPE,A.EMPNO,A.NAME,D.ORGNAME,E.STATIONNAME,F.WFROLENAME FROM B_HR_HIM_EMPLOYEE A INNER JOIN PUB_ROLE_USER B ON A.ID = B.EMPID")
			.append(" LEFT JOIN B_HR_HIM_ORGANDEMPLOYEE C ON C.EMPLOYEEID = A.ID AND C.RELATIONTYPE = 1")
			.append(" LEFT JOIN B_HR_ORG_ORGANIZATION D ON C.ORGID = D.ID LEFT JOIN B_HR_HIM_STATION E ON E.ID = C.STATIONID LEFT JOIN PUB_WF_ROLEINFO F ON B.WFROLEID = F.ID WHERE 1 = 1");
		
		if (StringUtils.isNotBlank(wfRoleID) && !"0".equals(wfRoleID)) {
			sql.append(" AND WFROLEID = ?");
			args.add(wfRoleID);
		}
		if (StringUtils.isNotBlank(src_empType)) {
			sql.append(" AND A.EMPTYPE = ?");
			args.add(src_empType);
		}
		if (StringUtils.isNotBlank(src_wfRoleName)) {
			sql.append(" AND F.WFROLENAME LIKE ?");
			args.add("%" + src_wfRoleName + "%");
		}
		StringBuffer tempSql = null;
		if(StringUtils.isNotBlank(src_EmpId)){
			String [] tempEmpIdArr = src_EmpId.split(",");
			if(tempEmpIdArr.length > 0 ){
				tempSql = new StringBuffer();
				tempSql.append(" AND ( ");
					for (int i = 0; i < tempEmpIdArr.length; i++) {
						if(i == 0 ){
							tempSql.append(" A.ID = '").append(tempEmpIdArr[i]).append("' ");
						}else{
							tempSql.append(" OR A.ID = '").append(tempEmpIdArr[i]).append("' ");
						}
					}
				tempSql.append(" ) ");
			}
			if(null != tempSql){
				sql.append(tempSql.toString());
				tempSql = null;
			}
		}
		if(StringUtils.isNotBlank(src_OrgId)){
			String [] tempOrgIdArr = src_OrgId.split(",");
			if(tempOrgIdArr.length > 0 ){
				tempSql = new StringBuffer();
				tempSql.append(" AND ( ");
					for (int i = 0; i < tempOrgIdArr.length; i++) {
						if(i == 0 ){
							tempSql.append(" C.ORGID = '").append(tempOrgIdArr[i]).append("' ");
						}else{
							tempSql.append(" OR C.ORGID = '").append(tempOrgIdArr[i]).append("' ");
						}
					}
				tempSql.append(" ) ");
			}
			if(null != tempSql){
				sql.append(tempSql.toString());
				tempSql = null;
			}
		}
		sql.append(" ORDER BY A.EMPNO,A.NAME");
//		return this.pagedForSql(NumberUtil.parseInt(params.get("page"), 1), NumberUtil.parseInt(params.get("rows"), 20), sql.toString()
//				, args.toArray());
	}
	
	/**
	 * 根据父节点查询系统代码的所有下一级节点
	 * 
	 * @param parentId
	 *            父节点ID
	 * @return
	 */
	public List<Map<String,Object>> querySysCodeInfoForTree(String parentId) {
		List<Map<String,Object>> retList = new ArrayList<Map<String,Object>>();	
		if (StringUtils.isNotBlank(parentId)) {			
			String sql = "SELECT DISTINCT * FROM PUB_WF_ROLEINFO order by WFROLENAME ";
//			retList = this.queryForListWithSql(sql.toString());
			for(Map<String,Object> retMap:retList) {
				retMap.put("PARENTID", 0);
				retMap.put("ISPARENT", false);
			}
		} else {
			Map<String,Object> retMap = new HashMap<String,Object>();
			retMap.put("ID", 0);
			retMap.put("WFROLENAME", "流程角色根目录");
			retMap.put("ROLENAMECODE", null);
			retMap.put("REMARK", null);
			retMap.put("PARENTID", null);
			retMap.put("ISPARENT", true);
			retList.add(retMap);
		}
		
		return retList;
	}
}
