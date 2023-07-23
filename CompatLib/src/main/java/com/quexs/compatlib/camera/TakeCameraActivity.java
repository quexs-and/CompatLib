package com.quexs.compatlib.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.LifecycleCameraController;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;

import com.google.common.util.concurrent.ListenableFuture;
import com.quexs.compatlib.compat.ScreenParamCompat;
import com.quexs.compatlib.databinding.ActivityTakeCameraBinding;
import com.quexs.compatlib.util.ViewTouchUtil;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2022/06/18
 * <p>
 * Time: 08:23
 * <p>
 * 备注：调用摄像头拍照
 */
public class TakeCameraActivity extends AppCompatActivity {

    private ActivityTakeCameraBinding binding;
    //屏幕方向监听
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    //是否前置摄像头
    private boolean isFrontCamera;
    private ImageCapture imageCapture;
    private ExecutorService executorService;
    private ProcessCameraProvider cameraProvider;

    private OrientationEventListener orientationEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTakeCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initStatusBarView();
        initCameraController();
        initOrientationListener();
        initViewListener();

    }

    private void initStatusBarView(){
        ConstraintLayout.LayoutParams cl = (ConstraintLayout.LayoutParams) binding.imvClose.getLayoutParams();
        cl.topMargin = new ScreenParamCompat(this).getStatusBarHeight() + 20;
        binding.imvClose.setLayoutParams(cl);
    }

    /**
     * 初始化相机控制
     */
    private void initCameraController(){
        executorService = Executors.newSingleThreadExecutor();
        LifecycleCameraController cameraController = new LifecycleCameraController(this);
        cameraController.bindToLifecycle(this);
        cameraController.setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA);
        binding.viewFinder.setController(cameraController);
    }

    /**
     * 随时持续更新设备旋转角度
     */
    private void initOrientationListener(){
        orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int i) {
                if (i == -1) {
                    return;
                }
                int rotation;
                if(i >= 45 && i < 135){
                    rotation = Surface.ROTATION_270;
                }else if(i >= 135 && i < 225){
                    rotation = Surface.ROTATION_180;
                }else if(i >= 225 && i < 315){
                    rotation = Surface.ROTATION_90;
                }else {
                    rotation = Surface.ROTATION_0;
                }
                if(imageCapture != null){
                    imageCapture.setTargetRotation(rotation);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        orientationEventListener.enable();
        startCameraPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        orientationEventListener.disable();
        cameraProvider.unbindAll();
    }

    /**
     * 开始预览界面
     */
    private void startCameraPreview(){
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                addPreview();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void initViewListener(){
        binding.imvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ViewTouchUtil.isValidClick(v,500)){
                    onBackPressed();
                }
            }
        });
        //单击拍照
        binding.recordView.setOnSingleClickListener(new CircleProgressButtonView.OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                if(ViewTouchUtil.isValidClick(view,500)){
                    onTakeCamera();
                }
            }
        });

        binding.imvSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ViewTouchUtil.isValidClick(v,500)){
                    isFrontCamera = !isFrontCamera;
                    startCameraPreview();
                }
            }
        });
    }

    private void addPreview(){
        try {
            cameraProvider = cameraProviderFuture.get();
            //1 定义图像预览接口
            Preview preview = new Preview.Builder().build();
            //2 拍照 接口
            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)//优化捕获速度，可能降低图片质量
                    .build();
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(isFrontCamera ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK)
                    .build();
            cameraProvider.unbindAll();
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            //5 把相机信息高速预览窗口
            preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void onTakeCamera(){
        ImageCapture.Metadata metadata = new ImageCapture.Metadata();
        metadata.setReversedHorizontal(isFrontCamera);
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), Calendar.getInstance().getTimeInMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).setMetadata(metadata).build();
        imageCapture.takePicture(outputFileOptions, executorService, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Intent intent = new Intent();
                intent.setData(outputFileResults.getSavedUri());
                setResult(Activity.RESULT_OK, intent);
                binding.recordView.post(new Runnable() {
                    @Override
                    public void run() {
                        onBackPressed();
                    }
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}