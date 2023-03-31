package com.example.socialappversion10;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class StatusAdapter extends RecyclerView.Adapter<StatusAdapter.ViewHolder> {

    private List<Status> mStatusList;
    private Context mContext;

    public StatusAdapter(Context context, List<Status> statusList) {
        mStatusList = statusList;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_status, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Status status = mStatusList.get(position);
        holder.statusTextView.setText(status.getText());

        if (status.isVideo()) {
            holder.statusImageView.setVisibility(View.GONE);
            holder.statusVideoView.setVisibility(View.VISIBLE);
            Uri videoUri = Uri.parse(status.getMediaUrl());
            holder.statusVideoView.setVideoURI(videoUri);
            holder.statusVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.setLooping(true);
                    mp.start();
                }
            });
        } else {
            holder.statusImageView.setVisibility(View.VISIBLE);
            holder.statusVideoView.setVisibility(View.GONE);
            Glide.with(mContext)
                    .load(status.getMediaUrl())
                    .into(holder.statusImageView);
        }

    }
    public void setStatusList(List<Status> statusList) {
        mStatusList = statusList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mStatusList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView statusTextView;
        public ImageView statusImageView;
        public VideoView statusVideoView;

        public ViewHolder(View itemView) {
            super(itemView);
            statusTextView = itemView.findViewById(R.id.textView);
            statusImageView = itemView.findViewById(R.id.imageView);
            statusVideoView = itemView.findViewById(R.id.videoView);
        }
    }
}
