package com.quexs.compatlib.wheel.zip.runable;

import android.util.Log;

import com.quexs.compatlib.wheel.util.FileUtils;
import com.quexs.compatlib.wheel.zip.listener.UnzipListener;
import com.quexs.compatlib.wheel.zip.listener.UnzipSizeConcurrentListener;

import java.io.File;
import java.util.Enumeration;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
* @date 2024/5/19 10:27
* @author Quexs
* @Description unzip 任务
*/
public class UnzipConcurrentRunnable implements Runnable, UnzipSizeConcurrentListener {

    private final File sourceFile;
    private final String targetFolderPath;
    private final UnzipListener unzipListener;
    private final ReentrantLock lock;
    public UnzipConcurrentRunnable(File sourceFile, String targetFolderPath, UnzipListener unzipListener){
        this.sourceFile = sourceFile;
        this.targetFolderPath = targetFolderPath;
        this.unzipListener = unzipListener;
        this.lock = new ReentrantLock();
    }

    private long unzipTotal = 0;
    long unzipSize = 0;
    @Override
    public void run() {
        if(unzipListener != null){
            unzipListener.unzipStart(sourceFile, targetFolderPath);
        }
        File targetFolder = new File(targetFolderPath,sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf(".")));
        if(targetFolder.exists()){
            FileUtils.deleteFile(targetFolder);
        }
        targetFolder.mkdirs();
        try (ZipFile zipFile = new ZipFile(sourceFile)) {
            //创建线程池-并发解压缩
            int nThreads = Runtime.getRuntime().availableProcessors();
            int fixedSize = Math.min(2 * nThreads + 1, 10);
            ThreadPoolExecutor fixedThreadPool = new ThreadPoolExecutor(fixedSize, fixedSize, 60L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
            fixedThreadPool.allowCoreThreadTimeOut(true);
            CompletionService<File> completionService = new ExecutorCompletionService<>(fixedThreadPool);
            if (unzipListener != null) {
                unzipTotal = unzipTotal(zipFile);
            }
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int futureTotal = 0;
            while (entries.hasMoreElements()) {
                futureTotal++;
                completionService.submit(new UnzipEntryCallable(entries.nextElement(), targetFolder, zipFile, this));
            }
            //循环等待所有线程解压缩结束
            for(int i = 0; i < futureTotal; i++){
                File file = completionService.take().get();
                if(file != null){
                    Log.d("concurrent_unzip_log", "unzip file path：" + file.getPath());
                    Log.d("concurrent_unzip_log", "unzip file length：" + file.length());
                }
            }
            fixedThreadPool.shutdown();
            if (unzipListener != null) {
                unzipListener.unzipEnd(targetFolder, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (unzipListener != null) {
                unzipListener.unzipEnd(null, e);
            }
        }
    }


    @Override
    public void addUnzipSize(int len) {
        if(unzipListener == null) return;
        lock.lock();
        unzipSize += len;
        unzipListener.unzipProgress((int) (unzipSize * 1d / unzipTotal * 100), unzipSize, unzipTotal);
        lock.unlock();
    }

    /**
     * 获取压缩包未压缩前的大小
     * @param zipFile
     * @return
     */
    private long unzipTotal(ZipFile zipFile) {
        long size = 0;
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            long fileSize = entries.nextElement().getSize();
            if(fileSize != -1){
                size += fileSize;
            }
        }
        return size;
    }
}
