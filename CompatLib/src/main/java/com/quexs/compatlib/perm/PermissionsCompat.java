package com.quexs.compatlib.perm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.quexs.compatlib.dialog.PermissionDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2024/02/03
 * <p>
 * Time: 20:23
 * <p>
 * 备注：权限工具类
 */
public class PermissionsCompat {

    private final String ACTION_REQUEST_PERMISSIONS = "androidx.activity.result.contract.action.REQUEST_PERMISSIONS";
    private final String  EXTRA_PERMISSIONS = "androidx.activity.result.contract.extra.PERMISSIONS";
    private final String EXTRA_PERMISSION_GRANT_RESULTS = "androidx.activity.result.contract.extra.PERMISSION_GRANT_RESULTS";

    private ActivityResultLauncher<Builder> permLauncher;
    private ActivityResultLauncher<Builder> permSettingLauncher;

    public PermissionsCompat(ActivityResultCaller resultCaller){
        permLauncher = resultCaller.registerForActivityResult(resultContract(), resultCallback());
        permSettingLauncher = resultCaller.registerForActivityResult(settingResultContract(), settingResultCallback());
    }

    public void requestPermissions(String[] perms) {
        requestPermissions(perms,null);
    }

    public void requestPermissions(String[] perms,PermissionsCompatListener permCompatListener) {
        requestPermissions(perms,null, null,null,null, permCompatListener);
    }

    public void requestPermissions(String[] perms, String dialogContent, PermissionsCompatListener permCompatListener) {
        requestPermissions(perms,dialogContent, null,null,null, permCompatListener);
    }

    public void requestPermissions(String[] perms, String dialogContent, String dialogTile, String dialogCancel, String dialogConfirm,PermissionsCompatListener permCompatListener) {
        Builder builder = new Builder();
        builder.setPermissions(perms);
        builder.setDialogTile(dialogTile);
        builder.setDialogContent(dialogContent);
        builder.setDialogCancel(dialogCancel);
        builder.setDialogConfirm(dialogConfirm);
        builder.setPermissionsCompatListener(permCompatListener);
        permLauncher.launch(builder);
    }


