package com.quexs.compatlib.wheel.zip.runable;

import com.quexs.compatlib.wheel.zip.listener.UnzipSizeConcurrentListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
* @date 2024/5/19 10:53
* @author Quexs
* @Description unzip 任务
*/
public class UnzipEntryCallable implements Callable<File> {
    private final ZipEntry zipEntry;
    private final File targetFolder;
    private final ZipFile zipFile;
    private final UnzipSizeConcurrentListener unzipSizeConcurrentListener;
    public UnzipEntryCallable(ZipEntry zipEntry, File targetFolder, ZipFile zipFile, UnzipSizeConcurrentListener unzipSizeConcurrentListener){
        this.zipEntry = zipEntry;
        this.targetFolder = targetFolder;
        this.zipFile = zipFile;
        this.unzipSizeConcurrentListener = unzipSizeConcurrentListener;
    }

    @Override
    public File call() throws Exception {
        //处理乱码问题
        boolean canEnCode = Charset.forName("GBK").newEncoder().canEncode(zipEntry.getName());
        String fileName = canEnCode ? zipEntry.getName() : new String(zipEntry.getName().getBytes(StandardCharsets.UTF_8), Charset.forName("GBK"));
        if ("../".equalsIgnoreCase(fileName)) {
            return null;
        }
        File entryDest = new File(targetFolder, fileName);
        if (zipEntry.isDirectory()) {
            entryDest.mkdirs();
            return null;
        }
        byte[] buffer = new byte[1024];
        int len;
        try (BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(zipEntry)); BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(entryDest))) {
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
                if(unzipSizeConcurrentListener != null){
                    unzipSizeConcurrentListener.addUnzipSize(len);
                }
            }
            bos.flush();
        }
        return entryDest;
    }
}
