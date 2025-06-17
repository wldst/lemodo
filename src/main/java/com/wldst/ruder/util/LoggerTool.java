package com.wldst.ruder.util;

import org.slf4j.Logger;

public class LoggerTool {
    private static boolean openLog=false;

    public static boolean isOpenLog() {
        return openLog;
    }

    public static void setOpenLog(Boolean openLog) {

        if(openLog!=null&&openLog){
            LoggerTool.openLog=true;
        }else{
            LoggerTool.openLog=false;
        }
    }
    public static void setOpenLog(String openLog) {
        if(openLog!=null&&openLog.length()>0&&openLog.trim().length()>0){
            String low = openLog.toLowerCase();
            if(low.equals("true")||low.equals("1")||low.equals("on")||low.equals("open")||low.equals("yes")||low.equals("y")||low.equals("打开")){
                setOpenLog(true);
            }else{
                setOpenLog(false);
            }
        }else{
            setOpenLog(false);
        }

    }

    public static void log(Logger lo ,String message){
        if(openLog){
            lo.info(message);
        }
    }
    public static void debug(Logger lo ,String message){
        if(openLog){
            lo.debug(message);
        }
    }
    public static void debug(Logger lo ,String message,Object... param){
        if(openLog){
            lo.debug(message,param);
        }
    }
    public static void info(Logger lo ,String message){
        if(openLog){
            lo.info(message);
        }
    }

    public static void info(Logger lo ,String message,Object... param){
        if(openLog){
            lo.info(message,param);
        }
    }
    public static void error(Logger lo ,String message,Exception e){
        if(openLog){
            lo.error(message,e);
        }
    }
    public static void error(Logger lo ,String message,Object... errorParam){
        if(openLog){
            lo.error(message,errorParam);
        }
    }
    public static void error(Logger lo ,String message){
        if(openLog){
            lo.error(message);
        }
    }
}
