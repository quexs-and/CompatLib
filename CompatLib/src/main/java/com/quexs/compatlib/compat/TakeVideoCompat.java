package com.quexs.compatlib.compat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

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
 * Date: 2023/09/03
 * <p>
 * Time: 0:24
 * <p>
 * 备注：调用系统相机录制视频
 */
public class TakeVideoCompat {

    private final ActivityResultLauncher<Object> takeCameraLauncher;
    private final ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private TakeVideoCompatListener mTakeVideoCompatListener;

    public TakeVideoCompat(ActivityResultCaller resultCaller){
        cameraPermissionLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), takePermCallback());
        takeCameraLauncher = resultCaller.registerForActivityResult(takeCameraContract(), takeCameraCallback());
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
                    takeCameraLauncher.launch(null);
                }else {
                    //有权限被拒绝
                    onPermissionsDenied(perms);
                }
            }
        };
    }

    private ActivityResultContract<Object, Intent> takeCameraContract(){
        return new ActivityResultContract<Object, Intent>() {
            Uri resultUri;
            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Object o) {
                File file = getPictureFile(context, (String)o);
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                    resultUri = Uri.fromFile(file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, resultUri);
                }else {
                    String authorities = context.getApplicationContext().getPackageName() + ".CompatLibFileProvider";
                    Uri uri = FileProvider.getUriForFile(context, authorities, file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    resultUri = Uri.fromFile(file);
                }
                return intent;
            }

            @Override
            public Intent parseResult(int i, @Nullable Intent intent) {
                if(i == Activity.RESULT_OK && intent != null){
                    intent.setData(resultUri);
                }
                return intent;
            }
        };
    }

    private File getPictureFile(Context context, String directoryPath){
        String fileName = "Video_" + Calendar.getInstance().getTimeInMillis() + ".3gp";
        if(!TextUtils.isEmpty(directoryPath)){
            File directory = new File(directoryPath);
            if(!directory.exists()){
                directory.mkdirs();
            }
            return new File(directory, fileName);
        }else {
            //默认使用外部存储目录Pictures目录下
            File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_MOVIES), "CompatLib");
            if(!directory.exists()){
                directory.mkdirs();
            }
            return new File(directory, fileName);
        }
    }

    private ActivityResultCallback<Intent> takeCameraCallback(){
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
         * 获取 获取缩略图（只有部分机型有返回） Bitmap bitmap = intent.getParcelableExtra("data");
         * @param result
         */
        void onResult(Intent result);
    }
}
