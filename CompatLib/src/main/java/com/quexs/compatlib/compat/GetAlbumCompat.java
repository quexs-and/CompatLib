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
 * 备注：调用相册选择器
 */
public class GetAlbumCompat {

    @StringDef({MimeType.IMAGE,MimeType.VIDEO,MimeType.ALL})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MimeType{
        //获取文件MD5
        String IMAGE = "image/*";
        String VIDEO = "video/*";
        String ALL = "*/*";
    }

    private final ActivityResultLauncher<String[]> permissionsLauncher;
    private final ActivityResultLauncher<Intent> contentLauncher;
    private GetAlbumCompatListener mGetAlbumCompatListener;
    private boolean isWorking;
    private String mimeType;
    private int maxSelectCount;

    public GetAlbumCompat(ActivityResultCaller resultCaller){
        permissionsLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), mGetContentPermCallback());
        contentLauncher = resultCaller.registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), mGetContentResult());
    }

    /**
     * 打开相册
     * @param selectCount 选择个
     * @param getAlbumCompatListener
     * @param mimeType
     */
    public void openAlbum(int selectCount, GetAlbumCompatListener getAlbumCompatListener, @MimeType String mimeType){
        if(this.isWorking) return;
        this.isWorking = true;
        this.mimeType = mimeType;
        maxSelectCount = Math.max(selectCount, 1);
        mGetAlbumCompatListener = getAlbumCompatListener;
        permissionsLauncher.launch(getPermissionForInputs(mimeType));
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
                    Intent contentIntent = new Intent();
                    //已获取全部权限
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        //action
                        contentIntent.setAction(MediaStore.ACTION_PICK_IMAGES);
                        if(maxSelectCount > 1){
                            contentIntent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, maxSelectCount);
                        }
                    }else {
                        //action
                        contentIntent.setAction(Intent.ACTION_PICK);
                        //启动多选
                        contentIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, maxSelectCount > 1);
                    }
                    //文件选择类型
                    contentIntent.setType(mimeType);
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

    private String[] getPermissionForInputs(@MimeType String mimeType){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(MimeType.IMAGE.equals(mimeType)){
                return new String[]{Manifest.permission.READ_MEDIA_IMAGES};
            }else if(MimeType.VIDEO.equals(mimeType)){
                return new String[]{Manifest.permission.READ_MEDIA_VIDEO};
            }else{
                return new String[]{Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO};
            }
        }
        return new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
    }


    public interface GetAlbumCompatListener{
        void onGetAlbumResult(List<Uri> results);
    }

}
