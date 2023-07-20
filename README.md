# CompatLib
优化中

## 引用依赖
Gradle
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies{
    implementation 'com.github.QuexSong:CompatLib:1.0.2'
}
```

## 调用媒体库获取图片/视频/音频文件
注：需要在onCreate中创建兼容类
```java
public class MediaActivity extends AppCompatActivity {

    private ActivityMediaBinding binding;
    private GetContentCompat mGetContentCompat;
    private TakeCameraCompat mTakeCameraCompat;
    private TakeVideoCompat mTakeVideoCompat;

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
            public void onClickItem(String mediaName) {
                onClickCompat(mediaName);
            }
        });
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        binding.recyclerView.setAdapter(mediaAdapter);
        List<String> list = new ArrayList<>();
        list.add("多媒体库文件");
        list.add("系统相机拍照");
        list.add("系统相机视频");
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

    }

    private void onClickCompat(String mediaName){
        Log.d("点击兼容类", "" + mediaName);
        switch (mediaName){
            case "多媒体库文件":
                mGetContentCompat.openContent(1, new GetContentCompat.GetContentCompatListener() {
                    @Override
                    public void onGetContentResult(List<Uri> results) {
                        if(results != null && results.size() > 0){
                            Log.d("回调结果", "" + results.get(0));
                        }
                    }
                },GetContentCompat.MineType.IMAGE);
                break;
            case "系统相机拍照":
                mTakeCameraCompat.takeCamera(new TakeCameraCompat.TakeCameraCompatListener() {
                    @Override
                    public void onResult(Uri uri) {
                        if(uri != null){
                            Log.d("回调结果", "" + uri);
                        }
                    }
                });
                break;
            case "系统相机视频":
                mTakeVideoCompat.takeVideo(new TakeVideoCompat.TakeVideoCompatListener() {
                    @Override
                    public void onResult(Intent result) {
                        if(result != null){
                            Log.d("回调结果", "" + result)
                        }
                    }
                });
                break;
        }
    }

}
```