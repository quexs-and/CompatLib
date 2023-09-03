package com.quexs.cameraxlib.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Quexs
 * @description:
 * @date :2023/9/3 15:40
 */
public class ImageFileUtil {

    /**
     * 获取指定目录下 最后修改 的图片
     * @param directory
     * @return
     */
    static public File getLastModifiedImageFileForDirectory(File directory){
        if(directory == null || !directory.exists() && !directory.isDirectory()) return null;
        File[] files = directory.listFiles();
        if(files == null) return null;
        File lastModifiedFile = null;
        for (File file : files){
            if(file.isDirectory()){
                //文件目录继续读取
                File childLastModifiedFile = getLastModifiedImageFileForDirectory(directory);
                if(lastModifiedFile == null || lastModifiedFile.lastModified() < childLastModifiedFile.lastModified()){
                    lastModifiedFile = childLastModifiedFile;
                }
            }else{
                try {
                    FileInputStream is = new FileInputStream(file);
                    BufferedInputStream bin = new BufferedInputStream(is);
                    String mimeType = MineTypeUtil.guessContentTypeFromStream(bin);
                    if(mimeType != null){
                        if(Pattern.compile("image/*").matcher(mimeType).find()){
                            if(lastModifiedFile == null){
                                lastModifiedFile = file;
                            }else if(lastModifiedFile.lastModified() < file.lastModified()){
                                lastModifiedFile = file;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return lastModifiedFile;
    }

    /**
     * 获取指定目录下 所有图片
     * @param directory
     * @return
     */
   static public List<File> getImagesForDirectory(File directory){
       if(directory == null || !directory.exists() && !directory.isDirectory()) return null;
       File[] files = directory.listFiles();
       if(files == null) return null;
       List<File> fileList = new ArrayList<>();
       for (File file : files){
           if(file.isDirectory()){
               List<File> childFileList = getImagesForDirectory(file);
               if(!childFileList.isEmpty()){
                   fileList.addAll(childFileList);
               }
           }else {
               try {
                   FileInputStream is = new FileInputStream(file);
                   BufferedInputStream bin = new BufferedInputStream(is);
                   String mimeType = MineTypeUtil.guessContentTypeFromStream(bin);
                   if(mimeType != null && Pattern.compile("image/*").matcher(mimeType).find()){
                       fileList.add(file);
                   }
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       }
       return fileList;
   }

}
