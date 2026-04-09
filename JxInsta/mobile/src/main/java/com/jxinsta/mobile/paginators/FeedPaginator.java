package com.jxinsta.mobile.paginators;

import com.jxinsta.mobile.InstagramException;
import com.jxinsta.mobile.endpoints.post.Post;
import com.jxinsta.mobile.endpoints.post.PostData;
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
 * Paginator for fetching the home feed posts of the logged-in user in the mobile API context.
 * Implements {@link Iterator} to provide pages of {@link Post} objects.
 */
public class FeedPaginator implements Iterator<List<Post>> {
    private final String auth;
    private String nextCursor;
    private boolean hasMore = true;

    /**
     * Internal constructor for FeedPaginator.
     *
     * @param auth The authentication token.
     */
    public FeedPaginator(String auth) {
        this.auth = auth;
    }

    /**
     * Checks if there are more pages available in the feed.
     *
     * @return {@code true} if another page can be fetched, {@code false} otherwise.
     */
    @Override
    public boolean hasNext() {
        return hasMore;
    }

    /**
     * Fetches the next page of feed posts.
     *
     * @return A list of {@link Post} objects for the current page.
     * @throws NoSuchElementException If no more pages are available.
     * @throws RuntimeException If an {@link InstagramException} occurs during the API call.
     */
    @Override
    public List<Post> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        try {
            Map<String, String> body = new HashMap<>();
            body.put("reason", "pagination");
            body.put("battery_level", "69");
            body.put("is_pull_to_refresh", "0");
            body.put("is_charging", "1");
            if (nextCursor != null) {
                body.put("max_id", nextCursor);
            }

            JSONObject res = Utils.post("feed/timeline/", auth, body);
            
            if (!res.has("items")) {
                hasMore = false;
                return List.of();
            }

            // update pagination state
            this.nextCursor = res.optString("next_max_id", null);
            this.hasMore = res.optBoolean("more_available", false);

            // parse posts
            JSONArray itemsArray = res.optJSONArray("items");
            List<Post> posts = new ArrayList<>();
            if (itemsArray != null) {
                for (int i = 0; i < itemsArray.length(); i++) {
                    JSONObject postJson = itemsArray.getJSONObject(i);
                    posts.add(new Post(auth, postJson));
                }
            }

            return posts;
        } catch (InstagramException e) {
            throw new RuntimeException(e);
        }
    }
}
