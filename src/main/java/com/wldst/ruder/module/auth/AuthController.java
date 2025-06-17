package com.wldst.ruder.module.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.annotation.ControllerLog;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.domain.AuthDomain;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.ParseExcuteSentence;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.parse.ParseExcuteSentence2;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("${server.context}/auth/")
public class AuthController extends AuthDomain {
    @Autowired
    private CrudUtil crudUtil;
    private static Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private ParseExcuteSentence pes;
    @Autowired
    private RelationService relationService;
    @Autowired
    private ParseExcuteSentence2 pes2;
    @Autowired
    private UserAdminService admin;
    @Autowired
    private RuleDomain rule;
    @Autowired
    private Neo4jOptByUser optByUserSevice;
    @Autowired
    private HtmlShowService htmlShowService;

    @ControllerLog(description = "chatSys")
    @RequestMapping(value = "/talk", method = {RequestMethod.POST,
            RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult talk(@RequestBody JSONObject vo)
            throws DefineException {
        PageObject page = crudUtil.validatePage(vo);
        String msg = string(vo, "msg");
        String myId = string(vo, "myId");
        String pipeSeparate=" | ";
        Map<String, Object> parseAuthTalk =null;
        if(msg.indexOf(pipeSeparate)>0){//管道分割
            String[] parts=msg.split(" \\| ");
            if(parts.length>1){
                int i=0;
                String answer = null;
                for(String pi:parts){
                    if(answer==null){
                        parseAuthTalk = pes2.parseTalkMsg(pi, myId);
                        if(parseAuthTalk==null){
                            return ResultWrapper.wrapResult(false, null, null, EXECUTE_FAILED);
                        }
                        Object data1=parseAuthTalk.get("data");
                        if(data1==null){
                            return ResultWrapper.wrapResult(false, null, null, EXECUTE_FAILED);
                        }
                        if(data1 instanceof Map datamap){
                                answer =string(datamap,"msg");
                        }
                        if(data1 instanceof List){
                            List<Map<String, Object>> data = listMapObject(parseAuthTalk,"data");
                            answer =string(data.get(0),"msg");
                        }
                    }else{
                        parseAuthTalk = pes2.parseTalkMsg(pi+answer, myId);
                        if(parseAuthTalk==null){
                            return ResultWrapper.wrapResult(false, null, null, EXECUTE_FAILED);
                        }
                        Object data1=parseAuthTalk.get("data");
                        if(data1==null){
                            return ResultWrapper.wrapResult(false, null, null, EXECUTE_FAILED);
                        }
                        if(data1 instanceof Map datamap){
                            answer =string(datamap,"msg");
                        }
                        if(data1 instanceof List){
                            List<Map<String, Object>> data = listMapObject(parseAuthTalk,"data");
                            answer =string(data.get(0),"msg");
                        }
                    }
                }
            }else{
                parseAuthTalk = pes2.parseTalkMsg(parts[0], myId);
            }
        }else{

            parseAuthTalk = pes2.parseTalkMsg(msg, myId);
        }
        return ResultWrapper.wrapResult(true, parseAuthTalk, page, EXECUTE_SUCCESS);
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult add(@RequestBody JSONObject vo) {
        Long longValue = longValue(vo, "start");
        String ends = string(vo, "end");
        String[] split = ends.split(",");
        for (int i = 0; i < split.length; i++) {
            String endi = split[i];
            relationService.addRel(HAS_PERMISSION, longValue, Long.valueOf(endi));
        }

        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    @RequestMapping(value = "/del", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult del(@RequestBody JSONObject vo) {
        Long longValue = longValue(vo, "start");
        String ends = string(vo, "end");
        String[] split = ends.split(",");
        for (int i = 0; i < split.length; i++) {
            String endi = split[i];
            Long valueOf = Long.valueOf(endi);
            neo4jService.delRelation(longValue, valueOf, HAS_PERMISSION);
        }

        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    @RequestMapping(value = "/hasPermission", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult hasPermission(@RequestBody JSONObject vo) {
        Long startId = longValue(vo, "start");
        Long endId = longValue(vo, "end");
        String endLabel = label(vo);
        if (endId != null) {
            return ResultWrapper.wrapResult(true, neo4jService.hasPermission(startId, endId), null, QUERY_SUCCESS);
        }
        List<Map<String, Object>> hasPermission = neo4jService.hasPermission(endLabel, startId);
        return ResultWrapper.wrapResult(true, hasPermission, null, QUERY_SUCCESS);
    }


    @RequestMapping(value = "/chatOperate", method = {RequestMethod.GET, RequestMethod.POST})
    public String chatOperate(Model model, HttpServletRequest request) throws Exception {
        String label = "AuthCommand";
        Map<String, Object> metaData = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (metaData == null || metaData.isEmpty()) {
            throw new DefineException(label + "未定义！");
        }

        model.addAttribute("myName", admin.getCurrentName());
        model.addAttribute("myId", admin.getCurrentPasswordId());
        ModelUtil.setKeyValue(model, metaData);
        return "chatOperate";
    }


    /**
     * 查询有权限的数据
     *
     * @param label
     * @param vo
     * @return
     */
    @RequestMapping(value = "/query/{label}", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public WrappedResult queryAuth(@PathVariable String label, @RequestBody JSONObject vo) {
        PageObject page = crudUtil.validatePage(vo);
        String[] columns = null;
        Map<String, Object> md = null;
        try {
            md = admin.checkAuth(label, "QueryOperate", "查询");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        String[] splitValue = splitValue(vo, "notNull");
        if (splitValue != null && splitValue.length > 0) {
            for (String si : splitValue) {
                String fi = string(vo, si);
                if (fi == null || "".equals(fi)) {
                    return ResultWrapper.wrapResult(true, null, page, QUERY_SUCCESS);
                }
            }
        }
        vo.remove("notNull");

        try {
            columns = crudUtil.getMdColumns(label);
        } catch (DefineException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 脱敏处理字段
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        Map<String, Object> validParam = validParam(vo, columns);

        vo.remove("page");
        if (label.equals("DBTable")) {
            rule.validMyRule(label, validParam, md);
        }
        String queryText = string(vo, "KeyWord");
        if (queryText != null && !"".equals(queryText.trim())) {
            validParam.put(NAME, queryText);
            // vo.put(CODE, queryText);
            vo.remove("KeyWord");
        }

        //parentId handle
        if (validParam.get("parentId") != null) {
            Map<String, Object> one2 = neo4jService.getOne("Match(t:TreeDefine) where t.mdLabel='" + label(md) + "' return t.parentIdField AS parentIdField,t.code AS code");
            String one = string(one2, "parentIdField");
            validParam.put(one, validParam.get("parentId"));
            validParam.remove("parentId");
        }
        String query = optByUserSevice.queryAuthObj(validParam, label, columns, page);
        List<Map<String, Object>> dataList = neo4jService.cypher(query);
        if (dataList != null) {
            rule.formateQueryField(dataList);
            if (!dataList.isEmpty() && !vo.containsKey(ID)) {
                page.setTotal(crudUtil.total(query,vo));
            }
            //如何进行数据转换，在理配置规则？
            if ("RightMenu".equals(label)) {
                htmlShowService.btnUrlReplace(dataList);
            }

            try {
                deSensitive(label, dataList);
            } catch (DefineException e) {
                LoggerTool.error(logger, e.getMessage(), e);
                return ResultWrapper.wrapResult(false, null, page, e.getMessage());
            }
        }
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }

    /**
     * 清理参数
     *
     * @param vo
     * @param columns
     * @return
     */
    private Map<String, Object> validParam(JSONObject vo, String[] columns) {
        Map<String, Object> param = new HashMap<>();
        for (String ci : columns) {
            Object value2 = vo.get(ci);
            if (value2 != null && !"".equals(value2) && !"null".equals(value2)) {
                param.put(ci, value2);
            }
        }
        return param;
    }


    /**
     * 脱敏处理
     *
     * @param label
     * @param dataList
     * @throws DefineException
     */
    private void deSensitive(String label, List<Map<String, Object>> dataList) throws DefineException {
        if (!dataList.isEmpty()) {
            String[] sensitiveColumn = crudUtil.getSensitiveColumn(label);
            if (sensitiveColumn != null && sensitiveColumn.length > 0) {
                for (Map<String, Object> di : dataList) {
                    for (String si : sensitiveColumn) {
                        String string = string(di, si);
                        String dsValue = string.substring(0, 1);
                        di.put(si, dsValue + "***");
                    }
                }
            }
        }
    }

}
