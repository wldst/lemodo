package com.wldst.ruder.module.manage;

import static com.wldst.ruder.constant.CruderConstant.COLUMNS;
import static com.wldst.ruder.constant.CruderConstant.END_LABEL;
import static com.wldst.ruder.constant.CruderConstant.HEADER;
import static com.wldst.ruder.constant.CruderConstant.LABEL;
import static com.wldst.ruder.constant.CruderConstant.META_DATA;
import static com.wldst.ruder.constant.CruderConstant.PO_PROP_SPLITER;
import static com.wldst.ruder.constant.CruderConstant.RELATION_LABEL;
import static com.wldst.ruder.constant.CruderConstant.START_LABEL;
import static com.wldst.ruder.constant.CruderConstant.VO_COLUMN;
import static com.wldst.ruder.constant.Msg.SAVE_FAILED;
import static com.wldst.ruder.constant.Msg.SAVE_SUCCESS;
import static com.wldst.ruder.domain.SystemDomain.MICRO_SERVICE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.util.*;
import org.neo4j.graphdb.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.DataBaseDomain;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.exception.DefineException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * po管理，页面控制器 Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/md")
public class MetaDataManageController extends DataBaseDomain{

    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private RuleDomain rule;
    @Autowired
    private HtmlShowService showService;

    @RequestMapping(value = "/select/{toPo}", method = {RequestMethod.GET, RequestMethod.POST})
    public String selectPo(Model model, @PathVariable("toPo") String endLabel,
                           HttpServletRequest request) throws Exception{
        LabelValidator.validateLabel(endLabel);
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
        if(po==null||po.isEmpty()){
            throw new DefineException(endLabel+"未定义！");
        }
        ModelUtil.setKeyValue(model, po);
        if(endLabel.equalsIgnoreCase("iconFont")){
            model.addAttribute("selectValue", "unicode");
        }
        showService.showMetaInstanceCrudPage(model, po, true);
        // table2(model, po);
        //	st.createHtml("instanceSelect", model.asMap());
        return "instanceSelect";
    }

    @RequestMapping(value = "/{po}", method = {RequestMethod.GET, RequestMethod.POST})
    public String instance(Model model, @PathVariable("po") String label, HttpServletRequest request) throws Exception{
        LabelValidator.validateLabel(label);
        if(label.indexOf("+")>0||label.indexOf("\"")>0){
            return "layui/metaDataManage";
        }
        Map<String, Object> metaData=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(metaData==null||metaData.isEmpty()){
            throw new DefineException(label+"未定义！");
        }
        ModelUtil.setKeyValue(model, metaData);
        StringBuilder jsvars=new StringBuilder();
        jsvars.append("var mySessionId='"+request.getRequestedSessionId()+"';");
        Map<String, Object> attMapBy=neo4jService.getAttMapBy(NAME, "sign", MICRO_SERVICE);

        String signBaseUrl=string(attMapBy, "baseUrl");

        if(signBaseUrl==null){
            signBaseUrl="http://127.0.0.1:19300/sign";
        }
        jsvars.append("var docxHtmlUrl='"+signBaseUrl+"/signDocument/docxHtml/';\n");
        jsvars.append("var showHtmlUrl='"+signBaseUrl+"/signDocument/showHtml/';");


        showService.showMetaInstanceCrudPage(model, metaData, true);
        showService.d2tableToolBtn(model, label, metaData);
        model.addAttribute("jsvars", jsvars.toString());
//	st.createHtml("layui/poManage", model.asMap());
        return "layui/metaDataManage";
    }

    @RequestMapping(value = "/columnList/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public String columnsList(Model model, @PathVariable("id") String id, HttpServletRequest request) throws Exception{
        Map<String, Object> po=neo4jService.getPropMapByNodeId(Long.valueOf(id));
        if(po==null||po.isEmpty()){
            throw new DefineException(id+"未定义！");
        }
        if(!po.containsKey(NAME)){
            po.put(NAME, po.get("tableName"));
        }
        ModelUtil.setKeyValue(model, po);
        String[] columns=columns(po);
        String[] header=headers(po);
        String[] colSizes=splitColumnValue(po, COLUMN_SIZE);
        String[] ctypes=splitColumnValue(po, COLUMN_TYPE, TYPE_SPLITER);
        String[] nulls=splitColumnValue(po, COLUMN_NULL_ABLE);
        if(columns==null){
//	    st.createHtml("layui/columnList", model.asMap());
            return "layui/columnList";
        }
        JSONArray data=new JSONArray();
        for(int i=0; i<columns.length; i++){
            JSONObject joi=new JSONObject();
            joi.put("index", i+1);
            joi.put("columnName", columns[i]);
            joi.put("columnCode", header[i]);
            if(ctypes!=null&&ctypes.length>=i){
                joi.put("columnType", ctypes[i]);
            }

            if(colSizes!=null){
                joi.put("columnSize", colSizes[i]);
            }
            if(nulls!=null){
                joi.put("nullAble", nulls[i]);
            }

            data.add(joi);
        }
        model.addAttribute("data", data);
//	st.createHtml("layui/columnList", model.asMap());
        return "layui/columnList";
    }

