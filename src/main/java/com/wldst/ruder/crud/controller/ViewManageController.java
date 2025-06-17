package com.wldst.ruder.crud.controller;

import static com.wldst.ruder.constant.CruderConstant.LABEL;
import static com.wldst.ruder.constant.CruderConstant.META_DATA;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.util.LabelValidator;
import com.wldst.ruder.util.ModelUtil;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.CrudUtil;

/**
 * domain控制器 Created by liuqiang（wldst）.
 */
@Controller
@RequestMapping("/page")
public class ViewManageController {
    @Autowired
    private CrudNeo4jService neo4jService;

    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private RuleDomain rule;

    @RequestMapping(value = "/{po}", method = { RequestMethod.GET, RequestMethod.POST })
    public String instance(Model model, @PathVariable("po") String label, HttpServletRequest request) throws Exception {
			LabelValidator.validateLabel(label);
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
		ModelUtil.setKeyValue(model, po);
		table2(model, po);
	return "instance";
    }



	private void table2(Model model, Map<String, Object> po) {
	if (po.containsKey("header")) {
	    String retColumns = String.valueOf(po.get("columns"));
	    String header = String.valueOf(po.get("header"));
	    String[] columnArray = crudUtil.getColumns(retColumns);
	    String[] headers = crudUtil.getColumns(header);
	    // List<String> props = new ArrayList<>();
	    List<Map<String, String>> cols = new ArrayList<>();
	    StringBuilder sbbBuilder = new StringBuilder();
	    sbbBuilder.append("<div  class=\"layui-form-item\">");
	    for (int i = 0; i < headers.length; i++) {
		Map<String, String> piMap = new HashMap<>();
		piMap.put("code", "{field:'" + columnArray[i] + "', sort: true}");
		piMap.put("name", headers[i]);
		piMap.put("field", columnArray[i]);
		cols.add(piMap);
		sbbBuilder.append(layFormItem(columnArray[i], headers[i]));
	    }
	    sbbBuilder.append("</div>");
	    model.addAttribute("formContent", sbbBuilder.toString());
	    model.addAttribute("cols", cols);
	    model.addAttribute("colCodes", columnArray);
	}
    }

    private void table(Model model, Map<String, Object> po) {
	if (po.containsKey("header")) {
	    String retColumns = String.valueOf(po.get("columns"));
	    String header = String.valueOf(po.get("header"));
	    String[] columnArray = crudUtil.getColumns(retColumns);
	    String[] headers = crudUtil.getColumns(header);
	    // List<String> props = new ArrayList<>();
	    List<Map<String, String>> cols = new ArrayList<>();
	    StringBuilder sbbBuilder = new StringBuilder();
	    sbbBuilder.append("<div  class=\"layui-form-item\">");
	    for (int i = 0; i < headers.length; i++) {
		Map<String, String> piMap = new HashMap<>();
		piMap.put("code", "{field:'" + columnArray[i] + "', sort: true}");
		piMap.put("name", headers[i]);
		cols.add(piMap);
		sbbBuilder.append(layFormItem(columnArray[i], headers[i]));
	    }
	    sbbBuilder.append("</div>");
	    model.addAttribute("cols", cols);
	    model.addAttribute("colCodes", columnArray);
	    model.addAttribute("formContent", sbbBuilder.toString());
	}
    }

    /**
     * <label class=\"layui-form-label\" th:text=\""+name+"\"></label>: <div
     * class=\"layui-input-inline\"> <input th:name=\""+code+"\"
     * class=\"layui-input\" th:id=\""+code+"\" placeholder=\"请输入"+name+"\"
     * autocomplete=\"off\"> </div>
     * 
     * @param code
     * @param name
     * @return
     */
    public String layFormItem(String code, String name) {
	StringBuilder sb = new StringBuilder();
	sb.append(" <label  class=\"layui-form-label\" >" + name + "</label>");
	sb.append("	<div class=\"layui-input-inline\">");
	sb.append("		<input name=\"" + code + "\" class=\"layui-input\" id=\"" + code + "\"");
	sb.append("			placeholder=\"请输入" + name + "\" autocomplete=\"off\">");
	sb.append("	</div>");
	return sb.toString();
    }

}
