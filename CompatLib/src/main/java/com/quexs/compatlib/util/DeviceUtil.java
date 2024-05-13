package com.quexs.compatlib.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

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

    /**
     * 如果使用targetSdkVersion 30或以上的sdk就会拿不到Mac地址 以前获取MAC的设备唯一码的方法被废弃了11之后就不再使用了
     * @return
     */
    public static String getLocalMacAddress(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            return "02:00:00:00:00:00";
        }
        String mac = null;
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
                            mac = str.toString();
                        }
                    }
                }
            }
        }catch (SocketException e){
            e.printStackTrace();
        }
        if(TextUtils.isEmpty(mac)){
            mac = "02:00:00:00:00:00";
        }
        return mac;
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
        }else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.Q){
            try {
                Method method = telephonyManager.getClass().getMethod("getImei", int.class);
                return (String) method.invoke(telephonyManager, 0);// 根据需求返回
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Settings.Secure.getString(context.getContentResolver(),Settings.Secure.ANDROID_ID);
    }
}
