package com.quexs.compatlib.wheel.util;

import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Quexs
 * @description: HttpURLConnection/HttpsURLConnection 简易封装
 * @date: 2023/9/13 21:15
 */
public class HttpUtils {

    /**
     * Get的请求
     * @param url
     * @param params 请求参数
     * @param headers 请求头
     * @return 返回请求成功结果
     * @throws IOException
     */
    static public String httpsGet(String url, Map<String, Object> params, Map<String, String> headers) throws IOException {
        HttpsURLConnection conn = null;
        try {
            URL httpURL;
            if(params != null && !params.isEmpty()){
                Uri.Builder builder = Uri.parse(url).buildUpon();
                for(Map.Entry<String, Object> entry : params.entrySet()){
                    builder.appendQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
                }
                httpURL = new URL(builder.build().toString());
            }else {
                httpURL = new URL(url);
            }
            conn = (HttpsURLConnection) httpURL.openConnection();
            conn.setRequestMethod("GET");//GET和POST必须全大写
            conn.setConnectTimeout(10 * 1000);//连接的超时时间
            conn.setReadTimeout(30 * 1000);//读数据的超时时间
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept", "*/*");
            if(headers != null){
                for(Map.Entry<String, String> entry : headers.entrySet()){
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            int responseCode = conn.getResponseCode();
            if(responseCode == 200){
                return callbackResult(conn);
            }
            return "";
        }catch (IOException e){
            throw e;
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }
    }

    /**
     * POST 请求
     * @param url
     * @param params 请求参数
     * @param headers 请求头
     * @return 返回请求成功结果
     * @throws IOException
     */
    static public String httpsPost(String url, Map<String, Object> params, Map<String, String> headers) throws IOException{
        HttpsURLConnection conn = null;
        try {
            URL httpURL = new URL(url);
            conn = (HttpsURLConnection) httpURL.openConnection();
            conn.setRequestMethod("POST");//GET和POST必须全大写
            conn.setConnectTimeout(10 * 1000);//连接的超时时间
            conn.setReadTimeout(30 * 1000);//读数据的超时时间
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept", "*/*");
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            if(headers != null){
                for(Map.Entry<String, String> entry : headers.entrySet()){
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            conn.setDoOutput(true);
            conn.connect();
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式
            if(params != null){
                DataOutputStream outPut = new DataOutputStream(conn.getOutputStream());
                StringBuilder builder = new StringBuilder();
                for(Map.Entry<String, Object> entry : params.entrySet()){
                    if(builder.length() > 0){
                        builder.append("&");
                    }
                    builder.append(entry.getKey());
                    builder.append("=");
                    builder.append(entry.getValue());
                }
                outPut.write(builder.toString().getBytes(StandardCharsets.UTF_8));
                outPut.flush();
            }
            int responseCode = conn.getResponseCode();
            if(responseCode == 200){
                return callbackResult(conn);
            }
            return "";
        }catch (IOException e){
            throw e;
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }
    }

    /**
     * POST
     * @param url
     * @param paramJson 请求参数
     * @param headers 请求头
     * @return 返回请求成功结果
     * @throws IOException
     */
    static public String httpsPostJson(String url, String paramJson, Map<String, String> headers) throws IOException{
        HttpsURLConnection conn = null;
        try {
            URL httpURL = new URL(url);
            conn = (HttpsURLConnection) httpURL.openConnection();
            conn.setRequestMethod("POST");//GET和POST必须全大写
            conn.setConnectTimeout(10 * 1000);//连接的超时时间
            conn.setReadTimeout(30 * 1000);//读数据的超时时间
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept", "*/*");
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            if(headers != null){
                for(Map.Entry<String, String> entry : headers.entrySet()){
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            conn.setDoOutput(true);
            conn.connect();
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式
            DataOutputStream outPut = new DataOutputStream(conn.getOutputStream());
            outPut.write(paramJson.getBytes(StandardCharsets.UTF_8));
            outPut.flush();
            int responseCode = conn.getResponseCode();
            if(responseCode == 200){
                return callbackResult(conn);
            }
            return "";
        }catch (IOException e){
            throw e;
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }
    }

    /**
     *
     * @param url
     * @param paramXml
     * @param headers
     * @return
     * @throws IOException
     */
    static public String httpsPostXml(String url, String paramXml, Map<String, String> headers) throws IOException{
        HttpsURLConnection conn = null;
        try {
            URL httpURL = new URL(url);
            conn = (HttpsURLConnection) httpURL.openConnection();
            conn.setRequestMethod("POST");//GET和POST必须全大写
            conn.setConnectTimeout(10 * 1000);//连接的超时时间
            conn.setReadTimeout(30 * 1000);//读数据的超时时间
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept", "*/*");
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            if(headers != null){
                for(Map.Entry<String, String> entry : headers.entrySet()){
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            conn.setDoOutput(true);
            conn.connect();
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式
            DataOutputStream outPut = new DataOutputStream(conn.getOutputStream());
            outPut.write(paramXml.getBytes(StandardCharsets.UTF_8));
            outPut.flush();
            int responseCode = conn.getResponseCode();
            if(responseCode == 200){
                return callbackResult(conn);
            }
            return "";
        }catch (IOException e){
            throw e;
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

    }

    /**
     * Get的请求
     * @param url
     * @param params 请求参数
     * @param headers 请求头
     * @return 返回请求成功结果
     * @throws IOException
     */
    static public String httpGet(String url, Map<String, Object> params,Map<String, String> headers) throws IOException{
        HttpURLConnection conn = null;
        try {
            URL httpURL;
            if(params != null && !params.isEmpty()){
                Uri.Builder builder = Uri.parse(url).buildUpon();
                for(Map.Entry<String, Object> entry : params.entrySet()){
                    builder.appendQueryParameter(entry.getKey(), String.valueOf(entry.getValue()));
                }
                httpURL = new URL(builder.build().toString());
            }else {
                httpURL = new URL(url);
            }
            conn = (HttpURLConnection) httpURL.openConnection();
            conn.setRequestMethod("GET");//GET和POST必须全大写
            conn.setConnectTimeout(10 * 1000);//连接的超时时间
            conn.setReadTimeout(30 * 1000);//读数据的超时时间
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept", "*/*");
            if(headers != null){
                for(Map.Entry<String, String> entry : headers.entrySet()){
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            int responseCode = conn.getResponseCode();
            if(responseCode == 200){
                return callbackResult(conn);
            }
            return "";
        }catch (IOException e){
            throw e;
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }


    }

    /**
     * POST 请求
     * @param url
     * @param params 请求参数
     * @param headers 请求头
     * @return 返回请求成功结果
     * @throws IOException
     */
    static public String httpPost(String url, Map<String, Object> params, Map<String, String> headers) throws IOException{
        HttpURLConnection conn = null;
        try {
            URL httpURL = new URL(url);
            conn = (HttpURLConnection) httpURL.openConnection();
            conn.setRequestMethod("POST");//GET和POST必须全大写
            conn.setConnectTimeout(10 * 1000);//连接的超时时间
            conn.setReadTimeout(30 * 1000);//读数据的超时时间
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept", "*/*");
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            if(headers != null){
                for(Map.Entry<String, String> entry : headers.entrySet()){
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            conn.setDoOutput(true);
            conn.connect();
            //入参
            if(params != null){
                // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式
                DataOutputStream outPut = new DataOutputStream(conn.getOutputStream());
                StringBuilder builder = new StringBuilder();
                for(Map.Entry<String, Object> entry : params.entrySet()){
                    if(builder.length() > 0){
                        builder.append("&");
                    }
                    builder.append(entry.getKey());
                    builder.append("=");
                    builder.append(entry.getValue());
                }
                outPut.write(builder.toString().getBytes(StandardCharsets.UTF_8));
                outPut.flush();
            }

            int responseCode = conn.getResponseCode();
            if(responseCode == 200){
                return callbackResult(conn);
            }
            return "";
        }catch (IOException e){
            throw e;
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }
    }



    /**
     *
     * @param url
     * @param paramJson 请求参数
     * @param headers 请求头
     * @return 返回请求成功结果
     * @throws IOException
     */
    static public String httpPostJson(String url, String paramJson, Map<String, String> headers) throws IOException{
        HttpURLConnection conn = null;
        try {
            URL httpURL = new URL(url);
            conn = (HttpURLConnection) httpURL.openConnection();
            conn.setRequestMethod("POST");//GET和POST必须全大写
            conn.setConnectTimeout(10 * 1000);//连接的超时时间
            conn.setReadTimeout(30 * 1000);//读数据的超时时间
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            if(headers != null){
                for(Map.Entry<String, String> entry : headers.entrySet()){
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            conn.setDoOutput(true);
            conn.connect();
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式
            DataOutputStream outPut = new DataOutputStream(conn.getOutputStream());
            outPut.write(paramJson.getBytes(StandardCharsets.UTF_8));
            outPut.flush();
            int responseCode = conn.getResponseCode();
            if(responseCode == 200){
                return callbackResult(conn);
            }
            return "";
        }catch (IOException e){
            throw e;
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

    }

    /**
     *
     * @param url
     * @param paramXml
     * @param headers
     * @return
     * @throws IOException
     */
    static public String httpPostXml(String url, String paramXml, Map<String, String> headers) throws IOException{
        HttpURLConnection conn = null;
        try {
            URL httpURL = new URL(url);
            conn = (HttpURLConnection) httpURL.openConnection();
            conn.setRequestMethod("POST");//GET和POST必须全大写
            conn.setConnectTimeout(10 * 1000);//连接的超时时间
            conn.setReadTimeout(30 * 1000);//读数据的超时时间
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept", "*/*");
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            if(headers != null){
                for(Map.Entry<String, String> entry : headers.entrySet()){
                    conn.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            conn.setDoOutput(true);
            conn.connect();
            DataOutputStream outPut = new DataOutputStream(conn.getOutputStream());
            outPut.write(paramXml.getBytes(StandardCharsets.UTF_8));
            outPut.flush();
            int responseCode = conn.getResponseCode();
            if(responseCode == 200){
                return callbackResult(conn);
            }
            return "";
        }catch (IOException e){
            throw e;
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }
    }

//    /**
//     *
//     * @param params
//     * @return
//     */
//   static public String getXmlFromParam(Map<String, Object> params, boolean isAddPrefix) {
//        StringBuilder builder = new StringBuilder();
//        if(isAddPrefix){
//            builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
//        }
//        for(Map.Entry<String, Object> entry : params.entrySet()){
//            builder.append("<").append(entry.getKey()).append(">");
//            Object obj = entry.getValue();
//            if(obj instanceof List){
//                List<Map<String, Object>> list = (List<Map<String, Object>>) obj;
//                for(Map<String, Object> map : list){
//                    builder.append(getXmlFromParam(map,false));
//                }
//            }else if(obj instanceof Map){
//                builder.append(getXmlFromParam((Map<String, Object>) obj,false));
//            }else {
//                builder.append(obj);
//            }
//            builder.append("</").append(entry.getKey()).append(">");
//        }
//
//        return builder.toString();
//    }

    /**
     * 通过字节输入流返回一个字符串信息
     * @param conn
     * @return 返回请求成功结果
     * @throws IOException
     */
    private static String callbackResult(HttpURLConnection conn) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); InputStream is = conn.getInputStream()){
            byte[] buffer = new byte[1024];
            int len;
            while((len = is.read(buffer)) != -1){
                bos.write(buffer, 0, len);
            }
            bos.flush();
            // 把流中的数据转换成字符串, 采用的编码是: utf-8
            conn.disconnect();
            return bos.toString();
        }
    }

}
