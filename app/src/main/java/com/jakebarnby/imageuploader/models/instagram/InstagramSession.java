package com.jakebarnby.imageuploader.models.instagram;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * Created by jake on 1/18/17.
 */

public class InstagramSession {
    private Context mContext;
    private SharedPreferences mSharedPref;

    private static final String SHARED = "Instagram_Preferences";
    private static final String USERID	= "userid";
    private static final String USERNAME = "username";
    private static final String FULLNAME = "fullname";
    private static final String PROFILPIC = "profilpic";
    private static final String ACCESS_TOKEN = "access_token";

    public InstagramSession(Context context) {
        mContext = context;
        mSharedPref = context.getSharedPreferences(SHARED, Context.MODE_PRIVATE);
    }

    /**
     * Save user data
     *
     * @param user User data
     */
    public void store(InstagramUser user) {
        SharedPreferences.Editor editor = mSharedPref.edit();

        editor.putString(ACCESS_TOKEN, user.getAccessToken());
        editor.putString(USERID, user.getId());
        editor.putString(USERNAME, user.getUsername());
        editor.putString(FULLNAME, user.getFullName());
        editor.putString(PROFILPIC, user.getProfilPicture());

        editor.commit();
    }

    /**
     * Reset user data
     */
    public void reset() {
        SharedPreferences.Editor editor = mSharedPref.edit();

        editor.putString(ACCESS_TOKEN, "");
        editor.putString(USERID, "");
        editor.putString(USERNAME, "");
        editor.putString(FULLNAME, "");
        editor.putString(PROFILPIC, "");

        editor.commit();

        CookieSyncManager.createInstance(mContext);

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
    }

    /**
     * Get user data
     *
     * @return User data
     */
    public InstagramUser getUser() {
        if (mSharedPref.getString(ACCESS_TOKEN, "").equals("")) {
            return null;
        }
        InstagramUser user 	= new InstagramUser();

        user.setId(mSharedPref.getString(USERID, ""));
        user.setUsername(mSharedPref.getString(USERNAME, ""));
        user.setFullName(mSharedPref.getString(FULLNAME, ""));
        user.setProfilPicture(mSharedPref.getString(PROFILPIC, ""));
        user.setAccessToken(mSharedPref.getString(ACCESS_TOKEN, ""));

        return user;
    }

    /**
     * Get access token
     *
     * @return Access token
     */
    public String getAccessToken() {
        return mSharedPref.getString(ACCESS_TOKEN, "");
    }

    /**
     * Check if ther is an active session.
     *
     * @return true if active and vice versa
     */
    public boolean isActive() {
        return (mSharedPref.getString(ACCESS_TOKEN, "").equals("")) ? false : true;
    }
}
