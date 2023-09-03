package com.quexs.cameraxlib.camera;

import android.Manifest;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.LifecycleCameraController;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.common.util.concurrent.ListenableFuture;
import com.quexs.cameraxlib.R;
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
 * Date: 2022/09/03
 * <p>
 * Time: 08:23
 * <p>
 * 备注：调用摄像头拍照
 */
public class TakeCameraFragment extends Fragment {

    private TakeCameraFrgListener takeCameraFrgListener;

    private ImageCapture imageCapture;
    private PreviewView viewFinder;
    private ImageButton btnBack,btnPhoto,btnCapture,btnCameraSwitch;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private boolean isFrontCamera;
    private ExecutorService executorService;
    private ProcessCameraProvider cameraProvider;
    private OrientationEventListener orientationEventListener;
    private TakeCameraViewModel viewModel;
    public TakeCameraFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        takeCameraFrgListener = (TakeCameraFrgListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initViewModel();
        initCameraPermissionsResult();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.camerax_lib_fragment_take_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewFinder = view.findViewById(R.id.view_finder);
        btnBack = view.findViewById(R.id.compat_lib_back_button);
        btnPhoto = view.findViewById(R.id.compat_lib_photo_view_button);
        btnCapture = view.findViewById(R.id.compat_lib_camera_capture_button);
        btnCameraSwitch = view.findViewById(R.id.compat_lib_camera_switch_button);
        //获取屏幕密度
        float scale = DensityUtil.getScale(view.getContext());
        //配置返回键大小
        ConstraintLayout.LayoutParams clBack = (ConstraintLayout.LayoutParams) btnBack.getLayoutParams();
        clBack.width = DensityUtil.dpToPx(scale, 32);
        clBack.height = DensityUtil.dpToPx(scale, 32);
        clBack.topMargin = DensityUtil.dpToPx(scale, 12);
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
        //初始化相机控制
        initCameraController(view.getContext());
        //初始化View监听
        initViewListener();
    }

    private void initViewListener(){
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ViewTouchUtil.isValidClick(v,500)){
                    if(takeCameraFrgListener != null){
                        takeCameraFrgListener.onFragmentBackPressed();
                    }
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
            public void onClick(View v) {
                if(ViewTouchUtil.isValidClick(v,1000)){
                    if(takeCameraFrgListener != null){
                        takeCameraFrgListener.onFragmentReplace(CameraPictureFragment.class.getName(), btnPhoto, "camerax_lib_picture");
                    }
                }
            }
        });
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

    /**
     * 初始化相机控制
     */
    private void initCameraController(Context context){
        LifecycleCameraController cameraController = new LifecycleCameraController(context);
        cameraController.bindToLifecycle(this);
        cameraController.setCameraSelector(CameraSelector.DEFAULT_BACK_CAMERA);
        viewFinder.setController(cameraController);
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
                    if(takeCameraFrgListener != null){
                        takeCameraFrgListener.onFragmentBackPressed();
                    }
                }
            }
        };
    }

    /**
     * 开始预览界面
     */
    private void startCameraPreview(){
        if(executorService == null){
            executorService = Executors.newSingleThreadExecutor();
        }
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                addPreview();
            }
        }, ContextCompat.getMainExecutor(getContext()));
    }

    /**
     * 随时持续更新设备旋转角度
     */
    private void initOrientationListener(){
        if(orientationEventListener == null){
            orientationEventListener = new OrientationEventListener(getContext()) {
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
    public void onStart() {
        super.onStart();
        if(viewModel != null){
            viewModel.onTaskGetPath();
        }
        if(orientationEventListener != null){
            orientationEventListener.enable();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(orientationEventListener != null){
            orientationEventListener.disable();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(cameraProvider != null){
            cameraProvider.unbindAll();
        }
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
        File file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), Calendar.getInstance().getTimeInMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).setMetadata(metadata).build();
        imageCapture.takePicture(outputFileOptions, executorService, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                if(outputFileResults.getSavedUri() != null){
                    btnPhoto.post(new ImageSavedRunnable(outputFileResults.getSavedUri()));
                }

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
            Glide.with(btnPhoto)
                    .load(imageUri)
                    .apply(new RequestOptions().circleCrop())
                    .into(btnPhoto);
            int padding = DensityUtil.dpToPx(btnPhoto.getContext(), 2.5f);
            btnPhoto.setPadding(padding,padding,padding,padding);
            if(takeCameraFrgListener != null){
                takeCameraFrgListener.onFragmentReplace(CameraPictureFragment.class.getName(), btnPhoto, "camerax_lib_picture");
            }
        }
    }


}