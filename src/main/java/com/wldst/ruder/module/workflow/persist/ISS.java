package com.wldst.ruder.module.workflow.persist;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.exceptions.DatabaseException;

import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.template.beans.Twf;

public interface ISS {

	/**
	 * 新增流程模板信息
	 * 
	 * @param dba
	 *            数据库操作对象
	 * @param tempWF
	 *            流程模板信息
	 * @return 新增操作执行结果
	 * @throws DatabaseException
	 */
	public abstract boolean insertWfTemplate(Twf tempWF)
			throws CrudBaseException;

	/**
	 * 更新流程模板信息
	 * 
	 * @param dba
	 *            数据库操作封装
	 * @param tempWF
	 *            流程模板信息
	 * @return 更新操作执行结果
	 * @throws CrudBaseException
	 */
	public abstract boolean updateWfTemplate(Twf tempWF)
			throws CrudBaseException;

	/**
	 * 检索被监控的流程模板列表,其中包括每个模板所对应的流程实例个数
	 * 
	 * @param dba
	 *            数据库操作封装对象
	 * @return 流程模板等信息列表
	 * @throws CrudBaseException
	 */
	public abstract List<Map<String,Object>> srchMonitorWfTemplateList(String srchName,
			String srchMark) throws CrudBaseException;

	/**
	 * 根据流程唯一标识得到流程模板信息
	 * 
	 * @param dba
	 *            数据库封装对象
	 * @param tempMark
	 *            流程模板唯一标识
	 * @return 流程模板信息
	 * @throws CrudBaseException
	 */
	public abstract Twf getWfTemplate(String tempMark)
			throws CrudBaseException;

	/**o
	 * 检索得到所有最新版本的工作流定义信息列表
	 * 
	 * @param dba
	 *            数据库操作对象
	 * @return 工作流定义信息列表
	 * @throws CrudBaseException
	 */
	public abstract List<Map<String,Object>> srchWfTemplateList() throws CrudBaseException;

}
