package com.errorxcode.jxinsta.endpoints.profile;

import com.errorxcode.jxinsta.AuthInfo;
import com.errorxcode.jxinsta.AuthenticationType;
import com.errorxcode.jxinsta.Constants;
import com.errorxcode.jxinsta.InstagramException;
import com.errorxcode.jxinsta.JxInsta;
import com.errorxcode.jxinsta.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.ref.Cleaner;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import port.org.json.JSONException;
import port.org.json.JSONObject;

/**
 * This class represents the profile of an Instagram user. You can get the profile of any user using this class.
 * All the profile related method are in this class. This class only has private apis, public related apis are in PublicAPIS class
 */
public class Profile {
    protected final AuthInfo authInfo;
    public String posts_end_cursor;
    public String username;
    public long pk;
    public String full_name;
    public String biography;
    public String profile_pic_url;
    public boolean is_private;
    public boolean is_verified;
    public List<Post> posts = new ArrayList<>();
    public int followers;
    public int followings;
    public boolean isBusinessAccount;

    /**
     * Constructor of the class, must not be initialized directly. Use JxInsta.getProfile() to get the profile of a user
     * @param authInfo auth info of the user
     * @param username username of the user
     */
    public Profile(@NotNull AuthInfo authInfo, @NotNull String username) throws InstagramException, IOException {
        this.authInfo = authInfo;
        this.username = username;
        var request = Utils.createGetRequest("users/web_profile_info/?username=" + username,authInfo);
        request = Utils.injectAppId(request);
        try (var response = Utils.call(request, authInfo)) {
            var profile = buildProfile(response, authInfo);
            this.username = profile.username;
            this.pk = profile.pk;
            this.full_name = profile.full_name;
            this.biography = profile.biography;
            this.profile_pic_url = profile.profile_pic_url;
            this.is_private = profile.is_private;
            this.is_verified = profile.is_verified;
            this.posts = profile.posts;
            this.followers = profile.followers;
            this.followings = profile.followings;
            this.isBusinessAccount = profile.isBusinessAccount;
        }
    }

    public Profile(@NotNull String username) {
        this.authInfo = null;
        this.username = username;
    }

    public static Profile buildProfile(@NotNull Response response,@Nullable AuthInfo info) throws IOException {
        var json = new JSONObject(response.body().string()).getJSONObject("data").getJSONObject("user");
        var profile = new Profile(json.getString("username"));
        profile.full_name = json.getString("full_name");
        profile.pk = json.getLong("id");
        profile.biography = json.getString("biography");
        profile.profile_pic_url = json.getString("profile_pic_url");
        profile.is_private = json.getBoolean("is_private");
        profile.is_verified = json.getBoolean("is_verified");
        var timeline_media = json.getJSONObject("edge_owner_to_timeline_media");
        profile.posts_end_cursor = timeline_media.getJSONObject("page_info").optString("end_cursor");
        var edges = timeline_media.getJSONArray("edges");
        for (int i = 0; i < edges.length(); i++) {
            var post = edges.getJSONObject(i).getJSONObject("node");
            var aPost = new Post(info,post.getLong("id"));
            aPost.shortcode = post.getString("shortcode");
            aPost.caption = post.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");
            aPost.likes = post.getJSONObject("edge_media_preview_like").getInt("count");
            aPost.comments = post.getJSONObject("edge_media_to_comment").getInt("count");
            aPost.isVideo = post.getBoolean("is_video");
            aPost.download_url = aPost.isVideo ? post.getString("video_url") : post.getString("display_url");
            aPost.next_cursor = profile.posts_end_cursor;
            profile.posts.add(aPost);
        }
        profile.followers = json.getJSONObject("edge_followed_by").getInt("count");
        profile.followings = json.getJSONObject("edge_follow").getInt("count");
        profile.isBusinessAccount = json.getBoolean("is_business_account");
        return profile;

    }

