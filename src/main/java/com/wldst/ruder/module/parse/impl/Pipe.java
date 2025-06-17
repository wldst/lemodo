package com.wldst.ruder.module.parse.impl;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.ConfigDomain;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * 给xx 管道，连接多个处理器
 *
 * @author wldst
 */
@Component
public class Pipe extends ParseExcuteDomain implements MsgProcess{

    @Autowired
    private HtmlShowService showService;

    @Override
    public List<Map<String, Object>> process(String msg, Map<String, Object> context){
        List<Map<String, Object>> data=new ArrayList<>();
        context.put(USED, "false");
        String pipeSeparate=" | ";
        for(String prefix : pipeSeparate.trim().split(",")){
            if(!bool(context, USED)&&msg.startsWith(prefix)){// 根据角色权限，账号权限，来确定打开范围
                context.put(USED, true);
                msg=msg.replaceFirst(prefix, "");
                if(containLabelInfo(msg)){
                    Map<String, Object> dataOfKuohao=getDataOfKuohao(msg);
                    String meta=string(dataOfKuohao, "meta");
                    String[] split=strArray(dataOfKuohao, "split");
                    List<Map<String, Object>> metaDataByName=getMetaDataByName(meta);
                    for(Map<String, Object> mi : metaDataByName){
                        List<Map<String, Object>> dataBy=neo4jUService.getDataBy(label(mi), split[0]);
                        if(dataBy!=null){
                            for(Map<String, Object> di : dataBy){
                                Map<String, Object> config=newMap();
                                config.put("name", name(di));
                                if(code(di)!=null){
                                    config.put("code", code(di)+ConfigDomain.CONFIGURATION);
                                }else{
                                    config.put("code", ConfigDomain.CONFIGURATION+Calendar.getInstance().getTimeInMillis());
                                }
                                neo4jService.save(config, ConfigDomain.CONFIGURATION);
                                neo4jService.addRel("config", id(di), id(config));
                                di.put("configId", id(config));
                                processConfig(label(di), data, di);
                            }
                        }
                    }
                }else{//无配置括号标识则是配置本身
                    List<Map<String, Object>> dataBy=neo4jUService.getDataBy(ConfigDomain.CONFIGURATION, msg);
                    if(dataBy!=null&&!dataBy.isEmpty()){
                        for(Map<String, Object> di : dataBy){
                            String configUrl=LemodoApplication.MODULE_NAME+"/manage/"+stringId(di)+"/setting";
                            di.put("url", configUrl);
                            showService.validUrlPrefix(di);
                            data.add(di);
                        }
                    }else{
                        Map<String, Object> config=newMap();
                        config.put("name", msg);
                        config.put("code", ConfigDomain.CONFIGURATION+Calendar.getInstance().getTimeInMillis());
                        neo4jService.save(config, ConfigDomain.CONFIGURATION);
                        config.put("configId", id(config));
                        processConfig(ConfigDomain.CONFIGURATION, data, config);
                    }
                }
            }
        }
        return data;
    }


    public void processConfig(String labelOf, List<Map<String, Object>> data, Map<String, Object> data2){
        if(data2!=null){
            String configUrl=LemodoApplication.MODULE_NAME+"/manage/"+string(data2, "configId")+"/setting";

            List<Map<String, Object>> itemList=showService.getItemList(longValue(data2,"configId"));
            if(itemList==null||itemList.isEmpty()){
                configUrl=LemodoApplication.MODULE_NAME+"/manage/"+ConfigDomain.CONFIGURATION;
            }

            data2.put("url", configUrl);
            showService.validUrlPrefix(data2);
            data.add(data2);
        }
    }

}
