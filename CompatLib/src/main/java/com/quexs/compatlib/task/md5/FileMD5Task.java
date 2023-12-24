package com.quexs.compatlib.task.md5;

import android.os.RemoteException;

import com.quexs.compatlib.IAsyncTaskCallback;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Quexs
 * @description: 文件获取Md5
 * @date: 2023/11/14 21:56
 */
public class FileMD5Task implements Runnable{
    private String filePath;
    private IAsyncTaskCallback iAsyncTaskCallback;
    public FileMD5Task(String filePath, IAsyncTaskCallback iAsyncTaskCallback){
        this.filePath = filePath;
        this.iAsyncTaskCallback = iAsyncTaskCallback;
    }

    @Override
    public void run() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(filePath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = bis.read(buffer)) != -1) {
                md.update(buffer, 0, length);
            }
            bis.close();
            byte[] digest = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            if(iAsyncTaskCallback != null){
                iAsyncTaskCallback.onResult(sb.toString());
                iAsyncTaskCallback = null;
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
