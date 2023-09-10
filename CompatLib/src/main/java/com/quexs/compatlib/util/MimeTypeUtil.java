package com.quexs.compatlib.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
public class MimeTypeUtil {

    static public String getMimeTypeFromFile(File file) {
        String mimeType = null;
        try {
            FileInputStream is = new FileInputStream(file);
            BufferedInputStream bin = new BufferedInputStream(is);
            mimeType = MimeTypeUtil.guessContentTypeFromStream(bin);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(mimeType == null){
            mimeType = "*/*";
        }
        return mimeType;
    }

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
            //压缩文件
            if(c1 == 0x50 && c2 == 0x4b && c15 == 0x00 && c16 == 0x00){
                if(c3 == 0x03 && c4 == 0x04 && c8 == 0x00 && c9 == 0x00 && c10 == 0x00){
                    if(c5 == 0x14 && c6 == 0x00 && c7 == 0x18 && c11 == 0x75 && c12 == 0x05 && c13 == 0x2a && c14 == 0x57){
                        return "application/x-zip-compressed";
                    }else if(c5 == 0x00 && c6 == 0x00 && c7 == 0x00 && c11 == 0x21 && c12 == 0x08 && c13 == 0x21){
                        return "application/vnd.android.package-archive";
                    }
                }
            }
            //TODO 待添加识别其他文件格式
        }
        return mineType;
    }
}
