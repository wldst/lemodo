package com.wldst.ruder.module.fun.service;

import java.util.Map;

/**
 * 常用规则处理。后端校验规则
 * 后端查询规则进行处理。
 * @author wldst
 *
 */
public interface RuleService {
    Boolean isDuplicate(Map<String, Object> map, String key);
    Boolean isOutOfRange(Map<String, Object> map, String key);
    Boolean isDate(Map<String, Object> map, String key);
    Boolean isNull(Map<String, Object> map, String key);
    Boolean isEmail(Map<String, Object> map, String key);
    Boolean isNumber(Map<String, Object> map, String key);
    Boolean isPoneNumber(Map<String, Object> map, String key);
    Boolean isIdCard(Map<String, Object> map, String key);
    Boolean isURL(Map<String, Object> map, String key);
    Boolean validField(Map<String, Object> map);

    Map<String, Object> formateQueryField(Map<String, Object> map);
    public void formateDateField(Map<String, Object> ddMap, Object value2, String key);
    void clearFieldInfo(Map<String, Object> map);
    
}
