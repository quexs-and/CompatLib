package com.quexs.compatlib.base;

import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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

    public <T extends Fragment> void loadFragment(@IdRes int containerViewId, Bundle bundle, boolean isReplace, Class<T> tClass){
        loadFragment(containerViewId, getSupportFragmentManager(), bundle, isReplace, tClass);
    }

    public <T extends Fragment> void loadFragment(@IdRes int containerViewId, FragmentManager fm, Bundle bundle, boolean isReplace, Class<T> tClass){
        T t = findFragment(fm, tClass);
        if(t == null){
            t = createFragment(fm, tClass);
            if(bundle != null){
                t.setArguments(bundle);
            }
        }
        FragmentTransaction ft = fm.beginTransaction();
        if(isReplace){
            ft.replace(containerViewId, t, t.isAdded() ? null : tClass.getName());
        }else {
            ft.add(containerViewId, t, t.isAdded() ? null : tClass.getName());
        }
        ft.addToBackStack(null);
        ft.commit();
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
