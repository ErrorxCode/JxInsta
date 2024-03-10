package com.errorxcode.jxinsta.endpoints;

import com.errorxcode.jxinsta.InstagramException;
import com.errorxcode.jxinsta.Utils;
import com.errorxcode.jxinsta.endpoints.profile.Post;
import com.errorxcode.jxinsta.endpoints.profile.Profile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import port.org.json.JSONObject;

/**
 * This class contains all the public APIs that are available in the library. These endpoints/methods do not require login/authentication
 */
public class PublicAPIs {

    /**
     * Get the information of a post using the URL of the post. This post object must not be used to like, comment, etc. It's only for getting information
     * @param url URL of the post
     * @return Post object
     * @throws InstagramException if there's an error in the Instagram API
     * @throws IOException if there's an error in the network
     */
    public static Post getPostInfo(@NotNull String url) throws InstagramException, IOException {
        return Post.getPost(url);
    }

    /**
     * Get the information of a profile using the username of the profile. This profile object must not be used to follow, unfollow, etc. It's only for getting information
     * @param username username of the profile
     * @return Profile object
     * @throws InstagramException if there's an error in the Instagram API
     * @throws IOException if there's an error in the network
     */
    public static Profile getProfileInfo(@NotNull String username) throws InstagramException, IOException {
        var request = Utils.createPublicRequest("users/web_profile_info/?username=" + username);
        return Profile.buildProfile(Utils.call(request,null),null);
    }

    /**
     * Get's posts of a profile using the pk/id of the user.
     * @param pk pk/id of the user
     * @param nextCursor next cursor to get the next page of posts. If null, it will get the first page
     * @return List of posts
     * @throws InstagramException if there's an error in the Instagram API
     * @throws IOException if there's an error in the network
     */
    public static List<Post> getPosts(long pk, @Nullable String nextCursor) throws InstagramException, IOException {
        return Profile._getPosts(null,pk, nextCursor);
    }

    public static List<Post> hashtagSearch(@NotNull String tag) throws InstagramException, IOException {
        var req = Utils.createPublicRequest("tags/logged_out_web_info/?tag_name=" + tag);
        var res = Utils.call(req,null);
        var json = new JSONObject(res.body().string());
        var posts = json.getJSONObject("data").getJSONObject("hashtag").getJSONObject("edge_hashtag_to_media").getJSONArray("edges");
        var list = new java.util.ArrayList<Post>();
        for (int i = 0; i < posts.length(); i++) {
            var post = posts.getJSONObject(i).getJSONObject("node");
            var pst = new Post(null, post.getLong("id"));
            pst.isVideo = post.getBoolean("is_video");
            pst.caption = post.has("edge_media_to_caption") ? post.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text") : "";
            pst.likes = post.getJSONObject("edge_media_preview_like").getInt("count");
            pst.comments = post.getJSONObject("edge_media_to_comment").getInt("count");
            pst.shortcode = post.getString("shortcode");
            pst.download_url = post.getString("display_url");
            list.add(pst);
        }
        res.close();
        return list;
    }
}
