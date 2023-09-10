package com.quexs.compatlib.compat;

import android.Manifest;
import android.app.Activity;
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
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import org.json.JSONObject;

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
 * Date: 2022/02/13
 * <p>
 * Time: 0:24
 * <p>
 * 备注：调用系统相机拍照并保存到相册
 */
public class TakeCameraAlbumCompat {

    private final ActivityResultLauncher<Object> takeCameraLauncher;
    private final ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private TakeCameraCompatListener mTakeCameraCompatListener;

    public TakeCameraAlbumCompat(ActivityResultCaller resultCaller){
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
            Uri resultUri;
            Context mContext;
            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Object o) {
                String fileName = "IMAGE_" + Calendar.getInstance().getTimeInMillis() + ".jpg";
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    // >= Android Q 设备拍照后 会直接共享到相册
                    String mineType = "image/jpeg";
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, mineType);
                    //相对路径
                    String relativePath = Environment.DIRECTORY_DCIM + File.separator + "CompatLib";
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
                    resultUri = context.getApplicationContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, resultUri);
                }else {
                    // < Android Q 设备拍照后 需要通知更新到相册
                    mContext = context;
                    File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_DCIM + File.separator + "CompatLib");
                    if(!fileDir.exists()){
                        fileDir.mkdirs();
                    }
                    File file = new File(fileDir,fileName);
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                        resultUri = Uri.fromFile(file);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, resultUri);
                    }else {
                        String authorities = context.getApplicationContext().getPackageName() + ".fileProvider";
                        Uri uri = FileProvider.getUriForFile(context, authorities, file);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        resultUri = Uri.fromFile(file);
                    }
                    intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                }
                return intent;
            }

            @Override
            public Uri parseResult(int i, @Nullable Intent intent) {
                if(i == Activity.RESULT_OK){
                    if(mContext != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                        //Android Q 之前需要通知相册更新
                        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, resultUri));
                        //防止内存泄露
                        mContext = null;
                    }
                    return resultUri;
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
