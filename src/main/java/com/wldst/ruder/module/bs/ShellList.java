package com.wldst.ruder.module.bs;

import java.util.Map;

public interface ShellList extends Shell {
    /**
     * 宏命令聚集的管理方法
     * 可以添加一个成员命令
     */
    public void add(Shell cmd);
    /**
     * 宏命令聚集的管理方法
     * 可以删除一个成员命令
     */
    public void remove(Shell cmd);
    public boolean isEmpty();
    
    public Map<String,Object> getRetData();
}
