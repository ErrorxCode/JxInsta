package com.jxinsta.mobile.endpoints.direct;

import com.jxinsta.mobile.InstagramException;
import com.jxinsta.mobile.utils.Constants;
import com.jxinsta.mobile.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import port.org.json.JSONArray;
import port.org.json.JSONObject;

/**
 * Represents the Direct Message inbox for an Instagram account.
 * This class provides access to existing threads and users in the inbox, 
 * as well as methods to fetch message requests or specific threads.
 */
public class Inbox {
    private final String auth;
    /** The total number of threads currently loaded in the inbox. */
    public final int totalThreads;
    /** The list of {@link Thread} objects in the inbox. */
    public final List<Thread> threads;
    /** The list of usernames involved in the inbox threads. */
    public final List<String> users;


    /**
     * Internal constructor for Inbox.
     *
     * @param auth The authentication token.
     * @param json The JSON object containing inbox data.
     */
    public Inbox(String auth, JSONObject json) {
        this.auth = auth;

        // Extract threads
        JSONArray threadsArray = json.optJSONObject("inbox").optJSONArray("threads");
        this.totalThreads = threadsArray != null ? threadsArray.length() : 0;
        this.threads = new ArrayList<>();

        if (threadsArray != null) {
            for (int i = 0; i < threadsArray.length(); i++) {
                JSONObject threadJson = threadsArray.getJSONObject(i);
                this.threads.add(new Thread(auth,threadJson));
            }
        }

        // Extract users
        JSONArray usersArray = json.optJSONObject("inbox").optJSONArray("users");
        this.users = new ArrayList<>();
        if (usersArray != null) {
            for (int i = 0; i < usersArray.length(); i++) {
                JSONObject userJson = usersArray.getJSONObject(i);
                this.users.add(userJson.optString("username"));
            }
        }
    }

    /**
     * Fetches the list of users who are currently online.
     * 
     * @return A list of usernames.
     * @throws InstagramException If the API returns an error.
     */
    private List<String> getOnlineUsers() throws InstagramException {
        return null;
        // Unimplemented
    }

    /**
     * Fetches pending message requests.
     *
     * @param maxThreads  The maximum number of threads to fetch.
     * @param maxMessages The maximum number of messages per thread to fetch.
     * @return A list of {@link Thread} objects representing the requests.
     * @throws InstagramException If the API returns an error.
     */
    public List<Thread> getRequests(int maxThreads,int maxMessages) throws InstagramException {
        var params = new HashMap<String,Object>();
        params.put("visual_message_return_type", "all");
        params.put("limit", maxThreads);
        params.put("is_prefetching", false);
        params.put("thread_message_limit", maxMessages);
        var res = Utils.get(Constants.Endpoints.DM_REQUESTS, auth, params);
        var list = new ArrayList<Thread>();
        var threads = res.optJSONObject("inbox").optJSONArray("threads");
        for (int i = 0; i < threads.length(); i++) {
            list.add(new Thread(auth,threads.getJSONObject(i)));
        }
        return list;
    }

    /**
     * Fetches a specific thread by its ID.
     *
     * @param id          The unique identifier of the thread.
     * @param maxMessage The maximum number of recent messages to fetch in the thread.
     * @return A {@link Thread} instance.
     * @throws InstagramException If the API returns an error.
     */
    public Thread getThread(String id,int maxMessage) throws InstagramException {
        var params = new HashMap<String,Object>();
        params.put("visual_message_return_type", "all");
        params.put("limit", maxMessage);
        params.put("direction", "older");
        var res = Utils.get(Constants.Endpoints.getThread(id), auth, params);
        return new Thread(auth,res.optJSONObject("thread"));
    }
}
