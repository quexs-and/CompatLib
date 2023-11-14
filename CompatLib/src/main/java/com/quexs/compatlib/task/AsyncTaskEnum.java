package com.quexs.compatlib.task;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Quexs
 * @description: 跨进程任务编码
 * @date: 2023/11/15 0:07
 */
public class AsyncTaskEnum {

    @IntDef({TaskCode.FILE_MD5})
    @Retention(RetentionPolicy.SOURCE)
    public @interface TaskCode{
        //获取文件MD5
        int FILE_MD5 = 100001;
    }

}
