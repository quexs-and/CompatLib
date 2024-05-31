package com.quexs.compatlib.wheel.zip.runable;

import com.quexs.compatlib.wheel.zip.listener.DeCompressConcurrentListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
* @date 2024/6/1 0:19
* @author Quexs
* @Description
*/
public class ZipEntryConcurrentCallable implements Callable<File> {
    private final ZipEntry zipEntry;
    private final File entryDest;
    private final ZipFile zipFile;
    private final DeCompressConcurrentListener deCompressConcurrentListener;

    public ZipEntryConcurrentCallable(ZipEntry zipEntry, File entryDest, ZipFile zipFile, DeCompressConcurrentListener deCompressConcurrentListener){
        this.zipEntry = zipEntry;
        this.entryDest = entryDest;
        this.zipFile = zipFile;
        this.deCompressConcurrentListener = deCompressConcurrentListener;
    }

    @Override
    public File call() throws Exception {
        byte[] buffer = new byte[1024];
        int len;
        try (BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(entryDest))) {
            while ((len = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
                if(deCompressConcurrentListener != null){
                    deCompressConcurrentListener.deCompressSize(len);
                }
            }
            bos.flush();
        }
        return entryDest;
    }
}
