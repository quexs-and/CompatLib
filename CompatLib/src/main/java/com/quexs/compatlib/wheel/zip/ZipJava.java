package com.quexs.compatlib.wheel.zip;


import com.quexs.compatlib.wheel.zip.listener.UnzipListener;
import com.quexs.compatlib.wheel.zip.listener.UnzipProgressListener;
import com.quexs.compatlib.wheel.zip.listener.ZipListener;
import com.quexs.compatlib.wheel.zip.listener.ZipProgressListener;
import com.quexs.compatlib.wheel.zip.runable.UnzipCallable;
import com.quexs.compatlib.wheel.zip.runable.UnzipConcurrentCallable;
import com.quexs.compatlib.wheel.zip.runable.UnzipConcurrentRunnable;
import com.quexs.compatlib.wheel.zip.runable.UnzipRunnable;
import com.quexs.compatlib.wheel.zip.runable.ZipCallable;
import com.quexs.compatlib.wheel.zip.runable.ZipRunnable;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
        //设置最大并发数为3条
        cachedThreadPool = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                60L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>());
        //设置允许超时关闭
        cachedThreadPool.allowCoreThreadTimeOut(true);
    }

    /**
     * 文件压缩
     * @param sourceFile 源文件
     * @param targetFolderPath 压缩文件存放目录路径
     * @param zipListener 压缩监听类
     */
    public void zip(File sourceFile, String targetFolderPath, ZipListener zipListener){
        cachedThreadPool.execute(new ZipRunnable(sourceFile,targetFolderPath,zipListener));
    }

    /**
     * 文件压缩(为了实时获取压缩进程，多消耗了一条线程)
     * @param sourceFile 源文件
     * @param targetFolderPath 压缩文件存放目录路径
     * @param zipProgressListener 压缩进度监听类
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public File zip(File sourceFile, String targetFolderPath, ZipProgressListener zipProgressListener) throws ExecutionException, InterruptedException {
        Future<File> future = cachedThreadPool.submit(new ZipCallable(sourceFile,targetFolderPath,zipProgressListener));
        return future.get();
    }

    /**
     * zip解压缩
     * @param sourceFile 源文件
     * @param targetFolderPath 解压缩文件存放目录
     * @param unzipListener
     */
    public void unzip(File sourceFile, String targetFolderPath, UnzipListener unzipListener) {
        cachedThreadPool.execute(new UnzipRunnable(sourceFile,targetFolderPath,unzipListener));
    }

    /**
     * zip解压缩（使用线程池并发解压缩）
     * @param sourceFile 源文件
     * @param targetFolderPath 解压缩文件存放目录
     * @param unzipListener
     */
    public void unzipConcurrent(File sourceFile, String targetFolderPath, UnzipListener unzipListener) {
        cachedThreadPool.execute(new UnzipConcurrentRunnable(sourceFile,targetFolderPath,unzipListener));
    }

    /**
     * zip解压缩 (为了实时获取解压缩进程，多消耗了一条线程)
     * @param sourceFile
     * @param targetFolderPath
     * @param unzipProgressListener
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public File unzip(File sourceFile, String targetFolderPath, UnzipProgressListener unzipProgressListener) throws ExecutionException, InterruptedException {
        Future<File> future = cachedThreadPool.submit(new UnzipCallable(sourceFile,targetFolderPath,unzipProgressListener));
        return future.get();
    }

    /**
     * zip解压缩 (为了实时获取解压缩进程，多消耗了一条线程)
     * @param sourceFile
     * @param targetFolderPath
     * @param unzipProgressListener
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public File unzipConcurrent(File sourceFile, String targetFolderPath, UnzipProgressListener unzipProgressListener) throws ExecutionException, InterruptedException {
        Future<File> future = cachedThreadPool.submit(new UnzipConcurrentCallable(sourceFile,targetFolderPath,unzipProgressListener));
        return future.get();
    }


    public void release(){
        cachedThreadPool.shutdown();
    }

}
