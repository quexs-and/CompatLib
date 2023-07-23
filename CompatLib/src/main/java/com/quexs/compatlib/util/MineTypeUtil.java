package com.quexs.compatlib.util;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLConnection;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2023/7/22
 * <p>
 * Time: 17:48
 * <p>
 * 备注：枚举类型
 */
public class MineTypeUtil {
    /**
     * 注意：此方法在 URLConnection.guessContentTypeFromStream(InputStream is) 方法上做补充
     * <p>
     * Tries to determine the type of an input stream based on the characters at the beginning of the input stream. This method can be used by subclasses that override the getContentType method.
     * Ideally, this routine would not be needed. But many http servers return the incorrect content type; in addition, there are many nonstandard extensions. Direct inspection of the bytes to determine the content type is often more accurate than believing the content type claimed by the http server.
     * Params:
     * is – an input stream that supports marks.
     * Returns:
     * a guess at the content type, or null if none can be determined.
     * Throws:
     * IOException – if an I/O error occurs while reading the input stream.
     * See Also:
     * InputStream.mark(int), InputStream.markSupported(), getContentType()
     *
     * @param is
     * @return
     */
    static public String guessContentTypeFromStream(InputStream is) throws IOException {
        String mineType = URLConnection.guessContentTypeFromStream(is);
        if (mineType == null) {
            if (!is.markSupported()) return null;
            is.mark(16);
            int c1 = Integer.parseInt(Integer.toHexString(is.read()));
            int c2 = Integer.parseInt(Integer.toHexString(is.read()));
            int c3 = Integer.parseInt(Integer.toHexString(is.read()));
            int c4 = Integer.parseInt(Integer.toHexString(is.read()));
            int c5 = Integer.parseInt(Integer.toHexString(is.read()));
            int c6 = Integer.parseInt(Integer.toHexString(is.read()));
            int c7 = Integer.parseInt(Integer.toHexString(is.read()));
            int c8 = Integer.parseInt(Integer.toHexString(is.read()));
            int c9 = Integer.parseInt(Integer.toHexString(is.read()));
            int c10 = Integer.parseInt(Integer.toHexString(is.read()));
            int c11 = Integer.parseInt(Integer.toHexString(is.read()));
            int c12 = Integer.parseInt(Integer.toHexString(is.read()));
            int c13 = Integer.parseInt(Integer.toHexString(is.read()));
            int c14 = Integer.parseInt(Integer.toHexString(is.read()));
            int c15 = Integer.parseInt(Integer.toHexString(is.read()));
            int c16 = Integer.parseInt(Integer.toHexString(is.read()));

            Log.d("cc=", "" + c1);
            Log.d("cc=", "" + c2);
            Log.d("cc=", "" + c3);
            Log.d("cc=", "" + c4);
            Log.d("cc=", "" + c5);
            Log.d("cc=", "" + c6);
            Log.d("cc=", "" + c7);
            Log.d("cc=", "" + c8);
            Log.d("cc=", "" + c9);
            Log.d("cc=", "" + c10);
            Log.d("cc=", "" + c11);
            Log.d("cc=", "" + c12);

            is.reset();
            //新增视频文件格式识别
            if (c1 == 0 && c2 == 0 && c3 == 0 && c5 == 0x66 && c6 == 0x74 && c7 == 0x79 && c8 == 0x70) {
                if(c9 == 0x33 && c10 == 0x67 && c11 == 0x70){
                    if(c4 == 0x14){
                        return "video/3gpp";
                    }
                    if(c4 == 0x18){
                        return "video/mpeg-4";
                    }
                    if(c4 == 0x20){
                        return "video/3gpp2";
                    }
                }else if(c4 == 0x20 && c9 == 0x4D && c10 == 0x34 && c11 == 0x41){
                    return "video/mp4a-latm";
                }
            }
            //TODO 待添加识别其他文件格式
        }
        return mineType;
    }
}
