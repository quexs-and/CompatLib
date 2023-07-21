package com.quexs.compatlib.util;

import android.view.View;
import com.quexs.compatlib.R;
import java.util.Calendar;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2023/7/21
 * <p>
 * Time: 23:44
 * <p>
 * 备注：
 */
public class ViewTouchUtil {

    /**
     * 是否有效点击
     * @param view
     * @param intervalMillisecond 间隔时长(单位毫秒)
     * @return
     */
    public static boolean isValidClick(View view, long intervalMillisecond){
        long curTime = Calendar.getInstance().getTimeInMillis();
        Object enableTag = view.getTag(R.string.view_key_enable_delay);
        if(enableTag == null){
            view.setTag(R.string.view_key_enable_delay, curTime);
            return true;
        }
        long lastTime = (long) enableTag;
        if(curTime - lastTime >= intervalMillisecond){
            view.setTag(R.string.view_key_enable_delay, curTime);
            return true;
        }
        return false;
    }

}
