package com.wldst.ruder.module.fix;

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
import com.wldst.ruder.module.fix.service.FixService;
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

/**
 * 修复控制器
 * 该类负责处理与数据库修复相关的 HTTP 请求
 */
@RestController
@ResponseBody
@RequestMapping("${server.context}/fix/")
@CrossOrigin
public class FixController extends RuleConstants {
    private static Logger logger = LoggerFactory.getLogger(FixController.class);
    @Autowired
    private CrudUtil crudUtil;
    @Autowired
    private CrudNeo4jService neo4jService;

    @Autowired
    private FixService fixService;

    /**
     * 修复日期时间
     *
     * @return 操作结果
     * @throws DefineException 如果修复过程中发生异常
     */
    @RequestMapping(value = "/dateTime", method = {RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public Result dateTime() throws DefineException {
        fixService.fixDateTime();
        return Result.success();
    }

    /**
     * 修复结构
     *
     * @param vo 请求体中的 JSON 对象
     * @param request HTTP 请求对象
     * @return 操作结果
     * @throws DefineException 如果修复过程中发生异常
     */
    @RequestMapping(value = "/struct", method = {RequestMethod.GET}, produces = "application/json;charset=UTF-8")
    public Result struct(@RequestBody JSONObject vo, HttpServletRequest request) throws DefineException {
//        LabelValidator.validateLabel(label(vo));
        fixService.fixStruct(vo);
        return Result.success();
    }
    //刷新缓存

}
