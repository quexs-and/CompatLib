package com.quexs.compatlib.util;

import android.content.Context;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2023/7/28
 * <p>
 * Time: 17:55
 * <p>
 * 备注：dp 和 px 转换
 */
public class DensityUtil {

    /**
     * 根据手机的分辨率从 dp(相对大小) 的单位 转成为 px(像素)
     */
    static public int dpToPx(Context context, float dpValue) {
        // 结果+0.5是为了int取整时更接近
        return dpToPx(getScale(context), dpValue);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp(相对大小)
     */
    static public int pxToDp(Context context, float pxValue) {
        return pxToDp(getScale(context),pxValue);
    }

    static public float getScale(Context context){
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 根据手机的分辨率从 dp(相对大小) 的单位 转成为 px(像素)
     */
    static public int dpToPx(float scale, float dpValue) {
        // 结果+0.5是为了int取整时更接近
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp(相对大小)
     */
    static public int pxToDp(float scale, float pxValue) {
        return (int) (pxValue / scale + 0.5f);
    }

    static public float dpToPxGetFloat(Context context, float dpValue){
        return dpValue * getScale(context);
    }

}
