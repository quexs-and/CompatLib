package com.quexs.compatlib.wheel.zip.listener;

import java.io.File;

public interface ZipListener {
    void zipStart(File inZipFile, String targetFolderPath);
    /**
     *
     * @param progress 压缩进度
     * @param compressedSize 已压缩的文件大小
     * @param sourceFileTotal 压缩前文件总大小
     */
    void zipProgress(int progress, long compressedSize, long sourceFileTotal);
    void zipEnd(File zipFile, Exception e);
}
