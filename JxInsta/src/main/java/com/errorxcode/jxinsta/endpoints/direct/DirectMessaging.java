package com.errorxcode.jxinsta.endpoints.direct;

import com.errorxcode.jxinsta.AuthInfo;
import com.errorxcode.jxinsta.InstagramException;
import com.errorxcode.jxinsta.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import port.org.json.JSONObject;

public class DirectMessaging {
    private final AuthInfo authInfo;
    private String nextCursor;

    public DirectMessaging(AuthInfo info) {
        this.authInfo = info;
    }

    public Thread getThread(String threadID) throws InstagramException, IOException {
        return new Thread(threadID, authInfo);
    }

    public void deleteThread(String threadID) throws InstagramException, IOException {
        Thread.delete(authInfo, threadID);
    }

    public List<Thread> listThreads(int limit,int folder) throws InstagramException, IOException {
        var params = new HashMap<String,Object>();
        params.put("fetch_reason", "manual_refresh");
        params.put("visual_message_return_type", "unseen");
        params.put("folder",folder);
        params.put("limit",limit);
        params.put("thread_message_limit",20);
        if (nextCursor != null)
            params.put("cursor",nextCursor);


        var req = Utils.createGetRequest("direct_v2/inbox/?" + Utils.map2query(params), authInfo);
        try (var res = Utils.call(req, authInfo)) {
            var json = new JSONObject(res.body().string());
            var status = json.getString("status");
            if (!status.equals("ok")) {
                throw new InstagramException("Error in getting threads: " + json, InstagramException.Reasons.UNKNOWN);
            }


            var inbox = json.getJSONObject("inbox");
            nextCursor = inbox.optString("oldest_cursor",null);
            var threads = inbox.getJSONArray("threads");
            var list = new ArrayList<Thread>();

            for (int i = 0; i < threads.length(); i++) {
                var thread = threads.getJSONObject(i);
                var mThread = new Thread(thread.getString("thread_id"), authInfo);
                mThread.username = thread.getString("thread_title");
                mThread.pk = thread.getLong("thread_id");
                mThread.previousCursor = thread.getString("prev_cursor");

                var mMessages = thread.getJSONArray("items");
                var messages = new Message[mMessages.length()];
                for (int j = 0; j < mMessages.length(); j++) {
                    messages[j] = Message.fromJSON(mMessages.getJSONObject(j));
                }
                mThread.messages = messages;
                list.add(mThread);
            }
            return list;
        }
    }
}
