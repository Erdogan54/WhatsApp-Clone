package com.ozgurerdogan.chatexample.models;

public class MessageModel {

    String sendUid,recUid,message,messageId;
    Long timestamp;

    public MessageModel(String sendUid, String recUid, String message, String messageId, Long timestamp) {
        this.sendUid = sendUid;
        this.recUid = recUid;
        this.message = message;
        this.messageId = messageId;
        this.timestamp = timestamp;
    }

    public MessageModel(String uId, String message, Long timestamp) {
        this.sendUid = sendUid;
        this.message = message;
        this.timestamp = timestamp;

    }

    public MessageModel(String uId, String message) {
        this.sendUid = sendUid;
        this.message = message;
    }

    public MessageModel (){}

    public String getSendUid() {
        return sendUid;
    }

    public void setSendUid(String sendUid) {
        this.sendUid = sendUid;
    }

    public String getRecUid() {
        return recUid;
    }

    public void setRecUid(String recUid) {
        this.recUid = recUid;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
