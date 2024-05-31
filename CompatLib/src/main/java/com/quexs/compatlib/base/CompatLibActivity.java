package com.quexs.compatlib.base;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onDestroy() {
        progressDialogHelper = null;
        super.onDestroy();
    }

    @Override
    public CompatLibActivity getCurrentActivity() {
        return this;
    }
}
