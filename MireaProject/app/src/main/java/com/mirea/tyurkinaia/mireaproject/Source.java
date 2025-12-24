package com.mirea.tyurkinaia.mireaproject;
import com.google.gson.annotations.SerializedName;
public class Source {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    public String getName() {
        return name != null ? name : "";
    }

    public String getId() {
        return id != null ? id : "";
    }
}