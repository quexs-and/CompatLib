package com.quexs.compatlib.wheel.zip.listener;

public interface UnzipProgressListener {
    /**
     *
     * @param progress 解压缩进度
     * @param unzipSize 已解压缩的文件大小
     * @param unzipTotal 解压缩后文件总大小
     */
    void unzipProgress(int progress,long unzipSize, long unzipTotal);
}
