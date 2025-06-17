package com.wldst.ruder.module.manage;

import java.util.Map;

import com.wldst.ruder.util.ModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.domain.DataBaseDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.MapTool;

import jakarta.servlet.http.HttpServletRequest;

/**
 * po管理，页面控制器 Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/form")
public class FormManageController extends DataBaseDomain{

    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private HtmlShowService showService;
    
    @RequestMapping(value = "/{id}/field", method = { RequestMethod.GET, RequestMethod.POST })
    public String columnsManage(Model model, @PathVariable("id") String id, HttpServletRequest request) throws Exception {
	Map<String, Object> po = neo4jService.getPropLabelByNodeId(Long.valueOf(id));
	if (po == null || po.isEmpty()) {
	    throw new DefineException(id + "未定义！");
	}
	model.addAttribute(POLABEL_ID, MapTool.string(po,LABEL)+"Id");
	ModelUtil.setKeyValue(model, po);
//	st.createHtml("layui/formField", model.asMap());
	return "layui/formField";
    }
    
    @RequestMapping(value = "/{po}/readOnly", method = { RequestMethod.GET, RequestMethod.POST })
    public String editForm(Model model, @PathVariable("po") String label, HttpServletRequest request) throws Exception {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	ModelUtil.setKeyValue(model, po);
	showService.readOnlyForm(model, po, true);
	showService.columnInfo(model, po, false);
	return "layui/readOnlyForm";
    }

}
