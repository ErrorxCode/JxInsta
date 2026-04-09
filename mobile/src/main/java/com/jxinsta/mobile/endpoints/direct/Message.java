package com.jxinsta.mobile.endpoints.direct;

import port.org.json.JSONArray;
import port.org.json.JSONObject;

/**
 * Represents a single message within a Direct Message thread.
 * Supports various message types such as text, images, videos, and shared posts.
 */
public class Message {
    /** The unique identifier of the message item. */
    public final String id;
    /** The text content of the message. Only populated if {@code type == TEXT}. */
    public String text;
    /** The URL to the media (image/video). Populated if {@code type == IMAGE} or {@code type == VIDEO}. */
    public String mediaURL;
    /** The URL of a shared post. Populated if {@code type == POST}. */
    public String postURL;
    /** The type of this message. */
    public TYPE type;
    /** Indicates if this message is a reply to another message. */
    public boolean isReply;
    /** Indicates if the current user sent this message. */
    public boolean isSent;
    /** The message that this message is replying to. {@code null} if {@code isReply} is false. */
    public Message replyOf;

    /**
     * Constructs a Message object from a JSON item.
     *
     * @param item The JSON object representing a message item from the Instagram API.
     */
    public Message(JSONObject item) {
        this.id = item.optString("item_id");
        this.isSent = item.optBoolean("is_sent_by_viewer", false);
        this.isReply = item.has("replied_to_message");

        if (isReply){
            replyOf = new Message(item.optJSONObject("replied_to_message"));
        }

        String itemType = item.optString("item_type");
        switch (itemType) {
            case "text":
                this.type = TYPE.TEXT;
                this.text = item.optString("text");
                break;

            case "media":
                JSONObject mediaObj = item.optJSONObject("media");
                if (mediaObj != null) {
                    int mediaType = mediaObj.optInt("media_type", 1);
                    if (mediaType == 2) {
                        this.type = TYPE.VIDEO;
                    } else {
                        this.type = TYPE.IMAGE;
                    }
                    JSONObject imageVersions = mediaObj.optJSONObject("image_versions2");
                    if (imageVersions != null) {
                        JSONArray candidates = imageVersions.optJSONArray("candidates");
                        if (candidates != null && !candidates.isEmpty()) {
                            this.mediaURL = candidates.getJSONObject(0).optString("url");
                        }
                    }
                }
                break;

            case "media_share":
                this.type = TYPE.POST;
                JSONObject shareObj = item.optJSONObject("direct_media_share");
                if (shareObj != null) {
                    JSONObject sharedMedia = shareObj.optJSONObject("media");
                    if (sharedMedia != null) {
                        JSONObject imageVersions = sharedMedia.optJSONObject("image_versions2");
                        if (imageVersions != null) {
                            JSONArray candidates = imageVersions.optJSONArray("candidates");
                            if (candidates != null && !candidates.isEmpty()) {
                                this.postURL = candidates.getJSONObject(0).optString("url");
                            }
                        }
                    }
                }
                break;

            default:
                this.type = TYPE.UNDEFINED;
                break;
        }
    }


    /**
     * Enum defining the possible types of a direct message.
     */
    public enum TYPE {
        /** Plain text message. */
        TEXT,
        /** An image message. */
        IMAGE,
        /** A video message. */
        VIDEO,
        /** A shared Instagram post. */
        POST,
        /** Unknown or unsupported message type. */
        UNDEFINED
    }
}
