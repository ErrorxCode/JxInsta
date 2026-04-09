package com.jxinsta.mobile.endpoints.direct;

import java.util.ArrayList;
import java.util.List;

import port.org.json.JSONArray;
import port.org.json.JSONObject;

/**
 * Represents a group direct message thread on Instagram.
 * Extends {@link Thread} to include group-specific data like recipients and admins.
 */
public class Group extends Thread {
    /** The list of usernames of the recipients in the group. */
    public List<String> recipients = new ArrayList<>();
    /** The list of usernames of users who have left the group. */
    public List<String> leftUsers = new ArrayList<>();
    /** The list of user IDs (PKs) of the group administrators. */
    public List<Long> admins = new ArrayList<>();
    /** The URL of the group's display picture. */
    public String displayPicture;
    /** The cursor for fetching older messages in this group. */
    protected String oldestCursor;

    /**
     * Internal constructor for Group.
     *
     * @param auth   The authentication token.
     * @param thread The JSON object containing group thread data.
     */
    public Group(String auth, JSONObject thread) {
        super(auth, thread);
        this.oldestCursor = thread.optString("oldest_cursor");

        JSONArray usersArray = thread.optJSONArray("users");
        if (usersArray != null) {
            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject userJson = usersArray.getJSONObject(i);
                recipients.add(userJson.optString("username"));
            }
        }

        JSONArray leftUsersArray = thread.optJSONArray("left_users");
        if (leftUsersArray != null) {
            for (int i = 0; i < leftUsersArray.length(); i++) {
                JSONObject leftUserJson = leftUsersArray.getJSONObject(i);
                leftUsers.add(leftUserJson.optString("username"));
            }
        }

        JSONArray adminsArray = thread.optJSONArray("admin_user_ids");
        if (adminsArray != null) {
            for (int i = 0; i < adminsArray.length(); i++) {
                var adminId = adminsArray.getLong(i);
                admins.add(adminId);
            }
        }

        var threadImageJson = thread.optJSONObject("thread_image");
        if (threadImageJson != null) {
            var candidates = threadImageJson.optJSONObject("image_versions2").optJSONArray("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                displayPicture = candidates.getJSONObject(0).optString("url");
            }
        }
    }
}
