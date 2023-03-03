package com.github.hcsp;

import java.time.Instant;

public class News {
    private int id;
    private String url;
    private String title;
    private String content;
    private Instant createdAt;

    public Instant getCreatedAt() {
        return createdAt;
    }

    public News() {
    }

    public News(News old) {
        this.id = old.id;
        this.url = old.url;
        this.title = old.title;
        this.content = old.content;
        this.createdAt = old.createdAt;
        this.modifiedAt = old.modifiedAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    private Instant modifiedAt;

    public News(String url, String title, String content) {
        this.url = url;
        this.title = title;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
