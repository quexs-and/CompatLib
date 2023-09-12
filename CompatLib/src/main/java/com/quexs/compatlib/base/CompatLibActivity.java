package com.quexs.compatlib.base;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.quexs.compatlib.dialog.ProgressDialog;

/**
 * @author Quexs
 * @description:
 * @date: 2023/9/11 23:14
 */
public class CompatLibActivity extends AppCompatActivity {


    /**
     * 显示弹窗
     * @param msg
     */
    public void showProgressDialog(String msg) {
        String tag = ProgressDialog.class.getName();
        FragmentManager fm = getSupportFragmentManager();
        ProgressDialog dialog = (ProgressDialog) fm.findFragmentByTag(tag);
        if (dialog == null) {
            dialog = (ProgressDialog) fm.getFragmentFactory().instantiate(getClassLoader(), tag);
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            dialog.setCancelable(false);
        }
        if(!dialog.isShowing()){
            Bundle bundle = new Bundle();
            bundle.putString("msg", msg);
            dialog.setArguments(bundle);
            if (!dialog.isAdded()) {
                dialog.show(fm, tag);
            } else {
                FragmentTransaction ft = fm.beginTransaction();
                ft.show(dialog);
                ft.commit();
            }
        }else {
            dialog.refreshUI(msg);
        }
    }

    /**
     * 关闭弹窗
     */
    public void hideProgressDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ProgressDialog dialog = (ProgressDialog) fm.findFragmentByTag(ProgressDialog.class.getName());
        if (dialog != null) {
            dialog.dismiss();
        }
    }
}
