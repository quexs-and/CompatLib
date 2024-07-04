// IPCCallback.aidl
package com.quexs.compatlib;

// Declare any non-default types here with import statements

interface IPCCallback {
    oneway void receiveMessages(String sourceKey, String message, boolean isNeedCallback);
}