    private ActivityResultContract<Builder, Builder> resultContract(){
        return new ActivityResultContract<Builder, Builder>() {
            private Builder mBuilder;
            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Builder builder) {
                Intent intent = new Intent(ACTION_REQUEST_PERMISSIONS);
                intent.putExtra(EXTRA_PERMISSIONS, builder.getPermissions());
                return intent;
            }

            @Override
            public Builder parseResult(int resultCode, @Nullable Intent intent) {
                if (resultCode != Activity.RESULT_OK || intent == null) {
                    mBuilder.setCancel(true);
                    return mBuilder;
                }
                String[] permissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS);
                int[] grantResults = intent.getIntArrayExtra(EXTRA_PERMISSION_GRANT_RESULTS);
                if (grantResults == null || permissions == null) {
                    return mBuilder;
                }
                for(int i = 0; i  < permissions.length; i++){
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        mBuilder.getDeniedPermissions().add(permissions[i]);
                    }
                }
                return mBuilder;
            }

            @Nullable
            @Override
            public SynchronousResult<Builder> getSynchronousResult(@NonNull Context context, Builder input) {
                input.setReference(new WeakReference<>(context));
                if(input.getPermissions() == null){
                    return new SynchronousResult<>(input);
                }
                List<String> unPerms = new ArrayList<>();
                input.setDeniedPermissions(unPerms);

                for(String perm : input.getPermissions()){
                    if(ActivityCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED){
                        if(input.getPermissionsCompatListener() != null){
                            input.getPermissionsCompatListener().permissionRequestBefore(input);
                        }
                        mBuilder = input;
                        return super.getSynchronousResult(context, input);
                    }
                }
                return new SynchronousResult<>(input);
            }
        };
    }

    private ActivityResultCallback<Builder> resultCallback(){
        return new ActivityResultCallback<Builder>() {
            @Override
            public void onActivityResult(Builder result) {
                if(result.isCancel() || (result.getDeniedPermissions() != null && !result.getDeniedPermissions().isEmpty())){
                    if(permSettingLauncher != null){
                        if(TextUtils.isEmpty(result.getDialogContent())){
                            permSettingLauncher.launch(result);
                        }else {
                            showPermissionDialog(result);
                        }
                    }
                }else {
                    PermissionsCompatListener permissionsCompatListener = result.getPermissionsCompatListener();
                    if(permissionsCompatListener != null){
                        result.setPermissionsCompatListener(null);
                        permissionsCompatListener.permissionRequestResult(result);
                    }
                }
            }
        };
    }

    private ActivityResultContract<Builder, Builder> settingResultContract(){
        return new ActivityResultContract<Builder, Builder>() {
            private Builder mBuilder;

            @NonNull
            @Override
            public Intent createIntent(@NonNull Context context, Builder builder) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                return intent;
            }

            @Nullable
            @Override
            public SynchronousResult<Builder> getSynchronousResult(@NonNull Context context, Builder input) {
                mBuilder = input;
                mBuilder.setReference(new WeakReference<>(context));
                return super.getSynchronousResult(context, input);
            }

            @Override
            public Builder parseResult(int resultCode, @Nullable Intent intent) {
                Iterator<String> it = mBuilder.getDeniedPermissions().iterator();
                while (it.hasNext()){
                    int checkPerm = ActivityCompat.checkSelfPermission(mBuilder.getReference().get(), it.next());
                    if(checkPerm == PackageManager.PERMISSION_GRANTED){
                        it.remove();
                    }
                }
                return mBuilder;
            }
        };
    }

    private ActivityResultCallback<Builder> settingResultCallback(){
        return new ActivityResultCallback<Builder>() {
            @Override
            public void onActivityResult(Builder result) {
                PermissionsCompatListener permissionsCompatListener = result.getPermissionsCompatListener();
                if(permissionsCompatListener != null){
                    result.setPermissionsCompatListener(null);
                    permissionsCompatListener.permissionRequestResult(result);
                }
            }
        };
    }

    private void showPermissionDialog(Builder builder) {
        FragmentActivity fragmentActivity = (FragmentActivity) builder.getReference().get();
        String tag = PermissionDialog.class.getName();
        FragmentManager fm = fragmentActivity.getSupportFragmentManager();
        PermissionDialog dialog = (PermissionDialog) fm.findFragmentByTag(tag);
        if (dialog == null) {
            dialog = (PermissionDialog) fm.getFragmentFactory().instantiate(builder.getReference().get().getClassLoader(), tag);
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            dialog.setCancelable(false);
        }
        dialog.setPermissionDialogListener(new PermissionDialog.PermissionDialogListener() {
            @Override
            public void onCancelSettingPermission(Builder builder) {
                builder.setCancel(true);
                PermissionsCompatListener permissionsCompatListener = builder.getPermissionsCompatListener();
                if(permissionsCompatListener != null){
                    builder.setPermissionsCompatListener(null);
                    permissionsCompatListener.permissionRequestResult(builder);
                }
            }

            @Override
            public void onConfirmSettingPermission(Builder builder) {
                if(permSettingLauncher != null){
                    permSettingLauncher.launch(builder);
                }
            }
        });
        if(!dialog.isShowing()){
            dialog.setBuilder(builder);
            if (!dialog.isAdded()) {
                dialog.show(fm, tag);
            } else {
                FragmentTransaction ft = fm.beginTransaction();
                ft.show(dialog);
                ft.commit();
            }
        }else {
            dialog.refreshUI(builder);
        }
    }

    public void onRelease(){
        permLauncher = null;
        permSettingLauncher = null;
    }

    public static class Builder{
        private WeakReference<Context> reference;
        private String dialogTile,dialogContent,dialogCancel,dialogConfirm;
        private String[] permissions;
        private List<String> deniedPermissions;
        private boolean isCancel;
        private PermissionsCompatListener permissionsCompatListener;

        public WeakReference<Context> getReference() {
            return reference;
        }

        public void setReference(WeakReference<Context> reference) {
            this.reference = reference;
        }

        public String getDialogContent() {
            return dialogContent;
        }

        public void setDialogContent(String dialogContent) {
            this.dialogContent = dialogContent;
        }

        public String getDialogTile() {
            return dialogTile;
        }

        public void setDialogTile(String dialogTile) {
            this.dialogTile = dialogTile;
        }

        public String getDialogCancel() {
            return dialogCancel;
        }

        public void setDialogCancel(String dialogCancel) {
            this.dialogCancel = dialogCancel;
        }

        public String getDialogConfirm() {
            return dialogConfirm;
        }

        public void setDialogConfirm(String dialogConfirm) {
            this.dialogConfirm = dialogConfirm;
        }

        public String[] getPermissions() {
            return permissions;
        }

        public void setPermissions(String[] permissions) {
            this.permissions = permissions;
        }

        public List<String> getDeniedPermissions() {
            return deniedPermissions;
        }

        public void setDeniedPermissions(List<String> deniedPermissions) {
            this.deniedPermissions = deniedPermissions;
        }

        public boolean isCancel() {
            return isCancel;
        }

        public void setCancel(boolean cancel) {
            isCancel = cancel;
        }

        public PermissionsCompatListener getPermissionsCompatListener() {
            return permissionsCompatListener;
        }

        public void setPermissionsCompatListener(PermissionsCompatListener permissionsCompatListener) {
            this.permissionsCompatListener = permissionsCompatListener;
        }

        @Override
        public String toString() {
            return "Builder{" +
                    ", dialogTile='" + dialogTile + '\'' +
                    ", dialogContent='" + dialogContent + '\'' +
                    ", dialogCancel='" + dialogCancel + '\'' +
                    ", dialogConfirm='" + dialogConfirm + '\'' +
                    ", permissions=" + Arrays.toString(permissions) +
                    ", deniedPermissions=" + deniedPermissions +
                    ", isCancel=" + isCancel +
                    ", permissionsCompatListener=" + permissionsCompatListener +
                    '}';
        }
    }

    public interface PermissionsCompatListener{
        void permissionRequestBefore(Builder builder);
        void permissionRequestResult(Builder builder);
    }



}
