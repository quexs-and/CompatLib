package com.quexs.compatdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.quexs.compatdemo.databinding.ActivityMainBinding;
import com.quexs.compatdemo.media.MediaActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initAdapter();
        Log.d("测试16进制值", "" + 0x66);
    }

    private void initAdapter(){
        MainAdapter mainAdapter = new MainAdapter(new MainAdapter.MainAdapterListener() {
            @Override
            public void onClickItem(String mainName) {
                switchCompat(mainName);
            }
        });
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this,2));
        binding.recyclerView.setAdapter(mainAdapter);

        List<String> list = new ArrayList<>();
        list.add("媒体库兼容");
        mainAdapter.addItems(list);
    }

    private void switchCompat(String mainName){
        switch (mainName){
            case "媒体库兼容":
                startActivity(new Intent(this, MediaActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}