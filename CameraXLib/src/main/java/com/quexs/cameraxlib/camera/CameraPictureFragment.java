package com.quexs.cameraxlib.camera;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.quexs.cameraxlib.R;
import com.quexs.cameraxlib.util.DensityUtil;
import com.quexs.cameraxlib.util.ViewTouchUtil;

import java.io.File;
import java.util.List;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2022/09/03
 * <p>
 * Time: 08:23
 * <p>
 * 备注：调用摄像头拍照 图库
 */
public class CameraPictureFragment extends Fragment {
    private TakeCameraFrgListener takeCameraFrgListener;
    private CameraPictureAdapter pictureAdapter;
    private ViewPager2 viewPager2;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        takeCameraFrgListener = (TakeCameraFrgListener) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPictureAdapter();
        initViewModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.camerax_lib_fragment_camera_picture, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager2 = view.findViewById(R.id.view_pager_2);
        ImageButton btnDelete = view.findViewById(R.id.compat_lib_delete);
        ImageButton btnCheck = view.findViewById(R.id.compat_lib_check);
        ImageButton btnBack = view.findViewById(R.id.compat_lib_back_button);
        float scale = DensityUtil.getScale(view.getContext());
        ConstraintLayout.LayoutParams clPager = (ConstraintLayout.LayoutParams) viewPager2.getLayoutParams();
        clPager.bottomMargin = DensityUtil.dpToPx(scale, 48);
        viewPager2.setLayoutParams(clPager);
        viewPager2.setPageTransformer(new MarginPageTransformer(DensityUtil.dpToPx(getContext(), 10)));
        viewPager2.setAdapter(pictureAdapter);

        //配置返回键大小
        ConstraintLayout.LayoutParams clBack = (ConstraintLayout.LayoutParams) btnBack.getLayoutParams();
        clBack.width = DensityUtil.dpToPx(scale, 32);
        clBack.height = DensityUtil.dpToPx(scale, 32);
        clBack.topMargin = DensityUtil.dpToPx(scale, 12);
        clBack.leftMargin = DensityUtil.dpToPx(scale, 16);
        btnBack.setLayoutParams(clBack);

        ConstraintLayout.LayoutParams clDelete = (ConstraintLayout.LayoutParams) btnDelete.getLayoutParams();
        clDelete.width = DensityUtil.dpToPx(scale, 32);
        clDelete.height = DensityUtil.dpToPx(scale, 32);
        btnDelete.setLayoutParams(clDelete);

        ConstraintLayout.LayoutParams clCheck = (ConstraintLayout.LayoutParams) btnCheck.getLayoutParams();
        clCheck.width = DensityUtil.dpToPx(scale, 32);
        clCheck.height = DensityUtil.dpToPx(scale, 32);
        btnCheck.setLayoutParams(clCheck);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ViewTouchUtil.isValidClick(v, 500)){
                    if(takeCameraFrgListener != null){
                        takeCameraFrgListener.onFragmentBackPressed();
                    }
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ViewTouchUtil.isValidClick(v, 500) && pictureAdapter.getItemCount() > 0){
                    pictureAdapter.removeItem(viewPager2.getCurrentItem());
                }
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ViewTouchUtil.isValidClick(v, 1000) && pictureAdapter.getItemCount() > 0){
                    File file = pictureAdapter.getItem(viewPager2.getCurrentItem());
                    Intent intent = new Intent();
                    intent.setData(Uri.fromFile(file));
                    if(takeCameraFrgListener != null){
                        takeCameraFrgListener.onFragmentBackResult(intent);
                    }
                }
            }
        });
    }

    private void initPictureAdapter(){
        pictureAdapter = new CameraPictureAdapter();
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



}