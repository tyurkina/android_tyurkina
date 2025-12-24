package com.mirea.tyurkinaia.mireaproject;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class NewsResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("totalResults")
    private int totalResults;

    @SerializedName("articles")
    private List<NewsArticle> articles;

    public String getStatus() {
        return status != null ? status : "";
    }

    public int getTotalResults() {
        return totalResults;
    }

    public List<NewsArticle> getArticles() {
        return articles != null ? articles : java.util.Collections.emptyList();
    }
}