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

/**
 * Paginator for fetching posts from a specific user's profile in the mobile API context.
 * Implements {@link Iterator} to provide pages of {@link Post} objects.
 */
public class PostPaginator implements Iterator<List<Post>> {
    private final String auth;
    private final String pk;
    private String nextCursor;
    private boolean hasMore = true;

    /**
     * Internal constructor for PostPaginator.
     *
     * @param auth       The authentication token.
     * @param pk         The numeric ID (pk) of the user whose posts are being fetched.
     * @param nextCursor The initial pagination cursor. Pass {@code null} for the first page.
     */
    public PostPaginator(String auth, String pk, String nextCursor) {
        this.auth = auth;
        this.pk = pk;
        this.nextCursor = nextCursor;
    }

    /**
     * Checks if there are more pages of posts available.
     *
     * @return {@code true} if another page can be fetched, {@code false} otherwise.
     */
    @Override
    public boolean hasNext() {
        return hasMore;
    }

    /**
     * Fetches the next page of posts.
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
            Map<String, Object> params = new HashMap<>();
            params.put("count", 12);
            if (nextCursor != null) {
                params.put("max_id", nextCursor);
            }
            params.put("exclude_comment", true);
            params.put("only_fetch_first_carousel_media", false);

            var res = Utils.get("feed/user/" + pk + "/", auth, params);
            var items = res.getJSONArray("items");
            List<Post> list = new ArrayList<>();
            for (int i = 0; i < items.length(); i++) {
                var item = items.getJSONObject(i);
                list.add(new Post(auth,item));
            }

            if (res.has("next_max_id")) {
                nextCursor = res.getString("next_max_id");
                hasMore = true;
            } else {
                nextCursor = null;
                hasMore = false;
            }

            return list;
        } catch (InstagramException e) {
            throw new RuntimeException(e);
        }
    }
}
