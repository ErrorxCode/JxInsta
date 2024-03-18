package com.errorxcode.jxinsta;

import com.errorxcode.jxinsta.endpoints.direct.DirectMessaging;
import com.errorxcode.jxinsta.endpoints.profile.Post;
import com.errorxcode.jxinsta.endpoints.profile.Profile;
import com.errorxcode.jxinsta.endpoints.profile.Story;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import port.org.json.JSONException;
import port.org.json.JSONObject;

/**
 * This is the main interface of the library. You can perform account level actions using this class
 * For other actions like getting media, liking media, etc, there are other respective classes.
 */
public class JxInsta extends AuthInfo {
    public String username;
    public String password;

    public enum LoginType {
        WEB_AUTHENTICATION,
        APP_AUTHENTICATION,
        BOTH_WEB_AND_APP_AUTHENTICATION
    }

    /**
     * For future use, if you want dual authentication. For now, it's not supported
     *
     * @param username username of the account
     * @param password password of the account
     * @param type     type of login
     * @throws IOException        if there's an error in the network
     * @throws InstagramException if there's an error in the Instagram API
     */
    public JxInsta(String username, String password, LoginType type) throws IOException, InstagramException {
        this.loginType = type;
        this.username = username;
        this.password = password;

        var encPassword = "#PWD_INSTAGRAM_BROWSER:0:" + new Date().getTime() + ":" + password;
        var body = new FormBody.Builder()
                .add("enc_password", encPassword)
                .add("username", username)
                .add("optIntoOneTap", "false")
                .add("queryParams", "{}")
                .add("trustedDeviceRecords", "{}")
                .build();
        Request.Builder builder = new Request.Builder()
                .url(Constants.LOGIN_URL)
                .method("POST", body)
                .headers(Headers.of(Constants.BASE_HEADERS));

        if (loginType == LoginType.WEB_AUTHENTICATION) {
            builder.addHeader("x-csrftoken", Utils.getCrsf());
            builder.addHeader("user-agent", Constants.WEB_USER_AGENT);
        } else  if (loginType == LoginType.APP_AUTHENTICATION) {
            builder.addHeader("user-agent", Constants.MOBILE_USER_AGENT);
        } else if (loginType == LoginType.BOTH_WEB_AND_APP_AUTHENTICATION) {
            token = new JxInsta(username, password, LoginType.APP_AUTHENTICATION).token;
            cookie = new JxInsta(username, password, LoginType.WEB_AUTHENTICATION).cookie;
            authorization = cookie;
            return;
        } else {
            throw new IllegalArgumentException("Invalid login type");
        }


        var response = Utils.call(builder.build(), null);
        if (response.isSuccessful()) {
            if (response.body().string().equals("{\"user\":true,\"authenticated\":false,\"status\":\"ok\"}"))
                throw new InstagramException("Wrong password", InstagramException.Reasons.INVALID_CREDENTIAL);


            if (type == LoginType.WEB_AUTHENTICATION) {
                cookie = Utils.extractCookie(response.headers("set-cookie"));
                authorization = cookie;
                crsf = cookie.split(";")[0].split("=")[1];
            } else if (type == LoginType.APP_AUTHENTICATION) {
                token = response.header("ig-set-authorization");
                authorization = token;
            }  else {
                throw new IllegalArgumentException("Invalid login type");
            }

            response.close();
        } else {
            var json = new JSONObject(response.body().string());
            if (json.getString("status").equals("fail"))
                if (!json.getString("message").isEmpty())
                    throw new InstagramException(json.getString("message"), InstagramException.Reasons.UNKNOWN);
                else
                    throw new InstagramException(json.getString("error_type"), InstagramException.Reasons.TWO_FACTOR_REQUIRED);

            throw new InstagramException(json.toString(3), InstagramException.Reasons.UNKNOWN_LOGIN_ERROR);
        }
    }

    /**
     * Login using existing cookie. This is only for web authentication
     *
     * @param cookie OPTIONAL if auth is passed : Cookie for authentication (For web api)
     */
    public JxInsta(String cookie,String bearer) {
        if (cookie == null && bearer == null)
            throw new IllegalArgumentException("One of the cookie or token should be provided");

        this.cookie = cookie;
        this.token = bearer;
        this.crsf = cookie.split(";")[0].split("=")[1];
        System.out.println(crsf);
        authorization = cookie;
        loginType = LoginType.WEB_AUTHENTICATION;
    }


    public Profile getProfile(String username) throws IOException, InstagramException {
        return new Profile(this, username);
    }

    @AuthenticationType(value = AuthenticationType.Method.WEB_AUTH)
    public List<Post> getFeedPosts(int count, @Nullable String cursor) throws IOException, InstagramException {
        var params = new HashMap<String, Object>();
        params.put("fetch_media_item_count", count);
        params.put("fetch_media_item_cursor", cursor == null ? "" : cursor);
        params.put("fetch_comment_count", 0);
        params.put("fetch_like", 0);
        params.put("has_stories", false);
        params.put("has_threaded_comments", false);

        try (var res = Utils.graphql("17842794232208280", params, authorization)) {
            var json = new JSONObject(res.body().string());
            var timeline = json.getJSONObject("data").getJSONObject("user").getJSONObject("edge_web_feed_timeline");
            var posts = timeline.getJSONArray("edges");
            var list = new ArrayList<Post>();

            for (int i = 0; i < posts.length(); i++) {
                var post = posts.getJSONObject(i).getJSONObject("node");
                var pst = new Post(this, post.getLong("id"));
                pst.isVideo = post.getBoolean("is_video");
                pst.caption = post.has("edge_media_to_caption") ? post.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text") : "";
                pst.likes = post.getJSONObject("edge_media_preview_like").getInt("count");
                pst.comments = post.getJSONObject("edge_media_to_comment").getInt("count");
                pst.shortcode = post.getString("shortcode");
                pst.download_url = post.getString("display_url");
                pst.next_cursor = timeline.getJSONObject("page_info").getString("end_cursor");
                list.add(pst);
            }
            return list;
        }
    }

