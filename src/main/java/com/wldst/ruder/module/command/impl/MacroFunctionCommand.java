package com.wldst.ruder.module.command.impl;

import java.util.ArrayList;
import java.util.List;

import com.wldst.ruder.module.command.Command;
import com.wldst.ruder.module.command.MacroCommand;

public class MacroFunctionCommand implements MacroCommand {
    
    private List<Command> commandList = new ArrayList<Command>();
    /**
     * 宏命令聚集管理方法
     */
    @Override
    public void add(Command cmd) {
        commandList.add(cmd);
    }
    /**
     * 宏命令聚集管理方法
     */
    @Override
    public void remove(Command cmd) {
        commandList.remove(cmd);
    }
    /**
     * 执行方法
     */
    @Override
    public void execute() {
        for(Command cmd : commandList){
            cmd.execute();
        }
    }

}
