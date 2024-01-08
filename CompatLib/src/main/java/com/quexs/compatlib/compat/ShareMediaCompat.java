package com.quexs.compatlib.compat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.quexs.compatlib.util.MimeTypeUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2022/02/14
 * <p>
 * Time: 0:24
 * <p>
 * 备注：共享文件到多媒体库
 */
public class ShareMediaCompat {
    private final Context appContext;
    private ActivityResultLauncher<String> permLauncher;
    private Object objFile;
    private ThreadPoolExecutor threadPool;
    private ShareMediaCompatListener mShareMediaCompatListener;

    public ShareMediaCompat(Context context, ActivityResultCaller resultCaller){
        appContext = context.getApplicationContext();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            permLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.RequestPermission(), permResultCallback());
        }
        initThreadPool();
    }

    /**
     * 共享图片到相册
     * @param image
     * @param shareMediaCompatListener
     */
    public void shareBitmap(Bitmap image, ShareMediaCompatListener shareMediaCompatListener){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            objFile = image;
            mShareMediaCompatListener = shareMediaCompatListener;
            permLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }else {
            threadPool.execute(new ShareBitmapRunnable(image, shareMediaCompatListener));
        }
    }

    /**
     * 共享文件到媒体库
     * @param file
     * @param shareMediaCompatListener
     */
    public void shareFile(File file,ShareMediaCompatListener shareMediaCompatListener){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            objFile = file;
            mShareMediaCompatListener = shareMediaCompatListener;
            permLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }else {
            threadPool.execute(new ShareFileRunnable(file, shareMediaCompatListener));
        }
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
                    if(objFile instanceof File){
                        threadPool.execute(new ShareFileRunnable((File)objFile, mShareMediaCompatListener));
                    }else{
                        threadPool.execute(new ShareBitmapRunnable((Bitmap)objFile, mShareMediaCompatListener));
                    }
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
    private void saveFileToMedia(File file) throws IOException{
        FileInputStream is = new FileInputStream(file);
        File parentFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Camera");
        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
        File mediaFile = new File(parentFile, file.getName());
        FileOutputStream fos = new FileOutputStream(mediaFile);
        byte[] buff = new byte[1024];
        int len;
        while ((len = is.read(buff)) != -1) {
            fos.write(buff, 0, len);
        }
        is.close();
        fos.flush();
        fos.close();
        // 保存图片、视频后发送广播通知更新数据库
        Uri uri = Uri.fromFile(mediaFile);
        appContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
    }

    /**
     * 共享文件到媒体库
     * @param file
     */
    private void saveFileToMediaQ(File file) throws Exception{
        //配置共享文件参数
        ContentResolver resolver = appContext.getContentResolver();
        FileInputStream is = new FileInputStream(file);
        BufferedInputStream bin = new BufferedInputStream(is);
        String mimeType = MimeTypeUtil.guessContentTypeFromStream(bin);
        if(mimeType == null){
            throw new Exception("MimeType Unrecognized");
        }
        ContentValues localContentValues = getContentValues(file.getName(), mimeType);
        localContentValues.put(MediaStore.Video.Media.SIZE, file.length());
        String relativePath = Environment.DIRECTORY_DCIM + File.separator + "Camera";
        localContentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
        Uri localUri;
        if (Pattern.compile("image/*").matcher(mimeType).find()){
            localContentValues.put(MediaStore.MediaColumns.ORIENTATION, 0);
            localUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, localContentValues);
        }else if(Pattern.compile("video/*").matcher(mimeType).find()){
            localContentValues.put(MediaStore.MediaColumns.ORIENTATION, 0);
            localUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, localContentValues);
        }else if(Pattern.compile("audio/*").matcher(mimeType).find()){
            //相对路径
            localUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, localContentValues);
        }else {
            throw new Exception("MimeType is not a media file");
        }
        //复制文件到共享目录
        OutputStream out = resolver.openOutputStream(localUri);
        int len;
        byte[] buffer = new byte[1024];
        while ((len = bin.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.flush();
        out.close();
        bin.close();
        localContentValues.clear();
        localContentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
        localContentValues.putNull(MediaStore.MediaColumns.DATE_EXPIRES);
        resolver.update(localUri,localContentValues,null,null);
//
//        //通知图库刷新
//        Intent localIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//        localIntent.setData(localUri);
//        //保存图片、视频后发送广播通知更新数据库
//        appContext.sendBroadcast(localIntent);
    }

    private ContentValues getContentValues(String displayName, String mineType){
        ContentValues localContentValues = new ContentValues();
        long paramLong = Calendar.getInstance().getTimeInMillis();
        // 标题
        localContentValues.put(MediaStore.MediaColumns.TITLE, displayName);
        // 文件名
        localContentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        // 文件类型
        localContentValues.put(MediaStore.MediaColumns.MIME_TYPE, mineType);
        //插入时间
        localContentValues.put(MediaStore.MediaColumns.DATE_TAKEN, paramLong);
        // 修改时间
        localContentValues.put(MediaStore.MediaColumns.DATE_MODIFIED, paramLong / 1000);
        // 添加时间
        localContentValues.put(MediaStore.MediaColumns.DATE_ADDED, paramLong / 1000);

        localContentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);
        return localContentValues;
    }

    private class ShareBitmapRunnable implements Runnable{
        private final Bitmap image;
        private final ShareMediaCompatListener shareMediaCompatListener;
        public ShareBitmapRunnable(Bitmap image,ShareMediaCompatListener shareMediaCompatListener){
            this.image = image;
            this.shareMediaCompatListener = shareMediaCompatListener;
        }

        @Override
        public void run() {
            try {
                if(shareMediaCompatListener != null){
                    shareMediaCompatListener.shareStart();
                }
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    saveImageToGalleryQ(image);
                }else {
                    saveImageToGallery(image);
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

    private class ShareFileRunnable implements Runnable{
        private final File file;
        private final ShareMediaCompatListener shareMediaCompatListener;
        private ShareFileRunnable(File file, ShareMediaCompatListener shareMediaCompatListener){
            this.file = file;
            this.shareMediaCompatListener = shareMediaCompatListener;
        }

        @Override
        public void run() {
            try {
                if(shareMediaCompatListener != null){
                    shareMediaCompatListener.shareStart();
                }
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    saveFileToMediaQ(file);
                }else {
                    saveFileToMedia(file);
                }
                if(shareMediaCompatListener != null){
                    shareMediaCompatListener.shareSuccess();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if(shareMediaCompatListener != null){
                    shareMediaCompatListener.shareError(e);
                }
            }
        }
    }

    /**
     * 保存图片到相册
     * @param image
     * @throws IOException
     */
    private void saveImageToGallery(Bitmap image) throws IOException {
        File parentFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Camera");
        if(!parentFile.exists()){
            parentFile.mkdirs();
        }
        String fileName = Calendar.getInstance().getTimeInMillis() + ".jpg";
        File file = new File(parentFile, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        // 通过io流的方式来压缩保存图片
        image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();
        // 保存图片后发送广播通知更新数据库
        Uri uri = Uri.fromFile(file);
        appContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
    }

    /**
     * 保存图片到相册
     * @param image
     */
    private void saveImageToGalleryQ(Bitmap image) throws FileNotFoundException {
        String fileName = Calendar.getInstance().getTimeInMillis() + ".jpg";
        ContentValues localContentValues = getContentValues(fileName, "image/jpeg");
        String relativePath = Environment.DIRECTORY_DCIM + File.separator + "Camera";
        localContentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
        ContentResolver resolver = appContext.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, localContentValues);
        OutputStream out = resolver.openOutputStream(uri);
        image.compress(Bitmap.CompressFormat.JPEG, 100, out);
        localContentValues.clear();
        localContentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
        localContentValues.putNull(MediaStore.MediaColumns.DATE_EXPIRES);
        resolver.update(uri, localContentValues, null, null);
    }


    public interface ShareMediaCompatListener{
        void shareStart();
        void shareError(Exception e);
        void shareSuccess();
    }


}
