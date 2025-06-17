package com.wldst.ruder.crud.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.BeanShellService;
import com.wldst.ruder.module.database.DbInfoService;
import com.wldst.ruder.util.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.annotation.ControllerLog;
import com.wldst.ruder.annotation.MyAnnotation;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.constant.RuleConstants;
import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.crud.service.HtmlShowService;
import com.wldst.ruder.crud.service.ObjectService;
import com.wldst.ruder.crud.service.RelationService;
import com.wldst.ruder.crud.service.TabListShowService;
import com.wldst.ruder.crud.service.WorkFlowService;
import com.wldst.ruder.domain.FileDomain;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.exception.AuthException;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.bs.ShellOperator;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.state.service.StateService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.wldst.ruder.domain.DataBaseDomain.LABLE_TABLE;
import static com.wldst.ruder.domain.DataBaseDomain.TABEL_NAME;
import static com.wldst.ruder.module.workflow.constant.BpmDo.label;

@RestController
@ResponseBody
@RequestMapping("${server.context}/cruder/{po}")
@CrossOrigin
public class CruderController extends RuleConstants {
    private static Logger logger = LoggerFactory.getLogger(CruderController.class);
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private CrudNeo4jService neo4jService;
    @Autowired
    private WorkFlowService workFlowService;
    @Autowired
    private RelationService relationService;
    @Autowired
    private Neo4jOptByUser optByUserSevice;
    @Autowired
    private StateService statusService;
    @Autowired
    private TabListShowService tabService;
    @Autowired
    protected BeanShellService bss;
    @Autowired
    private UserAdminService  adminService;

    @Autowired
    private HtmlShowService htmlShowService;

    @Autowired
    private ObjectService objectService;
    @Autowired
    private DbInfoService dbInfoService;
    @Autowired
    private RuleDomain rule;
    @Autowired
    private ShellOperator so;
    @Autowired
    @Qualifier("smPersister")
    private StateMachinePersister<Long, Long, String> smPersister;

    public List<Map<String, Object>> loadRules(String name) {
        Map<String, Object> bodyMap = new HashMap<>();
        bodyMap.put("ruleKey", name);
        return loadRules(bodyMap);
    }

