package com.wldst.ruder.module.workflow.biz;

//public class BizWfRoleOperate{
//
//	
//	/**
//	 * 新增或更新流程角色
//	 * @param wfRoleInfo
//	 * @return 插入操作执行结果
//	 */
//	/**
//	 * 删除流程角色和人员关联关系
//	 * @param ids
//	 */
//	public void deleteWfRoleAndUser(String[] ids) {
//		deleteWfRoleInfo(ids);
//		deleteWfRoleAndUser(ids);	
//	}	
//	 
//
//	/**
//	 * 为相关人员分配流程角色
//	 * @param roleUserMapList
//	 */
//	public void distributeWFRole(List<RoleUserMap> roleUserMapList) {
//		for(RoleUserMap roleUserMap:roleUserMapList) {
//			String ID = IDGenerator.nextStringId();
//			String empID = roleUserMap.getEmpID();
//			String wfRoleID = roleUserMap.getWFRoleID();
//			Object[] params = {ID,wfRoleID,empID};
//			distributeWFRole(params);
//		}		
//	}
//	
//	public PageFinder queryAllUserAndEmpId(Map<String, String> params) {
//		return queryAllUserAndEmpId(params);
//	}
//	
//	/**
//	 * 根据父节点查询系统代码的所有下一级节点
//	 * 
//	 * @param parentId
//	 *            父节点ID
//	 * @return
//	 */
//	public List<Map<String,Object>> queryCodeForTree(String parentId) {
//		return daoWfRoleOperate.querySysCodeInfoForTree(parentId);
//	}
//
//	/**
//	 * 分页查询所有用户信息和人员编号
//	 * @return List
//	 */
//	public PageFinder queryUserByWFRoleID(Map<String,String> params) {
//		return queryUserByWFRoleID(params);
//	}
//
//	public void deleteUserAndRoleByID(String[] ids) {
//		deleteUserAndRoleByID(ids);		
//	}
//
//	@SuppressWarnings("unchecked")
//	public boolean saveAllPersonToRole(Map<String, String> params) {
//		boolean retFlag = false;
//		params.put("page", "1");
//		params.put("rows", "0");
//		String wfRoleID = params.get("srcWFRoleID");
//		if(wfRoleID == null || StringUtils.isBlank(wfRoleID) || 
//				wfRoleID.equals("") || wfRoleID.equals("nulls")){
//			throw new BizException("需要配置的流程角色信息为空，请点击左侧树形列表选择。");
//		}
//		List<Map<String,String>> list = 
//			queryAllUserAndEmpId(params);
//		String empId = "";
//		List<Object[]> args = new ArrayList<Object[]>();
//		if( list != null && list.size() > 0){
//			for (Map<String, String> map : list) {
//				if(args != null && args.size() > 100 ){
//					distributeWFRole(args);
//					args.clear();
//				}
//				empId = TextUtil.nvl(map.get("ID"));
//				if(StringUtils.isNotBlank(empId) && !empId.equals("nulls")){
//					args.add(new Object[]{IDGenerator.nextStringId(),wfRoleID,empId});
//				}
//			}
//			if(args != null && args.size() > 0){
//				distributeWFRole(args);
//				args.clear();
//			}
//			retFlag = true ;
//		}else{
//			throw new BizException("该查询条件下的查询结果为空，不操作。");
//		}
//		return retFlag;
//	}
//}
