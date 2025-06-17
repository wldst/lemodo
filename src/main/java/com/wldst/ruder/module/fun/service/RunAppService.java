package com.wldst.ruder.module.fun.service;

import java.util.List;
import java.util.Map;

/**
 * 常用规则处理。后端校验规则
 * 后端查询规则进行处理。
 * @author wldst
 *
 */
public interface RunAppService {
    public void runPrograms(List<Map<String, Object>> openTasks);
    public void runApp(List<Map<String, Object>> openTasks);
    public void run(Map<String, Object> oi);
    public void runApp(String ids);
    public void explore(String ids);
    public void explore(Map<String, Object> oi);
    public void explore(List<Map<String, Object>> openTasks);
}
