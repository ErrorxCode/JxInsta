package com.errorxcode.jxinsta.endpoints.direct;

import org.jetbrains.annotations.NotNull;

import port.org.json.JSONObject;

public class Message {
    public String message;
    public long sender;
    public String threadId;
    public String id;
    public long timestamp;
    public ItemType itemType;

    public static Message fromJSON(@NotNull JSONObject json){
        var message = new Message();
        message.itemType = ItemType.valueOf(json.getString("item_type").toUpperCase());
        message.sender = json.getLong("user_id");
        message.id = json.getString("item_id");
        message.timestamp = json.getLong("timestamp");
        if (message.itemType == Message.ItemType.TEXT) {
            message.message = json.getString("text");
        } else if (message.itemType == Message.ItemType.MEDIA) {
            String link = "";
            json = json.getJSONObject("media");
            var isVideo = json.getInt("media_type") == 2;
            if (isVideo) {
                link = json.getJSONArray("video_versions").getJSONObject(0).getString("url");
            } else {
                link = json.getJSONObject("image_versions2").getJSONArray("candidates").getJSONObject(0).getString("url");
            }
            message.message = link;
        }
        return message;
    }

    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", sender='" + sender + '\'' +
                ", threadId='" + threadId + '\'' +
                ", id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", itemType=" + itemType +
                '}';
    }

    static enum ItemType {
        TEXT,
        MEDIA,
        LINK,
        LOCATION,
        ACTION_LOG,
        PROFILE
    }
}

