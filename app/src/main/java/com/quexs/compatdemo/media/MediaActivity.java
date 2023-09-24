package com.quexs.compatdemo.media;


import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.quexs.cameraxlib.compat.TakeCameraXCompat;
import com.quexs.compatdemo.databinding.ActivityMediaBinding;
import com.quexs.compatlib.base.CompatLibActivity;
import com.quexs.compatlib.compat.GetContentCompat;
import com.quexs.compatlib.compat.GetAlbumCompat;
import com.quexs.compatlib.compat.TakeCameraAlbumCompat;
import com.quexs.compatlib.compat.TakeCameraCompat;
import com.quexs.compatlib.compat.TakeVideoAlbumCompat;
import com.quexs.compatlib.compat.TakeVideoCompat;
import com.quexs.compatlib.util.ViewTouchUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 多媒体功能兼容类测试
 */
public class MediaActivity extends CompatLibActivity {

    private ActivityMediaBinding binding;
    private GetContentCompat mGetContentCompat;
    private GetAlbumCompat mGetAlbumCompat;
    private TakeCameraCompat mTakeCameraCompat;
    private TakeCameraAlbumCompat mTakeCameraAlbumCompat;
    private TakeVideoCompat mTakeVideoCompat;
    private TakeVideoAlbumCompat mTakeVideoAlbumCompat;
    private TakeCameraXCompat mTakeCameraXCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMediaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initAdapter();
        initCompat();
    }

    private void initAdapter(){
        MediaAdapter mediaAdapter = new MediaAdapter(new MediaAdapter.MediaAdapterListener() {
            @Override
            public void onClickItem(View view,String mediaName) {
                onClickCompat(view, mediaName);
            }
        });
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(mediaAdapter);
        List<String> list = new ArrayList<>();
        list.add("相册选取");
        list.add("系统相机拍照");
        list.add("系统相机拍照并共享到相册");
        list.add("系统相机录制视频");
        list.add("系统相机录制视频并共享到相册");
        list.add("摄像头拍照");
        mediaAdapter.addItems(list);
    }

    private void initCompat(){
        //调用媒体库
        mGetAlbumCompat = new GetAlbumCompat(this){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);
            }
        };
        //调用文件管理器
        mGetContentCompat = new GetContentCompat(this){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);
                //此处处理未赋予权限问题
            }
        };
        //调用系统相机
        mTakeCameraCompat = new TakeCameraCompat(this){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);
                //此处处理未赋予权限问题
            }
        };
        //调用相机拍照并共享到相册
        mTakeCameraAlbumCompat = new TakeCameraAlbumCompat(this){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);
                //此处处理未赋予权限问题
            }
        };
        //调用相机录制视频并保存到相册
        mTakeVideoCompat = new TakeVideoCompat(this){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);
                //此处处理未赋予权限问题
            }
        };
        //调用相机录制视频并保存到相册
        mTakeVideoAlbumCompat = new TakeVideoAlbumCompat(this){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);
                //此处处理未赋予权限问题
            }
        };
        //调用摄像头拍照
        mTakeCameraXCompat = new TakeCameraXCompat(this){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);
                //此处处理未赋予权限问题
            }
        };
    }

    private void onClickCompat(View view, String mediaName){
        if(!ViewTouchUtil.isValidClick(view, 500)) return;
        switch (mediaName) {
            case "相册选取" ->
                    mGetAlbumCompat.openAlbum(3, new GetAlbumCompat.GetAlbumCompatListener() {
                        @Override
                        public void onGetAlbumResult(List<Uri> results) {
                            if (results != null) {
                                Intent intent = new Intent(MediaActivity.this, ImagePlayActivity.class);
                                intent.setData(results.get(0));
                                startActivity(intent);
                            }
                        }
                    }, "image/*,video/*", "image/jpeg", "video/3gp");
            case "系统相机拍照" ->
                    mTakeCameraCompat.takeCamera(new TakeCameraCompat.TakeCameraCompatListener() {
                        @Override
                        public void onResult(Uri uri) {
                            if (uri != null) {
                                Intent intent = new Intent(MediaActivity.this, ImagePlayActivity.class);
                                intent.putExtra("share", true);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }
                    });
            case "系统相机拍照并共享到相册" ->
                    mTakeCameraAlbumCompat.takeCamera(new TakeCameraAlbumCompat.TakeCameraCompatListener() {
                        @Override
                        public void onResult(Uri uri) {
                            if (uri != null) {
                                Intent intent = new Intent(MediaActivity.this, ImagePlayActivity.class);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }
                    });
            case "系统相机录制视频" ->
                    mTakeVideoCompat.takeVideo(new TakeVideoCompat.TakeVideoCompatListener() {
                        @Override
                        public void onResult(Intent result) {
                            if(result != null){
                                Uri VideoUri = result.getData();
                                if (VideoUri != null) {
                                    Intent intent = new Intent(MediaActivity.this, VideoPlayActivity.class);
                                    intent.putExtra("share", true);
                                    intent.setData(VideoUri);
                                    startActivity(intent);
                                }
                            }
                        }
                    });
            case "系统相机录制视频并共享到相册" ->
                    mTakeVideoAlbumCompat.takeVideo(new TakeVideoAlbumCompat.TakeVideoCompatListener() {
                        @Override
                        public void onResult(Intent result) {
                            if(result != null){
                                Uri VideoUri = result.getData();
                                if (VideoUri != null) {
                                    Intent intent = new Intent(MediaActivity.this, VideoPlayActivity.class);
                                    intent.setData(VideoUri);
                                    startActivity(intent);
                                }
                            }
                        }
                    });
            case "摄像头拍照"->
                    mTakeCameraXCompat.takeCamera(new TakeCameraXCompat.TakeCameraXCompatListener() {
                        @Override
                        public void onResult(Uri uri) {
                            if (uri != null) {
                                Intent intent = new Intent(MediaActivity.this, ImagePlayActivity.class);
                                intent.setData(uri);
                                intent.putExtra("share", true);
                                startActivity(intent);
                            }
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}