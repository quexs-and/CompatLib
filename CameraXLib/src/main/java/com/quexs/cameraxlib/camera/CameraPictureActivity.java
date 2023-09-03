package com.quexs.cameraxlib.camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.quexs.cameraxlib.R;
import com.quexs.cameraxlib.compat.ScreenParamCompat;
import com.quexs.cameraxlib.util.DensityUtil;
import com.quexs.cameraxlib.util.ViewTouchUtil;

import java.io.File;
import java.util.List;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2022/06/18
 * <p>
 * Time: 08:23
 * <p>
 * 备注：调用摄像头拍照 图片显示
 */
public class CameraPictureActivity extends AppCompatActivity {
    private CameraPictureAdapter pictureAdapter;
    private ViewPager2 viewPager2;
    private ImageButton btnDelete,btnCheck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerax_lib_activity_camera_picture);
        initViewId();
        initAdapter();
        initViewModel();
        initViewListener();
    }

    private void initViewId(){
        viewPager2 = findViewById(R.id.view_pager_2);
        btnDelete = findViewById(R.id.compat_lib_delete);
        btnCheck = findViewById(R.id.compat_lib_check);
        float scale = DensityUtil.getScale(this);
        ConstraintLayout.LayoutParams clPager = (ConstraintLayout.LayoutParams) viewPager2.getLayoutParams();
        clPager.bottomMargin = DensityUtil.dpToPx(scale, 48);
        viewPager2.setLayoutParams(clPager);

        ConstraintLayout.LayoutParams clDelete = (ConstraintLayout.LayoutParams) btnDelete.getLayoutParams();
        clDelete.width = DensityUtil.dpToPx(scale, 32);
        clDelete.height = DensityUtil.dpToPx(scale, 32);
        btnDelete.setLayoutParams(clDelete);

        ConstraintLayout.LayoutParams clCheck = (ConstraintLayout.LayoutParams) btnCheck.getLayoutParams();
        clCheck.width = DensityUtil.dpToPx(scale, 32);
        clCheck.height = DensityUtil.dpToPx(scale, 32);
        btnCheck.setLayoutParams(clCheck);

    }

    private void initAdapter(){
        pictureAdapter = new CameraPictureAdapter();
        viewPager2.setPageTransformer(new MarginPageTransformer(DensityUtil.dpToPx(this, 10)));
        viewPager2.setAdapter(pictureAdapter);
    }

    private void initViewModel(){
        CameraPictureViewModel viewModel = new ViewModelProvider(this).get(CameraPictureViewModel.class);
        viewModel.getFileListData().observe(this, new Observer<List<File>>() {
            @Override
            public void onChanged(List<File> files) {
                pictureAdapter.addItems(files);
            }
        });
    }

    private void initViewListener(){
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ViewTouchUtil.isValidClick(v, 500)){
                    pictureAdapter.removeItem(viewPager2.getCurrentItem());
                }
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ViewTouchUtil.isValidClick(v, 1000)){
                    File file = pictureAdapter.getItem(viewPager2.getCurrentItem());
                    Intent intent = new Intent();
                    intent.setData(Uri.fromFile(file));
                    setResult(Activity.RESULT_OK, intent);
                    onBackPressed();
                }
            }
        });
    }

}