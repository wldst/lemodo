package com.wldst.ruder.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.module.auth.service.UserAdminService;
import com.wldst.ruder.module.bs.BeanShellService;
import com.wldst.ruder.module.bs.DomainLogicOperator;
import com.wldst.ruder.module.bs.DomainOperator;
import com.wldst.ruder.module.bs.ShellOperator;
import com.wldst.ruder.module.fun.Neo4jOptByUser;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.MapTool;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * 系统设置相关的常量
 * 
 * @author wldst
 *
 */
@Service
public class BeanShellDomain extends MapTool {
    public static final String BS_SCRIPT = "Content";
    protected static final String OPERATOR = "type";
    protected static final String BIZ_VALUE = "bizValue";
    protected static final String TRAN_KEY = "tranKey";
    protected static final String PROP = "PROP";
    protected static final String CMD_CRUD = "CRUDShell";
    protected static final String CMD_MARCO = "MarcoShell";
    
    protected static final String OPERATE_LABEL ="OperateLabel";
    protected static final String OPERATE_META ="OperateMeta";
     protected static final String OPERATE_OBJECT ="OperateObject";
    protected static final String OPERATE_RELATION ="OperateRelation";

}
