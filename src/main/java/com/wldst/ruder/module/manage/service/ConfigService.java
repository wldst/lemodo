package com.wldst.ruder.module.manage.service;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.ConfigDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.ModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class ConfigService extends MapTool {
    final static Logger logger= LoggerFactory.getLogger(ConfigService.class);
    @Autowired
    private CrudNeo4jService cruderService;
    @Autowired
    private HtmlShowService showService;

    public Map<String, Object> getConfigMap(String label) throws DefineException {
        String validLabel=  label.trim();
        logger.info("+++++++++++++++==============="+validLabel);
        List<Map<String, Object>> dataBy=cruderService.getDataBy(validLabel,ConfigDomain.CONFIGURATION);
        Map<String, Object> po =null;
        if(dataBy!=null&!dataBy.isEmpty()){
            po =dataBy.get(0);
        }
        List<Map<String, Object>> config=showService.getItemList(id(po));
        Map<String,Object> configMap=new HashMap<>();
        for(Map<String, Object> ci:config){
            configMap.put(code(ci),value(ci));
        }
        configMap.putAll(po);
        return configMap;
    }
}
