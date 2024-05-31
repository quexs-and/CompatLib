package com.quexs.compatlib.wheel.zip.listener;

public interface ZipDeCompressListener {
    void deCompressProgress(int progress,long unzipSize, long unzipTotal);
    void deCompressException(Exception e);
}
