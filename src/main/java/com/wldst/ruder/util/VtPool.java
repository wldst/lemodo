package com.wldst.ruder.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VtPool {
    final static Logger logger = LoggerFactory.getLogger(VtPool.class);
    private static ExecutorService exec = getExecutorService();
    public static ExecutorService getExecutorService() {
		return  Executors.newFixedThreadPool(10);
//	 return Executors.newVirtualThreadPerTaskExecutor();
   }
    public static <T> T vt(Callable<T> callabel) {
	exec.submit(callabel);
	Future<T> submit = exec.submit(callabel);
	try {
	    return submit.get();
	} catch (InterruptedException | ExecutionException e) {
	    LoggerTool.error(logger,e.getMessage(), e);
	}
	return null;
    }
}
