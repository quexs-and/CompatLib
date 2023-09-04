package com.quexs.compatdemo.media;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.quexs.cameraxlib.compat.TakeCameraXCompat;
import com.quexs.compatdemo.databinding.ActivityMediaBinding;
import com.quexs.compatlib.compat.GetContentCompat;
import com.quexs.compatlib.compat.ShareMediaCompat;
import com.quexs.compatlib.compat.TakeCameraCompat;
import com.quexs.compatlib.compat.TakeCameraSMCompat;
import com.quexs.compatlib.compat.TakeVideoCompat;
import com.quexs.compatlib.util.ViewTouchUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 多媒体功能兼容类测试
 */
public class MediaActivity extends AppCompatActivity {

    private ActivityMediaBinding binding;
    private GetContentCompat mGetContentCompat;
    private TakeCameraCompat mTakeCameraCompat;
    private TakeVideoCompat mTakeVideoCompat;
    private TakeCameraXCompat mTakeCameraXCompat;
    private ShareMediaCompat shareMediaCompat;
    private TakeCameraSMCompat mTakeCameraSMCompat;
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
        list.add("媒体库选取");
        list.add("系统相机拍照");
        list.add("系统相机拍照-严格模式");
        list.add("系统相机视频");
        list.add("摄像头拍照");
        mediaAdapter.addItems(list);
    }

    private void initCompat(){
        //调用媒体库
        mGetContentCompat = new GetContentCompat(this){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);
                //此处处理未赋予权限问题
            }
        };
        //调用相机拍照
        mTakeCameraCompat = new TakeCameraCompat(this){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);
                //此处处理未赋予权限问题
            }
        };
        //调用相机录制视频
        mTakeVideoCompat = new TakeVideoCompat(this){
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
        //分享到媒体库
        shareMediaCompat = new ShareMediaCompat(this,this){
            @Override
            public void onPermissionDenied(String perm) {
                super.onPermissionDenied(perm);
                //此处处理未赋予权限问题
            }
        };
        //调用系统相机拍照严格模式
        mTakeCameraSMCompat = new TakeCameraSMCompat(this){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);

            }
        };

    }

    private void onClickCompat(View view, String mediaName){
        if(!ViewTouchUtil.isValidClick(view, 500)) return;
        switch (mediaName) {
            case "媒体库选取" ->
                    mGetContentCompat.openContent(1, new GetContentCompat.GetContentCompatListener() {
                        @Override
                        public void onGetContentResult(List<Uri> results) {
                            if (results != null && results.size() > 0) {
                                Intent intent = new Intent(MediaActivity.this, ImagePlayActivity.class);
                                intent.setData(results.get(0));
                                startActivity(intent);
                            }
                        }
                    }, GetContentCompat.MineType.IMAGE);
            case "系统相机拍照" ->
                    mTakeCameraCompat.takeCamera(new TakeCameraCompat.TakeCameraCompatListener() {
                        @Override
                        public void onResult(Uri uri) {
                            if (uri != null) {
                                Log.d("回调结果", "" + uri);
                                Intent intent = new Intent(MediaActivity.this, ImagePlayActivity.class);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }
                    });
            case "系统相机拍照-严格模式" ->
                    mTakeCameraSMCompat.takeCamera(new TakeCameraSMCompat.TakeCameraSMCompatListener() {
                        @Override
                        public void onResult(Uri uri) {
                            if (uri != null) {
                                Log.d("回调结果", "" + uri);
                                Intent intent = new Intent(MediaActivity.this, ImagePlayActivity.class);
                                intent.setData(uri);
                                startActivity(intent);
                            }
                        }
                    });
            case "系统相机视频" ->
                    mTakeVideoCompat.takeVideo(new TakeVideoCompat.TakeVideoCompatListener() {
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