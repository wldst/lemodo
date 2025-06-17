package com.wldst.ruder.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.constant.CruderConstant;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;


public class MapTool extends CruderConstant{

    public static Long id(Map<String, Object> mapData){
        try{
            return longValue(mapData, ID);
        }catch(NumberFormatException e){
            return null;
        }
    }

    public static Long endId(Map<String, Object> mapData){
        try{
            return longValue(mapData, RELATION_END_ID);
        }catch(NumberFormatException e){
            return null;
        }
    }

    public static Long startId(Map<String, Object> mapData){
        try{
            return longValue(mapData, RELATION_START_ID);
        }catch(NumberFormatException e){
            return null;
        }
    }

    public static String url(Map<String, Object> mapData){
        return string(mapData, "url");
    }

    public HashMap<String, Object> result(String msg){
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("msg", msg);
        hashMap.put("status", 200);
        return hashMap;
    }

    public HashMap<String, Object> success(String msg, Object data){
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("msg", msg);
        hashMap.put("data", data);
        hashMap.put("status", 200);
        return hashMap;
    }

    public HashMap<String, Object> failed(String msg){
        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("msg", msg);
        hashMap.put("status", 500);
        return hashMap;
    }

    public static Long parentId(Map<String, Object> mapData){
        try{
            return longValue(mapData, PARENT_ID);
        }catch(NumberFormatException e){
            return null;
        }
    }

    public static Long parentId(Map<String, Object> mapData, String key){
        try{
            return longValue(mapData, key);
        }catch(NumberFormatException e){
            return null;
        }
    }

    public static String label(Node newData){
        Iterable<Label> labels=newData.getLabels();
        StringBuilder sb=new StringBuilder();
        for(Label li : labels){
            if(sb.length()>0){
                sb.append(",");
            }
            sb.append(li.name());
        }
        return sb.toString();
    }

    public static String stringId(Map<String, Object> mapData){
        return string(mapData, ID);
    }

    public static String stringParentId(Map<String, Object> mapData){
        return string(mapData, PARENT_ID);
    }

    public static String label(Map<String, Object> mapData){
        return string(mapData, LABEL);
    }

    public static String searchVal(Map<String, Object> mapData){
        return string(mapData, SEARCH_VAL);
    }

    public static String keyWord(Map<String, Object> mapData){

        String queryText=string(mapData, KEY_WORD);
        if(queryText==null){
            return null;
        }
        queryText=queryText.trim().toLowerCase().replaceAll("and", "");
        queryText=queryText.replaceAll("or", "");
        queryText=queryText.replaceAll("'", "");
        queryText=queryText.replaceAll("\"", "");
        queryText=queryText.replaceAll("where", "");
        queryText=queryText.replaceAll("set", "");
        queryText=queryText.replaceAll("update", "");
        queryText=queryText.replaceAll(" ", "");
        queryText=queryText.replaceAll("=", "");
        queryText=queryText.replaceAll("<", "");
        queryText=queryText.replaceAll(">", "");
        queryText=queryText.replaceAll("$", "");
        queryText=queryText.replaceAll("%", "");
        queryText=queryText.replaceAll("#", "");
        if(queryText.equals(string(mapData, KEY_WORD).trim().toLowerCase())){
            return string(mapData, KEY_WORD);
        }
        return queryText;
    }

    public static String startLabel(Map<String, Object> mapData){
        return string(mapData, START_LABEL);
    }

    public static String endLabel(Map<String, Object> mapData){
        return string(mapData, END_LABEL);
    }

    public static String relLabel(Map<String, Object> mapData){
        return string(mapData, RELATION_LABEL);
    }

    public static String type(Map<String, Object> mapData){
        return string(mapData, TYPE);
    }

    public static Date date(Map<String, Object> mapData, String key){
        Calendar instance=Calendar.getInstance();
        instance.setTimeInMillis(longValue(mapData, key));
        return instance.getTime();
    }

    public static String dateStr(Map<String, Object> mapData, String key){
        return DateUtil.dateToStrLong(date(mapData, key));
    }

    public static String createTime(Map<String, Object> mapData){
        return dateStr(mapData, CREATETIME);
    }

    public static String updateTime(Map<String, Object> mapData){
        return dateStr(mapData, UPDATETIME);
    }

    /**
     * 将long类型的值转换为日期时间格式YEAR-MM-DD HH24:mi:ss
     *
     * @param mapData
     * @param key
     */
    public static void toDateStrValue(Map<String, Object> mapData, String key){
        mapData.put(key, dateStr(mapData, key));
    }

    public static Date date(Long key){
        Calendar instance=Calendar.getInstance();
        instance.setTimeInMillis(key);
        return instance.getTime();
    }

    public static Map<String, Object> data(Map<String, Object> mapData){
        return mapObject(mapData, DATA);
    }

    public static Set<Long> longSet(Map<String, Object> mapData, String key){
        Object object=mapData.get(key);
        if(object!=null){
            Set<Long> mdSet=(Set<Long>) object;
            return mdSet;
        }
        return null;
    }

    public static Set<String> stringsSet(Map<String, Object> mapData, String key){
        Object object=mapData.get("mdIdSet");
        if(object!=null){
            Set<String> mdSet=(Set<String>) object;
            return mdSet;
        }
        return null;
    }


    public static List<Map<String, Object>> dataList(Map<String, Object> mapData){
        return listObjectMap(mapData, DATA);
    }

    public static List<Map<String, Object>> listStr2Object(List<Map<String, String>> deptRegion){
        List<Map<String, Object>> deptRegion2=new ArrayList<Map<String, Object>>(deptRegion.size());
        for(Map<String, String> mi : deptRegion){
            Map<String, Object> di=new HashMap<String, Object>();
            for(String ki : mi.keySet()){
                di.put(ki, di.get(ki));
            }
            deptRegion2.add(di);
        }
        return deptRegion2;
    }


    public static Map<String, Object> metaData(Map<String, Object> mapData){
        return mapObject(mapData, META_DATA);
    }

    public static String dateStr(Long key){
        return DateUtil.dateToStrLong(date(key));
    }

    public static String now(){
        return dateStr(Calendar.getInstance().getTimeInMillis());
    }

