package com.wldst.ruder.module.state.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wldst.ruder.util.ModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.FormShowService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.module.database.DbInfoService;
import com.wldst.ruder.domain.StepDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.state.service.StepShowService;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.MapTool;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 活动控制器：查询活动实例，包含步骤信息。 定义活动步骤。 创建活动，创建项目。
 * 
 * @author wldst
 *
 */
@Controller
@RequestMapping("${server.context}/activity")
public class ActivityController extends StepDomain {

    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private StepShowService stepShowService;
    @Autowired
    private HtmlShowService showService;
    @Autowired
    private  FormShowService formShowService;
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private DbInfoService dbInfoGather;

    /**
     * 查询活动步骤
     * 
     * @param model
     * @param actId
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{actId}", method = { RequestMethod.GET, RequestMethod.POST })
    public String instance(Model model, @PathVariable("actId") String actId, HttpServletRequest request)
	    throws Exception {
	Map<String, Object> tableMap = neo4jService.getAttMapBy(LABEL, STEP, META_DATA);
	if (tableMap == null || tableMap.isEmpty()) {
	    throw new DefineException("STEP未定义！");
	}

	String[] columns = splitValue(tableMap, COLUMNS);

//	Map<String, Object> bizMap = neo4jService.getAttMapBy(ID, actId, BIZ_ACTIVITY);
	String cypher = "match(s:" + STEP + ") where s.activityId='" + actId + "' return "
		+ Neo4jOptCypher.returnColumn("s", columns) + " order by s.orderNum";

	List<Map<String, Object>> steps = neo4jService.cypher(cypher);
	StringBuilder sBuilder = new StringBuilder();
	int i = 0;
	StringBuilder titles = new StringBuilder();
	StringBuilder stepJs = new StringBuilder();
	 Set<String> layUseInfo = new HashSet<>();
	 layUseInfo.add(STEP);
	for (Map<String, Object> si : steps) {
	    String poLabeli = MapTool.string(si, "poLabel");
	    String stepFormi="";
	    if(poLabeli!=null) {
		stepFormi = formShowService.stepForm(model,poLabeli,layUseInfo);
	    }
	    
	    Map<String, Object> attMapBy = neo4jService.getAttMapBy(LABEL, poLabeli, META_DATA);
	    String stepName = name(si);
	    stepTitle(titles, stepName);
	    nextJs(i, stepJs);	    
	    
	    if (i == 0) {
		sBuilder.append("""
			<div>
			       <form class="layui-form" style="margin: 0 auto;max-width: 460px;padding-top: 40px;">
			       <form class="layui-form"
				""");
		
		sBuilder.append("  id=\"step"+i+"\" lay-filter=\"step"+
		i+"\">");
		sBuilder.append(stepFormi+"</form>");
				
		sBuilder.append("""
			           <div class="layui-form-item">
			               <div class="layui-input-block">
			                  <button class="layui-btn" lay-submit
			                    """);
		
		
			
		sBuilder.append("    lay-filter=\"formStep" + i + "\">");		
		sBuilder.append("""
			                     &emsp;下一步&emsp;
			                 </button>
			             </div>
			         </div>
			     </form>
			 </div>
			""");

	    } else if (i == steps.size() - 1) {
		sBuilder.append(
			"""
				<div>
				    <div style="text-align: center;margin-top: 90px;">
				        <i class="layui-icon layui-circle"
				           style="color: white;font-size:30px;font-weight:bold;background: #52C41A;padding: 20px;line-height: 80px;">&#xe605;</i>

				    </div>
				    <div style="text-align: center;margin-top: 50px;">
				        <button class="layui-btn next">再来一次</button>
				        <button class="layui-btn layui-btn-primary">查看账单</button>
				    </div>
				</div>
				   		""");

	    } else {
		sBuilder.append("""
			<div>
			     <form class="layui-form" style="margin: 0 auto;max-width: 460px;padding-top: 40px;">
				""");
		sBuilder.append(stepFormi);
		sBuilder.append("""
			          <div class="layui-form-item">
			             <div class="layui-input-block">
			                 <button type="button" class="layui-btn layui-btn-primary pre">上一步</button>
			                <button class="layui-btn" lay-submit
			                  """);
		sBuilder.append("    lay-filter=\"formStep" + i + "\">");
		sBuilder.append("""

			    &emsp;确认入款&emsp;
			                </button>
			            </div>
			        </div>
			    </form>
			</div>
				""");
	    }
	    i++;
	}
	formShowService.useLayModule(model, false, layUseInfo);

	model.addAttribute("titles", titles);
	model.addAttribute("contents", sBuilder);
	model.addAttribute("stepJs", stepJs);
	model.addAllAttributes(steps);
	// 根据事务，获取步骤列表。
	// 每个步骤关联表单。获取表单列表。
	// 添加表单中的列表显示
	// 每个表单中添加按钮。步骤关联按钮。
	// stepShowService.
	// 组装表单列表。添加上下步按钮。首尾步骤按钮。
	// stepShowService.table2(model, tableMap, true);

	return "layui/step";
    }

    private void stepTitle(StringBuilder titles, String stepName) {
	if (!titles.isEmpty()) {
	titles.append(",");
	}
	titles.append("{\n" + "                    title: '" + stepName + "'\n" + " }");
    }

    private void nextJs(int i, StringBuilder stepJs) {
	stepJs.append("form.on('submit(formStep"+i+")', function (data) {\n"
		+ "                step.next('#stepForm');\n"
		+ "                return false;\n"
		+ "            });");
    }

    /**
     * 管理步骤，对活动的步骤进行定义管理
     * 
     * @param model
     * @param activityId
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{activityId}/stepManage", method = { RequestMethod.GET, RequestMethod.POST })
    public String activityStep(Model model, @PathVariable("activityId") String activityId,
	     HttpServletRequest request) throws Exception {
	Map<String, Object> stepMap = neo4jService.getAttMapBy(LABEL, STEP, META_DATA);
	if (stepMap == null) {
	    throw new DefineException("步骤未定义！");
	}	
	//根据
	Map<String, Object> activeMap = neo4jService.getAttMapBy(ID, activityId, BIZ_ACTIVITY);
	if (activeMap == null) {
	    throw new DefineException("活动未定义！");
	}
	model.addAttribute(ACTIVITY_ID, activityId);
	ModelUtil.setKeyValue(model, stepMap);
	showService.showMetaInstanceCrudPage(model, stepMap, true);
	showService.tableToolBtn(model, stepMap);
	
	return "layui/stepManage";
    }


}
