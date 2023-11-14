// IAsyncTaskInterface.aidl
package com.quexs.compatlib;

// Declare any non-default types here with import statements
import com.quexs.compatlib.IAsyncTaskCallback;
interface IAsyncTaskInterface {

     oneway void onCommand(int commandCode, String aString, in IAsyncTaskCallback callback);
}