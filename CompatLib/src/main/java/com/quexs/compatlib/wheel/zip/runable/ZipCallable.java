package com.quexs.compatlib.wheel.zip.runable;

import com.quexs.compatlib.wheel.util.FileUtils;
import com.quexs.compatlib.wheel.zip.listener.ZipProgressListener;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
* @date 2024/5/19 9:21
* @author Quexs
* @Description zip 任务
*/
public class ZipCallable implements Callable<File> {

    private final File sourceFile;
    private final String targetFolderPath;
    private final ZipProgressListener zipProgressListener;

    public ZipCallable(File sourceFile, String targetFolderPath, ZipProgressListener zipProgressListener){
        this.sourceFile = sourceFile;
        this.targetFolderPath = targetFolderPath;
        this.zipProgressListener = zipProgressListener;
    }

    byte[] buffer;
    long compressedSize;
    long sourceFileTotal;
    @Override
    public File call() throws Exception {
        File targetFolder = new File(targetFolderPath);
        if(!targetFolder.exists()){
            targetFolder.mkdirs();
        }
        String zipFileName = (sourceFile.isDirectory() ? sourceFile.getName() : sourceFile.getName().substring(0, sourceFile.getName().lastIndexOf("."))) + ".zip";
        File targetFile = new File(targetFolder, zipFileName);
        if(zipProgressListener != null){
            //计算源文件大小
            sourceFileTotal = FileUtils.calculateFileSize(sourceFile);
        }
        buffer = new byte[1024];
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(targetFile))) {
            toZipFile(sourceFile, "", zos);
        }
        return targetFile;
    }

    private void toZipFile(File sourceFile, String folderPath, ZipOutputStream zos) throws IOException {
        if(sourceFile.isDirectory()){
            File[] files = sourceFile.listFiles();
            if(files == null || files.length == 0){
                zos.putNextEntry(new ZipEntry(folderPath + File.separator + sourceFile.getName() + File.separator));
                zos.closeEntry();
                return;
            }
            for(File childFile : files){
                toZipFile(childFile,folderPath + File.separator + sourceFile.getName(), zos);
            }
        }else {
            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(sourceFile))) {
                zos.putNextEntry(new ZipEntry(folderPath + File.separator + sourceFile.getName()));
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    zos.write(buffer, 0, len);
                    //累计已压缩的文件大小
                    if (zipProgressListener != null) {
                        compressedSize += len;
                        zipProgressListener.zipProgress((int) (compressedSize * 1d / sourceFileTotal * 100), compressedSize, sourceFileTotal);
                    }
                }
                zos.closeEntry();
                zos.flush();
            }
        }
    }
}
