package com.quexs.compatdemo.media;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.quexs.compatdemo.databinding.ActivityVideoPlayBinding;
import com.quexs.compatlib.view.AutoVideoView;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2023/7/28
 * <p>
 * Time: 22:07
 * <p>
 * 备注：视频播放
 */
public class VideoPlayActivity extends AppCompatActivity {

   private ActivityVideoPlayBinding binding;
   private Uri videoUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVideoPlayBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initData();
        initViewListener();
        loadVideo();
    }

    private void initData(){
        Intent intent = getIntent();
        if(intent != null){
            videoUri = intent.getData();
        }
    }

    private void initViewListener(){
        binding.videoView.setFrameAtTimeListener(new AutoVideoView.AutoVideoViewFrameAtTimeListener() {
            @Override
            public void mGetFrameAtTime(Bitmap bitmap) {
                binding.imvThumbnail.setImageBitmap(bitmap);
            }
        });
        binding.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                //视频加载完成
                binding.btnStart.setVisibility(View.VISIBLE);
            }
        });
        binding.videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //视频播放完成
                binding.btnStart.setVisibility(View.VISIBLE);
                binding.imvThumbnail.setVisibility(View.VISIBLE);
            }
        });
        binding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(binding.videoView.isPlaying()){
                    binding.videoView.pause();
                    binding.btnStart.setVisibility(View.VISIBLE);
                }else {
                    binding.videoView.start();
                    binding.btnStart.setVisibility(View.GONE);
                    binding.imvThumbnail.setVisibility(View.GONE);
                }
            }
        });
    }

    private void loadVideo(){
        binding.videoView.setVideoURI(videoUri);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }




}
