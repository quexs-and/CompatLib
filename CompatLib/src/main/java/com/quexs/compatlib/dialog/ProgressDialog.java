package com.quexs.compatlib.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.quexs.compatlib.R;
import com.quexs.compatlib.util.DensityUtil;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2022/02/14
 * <p>
 * Time: 22:45
 * <p>
 * 备注：加载弹窗
 */
public class ProgressDialog extends DialogFragment {
    private String msg;
    private TextView txvToast;
    private boolean isShowingDialog;

    public ProgressDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(bundle != null){
            msg = bundle.getString("msg","");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.compat_lib_fragment_progress_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProgressBar progressBar = view.findViewById(R.id.pb_load);
        ConstraintLayout.LayoutParams clPbLoad = (ConstraintLayout.LayoutParams)progressBar.getLayoutParams();
        clPbLoad.width = DensityUtil.dpToPx(view.getContext(), 65);
        clPbLoad.height = DensityUtil.dpToPx(view.getContext(), 65);
        progressBar.setLayoutParams(clPbLoad);
        txvToast = view.findViewById(R.id.txv_load_dialog);
        txvToast.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        ConstraintLayout.LayoutParams clTxvToast = (ConstraintLayout.LayoutParams)txvToast.getLayoutParams();
        clTxvToast.topMargin = DensityUtil.dpToPx(view.getContext(), 2);
        txvToast.setLayoutParams(clTxvToast);
        txvToast.setText(msg);
    }

    public void refreshUI(String msg){
        if(getDialog() != null || getDialog().isShowing()){
            txvToast.post(new RefreshRunnable(msg));
        }else {
            this.msg = msg;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = DensityUtil.dpToPx(getContext(), 100);
        lp.height = DensityUtil.dpToPx(getContext(), 100);
        lp.gravity = Gravity.CENTER;
        lp.alpha = 1;
        lp.dimAmount = 0f;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void show(@NonNull FragmentManager manager, @Nullable String tag) {
        isShowingDialog = true;
        super.show(manager, tag);
    }

    @Override
    public void dismiss() {
        if(getDialog() == null || !getDialog().isShowing()) return;
        isShowingDialog = false;
        if(isResumed()){
            super.dismiss();
        }else {
            dismissAllowingStateLoss();
        }
    }

    @Override
    public void onDestroyView() {
        isShowingDialog = false;
        super.onDestroyView();
    }

    /**
     * 判断弹窗是否显示
     *
     * @return
     */
    public boolean isShowing() {
        return isShowingDialog || (getDialog() != null && getDialog().isShowing());
    }

    private class RefreshRunnable implements Runnable{
        private final String msg;

        public RefreshRunnable(String msg) {
            this.msg = msg;
        }

        @Override
        public void run() {
            txvToast.setText(msg);
        }
    }

}