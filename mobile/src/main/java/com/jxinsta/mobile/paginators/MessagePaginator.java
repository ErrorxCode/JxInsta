package com.jxinsta.mobile.paginators;

import com.jxinsta.mobile.InstagramException;
import com.jxinsta.mobile.endpoints.direct.Message;
import com.jxinsta.mobile.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import port.org.json.JSONArray;
import port.org.json.JSONObject;

/**
 * Paginator for fetching older messages in a direct message thread.
 * Implements {@link Iterator} to provide pages of {@link Message} objects.
 */
public class MessagePaginator implements Iterator<List<Message>> {
    private final String auth;
    private final String thread_id;
    private String cursor;
    private boolean hasMore = true;

    /**
     * Internal constructor for MessagePaginator.
     *
     * @param auth     The authentication token.
     * @param threadId The unique identifier of the DM thread.
     * @param cursor   The initial pagination cursor.
     */
    public MessagePaginator(String auth, String threadId, String cursor) {
        this.auth = auth;
        this.thread_id = threadId;
        this.cursor = cursor;
    }

    /**
     * Checks if there are more messages available in the thread's history.
     *
     * @return {@code true} if another page can be fetched, {@code false} otherwise.
     */
    @Override
    public boolean hasNext() {
        return hasMore;
    }

    /**
     * Fetches the next page of messages (older than the current cursor).
     *
     * @return A list of {@link Message} objects for the current page.
     * @throws NoSuchElementException If no more pages are available.
     * @throws RuntimeException If an {@link InstagramException} occurs during the API call.
     */
    @Override
    public List<Message> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("visual_message_return_type", "unseen");
            params.put("limit", 20);
            params.put("direction", "older");
            if (cursor != null && !cursor.isEmpty()) {
                params.put("cursor", cursor);
            }

            JSONObject res = Utils.get("direct_v2/threads/" + thread_id + "/", auth, params);
            
            if (!res.has("thread")) {
                hasMore = false;
                return List.of();
            }

            JSONObject thread = res.getJSONObject("thread");
            JSONArray items = thread.optJSONArray("items");
            List<Message> list = new ArrayList<>();
            
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    list.add(new Message(items.getJSONObject(i)));
                }
            }

            if (thread.has("oldest_cursor")) {
                cursor = thread.getString("oldest_cursor");
                hasMore = cursor != null && !cursor.isEmpty() && !cursor.equals("null");
            } else {
                cursor = null;
                hasMore = false;
            }

            return list;
        } catch (InstagramException e) {
            throw new RuntimeException(e);
        }
    }
}
