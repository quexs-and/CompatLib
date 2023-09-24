package com.quexs.compatlib.compat;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2022/02/16
 * <p>
 * Time: 0:34
 * <p>
 * 备注：调用相册选择器
 */
public class GetAlbumCompat {

    private final ActivityResultLauncher<String[]> permissionsLauncher;
    private final ActivityResultLauncher<Intent> contentLauncher;
    private GetAlbumCompatListener mGetAlbumCompatListener;
    private boolean isWorking;
    private String[] mimeTypes;
    private String contentType;
    private int maxSelectCount;

    public GetAlbumCompat(ActivityResultCaller resultCaller){
        permissionsLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), mGetContentPermCallback());
        contentLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), mGetContentResult());
    }

    /**
     * 打开相册
     * @param selectCount 选择个
     * @param getAlbumCompatListener
     * @param contentType 显示类型
     *                    图片类型 "image/*"
     *                    视频类型 "audio/*"
     *                    图片+视频 "image/*,audio/*"
     * @param mimeTypes 文件格式筛选
     */
    public void openAlbum(int selectCount, GetAlbumCompatListener getAlbumCompatListener, String contentType, String... mimeTypes){
        if(this.isWorking) return;
        this.isWorking = true;
        this.contentType = contentType;
        this.mimeTypes = mimeTypes;
        maxSelectCount = Math.max(selectCount, 1);
        mGetAlbumCompatListener = getAlbumCompatListener;
        permissionsLauncher.launch(getPermissionForInputs(contentType));
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
                        Intent contentIntent = new Intent(MediaStore.ACTION_PICK_IMAGES);
                        //文件选择类型
                        contentIntent.setType(contentType);
                        if(mimeTypes != null && mimeTypes.length > 0){
                            //过滤文件类型
                            contentIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                        }
                        contentIntent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxSelectCount);
                        contentLauncher.launch(contentIntent);
                        return;
                    }
                    Intent contentIntent = new Intent(Intent.ACTION_PICK);
                    //文件选择类型
                    contentIntent.setType(contentType);
                    if(mimeTypes != null && mimeTypes.length > 0){
                        //过滤文件类型
                        contentIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                    }
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
                if (mGetAlbumCompatListener != null) {
                    mGetAlbumCompatListener.onGetAlbumResult(uriList);
                }
            }
        };
    }

    private String[] getPermissionForInputs(String mimeType){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(TextUtils.isEmpty(mimeType)) {
                return new String[]{Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO};
            }
            Set<String> set = new HashSet<>();
            for(String input : mimeTypes){
                if(Pattern.compile("image/*").matcher(input).find()){
                    set.add(Manifest.permission.READ_MEDIA_IMAGES);
                }else if(Pattern.compile("video/*").matcher(input).find()){
                    set.add(Manifest.permission.READ_MEDIA_VIDEO);
                }
            }
            return set.toArray(new String[set.size()]);
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }


    public interface GetAlbumCompatListener{
        void onGetAlbumResult(List<Uri> results);
    }

}
