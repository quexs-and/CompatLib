package com.quexs.compatlib.compat.keySpec.rsa;

import android.util.Base64;

import com.quexs.compatlib.tool.TakeLogTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

/**
 * RSA加密算法 公钥 加密、验签
 * @author Quexs
 * @description:
 * @date: 2023/12/16 0:15
 */
public class RSAPubCompat {

    //公钥
    private RSAPublicKey rsaPublicKey;

    /**
     * Base64字符串公钥
     * @param publicKeyBase64
     */
    public RSAPubCompat(String publicKeyBase64){
        this.rsaPublicKey = restorePublicKey(Base64.decode(publicKeyBase64,Base64.NO_WRAP));
    }

    /**
     * 公钥 publicKeyEncoded
     * @param publicKeyEncoded
     */
    public RSAPubCompat(byte[] publicKeyEncoded) {
        this.rsaPublicKey = restorePublicKey(publicKeyEncoded);
    }

    /**
     * 公钥-文件流
     * @param in
     */
    public RSAPubCompat(InputStream in){
        this.rsaPublicKey = restorePublicKey(Base64.decode(readKey(in),Base64.NO_WRAP));
    }

    /**
     * 使用N、e值还原公钥
     * @param modulus
     * @param publicExponent
     */
    public RSAPubCompat(String modulus, String publicExponent) {
        BigInteger bigIntModulus = new BigInteger(modulus);
        BigInteger bigIntPrivateExponent = new BigInteger(publicExponent);
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(bigIntModulus, bigIntPrivateExponent);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.rsaPublicKey = (RSAPublicKey)keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            TakeLogTool.getInstance().myLog("RSA", "restore public key error", e, TakeLogTool.LogLevel.ERROR);
        }
    }

    /**
     *
     * @return
     */
    public RSAPublicKey getRsaPublicKey() {
        return this.rsaPublicKey;
    }

    /**
     * 公钥加密-ECB模式
     * @param data
     * @return
     */
    public byte[] encrypt(byte[] data){
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            // 编码前设定编码方式及密钥
            cipher.init(Cipher.ENCRYPT_MODE, this.rsaPublicKey);
            // 传入编码数据并返回编码结果
            return cipher.doFinal(data);
        } catch (Exception e) {
            TakeLogTool.getInstance().myLog("RSA", "public key encrypt error", e, TakeLogTool.LogLevel.ERROR);
        }
        return null;
    }

    /**
     * 公钥解密-ECB模式 (对私钥加密的数据解密)
     * @param data
     * @return
     */
    public byte[] decrypt(byte[] data){
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            // 编码前设定编码方式及密钥
            cipher.init(Cipher.DECRYPT_MODE, this.rsaPublicKey);
            // 传入编码数据并返回编码结果
            return cipher.doFinal(data);
        } catch (Exception e) {
            TakeLogTool.getInstance().myLog("RSA", "public key decrypt error", e, TakeLogTool.LogLevel.ERROR);
        }
        return null;
    }

    /**
     * 公钥验签
     * @param originData
     * @param signData
     * @return
     */
    public boolean verify(String originData, byte[] signData){
        try {
            Signature signature = Signature.getInstance("SHA1WithRSA");
            signature.initVerify(this.rsaPublicKey);
            signature.update(originData.getBytes(StandardCharsets.UTF_8));
            return signature.verify(signData);
        } catch (Exception e) {
            TakeLogTool.getInstance().myLog("RSA", "verify sign error, content: {}, sign: {}", e, TakeLogTool.LogLevel.ERROR);
        }
        return false;
    }

    /**
     * 公钥还原
     * 通过公钥byte[](publicKey.getEncoded())将公钥还原，适用于RSA算法
     * @param publicKeyEncoded
     * @return
     */
    private RSAPublicKey restorePublicKey(byte[] publicKeyEncoded) {
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyEncoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey)keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            TakeLogTool.getInstance().myLog("RSA", "restore public key error", e, TakeLogTool.LogLevel.ERROR);
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
            TakeLogTool.getInstance().myLog("RSA", "restore public key Reade error", e, TakeLogTool.LogLevel.ERROR);
        }
        return null;
    }


}
