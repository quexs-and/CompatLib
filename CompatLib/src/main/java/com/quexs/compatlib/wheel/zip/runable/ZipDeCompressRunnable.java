package com.quexs.compatlib.wheel.zip.runable;

import android.os.Build;

import com.quexs.compatlib.wheel.util.FileUtils;
import com.quexs.compatlib.wheel.zip.listener.ZipDeCompressListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
* @date 2024/6/1 0:08
* @author Quexs
* @Description
*/
public class ZipDeCompressRunnable implements Runnable{
    private final File sourceFile;
    private final String targetFolderPath;
    private final ZipDeCompressListener zipDeCompressListener;

    public ZipDeCompressRunnable(File sourceFile, String targetFolderPath, ZipDeCompressListener zipDeCompressListener){
        this.sourceFile = sourceFile;
        this.targetFolderPath = targetFolderPath;
        this.zipDeCompressListener = zipDeCompressListener;
    }

    @Override
    public void run() {
        try {
            deCompress(sourceFile, targetFolderPath, zipDeCompressListener);
        } catch (IOException e) {
            if(zipDeCompressListener != null){
                zipDeCompressListener.deCompressException(e);
            }
        }
    }


    /**
     *  zip解压缩
     * @param sourceFile 文件来源
     * @param targetFolderPath 解压目录
     */
    public void deCompress(File sourceFile, String targetFolderPath, ZipDeCompressListener zipDeCompressListener) throws IOException {
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
        //计算解压缩后文件总长度
        long deCompressFileTotal;
        try {
            deCompressFileTotal = getDeCompressZipTotal(zipFile);
        }catch (IllegalArgumentException e){
            zipFile.close();
            zipFile = new ZipFile(sourceFile, Charset.forName("GBK"));
            deCompressFileTotal =  getDeCompressZipTotal(zipFile);
        }
        int len;
        byte[] buffer = new byte[1024];
        long deCompressFileSize = 0;
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()){
                ZipEntry zipEntry = entries.nextElement();
                //处理乱码问题
//                boolean canEnCode = Charset.forName("GBK").newEncoder().canEncode(zipEntry.getName());
//                String fileName = canEnCode ? zipEntry.getName() : new String(zipEntry.getName().getBytes(StandardCharsets.UTF_8), Charset.forName("GBK"));
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
                BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(entryDest));
                while ((len = bis.read(buffer)) != -1) {
                    bos.write(buffer, 0, len);
                    if(zipDeCompressListener != null){
                        deCompressFileSize += len;
                        zipDeCompressListener.deCompressProgress((int) (deCompressFileSize * 1d / deCompressFileTotal * 100), deCompressFileSize, deCompressFileTotal);
                    }
                }
                bos.flush();
                bos.close();
                bis.close();
            }
        }finally {
            zipFile.close();
        }
    }

    /**
     * 获取压缩包未压缩前的大小
     * @param zipFile
     * @return
     */
    private long getDeCompressZipTotal(ZipFile zipFile) throws IllegalArgumentException, IOException {
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
