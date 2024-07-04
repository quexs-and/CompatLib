// IPCInterface.aidl
package com.quexs.compatlib;

// Declare any non-default types here with import statements
import com.quexs.compatlib.IPCCallback;
interface IPCInterface {

     oneway void reginsterCallback(String sourceKey, in IPCCallback callback);
     oneway void sendMessage(String targetKey, String sourceKey, String message, boolean isNeedCallback);
     oneway void unreginsterCallback(String targetKey);
}