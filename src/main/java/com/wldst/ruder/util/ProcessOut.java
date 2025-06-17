package com.wldst.ruder.util;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessOut {
    private static Logger logger = LoggerFactory.getLogger(ProcessOut.class);
    private Process p = null;
    private OutputStream ops =null;

    public ProcessOut(Process process) {
	p = process;
	ops = p.getOutputStream();
    }
    
    public void input(String text){
	try {
	    if(ops==null) {
		ops = p.getOutputStream();
	    }
	    LoggerTool.info(logger,"ops======"+ops+" inputText is  "+text);
	    if(ops==null) {
		LoggerTool.info(logger," ops is null"+ops);
		return ;
	    }
	    PrintWriter outputWriter = new PrintWriter(new OutputStreamWriter(ops), true);

            // 向Python程序发送数据
            outputWriter.println(text);
	    
	    // 注意这个地方如果关闭，则父进程只可以给子进程发送一次信息，如果这个地方开启close()则父进程给子进程不管发送大小多大的数据，子进程都可以返回
	    // 如果这个地方close()不开启，则父进程给子进程发送数据累加到8192子进程才返回。
	    // ops.close();
	} catch (Exception e) {
	    LoggerTool.error(logger,"Exception:porcessOut",e);
	}
    }

     
}
