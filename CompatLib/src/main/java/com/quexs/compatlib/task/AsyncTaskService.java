package com.quexs.compatlib.task;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import com.quexs.compatlib.IAsyncTaskCallback;
import com.quexs.compatlib.IAsyncTaskInterface;
import com.quexs.compatlib.task.md5.FileMD5Task;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Quexs
 * @description:
 * @date: 2023/11/14 22:21
 */
public class AsyncTaskService extends Service {

    private ThreadPoolExecutor threadPool;
    @Override
    public void onCreate() {
        super.onCreate();
        initThreadPool();
    }

    private void initThreadPool(){
        int cpuCount = Runtime.getRuntime().availableProcessors();
        //核心线程总数 设定为3个
        int corPoolSize = Math.min(cpuCount, 5);
        //线程空闲后的存活时长 1 秒
        long keepAliveTime = 100L;
        threadPool = new ThreadPoolExecutor(corPoolSize,corPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
        //允许核心线程超时关闭
        threadPool.allowCoreThreadTimeOut(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub.asBinder();
    }

   private final IAsyncTaskInterface.Stub stub = new IAsyncTaskInterface.Stub() {

       @Override
       public void onCommand(int commandCode, String aString, IAsyncTaskCallback callback) throws RemoteException {
            switch (commandCode){
                case AsyncTaskEnum.TaskCode.FILE_MD5:
                    //计算文件MD5
                    threadPool.execute(new FileMD5Task(aString, callback));
                    break;
            }
       }
   };

}
