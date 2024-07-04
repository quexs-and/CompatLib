package com.quexs.compatlib.ipc;

import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.quexs.compatlib.IPCCallback;
import com.quexs.compatlib.IPCInterface;
import com.quexs.compatlib.wheel.util.ApkUtils;

/**
 * @author Quexs
 * @description: 跨进程任务兼容类
 * @date: 2023/11/15 0:19
 */
public class IPCHelper {

    private ContextWrapper wrapper;
    private ServiceConnection serviceConnection;
    private IPCInterface ipcInterface;
    private IpcCallbackListener ipcCallbackListener;
    private final String ownKey;

    public IPCHelper(ContextWrapper wrapper){
        this(wrapper, ApkUtils.getProcessName());
    }

    public IPCHelper(ContextWrapper wrapper, String ownKey){
        this.wrapper = wrapper;
        this.ownKey = ownKey;
    }

    /**
     * 绑定跨进程服务
     */
     public void bindIPCService(){
        this.serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                ipcInterface = IPCInterface.Stub.asInterface(service);
                try {
                    ipcInterface.reginsterCallback(ownKey, ipcCallback);
                } catch (RemoteException e) {
                    Log.e("IPC","", e);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                try {
                    ipcInterface.unreginsterCallback(ownKey);
                } catch (RemoteException e) {
                    Log.e("IPC","", e);
                }
                ipcInterface = null;
            }
        };
        Intent intent = new Intent(this.wrapper, IPCService.class);
        this.wrapper.bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void setIpcCallbackListener(IpcCallbackListener ipcCallbackListener){
        this.ipcCallbackListener = ipcCallbackListener;
    }

    public void addIpcCallback(String key, IPCCallback.Stub ipcCallback){
         if(ipcInterface != null){
             try {
                 ipcInterface.reginsterCallback(key, ipcCallback);
             } catch (RemoteException e) {
                 Log.e("IPC","", e);
             }
         }
    }

    /**
     * 发送消息
     * @param targetKey
     * @param message
     */
    public void sendMessage(String targetKey, String message){
        if(ipcInterface == null) return;
        try {
            ipcInterface.sendMessage(targetKey, ownKey, message, false);
        } catch (RemoteException e) {
            Log.e("IPC","", e);
        }
    }

    /**
     * 发送消息并回调
     * @param targetKey
     * @param sourceKey
     * @param message
     * @param ipcCallbackListener
     */
    public void sendMessageAndCallback(String targetKey, String sourceKey, String message, IpcCallbackListener ipcCallbackListener){
        if(ipcInterface == null) return;
        try {
            ipcInterface.reginsterCallback(targetKey, new LIPCCallback(sourceKey, ipcInterface, ipcCallbackListener));
            ipcInterface.sendMessage(targetKey, sourceKey, message, true);
        } catch (RemoteException e) {
            Log.e("IPC","", e);
        }
    }

    /**
     * 取消跨进程服务
     */
    public void unbindIPCService(){
        if(this.serviceConnection != null){
            this.wrapper.unbindService(serviceConnection);
            this.serviceConnection = null;
        }
        this.wrapper = null;
    }

    private final IPCCallback.Stub ipcCallback = new IPCCallback.Stub() {

        @Override
        public void receiveMessages(String sourceKey, String message, boolean isNeedCallback) throws RemoteException {
            if(ipcCallbackListener != null){
                ipcCallbackListener.receiveMessages(sourceKey, message,isNeedCallback);
            }
        }
    };

    public interface IpcCallbackListener{
        void receiveMessages(String sourceKey, String message, boolean isNeedCallback);
    }



}
