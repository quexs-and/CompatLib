package com.quexs.compatlib.base;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
