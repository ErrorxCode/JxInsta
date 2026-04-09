package com.jxinsta.mobile.endpoints.post;

import com.jxinsta.mobile.InstagramException;
import com.jxinsta.mobile.JxInsta;
import com.jxinsta.mobile.utils.Constants;
import com.jxinsta.mobile.utils.Utils;
import com.jxinsta.mobile.utils.Likable;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import port.org.json.JSONObject;

/**
 * Represents a comment on an Instagram post in the mobile API context.
 * Provides methods for interacting with the comment, such as liking, disliking, replying, and deleting.
 */
public class Comment implements Likable {
    /** The unique identifier (PK) of the comment. */
    public final String id;
    /** The media ID of the post this comment belongs to. */
    public long mediaId;
    /** The text content of the comment. */
    public String text;
    /** The username of the user who posted the comment. */
    public String username;
    /** The number of likes this comment has received. */
    public int likes;
    /** The timestamp (Unix) when the comment was created. */
    public long timestamp;
    /** The authentication token. */
    public final String auth;


    /**
     * Internal constructor for Comment.
     *
     * @param auth    The authentication token.
     * @param comment The JSON object containing comment data.
     */
    public Comment(String auth, JSONObject comment){
        this.auth = auth;
        this.text = comment.getString("text");
        this.username = comment.getJSONObject("user").getString("username");
        this.likes = comment.optInt("comment_like_count");
        this.timestamp = comment.getLong("created_at");
        this.id = comment.getString("pk");
        this.mediaId = comment.getLong("media_id");
    }

    /**
     * Likes this comment.
     *
     * @throws InstagramException If the API returns an error.
     */
    @Override
    public void like() throws InstagramException {
        Utils.post(Constants.Endpoints.commentLike(id), auth, null);
    }


    /**
     * Removes the like from this comment.
     *
     * @throws InstagramException If the API returns an error.
     */
    public void dislike() throws InstagramException {
        Utils.post(Constants.Endpoints.commentUnlike(id), auth, null);
    }

    /**
     * Replies to this comment.
     *
     * @param reply The text content of the reply.
     * @throws InstagramException If the API returns an error.
     */
    public void reply(@NotNull String reply) throws InstagramException {
        Map<String, String> body = new HashMap<>();
        body.put("comment_text", reply);
        body.put("replied_to_comment_id", id);
        Utils.post(Constants.Endpoints.mediaComment(String.valueOf(mediaId)), auth, body);
    }

    /**
     * Deletes this comment.
     *
     * @throws InstagramException If the API returns an error.
     */
    public void delete() throws InstagramException {
        Map<String, String> body = new HashMap<>();
        body.put("comment_ids_to_delete", id);
        Utils.post(Constants.Endpoints.commentBulkDelete(mediaId), auth, body);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", mediaId='" + mediaId + '\'' +
                ", text='" + text + '\'' +
                ", username='" + username + '\'' +
                ", likes=" + likes +
                ", timestamp=" + timestamp +
                ", auth='" + auth + '\'' +
                '}';
    }
}
