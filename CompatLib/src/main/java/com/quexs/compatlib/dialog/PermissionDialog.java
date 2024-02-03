package com.quexs.compatlib.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.quexs.compatlib.R;
import com.quexs.compatlib.compat.ScreenParamCompat;
import com.quexs.compatlib.perm.PermissionsCompat;
import com.quexs.compatlib.util.DensityUtil;
import com.quexs.compatlib.util.StringUtil;
import com.quexs.compatlib.util.ViewTouchUtil;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2024/02/03
 * <p>
 * Time: 21:19
 * <p>
 * 备注：权限提醒弹窗
 */
public class PermissionDialog extends DialogFragment {

    private PermissionsCompat.Builder builder;

    private TextView txvTitle;
    private TextView txvContent;

    private TextView txvCancel;
    private TextView txvConfirm;

    private PermissionDialogListener permissionDialogListener;

    public PermissionDialog() {
        // Required empty public constructor
    }

    public void setPermissionDialogListener(PermissionDialogListener permissionDialogListener){
        this.permissionDialogListener = permissionDialogListener;
    }

    public void setBuilder(PermissionsCompat.Builder builder) {
        this.builder = builder;
    }

    public void refreshUI(PermissionsCompat.Builder builder){
        this.builder = builder;
        if(txvContent != null){
            txvContent.post(this::initData);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.compat_lib_fragment_permission_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViewConfig(view);
        initData();
        initViewListener();
    }

    private void initViewConfig(View view){
        int dividerHeightOrWidth = DensityUtil.dpToPx(view.getContext(),0.5f);
        int contentPadding = DensityUtil.dpToPx(view.getContext(),10);
        int viewHeight = DensityUtil.dpToPx(view.getContext(),44);

        CardView cardView = view.findViewById(R.id.card_view);
        txvTitle = view.findViewById(R.id.txv_title);
        View topDivider = view.findViewById(R.id.view_top_divider);
        View bottomDivider = view.findViewById(R.id.view_bottom_divider);
        View centerDivider = view.findViewById(R.id.view_bottom_divider_v);
        View viewBottom = view.findViewById(R.id.view_bottom);
        txvContent = view.findViewById(R.id.txv_content);
        txvCancel = view.findViewById(R.id.txv_cancel);
        txvConfirm = view.findViewById(R.id.txv_confirm);

//        cardView.setCardElevation(DensityUtil.dpToPxGetFloat(view.getContext(), 4));
        cardView.setRadius(DensityUtil.dpToPxGetFloat(view.getContext(), 9));

        ConstraintLayout.LayoutParams clTitle = (ConstraintLayout.LayoutParams) txvTitle.getLayoutParams();
        clTitle.height = viewHeight;
        txvTitle.setLayoutParams(clTitle);
        txvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        ConstraintLayout.LayoutParams clTopDivider = (ConstraintLayout.LayoutParams) topDivider.getLayoutParams();
        clTopDivider.height = dividerHeightOrWidth;
        topDivider.setLayoutParams(clTopDivider);

        ConstraintLayout.LayoutParams clBottomDivider = (ConstraintLayout.LayoutParams) bottomDivider.getLayoutParams();
        clBottomDivider.height = dividerHeightOrWidth;
        clBottomDivider.topMargin = contentPadding;
        bottomDivider.setLayoutParams(clBottomDivider);

        ConstraintLayout.LayoutParams clCenterDivider = (ConstraintLayout.LayoutParams) centerDivider.getLayoutParams();
        clCenterDivider.width = dividerHeightOrWidth;
        centerDivider.setLayoutParams(clCenterDivider);

        ConstraintLayout.LayoutParams clViewBottom = (ConstraintLayout.LayoutParams) viewBottom.getLayoutParams();
        clViewBottom.height = viewHeight;
        viewBottom.setLayoutParams(clViewBottom);

        ConstraintLayout.LayoutParams clContent = (ConstraintLayout.LayoutParams) txvContent.getLayoutParams();
        clContent.leftMargin = contentPadding;
        clContent.rightMargin = contentPadding;
        clContent.topMargin = contentPadding;
        txvContent.setLayoutParams(clContent);

        txvCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        txvConfirm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
    }

    private void initData(){
        txvContent.setText(builder.getDialogContent());
        if(!StringUtil.isBlank(builder.getDialogTile())){
            txvTitle.setText(builder.getDialogTile());
        }
        if(!StringUtil.isBlank(builder.getDialogCancel())){
            txvCancel.setText(builder.getDialogCancel());
        }
        if(!StringUtil.isBlank(builder.getDialogConfirm())){
            txvConfirm.setText(builder.getDialogConfirm());
        }
    }

    private void initViewListener(){
        txvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ViewTouchUtil.isValidClick(v,1000)) return;
                dismiss();
                if(permissionDialogListener != null){
                    permissionDialogListener.onCancelSettingPermission(builder);
                }
            }
        });
        txvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!ViewTouchUtil.isValidClick(v,1000)) return;
                dismiss();
                if(permissionDialogListener != null){
                    permissionDialogListener.onConfirmSettingPermission(builder);
                }
            }
        });
    }

    /**
     * 判断弹窗是否显示
     *
     * @return
     */
    public boolean isShowing() {
        return getDialog() != null && getDialog().isShowing();
    }


    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        int width = new ScreenParamCompat(getActivity()).getWidth();
        int leftAndRight = DensityUtil.dpToPx(getContext(), 16);
        lp.width = width - (leftAndRight * 2);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        lp.alpha = 1;
        lp.dimAmount = 0.7f;
        window.setWindowAnimations(R.style.compat_lib_dialog_zoom);
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public interface PermissionDialogListener{
        void onCancelSettingPermission(PermissionsCompat.Builder builder);
        void onConfirmSettingPermission(PermissionsCompat.Builder builder);
    }

}