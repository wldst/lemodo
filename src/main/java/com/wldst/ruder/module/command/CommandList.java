package com.wldst.ruder.module.command;

import java.util.Map;

public interface CommandList extends CRUDCommand {
    /**
     * 宏命令聚集的管理方法
     * 可以添加一个成员命令
     */
    public void add(CRUDCommand cmd);
    /**
     * 宏命令聚集的管理方法
     * 可以删除一个成员命令
     */
    public void remove(CRUDCommand cmd);
    public boolean isEmpty();
    
    public Map<String,Object> getRetData();
}
