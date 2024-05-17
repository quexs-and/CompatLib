package com.quexs.compatdemo.util;

import android.os.Build;
import android.util.Log;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UnzipUtils {
    private static final String TAG = "unzip_log";

    public static boolean doUnZip(String inZipPath, String outPath){
        boolean zipResult = false;
        long unzipStartTime = System.currentTimeMillis();
        File file = new File(inZipPath);
        Log.w(TAG, "doUnZipSelectedPaths length = " + file.length());

        net.lingala.zip4j.ZipFile zipFile = null;
        try{
            zipFile = new net.lingala.zip4j.ZipFile(file);
            Log.w(TAG, "doUnZipSelectedPaths, zipFile: " + zipFile.getFile().getName() + " length = " + file.length());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                zipFile.setCharset(StandardCharsets.UTF_8);
            }
            List<net.lingala.zip4j.model.FileHeader> headers = zipFile.getFileHeaders();
            if (isRandomCode(headers)) {
                try {
                    zipFile.close();
                    zipFile = new net.lingala.zip4j.ZipFile(file);
                    zipFile.setCharset(Charset.forName("GBK"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!zipFile.isValidZipFile()){
                Log.w(TAG, "doUnZipSelectedPaths, zipFile is invalid");
                return zipResult;
            }
            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
            zipFile.setRunInThread(true);
            Log.w(TAG, "extractAll start state = " + progressMonitor.getState());

            zipFile.extractAll(outPath);
            //判断解压缩进度
            int progress = 0;
            //方法一
            while (progressMonitor.getState() == ProgressMonitor.State.BUSY){
                if(progress < progressMonitor.getPercentDone()){
                    progress = progressMonitor.getPercentDone();
                    Log.d(TAG, "progress：" + progress);
                }
            }
            //方法二
            while (progressMonitor.getResult() != ProgressMonitor.Result.SUCCESS
                    && progressMonitor.getResult() != ProgressMonitor.Result.ERROR
                    && progressMonitor.getResult() != ProgressMonitor.Result.CANCELLED){
                if(progress < progressMonitor.getPercentDone()){
                    progress = progressMonitor.getPercentDone();
                    Log.d(TAG, "progress：" + progress);
                }
            }
            zipResult = true;

            Log.w(TAG, "doUnZipSelectedPaths end useTime = " + (System.currentTimeMillis() - unzipStartTime));
        }catch (ZipException e){
            Log.e(TAG, "doUnZipSelectedPaths catch ZipException : " + e.getMessage(), e);
            switch (e.getType()){
                case WRONG_PASSWORD:
                    break;
                case FILE_NOT_FOUND:
                case UNSUPPORTED_ENCRYPTION:
                    break;
                default:
                    break;
            }
        }finally {
            if (zipFile != null){
                try {
                    zipFile.close();
                } catch (IOException e) {
                    Log.e(TAG, "doUnZipSelectedPaths catch IOException : " + e.getMessage(), e);
                }
            }
        }
        return zipResult;
    }

    private static boolean isRandomCode(List<FileHeader> fileHeaders) {
        for (int i = 0; i < fileHeaders.size(); i++) {
            net.lingala.zip4j.model.FileHeader fileHeader = fileHeaders.get(i);
            boolean canEnCode = Charset.forName("GBK").newEncoder().canEncode(fileHeader.getFileName());
            if (!canEnCode) {//canEnCode为true，表示不是乱码。false.表示乱码。是乱码则需要重新设置编码格式
                return true;
            }
        }
        return false;
    }

}
