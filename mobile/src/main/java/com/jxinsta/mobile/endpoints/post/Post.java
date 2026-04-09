package com.jxinsta.mobile.endpoints.post;


import com.jxinsta.mobile.paginators.CommentPaginator;
import com.jxinsta.mobile.utils.Constants;
import com.jxinsta.mobile.InstagramException;
import com.jxinsta.mobile.utils.Utils;
import com.jxinsta.mobile.utils.Likable;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import port.org.json.JSONObject;

/**
 * Represents an Instagram post in the mobile API context.
 * Extends {@link PostData} to provide interactive methods such as liking, commenting, and fetching likers.
 */
public class Post extends PostData implements Likable {
    protected final String auth;
    /** Indicates if there are more comments or related data to fetch (internal use). */
    public boolean hasMore;

    /**
     * Internal constructor for Post.
     *
     * @param auth     The authentication token.
     * @param postItem The JSON object containing post data.
     */
    public Post(@NotNull String auth, @NotNull JSONObject postItem) {
        super(postItem);
        this.auth = auth;
    }

    /**
     * Fetches post data from a given URL using the public GraphQL API.
     *
     * @param url The URL of the post.
     * @return A {@link PostData} instance containing the post's information.
     * @throws InstagramException If the API returns an error.
     */
    public static PostData getPost(@NotNull String url) throws InstagramException {
        var shortcode = url.split("/p/")[1].split("/")[0];
        var vars = new JSONObject();
        vars.put("shortcode",shortcode);
        vars.put("fetch_comment_count",4);
        vars.put("fetch_tagged_user_count",0);

        var body = new FormBody.Builder()
                .addEncoded("doc_id", "10015901848480474")
                .addEncoded("variables", vars.toString())
                .addEncoded("lsd","JxInsta")
                .build();
        var req = new Request.Builder()
                .url("https://www.instagram.com/api/graphql")
                .headers(Headers.of(Constants.BASE_HEADERS))
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("x-fb-lsd", "JxInsta")
                .header("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36")
                .method("POST",body)
                .build();

        var json = Utils.callAPI(req);
        var shortcode_media = json.getJSONObject("xdt_shortcode_media");
        return new PostData(shortcode_media);
    }

    /**
     * Likes this post.
     *
     * @throws InstagramException If the API returns an error.
     */
    @Override
    public void like() throws InstagramException {
        Map<String, String> body = new HashMap<>();
        body.put("media_id", id);
        Utils.post(Constants.Endpoints.mediaLike(id), auth, body);
    }

    /**
     * Removes the like from this post.
     *
     * @throws InstagramException If the API returns an error.
     */
    public void dislike() throws InstagramException {
        Map<String, String> body = new HashMap<>();
        body.put("media_id", id);
        Utils.post(Constants.Endpoints.mediaUnlike(id), auth, body);
    }

    /**
     * Fetches the usernames of users who liked this post.
     *
     * @return A list of usernames.
     * @throws InstagramException If the API returns an error.
     */
    public List<String> likers() throws InstagramException {
        var json = Utils.get(Constants.Endpoints.mediaLikers(id), auth, null);
        var users = json.getJSONArray("users");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < users.length(); i++) {
            list.add(users.getJSONObject(i).getString("username"));
        }
        return list;
    }


    /**
     * Returns a paginator for fetching comments on this post.
     *
     * @return A {@link CommentPaginator}.
     */
    public CommentPaginator getComments() {
        return new CommentPaginator(id,auth);
    }


    /**
     * Adds a comment to this post.
     *
     * @param comment The text of the comment.
     * @throws InstagramException If the API returns an error.
     */
    public void comment(@NotNull String comment) throws InstagramException {
        Map<String, String> body = new HashMap<>();
        body.put("comment_text", comment);
        Utils.post(Constants.Endpoints.mediaComment(id), auth, body);
    }

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", shortcode='" + shortcode + '\'' +
                ", download_url='" + Arrays.toString(download_url) + '\'' +
                ", caption='" + caption + '\'' +
                ", likes=" + likes +
                ", comments=" + comments +
                '}';
    }

    /**
     * Supported media types for an Instagram post.
     */
    public enum MEDIA_TYPE {
        /** Video content. */
        VIDEO,
        /** Single image. */
        IMAGE,
        /** Multi-item post (slideshow). */
        CAROUSEL
    }
}