    /**
     * 字段排序
     *
     * @param model
     * @param id
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/orderColumn/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public String orderColumn(Model model, @PathVariable("id") String id, HttpServletRequest request) throws Exception{
        Map<String, Object> po=neo4jService.getPropMapByNodeId(Long.valueOf(id));
        if(po==null||po.isEmpty()){
            throw new DefineException(id+"未定义！");
        }
        if(!po.containsKey(NAME)){
            po.put(NAME, po.get("tableName"));
        }
        ModelUtil.setKeyValue(model, po);
        String[] columns=columns(po);
        String[] header=headers(po);
        String[] colSizes=splitColumnValue(po, COLUMN_SIZE);
        String[] ctypes=splitColumnValue(po, COLUMN_TYPE, TYPE_SPLITER);
        String[] nulls=splitColumnValue(po, COLUMN_NULL_ABLE);

        if(columns==null){
            return "vue/orderColumn";
        }
        JSONArray data=new JSONArray();
        for(int i=0; i<columns.length; i++){
            JSONObject joi=new JSONObject();
            joi.put("index", i+1);
            String hi=header[i];
            if(hi.contains("[")||hi.contains("]")){
                String replaceAll=hi.replaceAll("\\[", "").replaceAll("\\]", "");
                joi.put("columnName", replaceAll);
            }else{
                joi.put("columnName", hi);
            }

            String ci=columns[i];
            if(ci.contains("[")||ci.contains("]")){
                String replaceAll2=ci.replaceAll("\\[", "").replaceAll("\\]", "");
                joi.put("columnCode", replaceAll2);
            }else{
                joi.put("columnCode", ci);
            }

            if(ctypes!=null&&ctypes.length>=i){
                joi.put("columnType", ctypes[i]);
            }

            if(colSizes!=null){
                joi.put("columnSize", colSizes[i]);
            }
            if(nulls!=null){
                joi.put("nullAble", nulls[i]);
            }

            data.add(joi);
        }
        model.addAttribute("data", data);
//	st.createHtml("layui/columnList", model.asMap());
        return "vue/orderColumn";
    }

    /**
     * 元数据可视化定义
     *
     * @param model
     * @param label
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}/showDefine", method = {RequestMethod.GET, RequestMethod.POST})
    public String showDefine(Model model, @PathVariable("po") String label, HttpServletRequest request)
            throws Exception{
        LabelValidator.validateLabel(label);
        Map<String, Object> md=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(md==null||md.isEmpty()){
            throw new DefineException(label+"未定义！");
        }
        ModelUtil.setKeyValue(model, md);
        String formContent="showForm,shortShowForm,roleShowForm,userShowForm";
        showService.showDefine(model, md, formContent);
        return "layui/showDefine";
    }


    /**
     * 视图对象新增
     *
     * @param vo
     * @return
     */
    @RequestMapping(value = "/{label}/saveShow", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult saveShow(@PathVariable("label") String label, @RequestBody JSONObject vo) throws DefineException{
        LabelValidator.validateLabel(label);
        if(vo.isEmpty()){
            return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
        }
        // 主实体
        Map<String, Object> metaInfo=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        String type2=type(vo);
//	Map<String, String> columnHeaderMap = mapColHead(metaInfo);
        Map<String, Object> copy=copy(vo);
        copy.remove(TYPE);
        List<String> kis=new ArrayList<>();
        Map<String, Object> map=new HashMap<>();
        for(Entry<String, Object> ki : copy.entrySet()){
            String key2=ki.getKey();
            Object value2=ki.getValue();
            if(Boolean.valueOf(String.valueOf(value2))){
                kis.add(key2.split("-_-")[1]);
            }else{
                map.put(key2, value2);
            }
        }
        String selectedColumns=String.join(",", kis);
        metaInfo.put(type2, selectedColumns);
        if("role".equals(type2)){
            String ci="MATCH(s:Role),(e:MetaData) "+
                    " where id(e)="+id(metaInfo)+" and e.label=\""+label+"\" and id(s)="+id(vo)
                    +" create (s)-[r:visible{columns:\""+selectedColumns+"\"}]->(e)";
            neo4jService.execute(ci);
        }else if("user".equals(type2)){
            String c2="MATCH(s:User),(e:MetaData)  "+
                    " where id(e)="+id(metaInfo)+" and e.label=\""+label+"\" and  id(s)="+id(vo)
                    +" create (s)-[r:visible{columns:\""+selectedColumns+"\"}]->(e)";
            neo4jService.execute(c2);
        }else{
            neo4jService.save(metaInfo);
        }
        return ResultWrapper.wrapResult(true, metaInfo, null, SAVE_SUCCESS);
    }

    /**
     * 映射主实体的字段与表头
     *
     * @param mainPo
     * @return
     */
    private Map<String, String> mapColHead(Map<String, Object> mainPo){
        String[] columnArray=columns(mainPo);
        String[] headers=headers(mainPo);
        // 主实体字段映射
        Map<String, String> mainPofieldMap=new HashMap<>();
        for(int i=0; i<headers.length; i++){
            mainPofieldMap.put(columnArray[i], headers[i]);
        }

        return mainPofieldMap;
    }

}
