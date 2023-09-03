package com.quexs.cameraxlib.camera;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.util.concurrent.ListenableFuture;
import com.quexs.cameraxlib.R;
import com.quexs.cameraxlib.compat.ScreenParamCompat;
import com.quexs.cameraxlib.util.DensityUtil;
import com.quexs.cameraxlib.util.ViewTouchUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
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

    //屏幕方向监听
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    //是否前置摄像头
    private boolean isFrontCamera;
    private ImageCapture imageCapture;
    private ExecutorService executorService;
    private ProcessCameraProvider cameraProvider;
    private OrientationEventListener orientationEventListener;
    private PreviewView viewFinder;
    private ImageButton btnBack,btnPhoto,btnCapture,btnCameraSwitch;
    private TakeCameraViewModel viewModel;
    private ActivityResultLauncher<Intent> pictureResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerax_lib_activity_take_camera);
        initViewId();
        initViewModel();
        initRegisterForActivityResult();
        initCameraController();
        initViewListener();
        initCameraPermissionsResult();
    }


    private void initViewId(){
        viewFinder = findViewById(R.id.view_finder);
        btnBack = findViewById(R.id.compat_lib_back_button);
        btnPhoto = findViewById(R.id.compat_lib_photo_view_button);
        btnCapture = findViewById(R.id.compat_lib_camera_capture_button);
        btnCameraSwitch = findViewById(R.id.compat_lib_camera_switch_button);
        //获取屏幕密度
        float scale = DensityUtil.getScale(this);
        //配置返回键大小
        ConstraintLayout.LayoutParams clBack = (ConstraintLayout.LayoutParams) btnBack.getLayoutParams();
        clBack.width = DensityUtil.dpToPx(scale, 32);
        clBack.height = DensityUtil.dpToPx(scale, 32);
        clBack.topMargin = new ScreenParamCompat(this).getStatusBarHeight() + DensityUtil.dpToPx(scale, 10);
        clBack.leftMargin = DensityUtil.dpToPx(scale, 16);
        btnBack.setLayoutParams(clBack);
        //配置相册控件大小
        ConstraintLayout.LayoutParams clPhoto = (ConstraintLayout.LayoutParams) btnPhoto.getLayoutParams();
        clPhoto.width = DensityUtil.dpToPx(scale, 64);
        clPhoto.height = DensityUtil.dpToPx(scale, 64);
        clPhoto.bottomMargin = DensityUtil.dpToPx(scale, 92);
        clPhoto.leftMargin = DensityUtil.dpToPx(scale, 43);
        btnPhoto.setLayoutParams(clPhoto);
        int clPhotoPadding = DensityUtil.dpToPx(scale, 16);
        btnPhoto.setPadding(clPhotoPadding,clPhotoPadding,clPhotoPadding,clPhotoPadding);
        //配置拍照控件大小
        ConstraintLayout.LayoutParams clCapture = (ConstraintLayout.LayoutParams) btnCapture.getLayoutParams();
        clCapture.width = DensityUtil.dpToPx(scale, 92);
        clCapture.height = DensityUtil.dpToPx(scale, 92);
        clCapture.bottomMargin = DensityUtil.dpToPx(scale, 80);
        btnCapture.setLayoutParams(clCapture);
        //配置摄像头切换控件大小
        ConstraintLayout.LayoutParams clCameraSwitch = (ConstraintLayout.LayoutParams) btnCameraSwitch.getLayoutParams();
        clCameraSwitch.width = DensityUtil.dpToPx(scale, 64);
        clCameraSwitch.height = DensityUtil.dpToPx(scale, 64);
        clCameraSwitch.bottomMargin = DensityUtil.dpToPx(scale, 92);
        clCameraSwitch.rightMargin = DensityUtil.dpToPx(scale, 32);
        btnCameraSwitch.setLayoutParams(clCameraSwitch);
        int clCameraSwitchPadding = DensityUtil.dpToPx(scale, 4);
        btnCameraSwitch.setPadding(clCameraSwitchPadding,clCameraSwitchPadding,clCameraSwitchPadding,clCameraSwitchPadding);
    }

    private void initViewModel(){
        viewModel = new ViewModelProvider(this).get(TakeCameraViewModel.class);
        viewModel.getPathData().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Glide.with(btnPhoto)
                        .load(s)
                        .apply(new RequestOptions().circleCrop())
                        .into(btnPhoto);
                int padding = DensityUtil.dpToPx(btnPhoto.getContext(), 2.5f);
                btnPhoto.setPadding(padding,padding,padding,padding);
            }
        });
    }

    private void initRegisterForActivityResult(){
        pictureResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    setResult(Activity.RESULT_OK, result.getData());
                    onBackPressed();
                } else {
                    viewModel.onTaskGetPath();
                }
            }
        });
    }

    /**
     * 初始化相机控制
     */
    private void initCameraController(){
        executorService = Executors.newSingleThreadExecutor();
        LifecycleCameraController cameraController = new LifecycleCameraController(this);
        cameraController.bindToLifecycle(this);
        cameraController.setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA);
        viewFinder.setController(cameraController);
    }

    private void initViewListener(){
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ViewTouchUtil.isValidClick(v,500)){
                    onBackPressed();
                }
            }
        });
        //单击拍照
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ViewTouchUtil.isValidClick(v,500)){
                    onTakeCamera();
                }
            }
        });
        //切换摄像头
        btnCameraSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ViewTouchUtil.isValidClick(v,500)){
                    isFrontCamera = !isFrontCamera;
                    startCameraPreview();
                }
            }
        });
        //点击查看图片
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TakeCameraActivity.this, CameraPictureActivity.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(TakeCameraActivity.this, btnPhoto, "camerax_lib_picture");
                pictureResult.launch(intent, optionsCompat);
            }
        });
    }

    private void initCameraPermissionsResult(){
        ActivityResultLauncher<String[]> cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), takePermCallback());
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            cameraPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
        }else {
            cameraPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }
    }

    private ActivityResultCallback<Map<String, Boolean>> takePermCallback(){
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
                    startCameraPreview();
                }else {
                    //有权限被拒绝
                    onBackPressed();
                }
            }
        };
    }

    /**
     * 随时持续更新设备旋转角度
     */
    private void initOrientationListener(){
        if(orientationEventListener == null){
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(orientationEventListener != null){
            orientationEventListener.enable();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(orientationEventListener != null){
            orientationEventListener.disable();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraProvider != null){
            cameraProvider.unbindAll();
        }
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

    private void addPreview(){
        try {
            cameraProvider = cameraProviderFuture.get();
            //1 定义图像预览接口
            Preview preview = new Preview.Builder().build();
            //2 拍照 接口
            imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)//优化捕获速度，可能降低图片质量
                    .setTargetRotation(viewFinder.getDisplay().getRotation())
                    .build();
            CameraSelector cameraSelector = new CameraSelector.Builder()
                    .requireLensFacing(isFrontCamera ? CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK)
                    .build();
            //3 移除已绑定的视图
            cameraProvider.unbindAll();
            //4 绑定视图
            cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            //5 把相机信息高速预览窗口
            preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
            //6 监听手机旋转方向
            initOrientationListener();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void onTakeCamera(){
        ImageCapture.Metadata metadata = new ImageCapture.Metadata();
        // 前置摄像头需要水平镜像
        metadata.setReversedHorizontal(isFrontCamera);
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), Calendar.getInstance().getTimeInMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).setMetadata(metadata).build();
        imageCapture.takePicture(outputFileOptions, executorService, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                  runOnUiThread(new ImageSavedRunnable(outputFileResults.getSavedUri()));
//                Intent intent = new Intent();
//                intent.setData(outputFileResults.getSavedUri());
//                setResult(Activity.RESULT_OK, intent);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        onBackPressed();
//                    }
//                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                exception.printStackTrace();
            }
        });
    }

    private class ImageSavedRunnable implements Runnable{
        private final Uri imageUri;
        public ImageSavedRunnable(Uri uri){
            this.imageUri = uri;
        }

        @Override
        public void run() {
            if(imageUri != null){
                Glide.with(btnPhoto)
                        .load(imageUri)
                        .apply(new RequestOptions().circleCrop())
                        .into(btnPhoto);
                int padding = DensityUtil.dpToPx(btnPhoto.getContext(), 2.5f);
                btnPhoto.setPadding(padding,padding,padding,padding);

                Intent intent = new Intent(TakeCameraActivity.this, CameraPictureActivity.class);
                ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(TakeCameraActivity.this, btnPhoto, "camerax_lib_picture");
                pictureResult.launch(intent, optionsCompat);
            }
        }
    }

}