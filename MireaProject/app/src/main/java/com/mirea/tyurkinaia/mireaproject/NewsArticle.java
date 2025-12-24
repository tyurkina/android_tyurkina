package com.mirea.tyurkinaia.mireaproject;

import com.google.gson.annotations.SerializedName;

public class NewsArticle {
    @SerializedName("source")
    private Source source;

    @SerializedName("author")
    private String author;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("url")
    private String url;

    @SerializedName("urlToImage")
    private String urlToImage;

    @SerializedName("publishedAt")
    private String publishedAt;

    @SerializedName("content")
    private String content;

    // Геттеры должны быть публичными и возвращать значения
    public Source getSource() {
        return source != null ? source : new Source();
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public String getDescription() {
        return description != null ? description : "";
    }

    public String getUrl() {
        return url != null ? url : "";
    }

    public String getImageUrl() {
        return urlToImage != null ? urlToImage : "";
    }

    public String getPublishedAt() {
        return publishedAt != null ? publishedAt : "";
    }

    public String getAuthor() {
        return author != null ? author : "";
    }
}