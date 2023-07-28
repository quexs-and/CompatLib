# CompatLib
优化中

## 引用依赖
Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies{
    //核心依赖
    implementation 'com.github.QuexSong:CompatLib:1.0.6'
    //调用摄像头拍照 添加camerax相关依赖 及 Glide依赖
    def camerax_version = "1.3.0-alpha04"
    implementation "androidx.camera:camera-core:${camerax_version}"
    implementation "androidx.camera:camera-camera2:${camerax_version}"
    implementation "androidx.camera:camera-lifecycle:${camerax_version}"
    implementation "androidx.camera:camera-video:${camerax_version}"
    implementation "androidx.camera:camera-view:${camerax_version}"
    //glide
    implementation 'com.github.bumptech.glide:glide:4.15.1'
}
```

## 调用方法 请参考Demo中 MediaActivity
注：需要在onCreate中创建兼容类
```java
public class MediaActivity extends AppCompatActivity {

    private ActivityMediaBinding binding;
    private GetContentCompat mGetContentCompat;
    private TakeCameraCompat mTakeCameraCompat;
    private TakeVideoCompat mTakeVideoCompat;
    private TakeCameraXCompat mTakeCameraXCompat;
    private ShareMediaCompat shareMediaCompat;
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
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        binding.recyclerView.setAdapter(mediaAdapter);
        List<String> list = new ArrayList<>();
        list.add("媒体库选取");
        list.add("系统相机拍照");
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
        shareMediaCompat = new ShareMediaCompat(this,this);

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
}
```