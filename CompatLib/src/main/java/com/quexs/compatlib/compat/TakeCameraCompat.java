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
 * 调用系统相机拍照
 */
public class TakeCameraCompat {

    private final ActivityResultLauncher<Object> takeCameraLauncher;
    private final ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private TakeCameraCompatListener mTakeCameraCompatListener;

    public TakeCameraCompat(ActivityResultCaller resultCaller){
        cameraPermissionLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), takePermCallback());
        takeCameraLauncher = resultCaller.registerForActivityResult(takeCameraContract(), takeCameraCallback());
    }

    /**
     * 拍照
     * @param takeCameraCompatListener
     */
    public void takeCamera(TakeCameraCompatListener takeCameraCompatListener){
        this.mTakeCameraCompatListener = takeCameraCompatListener;
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

    private ActivityResultContract<Object, Uri> takeCameraContract(){
        return new ActivityResultContract<Object, Uri>() {
            Uri uri;
            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Object o) {
                String mineType = "image/jepg";
                String fileName = "take_camera_" + Calendar.getInstance().getTimeInMillis() + ".jpg";
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, mineType);
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                    uri = context.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
                }else {
                    String authorities = context.getApplicationContext().getPackageName();
                    uri = FileProvider.getUriForFile(context, authorities, new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),fileName));
                }
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                return intent;
            }

            @Override
            public Uri parseResult(int i, @Nullable Intent intent) {
                if(i == Activity.RESULT_OK){
                    return uri;
                }
                return null;
            }
        };
    }

    private ActivityResultCallback<Uri> takeCameraCallback(){
        return new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if(mTakeCameraCompatListener != null){
                    mTakeCameraCompatListener.onResult(result);
                }
            }
        };
    }

    public interface TakeCameraCompatListener{
        void onResult(Uri uri);
    }
}
