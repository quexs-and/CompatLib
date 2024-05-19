package com.quexs.compatlib.wheel.zip.runable;

import com.quexs.compatlib.wheel.util.FileUtils;
import com.quexs.compatlib.wheel.zip.listener.ZipListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
* @date 2024/5/19 09:11
* @author Quexs
* @Description zip 任务
*/
public class ZipRunnable implements Runnable{
    private final File sourceFile;
    private final String targetFolderPath;
    private final ZipListener zipListener;

    public ZipRunnable(File sourceFile, String targetFolderPath, ZipListener zipListener){
        this.sourceFile = sourceFile;
        this.targetFolderPath = targetFolderPath;
        this.zipListener = zipListener;
    }

    byte[] buffer;
    long compressedSize;
    long sourceFileTotal;

    @Override
    public void run() {
        if(zipListener != null){
            zipListener.zipStart(sourceFile, targetFolderPath);
        }
        File targetFolder = new File(targetFolderPath);
        if(!targetFolder.exists()){
            targetFolder.mkdirs();
        }
        String zipFileName = (sourceFile.isDirectory() ? sourceFile.getName() : sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf("."))) + ".zip";
        File targetFile = new File(targetFolder, zipFileName);
        //计算源文件大小
        if(zipListener != null){
            sourceFileTotal = FileUtils.calculateFileSize(sourceFile);
        }
        buffer = new byte[1024];
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetFile))) {
            toZipFile(sourceFile, "", zos);
            if (zipListener != null) {
                zipListener.zipEnd(targetFile, null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (zipListener != null) {
                zipListener.zipEnd(null, e);
            }
        }
    }

    private void toZipFile(File sourceFile, String folderPath, ZipOutputStream zos) throws IOException {
        if(sourceFile.isDirectory()){
            File[] files = sourceFile.listFiles();
            if(files == null || files.length == 0){
                //保留原目录结构
                zos.putNextEntry(new ZipEntry(folderPath + File.separator + sourceFile.getName() + File.separator));
                zos.closeEntry();
                return;
            }
            for(File childFile : files){
                toZipFile(childFile,folderPath + File.separator + sourceFile.getName() + File.separator, zos);
            }
        }else {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile))) {
                zos.putNextEntry(new ZipEntry(folderPath + File.separator + sourceFile.getName()));
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                    if (zipListener != null) {
                        //累计已压缩的文件大小
                        compressedSize += len;
                        zipListener.zipProgress((int) (compressedSize * 1d / sourceFileTotal * 100), compressedSize, sourceFileTotal);
                    }
                }
                zos.closeEntry();
                zos.flush();
            }
        }
    }
}
