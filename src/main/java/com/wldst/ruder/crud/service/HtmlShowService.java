package com.wldst.ruder.crud.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.util.LoggerTool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.domain.LayUIDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.util.CommonUtil;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.StringGet;

import static com.wldst.ruder.module.workflow.constant.BpmDo.idString;

@Service
public class HtmlShowService extends LayUIDomain{
    private final static Logger logger=LoggerFactory.getLogger(HtmlShowService.class);

    private CrudNeo4jService neo4jService;
    private ObjectService objectService;
    private UserAdminService adminService;
    @Autowired
    private CrudNeo4jDriver driver;
    @Autowired
    private ViewService vService;
    @Autowired
    private CrudUtil crudUtil;

    private Map<String, List<Map<String, Object>>> cacheMap=new HashMap<>();

    @Autowired
    public HtmlShowService(@Lazy CrudNeo4jService neo4jService, @Lazy ObjectService objectService, @Lazy UserAdminService adminService){
        this.neo4jService=neo4jService;
        this.objectService=objectService;
        this.adminService=adminService;
    }

    /**
     * table：展现列表和表单，关系tab
     *
     * @param model
     * @param meataData
     * @param useTab
     */
    public void showMetaInstanceCrudPage(Model model, Map<String, Object> meataData, boolean useTab){
        if(meataData.containsKey(HEADER)){
            String metaLabel=label(meataData);
            String visible="Match(s:User)-[r:visible]->(e:MetaData) "+" where id(s)="
                    +adminService.getCurrentUserId()+" and e.label=\""+metaLabel+"\" return r";
            List<Map<String, Object>> query=neo4jService.cypher(visible);

            String visible2="Match(s:User)-->(role:Role)-[r:visible]->(e:MetaData) "+" where id(s)="
                    +adminService.getCurrentUserId()+" and e.label=\""+metaLabel+"\"  return r";
            List<Map<String, Object>> query2=neo4jService.cypher(visible2);
            String[] columnArray=columns(meataData);
            Set<String> cols=new HashSet<>();
            if(query!=null&&!query.isEmpty()){
                toSet(cols, columns(query.get(0)));
            }
            if(query2!=null&&!query2.isEmpty()){
                toSet(cols, columns(query2.get(0)));
            }

            String[] headers=headers(meataData);

            if(!cols.isEmpty()){
                String[] seeHeaders=new String[cols.size()];
                String[] seeCols=new String[cols.size()];
                for(int i=0, j=0; i<columnArray.length; i++){
                    if(cols.contains(columnArray[i])){
                        seeHeaders[j]=headers[i];
                        seeCols[j]=columnArray[i];
                        j++;
                    }
                }
                List<Map<String, Object>> customType=new ArrayList<>();
                Set<String> layUseInfo=formContentTemplate(model, meataData, seeCols, seeHeaders, customType);
                layUseInfo.add("dropdown");
                useLayModule(model, useTab, layUseInfo);
                tableListColumnTemplate2(model, seeCols, seeHeaders, customType);
            }else{
                Set<String> show=splitValue2Set(meataData, "show");
                if(!metaLabel.equals("Field")&&show!=null&&!show.isEmpty()){
                    String[] seeHeaders=new String[show.size()];
                    String[] seeCols=new String[show.size()];
                    for(int i=0, j=0; i<columnArray.length; i++){
                        if(show.contains(columnArray[i])){
                            seeHeaders[j]=headers[i];
                            seeCols[j]=columnArray[i];
                            j++;
                        }
                    }

                    List<Map<String, Object>> customType=new ArrayList<>();
                    Set<String> layUseInfo=formContentTemplate(model, meataData, seeCols, seeHeaders, customType);
                    layUseInfo.add("dropdown");
                    useLayModule(model, useTab, layUseInfo);
                    tableListColumnTemplate2(model, seeCols, seeHeaders, customType);

                }else{
                    List<Map<String, Object>> customType=new ArrayList<>();
                    Set<String> layUseInfo=formContentTemplate(model, meataData, columnArray, headers, customType);
                    layUseInfo.add("dropdown");
                    useLayModule(model, useTab, layUseInfo);
                    tableListColumnTemplate2(model, columnArray, headers, customType);
                }
            }

            // List<String> props = new ArrayList<>();

        }
    }

    public void showSelectPage(Model model, Map<String, Object> meataData, boolean useTab){
        if(meataData.containsKey(HEADER)){
            String metaLabel=label(meataData);
            String[] columnArray=shortShow(meataData);
            if(columnArray==null||columnArray.length==0){
                columnArray= show(meataData);
            }
            if(columnArray==null||columnArray.length==0){
                columnArray= columns(meataData);
            }
            String[] headers=shortHeaders(meataData);
            Set<String> show=splitValue2Set(meataData, "shortShow");
            if(!metaLabel.equals("Field")&&show!=null&&!show.isEmpty()){
                String[] seeHeaders=new String[show.size()];
                String[] seeCols=new String[show.size()];
                for(int i=0, j=0; i<columnArray.length; i++){
                    if(show.contains(columnArray[i])){
                        seeHeaders[j]=headers[i];
                        seeCols[j]=columnArray[i];
                        j++;
                    }
                }

                List<Map<String, Object>> customType=new ArrayList<>();
                Set<String> layUseInfo=formContentTemplate(model, meataData, seeCols, seeHeaders, customType);
                layUseInfo.add("dropdown");
                useLayModule(model, useTab, layUseInfo);
                tableListColumnTemplate2(model, seeCols, seeHeaders, customType);
            }else{
                List<Map<String, Object>> customType=new ArrayList<>();
                Set<String> layUseInfo=formContentTemplate(model, meataData, columnArray, headers, customType);
                layUseInfo.add("dropdown");
                useLayModule(model, useTab, layUseInfo);
                tableListColumnTemplate2(model, columnArray, headers, customType);
            }
        }
    }


    /**
     * 来自Detail页面的请求，需要添加表单按钮
     * @param model
     * @param meataData
     * @param useTab
     */
    public void showNodeFormPage(Model model, Map<String, Object> meataData, boolean useTab){
        if(!meataData.containsKey(HEADER)){
            return;
        }
        String metaLabel=label(meataData);
        String visible="Match(s:User)-[r:visible]->(e:MetaData) "+" where id(s)="
                +adminService.getCurrentUserId()+" and e.label=\""+metaLabel+"\" return r";
        List<Map<String, Object>> query=neo4jService.cypher(visible);

        String visible2="Match(s:User)-->(role:Role)-[r:visible]->(e:MetaData) "+" where id(s)="
                +adminService.getCurrentUserId()+" and e.label=\""+metaLabel+"\"  return r";
        List<Map<String, Object>> query2=neo4jService.cypher(visible2);
        String[] columnArray=columns(meataData);
        model.addAttribute("cols",columnArray);
        Set<String> cols=new HashSet<>();
        if(query!=null&&!query.isEmpty()){
            toSet(cols, columns(query.get(0)));
        }
        if(query2!=null&&!query2.isEmpty()){
            toSet(cols, columns(query2.get(0)));
        }

        String[] headers=headers(meataData);

        if(!cols.isEmpty()){
            String[] seeHeaders=new String[cols.size()];
            String[] seeCols=new String[cols.size()];
            for(int i=0, j=0; i<columnArray.length; i++){
                if(cols.contains(columnArray[i])){
                    seeHeaders[j]=headers[i];
                    seeCols[j]=columnArray[i];
                    j++;
                }
            }
            List<Map<String, Object>> customType=new ArrayList<>();
            Set<String> layUseInfo=formPage(model, meataData, seeCols, seeHeaders, customType);
            useLayModule(model, useTab, layUseInfo);
        }else{
            Set<String> show=splitValue2Set(meataData, "show");
            if(!metaLabel.equals("Field")&&show!=null&&!show.isEmpty()){
                String[] seeHeaders=new String[show.size()];
                String[] seeCols=new String[show.size()];
                for(int i=0, j=0; i<columnArray.length; i++){
                    if(show.contains(columnArray[i])){
                        seeHeaders[j]=headers[i];
                        seeCols[j]=columnArray[i];
                        j++;
                    }
                }
                List<Map<String, Object>> customType=new ArrayList<>();
                Set<String> layUseInfo=formPage(model, meataData, seeCols, seeHeaders, customType);
                useLayModule(model, useTab, layUseInfo);
            }else{
                List<Map<String, Object>> customType=new ArrayList<>();
                Set<String> layUseInfo=formPage(model, meataData, columnArray, headers, customType);
                useLayModule(model, useTab, layUseInfo);
            }
        }

    }


    public void showWumaCrudPage(Model model, Map<String, Object> meataData, boolean useTab){
        if(meataData.containsKey(HEADER)){
            String retColumns=String.valueOf(meataData.get(COLUMNS));
            String header=String.valueOf(meataData.get(HEADER));
            String[] columnArray=retColumns.split(",");
            String[] headers=StringGet.split(header);
            // List<String> props = new ArrayList<>();
            List<Map<String, Object>> customType=new ArrayList<>();
            Set<String> layUseInfo=wmFormContentTemplate(model, meataData, columnArray, headers, customType);
            layUseInfo.add("dropdown");
            useLayModule(model, useTab, layUseInfo);
            tableListColumnTemplate(model, columnArray, headers, customType);
        }
    }

    public void showWumaInstanceCrudPage(Model model, Map<String, Object> meataData, boolean useTab){
        if(meataData.containsKey(HEADER)){
            String[] columnArray=columns(meataData);
            String[] headers=headers(meataData);
            List<Map<String, Object>> customType=new ArrayList<>();
            Set<String> layUseInfo=formWumaTemplate(model, meataData, columnArray, headers, customType);
            layUseInfo.add("dropdown");
            useLayModule(model, useTab, layUseInfo);
            tableListColumnTemplate(model, columnArray, headers, customType);
        }
    }

    public void columnInfo(Model model, Map<String, Object> md, boolean useTab){
        if(md.containsKey(HEADER)){
            String retColumns=String.valueOf(md.get(COLUMNS));
            String header=String.valueOf(md.get(HEADER));
            String[] columnArray=retColumns.split(",");
            String[] headers=StringGet.split(header);
            List<Map<String, Object>> customType=new ArrayList<>();
            if(model.getAttribute("customType")!=null){
                customType=(List<Map<String, Object>>) model.getAttribute("customType");
            }


            tableListColumnTemplate2(model, columnArray, headers, customType);
        }
    }

    public void voTable(Model model, Map<String, Object> po, boolean useTab){
        if(po.containsKey(HEADER)){
            String retColumns=String.valueOf(po.get("voColumns"));
            if(retColumns==null){
                return;
            }
            String header=String.valueOf(po.get(HEADER));
            String[] columnArray=retColumns.split(",");

            String[] headers=StringGet.split(header);

            // List<String> props = new ArrayList<>();
            Set<String> layUseInfo=voFormField(model, po, columnArray, headers);
            layUseInfo.add("dropdown");
            useLayModule(model, useTab, layUseInfo);
            tableInfo(model, columnArray, headers);
        }
    }

//    public void table1(Model model, Map<String, Object> po, boolean useTab) {
//	if (po.containsKey(HEADER)) {
//	    String retColumns = String.valueOf(po.get(COLUMNS));
//	    String header = String.valueOf(po.get(HEADER));
//	    String[] columnArray = retColumns.split(",");
//	    String[] headers = StringGet.split(header);
//	    // List<String> props = new ArrayList<>();
//	    Set<String> layUseInfo = formField1(model, po, columnArray, headers);
//	    useLayModule(model, useTab, layUseInfo);
//	    tableInfo(model, columnArray, headers);
//	}
//    }

    /**
     * @param model
     * @param po
     * @param useTab
     */
    public void editForm(Model model, Map<String, Object> po, boolean useTab){
        if(po.containsKey(HEADER)){
            String retColumns=String.valueOf(po.get(COLUMNS));
            String header=String.valueOf(po.get(HEADER));
            String[] columnArray=retColumns.split(",");
            String[] headers=StringGet.split(header);
            // List<String> props = new ArrayList<>();
            Set<String> layUseInfo=formField(model, po, columnArray, headers);
            useLayModule(model, useTab, layUseInfo);
        }
    }

    public void configForm(Model model, Map<String, Object> po, boolean useTab){
        List<Map<String, Object>> config=getItemList(id(po));
        Set<String> layUseInfo=configField(model, po, config);
        useLayModule(model, useTab, layUseInfo);
        List<String> colsCodes=new ArrayList<>();
        List<String> itemIdList = new ArrayList<>();
        for(Map<String, Object> si : config){
            colsCodes.add(code(si));
        }

        model.addAttribute("colCodes", colsCodes);
    }


    public List<Map<String, Object>> getItemList(Long configId){
        List<Map<String, Object>> config=new ArrayList<>();
        //id,name,value,code,comment
        List<Map<String, Object>> settings=neo4jService.cypher("MATCH(n)-[r:configItem]->(s:Settings) where id(n)="+configId+" return s");
        List<Map<String, Object>> configItems=neo4jService.cypher("MATCH(n)-[r:configItem]->(c:ConfigItem) where id(n)="+configId+" return c");
        if(configItems!=null&&!configItems.isEmpty()){
            config.addAll(configItems);
        }
        if(settings!=null&&!settings.isEmpty()){
            config.addAll(settings);
        }
        return config;
    }


    public void readOnlyForm(Model model, Map<String, Object> po, boolean useTab){
        if(po.containsKey(HEADER)){
            String retColumns=String.valueOf(po.get(COLUMNS));
            String header=String.valueOf(po.get(HEADER));
            String[] columnArray=retColumns.split(",");
            String[] headers=StringGet.split(header);
            // List<String> props = new ArrayList<>();
            Set<String> layUseInfo=readOnlyFormField(model, po, columnArray, headers);
            useLayModule(model, useTab, layUseInfo);
        }
    }

    /**
     * VOForm
     *
     * @param model
     * @param vo
     * @param useTab
     */
    public void editVoForm(Model model, Map<String, Object> vo, boolean useTab){
        if(vo.containsKey(HEADER)){
            String retColumns=String.valueOf(vo.get("voColumns"));
            String header=String.valueOf(vo.get(HEADER));
            String[] columnArray=retColumns.split(",");
            String[] headers=StringGet.split(header);
            // List<String> props = new ArrayList<>();
            Set<String> layUseInfo=formField(model, vo, columnArray, headers);
            useLayModule(model, useTab, layUseInfo);
        }
    }

    public void document(Model model, Map<String, Object> po){
        if(po.containsKey(HEADER)){
            String retColumns=String.valueOf(po.get(COLUMNS));
            String header=String.valueOf(po.get(HEADER));
            String[] columnArray=retColumns.split(",");
            String[] headers=StringGet.split(header);
            readOnlyFormField(model, po, columnArray, headers);
            tableInfo(model, columnArray, headers);
            layOnlyTabJs(model);
        }
    }

    public void documentReadOnly(Model model, Map<String, Object> po){
        if(po.containsKey(HEADER)){
            String retColumns=String.valueOf(po.get(COLUMNS));
            String header=String.valueOf(po.get(HEADER));
            String[] columnArray=retColumns.split(",");
            String[] headers=StringGet.split(header);
            readOnlyFormField(model, po, columnArray, headers);
            tableInfo(model, columnArray, headers);
            layOnlyTabJs(model);
        }
    }

    public void showDefine(Model model, Map<String, Object> md, String formContent){
        if(md.containsKey(HEADER)){
            String label=String.valueOf(md.get(LABEL));

            String endPoName=String.valueOf(md.get(NAME));
            String[] split=formContent.split(",");

            StringBuilder sb=new StringBuilder();
            fieldCheckBoxInput(md, label, sb, endPoName);

            StringBuilder js=new StringBuilder();
            if(split.length>1){
                StringBuilder formBtn=new StringBuilder();
                appendSubmitResetBtn(formBtn);
                for(String si : split){
                    StringBuilder roleOruser=new StringBuilder();
                    if(si.equals("roleShowForm")){
                        roleOruser.append("<div class=\"layui-form-item\" >");
                        Map<String, Object> ctypei=newMap();
                        ctypei.put(NAME, "角色");
                        ctypei.put(CODE, "role");
                        ctypei.put(value_field, ID);
                        roleOruser.append(selectWindow(js, label, ctypei, "Role"));
                        roleOruser.append("</div>");
                        model.addAttribute(si, roleOruser.toString()+sb.toString()+formBtn.toString());
                        continue;
                    }
                    if(si.equals("userShowForm")){
                        roleOruser.append("<div class=\"layui-form-item\" >");
                        Map<String, Object> ctypei=newMap();
                        ctypei.put(NAME, "用户");
                        ctypei.put(CODE, "user");
                        ctypei.put(value_field, ID);
                        roleOruser.append(selectWindow(js, label, ctypei, "User"));
                        roleOruser.append(" </div>");
                        model.addAttribute(si, roleOruser.toString()+sb.toString()+formBtn.toString());
                        continue;
                    }

                    model.addAttribute(si, sb.toString()+formBtn.toString());
                }
            }else{
                appendSubmitResetBtn(sb);
                model.addAttribute(formContent, sb.toString());
            }
            model.addAttribute("formJs", js.toString());
            fieldFormTabJs(model);
        }
    }

    public void fieldCheckBoxInput(Map<String, Object> md, String label, StringBuilder sb, String endPoName){
        sb.append("<div class=\"layui-form-item\" >");
        sb.append("<label class=\"layui-form-label\">"+endPoName+"</label>");
        sb.append("<div class=\"layui-input-block\">");
        sb.append(vService.getFieldCheckList(label, md));
        sb.append(" </div> </div>");
    }

    public void viewDefine(Model model, Map<String, Object> po){
        if(po.containsKey(HEADER)){
            String label=String.valueOf(po.get(LABEL));
            StringBuilder sb=new StringBuilder();
            String endPoName=String.valueOf(po.get(NAME));

            fieldCheckBoxInput(po, label, sb, endPoName);

            sb.append(formTabList(label));
            appendSubmitResetBtn(sb);
            model.addAttribute("formContent", sb.toString());
            fieldFormTabJs(model);
        }
    }

    /**
     * 实体表单列表
     *
     * @param label
     * @return
     */
    public String formTabList(String label){
        List<Map<String, Object>> relationDefineList=neo4jService.queryRelationDefine("startLabel", label);
        StringBuilder htmlBuilder=new StringBuilder();
        for(Map<String, Object> ri : relationDefineList){
            Object endLabel=ri.get(END_LABEL);
            Object relLabelObj=ri.get(RELATION_LABEL);
            if(endLabel!=null){
                String eLabel=String.valueOf(endLabel);
                String relLabel=String.valueOf(relLabelObj);
                Map<String, Object> endiMeta=neo4jService.getAttMapBy(LABEL, eLabel, META_DATA);
                if(endiMeta!=null&&!endiMeta.isEmpty()){
                    String relationName="";
                    Object obj=ri.get(NAME);
                    String name=String.valueOf(endiMeta.get(NAME));

                    if(obj!=null){
                        relationName=String.valueOf(obj);
                        if(!relationName.equals(name)){
                            relationName=relationName+"("+String.valueOf(endiMeta.get(NAME))+")";
                        }else{
                            relationName=relLabel+"("+name+")";
                        }
                    }else{
                        relationName=relLabel;
                    }


                    htmlBuilder.append("<div class=\"layui-form-item\" >");
                    htmlBuilder.append("<label class=\"layui-form-label\">"+relationName+"</label>");
                    htmlBuilder.append("<div class=\"layui-input-block\">");
                    htmlBuilder.append(vService.getFieldCheckList(relLabel+"_"+eLabel, endiMeta));
                    htmlBuilder.append(" </div> </div>");
                }
            }
        }
        return htmlBuilder.toString();
    }

    public void module(Model model){
        useLayModule(model, true, new HashSet<>());
    }

    /**
     * 字段定义
     *
     * @param model
     * @param po            对象
     * @param columnMapList 当前对象的字段列表
     */
    public void field(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList, Boolean useTab){
        if(po.containsKey(HEADER)){
            String[] columnArray=columns(po);
            String[] headers=headers(po);
            // List<String> props = new ArrayList<>();

            Set<String> layUseInfo=formField2(model, po, columnArray, headers, columnMapList);

            useLayModule(model, useTab, layUseInfo);

            if(label(po).equals("Field")){
                String[] show=splitValue(po, "show");
                if(show!=null&&show.length>0){
                    tableShowInfo(model, show, columnArray, headers);
                }
            }else{
                String[] shortShows=splitValue(po, "shortShow");
                if(shortShows!=null&&shortShows.length>0){
                    tableShowInfo(model, shortShows, columnArray, headers);
                }else{
                    tableInfo(model, columnArray, headers);
                }
            }


        }
    }

    /**
     * 字段校验
     *
     * @param model
     * @param po
     * @param columnMapList
     */
    public void fieldValidate(Model model, Map<String, Object> po, List<Map<String, Object>> columnMapList,
                              Boolean useTab){
        if(po.containsKey(HEADER)){
            String[] columnArray=columns(po);
            String[] headers=headers(po);
            Set<String> layUseInfo=formField2(model, po, columnArray, headers, columnMapList);

            useLayModule(model, useTab, layUseInfo);

            tableInfo(model, columnArray, headers);
        }
    }

    /**
     * 展现静态表单
     *
     * @param po
     */
    public String staticForm(Map<String, Object> po, Map<String, Object> propMap){
        if(po.containsKey(HEADER)){
            String retColumns=String.valueOf(po.get(COLUMNS));
            String header=String.valueOf(po.get(HEADER));
            String[] columnArray=retColumns.split(",");
            String[] headers=StringGet.split(header);
            // List<String> props = new ArrayList<>();
            return staticFormField(po, columnArray, headers, propMap);
        }
        return "";
    }

    private Set<String> voFormField(Model model, Map<String, Object> po, String[] columnArray, String[] headers){
        Set<String> boolMap=new HashSet<>();
        JSONObject vo=new JSONObject();
        Long longValue=longValue(po, "voId");
        vo.put("objectId", longValue);
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");
        List<Map<String, Object>> validateList=objectService.getBy(vo, "FieldValidate");
        Map<String, Map<String, Object>> customFieldMap=new HashMap<>(fieldInfoList.size());
        for(Map<String, Object> fi : fieldInfoList){
            Object object=fi.get("field");
            object=object==null ? fi.get("id") : object;
            customFieldMap.put(String.valueOf(object), fi);
        }
        Map<String, String> columnShow=new HashMap<>();

        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item\">");
        List<Map<String, Object>> customType=new ArrayList<>();
        boolean hasDateField=false;
        if(validateList!=null&&!validateList.isEmpty()){
            Map<String, Map<String, Object>> fieldValidateMap=new HashMap<>(validateList.size());
            for(Map<String, Object> fi : validateList){
                Object object=fi.get("field");
                object=object==null ? fi.get("id") : object;
                Object validator=fi.get(COLUMN_VALIDATOR);
                JSONObject query=new JSONObject();
                query.put(CODE, validator);
                String[] columString={COLUMN_VALIDATOR};
                List<Map<String, Object>> by=objectService.getColumnsBy(query, "InputValidate", columString);
                if(by!=null&&!by.isEmpty()){
                    fi.putAll(by.get(0));
                }

                fieldValidateMap.put(String.valueOf(object), fi);
            }
            hasDateField=fieldHandle(columnArray, headers, customFieldMap, fieldValidateMap, columnShow, customType);
        }else{
            hasDateField=fieldHandle(columnArray, headers, customFieldMap, columnShow, customType);
        }

        boolMap.add("laydate");
        Set<String> layUseInfo=showCustomField(model, columnShow, customType, null, columnArray.length);
        boolMap.addAll(layUseInfo);

