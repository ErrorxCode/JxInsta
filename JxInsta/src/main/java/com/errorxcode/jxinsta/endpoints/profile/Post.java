package com.errorxcode.jxinsta.endpoints.profile;

import com.errorxcode.jxinsta.AuthInfo;
import com.errorxcode.jxinsta.Constants;
import com.errorxcode.jxinsta.InstagramException;
import com.errorxcode.jxinsta.JxInsta;
import com.errorxcode.jxinsta.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import port.org.json.JSONObject;

public class Post {
    public final AuthInfo authInfo;
    public final long id;
    public boolean isVideo;
    public String shortcode;
    public String download_url;
    public String next_cursor;
    public String caption;
    public int likes;
    public int comments;

    public Post(@Nullable AuthInfo authInfo, long id) {
        this.authInfo = authInfo;
        this.id = id;
    }


    public static Post getPost(@NotNull String url) throws InstagramException, IOException {
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
                .addHeader("user-agent", Constants.WEB_USER_AGENT)
                .addHeader("x-fb-lsd", "JxInsta")
                .method("POST",body)
                .build();

        try (var res = Utils.call(req,null)) {
            var json = new JSONObject(res.body().string());
            var data = json.getJSONObject("data");
            var shortcode_media = data.getJSONObject("xdt_shortcode_media");
            var post = new Post(null,shortcode_media.getLong("id"));
            post.shortcode = shortcode;
            post.caption = shortcode_media.getJSONObject("edge_media_to_caption").getJSONArray("edges").getJSONObject(0).getJSONObject("node").getString("text");
            post.likes = shortcode_media.getJSONObject("edge_media_preview_like").getInt("count");
            post.comments = shortcode_media.getJSONObject("edge_media_to_comment").getInt("count");
            post.isVideo = shortcode_media.getBoolean("is_video");
            post.download_url = post.isVideo ? shortcode_media.getString("video_url") : shortcode_media.getString("display_url");
            return post;

        }

    }

    public void like() throws InstagramException, IOException {
        var req = Utils.createPostRequest(authInfo,"web/likes/" + id + "/like/", null);
        try (var res = Utils.call(req,authInfo)) {
            var json = new JSONObject(res.body().string());
            if (json.has("status") && !json.getString("status").equals("ok")) {
                throw new InstagramException(json.getString("message"), InstagramException.Reasons.UNKNOWN);
            }
        }
    }

    public void dislike() throws InstagramException, IOException {
        var req = Utils.createPostRequest(authInfo,"web/likes/" + id + "/unlike/", null);
        try (var res = Utils.call(req,authInfo)) {
            var json = new JSONObject(res.body().string());
            if (json.has("status") && !json.getString("status").equals("ok")) {
                throw new InstagramException(json.getString("message"), InstagramException.Reasons.UNKNOWN);
            }
        }
    }

    public List<String> likers() throws InstagramException, IOException {
        var req = Utils.createGetRequest("media/" + id + "/likers/",authInfo);
        try (var res = Utils.call(req,null)) {
            var json = new JSONObject(res.body().string());
            if (json.has("status") && !json.getString("status").equals("ok")) {
                throw new InstagramException(json.getString("message"), InstagramException.Reasons.UNKNOWN);
            }
            var likers = json.getJSONArray("users");
            var list = new ArrayList<String>();
            for (int i = 0; i < likers.length(); i++) {
                list.add(likers.getJSONObject(i).getString("username"));
            }
            return list;
        }
    }

    public List<Comment> getComments(int count) throws IOException, InstagramException {
        var req = Utils.createGetRequest("media/" + id + "/comments?can_support_threading=true&permalink_enabled=false",authInfo);
        try (var res = Utils.call(req,null)) {
            var json = new JSONObject(res.body().string());
            if (json.has("status") && !json.getString("status").equals("ok")) {
                throw new InstagramException(json.getString("message"), InstagramException.Reasons.UNKNOWN);
            }
            var comments = json.getJSONArray("comments");
            var list = new ArrayList<Comment>();
            for (int i = 0; i < comments.length(); i++) {
                var comment = comments.getJSONObject(i);
                var c = new Comment(authInfo,comment.getString("pk"),id);
                c.mediaId = comment.getLong("media_id");
                c.text = comment.getString("text");
                c.username = comment.getJSONObject("user").getString("username");
                c.likes = comment.getInt("comment_like_count");
                c.timestamp = comment.getLong("created_at");
                list.add(c);
            }
            return list;
        }
    }


    public void comment(@NotNull String comment) throws InstagramException, IOException {
        var req = Utils.createPostRequest(authInfo,"web/comments/" + id + "/add/",Map.of("comment_text",comment));
        try (var res = Utils.call(req,authInfo)) {
            var json = new JSONObject(res.body().string());
            if (json.has("status") && !json.getString("status").equals("ok")) {
                throw new InstagramException(json.getString("message"), InstagramException.Reasons.UNKNOWN);
            }
        }
    }

    @Override
    public String toString() {
        return "Post{" +
                "id='" + id + '\'' +
                ", shortcode='" + shortcode + '\'' +
                ", download_url='" + download_url + '\'' +
                ", page_max_id='" + next_cursor + '\'' +
                ", caption='" + caption + '\'' +
                ", likes=" + likes +
                ", comments=" + comments +
                '}';
    }

    public static class Comment {
        public final String id;
        public long mediaId;
        public String text;
        public String username;
        public int likes;
        public long timestamp;
        public final AuthInfo authInfo;

        public Comment(AuthInfo authInfo,String id,long postId) {
            this.id = id;
            this.mediaId = postId;
            this.authInfo = authInfo;
        }

        public void like() throws InstagramException, IOException {
            var req = Utils.createPostRequest(authInfo,"web/comments/like/" + id + "/", null);
            System.out.println(req.headers());
            System.out.println(req.url());
            try (var res = Utils.call(req,authInfo)) {
                var json = new JSONObject(res.body().string());
                if (json.has("status") && !json.getString("status").equals("ok")) {
                    throw new InstagramException(json.getString("message"), InstagramException.Reasons.UNKNOWN);
                }
            }
        }


        public void dislike() throws InstagramException, IOException {
            var req = Utils.createPostRequest(authInfo,"web/comments/unlike/" + id + "/", null);
            try (var res = Utils.call(req,authInfo)) {
                var json = new JSONObject(res.body().string());
                if (json.has("status") && !json.getString("status").equals("ok")) {
                    throw new InstagramException(json.getString("message"), InstagramException.Reasons.UNKNOWN);
                }
            }
        }

        public void reply(@NotNull String reply) throws InstagramException, IOException {
            var req = Utils.createPostRequest(authInfo,"web/comments/" + mediaId + "/add/", Map.of("comment_text", reply, "replied_to_comment_id", id));
            try (var res = Utils.call(req,authInfo)) {
                var json = new JSONObject(res.body().string());
                if (json.has("status") && !json.getString("status").equals("ok")) {
                    throw new InstagramException(json.getString("message"), InstagramException.Reasons.UNKNOWN);
                }
            }
        }
    }
}
