package com.wldst.ruder.module.manage.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.util.MapTool;
 
/**
 * 系统代码工具类
 * @author zhangjianke
 *
 */
@Service
public class SysCodeManager extends MapTool{
	/**
	 * 对象锁
	 */
	private static final Object lock = new Object();
	 
	@Autowired
	private CrudNeo4jService cruderService;
	/**
	 * 单例对象
	 */
	private static SysCodeManager instance;
	
	/**
	 * 系统代码映射集
	 */
	private Map<String, List<Map<String,Object>>> codeMap = new HashMap<>();
	
	private Map<String,String> codeNameMap = new HashMap<String, String>();
	
	/**
	 * 系统代码树
	 */
	private List<Map<String,Object>> codes;
	
	/**
	 * 单例类初始化
	 */
	public void init() {
		synchronized (lock) {
			instance = this;
			instance.codes = cruderService.listAllByLabel("SysCode");
			codeMap.clear();
		}
		
	}

	/**
	 * 根据主码获取系统代码树结构
	 * @param key 主码
	 * @return
	 */
	public static List<Map<String,Object>> getCode(String key) {
		synchronized (lock) {
			if (instance.getCodeMap().containsKey(key)) {
				return instance.getCodeMap().get(key);
			} else {
				List<Map<String,Object>> nodes = searchNodes(key, instance.getCodes());
				instance.getCodeMap().put(key, nodes);
				return nodes;
			}
		}
	}
	
	/**
	 * 根据主码获取系统代码树结构
	 * @param key 主码
	 * @return
	 */
	public static List<Map<String,Object>> getCode(String key,String minorCode) {
		synchronized (lock) {
			if(StringUtils.hasLength(minorCode)){
				if (instance.getCodeMap().containsKey(key+minorCode)) {
					return instance.getCodeMap().get(key+minorCode);
				} else {
					List<Map<String,Object>> nodes = searchNodes(key,minorCode, instance.getCodes());
					instance.getCodeMap().put(key+minorCode, nodes);
					return nodes;
				}
			}else{
				if (instance.getCodeMap().containsKey(key)) {
					return instance.getCodeMap().get(key);
				} else {
					List<Map<String,Object>> nodes = searchNodes(key, instance.getCodes());
					instance.getCodeMap().put(key, nodes);
					return nodes;
				}
			}
		}
	}
	/**
	 * 搜索系统代码
	 * @param key 主码
	 * @param nodes 代码节点集
	 * @return
	 */
	public static List<Map<String,Object>> searchNodes(String key, List<Map<String,Object>> nodes) {
		List<Map<String,Object>> reNodes = null;
		if (nodes != null && !StringUtils.hasLength(key)) {
			for (Map<String,Object> node : nodes) {
				if (key.trim().equals(string(node,"majorCode"))) {
					return listMapObject(node,"children");
				} else {
					reNodes = searchNodes(key, listMapObject(node,"children"));
					if (null != reNodes) {
						break;
					}
				}
			}
		}
		return reNodes;
	}
	
	/**
	 * 搜索系统代码
	 * @param key 主码
	 * @param nodes 代码节点集
	 * @return
	 */
	public static List<Map<String,Object>> searchNodes(String key,String minorCode, List<Map<String,Object>> nodes) {
		List<Map<String,Object>> reNodes = null;
		if (nodes != null && StringUtils.hasLength(key)) {
			for (Map<String,Object> node : nodes) {
				if (key.trim().equals(string(node,"majorCode"))&&minorCode.trim().equals(string(node,"minorCode"))) {
						return listMapObject(node,"children");
				} else {
					reNodes = searchNodes(key,minorCode, listMapObject(node,"children"));
					if (null != reNodes) {
						break;
					}
				}
			}
		}
		return reNodes;
	}
	
	/**
	 * 根据主码和次码获取系统代码的名称
	 * @param majorCode 主码
	 * @param minorCode 次码
	 * @return
	 */
	public static String getCodeName(String majorCode, String minorCode) {
		List<Map<String,Object>> nodes =  getCode(majorCode);
		Map<String,Object> node = searchNodeName(minorCode, nodes);
		return node == null ? "":name(node);
	}
	
	/**
	 * 根据主码和次码获取系统代码的名称
	 * @param majorCode 主码
	 * @param minorCode 次码
	 * @return
	 */
	public static String getCodeNameByCode(String type, String minorCode) {
		return name(instance.getNameByCode(type, minorCode));
	}
	
	/**
	 * 根据主码和次码获取系统代码的名称
	 * @param majorCode 主码
	 * @param id 次码
	 * @return
	 */
	public static Map<String,Object> getCodeById(String majorCode, String id) {
		List<Map<String,Object>> nodes =  getCode(majorCode);
		return searchNodeById(id, nodes);
	}
	
	public static int getLayout(String majorCode, String minorCode) {
		List<Map<String,Object>> nodes =  getCode(majorCode);
		return searchNodeLayout(minorCode, nodes, 0);
	}
	