    /**
     * Get the posts of the user. Uses authenticated request
     * @param next_cursor next max id of the page
     * @return list of posts
     * @throws IOException if there's an error in the network
     * @throws InstagramException if there's an error in the Instagram API
     */
    public List<Post> getPosts(@Nullable String next_cursor) throws IOException, InstagramException {
        return _getPosts(authInfo,pk,next_cursor);
    }

    @AuthenticationType(AuthenticationType.Method.WEB_AUTH)
    public static List<Post> _getPosts(@Nullable AuthInfo authInfo,long pk,@Nullable String cursor) throws IOException, InstagramException {
        var params = new HashMap<String, Object>();
        params.put("id", pk);
        params.put("first", 20);
        if (cursor != null)
            params.put("after", cursor);

        try (var response = Utils.graphql("17888483320059182",params,authInfo == null ? null : authInfo.authorization)){
            var json = new JSONObject(response.body().string()).getJSONObject("data").getJSONObject("user").getJSONObject("edge_owner_to_timeline_media");
            var posts = json.getJSONArray("edges");
            var _cursor = json.getJSONObject("page_info").getString("end_cursor");
            var list = new ArrayList<Post>();
            for (int i = 0; i < posts.length(); i++) {
                var post = posts.getJSONObject(i).getJSONObject("node");
                var aPost = new Post(authInfo,post.getLong("id"));
                aPost.shortcode = post.getString("shortcode");
                aPost.caption = post.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");
                aPost.likes = post.getJSONObject("edge_media_preview_like").getInt("count");
                aPost.comments = post.getJSONObject("edge_media_to_comment").getInt("count");
                aPost.download_url = post.getString("display_url");
                aPost.next_cursor = _cursor;
                list.add(aPost);
            }
            return list;
        }
    }

    /**
     * Get the followers of the user
     * @param count number of followers to get
     * @param nextCursor next cursor of the page
     * @return list of followers
     * @throws IOException if there's an error in the network
     * @throws InstagramException if there's an error in the Instagram API
     */
    public List<String> getFollowers(int count,@Nullable String nextCursor) throws IOException, InstagramException {
        var req = Utils.createGetRequest("friendships/" + pk + "/followers/?count=" + count + "&maxId=" + nextCursor,authInfo);
        try (var response = Utils.call(req,authInfo)){
            var json = new JSONObject(response.body().string());
            var list = new ArrayList<String>();
            var users = json.getJSONArray("users");
            for (int i = 0; i < users.length(); i++) {
                list.add(users.getJSONObject(i).getString("username"));
            }
            return list;
        }
    }

    /**
     * Get the followings of the user
     * @param count number of followings to get
     * @param nextCursor next cursor of the page for pagination
     * @return list of followings
     * @throws IOException if there's an error in the network
     * @throws InstagramException if there's an error in the Instagram API
     */
    public List<String> getFollowings(int count,@Nullable String nextCursor) throws IOException, InstagramException {
        var req = Utils.createGetRequest("friendships/" + pk + "/following/?count=" + count + "&maxId=" + nextCursor,authInfo);
        try (var response = Utils.call(req,authInfo)){
            var json = new JSONObject(response.body().string());
            var list = new ArrayList<String>();
            var users = json.getJSONArray("users");
            for (int i = 0; i < users.length(); i++) {
                list.add(users.getJSONObject(i).getString("username"));
            }
            return list;
        }
    }


    /**
     * Get the story of the user
     * @param pk pk of the user
     * @return list of stories
     * @throws IOException if there's an error in the network
     * @throws InstagramException if there's an error in the Instagram API
     */
    @AuthenticationType(AuthenticationType.Method.WEB_AUTH)
    public List<Story> getStory(String pk) throws IOException, InstagramException {
        if (authInfo.loginType == JxInsta.LoginType.APP_AUTHENTICATION)
            throw new InstagramException("Cannot get stories with app authentication. Use web authentication only for this", InstagramException.Reasons.INVALID_LOGIN_TYPE);

        try {
            return Story.getActualStory(pk, authInfo);
        } catch (JSONException e){
            throw new InstagramException("Invalid response, require login", InstagramException.Reasons.LOGIN_EXPIRED);
        }
    }

