package com.quexs.compatlib.dialog.progress;

import android.content.Context;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.quexs.compatlib.bean.PString;

import java.util.concurrent.locks.ReentrantLock;

/**
* @date 2024/5/31 22:49
* @author Quexs
* @Description
*/
public class ProgressDialogHelper {

    private final ReentrantLock lock;
    public ProgressDialogHelper(){
        lock = new ReentrantLock();
    }

    public void showProgressDialog(Context context, String msg, FragmentManager fm) {
        lock.lock();
        String tag = ProgressDialog.class.getName();
        ProgressDialog progressDialog = (ProgressDialog) fm.findFragmentByTag(tag);
        if (progressDialog == null) {
            progressDialog = (ProgressDialog) fm.getFragmentFactory().instantiate(context.getClassLoader(), tag);
            progressDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            progressDialog.setCancelable(false);
        }
        progressDialog.show(fm, tag, new PString(msg));
        lock.unlock();
    }

    public void hideProgressDialog(FragmentManager fm) {
        lock.lock();
        String tag = ProgressDialog.class.getName();
        ProgressDialog progressDialog = (ProgressDialog) fm.findFragmentByTag(tag);
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        lock.unlock();
    }
}
