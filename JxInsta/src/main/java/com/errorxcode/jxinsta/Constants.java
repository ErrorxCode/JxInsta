package com.errorxcode.jxinsta;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String BASE_URL = "https://i.instagram.com/api/v1/";
    public static final String LOGIN_URL = BASE_URL + "web/accounts/login/ajax/";
    public static final String MOBILE_USER_AGENT = "Instagram 244.0.0.17.110 Android";
    public static final String WEB_USER_AGENT = "Mozilla/5.0";
    public static final String X_APP_ID = "936619743392459";
    public static final Map<String,String> BASE_HEADERS = new HashMap<>(){
        {
            put("authority", "www.instagram.com");
            put("accept", "application/json");
            put("origin", "https://www.instagram.com");
            put("content-type", "application/x-www-form-urlencoded");

        }
    };



    public static class GraphQl {
        public static final String BASE_URL = "https://www.instagram.com/graphql/query/?query_id=";
        public static final String USER_FOLLOWING = "17874545323001329";
        public static final String USER_FOLLOWERS = "17851374694183129";
        public static final String COMMENTS = "17852405266163336";
        public static final String LIKES = "17864450716183058";
        public static final String FEED_POSTS = "17842794232208280";
    }
}
