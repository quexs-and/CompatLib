package com.quexs.compatdemo.media;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.quexs.compatdemo.R;
import com.quexs.compatdemo.databinding.ItemMianNameBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Android Studio.
 * <p>
 * author: Quexs
 * <p>
 * Date: 2022/7/20
 * <p>
 * Time: 20:38
 * <p>
 * 备注：
 */
public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder>{
    private final ArrayList<String> items = new ArrayList<>();
    private MediaAdapterListener mediaAdapterListener;

    public MediaAdapter(MediaAdapterListener mediaAdapterListener){
        this.mediaAdapterListener = mediaAdapterListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mian_name, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.binding.txvName.setText(getItem(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public String getItem(int position){
        return items.get(position);
    }

    public void addItems(List<String> list){
        int oldCount = this.items.size();
        if(oldCount > 0){
            notifyItemRangeRemoved(0, oldCount);
            this.items.clear();
        }
        int addCount = list == null ? 0 : list.size();
        if(addCount > 0){
            this.items.addAll(list);
            notifyItemRangeInserted(0, addCount);
        }
    }



    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ItemMianNameBinding binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemMianNameBinding.bind(itemView);
            binding.cardItem.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            String mediaName = getItem(position);
            if(mediaAdapterListener != null){
                mediaAdapterListener.onClickItem(mediaName);
            }
        }
    }

    public interface MediaAdapterListener{
        void onClickItem(String mediaName);
    }


}
