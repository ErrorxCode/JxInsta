package com.jxinsta.mobile.endpoints.profile;

import com.jxinsta.mobile.InstagramException;
import com.jxinsta.mobile.utils.Constants;
import com.jxinsta.mobile.utils.Utils;
import com.jxinsta.mobile.paginators.PostPaginator;
import com.jxinsta.mobile.paginators.ProfilePaginator;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import port.org.json.JSONObject;

/**
 * Represents an Instagram user profile in the mobile API context.
 * Provides methods to fetch user media (posts, stories, highlights) and perform social actions (follow, block).
 */
public class Profile extends ProfileData {
    private final String auth;

    /**
     * Internal constructor for Profile.
     *
     * @param data The JSON object containing profile data.
     * @param auth The authentication token.
     */
    public Profile(@NotNull JSONObject data, @NotNull String auth) {
        super(data);
        this.auth = auth;
    }

    /**
     * Returns a paginator for fetching posts by this user.
     *
     * @return A {@link PostPaginator}.
     */
    public PostPaginator getPosts() {
        return new PostPaginator(auth, pk, null);
    }

    /**
     * Returns a paginator for fetching this user's followers.
     *
     * @return A {@link ProfilePaginator} for followers.
     */
    public ProfilePaginator getFollowers() {
        return new ProfilePaginator(auth, pk, "followers", null);
    }

    /**
     * Returns a paginator for fetching accounts this user is following.
     *
     * @return A {@link ProfilePaginator} for followings.
     */
    public ProfilePaginator getFollowings() {
        return new ProfilePaginator(auth, pk, "following", null);
    }

    /**
     * Fetches the active story reel for this user.
     *
     * @return A list of {@link Story} objects.
     * @throws InstagramException If the API returns an error.
     */
    public List<Story> getStory() throws InstagramException {
        var res = Utils.get(Constants.Endpoints.userReelMedia(pk), auth, null);
        var list = new ArrayList<Story>();
        if (res.has("items")) {
            var items = res.getJSONArray("items");
            for (int i = 0; i < items.length(); i++) {
                list.add(new Story(items.getJSONObject(i),auth));
            }
        }
        return list;
    }

    /**
     * Fetches the story highlights for this user.
     *
     * @return A list of {@link Story} objects belonging to highlights.
     * @throws InstagramException If the API returns an error.
     */
    public List<Story> getHighlights() throws InstagramException {
        var res = Utils.get(Constants.Endpoints.userHighlightsTray(pk), auth, null);
        var list = new ArrayList<Story>();
        if (res.has("tray")) {
            var tray = res.getJSONArray("tray");
            for (int i = 0; i < tray.length(); i++) {
                var item = tray.getJSONObject(i);
                if (item.has("items")) {
                    var items = item.getJSONArray("items");
                    for (int j = 0; j < items.length(); j++) {
                        list.add(new Story(items.getJSONObject(j),auth));
                    }
                }
            }
        }
        return list;
    }

    /**
     * Follows this user.
     *
     * @throws InstagramException If the API returns an error.
     */
    public void follow() throws InstagramException {
        Utils.post(Constants.Endpoints.friendshipsCreate(pk), auth, null);
    }

    /**
     * Unfollows this user.
     *
     * @throws InstagramException If the API returns an error.
     */
    public void unfollow() throws InstagramException {
        Utils.post(Constants.Endpoints.friendshipsDestroy(pk), auth, null);
    }

    /**
     * Blocks this user.
     *
     * @throws InstagramException If the API returns an error.
     */
    public void block() throws InstagramException {
        Utils.post(Constants.Endpoints.friendshipsBlock(pk), auth, null);
    }
}
