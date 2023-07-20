package com.quexs.compatlib.compat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2022/02/16
 * <p>
 * Time: 0:34
 * <p>
 * 备注：获取文档文件
 */
public class GetContentCompat {

    @StringDef({MineType.IMAGE, MineType.VIDEO,  MineType.AUDIO, MineType.ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MineType {
        /**
         * 视频
         */
        String VIDEO = "video/*";
        /**
         * 图片
         */
        String IMAGE = "image/*";
        /**
         * 音频
         */
        String AUDIO = "audio/*";
        /**
         * 所有文件
         */
        String ALL = "*/*";
    }

    private final ActivityResultLauncher<String[]> permissionsLauncher;
    private final ActivityResultLauncher<Intent> contentLauncher;
    private GetContentCompatListener mGetContentCompatListener;
    private boolean isWorking;
    private String contentType;
    private int maxSelectCount;

    public GetContentCompat(ActivityResultCaller resultCaller){
        permissionsLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), mGetContentPermCallback());
        contentLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), mGetContentResult());
    }

    public void openContent(int selectCount, GetContentCompatListener getContentCompatListener, @MineType String... inputs){
        if(isWorking) return;
        maxSelectCount = Math.max(selectCount, 1);
        mGetContentCompatListener = getContentCompatListener;
        isWorking = true;
        contentType = getContentType(inputs);
        permissionsLauncher.launch(getPermissionInputs(inputs));
    }

    /**
     * 未授予的权限
     * @param perms
     */
    public void onPermissionsDenied(List<String> perms){

    }

    private ActivityResultCallback<Map<String, Boolean>> mGetContentPermCallback(){
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        //Android 13 适配新版相册
                        if(!MineType.ALL.equals(contentType)){
                            Intent contentIntent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                            contentIntent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxSelectCount);
                            contentLauncher.launch(contentIntent);
                            return;
                        }
                    }
                    Intent contentIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentIntent.setType(contentType);
                    contentIntent.putExtra(Intent.EXTRA_MIME_TYPES, contentType.split(";"));
                    //启动多选
                    contentIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, maxSelectCount > 1);
                    contentLauncher.launch(contentIntent);
                }else {
                    //有权限被拒绝
                    isWorking = false;
                    onPermissionsDenied(perms);
                }

            }
        };
    }

    private ActivityResultCallback<ActivityResult> mGetContentResult(){
        return new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                isWorking = false;
                Intent intent = result.getData();
                List<Uri> uriList = null;
                if (intent != null && result.getResultCode() == Activity.RESULT_OK) {
                    if(result.getData().getData() != null){
                        uriList = new ArrayList<>();
                        uriList.add(result.getData().getData());
                    }else if(result.getData().getClipData() != null){
                        ClipData clipData = result.getData().getClipData();
                        int lastCount = clipData.getItemCount();
                        LinkedHashSet<Uri> resultSet = new LinkedHashSet<>();
                        for (int i = 0; i < lastCount; i++) {
                            if (i >= maxSelectCount) {
                                break;
                            }
                            resultSet.add(clipData.getItemAt(i).getUri());
                        }
                        uriList = new ArrayList<>(resultSet);
                    }
                }
                if (mGetContentCompatListener != null) {
                    mGetContentCompatListener.onGetContentResult(uriList);
                }
            }
        };
    }

    private String getContentType(String... inputs){
        if(inputs == null) return MineType.ALL;
        StringBuilder builder = new StringBuilder();
        for(String input : inputs){
            if(MineType.ALL.equals(input)){
                return MineType.ALL;
            }
            if(builder.length() > 0){
                builder.append(";");
            }
            builder.append(input);
        }
        return builder.toString();
    }

    private String[] getPermissionInputs(String... inputs){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            String[] permissionInputs = new String[inputs.length];
            int i = 0;
            for(String input : inputs){
                if(MineType.ALL.equals(input)){
                    return new String[]{Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_AUDIO};
                }else if(MineType.IMAGE.equals(inputs[i])){
                    permissionInputs[i] = Manifest.permission.READ_MEDIA_IMAGES;
                }else if(MineType.VIDEO.equals(inputs[i])){
                    permissionInputs[i] = Manifest.permission.READ_MEDIA_VIDEO;
                }else if(MineType.AUDIO.equals(inputs[i])){
                    permissionInputs[i] = Manifest.permission.READ_MEDIA_AUDIO;
                }
            }
            return permissionInputs;
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }

    public interface GetContentCompatListener{
        void onGetContentResult(List<Uri> results);
    }

}
