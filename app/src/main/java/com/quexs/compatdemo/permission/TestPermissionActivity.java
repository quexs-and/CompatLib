package com.quexs.compatdemo.permission;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.quexs.compatdemo.R;
import com.quexs.compatdemo.databinding.ActivityTestPermissionBinding;
import com.quexs.compatlib.perm.PermissionsCompat;

/**
 * 测试权限
 */
public class TestPermissionActivity extends AppCompatActivity {

    private ActivityTestPermissionBinding binding;
    private PermissionsCompat permissionsCompat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTestPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        permissionsCompat = new PermissionsCompat(this);
        initViewListener();
    }

    private void initViewListener(){
        binding.btnPermission1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionsCompat.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA});
            }
        });

        binding.btnPermission2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionsCompat.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, new PermissionsCompat.PermissionsCompatListener() {
                    @Override
                    public void permissionRequestBefore(PermissionsCompat.Builder builder) {
                        Log.d("requestPermissions", "permissionRequestBefore");
                    }

                    @Override
                    public void permissionRequestResult(PermissionsCompat.Builder builder) {
                        Log.d("requestPermissions", "permissionRequestResult");
                        Log.d("requestPermissions", builder.toString());
                    }
                });
            }
        });

        binding.btnPermission3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionsCompat.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, "哈哈哈哈哈", new PermissionsCompat.PermissionsCompatListener() {
                    @Override
                    public void permissionRequestBefore(PermissionsCompat.Builder builder) {
                        Log.d("requestPermissions", "permissionRequestBefore");
                    }

                    @Override
                    public void permissionRequestResult(PermissionsCompat.Builder builder) {
                        Log.d("requestPermissions", "permissionRequestResult");
                        Log.d("requestPermissions", builder.toString());
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}