package com.wldst.ruder.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ShellRun{
    private static String outDir="D:\\liuqiang\\work\\wangge\\部署\\mserver\\mserver";
    public static void main(String[] args) {
        System.out.println("Hello World!");
//        msAcloud();

        msRin();
    }

    private static void msAcloud(){
        //  读取file/template/*.sh文件。根据变量表，更新变量，并生产新的文件
        String pathname="D:\\liuqiang\\work\\wangge\\部署\\mserver\\mserver";
        startupStopShell(pathname, "/u01/ms/mserver/");
    }

    public static void startupStopShell(String pathname, String serverPath){
        String outPutDir = pathname.trim()+"\\shout\\";
        startStop(pathname, serverPath.trim(),outPutDir);
    }

    private static void msRin(){
        //  读取file/template/*.sh文件。根据变量表，更新变量，并生产新的文件
        String pathname="D:\\liuqiang\\work\\wangge\\部署\\rin\\rin";
        startupStopShell(pathname, "/u01/rin/rin2");
    }

    private static void startStop(String pathname, String serverPath, String outPutDir){
        ShellRun.outDir=outPutDir;
        File dir = new File(pathname);
        generateOnStopSh(dir, serverPath);
        generateAllStartStopSh(dir, serverPath);
    }

    private static void generateAllStartStopSh(File dir, String serverPath){
        String on = "file/template/startup.sh";
        String stop = "file/template/stopAll.sh";
        String stopItem="""
                     
                    "$ROOT"/#{name}/stop.sh >/dev/null  2>&1
                    echo -e "\\033[0;31m  #{name}服务已关闭 \\033[0m"                                
                    """;

        String startItem="""
                    
                    "$ROOT"/#{name}/on.sh >/dev/null  2>&1
                    echo -e "\\033[0;31m  #{name}服务已启动 \\033[0m"                                
                    """;
        String stopContent =FileOpt.readFileContent(stop);
        String onContent =FileOpt.readFileContent(on);

        StringBuilder startBuffer = new StringBuilder();
        StringBuilder stopBuffer = new StringBuilder();

        for(File fi: dir.listFiles()){
            if(fi.isDirectory()){
                Map<String, String> varMap = new HashMap<>();
                String dirName=fi.getName();
                varMap.put("name", dirName);

                String startItemi=replaceVar(varMap, startItem);
                startBuffer.append(startItemi);

                String stopItemi=replaceVar(varMap, stopItem);
                stopBuffer.append(stopItemi);
            }
        }

        onContent= onContent.replace("#{dir}", serverPath);
        stopContent= stopContent.replace("#{dir}", serverPath);

        onContent= onContent.replace("${onSh}", startBuffer.toString());
        stopContent= stopContent.replace("${stopSh}", stopBuffer.toString());

        //stop
        String stopSh = outDir+"stopAll.sh";
        FileOpt.writeFile(stopContent,stopSh);

        String startup = outDir+"startup.sh";
        FileOpt.writeFile(onContent,startup);
    }

    private static void generateOnStopSh(File dir, String serverPath){
        for(File fi: dir.listFiles()){
            String on = "file/template/on.sh";
            String stop = "file/template/stop.sh";
            String restart = "file/template/restart.sh";
            if(fi.isDirectory()){
                Map<String, String> varMap = new HashMap<>();
                for(File fj:fi.listFiles()){
                    if(fj.getName().endsWith(".jar")){
                        varMap.put("jar", fj.getName());
                    }
                }
                String dirName=fi.getName();
                varMap.put("dir", serverPath+"/"+dirName);
                varMap.put("name", dirName);
                String newFileName = outDir+File.separator+dirName+"/on.sh";
                generateNewFile(on, varMap, newFileName);
                //stop
                String stopSh = outDir+File.separator+dirName+"/stop.sh";
                generateNewFile(stop, varMap, stopSh);

                //stop
                String restartSh = outDir+File.separator+dirName+"/restart.sh";
                generateNewFile(restart, varMap, restartSh);
            }
        }
    }


    private static void generateNewFile(String stop, Map<String, String> varMap, String stopSh){
        String stopContent =FileOpt.readFileContent(stop);

        stopContent=replaceVar(varMap, stopContent);
        FileOpt.writeFile(stopContent,stopSh);
    }

    private static String replaceVar(Map<String, String> varMap, String stopContent){
        for(Map.Entry<String, String> ei: varMap.entrySet()){
            stopContent= stopContent.replaceAll("\\#\\{"+ei.getKey()+"\\}", ei.getValue());
        }
        return stopContent;
    }
}
