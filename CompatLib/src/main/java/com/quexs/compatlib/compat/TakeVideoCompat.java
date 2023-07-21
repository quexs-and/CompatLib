package com.quexs.compatlib.compat;

import android.Manifest;
import android.app.Activity;
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
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2022/02/14
 * <p>
 * Time: 0:24
 * <p>
 * 备注：调用系统相机录制视频
 */
public class TakeVideoCompat {

    private final ActivityResultLauncher<Object> takeVideoLauncher;
    private final ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private TakeVideoCompatListener mTakeVideoCompatListener;

    public TakeVideoCompat(ActivityResultCaller resultCaller){
        cameraPermissionLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), takePermCallback());
        takeVideoLauncher = resultCaller.registerForActivityResult(takeVideoContract(), takeVideoCallback());
    }

    /**
     * 录制视频
     * @param takeVideoCompatListener
     */
    public void takeVideo(TakeVideoCompatListener takeVideoCompatListener){
        this.mTakeVideoCompatListener = takeVideoCompatListener;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            cameraPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
        }else {
            cameraPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }

    /**
     * 未授予的权限
     * @param perms
     */
    public void onPermissionsDenied(List<String> perms){

    }

    private ActivityResultCallback<Map<String, Boolean>> takePermCallback(){
        return new ActivityResultCallback<Map<String, Boolean>>() {
            @Override
            public void onActivityResult(Map<String, Boolean> result) {
                List<String> perms = new ArrayList<>();
                for(Map.Entry<String, Boolean> entry : result.entrySet()){
                    if(!entry.getValue()){
                        perms.add(entry.getKey());
                    }
                }
                if(perms.isEmpty()){
                    //已获取全部权限
                    takeVideoLauncher.launch(null);
                }else {
                    //有权限被拒绝
                    onPermissionsDenied(perms);
                }
            }
        };
    }

    private ActivityResultContract<Object, Intent> takeVideoContract(){
        return new ActivityResultContract<Object, Intent>() {
            Uri uri;
            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Object o) {
                Uri uri;
                String mineType = "video/mpeg-4";
                String fileName = "take_video_" + Calendar.getInstance().getTimeInMillis() + ".mp4";
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, mineType);
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
                    uri = context.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
                }else {
                    String authorities = context.getApplicationContext().getPackageName() + ".fileprovider";
                    uri = FileProvider.getUriForFile(context, authorities, new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM),fileName));
                }
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                return intent;
            }

            @Override
            public Intent parseResult(int i, @Nullable Intent intent) {
                if(i == Activity.RESULT_OK && intent != null){
                    intent.setData(uri);
                    return intent;
                }
                return null;
            }
        };
    }

    private ActivityResultCallback<Intent> takeVideoCallback(){
        return new ActivityResultCallback<Intent>() {
            @Override
            public void onActivityResult(Intent result) {
                if(mTakeVideoCompatListener != null){
                    mTakeVideoCompatListener.onResult(result);
                }
            }
        };
    }

    public interface TakeVideoCompatListener{
        /**
         * 结果回调
         * 获取 Uri uri = result.getData();
         * 获取 获取缩略图 Bitmap bitmap = intent.getParcelableExtra("data");
         * @param result
         */
        void onResult(Intent result);
    }
}
