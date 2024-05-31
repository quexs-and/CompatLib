package com.quexs.compatlib.wheel.zip;


import com.quexs.compatlib.wheel.zip.listener.ZipDeCompressListener;
import com.quexs.compatlib.wheel.zip.runable.ZipDeCompressConcurrentRunnable;
import com.quexs.compatlib.wheel.zip.runable.ZipDeCompressRunnable;

import java.io.File;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
* @date 2024/5/19 08:32
* @author Quexs
* @Description java zip 压缩库封装
*/
public class ZipJava {
    private final ThreadPoolExecutor cachedThreadPool;
    public ZipJava(){
        cachedThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,60L, TimeUnit.MILLISECONDS,new SynchronousQueue<Runnable>());
        //设置允许超时关闭
        cachedThreadPool.allowCoreThreadTimeOut(true);
    }

    public void deCompress(File sourceFile, String targetFolderPath, ZipDeCompressListener zipDeCompressListener){
        cachedThreadPool.execute(new ZipDeCompressRunnable(sourceFile,targetFolderPath,zipDeCompressListener));
    }

    public void deCompressConcurrent(File sourceFile, String targetFolderPath, int nThreads, ZipDeCompressListener zipDeCompressListener){
        cachedThreadPool.execute(new ZipDeCompressConcurrentRunnable(sourceFile,targetFolderPath,nThreads,zipDeCompressListener));
    }

    public void release(){
        cachedThreadPool.shutdown();
    }

}
