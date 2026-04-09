package com.jxinsta.mobile.endpoints.profile;

import com.jxinsta.mobile.InstagramException;
import com.jxinsta.mobile.utils.Constants;
import com.jxinsta.mobile.utils.Utils;
import com.jxinsta.mobile.utils.Likable;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import port.org.json.JSONObject;

/**
 * Represents an Instagram story or highlight item in the mobile API context.
 * Provides metadata about the story and methods to like or dislike it.
 */
public class Story implements Likable {
    /** The PK (id) of the user who posted the story. */
    public String userID;
    /** The username of the user who posted the story. */
    public String username;
    /** The unique identifier of the story item. */
    public String storyId;
    /** The URL to download the story media (image or video). */
    public String download_url;
    /** Whether the story is a video. */
    public boolean is_video;
    /** The number of views the story has received. -1 if unknown. */
    public int views = -1;
    /** The duration of the video story in seconds. -1 if not a video. */
    public int duration = -1;
    /** The number of likes the story has received. -1 if unknown. */
    public int likes = -1;
    /** The authentication token. */
    public final String auth;

    /**
     * Internal constructor for Story.
     *
     * @param storyItem The JSON object containing story item data.
     * @param auth      The authentication token.
     */
    public Story(@NotNull JSONObject storyItem, String auth) {
        this.auth = auth;
        JSONObject user = storyItem.optJSONObject("user");
        if (user != null) {
            this.userID = user.optString("id");
            this.username = user.optString("username");
        }

        this.storyId = storyItem.optString("id");
        this.is_video = storyItem.optInt("media_type") == 2;

        if (storyItem.has("video_duration")) {
            this.duration = (int) storyItem.optDouble("video_duration");
        }

        this.likes = storyItem.optBoolean("has_privately_liked", false) ? 1 : 0;

        // Extract Download URL
        if (is_video && storyItem.has("video_versions")) {
            this.download_url = storyItem.optJSONArray("video_versions")
                    .optJSONObject(0)
                    .optString("url");
        } else if (storyItem.has("image_versions2")) {
            JSONObject imageVersions = storyItem.optJSONObject("image_versions2");
            if (imageVersions != null && imageVersions.has("candidates")) {
                this.download_url = imageVersions.optJSONArray("candidates")
                        .optJSONObject(0)
                        .optString("url");
            }
        }
    }


    @Override
    public String toString() {
        return "Story{" +
                "id='" + userID + '\'' +
                ", download_url='" + download_url + '\'' +
                ", isVideo='" + is_video + '\'' +
                ", views=" + views +
                ", duration=" + duration +
                ", likes=" + likes +
                '}';
    }

    /**
     * Likes this story.
     *
     * @throws InstagramException If the API returns an error.
     */
    @Override
    public void like() throws InstagramException {
        var body = Utils.genSignedBody(Map.of("media_id", storyId));
        Utils.post(Constants.Endpoints.STORY_LIKE, auth, body);
    }

    /**
     * Removes the like from this story.
     *
     * @throws InstagramException If the API returns an error.
     */
    public void dislike() throws InstagramException {
        var body = Utils.genSignedBody(Map.of("media_id", storyId));
        Utils.post(Constants.Endpoints.STORY_UNLIKE, auth, body);
    }
}
