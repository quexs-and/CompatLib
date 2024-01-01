package com.quexs.compatlib.util;

/**
 * @author Quexs
 * @description:
 * @date: 2024/1/1 13:00
 */
public class HexUtil {

    /**
     * 16进制字符串 转 字节数组
     * @param hexStr 16进制字符串
     * @return
     */
    static public byte[] toBytes(String hexStr) {
        char[] hexChars = hexStr.toCharArray();
        int length = hexStr.length() / 2;
        byte[] bytes = new byte[length];
        int pos;
        for (int i = 0; i < length; i++) {
            pos = i * 2;
            bytes[i] = (byte) Integer.parseInt(String.valueOf(hexChars[pos]) + hexChars[pos + 1], 16);
        }
        return bytes;
    }

    /**
     * 字节数组 转 16进制字符串
     * @param bytes 字节数组
     * @param isSpaceDisplay 是否空格显示
     * @return 16进制字符串（空格隔开）
     */
    static public String toHexStr(byte[] bytes, boolean isSpaceDisplay) {
        StringBuilder builder = new StringBuilder();
        for(byte b : bytes){
            if(isSpaceDisplay && builder.length() > 0){
                builder.append(" ");
            }
            String hex = Integer.toHexString(b & 0xff);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString().toUpperCase();
    }

    /**
     * 字符数组转16进制字符串
     * @param chars 字符数组
     * @param isSpaceDisplay 是否空格显示
     * @return 16进制字符串（空格隔开）
     */
   static public String toHexStr(char[] chars, boolean isSpaceDisplay){
        StringBuilder builder = new StringBuilder();
        for(char ch : chars){
            if(isSpaceDisplay && builder.length() > 0){
                builder.append(" ");
            }
            String hex = Integer.toHexString(ch);
            if(hex.length() < 2){
                builder.append("0");
            }
            builder.append(hex);
        }
        return builder.toString().toUpperCase();
    }

}
