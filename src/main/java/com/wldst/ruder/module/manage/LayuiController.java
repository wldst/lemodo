package com.wldst.ruder.module.manage;

import java.util.List;
import java.util.Map;

import com.wldst.ruder.util.ModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.crud.controller.BaseLayuiController;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/layui")
public class LayuiController extends BaseLayuiController{
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private HtmlShowService showService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private Neo4jOptByUser optByUserSevice;

    @RequestMapping(value = "/po", method = {RequestMethod.GET, RequestMethod.POST})
    public String po(Model model, String table, HttpServletRequest request) throws Exception{
        return "layui/po";
    }

    @RequestMapping(value = "/calendar", method = {RequestMethod.GET, RequestMethod.POST})
    public String domainCalendar(Model model, String table, HttpServletRequest request) throws Exception{
        return "layui/calendar/domainCalendar";
    }

    @RequestMapping(value = "/booking", method = {RequestMethod.GET, RequestMethod.POST})
    public String bookingOrder(Model model, String table, HttpServletRequest request) throws Exception{
        return "layui/calendar/bookingManage";
    }

    @RequestMapping(value = "/{po}/timelineData", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult poTimeline(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException{
        String[] columns=crudUtil.getMdColumns(label);
        String query=optByUserSevice.queryObj(vo, label, columns);
        List<Map<String, Object>> query2=neo4jService.query(query, vo);
        return ResultWrapper.ret(true, query2, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/{po}/timeline", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public String timeline(Model model, @PathVariable("po") String metai, @RequestBody JSONObject vo) throws DefineException{
        Map<String, Object> metaData=neo4jService.getAttMapBy(LABEL, metai, META_DATA);
        if(metaData==null||metaData.isEmpty()){
            throw new DefineException(metai+"未定义！");
        }
        ModelUtil.setKeyValue(model, metaData);
        return "layui/timeLine";
    }

    @RequestMapping(value = "/design", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public String designTable(Model model) throws DefineException{
        return "layui/design";
    }

    @RequestMapping(value = "/workBench", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public String workBench(Model model) throws DefineException{
        return "layui/manage/manageBench";
    }

    @RequestMapping(value = "/fileUploadTest", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public String fileUploadTest(Model model) throws DefineException{
        return "layui/fileUploadTest";
    }

    @RequestMapping(value = "/designData/{metai}", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public Result designData(Model model, @PathVariable("metai") String metai, @RequestBody JSONObject vo) throws DefineException{
        Map<String, Object> metaData=neo4jService.getAttMapBy(LABEL, metai, META_DATA);
        if(metaData==null||metaData.isEmpty()){
            throw new DefineException(metai+"未定义！");
        }
        ModelUtil.setKeyValue(model, metaData);
        return Result.success(null, metai);
    }

    /**
     * 编辑表单
     *
     * @param model
     * @param label
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}/form", method = {RequestMethod.GET, RequestMethod.POST})
    public String editForm(Model model, @PathVariable("po") String label, HttpServletRequest request) throws Exception{
        Map<String, Object> md=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(md==null||md.isEmpty()){
            throw new DefineException(label+"未定义！");
        }
        String dataId=request.getParameter("id");
        if(dataId!=null){
            model.addAttribute("currentId", dataId);
        }

        ModelUtil.setKeyValue(model, md);
        showService.editForm(model, md, true);
        showService.columnInfo(model, md, false);
        String idString=String.valueOf(md.get(ID));
        embedListParse(model, idString);
        String one=neo4jService.getOne("Match(t:TreeDefine) where t.mdId='"+id(md)+"' return t.parentIdField AS parentIdField", "parentIdField");
        model.addAttribute("parentField", one);
        Map<String, Object> i3Data=neo4jService.getThirdInterface(idString);
        if(i3Data!=null){
            ModelUtil.setKeyValue(model, i3Data);
            return "layui/editForm3";
        }

        // 获取场景数据，以及对应的按钮数据。
        String queryMetaBtn="MATCH(md:MetaData)-[r]->(b:formBtn) where md.label='"+label+"' return b";
        List<Map<String, Object>> query=neo4jService.cypher(queryMetaBtn);

        showService.formBtn(model, query);

        return "layui/editForm";
    }

    @RequestMapping(value = "/{po}/form/{dataId}", method = {RequestMethod.GET, RequestMethod.POST})
    public String editForm(Model model, @PathVariable("po") String label, @PathVariable("dataId") String dataId, HttpServletRequest request) throws Exception{
        Map<String, Object> md=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(md==null||md.isEmpty()){
            throw new DefineException(label+"未定义！");
        }
        Map<String, Object> nodeMapById=neo4jService.getNodeMapById(dataId);

        if(dataId!=null&&nodeMapById!=null){
            model.addAttribute("currentId", dataId);
        }

        ModelUtil.setKeyValue(model, md);
        showService.editForm(model, md, true);
        showService.columnInfo(model, md, false);
        String idString=String.valueOf(md.get(ID));
        embedListParse(model, idString);
        String one=neo4jService.getOne("Match(t:TreeDefine) where t.mdId='"+id(md)+"' return t.parentIdField AS parentIdField", "parentIdField");
        model.addAttribute("parentField", one);
        Map<String, Object> i3Data=neo4jService.getThirdInterface(idString);
        if(i3Data!=null){
            ModelUtil.setKeyValue(model, i3Data);
            return "layui/editForm3";
        }

        // 获取场景数据，以及对应的按钮数据。
        String queryMetaBtn="MATCH(md:MetaData)-[r]->(b:formBtn) where md.label='"+label+"' return b";
        List<Map<String, Object>> query=neo4jService.cypher(queryMetaBtn);

        showService.formBtn(model, query);

        return "layui/editForm";
    }


    @RequestMapping(value = "/{po}/readForm", method = {RequestMethod.GET, RequestMethod.POST})
    public String readOnlyForm(Model model, @PathVariable("po") String label, HttpServletRequest request) throws Exception{
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null||po.isEmpty()){
            throw new DefineException(label+"未定义！");
        }
        ModelUtil.setKeyValue(model, po);
        showService.editForm(model, po, true);
        showService.columnInfo(model, po, false);
        String idString=String.valueOf(po.get(ID));
        embedListParse(model, idString);

        Map<String, Object> i3Data=neo4jService.getThirdInterface(idString);
        if(i3Data!=null){
            ModelUtil.setKeyValue(model, i3Data);
            return "layui/editForm3";
        }
        return "layui/editForm";
    }

    /**
     * 解析嵌套字段
     *
     * @param model
     * @param idString
     */
    private void embedListParse(Model model, String idString){
        List<Map<String, Object>> oneRelationList=neo4jService.getOneRelationList(Long.valueOf(idString),
                "parseData");
        if(oneRelationList!=null&&!oneRelationList.isEmpty()){
            StringBuilder changeForm=new StringBuilder();
            StringBuilder bcf=new StringBuilder();

            changeForm.append(" function formDataChange(formData){");
            int i=0;
            for(Map<String, Object> map : oneRelationList){
                Map<String, Object> endMap=(Map<String, Object>) map.get(RELATION_ENDNODE_PROP);
                String columns=String.valueOf(endMap.get(COLUMNS));
                List<Map<String, Object>> listAllByLabel=neo4jService
                        .listAllByLabel(String.valueOf(endMap.get(LABEL)));
                // map.get(changeForm)
                changeForm.append("\n var selChanForm=formData['parseData"+i+"'];");
                for(Map<String, Object> datai : listAllByLabel){
                    changeForm.append("\n if(selChanForm=='"+datai.get("code")+"') {\n");
                    for(String key : columns.split(",")){
                        if(" id name code primaryKey ".indexOf(key)==-1){
                            changeForm.append("\n formData['"+key+"']='"+String.valueOf(datai.get(key))+"';\n");

                        }
                    }
                    changeForm.append(" }");
                }

                bcf.append(" formData['parseData"+i+"']=$('#parseData"+i+"').val();\n");
                i++;
            }
            changeForm.append(" }");
            changeForm.append("\nformDataChange(formData);\n");
            bcf.append(changeForm.toString());
            model.addAttribute("formDataChange", bcf.toString());
        }
    }

    /**
     * 档案
     *
     * @param model
     * @param label
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}/document", method = {RequestMethod.GET, RequestMethod.POST})
    public String document(Model model, @PathVariable("po") String label, HttpServletRequest request) throws Exception{
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null||po.isEmpty()){
            po=neo4jService.getLablePropBy(label);
            if(po==null||po.isEmpty()){
                throw new DefineException(label+"未定义！");
            }else{
                po=neo4jService.getAttMapBy(LABEL, String.valueOf(po.get(LABEL)), META_DATA);
            }
        }
        String dataId=request.getParameter("id");
        if(dataId!=null){
            model.addAttribute("currentId", dataId);
        }

        ModelUtil.setKeyValue(model, po);
        showService.document(model, po);
        return "layui/document";
    }

    @RequestMapping(value = "/{po}/documentRel", method = {RequestMethod.GET, RequestMethod.POST})
    public String documentRel(Model model, @PathVariable("po") String label, HttpServletRequest request)
            throws Exception{
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null||po.isEmpty()){
            po=neo4jService.getLablePropBy(label);
            if(po==null||po.isEmpty()){
                throw new DefineException(label+"未定义！");
            }else{
                po=neo4jService.getAttMapBy(LABEL, String.valueOf(po.get(LABEL)), META_DATA);
            }
        }


        String dataId=request.getParameter("id");
        if(dataId!=null){
            model.addAttribute("currentId", dataId);
        }

        ModelUtil.setKeyValue(model, po);
        showService.documentReadOnly(model, po);
        return "layui/documentRel";
    }

    @RequestMapping(value = "/{po}/{id}/documentRel", method = {RequestMethod.GET, RequestMethod.POST})
    public String documentRel(Model model, @PathVariable("po") String label,
                              @PathVariable("id") String id,
                              HttpServletRequest request)
            throws Exception{
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null||po.isEmpty()){
            try{
                po=neo4jService.getLablePropBy(label);
            }catch(Exception e){
                Map<String, Object> data=neo4jService.getNodeMapById(id);
                List<String> listStr=listStr(data, "Mark-label");
                listStr.remove(label);
                if(!listStr.contains(label)&&listStr.size()>0){
                    label=listStr.get(0);
                    po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
                }
            }
            if(po==null||po.isEmpty()){
                throw new DefineException(label+"未定义！");
            }else{
                po=neo4jService.getAttMapBy(LABEL, String.valueOf(po.get(LABEL)), META_DATA);
            }

        }

        if(id!=null){
            model.addAttribute("currentId", id);
        }

        ModelUtil.setKeyValue(model, po);
        showService.documentReadOnly(model, po);
        return "layui/documentRel";
    }

    @RequestMapping(value = "/{po}/documentRead", method = {RequestMethod.GET, RequestMethod.POST})
    public String documentReadOnly(Model model, @PathVariable("po") String label, HttpServletRequest request)
            throws Exception{
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null||po.isEmpty()){
            po=neo4jService.getLablePropBy(label);
            if(po==null||po.isEmpty()){
                throw new DefineException(label+"未定义！");
            }else{
                po=neo4jService.getAttMapBy(LABEL, String.valueOf(po.get(LABEL)), META_DATA);
            }
        }


        String dataId=request.getParameter("id");
        if(dataId!=null){
            model.addAttribute("currentId", dataId);
        }

        ModelUtil.setKeyValue(model, po);
        showService.documentReadOnly(model, po);
        return "layui/documentRead";
    }

    /**
     * 视图定义
     *
     * @param model
     * @param label
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}/viewDefine", method = {RequestMethod.GET, RequestMethod.POST})
    public String viewDefine(Model model, @PathVariable("po") String label, HttpServletRequest request)
            throws Exception{
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null||po.isEmpty()){
            throw new DefineException(label+"未定义！");
        }
        ModelUtil.setKeyValue(model, po);
        showService.viewDefine(model, po);
        return "layui/viewDefine";
    }

    /**
     * 详情
     *
     * @param model
     * @param label
     * @param id
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}/{id}/detail", method = {RequestMethod.GET, RequestMethod.POST})
    public String detail(Model model, @PathVariable("po") String label, @PathVariable("id") String id,
                         HttpServletRequest request) throws Exception{
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null||po.isEmpty()){
            throw new DefineException(label+"未定义！");
        }
        ModelUtil.setKeyValue(model, po);
        model.addAttribute("currentId", id);

        showService.showNodeFormPage(model, po, true);
        return "layui/instanceDetail";
    }

    /**
     * 实例相关数据的不同维度展现
     *
     * @param model
     * @param label
     * @param id
     * @param relLabel
     * @param relPo
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{po}/{id}/{relLabel}/{relPo}", method = {RequestMethod.GET, RequestMethod.POST})
    public String relInstance(Model model, @PathVariable("po") String label, @PathVariable("id") String id,
                              @PathVariable("relLabel") String relLabel, @PathVariable("relPo") String relPo, HttpServletRequest request)
            throws Exception{
        Map<String, Object> po=neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if(po==null||po.isEmpty()){
            throw new DefineException(label+"未定义！");
        }
        Map<String, Object> relMap=neo4jService.getAttMapBy(LABEL, relPo, META_DATA);
        if(relMap==null||relMap.isEmpty()){
            throw new DefineException(relPo+"未定义！");
        }
        ModelUtil.setKeyValue(model, relMap);
        model.addAttribute("startLabel", label);
        model.addAttribute("startId", id);
        model.addAttribute("relLabel", relLabel);

        showService.showMetaInstanceCrudPage(model, relMap, true);
        // tableTemplate(model, columnArray, headers,customType);
        showService.tableToolBtn(model, label, true);
        return "layui/instanceOnly";
    }
}
