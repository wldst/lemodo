package com.wldst.ruder.module.workflow.biz;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.workflow.beans.BpmInstance;
import com.wldst.ruder.module.workflow.beans.BpmTask;
import com.wldst.ruder.module.workflow.exceptions.CrudBaseException;
import com.wldst.ruder.module.workflow.template.beans.*;
import com.wldst.ruder.module.workflow.util.NumberUtil;
import com.wldst.ruder.module.workflow.util.TextUtil;
import com.wldst.ruder.module.workflow.util.WFEConstants;
import org.apache.commons.lang3.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 流程模板常用方法提供者
 */
@Service
public class BpmGraphService {
    // 日志对象
    private static Logger logger = LoggerFactory.getLogger(BpmGraphService.class);

    // 流程模板常用方法提供者静态实例变量
    private static BpmGraphService instance;
    @Autowired
    private UserAdminService adminService;
    @Autowired
    private CrudNeo4jService crudService;

    private static Map<String, Map<String, Object>> workflowTemplates;

    /**
     * 屏蔽默认构造函数
     */
    private BpmGraphService() {

    }

    /**
     * 获取流程模板常用方法提供者实例对象
     *
     * @return 流程模板常用方法提供者实例对象
     */
    public synchronized static BpmGraphService getInstance() {
        if (instance == null) {
            instance = new BpmGraphService();
            workflowTemplates = new HashMap<>();
        }
        return instance;
    }

    /**
     * 得到指定的流程模板定义信息
     *
     * @param bpmCode 流程模板唯一标识
     * @return 流程模板定义信息 @
     */

    public Map<String, Object> getWorkFlowTemplate(String bpmCode) {
        Map<String, Object> retTemplate = (Map<String, Object>) workflowTemplates.get(bpmCode);
        if (retTemplate == null) {
            Map<String, Object> attMapBy = crudService.getAttMapBy("code", bpmCode, "BpmGraph");
            workflowTemplates.put(bpmCode, attMapBy);
        }
        return retTemplate;
    }
    private void taskContent(Document rootDocument, String charset) throws IOException {
        Format format = Format.getPrettyFormat();
        Format format2 = Format.getPrettyFormat();
        format.setEncoding(charset);
        XMLOutputter xmlout2 = new XMLOutputter(format);
        ByteArrayOutputStream bo2 = new ByteArrayOutputStream();
        xmlout2.output(rootDocument, bo2);
        System.out.println("生成流程模板对象XML信息=======" + charset + "===============:" + bo2.toString());
    }

}
