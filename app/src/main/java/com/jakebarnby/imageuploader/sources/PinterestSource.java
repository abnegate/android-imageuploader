package com.jakebarnby.imageuploader.sources;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.jakebarnby.imageuploader.models.Image;
import com.jakebarnby.imageuploader.models.Source;
import com.jakebarnby.imageuploader.models.SourceHTTPRequest;
import com.jakebarnby.imageuploader.models.SourceLoginDialog;
import com.jakebarnby.imageuploader.models.SourceSession;
import com.jakebarnby.imageuploader.models.SourceUser;
import com.jakebarnby.imageuploader.ui.AdapterInterface;
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
 * Created by Jake on 1/19/2017.
 */

public class PinterestSource extends Source {

    public PinterestSource(Context context, AdapterInterface adapterInterface) {
        super(context, adapterInterface);
        setSession(new SourceSession(context, Constants.SOURCE_PINTEREST));
    }

    @Override
    public void load() {
        if (!isLoggedIn()) {
            login(new Source.SourceAuthListener() {
                @Override
                public void onSuccess(SourceUser user) {
                    setLoggedIn(true);
                    loadAllImages(user.getAccessToken());
                }

                @Override
                public void onError(String error) {
                }

                @Override
                public void onCancel() {
                }
            });
        }
    }

    public void login(SourceAuthListener listener) {
        setListener(listener);

        String authUrl = "https://api.pinterest.com/oauth/?response_type=code&redirect_uri="+
                Constants.CALLBACK_URL+
                "&client_id="+
                Constants.PINTEREST_CLIENT_ID+
                "&scope=read_public,write_public&state=768uyFys";

        new SourceLoginDialog(getContext(), authUrl, Constants.CALLBACK_URL, new SourceLoginDialog.SourceLoginDialogListener() {
            @Override
            public void onSuccess(String code) {
                List<NameValuePair> params = new ArrayList<NameValuePair>(5);
                params.add(new BasicNameValuePair("grant_type", "authorization_code"));
                params.add(new BasicNameValuePair("client_id", Constants.PINTEREST_CLIENT_ID));
                params.add(new BasicNameValuePair("client_secret", Constants.PINTEREST_CLIENT_SECRET));
                params.add(new BasicNameValuePair("code", code));

                retreiveAccessToken(code, Constants.PINTEREST_API_BASE_URL, Constants.PINTEREST_ACCESS_TOKEN_URL, params);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(String error) {
            }
        }).show();
    }

    @Override
    protected void parseTokenResponse(JSONObject response) {
        try {
            String token = response.getString("access_token");
            getUser().setAccessToken(token);
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
        final ProgressDialog pDialog = ProgressDialog.show(getContext(), null, "Please wait...");
        new AsyncTask<URL, Void, Void>() {
            @Override
            protected Void doInBackground(URL... urls) {
                try {
                    List<NameValuePair> params = new ArrayList<NameValuePair>(1);
                    params.add(new BasicNameValuePair("fields", "image"));

                    SourceHTTPRequest request = new SourceHTTPRequest(Constants.PINTEREST_API_BASE_URL, token);
                    String response = request.requestGet("/me/pins", params);

                    if (!response.equals("")) {
                        JSONObject jsonObj = (JSONObject) new JSONTokener(response).nextValue();
                        JSONArray jsonData = jsonObj.getJSONArray("data");

                        for (int i = 0; i < jsonData.length(); i++) {
                            JSONObject image = jsonData.getJSONObject(i).getJSONObject("image").getJSONObject("original");
                            String link = image.getString("url");
                            getImages().add(new Image(Uri.parse(link)));
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
                getAdapterInterface().notifyAdaptersDatasetChanged();
                pDialog.hide();
            }

        }.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }
}
