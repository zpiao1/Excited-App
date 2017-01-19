package com.example.zpiao1.excited.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
    public static final int STATUS_INIT = 0;
    public static final int STATUS_LOGGED_IN = 1;
    public static final int STATUS_LOGGED_OUT = 2;
    public static final int STATUS_PASSWORD_CHANGED = 3;
    @SerializedName("_id")
    @Expose
    public String id;
    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("localProfile")
    @Expose
    public LocalProfile localProfile;
    @SerializedName("facebookProfile")
    @Expose
    public FacebookProfile facebookProfile;
    @SerializedName("googleProfile")
    @Expose
    public GoogleProfile googleProfile;
    @SerializedName("hasLocalProfile")
    @Expose
    public Boolean hasLocalProfile;
    public int status = STATUS_INIT;

    public String getEmail() {
        return email;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        if (localProfile != null && localProfile.displayName != null)
            return localProfile.displayName;
        if (googleProfile != null && googleProfile.displayName != null)
            return googleProfile.displayName;
        if (facebookProfile != null && facebookProfile.displayName != null)
            return facebookProfile.displayName;
        return null;
    }

    public String getImageUrl() {
        if (localProfile != null && localProfile.imageUrl != null)
            return localProfile.imageUrl;
        if (googleProfile != null && googleProfile.imageUrl != null)
            return googleProfile.imageUrl;
        if (facebookProfile != null && facebookProfile.imageUrl != null)
            return facebookProfile.imageUrl;
        return null;
    }

    public String getGoogleEmail() {
        return (googleProfile != null ? googleProfile.email : null);
    }

    public String getFacebookEmail() {
        return (facebookProfile != null ? facebookProfile.email : null);
    }

    public String getGoogleId() {
        return (googleProfile != null ? googleProfile.googleId : null);
    }

    public String getFacebookId() {
        return (facebookProfile != null ? facebookProfile.facebookId : null);
    }

    public static class Profile {
        @SerializedName("displayName")
        @Expose
        public String displayName;
        @SerializedName("imageUrl")
        @Expose
        public String imageUrl;
    }

    public static class LocalProfile extends Profile {
    }

    public static class FacebookProfile extends Profile {
        @SerializedName("facebookId")
        @Expose
        public String facebookId;
        @SerializedName("email")
        @Expose
        public String email;
    }

    public static class GoogleProfile extends Profile {
        @SerializedName("googleId")
        @Expose
        public String googleId;
        @SerializedName("email")
        @Expose
        public String email;
    }
}
