package com.errorxcode.jxinsta.endpoints.profile;

import com.errorxcode.jxinsta.AuthInfo;
import com.errorxcode.jxinsta.InstagramException;
import com.errorxcode.jxinsta.JxInsta;
import com.errorxcode.jxinsta.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import port.org.json.JSONObject;

public class Story {
    protected AuthInfo authInfo;
    public long userPk;
    public String username;
    public String storyId;
    public String download_url;
    public boolean is_video;
    public int views = -1;
    public int duration = -1;
    public int likes = -1;

    public Story(long pk, String id, @NotNull AuthInfo info) {
        this.storyId = id;
        this.userPk = pk;
        this.authInfo = info;
    }

    public static List<Story> getActualStory(String id,@NotNull AuthInfo au) throws InstagramException, IOException {
        List<Story> stories = new ArrayList<>();
        var request = Utils.createGetRequest("feed/reels_media/?reel_ids=" + id,au);
        try (var response = Utils.call(request,null)) {
            if (response.isSuccessful()) {
                var resJson = response.body().string();
                var reels = new JSONObject(resJson).getJSONArray("reels_media");
                if (reels.isEmpty())
                    return null;

                for (int i = 0; i < reels.length(); i++) {
                    var reel = reels.getJSONObject(i);
                    var user = reel.getJSONObject("user");
                    var items = reel.getJSONArray("items");
                    for (int j = 0; j < items.length(); j++) {
                        var item = items.getJSONObject(j);
                        var story = new Story(user.getLong("pk"), item.getString("id"),au);
                        story.username = user.getString("username");
                        story.is_video = item.getInt("media_type") == 2;
                        if (story.is_video) {
                            story.download_url = item.getJSONArray("video_versions").getJSONObject(0).getString("url");
                            story.duration = (int) item.getFloat("video_duration");
                        } else
                            story.download_url = item.getJSONObject("image_versions2").getJSONArray("candidates").getJSONObject(0).getString("url");

                        story.views += item.optInt("viewer_count", -1);
                        stories.add(story);
                    }
                }
            }
            return stories;
        }
    }

        @Override
        public String toString () {
            return "Story{" +
                    "id='" + userPk + '\'' +
                    ", download_url='" + download_url + '\'' +
                    ", isVideo='" + is_video + '\'' +
                    ", views=" + views +
                    ", duration=" + duration +
                    ", likes=" + likes +
                    '}';
        }
    }
