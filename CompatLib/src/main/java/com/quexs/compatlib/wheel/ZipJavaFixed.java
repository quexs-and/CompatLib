package com.quexs.compatlib.wheel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ZipJavaFixed {
//    private final ExecutorService executorService;
    public ZipJavaFixed(){
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1,
//                0L, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<Runnable>());
//        threadPoolExecutor.allowCoreThreadTimeOut(true);
//        executorService = new FinalizableDelegatedExecutorService(threadPoolExecutor);

//        int nThreads = Runtime.getRuntime().availableProcessors();
//        fixedThreadPool = new ThreadPoolExecutor(2 * nThreads, 2 * nThreads,
//                60L, TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<Runnable>());
//        //设置允许超时关闭
//        fixedThreadPool.allowCoreThreadTimeOut(true);
//        Executors.newSingleThreadExecutor()
    }

    public void release(){
//        if(executorService == null) return;
//        executorService.shutdown();
    }



}
