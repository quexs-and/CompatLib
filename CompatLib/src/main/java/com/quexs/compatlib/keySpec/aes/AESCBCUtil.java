package com.quexs.compatlib.keySpec.aes;

import com.quexs.compatlib.util.HexUtil;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Quexs
 * @description:
 * @date: 2024/1/1 12:38
 */
public class AESCBCUtil {
    //加解密算法/工作模式/填充方式
    static private final String transformation = "AES/CBC/PKCS7Padding";
    static private final String algorithm = "AES";

    /**
     * AES 加密
     *
     * @param keyData         加密密钥
     * @param data            待加密内容
     * @param dataIvParameter 密钥偏移量
     * @return 返回byte[]加密数组
     */
    static public byte[] encrypt(byte[] keyData, byte[] data, byte[] dataIvParameter) throws Exception {
        // 创建AES密钥
        SecretKeySpec skeySpec = new SecretKeySpec(keyData, algorithm);
        // 创建密码器
        Cipher cipher = Cipher.getInstance(transformation);
        // 创建偏移量
        IvParameterSpec iv = new IvParameterSpec(dataIvParameter);
        // 初始化加密器
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
        return cipher.doFinal(data);
    }

    /**
     * AES 加密
     *
     * @param hexKey
     * @param data
     * @param strIvParameter
     * @return
     * @throws Exception
     */
    static public byte[] encrypt(String hexKey, byte[] data, String strIvParameter) throws Exception {
        byte[] keyData = HexUtil.toBytes(hexKey);
        byte[] dataIvParameter = strIvParameter.getBytes(StandardCharsets.UTF_8);
        return encrypt(keyData, data, dataIvParameter);
    }

    /**
     * AES 解密
     *
     * @param keyData 解密密钥
     * @param data   待解密内容
     * @param dataIvParameter 偏移量
     * @return 返回Base64转码后的加密数据
     */
    static public byte[] decrypt(byte[] keyData, byte[] data, byte[] dataIvParameter) throws Exception {
        // 创建AES秘钥
        SecretKeySpec skeySpec = new SecretKeySpec(keyData, algorithm);
        // 创建密码器
        Cipher cipher = Cipher.getInstance(transformation);
        // 创建偏移量
        IvParameterSpec iv = new IvParameterSpec(dataIvParameter);
        // 初始化解密器
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
        return cipher.doFinal(data);
    }

    /**
     * AES 解密
     * @param hexKey 解密密钥
     * @param data 待解密内容
     * @param strIvParameter 偏移量
     * @return
     * @throws Exception
     */
    static public byte[] decrypt(String hexKey, byte[] data, String strIvParameter) throws Exception {
        byte[] keyData = HexUtil.toBytes(hexKey);
        byte[] dataIvParameter = strIvParameter.getBytes(StandardCharsets.UTF_8);
        return decrypt(keyData, data, dataIvParameter);
    }



}
