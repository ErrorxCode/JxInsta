package com.jxinsta.mobile.endpoints.post;

import port.org.json.JSONObject;

/**
 * A data class representing basic information about an Instagram post in the mobile API context.
 * This class is used for read-only post information and as a base for the interactive {@link Post} class.
 */
public class PostData {
    /** The type of media (IMAGE, VIDEO, or CAROUSEL). */
    public Post.MEDIA_TYPE mediaType;
    /** The shortcode of the post (e.g., the "CODE" in /p/CODE/). */
    public String shortcode;
    /** An array of URLs to download the media. For carousels, this contains URLs for all items. */
    public String[] download_url;
    /** The caption of the post. */
    public String caption;
    /** The number of likes on the post. */
    public int likes;
    /** The number of comments on the post. */
    public int comments;
    /** The unique identifier (PK) of the post. */
    public String id;

    /**
     * Default constructor for PostData.
     */
    public PostData(){

    }

    /**
     * Constructs a PostData object from a JSON post object.
     *
     * @param postData The JSON object containing post data from Instagram's mobile API.
     */
    public PostData (JSONObject postData){
        this.id = postData.optString("pk", postData.optString("id"));
        this.shortcode = postData.optString("code");
        int type = postData.optInt("media_type");
        if (type == 1) mediaType = Post.MEDIA_TYPE.IMAGE;
        else if (type == 2) mediaType = Post.MEDIA_TYPE.VIDEO;
        else if (type == 8) mediaType = Post.MEDIA_TYPE.CAROUSEL;

        if (postData.has("caption") && !postData.isNull("caption")) {
            this.caption = postData.getJSONObject("caption").optString("text");
        }
        this.likes = postData.optInt("like_count");
        this.comments = postData.optInt("comment_count");

        if (postData.has("image_versions2")) {
            var candidates = postData.getJSONObject("image_versions2").getJSONArray("candidates");
            this.download_url = new String[]{candidates.getJSONObject(0).getString("url")};
        } else if (postData.has("carousel_media")) {
            var carousel = postData.getJSONArray("carousel_media");
            this.download_url = new String[carousel.length()];
            for (int i = 0; i < carousel.length(); i++) {
                this.download_url[i] = carousel.getJSONObject(i).getJSONObject("image_versions2").getJSONArray("candidates").getJSONObject(0).getString("url");
            }
        }
    }

    /**
     * Factory method to build a PostData object from a user post node.
     *
     * @param node The JSON object containing post data.
     * @return A new PostData instance.
     */
    public static PostData buildForUserPost(JSONObject node) {
        return new PostData(node);
    }
}
