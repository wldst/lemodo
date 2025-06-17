package com.wldst.ruder.module.manage;

import java.util.Map;

import com.wldst.ruder.util.LabelValidator;
import com.wldst.ruder.util.ModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.DataBaseDomain;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.CrudUtil;

import jakarta.servlet.http.HttpServletRequest;

/**
 * po管理，页面控制器 Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/po")
public class PoManageController extends DataBaseDomain {

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
                           HttpServletRequest request) throws Exception {
        LabelValidator.validateLabel(endLabel);
        Map<String, Object> po = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
        if (po == null || po.isEmpty()) {
            throw new DefineException(endLabel + "未定义！");
        }
        ModelUtil.setKeyValue(model, po);
        if (endLabel.equalsIgnoreCase("iconFont")) {
            model.addAttribute("selectValue", "unicode");
        }
        showService.showMetaInstanceCrudPage(model, po, true);
        // table2(model, po);
//	st.createHtml("instanceSelect", model.asMap());
        return "instanceSelect";
    }

    @RequestMapping(value = "/{po}", method = {RequestMethod.GET, RequestMethod.POST})
    public String instance(Model model, @PathVariable("po") String label, HttpServletRequest request) throws Exception {
        LabelValidator.validateLabel(label);
        if (label.indexOf("+") > 0 || label.indexOf("\"") > 0) {
            return "layui/poManage";
        }
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (md == null || md.isEmpty()) {
            throw new DefineException(label + "未定义！");
        }
        ModelUtil.setKeyValue(model, md);
        showService.showMetaInstanceCrudPage(model, md, true);
//	showService.tableToolBtn(model, label, md);
        showService.d2tableToolBtn(model, label, md);
//	st.createHtml("layui/poManage", model.asMap());
        return "layui/poManage";
    }

    @RequestMapping(value = "/columnList/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public String columnsList(Model model, @PathVariable("id") String id, HttpServletRequest request) throws Exception {
        Map<String, Object> po = neo4jService.getPropMapByNodeId(Long.valueOf(id));
        if (po == null || po.isEmpty()) {
            throw new DefineException(id + "未定义！");
        }
        if (!po.containsKey(NAME)) {
            po.put(NAME, po.get("tableName"));
        }
        ModelUtil.setKeyValue(model, po);
        String[] columns = columns(po);
        String[] header = headers(po);
        String[] colSizes = splitColumnValue(po, COLUMN_SIZE);
        String[] ctypes = splitColumnValue(po, COLUMN_TYPE, TYPE_SPLITER);
        String[] nulls = splitColumnValue(po, COLUMN_NULL_ABLE);
        if (columns == null) {
//	    st.createHtml("layui/columnList", model.asMap());
            return "layui/columnList";
        }
        JSONArray data = new JSONArray();
        for (int i = 0; i < columns.length; i++) {
            JSONObject joi = new JSONObject();
            joi.put("index", i + 1);
            joi.put("columnName", columns[i]);
            joi.put("columnCode", header[i]);
            if (ctypes != null && ctypes.length >= i) {
                joi.put("columnType", ctypes[i]);
            }

            if (colSizes != null) {
                joi.put("columnSize", colSizes[i]);
            }
            if (nulls != null) {
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
    public String orderColumn(Model model, @PathVariable("id") String id, HttpServletRequest request) throws Exception {
        Map<String, Object> po = neo4jService.getPropMapByNodeId(Long.valueOf(id));
        if (po == null || po.isEmpty()) {
            throw new DefineException(id + "未定义！");
        }
        if (!po.containsKey(NAME)) {
            po.put(NAME, po.get("tableName"));
        }
        ModelUtil.setKeyValue(model, po);
        String[] columns = columns(po);
        String[] header = headers(po);
        String[] colSizes = splitColumnValue(po, COLUMN_SIZE);
        String[] ctypes = splitColumnValue(po, COLUMN_TYPE, TYPE_SPLITER);
        String[] nulls = splitColumnValue(po, COLUMN_NULL_ABLE);

        if (columns == null) {
            return "vue/orderColumn";
        }
        JSONArray data = new JSONArray();
        for (int i = 0; i < columns.length; i++) {
            JSONObject joi = new JSONObject();
            joi.put("index", i + 1);
            String hi = header[i];
            if (hi.contains("[") || hi.contains("]")) {
                String replaceAll = hi.replaceAll("\\[", "").replaceAll("\\]", "");
                joi.put("columnName", replaceAll);
            } else {
                joi.put("columnName", hi);
            }

            String ci = columns[i];
            if (ci.contains("[") || ci.contains("]")) {
                String replaceAll2 = ci.replaceAll("\\[", "").replaceAll("\\]", "");
                joi.put("columnCode", replaceAll2);
            } else {
                joi.put("columnCode", ci);
            }

            if (ctypes != null && ctypes.length >= i) {
                joi.put("columnType", ctypes[i]);
            }

            if (colSizes != null) {
                joi.put("columnSize", colSizes[i]);
            }
            if (nulls != null) {
                joi.put("nullAble", nulls[i]);
            }

            data.add(joi);
        }
        model.addAttribute("data", data);
//	st.createHtml("layui/columnList", model.asMap());
        return "vue/orderColumn";
    }

}