    public static Integer version(Map<String, Object> mapData){
        return integer(mapData, VERSION_DATA);
    }

    public static Map<String, Object> newMap(){
        return new HashMap<String, Object>();
    }

    public static Map<String, Object> newHashMap(){
        return new HashMap<String, Object>();
    }


    public static List<Long> ids(List<Map<String, Object>> listMap){
        List<Long> ids=new ArrayList<>(listMap.size());
        for(Map<String, Object> mi : listMap){
            ids.add(id(mi));
        }
        return ids;
    }

    public static String joinObject(List<Object> ids){
        if(ids==null){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        for(Object idi : ids){
            if(sb.length()>0){
                sb.append(",");
            }
            sb.append(String.valueOf(idi));
        }
        return sb.toString();
    }

    public static String joinStr(List<Object> ids){
        if(ids==null){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        for(Object idi : ids){
            if(sb.length()>0){
                sb.append(",");
            }
            sb.append("\""+String.valueOf(idi)+"\"");
        }
        return "["+sb.toString()+"]";
    }

    public static String joinLong(List<Long> ids){
        if(ids==null){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        for(Long idi : ids){
            if(sb.length()>0){
                sb.append(",");
            }
            sb.append(String.valueOf(idi));
        }
        return sb.toString();
    }


    public static List<String> idStrList(List<Map<String, Object>> listMap){
        List<String> ids=new ArrayList<>(listMap.size());
        for(Map<String, Object> mi : listMap){
            ids.add(string(mi, ID));
        }
        return ids;
    }

    public static String code(Map<String, Object> mapData){
        return string(mapData, CODE);
    }

    public static String name(Map<String, Object> mapData){
        return string(mapData, NAME);
    }

    public static String value(Map<String, Object> mapData){
        return string(mapData, VALUE);
    }

    public static void putKv(Map<String, Object> mapData, String key, Object value){
        if(mapData==null){
            return;
        }

        if(key.contains(".")){
            String[] split=key.split("\\.");
            if(split.length>1){
                Map<String, Object> tempMap=null;
                for(int i=0; i<split.length; i++){
                    String si=split[i];
                    if(tempMap!=null){
                        if(i<split.length-1){
                            tempMap=mapObject(tempMap, si);
                        }else{
                            tempMap.put(si, value);
                        }
                    }else{
                        tempMap=mapObject(mapData, si);
                    }
                }
            }

        }else{
            mapData.put(key, value);
        }
    }

    public static String oneLabel(Map<String, Object> mapData, String key){
        String string=string(mapData, key);
        string=string.replaceAll("\\[", "");
        string=string.replaceAll("\\]", "");
        return string;
    }

    public static String names(List<Map<String, Object>> workAgent){
        StringBuilder agentNames=new StringBuilder();
        for(Map<String, Object> wi : workAgent){
            if(agentNames.isEmpty()){
                agentNames.append(",");
            }
            agentNames.append(name(wi));
        }
        return agentNames.toString();
    }

    public static String valuesOfKey(List<Map<String, Object>> workAgent, String key){
        StringBuilder values=new StringBuilder();
        for(Map<String, Object> wi : workAgent){
            if(values.isEmpty()){
                values.append(",");
            }
            values.append(string(wi, key));
        }
        return values.toString();
    }

    public static String string(Map<String, Object> mapData, String key){
        if(mapData==null||key==null){
            return null;
        }

        if(key.contains(".")){
            String[] split=key.split("\\.");
            if(split.length>1){
                Map<String, Object> tempMap=null;
                String pathValue=null;
                for(int i=0; i<split.length; i++){
                    String si=split[i];
                    if(tempMap!=null){
                        if(i<split.length-1){
                            tempMap=mapObject(tempMap, si);
                        }else{
                            pathValue=string(tempMap, si);
                        }
                    }else{
                        tempMap=mapObject(mapData, si);
                    }
                }
                return pathValue;
            }

        }
        Object obj=mapData.get(key);
        if(obj==null){
            return null;
        }
        return String.valueOf(obj);
    }

    public static String string0(Map<String, Object> mapData, String key){
        if(mapData==null||key==null){
            return null;
        }

        Object obj=mapData.get(key);
        if(obj==null){
            return null;
        }
        return String.valueOf(obj);
    }

    public static String lowCaseStr(Map<String, Object> mapData, String key){
        String string=string(mapData, key);
        if(string==null||"".equals(string.trim())){
            return null;
        }
        return string.toLowerCase();
    }

    public static String upperCaseStr(Map<String, Object> mapData, String key){
        String string=string(mapData, key);
        if(string==null||"".equals(string.trim())){
            return null;
        }
        return string.toUpperCase();
    }

    public static Boolean bool(Map<String, Object> mapData, String key){
        String string=string(mapData, key);
        if(string==null||"".equals(string.trim())){
            return false;
        }
        return Boolean.valueOf(string);
    }

    public static boolean is(Map<String, Object> myContext, String string){

        return bool(myContext, string);
    }
    public static boolean on(Map<String, Object> mapData,String key){
        String settingBy=string(mapData,key);
        boolean equals="on".equals(settingBy)||"true".equals(settingBy)||"1".equals(settingBy);
        return equals;
    }
    public static boolean off(Map<String, Object> mapData,String key){
        return !on(mapData,key);
    }

    public static String stringIgnoreCase(Map<String, Object> mapData, String key){
        if(mapData==null){
            return null;
        }
        Object obj=mapData.get(key);
        if(obj==null){
            for(Entry<String, Object> ei : mapData.entrySet()){
                if(ei.getKey().equalsIgnoreCase(key)){
                    return String.valueOf(ei.getValue());
                }
            }
            return null;
        }
        return String.valueOf(obj);
    }

    public static Set<String> stringSet(List<Map<String, Object>> mapData, String key){
        Set<String> idSet=new HashSet<>();
        for(Map<String, Object> mi : mapData){
            String obj=string(mi, key);
            if(obj==null){
                continue;
            }
            idSet.add(obj);
        }

        return idSet;
    }

    public static Map<String, Object> copy(Map<String, Object> mapData){
        if(mapData!=null&&!mapData.isEmpty()){
            Map<String, Object> mapObject=new HashMap<>();
            mapObject.putAll(mapData);
            return mapObject;
        }
        return null;
    }

    public static <T> Set<T> copySet(Set<T> set){
        if(set!=null){
            Set<T> mapObject=new HashSet<T>();
            mapObject.addAll(set);
            return mapObject;
        }
        return null;
    }

    public static List<Map<String, Object>> copyList(List<Map<String, Object>> mapList){
        List<Map<String, Object>> dataList=new ArrayList<>(mapList.size());
        for(Map<String, Object> mapData : mapList){
            if(mapData!=null&&!mapData.isEmpty()){
                Map<String, Object> mapObject=copy(mapData);
                dataList.add(mapObject);
            }
        }

        return dataList;
    }

    public static Boolean isSameList(List<Map<String, Object>> mapList){
        List<Map<String, Object>> dataList=copyList(mapList);
        if(dataList.size()<1){
            return false;
        }
        Map<String, Object> mapData=dataList.get(0);
        clearModifyInfo(mapData);
        for(int i=1; i<dataList.size(); i++){
            Map<String, Object> di=dataList.get(i);
            clearModifyInfo(di);
            if(mapData!=null&&!mapData.isEmpty()&&mapData.size()==di.size()){
                for(String ki : mapData.keySet()){
                    Object object=mapData.get(ki);
                    Object obj=di.get(ki);
                    if(object!=null&&obj!=null&&!object.equals(obj)||object==null&&obj!=null||object!=null&&obj==null){
                        return false;
                    }
                    if(object==null&&obj==null){
                        continue;
                    }
                }
            }else{
                return false;
            }
        }

        return true;
    }

    public static void clearModifyInfo(Map<String, Object> mapData){
        mapData.remove(ID);
        mapData.remove(CREATETIME);
        mapData.remove(UPDATETIME);
        mapData.remove(UPDATOR);
        mapData.remove(CREATOR);
        mapData.remove(VERSION_DATA);
    }

    public static Map<String, Object> copyWithKeys(Map<String, Object> mapData, String keys){
        if(mapData!=null&&!mapData.isEmpty()){
            Map<String, Object> mapObject=new HashMap<>();
            if(keys!=null&&keys.length()>1){
                for(String ki : keys.split(",")){
                    mapObject.put(ki, mapData.get(ki));
                }
            }else{
                mapObject.putAll(mapData);
            }

            return mapObject;
        }
        return null;
    }

    public static Map<String, Object> copyWithoutKeys(Map<String, Object> mapData, String keys){
        if(mapData!=null&&!mapData.isEmpty()){
            Map<String, Object> mapObject=new HashMap<>();

            if(keys!=null&&keys.length()>1){
                Set<String> ks=new HashSet<>();
                for(String ki : keys.split(",")){
                    ks.add(ki);
                }
                for(String ki : mapData.keySet()){
                    if(!ks.contains(ki)){
                        mapObject.put(ki, mapData.get(ki));
                    }
                }
            }else{
                mapObject.putAll(mapData);
            }
            return mapObject;
        }
        return null;
    }

    public static Map<String, Object> copyWithKeys(Map<String, Object> mapData, String[] keys){
        if(mapData!=null&&!mapData.isEmpty()){
            Map<String, Object> mapObject=new HashMap<>();
            if(keys!=null&&keys.length>1){
                Map<String, Object> cMap=new HashMap<>();
                for(Entry<String, Object> mi : mapData.entrySet()){
                    String key2=mi.getKey();
                    if(key2.contains("\"")||key2.contains("'")){
                        String clearKey=key2.replaceAll("\"", "").replaceAll("'", "");
                        Object value2=mi.getValue();
                        cMap.put(clearKey, value2);
                    }
                }
                for(String ki : keys){
                    // clear keys
                    if(cMap.get(ki)!=null){
                        String string=string(cMap, ki);
                        clearValue(mapObject, ki, string);
                    }else{
                        String value2=string(mapData, ki);
                        clearValue(mapObject, ki, value2);
                    }
                }
            }else{
                mapObject.putAll(mapData);
            }

            return mapObject;
        }
        return null;
    }

    public static void clearValue(Map<String, Object> mapObject, String ki, String string){
        if(string==null||"null".equals(string)){
            return;
        }
        if(string.startsWith("\"")||string.startsWith("'")||string.endsWith("\"")||string.endsWith("'")){
            string=string.substring(1, string.length()-1);
            mapObject.put(ki, string);
        }else{
            mapObject.put(ki, string);
        }

    }

    public static Map<String, String> stringMap(Map<String, Object> mapData, String key){
        Map<String, String> stringMap=new HashMap<>();
        Map<String, Object> mapObject=mapObject(mapData, key);
        if(mapObject!=null){
            for(Entry<String, Object> mi : mapObject.entrySet()){
                Object value=mi.getValue();
                if(value==null){
                    continue;
                }
                String obj=String.valueOf(value);
                stringMap.put(mi.getKey(), obj);
            }
        }

        return stringMap;
    }

    public static Map<String, Object> copyMap(Map<String, Object> mapData){
        Map<String, Object> mapObject=new HashMap<>(mapData.size());
        for(Entry<String, Object> mi : mapData.entrySet()){
            Object value=mi.getValue();
            if(value==null){
                continue;
            }
            mapObject.put(mi.getKey(), value);
        }

        return mapObject;
    }

    /**
     * 根据指定列，进行赋值
     *
     * @param source
     * @param target
     * @param columns
     */
    public static void copyValues(Map<String, Object> source, Map<String, Object> target, String columns){
        for(String ki : columns.split(",")){
            target.put(ki, source.get(ki));
        }
    }

    public static void copyValues(Map<String, Object> source, Map<String, Object> target, Set<String> columns){
        for(String ki : columns){
            target.put(ki, source.get(ki));
        }
    }

    public static Map<String, String> toMapString2(Map<String, Object> mapData){
        Map<String, String> stringMap=new HashMap<>();

        if(mapData!=null){
            for(Entry<String, Object> mi : mapData.entrySet()){
                Object value=mi.getValue();
                if(value==null){
                    continue;
                }
                String obj=String.valueOf(value);
                stringMap.put(mi.getKey(), obj);
            }
        }

        return stringMap;
    }

    public static String[] splitValue(Map<String, Object> mapData, String key){
        return splitValue(mapData, key, ",");
    }

    public static String[] splitColumnValue(Map<String, Object> mapData, String key){
        return splitColumnValue(mapData, key, ",");
    }

    public static Set<String> splitValue2Set(Map<String, Object> mapData, String key){
        Set<String> set=new HashSet<>();
        String[] splitValue=splitValue(mapData, key, ",");
        if(splitValue!=null&&splitValue.length>0){
            for(String vi : splitValue){
                set.add(vi);
            }

        }
        return set;
    }

    public static Set<Long> splitValueLongSet(Map<String, Object> mapData, String key){
        Set<Long> set=new HashSet<>();
        String[] splitValue=splitValue(mapData, key, ",");
        if(splitValue!=null&&splitValue.length>0){
            for(String vi : splitValue){
                set.add(Long.valueOf(vi));
            }

        }
        return set;
    }

    public static List<String> listStr(Map<String, Object> mapData, String key){

        return (List<String>) mapData.get(key);
    }

    public static List<String> splitValue2List(Map<String, Object> mapData, String key){
        List<String> set=new ArrayList<>();
        String[] splitValue=splitValue(mapData, key, ",");
        if(splitValue!=null&&splitValue.length>0){
            for(String vi : splitValue){
                set.add(vi);
            }

        }
        return set;
    }

    public static String getColByHeader(Map<String, Object> mapData, String header){
        return nameColumn(mapData).get(header);
    }

    public static Map<String, String> nameColumn(Map<String, Object> mapData){

        String[] columns=columns(mapData);
        String[] headers=headers(mapData);

        Map<String, String> xx=new HashMap<>();
        for(int i=0; i<columns.length; i++){
            if(i+1>headers.length){
                break;
            }
            xx.put(headers[i], columns[i]);
        }
        return xx;
    }

    public static String getHeaderByCol(Map<String, Object> mapData, String col){
        return colName(mapData).get(col);
    }

    /**
     * 返回元数据的字段 映射 表头 map
     *
     * @param mapData
     * @return
     */
    public static Map<String, String> colName(Map<String, Object> mapData){
        String[] columns=columns(mapData);
        String[] headers=headers(mapData);
        Map<String, String> xx=new HashMap<>();
        for(int i=0; i<columns.length; i++){
            xx.put(columns[i], headers[i]);
        }
        return xx;
    }
    public static String[] shortHeaders(Map<String, Object> mapData){
        String[] shortCols=shortShow(mapData);
        if(shortCols!=null&&shortCols.length>0){
            Map<String, String> colName=colName(mapData);
            String[] shortHaders=new String[shortCols.length];
            for(int i=0; i<shortCols.length; i++){
                shortHaders[i]=colName.get(shortCols[i]);
            }
           return shortHaders;
        }
        return splitColumnValue(mapData, HEADER);
    }

    public static String[] headers(Map<String, Object> mapData){
        return splitColumnValue(mapData, HEADER);
    }

    public static String headersString(Map<String, Object> mapData){
        return string(mapData, HEADER);
    }

    public static String header(Map<String, Object> mapData){
        return string(mapData, HEADER);
    }

    public static String[] columns(Map<String, Object> mapData){
        return splitColumnValue(mapData, COLUMNS);
    }
    // 返回Map的Key的列表
    public static Set<String> keys(Map<String, Object> mapData){
        return mapData.keySet();
    }
    public static String keyString(Map<String, Object> mapData){
        return String.join(",", mapData.keySet());
    }

    public static String columnsString(Map<String, Object> mapData){
        return string(mapData, COLUMNS);
    }

    public static String[] show(Map<String, Object> mapData){
        return splitColumnValue(mapData, "show");
    }
    public static String[] shortShow(Map<String, Object> mapData){
        return splitColumnValue(mapData, "shortShow");
    }



    public static Map<String, String> dbColMap(Map<String, Object> mapData){
        String[] columns=columns(mapData);
        String[] dbColumns=splitColumnValue(mapData, "dbColumn");
        Map<String, String> xx=new HashMap<>();
        for(int i=0; i<columns.length; i++){
            xx.put(dbColumns[i], columns[i]);
        }
        return xx;
    }

    public static String content(Map<String, Object> mapData){
        return string(mapData, CONTENT);
    }

    public static String status(Map<String, Object> mapData){
        return string(mapData, STATUS);
    }

    public static String creator(Map<String, Object> mapData){
        return string(mapData, CREATOR);
    }

    public static String updator(Map<String, Object> mapData){
        return string(mapData, UPDATOR);
    }

    public static String cypher(Map<String, Object> mapData){
        return string(mapData, CYPHER);
    }

    public static String[] voColumns(Map<String, Object> mapData){
        return splitColumnValue(mapData, VO_COLUMN);
    }

    public static Set<String> columnSet(Map<String, Object> mapData){
        return splitValue2Set(mapData, COLUMNS);
    }

    public static Set<String> splitColumnValue2Set(Map<String, Object> mapData, String key){
        Set<String> set=new HashSet<>();
        String[] splitValue=splitColumnValue(mapData, key, ",");
        if(splitValue!=null&&splitValue.length>0){
            for(String vi : splitValue){
                set.add(vi);
            }

        }
        return set;
    }

//    public static JSONObject compatiblity(JSONObject mapData, Map<String, Object> po){
//        return compatiblity(mapData, po);
//    }
    /**
     * 合法性校验
     *
     * @param mapData
     * @param po
     * @return
     */
    public static Map<String, Object> compatDb(Map<String, Object> mapData, Map<String, Object> po){
        Map<String, Object> map=new HashMap<>(mapData.size());

        String[] columnArray=null;
        if(po.containsKey(VO_COLUMN)&&po.get(VO_COLUMN)!=null){
            columnArray=voColumns(po);
        }else{
            columnArray=columns(po);
        }
        boolean hasRight=false;
        for(String ci : columnArray){
            Object object=mapData.get(ci);
            if(object==null&&ci.contains(".")){
                String[] split=ci.split("\\.");
                boolean b=split.length>1;
                if(b){
                    object=mapData.get(split[1]);
                }
            }

            if(object!=null){
                hasRight=true;
                break;
            }
        }
        if(!hasRight){
            for(String ci : columnArray){
                if(ci.equals(CruderConstant.ID)){
                    continue;
                }
                Object object=mapData.get(ci.toUpperCase());
                if(object!=null){
                    map.put(ci, object);
                }
            }
        }else{
            return mapData;
        }

        return map;
    }
    public static Map<String, Object> compat(Map<String, Object> mapData, Map<String, Object> po){
        Map<String, Object> map=new HashMap<>(mapData.size());

        String[] columnArray=null;
        if(po.containsKey(VO_COLUMN)&&po.get(VO_COLUMN)!=null){
            columnArray=voColumns(po);
        }else{
            columnArray=columns(po);
        }
        boolean hasRight=false;
        for(String ci : columnArray){
            Object object=mapData.get(ci);
            if(object==null&&ci.contains(".")){
                String[] split=ci.split("\\.");
                boolean b=split.length>1;
                if(b){
                    object=mapData.get(split[1]);
                }
            }

            if(object!=null){
                hasRight=true;
                break;
            }
        }
        if(!hasRight){
            for(String ci : columnArray){
                if(ci.equals(CruderConstant.ID)){
                    continue;
                }
                Object object=mapData.get(ci);
                if(object!=null){
                    map.put(ci, object);
                }
            }
        }else{
            return mapData;
        }

        return map;
    }

    public static String[] splitColumnValue(Map<String, Object> mapData, String key, String splitor){
        String vavlueOfKey=string(mapData, key);
        if(vavlueOfKey==null){
            return null;
        }

        if(vavlueOfKey!=null&&!vavlueOfKey.isEmpty()){
            if(vavlueOfKey.contains(splitor)){
                return vavlueOfKey.split(splitor);
            }
            String string=vavlueOfKey.toUpperCase();
            if(string.contains(splitor.toUpperCase())){
                return string.split(splitor.toUpperCase());
            }
            String[] split=string.split(",");
            String[] validType=new String[split.length];
            String temp="";
            int i=0;
            for(String ki : split){
                if(ki.contains("(")&&!ki.endsWith(")")){
                    temp=ki;
                    continue;
                }
                if(!ki.contains("(")&&ki.endsWith(")")&&!temp.isBlank()){
                    temp=temp+","+ki;
                    validType[i]=temp;
                    temp="";
                    i++;
                    continue;
                }
                validType[i]=ki;
                i++;
            }
            return validType;
        }
        return null;
    }

    public static String[] splitValue(Map<String, Object> mapData, String key, String splitor){
        String string=string(mapData, key);
        if(string!=null&&!string.isEmpty()){
            if(string.contains(splitor)){
                return string.split(splitor);
            }
            String[] split=string.split(",");
            String[] validType=new String[split.length];
            String temp="";
            int i=0;
            for(String ki : split){
                if(ki.contains("(")&&!ki.endsWith(")")){
                    temp=ki;
                    continue;
                }
                if(!ki.contains("(")&&ki.endsWith(")")&&!temp.isBlank()){
                    temp=temp+","+ki;
                    validType[i]=temp;
                    temp="";
                    i++;
                    continue;
                }
                validType[i]=ki;
                i++;
            }
            return validType;
        }
        return null;
    }

    public static Map<String, String> toMap(String[] keys, String[] values){
        Map<String, String> map=new HashMap<>();
        int k=0;
        for(String ki : keys){
            map.put(ki, values[k]);
            k++;
        }
        return map;
    }

    public void toSet(Set<String> cols, String[] columns2){
        for(String si : columns2){
            cols.add(si);
        }
    }

    public Set<String> toSet(String[] columns2){
        Set<String> cols=new HashSet<>();
        for(String si : columns2){
            cols.add(si);
        }
        return cols;
    }

    public JSONObject toJSON(Map<String, String> params){
        JSONObject inputJson=null;
        ObjectMapper mapper=new ObjectMapper();
        try{
            inputJson=JSON.parseObject(mapper.writeValueAsString(params));
        }catch(JsonProcessingException e){
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return inputJson;
    }

    public static String listMapString(List<Map<String, Object>> rule){
        StringBuilder sb=new StringBuilder();

        for(Map<String, Object> mi : rule){
            sb.append(mapString(mi)+"\n");
        }
        return sb.toString();
    }

    public static <T> T toObject(Map<String, Object> map,Class<T> t){
        T userOTP =null;
        try{
            userOTP=(T) t.getClass().newInstance();
        }catch(InstantiationException e){
            throw new RuntimeException(e);
        }catch(IllegalAccessException e){
            throw new RuntimeException(e);
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            try {
                java.lang.reflect.Field field = t.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(userOTP, value);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return userOTP;
    }



    public static String jsonString(List<Map<String, Object>> rule){
        StringBuilder sb=new StringBuilder();

        for(Map<String, Object> mi : rule){
            sb.append(JSON.toJSONString(mi));
        }
        return sb.toString();
    }

    public static Set<String> stringSet(Map<String, Object> map, String rule){
        Set<String> ds=new HashSet<>();
        String[] splitValue=splitValue(map, rule);
        for(String si : splitValue){
            ds.add(si.replace("[", "").replaceAll("]", ""));
        }
        return ds;
    }

    public static String[] strArray(Map<String, Object> map, String rule){
        String[] splitValue=splitValue(map, rule);
        for(int i=0; i<splitValue.length; i++){
            String si=splitValue[i];
            splitValue[i]=si.replace("[", "").replaceAll("]", "");
        }
        return splitValue;
    }

    public static String listString(List rule){
        StringBuilder sb=new StringBuilder();

        for(Object mi : rule){
            if(mi instanceof List l){
                sb.append(listString(l));
                continue;
            }
            if(mi instanceof Map m){
                sb.append(mapString(m));
                continue;
            }
            if(mi instanceof String s){
                sb.append(s);
                continue;
            }
            sb.append(String.valueOf(mi));
        }
        return sb.toString();
    }

    /**
     * 可视化数据
     *
     * @param data
     * @return
     */
    public static String mapHtmlString(Map<String, Object> data){

        Map<String, Object> copy=new HashMap<>();
        if(data.size()==1){
            Object value=data.get("data");

            if(value instanceof List s){
                StringBuilder sb=new StringBuilder();
                List<Map<String, Object>> list=(List<Map<String, Object>>) s;
                for(Map<String, Object> si : list){
                    String label2=label(si);
                    if(label2!=null){
                        for(String ki : si.keySet()){
                            if(sb.length()>0){
                                sb.append(",");
                            }
                            sb.append(string(si, ki));
                        }
                        String name2=name(si);
                        String nodeName="";
                        if(sb.length()>1){
                            nodeName="【("+label2+"):"+sb.toString()+"】";
                        }else{
                            nodeName="【("+label2+"):"+name2+"】";
                        }
                        sb.append("<a href=\"javascript:;\" onclick=\"window.open('"+LemodoApplication.MODULE_NAME+"/layui/"+label2+"/documentRead?id="+id(si)+"')\">"+nodeName+"</a>");

                    }
                }
                return sb.toString();
            }
            if(value instanceof String s){
                return s;
            }
        }else{
            copy.putAll(data);
        }
        StringBuilder sb=new StringBuilder();

        String label2=label(data);

        if(label2!=null){
            sb.append("<a href=\"javascript:;\" onclick=\"window.open('"+LemodoApplication.MODULE_NAME+"/layui/"+label2+"/documentRead?id="+id(data)+"')\">【"+"("+label2+"):"+name(data)+"】</a>");
            clearMetaInfo(copy);
        }


        DateTool.replaceTimeLong2DateStr(copy);

        int i=0;
        for(Entry<String, Object> ei : copy.entrySet()){
            Object value=ei.getValue();

            if(value!=null&&!value.equals("")&&!value.equals("null")){
                if(sb.length()>1){
                    sb.append(",");
                }
                if(i>0&&i%3==0){
                    sb.append("\n");
                }
                i++;
                String key=ei.getKey();
                if("出关系".equals(key)||"入关系".equals(key)||"关系".equals(key)||"->".equals(key)||"<-".equals(key)){
                    if(value instanceof String s){
                        sb.append(s);
                        continue;
                    }
                }
                if(value instanceof String s){
                    sb.append(key+":"+s+"");
                    continue;
                }
                if(value instanceof Long s){
                    sb.append(key+":"+s+"");
                    continue;
                }
                if(value instanceof Integer s){
                    sb.append(key+":"+s+"");
                    continue;
                }
                if(value instanceof Calendar s){
                    sb.append(key+":"+s+"");
                    continue;
                }
                if(value instanceof Map s){
                    sb.append(key+":"+mapString(s));
                    continue;
                }
                if(value instanceof List s){
                    sb.append(key+":"+listString(s));
                    continue;
                }

                sb.append(key+":"+String.valueOf(value));
            }
        }

        return sb.toString();
    }

    public static void clearMetaInfo(Map<String, Object> copy){
        copy.remove(ID);
        copy.remove(NAME);
        copy.remove(LABEL);
        copy.remove(COLUMNS);
        copy.remove(HEADER);
    }

    public static String mapString(Map<String, Object> rule){
        Map<String, Object> copy=new HashMap<>();
        copy.putAll(rule);
        DateTool.replaceTimeLong2DateStr(copy);
        StringBuilder sb=new StringBuilder();
        for(Entry<String, Object> ei : copy.entrySet()){
            Object value=ei.getValue();

            if(value!=null){
                if(sb.length()>1){
                    sb.append(",");
                }
                String key=ei.getKey();
                if(value instanceof String s){
                    sb.append(key+":\""+s+"\"");
                    continue;
                }
                if(value instanceof Long s){
                    sb.append(key+":\""+s+"\"");
                    continue;
                }
                if(value instanceof Integer s){
                    sb.append(key+":\""+s+"\"");
                    continue;
                }
                if(value instanceof Calendar s){
                    sb.append(key+":\""+s+"\"");
                    continue;
                }
                if(value instanceof Map s){
                    sb.append(key+":"+mapString(s));
                    continue;
                }
                if(value instanceof List s){
                    sb.append(key+":"+listString(s));
                    continue;
                }

                sb.append(key+":\""+String.valueOf(value)+"\"");
            }
        }

        return sb.toString();
    }

    public static String jsonString(Map<String, Object> rule){
        return JSON.toJSONString(rule);
    }

    public static Integer integer(Map<String, Object> mapData, String key){
        String string=string(mapData, key);
        if(string==null||string.equals("")){
            return null;
        }
        return Integer.valueOf(string);
    }

    public static JSONObject json(Map<String, Object> mapData, String key){
        String string=string(mapData, key);
        if(string==null||string.equals("")){
            return null;
        }
        return JSON.parseObject(string);
    }

    public static Map<String, Map<String, Object>> list2KeyMap(List<Map<String, Object>> listMap, String key){
        Map<String, Map<String, Object>> mapData=new HashMap<>(listMap.size());
        for(Map<String, Object> mi : listMap){
            String keyMi=string(mi, key);
            if(keyMi==null){
                continue;
            }
            mapData.put(keyMi, mi);
        }

        return mapData;
    }

    public static List<Node> listNode(Map<String, Object> mapData, String key){
        Object object=mapData.get(key);
        if(object==null){
            return null;
        }
        if(object instanceof List l){
            return l;
        }
        return (List<Node>) object;
    }

    public static List list(Map<String, Object> mapData, String key){
        Object object=mapData.get(key);
        if(object==null){
            return null;
        }
        if(object instanceof List l){
            return l;
        }
        return null;
    }

    public static List<Object> listObject(Map<String, Object> mapData, String key){
        Object object=mapData.get(key);
        if(object==null){
            return null;
        }
        if(object instanceof List l){
            return (List<Object>) l;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static String joinString(Map<String, Object> mapData, String key){
        Object object=mapData.get(key);
        if(object==null){
            return null;
        }
        if(object instanceof @SuppressWarnings("rawtypes")List l){
            return String.join(",", l);
        }
        return null;
    }

    public static List<Map<String, String>> listMap(Map<String, Object> mapData, String key){
        Object object=mapData.get(key);
        if(object==null){
            return null;
        }
        return (List<Map<String, String>>) object;
    }

    public static List<Map<String, Object>> listObjectMap(Map<String, Object> mapData, String key){
        Object object=mapData.get(key);
        if(object==null){
            return null;
        }
        if(object instanceof List){
            return (List<Map<String, Object>>) object;
        }
        JSONArray parseArray=JSON.parseArray(String.valueOf(object));
        List<Map<String, Object>> dd=new ArrayList<>(parseArray.size());
        for(Object oi : parseArray){
            JSONObject parseObject=JSON.parseObject(String.valueOf(oi));
            dd.add(parseObject);

        }
        return dd;
    }

    /**
     * @param mapData
     * @param key
     * @param data
     * @return
     */
    public static void addMap(Map<String, Object> mapData, String key, Map<String, Object> data){
        List<Map<String, Object>> object=listObjectMap(mapData, key);
        if(object==null){
            List<Map<String, Object>> newData=new ArrayList<>();
            newData.add(data);
            mapData.put(key, newData);
        }else{
            List<Map<String, Object>> newData=object;
            newData.add(data);
            mapData.put(key, newData);
        }
    }

    public static void addListMap(Map<String, Object> mapData, String key, List<Map<String, Object>> listMap){
        List<Map<String, Object>> object=listObjectMap(mapData, key);
        if(object==null){
            mapData.put(key, listMap);
        }else{
            object.addAll(listMap);
            mapData.put(key, object);
        }
    }

    public static void remove(Map<String, Object> mapData, String key){
        mapData.remove(key);
    }

    public static void putNull(Map<String, Object> mapData, String key){
        mapData.put(key, null);
    }

    public static void put(Map<String, Object> mapData, String key, List<Map<String, Object>> listMap){
        mapData.put(key, listMap);
    }

    public static void put(Map<String, Object> mapData, String key, Map<String, Object> data){
        mapData.put(key, data);
    }

    public static Map<String, String> map(Map<String, Object> mapData, String key){
        Object object=mapData.get(key);
        if(object==null){
            return null;
        }
        return (Map<String, String>) object;
    }


    public static Map<String, Object> mapObject(Map<String, Object> mapData, String key){
        Object object=mapData.get(key);
        if(object==null){
            return null;
        }
        if(object instanceof Map m){
            return (Map<String, Object>) m;
        }

        if(object instanceof String s){
            return JSON.parseObject(s);
        }

        return (Map<String, Object>) object;
    }

    public static Map<String, Map<String, Object>> mapKeyMap(Map<String, Object> mapData, String key){
        Object object=mapData.get(key);
        if(object==null){
            return null;
        }
        return (Map<String, Map<String, Object>>) object;
    }

    public static Boolean compareMap(Map<String, Object> oldData, Map<String, Object> newData){
        if(oldData==null||newData==null||newData.isEmpty()||oldData.isEmpty()){
            return false;
        }
        Map<String, Object> old=copy(oldData);
        Map<String, Object> newObj=copy(newData);
        old.remove(ID);
        newObj.remove(ID);
        for(Entry<String, Object> mi : old.entrySet()){
            Object value=mi.getValue();

            Object newValue=newObj.get(mi.getKey());
            if(value==null&&newValue!=null){
                return false;
            }
            if(value!=null&&newValue==null){
                return false;
            }
            if(value!=null&&newValue!=null&&!value.equals(newValue)){
                return false;
            }
        }

        for(Entry<String, Object> mi : newObj.entrySet()){
            Object newValue=mi.getValue();

            Object oldValue=old.get(mi.getKey());
            if(newValue==null&&oldValue!=null){
                return false;
            }
            if(newValue!=null&&oldValue==null){
                return false;
            }
            if(newValue!=null&&oldValue!=null&&!newValue.equals(oldValue)){
                return false;
            }
        }

        return true;
    }

    public static List<Map<String, Object>> listMapObject(Map<String, Object> mapData, String key){
        Object object=mapData.get(key);
        if(object==null){
            return null;
        }
        if(object instanceof List){
            return (List<Map<String, Object>>) object;
        }
        if(object instanceof String s){
            if("".equals(object)){
                return null;
            }
            JSONArray parseArray=JSON.parseArray(s);
            List<Map<String, Object>> data=new ArrayList<>(parseArray.size());
            for(Object i : parseArray.toArray()){
                data.add((Map<String, Object>) i);
            }
            return data;
        }
        return (List<Map<String, Object>>) object;
    }

    public static List<String> arrayList(Map<String, Object> mapData, String key){
        List<String> list=new ArrayList<>();
        Object object=mapData.get(key);
        if(object==null){
            return null;
        }
        if(object instanceof String s){
            list.add(s);
            return list;
        }
        if(object instanceof List s){
            return s;
        }
        return (List<String>) object;
    }

    public static Object[] array(Map<String, Object> mapData, String key){
        Object object=mapData.get(key);
        if(object==null){
            return null;
        }

        if(object instanceof List s){
            return s.toArray();
        }
        return null;
    }

    public static Long longValue(Map<String, Object> mapData, String key){
        String string=string(mapData, key);
        if(string==null||string.isBlank()){
            return null;
        }
        if(string.contains(":")&&string.trim().contains(" ")){
            return DateUtil.strToDateLong(string).getTime();
        }
        return Long.valueOf(string);
    }

    public static Float floatValue(Map<String, Object> mapData, String key){
        String string=string(mapData, key);
        if(string==null||string.isBlank()){
            return null;
        }
        return Float.valueOf(string);
    }

    public static long[] splitLong(Map<String, Object> mapData, String key){
        String[] strs=splitValue(mapData, key);
        if(strs==null||strs.length<1){
            return null;
        }
        long[] los=new long[strs.length];
        for(int i=0; i<strs.length; i++){
            los[i]=Long.valueOf(strs[i]);
        }
        return los;
    }

    public static int[] splitInt(Map<String, Object> mapData, String key){
        String[] strs=splitValue(mapData, key);
        if(strs==null||strs.length>0){
            return null;
        }
        int[] los=new int[strs.length];
        for(int i=0; i<strs.length; i++){
            los[i]=Integer.valueOf(strs[i]);
        }
        return los;
    }

    public static BigDecimal bigDecimal(Map<String, Object> mapData, String key){
        return BigDecimal.valueOf(longValue(mapData, key));
    }

    public static Date dateValue(Map<String, Object> mapData, String key){
        Object obj=mapData.get(key);
        if(obj==null){
            return null;
        }
        // if(obj instanceof Date) {
        // return (Date)obj;
        // }
        return DateUtil.strToDateLong(String.valueOf(obj));
    }

    public static Long dateLongValue(Map<String, Object> mapData, String key){
        Object obj=mapData.get(key);
        if(obj==null){
            return null;
        }
        // if(obj instanceof Date d) {
        // return d.getTime();
        // }
        return DateUtil.strToDateLong(String.valueOf(obj)).getTime();
    }

    /**
     * 根据columns将数据 List<Map<String, Object>> 转换为 List<Object[]>
     *
     * @param columns
     * @param query2
     * @return
     */
    public static List<Object[]> listMap2ListObjectArray(String[] columns, List<Map<String, Object>> query2){
        List<Object[]> list=new ArrayList<>(query2.size());
        for(Map<String, Object> di : query2){
            Object[] dai=new Object[columns.length];
            for(int k=0; k<columns.length; k++){
                String ci=columns[k];
                dai[k]=di.get(ci);
            }
            list.add(dai);
        }
        return list;
    }

    public static List<Object> listMap2List(String coli, List<Map<String, Object>> query2){
        List<Object> xx=new ArrayList<>();
        List<Object[]> list=new ArrayList<>(query2.size());
        for(Map<String, Object> di : query2){
            xx.add(di.get(coli));
        }
        return xx;
    }

    public static List<String> listMap2ListString(String coli, List<Map<String, Object>> query2){
        List<String> xx=new ArrayList<>();
        List<Object[]> list=new ArrayList<>(query2.size());
        for(Map<String, Object> di : query2){
            xx.add(string(di, coli));
        }
        return xx;
    }

    /**
     * 聚合数据为一条
     *
     * @param dataList
     * @return
     */
    public static Map<String, Object> mergOne(List<Map<String, Object>> dataList){
        if(dataList==null||dataList.isEmpty()){
            return null;
        }
        Map<String, Set<String>> multi=new HashMap<>();
        for(Map<String, Object> di : dataList){
            for(Entry<String, Object> ei : di.entrySet()){
                Set<String> setValue=multi.get(ei.getKey());
                if(setValue==null){
                    setValue=new HashSet<>();
                }
                setValue.add(string0(di, ei.getKey()));
                multi.put(ei.getKey(), setValue);
            }
        }
        Map<String, Object> retData=dataList.get(0);
        for(String ki : multi.keySet()){
            Set<String> mValue=multi.get(ki);
            if(mValue.size()>1){
                retData.put(ki, String.join(",", mValue));
            }
        }
        return retData;
    }

    public static List<Map<String, Object>> formatDates(List<Map<String, Object>> dataList){
        if(dataList==null||dataList.isEmpty()){
            return dataList;
        }
        List<Map<String, Object>> copyList = copyList(dataList);
        Map<String,Map<String, Object>> dataMap = new HashMap<>();
        for(Map<String, Object> di : copyList){
            dataMap.put(string(di,"id"),di);
        }
        for(Map<String, Object> di : dataList){
            Map<String, Object> datai = dataMap.get(string(di, "id"));
            Map<String, Object> dataDate = formatDate(datai);
            datai.putAll(dataDate);
        }
        return copyList;
    }

    public static void fileDatas(List<Map<String, Object>> dataList){
        if(dataList==null||dataList.isEmpty()){
            return;
        }
        for(Map<String, Object> di : dataList){
            fileData(di);
        }
    }

    public static void fileData(Map<String, Object> di){
        Object createTime=di.get(CREATETIME);
        if(createTime!=null){
            if(createTime instanceof String s){
                if(!s.contains("-")&&!s.contains(":")&&!s.contains("/")){
                    di.put(CREATETIME, dateStr(di, CREATETIME));
                }
            }
            if(createTime instanceof Long l){
                di.put(CREATETIME, dateStr(di, CREATETIME));
            }

        }
        Object update=di.get(UPDATETIME);
        if(update!=null&&update instanceof String s){
            if(!s.contains("-")&&!s.contains(":")&&!s.contains("/")){
                di.put(UPDATETIME, dateStr(di, UPDATETIME));
            }
            if(update instanceof Long l){
                di.put(UPDATETIME, dateStr(di, UPDATETIME));
            }
        }
    }

    public static Map<String, Object> formatDate(Map<String, Object> di){
         if(di==null){
            return null;
        }
        Map<String, Object> copy = copy(di);
        Object createTime=di.get(CREATETIME);
        if(createTime!=null){
            if(createTime instanceof String s){
                if(!s.contains("-")&&!s.contains(":")&&!s.contains("/")){
                    copy.put(CREATETIME, dateStr(di, CREATETIME));
                }
            }
            if(createTime instanceof Long l){
                copy.put(CREATETIME, dateStr(di, CREATETIME));
            }

        }
        Object update=di.get(UPDATETIME);
        if(update!=null&&update instanceof String s){
            if(!s.contains("-")&&!s.contains(":")&&!s.contains("/")){
                copy.put(UPDATETIME, dateStr(di, UPDATETIME));
            }
            if(update instanceof Long l){
                copy.put(UPDATETIME, dateStr(di, UPDATETIME));
            }
        }
        return copy;
    }

    public static Map<String, String> colMap(Map<String, Object> mapData){
        String[] columns=columns(mapData);
        String[] dbColumns=splitColumnValue(mapData, "dbColumn");
        Map<String, String> xx=new HashMap<>();
        for(int i=0; i<columns.length; i++){
            xx.put(columns[i], dbColumns[i]);
        }
        return xx;
    }


}
