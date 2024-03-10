package com.errorxcode.jxinsta.endpoints.direct;

import com.errorxcode.jxinsta.AuthInfo;
import com.errorxcode.jxinsta.Constants;
import com.errorxcode.jxinsta.InstagramException;
import com.errorxcode.jxinsta.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import port.org.json.JSONObject;

public class Thread {
    public final AuthInfo authInfo;
    public String threadId;
    public long pk;
    public String username;
    public String previousCursor;
    public Message[] messages;

    public Thread(@NotNull String threadId,@NotNull AuthInfo info) throws InstagramException, IOException {
        this.threadId = threadId;
        this.authInfo = info;
        fetchThread(10);
    }

    public Thread(@NotNull AuthInfo authInfo){
        this.authInfo = authInfo;
    }

    private void fetchThread(int count) throws InstagramException, IOException {
        var params = new HashMap<String,Object>();
        params.put("visual_message_return_type","unseen");
        params.put("direction","older");
        params.put("seq_id",40065);
        params.put("limit",count);
        if (previousCursor != null) {
            params.put("cursor",previousCursor);
        }

        var req = Utils.createGetRequest("direct_v2/threads/" + threadId + "/?" + Utils.map2query(params),authInfo);
        try (var res = Utils.call(req,null)) {
            var json = new JSONObject(res.body().string());
            var status = json.getString("status");
            if (!status.equals("ok")) {
                throw new InstagramException("Error in getting thread: " + json, InstagramException.Reasons.UNKNOWN);
            }

            var thread = json.getJSONObject("thread");
            pk = thread.getJSONArray("users").getJSONObject(0).getLong("pk");
            username = thread.getString("thread_title");
            previousCursor = thread.getString("prev_cursor");

            var items = thread.getJSONArray("items");
            messages = new Message[items.length()];

            for (int i = 0; i < items.length(); i++) {
                messages[i] = Message.fromJSON(items.getJSONObject(i));
            }
        }
    }

    public String sendMessage(String message,boolean forwarded) throws IOException, InstagramException {
        var body = new HashMap<String,Object>();
        body.put("text",message);
        body.put("client_context",Math.random()* messages.length);
        body.put("recipient_users",Arrays.toString(new long[]{pk}));
        body.put("send_attribution","message_button");
        body.put("action","send_item");
        body.put("send_silently",false);
        body.put("is_x_transport_forward",forwarded);

        var req = Utils.createPostRequest(authInfo,"direct_v2/threads/broadcast/text/",body);
        try (var res = Utils.call(req,null)) {
            var json = new JSONObject(res.body().string());
            var status = json.getString("status");
            if (!status.equals("ok")) {
                throw new InstagramException("Error in sending message: " + json, InstagramException.Reasons.UNKNOWN);
            }
            return json.getJSONObject("payload").getString("item_id");
        }
    }

    private long uploadPhoto(@NotNull File photo) throws InstagramException, IOException {
        if (!photo.exists()) {
            throw new InstagramException("File does not exist", InstagramException.Reasons.UNKNOWN);
        } else {
            var extension = photo.getName().substring(photo.getName().lastIndexOf("."));
            if (!extension.equals(".jpg") && !extension.equals(".png") && !extension.equals(".jpeg")) {
                throw new InstagramException("Invalid file type", InstagramException.Reasons.UNKNOWN);
            }
        }

        var req = new Request.Builder()
                .url("https://rupload.facebook.com/messenger_image/" + photo.getName())
                .post(RequestBody.create(photo,MediaType.parse("application/octet-stream")))
                .headers(Headers.of(Constants.BASE_HEADERS))
                .addHeader("x-entity-length",String.valueOf(photo.length()))
                .addHeader("x-entity-name",photo.getName())
                .addHeader("x-entity-type","image/jpeg")
                .addHeader("image_type","FILE_ATTACHMENT")
                .addHeader("user-agent",Constants.MOBILE_USER_AGENT)
                .addHeader("authorization",authInfo.authorization)
                .addHeader("offset","0")
                .build();

        try (var res = Utils.call(req,null)) {
            return new JSONObject(res.body().string()).getLong("media_id");
        }
    }

    public void sendPhoto(@NotNull File photo) throws IOException, InstagramException {
        var id = uploadPhoto(photo);
        var body = new HashMap<String,Object>();
        body.put("attachment_fbid",id);
        body.put("allow_full_aspect_ratio",true);
        body.put("recipient_users",Arrays.toString(new long[]{pk}));
        body.put("action","send_item");
        body.put("is_x_transport_forward",false);
        var req = Utils.createPostRequest(authInfo,"direct_v2/threads/broadcast/photo_attachment/",body);
        try (var res = Utils.call(req,null)) {
            var json = new JSONObject(res.body().string());
            var status = json.getString("status");
            if (!status.equals("ok")) {
                throw new InstagramException("Error in sending photo: " + json, InstagramException.Reasons.UNKNOWN);
            }
        }
    }

    public void markSeen() throws InstagramException, IOException {
        var req = Utils.createPostRequest(authInfo,"direct_v2/threads/" + threadId + "/items/" + messages[0].id + "/seen/",null);
        try (var res = Utils.call(req,authInfo)) {
            var json = new JSONObject(res.body().string());
            var status = json.getString("status");
            if (!status.equals("ok")) {
                throw new InstagramException("Error in marking seen: " + json, InstagramException.Reasons.UNKNOWN);
            }
        }
    }

    public List<Message> getMessages(int count) throws InstagramException, IOException {
        fetchThread(count);
        return Arrays.asList(messages);
    }

    public void unsend(@NotNull String messageId) throws InstagramException, IOException {
        var req = Utils.createPostRequest(authInfo,"direct_v2/threads/" + threadId + "/items/" + messageId + "/delete/",null);
        try (var res = Utils.call(req,authInfo)) {
            var json = new JSONObject(res.body().string());
            var status = json.getString("status");
            if (!status.equals("ok")) {
                throw new InstagramException("Error in unsent: " + json, InstagramException.Reasons.UNKNOWN);
            }
        }
    }

    public void delete() throws InstagramException, IOException {
        delete(authInfo,threadId);
    }

    protected static void delete(@NotNull AuthInfo authInfo,@NotNull String threadId) throws InstagramException, IOException {
        var req = Utils.createPostRequest(authInfo,"direct_v2/threads/" + threadId + "/hide/", Map.of("should_move_future_requests_to_spam",false));
        try (var res = Utils.call(req,authInfo)) {
            var json = new JSONObject(res.body().string());
            var status = json.getString("status");
            if (!status.equals("ok")) {
                throw new InstagramException("Error in deleting thread: " + json, InstagramException.Reasons.UNKNOWN);
            }
        }
    }

    @Override
    public String toString() {
        return "Thread{" +
                "authInfo=" + authInfo +
                ", threadId='" + threadId + '\'' +
                ", pk='" + pk + '\'' +
                ", username='" + username + '\'' +
                ", previousCursor='" + previousCursor + '\'' +
                ", messages=" + Arrays.toString(messages) +
                '}';
    }
}
