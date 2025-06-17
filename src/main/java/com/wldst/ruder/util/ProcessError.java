package com.wldst.ruder.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessError implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(ProcessError.class);
    private Process p = null;
    private BlockingQueue<String> queue;
    public ProcessError(Process process, BlockingQueue<String> qu) {
	p = process;
	queue=qu;
    }

    @Override
    public void run() {
	try {
	    InputStream in = p.getErrorStream();
	    BufferedReader bfr = new BufferedReader(new InputStreamReader(in));
	    
	    String rd;
	    while((rd = bfr.readLine()) != null) {
		    LoggerTool.info(logger,queue.size()+":"+rd);
		   // 输出子进程返回信息(即子进程中的System.out.println()内容)
		    queue.put(rd);
		    Thread.currentThread().sleep(120);
	    }
	    // 注意这个地方，如果关闭流则子进程的返回信息无法获取，如果不关闭只有当子进程返回字节为8192时才返回，为什么是8192下面说明.
	    // bfr.close();
	    // in.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
