package com.jxinsta.mobile.paginators;

import com.jxinsta.mobile.InstagramException;
import com.jxinsta.mobile.endpoints.post.Comment;
import com.jxinsta.mobile.endpoints.post.Post;
import com.jxinsta.mobile.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * Paginator for fetching comments from an Instagram post in the mobile API context.
 * Implements {@link Iterator} to provide pages of {@link Comment} objects.
 */
public class CommentPaginator implements Iterator<List<Comment>> {
    /** The current pagination cursor. */
    public String cursor;
    private final String id;
    private final String auth;
    private boolean canFetchMore = true;

    /**
     * Internal constructor for CommentPaginator.
     *
     * @param id   The unique identifier (PK) of the post.
     * @param auth The authentication token.
     */
    public CommentPaginator(String id, String auth) {
        this.id = id;
        this.auth = auth;
    }

    /**
     * Checks if there are more comments available to fetch.
     *
     * @return {@code true} if another page can be fetched, {@code false} otherwise.
     */
    @Override
    public boolean hasNext() {
        return canFetchMore;
    }

    /**
     * Fetches the next page of comments.
     *
     * @return A list of {@link Comment} objects for the current page.
     * @throws NoSuchElementException If no more pages are available.
     * @throws RuntimeException If an {@link InstagramException} occurs during the API call.
     */
    @Override
    public List<Comment> next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("can_support_threading", "false");
            if (cursor != null) {
                params.put("min_id", cursor);
            }
            var json = Utils.get("media/" + id + "/comments/", auth, params);
            if (json.has("next_min_id")) {
                cursor = json.getString("next_min_id");
                canFetchMore = true;
            } else {
                cursor = null;
                canFetchMore = false;
            }

            var commentsJson = json.getJSONArray("comments");
            List<Comment> list = new ArrayList<>();
            for (int i = 0; i < commentsJson.length(); i++) {
                var cj = commentsJson.getJSONObject(i);
                list.add(new Comment(auth,cj));
            }
            return list;
        } catch (InstagramException e) {
            throw new RuntimeException(e);
        }
    }
}