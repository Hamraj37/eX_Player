package com.hamraj37.explayer.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.imageview.ShapeableImageView;
import com.hamraj37.explayer.R;
import com.hamraj37.explayer.databinding.ItemVideoBinding;
import com.hamraj37.explayer.model.VideoItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(VideoItem videoItem, int position, List<VideoItem> currentList);
    }

    public static final int SORT_BY_TITLE = 0;
    public static final int SORT_BY_DATE = 1;
    public static final int SORT_BY_SIZE = 2;
    public static final int SORT_BY_DURATION = 3;

    private final List<VideoItem> originalList = new ArrayList<>();
    private final List<VideoItem> filteredList = new ArrayList<>();
    private final OnVideoClickListener clickListener;
    private String currentSearchQuery = "";

    public VideoAdapter(OnVideoClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setVideoList(List<VideoItem> newList) {
        originalList.clear();
        if (newList != null) {
            originalList.addAll(newList);
        }
        filter("");
    }

    public void filter(String query) {
        currentSearchQuery = query != null ? query : "";
        filteredList.clear();
        if (currentSearchQuery.trim().isEmpty()) {
            filteredList.addAll(originalList);
        } else {
            String lowerQuery = currentSearchQuery.toLowerCase().trim();
            for (VideoItem item : originalList) {
                if (item.getTitle().toLowerCase().contains(lowerQuery) ||
                        item.getPath().toLowerCase().contains(lowerQuery)) {
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void sortVideos(int sortBy) {
        switch (sortBy) {
            case SORT_BY_TITLE:
                Collections.sort(originalList, (v1, v2) -> v1.getTitle().compareToIgnoreCase(v2.getTitle()));
                break;
            case SORT_BY_DATE:
                Collections.sort(originalList, (v1, v2) -> Long.compare(v2.getDateAdded(), v1.getDateAdded()));
                break;
            case SORT_BY_SIZE:
                Collections.sort(originalList, (v1, v2) -> Long.compare(v2.getSize(), v1.getSize()));
                break;
            case SORT_BY_DURATION:
                Collections.sort(originalList, (v1, v2) -> Long.compare(v2.getDuration(), v1.getDuration()));
                break;
        }
        filter(currentSearchQuery);
    }

    public List<VideoItem> getFilteredList() {
        return filteredList;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVideoBinding binding = ItemVideoBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VideoViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem item = filteredList.get(position);
        holder.bind(item, clickListener, filteredList);

        View.OnClickListener clickListenerAction = v -> {
            if (clickListener != null) {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && pos < filteredList.size()) {
                    clickListener.onVideoClick(item, pos, filteredList);
                }
            }
        };

        holder.itemView.setOnClickListener(clickListenerAction);
        if (holder.binding != null) {
            holder.binding.cvVideoCard.setOnClickListener(clickListenerAction);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        public final ItemVideoBinding binding;
        private final ShapeableImageView ivThumbnail;
        private final TextView tvDuration;
        private final TextView tvTitle;
        private final TextView tvFileSize;
        private final TextView tvDateAdded;
        private final TextView tvPath;

        public VideoViewHolder(@NonNull ItemVideoBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            ivThumbnail = binding.ivThumbnail;
            tvDuration = binding.tvDuration;
            tvTitle = binding.tvTitle;
            tvFileSize = binding.tvFileSize;
            tvDateAdded = binding.tvDateAdded;
            tvPath = binding.tvPath;
        }

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            this.binding = ItemVideoBinding.bind(itemView);
            ivThumbnail = binding.ivThumbnail;
            tvDuration = binding.tvDuration;
            tvTitle = binding.tvTitle;
            tvFileSize = binding.tvFileSize;
            tvDateAdded = binding.tvDateAdded;
            tvPath = binding.tvPath;
        }

        public void bind(VideoItem item, OnVideoClickListener listener, List<VideoItem> currentList) {
            tvTitle.setText(item.getTitle());
            tvDuration.setText(item.getFormattedDuration());
            tvFileSize.setText(item.getFormattedSize());
            tvDateAdded.setText(item.getFormattedDate());
            tvPath.setText(item.getPath());

            Glide.with(itemView.getContext())
                    .load(item.getUri())
                    .placeholder(R.drawable.ic_video_placeholder)
                    .error(R.drawable.ic_video_placeholder)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(ivThumbnail);

            View.OnClickListener clickListenerAction = v -> {
                if (listener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && pos < currentList.size()) {
                        listener.onVideoClick(item, pos, currentList);
                    }
                }
            };

            itemView.setOnClickListener(clickListenerAction);
            if (binding != null) {
                binding.cvVideoCard.setOnClickListener(clickListenerAction);
            }
        }
    }
}