    /**
     * Get the stories of the user
     * @return list of stories, null of no story is uploaded
     * @throws InstagramException if there's an error in the Instagram API
     * @throws IOException if there's an error in the network
     */
    public @Nullable List<Story> getStories() throws InstagramException, IOException {
        return getStory(String.valueOf(pk));
    }


    /**
     * Get the highlights of the user as the list of total stories in the highlights
     * @return list of highlights that contain stories
     * @throws IOException if there's an error in the network
     * @throws InstagramException if there's an error in the Instagram API
     */
    @AuthenticationType(AuthenticationType.Method.WEB_AUTH)
    public List<List<Story>> getHighlights() throws IOException, InstagramException {
        try (var response = Utils.graphql("9957820854288654", Map.of("include_highlight_reels","true","user_id",pk),authInfo.authorization)){
            if (response.isSuccessful()){
                var list = new ArrayList<List<Story>>();
                var res = response.body().string();
                var edges = new JSONObject(res).getJSONObject("data").getJSONObject("user").getJSONObject("edge_highlight_reels").getJSONArray("edges");
                for (int i = 0; i < edges.length(); i++) {
                    var id = edges.getJSONObject(i).getJSONObject("node").getString("id");
                    list.add(getStory("highlight:" + id));
                }
                return list;
            } else
                throw new InstagramException(response.body().string(), InstagramException.Reasons.UNKNOWN);
        }
    }

    public void follow() throws InstagramException, IOException {
        var req = Utils.createPostRequest(authInfo,"friendships/create/" + pk + "/",null);
        req = Utils.injectAppId(req);

        try (var res = Utils.call(req,authInfo)){
            if (res.isSuccessful()){
                var json = new JSONObject(res.body().string());
                if (json.has("status") && !json.getString("status").equals("ok")) {
                    throw new InstagramException(json.getString("message"), InstagramException.Reasons.UNKNOWN);
                }
            } else
                throw new InstagramException(res.body().string(), InstagramException.Reasons.UNKNOWN);
        }
    }

    public void unfollow() throws InstagramException, IOException {
        var req = Utils.createPostRequest(authInfo,"friendships/destroy/" + pk + "/",null);
        req = Utils.injectAppId(req);

        try (var res = Utils.call(req,authInfo)){
            if (res.isSuccessful()){
                var json = new JSONObject(res.body().string());
                if (json.has("status") && !json.getString("status").equals("ok")) {
                    throw new InstagramException(json.getString("message"), InstagramException.Reasons.UNKNOWN);
                }
            } else
                throw new InstagramException(res.body().string(), InstagramException.Reasons.UNKNOWN);
        }
    }

    public void block() throws InstagramException, IOException {
        var req = Utils.createPostRequest(authInfo,"friendships/block/" + pk + "/",null);
        req = Utils.injectAppId(req);

        try (var res = Utils.call(req,authInfo)){
            if (res.isSuccessful()){
                var json = new JSONObject(res.body().string());
                if (json.has("status") && !json.getString("status").equals("ok")) {
                    throw new InstagramException(json.getString("message"), InstagramException.Reasons.UNKNOWN);
                }
            } else
                throw new InstagramException(res.body().string(), InstagramException.Reasons.UNKNOWN);
        }
    }

    @Override
    public String toString() {
        return "Profile{" +
                "username='" + username + '\'' +
                ", pk=" + pk +
                ", full_name='" + full_name + '\'' +
                ", biography='" + biography + '\'' +
                ", profile_pic_url='" + profile_pic_url + '\'' +
                ", is_private=" + is_private +
                ", is_verified=" + is_verified +
                ", posts=" + posts +
                ", followers=" + followers +
                ", followings=" + followings +
                ", isBusinessAccount=" + isBusinessAccount +
                '}';
    }
}
