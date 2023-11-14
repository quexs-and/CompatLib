// IAsyncTaskCallback.aidl
package com.quexs.compatlib;

// Declare any non-default types here with import statements

interface IAsyncTaskCallback {

    oneway void onResult(String aString);
}