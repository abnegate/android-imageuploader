package com.jakebarnby.imageuploader.util;

/**
 * Created by Jake on 12/23/2016.
 */

public class Constants {

    public static final String CALLBACK_URL = "https://google.com";

    public static final String INSTAGRAM_CLIENT_ID = "a48b86aabc464b52867381ea6b16a101";
    public static final String INSTAGRAM_CLIENT_SECRET = "509e07c882ab49c5b8b5043e210fadcf";

    public static final String DROPBOX_CLIENT_ID = "rse09cxjnnn2yc1";
    public static final String DROPBOX_CLIENT_SECRET = "l2uo9343andrlfc";
    public static final String DROPBOX_AUTH_URL = "https://www.dropbox.com/1/oauth2/authorize?client_id=" +
            DROPBOX_CLIENT_ID +
            "&response_type=token&redirect_uri=" +
            CALLBACK_URL;
    public static final String DROPBOX_API_BASE_URL = "https://api.dropboxapi.com";

    public static final String INSTAGRAM_ACCESS_TOKEN_URL = "https://api.instagram.com/oauth/access_token";
    public static final String INSTAGRAM_API_BASE_URL = "https://api.instagram.com/v1";

    public static final String AWS_IDENTITY_POOL_KEY = "us-west-2:54b25798-bdce-4108-b40c-ffe1eec4c1ee";
    public static final String AWS_BUCKET = "jake-barnby-test";
    public static final int GRID_COLUMNS = 3;

    public static final String SOURCE_LOCAL = "LOCAL";
    public static final String SOURCE_FACEBOOK = "FACEBOOK";
    public static final String SOURCE_INSTAGRAM = "INSTAGRAM";
    public static final String SOURCE_DROPBOX = "DROPBOX";
}
