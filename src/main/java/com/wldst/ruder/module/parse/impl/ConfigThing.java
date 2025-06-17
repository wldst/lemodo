package com.wldst.ruder.module.parse.impl;

import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.ConfigDomain;
import com.wldst.ruder.domain.ParseExcuteDomain;
import com.wldst.ruder.module.parse.MsgProcess;
import com.wldst.ruder.util.DateTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.wldst.ruder.LemodoApplication;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * ConfigThing类负责处理与配置相关的消息，实现MsgProcess接口以处理特定格式的消息。
 * 它主要处理给定消息以生成或修改配置项，并将这些配置项作为数据返回。
 *
 * @author wldst
 */
@Component
public class ConfigThing extends ParseExcuteDomain implements MsgProcess{

    @Autowired
    private HtmlShowService showService;

    /**
     * 处理与配置相关的消息，根据消息内容和上下文生成相应的配置数据。
     *
     * @param msg 消息字符串，包含用户输入或请求信息。
     * @param context 上下文信息，用于存储和传递处理过程中的状态信息。
     * @return 返回一个包含配置数据的列表。
     */
    @Override
    public List<Map<String, Object>> process(String msg, Map<String, Object> context){
        List<Map<String, Object>> data=new ArrayList<>();
        context.put(USED, "false");
        String wakeupWord=neo4jService.getSettingBy("wakeup.word.config");
        for(String prefix : wakeupWord.trim().split(",")){
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


    /**
     * 处理配置项，生成配置项的URL并验证URL前缀，然后将配置项添加到数据列表中。
     *
     * @param labelOf 配置项的标签。
     * @param data 存储配置项数据的列表。
     * @param data2 单个配置项的数据。
     */
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