    public List<Story[]> getFeedStories() throws InstagramException, IOException {
        var list = new java.util.ArrayList<Story[]>();
        var req = Utils.createGetRequest("feed/reels_tray/?is_following_feed=false",this);
        try (var res = Utils.call(req,null)) {
            var users = new JSONObject(res.body().string()).getJSONArray("tray");
            for (int i = 0; i < users.length(); i++) {
                var user_story_id = users.getJSONObject(i).getLong("id");
                var stories = Story.getActualStory(String.valueOf(user_story_id), this);
                if (stories != null)
                    list.add(stories.toArray(new Story[0]));
            }

            return list;
        } catch (JSONException e) {
            e.printStackTrace();
            throw new InstagramException("Invalid response, require login or challenge", InstagramException.Reasons.UNKNOWN);
        }
    }

    public void postPicture(@NotNull InputStream inputStream,@NotNull String caption,boolean disableLikenComment) throws IOException, InstagramException {
        var id = Utils.uploadPicture(inputStream, cookie);
        var body = new HashMap<String,Object>();
        body.put("caption", caption);
        body.put("upload_id", id);
        body.put("archive_only", "false");
        body.put("clips_share_preview_to_feed", "1");
        body.put("disable_comments", disableLikenComment ? "1" : "0");
        body.put("igtv_share_preview_to_feed", "1");
        body.put("is_unified_video", "1");
        body.put("like_and_view_counts_disabled", disableLikenComment ? "1" : "0");
        body.put("source_type", "library");
        body.put("video_subtitles_enabled", "0");

        var req = Utils.createPostRequest(this,"media/configure/", body);
        req = req.newBuilder().addHeader("x-ig-app-id","936619743392459").build();
        try (var res = Utils.call(req, this)) {
            var json = new JSONObject(res.body().string());
            if (json.getString("status").equals("ok")) {
                System.out.println("Post successful");
            } else {
                throw new InstagramException(json.toString(3), InstagramException.Reasons.UNKNOWN);
            }
        }
    }

    @AuthenticationType(value = AuthenticationType.Method.MOBILE_AUTH)
    public void uploadStory(@NotNull File photo) throws IOException, InstagramException {
        if (token == null)
            throw new IllegalArgumentException("Story upload is only supported for mobile authenticated users. Make sure that you have Bearer token");


        var extension = photo.getName().substring(photo.getName().lastIndexOf("."));
        if (!(extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".png")))
            throw new IllegalArgumentException("Invalid file type. Only videos are supported");


        var uploadId = Utils.uploadPicture(new FileInputStream(photo), token);
        System.out.println(uploadId);
        var body = new HashMap<String,Object>();
        body.put("upload_id", uploadId);
        body.put("original_media_type", "photo");
        body.put("configure_mode", 1);
        body.put("creation_surface", "camera");
        body.put("has_original_sound", 1);
        body.put("capture_type","normal");

        var req = Utils.createPostRequest(AuthInfo.forMobile(token),"media/configure_to_story/", body);
        try (var res = Utils.call(req, AuthInfo.forMobile(token))) {
            var json = new JSONObject(res.body().string());
            if (json.getString("status").equals("ok")) {
                System.out.println("Story uploaded successfully");
            } else {
                throw new InstagramException(json.toString(3), InstagramException.Reasons.UNKNOWN);
            }
        }

    }

    @AuthenticationType(value = AuthenticationType.Method.MOBILE_AUTH)
    public static DirectMessaging instagramDirect(@NotNull String bearerToken) {
        return new DirectMessaging(AuthInfo.forMobile(bearerToken));
    }

    @AuthenticationType(value = AuthenticationType.Method.MOBILE_AUTH)
    public DirectMessaging directMessaging(){
        if (token == null)
            throw new IllegalArgumentException("Direct messaging is only supported for mobile authenticated users. Make sure that you have Bearer token");

        return new DirectMessaging(AuthInfo.forMobile(token));
    }

    /**
     * Search for a user
     * @param username username of the user
     * @return A map with key as the username and value as profile picture URL
     */
    @AuthenticationType(value = AuthenticationType.Method.WEB_AUTH)
    public Map<String,String> search(@NotNull String username) throws InstagramException, IOException {
        var req = Utils.createGetRequest("web/search/topsearch/?context=user&count=0&query=" + username, this);
        try (var res = Utils.call(req, this)) {
            var json = new JSONObject(res.body().string());
            var users = json.getJSONArray("users");
            var map = new HashMap<String,String>();
            for (int i = 0; i < users.length(); i++) {
                var user = users.getJSONObject(i).getJSONObject("user");
                map.put(user.getString("username"), user.getString("profile_pic_url"));
            }
            return map;
        }
    }
}