        formHtml(columnArray, headers, sb, columnShow);
        sb.append("</div>");
        appendSubmitResetBtn(sb);
        // 总的表单内容
        model.addAttribute("formContent", sb.toString());
        return boolMap;
    }

    private Set<String> configField(Model model, Map<String, Object> po, List<Map<String, Object>> settings){
        Set<String> boolMap=new HashSet<>();
        JSONObject param=new JSONObject();
        String labelPo=label(po);
        param.put("poId", labelPo);
        Map<String, Map<String, Object>> itemMap = new HashMap<>(settings.size());

        List<String> itemIdList = new ArrayList<>();
        for(Map<String, Object> si : settings){
            itemIdList.add(idString(si));
            itemMap.put(idString(si), si);
        }
        //加载字段展现类型以及自定义字段信息

        Map<String, Object> vo=new HashMap<>();
        vo.put("objectId", itemIdList);
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");
        List<Map<String, Object>> validateList=objectService.getBy(vo, "FieldValidate");

        Map<String, Map<String, Object>> customFieldMap=new HashMap<>(fieldInfoList.size());
        for(Map<String, Object> fi : fieldInfoList){
            Map<String, Object> ci=   itemMap.get( string(fi, "objectId"));
            customFieldMap.put(code(ci), fi);
        }


        StringBuilder sb=new StringBuilder();
        appendHidden(REL_START_ID, sb);
        appendHidden(REL_START_LABEL, sb);
        sb.append("<div  class=\"layui-form-item\">");
        Map<String, String> columnShow=new HashMap<>();
        List<Map<String, Object>> customType=new ArrayList<>();
        if(validateList!=null&&!validateList.isEmpty()){

            Map<String, Map<String, Object>> validFieldMap=new HashMap<>(fieldInfoList.size());
            for(Map<String, Object> fi : fieldInfoList){
                Map<String, Object> ci=   itemMap.get( string(fi, "poId"));
                validFieldMap.put(code(ci), fi);
            }

            Map<String, Map<String, Object>> fieldValidateMap=new HashMap<>(validateList.size());
            for(Map<String, Object> fi : validateList){
                Object object=fi.get("field");
                object=object==null ? fi.get("id") : object;
                Object validator=fi.get(COLUMN_VALIDATOR);
                JSONObject query=new JSONObject();
                query.put(CODE, validator);
                String[] columString={COLUMN_VALIDATOR};
                List<Map<String, Object>> by=objectService.getColumnsBy(query, "InputValidate", columString);
                if(by!=null&&!by.isEmpty()){
                    fi.putAll(by.get(0));
                }

                fieldValidateMap.put(String.valueOf(object), fi);
            }
            fieldHandleWithStatus(settings, customFieldMap, fieldValidateMap, columnShow, customType, labelPo);
        }else{
            fieldWithStatusHandle(settings, customFieldMap, columnShow, customType, labelPo);
        }
        isUseDate(settings, boolMap, customFieldMap);

        model.addAttribute("customType", customType);


        sb.append("<div  class=\"layui-form-item\">");


        Set<String> layUseInfo=customFormField(model, columnShow, customType, null, settings.size());
        boolMap.addAll(layUseInfo);
        model.addAttribute("customType", customType);

        formHtml(settings, sb, columnShow);
        sb.append("</div>");
         appendSubmitResetBtn(sb);
        Long confiId=neo4jService.getNodeId(CruderConstant.LABEL, label(po), CruderConstant.META_DATA);

        List<Map<String, Object>> formBtnList=listFormBtnById(confiId);
        StringBuilder formSb=new StringBuilder();
        appendBtn(model, formSb, formBtnList);
        model.addAttribute("opt", formSb.toString());
        // 总的表单内容
        model.addAttribute("formContent", sb.toString());
        return boolMap;
    }

    /**
     * form 根据定义信息组装表单字段，自定义和常规字段
     *
     * @param model
     * @param po
     * @param columnArray
     * @param headers
     * @return
     */
    private Set<String> formField(Model model, Map<String, Object> po, String[] columnArray, String[] headers){
        Set<String> boolMap=new HashSet<>();
        JSONObject vo=new JSONObject();
        String labelPo=label(po);
        vo.put("poId", labelPo);

        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");
        List<Map<String, Object>> validateList=objectService.getBy(vo, "FieldValidate");
        Map<String, Map<String, Object>> customFieldMap=new HashMap<>(fieldInfoList.size());
        //带有当前数据ID
        if(label(po).equals(META_DATA)){
            useMetaFieldInfo(fieldInfoList, customFieldMap);
        }else{
            String dataId = String.valueOf(model.getAttribute("currentId"));
            if(dataId!=null&&!"null".equals(dataId)){//带有当前数据ID
                //是否有当前字段的定义信息
                vo.put("objectId", dataId);
                // 查询自定义字段数据
                List<Map<String, Object>> fieldInfoList2=objectService.getBy(vo, "Field");
                Boolean hasFieldInfo=false;
                if(!fieldInfoList2.isEmpty()){
                    for(Map<String, Object> fi : fieldInfoList2){
                        Object ojbjectId=fi.get("objectId");
                        if(ojbjectId!=null&&ojbjectId.equals(dataId)){
                            hasFieldInfo=true;
                        }
                    }
                    if(hasFieldInfo){//字段展现信息
                        for(Map<String, Object> fi : fieldInfoList2){
                            Object ojbjectId = fi.get("objectId");
                            if(ojbjectId!=null&&ojbjectId.equals(dataId)){
                                Object object=fi.get("field");
                                object=object==null ? fi.get("id") : object;
                                customFieldMap.put(String.valueOf(object), fi);
                                break;
                            }
                        }
                    }
                }else{//无字段定义信息,带上ObjectID没查询到数据
                     if(fieldInfoList2.isEmpty()){
                         usePoFieldInfo(fieldInfoList,po, customFieldMap);
                     }else{
                         usePoFieldInfo(fieldInfoList2,po, customFieldMap);
                     }
                }
            }else{
                usePoFieldInfo(fieldInfoList,po, customFieldMap);
            }
        }
        StringBuilder sb=new StringBuilder();
        appendHidden(REL_START_ID, sb);
        appendHidden(REL_START_LABEL, sb);
        sb.append("<div  class=\"layui-form-item\">");
        Map<String, String> columnShow=new HashMap<>();
        List<Map<String, Object>> customType=new ArrayList<>();
        if(validateList!=null&&!validateList.isEmpty()){
            Map<String, Map<String, Object>> fieldValidateMap=new HashMap<>(validateList.size());
            for(Map<String, Object> fi : validateList){
                Object object=fi.get("field");
                object=object==null ? fi.get("id") : object;
                Object validator=fi.get(COLUMN_VALIDATOR);
                JSONObject query=new JSONObject();
                query.put(CODE, validator);
                String[] columString={COLUMN_VALIDATOR};
                List<Map<String, Object>> by=objectService.getColumnsBy(query, "InputValidate", columString);
                if(by!=null&&!by.isEmpty()){
                    fi.putAll(by.get(0));
                }

                fieldValidateMap.put(String.valueOf(object), fi);
            }
            fieldHandleWithStatus(columnArray, headers, customFieldMap, fieldValidateMap, columnShow, customType, labelPo);
        }else{
            fieldWithStatusHandle(columnArray, headers, customFieldMap, columnShow, customType, labelPo);
        }
        isUseDate(columnArray, boolMap, customFieldMap);

        Set<String> layUseInfo=customFormField(model, columnShow, customType, null, columnArray.length);
        boolMap.addAll(layUseInfo);
        model.addAttribute("customType", customType);

        formHtml(columnArray, headers, sb, columnShow);
        sb.append("</div>");
        // appendSubmitResetBtn(sb);
        List<Map<String, Object>> formBtnList=listFormBtnById(longValue(po, ID));
        appendSubmitResetBtn(model, sb, formBtnList);
        // 总的表单内容
        model.addAttribute("formContent", sb.toString());
        return boolMap;
    }

    private static void useFieldInfo(List<Map<String, Object>> fieldInfoList, Map<String, Map<String, Object>> customFieldMap){
        for(Map<String, Object> fi : fieldInfoList){
            Object ojbjectId = fi.get("objectId");
            if(ojbjectId==null){
                //使用元数据字段定义
                Object object=fi.get("field");
                object=object==null ? fi.get("id") : object;
                customFieldMap.put(String.valueOf(object), fi);
            }
        }
    }

    private static void usePoFieldInfo(List<Map<String, Object>> fieldInfoList,Map<String, Object> po, Map<String, Map<String, Object>> customFieldMap){
        for(Map<String, Object> fi : fieldInfoList){
            Object ojbjectId = fi.get("objectId");
            if(ojbjectId==null||ojbjectId.equals(stringId(po))||ojbjectId.equals(id(po))){
                //使用元数据字段定义
                Object object=fi.get("field");
                object=object==null ? fi.get("id") : object;
                customFieldMap.put(String.valueOf(object), fi);
            }
        }
    }

    private static void useMetaFieldInfo(List<Map<String, Object>> fieldInfoList, Map<String, Map<String, Object>> customFieldMap){
        for(Map<String, Object> fi : fieldInfoList){
            //使用元数据字段定义
            Object object=fi.get("field");
            object=object==null ? fi.get("id") : object;
            customFieldMap.put(String.valueOf(object), fi);

        }
    }

    private Set<String> readOnlyFormField(Model model, Map<String, Object> po, String[] columnArray, String[] headers){
        Set<String> boolMap=new HashSet<>();
        JSONObject vo=new JSONObject();
        String labelPo=label(po);
        vo.put("poId", labelPo);
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");
        List<Map<String, Object>> validateList=objectService.getBy(vo, "FieldValidate");
        Map<String, Map<String, Object>> customFieldMap=new HashMap<>(fieldInfoList.size());
        for(Map<String, Object> fi : fieldInfoList){
            Object object=fi.get("field");
            object=object==null ? fi.get("id") : object;
            customFieldMap.put(String.valueOf(object), fi);
        }

        StringBuilder sb=new StringBuilder();
        appendHidden(REL_START_ID, sb);
        appendHidden(REL_START_LABEL, sb);
        sb.append("<div  class=\"layui-form-item\">");
        Map<String, String> columnShow=new HashMap<>();
        List<Map<String, Object>> customType=new ArrayList<>();
        if(validateList!=null&&!validateList.isEmpty()){
            Map<String, Map<String, Object>> fieldValidateMap=new HashMap<>(validateList.size());
            for(Map<String, Object> fi : validateList){
                Object object=fi.get("field");
                object=object==null ? fi.get("id") : object;
                Object validator=fi.get(COLUMN_VALIDATOR);
                JSONObject query=new JSONObject();
                query.put(CODE, validator);
                String[] columString={COLUMN_VALIDATOR};
                List<Map<String, Object>> by=objectService.getColumnsBy(query, "InputValidate", columString);
                if(by!=null&&!by.isEmpty()){
                    fi.putAll(by.get(0));
                }

                fieldValidateMap.put(String.valueOf(object), fi);
            }
            fieldHandleWithStatus(columnArray, headers, customFieldMap, fieldValidateMap, columnShow, customType, labelPo);
        }else{
            fieldWithStatusHandle(columnArray, headers, customFieldMap, columnShow, customType, labelPo);
        }
        isUseDate(columnArray, boolMap, customFieldMap);

        Set<String> layUseInfo=customFormField(model, columnShow, customType, null, columnArray.length);
        boolMap.addAll(layUseInfo);


        readFormHtml(columnArray, headers, sb, columnShow);
        sb.append("</div>");
        List<Map<String, Object>> formBtnList=listFormBtnById(longValue(po, ID));
        appendBtns(model, sb, formBtnList);
        // 总的表单内容
        model.addAttribute("formContent", sb.toString());
        return boolMap;
    }

    /**
     * 查询自定义字段数据
     *
     * @param labelPo
     * @return
     */
    public List<Map<String, Object>> getField(String labelPo){
        List<Map<String, Object>> list=cacheMap.get(labelPo);
        if(list!=null&&!list.isEmpty()){
            return list;
        }
        JSONObject vo=new JSONObject();
        vo.put("poId", labelPo);
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");
        cacheMap.put(labelPo, fieldInfoList);
        return fieldInfoList;
    }
    public List<Map<String, Object>> getTimeField(){
        JSONObject vo=new JSONObject();
        vo.put("showType", "datetime");
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");
        return fieldInfoList;
    }

    public Set<String> getTimeField(String label){
        JSONObject vo=new JSONObject();
        vo.put("showType", "datetime");
        vo.put("poId", label);
        // 查询自定义字段数据
        Set<String> tf= new HashSet<>();
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");
        for(Map<String, Object> mi : fieldInfoList){
            String field=string(mi, "field");
            tf.add(field);
        }
        return tf;
    }

    public void clearFieldInfo(String labelPo){
        List<Map<String, Object>> list=cacheMap.get(labelPo);
        if(list!=null&&!list.isEmpty()){
            cacheMap.remove(labelPo);
        }
        JSONObject vo=new JSONObject();
        vo.put("poId", labelPo);
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");
        cacheMap.put(labelPo, fieldInfoList);
    }

    private void isUseDate(String[] columnArray, Set<String> boolMap, Map<String, Map<String, Object>> customFieldMap){
        for(String columni : customFieldMap.keySet()){
            Map<String, Object> field=customFieldMap.get(columni);
            if(field!=null){
                String showType=string(field, show_type);
                if("date".equals(showType)){
                    boolMap.add("laydate");
                }
            }
        }
    }

    private void isUseDate(List<Map<String, Object>> settings,Set<String> boolMap, Map<String, Map<String, Object>> customFieldMap){
        for(String columni : customFieldMap.keySet()){
            Map<String, Object> field=customFieldMap.get(columni);
            if(field!=null){
                String showType=string(field, show_type);
                if("date".equals(showType)){
                    boolMap.add("laydate");
                }
            }
        }
    }

    /**
     * 表单字段展现信息加载
     *
     * @param model
     * @param po
     * @param columnArray
     * @param headers
     * @param customType
     * @return
     */
    private Set<String> formContentTemplate(Model model, Map<String, Object> po, String[] columnArray, String[] headers,
                                            List<Map<String, Object>> customType){
        Set<String> boolMap=new HashSet<>();
        JSONObject vo=new JSONObject();
        String labelPo=label(po);
        vo.put("poId", labelPo);
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");

        List<Map<String, Object>> validateList=objectService.getBy(vo, "FieldValidate");
        Map<String, Map<String, Object>> mapFieldInfo=mapFieldInfo(fieldInfoList);

        StringBuilder sb=new StringBuilder();
        Map<String, String> columnShow=new HashMap<>();
        sb.append("<div  class=\"layui-form-item\">");

        if(validateList!=null&&!validateList.isEmpty()){
            Map<String, Map<String, Object>> fieldValidateMap=new HashMap<>(validateList.size());
            for(Map<String, Object> fi : validateList){
                Object object=fi.get("field");
                object=object==null ? fi.get("id") : object;
                Object validator=fi.get(COLUMN_VALIDATOR);
                JSONObject query=new JSONObject();
                query.put(CODE, validator);
                String[] columString={COLUMN_VALIDATOR};
                List<Map<String, Object>> by=objectService.getColumnsBy(query, "InputValidate", columString);
                if(by!=null&&!by.isEmpty()){
                    fi.putAll(by.get(0));
                }

                fieldValidateMap.put(String.valueOf(object), fi);
            }
            fieldHandleWithStatus(columnArray, headers, mapFieldInfo, fieldValidateMap, columnShow, customType, labelPo);
        }else{
            fieldWithStatusHandle(columnArray, headers, mapFieldInfo, columnShow, customType, labelPo);
        }

        isUseDate(columnArray, boolMap, mapFieldInfo);

        Set<String> layUseInfo=showCustomField(model, columnShow, customType, null, columnArray.length);
        boolMap.addAll(layUseInfo);
        formHtml(columnArray, headers, sb, columnShow);
        sb.append("</div>");
        // appendSubmitResetBtn(sb);
        List<Map<String, Object>> formBtnList=listFormBtnById(longValue(po, ID));
        appendSubmitResetBtn(model, sb, formBtnList);
        // 总的表单内容
        model.addAttribute("formContent", sb.toString());
        return boolMap;
    }

    /**
     * 针对表单添加自定义按钮的功能
     * @param model
     * @param po
     * @param columnArray
     * @param headers
     * @param customType
     * @return
     */
    private Set<String> formPage(Model model, Map<String, Object> po, String[] columnArray, String[] headers,
                                            List<Map<String, Object>> customType){
        Set<String> boolMap=new HashSet<>();
        JSONObject vo=new JSONObject();
        String labelPo=label(po);
        vo.put("poId", labelPo);
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");

        List<Map<String, Object>> validateList=objectService.getBy(vo, "FieldValidate");
        Map<String, Map<String, Object>> mapFieldInfo=mapFieldInfo(fieldInfoList);

        StringBuilder sb=new StringBuilder();
        Map<String, String> columnShow=new HashMap<>();
        sb.append("<div  class=\"layui-form-item\">");

        if(validateList!=null&&!validateList.isEmpty()){
            Map<String, Map<String, Object>> fieldValidateMap=new HashMap<>(validateList.size());
            for(Map<String, Object> fi : validateList){
                Object object=fi.get("field");
                object=object==null ? fi.get("id") : object;
                Object validator=fi.get(COLUMN_VALIDATOR);
                JSONObject query=new JSONObject();
                query.put(CODE, validator);
                String[] columString={COLUMN_VALIDATOR};
                List<Map<String, Object>> by=objectService.getColumnsBy(query, "InputValidate", columString);
                if(by!=null&&!by.isEmpty()){
                    fi.putAll(by.get(0));
                }

                fieldValidateMap.put(String.valueOf(object), fi);
            }
            fieldHandleWithStatus(columnArray, headers, mapFieldInfo, fieldValidateMap, columnShow, customType, labelPo);
        }else{
            fieldWithStatusHandle(columnArray, headers, mapFieldInfo, columnShow, customType, labelPo);
        }

        isUseDate(columnArray, boolMap, mapFieldInfo);

        Set<String> layUseInfo=showCustomField(model, columnShow, customType, null, columnArray.length);
        boolMap.addAll(layUseInfo);
        formHtml(columnArray, headers, sb, columnShow);
        sb.append("</div>");
        // appendSubmitResetBtn(sb);
        List<Map<String, Object>> formBtnList=listFormBtnById(longValue(po, ID));
        appendFormBtn(model, sb, formBtnList);
        // 总的表单内容
        model.addAttribute("formContent", sb.toString());
        return boolMap;
    }

    private void formHtml(String[] columnArray, String[] headers, StringBuilder sb, Map<String, String> columnShow){
        for(int i=0, k=0; i<columnArray.length; i++){
            if(k>1&&k%3==0){
                sb.append("</div><div  class=\"layui-form-item\">");
            }
            // 收集自定义字段信息
            String key2=columnArray[i];
            String customField=columnShow.get(key2);
            if(customField==null||"".equals(customField)){
                sb.append(fieldString(key2, null, headers[i]));
            }else{
                sb.append(customField);
            }
            k++;
        }
    }

    private void formHtml(List<Map<String, Object>> settings, StringBuilder sb, Map<String, String> columnShow){
        for(Map<String, Object> si : settings){
            // 收集自定义字段信息
            String key2=code(si);
            String customField=columnShow.get(key2);
            if(customField==null||"".equals(customField)){
                sb.append(formLine(key2, name(si), value(si)));
            }else{
                sb.append(customField);
            }
            sb.append("</div><div  class=\"layui-form-item\">");
        }
    }

    private void readFormHtml(String[] columnArray, String[] headers, StringBuilder sb, Map<String, String> columnShow){
        for(int i=0, k=0; i<headers.length; i++){
            //独占一行的字段列出来，k==0
            if(k>1&&k%3==0){
                sb.append("</div><div  class=\"layui-form-item\">");
            }
            // 收集自定义字段信息
            String key2=columnArray[i];
            String customField=columnShow.get(key2);
            if(customField==null||"".equals(customField)){
                sb.append(fieldString(key2, null, headers[i]));
            }else{
                sb.append(customField);
            }
            k++;
        }
    }

    private Set<String> wmFormContentTemplate(Model model, Map<String, Object> po, String[] columnArray, String[] headers,
                                              List<Map<String, Object>> customType){
        Set<String> boolMap=new HashSet<>();
        JSONObject vo=new JSONObject();
        String labelPo=label(po);
        vo.put("poId", labelPo);
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");

        List<Map<String, Object>> validateList=objectService.getBy(vo, "FieldValidate");
        Map<String, Map<String, Object>> mapFieldInfo=mapFieldInfo(fieldInfoList);

        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item\">");
        Map<String, String> columnShow=new HashMap<>();
        if(validateList!=null&&!validateList.isEmpty()){
            Map<String, Map<String, Object>> fieldValidateMap=new HashMap<>(validateList.size());
            for(Map<String, Object> fi : validateList){
                Object object=fi.get("field");
                object=object==null ? fi.get("id") : object;
                Object validator=fi.get(COLUMN_VALIDATOR);
                JSONObject query=new JSONObject();
                query.put(CODE, validator);
                String[] columString={COLUMN_VALIDATOR};
                List<Map<String, Object>> by=objectService.getColumnsBy(query, "InputValidate", columString);
                if(by!=null&&!by.isEmpty()){
                    fi.putAll(by.get(0));
                }

                fieldValidateMap.put(String.valueOf(object), fi);
            }
            fieldHandleWithStatus(columnArray, headers, mapFieldInfo, fieldValidateMap, columnShow, customType, labelPo);
        }else{
            fieldWithStatusHandle(columnArray, headers, mapFieldInfo, columnShow, customType, labelPo);
        }

        isUseDate(columnArray, boolMap, mapFieldInfo);

        Set<String> layUseInfo=wmShowCustomField(model, columnShow, customType, null, columnArray, headers);
        boolMap.addAll(layUseInfo);
        formHtml(columnArray, headers, sb, columnShow);
        sb.append("</div>");
        // appendSubmitResetBtn(sb);
        List<Map<String, Object>> formBtnList=listFormBtnById(longValue(po, ID));
        appendSubmitResetBtn(model, sb, formBtnList);
        // 总的表单内容
        model.addAttribute("formContent", sb.toString());
        return boolMap;
    }

    private Set<String> formWumaTemplate(Model model, Map<String, Object> po, String[] columnArray, String[] headers,
                                         List<Map<String, Object>> customType){
        Set<String> boolMap=new HashSet<>();
        JSONObject vo=new JSONObject();
        String labelPo=label(po);
        vo.put("poId", labelPo);
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");

        List<Map<String, Object>> validateList=objectService.getBy(vo, "FieldValidate");
        Map<String, Map<String, Object>> mapFieldInfo=mapFieldInfo(fieldInfoList);

        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item\">");
        Map<String, String> columnShow=new HashMap<>();

        if(validateList!=null&&!validateList.isEmpty()){
            Map<String, Map<String, Object>> fieldValidateMap=new HashMap<>(validateList.size());
            for(Map<String, Object> fi : validateList){
                Object object=fi.get("field");
                object=object==null ? fi.get("id") : object;
                Object validator=fi.get(COLUMN_VALIDATOR);
                JSONObject query=new JSONObject();
                query.put(CODE, validator);
                String[] columString={COLUMN_VALIDATOR};
                List<Map<String, Object>> by=objectService.getColumnsBy(query, "InputValidate", columString);
                if(by!=null&&!by.isEmpty()){
                    fi.putAll(by.get(0));
                }

                fieldValidateMap.put(String.valueOf(object), fi);
            }
            fieldHandleWithStatus(columnArray, headers, mapFieldInfo, fieldValidateMap, columnShow, customType, labelPo);
        }else{
            fieldWithStatusHandle(columnArray, headers, mapFieldInfo, columnShow, customType, labelPo);
        }

        isUseDate(columnArray, boolMap, mapFieldInfo);

        Set<String> layUseInfo=showCustomField(model, columnShow, customType, null, columnArray.length);
        boolMap.addAll(layUseInfo);
        sb.append("</div>");
        // appendSubmitResetBtn(sb);
        List<Map<String, Object>> formBtnList=listFormBtnById(id(po));
        appendSubmitResetBtn(model, sb, formBtnList);
        // 总的表单内容
        model.addAttribute("formContent", sb.toString());
        return boolMap;
    }

    /**
     * 获取字段模板信息
     *
     * @param fieldInfoList
     * @return
     */
    private Map<String, Map<String, Object>> mapFieldInfo(List<Map<String, Object>> fieldInfoList){
        Map<String, Map<String, Object>> customFieldTempateMap=new HashMap<>(fieldInfoList.size());
        for(Map<String, Object> fi : fieldInfoList){
            Object object=fi.get("field");
            object=object==null ? fi.get("id") : object;
            String query="match (n:Field)-[r:template]->(m:LayuiTemplate) where id(n)="+fi.get("id")
                    +"  return m.templateId,m.content";
            List<Map<String, Object>> query2=neo4jService.cypher(query);
            for(Map<String, Object> templat : query2){
                fi.put(TABLE_TEMPLATE_ID, templat.get(TABLE_TEMPLATE_ID));
                fi.put(TABLE_TEMPLATE_CONTENT, templat.get("content"));
            }
            customFieldTempateMap.put(String.valueOf(object), fi);
        }
        return customFieldTempateMap;
    }

    private void documentField(Model model, Map<String, Object> po, String[] columnArray, String[] headers){
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item\">");
        fieldDisableHandle(columnArray, headers, sb);
        sb.append("</div>");
        // 总的表单内容
        model.addAttribute("formContent", sb.toString());
    }

    /**
     * 无表单数据
     *
     * @param model
     * @param po
     * @param columnArray
     * @param headers
     * @return
     */
    private Set<String> formField1(Model model, Map<String, Object> po, String[] columnArray, String[] headers){
        Set<String> boolMap=new HashSet<>();
        JSONObject vo=new JSONObject();
        String labelPo=label(po);
        vo.put("poId", labelPo);
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(vo, "Field");
        List<Map<String, Object>> validateList=objectService.getBy(vo, "FieldValidate");
        Map<String, Map<String, Object>> customFieldMap=new HashMap<>(fieldInfoList.size());
        for(Map<String, Object> fi : fieldInfoList){
            Object object=fi.get("field");
            object=object==null ? fi.get("id") : object;
            customFieldMap.put(String.valueOf(object), fi);
        }
        List<Map<String, Object>> customType=new ArrayList<>();
        boolean hasDateField=false;
        if(validateList!=null&&!validateList.isEmpty()){
            Map<String, Map<String, Object>> fieldValidateMap=new HashMap<>(validateList.size());
            for(Map<String, Object> fi : validateList){
                Object object=fi.get("field");
                object=object==null ? fi.get("id") : object;
                Object validator=fi.get(COLUMN_VALIDATOR);
                JSONObject query=new JSONObject();
                query.put(CODE, validator);
                String[] columString={COLUMN_VALIDATOR};
                List<Map<String, Object>> by=objectService.getColumnsBy(query, "InputValidate", columString);
                if(by!=null&&!by.isEmpty()){
                    fi.putAll(by.get(0));
                }

                fieldValidateMap.put(String.valueOf(object), fi);
            }
            hasDateField=fieldHandle1(columnArray, headers, customFieldMap, fieldValidateMap, customType);
        }else{
            hasDateField=fieldHandle1(columnArray, headers, customFieldMap, customType);
        }

        boolMap.add("laydate");
        Set<String> layUseInfo=customFieldHandle1(model, customType, null);
        boolMap.addAll(layUseInfo);
        return boolMap;
    }

    /**
     * 字段表单
     *
     * @param model
     * @param po
     * @param columnArray
     * @param headers
     * @return
     */
    private Set<String> formField2(Model model, Map<String, Object> po, String[] columnArray, String[] headers,
                                   List<Map<String, Object>> selectOptions){
        String labelPo=label(po);
        List<Map<String, Object>> fieldInfoList=objectService.getFieldInfo(labelPo);
        Map<String, Map<String, Object>> customFieldMap=new HashMap<>(fieldInfoList.size());
        String key="field";
        for(Map<String, Object> fi : fieldInfoList){
            Object object=fi.get(key);// 获取字段
            object=object==null ? fi.get("id") : object;
            if(object!=null){
                customFieldMap.put(String.valueOf(object), fi);
            }
        }
        fieldSelect(columnArray, headers, customFieldMap, key);
        fieldSelect(columnArray, headers, customFieldMap, value_field);

        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item\">");
        Map<String, String> columnShow=new HashMap<>();
        List<Map<String, Object>> customType=new ArrayList<>();
        boolean hasDateField=fieldFormHandle(columnArray, headers, customFieldMap, columnShow, customType);
