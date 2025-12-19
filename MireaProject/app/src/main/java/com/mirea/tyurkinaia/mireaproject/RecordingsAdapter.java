package com.mirea.tyurkinaia.mireaproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.mirea.tyurkinaia.mireaproject.R;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RecordingsAdapter extends RecyclerView.Adapter<RecordingsAdapter.ViewHolder> {

    private List<MicrophoneFragment.RecordingItem> recordings;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onPlayClick(MicrophoneFragment.RecordingItem item);
        void onDeleteClick(MicrophoneFragment.RecordingItem item);
    }

    public RecordingsAdapter(List<MicrophoneFragment.RecordingItem> recordings, OnItemClickListener listener) {
        this.recordings = recordings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recording, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MicrophoneFragment.RecordingItem item = recordings.get(position);

        holder.fileNameTextView.setText(item.getFileName());

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        holder.dateTextView.setText(sdf.format(item.getTimestamp()));

        holder.playButton.setOnClickListener(v -> listener.onPlayClick(item));
        holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(item));
    }

    @Override
    public int getItemCount() {
        return recordings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fileNameTextView;
        TextView dateTextView;
        ImageButton playButton;
        ImageButton deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTextView = itemView.findViewById(R.id.fileNameTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            playButton = itemView.findViewById(R.id.playButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}