package com.wldst.ruder.crud.service;

import static com.wldst.ruder.constant.CruderConstant.END_LABEL;
import static com.wldst.ruder.constant.CruderConstant.MODULE;
import static com.wldst.ruder.constant.CruderConstant.NODE_CODE;
import static com.wldst.ruder.constant.CruderConstant.NODE_ID;
import static com.wldst.ruder.constant.CruderConstant.NODE_LABEL;
import static com.wldst.ruder.constant.CruderConstant.NODE_NAME;
import static com.wldst.ruder.constant.CruderConstant.META_DATA;
import static com.wldst.ruder.constant.CruderConstant.RELATION_ENDNODE_LABEL;
import static com.wldst.ruder.constant.CruderConstant.RELATION_ENDNODE_PROP;
import static com.wldst.ruder.constant.CruderConstant.RELATION_LABEL;
import static com.wldst.ruder.constant.CruderConstant.RELATION_PROP;
import static com.wldst.ruder.constant.CruderConstant.RELATION_TYPE;
import static com.wldst.ruder.constant.CruderConstant.START_LABEL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.util.CrudUtil;

import scala.annotation.meta.field;

/**
 * 选项卡服务
 * 
 * @author wldst
 *
 */

@Service
public class ViewService {
	@Autowired
	private CrudUtil crudUtil;

	/**
	 * 获取字段多选列表
	 * @param endLabel
	 * @param eLabel
	 * @param endiPo
	 * @param fieldForm
	 */
	public String getFieldCheckList(String eLabel, Map<String, Object> endiPo) {
		StringBuilder fieldForm = new StringBuilder();
		if (endiPo.containsKey("header")) {
			String retColumns = String.valueOf(endiPo.get("columns"));
			String header = String.valueOf(endiPo.get("header"));
			String[] columnArray = crudUtil.getColumns(retColumns);
			String[] headers = crudUtil.getColumns(header);
			StringBuilder sbbBuilder = new StringBuilder();
			for (int i = 0; i < headers.length; i++) {
				sbbBuilder.append(fieldForm(eLabel,columnArray[i], headers[i]));
			}
			
			fieldForm.append(sbbBuilder.toString());
		}
		return fieldForm.toString();
	}
	
	public String fieldForm(String label,String code, String name) {
		StringBuilder sb = new StringBuilder();
		sb.append("<input type=\"checkbox\" name=\""+label+"-_-"+code+"\" title=\""+name+"\" />");
		return sb.toString();
	}
}
