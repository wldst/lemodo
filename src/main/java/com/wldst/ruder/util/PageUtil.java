package com.wldst.ruder.util;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wldst.ruder.crud.service.CrudNeo4jService;

/**
 *  注入Context环境下的通用工具类
  *  MKUtil
  * @author liuqiang
  * @date 2019年9月19日 下午2:24:08
  * @version V1.0
 */
@Service
public class PageUtil {
	
	@Autowired
	private CrudNeo4jService neo4jService;
	
	/**
	  * pageTotal
	  *  
	  * @author liuqiang
	  * @date 2019年9月18日 下午6:15:50
	  * @version V1.0
	  * @param page
	  * @param query
	 */
	public Integer pageTotal(String query) {
	    if (!query.toLowerCase().contains(" return ")) {
		return 0;
	    }
	    String[] split = query.toLowerCase().split(" return ");
	    String prefix = query.substring(0, split[0].length());
	    if (split.length < 2) {
		split = query.split(" RETURN ");
	    }
	    String count = " return count(*)";
	    List<Map<String, Object>> ret = neo4jService.cypher(prefix + count);
	    Integer total = Integer.valueOf(String.valueOf(ret.get(0).get("count(*)")));
	    return total;
	}

}
