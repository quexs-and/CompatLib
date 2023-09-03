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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Quexs
 * @description:
 * @date: 2023/9/3 16:09
 */
public class CameraPictureViewModel extends AndroidViewModel {

    private final MutableLiveData<List<File>> fileListData;


    public CameraPictureViewModel(@NonNull Application application) {
        super(application);
        fileListData = new MutableLiveData<>();
        onTaskGetImageList();
    }


    public MutableLiveData<List<File>> getFileListData() {
        return fileListData;
    }

    public void onTaskGetImageList(){
        TaskRunnable.getInstance().executeCallable(new Callable<List<File>>() {
            @Override
            public List<File> call() throws Exception {
                File directory = getApplication().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                List<File> files = ImageFileUtil.getImagesForDirectory(directory);
                if(files != null && !files.isEmpty()){
                    Collections.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            if(o1.lastModified() < o2.lastModified()){
                                return 1;
                            }else if(o1.lastModified() == o2.lastModified()){
                                return 0;
                            }
                            return -1;
                        }
                    });
                }
                return files;
            }
        }, new TaskRunnable.OnCompletedCallback<List<File>>() {
            @Override
            public void onComplete(@Nullable List<File> result) {
                fileListData.setValue(result);
            }
        });
    }
}
