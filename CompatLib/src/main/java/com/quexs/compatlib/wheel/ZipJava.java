package com.quexs.compatlib.wheel;

import com.quexs.compatlib.util.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * java 压缩库封装
 */
public class ZipJava {
    private final ThreadPoolExecutor newFixedThreadPool;
    public ZipJava(){
        //设置最大并发数为3条
        newFixedThreadPool = new ThreadPoolExecutor(3, 3,
                50L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        //设置允许超时关闭
        newFixedThreadPool.allowCoreThreadTimeOut(true);
    }

    /**
     * 文件压缩
     * @param sourceFile 源文件
     * @param targetFolderPath 压缩文件存放目录路径
     * @param zipListener 压缩监听类
     */
    public void zip(File sourceFile,  String targetFolderPath, ZipListener zipListener){
        if(newFixedThreadPool == null) return;
        newFixedThreadPool.execute(new ZipRunnable(sourceFile, targetFolderPath, zipListener));
    }

    /**
     * zip解压缩
     * @param inZipFile
     * @param targetPath
     * @param unzipListener
     */
    public void unzip(File inZipFile, String targetPath, UnzipListener unzipListener) {
        if(newFixedThreadPool == null) return;
        newFixedThreadPool.execute(new UnzipRunnable(inZipFile,targetPath,unzipListener));
    }

    public void release(){
        if(newFixedThreadPool == null) return;
        newFixedThreadPool.shutdown();
    }

    private class ZipRunnable implements Runnable{
        private final File sourceFile;
        private final String targetFolderPath;
        private final ZipListener zipListener;

        byte[] buffer;
        long compressedSize;
        long sourceFileTotal;
        public ZipRunnable(File sourceFile, String targetFolderPath, ZipListener zipListener){
            this.sourceFile = sourceFile;
            this.targetFolderPath = targetFolderPath;
            this.zipListener = zipListener;
        }

        @Override
        public void run() {
            if(zipListener != null){
                zipListener.onZipStart(sourceFile, targetFolderPath);
            }
            if(!sourceFile.exists()){
                if(zipListener != null){
                    zipListener.onZipEnd(false,null,  new IOException("file does not exist"));
                }
            }
            File targetFolder = new File(targetFolderPath);
            if(!targetFolder.exists()){
                targetFolder.mkdirs();
            }
            String zipFileName = (sourceFile.isDirectory() ? sourceFile.getName() : sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf("."))) + ".zip";
            File targetFile = new File(targetFolder, zipFileName);
            //计算源文件大小
            sourceFileTotal = calculateFileSize(sourceFile);
            buffer = new byte[1024];
            ZipOutputStream zos = null;
            try {
                zos = new ZipOutputStream(new FileOutputStream(targetFile));
                toZipFile(sourceFile, "", zos);
                if(zipListener != null){
                    zipListener.onZipEnd(true, targetFile, null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if(zipListener != null){
                    zipListener.onZipEnd(false, null, e);
                }
            }finally {
                if(zos != null){
                    try {
                        zos.closeEntry();
                    } catch (IOException e) {
                        // 处理关闭时可能出现的异常
                    }
                }
            }
        }

        private void toZipFile(File sourceFile, String folderPath, ZipOutputStream zos) throws IOException {
            if(sourceFile.isDirectory()){
                File[] files = sourceFile.listFiles();
                for(File childFile : files){
                    toZipFile(childFile,folderPath + File.separator + sourceFile.getName(), zos);
                }
            }else {
                BufferedInputStream bis = null;
                try {
                    bis = new BufferedInputStream(new FileInputStream(sourceFile));
                    zos.putNextEntry(new ZipEntry(folderPath + File.separator + sourceFile.getName()));
                    int len;
                    while ((len = bis.read(buffer)) != -1){
                        zos.write(buffer, 0, len);
                        //累计已压缩的文件大小
                        compressedSize += len;
                        if(zipListener != null){
                            zipListener.onZipProgress((int)(compressedSize * 1d / sourceFileTotal * 100), compressedSize, sourceFileTotal);
                        }
                    }
                }finally {
                    if(bis != null){
                        try {
                            bis.close();
                        } catch (IOException e) {
                            // 处理关闭时可能出现的异常
                        }
                    }
                }

            }
        }

    }

    /**
     * 计算文件、目录大小
     * @param file
     * @return
     */
    private long calculateFileSize(File file){
        long size = 0;
        if(file.isDirectory()){
            File[] childFiles = file.listFiles();
            if(childFiles != null){
                for(File childFile : childFiles){
                    size += calculateFileSize(childFile);
                }
            }
        }else {
            size += file.length();
        }
        return size;
    }



    /**
     * Zip解压缩
     */
    private class UnzipRunnable implements Runnable{
        private final File sourceFile;
        private final String targetFolderPath;
        private final UnzipListener unzipListener;
        public UnzipRunnable(File sourceFile, String targetFolderPath, UnzipListener unzipListener){
            this.sourceFile = sourceFile;
            this.targetFolderPath = targetFolderPath;
            this.unzipListener = unzipListener;
        }

        @Override
        public void run() {
            if(unzipListener != null){
                unzipListener.onUnzipStart(sourceFile, targetFolderPath);
            }
            if(!sourceFile.exists()){
                if(unzipListener != null){
                    unzipListener.onUnzipEnd(false, null, new IOException("file does not exist"));
                }
            }
            File targetFolder = new File(targetFolderPath,sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf(".")));
            if(targetFolder.exists()){
                FileUtils.deleteFile(targetFolder);
            }
            targetFolder.mkdirs();
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(sourceFile);
                long unzipTotal = 0;
                if(unzipListener != null){
                    unzipTotal = unzipTotal(zipFile);
                }
                byte[] buffer = new byte[1024];
                int len;
                long unzipSize = 0;
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()){
                    ZipEntry zipEntry = entries.nextElement();
                    //处理乱码问题
                    boolean canEnCode = Charset.forName("GBK").newEncoder().canEncode(zipEntry.getName());
                    String fileName = canEnCode ? zipEntry.getName() : new String(zipEntry.getName().getBytes(StandardCharsets.UTF_8), Charset.forName("GBK"));
                    if("../".equalsIgnoreCase(fileName)){
                        continue;
                    }
                    File entryDest = new File(targetFolder, fileName);
                    if(zipEntry.isDirectory()){
                        entryDest.mkdirs();
                    }
                    BufferedInputStream bis = null;
                    BufferedOutputStream bos = null;
                    try {
                        bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                        bos = new BufferedOutputStream(new FileOutputStream(entryDest));
                        while ((len = bis.read(buffer)) != -1){
                            bos.write(buffer,0,len);
                            unzipSize += len;
                            if(unzipListener != null){
                                unzipListener.onUnzipProgress((int)(unzipSize * 1d / unzipTotal * 100),unzipSize, unzipTotal);
                            }
                        }
                        bos.flush();
                    }finally {
                        try {
                            bos.close();
                            bis.close();
                        }catch (IOException e){

                        }

                    }
                }
                if(unzipListener != null){
                    unzipListener.onUnzipEnd(true, targetFolder, null);
                }
            }catch (IOException e){
                e.printStackTrace();
                if(unzipListener != null){
                    unzipListener.onUnzipEnd(false, null, e);
                }
            }finally {
                if(zipFile != null){
                    try {
                        zipFile.close();
                    } catch (IOException e) {

                    }
                }
            }
        }
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

    public interface UnzipListener{
        void onUnzipStart(File inZipFile, String targetFolderPath);
        /**
         *
         * @param unzipSize 解压缩文件大小
         * @param unzipTotal 解压缩后文件总大小
         */
        void onUnzipProgress(int progress,long unzipSize, long unzipTotal);

        void onUnzipEnd(boolean isSuccess,File unzipFile,IOException e);

    }

    public interface ZipListener{
        void onZipStart(File inZipFile, String targetFolderPath);
        void onZipProgress(int progress, long compressedSize, long sourceFileTotal);
        void onZipEnd(boolean isSuccess, File zipFile, IOException e);
    }


}
