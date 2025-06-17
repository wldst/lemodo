package com.wldst.ruder.util;

import java.util.Map;

import org.neo4j.kernel.impl.core.NodeProxy;

public class NodeTool extends MapTool{
    
    public static NodeProxy node(Map<String, Object> mapData, String key) {
	Object object = mapData.get(key);
	if (object instanceof NodeProxy no) {
	    return no;
	}
	return null;
    }

}
