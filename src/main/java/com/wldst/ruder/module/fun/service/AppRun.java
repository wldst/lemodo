package com.wldst.ruder.module.fun.service;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.wldst.ruder.util.LoggerTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wldst.ruder.util.ProcessError;
import com.wldst.ruder.util.ProcessIn;


public class AppRun {
    private static Logger logger = LoggerFactory.getLogger(AppRun.class);
    private static Map<String, Process> processMap = new HashMap<>();
    private Map<String,Object> runData=new HashMap<>();
    // 创建一个阻塞队列，用于存储消息  
    private static BlockingQueue<String> queue = new LinkedBlockingQueue<>();  
    public static void main(String[] args) {
//	runEclipse();
//	runWps();
//	runNotepad();
//	runNotepadpp();
	exploreFile("D:\\liuqiang\\文档\\mind");
//	try {
//	    Runtime.getRuntime().exec();
//	} catch (IOException e) {
//	    // TODO Auto-generated catch block
//	    e.printStackTrace();
//	}  
    }
    public static void runNotepadpp() {
   	String pythonFile = "D:\\program files\\Notepad++\\notepad++.exe ";
   	List<String> cmdArgs = new ArrayList<>();
   	cmdArgs.add("D:\\liuqiang\\work\\pmis\\桌面\\常用URL地址.txt");
   	
   	
   	AppRun jrp = new AppRun();
   	jrp.runCmd(pythonFile,cmdArgs);
       }
    /**
     * 进入某个场景下，自动打开目录，比如打开目录，class目录。
     * @param folderPath
     */
   public static void exploreFile(String folderPath) {
       try {
           // 指定要打开的文件夹路径
           
           File folder = new File(folderPath);
           // 检查文件夹是否存在
           if (folder.exists()) {
               // 使用默认程序打开文件夹
               Desktop.getDesktop().open(folder.getAbsoluteFile());
           } else {
               System.out.println("文件夹不存在");
           }
       } catch (IOException e) {
           e.printStackTrace();
       }
   }
    
    public static void runNotepad() {
   	String pythonFile = "notepad.exe";
   	List<String> cmdArgs = new ArrayList<>();
   	cmdArgs.add("E:\\360MoveData\\Users\\wldst\\Desktop\\新建 文本文档.txt");
   	
   	
   	AppRun jrp = new AppRun();
   	jrp.runCmd(pythonFile,cmdArgs);
       }

    public static void runEclipse() {
	String pythonFile = "D:\\liuqiang\\env\\ide\\jee-2022-12\\eclipse\\eclipse.exe";
	List<String> cmdArgs = new ArrayList<>();
	cmdArgs.add("-nosplash");
	cmdArgs.add("-data");
	cmdArgs.add("D:\\liuqiang\\workspace\\lemodo");
	
	
	AppRun jrp = new AppRun();
	jrp.runCmd(pythonFile,cmdArgs);
    }
    public static void runWps() {
   	String pythonFile = "D:\\Program Files (x86)\\WPS Office\\ksolaunch.exe";
   	List<String> cmdArgs = new ArrayList<>();
   	AppRun jrp = new AppRun();
   	jrp.runCmd(pythonFile,cmdArgs);
    }

	public Map<String, Object> runCmd(String[] cmd, List<String> cmdArgs){
		if(cmd==null){
			return runData;
		}
		LoggerTool.info(logger, " runcmd is "+cmd+"============");
		try{
			ProcessBuilder processBuilder=new ProcessBuilder(cmd);
			if(cmdArgs!=null&&!cmdArgs.isEmpty()){
				// 添加参数
				processBuilder.command().addAll(cmdArgs);
			}

			Process process=processBuilder.start();


			LoggerTool.info(logger, " started process is "+cmd+"============");
			ProcessIn pi=new ProcessIn(process, queue);
			new Thread(pi).start();
			LoggerTool.info(logger, " started ProcessIn is "+cmd);
			runData.put("processIn", pi);

			ProcessError pe=new ProcessError(process, queue);
			new Thread(pe).start();
			LoggerTool.info(logger, " started ProcessError is "+cmd);

			runData.put("processError", pe);
			if(cmdArgs==null||cmdArgs.isEmpty()){
				processMap.put(String.join(",", cmd), process);
			}else{
				processMap.put(cmd+String.join(",", cmdArgs), process);
			}

		}catch(IOException e){
			LoggerTool.error(logger, "IOException:"+e.getMessage(), e);
		}
		return runData;
	}


	public Map<String, Object> runCmd(String cmd, List<String> cmdArgs){
		if(cmd==null){
			return runData;
		}
		LoggerTool.info(logger, " runcmd is "+cmd+"============");
		try{
			ProcessBuilder processBuilder=new ProcessBuilder(cmd);
			if(cmdArgs!=null&&!cmdArgs.isEmpty()){
				// 添加参数
				processBuilder.command().addAll(cmdArgs);
			}

			Process process=processBuilder.start();


			LoggerTool.info(logger, " started process is "+cmd+"============");
			ProcessIn pi=new ProcessIn(process, queue);
			new Thread(pi).start();
			LoggerTool.info(logger, " started ProcessIn is "+cmd);
			runData.put("processIn", pi);

			ProcessError pe=new ProcessError(process, queue);
			new Thread(pe).start();
			LoggerTool.info(logger, " started ProcessError is "+cmd);

			runData.put("processError", pe);
			if(cmdArgs==null||cmdArgs.isEmpty()){
				processMap.put(cmd, process);
			}else{
				processMap.put(cmd+String.join(",", cmdArgs), process);
			}

		}catch(IOException e){
			LoggerTool.error(logger, "IOException:"+e.getMessage(), e);
		}
		return runData;
	}

    public static BlockingQueue<String> getQueue() {
        return queue;
    }

    public static void setQueue(BlockingQueue<String> queuex) {
        queue = queuex;
    }
    
    public  static List<String> nowTask() {
	List<String> runs = new ArrayList<>(processMap.size());
	
	for(Entry<String,Process> pi:processMap.entrySet()) {
	    runs.add(pi.getKey()+pi.getValue().pid());
	}
	return runs;
    }
    
    public static void killAll() {
	for(Entry<String,Process> pi:processMap.entrySet()) {
	    pi.getValue().destroy();
	}
    }
    
    public  static void kill9All() {
	for(Entry<String,Process> pi:processMap.entrySet()) {
	    pi.getValue().destroyForcibly();
	}
    }

    
}
