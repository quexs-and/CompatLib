package com.quexs.compatlib.wheel.zip.runable;

import android.os.Build;
import android.util.Log;

import com.quexs.compatlib.wheel.util.FileUtils;
import com.quexs.compatlib.wheel.zip.listener.DeCompressConcurrentListener;
import com.quexs.compatlib.wheel.zip.listener.ZipDeCompressListener;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
* @date 2024/6/1 0:18
* @author Quexs
* @Description
*/
public class ZipDeCompressConcurrentRunnable implements Runnable, DeCompressConcurrentListener {

    private final File sourceFile;
    private final String targetFolderPath;
    private final int nThreads;
    private final ZipDeCompressListener zipDeCompressListener;
    private final AtomicInteger atomicInteger = new AtomicInteger(0);
    //计算解压缩后文件总长度
    long deCompressFileTotal;
    int deCompressFileSize;

    public ZipDeCompressConcurrentRunnable(File sourceFile, String targetFolderPath, int nThreads, ZipDeCompressListener zipDeCompressListener){
        this.sourceFile = sourceFile;
        this.targetFolderPath = targetFolderPath;
        this.nThreads = nThreads;
        this.zipDeCompressListener = zipDeCompressListener;
    }

    @Override
    public void run() {
        try {
            deCompressConcurrent();
        } catch (Exception e) {
            if(zipDeCompressListener != null){
                zipDeCompressListener.deCompressException(e);
            }
        }
    }

    @Override
    public void deCompressSize(int len) {
        deCompressFileSize = atomicInteger.addAndGet(len);
        if(zipDeCompressListener != null){
            zipDeCompressListener.deCompressProgress((int) (deCompressFileSize * 1d / deCompressFileTotal * 100), deCompressFileSize, deCompressFileTotal);
        }
    }

    /**
     * zip解压缩（多线程）
     * @throws IOException
     */
    public void deCompressConcurrent() throws Exception {
        File targetFolder = new File(targetFolderPath, sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf(".")));
        if(targetFolder.exists()){
            FileUtils.deleteFile(targetFolder);
        }
        ZipFile zipFile;
        try {
            zipFile = new ZipFile(sourceFile);
        }catch (ZipException e){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                zipFile = new ZipFile(sourceFile, Charset.forName("GBK"));
            }else {
                //TODO 待解决Android 6 不支持问题
                throw new IOException("Android 6 partial encoding is not supported");
            }
        }
        try {
            deCompressFileTotal = getDeCompressZipTotal(zipFile);
        }catch (IllegalArgumentException e){
            zipFile.close();
            zipFile = new ZipFile(sourceFile, Charset.forName("GBK"));
            deCompressFileTotal = getDeCompressZipTotal(zipFile);
        }
        //创建线程池-并发解压缩
        ThreadPoolExecutor fixedThreadPool = new ThreadPoolExecutor(nThreads, nThreads, 60L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<Runnable>());
        fixedThreadPool.allowCoreThreadTimeOut(true);
        CompletionService<File> completionService = new ExecutorCompletionService<>(fixedThreadPool);
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            int futureTotal = 0;
            while (entries.hasMoreElements()){
                ZipEntry zipEntry = entries.nextElement();
                String fileName = zipEntry.getName();
                if ("../".equalsIgnoreCase(fileName)) {
                    continue;
                }
                File entryDest = new File(targetFolder, fileName);
                String lastFileName = entryDest.getName();
                if(lastFileName.startsWith(".") && !".DS_Store".equals(lastFileName)){
                    continue;
                }
                if (zipEntry.isDirectory()) {
                    entryDest.mkdirs();
                    continue;
                }else {
                    File parentFile = entryDest.getParentFile();
                    if(parentFile != null && !parentFile.exists()){
                        parentFile.mkdirs();
                    }
                }
                completionService.submit(new ZipEntryConcurrentCallable(zipEntry, entryDest, zipFile,this));
                futureTotal++;
            }
            for(int i = 0; i < futureTotal; i++){
                File file = completionService.take().get();
                if(file != null){
                    Log.d("concurrent_unzip_log", "unzip file path：" + file.getPath());
                    Log.d("concurrent_unzip_log", "unzip file length：" + file.length());
                }
            }
        }finally {
            zipFile.close();
            fixedThreadPool.shutdown();
        }

    }


    /**
     * 获取压缩包未压缩前的大小
     * @param zipFile
     * @return
     */
    private long getDeCompressZipTotal(ZipFile zipFile) throws IllegalArgumentException {
        long size = 0;
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        if(entries.hasMoreElements()){
            ZipEntry zipEntry = entries.nextElement();
            do {
                long fileSize = zipEntry.getSize();
                if(fileSize != -1){
                    size += fileSize;
                }
            }while ((zipEntry = entries.nextElement()) != null);
        }
        return size;
    }

}
