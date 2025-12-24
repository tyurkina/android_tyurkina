package com.mirea.tyurkinaia.mireaproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsArticle> articles = new ArrayList<>();

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsArticle article = articles.get(position);
        holder.bind(article);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setArticles(List<NewsArticle> articles) {
        this.articles = articles != null ? articles : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView descriptionTextView;
        private TextView sourceTextView;
        private TextView dateTextView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
        }

        public void bind(NewsArticle article) {
            titleTextView.setText(article.getTitle());
            String description = article.getDescription();
            if (description != null && !description.isEmpty()) {
                descriptionTextView.setText(description);
                descriptionTextView.setVisibility(View.VISIBLE);
            } else {
                descriptionTextView.setVisibility(View.GONE);
            }
            String sourceName = article.getSource() != null ? article.getSource().getName() : "";
            sourceTextView.setText(sourceName);
            String dateStr = formatDate(article.getPublishedAt());
            dateTextView.setText(dateStr);
        }

        private String formatDate(String dateString) {
            if (dateString == null || dateString.isEmpty()) {
                return "";
            }

            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat(
                        "dd.MM.yyyy HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                return outputFormat.format(date);
            } catch (Exception e) {
                return dateString;
            }
        }
    }
}