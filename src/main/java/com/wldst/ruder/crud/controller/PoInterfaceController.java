package com.wldst.ruder.crud.controller;

import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.StringGet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.wldst.ruder.util.ModelUtil;

import static com.wldst.ruder.constant.CruderConstant.LABEL;
import static com.wldst.ruder.constant.CruderConstant.META_DATA;
 
/**
  *  domain控制器
 * Created by  liuqiang（wldst）.
 */
@Controller
@RequestMapping("${server.context}/interface")
public class PoInterfaceController {
	@Autowired
	private CrudNeo4jService neo4jService;
    
    @RequestMapping(value = "/{po}", method = {RequestMethod.GET,RequestMethod.POST})
    public String instance(Model model,@PathVariable("po")String label,HttpServletRequest request)throws Exception{
    	Map<String, Object> po = neo4jService.getAttMapBy(LABEL, label, META_DATA);
		if(po==null||po.isEmpty()){
			throw new DefineException(label+"未定义！");
		}   		
		ModelUtil.setKeyValue(model, po);
		table2(model, po);
    	return "interface";
    }
    
    private void table2(Model model, Map<String, Object> po) {
		if(po.containsKey("header")) {
			String retColumns = String.valueOf(po.get("columns"));
			String header = String.valueOf(po.get("header"));
			String[] columnArray = retColumns.split(",");
			String[] headers = StringGet.split(header);
			List<Map<String,String>> cols=new ArrayList<>();
			for(int i=0;i<headers.length;i++) {
				Map<String,String> piMap=new HashMap<>();
				piMap.put("code","{field:'"+columnArray[i]+"', sort: true}");
				piMap.put("name",headers[i]);
				piMap.put("field",columnArray[i]);
				cols.add(piMap);
			}
			model.addAttribute("cols", cols);
			model.addAttribute("colCodes", columnArray);
		}
	}
		
	/**
	 * <label  class=\"layui-form-label\" th:text=\""+name+"\"></label>:
    		<div class=\"layui-input-inline\">
    			<input th:name=\""+code+"\" class=\"layui-input\" th:id=\""+code+"\"
    				placeholder=\"请输入"+name+"\" autocomplete=\"off\">
    		</div>
	 * @param code
	 * @param name
	 * @return
	 */
	public String layFormItem(String code,String name) {
		StringBuilder sb=new StringBuilder();
    	sb.append(" <label  class=\"layui-form-label\" >"+name+"</label>");
    	sb.append("	<div class=\"layui-input-inline\">");
    	sb.append("		<input name=\""+code+"\" class=\"layui-input\" id=\""+code+"\"");
    	sb.append("			placeholder=\"请输入"+name+"\" autocomplete=\"off\">");
    	sb.append("	</div>");
    	return sb.toString();
   }
	
}
