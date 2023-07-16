package com.quexs.compatlib.compat;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Uri 转换 兼容
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
                        String displayName = cursor.getString(columnIndex);
                        File file = new File(appContext.getExternalFilesDir(Environment.DIRECTORY_DCIM), displayName);
                        if (!file.exists()) {
                            InputStream is = appContext.getContentResolver().openInputStream(uri);
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
                        }
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

}
