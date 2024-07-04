package com.quexs.compatlib.wheel.util;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ApkUtils {

    /**
     * 获取进程名称
     * @return
     */
    static public String getProcessName(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Application.getProcessName();
        }
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread", false, Application.class.getClassLoader());
            Method declaredMethod = activityThreadClass.getDeclaredMethod("currentProcessName");
            declaredMethod.setAccessible(true);
            final Object invoke = declaredMethod.invoke(null);
            if (invoke instanceof String) {
                return (String) invoke;
            }
        } catch (Exception e) {
            Log.e("ProcessName","", e);
        }
        return null;
    }

    /**
     * 获取栈顶Activity
     * @return
     */
    static public Activity getTopActivity(){
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread", false, Application.class.getClassLoader());
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            currentActivityThreadMethod.setAccessible(true);
            Object activityThread = currentActivityThreadMethod.invoke(null);
            // 获取mActivities字段
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            ArrayMap<?, ?> activities = (ArrayMap<?, ?>) activitiesField.get(activityThread);
            // 遍历mActivities，获取栈顶Activity
            for (Object activityRecord : activities.values()) {
                Class<?> activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!(boolean) pausedField.get(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            Log.e("TopActivity","", e);
        }
        return null;
    }

}
