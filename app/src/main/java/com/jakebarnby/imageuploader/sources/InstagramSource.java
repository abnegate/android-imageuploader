package com.jakebarnby.imageuploader.sources;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.jakebarnby.imageuploader.models.Image;
import com.jakebarnby.imageuploader.models.Source;
import com.jakebarnby.imageuploader.models.SourceHTTPRequest;
import com.jakebarnby.imageuploader.models.SourceLoginDialog;
import com.jakebarnby.imageuploader.models.SourceSession;
import com.jakebarnby.imageuploader.models.SourceUser;
import com.jakebarnby.imageuploader.ui.GridAdapter;
import com.jakebarnby.imageuploader.util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by jake on 1/18/17.
 */

public class InstagramSource extends Source {

    private SourceLoginDialog mDialog;

    public InstagramSource(Context context, GridAdapter.AdapterInterface adapterInterface, int progressBarResId, String apiBaseUrl) {
        super(context, adapterInterface, progressBarResId, apiBaseUrl);
        setSession(new SourceSession(context, Constants.SOURCE_INSTAGRAM));
    }

    @Override
    public void load() {
        if (!getSession().isActive()) {
            login(new Source.SourceAuthListener() {
                @Override
                public void onSuccess(SourceUser user) {
                    String token = user.getAccessToken();
                    setLoggedIn(true);
                    loadAllImages(token);
                }

                @Override
                public void onError(String error) {
                    Log.e("TOKEN_RETREIEVE", error);
                }

                @Override
                public void onCancel() {
                }
            });
        } else if (!isAlbumsLoaded()) {
            loadAllImages(getSession().getAccessToken());
        }
    }

    public void login(SourceAuthListener authListener) {
        setListener(authListener);

        String authURL = "https://api.instagram.com/oauth/authorize/?client_id=" +
                Constants.INSTAGRAM_CLIENT_ID +
                "&redirect_uri="+
                Constants.CALLBACK_URL+
                "&response_type=code";

        mDialog = new SourceLoginDialog(getContext(), authURL, Constants.CALLBACK_URL, getProgressBar(), new SourceLoginDialog.SourceLoginDialogListener() {
            @Override
            public void onSuccess(String code) {
                List<NameValuePair> params = new ArrayList<NameValuePair>(5);
                params.add(new BasicNameValuePair("client_id", Constants.INSTAGRAM_CLIENT_ID));
                params.add(new BasicNameValuePair("client_secret", Constants.INSTAGRAM_CLIENT_SECRET));
                params.add(new BasicNameValuePair("grant_type", "authorization_code"));
                params.add(new BasicNameValuePair("redirect_uri", Constants.CALLBACK_URL));
                params.add(new BasicNameValuePair("code", code));

                retreiveAccessToken(code, Constants.INSTAGRAM_API_BASE_URL, Constants.INSTAGRAM_ACCESS_TOKEN_URL, params);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(String error) {
                Log.e("INSTAGRAM_ERROR", error);
            }
        });
        mDialog.show();
    }

    @Override
    protected void parseTokenResponse(JSONObject response) {
        try {
            JSONObject jsonUser = response.getJSONObject("user");
            getUser().setAccessToken(response.getString("access_token"));
            getUser().setId(jsonUser.getString("id"));
            getUser().setUsername(jsonUser.getString("username"));
            getUser().setFullName(jsonUser.getString("full_name"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void loadAlbums() {

    }

    @Override
    public void loadAllImages() {

    }

    public void loadAllImages(final String token) {
        getProgressBar().setVisibility(View.VISIBLE);
        new AsyncTask<URL, Void, Void>() {
            @Override
            protected Void doInBackground(URL... urls) {
                long result = 0;

                try {
                    List<NameValuePair> params = new ArrayList<NameValuePair>(1);
                    params.add(new BasicNameValuePair("count", "20"));

                    SourceHTTPRequest request = new SourceHTTPRequest(Constants.INSTAGRAM_API_BASE_URL, token);
                    String response = request.requestGet("/users/self/media/recent", params);

                    if (!response.equals("")) {
                        JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                        JSONArray jsonData = jsonObj.getJSONArray("data");

                        int length = jsonData.length();

                        if (length > 0) {
                            for (int i = 0; i < length; i++) {
                                JSONObject jsonPhoto = jsonData.getJSONObject(i).getJSONObject("images").getJSONObject("standard_resolution");
                                getImages().add(new Image(Uri.parse(jsonPhoto.getString("url"))));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setAlbumsLoaded(true);
                getAdapterInterface().onDatasetChanged();
                getProgressBar().setVisibility(View.INVISIBLE);
            }

        }.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    /**
     * Reset session
     */
    public void resetSession() {
        getSession().reset();
        mDialog.clearCache();
    }

    /**
     * Get session
     *
     * @return Instagram session
     */
    public SourceSession getSession() {
        return super.getSession();
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return Constants.INSTAGRAM_API_BASE_URL + relativeUrl;
    }

}
