package com.wldst.ruder.module.parse.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.springframework.stereotype.Component;

import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;

/**
 * label: Cart name: 购物车 columns: isNotEmpty, storeGoods, invalidGoodItems,
 * isAllSelected, selectedGoodsCount, totalAmount, totalDiscountAmount headers:
 * 非空, 商店商品, 无效商品项, 全部选中, 选中商品数量, 总金额, 总折扣金额 添加元数据
 * 
 * @param msg
 * @param context
 */
@Component
public class MetaDataParse extends ParseExcuteDomain implements MsgProcess {

    /**
     * 解析元数据执行
     * 
     * @param msg
     * @param context
     */
    @Override
    public Object process(String msg, Map<String, Object> context) {
	if (!msg.contains("label:") || !msg.contains("name:") || !msg.contains("columns:")
		|| !msg.contains("headers:")) {
	    return null;
	}
	if (!bool(context, USED)) {
	    String s1 = msg.split("label:")[1];
	    String[] s2 = s1.split("name:");
	    String label = s2[0].trim();
	    String[] s3 = s2[1].split("columns:");
	    String name = s3[0].trim();
	    String[] s4 = s3[1].split("headers:");
	    String columns = s4[0].trim();
	    String headers = s4[1].trim();
	    if (label.length() > 0 && name.length() > 0 && columns.length() > 0 && headers.length() > 0) {
		Map<String, Object> meta = newMap();
		meta.put("label", label);
		meta.put("name", name);
		meta.put("columns", trimList(columns));
		meta.put("header", trimList(headers));
		context.put(USED, true);
		Node saveByBody = neo4jUService.saveByBody(meta, META_DATA);
		return saveByBody.getId();
	    }
	}
	return null;
    }

    public String trimList(String columns) {
	String[] columnsx = columns.split(","); 
	List<String> columnsSet = new ArrayList<>();
	 for(String key:columnsx) {
		 columnsSet.add(key.trim());
	 }
	return  String.join(",", columnsSet);
    }

}
