package com.quexs.compatlib.ipc;

import android.os.RemoteException;

import com.quexs.compatlib.IPCCallback;
import com.quexs.compatlib.IPCInterface;

public class LIPCCallback extends IPCCallback.Stub{
    private final String ownKey;
    private final IPCInterface ipcInterface;
    private final IPCHelper.IpcCallbackListener ipcCallbackListener;
    public LIPCCallback(String ownKey, IPCInterface ipcInterface, IPCHelper.IpcCallbackListener ipcCallbackListener){
        this.ownKey = ownKey;
        this.ipcInterface = ipcInterface;
        this.ipcCallbackListener = ipcCallbackListener;
    }

    @Override
    public void receiveMessages(String sourceKey, String message, boolean isNeedCallback) throws RemoteException {
        if(ipcCallbackListener != null){
            ipcCallbackListener.receiveMessages(sourceKey, message, isNeedCallback);
        }
        this.ipcInterface.unreginsterCallback(ownKey);
    }
}
