package com.chatterbug.app.models;

public class Message {
    private String content;
    private boolean isSent;
    private long timestamp;

    public Message(String content, boolean isSent, long timestamp) {
        this.content = content;
        this.isSent = isSent;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "content='" + content + '\'' +
                ", isSent=" + isSent +
                ", timestamp=" + timestamp +
                '}';
    }
}
