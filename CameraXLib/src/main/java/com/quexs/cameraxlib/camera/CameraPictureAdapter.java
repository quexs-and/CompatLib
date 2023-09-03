package com.quexs.cameraxlib.camera;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.quexs.cameraxlib.R;
import com.quexs.cameraxlib.task.TaskRunnable;
import com.quexs.cameraxlib.util.DensityUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Quexs
 * @description:
 * @date :2023/9/3 14:04
 */
public class CameraPictureAdapter extends RecyclerView.Adapter<CameraPictureAdapter.ViewHolder>{
    private final ArrayList<File> items = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.camerax_lib_item_camera_picture, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(holder.photoView)
                .load(getItem(position))
                .into(holder.photoView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public File getItem(int position){
        return items.get(position);
    }

    public void addItems(List<File> list){
        int oldCount = items.size();
        if(oldCount > 0){
            notifyItemRangeRemoved(0, oldCount);
            items.clear();
        }
        int addCount = list == null ? 0 : list.size();
        if(addCount > 0){
            items.addAll(list);
            notifyItemRangeInserted(0, addCount);
        }
    }

    public void removeItem(int position){
        TaskRunnable.getInstance().executeCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                File file = getItem(position);
                return file == null || file.exists() && file.delete();
            }
        }, new TaskRunnable.OnCompletedCallback<Boolean>() {
            @Override
            public void onComplete(@Nullable Boolean result) {
                notifyItemRemoved(position);
                items.remove(position);
            }
        });
    }

    protected class ViewHolder extends RecyclerView.ViewHolder{
        private final PhotoView photoView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photo_view);
        }
    }
}
