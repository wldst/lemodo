package com.wldst.ruder.module.fun.service;

import com.wldst.ruder.config.SpringContextUtil;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;

import java.util.List;
import java.util.Map;

import static com.wldst.ruder.constant.CruderConstant.META_DATA;
import static com.wldst.ruder.util.MapTool.*;

/**
 * 给xx 添加ss的什么权限,默认是权限， 带有元数据的，开始节点，结束节点。另做处理。
 * @author wldst
 *
 */
public class MetaDataService{


	public static String addField(String metaName, String name, String column){
		CrudUserNeo4jService neo4jUService=(CrudUserNeo4jService) SpringContextUtil.getBean(CrudUserNeo4jService.class);
		List<Map<String, Object>> metaDataBy=neo4jUService.getMetaDataBy(metaName);
		if(metaDataBy==null||metaDataBy.isEmpty()){
			return metaName+"的元数据不存在";
		}

		Map<String, Object> metaDataBy0=metaDataBy.get(0);
		columns(metaDataBy0);
		Map<String, String> nameColumn=nameColumn(metaDataBy0);
		if(nameColumn.containsKey(name)&&nameColumn.get(name).equals(column)){
			return metaName+"的字段"+name+"("+column+")"+"已经存在";
		}
		String header=header(metaDataBy0);
		String columns=columnsString(metaDataBy0);
		String[] his = header.split(",");
		String[] cols = columns.split(",");

		for(int i=0;i<his.length;i++){
			if(his[i].equals(name)){
				return metaName+"的字段"+name+"已经存在";
			}
		}

		if(his.length>cols.length){
			for(int i=cols.length;i<his.length;i++){
				his[i]="";
			}
			header=String.join(",",his);
		}
		if(his.length<cols.length){
			for(int i=his.length;i<cols.length;i++){
				cols[i]="";
			}
			columns=String.join(",",cols);
		}
		metaDataBy0.put(HEADER, header+","+name);
		metaDataBy0.put(COLUMNS, columns+","+column);
		neo4jUService.saveByBody(metaDataBy0, META_DATA);
		return "添加成功";
	}

	public static String delField(String metaName, String name, String column){
		CrudUserNeo4jService neo4jUService=(CrudUserNeo4jService) SpringContextUtil.getBean(CrudUserNeo4jService.class);
		List<Map<String, Object>> metaDataBy=neo4jUService.getMetaDataBy(metaName);
		if(metaDataBy==null||metaDataBy.isEmpty()){
			return metaName+"的元数据不存在";
		}

		Map<String, Object> metaDataBy0=metaDataBy.get(0);
		Map<String, String> nameColumn=nameColumn(metaDataBy0);
		if(!nameColumn.containsKey(name)||!nameColumn.get(name).equals(column)){
			return metaName+"的字段"+name+"("+column+")"+"不存在";
		}
		String header=header(metaDataBy0);
		String columns=columnsString(metaDataBy0);
		String[] his = header.split(",");
		String[] cols = columns.split(",");

		for(int i=0;i<his.length;i++){
			if(his[i].equals(name)){
				his[i]="";
			}
		}

		for(int i=0;i<cols.length;i++){
			if(cols[i].equals(column)){
				cols[i]="";
			}
		}

		if(his.length>cols.length){
			for(int i=cols.length;i<his.length;i++){
				his[i]="";
			}
			header=String.join(",",his);
		}
		if(his.length<cols.length){
			for(int i=his.length;i<cols.length;i++){
				cols[i]="";
			}
			columns=String.join(",",cols);
		}
		if(his.length==cols.length){
			header=String.join(",",his);
			columns=String.join(",",cols);
		}
		if(header.endsWith(",")){
			header= header.substring(0, header.length()-1);
		}
		if(columns.endsWith(",")){
			columns= columns.substring(0, columns.length()-1);
		}
		metaDataBy0.put(HEADER, header);
		metaDataBy0.put(COLUMNS, columns);

		neo4jUService.saveByBody(metaDataBy0, META_DATA);
		return "删除成功";
	}


}
