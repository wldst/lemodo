package com.wldst.ruder.module.desktop;

import java.util.HashMap;
import java.util.Map;

import com.wldst.ruder.crud.controller.RelationController;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.module.state.service.StateService;
import com.wldst.ruder.util.ModelUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wldst.ruder.LemodoApplication;
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
 * 桌面控制器
 * @author wldst
 *
 */
@Controller
@RequestMapping("${server.context}/desktop")
public class DesktopController extends BaseLayuiController {
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private HtmlShowService showService;
	@Autowired
	private StateService statusService;
	@Autowired
	private RelationService relationService;
    
    @RequestMapping(value = "/{po}/{id}/detail", method = { RequestMethod.GET, RequestMethod.POST })
    public String close(Model model, @PathVariable("po") String label, 
	    @PathVariable("id") String id,
	    @PathVariable("operate") String operate,
	    HttpServletRequest request) throws Exception {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	ModelUtil.setKeyValue(model, po);
	Map<String, Object> propMapByNodeId = neo4jService.getPropMapByNodeId(Long.valueOf(id));
	showService.showMetaInstanceCrudPage(model, po, true);
	return "layui/editForm"; 
    }
    
    @RequestMapping(value = "/removeApp/{desktop}/{appid}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult removeApp(Model model, @PathVariable("desktop") String desktop, 
	    @PathVariable("appid") String appid,
	    HttpServletRequest request) throws Exception {
	Long appNodeId = neo4jService.getNodeId("appid", appid, "App");
	Long desktopId = neo4jService.getNodeId("menuid", desktop, "Desktop");
//	neo4jService.
	neo4jService.delRelation(desktopId, appNodeId, "app");
	return ResultWrapper.wrapResult(true, null, null, "删除成功");
    }
    
    @RequestMapping(value = "/remove/{desktop}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult remove(Model model, @PathVariable("desktop") String desktop, 
	    @PathVariable("appid") String appid,
	    HttpServletRequest request) throws Exception {
	Object user = request.getSession().getAttribute("UserId");
	Long userId = neo4jService.getNodeId("username", String.valueOf(user), "Password");
	Long desktopId = neo4jService.getNodeId("menuid", desktop, "Desktop");

	relationService.addRel("remove", userId, desktopId);
	return ResultWrapper.wrapResult(true, null, null, "删除成功");
    }
    
    @RequestMapping(value = "/{po}/{id}/{operate}", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public WrappedResult operate(Model model, @PathVariable("po") String label, 
	    @PathVariable("id") String id,
	    @PathVariable("operate") String operate,
	    HttpServletRequest request) throws Exception {
	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
	if (po == null || po.isEmpty()) {
	    throw new DefineException(label + "未定义！");
	}
	 
	if("close".equals(operate)) {
	    String updateStatus = "Match(n:"+label+") where id(n)="+id +" set n.status='done' ";
	    neo4jService.execute(updateStatus);
		 HashMap<String, Object>  smap = new HashMap<>();
		 smap.put(ID, id);
		 smap.put(STATUS, "done");
		statusService.statusRefresh(label, smap,Long.valueOf(id));
	}
	return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
	
    }
    
    @RequestMapping(value = "/notePaper/{po}/{id}", method = { RequestMethod.GET, RequestMethod.POST })
    public String notePaper(Model model, 
	    @PathVariable("po") String label,
	    @PathVariable("id") String id,
	    HttpServletRequest request) throws Exception {
		Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
		if (po == null || po.isEmpty()) {
			throw new DefineException(label + "未定义！");
		}
		Map<String, Object> propData = neo4jService.getPropMapByNodeId(Long.valueOf(id));

		propData.put("label", label);
		ModelUtil.setKeyValue(model, propData);
		return "layui/notePaper";
    }

}
