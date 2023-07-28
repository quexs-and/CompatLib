package com.quexs.compatlib.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

import com.quexs.compatlib.util.DensityUtil;

import java.io.IOException;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2023/7/28
 * <p>
 * Time: 22:21
 * <p>
 * 备注：
 */
public class AutoVideoView extends VideoView {
    //最终的视频资源宽度
    private int mVideoWidth = 480;
    //最终视频资源高度
    private int mVideoHeight = 720;
    //视频资源原始宽度
    private int videoRealW = 1;
    //视频资源原始高度
    private int videoRealH = 1;


    private AutoVideoViewFrameAtTimeListener frameAtTimeListener;

    public AutoVideoView(Context context) {
        this(context, null);
    }

    public AutoVideoView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AutoVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

    }

    public void setFrameAtTimeListener(AutoVideoViewFrameAtTimeListener frameAtTimeListener) {
        this.frameAtTimeListener = frameAtTimeListener;
    }

    @Override
    public void setVideoPath(String path) {
        super.setVideoPath(path);
        MediaMetadataRetriever retr = null;
        try {
            retr = new MediaMetadataRetriever();
            retr.setDataSource(path);
            autoOrientation(retr);
        } catch (IllegalArgumentException e) {
            Log.e("----->" + "VideoView", "setVideoPath:" + e);
        }finally {
            try {
                if(retr != null){
                    retr.release();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setVideoURI(Uri uri) {
        super.setVideoURI(uri);
        MediaMetadataRetriever retr = null;
        try {
            retr = new MediaMetadataRetriever();
            retr.setDataSource(getContext(),uri);
            autoOrientation(retr);
        } catch (IllegalArgumentException e) {
            Log.e("----->" + "VideoView", "setVideoPath:" + e);
        }finally {
            try {
                if(retr != null){
                    retr.release();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void autoOrientation(MediaMetadataRetriever retr){
        String height = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);// 视频高度
        String width = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);// 视频宽度
        String orientation = retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);//旋转角度
        if(TextUtils.equals("90", orientation) || TextUtils.equals("270", orientation)){
            videoRealH = Integer.parseInt(width);
            videoRealW = Integer.parseInt(height);
        }else {
            videoRealW = Integer.parseInt(width);
            videoRealH = Integer.parseInt(height);
        }
        if(frameAtTimeListener != null){
            frameAtTimeListener.mGetFrameAtTime(retr.getFrameAtTime());
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        if (height > width) {
            //竖屏
            if (videoRealH > videoRealW) {
                //如果视频资源是竖屏
                //占满屏幕
                mVideoHeight = height;
                mVideoWidth = width;
            } else {
                //如果视频资源是横屏
                //宽度占满，高度保存比例
                mVideoWidth = width;
                float r = videoRealH * 1f / videoRealW;
                mVideoHeight = (int) (mVideoWidth * r);
            }
        } else {
            //横屏
            if (videoRealH > videoRealW) {
                //如果视频资源是竖屏
                //宽度占满，高度保存比例
                mVideoHeight = height;
                float r = videoRealW * 1f / videoRealH;
                mVideoWidth = (int) (mVideoHeight * r);

            } else {
                //如果视频资源是横屏
                //占满屏幕
                mVideoHeight = height;
                mVideoWidth = width;
            }
        }
        if (videoRealH == videoRealW && videoRealH == 1) {
            //没能获取到视频真实的宽高，自适应就可以了，什么也不用做
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(mVideoWidth, mVideoHeight);
        }
    }

    public int getVideoRealW() {
        return videoRealW;
    }

    public int getVideoRealH() {
        return videoRealH;
    }


    public interface AutoVideoViewFrameAtTimeListener{
        void mGetFrameAtTime(Bitmap bitmap);
    }
}
