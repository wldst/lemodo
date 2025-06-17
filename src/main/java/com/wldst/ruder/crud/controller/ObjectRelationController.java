package com.wldst.ruder.crud.controller;

import static com.wldst.ruder.constant.CruderConstant.LABEL;
import static com.wldst.ruder.constant.CruderConstant.META_DATA;
import static com.wldst.ruder.constant.CruderConstant.NODE_ID;
import static com.wldst.ruder.constant.Msg.SAVE_SUCCESS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.util.ModelUtil;
import jakarta.servlet.http.HttpServletRequest;

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
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.util.MapTool;
import com.wldst.ruder.util.ResultWrapper;
import com.wldst.ruder.util.WrappedResult;

/**
 * 对象关系查询
 *
 * @author wldst
 */
@Controller
@RequestMapping("${server.context}/objectRel")
public class ObjectRelationController extends MapTool {
    @Autowired
    private HtmlShowService showService;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private RelationService relationService;

    @RequestMapping(value = "/{po}/{toPo}", method = {RequestMethod.GET, RequestMethod.POST})
    public String instance(Model model, @PathVariable("po") String label, @PathVariable("toPo") String endLabel,
                           HttpServletRequest request) throws Exception {
        Map<String, Object> po = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
        if (po == null || po.isEmpty()) {
            if (endLabel.equals("Po")) {
                po = neo4jService.getAttMapBy(LABEL, META_DATA, META_DATA);
            } else {
                throw new DefineException(endLabel + "未定义！");
            }
        }
        ModelUtil.setKeyValue(model, po);
        model.addAttribute("startLabel", label);
        if (endLabel.equalsIgnoreCase("iconFont")) {
            model.addAttribute("selectValue", "unicode");
        }
        showService.showSelectPage(model, po, true);
//		table2(model, po);
        return "instanceSelect";
    }

