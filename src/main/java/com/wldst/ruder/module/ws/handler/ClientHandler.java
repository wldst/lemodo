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
public class ClientHandler extends AuthDomain {
    @Autowired
    private CrudUtil crudUtil;
	@Autowired
	private CrudNeo4jService neo4jService;
    private static Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    public Result<Object> handleIt(Map<String, Object> cmdInfo) {
	String cmd = string(cmdInfo, CMD);
	Map<String, Object> data2 = data(cmdInfo);
	switch (cmd) {
	case "saveOwner":
	    LoggerTool.info(logger,"saveOwner");
	    neo4jService.save(data2, OWNER);
	    break;
	case "forbid":
	    LoggerTool.info(logger,"serverSay forbid");
	    neo4jService.save(data2, OWNER);
	    break;
	case "client":
	    LoggerTool.info(logger,"client say..."+data2);
	    neo4jService.save(data2, LABEL_CLIENT);
	    break;
	case CMD_NOTICE:
	    Map<String, Object> notice = mapObject(cmdInfo, LABEL_NOTICE);
	    LoggerTool.info(logger,"serverSay notice");
	    neo4jService.save(notice, LABEL_NOTICE);
	    break;
	case "data":
	    String dataLabel = label(cmdInfo);
	    Map<String, Object> mapObject = mapObject(cmdInfo, dataLabel);
	    LoggerTool.info(logger,"serverSay data:" + mapString(mapObject));
	    neo4jService.save(mapObject, dataLabel);
	    break;
	case CMD_PUSH:
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
	case "meta":
	    List<Map<String, Object>> metaList = listMapObject(cmdInfo, META_DATA);
	    LoggerTool.info(logger,"serverSay data:" + listMapString(metaList));
	    neo4jService.save(metaList, META_DATA);
	    break;
	case "dataList":
	    String dataLabels = label(cmdInfo);
	    for (String li : dataLabels.split(",")) {
		List<Map<String, Object>> listMapObject = listMapObject(cmdInfo, li);
		if (listMapObject != null && !listMapObject.isEmpty()) {
		    neo4jService.save(listMapObject, li);
		}
	    }
	    break;
	case "upgrade":
	    cmdInfo.put("update", cmd);
	    LoggerTool.info(logger,"update");
	    neo4jService.save(cmdInfo, "Update");
	    break;
	}
	
	String[] columns;
	try {
	    columns = crudUtil.getMdColumns(LABEL_CLIENT);
	    Map<String, String> getParams = new HashMap<>();
	    if(columns!=null) {
		for (String ci : columns) {
			String string = string(cmdInfo, ci);
			if (string != null && !ID.equals(ci)) {
			    getParams.put(ci, string);
			}
		}
	    }
	    
	    if (getParams.size() < 2) {
		return Result.failed("参数缺失！");
	    }

	} catch (DefineException e) {
	    e.printStackTrace();
	    return Result.failed("服务器异常！");
	}
	return Result.success();
    }
}
