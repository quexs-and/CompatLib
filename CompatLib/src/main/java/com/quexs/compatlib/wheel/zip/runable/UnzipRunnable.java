package com.quexs.compatlib.wheel.zip.runable;

import com.quexs.compatlib.wheel.util.FileUtils;
import com.quexs.compatlib.wheel.zip.listener.UnzipListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
* @date 2024/5/19 11:50
* @author Quexs
* @Description unzip 任务
*/
public class UnzipRunnable implements Runnable{
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
            unzipListener.unzipStart(sourceFile, targetFolderPath);
        }
        File targetFolder = new File(targetFolderPath,sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf(".")));
        if(targetFolder.exists()){
            FileUtils.deleteFile(targetFolder);
        }
        targetFolder.mkdirs();
        try (ZipFile zipFile = new ZipFile(sourceFile)) {
            long unzipTotal = 0;
            if (unzipListener != null) {
                unzipTotal = unzipTotal(zipFile);
            }
            byte[] buffer = new byte[1024];
            int len;
            long unzipSize = 0;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                //处理乱码问题
                boolean canEnCode = Charset.forName("GBK").newEncoder().canEncode(zipEntry.getName());
                String fileName = canEnCode ? zipEntry.getName() : new String(zipEntry.getName().getBytes(StandardCharsets.UTF_8), Charset.forName("GBK"));
                if ("../".equalsIgnoreCase(fileName)) {
                    continue;
                }
                File entryDest = new File(targetFolder, fileName);
                if (zipEntry.isDirectory()) {
                    entryDest.mkdirs();
                    continue;
                }
                try (BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(zipEntry)); BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(entryDest))) {
                    while ((len = bis.read(buffer)) != -1) {
                        bos.write(buffer, 0, len);
                        unzipSize += len;
                        if (unzipListener != null) {
                            unzipListener.unzipProgress((int) (unzipSize * 1d / unzipTotal * 100), unzipSize, unzipTotal);
                        }
                    }
                    bos.flush();
                }
            }
            if (unzipListener != null) {
                unzipListener.unzipEnd(targetFolder, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (unzipListener != null) {
                unzipListener.unzipEnd(null, e);
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
}
