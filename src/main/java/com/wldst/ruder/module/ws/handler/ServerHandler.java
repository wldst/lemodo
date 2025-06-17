package com.wldst.ruder.module.ws.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wldst.ruder.api.Result;
import com.wldst.ruder.domain.AuthDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.CrudUtil;

/**
 * 
 * @author wldst
 *
 */
@Component
public class ServerHandler extends AuthDomain {
    @Autowired
    private CrudUtil crudUtil;
	@Autowired
	private CrudNeo4jService neo4jService;
    private static Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    public Result<Object> handleIt(Map<String, Object> cmdInfo) {
	String cmd = string(cmdInfo, CMD);
	
	if(cmd==null) {
	    return Result.success();
	}
	 Map<String, Object> meta = mapObject(cmdInfo,META_DATA);
	switch (cmd) {
	
        	case "client":
        	    Map<String, Object> data2 = data(cmdInfo);
        	    LoggerTool.info(logger," sh: client say..."+data2);
        	    neo4jService.save(data2, LABEL_CLIENT);
        	    break;
        	case "data":
        	    String dataLabel = label(cmdInfo);
        	    Map<String, Object> data = data(cmdInfo);
        	    LoggerTool.info(logger," sh: client  data:" + mapString(data));
        	    String metaLabel = label(data);
        	    if(metaLabel==null) {
        		neo4jService.save(data, dataLabel);
        	    }else {
        		neo4jService.save(data, metaLabel);
        	    }
        	    
        	    break;
        	case "meta":
        	    List<Map<String, Object>> metaList = listMapObject(cmdInfo, META_DATA);
        	    LoggerTool.info(logger,"sh: client  data:" + listMapString(metaList));
        	    neo4jService.save(metaList, META_DATA);
        	    break;
        	case CMD_REPORT:
        	    Map<String, Object> metaData = copy(metaData(cmdInfo));
        	    metaData.remove(ID);
        	    Map<String, Object> serverMeta = neo4jService.getAttMapBy(LABEL,label(metaData), META_DATA);
        	    Map<String, Object> reportData = data(cmdInfo);
        	    Boolean isSame = compareMap(serverMeta,metaData);
        	    if(!isSame&&metaData!=null) {
        		neo4jService.save(metaData, META_DATA);
        	    }
        	    neo4jService.save(reportData, label(metaData));
        	    break;
        	case "dataList":
        	    //数据格式：{labels:"li,lii",li:[],lii:[]}
        	    String dataLabels = label(meta);
        	    for (String li : dataLabels.split(",")) {
        		List<Map<String, Object>> listMapObject = listMapObject(cmdInfo, li);
        		if (listMapObject != null && !listMapObject.isEmpty()) {
        		    neo4jService.save(listMapObject, li);
        		}
        	    }
        	    break;
        	case "upDataList":
		    Map<String, Object> metaDatax = copy(metaData(cmdInfo));
		    Map<String, Object> serverMetax = neo4jService.getAttMapBy(LABEL, label(metaDatax), META_DATA);
		    Boolean isSamex = compareMap(serverMetax, metaDatax);
		    if (!isSamex && metaDatax != null) {// 更新元数据
			neo4jService.save(metaDatax, META_DATA);
		    }
		    List<Map<String, Object>> listMapObject = listMapObject(cmdInfo, DATA);
		    if (listMapObject != null && !listMapObject.isEmpty()) {
			String li = label(meta);
			neo4jService.save(listMapObject, li);
		    }
        	    break;
        	case "version":
        	    cmdInfo.put("update", cmd);
        	    LoggerTool.info(logger,"update");
        	    neo4jService.save(cmdInfo, "Update");
        	    break;
	}
	
//	String[] columns;
//	try {
//	    columns = crudUtil.getPoColumn(LABEL_CLIENT);
//	    Map<String, String> getParams = new HashMap<>();
//	    if(columns!=null) {
//		for (String ci : columns) {
//			String string = string(cmdInfo, ci);
//			if (string != null && !ID.equals(ci)) {
//			    getParams.put(ci, string);
//			}
//
//		}
//	    }
//	    
//	    if (getParams.size() < 2) {
//		return Result.failed("参数缺失！");
//	    }
//
//	} catch (DefineException e) {
//	    e.printStackTrace();
//	    return Result.failed("服务器异常！");
//	}
	return Result.success();
    }
}
