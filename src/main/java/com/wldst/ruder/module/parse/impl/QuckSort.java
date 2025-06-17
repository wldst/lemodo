package com.wldst.ruder.module.parse.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.wldst.ruder.annotation.ServiceLog;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.util.QuickSort;
/**
 *  
 * 字符串大写小写转换
 * @param msg
 * @return
 */
@Component
public class QuckSort extends ParseExcuteDomain implements MsgProcess {
    
    final static Logger logger = LoggerFactory.getLogger(QuckSort.class);
    protected static List<String> qsWords = Arrays.asList("快排序", "排序", "sort", "QuckSort", "快速排序");
    
    /**
     * 查询谁的属性是什么，关系有哪些
     * 
     * @param msg
     * @return
     */
    @Override
    @ServiceLog(description = "UpperCase")
    public Object process(String msg, Map<String, Object> context) {
	msg = msg.trim().replaceAll("：", ":");
	msg = msg.trim().replaceAll("，", ",");
	// 并行概率执行
	for(String ui:qsWords) {
	    if (!bool(context, USED)&&msg.startsWith(ui)) {
		    msg=msg.replaceFirst(ui, "");
		    context.put(USED, true);
		    String[] split = msg.split(",");
		    QuickSort.quickSort(split);
			 return String.join(",",split);
		    }
	}
	 
	return null;
    }
    
      
}
