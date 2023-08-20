package com.quexs.compatlib.util;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetPermission;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2023/8/20
 * <p>
 * Time: 21:41
 * <p>
 * 备注： 设备相关
 */
public class DeviceUtil {

    /**
     * 移动网络获取本机IP地址
     * IPV4
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> enumNet = NetworkInterface.getNetworkInterfaces(); enumNet.hasMoreElements();){
                NetworkInterface networkInterface = enumNet.nextElement();
                for (Enumeration<InetAddress> enumIP = networkInterface.getInetAddresses(); enumIP.hasMoreElements();){
                    InetAddress inetAddress = enumIP.nextElement();
                    //如果不是回环地址
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address){
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取IP地址
     * IPV6
     * @return
     */
    public static String getLocalIpV6Address() {
        try {
            for (Enumeration<NetworkInterface> enumNet = NetworkInterface.getNetworkInterfaces(); enumNet.hasMoreElements();){
                NetworkInterface networkInterface = enumNet.nextElement();
                for (Enumeration<InetAddress> enumIP = networkInterface.getInetAddresses(); enumIP.hasMoreElements();){
                    InetAddress inetAddress = enumIP.nextElement();
                    //如果不是回环地址
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()){
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getMacAddress(){
        try {
            for (Enumeration<NetworkInterface> enumNet = NetworkInterface.getNetworkInterfaces(); enumNet.hasMoreElements();){
                NetworkInterface networkInterface = enumNet.nextElement();
                if (networkInterface != null && !TextUtils.isEmpty(networkInterface.getName())) {
                    if ("wlan0".equalsIgnoreCase(networkInterface.getName())) {
                        byte[] macBytes = networkInterface.getHardwareAddress();
                        if (macBytes != null && macBytes.length > 0) {
                            StringBuilder str = new StringBuilder();
                            for (byte b : macBytes) {
                                str.append(String.format("%02X:", b));
                            }
                            if (str.length() > 0) {
                                str.deleteCharAt(str.length() - 1);
                            }
                            return str.toString();
                        }
                    }

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getLocalMacForLocalIp(){
        try {
            for (Enumeration<NetworkInterface> enumNet = NetworkInterface.getNetworkInterfaces(); enumNet.hasMoreElements();){
                NetworkInterface networkInterface = enumNet.nextElement();
                for (Enumeration<InetAddress> enumIP = networkInterface.getInetAddresses(); enumIP.hasMoreElements();){
                    InetAddress inetAddress = enumIP.nextElement();
                    //如果不是回环地址
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address){
                        byte[] macBytes = NetworkInterface.getByInetAddress(inetAddress).getHardwareAddress();
                        if (macBytes != null && macBytes.length > 0) {
                            StringBuilder str = new StringBuilder();
                            for (byte b : macBytes) {
                                str.append(String.format("%02X:", b));
                            }
                            if (str.length() > 0) {
                                str.deleteCharAt(str.length() - 1);
                            }
                            return str.toString();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 获取 IMEI
     * @param context
     * @return
     */
    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getLocalImei(Context context){
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            try {
                return telephonyManager.getDeviceId();
            }catch (SecurityException e){
                e.printStackTrace();
            }
        }else if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
            try {
                return telephonyManager.getImei();
            }catch (SecurityException e){
                e.printStackTrace();
            }
        }
        return Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
    }
}
