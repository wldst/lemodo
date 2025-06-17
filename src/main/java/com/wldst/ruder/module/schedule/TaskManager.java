package com.wldst.ruder.module.schedule;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.wldst.ruder.util.DateTool;
import com.wldst.ruder.util.DateUtil;

public class TaskManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<String, Future<?>> tasks = new ConcurrentHashMap<>();
    private final Map<String, Long> taskCosts = new ConcurrentHashMap<>();
    private final Map<String, Long> taskStarts = new ConcurrentHashMap<>();

    public void addTask(String taskId, Callable<Void> task, long delay, TimeUnit unit) throws InterruptedException {
        Future<?> future = scheduler.schedule(task, delay, unit);
        tasks.put(taskId, future);
        taskStarts.put(taskId, DateTool.nowLong());
    }

    public void cancelTask(String taskId) throws InterruptedException {
        Future<?> future = tasks.get(taskId);
        if (future != null && !future.isDone()) {
            future.cancel(true);
        }
    }

    public long getTaskDuration(String taskId) throws InterruptedException, ExecutionException {
        Future<?> future = tasks.get(taskId);
        if (future != null && !future.isDone()) {
            return System.currentTimeMillis()-taskStarts.get(taskId);
        } else if (future != null && future.isDone()) {
            return taskCosts.get(taskId);
        } else {
            return Long.MAX_VALUE; // Unknown or cancelled task
        }
    }

    public void releaseResource(String resourceId) {
        // Release the resource associated with the given resource ID
    }
}

