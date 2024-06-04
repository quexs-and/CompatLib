package com.quexs.compatlib.base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
* @date 2024/5/31 22:46
* @author Quexs
* @Description
*/
public class CompatLibFragment extends Fragment {

    private CompatActivityListener compatActivityListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        compatActivityListener = (CompatActivityListener) context;
    }

    public <T extends Fragment> T findChildFragment(Class<T> tClass){
        if(compatActivityListener == null) return null;
        return compatActivityListener.getCurrentActivity().findFragment(getChildFragmentManager(), tClass);
    }

    public <T extends Fragment> T createChildFragment(Class<T> tClass){
        if(compatActivityListener == null) return null;
        return compatActivityListener.getCurrentActivity().createFragment(getChildFragmentManager(),tClass);
    }

    public <T extends Fragment> void loadChildFragment(@IdRes int containerViewId, Bundle bundle, boolean isReplace, Class<T> tClass){
        if(compatActivityListener == null) return;
        compatActivityListener.getCurrentActivity().loadFragment(containerViewId, getChildFragmentManager(), bundle, isReplace, tClass);
    }

    public void showProgressDialog(String msg) {
       if(compatActivityListener == null) return;
       compatActivityListener.getCurrentActivity().showProgressDialog(msg);
    }

    public void hideProgressDialog() {
        if(compatActivityListener == null) return;
        compatActivityListener.getCurrentActivity().hideProgressDialog();
    }

    public void runOnUiThread(Runnable action){
        if(compatActivityListener == null) return;
        compatActivityListener.getCurrentActivity().runOnUiThread(action);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        compatActivityListener = null;
    }
}
