package com.jxinsta.mobile.endpoints.profile;

import port.org.json.JSONObject;

/**
 * A data class representing basic information about an Instagram profile in the mobile API context.
 * This class is used for read-only user information and as a base for the interactive {@link Profile} class.
 */
public class ProfileData {
    /** The username of the account. */
    public String username;
    /** The unique identifier (PK) of the account. */
    public String pk;
    /** The full name displayed on the profile. */
    public String name;
    /** The bio/biography of the user. */
    public String biography;
    /** The URL of the profile picture. */
    public String profilePicURL;
    /** Indicates if the account is a business account. */
    public boolean isBusinessAccount;
    /** Indicates if the account is private. */
    public boolean isPrivate;
    /** Indicates if the account is verified. */
    public boolean isVerified;
    /** The total number of posts made by this user. */
    public int posts;
    /** The number of followers. */
    public int followers;
    /** The number of accounts this user is following. */
    public int followings;


    /**
     * Constructs a ProfileData object from a JSON user object.
     *
     * @param user The JSON object containing user data from Instagram's mobile API.
     */
    public ProfileData(JSONObject user) {
        this.pk = user.optString("pk_id", user.has("pk") ? String.valueOf(user.get("pk")) : user.optString("id", null));
        this.username = user.optString("username");
        this.name = user.optString("full_name");
        this.biography = user.optString("biography");
        this.profilePicURL = user.optString("profile_pic_url");
        this.isBusinessAccount = user.optBoolean("is_business");
        this.isPrivate = user.optBoolean("is_private");
        this.isVerified = user.optBoolean("is_verified");
        this.posts = user.optInt("media_count");
        this.followers = user.optInt("follower_count");
        this.followings = user.optInt("following_count");
    }

    @Override
    public String toString() {
        return "ProfileData{" +
                "username='" + username + '\'' +
                ", pk='" + pk + '\'' +
                ", name='" + name + '\'' +
                ", biography='" + biography + '\'' +
                ", profilePicURL='" + profilePicURL + '\'' +
                ", isBusinessAccount=" + isBusinessAccount +
                ", isPrivate=" + isPrivate +
                ", isVerified=" + isVerified +
                ", posts=" + posts +
                ", followers=" + followers +
                ", followings=" + followings +
                '}';
    }
}
