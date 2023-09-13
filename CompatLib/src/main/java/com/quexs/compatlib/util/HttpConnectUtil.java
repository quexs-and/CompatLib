package com.quexs.compatlib.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author Quexs
 * @description: HttpURLConnection/HttpsURLConnection 简易封装
 * @date: 2023/9/13 21:15
 */
public class HttpConnectUtil {

    /**
     * Get的请求
     * @param url
     * @param params 请求参数
     * @param headers 请求头
     * @return 返回请求成功结果
     * @throws IOException
     */
    static public String httpsGet(String url, Map<String, Object> params, Map<String, String> headers) throws IOException{
        URL httpURL;
        if(params != null){
            StringBuilder builder = new StringBuilder(url);
            builder.append("?");
            for(Map.Entry<String, Object> entry : params.entrySet()){
                if(builder.length() > 0){
                    builder.append("&");
                }
                builder.append(URLEncoder.encode(entry.getKey(),"UTF-8"));
                builder.append("=");
                builder.append(URLEncoder.encode(String.valueOf(entry.getValue()),"UTF-8"));
            }
            httpURL = new URL(builder.toString());
        }else {
            httpURL = new URL(url);
        }
        HttpsURLConnection conn = (HttpsURLConnection) httpURL.openConnection();
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
            return callbackResult(conn.getInputStream());
        }
        return "";
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
        URL httpURL = new URL(url);
        HttpsURLConnection conn = (HttpsURLConnection) httpURL.openConnection();
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
                builder.append(URLEncoder.encode(entry.getKey(),"UTF-8"));
                builder.append("=");
                builder.append(URLEncoder.encode(String.valueOf(entry.getValue()),"UTF-8"));
            }
            outPut.write(builder.toString().getBytes());
            outPut.flush();
        }
        int responseCode = conn.getResponseCode();
        if(responseCode == 200){
            return callbackResult(conn.getInputStream());
        }
        return "";
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
        URL httpURL = new URL(url);
        HttpsURLConnection conn = (HttpsURLConnection) httpURL.openConnection();
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
        outPut.write(paramJson.getBytes());
        outPut.flush();
        int responseCode = conn.getResponseCode();
        if(responseCode == 200){
            return callbackResult(conn.getInputStream());
        }
        return "";
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
        URL httpURL;
        if(params != null){
            StringBuilder builder = new StringBuilder(url);
            builder.append("?");
            for(Map.Entry<String, Object> entry : params.entrySet()){
                if(builder.length() > 0){
                    builder.append("&");
                }
                builder.append(URLEncoder.encode(entry.getKey(),"UTF-8"));
                builder.append("=");
                builder.append(URLEncoder.encode(String.valueOf(entry.getValue()),"UTF-8"));
            }
            httpURL = new URL(builder.toString());
        }else {
            httpURL = new URL(url);
        }
        HttpURLConnection conn = (HttpURLConnection) httpURL.openConnection();
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
            return callbackResult(conn.getInputStream());
        }
        return "";
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
        URL httpURL = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) httpURL.openConnection();
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
                builder.append(URLEncoder.encode(entry.getKey(),"UTF-8"));
                builder.append("=");
                builder.append(URLEncoder.encode(String.valueOf(entry.getValue()),"UTF-8"));
            }
            outPut.write(builder.toString().getBytes());
            outPut.flush();
        }

        int responseCode = conn.getResponseCode();
        if(responseCode == 200){
            return callbackResult(conn.getInputStream());
        }
        return "";
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
        URL httpURL = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) httpURL.openConnection();
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
        outPut.write(paramJson.getBytes());
        outPut.flush();
        int responseCode = conn.getResponseCode();
        if(responseCode == 200){
            return callbackResult(conn.getInputStream());
        }
        return "";
    }



    /**
     * 通过字节输入流返回一个字符串信息
     * @param is
     * @return 返回请求成功结果
     * @throws IOException
     */
    private static String callbackResult(InputStream is) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while((len = is.read(buffer)) != -1){
            bos.write(buffer, 0, len);
        }
        is.close();
        String result = bos.toString();// 把流中的数据转换成字符串, 采用的编码是: utf-8
        bos.close();
        return result;
    }

}
