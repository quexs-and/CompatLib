package com.quexs.cameraxlib.compat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.quexs.cameraxlib.camera.TakeCameraActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2021/08/19
 * <p>
 * Time: 0:24
 * <p>
 * 备注：调用系统相机拍照
 */
public class TakeCameraXCompat {

    private final ActivityResultLauncher<Object> takeCameraLauncher;
    private final ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private TakeCameraXCompatListener mTakeCameraXCompatListener;

    public TakeCameraXCompat(ActivityResultCaller resultCaller){
        cameraPermissionLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), takePermCallback());
        takeCameraLauncher = resultCaller.registerForActivityResult(takeCameraContract(), takeCameraCallback());
    }

    /**
     * 拍照
     * @param takeCameraXCompatListener
     */
    public void takeCamera(TakeCameraXCompatListener takeCameraXCompatListener){
        this.mTakeCameraXCompatListener = takeCameraXCompatListener;
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
                return new Intent(context, TakeCameraActivity.class);
            }

            @Override
            public Uri parseResult(int i, @Nullable Intent intent) {
                if(i == Activity.RESULT_OK && intent != null){
                    return intent.getData();
                }
                return null;
            }
        };
    }

    private ActivityResultCallback<Uri> takeCameraCallback(){
        return new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if(mTakeCameraXCompatListener != null){
                    mTakeCameraXCompatListener.onResult(result);
                }
            }
        };
    }

    public interface TakeCameraXCompatListener{
        void onResult(Uri uri);
    }
}
