package com.quexs.cameraxlib.camera;

import android.app.Application;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.quexs.cameraxlib.task.TaskRunnable;
import com.quexs.cameraxlib.util.ImageFileUtil;

import java.io.File;
import java.util.concurrent.Callable;

/**
 * @author Quexs
 * @description:
 * @date :2023/9/3 14:27
 */
public class TakeCameraViewModel extends AndroidViewModel {

    private final MutableLiveData<String> pathData;

    public TakeCameraViewModel(@NonNull Application application) {
        super(application);
        pathData = new MutableLiveData<>();
    }

    public MutableLiveData<String> getPathData() {
        return pathData;
    }

    public void onTaskGetPath(){
        TaskRunnable.getInstance().executeCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                File parentFile = getApplication().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File lastModifiedFile = ImageFileUtil.getLastModifiedImageFileForDirectory(parentFile);
                return lastModifiedFile == null ? "" : lastModifiedFile.getPath();
            }
        }, new TaskRunnable.OnCompletedCallback<String>() {
            @Override
            public void onComplete(@Nullable String result) {
                pathData.setValue(result);
            }
        });
    }
}
