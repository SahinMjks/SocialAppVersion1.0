package com.example.socialappversion10;

public class Status {

    private String id;
    private String userId;
    private String text;
    private String mediaUrl;
    private boolean isVideo;
    private long timestamp;

    public Status() {}

    public Status(String id, String userId, String text, String mediaUrl, boolean isVideo, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.text = text;
        this.mediaUrl = mediaUrl;
        this.isVideo = isVideo;
        this.timestamp = timestamp;
    }

    // getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setIsVideo(boolean isVideo) {
        this.isVideo = isVideo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
