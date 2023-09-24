package com.quexs.compatlib.compat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.result.ActivityResultCaller;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2022/02/15
 * <p>
 * Time: 3:27
 * <p>
 * 备注：WebView 兼容工具类
 */
public class WebViewCompat {
    private final GetContentCompat mGetContentCompat;
    private final TakeCameraAlbumCompat mTakeCameraAlbumCompat;
    private final TakeVideoAlbumCompat mTakeVideoAlbumCompat;

    private WebViewCompatProgressListener mWebViewCompatProgressListener;

    public WebViewCompat(WebView webView, ActivityResultCaller resultCaller){
        mGetContentCompat = oGetContentCompat(resultCaller);
        mTakeCameraAlbumCompat = oTakeCameraCompat(resultCaller);
        mTakeVideoAlbumCompat = oTakeVideoCompat(resultCaller);
        initWebViewConfig(webView);
        initWebViewListener(webView);
    }

    /**
     * 文件管理器未授予权限
     * @param perms
     */
    public void onPermissionsDeniedForGetContent(List<String> perms){

    }

    /**
     * 拍照未授予权限
     * @param perms
     */
    public void onPermissionsDeniedForTakeCamera(List<String> perms){

    }

    /**
     * 录制视频未授予权限
     * @param perms
     */
    public void onPermissionsDeniedForTakeVideo(List<String> perms){

    }

    /**
     * 设置WebView 加载网页进度监听
     * @param webViewCompatProgressListener
     */
    public void setWebViewCompatProgressListener(WebViewCompatProgressListener webViewCompatProgressListener) {
        this.mWebViewCompatProgressListener = webViewCompatProgressListener;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void initWebViewConfig(WebView webView){
        WebSettings webSettings = webView.getSettings();
        //支持JavaScript
        webSettings.setJavaScriptEnabled(true);
        //允许自动打开弹窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        //推荐使用的窗口，Html界面自适应屏幕
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setDomStorageEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAllowFileAccess(true);
        //支持自动加载图片
        webSettings.setLoadsImagesAutomatically(true);
        //保存表单数据
        webSettings.setSaveFormData(true);
        webSettings.setDefaultTextEncodingName("utf-8");
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
    }

    private void initWebViewListener(WebView webView){
        webView.setWebChromeClient(oWebChromeClient());
        webView.setWebViewClient(oWebViewClient());
    }

    private TakeVideoAlbumCompat oTakeVideoCompat(ActivityResultCaller resultCaller){
        return new TakeVideoAlbumCompat(resultCaller){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);
                onPermissionsDeniedForTakeVideo(perms);
            }
        };
    }

    private TakeCameraAlbumCompat oTakeCameraCompat(ActivityResultCaller resultCaller){
        return new TakeCameraAlbumCompat(resultCaller){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);
                onPermissionsDeniedForTakeCamera(perms);
            }
        };
    }

    private GetContentCompat oGetContentCompat(ActivityResultCaller resultCaller){
        return new GetContentCompat(resultCaller) {
            @Override
            public void onPermissionsDenied(List<String> perms) {
                onPermissionsDeniedForGetContent(perms);
            }
        };
    }

    private WebChromeClient oWebChromeClient(){
        return new WebChromeClient(){

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if(mWebViewCompatProgressListener != null){
                    mWebViewCompatProgressListener.onProgressChanged(view,newProgress);
                }
            }


            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if(fileChooserParams.isCaptureEnabled()){
                        //希望捕获方式获取照片-即拍照或者录制视频（H5样式：<input type="file" accept="image/*" capture>）
                        String accept = fileChooserParams.getAcceptTypes()[0];
                        if(Pattern.compile("image/*").matcher(accept).find()){
                            mTakeCameraAlbumCompat.takeCamera(new TakeCameraAlbumCompat.TakeCameraCompatListener() {
                                @Override
                                public void onResult(Uri uri) {
                                    filePathCallback.onReceiveValue(uri != null ? new Uri[]{uri} : null);
                                }
                            });
                        }else if(Pattern.compile("video/*").matcher(accept).find()){
                            mTakeVideoAlbumCompat.takeVideo(new TakeVideoAlbumCompat.TakeVideoCompatListener() {
                                @Override
                                public void onResult(Intent result) {
                                    filePathCallback.onReceiveValue(result != null && result.getData() != null ? new Uri[]{result.getData()} : null);
                                }
                            });
                        }
                    }else {
                        //从文件管理器获取文件
                        mGetContentCompat.openContent(1, new GetContentCompat.GetContentCompatListener() {
                            @Override
                            public void onGetContentResult(List<Uri> results) {
                                filePathCallback.onReceiveValue(results != null ? new Uri[]{results.get(0)} : null);
                            }
                        }, fileChooserParams.getAcceptTypes());
                    }
                }
                return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
            }
        };
    }

    private WebViewClient oWebViewClient(){
        return new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if(mWebViewCompatProgressListener != null){
                    mWebViewCompatProgressListener.onPageStarted(view,url,favicon);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(mWebViewCompatProgressListener != null){
                    mWebViewCompatProgressListener.onPageFinished(view,url);
                }
            }
        };
    }

    public interface WebViewCompatProgressListener{
        void onPageStarted(WebView view, String url, Bitmap favicon);
        void onProgressChanged(WebView view, int newProgress);
        void onPageFinished(WebView view, String url);
    }


}
