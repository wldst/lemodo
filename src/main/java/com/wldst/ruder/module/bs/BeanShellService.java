package com.wldst.ruder.module.bs;

import java.util.Map;

import bsh.EvalError;
import bsh.Interpreter;

public interface BeanShellService {

    Object runBeanShell(String shellId) throws EvalError;

    Object runShell(String shellId, Map<String, Object> vo) throws EvalError;

    Object run(Map<String,Object> vo) throws EvalError;

    void init(Interpreter in) throws EvalError;

}
