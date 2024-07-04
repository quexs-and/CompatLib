package com.quexs.compatlib.ipc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import com.quexs.compatlib.IPCCallback;
import com.quexs.compatlib.IPCInterface;

import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Quexs
 * @description:
 * @date: 2023/11/14 22:21
 */
public class IPCService extends Service {

    private final ConcurrentHashMap<String, IPCCallback> callbackMap = new ConcurrentHashMap<>();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub.asBinder();
    }

    private final IPCInterface.Stub stub = new IPCInterface.Stub() {

        @Override
        public void reginsterCallback(String sourceKey, IPCCallback callback) throws RemoteException {
            callbackMap.put(sourceKey,callback);
        }

        @Override
        public void sendMessage(String targetKey, String sourceKey, String message, boolean isNeedCallback) throws RemoteException {
            IPCCallback callback = callbackMap.get(targetKey);
            if(callback != null){
                callback.receiveMessages(sourceKey, message, isNeedCallback);
            }
        }

        @Override
        public void unreginsterCallback(String targetKey) throws RemoteException {
            callbackMap.remove(targetKey);
        }
    };


}
