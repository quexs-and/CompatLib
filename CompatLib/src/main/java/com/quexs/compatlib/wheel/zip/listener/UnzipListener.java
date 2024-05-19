package com.quexs.compatlib.wheel.zip.listener;

import java.io.File;

public interface UnzipListener {
    void unzipStart(File inZipFile, String targetFolderPath);
    /**
     *
     * @param progress 解压缩进度
     * @param unzipSize 已解压缩的文件大小
     * @param unzipTotal 解压缩后文件总大小
     */
    void unzipProgress(int progress,long unzipSize, long unzipTotal);

    void unzipEnd(File unzipFile,Exception e);
}