    @RequestMapping(value = "/{po}/{toPo}/{value}", method = {RequestMethod.GET, RequestMethod.POST})
    public String selectValueField(Model model, @PathVariable("po") String label,
                                   @PathVariable("toPo") String endLabel,
                                   @PathVariable("value") String valueField,
                                   HttpServletRequest request) throws Exception {
        Map<String, Object> po = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
        if (po == null || po.isEmpty()) {
            throw new DefineException(endLabel + "未定义！");
        }
        ModelUtil.setKeyValue(model, po);
        model.addAttribute("startLabel", label);

        if (endLabel.equalsIgnoreCase("iconFont")) {
            model.addAttribute("selectValue", "unicode");
        } else {
            model.addAttribute("selectValue", valueField);
        }
        showService.showMetaInstanceCrudPage(model, po, true);
//		table2(model, po);
        return "selectValueField";
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

    @RequestMapping(value = "/{po}/{toPo}/{relation}/del", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult relDel(@PathVariable("po") String startLabel, @PathVariable("toPo") String endLabel,
                                @PathVariable("relation") String relation, @RequestBody JSONObject vo) {

        if (!vo.containsKey("relations")) {
            return ResultWrapper.wrapResult(true, null, null, "没有关系数据可删除！");
        }
        JSONArray relations = vo.getJSONArray("relations");
        for (int i = 0; i < relations.size(); i++) {
            JSONObject ri = relations.getJSONObject(i);
            String startId = ri.getString("startId");
            String endIdValue = ri.getString("endId");
            if (startId != null && !"".equals(startId.trim())) {
                Map<String, Object> startNode = neo4jService.getPropMapBy(startId);
                if (endIdValue != null && !"".equals(endIdValue.trim())) {
                    for (String eki : endIdValue.split(",")) {
                        Map<String, Object> endNode = neo4jService.getPropMapBy(eki);
//						String delRrelation  = Neo4jOptCypher.delRelations(startNode, relation, endNode).toString();
//						neo4jService.query(delRrelation);
                        neo4jService.delRelation(startId, eki, startLabel, relation);
                    }
                } else {
//					String delRrelation  = Neo4jOptCypher.delRelations(startNode, relation, null).toString();
//					neo4jService.query(delRrelation);
                    neo4jService.delRelation(startId, null, startLabel, relation);
                }

            } else {
                neo4jService.delRelation(startLabel, relation);
            }

        }

        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    @RequestMapping(value = "/{po}/{toPo}/{relation}/save", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult relSave(@PathVariable("po") String startLabel, @PathVariable("toPo") String endLabel,
                                 @PathVariable("relation") String relation, @RequestBody JSONObject vo) {
        Map<String, Object> startPo = neo4jService.getAttMapBy(LABEL, startLabel, META_DATA);
//		String startKey = String.valueOf(startPo.get(CRUD_KEY));

        Map<String, Object> endPo = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
//		String endKey = String.valueOf(endPo.get(CRUD_KEY));
//		if(startKey==null||startKey.equals("null")) {
//			startKey=NODE_ID;
//		}
//		if(endKey==null||endKey.equals("null")) {
//			endKey=NODE_ID;
//		}
        Node start = null;
        if (!vo.containsKey("relations")) {
            return ResultWrapper.wrapResult(true, null, null, "没有关系数据可保存！");
        }
        String cypher = "Match(n:RelationDefine{reLabel:\"" + relation + "\"}) where n.startLabel  IN ['" + startLabel + "', '" + endLabel + "'] OR n.endLabel  IN ['" + startLabel + "', '" + endLabel + "'] return n";
        List<Map<String, Object>> relDefine = neo4jService.cypher(cypher);
        boolean reverse = false;
        if (relDefine != null && !relDefine.isEmpty()) {
            for (Map<String, Object> reli : relDefine) {
                if (!endLabel.equals(startLabel)) {
                    if (string(reli, "startLabel").equals(endLabel)
                            && string(reli, "endLabel").equals(startLabel)) {
                        reverse = true;
                        break;
                    }
                }
            }
        }
        JSONArray relations = vo.getJSONArray("relations");
        Map<String, Object> relProp =null;
        Object relProp1 = vo.get("relProp");
        if(relProp1!=null){
            relProp = (Map<String, Object>) relProp1;
        }else{
            relProp = new HashMap<>();
        }
        relProp.put(LABEL, relation);
        for (int i = 0; i < relations.size(); i++) {
            JSONObject ri = relations.getJSONObject(i);
            String startId = ri.getString("startId");
            String endIdValue = ri.getString("endId");
            if (reverse) {
                startId = ri.getString("endId");
                endIdValue = ri.getString("startId");
                String temp = startLabel;
                startLabel = endLabel;
                endLabel = temp;

                List<Long> startNodes = new ArrayList<>();
                for (String eki : startId.split(",")) {
                    Node findBy = neo4jService.getNodeById(eki);
                    if (findBy != null) {
                        startNodes.add(Long.valueOf(eki));
                    }
                }

                //更新关系定义
                if (META_DATA.equals(endLabel)) {
                    for (Long ei : startNodes) {
                        String eilabel = neo4jService.label(ei);
                        neo4jService.validateParts(eilabel, endLabel, relProp);
                    }
                } else {
                    neo4jService.validateParts(startLabel, endLabel, relProp);
                }

                if (start != null && !startNodes.isEmpty()) {
                    relationService.addRels(relation, startNodes, Long.valueOf(endIdValue), relProp);
                }
            } else {
                start = neo4jService.getNodeById(startId);

                List<Long> endNodes = new ArrayList<>();
                for (String eki : endIdValue.split(",")) {
                    Node findBy = neo4jService.getNodeById(eki);
                    if (findBy != null) {
                        endNodes.add(Long.valueOf(eki));
                    }
                }
                //更新关系定义
                if (META_DATA.equals(endLabel)) {
                    for (Long ei : endNodes) {
                        String eilabel = neo4jService.label(ei);
                        neo4jService.validateParts(startLabel, eilabel, relProp);
                    }
                } else {
                    neo4jService.validateParts(startLabel, endLabel, relProp);
                }

                if (start != null && !endNodes.isEmpty()) {
                    if (bool(relProp, "mainData")) {
                        neo4jService.execute("MATCH(s:" + startLabel + ")-[r:" + relation + "{mainData:'true'}]-(e:" + endLabel + ") where id(s)=" + startId + " delete r");
                    }
                    relationService.addRels(relation, start.getId(), endNodes, relProp);
                }
            }


        }


        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }


//	private void table2(Model model, Map<String, Object> po) {
//		if (po.containsKey("header")) {
//			String retColumns = String.valueOf(po.get("columns"));
//			String header = String.valueOf(po.get("header"));
//			String[] columnArray = retColumns.split(",");
//			String[] headers = StringGet.split(header);
//			// List<String> props = new ArrayList<>();
//			List<Map<String, String>> cols = new ArrayList<>();
//			StringBuilder sbbBuilder = new StringBuilder();
//			sbbBuilder.append("<div  class=\"layui-form-item\">");
//			for (int i = 0; i < headers.length; i++) {
//				Map<String, String> piMap = new HashMap<>();
//				piMap.put("code", "{field:'" + columnArray[i] + "', sort: true}");
//				piMap.put("name", headers[i]);
//				piMap.put("field", columnArray[i]);
//				cols.add(piMap);
//				sbbBuilder.append(layFormItem(columnArray[i], headers[i]));
//			}
//			sbbBuilder.append("</div>");
//			model.addAttribute("formContent", sbbBuilder.toString());
//			model.addAttribute("cols", cols);
//		}
//	}

    @RequestMapping(value = "/addRel", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult addRel(@RequestBody JSONObject vo) {
         Long startId = longValue(vo, "startId");
        Long endId = longValue(vo, "endId");


        Map<String, Object> stringObjectMap=relationService.addRel2(string(vo, "rel"), startId, endId, mapObject(vo, "data"));
        return ResultWrapper.wrapResult(true, stringObjectMap, null, SAVE_SUCCESS);
    }

}