    public List<Map<String, Object>> loadRules(Map<String, Object> body) {
        if (body == null) {
            body = new HashMap<>(); // 请求body
        }
        String label = "Rule";
        String[] columns;
        try {
            columns = crudUtil.getMdColumns(label);
            String query = Neo4jOptCypher.safeQueryObj(body, label, columns);
            return neo4jService.query(query,body);
        } catch (DefineException e) {
            LoggerTool.error(logger,"loadRules", e);
        }
        return null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    /**
     * 补全主键
     *
     * @param po
     * @return
     */
    private void completePK(Map<String, Object> po) {
        Object columnsStr = po.get(COLUMNS);
        Object headerObject = po.get(HEADER);

        String header = null;
        String headerValid = null;
        if (headerObject != null) {
            header = String.valueOf(headerObject);
            String[] hds = crudUtil.getColumns(header);
            headerValid = String.join(",", hds);
        }

        if (columnsStr != null) {
            String column = String.valueOf(columnsStr);
            String[] columns = crudUtil.getColumns(column);
            String columnValid = String.join(",", columns);
            Set<String> cSet = new HashSet<>();
            for (String ci : columns) {
                cSet.add(ci);
            }
            if (!cSet.contains(NODE_ID)) {
                column = NODE_ID + "," + columnValid;
                header = "编码," + headerValid;
            }
            po.put(COLUMNS, column);
        }
        po.put(HEADER, header);
    }

    private String completePK(Map<String, Object> po, String crudColumns, String crudHeader) {
        if (po == null) {
            return ID;
        }
        Object key = po.get(NODE_ID);
        Object columnsStr = po.get(crudColumns);

        Object headerObject = po.get(crudHeader);

        String crudKey = null;
        String header = null;
        String headerValid = null;
        if (headerObject != null) {
            header = String.valueOf(headerObject);
            String[] hds = crudUtil.getColumns(header);
            headerValid = String.join(",", hds);
        }

        if (columnsStr != null) {
            String column = String.valueOf(columnsStr);
            String[] columns = crudUtil.getColumns(column);
            String columnValid = String.join(",", columns);
            if (key != null) {
                crudKey = String.valueOf(key);
                Set<String> cSet = new HashSet<>();
                for (String ci : columns) {
                    cSet.add(ci);
                }
                if (!cSet.contains(crudKey) && StringUtils.isNotBlank(crudKey)) {
                    column = crudKey + "," + columnValid;
                    header = "编码," + headerValid;
                }
            } else {
                if (columns.length > 0) {
                    crudKey = columns[0];
                }
            }
            po.put(crudColumns, column);
        }
        po.put(crudHeader, header);
        return crudKey;
    }

    /**
     * 自定义查询功能
     *
     * @param vo
     * @return
     * @throws DefineException
     */
    @RequestMapping(value = "/custom", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult customQuery(@RequestBody JSONObject vo) throws DefineException {
//        LabelValidator.validateLabel(label);
        PageObject page = crudUtil.validatePage(vo);
        String query = (String) vo.get("query");
        page.setTotal(crudUtil.total(query));
        List<Map<String, Object>> query2 = neo4jService.cypher(query);
        return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
    }

    @MyAnnotation
    @RequestMapping(value = "/del", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult del(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException, AuthException {
        LabelValidator.validateLabel(label);
        if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
            return ResultWrapper.wrapResult(true, null, null, DELETE_FAILED);
        }
        try {
            adminService.checkAuth(label, "DeleteOperate", "删除操作");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }
        if(id(vo)==null){

            List<Map<String, Object>> maps = neo4jService.queryBy(vo, label);
            for (Map<String, Object> map : maps) {
                neo4jService.removeBy(String.valueOf(map.get(ID)), label);
            }
        }else{
            neo4jService.removeBy(string(vo, ID), label);
        }

        return ResultWrapper.wrapResult(true, null, null, DELETE_SUCCESS);
    }

    @RequestMapping(value = "/delAllData", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult delAllData(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException, AuthException {
        LabelValidator.validateLabel(label);
        if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
            return ResultWrapper.wrapResult(true, null, null, DELETE_FAILED);
        }
        try {
            adminService.checkAuth(label, "DeleteOperate", "删除数据");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }
        neo4jService.removeBy(string(vo, ID), label);
        return ResultWrapper.wrapResult(true, null, null, DELETE_SUCCESS);
    }

    @RequestMapping(value = "/delList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult delList(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException, AuthException {
        LabelValidator.validateLabel(label);
        if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
            return ResultWrapper.wrapResult(true, null, null, DELETE_FAILED);
        }
        try {
            adminService.checkAuth(label, "DeleteOperate", "删除操作");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }
        String id = vo.getString(ID);
        String delById = Neo4jOptCypher.delById(id, label);
        neo4jService.execute(delById);
        return ResultWrapper.wrapResult(true, null, null, DELETE_SUCCESS);
    }


    @ControllerLog(description = "查询")
    @RequestMapping(value = "/list", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult list(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        LabelValidator.validateLabel(label);
        try {
            adminService.checkAuth(label, "QueryOperate", "查询");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }
        String[] columns = crudUtil.getMdColumns(label);
        String query = optByUserSevice.queryObj(vo, label, columns);
        List<Map<String, Object>> query2 = neo4jService.query(query,vo);
//        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);

        for (Map<String, Object> di : query2) {
            rule.formateQueryField(di);
        }
        return ResultWrapper.ret(true, query2, QUERY_SUCCESS);
    }

    /**
     * 根据指定的标签和请求体，查询特定列的数据列表。
     * 此方法主要用于从Neo4j数据库中检索指定标签下的特定列数据，并对数据进行格式化处理。
     *
     * @param label 标签名称，用于定位特定的数据类型。
     * @param vo 请求体JSONObject，包含查询的具体列名和键名。
     * @return WrappedResult 包装了查询结果的对象，包含成功状态、数据、错误信息等。
     * @throws DefineException 如果标签验证失败或权限检查不通过，则抛出定义异常。
     */
    @ControllerLog(description = "查询")
    @RequestMapping(value = "/col1List", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult oneColumnList(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        // 验证标签的合法性
        LabelValidator.validateLabel(label);
        try {
            // 检查用户是否有查询操作的权限
            adminService.checkAuth(label, "QueryOperate", "查询");
        } catch (Exception e) {
            // 如果权限检查失败，返回错误信息
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }
        // 从请求体中提取要查询的列名和键名
        String oneColumn = string(vo, "column");
        String key = string(vo, "key");
        // 从请求体中移除不需要的参数
        vo.remove("column");
        vo.remove("key");

        // 从Neo4j服务中获取标签的元数据
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        // 获取所有可查询的列名集合
        Set<String> stringSet = stringSet(md, "columns");
        // 检查请求的列名是否在可查询的列名集合中
        if (!stringSet.contains(oneColumn)) {
            // 如果不在，返回错误信息
            return ResultWrapper.wrapResult(false, null, null, "请选择正确的字段进行查询");
        }
        // 准备查询的列名数组
        String[] columns = new String[1];
        columns[0] = oneColumn;
        // 构造查询语句
        String query = optByUserSevice.queryObj(vo, label, columns);
        // 执行查询
        List<Map<String, Object>> query2 = neo4jService.query(query,vo);

        // 对查询结果进行格式化处理
        for (Map<String, Object> di : query2) {
            rule.formateQueryField(di);
        }
        // 准备存储查询结果的数组
        String[] cols = new String[query2.size()];
        int i = 0;
        // 提取每条数据中指定列的值
        for (Map<String, Object> qi : query2) {
            cols[i] = string(qi, oneColumn);
            i++;
        }
        // 构建返回的数据映射
        Map<String, Object> retData = new HashMap<>();
        retData.put(key, cols);
        // 返回查询成功的结果
        return ResultWrapper.ret(true, retData, QUERY_SUCCESS);
    }

    @ControllerLog(description = "查询所有数据")
    @RequestMapping(value = "/listAll", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult listAll(@PathVariable("po") String label) throws DefineException {
        LabelValidator.validateLabel(label);
        try {
            adminService.checkAuth(label, "QueryOperate", "查询");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }
        List<Map<String, Object>> query2 = neo4jService.listAllByLabel(label);
        return ResultWrapper.ret(true, query2, QUERY_SUCCESS);
    }

    @ControllerLog(description = "查询所有数据的指定列")
    @RequestMapping(value = "/listSomeCol", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult listSomeCol(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        LabelValidator.validateLabel(label);
        try {
            adminService.checkAuth(label, "QueryOperate", "查询");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }
        String[] cols = columns(vo);
        vo.remove("columns");
        String queryx = Neo4jOptCypher.queryObj(vo, label, cols);
        List<Map<String, Object>> selectList = neo4jService.query(queryx);
        return ResultWrapper.ret(true, selectList, QUERY_SUCCESS);
    }

    /**
     * @param label
     * @param vo
     * @return
     * @throws DefineException
     * @author liuqiang
     * @date 2019年9月20日 下午3:17:14
     * @version V1.0
     */
    @RequestMapping(value = "/define", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult define(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        LabelValidator.validateLabel(label);
        String[] columns = crudUtil.getMdColumns(label);
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
        }
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        return ResultWrapper.wrapResult(true, md, null, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/shortDefine", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult shortDefine(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        LabelValidator.validateLabel(label);
        String[] columns = crudUtil.getMdColumns(label);
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
        }
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        return ResultWrapper.wrapResult(true, md, null, QUERY_SUCCESS);
    }

    /**
     * 处理针对特定对象的简要视图请求。
     * 该方法支持POST和GET请求，返回应用JSON格式的数据，编码为UTF-8。
     * 主要用于根据给定的对象标签（po）和请求体中的数据，获取并返回简要视图信息。
     *
     * @param label 对象标签，用于识别特定的对象类型。
     * @param vo 请求体中的JSONObject，承载额外的请求信息或过滤条件。
     * @return WrappedResult 包含请求结果的封装对象，其中包含成功标志、简要视图数据、错误代码等。
     * @throws DefineException 如果标签验证失败或数据查询过程中遇到问题，抛出此异常。
     */
    @RequestMapping(value = "/shortView", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult shortView(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        // 验证标签的合法性
        LabelValidator.validateLabel(label);
        // 根据标签获取对象的列信息
        String[] columns = crudUtil.getMdColumns(label);
        // 如果没有获取到列信息，返回查询失败的结果
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
        }
        // 从Neo4j中获取元数据信息
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        // 解析元数据，获取列名映射
        Map<String, String> colName = colName(md);
        // 初始化简要视图列表
        List<Map<String, String>> shortView = new ArrayList<>();

        // 从元数据中提取需要在简要视图中展示的字段
        List<String> shortShow = splitValue2List(md, "shortShow");
        // 如果有需要展示的字段，构建并添加到简要视图列表中
        if (shortShow != null && shortShow.size() > 0) {
            for (String s : shortShow) {
                Map<String, String> map = new HashMap<>();
                map.put("key", s);
                map.put("title", colName.get(s));
                shortView.add(map);
            }
        }
        // 返回查询成功的结果，包含构建的简要视图列表
        return ResultWrapper.wrapResult(true, shortView, null, QUERY_SUCCESS);
    }

    /**
     * 根据提供的标签和请求体中的条件，查询特定数据并返回。
     * 此方法支持POST和GET请求，返回结果以JSON格式编码。
     *
     * @param label 请求路径中的标签参数，用于指定查询的数据类型。
     * @param vo 请求体中的JSON对象，包含查询条件和分页信息。
     * @return 包含查询结果、分页信息和操作状态的封装对象。
     * @throws DefineException 如果查询过程中遇到定义错误。
     */
    @ControllerLog(description = "查询数据")
    @RequestMapping(value = "/cquery", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult getDataWithColumn(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        // 验证标签的合法性
        LabelValidator.validateLabel(label);
        // 校验并获取分页对象
        PageObject page = crudUtil.validatePage(vo);
        // 解析请求体中的列信息
        String[] columns = vo.getString("columns").split(",");
        vo.remove("columns");

        // 如果没有指定列，则返回查询失败的结果
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        // 根据标签获取元数据信息
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        vo.remove("page");

        // 如果标签为DBTable，执行额外的规则验证
        if (label.equals("DBTable")) {
            rule.validMyRule(label, vo, md);
        }

        // 处理查询关键字，并调整查询条件
        String queryText = string(vo, "KeyWord");
        if (queryText != null && !"".equals(queryText.trim())) {
            vo.put(NAME, queryText);
            vo.remove("KeyWord");
        }

        // 构造查询语句
        String query = optByUserSevice.safeNormalQueryObj(vo, label, columns, page);
        // 执行查询
        List<Map<String, Object>> dataList = neo4jService.query(query,vo);

        // 格式化查询结果，并计算总记录数
        if (dataList != null) {
            rule.formateQueryField(dataList);
            if (!dataList.isEmpty()) {
                page.setTotal(crudUtil.total(query,vo));
            }
        }

        // 返回查询成功的结果，包含数据列表和分页信息
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }

    /**
     * 处理查询请求的API接口。
     *
     * @param label 标签名称，用于确定查询的对象类型。
     * @param api 具体的查询接口名称，用于确定查询的操作。
     * @param vo 查询条件的JSONObject，包含分页信息和查询关键字等。
     * @return 包含查询结果的WrappedResult对象。
     * @throws DefineException 如果查询过程中遇到定义错误。
     */
    @ControllerLog(description = "嵌入式查询")
    @RequestMapping(value = "/api/{api}", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult queryApi(@PathVariable("po") String label, @PathVariable("api") String api, @RequestBody JSONObject vo) throws DefineException {
        // 校验标签的合法性
        LabelValidator.validateLabel(label);
        // 初始化分页对象
        PageObject page = crudUtil.validatePage(vo);

        // 根据不同的标签类型和API调用不同的处理方法
        if ("WorkFlow".equalsIgnoreCase(label)) {
            String documentType = string(vo, "documentType");

            // 处理特定的Workflow查询接口
            // 自定义接口
            if ("getCheckFactor".equals(api)) {
                return getCheckFator(vo, page, documentType);
            }
            if ("getCheckOperationPurview".equals(api)) {
                return getCheckOperationPurview(vo, page, documentType);
            }
        }
        if ("User".equalsIgnoreCase(label)) {
            // 处理特定的User查询接口
            // 自定义接口
            if ("getUserDept".equals(api)) {
                String query = "MATCH(u:User)-[r1]->(o:Organization)" + "-[r2:children]->(o2:Organization)" + " return u.name  AS username,u.username  AS  account,id(u) AS userId," + "id(o) AS organId,o.name AS organName" + ",o2.name AS organ2Name";
                String queryPage = optByUserSevice.cypherPage(query, page);
                List<Map<String, Object>> dataList = neo4jService.cypher(queryPage);
                if (dataList != null) {
                    if (!dataList.isEmpty() && !vo.containsKey(ID)) {
                        page.setTotal(crudUtil.total(queryPage));
                    }
                }
                return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
            }
        }

        // 获取查询对象的列定义
        String[] columns = crudUtil.getMdColumns(label);
        // 如果没有找到列定义，返回查询失败的结果
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        // 根据标签类型获取元数据信息
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);

        // 处理特定的查询接口
        if ("getFieldInfo".equals(api)) {
            return getFieldInfo(page, md);
        }
        if ("validate".equals(api)) {
            return validate(label, page, md);
        }

        // 对于DBTable标签，进行规则验证
        if (label.equals("DBTable")) {
            rule.validMyRule(label, vo, md);
        }

        // 处理查询关键字
        String queryText = string(vo, "KeyWord");
        if (queryText != null && !"".equals(queryText.trim())) {
            vo.put(NAME, queryText);
            vo.remove("KeyWord");
        }

        // 设置管理员服务
        optByUserSevice.setAdminService(adminService);
        // 构造查询语句
        String query = optByUserSevice.queryObj(vo, label, columns, page);
        List<Map<String, Object>> dataList = neo4jService.query(query,vo);
        // 格式化查询结果字段
        if (dataList != null) {
            rule.formateQueryField(dataList);
            if (!dataList.isEmpty() && !vo.containsKey(ID)) {
                page.setTotal(crudUtil.total(query));
            }
        }
        // 返回查询成功的结果
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }

    private WrappedResult validate(String label, PageObject page, Map<String, Object> po) {
        String[] splitColumnValue = columns(po);
        String[] headers = headers(po);
        List<Map<String, Object>> fieldInfo = objectService.getFieldInfo(label);
        Map<String, Map<String, Object>> fieldInfoMap = new HashMap<>();
        if (fieldInfo != null && !fieldInfo.isEmpty()) {
            for (Map<String, Object> fi : fieldInfo) {
                fieldInfoMap.put(string(fi, "field"), fi);
            }
        }

        List<Map<String, Object>> fieldsInfo = new ArrayList<>(splitColumnValue.length);

        for (int i = 0; i < splitColumnValue.length; i++) {
            String columni = splitColumnValue[i];
            String hi = headers[i];
            Map<String, Object> fi = new HashMap<>();
            fi.put("name", columni);
            fi.put("header", hi);
            fi.put("description", hi);
            String string = string(fi, TYPE);
            if (null == string) {
                fi.put("type", "string");
            } else {
                fi.put("type", string);
            }

            fieldsInfo.add(fi);
        }

        return ResultWrapper.wrapResult(true, po, page, QUERY_SUCCESS);
    }

    private WrappedResult getFieldInfo(PageObject page, Map<String, Object> po) {
        String label=label(po);
        String[] splitColumnValue = columns(po);
        String[] headers = headers(po);
        List<Map<String, Object>> fieldInfo = objectService.getFieldInfo(label);
        Map<String, Map<String, Object>> fieldInfoMap = new HashMap<>();
        if (fieldInfo != null && !fieldInfo.isEmpty()) {
            for (Map<String, Object> fi : fieldInfo) {
                fieldInfoMap.put(string(fi, "field"), fi);
            }
        }

        List<Map<String, Object>> fieldsInfo = new ArrayList<>(splitColumnValue.length);

        for (int i = 0; i < splitColumnValue.length; i++) {
            String columni = splitColumnValue[i];
            String hi = headers[i];
            Map<String, Object> fi=processField(columni, hi, fieldInfoMap);
            fieldsInfo.add(fi);
        }

        po.put("fields", fieldsInfo);
        return ResultWrapper.wrapResult(true, po, page, QUERY_SUCCESS);
    }

    @NotNull
    private static Map<String, Object> processField(String columni, String hi, Map<String, Map<String, Object>> fieldInfoMap){
        Map<String, Object> fi = new HashMap<>();
        fi.put("name", columni);
        fi.put("header", hi);
        fi.put("description", hi);
        fi.put("type", "string");
        Map<String, Object> fieldData=fieldInfoMap.get(columni);

        if(fieldData!=null){
            String string = string(fieldData, TYPE);
            if (null != string) {
                fi.put("type", string);
            }
        }
        return fi;
    }

    /**
     * 获取流程当前节点的表单操作权限信息。在相关表单中，可新增，删除，修改的地方。
     * @param vo
     * @param page
     * @param documentType
     * @return
     */
    private WrappedResult getCheckOperationPurview(JSONObject vo, PageObject page, String documentType) {
        Map<String, Object> md=neo4jService.getAttMapBy(LABEL, documentType, META_DATA);
        if(md==null){
            return ResultWrapper.wrapResult(false, null, null,QUERY_FAILED);
        }
        String cc="match(n:CheckOperationPurview{nodeId:"+longValue(vo, "dataId")+"})  return n";
        List<Map<String, Object>> purview=neo4jService.cypher(cc);
        Map<String, String> colName=colName(md);
        //流程定义表单信息，获取单据对应的表单信息。
        //如何根据当前流程当前节点，获取需要的表单。视图。节点绑定视图，以及操作权限。
        //在哪里定义用户的表单，在读取待办的时候，获取待办的表单。待办表单如何定义？

        Map<String, Map<String, Object>> checkInfoMap=new HashMap<>();
        if(purview!=null){
            for(Map<String, Object> ci : purview){
                String mdName=name(md);
                String fi=string(ci, "formId");
                Map<String, Object> info=copyWithKeys(ci, "canEdit,mustHaveData,canAdd,canDel");
                checkInfoMap.put(fi, info);
            }
        }

        List<Map<String, Object>> checkFactor=new ArrayList<>(checkInfoMap.size());

        for(String ci : checkInfoMap.keySet()){
            Map<String, Object> di=new HashMap<>();
            Map<String, Object> checkInfoi=checkInfoMap.get(ci);
            if(checkInfoi!=null&&!checkInfoi.isEmpty()){
                di=checkInfoi;
            }
            di.put(CODE, ci);
            di.put(NAME, colName.get(ci));
            checkFactor.add(di);
        }


        if(checkFactor!=null){
            if(!checkFactor.isEmpty()){
                page.setTotal(checkFactor.size());
            }
        }
        return ResultWrapper.wrapResult(true, checkFactor, page, QUERY_SUCCESS);
    }

    /**
     * 设计出，获取当前节点的审批要素，哪些节点谁可以更新那些字段，以及可见字段。
     * @param vo
     * @param page
     * @param documentType
     * @return
     */
    private WrappedResult getCheckFator(JSONObject vo, PageObject page, String documentType){
        // 获取当前流程能影像的字段。
        // 获取documentTypeName
        Map<String, Object> md=neo4jService.getAttMapBy(LABEL, documentType, META_DATA);
        String cc="match(n:CheckFactor{nodeId:"+longValue(vo, "dataId")+"})  return n";
        List<Map<String, Object>> checkInfo=neo4jService.cypher(cc);
        Map<String, String> colName=colName(md);

        Map<String, Map<String, Object>> checkInfoMap=new HashMap<>();
        if(checkInfo!=null){
            for(Map<String, Object> ci : checkInfo){
                String mdName=name(md);
                String fi=string(ci, "field");
                String fiName=colName.get(fi);
                String notNullable=string(ci, "notNullable");
                String canEdit=string(ci, "canEdit");
                String isHidden=string(ci, "isHidden");
                Map<String, Object> info=copyWithKeys(ci, "canEdit,notNullable,isHidden");
                checkInfoMap.put(fi, info);
            }
        }

        List<Map<String, Object>> checkFactor=new ArrayList<>(colName.size());

        for(String ci : colName.keySet()){
            Map<String, Object> di=new HashMap<>();
            Map<String, Object> checkInfoi=checkInfoMap.get(ci);
            if(checkInfoi!=null&&!checkInfoi.isEmpty()){
                di=checkInfoi;
            }
            di.put(CODE, documentType+"-"+ci);
            di.put(NAME, name(md)+"-"+colName.get(ci));
            checkFactor.add(di);
        }


        if(colName!=null){
            if(!colName.isEmpty()){
                page.setTotal(colName.size());
            }
        }
        return ResultWrapper.wrapResult(true, checkFactor, page, QUERY_SUCCESS);
    }
    /**
     * 处理查询请求，支持POST和GET方法，返回查询结果的JSON格式。
     * @param label 数据标签，用于确定查询的数据类型。
     * @param vo 查询条件的JSON对象，包含分页信息和过滤条件。
     * @return 包含查询结果、分页信息和操作状态的封装对象。
     * @throws DefineException 如果查询过程中遇到定义错误。
     */
    @ControllerLog(description = "查询数据")
    @RequestMapping(value = "/query", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult query(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        String[] columns = null;
        Map<String, Object> md = null;
        // 校验数据标签的合法性
        LabelValidator.validateLabel(label);
        System.out.println(vo.toJSONString());
        // 校验并获取分页信息
        PageObject page = crudUtil.validatePage(vo);
        try {
            // 检查操作权限
            md = adminService.checkAuth(label, "QueryOperate", "查询");
        } catch (Exception e) {
            // 权限检查失败，返回错误信息
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }

        // 获取查询字段信息
        columns = crudUtil.getMdColumns(label);
        // 如果没有查询字段，返回失败信息
        // 脱敏处理字段
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        String sort=string(vo, "sort");
                if(vo.containsKey("sort")){
                     vo.remove("sort");
                }

        Map<String, Object> validParam =new HashMap<>();
        if(vo.size()==3&&vo.containsKey("label")&&vo.containsKey("key")&&vo.containsKey("value")){
            validParam.put(string(vo, "key"), string(vo, "value"));
        }else{
            // 处理非空验证逻辑
            String[] splitValue = splitValue(vo, "notNull");
            if (splitValue != null && splitValue.length > 0) {
                for (String si : splitValue) {
                    String fi = string(vo, si);
                    if (fi == null || "".equals(fi)) {
                        // 返回成功状态，但不包含数据，因为存在必填字段为空的情况
                        return ResultWrapper.wrapResult(true, null, page, QUERY_SUCCESS);
                    }
                }
            }
            vo.remove("notNull");

            // 过滤掉非查询字段的参数
            validParam = validParam(vo, columns);

            vo.remove("page");
            // 如果是DBTable类型，执行额外的规则验证
            if (label.equals("DBTable")) {
                rule.validMyRule(label, validParam, md);
            }
            // 设置管理员服务
            optByUserSevice.setAdminService(adminService);

            // 处理parentId字段，根据标签动态调整查询条件
            //parentId handle
            if (validParam.get("parentId") != null) {
                Map<String, Object> one2 = neo4jService.getOne("Match(t:TreeDefine) where t.mdLabel='" + label(md) + "' return t.parentIdField AS parentIdField,t.code AS code");
                String one = string(one2, "parentIdField");
                validParam.put(one, validParam.get("parentId"));
                validParam.remove("parentId");
            }
        }

        // 构造查询语句并执行查询
        String query = optByUserSevice.queryObj(validParam, label, columns, page,sort);
        System.out.println(query);
        List<Map<String, Object>> dataList = neo4jService.query(query,validParam);
        System.out.println(dataList);
        if (dataList != null) {
            // 格式化查询结果字段
            try{
                rule.formateQueryField(dataList);
            }catch(Exception e){
               LoggerTool.error(logger,e.getMessage(),e);
            }

            // 如果查询结果不为空且vo中不包含ID字段，计算总记录数
            if (!dataList.isEmpty() && !vo.containsKey(ID)) {
                page.setTotal(crudUtil.total(query,validParam));
            }
            // 对查询结果进行脱敏处理
            deSensitive(label, dataList);
        }
        // 返回查询成功的结果，包含数据列表和分页信息
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }

    @ControllerLog(description = "查询数据")
    @RequestMapping(value = "/search", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult search(@PathVariable("po") String label, @RequestBody Map<String, Object> vo) throws DefineException {
        LabelValidator.validateLabel(label);
        PageObject page = crudUtil.validatePage(vo);
        String[] columns = null;
        Map<String, Object> md = null;
        try {
            md = adminService.checkAuth(label, "QueryOperate", "查询");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
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

        columns = crudUtil.getMdColumns(label);
        // 脱敏处理字段
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        Map<String, Object> validParam = validParam(vo, columns);

        vo.remove("page");
        if (label.equals("DBTable")) {
            rule.validMyRule(label, validParam, md);
        }

        optByUserSevice.setAdminService(adminService);

        //parentId handle
        if (validParam.get("parentId") != null) {
            Map<String, Object> one2 = neo4jService.getOne("Match(t:TreeDefine) where t.mdLabel='" + label(md) + "' return t.parentIdField AS parentIdField,t.code AS code");
            String one = string(one2, "parentIdField");
            validParam.put(one, validParam.get("parentId"));
            validParam.remove("parentId");
        }
        String queryText = keyWord(vo);
        String xx = string(md, "searchColumn");
        String query = optByUserSevice.searchObj(validParam, label, columns, page, xx, queryText);
        LoggerTool.info(logger,query);
        List<Map<String, Object>> dataList = neo4jService.cypher(query);
        if (dataList != null) {
            rule.formateQueryField(dataList);
            if (!dataList.isEmpty() && !vo.containsKey(ID)) {
                page.setTotal(crudUtil.total(query));
            }
            deSensitive(label, dataList);
        }
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }


    /**
     * 简单查询接口，支持POST和GET请求，返回查询结果的封装对象。
     * @param label 查询对象的标签，用于确定查询的对象类型。
     * @param vo 查询条件的JSONObject，包含分页信息和查询参数。
     * @return 包含查询结果数据、分页信息和状态码的封装对象。
     * @throws DefineException 如果查询过程中遇到定义错误，抛出此异常。
     */
    @ControllerLog(description = "查询数据")
    @RequestMapping(value = "/searchSimple", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult searchSimple(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        // 校验查询对象标签的合法性
        LabelValidator.validateLabel(label);
        // 校验并获取分页信息
        PageObject page = crudUtil.validatePage(vo);
        // 记录查询开始的信息
        LoggerTool.info(logger,"searchSimple ================================" + vo.toJSONString());

        String[] columns = null;
        Map<String, Object> md = null;

        try {
            // 检查用户是否有查询权限
            md = adminService.checkAuth(label, "QueryOperate", "查询");
        } catch (Exception e) {
            // 如果权限检查失败，返回错误信息
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }

        // 获取查询对象的列信息
        columns = crudUtil.getMdColumns(label);
        // 如果列信息为空或不合法，返回查询失败信息
        // 脱敏处理字段
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        // 过滤并校验查询参数
        Map<String, Object> validParam = validParam(vo, columns);

        // 移除分页信息，避免影响后续逻辑
        vo.remove("page");
        // 如果查询对象为DBTable，执行额外的规则验证
        if (label.equals("DBTable")) {
            rule.validMyRule(label, validParam, md);
        }

        // 设置管理员服务
        optByUserSevice.setAdminService(adminService);

        // 处理parentId字段，用于构建关联查询
        //parentId handle
        if (validParam.get("parentId") != null) {
            Map<String, Object> one2 = neo4jService.getOne("Match(t:TreeDefine) where t.mdLabel='" + label(md) + "' return t.parentIdField AS parentIdField,t.code AS code");
            String one = string(one2, "parentIdField");
            validParam.put(one, validParam.get("parentId"));
            validParam.remove("parentId");
        }

        // 构建查询关键词
        String queryText = keyWord(vo);


        String xx = string(md, "searchColumn");
        // 记录查询关键词和搜索列信息
        LoggerTool.info(logger,queryText+""+xx+"");
        // 构建并执行查询语句
        String query = optByUserSevice.searchObj(validParam, label, columns, page, xx, queryText);
        LoggerTool.info(logger,queryText+"query========"+query+"");
        LoggerTool.info(logger,query);

        // 执行查询并获取结果数据
        List<Map<String, Object>> dataList = neo4jService.cypher(query);
        // 如果查询结果不为空，更新分页信息中的总记录数
        if (dataList != null) {
            if (!dataList.isEmpty() && !vo.containsKey(ID)) {
                page.setTotal(crudUtil.total(query));
            }
        }
        // 返回查询成功的结果，包含数据、分页信息和成功状态码
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }

    private Map<String, Object> validParam(Map<String, Object> vo, String[] columns) {
        Map<String, Object> param = new HashMap<>();
        for (String ci : columns) {
            Object value2 = vo.get(ci);
            if (value2 != null && !"".equals(String.valueOf(value2).trim()) && !"null".equals(value2)) {
                param.put(ci, value2);
            }
        }
        return param;
    }

    private Map<String, Object> validParam(JSONObject vo, String[] columns) {
        Map<String, Object> param = new HashMap<>();
        for (String ci : columns) {
            Object value2 = vo.get(ci);
            if (value2 != null && !"".equals(value2) && !"null".equals(value2)) {
                if (value2 instanceof String s) {
                    param.put(ci, s.trim());
                } else {
                    param.put(ci, value2);
                }

            }
        }
        return param;
    }

    /**
     * 带结构的查询
     *
     * @param label
     * @param vo
     * @return
     * @throws DefineException
     */
    @ControllerLog(description = "查询结构数据")
    @RequestMapping(value = "/getStructData", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult getStructData(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        LabelValidator.validateLabel(label);
        PageObject page = crudUtil.validatePage(vo);
        String[] columns = crudUtil.getMdColumns(label);
        // 脱敏处理字段

        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        vo.remove("page");
        if (label.equals("DBTable")) {
            rule.validMyRule(label, vo, md);
        }
        String queryText = string(vo, "KeyWord");
        if (queryText != null && !"".equals(queryText.trim())) {
            vo.put(NAME, queryText);
            // vo.put(CODE, queryText);
            vo.remove("KeyWord");
        }
        optByUserSevice.setAdminService(adminService);
        String query = optByUserSevice.queryObj(vo, label, columns, page);
        List<Map<String, Object>> dataList = neo4jService.query(query,vo);
        if (dataList != null) {
            rule.formateQueryField(dataList);
            if (!dataList.isEmpty() && !vo.containsKey(ID)) {
                page.setTotal(crudUtil.total(query));
            }
            deSensitive(label, dataList);
        }

        List<Map<String, Object>> propInfoList = queryMetaRelDefine(label, "struct");

        Map<String, Object> mainData = dataList.get(0);
        // 逐个处理属性
        for (Map<String, Object> pi : propInfoList) {
            String piLabel = label(pi);
            String propKey = string(pi, "prop");
            StringBuilder sb = new StringBuilder("MATCH (s:" + label + ")");
            sb.append("-[r:prop]->(e:" + piLabel + ")");
            sb.append(" where id(s)=" + id(mainData) + " return props(e)");
            List<Map<String, Object>> piList = neo4jService.cypher(sb.toString());
            if (piList != null && !piList.isEmpty()) {
                mainData.put(propKey, piList);
            }
        }

        return ResultWrapper.wrapResult(true, mainData, page, QUERY_SUCCESS);
    }

    // @RequestMapping(value = "/myQuery", method = { RequestMethod.POST,
    // RequestMethod.GET }, produces = "application/json;charset=UTF-8")
    // public WrappedResult myQuery(@PathVariable("po") String label, @RequestBody
    // JSONObject vo) throws DefineException {
    // PageObject page = crudUtil.validatePage(vo);
    // String[] columns = crudUtil.getPoColumn(label);
    // // 脱敏处理字段
    //
    // if (columns == null || columns.length <= 0) {
    // return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
    // }
    //
    // Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
    // vo.remove("page");
    // if (label.equals("DBTable")) {
    // neo4jService.validMyRule(label, vo, po);
    // }
    // String queryText = string(vo, "KeyWord");
    // if (queryText != null && !"".equals(queryText.trim())) {
    // vo.put(NAME, queryText);
    // // vo.put(CODE, queryText);
    // vo.remove("KeyWord");
    // }
    // optByUserSevice.setAdminService(adminService);
    // Long currentUserId = adminService.getCurrentUserId();
    // //如何把当前用户信息，怎么配，根据、MyQuery
    // List<Map<String, Object>> myField = neo4jService.cypher("match (n:MyQuery)
    // where n.labeli='"+label+"' return n.field as myField" );
    // if(myField!=null&&!myField.isEmpty()) {
    // String string = string(myField.get(0),"myField");
    // vo.put(string, currentUserId);
    // }
    //
    //
    // String query = optByUserSevice.query(vo, label, columns, page);
    // List<Map<String, Object>> dataList = neo4jService.cypher(query);
    // return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    // }

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

    /**
     * 处理基本查询请求，支持POST和GET方法。
     * @param label 路径变量，表示标签名称，用于验证和查询。
     * @param vo 请求体中的JSONObject，包含查询条件和分页信息。
     * @return 包含查询结果和状态信息的WrappedResult对象。
     * @throws DefineException 如果标签验证失败或其他业务规则不满足时抛出。
     */
    @RequestMapping(value = "/bQuery", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult basicQuery(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        // 验证标签的合法性
        LabelValidator.validateLabel(label);
        // 根据请求体中的信息验证和构建分页对象
        PageObject page = crudUtil.validatePage(vo);

        // 根据标签名称获取对应的列信息
        String[] columns = crudUtil.getMdColumns(label);
        // 如果没有获取到列信息，返回查询失败的结果
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        Map<String, Object> data = new HashMap<>();
        // 通过标签名称获取元数据信息
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        // 移除请求体中的分页信息，以免影响后续处理
        vo.remove("page");
        // 如果标签是DBTable，需要进行额外的规则验证
        if (label.equals("DBTable")) {
            rule.validMyRule(label, vo, md);
        }
        // 从请求体中提取查询关键字，并根据情况更新请求体
        String queryText = string(vo, "KeyWord");
        if (queryText != null && !"".equals(queryText.trim())) {
            vo.put(NAME, queryText);
            vo.remove("KeyWord");
        }

        // 设置管理员服务，以便后续操作
        optByUserSevice.setAdminService(adminService);
        // 构建查询语句，用于获取数据关系和列信息
        String query = "match(n:" + META_DATA + ")-[r:domainData]->(d:" + META_DATA + ") where n.label='" + label + "' return d.label AS label,d.columns AS columns,r.prop AS prop";
        // 执行查询，获取数据
        List<Map<String, Object>> dataList = neo4jService.cypher(query);
        // 如果有查询结果，处理并组织数据
        if (dataList != null) {
            for (Map<String, Object> di : dataList) {
                String labeli = string(di, "label");
                String prop = string(di, "prop");
                String columnLowcase = string(di, "columns").toLowerCase();
                String[] columnsi = splitColumnValue(di, "columns");
                List<Map<String, Object>> dataiList = null;
                // 根据列信息判断是否需要获取整棵树，或者根据列信息查询数据
                if (columnLowcase.indexOf("isroot") > 0 || columnLowcase.indexOf("parentid") > 0) {
                    Map<String, Object> tree = neo4jService.getWholeTree(labeli);
                    if (tree != null) {
                        dataiList = listMapObject(tree, REL_TYPE_CHILDREN);
                    }
                } else {
                    String queryLabeli = optByUserSevice.getAllObject(labeli, columnsi);
                    dataiList = neo4jService.cypher(queryLabeli);
                }
                // 根据属性名或标签名组织查询结果
                if (prop != null) {
                    data.put(prop, dataiList);
                } else {
                    data.put(StringGet.firstLow(labeli) + "List", dataiList);
                }
            }
        }
        // 返回查询成功的结果，包含处理后的数据
        return ResultWrapper.wrapResult(true, data, page, QUERY_SUCCESS);
    }

    /**
     * 处理短查询请求。
     * 此方法支持POST和GET请求，返回格式为JSON。
     * 主要用于根据提供的标签和请求体中的条件，查询对应的数据。
     *
     * @param label 请求路径中的标签参数，用于指定查询的对象类型。
     * @param vo 请求体中的JSON对象，包含查询条件和分页信息。
     * @return 返回一个封装了查询结果的WrappedResult对象。
     * @throws DefineException 如果查询过程中遇到定义错误，抛出此异常。
     */
    @MyAnnotation
    @RequestMapping(value = "/sQuery", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult shortQuery(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        // 验证标签的合法性
        LabelValidator.validateLabel(label);
        // 根据请求体中的条件验证和构建分页对象
        PageObject page = crudUtil.validatePage(vo);


        // 从Neo4j中获取标签的元数据
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        // 获取标签对应的短列名数组
        String[] columns =  MapTool.shortShow(md);
        // 如果没有获取到有效的列名，返回查询失败的结果
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        // 移除请求体中的分页信息，因为它不再需要
        vo.remove("page");
        // 如果标签是"DBTable"，则对请求体进行额外的规则验证
        if (label.equals("DBTable")) {
            rule.validMyRule(label, vo, md);
        }
        // 从请求体中提取查询关键字
        String queryText = string(vo, "KeyWord");
        // 如果存在关键字，将其作为查询名称，并从请求体中移除关键字
        if (queryText != null && !"".equals(queryText.trim())) {
            vo.put(NAME, queryText);
            vo.remove("KeyWord");
        }
        // 设置管理员服务，以便后续操作
        optByUserSevice.setAdminService(adminService);
        // 根据请求体和列名生成查询语句，并执行查询
        String query = optByUserSevice.queryObj(vo, label, columns, page);
        List<Map<String, Object>> dataList = neo4jService.query(query,vo);
        // 如果查询到数据，对其进行格式化，并根据需要计算总条数
        if (dataList != null) {
            rule.formateQueryField(dataList);
            if (!dataList.isEmpty() && !vo.containsKey(ID)) {
                page.setTotal(crudUtil.total(query,vo));
            }
            // 对查询结果进行脱敏处理
            deSensitive(label, dataList);
        }
        // 返回查询成功的结果，包含数据列表和分页信息
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }

    @MyAnnotation
    @RequestMapping(value = "/select", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult select(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        // 验证标签的合法性
        LabelValidator.validateLabel(label);
        // 根据请求体中的条件验证和构建分页对象
        PageObject page = crudUtil.validatePage(vo);

        // 从Neo4j中获取标签的元数据
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);

        // 获取标签对应的短列名数组
        String[] columns =  MapTool.shortShow(md);
        // 如果没有获取到有效的列名，返回查询失败的结果
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        // 移除请求体中的分页信息，因为它不再需要
        vo.remove("page");
        // 如果标签是"DBTable"，则对请求体进行额外的规则验证
        if (label.equals("DBTable")) {
            rule.validMyRule(label, vo, md);
        }
        // 从请求体中提取查询关键字
        String queryText = string(vo, "KeyWord");
        // 如果存在关键字，将其作为查询名称，并从请求体中移除关键字
        if (queryText != null && !"".equals(queryText.trim())) {
            vo.put(NAME, queryText);
            vo.remove("KeyWord");
        }
        // 设置管理员服务，以便后续操作
        optByUserSevice.setAdminService(adminService);
        // 根据请求体和列名生成查询语句，并执行查询
        String query = optByUserSevice.queryObj(vo, label, columns, page);
        List<Map<String, Object>> dataList = neo4jService.query(query,vo);
        // 如果查询到数据，对其进行格式化，并根据需要计算总条数
        if (dataList != null) {
            rule.formateQueryField(dataList);
            if (!dataList.isEmpty() && !vo.containsKey(ID)) {
                page.setTotal(crudUtil.total(query,vo));
            }
            // 对查询结果进行脱敏处理
            deSensitive(label, dataList);
        }
        // 返回查询成功的结果，包含数据列表和分页信息
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }


    /**
     * 处理查询请求，支持POST和GET方法。
     * 该方法主要用于根据提供的标签和查询条件，从数据库中检索数据。
     * @param label 请求路径中的标签参数，用于指定查询的对象类型。
     * @param vo 请求体中的JSON对象，包含查询条件和分页信息。
     * @return 包含查询结果和状态信息的封装对象。
     * @throws DefineException 如果查询过程中遇到定义错误，抛出此异常。
     */
    @MyAnnotation
    @RequestMapping(value = "/calQuery", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult calQuery(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        // 验证标签的合法性
        LabelValidator.validateLabel(label);
        // 校验并获取分页信息
        PageObject page = crudUtil.validatePage(vo);
        // 根据标签获取对应的列信息
        String[] columns = crudUtil.getMdColumns(label);
        // 如果列信息为空或长度为0，则查询失败
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        // 从数据库中获取标签的元数据
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        // 移除查询条件中的分页信息，以免影响后续处理
        vo.remove("page");
        // 如果标签是"DBTable"，则需要进行额外的规则验证
        if (label.equals("DBTable")) {
            rule.validMyRule(label, vo, md);
        }
        // 从查询条件中提取关键字，如果存在，则将其作为查询名称
        String queryText = string(vo, "KeyWord");
        if (queryText != null && !"".equals(queryText.trim())) {
            vo.put(NAME, queryText);
            vo.remove("KeyWord");
        }
        // 设置管理员服务，以便后续操作
        optByUserSevice.setAdminService(adminService);
        // 根据查询条件、标签、列信息和分页信息，构造查询语句
        String query = optByUserSevice.cqueryObj(vo, label, columns, page);
        // 执行查询，并获取结果数据
        List<Map<String, Object>> dataList = neo4jService.cypher(query);
        // 格式化查询结果字段
        if (dataList != null) {
            rule.formateQueryField(dataList);
            // 更新分页对象的总记录数
            if (!dataList.isEmpty()) {
                page.setTotal(crudUtil.total(query));
            }
        }
        // 封装查询成功的结果
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }


    @ControllerLog(description = "查询数据")
    @RequestMapping(value = "/queryData", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    public WrappedResult queryData(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        LabelValidator.validateLabel(label);
        try {
            adminService.checkAuth(label, "QueryOperate", "查询操作");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }
        PageObject page = crudUtil.validatePage(vo);
        String[] columns = crudUtil.getMdColumns(label);
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        String query = optByUserSevice.queryObj(vo, label, columns, page);

        JSONObject params = new JSONObject();
        params.put("poId", label);
        // 查询自定义字段数据
        // 获取自定义字段信息。
        List<Map<String, Object>> dataList = neo4jService.query(query,vo);
        if (dataList != null) {
            // 更新数据
            // 获取字段定义，是文件的，且不为空，则转换为图片地址。
            dataList=formateFileSrc(label, dataList);
            dataList=formatDates(dataList);
            rule.formateQueryField(dataList);
            if (!dataList.isEmpty()) {
                page.setTotal(crudUtil.total(query,vo));
            }
        }
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }

    private List<Map<String, Object>> formateFileSrc(String label, List<Map<String, Object>> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return dataList;
        }
        List<Map<String, Object>> copyList = MapTool.copyList(dataList);
        Map<String,Map<String, Object>> dataMap = new HashMap<>();
        for(Map<String, Object> di : copyList){
            dataMap.put(string(di,"id"),di);
        }


        List<Map<String, Object>> fileList = neo4jService.cypher("MATCH (m:Field{poId:\"" + label + "\"}) where m.showType  CONTAINS 'Upload' return distinct m.field AS field");
        if (fileList != null && !fileList.isEmpty()) {
            for (Map<String, Object> di : dataList) {
                for (Map<String, Object> fi : fileList) {
                    String field = string(fi, "field");
                    Map<String, Object> datai = dataMap.get(string(di, "id"));
                    datai.put(field, LemodoApplication.MODULE_NAME+"/file/show/" + string(datai, field));
                }
            }
        }
        return copyList;
    }

    @RequestMapping(value = "/getData", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult getData(@PathVariable("po") String label) throws DefineException {
         LabelValidator.validateLabel(label);
        JSONObject vo = new JSONObject();
        PageObject page = crudUtil.validatePage(vo);
        String[] columns = crudUtil.getMdColumns(label);
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        String query = optByUserSevice.queryObj(vo, label, columns, page);
        List<Map<String, Object>> dataList = neo4jService.query(query,vo);
        if (dataList != null) {
            for (Map<String, Object> mi : dataList) {
                rule.formateQueryField(mi);
            }
            if (!dataList.isEmpty()) {
                page.setTotal(crudUtil.total(query,vo));
            }
        }
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "getBy/{relLabel}/{instanceLabel}/{instanceId}", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult getByInstance(@PathVariable("po") String label, @PathVariable("instanceLabel") String instanceLabel, @PathVariable("relLabel") String relLabel, @PathVariable("instanceId") String instanceId, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        PageObject page = crudUtil.validatePage(vo);
        String[] columns = crudUtil.getMdColumns(label);
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        String query = Neo4jOptCypher.queryByRelInstance(vo, label, relLabel, columns, page, instanceLabel, instanceId);
        List<Map<String, Object>> query2 = neo4jService.cypher(query);
        page.setTotal(crudUtil.total(query));
        return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
    }

    @ControllerLog(description = "根据Lable获取当前用户拥有的数据")
    @RequestMapping(value = "getMyData", method = {RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public Result<Map<String, Object>> getMyData(@PathVariable("po") String label) throws DefineException {
         LabelValidator.validateLabel(label);
        Long currentUserId = adminService.getCurrentUserId();
        if (currentUserId == null || currentUserId == 0) {
            //test
            String getMyData = "MATCH (u:User)-[r:myData]->(n:" + label + ") where u.username='liuqiang' return n";
            Map<String, Object> query2 = neo4jService.getOne(getMyData);
            return Result.success(query2, QUERY_SUCCESS);
        }
        String getMyData = "MATCH (u:User)-[r:myData]->(n:" + label + ") where id(u)=" + currentUserId + " return n";
        Map<String, Object> query2 = neo4jService.getOne(getMyData);
        return Result.success(query2, QUERY_SUCCESS);
    }

    @ControllerLog(description = "根据Lable获取当前用户拥有的数据")
    @RequestMapping(value = "getOne", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    public Result<Map<String, Object>> getOne(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
//	String getMyData = "MATCH (u:"+label+")-[r:myData]->(n:"+label+") where id(u)="+adminService.getCurrentUserId()+" return n";
//	String query = optByUserSevice.query(vo, label);
         LabelValidator.validateLabel(label);
        PageObject page = crudUtil.validatePage(vo);
        String[] columns = crudUtil.getMdColumns(label);
        // 脱敏处理字段
        if (columns == null || columns.length <= 0) {
            return Result.failed();
        }
        Map<String, Object> validParam = validParam(vo, columns);
        String query = optByUserSevice.getObj(validParam, label, columns, page);
        Map<String, Object> query2 = neo4jService.getOne(query,validParam);
        return Result.success(query2, QUERY_SUCCESS);
    }

    @ControllerLog(description = "根据Lable获取当前用户拥有的数据")
    @RequestMapping(value = "getList", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    public Result<List<Map<String, Object>>> getList(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        LabelValidator.validateLabel(label);
        PageObject page = crudUtil.validatePage(vo);
        String[] columns = crudUtil.getMdColumns(label);
        // 脱敏处理字段
        if (columns == null || columns.length <= 0) {
            return Result.failed();
        }
        Map<String, Object> validParam = validParam(vo, columns);
        String query = optByUserSevice.getObj(validParam, label, columns, page);
        List<Map<String, Object>> query2 = neo4jService.query(query,validParam);
        return Result.success(query2, QUERY_SUCCESS);
    }

    @ControllerLog(description = "根据Lable获取当前用户拥有的数据")
    @RequestMapping(value = "cypher", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    public Result<List<Map<String, Object>>> cypher(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        LabelValidator.validateLabel(label);
        String[] columns = crudUtil.getMdColumns(label);
        // 脱敏处理字段
        if (columns == null || columns.length <= 0) {
            return Result.failed();
        }
        Map<String, Object> validParam = validParam(vo, columns);
        String query = MapTool.cypher(vo);
        List<Map<String, Object>> query2 = neo4jService.query(query,validParam);
        return Result.success(query2, QUERY_SUCCESS);
    }

    @ControllerLog(description = "根据Lable获取当前用户拥有的数据")
    @RequestMapping(value = "getMyDataList", method = {RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public Result<List<Map<String, Object>>> getMyDataList(@PathVariable("po") String label) throws DefineException {
         LabelValidator.validateLabel(label);
        String getMyData = "MATCH(n:" + label + ")<-[r:myData]-(u:User) where id(u)=" + adminService.getCurrentUserId() + " return n";
        List<Map<String, Object>> query2 = neo4jService.cypher(getMyData);
        return Result.success(query2, QUERY_SUCCESS);
    }

    @MyAnnotation
    @RequestMapping(value = "/children", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult queryChild(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        PageObject page = crudUtil.validatePage(vo);
        String[] columns = crudUtil.getMdColumns(label);
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        Map<String, Object> nextOneLevelChildren = neo4jService.nextOneLevelChildren(label, vo);
        return ResultWrapper.wrapResult(true, nextOneLevelChildren, page, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/childrenList", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult childrenList(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        PageObject page = crudUtil.validatePage(vo);
        String[] columns = crudUtil.getMdColumns(label);
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        List<Map<String, Object>> chidList = neo4jService.chidlList(label, vo);
        if (chidList != null && !chidList.isEmpty()) {
            page.setPageSize(chidList.size());
            page.setTotal(chidList.size());
        }

        return ResultWrapper.wrapResult(true, chidList, page, QUERY_SUCCESS);
    }

    @ControllerLog(description = "查询子表数据")
    @RequestMapping(value = "/childList/{endLabel}", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult childList(@PathVariable("po") String label, @PathVariable("endLabel") String endLabel, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        PageObject page = crudUtil.validatePage(vo);
        if (vo.isEmpty()) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }

        String[] columns = crudUtil.getMdColumns(endLabel);
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        JSONObject jo = new JSONObject();
        jo.put("id", vo.getString("id"));
        StringBuilder labelChild = optByUserSevice.getLabelChild(page, jo, label, endLabel, columns);

        String query = labelChild.toString();
        List<Map<String, Object>> chidList = neo4jService.cypher(query);
        if (chidList != null && !chidList.isEmpty()) {
            page.setTotal(crudUtil.total(query));
        } else {
            page.setTotal(0);
        }

        return ResultWrapper.wrapResult(true, chidList, page, QUERY_SUCCESS);
    }


    @ControllerLog(description = "查询子表数据")
    @RequestMapping(value = "/childTableList", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult childTableList(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        PageObject page = crudUtil.validatePage(vo);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);

        String[] columns = columns(md);
        if (columns == null || columns.length <= 0) {
            return ResultWrapper.wrapResult(false, null, page, QUERY_FAILED);
        }
        String one = neo4jService.getOne("Match(t:TreeDefine) where t.mdLabel='" + label + "' return t.parentIdField AS parentIdField", "parentIdField");
        Map<String, Object> param = newMap();
        if (one == null) {
            one = PARENT_ID;
        }
        String relateValue = string(vo, one);
        if (parentId(vo) != null && relateValue == null) {
            param.put(one, parentId(vo));
        }
        if (relateValue != null) {
            param.put(one, relateValue);
        }

        List<Map<String, Object>> chidList = neo4jService.chidrenlList(label, param, one);
        if (chidList != null && !chidList.isEmpty()) {
            page.setPageSize(chidList.size());
            page.setTotal(chidList.size());
        }

        return ResultWrapper.wrapResult(true, chidList, page, QUERY_SUCCESS);
    }

    @ControllerLog(description = "获取关系的终点Id")
    @RequestMapping(value = "/getEndId/{relation}", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public List<Long> getEndId(@PathVariable("po") String label, @PathVariable("relation") String relation, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        return neo4jService.endNodeIdList(label, relation, vo);
    }

    @ControllerLog(description = "查询关系数据")
    @RequestMapping(value = "/rel/{relation}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult relations(@PathVariable("po") String label, @PathVariable("relation") String relation, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        PageObject page = crudUtil.validatePage(vo);

        String query = crudUtil.relationQuery(label, relation, vo, page);
        page.setTotal(crudUtil.total(query));
        JSONArray query2 = neo4jService.relation(query);
        return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
    }

    @ControllerLog(description = "查询某类关系")
    @RequestMapping(value = "/oneRel/{endLabel}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult oneRel(@PathVariable("po") String label, @PathVariable("endLabel") String endLabel, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        String query = crudUtil.oneEndRelationQuery(label, endLabel, vo);
        JSONArray query2 = neo4jService.relationOne(query);
        return ResultWrapper.wrapResult(true, query2, null, QUERY_SUCCESS);
    }

    @ControllerLog(description = "分页查询某类关系")
    @RequestMapping(value = "/oneRelPage/{endLabel}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult oneRelPage(@PathVariable("po") String label, @PathVariable("endLabel") String endLabel, @RequestBody JSONObject vo) throws DefineException {
        PageObject page = crudUtil.validatePage(vo);
 LabelValidator.validateLabel(label);
        String query = crudUtil.oneEndRelationQuery(label, endLabel, vo, page);
        page.setTotal(crudUtil.total(query));
        JSONArray query2 = neo4jService.relationOne(query);
        return ResultWrapper.wrapResult(true, query2, page, QUERY_SUCCESS);
    }

    @ControllerLog(description = "删除关系")
    @RequestMapping(value = "/rel/{relation}/del", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public WrappedResult relationsDel(@PathVariable("po") String label, @PathVariable("relation") String relation, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        String nodeLabelByNodeId = neo4jService.getNodeLabelByNodeId(endId(vo));
        Boolean differentDirctionRel = relationService.isDifferentDirctionRel(relation, label, nodeLabelByNodeId);
        boolean delRelation =false;
            Map<String, Object> param=copy(vo);
            param.put("startLabel",label);
            param.put("endLabel",nodeLabelByNodeId);
            param.put("relation",relation);
            param.put("reverse",differentDirctionRel);
            relationService.delRelation(param);
            return ResultWrapper.ret(true, true, DELETE_SUCCESS);
    }

    @ControllerLog(description = "新增关系")
    @RequestMapping(value = "/rel/{relation}/save", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult relSave(@PathVariable("po") String label, @PathVariable("relation") String relation, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (md == null || md.isEmpty()) {
            throw new DefineException(label + "未定义！");
        }
        Map<String, Object> relationDef = neo4jService.getAttMapBy(LABEL, relation, REALTION);
        if (relationDef == null || relationDef.isEmpty()) {
            return ResultWrapper.wrapResult(true, null, null, "关系未定义！");
        }

        String endLabel = String.valueOf(relationDef.get("End"));
        // String startKey = String.valueOf(po.get(CRUD_KEY));

        Map<String, Object> endDomain = neo4jService.getAttMapBy(LABEL, endLabel, META_DATA);
        // String endKey = String.valueOf(endDomain.get(CRUD_KEY));
        Node start = null;
        if (!vo.containsKey("relations")) {
            return ResultWrapper.wrapResult(true, null, null, "没有关系数据可保存！");
        }
        JSONArray relations = vo.getJSONArray("relations");
        for (int i = 0; i < relations.size(); i++) {
            JSONObject ri = relations.getJSONObject(i);
            String value = ri.getString("start");
            String endValue = ri.getString("end");
            start = neo4jService.findBy(NODE_ID, value, endLabel);
            List<Node> endNodes = new ArrayList<>();
            for (String eki : endValue.split(",")) {
                endNodes.add(neo4jService.findBy(NODE_ID, eki, endLabel));
            }
            neo4jService.addRelations(start, endNodes, relation);
        }

        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    @RequestMapping(value = "/saveRel", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult saveRel(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        Node start = null;
        if (!vo.containsKey("relations")) {
            return ResultWrapper.wrapResult(true, null, null, "没有关系数据可保存！");
        }
        JSONArray relations = vo.getJSONArray("relations");
        for (int i = 0; i < relations.size(); i++) {
            JSONObject ri = relations.getJSONObject(i);
            String value = ri.getString("start");
            String endValue = ri.getString("end");
            relationService.addRel(REL_TYPE_CHILDREN, value, endValue);
        }

        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }


    @RequestMapping(value = "/addRel", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult addRel(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (md == null || md.isEmpty()) {
            return ResultWrapper.wrapResult(true, null, null, label + "元数据缺失！");
        }
        String startIds = vo.getString("start");
        String endIds = vo.getString("end");
        String rel = vo.getString("rel");
        String relName = vo.getString("relName");
        String lowerCase = rel.toLowerCase();
        if (rel.length() > 10 || lowerCase.indexOf("create") > 0 || lowerCase.indexOf("delete") > 0 || lowerCase.indexOf("remove") > 0) {
            return ResultWrapper.wrapResult(true, null, null, rel + "敏感！");
        }
        try {
            relationService.addRel(rel, relName, startIds, endIds);
        } catch (NumberFormatException e) {
            LoggerTool.error(logger,"add rel exception", e);
        }
        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    @RequestMapping(value = "/addTansferRel", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult addTansferRel(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (md == null || md.isEmpty()) {
            return ResultWrapper.wrapResult(true, null, null, label + "元数据缺失！");
        }
        String startIds = vo.getString("start");
        List<Map<String, Object>> list = listMapObject(vo, "ends");
        List<Long> endIdList = new ArrayList<>(list.size());
        for (Map<String, Object> li : list) {
            endIdList.add(longValue(li, "value"));
        }
        String rel = vo.getString("rel");
        String relName = vo.getString("relName");
        String lowerCase = rel.toLowerCase();
        if (lowerCase.indexOf("update") > 0 || lowerCase.indexOf("create") > 0 || lowerCase.indexOf("delete") > 0 || lowerCase.indexOf("remove") > 0) {
            return ResultWrapper.wrapResult(true, null, null, rel + "敏感！");
        }
        try {
            relationService.addRel(rel, relName, startIds, endIdList);
        } catch (NumberFormatException e) {
            LoggerTool.error(logger,"add rel exception", e);
        }
        return ResultWrapper.wrapResult(true, endIdList, null, SAVE_SUCCESS);
    }


    /**
     * 转换关系，将parentId转为children
     *
     * @param label
     * @param vo    {rel:关系Code，relField:关系字段,relProp:{}}
     * @return
     */
    @RequestMapping(value = "/tranRel", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult tranRel(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (md == null || md.isEmpty()) {
            return ResultWrapper.wrapResult(true, null, null, label + "元数据缺失！");
        }
        Set<String> columns2 = columnSet(md);
        String rel = vo.getString("rel");
        String relName = vo.getString("relName");

        String relField = vo.getString("relField");
        if (!columns2.contains(relField)) {
            return ResultWrapper.wrapResult(true, null, null, label + "不包含字段" + relField + "！");
        }
        List<Map<String, Object>> listAllByLabel = neo4jService.listAllByLabel(label);

        for (Map<String, Object> di : listAllByLabel) {
            String metaDataLabel = string(di, "poId");
            if (metaDataLabel == null) {
                continue;
            }
            try {
                Long endId = longValue(di, relField);

                if (endId == null) {
                    Long nodeId = neo4jService.getNodeId(LABEL, metaDataLabel, META_DATA);
                    di.put(relField, nodeId);
                    neo4jService.update(di);
                    endId = nodeId;
                } else {
                    // 数据不同步,出现的问题
                    String nodeLabelByNodeId = neo4jService.getNodeLabelByNodeId(endId);
                    if (nodeLabelByNodeId == null || !metaDataLabel.equals(nodeLabelByNodeId)) {
                        Long nodeId = neo4jService.getNodeId(LABEL, metaDataLabel, META_DATA);
                        di.put(relField, nodeId);
                        neo4jService.update(di);
                        endId = nodeId;
                    }

                }

                // 获取关系属性
                Map<String, Object> relProps = mapObject(vo, "relProp");
                LoggerTool.info(logger,"===rel==" + rel + "=======startId============" + string(md, ID) + "======endId==" + endId);

                Long id2 = id(di);
                if (relProps != null && !relProps.isEmpty()) {
                    relationService.addRel(rel, id2, endId, relProps);
                } else {
                    relProps = new HashMap<>();
                    if (relName == null) {
                        relProps.put(NAME, "子" + name(md));
                    } else {
                        relProps.put(NAME, relName);
                    }

                    relationService.addRel(rel, id2, endId, relProps);
                }
            } catch (Exception e) {
                LoggerTool.error(logger,e.getMessage(), e);
                continue;
            }
        }

        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    @RequestMapping(value = "/addMyRel", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult addMyRel(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (md == null || md.isEmpty()) {
            return ResultWrapper.wrapResult(true, null, null, label + "元数据缺失！");
        }
        String rel = vo.getString("label");
        try {
            // cypherService.addRel(rel,endValue);
            relationService.addCurentActionRel(rel, vo);
        } catch (NumberFormatException e) {
            LoggerTool.error(logger,"add rel exception", e);
        }
        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    @RequestMapping(value = "/saveRelById/{objId}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult saveRelByObjId(@PathVariable("po") String label, @PathVariable("objId") String objId) throws DefineException {
         LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        relationService.addRel("is" + label, id(md), Long.valueOf(objId));
        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    /**
     * 保存父级关系，比如组织机构，用户。在保存用户基本信息后，保存用户所属组织机构的关系。
     *
     * @param label
     * @param vo
     * @return
     */
    @RequestMapping(value = "/saveParentRel", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    @MyAnnotation
    public WrappedResult saveParentRel(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);

        String relAId = string(vo, REL_START_ID);
        String relALabel = string(vo, REL_START_LABEL);
        String endId = string(vo, "endId");
//	String one = neo4jService.getOne("Match(t:TreeDefine) where t.mdId='"+id(md)+"' return t.parentIdField AS parentIdField", "parentIdField");

        relationService.addRel("belong" + relALabel, endId, relAId);
        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    @ControllerLog(description = "保存资源类型")
    @RequestMapping(value = "/saveResourceType/{objId}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult saveResourceType(@PathVariable("po") String label, @PathVariable("objId") String objId) throws DefineException {
         LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(CODE, label, RESOURCE);
        if (md == null || md.isEmpty()) {
            throw new DefineException(label + "未定义！");
        }
        relationService.addRel("is" + label + "Type", Long.valueOf(objId), id(md));
        return ResultWrapper.wrapResult(true, null, null, SAVE_SUCCESS);
    }

    /**
     * 将数据保存到数据库，根据元数据对应的表，将数据保存到数据库。
     * 元数据，读取对应的映射表数据
     * 表数据，数据源。
     * 生成对应的更新语句
     * @param label
     * @return
     * @throws DefineException
     * @throws AuthException
     */
    @ControllerLog(description = "保存数据")
    @RequestMapping(value = "/saveDb", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult saveDb(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException, AuthException{
        LabelValidator.validateLabel(label);
        if(vo.isEmpty()||!crudUtil.isColumnsNotEmpty(vo)){
            return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
        }

        LabelValidator.validateLabel(label);

        Map<String, Object> md=null;
        try{
            md =adminService.checkAuth(label, "SaveOperate", "保存更新");
        }catch(Exception e){
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }

        crudUtil.clearColumnOrHeader(vo);
        Map<String, Object>   voCompatible=compatDb(vo, md);
        Map<String, Object> entity=dbInfoService.toEntity(voCompatible, label+"DBColumn");
        if(entity==null){
            return ResultWrapper.wrapResult(false, null, null, SAVE_FAILED);
        }
        Map<String, Object> dbColumnMap=neo4jService.getAttMapBy(CODE, label+"DBColumn", "DbColumnMap");
        String table =  string(dbColumnMap,"table");

        List<Map<String, Object>> datasource=neo4jService.cypher("match(n)-[r:datasource]->(m) where n.id="+id(md)+" return m");
        dbInfoService.initConnect(datasource.get(0));


//        StringBuilder sb = new StringBuilder();
//        StringBuilder where = new StringBuilder();
//        String[] columns = crudUtil.getTableColumn(table);
//        String idString = vo.getString(ID);
//        String bigId = vo.getString(ID_BIG);
//        if ((idString == null || idString.isBlank())
//                && (bigId == null || bigId.isBlank())) {
//            // vo.put(ID, UUIDUtil.getUUID());
//            String columnxs = String.join(",", columns);
//            if (columnxs.toLowerCase().indexOf(",createtime,") > 0) {
//                vo.put("CREATETIME", DateTool.now());
//            }
//
//            if (columnxs.toLowerCase().indexOf(",create_time,") > 0) {
//                vo.put("CREATE_TIME", DateTool.now());
//            }
//            StringBuilder sBuilder = new StringBuilder();
//            List<String> keyList = DBOptUtil.insert(vo, table, sBuilder, columns);
//
//            boolean dataList = dbInfoService.prepareExcute(sBuilder.toString(), vo, keyList, columTypeMap(tableMap));
//
//            return ResultWrapper.wrapResult(true, dataList, null, QUERY_SUCCESS);
//        } else {
//            int total;
//            try {
//                if(idString==null) {
//                    idString=bigId;
//                }
//                table.split("from");
//                String byId = DBOptUtil.getById(table,idString);
//
//                List<Map<String, Object>> query = dbInfoService.cypher(byId);
//                total= query.size();
//                if(total>0) {
//                    StringBuilder sBuilder = new StringBuilder();
//                    List<String> keyList = DBOptUtil.update(vo, table, sBuilder,columns);
//                    boolean dataList = dbInfoService.prepareExcute(sBuilder.toString(), vo, keyList, columTypeMap(tableMap));
//                    return ResultWrapper.wrapResult(true, dataList, null, QUERY_SUCCESS);
//                }else {
//                    String columnxs = String.join(",", columns);
//                    if (columnxs.toLowerCase().indexOf(",createtime,") > 0) {
//                        vo.put("CREATETIME", DateTool.now());
//                    }
//
//                    if (columnxs.toLowerCase().indexOf(",create_time,") > 0) {
//                        vo.put("CREATE_TIME", DateTool.now());
//                    }
//                    StringBuilder sBuilder = new StringBuilder();
//                    List<String> keyList = DBOptUtil.insert(vo, table, sBuilder,columns);
//
//                    boolean dataList = dbInfoService.prepareExcute(sBuilder.toString(), vo, keyList, columTypeMap(tableMap));
//
//                    return ResultWrapper.wrapResult(true, dataList, null, QUERY_SUCCESS);
//                }
//            } catch (Exception e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//
//
//        }
//
//
//        for(String ki:entity.keySet()){
//            String value=string(entity,ki);
//            if(!sb.isEmpty()){
//               sb.append(",");
//            }
//            sb.append(ki);
//            if(value!=null){
//                if(value.contains("'")){
//                    value=value.replace("'", "\"");
//                }
//                sb.append(ki+"="+"'"+value+"'");
//            }else{
//                sb.append("=null");
//            }
//        }

        String id=null;
//        try{
//            List<Map<String, Object>> projectBycode=dbInfoService.cypher("select id from "+table+" where "+sb.toString());
//            if(projectBycode!=null&&!projectBycode.isEmpty()){
//                id=String.valueOf(projectBycode.get(0).get("ID"));
//                entity.put("id",id);
//            }
//        }catch(Exception e){
//            throw new RuntimeException(e);
//        }

//        dbInfoService.prepareExcute("",);
//        dbInfoService.
        Object[] params=new Object[entity.size()+1];
        if(id(entity)!=null){
            Map<String, Object> tableMap = neo4jService.getAttMapBy(TABEL_NAME, table, LABLE_TABLE);
            if (tableMap == null || tableMap.isEmpty()) {
                throw new DefineException(table + "未定义！");
            }
            //更新sql
            int pi=0;
            StringBuilder update = new StringBuilder();
            update.append("update "+table+" set ");
            for(String ki:entity.keySet()){
                String value=string(entity,ki);
                if(!update.isEmpty()){
                    update.append(",");
                }
                update.append(ki);
                if(value!=null){
                    if(value.contains("'")){
                        value=value.replace("'", "\"");
                    }
                    update.append(ki+"="+"'"+value+"'");
                }else{
                    update.append("=null");
                }
            }
            params[pi]=id(entity);
            update.append(" where id=?");
            dbInfoService.prepareExcute(update.toString(),params);
        }else{
            StringBuilder insert = new StringBuilder();
            insert.append("insert into "+table+"(");

            int pi=0;
            for(String ki:entity.keySet()){
                String value=string(entity,ki);
                params[pi]=value;
                if(!insert.isEmpty()){
                    insert.append(",");
                }
                insert.append(ki);
                pi++;
            }
            insert.append(" values(");
            for(String ki:entity.keySet()){
                String value=string(entity,ki);
                if(!insert.isEmpty()){
                    insert.append(",");
                }
                insert.append(ki);
                if(value!=null){
                    if(value.contains("'")){
                        value=value.replace("'", "\"");
                    }
                    insert.append(ki+"="+"'"+value+"'");
                }else{
                    insert.append("=null");
                }
            }
        }
//        if(id(entity)!=null){
//            where.append(" where id="+id(entity));
//        }

        Node saveNode=null;
        Long dataId=id(voCompatible);
        if(dataId!=null){
            neo4jService.update(voCompatible, dataId);
            saveNode=neo4jService.getNodeById(dataId);
        }else{
            saveNode=neo4jService.save(voCompatible, label);
        }

        if(saveNode==null){
            return ResultWrapper.wrapResult(false, null, null, SAVE_FAILED);
        }

        return ResultWrapper.wrapResult(true, saveNode.getId(), null, SAVE_SUCCESS);
    }

    @ControllerLog(description = "保存数据")
    @RequestMapping(value = "/save", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult save(@PathVariable("po") String label, @RequestBody JSONObject vo1) throws DefineException, AuthException {
         LabelValidator.validateLabel(label);
        if (vo1.isEmpty() || !crudUtil.isColumnsNotEmpty(vo1)) {
            return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
        }
         LabelValidator.validateLabel(label);

        Map<String, Object> md = null;
        try {
            md = adminService.checkAuth(label, "SaveOperate", "保存更新");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }
        if (label.equals(LABEL_FIELD)) {
            Long string = longValue(vo1, "objectId");
            Object object = vo1.get("poId");
            if ((object == null || "".equals(object)) && string != null) {
                String nodeLabelByNodeId = label(neo4jService.getNodeMapById(string));
                vo1.put("poId", nodeLabelByNodeId);
            }
        }
        crudUtil.clearColumnOrHeader(vo1);
        //更好的保存时间字段
        Set<String> field2=htmlShowService.getTimeField(label);
        for(String fi: field2){
            DateTool.dateFieldLong(vo1, vo1.get(fi), fi);
        }

        // if (!vo.containsKey(CRUD_KEY)) {
        // vo.put(CRUD_KEY, NODE_ID);
        // }
        completePK(md);
        Map<String, Object> dataC = compat(vo1, md);

        rule.validRule(label, dataC, md);
        if (dataC.containsKey(VALID_RESULT) && dataC.containsKey(VALIDATE_MSG) && !bool(dataC,VALID_RESULT)) {
            String msg = string(dataC, VALIDATE_MSG);
            return ResultWrapper.wrapResult(false, dataC, null, msg);
        }

        if (isComplicated(dataC)) {
            saveStruct(label, dataC);
            // 流程，无流程则创建，有流程则不处理。
            workFlowService.createFlow(label, longValue(md, ID), longValue(dataC, ID));
            return ResultWrapper.wrapResult(true, longValue(dataC, ID), null, SAVE_SUCCESS);
        } else {
            Node saveNode = null;
            Long dataId = id(dataC);
            handleTimeField(label, dataC);

            if(dataId !=null){
                neo4jService.update(dataC, dataId);
                saveNode=neo4jService.getNodeById(dataId);
            }else{
                saveNode = neo4jService.save(dataC, label);
            }

            if (saveNode == null) {
                return ResultWrapper.wrapResult(false, null, null, SAVE_FAILED);
            }

            // 判断字段是有关联其他MetaData
            if (label.toLowerCase().indexOf("define") == -1) {
                if (columnSet(md).contains(STATUS)) {
                    statusService.statusRefresh(label, dataC, saveNode.getId());
                }
                handleRelation(label, dataC, saveNode, md);
            }
            // 流程，无流程则创建，有流程则不处理。
            workFlowService.createFlow(label, longValue(md, ID), saveNode.getId());
            return ResultWrapper.wrapResult(true, saveNode.getId(), null, SAVE_SUCCESS);
        }
    }

    /**
     * 有些字段需要进行比对时，把其值转换到比对值上去。查询时，可以根据时间进行查询，默认使用longTime字段
     * @param label
     * @param dataC
     */
    private void handleTimeField(String label, Map<String, Object> dataC){
        Map<String, Object> param = newMap();
        param.put("poId", label);
        // 查询自定义字段数据
        List<Map<String, Object>> fieldInfoList=objectService.getBy(param, "Field");
        for(Map<String, Object> fi:fieldInfoList){
            if(fi!=null&&!fi.isEmpty()&&MapTool.on(fi,"isLongCompare")){
                dataC.put("longTime",dateLongValue(dataC,string(fi,"field")));
            }
        }
    }


    @ControllerLog(description = "新增数据")
    @RequestMapping(value = "/newData", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult newData(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        try {
            adminService.checkAuth(label, "SaveOperate", "新增操作");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }
        Node nodeId = neo4jService.saveByKey(vo, label, NODE_ID);
        return ResultWrapper.wrapResult(true, nodeId.getId(), null, SAVE_SUCCESS);
    }

    private boolean isComplicated(Map<String, Object> vo) {
        boolean isComplicatedData = false;
        for (Object vi : vo.values()) {
            if (vi instanceof Map || vi instanceof List) {
                isComplicatedData = true;
            }
        }
        return isComplicatedData;
    }

    private Map<String, Object> clearComplicated(Map<String, Object> vo) {
        List<String> keys = new ArrayList<>();
        for (Entry<String, Object> vi : vo.entrySet()) {
            if (vi.getValue() instanceof Map || vi.getValue() instanceof List) {
                keys.add(vi.getKey());
            }
        }
        for (String ki : keys) {
            vo.remove(ki);
        }
        return vo;
    }

    /**
     * 保存关系数据，保存结构数据
     *
     * @param label
     * @param relType 关系类型
     * @param vo
     * @return
     */
    private Long saveRelData(String label, String relType, Map<String, Object> vo) {
        Map<String, Object> copyData = copy(vo);

        Long mainId = neo4jService.saveByBody(vo, label).getId();
        vo.put(ID, mainId);
        handelProp(label, relType, copyData, mainId);
        return mainId;
    }

    /**
     * 处理属性
     *
     * 本函数旨在处理与特定标签和关系类型相关的属性数据，根据提供的标签、关系类型以及待复制的数据，
     * 它首先查询与给定标签和关系类型相关的所有属性信息，然后根据这些信息处理数据中的属性，
     * 最后，如果提供了主ID，则会建立实例数据与属性数据之间的关系。
     *
     * @param label 标签名称，用于查询属性信息
     * @param relType 关系类型，与标签一起用于定义属性
     * @param copyData 包含属性数据的映射，将从中处理和删除特定属性
     * @param mainId 主数据的ID，如果提供，则会尝试建立与属性数据的关系
     */
    private void handelProp(String label, String relType, Map<String, Object> copyData, Long mainId) {
        // 查询与给定标签和关系类型相关的所有属性信息
        List<Map<String, Object>> propInfoList = queryMetaRelDefine(label, relType);

        // 用于存储属性标签与名称的映射
        Map<String, String> propLabelNameMap = new HashMap<>();
        // 用于存储属性ID列表，键为属性标签和属性键的组合
        Map<String, List<Object>> propIds = new HashMap<>();

        // 逐个处理属性信息
        for (Map<String, Object> pi : propInfoList) {
            // 获取当前属性的标签
            String piLabel = label(pi);
            // 获取当前属性的名称
            String piName = name(pi);
            // 获取属性键
            String propKey = string(pi, "prop");
            // 将属性标签和名称的映射存入map
            propLabelNameMap.put(piLabel, piName);

            // 从复制数据中获取当前属性的值
            Object voPropValue = copyData.get(propKey);

            // 如果属性值不为空，则进行处理
            if (voPropValue != null) {
                // 初始化标签ID列表
                List<Object> labelIdList = new ArrayList<>();

                // 如果属性值是列表，则遍历处理每个属性值
                if (voPropValue instanceof List vps && !vps.isEmpty()) {
                    // 将属性值列表从copyData中提取出来
                    List<Map<String, Object>> voPropValues = listMapObject(copyData, propKey);
                    // 从copyData中移除已处理的属性
                    copyData.remove(propKey);

                    // 保存属性数据的ID列表
                    List<Long> savedIdList = new ArrayList<>(voPropValues.size());
                    for (Map<String, Object> propValuei : voPropValues) {
                        // 保存属性数据，并将其ID添加到列表中
                        savedIdList.add(saveProp(label, piLabel, relType, propValuei));
                    }
                    // 将保存的ID列表添加到labelIdList
                    for (Long si : savedIdList) {
                        labelIdList.add(si);
                    }
                    // 将标签和属性键的组合键与ID列表存入propIds
                    propIds.put(piLabel + "-" + propKey, labelIdList);
                }

                // 如果属性值是映射，则处理单个属性值
                if (voPropValue instanceof Map) {
                    // 从copyData中提取出属性值映射
                    Map<String, Object> nodei = mapObject(copyData, propKey);
                    // 从copyData中移除已处理的属性
                    copyData.remove(propKey);

                    // 保存属性数据的ID
                    labelIdList.add(saveProp(label, piLabel, relType, nodei));
                    // 将标签和属性键的组合键与ID列表存入propIds
                    propIds.put(piLabel + "-" + propKey, labelIdList);
                }
            } else {
                // 如果属性值为空，则进行相应处理（可能的占位符处理）
                noPropVar(label, copyData, propIds, piLabel);
            }
        }

        // 清理处理过程中不再需要的数据
        clearComplicated(copyData);

        // 如果提供了主ID，则尝试建立实例数据与属性数据的关系
        if (mainId != null) {
            // 遍历属性ID映射，建立关系
            for (String ki : propIds.keySet()) {
                List<Object> list = propIds.get(ki);
                if (list != null && !list.isEmpty()) {
                    // 解析键值，构建Neo4j关系创建语句，并执行
                    String[] split = ki.split("-");
                    String kLabel = split[0];
                    String createRel = " match(n:" + label + "),(m:" + kLabel + ") where id(n)= " + mainId + " and id(m) IN [" + StringGet.join(",", list) + "] CREATE UNIQUE (n)-[r:" + relType + "{name:\"" + propLabelNameMap.get(kLabel) + "\",prop:\"" + split[1] + "\"}]->(m)";
                    neo4jService.execute(createRel);
                }
            }

            // 兼容性处理，为属性建立额外的关系
            for (String li : propLabelNameMap.keySet()) {
                List<Object> list = propIds.get(li);
                if (list != null && !list.isEmpty()) {
                    // 构建并执行Neo4j关系创建语句
                    String createRel = " match(n:" + label + "),(m:" + li + ") where id(n)= " + mainId + " and id(m) IN [" + StringGet.join(",", list) + "] CREATE UNIQUE (n)-[r:" + li + relType + "{name:\"" + propLabelNameMap.get(li) + "," + "\"}]->(m)";
                    neo4jService.execute(createRel);
                }
            }
        }
    }

    private List<Map<String, Object>> queryMetaRelDefine(String label, String relType) {
        String query = "match(n:" + META_DATA + ")-[r:" + relType + "]->(d:" + META_DATA + ") where n.label='" + label + "' return d.name AS name,d.label AS label,d.columns AS columns,r.prop AS prop,r.name AS rName";
        // 获取属性对象定义信息列表
        List<Map<String, Object>> propInfoList = neo4jService.cypher(query);
        return propInfoList;
    }

    private Long saveStruct(String label, Map<String, Object> vo) {
        return saveRelData(label, "struct", vo);
    }

    // private Long saveStruct(String label, Map<String, Object> vo) {
    // Map<String, Object> copyData = copy(vo);
    // String query = "match(n:" + META_DATA + ")-[r:struct]->(d:" + META_DATA + ")
    // where n.label='" + label
    // + "' return d.name AS name,d.label AS label,d.columns AS columns,r.prop AS
    // prop,r.name AS rName";
    // List<Map<String, Object>> dataList = neo4jService.cypher(query);
    //
    // Map<String, String> labelNameMap = new HashMap<>();
    // Map<String, List<Object>> propIds = new HashMap<>();
    // for (Map<String, Object> mi : dataList) {
    // String miLabel = label(mi);
    // String miName = string(mi, NAME);
    // String prop = string(mi, "prop");
    // labelNameMap.put(miLabel, miName);
    // Object object = vo.get(prop);
    // if (object != null) {
    // List<Object> labelIdList = new ArrayList<>();
    // if (object instanceof List) {
    // List<Map<String, Object>> nodeList = listMapObject(vo, prop);
    // copyData.remove(prop);
    // List<Long> savedIdList = new ArrayList<>(nodeList.size());
    // for (Map<String, Object> nodei : nodeList) {
    // savedIdList.add(saveProp(label, miLabel, nodei));
    // }
    // for (Long si : savedIdList) {
    // labelIdList.add(si);
    // }
    // propIds.put(miLabel + "-" + prop, labelIdList);
    // }
    // if (object instanceof Map) {
    // Map<String, Object> nodei = mapObject(vo, prop);
    // copyData.remove(prop);
    // labelIdList.add(saveProp(label, miLabel, nodei));
    // propIds.put(miLabel + "-" + prop, labelIdList);
    // }
    // } else {
    // noPropVar(label, copyData, propIds, miLabel);
    // }
    // }
    // clearComplicated(copyData);
    // Long mainId = saveObjInfo(label, copyData);
    // if (mainId != null) {
    // vo.put(ID, mainId);
    // for (String ki : propIds.keySet()) {
    // List<Object> list = propIds.get(ki);
    // if (list != null && !list.isEmpty()) {
    // String[] split = ki.split("-");
    // String kLabel = split[0];
    // String createRel = " match(n:" + label + "),(m:" + kLabel + ") where id(n)= "
    // + mainId
    // + " and id(m) IN [" + StringGet.join(",", list) + "] CREATE UNIQUE
    // (n)-[r:struct{name:\""
    // + labelNameMap.get(kLabel) + "\",prop:\"" + split[1] + "\"}]->(m)";
    // neo4jService.execute(createRel);
    // }
    // }
    // for (String li : labelNameMap.keySet()) {
    // List<Object> list = propIds.get(li);
    // if (list != null && !list.isEmpty()) {
    // String createRel = " match(n:" + label + "),(m:" + li + ") where id(n)= " +
    // mainId
    // + " and id(m) IN [" + StringGet.join(",", list) + "] CREATE UNIQUE (n)-[r:" +
    // li + "struct{name:\""
    // + labelNameMap.get(li) + "," + "\"}]->(m)";
    // neo4jService.execute(createRel);
    // }
    // }
    // }
    // return mainId;
    // }

    /**
     * 递归保存复杂结构数据
     *
     * @param label
     * @param miLabel
     * @param relLabel
     * @param nodei
     * @return
     */
    private Long saveProp(String label, String miLabel, String relLabel, Map<String, Object> nodei) {
        Node saveByBody = null;
        if (isComplicated(nodei)) {
            return saveRelData(miLabel, relLabel, nodei);
        } else {
            saveByBody = neo4jService.addNew(nodei, miLabel);
            return saveByBody.getId();
        }
    }

    /**
     * 兼容复杂的属性类型，不规范的属性，找不到属性类型，进行尝试处理属性数据
     *
     * @param label
     * @param copyData
     * @param labelIdMap
     * @param miLabel
     */
    private void noPropVar(String label, Map<String, Object> copyData, Map<String, List<Object>> labelIdMap, String miLabel) {
        Boolean handleLabel = false;
        if (miLabel.startsWith(label) || miLabel.endsWith(label)) {
            String fixLabel = StringGet.fixLabel(miLabel, label);
            handleLabel = handleStructLabel(label, copyData, labelIdMap, miLabel, fixLabel);
        }

        if (handleLabel == null || handleLabel != true) {
            String firstLow = StringGet.firstLow(miLabel);
            handleLabel = handleStructLabel(label, copyData, labelIdMap, miLabel, firstLow);
        }
        if (handleLabel == null || handleLabel != true) {
            String lowerCase = miLabel.toLowerCase();
            handleLabel = handleStructLabel(label, copyData, labelIdMap, miLabel, lowerCase);
        }
        if (handleLabel == null || handleLabel != true) {
            String connectLabel = StringGet.firstLow(label) + miLabel;
            handleLabel = handleStructLabel(label, copyData, labelIdMap, miLabel, connectLabel);
        }
    }

    private Boolean handleStructLabel(String label, Map<String, Object> vo, Map<String, List<Object>> labelIdMap, String miLabel, String alias) {
        List<Object> labelIdList = labelIdMap.get(miLabel);
        String key2 = alias + "List";
        String key = alias + "Info";
        if (vo.containsKey(key2)) {
            List<Map<String, Object>> nodeList = listMapObject(vo, key2);
            vo.remove(key2);
            List<Long> savedIdList = new ArrayList<>(nodeList.size());
            for (Map<String, Object> pi : nodeList) {
                Node saveByBody = null;
                if (isComplicated(pi)) {
                    savedIdList.add(saveStruct(miLabel, pi));
                } else {
                    saveByBody = neo4jService.saveByBody(pi, label, false);
                    savedIdList.add(saveByBody.getId());
                }
            }

            if (labelIdList == null) {
                labelIdList = new ArrayList<>(savedIdList.size());
            }
            for (Long si : savedIdList) {
                labelIdList.add(si);
            }
            labelIdMap.put(miLabel, labelIdList);
            return true;
        }
        if (vo.containsKey(key)) {
            if (!key.equals(label + "Info")) {
                Long longValue = saveObjInfo(miLabel, vo);
                if (labelIdList == null) {
                    labelIdList = new ArrayList<>();
                }
                if (longValue != null) {
                    vo.remove(key);
                    labelIdList.add(longValue);
                    labelIdMap.put(miLabel, labelIdList);
                }
            }

            return true;
        }
        return null;
    }

    private Long saveObjInfo(String label, Map<String, Object> vo) {
        String firstLowLabel = StringGet.firstLow(label);
        Map<String, Object> mainInfo = mapObject(vo, firstLowLabel + "Info");
        if (mainInfo != null) {
            Node saveByBody = neo4jService.saveByBody(mainInfo, label);
            return saveByBody.getId();
        } else {
            return neo4jService.saveByBody(vo, label).getId();
        }
    }

//    private void handleRelation(String label, JSONObject vo, Node savedNode, Map<String, Object> md) {
//        handleRelation(label, vo,savedNode,md);
//    }
    /**
     * 定义类的元数据没有关系和状态。
     *
     * @param label
     * @param vo
     * @param savedNode
     */
    private void handleRelation(String label, Map<String, Object> vo, Node savedNode, Map<String, Object> md) {
        if (savedNode == null) {
            return;
        }

        if (!LABEL_FIELD.equals(label)) {
            List<Map<String, Object>> field2 = htmlShowService.getField(label);
            for (Map<String, Object> fi : field2) {
                Object object = fi.get("isPo");
                // 字段是实体关系的，保存数据时，建立关系。
                if (object != null && !"".equals(object) && Boolean.valueOf(String.valueOf(object))) {
                    String metaData = string(fi, "type");
                    String field = string(fi, "field");
                    String value = string(vo,field);

                    String valueField = string(fi, "valueField");
                    if (valueField == null) {
                        return;
                    }
                    if (value != null && !"".equals(value.trim())) {
                        if (value.indexOf("选择") > -1) {
                            continue;
                        }
                        String headerByCol = getHeaderByCol(md, field);
                        if (valueField.equals(ID)) {
                            Long id2 = so.getIdOfData(value, metaData);
                            if (id2 != null) {
                                relationService.addRel(field, headerByCol, String.valueOf(savedNode.getId()), String.valueOf(id2));
                            }
                        } else {
                            neo4jService.addRel(field, String.valueOf(savedNode.getId()), metaData, valueField, value, headerByCol);
                        }
                    }
                }
            }
        }
        Map<String, Object> treeDefin = neo4jService.getOne("Match(t:TreeDefine) where t.mdId='" + id(md) + "' return t.code AS code,t.parentIdField AS parentIdField");
        String one = string(treeDefin, "parentIdField");

        if (one == null || one.equals(COLUMN_PARENT)) {
            parentIdRelation(label, vo, savedNode);
        } else {
            treeRelation(label, treeDefin, vo, savedNode);
        }

    }


    /**
     * 子节点关系保存
     *
     * @param label
     * @param vo
     * @param saveByKey
     */
    private void parentIdRelation(String label, Map<String, Object> vo, Node saveByKey) {
        String string = string(vo,COLUMN_PARENT);
        if (vo.containsKey(COLUMN_PARENT) && string != null && !string.trim().isEmpty()) {
            if (!vo.containsKey(NODE_ID)) {
                relationService.addRel(REL_TYPE_CHILDREN, string(vo,"parentId"), String.valueOf(saveByKey.getId()));
            } else {
                Boolean existBoolean = false;
                List<Map<String, Object>> relationOneList = neo4jService.getOneRelationList(vo, label, REL_TYPE_CHILDREN);
                if (relationOneList != null && !relationOneList.isEmpty()) {
                    for (Map<String, Object> rei : relationOneList) {
                        Map<String, Object> object = (Map<String, Object>) rei.get(RELATION_ENDNODE_PROP);
                        Object relId = rei.get("id");
                        if (relId != null && relId.equals(string)) {
                            existBoolean = true;
                        }
                    }
                }
                if (!existBoolean) {
                    relationService.addRel(REL_TYPE_CHILDREN, string(vo,"parentId"), String.valueOf(saveByKey.getId()));
                }
            }
        }
    }

    /**
     * 自定义树关系处理。
     *
     * @param label
     * @param treeDefin
     * @param vo
     * @param saveByKey
     */
    private void treeRelation(String label, Map<String, Object> treeDefin, Map<String, Object> vo, Node saveByKey) {
        String relationField = string(treeDefin, "parentIdField");
        String relCode = code(treeDefin);
        String relateFieldValue = string(vo,relationField);
        if (vo.containsKey(relationField) && relateFieldValue != null && !relateFieldValue.trim().isEmpty()) {
            if (!vo.containsKey(NODE_ID)) {
                relationService.addRel(relCode, string(vo,relationField), String.valueOf(saveByKey.getId()));
            } else {
                Boolean existBoolean = false;
                List<Map<String, Object>> relationOneList = neo4jService.getOneRelationList(vo, label, relCode);
                if (relationOneList != null && !relationOneList.isEmpty()) {
                    for (Map<String, Object> rei : relationOneList) {
                        Map<String, Object> object = (Map<String, Object>) rei.get(RELATION_ENDNODE_PROP);
                        Object relId = rei.get("id");
                        if (relId != null && relId.equals(relateFieldValue)) {
                            existBoolean = true;
                        }
                    }
                }
                if (!existBoolean) {
                    relationService.addRel(relCode, string(vo,relationField), String.valueOf(saveByKey.getId()));
                }
            }
        }
    }


    @RequestMapping(value = "/map/{toPo}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult map(@PathVariable("po") String label, @PathVariable("toPo") String toPo) throws DefineException {
         LabelValidator.validateLabel(label);

        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        Map<String, Object> md2 = neo4jService.getAttMapBy(LABEL, toPo, META_DATA);
        List<Map<String, Object>> query2 = neo4jService.listAllByLabel(label);
        List<Map<String, Object>> newDatas = new ArrayList<>(query2.size());
        for (Map<String, Object> qi : query2) {
            Map<String, Object> newData = neo4jService.copyWithKeys(qi, columns(md2));
            newDatas.add(newData);
        }
        List<Long> save = neo4jService.save(newDatas, toPo);
        return ResultWrapper.wrapResult(true, save, null, "复制成功");
    }

    @RequestMapping(value = "/copy", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult copy(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        if (!vo.containsKey("params") || vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
            return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
        }

        Object object = vo.get("params");
        Map<String, Object> paramObject = (Map<String, Object>) JSON.parse(String.valueOf(object));

        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        crudUtil.clearColumnOrHeader(vo);
        // if (!vo.containsKey(CRUD_KEY)) {
        // vo.put(CRUD_KEY, NODE_ID);
        // }
        List<Map<String, Object>> outRelationsnList = new ArrayList<>();
        List<Map<String, Object>> relationOneList = neo4jService.getOneRelationList(md, label, COPY_RELATION);
        if (relationOneList != null && !relationOneList.isEmpty()) {
            List<String> copyLables = new ArrayList<>();
            for (Map<String, Object> reli : relationOneList) {
                Map<String, Object> relPropMap = (Map<String, Object>) reli.get(RELATION_PROP);
                Map<String, Object> propMap = (Map<String, Object>) reli.get(RELATION_ENDNODE_PROP);
                Object object2 = propMap.get(NODE_LABEL);
                if (object2 != null && !COPY_RELATION.equals(object2)) {
                    String endLabel = (String) object2;
                    copyLables.add(endLabel);
                }
            }
            outRelationsnList = neo4jService.getSomeRelationEndNodeId(paramObject, label, copyLables);
        }

        // String crudKey = completePK(po, CRUD_KEY, COLUMNS, HEADER);
        completePK(md);
        Node nodeCopy = neo4jService.copy(paramObject, label, NODE_ID);
        for (Map<String, Object> reli : outRelationsnList) {
            Map<String, Object> endPaMap = new HashMap<>();
            endPaMap.put("id", reli.get("eId"));
            String endLabeli = String.valueOf(reli.get(LABEL));
            Node endNodeCopy = neo4jService.copy(endPaMap, endLabeli, NODE_ID);
            String relLabeli = String.valueOf(reli.get("rLabel"));
            if (relLabeli == null || relLabeli.equals("null")) {
                continue;
            }
            Map<String, Object> relMap = new HashMap<>();
            relMap.put("name", reli.get("rName"));
            relMap.put(LABEL, relLabeli);
            neo4jService.addRelation(nodeCopy, endNodeCopy, relLabeli, relMap);
        }

        return ResultWrapper.wrapResult(true, nodeCopy.getId(), null, SAVE_SUCCESS);
    }

    /*
     * @RequestMapping(value = "/onetree", method = RequestMethod.POST, produces =
     * "application/json;charset=UTF-8")
     *
     * @ResponseBody public WrappedResult onetree(@PathVariable("po") String
     * label, @RequestBody JSONObject vo) { if
     * (!vo.containsKey("params")||vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo))
     * { return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED); } Object
     * object = vo.get("params"); Map<String,Object> paramObject = (Map<String,
     * Object>) JSONObject.parse(String.valueOf(object));
     *
     * Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
     * crudUtil.clearColumnOrHeader(vo); if (!vo.containsKey(CRUD_KEY)) {
     * vo.put(CRUD_KEY, NODE_ID); } Object oneRelation = paramObject.get("relType");
     * if(oneRelation==null) { return ResultWrapper.wrapResult(false, null, null,
     * "params中的参数relType必填"); }
     *
     * List<String> relationLabelList = new ArrayList<String>(); String valueOf =
     * String.valueOf(oneRelation); if(!valueOf.contains(",")) {
     * relationLabelList.add(valueOf); }else { for(String ri:valueOf.split(",")) {
     * relationLabelList.add(ri); } }
     *
     * List<Map<String, Object>> outRelationsnList = neo4jService.getPartTree(label,
     * String.valueOf(object), relationLabelList);
     *
     * return ResultWrapper.wrapResult(true, outRelationsnList, null,
     * QUERY_SUCCESS); }
     */

    @RequestMapping(value = "/save/{relLabel}/{startLabel}/{startId}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult saveInstance(@PathVariable("po") String label, @PathVariable("relLabel") String relLabel, @PathVariable("startLabel") String startLabel, @PathVariable("startId") String startId, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        try {
            adminService.checkAuth(label, "SaveOperate", "保存更新操作");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }
        if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
            return ResultWrapper.wrapResult(true, null, null, SAVE_FAILED);
        }

        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (md == null || md.isEmpty()) {
            throw new DefineException(label + "未定义！");
        }
        Map<String, Object> startPo = neo4jService.getAttMapBy(LABEL, startLabel, META_DATA);

        String relationName = neo4jService.getRelationName(startLabel, META_DATA, relLabel);
        crudUtil.clearColumnOrHeader(vo);
        // if (!vo.containsKey(CRUD_KEY)) {
        // vo.put(CRUD_KEY, NODE_ID);
        // }
        // String crudKey = completePK(po, CRUD_KEY, COLUMNS, HEADER);
        completePK(md);
        Node endNode = neo4jService.saveByKey(vo, label, NODE_ID);

        Node startNode = neo4jService.findBy(NODE_ID, startId, startLabel);

        neo4jService.saveRelationDefine(label, relLabel, startLabel, relationName);

        relationService.addRel(relLabel, relationName, startId, String.valueOf(endNode.getId()));
//	.addRelation(startNode, endNode, relLabel, relationName);
        return ResultWrapper.wrapResult(true, endNode.getId(), null, SAVE_SUCCESS);
    }

    /**
     * 获取指定类的字段列表
     *
     * @param label 指定类的标签，用于查询该类的字段信息
     * @return WrappedResult 包含查询结果的封装对象，其中字段列表以特定格式返回
     */
    @RequestMapping(value = "/fieldList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult fieldList(@PathVariable("po") String label) throws DefineException {
         LabelValidator.validateLabel(label);
        // 尝试通过类标签和预定义的元数据键获取字段信息的映射
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        // 如果未获取到预期的信息，则尝试仅使用类标签获取属性映射
        if (md == null) {
            md = neo4jService.getPropMapBy(label);
        }
        // 如果仍未获取到信息，则返回查询失败的结果
        if (md == null) {
            return ResultWrapper.wrapResult(false, null, null, QUERY_FAILED);
        }

        // 根据获取到的映射信息，提取列名和头信息
        String[] columnArray = columns(md);
        String[] headers = headers(md);

        // 根据列名和头信息，构造字段列表
        List<Map<String, String>> filesList = new ArrayList<>(headers.length);
        for (int i = 0; i < headers.length; i++) {
            Map<String, String> e = new HashMap<>();
            e.put("code", columnArray[i]);
            e.put("name", headers[i]);
            filesList.add(e);
        }

        // 返回字段列表查询成功的结果
        return ResultWrapper.wrapResult(true, filesList, null, SAVE_SUCCESS);
    }


    /**
     * 根据对象ID获取详细信息。
     *
     * @param label 对象的标签，用于查询特定类型的对象。
     * @param objId 对象的唯一标识符。
     * @return 返回一个包含对象详细信息的Map，以键值对的形式呈现。
     * @throws DefineException 如果对象ID为空、元数据缺失或查询出现问题时抛出。
     */
    @RequestMapping(value = "/detail/{objId}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Map<String, Object> detail(@PathVariable("po") String label, @PathVariable("objId") String objId) throws DefineException {
         LabelValidator.validateLabel(label);
        // 检查对象ID是否为空
        if (objId == null || "null".equals(objId)) {
            throw new DefineException(label + " id 为空！");
        }
        // 根据标签和元数据类型获取元数据信息
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        // 检查元数据是否缺失
        if (md == null || md.isEmpty()) {
            throw new DefineException(label + "元数据缺失！");
        }

        // 根据对象ID获取具体的属性信息
        Map<String, Object> attMapBy = neo4jService.getOneMapById(Long.valueOf(objId), label);
        // 格式化日期属性
        formatDate(attMapBy);
        return attMapBy;
    }

    /**
     * 根据对象ID获取详细信息。
     *
     * @param label 对象的标签，用于查询特定类型的对象。
     * @return 返回一个包含对象详细信息的Map，以键值对的形式呈现。
     * @throws DefineException 如果对象ID为空、元数据缺失或查询出现问题时抛出。
     */
    @RequestMapping(value = "/detail", method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public WrappedResult detailData(@PathVariable("po") String label, @RequestBody JSONObject vo) throws DefineException {
        LabelValidator.validateLabel(label);
        String objId = string(vo, "objId");
        // 检查对象ID是否为空
        if (objId == null || "null".equals(objId)) {
            throw new DefineException(label + " id 为空！");
        }
        // 根据标签和元数据类型获取元数据信息
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        // 检查元数据是否缺失
        if (md == null || md.isEmpty()) {
            throw new DefineException(label + "元数据缺失！");
        }

        // 根据对象ID获取具体的属性信息
        Map<String, Object> attMapBy = neo4jService.getOneMapById(Long.valueOf(objId), label);
        // 格式化日期属性
        formatDate(attMapBy);
        return ResultWrapper.wrapResult(true, attMapBy, null, QUERY_SUCCESS);
    }

    /**
     * 根据对象ID和标签获取对象的详细信息。
     *
     * @param label 对象的标签，用于识别不同的对象类型。
     * @param objId 对象的唯一标识符。
     * @return 返回一个包含对象信息的Map，格式和内容由具体实现决定。
     * @throws DefineException 如果对象ID为空、元数据缺失或查询出现问题时抛出。
     */
    @RequestMapping(value = "/get/{objId}", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Map<String, Object> get(@PathVariable("po") String label, @PathVariable("objId") String objId) throws DefineException {
         LabelValidator.validateLabel(label);
        // 检查对象ID是否为空
        if (objId == null || "null".equals(objId)) {
            throw new DefineException(label + " id 为空！");
        }
        // 通过标签和元数据标识获取元数据信息
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        // 检查元数据是否缺失
        if (md == null || md.isEmpty()) {
            throw new DefineException(label + "元数据缺失！");
        }

        // 根据对象ID获取属性信息
        Map<String, Object> attMapBy = neo4jService.getOneMapById(Long.valueOf(objId), label);
        // 格式化日期等信息
        formatDate(attMapBy);
        // 获取简要展示信息
        return attMapBy;
    }

    @RequestMapping(value = "/getShort/{objId}", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Map<String, Object> getShort(@PathVariable("po") String label, @PathVariable("objId") String objId) throws DefineException {
        LabelValidator.validateLabel(label);
        // 检查对象ID是否为空
        if (objId == null || "null".equals(objId)) {
            throw new DefineException(label + " id 为空！");
        }
        // 通过标签和元数据标识获取元数据信息
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        // 检查元数据是否缺失
        if (md == null || md.isEmpty()) {
            throw new DefineException(label + "元数据缺失！");
        }

        // 根据对象ID获取属性信息
        Map<String, Object> attMapBy = neo4jService.getOneMapById(Long.valueOf(objId), label);
        // 格式化日期等信息
        formatDate(attMapBy);
        // 获取简要展示信息
        String shortShow = string(md, "shortShow");
        if (shortShow == null) {
            return attMapBy;
        }

        // 根据简要展示信息，复制并返回关键信息
        Map<String, Object> copyWithKeys = copyWithKeys(attMapBy, shortShow);
        return copyWithKeys;
    }

/**
 * 根据提供的键从数据库中获取值。
 *
 * @param label 实体的标签，对应数据库中的节点类型。
 * @param key 需要获取数据的键名。
 * @param vo 包含请求数据的JSON对象，可用于进一步筛选数据。
 * @return 一个包含结果数据的Map对象，如果请求的键不存在，则返回一个空的Map。
 * @throws DefineException 如果提供的标签或键为空，或者请求的数据不存在，抛出此异常。
 */
@RequestMapping(value = "/getValue/{key}", method = {RequestMethod.GET, RequestMethod.POST}, produces = "application/json;charset=UTF-8")
@ResponseBody
public Map<String, Object> getValue(@PathVariable("po") String label, @PathVariable("key") String key, @RequestBody JSONObject vo) throws DefineException {
    // 校验vo中的label属性是否符合Cypher节点的label命名规范
    if (!label.matches(CruderConstant.REGEX_NODE_LABEL)) {
        throw new DefineException("节点label不符合命名规范！");
    }
    // 检查key是否为空
    if (key == null || "null".equals(key)) {
        throw new DefineException(label + " id 为空！");
    }
    // 从数据库获取指定标签的元数据
    Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
    // 检查元数据是否存在
    if (md == null || md.isEmpty()) {
        throw new DefineException(label + "元数据缺失！");
    }
    // 获取元数据中的列集合
    Set<String> columns = columnSet(md);
    // 检查请求的键是否包含在列集合中
    if (!columns.contains(key)) {
        Map<String, Object> newMap = newMap();
        return newMap;
    }

    // 根据请求的键和值获取对应的数据
    String retCol = string(vo, "returnValue");
    Map<String, Object> datax = neo4jService.getAttMapBy(key, string(vo, key), label);
    Map<String, Object> newMap = newMap();
    // 将获取到的数据放入返回的Map中
    newMap.put(DATA, string(datax, retCol));
    return newMap;
}


    @RequestMapping(value = "/getByCode/{code}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Map<String, Object> getByCode(@PathVariable("po") String label, @PathVariable("code") String code) throws DefineException {
         LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        Map<String, Object> attMapBy = neo4jService.getAttMapBy(NODE_CODE, code, label);
        return attMapBy;
    }

    @ControllerLog(description = "获取元数据的某一类关系")
    @RequestMapping(value = "/getRelation/{relation}", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public List<Map<String, Object>> getRelation(@PathVariable("po") String label, @PathVariable("relation") String relation, @RequestBody JSONObject vo) throws DefineException {
         LabelValidator.validateLabel(label);
        PageObject page = crudUtil.validatePage(vo);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        List<Map<String, Object>> oneOutRelation = neo4jService.getOutRelationList(vo, label, relation);
        return oneOutRelation;
    }

    @ControllerLog(description = "获取元数据的TabList")
    @RequestMapping(value = "/tabList", method = {RequestMethod.GET, RequestMethod.POST})
    public WrappedResult tabList(Model model, @PathVariable("po") String label, @RequestBody JSONObject vo, HttpServletRequest request) throws Exception {

        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);

        if (md == null || md.isEmpty()) {
            try {
                if (Long.valueOf(label) != null) {
                    md = neo4jService.getNodeMapById(Long.valueOf(label));
                } else {
                    throw new DefineException(label + "未定义！");
                }
            } catch (Exception e) {
                LoggerTool.error(logger,e.getMessage(), e);
                //查询数据库
                md = neo4jService.getAttMapBy("TableName", label, "DBTable");
                if (md == null) {
                    throw new DefineException(label + "未定义！");
                }

            }

            if(label(md)!=null){
                label=label(md);
            }else{
                label=String.valueOf(listObject(md,"Mark-label").get(0));
            }
        }
        if("Vo".equals(label)){
            return ResultWrapper.wrapResult(true, null, null, QUERY_SUCCESS);
        }
        LabelValidator.validateLabel(label);

        Map<String, Object> tabList = tabService.tabList(label, vo);
        // page.setTotal(crudUtil.total(query));
        return ResultWrapper.wrapResult(true, tabList, null, QUERY_SUCCESS);
    }

    @ControllerLog(description = "获取元数据的TabList")
    @RequestMapping(value = "/readTabList", method = {RequestMethod.GET, RequestMethod.POST})
    public WrappedResult readTabList(Model model, @PathVariable("po") String label, @RequestBody JSONObject vo, HttpServletRequest request) throws Exception {
         LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (md == null || md.isEmpty()) {
            try {
                if (Long.valueOf(label) != null) {
                    md = neo4jService.getNodeMapById(Long.valueOf(label));
                } else {
                    throw new DefineException(label + "未定义！");
                }
            } catch (Exception e) {
                LoggerTool.error(logger,e.getMessage(), e);
                throw new DefineException(label + "未定义！");
            }

        }

        Map<String, Object> tabList = tabService.readTabList(label, vo);
        // page.setTotal(crudUtil.total(query));
        return ResultWrapper.wrapResult(true, tabList, null, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/detailTabList", method = {RequestMethod.GET, RequestMethod.POST})
    public WrappedResult detailTabList(Model model, @PathVariable("po") String label, @RequestBody JSONObject vo, HttpServletRequest request) throws Exception {
         LabelValidator.validateLabel(label);
        if (META_DATA.equals(label)) {
            label = vo.getString(LABEL);
        }
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (md == null || md.isEmpty()) {
            throw new DefineException(label + "未定义！");
        }

        Map<String, Object> tabList = tabService.detailTabList(label, vo);
        // page.setTotal(crudUtil.total(query));
        return ResultWrapper.wrapResult(true, tabList, null, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/instanceData", method = {RequestMethod.GET, RequestMethod.POST})
    public WrappedResult instatnceData(Model model, @PathVariable("po") String label, @RequestBody JSONObject vo, HttpServletRequest request) throws Exception {
         LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        if (md == null || md.isEmpty()) {
            throw new DefineException(label + "未定义！");
        }

        Map<String, Object> tabList = tabService.instanceDataTabList(label, vo);
        return ResultWrapper.wrapResult(true, tabList, null, QUERY_SUCCESS);
    }

    @RequestMapping("/update")
    public WrappedResult update(@RequestBody JSONObject vo, @PathVariable("po") String label, HttpServletRequest request) throws DefineException {
         LabelValidator.validateLabel(label);
        try {
            adminService.checkAuth(label, "SaveOperate", "保存更新操作");
        } catch (Exception e) {
            return ResultWrapper.wrapResult(false, null, null, e.getMessage());
        }
        if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
            return ResultWrapper.wrapResult(true, null, null, UPDATE_FAILED);
        }
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        String crudKey = String.valueOf(md.get(CRUD_KEY));
        neo4jService.update(vo, label, crudKey.split(","));
        return ResultWrapper.wrapResult(true, null, null, UPDATE_SUCCESS);
    }

    @ControllerLog(description = "导入CSV数据")
    @RequestMapping("/importCsv")
    public WrappedResult importCsv(@RequestBody JSONObject vo, @PathVariable("po") String label, HttpServletRequest request) throws DefineException {
         LabelValidator.validateLabel(label);
        if (vo.isEmpty() || !crudUtil.isColumnsNotEmpty(vo)) {
            return ResultWrapper.wrapResult(true, null, null, UPDATE_FAILED);
        }
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        String crudKey = String.valueOf(md.get(CRUD_KEY));
        neo4jService.update(vo, label, crudKey.split(","));
        return ResultWrapper.wrapResult(true, null, null, UPDATE_SUCCESS);
    }

    @RequestMapping(value = "/exportData", method = RequestMethod.GET)
    public void exportData(@PathVariable("po") String label, HttpServletRequest request, HttpServletResponse response) throws DefineException {
         LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        String[] headers = headers(md);
        String[] columns = columns(md);
        // 获取自定义关联字段，并默认根据name来定位ID。
        JSONObject vo = new JSONObject();
        vo.put("poId", label);
        List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");
        Map<String, Map<String, Object>> customFieldMap = new HashMap<>(fieldInfoList.size());
        for (Map<String, Object> fi : fieldInfoList) {
            Object object = fi.get("field");
            object = object == null ? fi.get("id") : object;
            customFieldMap.put(String.valueOf(object), fi);
        }
        List<Map<String, Object>> nameLize = neo4jService.listNameLize(customFieldMap, label);
        List<Object[]> list = listMap2ListObjectArray(columns, nameLize);
        String metaName = name(md);
        String fileName = label + DateTool.now() + FileDomain.FILE_TYPE_XLS;
        ExportExcel e2 = new ExportExcel(fileName, metaName + "数据", headers, list, response);
        e2.export();
    }

    @ControllerLog(description = "导入数据")
    @RequestMapping("/importData")
    public WrappedResult importData(@RequestParam("file") MultipartFile file, @PathVariable("po") String label, HttpServletRequest request) throws DefineException {
        LabelValidator.validateLabel(label);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, label, META_DATA);
        Set<String> columnSet = columnSet(md);
        // 获取自定义关联字段，并默认根据name来定位ID。
        JSONObject vo = new JSONObject();
        vo.put("poId", label);
        List<Map<String, Object>> fieldInfoList = objectService.getBy(vo, "Field");
        Map<String, Map<String, Object>> customFieldMap = new HashMap<>(fieldInfoList.size());
        for (Map<String, Object> fi : fieldInfoList) {
            Object fieldValue = fi.get("field");
            fieldValue = fieldValue == null ? fi.get("id") : fieldValue;
            customFieldMap.put(String.valueOf(fieldValue), fi);
        }
        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            LoggerTool.info(logger,"fileName:{},label:{}", fileName, label);
            List<List<String>> readExcel = ImportlUtils.readData(fileName, inputStream);
            String[] columns2 = columns(md);
            LoggerTool.info(logger,"readExcel:{},label:{},size:{}", String.join(",", columns2), label, readExcel.size());

            List<Map<String, Object>> tempData = new ArrayList<>();
            boolean hasParent = columnSet.contains(PARENT_ID);
            Map<String, Long> parentIdMap = new HashMap<>();
            int rowi = 0;
            for (List<String> dataRowi : readExcel) {
                List<String> poKeys = new ArrayList<>();
                Map<String, Object> mi = new HashMap<>();
                for (int i = 0; i < columns2.length - 1; i++) {
                    String metaColumni = columns2[i + 1];
                    String colValuei = dataRowi.get(i + 1);
                    if(colValuei.startsWith("\"")&&colValuei.endsWith("\"")){
                        colValuei=colValuei.substring(1,colValuei.length()-1);
                    }

                    String trimColi = metaColumni.trim();
                    if (trimColi.equals("") || !columnSet.contains(trimColi)) {
                        continue;
                    }

                    if (!trimColi.equals("")) {
                        if (customFieldMap.containsKey(trimColi)) {
                            poKeys.add(trimColi);
                            String dataObjectId = neo4jService.col2ObjectId(customFieldMap, trimColi, colValuei);
                            if (dataObjectId != null && !"".equals(dataObjectId.trim())) {
                                mi.put(metaColumni, dataObjectId);
                            }
                        } else {
                            mi.put(metaColumni, colValuei);
                        }
                    }
                }
                rowi++;
                // LoggerTool.info(logger,"mi.isEmpty():{}",mi.isEmpty());
                if (!mi.isEmpty()) {
                    // LoggerTool.info(logger,"fileName:{},label:{}",fileName,label);
                    neo4jService.saveByGetExceptPoKeys(label, poKeys, mi);
                    // neo4jService.saveByBody(mi, label, false);
                    if (hasParent) {
                        String stringParentId = stringParentId(mi);
                        if (stringParentId != null) {
                            parentIdMap.put(stringParentId, id(mi));
                            tempData.add(mi);
                        }
                    }
                }
            }
            if (hasParent) {
                for (Map<String, Object> ti : tempData) {
                    String stringParentId = stringParentId(ti);
                    ti.put(PARENT_ID, parentIdMap.get(stringParentId));
                }
                neo4jService.updateBodyList(tempData, label);
            }
        } catch (IOException e) {
        }
        return ResultWrapper.wrapResult(true, null, null, UPDATE_SUCCESS);
    }

    @RequestMapping(value = "/treeData", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public WrappedResult treeData(Model model, @PathVariable("po") String poLabel, HttpServletRequest request) throws Exception {
        LabelValidator.validateLabel(poLabel);
        Map<String, Object> endPo = neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
        if (endPo == null || endPo.isEmpty()) {
            throw new DefineException(poLabel + "未定义！");
        }
        Map<String, Object> tree = neo4jService.getWholeTree(poLabel);
        if(tree==null){
            return ResultWrapper.wrapResult(true, null, null, UPDATE_SUCCESS);
        }
        JSONArray zNodesList = new JSONArray();
        zNodesList.add(tree);
        return ResultWrapper.wrapResult(true, zNodesList, null, QUERY_SUCCESS);
    }

    @RequestMapping(value = "/customTreeData", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public WrappedResult customTreeData(Model model, @PathVariable("po") String poLabel, HttpServletRequest request) throws Exception {
        LabelValidator.validateLabel(poLabel);
        Map<String, Object> md = neo4jService.getAttMapBy(LABEL, poLabel, META_DATA);
        if (md == null || md.isEmpty()) {
            throw new DefineException(poLabel + "未定义！");
        }
        JSONArray zNodesList = new JSONArray();
        Map<String, Object> tree = null;
//	
        Map<String, Object> one2 = neo4jService.getOne("Match(t:TreeDefine) where t.mdLabel='" + label(md) + "' return t.parentIdField AS parentIdField,t.code AS code");
        String one = string(one2, "parentIdField");
        if (one != null) {
            tree = neo4jService.getTreeByDefine(poLabel, one2, columns(md));
            if (tree == null) {
                return ResultWrapper.wrapResult(true, zNodesList, null, UPDATE_SUCCESS);
            }
        } else {
            tree = neo4jService.getWholeTreeWithColumn(poLabel, columns(md));
            if (tree == null) {
                return ResultWrapper.wrapResult(true, zNodesList, null, UPDATE_SUCCESS);
            }
        }

        if (tree.size() < 2) {
            List<Map<String, Object>> listMapObject = listMapObject(tree, REL_TYPE_CHILDREN);
            for (Map<String, Object> ti : listMapObject) {
                if (string(ti, one) == null) {
                    List<Map<String, Object>> listMapObject2 = listMapObject(ti, REL_TYPE_CHILDREN);
                    if (listMapObject2 != null && !listMapObject2.isEmpty()) {
                        zNodesList.addAll(listMapObject2);
                    }
                } else {
                    zNodesList.add(ti);
                }
            }
//	    if(!listMapObject.isEmpty()) {
//		zNodesList.addAll(listMapObject);
//	    }
            return ResultWrapper.wrapResult(true, zNodesList, null, UPDATE_SUCCESS);
        }

        zNodesList.add(tree);
        return ResultWrapper.wrapResult(true, zNodesList, null, UPDATE_SUCCESS);
    }

}
