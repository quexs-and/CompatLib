package com.quexs.compatlib.compat.keySpec.rsa;

import android.util.Base64;

import com.quexs.compatlib.tool.TakeLogTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;

import javax.crypto.Cipher;

/**
 * RSA加密算法 私钥 加密、签名
 * @author Quexs
 * @description:
 * @date: 2023/12/16 0:15
 */
public class RSAPrvtCompat {

    //公钥
    private RSAPrivateKey rsaPrivateKey;

    /**
     * Base64字符串公钥
     * @param privateKeyBase64
     */
    public RSAPrvtCompat(String privateKeyBase64){
        this.rsaPrivateKey = restorePrivateKey(Base64.decode(privateKeyBase64,Base64.NO_WRAP));
    }

    /**
     * 公钥 publicKeyEncoded
     * @param privateKeyEncoded
     */
    public RSAPrvtCompat(byte[] privateKeyEncoded) {
        this.rsaPrivateKey = restorePrivateKey(privateKeyEncoded);
    }

    /**
     * 公钥-文件流
     * @param in
     */
    public RSAPrvtCompat(InputStream in) {
        this.rsaPrivateKey = restorePrivateKey(Base64.decode(readKey(in),Base64.NO_WRAP));
    }

    /**
     * 使用N、d值还原私钥
     * @param modulus
     * @param privateExponent
     */
    public RSAPrvtCompat(String modulus, String privateExponent) {
        BigInteger bigIntModulus = new BigInteger(modulus);
        BigInteger bigIntPrivateExponent = new BigInteger(privateExponent);
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(bigIntModulus, bigIntPrivateExponent);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.rsaPrivateKey = (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            TakeLogTool.getInstance().myLog("RSA", "restore private key error", e, TakeLogTool.LogLevel.ERROR);
        }
    }

    /**
     *
     * @return
     */
    public RSAPrivateKey getRSAPrivateKey() {
        return this.rsaPrivateKey;
    }

    /**
     * 私钥加密-ECB模式
     * @param data
     * @return
     */
    public byte[] encrypt(byte[] data){
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            // 编码前设定编码方式及密钥
            cipher.init(Cipher.ENCRYPT_MODE, rsaPrivateKey);
            // 传入编码数据并返回编码结果
            return cipher.doFinal(data);
        } catch (Exception e) {
            TakeLogTool.getInstance().myLog("RSA", "private key encrypt error", e, TakeLogTool.LogLevel.ERROR);
        }
        return null;
    }

    /**
     * 私钥解密-ECB模式 (对公钥加密的数据解密)
     * @param data
     * @return
     */
    public byte[] decrypt(byte[] data){
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            // 编码前设定编码方式及密钥
            cipher.init(Cipher.DECRYPT_MODE, this.rsaPrivateKey);
            // 传入编码数据并返回编码结果
            return cipher.doFinal(data);
        } catch (Exception e) {
            TakeLogTool.getInstance().myLog("RSA", "private key decrypt error", e, TakeLogTool.LogLevel.ERROR);
        }
        return null;
    }

    /**
     * 私钥签名
     * @param data
     * @return
     */
    public byte[] sign(byte[] data){
        try {
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initSign(this.rsaPrivateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            TakeLogTool.getInstance().myLog("RSA", "sign error, sign: {}", e, TakeLogTool.LogLevel.ERROR);
        }
        return null;
    }

    /**
     * 私钥签名
     * @param data
     * @return
     */
    public String signToString(byte[] data){
        return Base64.encodeToString(sign(data), Base64.NO_WRAP);
    }


    /**
     * 私钥还原
     * 通过公钥byte[](privateKey.getEncoded())将公钥还原，适用于RSA算法
     * @param privateKeyEncoded
     * @return
     */
    private RSAPrivateKey restorePrivateKey(byte[] privateKeyEncoded) {
        try {
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyEncoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey)keyFactory.generatePrivate(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            TakeLogTool.getInstance().myLog("RSA", "restore private key error", e, TakeLogTool.LogLevel.ERROR);
        }
        return null;
    }

    private String readKey(InputStream in) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String readLine;
            StringBuilder sb = new StringBuilder();
            while ((readLine = br.readLine()) != null) {
                if (readLine.charAt(0) == '-') {
                    continue;
                }
                sb.append(readLine);
                sb.append('\r');
            }
            br.close();
            return sb.toString();
        }catch (IOException e){
            TakeLogTool.getInstance().myLog("RSA", "restore private key inputStream read error", e, TakeLogTool.LogLevel.ERROR);
        }
        return null;
    }


}
