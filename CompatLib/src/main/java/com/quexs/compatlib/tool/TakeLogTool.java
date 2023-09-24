package com.quexs.compatlib.tool;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.IntDef;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author Quexs
 * @description: 日志打印工具类（json字符格式显示）
 * @date: 2023/9/9 18:24
 */
public class TakeLogTool {

    @IntDef({TakeLogTool.LogLevel.VERBOSE, TakeLogTool.LogLevel.DEBUG,  TakeLogTool.LogLevel.INFO,TakeLogTool.LogLevel.WARN, TakeLogTool.LogLevel.ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LogLevel {
        //用于打印那些最为琐碎的、意义最小的日志信息。对应级别verbose，是Android日志里面级别最低的一种
        int VERBOSE = 0;
        //用于打印一些调试信息，这些信息对你调试程序和分析问题应该是有帮助的。对应级别debug，比verbose高一级
        int DEBUG = 1;
        //用于打印一些比较重要的数据，这些数据应该是你非常想看到的、可以帮你分析用户行为的数据。对应级别info，比debug高一级
        int INFO = 2;
        //用于打印一些警告信息，提示程序在这个地方可能会有潜在的风险，最好去修复一下这些出现警告的地方。对应级别warn，比info高一级
        int WARN = 3;
        //用于打印程序中的错误信息，比如程序进入了catch语句中。当有错误信息打印
        int ERROR = 4;
    }

    private static volatile TakeLogTool instance;
    private final ThreadPoolExecutor mThreadPool;
    private boolean isEnable;

    public static TakeLogTool getInstance(){
        if(instance == null){
            synchronized (TakeLogTool.class){
                if(instance == null){
                    instance = new TakeLogTool();
                }
            }
        }
        return instance;
    }

    private TakeLogTool(){
        mThreadPool = new ThreadPoolExecutor(1,1, 100L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>());
        mThreadPool.allowCoreThreadTimeOut(true);
    }

    /**
     * 打印Log
     * @param tag
     * @param msg
     * @param level
     */
    public void myLog(String tag, String msg, @LogLevel int level){
        mThreadPool.execute(new LogRunnable(tag,msg,level));
    }

    /**
     * 启用日志打印
     */
    public void enable(){
        isEnable = true;
    }

    private class LogRunnable implements Runnable{
        private final String tag;
        private String msg;
        private final @LogLevel int level;
        protected LogRunnable(String tag, String msg, @LogLevel int level){
            this.tag = tag;
            this.msg = msg;
            this.level = level;
        }

        @Override
        public void run() {
            if(isEnable && !TextUtils.isEmpty(msg)){
                if(new JsonValidator().validate(msg)){
                    //json格式字符串
                    logMsg(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════",level);
                    if(Pattern.compile("\\{.*\\}").matcher(msg).find()){
                        try {
                            JSONObject jsonObject = new JSONObject(msg);
                            msg = jsonObject.toString(4);
                        } catch (JSONException e) {
                            logMsg(tag, msg, level);
                        }
                    }else if(Pattern.compile("\\[.*\\]").matcher(msg).find()){
                        try {
                            JSONArray jsonObject = new JSONArray(msg);
                            msg = jsonObject.toString(4);
                        } catch (JSONException e) {
                            logMsg(tag, msg, level);
                        }
                    }
                    String lineSeparator = System.getProperty("line.separator");
                    if(lineSeparator == null){
                        lineSeparator = "\r\n|\r";
                    }
                    String[] lines = msg.split(lineSeparator);
                    for(String line : lines){
                        logMsg(tag, "║ " + line, level);
                    }
                    logMsg(tag, "╚════════════════════════════════════════════════════════════════════════════════════════",level);
                }else {
                    //普通字符串
                    logMsg(tag,msg,level);
                }

            }

        }
    }

    private void logMsg(String tag,String msg, @LogLevel int level){
        switch (level) {
            case LogLevel.VERBOSE:
                Log.v(tag, msg);
                break;
            case LogLevel.DEBUG:
                Log.d(tag, msg);
                break;
            case LogLevel.INFO:
                Log.i(tag, msg);
                break;
            case LogLevel.WARN:
                Log.w(tag, msg);
                break;
            case LogLevel.ERROR:
                Log.e(tag, msg);
                break;
        }
    }

}