	/**
	 * 根据主码和次码获取系统代码
	 * @param majorCode 主码
	 * @param minorCode 次码
	 * @return
	 */
	public static Map<String,Object> getNameByCode(String majorCode, String minorCode) {
		List<Map<String,Object>> nodes =  getCode(majorCode);
		return searchNodeName(minorCode, nodes);
	}
	/**
	 * 搜索系统代码名称
	 * @param minorCode 次码
	 * @param nodes 代码节点集
	 * @return
	 */
	private static Map<String,Object> searchNodeName(String minorCode, List<Map<String,Object>> nodes) {
		Map<String,Object> renode = null;
		if (nodes != null && StringUtils.hasLength(minorCode)) {
			for (Map<String,Object> node : nodes) {
				if (minorCode.trim().equals(string(node,"minorCode"))) {
					return node;
				} else {
					renode = searchNodeName(minorCode, listMapObject(node,"children"));
					if (null != renode) {
						break;
					}
				}
			}
		}
		return renode;
	}
	
	/**
	 * 搜索系统代码名称
	 * @param minorCode 次码
	 * @param nodes 代码节点集
	 * @return
	 */
	private static Map<String,Object> searchNodeById(String nodeId, List<Map<String,Object>> nodes) {
		Map<String,Object> renode = null;
		if (nodes != null && StringUtils.hasLength(nodeId)) {
			for (Map<String,Object> node : nodes) {
				if (nodeId.trim().equals(stringId(node))) {
					return node;
				} else {
					renode = searchNodeById(nodeId, listMapObject(node,"children"));
					if (null != renode) {
						break;
					}
				}
			}
		}
		return renode;
	}
	
	/**
	 * 搜索系统代码名称
	 * @param minorCode 次码
	 * @param nodes 代码节点集
	 * @return
	 */
	private static int searchNodeLayout(String minorCode, List<Map<String,Object>> nodes, int layout) {
		if (nodes != null && StringUtils.hasLength(minorCode)) {
			layout += 1;
			for (Map<String,Object> node : nodes) {
				if (minorCode.trim().equals(string(node,"minorCode"))) {
					return layout;
				} else {
					return searchNodeLayout(minorCode, listMapObject(node,"children"), layout);
				}
			}
		}
		return -1;
	}
	
	/**
	 * 根据主码和代码含义获取系统代码
	 * @param majorCode 主码
	 * @param codeName 代码含义
	 * @return
	 */
	public static Map<String,Object> getCodeByName(String majorCode, String codeName) {
		List<Map<String,Object>> nodes =  getCode(majorCode);
		return searchNodeByName(codeName, nodes);
	}
	
	/**
	 * 根据主码和代码含义获取系统代码次码
	 * @param majorCode 主码
	 * @param codeName 代码含义
	 * @return
	 */
	public static String getMinorCodeByName(String majorCode, String codeName) {
		Map<String,Object> code = getCodeByName(majorCode, codeName);
		if (null == code) {
			return "";
		} else {
			return string(code,"minorCode");
		}
	}
	
	/**
	 * 根据主码和代码含义获取系统代码次码
	 * @param majorCode 主码
	 * @param codeName 代码含义
	 * @return
	 */
	public static String getMinorCodeByNameAndType(String type, String codeName,String parent) {
		StringBuilder temp = new StringBuilder();
		String res = null;
		temp.append(type).append("_").append(codeName).append("_").append(parent);
		if (instance.getCodeNameMap().containsKey(temp.toString())) {
			res = instance.getCodeNameMap().get(temp.toString());
		} else {
			res = instance.getCodeByName(type, codeName,parent);
			instance.getCodeNameMap().put(temp.toString(), res);
		}
		return res;
	}
	
	private String getCodeByName(String type, String codeName, String parent) {
	    // TODO Auto-generated method stub
	    return null;
	}

	/**
	 * 根据代码名称搜索系统代码
	 * @param minorCode 次码
	 * @param nodes 代码节点集
	 * @return
	 */
	private static Map<String,Object> searchNodeByName(String codeName, List<Map<String,Object>> nodes) {
		Map<String,Object> renode = null;
		if (nodes != null && StringUtils.hasLength(codeName)) {
			String tmpNodeName = "";
			for (Map<String,Object> node : nodes) {
				if (StringUtils.hasLength(name(node))) {
					tmpNodeName = name(node).trim();
				} else {
					tmpNodeName = "";
				}
				if (codeName.trim().equals(tmpNodeName) && bool(node,"enable")) {
					return node;
				} else if (bool(node,"enable")){
					renode = searchNodeByName(codeName, listMapObject(node,"children"));
					if (null != renode) {
						break;
					}
				}
			}
		}
		return renode;
	}
	
	
	/**
	 * 重新刷新缓存
	 */
	public void clearCache() {
		synchronized (lock) {
			instance.codeMap.clear();
			instance.codeNameMap.clear();
			instance.codes = cruderService.listAllByLabel("SysCode");
		}
	}
 

	/**
	 * @return the codeMap
	 */
	public Map<String, List<Map<String,Object>>> getCodeMap() {
		return codeMap;
	}

	/**
	 * @param codeMap the codeMap to set
	 */
	public void setCodeMap(Map<String, List<Map<String,Object>>> codeMap) {
		this.codeMap = codeMap;
	}

	/**
	 * @return the codes
	 */
	public List<Map<String,Object>> getCodes() {
		return codes;
	}

	/**
	 * @param codes the codes to set
	 */
	public void setCodes(List<Map<String,Object>> codes) {
		this.codes = codes;
	}

	public Map<String, String> getCodeNameMap() {
		return codeNameMap;
	}

	public void setCodeNameMap(Map<String, String> codeNameMap) {
		this.codeNameMap = codeNameMap;
	}
}
