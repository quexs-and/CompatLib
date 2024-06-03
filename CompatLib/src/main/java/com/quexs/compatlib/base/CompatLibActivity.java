package com.quexs.compatlib.base;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.quexs.compatlib.dialog.progress.ProgressDialogHelper;

/**
 * @author Quexs
 * @description:
 * @date: 2023/9/11 23:14
 */
public class CompatLibActivity extends AppCompatActivity implements CompatActivityListener{
    private ProgressDialogHelper progressDialogHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialogHelper = new ProgressDialogHelper();
    }

    @Override
    protected void onDestroy() {
        progressDialogHelper = null;
        super.onDestroy();
    }

    @Override
    public CompatLibActivity getCurrentActivity() {
        return this;
    }

    public <T extends Fragment> T findFragment(Class<T> tClass){
        return findFragment(getSupportFragmentManager(), tClass);
    }

    public <T extends Fragment> T findFragment(FragmentManager fm, Class<T> tClass){
        return tClass.cast(fm.findFragmentByTag(tClass.getName()));
    }

    public <T extends Fragment> T createFragment(Class<T> tClass){
        return createFragment(getSupportFragmentManager(), tClass);
    }

    public <T extends Fragment> T createFragment(FragmentManager fm, Class<T> tClass){
        return tClass.cast(fm.getFragmentFactory().instantiate(getClassLoader(), tClass.getName()));
    }

    /**
     * 显示弹窗
     * @param msg
     */
    public void showProgressDialog(String msg) {
        if(progressDialogHelper == null) return;
        progressDialogHelper.showProgressDialog(this, msg, getSupportFragmentManager());
    }

    /**
     * 关闭弹窗
     */
    public void hideProgressDialog() {
        if(progressDialogHelper == null) return;
        progressDialogHelper.hideProgressDialog(getSupportFragmentManager());
    }
}
