package com.jakebarnby.imageuploader.sources;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.jakebarnby.imageuploader.models.Image;
import com.jakebarnby.imageuploader.models.Source;
import com.jakebarnby.imageuploader.ui.AdapterInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by jake on 1/17/17.
 */

public class FacebookSource extends Source implements FacebookCallback<LoginResult> {

    private final CallbackManager mCallbackManager;

    public FacebookSource(Context context, AdapterInterface adapterInterface) {
        super(context, adapterInterface);
        FacebookSdk.sdkInitialize(getContext());
        AppEventsLogger.activateApp(getContext());
        mCallbackManager = CallbackManager.Factory.create();
        setAdapterInterface(adapterInterface);

        LoginManager.getInstance().registerCallback(mCallbackManager,this);
    }

    /**
     * Starts the facebook login flow
     * @param activity      The calling activity
     * @param permissions   The facebook permissions to grant
     */
    public void login(Activity activity, String[] permissions) {
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList(permissions));
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSuccess(LoginResult loginResult) {
        setLoggedIn(true);
        loadAllImages();
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onError(FacebookException error) {

    }

    @Override
    public void loadAlbums() {
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "me/albums",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response != null) {
                            try {
                                JSONObject data = response.getJSONObject();
                                if (data.has("data")) {
                                    JSONArray albums = data.getJSONArray("data");
                                    int albumCount = albums.length();
                                    for (int i = 0; i < albums.length(); i++) {
                                        JSONObject album = albums.getJSONObject(i);
                                        getAlbumNames().add(album.getString("name"));
                                        if (i < albumCount-1) {
                                            getAlbumThumbnailUrl(album.getString("id"), false);
                                        } else {
                                            getAlbumThumbnailUrl(album.getString("id"), true);
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).executeAsync();
    }

    @Override
    public void loadAllImages() {
        final ProgressDialog progressDialog = ProgressDialog.show(getContext(), null,"Please wait...", true);

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "me/photos?fields=images&limit=500",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        if (response != null) {
                            JSONObject obj = response.getJSONObject();
                            if (obj.has("data")) {
                                try {
                                    JSONArray photos = obj.getJSONArray("data");
                                    for (int i = 0; i < photos.length(); i++) {
                                        JSONObject images = photos.getJSONObject(i);
                                        JSONArray image = images.getJSONArray("images");
                                        JSONObject hiRes = image.getJSONObject(3);
                                        String url = hiRes.getString("source");
                                        getImages().add(new Image((Uri.parse(url))));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                progressDialog.hide();
                                getAdapterInterface().notifyAdaptersDatasetChanged();
                            }
                        }
                    }
                }
        ).executeAsync();
    }

    /**
     * Get the thumbnail for the given albumId
     * @param albumId           The ID of the album to get the thumbnail for
     * @param isFinalAlbum      Is this the users last album
     */
    private void getAlbumThumbnailUrl(final String albumId, final boolean isFinalAlbum)
    {
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                albumId+"?fields=cover_photo",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response != null) {
                            try {
                                JSONObject joMain = response.getJSONObject();
                                if (joMain.has("cover_photo")) {
                                    String id = joMain.getJSONObject("cover_photo").getString("id");
                                    new GraphRequest(
                                            AccessToken.getCurrentAccessToken(),
                                            id+"?fields=link",
                                            null,
                                            HttpMethod.GET,
                                            new GraphRequest.Callback() {
                                                @Override
                                                public void onCompleted(GraphResponse response) {
                                                    if (response != null) {
                                                        try {
                                                            JSONObject joMain = response.getJSONObject();
                                                            if (joMain.has("link")) {
                                                                getAlbumThumnailUris().add(Uri.parse(joMain.getString("link")));
                                                            }
                                                            else {
                                                                Log.d("TumbnailRequest", "joMain does not have link field");
                                                            }

                                                        } catch (JSONException e) {

                                                        }

                                                        if (isFinalAlbum) {
                                                            //TODO: Loaded
                                                            setAlbumsLoaded(true);
                                                        }
                                                    }
                                                }
                                            }).executeAsync();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        ).executeAsync();
    }
}
