package com.wldst.ruder.domain;

import com.wldst.ruder.util.MapTool;

import java.util.*;

/**
 * 邮箱相关的常量
 * 
 * @author wldst
 *
 */
public class LLMChatDomain extends MapTool{

    public static final String ROLE = "role";
    public static final String DESCRIPTION = "description";


    public static String role(Map<String, Object> mapData) {
        return string(mapData, ROLE);
    }

    public static String description(Map<String, Object> mapData) {
        return string(mapData, DESCRIPTION);
    }

    /**
     * 获取listString
     * @param mapData
     * @param key
     * @return
     */
    public static List<String> listString(Map<String, Object> mapData,String key) {
       String str= string(mapData,key);
       if(str==null){
           return null;
       }
        str = str.substring(1, str.length() - 1); // 去掉首尾的方括号
        String[] arr = str.split("','"); // 使用逗号和单引号分割字符串
        List<String> list = new ArrayList<>();
        for (String s : arr) {
            list.add(s);
        }
        return list;
    }

    public static String arrayString(Set<String> sets) {
        return "['"+String.join("','", sets)+"']";
    }

    public static String arrayString(String[] sets) {
        return "['"+String.join("','", sets)+"']";
    }

}
