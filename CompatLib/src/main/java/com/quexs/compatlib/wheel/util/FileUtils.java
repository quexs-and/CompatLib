package com.quexs.compatlib.wheel.util;

import java.io.File;
import java.text.DecimalFormat;

/**
* @date 2024/5/17 22:52
* @author Quexs
* @Description File工具类
*/
public class FileUtils {

    /**
     * 删除文件
     * @param file
     * @return
     */
    static public boolean deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if(files != null) {
                for(File childFile : files){
                    boolean isSuccess = deleteFile(childFile);
                    if(!isSuccess){
                        return false;
                    }
                }
            }
        }
        return file.delete();
    }

    static public int calculateChildCount(File file){
        int count = 0;
        if(file.isDirectory()){
            File[] childFiles = file.listFiles();
            if(childFiles != null){
                for(File childFile : childFiles){
                    count += calculateChildCount(childFile);
                }
            }
        }
        return count;
    }

    /**
     * 计算文件大小
     * @param file
     * @return
     */
    static public long calculateFileSize(File file){
        long size = 0;
        if(file.isDirectory()){
            File[] childFiles = file.listFiles();
            if(childFiles != null){
                for(File childFile : childFiles){
                    size += calculateFileSize(childFile);
                }
            }
        }else {
            size += file.length();
        }
        return size;
    }

    static public String convertSizeToUnit(long size){
        DecimalFormat df = new DecimalFormat("#.00");
        if(size < 1024){
            return df.format((double) size) + "B";
        }else if(size < 1048576){
            return df.format((double) size / 1024) + "KB";
        }else if(size < 1073741824){
            return df.format((double) size / 1048576) + "MB";
        }else {
            return df.format((double) size / 1073741824) + "GB";
        }
    }


}
