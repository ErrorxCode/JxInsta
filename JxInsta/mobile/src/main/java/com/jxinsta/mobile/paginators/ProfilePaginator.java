package com.jxinsta.mobile.paginators;

import com.jxinsta.mobile.InstagramException;
import com.jxinsta.mobile.endpoints.profile.Profile;
import com.jxinsta.mobile.endpoints.profile.ProfileData;
import com.jxinsta.mobile.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Paginator for fetching lists of profiles, such as followers or following, in the mobile API context.
 * Implements {@link Iterator} to provide pages of {@link Profile} objects.
 */
public class ProfilePaginator implements Iterator<List<Profile>> {
    private final String auth;
    private final String pk;
    private final String endpoint;
    private String nextCursor;
    private boolean hasMore = true;

    /**
     * Internal constructor for ProfilePaginator.
     *
     * @param auth       The authentication token.
     * @param pk         The numeric ID (pk) of the user.
     * @param endpoint   The specific endpoint for the profile list (e.g., "followers", "following").
     * @param nextCursor The initial pagination cursor. Pass {@code null} for the first page.
     */
    public ProfilePaginator(String auth, String pk, String endpoint, String nextCursor) {
        this.auth = auth;
        this.pk = pk;
        this.endpoint = endpoint;
        this.nextCursor = nextCursor;
    }

    /**
     * Checks if there are more pages of profiles available.
     *
     * @return {@code true} if another page can be fetched, {@code false} otherwise.
     */
    @Override
    public boolean hasNext() {
        return hasMore;
    }

    /**
     * Fetches the next page of profiles.
     *
     * @return A list of {@link Profile} objects for the current page.
     * @throws NoSuchElementException If no more pages are available.
     * @throws RuntimeException If an {@link InstagramException} occurs during the API call.
     */
    @Override
    public List<Profile> next() {
        if (!hasNext()) throw new NoSuchElementException();

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("count", 200);
            if (nextCursor != null) {
                params.put("max_id", nextCursor);
            }

            var res = Utils.get("friendships/" + pk + "/" + endpoint + "/", auth, params);
            var users = res.getJSONArray("users");
            List<Profile> list = new ArrayList<>();
            for (int i = 0; i < users.length(); i++) {
                var userObj = users.getJSONObject(i);
                list.add(new Profile(userObj, auth));
            }

            if (res.has("next_max_id") && !users.isEmpty()) {
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
