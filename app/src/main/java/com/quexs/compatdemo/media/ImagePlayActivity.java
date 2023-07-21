package com.quexs.compatdemo.media;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.quexs.compatdemo.R;
import com.quexs.compatdemo.databinding.ActivityImagePlayBinding;
import com.quexs.compatlib.compat.ShareMediaCompat;
import com.quexs.compatlib.util.ViewTouchUtil;

import java.io.IOException;

/**
 * 显示图片
 */
public class ImagePlayActivity extends AppCompatActivity {

    private ActivityImagePlayBinding binding;
    private ShareMediaCompat shareMediaCompat;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityImagePlayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initData();
        initCompat();
        initViewListener();
    }

    private void initData(){
        Intent intent = getIntent();
        imageUri = intent.getData();
        Glide.with(this).load(imageUri).into(binding.imvPicture);
    }

    private void initCompat(){
        shareMediaCompat = new ShareMediaCompat(this, this){
            @Override
            public void onPermissionDenied(String perm) {
                super.onPermissionDenied(perm);
                //此处处理未赋予权限问题
            }
        };

    }

    private void initViewListener(){
        binding.btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ViewTouchUtil.isValidClick(view, 500)){
                    shareMediaCompat.shareFile(imageUri, new ShareMediaCompat.ShareMediaCompatListener() {
                        @Override
                        public void shareStart() {
                            Log.d("分享", "shareStart");
                        }

                        @Override
                        public void shareError(IOException e) {
                            Log.d("分享", "shareError");
                        }

                        @Override
                        public void shareSuccess() {
                            Log.d("分享", "shareSuccess");
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}