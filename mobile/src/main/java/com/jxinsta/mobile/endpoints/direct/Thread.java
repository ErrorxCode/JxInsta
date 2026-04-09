package com.jxinsta.mobile.endpoints.direct;

import com.jxinsta.mobile.InstagramException;
import com.jxinsta.mobile.paginators.MessagePaginator;
import com.jxinsta.mobile.utils.Constants;
import com.jxinsta.mobile.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import port.org.json.JSONArray;
import port.org.json.JSONObject;

/**
 * Represents a direct message thread on Instagram.
 * A thread can be a one-on-one conversation or a group chat.
 */
public class Thread {
    private final String auth;
    /** The unique identifier of the thread. */
    public final String id;
    /** The display title of the thread. */
    public final String title;
    /** Indicates if the thread is a group chat. */
    public final boolean isGroup;
    /** The list of messages currently loaded in this thread. */
    public final List<Message> messages;
    /** Information about the group, if this is a group thread. */
    public final Group group;

    /**
     * Internal constructor for Thread.
     *
     * @param auth       The authentication token.
     * @param threadJson The JSON object containing thread data.
     */
    public Thread(String auth, JSONObject threadJson) {
        this.auth = auth;
        this.id = threadJson.getString("thread_id");
        this.title = threadJson.optString("thread_title");
        this.isGroup = threadJson.optBoolean("is_group");
        this.messages = new ArrayList<>();
        
        JSONArray items = threadJson.optJSONArray("items");
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                this.messages.add(new Message(auth, items.getJSONObject(i), id));
            }
        }

        if (isGroup) {
            this.group = new Group(auth,threadJson);
        } else {
            this.group = null;
        }
    }

    /**
     * Sends a text message to this thread.
     *
     * @param text The message content.
     * @throws InstagramException If the API returns an error.
     */
    public void sendMessage(@NotNull String text) throws InstagramException {
        var params = new HashMap<String,Object>();
        params.put("text", text);
        params.put("thread_ids", "[" + id + "]");
        params.put("action", "send_item");
        params.put("client_context", Utils.genClientContext());
        Utils.post(Constants.Endpoints.DM_SEND, auth, Utils.genSignedBody(params));
    }

    /**
     * Marks the thread as seen.
     *
     * @throws InstagramException If the API returns an error.
     */
    public void markAsSeen() throws InstagramException {
        var params = new HashMap<String,Object>();
        params.put("thread_id", id);
        params.put("action", "mark_as_seen");
        Utils.post(Constants.Endpoints.DM_SEEN, auth, Utils.genSignedBody(params));
    }

    /**
     * Leaves the thread (only for group chats).
     *
     * @throws InstagramException If the API returns an error.
     */
    public void leave() throws InstagramException {
        Utils.post(Constants.Endpoints.DM_LEAVE, auth, Map.of("thread_id", id));
    }

    /**
     * Returns a paginator to fetch older messages in this thread.
     *
     * @return A {@link MessagePaginator}.
     */
    public MessagePaginator getMessagePaginator() {
        var lastMessageId = messages.isEmpty() ? null : messages.get(messages.size() - 1).id;
        return new MessagePaginator(auth, id, lastMessageId);
    }
}
