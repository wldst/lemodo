package com.wldst.ruder.module.database.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudUserNeo4jService;
import com.wldst.ruder.domain.DataBaseDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.database.DbInfoService;
import com.wldst.ruder.module.database.util.DBOptUtil;

/**
 * po管理，页面控制器 Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/oracle/data")
public class DataController extends DataBaseDomain {
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private DbInfoService dbInfoGather;
    @Autowired
    private CrudUserNeo4jService neo4jService;
    final static Logger logger = LoggerFactory.getLogger(DataController.class);
    
    
//    @ResponseBody
//    @RequestMapping(value = "/{funId}/clobContent", method = { RequestMethod.POST,
//	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
//    public WrappedResult fileContentUpload(@PathVariable("funId") String funId) throws DefineException {
//	List<Map<String, Object>> dataBy = neo4jService.getDataBy(funId);
//	Map<String, Object> mapi = dataBy.get(0);
//	String table = string(mapi,"table");
//	
//	String contentCols = string(mapi,"contentCol");
//	
//	// "D:\\liuqiang\\pmis\\work\\2023年各部门安全目标责任书模板html"
//	String dir = string(mapi,"dir");
//	String[] columns = crudUtil.getTableColumn(table);
//	
//	
//	
//	if (columns == null || columns.length <= 0) {
//	    return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
//	}
////	Map<String, Object> tableMap = neo4jService.getAttMapBy(TABEL_NAME, table, LABLE_TABLE);
//	
//	HashMap<String, Object> mpMap2=new HashMap<>();
//	mpMap2.put("YEAR", DateUtil.getCurrentYear());
//	String query= DBOptUtil.getAllObject(mpMap2, table, columns, null).toString();
//	List<Map<String, Object>> dataList;
//	try {
//	    dataList = dbInfoGather.cypher(query);
//	    if (dataList != null) {		
//		for (Map<String, Object> dui : dataList) {
//		    String orgName = string(dui, "ORGNAME");
//		    String postName = string(dui, "POSTNAME");
//		    String ID = string(dui, "ID");
//		    
//		    File dirWordHtml = dirWordHtml(dir,orgName,postName);
//		    if(dirWordHtml!=null) {
//			String readFile = FileOpt.readFile(dirWordHtml); 
//			String preparSql = "SELECT "+contentCols+" FROM "+table+" where id="+ID+" for update";
//			LoggerTool.info(logger,"===============update sql={}",preparSql);
//			Boolean prepareExcute = dbInfoGather.prepareExcuteClob(preparSql, readFile);
//			LoggerTool.info(logger,"===============update result {}",prepareExcute);
//		    }		    
//		}
//		return ResultWrapper.wrapResult(true, "", null, QUERY_SUCCESS);
//	    }
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    String message = e.getMessage();
//	    ResultWrapper.wrapResult(false, null, null, message);
//	}
//	return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
//
//    }


    
    /**
     * 更新Clob字段内容，OK2023年10月7日20:48:45
     * @param funId
     * @return
     * @throws DefineException
     */
    @ResponseBody
    @RequestMapping(value = "/{funId}/updateAll", method = { RequestMethod.POST,
	    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    public WrappedResult updateAll(@PathVariable("funId") String funId) throws DefineException {
//	List<Map<String, Object>> dataBy = neo4jService.getDataBy(funId);
	List<Map<String, Object>> dataBy = neo4jService.getDataBy("TableClob",funId);
	Map<String, Object> mapi = dataBy.get(0);
	String table = string(mapi, "table");
	
	
	String[] columns = crudUtil.getTableColumn(table);
	if (columns == null || columns.length <= 0) {
	    return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
	}
	Map<String, Object> tableMap = neo4jService.getAttMapBy(TABEL_NAME, table, LABLE_TABLE);
	
	HashMap<String, Object> mpMap2=new HashMap<>();
	mpMap2.put("YEAR", DateUtil.getCurrentYear());
	String query= DBOptUtil.getAllObject(mpMap2, table, columns, null).toString();
	List<Map<String, Object>> dataList;
	try {
	    dataList = dbInfoGather.query(query);
	    if (dataList != null) {		
		for (Map<String, Object> dui : dataList) {
		    String orgName = string(dui, "ORGNAME");
		    String postName = string(dui, "POSTNAME");
		    String ID = string(dui, "ID");
		    File dirWordHtml = dirWordHtml(string(mapi, "dir"),orgName,postName);
		    if(dirWordHtml!=null) {
			String readFile = FileOpt.readFile(dirWordHtml);
			Map<String, Object> updateColumn = new HashMap<>();
			updateColumn.put("SAFEDUTY", readFile);
			
			Map<String, Object> condition = new HashMap<>();
			condition.put("year", DateUtil.getCurrentYear());
			condition.put("ID", ID);
			
			StringBuilder sb = new StringBuilder();
			DBOptUtil.updateBy(updateColumn,condition, table, tableMap, sb);
			List<String> paramKey = new ArrayList<>();
			paramKey.add("SAFEDUTY");
			LoggerTool.info(logger,"\n==ID={}=======update sql:{},\n====keys:\n{}",ID,sb.toString(),String.join(",", paramKey));
			Boolean prepareExcute = dbInfoGather.prepareExcute(sb.toString(), updateColumn, paramKey, columTypeMap(tableMap));
			LoggerTool.info(logger,"\n===============update result {} :sql={}",prepareExcute,sb.toString());
			
		    }		    
		}
		 
		return ResultWrapper.wrapResult(true, "", null, QUERY_SUCCESS);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    String message = e.getMessage();

	    ResultWrapper.wrapResult(false, null, null, message);
	}
	return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);

    }
    
// 通过funId更新特定功能的模板信息
@ResponseBody
@RequestMapping(value = "/{funId}/updateCol", method = { RequestMethod.POST,
    RequestMethod.GET }, produces = "application/json;charset=UTF-8")
public WrappedResult updateTpl(@PathVariable("funId") String funId) throws DefineException {
    // 根据功能ID获取数据
    //List<Map<String, Object>> dataBy = neo4jService.getDataBy(funId);
    // 优化：从特定表中获取数据
    List<Map<String, Object>> dataBy = neo4jService.getDataBy("TableClob", funId);
    // 获取查询结果的第一个记录
    Map<String, Object> mapi = dataBy.get(0);
    // 提取所需的表名、内容列名和文件类型
    String table = string(mapi, "table");
    String contentCol = string(mapi, "contentCol");
    String fileType = string(mapi, "fileType");

    // 获取表的列信息
    String[] columns = crudUtil.getTableColumn(table);
    // 如果列信息为空，返回查询失败的结果
    if (columns == null || columns.length <= 0) {
        return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
    }
    // 获取表的属性映射
    Map<String, Object> tableMap = neo4jService.getAttMapBy(TABEL_NAME, table, LABLE_TABLE);

    // 构建查询条件
    HashMap<String, Object> mpMap2 = new HashMap<>();
    mpMap2.put("YEAR", DateUtil.getCurrentYear());
    String query = DBOptUtil.getAllObject(mpMap2, table, columns, null).toString();
    List<Map<String, Object>> dataList;
    try {
        // 执行查询
        dataList = dbInfoGather.query(query);
        if (dataList != null) {
            // 遍历查询结果
            for (Map<String, Object> dui : dataList) {
                String orgName = string(dui, "ORGNAME");
                String postName = string(dui, "POSTNAME");
                String ID = string(dui, "ID");

                // 生成并检查HTML目录文件
                File dirWordHtml = dirFile(string(mapi, "dir"), orgName, postName, fileType);
                if (dirWordHtml != null) {
                    // 读取文件内容并准备更新操作
                    String readFile = FileOpt.readFile(dirWordHtml);
                    Map<String, Object> updateColumn = new HashMap<>();
                    updateColumn.put(contentCol, readFile);

                    // 设置更新条件
                    Map<String, Object> condition = new HashMap<>();
                    condition.put("year", DateUtil.getCurrentYear());
                    condition.put("ID", ID);
                    StringBuilder sb = new StringBuilder();
                    // 构建更新SQL语句
                    DBOptUtil.updateBy(updateColumn, condition, table, tableMap, sb);
                    List<String> paramKey = new ArrayList<>();
                    paramKey.add(contentCol);
                    // 记录日志
                    LoggerTool.info(logger, "\n==ID={}=======update sql:{},\n====keys:\n{}", ID, sb.toString(), String.join(",", paramKey));
                    // 执行更新操作
                    Boolean prepareExcute = dbInfoGather.prepareExcute(sb.toString(), updateColumn, paramKey, columTypeMap(tableMap));
                    // 记录更新结果
                    LoggerTool.info(logger, "\n===============update result {} :sql={}", prepareExcute, sb.toString());
                }
            }
            // 返回更新成功的消息
            return ResultWrapper.wrapResult(true, "", null, QUERY_SUCCESS);
        }
    } catch (Exception e) {
        e.printStackTrace();
        String message = e.getMessage();
        // 返回更新失败的消息
        ResultWrapper.wrapResult(false, null, null, message);
    }
    // 默认返回查询失败的消息
    return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
}

    
    private File dirWordHtml(String filepath,String orgName,String postName) {
	String fileName;
	File dir = new File(filepath);

	for (File fi : dir.listFiles()) {
	    if (fi.isDirectory()) {
		File dirWordHtml = dirWordHtml(fi.getAbsolutePath(),orgName,postName);
		if(dirWordHtml!=null) {
		    return dirWordHtml;
		}
	    } else {
		fileName = fi.getName();
		 if(fileName.endsWith("html")&&fileName.indexOf(postName)>-1&&(fileName.indexOf(orgName)>-1||fi.getPath().indexOf(orgName)>-1)) {
		     LoggerTool.info(logger,"==read file="+orgName+"="+postName+"=====找到文件====="+fileName);
		     return fi;
		 }
	    }
	}
	return null;
    }
    
    private File dirFile(String filepath,String orgName,String postName,String fileType) {
	String fileName;
	File dir = new File(filepath);

	for (File fi : dir.listFiles()) {
	    if (fi.isDirectory()) {
		File dirWordHtml = dirFile(fi.getAbsolutePath(),orgName,postName,fileType);
		if(dirWordHtml!=null) {
		    return dirWordHtml;
		}
	    } else {
		fileName = fi.getName();
		 if(fileName.endsWith(fileType)&&fileName.indexOf(postName)>-1&&(fileName.indexOf(orgName)>-1||fi.getPath().indexOf(orgName)>-1)) {
		     LoggerTool.info(logger,"==read file="+orgName+"="+postName+"=====找到文件====="+fileName);
		     return fi;
		 }
	    }
	}
	return null;
    }

    private Map<String, String> columTypeMap(Map<String, Object> tableMap) {
	String[] keySet = string(tableMap, COLUMNS).split(",");
	String[] types = splitValue(tableMap, COLUMN_TYPE, TYPE_SPLITER);
	Map<String, String> typeMap = toMap(keySet, types);
	return typeMap;
    }

      

     
}