//	addPo(sb,headers.length-customType.size());
        Set<String> customFieldHandle=showCustomField(model, columnShow, customType, selectOptions, columnArray.length);


        if(hasDateField){
            customFieldHandle.add("laydate");
        }
        formHtml(columnArray, headers, sb, columnShow);
        sb.append("</div>");

        appendSubmitResetBtn(sb);
        // 总的表单内容
        model.addAttribute("formContent", sb.toString());
        return customFieldHandle;
    }


    private void appendSubmitResetBtn(StringBuilder sb){
        sb.append("<div class=\"layui-form-item\">");
        sb.append("<div class=\"layui-input-block\">");
        sb.append("  <button class=\"layui-btn\" lay-submit lay-filter=\"submit-form\">提交</button>");
        sb.append("  <button type=\"reset\" class=\"layui-btn layui-btn-primary\">重置</button>");
        sb.append("</div>");
        sb.append(" </div>");
    }

    private void appendSubmitResetBtn(Model model, StringBuilder sb, List<Map<String, Object>> customBtn){
        StringBuilder opt=new StringBuilder();
        StringBuilder toolFun=new StringBuilder();
        StringBuilder activLogic=new StringBuilder();
        String label=(String) model.getAttribute(LABEL);
        sb.append("<div class=\"layui-form-item\">");
        sb.append("<div class=\"layui-input-block\">");
        sb.append("  <button class=\"layui-btn\" id=\"save"+label+"\" >保存数据</button>");
        sb.append("  <button type=\"reset\" class=\"layui-btn layui-btn-primary\">重置</button>");
        sb.append("</div>");
        sb.append(" </div>");
        for(Map<String, Object> btni : customBtn){
            btnOnclick(btni);
            addBtn(opt, toolFun, activLogic, btni);
        }
        model.addAttribute("opt", opt.toString());

        model.addAttribute("activLogic", activLogic.toString());
        model.addAttribute("formFun", toolFun.toString());
    }

    private void appendFormBtn(Model model, StringBuilder sb, List<Map<String, Object>> customBtn){
        StringBuilder opt=new StringBuilder();
        StringBuilder toolFun=new StringBuilder();
        StringBuilder activLogic=new StringBuilder();
        String label=(String) model.getAttribute(LABEL);
        sb.append("<div class=\"layui-form-item\">");
        sb.append("<div class=\"layui-input-block\">");
        sb.append("  <button class=\"layui-btn\" id=\"save"+label+"\" >保存数据</button>");
        sb.append("  <button type=\"reset\" class=\"layui-btn layui-btn-primary\">重置</button>");
        sb.append("</div>");
        sb.append(" </div>");

        for(Map<String, Object> btni : customBtn){
            formBtnOnclick(btni);
            btni.put("currentId",model.getAttribute("currentId"));
            addFormOneBtn(opt, toolFun, activLogic, btni);
        }
        model.addAttribute("opt", opt.toString());

        model.addAttribute("formFun", toolFun.toString());
    }

    private void appendBtn(Model model, StringBuilder sb, List<Map<String, Object>> customBtn){
        StringBuilder opt=new StringBuilder();
        StringBuilder toolFun=new StringBuilder();
        StringBuilder activLogic=new StringBuilder();
        for(Map<String, Object> btni : customBtn){
            btnOnclick(btni);
            addFormBtn(opt, toolFun, activLogic, btni);
        }
        sb.append(opt);
        model.addAttribute("activLogic", activLogic.toString());
        model.addAttribute("formFun", toolFun.toString());
    }

    private void appendBtns(Model model, StringBuilder sb, List<Map<String, Object>> customBtn){
        StringBuilder opt=new StringBuilder();
        StringBuilder toolFun=new StringBuilder();
        StringBuilder activLogic=new StringBuilder();

        sb.append("<div class=\"layui-form-item\">");
        sb.append("<div class=\"layui-input-block\">");
        for(Map<String, Object> btni : customBtn){
            btnOnclick(btni);
            addBtn(opt, toolFun, activLogic, btni);
        }
        sb.append(opt);
        sb.append("</div>");
        sb.append(" </div>");
        model.addAttribute("formFun", toolFun.toString());
    }

    /**
     * 添加点击响应方法
     *
     * @param btni
     */
    private void btnOnclick(Map<String, Object> btni){
        String string=stringIgnoreCase(btni, HTML);
        String code=code(btni);
        int indexOf=string.indexOf(" lay-event");

        String head=string.substring(0, indexOf);
        //
        if(!head.contains(" id=")){
            head+="id=\""+code+"\" ";
        }

//	head += " onclick=\"" + code + "Form()\" ";
        String tail=string.substring(indexOf);
//	tail=tail.substring(tail.indexOf("\" >")+1);
        btni.put(HTML, head+tail);
    }

    private void formBtnOnclick(Map<String, Object> btni){
        String string=stringIgnoreCase(btni, HTML);
        String code=code(btni);
        int indexOf=string.indexOf(" lay-event");

        String head=string.substring(0, indexOf);
        //
        if(!head.contains(" id=")){
            head+="id=\""+code+"\" ";
        }

	head += " onclick=\"" + code + "(currentNode)\" ";
        String tail=string.substring(indexOf);
	tail=tail.substring(tail.indexOf("\" >")+1);
        btni.put(HTML, head+tail);
    }

    private void addPo(StringBuilder sb, String code, String name){
        sb.append("<div class=\"layui-input-inline\">");
        sb.append("<input type=\"button\" class=\"layui-btn layui-btn-primary\" id=\"relatePo\" value=\"关联类型\" />");
        sb.append("<label  class=\"layui-form-label\" > "+name+"</label>");
        sb.append("<input name=\"type\" type=\"hidden\" class=\"layui-input\" id=\""+code+"\"  readOnly  />");
        sb.append("</div>");
    }

    private String addPo(String code, String name){
        StringBuilder sb=new StringBuilder("<label  class=\"layui-form-label\" > "+name+"</label><div class=\"layui-input-inline\">");
        sb.append("<input type=\"button\" class=\"layui-btn layui-btn-primary\" id=\"relatePo\" value=\"关联类型\" />");
        sb.append("<input name=\"type\" type=\"hidden\" class=\"layui-input\" id=\""+code+"\"  readOnly  />");
        sb.append("</div>");
        return sb.toString();
    }

    /**
     * 配置字段时，配置为选择当前Po的字段
     *
     * @param columnArray
     * @param headers
     * @param customFieldMap
     * @param key
     */
    private void fieldSelect(String[] columnArray, String[] headers, Map<String, Map<String, Object>> customFieldMap,
                             String key){
        Map<String, Object> map=new HashMap<>();
        map.put("type", "poColumns");
        map.put(show_type, "select");
        map.put(NAME, headers[1]);
        map.put(CODE, columnArray[1]);
        map.put(is_po, false);
        customFieldMap.put(key, map);
    }

    public void selectField(String[] columnArray, String[] headers, Map<String, Map<String, Object>> customFieldMap,
                            String key){
        Map<String, Object> map=new HashMap<>();
        map.put("type", "poColumns");
        map.put(show_type, "select");
        map.put(NAME, "字段");
        map.put(CODE, FIELD);
        map.put(is_po, false);
        customFieldMap.put(key, map);
    }

    /**
     * 静态字段展现
     *
     * @param po
     * @param columnArray
     * @param headers
     * @return
     */
    private String staticFormField(Map<String, Object> po, String[] columnArray, String[] headers,
                                   Map<String, Object> propMap){
        JSONObject fieldQuery=new JSONObject();
        fieldQuery.put("poId", po.get(LABEL));
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.query(fieldQuery, "Field");
        Map<String, Map<String, Object>> customFieldMap=new HashMap<>(fieldInfoList.size());
        for(Map<String, Object> fi : fieldInfoList){
            customFieldMap.put(String.valueOf(fi.get("id")), fi);
        }
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item\">");
        List<Map<String, Object>> customTypeList=new ArrayList<>();
        staticFieldHandle(columnArray, headers, customFieldMap, sb, customTypeList, propMap);

        staticCustomFieldHandle(sb, customTypeList);
        // 总的表单内容
        return sb.toString();
    }

    /**
     * 静态字段处理
     *
     * @param columnArray
     * @param headers
     * @param fiMap
     * @param sb
     * @param customType
     * @return
     */
    private void staticFieldHandle(String[] columnArray, String[] headers, Map<String, Map<String, Object>> fiMap,
                                   StringBuilder sb, List<Map<String, Object>> customType, Map<String, Object> propMap){
        for(int i=0, k=0; i<headers.length; i++){
            // 收集自定义字段信息
            String code=columnArray[i];
            Map<String, Object> field=fiMap.get(code);
            String name=headers[i];
            Object value=propMap.get(code);
            if(field!=null&&!field.isEmpty()){
                Map<String, Object> typeiMap=new HashMap<>();
                String type=string(field, "type");
                typeiMap.put("type", type);
                typeiMap.put(NAME, name);
                typeiMap.put(CODE, code);
                typeiMap.put(is_po, string(field, is_po));
                defaultValue(field, typeiMap);
                String showType=getShowType(field);
                typeiMap.put(show_type, showType);
                typeiMap.put("value", String.valueOf(value));
                customType.add(typeiMap);
            }else{
                // 默认字段处理
                if(k>1&&k%3==0){
                    sb.append("</div><div  class=\"layui-form-item\">");
                }

                sb.append(layFormItem(code, name, value, null));
                k++;
            }
        }

        sb.append("</div>");
    }

    /**
     * 处理静态字段
     *
     * @param sb
     * @param customType
     */
    private void staticCustomFieldHandle(StringBuilder sb, List<Map<String, Object>> customType){
        for(Map<String, Object> ctypei : customType){
            String isHide=string(ctypei, "isHide");
            if(isHide!=null&&isHide.equals("on")){
                continue;
            }
            if(Boolean.valueOf(string(ctypei, is_po))){
                staticHandlePoType(ctypei, sb);
            }else{
                staticHandleShowType(ctypei, sb);
            }
        }
    }

    /**
     * render自定义字段Layui风格
     *
     * @param model
     * @param customType
     */
    private Set<String> showCustomField(Model model, Map<String, String> columnShow, List<Map<String, Object>> customType,
                                        List<Map<String, Object>> selectOptions, int total){
        Set<String> fieldUseJsMap=new HashSet<>();
        StringBuilder js=new StringBuilder();
        StringBuilder switchJs=new StringBuilder();
        StringBuilder textEditorJs=new StringBuilder();
        StringBuilder searchHtml=new StringBuilder();
        StringBuilder searchValueJs=new StringBuilder();
        StringBuilder formVerifyJs=new StringBuilder();
        searchValueJs.append("var searchForm={}\n");
        boolean statusInSearch=false;
        Set<String> codeAttList=new HashSet<>();
        String label=String.valueOf(model.getAttribute("label"));
        for(Map<String, Object> ctypei : customType){
            String fieldType=string(ctypei, "type");
            String name=name(ctypei);
            String code=code(ctypei);
            if(code.equals("status")){
                statusInSearch=true;
            }
            String showType=string(ctypei, show_type);

            String isSearchInput=string(ctypei, is_search_input);

            String isHide=string(ctypei, "isHide");
            if(isHide!=null&&isHide.equals("true")){
                continue;
            }
            String fieldString="";
            String searchField="";
            boolean showTypeNotEmpty=showType!=null&&!"".equals(showType);
            if(showTypeNotEmpty){
                confirmUseLayInfo(fieldUseJsMap, textEditorJs, codeAttList, code, showType);
                if(showType.equalsIgnoreCase("textEditor")){
                    formVerify(formVerifyJs, code);
                }
                if(fieldType!=null&&!"".equals(fieldType)&&!"null".equals(fieldType)){
                    if(fieldType.equals("poColumns")){
                        if("select".equalsIgnoreCase(showType)){
                            fieldString=poSelectType(ctypei, selectOptions);
                        }
                    }else{
                        fieldString=handlePoType(ctypei, switchJs, label);
                    }
                }else{
                    fieldString=handleShowType(ctypei, js, switchJs);
                    searchField=handleSearchShowType(ctypei, js, switchJs);
                }
            }

            if(StringUtils.isNotBlank(isSearchInput)&&"on".equalsIgnoreCase(isSearchInput)){
                searchInput(searchValueJs, code);
                if("status".equals(code)){
                    searchHtml.append(statusFieldSelect(label, code+"Reload", name));
                }else{
                    if(StringUtils.isBlank(fieldString)){
                        searchHtml.append(layFormItem(code+"Reload", name));
                    }else{
                        if(StringUtils.isNotBlank(searchField)){
                            searchHtml.append(searchField);
                        }else{
                            searchOn(switchJs, searchHtml, code, fieldString);
                        }
                    }
                }
            }

            if(showTypeNotEmpty&&!StringUtils.isBlank(fieldString)){
                columnShow.put(code, fieldString);
            }
        }

        js.append(switchJs.toString());
        js.append("\n});");
        String columns=String.valueOf(model.getAttribute(COLUMNS));


        if(!statusInSearch){
            searchInput(searchValueJs, "status");
            if(columns.indexOf(",status")>0){
                searchHtml.append(statusFieldSelect(label, "statusReload", "状态"));
            }
        }

        if(searchHtml.indexOf("nameReload")<1){
            String show=String.valueOf(model.getAttribute("show"));
            String header=String.valueOf(model.getAttribute(HEADER));
            String[] hi=header.split(",");
            String[] ci=columns.split(",");
            Map<String, String> chMap=new HashMap<>();

            for(int i=0; i<ci.length; i++){
                chMap.put(ci[i], hi[i]);
            }
            if(show!=null&&!"null".equals(show)&&show.length()>0){
                for(String si : show.split(",")){
                    if(chMap.containsKey(si)){
                        searchHtml.append(layFormItem(si+"Reload", chMap.get(si)));
                        searchInput(searchValueJs, si);
                    }
                }
            }else{
                searchHtml.append(layFormItem(KEY_WORD, "关键字"));
                searchKeyWord(searchValueJs, KEY_WORD);
            }
        }
        model.addAttribute("searcForm", searchHtml.toString());
        model.addAttribute("getSearchValue", searchValueJs.toString());
        model.addAttribute("renderSearchForm", "\nform.render('select');\n");

        modelCodeAtt(model, codeAttList);
        model.addAttribute("layField", js.toString());
        if(textEditorJs.length()>0){
            model.addAttribute("textEditorValue", textEditorJs.toString());
            model.addAttribute(FORM_VERIFY_JS, formVerifyJs.toString());
            model.addAttribute(EDIT_INDEX, "var "+EDIT_INDEX+"={};");
        }
        return fieldUseJsMap;
    }


    public void searchField(StringBuilder searchHtml, StringBuilder searchValueJs, String columns,
                            Map<String, String> chMap, String field){
        if(columns.indexOf(field)>=0){
            searchHtml.append(layFormItem(field+"Reload", chMap.get(field)));
            searchInput(searchValueJs, field);
        }
    }

    private Set<String> wmShowCustomField(Model model, Map<String, String> columnShow, List<Map<String, Object>> customType,
                                          List<Map<String, Object>> selectOptions, String[] columnArray, String[] headers){
        Set<String> fieldUseJsMap=new HashSet<>();
        StringBuilder js=new StringBuilder();
        StringBuilder switchJs=new StringBuilder();
        StringBuilder textEditorJs=new StringBuilder();
        StringBuilder searchHtml=new StringBuilder();
        StringBuilder searchValueJs=new StringBuilder();
        StringBuilder formVerifyJs=new StringBuilder();
        searchValueJs.append("var searchForm={}\n");
        int k=columnArray.length-customType.size();
        boolean statusInSearch=false;
        Set<String> codeAttList=new HashSet<>();
        String label=String.valueOf(model.getAttribute("label"));
        for(Map<String, Object> ctypei : customType){
            String fieldType=string(ctypei, "type");
            String name=name(ctypei);
            String code=code(ctypei);
            if(code.equals("status")){
                statusInSearch=true;
            }
            String showType=string(ctypei, show_type);
            String isSearchInput=string(ctypei, is_search_input);

            String isHide=string(ctypei, "isHide");
            if(isHide!=null&&isHide.equals("true")){
                continue;
            }
            String fieldString="";
            String searchField="";
            boolean showTypeNotEmpty=showType!=null&&!"".equals(showType);
            if(showTypeNotEmpty){
                confirmUseLayInfo(fieldUseJsMap, textEditorJs, codeAttList, code, showType);
                if(showType.equalsIgnoreCase("textEditor")){
                    formVerify(formVerifyJs, code);
                }
                if(fieldType!=null&&fieldType.equals("poColumns")){
                    if("select".equalsIgnoreCase(showType)){
                        fieldString=poSelectType(ctypei, selectOptions);
                    }
                }else if(Boolean.valueOf(string(ctypei, is_po))){
                    fieldString=handlePoType(ctypei, switchJs, label);
                }else{
                    fieldString=handleShowType(ctypei, js, switchJs);
                    searchField=handleSearchShowType(ctypei, js, switchJs);
                }
            }

            if(StringUtils.isNotBlank(isSearchInput)&&"on".equalsIgnoreCase(isSearchInput)){
                searchInput(searchValueJs, code);
                if("status".equals(code)){
                    searchHtml.append(statusFieldSelect(label, code+"Reload", name));
                }else{
                    if(StringUtils.isBlank(fieldString)){
                        searchHtml.append(layFormItem(code+"Reload", name));
                    }else{
                        if(StringUtils.isNotBlank(searchField)){
                            searchHtml.append(searchField);
                        }else{
                            searchOn(switchJs, searchHtml, code, fieldString);
                        }
                    }
                }
            }

            if(showTypeNotEmpty&&!StringUtils.isBlank(fieldString)){
                columnShow.put(code, fieldString);
            }
        }

        js.append(switchJs.toString());
        js.append("\n});");

        if(!statusInSearch){
            searchInput(searchValueJs, "status");
            String attribute=String.valueOf(model.getAttribute(COLUMNS));
            if(attribute.indexOf(",status")>0){
                searchHtml.append(statusFieldSelect(label, "statusReload", "状态"));
            }
        }

        if(searchHtml.isEmpty()){// 如果搜索为空，则加一个默认的名称
            searchHtml.append(layFormItem(KEY_WORD, "关键字"));
            searchKeyWord(searchValueJs);
        }
        model.addAttribute("searcForm", searchHtml.toString());
        model.addAttribute("getSearchValue", searchValueJs.toString());
        model.addAttribute("renderSearchForm", "\nform.render('select');\n");

        modelCodeAtt(model, codeAttList);
        model.addAttribute("layField", js.toString());
        if(textEditorJs.length()>0){
            model.addAttribute("textEditorValue", textEditorJs.toString());
            model.addAttribute(FORM_VERIFY_JS, formVerifyJs.toString());
            model.addAttribute(EDIT_INDEX, "var "+EDIT_INDEX+"={};");
        }
        return fieldUseJsMap;
    }

    private void searchOn(StringBuilder switchJs, StringBuilder searchHtml, String code, String fieldString){
        String search=fieldString.replaceAll("\""+code+"\"", "\""+code+"Reload\"");
        search=search.replaceAll("required", "");
        searchHtml.append(search);
        if(!switchJs.isEmpty()&&switchJs.toString().indexOf(code+"ClickFun")>0){
            switchJs.append("layui.$('#"+code+"Reload').on('click',"+code+"ClickFun);");
        }
    }

    private void confirmUseDateTime(Set<String> fieldUseJsMap, String showType){
        if("datetime".equalsIgnoreCase(showType)||"date".equalsIgnoreCase(showType)
                ||"time".equalsIgnoreCase(showType)){
            fieldUseJsMap.add("laydate");
        }
    }

    private void confirmUseColorPicker(Set<String> fieldUseJsMap, String showType){
        if("colorPicker".equalsIgnoreCase(showType)||showType.startsWith("colorPicker")){
            fieldUseJsMap.add("colorpicker");
        }
    }

    private void comfirmUse(Set<String> fieldUseJsMap, String showType, String layPlugin){
        if(layPlugin.equalsIgnoreCase(showType)||showType.startsWith(layPlugin)){
            fieldUseJsMap.add(layPlugin);
        }
    }

    private void confirmUsedFile(Set<String> fieldUseJsMap, String showType){
        String lowerCase=showType.toLowerCase();
        if("fileUpload".equalsIgnoreCase(showType)||lowerCase.contains("image")||lowerCase.contains("file")
                ||lowerCase.contains("upload")){
            fieldUseJsMap.add("upload");
        }
    }

    private Set<String> customFormField(Model model, Map<String, String> columnShow, List<Map<String, Object>> customType,
                                        List<Map<String, Object>> selectOptions, int total){
        Set<String> fieldUseJsMap=new HashSet<>();
        StringBuilder js=new StringBuilder();
        StringBuilder switchJs=new StringBuilder();
        StringBuilder textEditorJs=new StringBuilder();
        int k=total-customType.size();
        Set<String> codeAttList=new HashSet<>();
        String label=String.valueOf(model.getAttribute("label"));
        for(Map<String, Object> ctypei : customType){
            String fieldType=string(ctypei, "type");
            String name=name(ctypei);
            String code=code(ctypei);
            String showType=string(ctypei, show_type);

            String isHide=string(ctypei, "isHide");
            if(isHide!=null&&isHide.equals("true")){
                continue;
            }
            String fieldString="";
            confirmUseLayInfo(fieldUseJsMap, textEditorJs, codeAttList, code, showType);

            if(fieldType!=null&&fieldType.equals("poColumns")){
                if("select".equalsIgnoreCase(showType)){
                    fieldString=poSelectType(ctypei, selectOptions);
                }
            }else if(Boolean.valueOf(string(ctypei, is_po))){
                fieldString=handlePoType(ctypei, switchJs, label);
            }else{
                fieldString=handleShowType(ctypei, js, switchJs);
            }
            columnShow.put(code, fieldString);
        }

        js.append(switchJs.toString());
        js.append("\n});");

        modelCodeAtt(model, codeAttList);
        model.addAttribute("layField", js.toString());
        if(textEditorJs.length()>0){
            model.addAttribute("textEditorValue", textEditorJs.toString());
        }
        return fieldUseJsMap;
    }

    /**
     * 判断lay模块使用情况
     *
     * @param fieldUseJsMap
     * @param textEditorJs
     * @param codeAttList
     * @param code
     * @param showType
     */
    private void confirmUseLayInfo(Set<String> fieldUseJsMap, StringBuilder textEditorJs, Set<String> codeAttList,
                                   String code, String showType){
        if(null==showType||"".equals(showType)){
            return;
        }
        confirmUseDateTime(fieldUseJsMap, showType);
        confirmUseColorPicker(fieldUseJsMap, showType);
        comfirmUse(fieldUseJsMap, showType, "slider");
        comfirmUse(fieldUseJsMap, showType, "rate");
        comfirmUse(fieldUseJsMap, showType, "iconPicker");
        confirmUsedFile(fieldUseJsMap, showType);
        confirmeTextEditor(fieldUseJsMap, textEditorJs, code, showType);
        confirmUseCode(fieldUseJsMap, codeAttList, code, showType);
    }

    private void confirmUseCode(Set<String> fieldUseJsMap, Set<String> codeAttList, String code, String showType){
        if("javaCode".equalsIgnoreCase(showType)||"htmlCode".equalsIgnoreCase(showType)
                ||"c++Code".equalsIgnoreCase(showType)||"javaScriptCode".equalsIgnoreCase(showType)){
            fieldUseJsMap.add(CODE);
            codeAttList.add(code);
        }
    }

    private void confirmeTextEditor(Set<String> fieldUseJsMap, StringBuilder textEditorJs, String code,
                                    String showType){
        if("textEditor".equalsIgnoreCase(showType)){
            fieldUseJsMap.add("layedit");
            String options="""
                    {uploadImage: {url: '"+LemodoApplication.MODULE_NAME+"/file/uploadImage',type:'post'}	
                    , devmode: true
                           //插入代码设置
                           , codeConfig: {
                               hide: true,  //是否显示编码语言选择框
                               default: 'javascript' //hide为true时的默认语言格式
                           }
                           , tool: [
                               'html', 'code', 'strong','italic',  'underline', 'del', 'addhr', '|', 'fontFomatt', 'colorpicker', 'face'
                               , '|', 'left', 'center', 'right', '|', 'link', 'unlink','images', 'image_alt',
                               , '|', 'fullScreen'
                           ]
                           ,height: 500, //设置编辑器高度
                           }
                          
                        		""";
            textEditorJs.append("\n if(data."+code+"!=undefined){"+"$('#"+code+"').val(data."+code+");"
                    +" layedit.set("+options+");\n"

                    +" layedit.build('"+code+"');}");
        }
    }

    private void modelCodeAtt(Model model, Set<String> codeAttList){
        if(!codeAttList.isEmpty()){
            StringBuilder codeSb=new StringBuilder();
            StringBuilder codeInit=new StringBuilder();
            for(String codei : codeAttList){
                codeSb.append(" $('#"+codei+"').html(rowi['"+codei+"'])");
                codeInit.append(" $('#"+codei+"').html(data['"+codei+"'])");
            }
            model.addAttribute("codeSet", codeSb.toString());
            model.addAttribute("codeInit", codeInit.toString());
        }
    }

    /**
     * 无表单数据
     *
     * @param model
     * @param customType
     * @param selectOptions
     * @return
     */
    private Set<String> customFieldHandle1(Model model, List<Map<String, Object>> customType,
                                           List<Map<String, Object>> selectOptions){
        Set<String> fieldUseJsMap=new HashSet<>();
        StringBuilder js=new StringBuilder();
        StringBuilder switchJs=new StringBuilder();
        StringBuilder searchHtml=new StringBuilder();
        StringBuilder searchValueJs=new StringBuilder();
        searchValueJs.append("var searchForm={}\n");
        Set<String> codeAttList=new HashSet<>();
        int k=0;
        for(Map<String, Object> ctypei : customType){
            String fieldType=string(ctypei, "type");
            String name=name(ctypei);
            String code=code(ctypei);
            String showType=string(ctypei, show_type);

            String isSearchInput=string(ctypei, is_search_input);
            String fieldString="";
            String searchField="";
            if(fieldType.equals("poColumns")){
                if("select".equalsIgnoreCase(showType)){
                    fieldString=poSelectType(ctypei, selectOptions);
                }
            }else if(Boolean.valueOf(string(ctypei, is_po))){
                String label=String.valueOf(model.getAttribute("label"));
                fieldString=handlePoType(ctypei, switchJs, label);
            }else{
                if("javaCode".equalsIgnoreCase(showType)){
                    fieldUseJsMap.add(CODE);
                    codeAttList.add(code);
                }
                fieldString=handleShowType(ctypei, js, switchJs);
                searchField=handleSearchShowType(ctypei, js, switchJs);
            }
            if(StringUtils.isNotBlank(isSearchInput)&&"on".equalsIgnoreCase(isSearchInput)){
                searchInput(searchValueJs, code);
                if(StringUtils.isBlank(fieldString)){
                    fieldString=layFormItem(code, name);
                    searchHtml.append(layFormItem(code, name));
                }else{
                    if(StringUtils.isNotBlank(searchField)){
                        searchHtml.append(searchField);
                    }else{
                        String search=fieldString.replaceAll(code, code+"Reload");
                        searchHtml.append(search);
                    }
                }
            }

        }

        js.append(switchJs.toString());
        js.append("});");
        if(searchHtml.isEmpty()){// 如果搜索为空，则加一个默认的名称
            searchHtml.append(layFormItem(KEY_WORD, "关键字"));
            searchKeyWord(searchValueJs);
        }
        model.addAttribute("searcForm", searchHtml.toString());
        model.addAttribute("getSearchValue", searchValueJs.toString());
        modelCodeAtt(model, codeAttList);
        model.addAttribute("layField", js.toString());

        return fieldUseJsMap;
    }

    private String getShowType(Map<String, Object> ctypei){
        String showType=string(ctypei, show_type);

        if(CommonUtil.isNumber(showType)){
            Map<String, Object> nodePropertiesById=driver.getNodePropertiesById(Long.parseLong(showType));
            showType=String.valueOf(nodePropertiesById.get(CODE));
        }
        return showType;
    }


    /**
     * 字段定义表单处理
     *
     * @param columnArray
     * @param headers
     * @param customFieldMap
     * @param customType
     * @return
     */
    private boolean fieldFormHandle(String[] columnArray, String[] headers,
                                    Map<String, Map<String, Object>> customFieldMap, Map<String, String> columnShow, List<Map<String, Object>> customType){
        boolean hasDateField=false;
        for(int i=0; i<headers.length; i++){
            // 收集自定义字段信息
            String coli=columnArray[i];
            Map<String, Object> customField=customFieldMap.get(coli);
            String hi=headers[i];
            if(customField!=null&&!customField.isEmpty()){
                Map<String, Object> typeiMap=new HashMap<>();
                String type=String.valueOf(customField.get("type"));
                typeiMap.put("type", type);
                typeiMap.put(NAME, hi);
                typeiMap.put(CODE, coli);
                defaultValue(customField, typeiMap);
                typeiMap.put(is_po, String.valueOf(customField.get(is_po)));
                String showType=getShowType(customField);
                typeiMap.put(show_type, getShowType(customField));
                if("date".equals(showType)){
                    hasDateField=true;
                }
                Object valueField=customField.get(value_field);
                if(valueField!=null&&!"null".equals(valueField)){
                    typeiMap.put(value_field, String.valueOf(valueField));
                }
                customType.add(typeiMap);
            }else{
                String fileHTML=null;
                if(coli.equals("type")){
                    fileHTML=addPo(coli, hi);
                }else{
                    fileHTML=layFormItem(coli, hi);
                }
                columnShow.put(coli, fileHTML);
            }
        }
        return hasDateField;
    }


    /**
     * field默认字段render，自定义字段信息收集
     *
     * @param columnArray
     * @param headers
     * @param fiMap
     * @param customType
     * @return
     */
    private boolean fieldHandle(String[] columnArray, String[] headers, Map<String, Map<String, Object>> fiMap,
                                Map<String, String> columnShow, List<Map<String, Object>> customType){
        boolean hasDateField=false;
        for(int i=0, k=0; i<headers.length; i++){
            // 收集自定义字段信息
            String columni=columnArray[i];
            Map<String, Object> field=fiMap.get(columni);
            String headeri=headers[i];

            if(field!=null&&!field.isEmpty()){
                String showType=string(field, show_type);
                if("date".equals(showType)){
                    hasDateField=true;
                }
                addCustomColumn(customType, columni, field, headeri);
            }else{
                columnShow.put(columni, layFormItem(columni, headeri));
            }
        }
        return hasDateField;
    }


    private void fieldWithStatusHandle(List<Map<String, Object>> settings, Map<String, Map<String, Object>> fiMap,
                                       Map<String, String> columnShow, List<Map<String, Object>> customType, String poLabel){
        for(Map<String, Object> si : settings){
            // 收集自定义字段信息
            String columni=code(si);
            if(columni==null||"".equals(columni.trim())){
                continue;
            }
            Map<String, Object> field=fiMap.get(columni);
            String value= value(si);
            String headeri=name(si);

            if(value!=null){
                if(field!=null&&!field.isEmpty()){
                    String showType=string(field, show_type);

                    if(showType!=null&&!showType.isEmpty()){
                        addCustomColumn(customType, columni, field, headeri);
                        if(columni.equals("status")){
                            columnShow.put(columni, statusFieldSelect(poLabel, columni, headeri,value));
                        }
                    }else if(!string(field, is_search_input).isEmpty()){
                        addCustomColumn(customType, columni, field, headeri);
                        if("".equals(showType)||null==showType){
                            columnShow.put(columni, defaultValueColumn(poLabel, columni, headeri,value));
                        }
                    }
                }else{
                    columnShow.put(columni, defaultValueColumn(poLabel, columni, headeri,value));
                }
            }else{
                if(field!=null&&!field.isEmpty()){
                    String showType=string(field, show_type);

                    if(showType!=null&&!showType.isEmpty()){
                        addCustomColumn(customType, columni, field, headeri);
                        if(columni.equals("status")){
                            columnShow.put(columni, statusFieldSelect(poLabel, columni, headeri));
                        }
                    }else if(!string(field, is_search_input).isEmpty()){
                        addCustomColumn(customType, columni, field, headeri);
                        if("".equals(showType)||null==showType){
                            columnShow.put(columni, defaultColumn(poLabel, columni, headeri));
                        }
                    }
                }else{
                    columnShow.put(columni, defaultColumn(poLabel, columni, headeri));
                }
            }

        }
    }

    private void fieldWithStatusHandle(String[] columnArray, String[] headers, Map<String, Map<String, Object>> fiMap,
                                       Map<String, String> columnShow, List<Map<String, Object>> customType, String poLabel){
        for(int i=0; i<columnArray.length; i++){
            // 收集自定义字段信息
            String columni=columnArray[i];
            if(columni==null||"".equals(columni.trim())){
                continue;
            }
            Map<String, Object> field=fiMap.get(columni);

            String headeri=headers[i];

            if(field!=null&&!field.isEmpty()){
                String showType=string(field, show_type);

                if(showType!=null&&!showType.isEmpty()){
                    addCustomColumn(customType, columni, field, headeri);
                    if(columni.equals("status")){
                        columnShow.put(columni, statusFieldSelect(poLabel, columni, headeri));
                    }
                }else if(!string(field, is_search_input).isEmpty()){
                    addCustomColumn(customType, columni, field, headeri);
                    if("".equals(showType)||null==showType){
                        columnShow.put(columni, defaultColumn(poLabel, columni, headeri));
                    }
                }
            }else{
                columnShow.put(columni, defaultColumn(poLabel, columni, headeri));
            }

        }
    }


    /**
     * 默认字段处理
     *
     * @param poLabel
     * @param columni
     * @param headeri
     * @return
     */
    private String defaultColumn(String poLabel, String columni, String headeri){
        if(columni.equals("status")){
            return statusFieldSelect(poLabel, columni, headeri);
        }else{
            return layFormItem(columni, headeri);
        }
    }

    private String defaultValueColumn(String poLabel, String columni, String headeri,String value){
        if(columni.equals("status")){
            return statusFieldSelect(poLabel, columni, headeri,value);
        }else{
            return formItemValue(columni, headeri,value);
        }
    }

    private void addCustomColumn(List<Map<String, Object>> customType, String columni, Map<String, Object> field,
                                 String headeri){
        Map<String, Object> typeiMap=new HashMap<>();
        String type=string(field, "type");
        typeiMap.put("type", type);
        typeiMap.put(NAME, headeri);
        typeiMap.put(CODE, columni);
        typeiMap.put(is_po, string(field, is_po));
        defaultValue(field, typeiMap);

        typeiMap.put(value_field, string(field, value_field));
        typeiMap.put(COLUMN_VALIDATOR, string(field, COLUMN_VALIDATOR));

        String showType=getShowType(field);
        typeiMap.put(show_type, showType);
        copyextendsProp(field, typeiMap);

        switchOnHandle(field, typeiMap);
        copyTemplateInfo(field, typeiMap);

        customType.add(typeiMap);
    }


    public void copyextendsProp(Map<String, Object> field, Map<String, Object> typeiMap){
        copyNonNull(is_search_input, field, typeiMap);
        copyNonNull("formateQuery", field, typeiMap);
        copyNonNull("readOnly", field, typeiMap);
    }


    public void copyNonNull(String fi, Map<String, Object> field, Map<String, Object> typeiMap){
        Object object=field.get(fi);
        if(null!=object){
            typeiMap.put(fi, String.valueOf(object));
        }
    }

    /**
     * 状态字段用Select，下拉选
     *
     * @param poLabel
     * @param columni
     * @param headeri
     */
    private String statusFieldSelect(String poLabel, String columni, String headeri){
        String query=Neo4jOptCypher.getStatusList(poLabel);
        List<Map<String, Object>> selectList=neo4jService.cypher(query);
        Map<String, Object> ctypei=new HashMap<>();
        ctypei.put(NAME, headeri);
        ctypei.put(CODE, columni);
        return statusSelect(ctypei, selectList);
    }
    private String statusFieldSelect(String poLabel, String columni, String headeri,String value){
        String query=Neo4jOptCypher.getStatusList(poLabel);
        List<Map<String, Object>> selectList=neo4jService.cypher(query);
        Map<String, Object> ctypei=new HashMap<>();
        ctypei.put(NAME, headeri);
        ctypei.put(CODE, columni);
        ctypei.put(VALUE,value);
        return statusSelect(ctypei, selectList);
    }

    private boolean fieldDisableHandle(String[] columnArray, String[] headers, StringBuilder sb){
        boolean hasDateField=false;
        for(int i=0, k=0; i<headers.length; i++){
            // 默认字段处理
            if(k>1&&k%3==0){
                sb.append("</div><div  class=\"layui-form-item\">");
            }
            sb.append(layReadOnlyFormItem(columnArray[i], headers[i], null));
            k++;
        }
        return hasDateField;
    }

    private boolean fieldHandle1(String[] columnArray, String[] headers, Map<String, Map<String, Object>> fiMap,
                                 List<Map<String, Object>> customType){
        boolean hasDateField=false;
        for(int i=0, k=0; i<headers.length; i++){
            // 收集自定义字段信息
            Map<String, Object> field=fiMap.get(columnArray[i]);
            if(field!=null&&!field.isEmpty()){

                Map<String, Object> typeiMap=new HashMap<>();
                String type=string(field, "type");
                typeiMap.put("type", type);
                typeiMap.put(NAME, headers[i]);
                typeiMap.put(CODE, columnArray[i]);
                typeiMap.put(is_po, string(field, is_po));
                typeiMap.put(value_field, string(field, value_field));
                typeiMap.put(COLUMN_VALIDATOR, string(field, COLUMN_VALIDATOR));
                defaultValue(field, typeiMap);

                String showType=getShowType(field);
                typeiMap.put(show_type, showType);
                copyextendsProp(field, typeiMap);
                switchOnHandle(field, typeiMap);
                copyTemplateInfo(field, typeiMap);

                if("date".equals(showType)){
                    hasDateField=true;
                }
                customType.add(typeiMap);
            }
        }
        return hasDateField;
    }

    private boolean fieldHandle(String[] columnArray, String[] headers, Map<String, Map<String, Object>> fiMap,
                                Map<String, Map<String, Object>> fivMap, Map<String, String> columnShow, List<Map<String, Object>> customType){
        boolean hasDateField=false;
        for(int i=0; i<headers.length; i++){
            // 收集自定义字段信息
            String columni=columnArray[i];
            Map<String, Object> field=fiMap.get(columni);
            Map<String, Object> vfield=fivMap.get(columni);
            String headeri=headers[i];
            if(field!=null&&!field.isEmpty()){
                String type=string(field, "type");
                Map<String, Object> typeiMap=new HashMap<>();
                typeiMap.put("type", type);
                typeiMap.put(NAME, headeri);
                typeiMap.put(CODE, columni);
                typeiMap.put(is_po, string(field, is_po));
                typeiMap.put(value_field, string(field, value_field));
                if(vfield!=null){
                    typeiMap.put(COLUMN_VALIDATOR, String.valueOf(vfield.get(COLUMN_VALIDATOR)));
                }
                defaultValue(field, typeiMap);

                copyTemplateInfo(field, typeiMap);
                String showType=getShowType(field);
                typeiMap.put(show_type, showType);
                copyextendsProp(field, typeiMap);
                switchOnHandle(field, typeiMap);


                if("date".equals(showType)){
                    hasDateField=true;
                }
                customType.add(typeiMap);
            }else{
                columnShow.put(columni, fieldString(columni, vfield, headeri));
            }
        }
        return hasDateField;
    }

    /**
     * 渲染字段，并收集自定义字段信息。对状态字段进行处理。
     *
     * @param columMapField
     * @param validFieldMap
     * @param customType
     * @param poLabel
     * @return
     */
    private void fieldHandleWithStatus(List<Map<String, Object>> settings,
                                       Map<String, Map<String, Object>> columMapField, Map<String, Map<String, Object>> validFieldMap,
                                       Map<String, String> columnShow, List<Map<String, Object>> customType, String poLabel){
        for(Map<String, Object> si : settings){
            // 收集自定义字段信息
            String columni=code(si);
            Map<String, Object> vfield=validFieldMap.get(columni);

            Map<String, Object> fieldInfo=columMapField.get(columni);
            String headeri=name(si);

            if(fieldInfo!=null&&!fieldInfo.isEmpty()){
                Object showTypeobj=fieldInfo.get(show_type);
                if(showTypeobj!=null&&!"".equals(showTypeobj)){
                    addCustomFileInfo(customType, columni, fieldInfo, vfield, headeri);
                }else{
                    fieldHtml(columnShow, poLabel, columni, vfield, headeri);
                }
            }else{
                fieldHtml(columnShow, poLabel, columni, vfield, headeri);
            }
        }
    }

    private void fieldHandleWithStatus(String[] columnArray, String[] headers,
                                       Map<String, Map<String, Object>> columMapField, Map<String, Map<String, Object>> validFieldMap,
                                       Map<String, String> columnShow, List<Map<String, Object>> customType, String poLabel){
        for(int i=0; i<headers.length; i++){
            // 收集自定义字段信息
            String columni=columnArray[i];
            Map<String, Object> vfield=validFieldMap.get(columni);

            Map<String, Object> fieldInfo=columMapField.get(columni);
            String headeri=headers[i];

            if(fieldInfo!=null&&!fieldInfo.isEmpty()){
                Object showTypeobj=fieldInfo.get(show_type);
                if(showTypeobj!=null&&!"".equals(showTypeobj)){
                    addCustomFileInfo(customType, columni, fieldInfo, vfield, headeri);
                }else{
                    fieldHtml(columnShow, poLabel, columni, vfield, headeri);
                }
            }else{
                fieldHtml(columnShow, poLabel, columni, vfield, headeri);
            }
        }
    }


    private void fieldHtml(Map<String, String> columnShow, String poLabel, String columni, Map<String, Object> vfield,
                           String headeri){
        if(columni==null) return;
        String temp=null;
        if(columni.equals("status")){
            temp=statusFieldSelect(poLabel, columni, headeri);
        }else{
            temp=fieldString(columni, vfield, headeri);
        }
        columnShow.put(columni, temp);
    }

    /**
     * 添加自定义字段
     *
     * @param customType
     * @param columni
     * @param fieldInfo
     * @param vfield
     * @param headeri
     */
    private void addCustomFileInfo(List<Map<String, Object>> customType, String columni, Map<String, Object> fieldInfo,
                                   Map<String, Object> vfield, String headeri){
        Map<String, Object> typeiMap=new HashMap<>();
        String type=String.valueOf(fieldInfo.get("type"));
        typeiMap.put("type", type);
        typeiMap.put(NAME, headeri);
        typeiMap.put(CODE, columni);
        typeiMap.put(is_po, String.valueOf(fieldInfo.get(is_po)));
        typeiMap.put(value_field, String.valueOf(fieldInfo.get(value_field)));
        if(vfield!=null){
            typeiMap.put(COLUMN_VALIDATOR, String.valueOf(vfield.get(COLUMN_VALIDATOR)));
        }
        defaultValue(fieldInfo, typeiMap);

        typeiMap.put(show_type, getShowType(fieldInfo));
        copyTemplateInfo(fieldInfo, typeiMap);

        copyextendsProp(fieldInfo, typeiMap);
        switchOnHandle(fieldInfo, typeiMap);

        customType.add(typeiMap);
    }

    /**
     * 表单字段
     *
     * @param columni
     * @param vfield
     * @param headeri
     */
    private String fieldString(String columni, Map<String, Object> vfield, String headeri){
        if(vfield!=null){
            return layFormItem(columni, headeri, String.valueOf(vfield.get(COLUMN_VALIDATOR)));
        }else{
            return layFormItem(columni, headeri, null);
        }
    }

    private String fieldString(String columni, Map<String, Object> vfield, String headeri, String value){
        if(vfield!=null){
            return layFormItem(columni, headeri, value, String.valueOf(vfield.get(COLUMN_VALIDATOR)));
        }else{
            return layFormItem(columni, headeri, value, null);
        }
    }

    private void switchOnHandle(Map<String, Object> field, Map<String, Object> typeiMap){
        handleBooleanColumn(field, typeiMap, "isHide");
        handleBooleanColumn(field, typeiMap, "disabled");
        handleBooleanColumn(field, typeiMap, "readOnly");
    }

    private void handleBooleanColumn(Map<String, Object> field, Map<String, Object> typeiMap, String columnKey){
        if(getBoolean(field, columnKey)){
            typeiMap.put(columnKey, "true");
        }
    }

    /**
     * 复制模板信息
     *
     * @param field
     * @param typeiMap
     */
    private void copyTemplateInfo(Map<String, Object> field, Map<String, Object> typeiMap){
        copyTemplateContent(field, typeiMap, TABLE_TEMPLATE_ID);
        copyTemplateContent(field, typeiMap, TABLE_TEMPLATE_CONTENT);
    }

    private void copyTemplateContent(Map<String, Object> field, Map<String, Object> typeiMap, String key){
        Object templateContent=field.get(key);
        if(templateContent!=null&&!"".equals(templateContent)){// 表格模板
            typeiMap.put(key, String.valueOf(templateContent));
        }
    }

    private boolean fieldHandle1(String[] columnArray, String[] headers, Map<String, Map<String, Object>> fiMap,
                                 Map<String, Map<String, Object>> fivMap, List<Map<String, Object>> customType){
        boolean hasDateField=false;
        for(int i=0, k=0; i<headers.length; i++){
            // 收集自定义字段信息
            Map<String, Object> field=fiMap.get(columnArray[i]);
            Map<String, Object> validateField=fivMap.get(columnArray[i]);
            if(field!=null&&!field.isEmpty()){
                Map<String, Object> typeiMap=new HashMap<>();
                String type=string(field, "type");
                typeiMap.put("type", type);
                typeiMap.put(NAME, headers[i]);
                typeiMap.put(CODE, columnArray[i]);
                typeiMap.put(is_po, string(field, is_po));
                typeiMap.put(value_field, string(field, value_field));
                if(validateField!=null){
                    typeiMap.put(COLUMN_VALIDATOR, String.valueOf(validateField.get(COLUMN_VALIDATOR)));
                }
                defaultValue(field, typeiMap);

                String showType=string(field, show_type);
                typeiMap.put(show_type, showType);
                copyextendsProp(field, typeiMap);
                switchOnHandle(field, typeiMap);
                copyTemplateInfo(field, typeiMap);

                if("date".equals(showType)){
                    hasDateField=true;
                }
                customType.add(typeiMap);
            }
        }
        return hasDateField;
    }

    /**
     * 处理默认数据
     *
     * @param field
     * @param typeiMap
     */
    private void defaultValue(Map<String, Object> field, Map<String, Object> typeiMap){
        if(string(field, FIELD_DEFAULT_VALUE)!=null){
            typeiMap.put(FIELD_DEFAULT_VALUE, string(field, FIELD_DEFAULT_VALUE));
        }
    }

    /**
     * table 列定义
     *
     * @param model
     * @param columnArray
     * @param headers
     */
    private void tableInfo(Model model, String[] columnArray, String[] headers){
        List<Map<String, String>> cols=new ArrayList<>();
        for(int i=0; i<headers.length; i++){
            Map<String, String> piMap=new HashMap<>();
            piMap.put(CODE, "{field:'"+columnArray[i]+"', sort: true}");
            piMap.put(NAME, headers[i]);
            piMap.put("field", columnArray[i]);
            cols.add(piMap);
        }
        model.addAttribute("cols", cols);
        model.addAttribute("colCodes", columnArray);
    }

    private void tableShowInfo(Model model, String[] showArray, String[] columnArray, String[] headers){
        List<Map<String, String>> cols=new ArrayList<>();
        List<String> columns=new ArrayList<>();
        Set<String> showSet=new HashSet<>();
        for(String si : showArray){
            showSet.add(si);
        }


        for(int i=0; i<headers.length; i++){
            if(!showSet.contains(columnArray[i])){
                continue;
            }
            Map<String, String> piMap=new HashMap<>();
            piMap.put(CODE, "{field:'"+columnArray[i]+"', sort: true}");
            piMap.put(NAME, headers[i]);
            piMap.put("field", columnArray[i]);
            cols.add(piMap);
        }
        model.addAttribute("cols", cols);
        model.addAttribute("colCodes", columnArray);
    }

    /**
     * 添加tableList的模板字段
     *
     * @param model
     * @param columnArray
     * @param headers
     */
    private void tableListColumnTemplate(Model model, String[] columnArray, String[] headers,
                                         List<Map<String, Object>> customType){
        Map<String, String> tempalteInfoMap=new HashMap<>();
        Map<String, String> tempalteContentMap=new HashMap<>();

        for(Map<String, Object> ctypei : customType){
            String value=string(ctypei, TABLE_TEMPLATE_ID);
            if(value!=null&&!value.trim().equals("")){
                tempalteInfoMap.put(code(ctypei), value);
                tempalteContentMap.put(code(ctypei), string(ctypei, TABLE_TEMPLATE_CONTENT));
            }
        }
        if(!tempalteContentMap.isEmpty()&&tempalteContentMap.size()>0){
            StringBuffer sBuffer=new StringBuffer();
            for(Entry<String, String> ei : tempalteContentMap.entrySet()){
                // 替换模板中的字段
                String value=ei.getValue();
                if(!ei.getKey().equals(unicode)){
                    value=value.replace("{{d.unicode}}", "{{d."+ei.getKey()+"}}");
                }
                sBuffer.append(value);
            }
            String htmlUnescape=HtmlUtils.htmlUnescape(sBuffer.toString());
            htmlUnescape=htmlUnescape.replaceAll("<p>", "");
            htmlUnescape=htmlUnescape.replaceAll("</p>", "");
            model.addAttribute("tempalteContent", htmlUnescape);
        }else{
            model.addAttribute("tempalteContent", " ");
        }
        List<Map<String, String>> cols=new ArrayList<>();
        List<Map<String, String>> shortCols=new ArrayList<>();
        String shortColumns="id code label name title status memo username createTime updateTime";
        String dont=" desc remark content detail field type size create update modify";

        for(int i=0, k=0; i<headers.length; i++){
            Map<String, String> piMap=new HashMap<>();
            String coli=columnArray[i];
            if(coli==null){
                continue;
            }
            piMap.put(CODE, "{field:'"+coli+"', sort: true}");
            piMap.put(NAME, headers[i]);
            customWidth(piMap, coli);

            piMap.put("field", coli);
            String ciTemplate=tempalteInfoMap.get(coli);
            if(ciTemplate!=null&&!ciTemplate.trim().equals("")){
                piMap.put("templat", ",templet:'#"+ciTemplate+"'");
            }else{
                piMap.put("templat", " ");
            }
            if(containColumn(coli, shortColumns)){
                shortCols.add(piMap);
            }
            cols.add(piMap);
        }
        if(shortCols.size()<=6&&cols.size()>9){
            model.addAttribute("cols", cols.subList(0, 8));
        }else if(shortCols.size()>6&&cols.size()>9){
            model.addAttribute("cols", shortCols);
        }else{
            model.addAttribute("cols", cols);
        }

        model.addAttribute("colCodes", columnArray);
    }

    private void formateFileOrImg(Map<String, Object> map, Map<String, Object> mi, String field, String lowerCase){
        if(lowerCase.contains("image")||lowerCase.contains("file")){
            String idString=string(map, field);

            if(idString!=null&&!idString.trim().isBlank()){
                if(!idString.contains(",")){
                    try{
                        Long.valueOf(idString);
                    }catch(Exception e){
                        return;
                    }
                }
                String[] split=idString.split(",");

                if("on".equals(string(mi, "showImage"))){
                    String downLoadPath=LemodoApplication.MODULE_NAME+"/file/show/";
                    if(split.length>1){
                        StringBuilder sb=new StringBuilder();

                        for(String idi : split){
                            // Map<String, Object> propMapBy = neo4jService.getPropMapBy(idi);
                            // String fileName = name(di);
                            String[] split2=idi.split("/");

                            if(split2.length>0){
                                idString=split2[split2.length-1];
                                sb.append(downLoadPath+idi+",");
                            }else{
                                sb.append(idi+",");
                            }
                        }
                        map.put(field, sb.toString());
                    }else{
                        // Map<String, Object> propMapBy = neo4jService.getPropMapBy(idString);
                        // String fileName = name(di);
                        String[] split2=idString.split("/");

                        if(split2.length>0){
                            idString=split2[split2.length-1];
                        }
                        map.put(field, downLoadPath+idString);
                    }
                }else{
                    if(split.length>1){
                        StringBuilder sb=new StringBuilder();

                        for(String idi : split){
                            Map<String, Object> propMapBy=neo4jService.getPropMapBy(idi);
                            String fileName=name(propMapBy);
                            sb.append("<button id=\"downLoad\" class=\"layui-btn layui-btn-xs\" onclick=\"downLoad("
                                    +idi+")\">"+fileName+"</button>");
                        }
                        map.put(field, sb.toString());
                    }else{
                        Map<String, Object> propMapBy=neo4jService.getPropMapBy(idString);
                        if(propMapBy!=null){
                            String fileName=name(propMapBy);
                            map.put(field,
                                    "<button id=\"downLoad\" class=\"layui-btn layui-btn-xs\" onclick=\"downLoad("
                                            +idString+")\">"+fileName+"</button>");
                        }

                    }
                }

            }else{
                String template="<div>{{ downloadTmplate(d."+field+") }}</div>";
                map.put(field, template);
            }

        }
    }

    private void tableListColumnTemplate2(Model model, String[] columnArray, String[] headers,
                                          List<Map<String, Object>> customType){
        Map<String, Object> tempalteInfoMap=new HashMap<>();
        Map<String, Object> tempalteContentMap=new HashMap<>();

        for(Map<String, Object> ctypei : customType){
            String value=string(ctypei, TABLE_TEMPLATE_ID);
            if(value!=null&&!value.trim().equals("")){
                tempalteInfoMap.put(code(ctypei), value);
                tempalteContentMap.put(code(ctypei), string(ctypei, TABLE_TEMPLATE_CONTENT));
            }
            String showType=string(ctypei, "showType");
            if("fileUpload".equals(showType)){
                formateFileOrImg(tempalteInfoMap, ctypei, code(ctypei), showType.toLowerCase());
            }
        }
        if(!tempalteContentMap.isEmpty()&&tempalteContentMap.size()>0){
            StringBuffer sBuffer=new StringBuffer();
            for(Entry<String, Object> ei : tempalteContentMap.entrySet()){
                // 替换模板中的字段
                String value=String.valueOf(ei.getValue());
                if(!ei.getKey().equals(unicode)){
                    value=value.replace("{{d.unicode}}", "{{d."+ei.getKey()+"}}");
                }
                sBuffer.append(value);
            }
            String htmlUnescape=HtmlUtils.htmlUnescape(sBuffer.toString());
            htmlUnescape=htmlUnescape.replaceAll("<p>", "");
            htmlUnescape=htmlUnescape.replaceAll("</p>", "");
            model.addAttribute("tempalteContent", htmlUnescape);
        }else{
            model.addAttribute("tempalteContent", " ");
        }
        List<Map<String, String>> cols=new ArrayList<>();
        Map<String, Map<String, Object>> xx=new HashMap<>();
        List<String> formats=new ArrayList<>();
        for(Map<String, Object> ci : customType){
            String formatCol=null;
            String formateQuery=string(ci, "formateQuery");

            if("on".equals(formateQuery)){
                formatCol=code(ci)+COLUMN_FORMAT;
                formats.add(formatCol);
            }
            xx.put(code(ci), ci);
        }
        for(int i=0, k=0; i<columnArray.length; i++){
            Map<String, String> piMap=new HashMap<>();
            String column=columnArray[i];
            if(column==null){
                continue;
            }
            String formateQuery=string(xx.get(column), "formateQuery");

            if("on".equals(formateQuery)){
                column=column+COLUMN_FORMAT;
            }
            piMap.put(CODE, "{field:'"+column+"', sort: true}");
            piMap.put(NAME, headers[i]);

            customWidth(piMap, column);

            piMap.put("field", column);
            String string=string(tempalteInfoMap, column);
            if(string!=null&&!string.trim().equals("")){
                if(string.trim().startsWith("function")||string.trim().startsWith("<div>{{")){
                    piMap.put("templat", ",templet: '"+string+"' ");
                }else{
                    piMap.put("templat", ",templet:'#"+string+"'");
                }

            }else{
                piMap.put("templat", " ");
            }

            cols.add(piMap);
        }
        model.addAttribute("cols", cols);
        if(!formats.isEmpty()){
            model.addAttribute(COLUMN_FORMAT, formats.toArray());
        }
        model.addAttribute("colCodes", columnArray);


    }

    private void customWidth(Map<String, String> piMap, String column){
        if(column==null){
            return;
        }
        Boolean find=false;
        String title=" code label name title remark Key columns header ";
        String widthValue="minWidth: '100',maxWidth: '250',";
        widthValue=editableExceptId(column, widthValue);
        find=widthValue(piMap, column, find, title, widthValue);
        if(!find){
            if(column.equals(ID)){
                String widthValue1="width: '60',fixed: 'left',";
                piMap.put("width", widthValue1);
            }else{
                piMap.put("width", "minWidth: 80,maxWidth: 250,");
            }
        }


    }

    private String editableExceptId(String column, String widthValue){
        if(!column.equals(ID)){
            widthValue=widthValue+"edit: 'text',";
        }
        return widthValue;
    }

    private Boolean widthValue(Map<String, String> piMap, String column, Boolean find, String title,
                               String widthValue){
        String[] split=title.trim().split(" ");
        for(String key : split){
            if("".equals(key.trim())){
                continue;
            }
            if(column.toLowerCase().startsWith(key.toLowerCase())){
                piMap.put("width", widthValue);
                find=true;
                break;
            }
        }
        return find;
    }

    private Boolean containColumn(String column, String title){
        Boolean find=false;
        String[] split=title.split(" ");
        for(String key : split){
            if("".equals(key.trim())){
                continue;
            }
            if(column.toLowerCase().contains(key.toLowerCase())){
                find=true;
                break;
            }
        }
        return find;
    }

    /**
     * laymodule使用组装
     *
     * @param model
     * @param useTab
     */
    private void useLayModule(Model model, Boolean useTab, Set<String> boolMap){
        StringBuilder layUse=new StringBuilder();
        layUseJs(layUse, boolMap, useTab);
        model.addAttribute(LAY_USE, layUse.toString());
    }

    /**
     * handle 字段展示类型
     *
     * @param js
     * @param switchJs
     */
    private String handleShowType(Map<String, Object> ctypei, StringBuilder js, StringBuilder switchJs){
        String fieldType=string(ctypei, "type");
        String name=string(ctypei, NAME);
        String code=string(ctypei, CODE);
        String valueField=string(ctypei, value_field);
        String defaultValue=string(ctypei, FIELD_DEFAULT_VALUE);
        String validator=string(ctypei, COLUMN_VALIDATOR);
        String showType=string(ctypei, show_type);

        String fieldString="";
        if(showType==null){
            return layFormItem(code, name, validator);
        }

        Map<String, Object> attMapBy=neo4jService.getAttMapBy(CODE, showType, FIELD_TYPE);
        if(attMapBy!=null){
            String jsCode=string(attMapBy, JS_CODE);
            String html=stringIgnoreCase(attMapBy, HTML);

            String size="10";
            size=string(attMapBy, "FIELDSIZE");

            if(jsCode!=null&&!jsCode.trim().equals("")){
                if(jsCode!=null&&jsCode.contains("{UPLOADURL}")&&size==null){
                    size="5000";
                }
                jsCode=jsCode.replaceAll("\\$\\{FIELDID\\}", code);
                jsCode=jsCode.replaceAll("\\{FIELDID\\}", code);
                if(jsCode.contains("${UPLOADURL}")){
                    jsCode=jsCode.replaceAll("\\$\\{UPLOADURL\\}", LemodoApplication.MODULE_NAME+"/file/upload");
                }
                jsCode=jsCode.replaceAll("\\$\\{FIELDSIZE\\}", size);
                js.append(jsCode);
            }

            if(html!=null){
                html=html.replaceAll("\\$\\{FIELDID\\}", code);
                html=html.replaceAll("\\{FIELDID\\}", code);
                html=updateIcon(attMapBy, html);
                html=html.replaceAll("\\$\\{FIELDNAME\\}", name);
                html=html.replaceAll("\\$\\{FIELDSIZE\\}", size);
                return html;
            }
        }

        if("String".equalsIgnoreCase(showType)){
            if(defaultValue!=null){
                fieldString=layFormItem(code, name, defaultValue, validator);
            }else{
                fieldString=layFormItem(code, name, validator);
            }

        }
        if("radio".equalsIgnoreCase(showType)){
            if(defaultValue!=null){
                fieldString=layFormItem(code, name, defaultValue, validator);
            }else{
                fieldString=layFormItem(code, name, validator);
            }

        }
        if("number".equalsIgnoreCase(showType)){
            if(defaultValue!=null){
                fieldString=layFormNumber(code, name, defaultValue, validator);
            }else{
                fieldString=layFormNumber(code, name, validator);
            }

        }
        if("textarea".equalsIgnoreCase(showType)){
            if(defaultValue!=null){
                fieldString=textArea(name, code, defaultValue, validator, getReadOnly(ctypei));
            }else{
                fieldString=textArea(name, code, validator, getReadOnly(ctypei));
            }
        }
        if("textEditor".equalsIgnoreCase(showType)){
            if(defaultValue!=null){
                fieldString=textArea(name, code, defaultValue, validator, getReadOnly(ctypei));
            }else{
                fieldString=textArea(name, code, validator, getReadOnly(ctypei));
            }
            js.append(" layedit.set({uploadImage: {url: '"+LemodoApplication.MODULE_NAME+"/file/uploadImage',type:'post',success: function(data){ \r\n"
                    +"               console.log(data); \r\n"
                    +"            }}});\n");
            js.append(" var "+code+"Index = layedit.build('"+code+"');\n");
        }
        if("line".equalsIgnoreCase(showType)){
            if(defaultValue!=null){
                fieldString=layFormLine(code, name, defaultValue);
            }else{
                fieldString=layFormLine(code, name, null);
            }
        }

        if("password".equalsIgnoreCase(showType)){
            fieldString=password(name, code, validator);
        }
        if("date".equalsIgnoreCase(showType)){
            if(defaultValue!=null){
                if(defaultValue.equals("now")||defaultValue.toLowerCase().startsWith("current")){
                    String now=now();
                    fieldString=date(name, code, now, validator, getDisabled(ctypei));
                }else{
                    fieldString=date(name, code, defaultValue, validator, getDisabled(ctypei));
                }

            }else{
                fieldString=date(name, code, validator, getDisabled(ctypei));
            }
            js.append(" \n"+" laydate.render({\n"+"	 elem: '#"+code+"'\n"+" });\n");
        }
        if("switch".equalsIgnoreCase(showType)){
            if(defaultValue!=null){
                ctypei.put("value", defaultValue);
            }
            fieldString=switchOn(ctypei, "ON|OFF", switchJs);
        }
        if("datetime".equalsIgnoreCase(showType)){
            if(defaultValue!=null){
                fieldString=dateTime(name, code, defaultValue, validator, getDisabled(ctypei));
            }else{
                fieldString=dateTime(name, code, validator, getDisabled(ctypei));
            }

            js.append(" \n"+" laydate.render({\n"+"	 elem: '#"+code+"',type: 'datetime'\n"+" });\n");
        }
        if("time".equalsIgnoreCase(showType)){
            if(defaultValue!=null){
                fieldString=dateTime(name, code, defaultValue, validator, getDisabled(ctypei));
            }else{
                fieldString=dateTime(name, code, validator, getDisabled(ctypei));
            }
            js.append(" \n"+" laydate.render({\n"+"	 elem: '#"+code+"',type: 'time'\n"+" });\n");
        }
        if(showType.toLowerCase().endsWith("upload")||"fileUpload".equalsIgnoreCase(showType)
                ||"file".equalsIgnoreCase(showType)){
            String url=LemodoApplication.MODULE_NAME+"/file/upload";
            if(code.equals("driverFile")){
                url=LemodoApplication.MODULE_NAME+"/file/uploadDriver";
            }

            js.append(" upload.render({\r\n"+"		      elem: '#"+code+"Choose',url: '"+url
                    +"'\r\n"+"       ,accept: 'file' //普通文件\r\n"+"       ,exts: 'jar' //只允许上传jar文件\r\n"
                    +"      ,auto: false,bindAction: '#"+code+"UpBtn',done: function(res){\r\n"
                    +"		       $('#"+code
                    +"').val(res.data); layer.msg('上传成功');\r\n 		      }\r\n"
                    +"		    });");

            fieldString=fileUpload(name, code, validator);
        }
        if("multiFileUpload".equalsIgnoreCase(showType)||"file".equalsIgnoreCase(showType)){
            String url=LemodoApplication.MODULE_NAME+"/file/upload";
            if(code.equals("driverFile")){
                url=LemodoApplication.MODULE_NAME+"/file/uploadDriver";
            }

            js.append(" upload.render({\r\n"+"		      elem: '#"+code+"Choose',url: '"+url
                    +"'\r\n"+"       ,accept: 'file' //普通文件\r\n"+"       ,exts: 'jar' //只允许上传jar文件\r\n"
                    +"      ,auto: false,bindAction: '#"+code+"UpBtn',done: function(res){\r\n"
                    +"		       $('#"+code
                    +"').val(res.data); layer.msg('上传成功');\r\n 		      }\r\n"
                    +"		    });");

            fieldString=fileUpload(name, code, validator);
        }
        if("iconFont".equalsIgnoreCase(showType)){
            fieldString=iconFont(name, code);
        }

        if("javascriptCode".equalsIgnoreCase(showType)||"javaCode".equalsIgnoreCase(showType)){
            fieldString=javaCode(name, code);
            js.append(" layui.code({");
            js.append(" 	  title: '代码' ");
            js.append(" 	  ,skin: 'java'");
            js.append(" 	});");
        }
        return fieldString;
    }

    private String updateIcon(Map<String, Object> attMapBy, String html){
        String icon=string(attMapBy, ICON);
        if(icon!=null&&!"".equals(icon.trim())&&html.indexOf("${icon}")>0){
            html=html.replaceAll("\\$\\{icon\\}", icon);
        }
        return html;
    }

    private Boolean getBoolean(Map<String, Object> ctypei, String key){
        Object switchValue=ctypei.get(key);
        if(switchValue==null||switchValue.equals("")||switchValue.equals("off")||switchValue.equals("null")){
            return false;
        }
        if(String.valueOf(switchValue).equals("on")){
            return true;
        }
        return Boolean.valueOf(String.valueOf(switchValue));
    }

    private Boolean getReadOnly(Map<String, Object> ctypei){
        String readOnly=string(ctypei, "readOnly");
        if(readOnly==null||readOnly.equals("")){
            return false;
        }
        return Boolean.valueOf(readOnly);
    }

    private Boolean getDisabled(Map<String, Object> ctypei){
        String disabled=string(ctypei, "disabled");
        if(disabled==null||disabled.equals("")){
            return false;
        }
        return Boolean.valueOf(disabled);
    }

    /**
     * 查询表单字段
     *
     * @param ctypei
     * @param js
     * @param switchJs
     * @return
     */
    private String handleSearchShowType(Map<String, Object> ctypei, StringBuilder js, StringBuilder switchJs){
        String name=name(ctypei);
        String code=code(ctypei)+"Reload";
        String validator="";

        String showType=string(ctypei, show_type);
        String fieldString="";
        if("textarea".equalsIgnoreCase(showType)){
            fieldString=textArea(name, code, validator, getReadOnly(ctypei));
        }
        if("textEditor".equalsIgnoreCase(showType)){
            fieldString=textArea(name, code, validator, getReadOnly(ctypei));
            js.append(" layedit.set({uploadImage: {url: '"+LemodoApplication.MODULE_NAME+"/file/uploadImage',type:'post',success: function(data){ \r\n"
                    +"               console.log(data); \r\n"
                    +"            }}});\n");
            js.append(" var "+code+"Index = layedit.build('"+code+"');\n");
        }
        if("line".equalsIgnoreCase(showType)){
            fieldString=layFormLine(code, name, null);
        }
        if("password".equalsIgnoreCase(showType)){
            fieldString=password(name, code, validator);
        }
        if("date".equalsIgnoreCase(showType)){
            fieldString=date(name, code, validator, getReadOnly(ctypei));
            js.append(" \n"+" laydate.render({\n"+"	 elem: '#"+code+"'\n"
                    +",trigger: 'click',format:'yyyy-MM-dd' });");
        }
        if("switch".equalsIgnoreCase(showType)){
            fieldString=switchOn(ctypei, "ON|OFF", switchJs);
        }
        if("datetime".equalsIgnoreCase(showType)){
            fieldString=dateTime(name, code, validator, getDisabled(ctypei));
            js.append(" \n"+" laydate.render({\n"+"	 elem: '#"+code+"'\n"
                    +",trigger: 'click' ,format:'yyyy-MM-dd HH:mm:ss'"+"            ,type:'datetime' });");
        }
        return fieldString;
    }

    private void staticHandleShowType(Map<String, Object> ctypei, StringBuilder sb){

        String name=name(ctypei);
        String code=code(ctypei);
        String showType=string(ctypei, show_type);
        String value=string(ctypei, "value");
        String validator=string(ctypei, COLUMN_VALIDATOR);
        String fieldString="";
        if("textarea".equalsIgnoreCase(showType)){
            fieldString=textArea(name, code, value, validator, getReadOnly(ctypei));
        }
        if("password".equalsIgnoreCase(showType)){
            // password(name,code, sb);
        }
        if("date".equalsIgnoreCase(showType)){
            fieldString=date(name, code, value, validator, getDisabled(ctypei));

        }
        if("switch".equalsIgnoreCase(showType)){
            fieldString=switchOn(ctypei, "ON|OFF", null);
        }
        if("datetime".equalsIgnoreCase(showType)){
            fieldString=dateTime(name, code, value, validator, getDisabled(ctypei));
        }
    }

    /**
     * 收集处理Po类型
     *
     * @param switchJs
     */
    private String handlePoType(Map<String, Object> ctypei, StringBuilder switchJs, String poLabel){
        String fieldType=string(ctypei, "type");
        if(fieldType==null||"".equals(fieldType)){
            return "";
        }
        String name=name(ctypei);
        String code=code(ctypei);
        String showType=string(ctypei, show_type);
        String valueField=string(ctypei, value_field);
        String defaultValue=string(ctypei, FIELD_DEFAULT_VALUE);
        String fieldString="";

        // if ("iconFont".equalsIgnoreCase(showType)) {
        // fieldString = poSelectIconFont(ctypei, selectList);
        // }
        String label="";
        if(PO.equals(fieldType)){
            label=META_DATA;
        }else{
            label=fieldType;
        }

        if("manage".equalsIgnoreCase(showType)){
            fieldString=manage(name, code);
            manageClick(code, "管理"+name, LemodoApplication.MODULE_NAME+"/manage/"+label, switchJs);
        }
        if("window".equalsIgnoreCase(showType)){
            fieldString=selectWindow(switchJs, poLabel, ctypei, label);
        }
        if("iconFont".equalsIgnoreCase(showType)){
            fieldString=manage(name, code);
            iconSelectClick(code, "选择"+name, LemodoApplication.MODULE_NAME+"/objectRel/"+label+"/iconFont", switchJs);
        }


        Set<String> inList=Set.of("select", "checkbox", "radio", "switch");
        if(inList.contains(showType.toLowerCase())){
            List<Map<String, Object>> selectList=selectList(ctypei, fieldType, valueField);
            if("select".equalsIgnoreCase(showType)&&selectList!=null){

                fieldString=poSelectType(ctypei, selectList);
            }

            if("checkBox".equalsIgnoreCase(showType)){
                fieldString=poCheckBox(ctypei, selectList);
            }
            if("radio".equalsIgnoreCase(showType)){
                fieldString=poRadio(ctypei, selectList);
            }
            if("switch".equalsIgnoreCase(showType)){
                fieldString=poSwitchOn(ctypei, selectList, switchJs);
            }
        }

        // switch(showType.toLowerCase()){
        // case "radio":fieldString = poRadio(ctypei, selectList);
        // case "checkbox":fieldString = poCheckBox(ctypei, selectList);
        // case "switch": fieldString = poSwitchOn(ctypei, selectList, switchJs);
        //
        // }
        return fieldString;
    }

    public String selectWindow(StringBuilder switchJs, String poLabel, Map<String, Object> ctypei,
                               String label){
        String name=name(ctypei);
        String code=code(ctypei);
        String formateQuery=string(ctypei, "formateQuery");
        String valueField=string(ctypei, value_field);
        String fieldString=selectFromWindow(name, code, formateQuery);
        if(valueField==null){
            selectWindowClick(code, "选择"+name, LemodoApplication.MODULE_NAME+"/objectRel/"+poLabel+"/"+label, switchJs);
        }else{
            selectWindowClick(code, "选择"+name, LemodoApplication.MODULE_NAME+"/objectRel/"+poLabel+"/"+label+"/"+valueField, switchJs, valueField);
        }
        return fieldString;
    }

    /**
     * 获取某个类的所有数据，id,xx,name
     *
     * @param ctypei
     * @param fieldType
     * @param valueField
     * @return
     */
    private List<Map<String, Object>> selectList(Map<String, Object> ctypei, String fieldType, String valueField){
        String[] columns=SELECT_COLUMN.split(",");

        if(ctypei.containsKey(value_field)&&valueField!=null&&!valueField.trim().equals("")
                &&!valueField.equals(ID)&&!valueField.equals(CODE)&&!valueField.equals(NAME)){
            String string="id,"+valueField+",name";
            columns=string.split(",");
        }

        try{
            Set<String> poColSet=crudUtil.getPoColumnSet(fieldType);
            if(!poColSet.contains(CODE)||!poColSet.contains(NAME)){
                String querySelectField="match(n:SelectFieldMap) where n.label='"+fieldType
                        +"' return n.id,n.name,n.code,n.value";
                if(valueField!=null&&!valueField.equals(ID)){
                    querySelectField="match(n:SelectFieldMap) where n.label='"+fieldType+"' return n.id,n."
                            +valueField+",n.name,n.code,n.value";
                }

                List<Map<String, Object>> query=neo4jService.cypher(querySelectField);

                if(query!=null&&!query.isEmpty()){
                    Map<String, Object> map=query.get(0);
                    String code=code(map);
                    String name=string(map, NAME);
                    if(valueField!=null&&!valueField.equals(ID)){
                        String string="id,"+valueField+","+code+" as code, "+name+" as name";
                        columns=string.split(",");
                    }
                }

            }
        }catch(DefineException e){
            e.printStackTrace();
        }
        String query=Neo4jOptCypher.queryObj(null, fieldType, columns);
        List<Map<String, Object>> selectList=neo4jService.cypher(query);
        return selectList;
    }

    private void selectWindowClick(String code, String title, String url, StringBuilder switchJs){

        switchJs.append("layui.$('#"+code+"').on('click',"+code+"ClickFun);");
        switchJs.append("function "+code+"ClickFun(data){\n");
        switchJs.append("""
                layer.open({
                             	      type: 2,
                             	      anim: 0,
                             	      shade: 0,
                             	      maxmin: true,
                    """);
        switchJs.append("     title: '"+title+"',\n");
        switchJs.append("""
                      area: ['100%', '100%'],

                             	      btn:['关闭'],
                             	      yes:function(index,layero)
                             	      {
                                     	      var body = layer.getChildFrame('body', index);
                                     	      var selected = body.find('#selectObj').val();
                                     	      var selectedName = body.find('#selectObjName').val();
                """);
        switchJs.append("     	      $(\'#"+code+"').val(selected);\n");
        switchJs.append("""
                	close()
                         	      	          //index为当前层索引
                         	      	          layer.close(index)
                         	      },
                         	      cancel:function(){//右上角关闭毁回调
                         	      	     	close()
                         	      	     	var index = parent.layer.getFrameIndex(data.name);
                         	      	     	parent.layer.close(index);
                         	      },
                         	      zIndex: layer.zIndex //重点1
                         	      ,success: function(layero, index){
                         	      	 layer.setTop(layero); //重点2
                         	         var body = layer.getChildFrame('body', index);
                         	         var objId=body.find('#objId');
                         	         if(objId!=null&&currentNode!=null){
                         			if(currentNode.id!=undefined){
                         			   objId.val(currentNode.id);
                         			}else if(currentNode.code!=undefined){
                         	        	   objId.val(currentNode.code);
                         			}
                         		}
                         	      },
                """);
        switchJs.append("      content: '"+url+"'\n");
        switchJs.append("      	     });\n");
        switchJs.append("      	     }\n");
    }

    private void selectWindowClick(String code, String title, String url, StringBuilder js, String valueField){

        js.append("layui.$('#"+code+"Button').on('click',"+code+"ClickFun);");
        js.append("function "+code+"ClickFun(data){\n");
        js.append("""
                layer.open({
                             	      type: 2,
                             	      anim: 0,
                             	      shade: 0,
                             	      maxmin: true,
                    """);
        js.append("     title: '"+title+"',\n");
        js.append("""
                      area: ['100%', '100%'],

                             	      btn:['关闭'],
                             	      yes:function(index,layero)
                             	      {
                                     	      var body = layer.getChildFrame('body', index);
                                     	      var selected = body.find('#selectObj').val();
                                     	      var selectValue = body.find('#selectValue').val();
                                     	      var selectedName = body.find('#selectObjName').val();
                """);
        js.append("     	 if(selectValue){     $(\'#"+code+"').val(selectValue)}\n");
        js.append("     	     else{ $(\'#"+code+"').val(selected);}\n");
        js.append("     	 if(selectedName){     $(\'#"+code+"Button').val(selectedName)}\n");
        js.append("     	     else{ $(\'#"+code+"Button').val(selected);}\n");
        js.append("""
                close()
                        	      	          //index为当前层索引
                        	      	          layer.close(index)
                        	      },
                        	      cancel:function(){//右上角关闭毁回调
                        	      	     	close()
                        	      	     	var index = parent.layer.getFrameIndex(data.name);
                        	      	     	parent.layer.close(index);
                        	      },
                        	      zIndex: layer.zIndex //重点1
                        	      ,success: function(layero, index){
                        	      	 layer.setTop(layero); //重点2
                        	         var body = layer.getChildFrame('body', index);
                        	         var objId=body.find('#objId');
                        	         if(objId!=null&&currentNode!=null){
                             		         	if(currentNode.id!=undefined){
                             		         		objId.val(currentNode.id);
                             		         	}else if(currentNode.code!=undefined){
                             		         	        objId.val(currentNode.code);
                             		         	}
                      			}
                	      },
                     """);
        js.append("      content: '"+url+"'\n");
        js.append("      	     });\n");
        js.append("      	     }\n");
    }

    /**
     * 弹出层选择对象，获取当前选择对象数据
     *
     * @param code
     */
    private void manageClick(String code, String title, String url, StringBuilder switchJs){

        switchJs.append("layui.$('#"+code+"').on('click',"+code+"ClickFun);");
        switchJs.append("function "+code+"ClickFun(data){\n");

        switchJs.append("     layer.open({\n");
        switchJs.append("      type: 2,\n");
        switchJs.append("      anim: 0,\n");
        switchJs.append("      shade: 0,\n");
        switchJs.append("      maxmin: true,\n");
        switchJs.append("      title: '"+title+"',\n");
        switchJs.append("      area: ['100%', '100%'],\n");
        switchJs.append("      btn:['关闭'],\n");
        switchJs.append("      yes:function(index,layero)\n");
        switchJs.append("      {\n");
        switchJs.append("      var body = layer.getChildFrame('body', index);\n");
        switchJs.append("      var selected = body.find('#selectObj').val();\n");
        // switchJs.append(" var selectedName = body.find('#selectObjName').val();\n");

        // switchJs.append(" $(\"#relationObj\").val(selectedName);\n");
        switchJs.append("      $(\'#"+code+"').val(selected);\n");
        switchJs.append("      	      	  close()\n");
        switchJs.append("      	          //index为当前层索引\n");
        switchJs.append("      	          layer.close(index)\n");
        switchJs.append("      },\n");
        switchJs.append("      cancel:function(){//右上角关闭毁回调\n");
        switchJs.append("      	     	 close()\n");
        switchJs.append("      	     	 var index = parent.layer.getFrameIndex(data.name); //先得到当前iframe层的索引\n");
        switchJs.append("      	     		parent.layer.close(index); //再执行关闭\n");
        switchJs.append("      },\n");
        switchJs.append("      zIndex: layer.zIndex //重点1\n");
        switchJs.append("      ,success: function(layero, index){\n");
        switchJs.append("      	        layer.setTop(layero); //重点2\n");
        switchJs.append("               var body = layer.getChildFrame('body', index);\n");
        switchJs.append("               var objId=body.find('#objId');\n");
        switchJs.append("               if(objId!=null&&currentNode!=null){          \n");
        switchJs.append("		   			if(currentNode.id!=undefined){");
        switchJs.append("						objId.val(currentNode.id); \n");
        switchJs.append("					}else if(currentNode.code!=undefined){");
        switchJs.append("        			     objId.val(currentNode.code);     ");
        switchJs.append("					}}        \n");

        switchJs.append("      },		\n");
        switchJs.append("      content: '"+url+"'\n");
        switchJs.append("      	     });\n");
        switchJs.append("      	    }\n");
    }

    private void iconSelectClick(String code, String title, String url, StringBuilder clickJs){

        clickJs.append("layui.$('#"+code+"').on('click', function(data){\n");

        clickJs.append("     layer.open({\n");
        clickJs.append("      type: 2,\n");
        clickJs.append("      anim: 0,\n");
        clickJs.append("      shade: 0,\n");
        clickJs.append("      maxmin: true,\n");
        clickJs.append("      title: '"+title+"',\n");
        clickJs.append("      area: ['100%', '100%'],\n");
        clickJs.append("      btn:['关闭'],\n");
        clickJs.append("      yes:function(index,layero)\n");
        clickJs.append("      {\n");
        clickJs.append("      var body = layer.getChildFrame('body', index);\n");
        clickJs.append("      var selected = body.find('#selectObj').val();\n");
        clickJs.append("      var selectValue = body.find('#selectValue').val();\n");
        // switchJs.append(" var selectedName = body.find('#selectObjName').val();\n");

        // clickJs.append(" $(\"#selectValue\").val(selectValue);\n");
        // clickJs.append(" $(\"#relationObj\").val(selectedName);\n");
        clickJs.append("      $(\'#"+code+"').val(selectValue);\n");
        clickJs.append("      	      	  close()\n");
        clickJs.append("      	          //index为当前层索引\n");
        clickJs.append("      	          layer.close(index)\n");
        clickJs.append("      },\n");
        clickJs.append("      cancel:function(){//右上角关闭毁回调\n");
        clickJs.append("      	     	 close()\n");
        clickJs.append("      	     	 var index = parent.layer.getFrameIndex(data.name); //先得到当前iframe层的索引\n");
        clickJs.append("      	     		parent.layer.close(index); //再执行关闭\n");
        clickJs.append("      },\n");
        clickJs.append("      zIndex: layer.zIndex //重点1\n");
        clickJs.append("      ,success: function(layero, index){\n");
        clickJs.append("      	        layer.setTop(layero); //重点2\n");
        clickJs.append("               var body = layer.getChildFrame('body', index);\n");
        clickJs.append("               var objId=body.find('#objId');\n");
        clickJs.append("               if(objId!=null&&currentNode!=null){          \n");
        clickJs.append("		   			if(currentNode.id!=undefined){");
        clickJs.append("						objId.val(currentNode.id); \n");
        clickJs.append("					}else if(currentNode.code!=undefined){");
        clickJs.append("        			     objId.val(currentNode.code);     ");
        clickJs.append("					}}        \n");

        clickJs.append("      },		\n");
        clickJs.append("      content: '"+url+"'\n");
        clickJs.append("      	     });\n");
        clickJs.append("      	    });\n");
    }

    private void staticHandlePoType(Map<String, Object> ctypei, StringBuilder sb){
        String fieldType=string(ctypei, "type");
        String showType=string(ctypei, show_type);

        String[] columns=SELECT_COLUMN.split(",");
        String query=Neo4jOptCypher.queryObj(null, fieldType, columns);
        List<Map<String, Object>> selectList=neo4jService.cypher(query);
        String fieldHtml="";
        if("select".equalsIgnoreCase(showType)){
            fieldHtml=poSelectType(ctypei, selectList);
        }
        if("checkBox".equalsIgnoreCase(showType)){
            fieldHtml=poCheckBox(ctypei, selectList);
        }
        if("radio".equalsIgnoreCase(showType)){
            fieldHtml=poRadio(ctypei, selectList);
        }
        if("switch".equalsIgnoreCase(showType)){
            fieldHtml=poSwitchOn(ctypei, selectList, null);
        }
        sb.append(fieldHtml);
    }

    /**
     * 生成layuse相关代码
     *
     * @param layUse
     */
    private void layUseJs(StringBuilder layUse, Set<String> layUseMap, Boolean useTab){
        Set<String> modules=new HashSet<String>();
        modules.add("form");
        // modules.add("'laytpl'");

        modules.add("table");
        List<String> declares=new ArrayList<>();
        declares.add(" form = layui.form\n");
        declares.add(" ,table = layui.table\n");
        // declares.add(" ,laytpl = layui.laytpl\n");

        declares.add(" ,layer = layui.layer;\n");

        if(useTab){
            useModule(modules, declares, "element");
        }
        if(layUseMap.contains("layedit")){
            useModule(modules, declares, "layedit");
        }

        if(layUseMap.contains("upload")){
            useModule(modules, declares, "upload");
        }
        useIt(layUseMap, modules, declares, "iconPicker");
        useIt(layUseMap, modules, declares, "colorpicker");
        useIt(layUseMap, modules, declares, "slider");

        String dropDown="dropdown";
        useIt(layUseMap, modules, declares, dropDown);
        useIt(layUseMap, modules, declares, CODE);

        if(layUseMap.contains("laydate")){
            useModule(modules, declares, "laydate");
        }

        String jsGlobalParam="\n var layer,crudTable";
        if(!modules.isEmpty()){
            jsGlobalParam=jsGlobalParam+","+String.join(",", modules);
        }
        jsGlobalParam=jsGlobalParam+";";

//	layUse.append(jsGlobalParam + "\n " + layuiConfig);
        // if(modules.contains("iconSelected")||modules.contains("numinput")) {
        // extend({
        // numinput: '{/}/static/layui/lay/layui_exts/numinput/js/index',
        // iconSelected: '{/}/static/layui/lay/layui_exts/iconSelected/js/index',
        // })
        // }

        String join="'"+String.join("','", modules)+"'";
        layUse.append(jsGlobalParam+"\n "+layuiConfig+".use(["+join+"], function(){\n"
                +String.join(" ", declares));
//
//	layUse.append(".use(['" + String.join("','", modules) + "'], function(){\n" + String.join(" ", declares));

        if(modules.contains(dropDown)){
            layUse.append("dropDown=dropdown;");
        }
    }

    private void useIt(Set<String> layUseMap, Set<String> modules, List<String> declares, String key){
        if(layUseMap.contains(key)){
            useModule(modules, declares, key);
        }
    }

    private void layOnlyTabJs(Model model){
        StringBuilder layUse=new StringBuilder();
        Set<String> modules=new HashSet<String>();
        modules.add("form");
        modules.add("table");
        List<String> declares=new ArrayList<>();
        declares.add(" layer = layui.layer;\n");
        declares.add(" form = layui.form;\n");
        useModule(modules, declares, "element");
        String jsGlobalParam="\n var layer,form;";

        layUse.append(jsGlobalParam+"\n "+layuiConfig+".use(['"+String.join("','", modules)
                +"'], function(){\n"+String.join(" ", declares));
        model.addAttribute(LAY_USE, layUse.toString());
        model.addAttribute("layField", "});");
    }

    /**
     * 字段表单Tabjs
     *
     * @param model
     */
    private void fieldFormTabJs(Model model){
        StringBuilder layUse=new StringBuilder();
        Set<String> modules=new HashSet<String>();
        modules.add("form");
        List<String> declares=new ArrayList<>();
        declares.add(" layer = layui.layer;\n");
        declares.add(" form = layui.form;\n");
        useModule(modules, declares, "element");
        String jsGlobalParam="\n var layer,form;";

        layUse.append(jsGlobalParam+"\n "+layuiConfig+".use(['"+String.join("','", modules)
                +"'], function(){\n"+String.join(" ", declares));
        model.addAttribute(LAY_USE, layUse.toString());

        model.addAttribute("layField", "});");
    }

    private void useModule(Set<String> modules, List<String> declares, String module){
        modules.add(module);
        declares.add(" "+module+" = layui."+module+";\n");
    }

    /**
     * 开关
     */
    private String poSwitchOn(Map<String, Object> ctypei, List<Map<String, Object>> selectList,
                              StringBuilder switchJs){
        List<String> switchText=new ArrayList<>(2);
        for(Map<String, Object> opti : selectList){
            // switchText.add(opti.get("id"));
            switchText.add(String.valueOf(opti.get(NAME)));
        }
        return switchOn(ctypei, String.join("|", switchText), switchJs);
    }

    private String switchOn(Map<String, Object> ctypei, String text, StringBuilder switchJs){
        String name=name(ctypei);
        String code=code(ctypei);
        String value=string(ctypei, "value");

        StringBuilder sb=new StringBuilder();
        // sb.append("</div><div class=\"layui-form-item\">");
        sb.append("<label class=\"layui-form-label\">"+name+"</label>");
        // sb.append("<div class=\"layui-input-block\">");
        sb.append("	<div class=\"layui-input-inline\">");
        sb.append("<input id=\""+code+"\" name=\""+code+"\" type=\"checkbox\" ");
        if(value!=null&&text.startsWith(value)){
            sb.append(" checked=\"true\" ");
        }else{
            sb.append(" value=\"off\" ");
        }

        sb.append("lay-filter=\""+code+"\" lay-skin=\"switch\" lay-text=\""+text+"\">");
        sb.append("</div>");
        if(switchJs!=null&&switchJs.indexOf("form.on('switch("+code+")'")<0){
            switchJs.append("\n form.on('switch("+code+")', function(data){");
            switchJs.append("\n this.value=this.checked==true ? 'on' : 'off'");
            switchJs.append("\n  });\n");
        }
        return sb.toString();
    }

    private String date(String name, String code, String validator, Boolean disabled){
        return dateTime(name, code, null, "yyyy-MM-dd", validator, disabled);
    }

    private String dateTime(String name, String code, String validator, Boolean disabled){
        return dateTime(name, code, null, "yyyy-MM-dd HH:mm:ss", validator, disabled);
    }

    private String date(String name, String code, String value, String validator, Boolean readOnly){
        return dateTime(name, code, value, "yyyy-MM-dd", validator, readOnly);
    }

    private String dateTime(String name, String code, String value, String validator, Boolean disabled){
        return dateTime(name, code, value, "yyyy-MM-dd HH:mm:ss", validator, disabled);
    }

    private String dateTime(String name, String code, String value, String formate, String validator,
                            Boolean disabled){
        StringBuilder sb=new StringBuilder();
        sb.append("<div class=\"layui-inline\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append(" <div class=\"layui-input-inline\">");
        sb.append("	<input class=\"layui-input dateInput\" id=\""+code+"\" name=\""+code+"\"");
        addValidator(validator, sb);
        if(disabled){
            sb.append(" disabled=\"true\"");
        }
        sb.append(" type=\"text\" placeholder=\""+formate+"\">");
        if(value!=null){
            sb.append(value);
        }
        sb.append("</input> </div> </div>");
        return sb.toString();
    }

    private void addValidator(String validator, StringBuilder sb){
        if(StringUtils.isNoneBlank(validator)&&!"null".equalsIgnoreCase(validator.trim())){
            sb.append(validator);
        }
    }

    /**
     * 单选框
     *
     * @param selectList
     */
    private String poRadio(Map<String, Object> ctypei, List<Map<String, Object>> selectList){
        String name=name(ctypei);
        String code=code(ctypei);
        String value=string(ctypei, "value");

        StringBuilder sb=new StringBuilder();
        sb.append("<div class=\"layui-form-item\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append(" <div class=\"layui-input-block\">");
        for(Map<String, Object> opti : selectList){
            sb.append(" <input name=\""+code+"\" title=\""+opti.get(NAME)+"\"");
            Object object=opti.get("id");
            if(value!=null&&value.equals(object)){
                sb.append(" checked=\"true\" ");
            }
            sb.append(" value=\""+object+"\"type=\"radio\">");
        }
        sb.append(" </div> </div>");
        return sb.toString();
    }

    /**
     * 复选框
     *
     * @param selectList
     */
    private String poCheckBox(Map<String, Object> ctypei, List<Map<String, Object>> selectList){
        String name=name(ctypei);
        String code=code(ctypei);
        String value=string(ctypei, "value");

        StringBuilder sb=new StringBuilder();
        sb.append("<div class=\"layui-form-item\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append(" <div class=\"layui-input-block\">");
        for(Map<String, Object> opti : selectList){
            sb.append(" <input name=\""+code+"["+opti.get("id")+"]\" title=\""+opti.get(NAME)+"\" ");
            Object object=opti.get("id");
            if(value!=null&&value.equals(object)){
                sb.append(" checked=\"true\" ");
            }
            sb.append("type=\"checkbox\">");
        }
        sb.append(" </div> </div>");
        return sb.toString();
    }

    private String poSelect(Map<String, Object> ctypei, List<Map<String, Object>> selectList){
        String name=name(ctypei);
        String code=code(ctypei);
        String value=string(ctypei, "value");

        StringBuilder sb=new StringBuilder();
        sb.append("<div class=\"layui-form-item\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append(" <div class=\"layui-input-block\">");
        for(Map<String, Object> opti : selectList){
            sb.append(" <input name=\""+code+"["+opti.get("id")+"]\" title=\""+opti.get(NAME)+"\" ");
            Object object=opti.get("id");
            if(value!=null&&value.equals(object)){
                sb.append(" checked=\"true\" ");
            }
            sb.append("type=\"checkbox\">");
        }
        sb.append(" </div> </div>");
        return sb.toString();
    }

    /**
     * 选择对象列表
     *
     * @param ctypei
     * @param selectList
     * @return
     */
    public String poSelectType(Map<String, Object> ctypei, List<Map<String, Object>> selectList){
        String name=name(ctypei);
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-inline \">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append(" <div class=\"layui-input-inline\">");
        addSelect(ctypei, sb, selectList);
        sb.append(" </div> </div>");
        return sb.toString();
    }

    public String statusSelect(Map<String, Object> ctypei, List<Map<String, Object>> selectList){
        String name=name(ctypei);
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-inline \">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append(" <div class=\"layui-input-inline\">");
        addStatusSelect(ctypei, sb, selectList);
        sb.append(" </div> </div>");
        return sb.toString();
    }

    private String poSelectIconFont(Map<String, Object> ctypei, List<Map<String, Object>> selectList){
        String name=name(ctypei);
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-inline \">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append(" <div class=\"layui-input-inline\">");
        addSelectIconFont(ctypei, sb, selectList);
        sb.append(" </div> </div>");
        return sb.toString();
    }

    public String addSelect(Map<String, Object> ctypei, List<Map<String, Object>> selectList){
        StringBuilder sb=new StringBuilder();
        addSelect(ctypei, sb, selectList);
        return sb.toString();
    }

    /**
     * 添加select
     *
     * @param sb
     * @param selectList
     */
    private void addSelect(Map<String, Object> ctypei, StringBuilder sb, List<Map<String, Object>> selectList){
        String name=name(ctypei);
        String code=code(ctypei);
        String value=string(ctypei, "value");
        String dvalue=string(ctypei, FIELD_DEFAULT_VALUE);

        if(value==null&&dvalue!=null){
            value=dvalue;
        }
        String valueField=string(ctypei, value_field);
        sb.append(" <select name=\""+code+"\" id=\""+code+"\" value=\""+value+"\" ");

        String msgString="请选择"+name;
        if(selectList!=null&&selectList.size()>15){
            sb.append(" lay-search=\"\" ");
            msgString="直接选择或搜索选择";
        }
        // lay-verify=\"required\"
        sb.append(">");
        sb.append(addSelectOption(selectList, value, msgString, valueField));
        sb.append(" </select>");
    }

    private void addStatusSelect(Map<String, Object> ctypei, StringBuilder sb, List<Map<String, Object>> selectList){
        String name=name(ctypei);
        String code=code(ctypei);
        String value=string(ctypei, "value");
        String dvalue=string(ctypei, FIELD_DEFAULT_VALUE);

        if(value==null&&dvalue!=null){
            value=dvalue;
        }
        sb.append(" <select name=\""+code+"\" id=\""+code+"\" value=\""+value+"\" ");

        String msgString="请选择"+name;
        if(selectList!=null&&selectList.size()>15){
            sb.append(" lay-search=\"\" ");
            msgString="直接选择或搜索选择";
        }
        // lay-verify=\"required\"
        sb.append(">");
        sb.append(addStatusSelectOption(selectList, value, msgString));
        sb.append(" </select>");
    }

    private void addSelectIconFont(Map<String, Object> ctypei, StringBuilder sb, List<Map<String, Object>> selectList){
        String name=name(ctypei);
        String code=code(ctypei);
        String value=string(ctypei, unicode);
        String valueField=string(ctypei, value_field);
        sb.append(" <select name=\""+code+"\" id=\""+code+"\" ");

        String msgString="请选择"+name;
        if(selectList.size()>15){
            sb.append(" lay-search=\"\" ");
            msgString="直接选择或搜索选择";
        }
        // lay-verify=\"required\"
        sb.append(">");
        sb.append(addSelectIFOption(selectList, value, msgString, valueField));
        sb.append(" </select>");
    }

    /**
     * 添加选择选项
     *
     * @param selectList
     * @param value
     * @param msgString
     */
    private String addSelectOption(List<Map<String, Object>> selectList, String value, String msgString,
                                   String valueField){
        StringBuilder sb=new StringBuilder();
        sb.append("    <option value=\"\">"+msgString+"</option>");
        for(Map<String, Object> opti : selectList){
            sb.append("   <option ");
            Object oValueobject="";
            if(valueField!=null){
                oValueobject=opti.get(valueField);
            }else{
                oValueobject=opti.get(CODE);
            }

            if(value!=null&&value.equals(oValueobject)){
                sb.append(" selected=\"true\" ");
            }
            sb.append(" value=\""+oValueobject+"\">"+opti.get(NAME)+"</option>");
        }
        return sb.toString();
    }

    private String addStatusSelectOption(List<Map<String, Object>> selectList, String value, String msgString){
        StringBuilder sb=new StringBuilder();
        sb.append("    <option value=\"\">"+msgString+"</option>");
        for(Map<String, Object> opti : selectList){
            sb.append("   <option ");
            String oValueobject=value(opti);

            if(value!=null&&value.equals(oValueobject)){
                sb.append(" selected=\"true\" ");
            }
            sb.append(" value=\""+oValueobject+"\">"+opti.get(NAME)+"</option>");
        }
        return sb.toString();
    }

    private String addSelectIFOption(List<Map<String, Object>> selectList, String value, String msgString,
                                     String valueField){
        StringBuilder sb=new StringBuilder();
        sb.append("    <option value=\"\">"+msgString+"</option>");
        for(Map<String, Object> opti : selectList){
            sb.append("   <option ");
            Object oValueobject="";
            if(valueField!=null){
                oValueobject=opti.get(valueField);
            }else{
                oValueobject=opti.get(CODE);
            }

            if(value!=null&&value.equals(oValueobject)){
                sb.append(" checked=\"true\" ");
            }
            sb.append(" value=\""+oValueobject+"\"><i class=\"layui-icon\">"+oValueobject+"</i></option>");
        }
        return sb.toString();
    }

    /**
     * 添加选择选项
     *
     * @param selectList
     * @param msgString
     * @return
     */
    public String addSelectOption(List<Map<String, Object>> selectList, String msgString, String valueField){
        return addSelectOption(selectList, null, msgString, valueField);
    }

    public String addSelectOption(List<Map<String, Object>> selectList, String msgString){
        return addSelectOption(selectList, msgString, null);
    }

    /**
     * 返回文本域
     *
     * @param name
     * @param code
     */
    private String textArea(String name, String code, String validator, Boolean readOnly){
        return textArea(name, code, null, validator, readOnly);
    }

    private String manage(String name, String code){
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-inline\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");

        sb.append(" <div class=\"layui-input-inline\">");
        sb.append("<input type=\"button\" id=\""+code
                +"\" class=\"layui-btn layui-btn-primary \" value=\"管理\" lay-event=\"manage\"></input>");
        sb.append("  </div></div>");
        return sb.toString();
    }

    private String selectFromWindow(String name, String code, String isformat){
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-inline\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");

        sb.append(" <div class=\"layui-input-inline\">");
        if(isformat!=null&&isformat.equals("on")){
            sb.append(" <label  id=\""+code+COLUMN_FORMAT+"\" ></label>");
        }
        sb.append(" <input type=\"hidden\" id=\""+code+"\" />");
        sb.append(" <input type=\"button\" id=\""+code+"Button\" class=\"layui-btn layui-btn-primary \" value=\"选择\" lay-event=\"selectWindow\"></input>");
        sb.append("  </div></div>");
        return sb.toString();
    }

    private String textArea(String name, String code, Object value, String validator, Boolean readOnly){
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item  layui-form-text\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append(" <div class=\"layui-input-block\">");
        sb.append("       <textarea id=\""+code+"\" name=\""+code+"\" class=\"layui-textarea\"  lay-verify=\""
                +code+"\" onscroll=\"this.rows++;\"");
        addValidator(validator, sb);
        if(readOnly!=null&&readOnly){
            sb.append(" readOnly=true ");
        }
        sb.append("placeholder=\"请输入"+name+"\">");
        if(value!=null){
            sb.append(String.valueOf(value));
        }
        sb.append(" </textarea>");
        sb.append("  </div></div>");
        return sb.toString();
    }

    private String javaCode(String name, String code){
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item  layui-form-text\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append(" <div class=\"layui-input-block\">");
        sb.append(" <pre class=\"layui-code\" id=\""+code+"\" name=\""+code+"\" >");
        sb.append("</pre>");
        sb.append("  </div></div>");
        return sb.toString();
    }

    private String iconFont(String name, String code){
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item  layui-form-text\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+" <i class=\"layui-icon\" id=\""+code
                +"Icon\">&#xe60c;</i></label>");
        sb.append("	<div class=\"layui-input-inline\">");
        sb.append("		<input name=\""+code+"\" class=\"layui-input\" id=\""+code+"\"");
        sb.append("			placeholder=\"请输入"+name+"\" autocomplete=\"off\" >");
        sb.append("	</div></div>");
        return sb.toString();
    }

    private String iconSelected(String name, String code){
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item  layui-form-text\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+" <i class=\"layui-icon\" id=\""+code
                +"Icon\">&#xe60c;</i></label>");
        sb.append("	<div class=\"layui-input-inline\">");
        sb.append("		<input name=\""+code+"\" class=\"layui-input\" id=\""+code+"\"");
        sb.append("			placeholder=\"请输入"+name+"\" autocomplete=\"off\" >");
        sb.append("	</div></div>");
        return sb.toString();
    }

    private String password(String name, String code, String validator){
        StringBuilder sb=new StringBuilder();

        sb.append("<div  class=\"layui-form-item\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append(" <div class=\"layui-input-inline\">");
        sb.append("       <input id=\""+code+"\" name=\""+code
                +"\" type=\"password\" class=\"layui-input\" placeholder=\"请输入"+name
                +"\" lay-verify=\"pass\"></input>");
        sb.append("  </div> <div class=\"layui-form-mid layui-word-aux\">请填写6到12位密码</div></div>");
        return sb.toString();
    }

    private String fileUpload(String name, String code, String validator){
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append("""
                <div class="layui-upload">
                <input type="hidden"
                """);
        sb.append("id=\""+code+"\"");
        addValidator(validator, sb);
        sb.append("""
                	>
                <button type="button" class="layui-btn layui-btn-normal"
                """);
        sb.append("id=\""+code+"Choose\"");
        sb.append("""
                >选择文件</button>
                <button type="button" class="layui-btn"
                """);
        sb.append("id=\""+code+"UpBtn\"");
        sb.append("""
                  >开始上传</button>
                </div>
                	""");
        // <button type="button" class="layui-btn" id="
        // """);
        //
        // sb.append(code+"\" ");
        //
        // sb.append("""
        // ><i class="layui-icon"></i>上传文件</button>
        // <input class="layui-upload-file" type="file" accept name="file">
        // """);
        // sb.append(" </div>");

        return sb.toString();
    }

    /**
     * layui 表单字段 <label class=\"layui-form-label\" th:text=\""+name+"\"></label>:
     * <div class=\"layui-input-inline\"> <input th:name=\""+code+"\"
     * class=\"layui-input\" th:id=\""+code+"\" placeholder=\"请输入"+name+"\"
     * autocomplete=\"off\"> </div>
     *
     * @param code
     * @param name
     * @return
     */
    public String layFormItem(String code, String name, String validator){
        return layFormItem(code, name, null, validator);
    }

    public String layFormNumber(String code, String name, String validator){
        return layFormNumber(code, name, null, validator);
    }
    public String layFormNumber(String code, String name,String defaultValue, String validator){
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item  layui-form-text\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append("	<div class=\"layui-input-block\">");
        sb.append("   <input type=\"number\" name=\""+code+"\" id=\""+code+"\" required lay-verify=\"number\" autocomplete=\"off\" class=\"layui-input\" ");

        if(defaultValue!=null){
            sb.append("value=\""+String.valueOf(defaultValue)+"\"");
        }
        sb.append(">	</div></div>");
        return sb.toString();
    }



    public String layFormItem(String code, String name){
        return layFormItem(code, name, null, null);
    }
    public String formItemValue(String code, String name,String value){
        return layFormItem(code, name, value, null);
    }

    public String layFormLine(String code, String name, Object value){
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item  layui-form-text\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append("	<div class=\"layui-input-block\">");
        sb.append("		<input name=\""+code+"\" class=\"layui-input\" id=\""+code+"\"");
        sb.append("			placeholder=\"请输入"+name+"\" autocomplete=\"off\" ");
        if(value!=null){
            sb.append("value=\""+String.valueOf(value)+"\"");
        }
        sb.append(">	</div></div>");
        return sb.toString();
    }

    public String formLine(String code, String name, Object value){
        StringBuilder sb=new StringBuilder();
        sb.append("<div  class=\"layui-form-item  layui-form-text\">");
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append("	<div class=\"layui-input-block\">");
        sb.append("		<input name=\""+code+"\" class=\"layui-input\" id=\""+code+"\"");
        sb.append("			placeholder=\"请输入"+name+"\" autocomplete=\"off\" ");
        if(value!=null){
            sb.append("value=\""+String.valueOf(value)+"\"");
        }
        sb.append(">	</div></div>");
        return sb.toString();
    }

    public String layFormItem(String code, String name, Object value, String validator){
        StringBuilder sb=new StringBuilder();
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        appendCode(code, name, value, validator, sb);
        return sb.toString();
    }

    private void appendCode(String code, String name, Object value, String validator, StringBuilder sb){
        sb.append("	<div class=\"layui-input-inline\">");
        sb.append("		<input name=\""+code+"\" class=\"layui-input\" id=\""+code+"\"");
        addValidator(validator, sb);

        sb.append("			placeholder=\"请输入"+name+"\" autocomplete=\"off\" ");
        if(value!=null){
            sb.append("value=\""+String.valueOf(value)+"\"");
        }
        sb.append(">	</div>");
    }

    private void appendHidden(String code, StringBuilder sb){
        sb.append("		<input type=\"hidden\" name=\""+code+"\"  id=\""+code+"\"/>");
    }


    /**
     * 只读属性
     *
     * @param code
     * @param name
     * @param value
     * @return
     */
    public String layReadOnlyFormItem(String code, String name, Object value){
        StringBuilder sb=new StringBuilder();
        sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
        sb.append("	<div class=\"layui-input-inline\">");
        sb.append("		<input name=\""+code+"\" class=\"layui-input\" id=\""+code+"\"");

        sb.append("		readOnly=true autocomplete=\"off\" ");
        if(value!=null){
            sb.append("value=\""+String.valueOf(value)+"\"");
        }
        sb.append(">	</div>");
        return sb.toString();
    }

    /**
     * 添加按钮,列表判断是否有管理按钮 更多操作：
     * <table class="layui-table">
     * <tbody>
     * <tr>
     * <td>列表 1</td>
     * <td><button class="layui-btn layui-btn-sm demolist" data-id=
     * "111">更多操作</button></td>
     * </tr>
     * </tbody>
     * </table>
     * <script> layui.use('dropdown', function(){ var dropdown = layui.dropdown ,$ =
     * layui.jquery;
     * <p>
     * dropdown.render({ elem: '.demolist' ,data: [{ title: 'item 1' ,id: 'aaa' }]
     * ,click: function(data, othis){ var elem = $(this.elem) ,listId =
     * elem.data('id'); //表格列表的预埋数据 layer.msg('得到表格列表的 id：'+ listId +'，下拉菜单 id：'+
     * data.id); } }); }); </script>
     *
     * @param model
     * @param po
     */
    public List<Map<String, Object>> tableToolBtn(Model model, Map<String, Object> po){
        List<Map<String, Object>> btnList=listMetaBtn(po);
        List<Map<String, Object>> tableHeadBtnList=listToolBarBtn(po);

        StringBuilder opt=new StringBuilder();
        StringBuilder toolbarOpt=new StringBuilder();

        StringBuilder toolFun=new StringBuilder();
        StringBuilder dropDownFun=new StringBuilder();
        StringBuilder dropDownItems=new StringBuilder();
        StringBuilder activLogic=new StringBuilder();
        StringBuilder toolBarActiveLogic=new StringBuilder();

        boolean removeBtn=false;
        boolean documentBtn=false;

        if(btnList.size()>0){
            for(Map<String, Object> btni : btnList){
                if("removeBtn".equals(btni.get(NODE_CODE))){
                    removeBtn=true;
                }
                if("documentBtn".equals(btni.get(NODE_CODE))){
                    documentBtn=true;
                }
                addBtn(opt, toolFun, activLogic, btni);
            }
            if(tableHeadBtnList.size()>0){
                btnList.removeAll(tableHeadBtnList);
                for(Map<String, Object> thBtni : tableHeadBtnList){
                    addBtn(toolbarOpt, toolFun, toolBarActiveLogic, thBtni);
                }
            }
            if(!removeBtn){
                addOneDropDown(dropDownItems, "removeBtn");
                addOneBtn("removeBtn", opt, toolFun, activLogic);
            }
            if(!documentBtn){
                addOneDropDown(dropDownItems, "documentBtn");
                addOneBtn("documentBtn", opt, toolFun, activLogic);
            }
            dropDownItem(btnList, dropDownItems);
        }else{
            addDefaultDropDown(dropDownItems);
            addDefaultBtn(opt, toolFun, activLogic);
        }

        // 是否可管理
        addManageBtn(po, opt, toolbarOpt, toolFun, activLogic, toolBarActiveLogic);
        addDefaultTableHeadBtn(toolbarOpt, toolFun, toolBarActiveLogic);
        if(dropDownItems.isEmpty()){
            model.addAttribute("opt", opt.toString());
        }else{
            model.addAttribute("dropDownFun", dropDownFun.toString());
            model.addAttribute("dropDwonItem", "[\n"+dropDownItems.toString()+"]");
        }
        model.addAttribute("toolbarOpt", toolbarOpt.toString());
        model.addAttribute("toolFun", toolFun.toString());
        model.addAttribute("activLogic", activLogic.toString());

        model.addAttribute("toolBarActiveLogic", toolBarActiveLogic.toString());
        return btnList;

    }

    /**
     * 场景按钮展现
     *
     * @param model
     * @param btnList
     * @return
     */
    public List<Map<String, Object>> sceneBtn(Model model, List<Map<String, Object>> btnList){

        StringBuilder opt=new StringBuilder();

        StringBuilder toolFun=new StringBuilder();
        if(btnList.size()>0){
            for(Map<String, Object> btni : btnList){
                addBtn(opt, toolFun, btni);
            }
        }
        model.addAttribute("opt", opt.toString());
        model.addAttribute("toolFun", toolFun.toString());
        return btnList;
    }

    public List<Map<String, Object>> formBtn(Model model, List<Map<String, Object>> btnList){

        StringBuilder opt=new StringBuilder();

        StringBuilder toolFun=new StringBuilder();
        if(btnList.size()>0){
            for(Map<String, Object> btni : btnList){
                addFromBtn(opt, toolFun, btni);
            }
        }
        model.addAttribute("opt", opt.toString());
        model.addAttribute("toolFun", toolFun.toString());
        return btnList;
    }

    public List<Map<String, Object>> sceneLink(Model model, List<Map<String, Object>> hrefList){

        StringBuilder opt=new StringBuilder();

        StringBuilder toolFun=new StringBuilder();
        if(hrefList.size()>0){
            for(Map<String, Object> btni : hrefList){
                addBtn(opt, toolFun, btni);
            }
        }
        model.addAttribute("links", opt.toString());
        return hrefList;
    }

    public String sceneiBtn(StringBuilder toolFun, List<Map<String, Object>> btnList, Map<String, Object> mi){

        StringBuilder htmls=new StringBuilder();

        if(btnList.size()>0){
            for(Map<String, Object> btni : btnList){
                addBtn(htmls, toolFun, btni);
            }
        }

        return htmls.toString();
    }

    /**
     * 新版的下拉菜单功能
     *
     * @param model
     * @param label
     * @param po
     * @return
     */
    public List<Map<String, Object>> d2tableToolBtn(Model model, String label, Map<String, Object> po){
        List<Map<String, Object>> btnList=listMetaBtn(po);
        List<Map<String, Object>> tableHeadBtnList=listToolBarBtn(po);

        StringBuilder opt=new StringBuilder();
        StringBuilder toolbarOpt=new StringBuilder();

        StringBuilder toolFun=new StringBuilder();
        StringBuilder dropDownFun=new StringBuilder();

        List<Map<String, Object>> dropDownBtns=new ArrayList<>();
        StringBuilder activLogic=new StringBuilder();
        StringBuilder toolBarActiveLogic=new StringBuilder();

        boolean removeBtn=false;
        boolean documentBtn=false;

        if(btnList.size()>0){
            for(Map<String, Object> btni : btnList){
                if("removeBtn".equals(btni.get(NODE_CODE))){
                    removeBtn=true;
                }
                if("documentBtn".equals(btni.get(NODE_CODE))){
                    documentBtn=true;
                }
                addBtn(opt, toolFun, activLogic, btni);
            }
            if(tableHeadBtnList.size()>0){
                btnList.removeAll(tableHeadBtnList);
                for(Map<String, Object> thBtni : tableHeadBtnList){
                    addBtn(toolbarOpt, toolFun, toolBarActiveLogic, thBtni);
                }
            }
            if(!removeBtn){
                addDropDown(dropDownBtns, "removeBtn");
                addOneBtn("removeBtn", opt, toolFun, activLogic);
            }
            if(!documentBtn){
                addDropDown(dropDownBtns, "documentBtn");
                addOneBtn("documentBtn", opt, toolFun, activLogic);
            }
            dropDownItem(btnList, dropDownBtns);
        }else{
            addDefaultDropDown(dropDownBtns);
            addDefaultBtn(opt, toolFun, activLogic);
        }

        // 是否可管理
        addManageBtn(po, opt, toolbarOpt, toolFun, activLogic, toolBarActiveLogic);
        addDefaultTableHeadBtn(toolbarOpt, toolFun, toolBarActiveLogic);

        if(dropDownBtns.isEmpty()){
            model.addAttribute("opt", opt.toString());
        }else{
            model.addAttribute("dropDownFun", dropDownFun.toString());
            dropDownInfo(model, dropDownBtns);
        }
        model.addAttribute("toolbarOpt", toolbarOpt.toString());
        model.addAttribute("toolFun", toolFun.toString());
        model.addAttribute("activLogic", activLogic.toString());

        model.addAttribute("toolBarActiveLogic", toolBarActiveLogic.toString());
        return btnList;

    }

    public void dropDownInfo(Model model, List<Map<String, Object>> dropDownBtns){
        StringBuilder sb=new StringBuilder();
        for(Map<String, Object> di : dropDownBtns){
            String functionName=string(di, ID);
            sb.append("\nif(data.id=='"+functionName+"'){\n");
            sb.append(functionName+"(currentNode);");
            sb.append("}");

            model.addAttribute("activeFun", sb.toString());
        }
        boolean isEmpty=dropDownBtns==null||dropDownBtns.isEmpty();

        LoggerTool.info(logger, "dropDownBtns==null||dropDownBtns.isEmpty()========="+isEmpty+"===");
        if(isEmpty){
            model.addAttribute("dropDwonItem", "[]");
        }else{
            LoggerTool.info(logger, "dropDownBtns==null||dropDownBtns.isEmpty()========="+isEmpty+"==="+JSON.toJSONString(dropDownBtns));
            model.addAttribute("dropDwonItem", JSON.toJSONString(dropDownBtns));
        }

    }

    public List<Map<String, Object>> wmTableToolBtn(Model model, String label, Map<String, Object> po){
        List<Map<String, Object>> btnList=listMetaBtn(po);
        List<Map<String, Object>> tableHeadBtnList=listToolBarBtn(po);

        StringBuilder opt=new StringBuilder();
        StringBuilder toolbarOpt=new StringBuilder();

        StringBuilder toolFun=new StringBuilder();
        StringBuilder dropDownFun=new StringBuilder();
        StringBuilder dropDownItems=new StringBuilder();
        StringBuilder activLogic=new StringBuilder();
        StringBuilder toolBarActiveLogic=new StringBuilder();

        boolean removeBtn=false;
        boolean documentBtn=false;

        if(btnList.size()>0){
            for(Map<String, Object> btni : btnList){
                if("removeBtn".equals(btni.get(NODE_CODE))){
                    removeBtn=true;
                }
                if("documentBtn".equals(btni.get(NODE_CODE))){
                    documentBtn=true;
                }
                addBtn(opt, toolFun, activLogic, btni);
            }
            if(tableHeadBtnList.size()>0){
                btnList.removeAll(tableHeadBtnList);
                for(Map<String, Object> thBtni : tableHeadBtnList){
                    addBtn(toolbarOpt, toolFun, toolBarActiveLogic, thBtni);
                }
            }
            if(!removeBtn){
                addOneDropDown(dropDownItems, "removeBtn");
                addOneBtn("removeBtn", opt, toolFun, activLogic);
            }
            if(!documentBtn){
                addOneDropDown(dropDownItems, "documentBtn");
                addOneBtn("documentBtn", opt, toolFun, activLogic);
            }
            dropDownItem(btnList, dropDownItems);
        }else{
            addDefaultDropDown(dropDownItems);
            addDefaultBtn(opt, toolFun, activLogic);
        }

        // 是否可管理
        addManageBtn(po, opt, toolbarOpt, toolFun, activLogic, toolBarActiveLogic);
        addDefaultTableHeadBtn(toolbarOpt, toolFun, toolBarActiveLogic);
        if(dropDownItems.isEmpty()){
            model.addAttribute("opt", opt.toString());
        }else{
            model.addAttribute("dropDownFun", dropDownFun.toString());
            model.addAttribute("dropDwonItem", "[\n"+dropDownItems.toString()+"]");
        }
        model.addAttribute("toolbarOpt", toolbarOpt.toString());
        model.addAttribute("toolFun", toolFun.toString());
        model.addAttribute("activLogic", activLogic.toString());

        model.addAttribute("toolBarActiveLogic", toolBarActiveLogic.toString());
        return btnList;

    }

    public List<Map<String, Object>> vtableToolBtn(Model model, String label, Map<String, Object> vo){
        List<Map<String, Object>> btnList=listVoBtn(vo);
        List<Map<String, Object>> tableHeadBtnList=listToolBarBtn(vo);

        StringBuilder opt=new StringBuilder();
        StringBuilder toolbarOpt=new StringBuilder();

        StringBuilder toolFun=new StringBuilder();
        StringBuilder dropDownFun=new StringBuilder();
        StringBuilder dropDownItems=new StringBuilder();
        StringBuilder activLogic=new StringBuilder();
        StringBuilder toolBarActiveLogic=new StringBuilder();

        boolean removeBtn=false;
        boolean documentBtn=false;

        if(btnList.size()>0){
            for(Map<String, Object> btni : btnList){
                if("removeBtn".equals(btni.get(NODE_CODE))){
                    removeBtn=true;
                }
                if("documentBtn".equals(btni.get(NODE_CODE))){
                    documentBtn=true;
                }
                addBtn(opt, toolFun, activLogic, btni);
            }
            if(tableHeadBtnList.size()>0){
                btnList.removeAll(tableHeadBtnList);
                for(Map<String, Object> thBtni : tableHeadBtnList){
                    addBtn(toolbarOpt, toolFun, toolBarActiveLogic, thBtni);
                }
            }
            if(!removeBtn){
                addOneDropDown(dropDownItems, "removeBtn");
                addOneBtn("removeBtn", opt, toolFun, activLogic);
            }
            if(!documentBtn){
                addOneDropDown(dropDownItems, "documentBtn");
                addOneBtn("documentBtn", opt, toolFun, activLogic);
            }
            dropDownItem(btnList, dropDownItems);
        }else{
            addDefaultDropDown(dropDownItems);
            addDefaultBtn(opt, toolFun, activLogic);
        }

        // 是否可管理
        addManageBtn(vo, opt, toolbarOpt, toolFun, activLogic, toolBarActiveLogic);
        addDefaultTableHeadBtn(toolbarOpt, toolFun, toolBarActiveLogic);
        if(dropDownItems.isEmpty()){
            model.addAttribute("opt", opt.toString());
        }else{
            model.addAttribute("dropDownFun", dropDownFun.toString());
            model.addAttribute("dropDwonItem", "[\n"+dropDownItems.toString()+"]");
            model.addAttribute("opt", opt.toString());
        }
        model.addAttribute("toolbarOpt", toolbarOpt.toString());
        model.addAttribute("toolFun", toolFun.toString());
        model.addAttribute("activLogic", activLogic.toString());

        model.addAttribute("toolBarActiveLogic", toolBarActiveLogic.toString());
        return btnList;

    }

    public List<Map<String, Object>> d2vTableBtn(Model model, String label, Map<String, Object> vo){
        List<Map<String, Object>> btnList=listVoBtn(vo);
        List<Map<String, Object>> tableHeadBtnList=listToolBarBtn(vo);

        StringBuilder opt=new StringBuilder();
        StringBuilder toolbarOpt=new StringBuilder();

        StringBuilder toolFun=new StringBuilder();
        StringBuilder dropDownFun=new StringBuilder();
        List<Map<String, Object>> dropDownBtns=new ArrayList<>();
        StringBuilder activLogic=new StringBuilder();
        StringBuilder toolBarActiveLogic=new StringBuilder();

        boolean removeBtn=false;
        boolean documentBtn=false;

        if(btnList.size()>0){
            for(Map<String, Object> btni : btnList){
                if("removeBtn".equals(btni.get(NODE_CODE))){
                    removeBtn=true;
                }
                if("documentBtn".equals(btni.get(NODE_CODE))){
                    documentBtn=true;
                }
                addBtn(opt, toolFun, activLogic, btni);
            }
            if(tableHeadBtnList.size()>0){
                btnList.removeAll(tableHeadBtnList);
                for(Map<String, Object> thBtni : tableHeadBtnList){
                    addBtn(toolbarOpt, toolFun, toolBarActiveLogic, thBtni);
                }
            }
            if(!removeBtn){
                addDropDown(dropDownBtns, "removeBtn");
                addOneBtn("removeBtn", opt, toolFun, activLogic);
            }
            if(!documentBtn){
                addDropDown(dropDownBtns, "documentBtn");
                addOneBtn("documentBtn", opt, toolFun, activLogic);
            }
            dropDownItem(btnList, dropDownBtns);
        }else{
            addDefaultDropDown(dropDownBtns);
            addDefaultBtn(opt, toolFun, activLogic);
        }

        // 是否可管理
        addManageBtn(vo, opt, toolbarOpt, toolFun, activLogic, toolBarActiveLogic);
        addDefaultTableHeadBtn(toolbarOpt, toolFun, toolBarActiveLogic);
        if(dropDownBtns.isEmpty()){
            model.addAttribute("opt", opt.toString());
        }else{
            model.addAttribute("dropDownFun", dropDownFun.toString());
            dropDownInfo(model, dropDownBtns);
        }
        model.addAttribute("toolbarOpt", toolbarOpt.toString());
        model.addAttribute("toolFun", toolFun.toString());
        model.addAttribute("activLogic", activLogic.toString());

        model.addAttribute("toolBarActiveLogic", toolBarActiveLogic.toString());
        return btnList;

    }

    public List<Map<String, Object>> pageBtn(Model model, String label, Map<String, Object> po){
        List<Map<String, Object>> btnList=listBtn(po);

        StringBuilder btnHtml=new StringBuilder();

        StringBuilder jsFun=new StringBuilder();

        if(btnList.size()>0){
            for(Map<String, Object> btni : btnList){
                addPageBtnBykey(string(btni, CODE), btnHtml, jsFun);
            }
        }

        model.addAttribute("jsFun", jsFun.toString());
        model.addAttribute("btnHtml", btnHtml.toString());
        return btnList;

    }

    private void addDefaultBtn(StringBuilder opt, StringBuilder toolFun, StringBuilder activLogic){
        addOneBtn("documentBtn", opt, toolFun, activLogic);
        addOneBtn("removeBtn", opt, toolFun, activLogic);
    }

    private void addDefaultDropDown(StringBuilder dropDownItems){
        addOneDropDown(dropDownItems, "documentBtn");
        addOneDropDown(dropDownItems, "removeBtn");
    }

    private void addDefaultDropDown(List<Map<String, Object>> dropDownItems){
        addDropDown(dropDownItems, "documentBtn");
        addDropDown(dropDownItems, "removeBtn");
    }

    private void addManageBtn(Map<String, Object> po, StringBuilder opt, StringBuilder toolbarOpt,
                              StringBuilder toolFun, StringBuilder activLogic, StringBuilder toolBarActiveLogic){
        Object object=po.get("isManage");
        if(object!=null&&"on".equals(object)){
            addOneBtn("manageBtn", toolbarOpt, toolFun, toolBarActiveLogic);
            addOneBtn("fieldBtn", opt, toolFun, activLogic);
        }
    }

    /**
     * 添加创建和删除表头按钮
     *
     * @param toolbarOpt
     * @param toolFun
     * @param toolBarActiveLogic
     */
    private void addDefaultTableHeadBtn(StringBuilder toolbarOpt, StringBuilder toolFun,
                                        StringBuilder toolBarActiveLogic){
        addOneBtn("createBtn", toolbarOpt, toolFun, toolBarActiveLogic);
        addOneBtn("delListBtn", toolbarOpt, toolFun, toolBarActiveLogic);
        addOneBtn("importDataBtn", toolbarOpt, toolFun, toolBarActiveLogic);
    }

    /*
    public List<Map<String, Object>> tableToolBtn(Model model, String label, Map<String, Object> po) {
    List<Map<String, Object>> btnList = listBtn(po);

    StringBuilder opt = new StringBuilder();
    StringBuilder toolbarOpt = new StringBuilder();

    StringBuilder toolFun = new StringBuilder();
    StringBuilder dropDownFun = new StringBuilder();
    StringBuilder dropDownItems = new StringBuilder();
    StringBuilder activLogic = new StringBuilder();
    StringBuilder toolBarActiveLogic = new StringBuilder();

    boolean removeBtn = false;

    if(btnList.size()>0) {
        addOneBtn("documentBtn", opt, toolFun, activLogic);
        for (Map<String, Object> btni : btnList) {
    	    if ("removeBtn".equals(btni.get(NODE_CODE))) {
    		removeBtn = true;
    	    }
    //		    addBtn(opt, toolFun, activLogic, btni);
         }
        if (!removeBtn) {
    	    addOneBtn("removeBtn", opt, toolFun, activLogic);
        }
    //	    dropDownMoreOpt(label, btnList, opt, dropDownFun);


    }else {
        addOneBtn("documentBtn", opt, toolFun, activLogic);
        addOneBtn("removeBtn", opt, toolFun, activLogic);
    }



    // 是否可管理
    Object object = po.get("isManage");
    if (object != null && "on".equals(object)) {
        addOneBtn("manageBtn", toolbarOpt, toolFun, toolBarActiveLogic);
        addOneBtn("fieldBtn", opt, toolFun, activLogic);
    }
    addOneBtn("createBtn", toolbarOpt, toolFun, toolBarActiveLogic);
    addOneBtn("delListBtn", toolbarOpt, toolFun, toolBarActiveLogic);

    model.addAttribute("opt", opt.toString());
    model.addAttribute("toolbarOpt", toolbarOpt.toString());
    model.addAttribute("toolFun", toolFun.toString());
    model.addAttribute("dropDownFun", dropDownFun.toString());
    model.addAttribute("activLogic", activLogic.toString());
    model.addAttribute("toolBarActiveLogic", toolBarActiveLogic.toString());
    return btnList;

    }*/

    /**
     * [{layIcon: 'layui-icon-edit', txt: '修改用户名', event:'edit'}]
     *
     * @param btnList
     * @param dropDownItems
     */
    private void dropDownItem(List<Map<String, Object>> btnList, StringBuilder dropDownItems){
        for(Map<String, Object> btni : btnList){
            addOneDropDown(dropDownItems, btni);
        }
    }

    private void dropDownItem(List<Map<String, Object>> btnList, List<Map<String, Object>> dropDownItems){
        for(Map<String, Object> btni : btnList){
            addDropDown(dropDownItems, btni);
        }
    }

    private void addOneDropDown(StringBuilder dropDownItems, Map<String, Object> btni){
        String btnName=name(btni);
        String name="";
        if(btnName!=null){
            name=btnName.replaceAll("按钮", "");
        }else{
            btnName=code(btni);
        }
        String iconString=string(btni, ICON);
        if(iconString!=null){
            name+="',layIcon:'"+iconString;
        }
        boolean b=dropDownItems.length()>0;
        if(b){
            dropDownItems.append(",\n");
        }
        dropDownItems.append("{txt: '"+name+"',event: '"+code(btni)+"'}");

    }


    private void addOneDropDown(StringBuilder dropDownItems, String key){
        Map<String, Object> btnMap=neo4jService.getAttMapBy(NODE_CODE, key, LayUIDomain.LAYUI_TABLE_TOOL_BTN);
        addOneDropDown(dropDownItems, btnMap);
    }

    private void addDropDown(List<Map<String, Object>> dropDownItems, String key){
        Map<String, Object> btnMap=neo4jService.getAttMapBy(NODE_CODE, key, LayUIDomain.LAYUI_TABLE_TOOL_BTN);
        addDropDown(dropDownItems, btnMap);
    }

    private void addDropDown(List<Map<String, Object>> dropDownItems, Map<String, Object> btni){
        if(btni==null||btni.isEmpty()){
            return;
        }
        Map<String, Object> map=new HashMap<>();
        String name2=name(btni);
        if(name2!=null){
            String name=name2.replaceAll("按钮", "");

            map.put("title", name);
        }else{
            map.put("title", code(btni));
        }

        map.put(ID, code(btni));
        dropDownItems.add(map);
    }

    private void dropDownMoreOpt(String label, List<Map<String, Object>> btnList, StringBuilder opt,
                                 StringBuilder dropDownFun){
        opt.append(
                "<button class=\"layui-btn layui-btn-sm moreOptlist\" data-id=\""+label+"moreOpt\">更多操作</button>");
        dropDownFun.append(
                " dropdown.sute({\r\n"+"		    elem: '.moreOptlist'\r\n"+"		    ,data: [");
        int bi=0;
        for(Map<String, Object> btni : btnList){
            if(bi>0){
                dropDownFun.append(",");
            }
            dropDownFun.append("{title: '"+btni.get(NAME)+"',id: '"+code(btni)+"'}");
            bi++;
        }

        dropDownFun.append("]\n,click: function(data, othis){\n");
        dropDownFun.append("   var elem = $(this.elem)");
        dropDownFun.append("  ,listId = elem.data('id'); //表格列表的预埋数据\n");
        // dropDownFun.append(" layer.msg('得到表格列表的 id：'+ listId +'，下拉菜单 id：'+
        // data.id)\n");
        for(Map<String, Object> btni : btnList){
            dropDownFun.append("   if(data.id=='"+code(btni)+"'){"+code(btni)+"(currentNode);}");
        }

        dropDownFun.append(" }\r\n"+"});\n");
    }

    private List<Map<String, Object>> listMetaBtn(Map<String, Object> po){
        Long poId=id(po);
        return listMetaBtnById(poId);
    }

    private List<Map<String, Object>> listVoBtn(Map<String, Object> po){
        Long poId=id(po);
        List<Map<String, Object>> btnList=listBtnById(poId);
        btnUrlReplace(btnList);
        return btnList;
    }

    private List<Map<String, Object>> listBtnById(Long voId){
        String entityString="match (n) -[r]->(e:layTableToolOpt) where id(n)="+voId
                +" return e";
        List<Map<String, Object>> btnList=neo4jService.cypher(entityString);
        btnUrlReplace(btnList);
        return btnList;
    }

    private List<Map<String, Object>> listMetaBtnById(Long metaId){
        String entityString="match (n:"+META_DATA+") -[r]->(e:layTableToolOpt) where id(n)="+metaId
                +" return e";
        List<Map<String, Object>> btnList=neo4jService.cypher(entityString);
        btnUrlReplace(btnList);
        return btnList;
    }

    private List<Map<String, Object>> listBtn(Map<String, Object> po){
        String entityString="match (n)-[r]->(e:layTableToolOpt) where id(n)="+id(po)+" return e";
        List<Map<String, Object>> btnList=neo4jService.cypher(entityString);
        btnUrlReplace(btnList);
        return btnList;
    }

    private List<Map<String, Object>> listToolBarBtn(Map<String, Object> po){
        String entityString="match (n) -[r:ToolBarBtn]->(e:layTableToolOpt) where id(n)="
                +id(po)+" return e";
        List<Map<String, Object>> btnList=neo4jService.cypher(entityString);
        //对JavaScript中的节点进行更新。
        btnUrlReplace(btnList);
        return btnList;
    }

    public void btnUrlReplace(List<Map<String, Object>> btnList){
        for(Map<String, Object> btn : btnList){
            validateContextPrefix(btn, "JavaScript");
            validUrlPrefix(btn);
        }
    }

    public String validateContextPrefix(Map<String, Object> btn, String key){
        String javaScript="JavaScript";

        if(!key.equals(javaScript)){
            return string(btn, key);
        }
        String js=string(btn, key);
        if(js==null){
            return null;
        }
        Map<String, Object> ci=copy(btn);
        String jsCi=string(ci, key);

        if(js.contains("\"/cd/")){
            js=js.replaceAll("\"/cd/", "\""+LemodoApplication.MODULE_NAME+"/");
            jsCi=jsCi.replaceAll("\"/cd/", "\"\\$\\{MODULE_NAME\\}/");
            jsCi=jsCi.replaceAll("'/cd/", "'\\$\\{MODULE_NAME\\}/");
            ci.put(javaScript, jsCi);
            neo4jService.update(ci);
        }
        if(js.contains("${MODULE_NAME}/")){
            js=js.replaceAll("\\$\\{MODULE_NAME}/", LemodoApplication.MODULE_NAME+"/");
        }else if(js.contains("${MODULE_NAME}")){
            js=js.replaceAll("\\$\\{MODULE_NAME}", LemodoApplication.MODULE_NAME+"/");
            jsCi=jsCi.replaceAll("\\$\\{MODULE_NAME}", "\\$\\{MODULE_NAME\\}/");
            ci.put(javaScript, jsCi);
            neo4jService.update(ci);
        }
        if(js.contains(LemodoApplication.MODULE_NAME+"/")){
            jsCi=jsCi.replaceAll(LemodoApplication.MODULE_NAME+"/", "\\$\\{MODULE_NAME\\}/");
            ci.put(javaScript, jsCi);
            neo4jService.update(ci);
        }
        btn.put(javaScript, js);
        return js;
    }

    public String validUrlPrefix(Map<String, Object> btn){
        String urlKey="url";


        String url=string(btn, urlKey);
        if(url==null){
            return null;
        }
        Map<String, Object> ci=copy(btn);
        String jsCi=string(ci, urlKey);
        if(!LemodoApplication.MODULE_NAME.equals("/cd")){
            if(url.contains("/cd/")){
                url=url.replaceAll("/cd/", LemodoApplication.MODULE_NAME+"/");
                jsCi=jsCi.replaceAll("/cd/", "\\$\\{MODULE_NAME\\}/");
                ci.put(urlKey, jsCi);
                neo4jService.update(ci);
            }
        }

        if(url.contains("${MODULE_NAME}/")){
            url=url.replaceAll("\\$\\{MODULE_NAME}/", LemodoApplication.MODULE_NAME+"/");
        }else if(url.contains("${MODULE_NAME}")){
            url=url.replaceAll("\\$\\{MODULE_NAME}", LemodoApplication.MODULE_NAME+"/");
            jsCi=jsCi.replaceAll("\\$\\{MODULE_NAME}", "\\$\\{MODULE_NAME\\}/");
            ci.put(urlKey, jsCi);
            neo4jService.update(ci);
        }else if(url.contains(LemodoApplication.MODULE_NAME+"/")){
            jsCi=jsCi.replaceAll(LemodoApplication.MODULE_NAME+"/", "\\$\\{MODULE_NAME\\}/");
            ci.put(urlKey, jsCi);
            neo4jService.update(ci);
        }
        btn.put(urlKey, url);
        return url;
    }


    private List<Map<String, Object>> listFormBtn(Map<String, Object> po){
        Long poId=id(po);
        return listFormBtnById(poId);
    }

    /**
     * 获取表单按钮
     *
     * @param poId
     * @return
     */
    private List<Map<String, Object>> listFormBtnById(Long poId){
        String entityString="match (n:"+META_DATA+") -[r:FormBtn]->(e:layTableToolOpt) where id(n)="+poId
                +" return e";
        List<Map<String, Object>> btnList=neo4jService.cypher(entityString);
        return btnList;
    }

    private List<Map<String, Object>> listWuMaFormBtnById(Long poId){
        String entityString="match (n:"+WUMA+") -[r:FormBtn]->(e:layTableToolOpt) where id(n)="+poId
                +" return e";
        List<Map<String, Object>> btnList=neo4jService.cypher(entityString);
        return btnList;
    }

    private Integer countBtn(Map<String, Object> po){
        String entityString="match (n:"+META_DATA+") -[r]->(e:layTableToolOpt) where id(n)="+po.get("id")
                +" return count(e) as countBtn";
        List<Map<String, Object>> btnList=neo4jService.cypher(entityString);

        Object object=btnList.get(0).get("countBtn");
        return Integer.valueOf(String.valueOf(object));
    }

    /**
     * 详情按钮列表
     *
     * @param model
     * @param label
     * @param instancesOnly
     */
    public void tableToolBtn(Model model, String label, Boolean instancesOnly){
        String queryString="match (n:"+label+") -[r]->(e:layTableToolOpt)  return e";
        List<Map<String, Object>> btnList=neo4jService.cypher(queryString);
        btnUrlReplace(btnList);
        StringBuilder opt=new StringBuilder();
        StringBuilder toolbarOpt=new StringBuilder();
        StringBuilder toolFun=new StringBuilder();
        StringBuilder activLogic=new StringBuilder();

        for(Map<String, Object> btni : btnList){
            handleHtml(opt, btni);
            toolFun.append(btni.get("JavaScript"));
            activLogic.append(btni.get("btnAcitive"));
        }
        if(!instancesOnly){
            addOneBtn("removeBtn", opt, toolFun, activLogic);
        }else{
            addOneBtn("removeRelBtn", opt, toolFun, activLogic);
        }

        model.addAttribute("opt", opt.toString());
        model.addAttribute("toolbarOpt", toolbarOpt.toString());
        model.addAttribute("toolFun", toolFun.toString());
        model.addAttribute("activLogic", activLogic.toString());
    }

    /**
     * 添加一个按钮
     */
    private void addOneBtn(String btnKey, StringBuilder opt, StringBuilder toolFun, StringBuilder activLogic){
        Map<String, Object> btnMap=neo4jService.getAttMapBy(NODE_CODE, btnKey, LayUIDomain.LAYUI_TABLE_TOOL_BTN);
        if(btnMap!=null&&!btnMap.isEmpty()){
            addBtn(opt, toolFun, activLogic, btnMap);
        }

    }

    private void addPageBtnBykey(String btnKey, StringBuilder btnHtml, StringBuilder jsFun){
        Map<String, Object> btnMap=neo4jService.getAttMapBy(NODE_CODE, btnKey, LayUIDomain.LAYUI_TABLE_TOOL_BTN);
        addPageBtn(btnHtml, jsFun, btnMap);
    }

    private void addPageBtn(StringBuilder opt, StringBuilder toolFun, Map<String, Object> btnMap){
        handleHtml(opt, btnMap);
        Object javascript=btnMap.get("JavaScript");
        if(javascript!=null){
            String string=code(btnMap);
            String[] funBody=string.split("()\\{\n");
            String funHead=funBody[0].trim();
            if(funHead.startsWith("function ")){
                String[] function=funHead.split(" ");
                if(toolFun.length()>1){
                    toolFun.append(",");
                }
                toolFun.append(function[1]);
                toolFun.append(":function(){\n"+funBody[1]);
            }
        }
    }

    private void addBtn(StringBuilder html, StringBuilder toolFun, StringBuilder activLogic,
                        Map<String, Object> btnMap){
        handleHtml(html, btnMap);
        appendContent(toolFun, btnMap, "JavaScript");
        String code2=code(btnMap);
        if(toolFun.toString().contains(code2+"Form")){
            toolFun.append(" $(\"#"+code2+"\").click("+code2+"Form);");
        }


        appendContent(activLogic, btnMap, "btnAcitive");
    }

    private void addFormOneBtn(StringBuilder html, StringBuilder toolFun, StringBuilder activLogic,
                        Map<String, Object> btnMap){
        handleHtml(html, btnMap);
        appendContent(toolFun, btnMap, "JavaScript");
        String code2=code(btnMap);
        toolFun.append("\n");
//        toolFun.append("layui.$('#relationPo').on('click',"+code2+"(currentNode));\n");

//        toolFun.append(" $(\"#"+code2+"\").on('click',"+code2+"(currentNode));");
    }

    private void addFormBtn(StringBuilder html, StringBuilder toolFun, StringBuilder activLogic,
                        Map<String, Object> btnMap){
        handleHtml(html, btnMap);
        appendContent(toolFun, btnMap, "JavaScript");
        String code2=code(btnMap);
        if(toolFun.toString().contains(code2+"Form")){
            toolFun.append(" $(\"#"+code2+"\").click("+code2+"Form);");
        }


        appendFormBtnContent(activLogic, btnMap, "btnAcitive");
    }

    private void addBtn(StringBuilder html, StringBuilder toolFun,
                        Map<String, Object> btnMap){
        btnHtml(html, btnMap);
        appendContent(toolFun, btnMap, "JavaScript");
        String code2=code(btnMap);
        if(toolFun.toString().contains(code2+"Form")){
            toolFun.append(" $(\"#"+code2+"\").click("+code2+"Form);");
        }

    }

    /**
     * formBtn
     *
     * @param html
     * @param toolFun
     * @param btnMap
     */
    private void addFromBtn(StringBuilder html, StringBuilder toolFun,
                            Map<String, Object> btnMap){
        formBtnHtml(html, btnMap);
        appendContent(toolFun, btnMap, "JavaScript");
        String code2=code(btnMap);
        if(toolFun.toString().contains(code2+"Form")){
            toolFun.append(" $(\"#"+code2+"\").click("+code2+"Form);");
        }
    }

    private void appendContent(StringBuilder toolFun, Map<String, Object> btnMap, String key){
        String javascript=string(btnMap, key);
        if(key.equals("JavaScript")){
            javascript=validateContextPrefix(btnMap, key);
        }

        if(javascript!=null){
            toolFun.append("\n"+javascript);
        }
    }

    private void appendFormBtnContent(StringBuilder toolFun, Map<String, Object> btnMap, String key){
        String javascript=string(btnMap, key);
        if(key.equals("JavaScript")){
            javascript=validateContextPrefix(btnMap, key);
        }

        if(javascript!=null){
            toolFun.append("\n"+javascript);
        }
    }


    private void handleHtml(StringBuilder opt, Map<String, Object> btnMap){
        String html=string(btnMap, "Html");
        if(html!=null){
            html=updateIcon(btnMap, html);
            opt.append(html);
        }
    }

    private void btnHtml(StringBuilder opt, Map<String, Object> btnMap){
        String html=string(btnMap, "Html");
        if(html!=null){
            html=updateIcon(btnMap, html);
            String[] split=html.split("lay-event");
            opt.append(split[0]+" onclick=\""+code(btnMap)+"()\" lay-event"+split[1]);
        }
    }

    private void formBtnHtml(StringBuilder opt, Map<String, Object> btnMap){
        String html=string(btnMap, "Html");
        if(html!=null){
            html=updateIcon(btnMap, html);
            opt.append(html);
        }
    }

}
