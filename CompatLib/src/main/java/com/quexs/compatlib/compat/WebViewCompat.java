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

/**
 * WebView 兼容工具类
 */
public class WebViewCompat {
    private final GetContentCompat mGetContentCompat;
    private final TakeCameraCompat mTakeCameraCompat;
    private final TakeVideoCompat mTakeVideoCompat;

    private WebViewCompatProgressListener mWebViewCompatProgressListener;

    public WebViewCompat(WebView webView, ActivityResultCaller resultCaller){
        mGetContentCompat = oGetContentCompat(resultCaller);
        mTakeCameraCompat = oTakeCameraCompat(resultCaller);
        mTakeVideoCompat = oTakeVideoCompat(resultCaller);
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

    private TakeVideoCompat oTakeVideoCompat(ActivityResultCaller resultCaller){
        return new TakeVideoCompat(resultCaller){
            @Override
            public void onPermissionsDenied(List<String> perms) {
                super.onPermissionsDenied(perms);
                onPermissionsDeniedForTakeVideo(perms);
            }
        };
    }

    private TakeCameraCompat oTakeCameraCompat(ActivityResultCaller resultCaller){
        return new TakeCameraCompat(resultCaller){
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
                        if("image/*".equals(accept)){
                            mTakeCameraCompat.takeCamera(new TakeCameraCompat.TakeCameraCompatListener() {
                                @Override
                                public void onResult(Uri uri) {
                                    filePathCallback.onReceiveValue(uri != null ? new Uri[]{uri} : null);
                                }
                            });
                        }else if("video/*".equals(accept)){
                            mTakeVideoCompat.takeVideo(new TakeVideoCompat.TakeVideoCompatListener() {
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
