package com.quexs.cameraxlib.camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.quexs.cameraxlib.R;

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
public class TakeCameraActivity extends AppCompatActivity implements TakeCameraFrgListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerax_lib_activity_take_camera);
        addOrReplaceFragment(TakeCameraFragment.class.getName(), false, null, null);
    }

    private void addOrReplaceFragment(String frgName, boolean isReplace ,View sharedElement, String sharedElementName){
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(frgName);
        if(fragment == null){
            fragment = fm.getFragmentFactory().instantiate(getClassLoader(), frgName);
        }
        FragmentTransaction transaction = fm.beginTransaction();
        if(isReplace){
            if(fragment.isAdded()){
                transaction.replace(R.id.container, fragment);
            }else {
                transaction.replace(R.id.container, fragment,frgName);
            }
            transaction.addToBackStack(null);
            if(sharedElement != null && !TextUtils.isEmpty(sharedElementName)){
                Log.d("监听", "有进来");
                transaction.addSharedElement(sharedElement,sharedElementName);
            }
        }else {
            if(fragment.isAdded()){
                transaction.add(R.id.container, fragment);
            }else {
                transaction.add(R.id.container, fragment,frgName);
            }
        }
        transaction.commit();
    }


    @Override
    public void onFragmentBackPressed() {
        onBackPressed();
    }



    @Override
    public void onFragmentReplace(String frgName, View sharedElement, String sharedElementName) {
        addOrReplaceFragment(frgName, true,sharedElement, sharedElementName);
    }

    @Override
    public void onFragmentBackResult(Intent intent) {
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}