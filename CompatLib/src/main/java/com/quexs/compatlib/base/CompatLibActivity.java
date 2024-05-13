package com.quexs.compatlib.base;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.quexs.compatlib.dialog.bean.PString;
import com.quexs.compatlib.dialog.ProgressDialog;

/**
 * @author Quexs
 * @description:
 * @date: 2023/9/11 23:14
 */
public class CompatLibActivity extends AppCompatActivity {
    private ProgressDialog progressDialog;

    /**
     * 显示弹窗
     * @param msg
     */
    public void showProgressDialog(String msg) {
        String tag = ProgressDialog.class.getName();
        FragmentManager fm = getSupportFragmentManager();
        if(progressDialog == null){
            progressDialog  = (ProgressDialog) fm.findFragmentByTag(tag);
        }
        if (progressDialog == null) {
            progressDialog = (ProgressDialog) fm.getFragmentFactory().instantiate(getClassLoader(), tag);
            progressDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            progressDialog.setCancelable(false);
        }
        progressDialog.show(fm, tag, new PString(msg));
    }

    /**
     * 关闭弹窗
     */
    public void hideProgressDialog() {
        if(progressDialog == null){
            FragmentManager fm = getSupportFragmentManager();
            String tag = ProgressDialog.class.getName();
            progressDialog  = (ProgressDialog) fm.findFragmentByTag(tag);
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        progressDialog = null;
        super.onDestroy();
    }
}
