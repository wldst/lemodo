package com.wldst.ruder.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolManager {

    // 使用AtomicInteger来追踪提交的任务数
    private static final AtomicInteger taskCount = new AtomicInteger(0);

    @SuppressWarnings("preview")
    public static ExecutorService getExecutorService() {
        // 创建一个固定大小的虚拟线程线程池，以限制同时执行的任务数量
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(),
                r -> new Thread(r) {
                    @Override
                    public void run() {
                        // 虚拟线程的实现
                        super.run();
                        taskCount.incrementAndGet();
                    }
                });

        // 注册一个JVM关闭钩子，以确保线程池被适当关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdown(); // 尝试优雅关闭线程池

            try {
                // 等待线程池关闭，最多等待10秒
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    executorService.shutdownNow(); // 如果无法优雅关闭，则强制关闭
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 恢复中断状态
                executorService.shutdownNow(); // 强制关闭
            }
        }));

        return executorService;
    }

    // 示例：提交任务并管理执行结果
    public static void executeTask(Runnable task) {
        ExecutorService executorService = getExecutorService();
        // 假设我们希望确保任务执行异常被捕捉并处理
        executorService.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                // 异常处理逻辑
                e.printStackTrace();
            } finally {
                // 任务完成后的清理逻辑（如果有的话）
            }
        });
    }

    // 主函数，仅作示例用途
    public static void main(String[] args) {
        // 提交一些示例任务
        for (int i = 0; i < 5; i++) {
            executeTask(() -> {
                System.out.println("Task executing...");
                // 模拟任务执行
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Task completed.");
            });
        }

        // 等待所有任务执行完成
        while (taskCount.get() != 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("All tasks completed.");
    }
}
