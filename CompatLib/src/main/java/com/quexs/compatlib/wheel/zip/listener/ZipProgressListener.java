package com.quexs.compatlib.wheel.zip.listener;

public interface ZipProgressListener {
    /**
     *
     * @param progress 压缩进度
     * @param compressedSize 已压缩的文件大小
     * @param sourceFileTotal 压缩前文件总大小
     */
    void zipProgress(int progress, long compressedSize, long sourceFileTotal);
}
