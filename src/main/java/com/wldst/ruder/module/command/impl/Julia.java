package com.wldst.ruder.module.command.impl;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson2.JSON;
import com.wldst.ruder.module.command.Command;

public class Julia {
    public static void main(String[]args){
	Map<String,Object> data = new HashMap<>();
	data.put("test1", "testdddddd");
        //创建接收者对象
        AudioPlayer audioPlayer = new AudioPlayer();
        //创建命令对象
        Command playCommand = new PlayCommand(audioPlayer,data);
        Command rewindCommand = new RewindCommand(audioPlayer,data);
        Command stopCommand = new StopCommand(audioPlayer,data);
        //创建请求者对象
        Keypad keypad = new Keypad();
        keypad.setPlayCommand(playCommand);
        keypad.setRewindCommand(rewindCommand);
        keypad.setStopCommand(stopCommand);
        //测试
        keypad.play();
        keypad.rewind();
        keypad.stop();
        keypad.play();
        keypad.stop();
        
        
        MacroFunctionCommand marco = new MacroFunctionCommand();
        marco.add(playCommand);
        marco.add(rewindCommand);
        marco.add(stopCommand);
        marco.add(new AddCreateInfoCommand(audioPlayer,data));
        marco.add(new AddUpdateInfoCommand(audioPlayer,data));
        marco.execute();
        
        System.out.println(JSON.toJSONString(data));
        
    }
}