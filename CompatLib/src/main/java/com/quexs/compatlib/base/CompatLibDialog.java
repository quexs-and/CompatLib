package com.quexs.compatlib.base;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

/**
 * @author Quexs
 * @description: DialogFragment 通用基类
 * @date: 2024/5/13 23:02
 */
public class CompatLibDialog<T extends Parcelable> extends DialogFragment {

    private boolean isShowing;
    private T data;

    public void onLoadUIData(View view, T data){

    }

    public T getData() {
        return data;
    }

    /**
     * 显示弹窗
     * @param manager
     * @param tag
     * @param data
     */
    public void show(@NonNull FragmentManager manager, @Nullable String tag, T data) {
        this.data = data;
        if(getDialog() != null && getDialog().isShowing()){
            if(getView() != null){
                getView().post(new CompatLibRunnable<T>(data) {
                    @Override
                    public void compatLibRun(T t) {
                        onLoadUIData(getView(), t);
                    }
                });
                return;
            }
        }
        if(isShowing) return;
        isShowing = true;
        show(manager, tag);
    }

    @Override
    public void dismiss() {
        isShowing = false;
        if(isResumed()){
            super.dismiss();
        }else {
            dismissAllowingStateLoss();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(savedInstanceState == null){
            onLoadUIData(getView(), data);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("data", data);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null){
            data = savedInstanceState.getParcelable("data");
            onLoadUIData(getView(), data);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        isShowing = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isShowing = false;
    }
}
