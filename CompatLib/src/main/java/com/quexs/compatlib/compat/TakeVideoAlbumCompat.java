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
 * 备注：调用系统相机录制视频并保存到相册
 */
public class TakeVideoAlbumCompat {

    private final ActivityResultLauncher<Object> takeVideoLauncher;
    private final ActivityResultLauncher<String[]> cameraPermissionLauncher;
    private TakeVideoCompatListener mTakeVideoCompatListener;

    public TakeVideoAlbumCompat(ActivityResultCaller resultCaller){
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
            Uri resultUri;
            Context mContext;
            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Object o) {
                String mineType = "video/3gpp";
                String fileName = Calendar.getInstance().getTimeInMillis() + ".3gp";
                Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    // >= Android Q 设备拍照后会直接存入相册
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, mineType);
                    //相对路径
                    String relativePath = Environment.DIRECTORY_DCIM + File.separator + context.getApplicationContext().getPackageName();
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
                    resultUri = context.getApplicationContext().getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,values);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, resultUri);
                }else {
                    mContext = context;
                    //视频存在本地，需要自己共享到相册
                    String authorities = context.getApplicationContext().getPackageName() + ".fileProvider";
                    File fileDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Environment.DIRECTORY_DCIM + File.separator + context.getApplicationContext().getPackageName());
                    if(!fileDir.exists()){
                        fileDir.mkdirs();
                    }
                    File file = new File(fileDir,fileName);
                    Uri uri = FileProvider.getUriForFile(context, authorities, file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    resultUri = Uri.fromFile(file);
                }
                return intent;
            }

            @Override
            public Intent parseResult(int i, @Nullable Intent intent) {
                if(i == Activity.RESULT_OK && intent != null){
                    if(mContext != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                        //Android Q 之前需要通知相册更新
                        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, resultUri));
                    }
                    intent.setData(resultUri);
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
