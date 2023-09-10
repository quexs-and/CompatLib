package com.quexs.compatlib.compat;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.regex.Pattern;


/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2022/02/14
 * <p>
 * Time: 02:23
 * <p>
 * 备注：Uri 转换 兼容
 */
public class ConvertUriCompat {

    private final Context appContext;

    public ConvertUriCompat(Context appContext){
        this.appContext = appContext;
    }

    public File uriToFile(Uri uri){
        if (ContentResolver.SCHEME_FILE.equals(uri.getScheme())) {
            return new File(uri.getPath());
        }
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())){
            Cursor cursor = appContext.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToNext()) {
                        int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        InputStream is = appContext.getContentResolver().openInputStream(uri);

                        String displayName = cursor.getString(columnIndex);
                        int mineTypeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE);
                        String mineType = cursor.getString(mineTypeIndex);
                        File parentFile = getParentFile(mineType);
                        File file = new File(parentFile, Calendar.getInstance().getTimeInMillis() + "_" + displayName);
                        FileOutputStream fos = new FileOutputStream(file);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            FileUtils.copy(is, fos);
                        } else {
                            byte[] bt = new byte[1024];
                            int l;
                            while ((l = is.read(bt)) > 0) {
                                fos.write(bt, 0, l);
                            }
                            fos.flush();
                        }
                        fos.close();
                        is.close();
                        Log.d("UriToFile", "copy file length=" + file.length());
                        return file;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
        }
        return null;
    }

    private File getParentFile(String mineType){
        if (Pattern.compile("image/*").matcher(mineType).find()){
            return appContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }else if(Pattern.compile("video/*").matcher(mineType).find()){
            return appContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        }else if(Pattern.compile("audio/*").matcher(mineType).find()){
            return appContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        }
        return appContext.getExternalFilesDir(Environment.DIRECTORY_DCIM);
    }


}
