package com.wldst.ruder.crud.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.wldst.ruder.LemodoApplication;
import com.wldst.ruder.annotation.ControllerLog;
import com.wldst.ruder.annotation.MyAnnotation;
import com.wldst.ruder.api.Result;
import com.wldst.ruder.constant.CruderConstant;
import com.wldst.ruder.constant.RuleConstants;
import com.wldst.ruder.crud.service.*;
import com.wldst.ruder.domain.FileDomain;
import com.wldst.ruder.domain.RuleDomain;
import com.wldst.ruder.exception.AuthException;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.BeanShellService;
import com.wldst.ruder.module.bs.ShellOperator;
import com.wldst.ruder.module.database.DbInfoService;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.module.state.service.StateService;
import com.wldst.ruder.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;

import static com.wldst.ruder.domain.DataBaseDomain.LABLE_TABLE;
import static com.wldst.ruder.domain.DataBaseDomain.TABEL_NAME;

@RestController
@ResponseBody
@RequestMapping("${server.context}/retrieve")
@CrossOrigin
public class QueryController extends RuleConstants {
    private static Logger logger = LoggerFactory.getLogger(QueryController.class);
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private CrudNeo4jService neo4jService;



    @ControllerLog(description = "查询数据")
    @RequestMapping(value = "/query", method = {RequestMethod.POST, RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public WrappedResult query(HttpServletRequest request,@RequestBody JSONObject vo) throws DefineException {

        Object innerHostCall=request.getSession().getAttribute("innerHostCall");
        if(null==innerHostCall||!(Boolean) innerHostCall){
            return ResultWrapper.failed("非法调用");
        }

//        对数据进行加密
        // 校验并获取分页信息
        PageObject page = crudUtil.validatePage(vo);
        // 记录查询开始的信息
        LoggerTool.info(logger,"search ================================" + vo.toJSONString());

        // 移除分页信息，避免影响后续逻辑
        vo.remove("page");

        // 构建并执行查询语句
        String query = string(vo, "cypher");
        Map<String, Object> param=mapObject(vo, "param");

        // 执行查询并获取结果数据
        List<Map<String, Object>> dataList = neo4jService.queryByCypher(query,param);
        // 如果查询结果不为空，更新分页信息中的总记录数
        if (dataList != null) {
            if (!dataList.isEmpty() && !vo.containsKey(ID)) {

                page.setTotal(crudUtil.total(query));
            }
        }
        // 返回查询成功的结果，包含数据、分页信息和成功状态码
        return ResultWrapper.wrapResult(true, dataList, page, QUERY_SUCCESS);
    }


}
