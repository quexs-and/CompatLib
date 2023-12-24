package com.quexs.compatdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.quexs.compatdemo.databinding.ActivityMainBinding;
import com.quexs.compatdemo.media.MediaActivity;
import com.quexs.compatlib.task.AsyncTaskService;
import com.quexs.compatlib.util.DeviceUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ServiceConnection connection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initAdapter();

        Log.d("Device", "SDK=" + Build.VERSION.SDK_INT);
        Log.d("Device", "IPV4=" + DeviceUtil.getLocalIpAddress());
        Log.d("Device", "MAC=" + DeviceUtil.getLocalMacAddress());
        Log.d("Device", "IMEI=" + DeviceUtil.getLocalImei(this));
        initService();
    }

    private void initService(){
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(new Intent(this, AsyncTaskService.class),connection, Context.BIND_AUTO_CREATE);
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