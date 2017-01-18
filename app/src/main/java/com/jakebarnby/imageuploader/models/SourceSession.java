package com.jakebarnby.imageuploader.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * Created by jake on 1/18/17.
 */

public class SourceSession {
    private Context mContext;
    private SharedPreferences mSharedPref;

    private String mShared = null;
    private final String mUserId = "userid";
    private final String mUsername = "username";
    private final String mFullname = "fullname";
    private final String mProfilePic = "profilpic";
    private final String mAccessToken = "access_token";

    public SourceSession(Context context, String sourceName) {
        mContext = context;
        mShared = sourceName+"_Preferences";
        mSharedPref = context.getSharedPreferences(mShared, Context.MODE_PRIVATE);
    }

    /**
     * Store user data for this
     * @param user  The user to store data in this session
     */
    public void store(SourceUser user) {
        SharedPreferences.Editor editor = mSharedPref.edit();

        editor.putString(mAccessToken, user.getAccessToken());
        editor.putString(mUserId, user.getId());
        editor.putString(mUsername, user.getUsername());
        editor.putString(mFullname, user.getFullName());
        editor.putString(mProfilePic, user.getProfilPicture());

        editor.commit();
    }

    /**
     * Reset user data
     */
    public void reset() {
        SharedPreferences.Editor editor = mSharedPref.edit();

        editor.putString(mAccessToken, "");
        editor.putString(mUserId, "");
        editor.putString(mUsername, "");
        editor.putString(mFullname, "");
        editor.putString(mProfilePic, "");
        editor.apply();

        CookieSyncManager.createInstance(mContext);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    /**
     * Get the user of this session
     * @return  The <code>SourceUser</code> of this session
     */
    public SourceUser getUser() {
        if (mSharedPref.getString(mAccessToken, "").equals("")) {
            return null;
        }
        SourceUser user 	= new SourceUser();

        user.setId(mSharedPref.getString(mUserId, ""));
        user.setUsername(mSharedPref.getString(mUsername, ""));
        user.setFullName(mSharedPref.getString(mFullname, ""));
        user.setProfilPicture(mSharedPref.getString(mProfilePic, ""));
        user.setAccessToken(mSharedPref.getString(mAccessToken, ""));

        return user;
    }

    /**
     *  Get the access token for this session
     * @return  The access token for this session
     */
    public String getAccessToken() {
        return mSharedPref.getString(mAccessToken, "");
    }

    /**
     * Check if there is an active session
     * @return Whether there is an active session
     */
    public boolean isActive() {
        return !mSharedPref.getString(mAccessToken, "").equals("");
    }
}
