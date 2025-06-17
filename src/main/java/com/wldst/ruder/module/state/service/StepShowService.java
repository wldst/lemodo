package com.wldst.ruder.module.state.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.crud.service.CrudNeo4jDriver;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.ObjectService;
import com.wldst.ruder.crud.service.ViewService;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.util.CommonUtil;
import com.wldst.ruder.util.MapTool;

@Service
public class StepShowService extends MapTool{
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private ObjectService objectService;
    @Autowired
    private CrudNeo4jDriver driver;
    @Autowired
    private ViewService vService;

    

    private void appendSubmitResetBtn(StringBuilder sb) {
	sb.append("<div class=\"layui-form-item\">");
	sb.append("<div class=\"layui-input-block\">");
	sb.append("  <button class=\"layui-btn\" lay-submit lay-filter=\"submit-form\">提交</button>");
	sb.append("  <button type=\"reset\" class=\"layui-btn layui-btn-primary\">重置</button>");
	sb.append("</div>");
	sb.append(" </div>");
    }

    private void appendNextStepBtn(StringBuilder sb, String stepi, String bizForm) {
	sb.append("form.on('submit(" + stepi + ")', function (data) {");
	sb.append("        step.next('#" + bizForm + "');");
	sb.append("        return false;");
	sb.append("    });");
    }

    private void appendNextPreStepBtn(StringBuilder sb, String bizForm) {
	sb.append(" $('.pre').click(function () {");
	sb.append("      step.pre('#" + bizForm + "');");
	sb.append(" });");

	sb.append("    $('.next').click(function () {");
	sb.append("    step.next('#" + bizForm + "');");
	sb.append("    });	");

    }
    
}
