package com.quexs.compatlib.compat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2022/02/14
 * <p>
 * Time: 0:24
 * <p>
 * 备注：共享多媒体文件
 */
public class ShareMediaCompat {

    private final Context appContext;
    private ActivityResultLauncher<String> permLauncher;
    private Uri shareUri;
    private ThreadPoolExecutor threadPool;
    private final ConvertUriCompat convertUriCompat;
    private ShareMediaCompatListener mShareMediaCompatListener;

    public ShareMediaCompat(Context context, ActivityResultCaller resultCaller){
        appContext = context.getApplicationContext();
        convertUriCompat = new ConvertUriCompat(appContext);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            permLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.RequestPermission(), permResultCallback());
        }
        initThreadPool();
    }

    public void shareFile(Uri uri,ShareMediaCompatListener shareMediaCompatListener){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            shareUri = uri;
            mShareMediaCompatListener = shareMediaCompatListener;
            permLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }else {
            threadPool.execute(new ShareFileRunnable(uri,shareMediaCompatListener));
        }
    }

    /**
     * 分析文件
     * @param file
     */
    public void shareFile(File file,ShareMediaCompatListener shareMediaCompatListener){
        shareFile(Uri.fromFile(file),shareMediaCompatListener);
    }

    /**
     * 未授予的权限
     * @param perm
     */
    public void onPermissionDenied(String perm){

    }

    private ActivityResultCallback<Boolean> permResultCallback(){
        return new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    threadPool.execute(new ShareFileRunnable(shareUri,mShareMediaCompatListener));
                }else {
                    onPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        };
    }

    /**
     * 初始线程池
     */
    private void initThreadPool() {
        int cpuCount = Runtime.getRuntime().availableProcessors();
        int corePoolSize = Math.min(cpuCount * 2, 3);
        int maximumPoolSize = Math.min(cpuCount * 2 + 1, 5);
        long keepAliveTime = 50L;
        LinkedBlockingDeque<Runnable> blockingDeque = new LinkedBlockingDeque<>();
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, blockingDeque);
        threadPool.allowCoreThreadTimeOut(true);
    }

    /**
     * 共享文件到媒体库
     * @param file
     */
    private void saveMediaFileGreaterThanOrEqualQ(File file) throws IOException{
        //配置共享文件参数
        long paramLong = Calendar.getInstance().getTimeInMillis();
        ContentValues localContentValues = new ContentValues();
        localContentValues.put(MediaStore.Images.Media.TITLE, file.getName());
        localContentValues.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
        FileInputStream is = new FileInputStream(file);
        BufferedInputStream bin = new BufferedInputStream(is);
        String mimeType = URLConnection.guessContentTypeFromStream(bin);
        localContentValues.put(MediaStore.Images.Media.MIME_TYPE, mimeType);
        localContentValues.put(MediaStore.Images.Media.DATE_TAKEN, paramLong);
        localContentValues.put(MediaStore.Images.Media.DATE_MODIFIED, paramLong);
        localContentValues.put(MediaStore.Images.Media.DATE_ADDED, paramLong);
        localContentValues.put(MediaStore.Images.Media.ORIENTATION, 0);
        localContentValues.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        localContentValues.put(MediaStore.Images.Media.SIZE, file.length());
        Uri localUri = appContext.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, localContentValues);
        //通知图库刷新
        Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        localIntent.setData(localUri);
        //复制文件到共享目录
        OutputStream out = appContext.getContentResolver().openOutputStream(localUri);
        int len;
        byte[] buffer = new byte[1024];
        while ((len = is.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        is.close();
        out.close();
        //保存图片、视频后发送广播通知更新数据库
        appContext.sendBroadcast(localIntent);
    }


    /**
     * 保存文件到本地目录 如果是多媒体文件则广播通知
     * @param file
     */
    private void saveMediaFileLessThanQ(File file) throws IOException{
        String appDirPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + Environment.DIRECTORY_DCIM + File.separator + appContext.getPackageName() + File.separator + "File";
        File parentFile = new File(appDirPath);
        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
        File mediaFile = new File(appDirPath, file.getName());
        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(mediaFile);
        byte[] buff = new byte[1024];
        int len;
        while ((len = fis.read(buff)) != -1) {
            fos.write(buff, 0, len);
        }
        fis.close();
        fos.flush();
        fos.close();
        // 保存图片、视频后发送广播通知更新数据库
        Uri uri = Uri.fromFile(mediaFile);
        appContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
    }

    private class ShareFileRunnable implements Runnable{
        private final Uri uri;
        private final ShareMediaCompatListener shareMediaCompatListener;
        private ShareFileRunnable(Uri uri, ShareMediaCompatListener shareMediaCompatListener){
            this.uri = uri;
            this.shareMediaCompatListener = shareMediaCompatListener;
        }

        @Override
        public void run() {
            try {
                if(shareMediaCompatListener != null){
                    shareMediaCompatListener.shareStart();
                }
                File file = convertUriCompat.uriToFile(uri);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    saveMediaFileGreaterThanOrEqualQ(file);
                }else {
                    saveMediaFileLessThanQ(file);
                }
                if(shareMediaCompatListener != null){
                    shareMediaCompatListener.shareSuccess();
                }
            } catch (IOException e) {
                e.printStackTrace();
                if(shareMediaCompatListener != null){
                    shareMediaCompatListener.shareError(e);
                }
            }

        }
    }

    public interface ShareMediaCompatListener{
        void shareStart();
        void shareError(IOException e);
        void shareSuccess();

    }


}
