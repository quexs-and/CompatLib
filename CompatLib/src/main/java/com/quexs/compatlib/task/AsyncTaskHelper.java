package com.quexs.compatlib.task;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.quexs.compatlib.IAsyncTaskCallback;
import com.quexs.compatlib.IAsyncTaskInterface;

/**
 * @author Quexs
 * @description: 跨进程任务兼容类
 * @date: 2023/11/15 0:19
 */
public class AsyncTaskHelper {

    private ContextWrapper wrapper;
    private ServiceConnection serviceConnection;
    private IAsyncTaskInterface iAsyncTaskInterface;

    public AsyncTaskHelper(ContextWrapper wrapper){
        this.wrapper = wrapper;
    }

    /**
     * 绑定跨进程服务
     */
    public void bindService(){
        this.serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                iAsyncTaskInterface = IAsyncTaskInterface.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                iAsyncTaskInterface = null;
            }
        };
        Intent intent = new Intent(this.wrapper, AsyncTaskService.class);
        this.wrapper.bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 发送指令
     * @param taskCode
     * @param aString
     * @param iAsyncTaskCallback
     * @return
     */
    public boolean onCommand(@AsyncTaskEnum.TaskCode int taskCode, String aString, IAsyncTaskCallback iAsyncTaskCallback){
        if(this.iAsyncTaskInterface != null){
            try {
                this.iAsyncTaskInterface.onCommand(taskCode, aString, iAsyncTaskCallback);
            } catch (RemoteException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 取消跨进程服务
     */
    public void unbindService(){
        if(this.serviceConnection != null){
            this.wrapper.unbindService(serviceConnection);
            this.serviceConnection = null;
            this.iAsyncTaskInterface = null;
        }
        this.wrapper = null;

    }

}
