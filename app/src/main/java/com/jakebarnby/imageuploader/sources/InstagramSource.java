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
    private static final String BASE_URL = "https://api.instagram.com/";
    private InstagramAuthListener mListener;
    private SourceSession mSession;
    private SourceLoginDialog mDialog;

    public InstagramSource(Context context, AdapterInterface adapterInterface, String apiBaseUrl) {
        super(context, adapterInterface, apiBaseUrl);
        mSession = new SourceSession(context, Constants.SOURCE_INSTAGRAM);
    }

    public void login(InstagramAuthListener authListener) {
        mListener = authListener;
        String authURL = "oauth/authorize/?client_id=" +
                Constants.INSTAGRAM_CLIENT_ID +
                "&redirect_uri="+
                Constants.CALLBACK_URL+
                "&response_type=code";

        mDialog = new SourceLoginDialog(getContext(), BASE_URL+authURL, Constants.CALLBACK_URL, new SourceLoginDialog.SourceLoginDialogListener() {
            @Override
            public void onSuccess(String code) {
                Log.d("INSTAGRAM_REQUEST", "Login success! Code: " + code);
                retreiveAccessToken(code);
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
    public void loadAlbums() {

    }

    @Override
    public void loadAllImages() {

    }

    public void loadAllImages(String token) {
        final ProgressDialog pDialog = ProgressDialog.show(getContext(), null, "Please wait...");
        new AsyncTask<URL, Void, Void>() {
            @Override
            protected Void doInBackground(URL... urls) {
                long result = 0;

                try {
                    List<NameValuePair> params = new ArrayList<NameValuePair>(1);
                    params.add(new BasicNameValuePair("count", "20"));

                    SourceHTTPRequest request = new SourceHTTPRequest(Constants.INSTAGRAM_API_BASE_URL, mSession.getAccessToken());
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
                getAdapterInterface().notifyAdaptersDatasetChanged();
                pDialog.hide();
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
        mSession.reset();
        mDialog.clearCache();
    }

    /**
     * Get session
     *
     * @return Instagram session
     */
    public SourceSession getSession() {
        return mSession;
    }

    /**
     * Retreive access token.
     *
     * @param code
     */
    private void retreiveAccessToken(String code) {
        new AccessTokenTask(code).execute();
    }

    public class AccessTokenTask extends AsyncTask<URL, Integer, Long> {
        ProgressDialog progressDlg;
        SourceUser user;
        String code;

        public AccessTokenTask(String code) {
            this.code = code;
            progressDlg = new ProgressDialog(getContext());
            progressDlg.setMessage("Please wait...");
        }

        protected void onCancelled() {
            progressDlg.cancel();
        }

        protected void onPreExecute() {
            progressDlg.show();
        }

        protected Long doInBackground(URL... urls) {
            long result = 0;

            try {
                List<NameValuePair> params = new ArrayList<NameValuePair>(5);

                params.add(new BasicNameValuePair("client_id", Constants.INSTAGRAM_CLIENT_ID));
                params.add(new BasicNameValuePair("client_secret", Constants.INSTAGRAM_CLIENT_SECRET));
                params.add(new BasicNameValuePair("grant_type", "authorization_code"));
                params.add(new BasicNameValuePair("redirect_uri", Constants.CALLBACK_URL));
                params.add(new BasicNameValuePair("code", code));

                SourceHTTPRequest request = new SourceHTTPRequest(Constants.INSTAGRAM_API_BASE_URL);
                String response	= request.post(Constants.INSTAGRAM_ACCESS_TOKEN_URL, params);

                if (!response.equals("")) {
                    JSONObject jsonObj 	= (JSONObject) new JSONTokener(response).nextValue();
                    JSONObject jsonUser	= jsonObj.getJSONObject("user");

                    user = new SourceUser();

                    user.setAccessToken(jsonObj.getString("access_token"));
                    user.setId(jsonUser.getString("id"));
                    user.setUsername(jsonUser.getString("username"));
                    user.setFullName(jsonUser.getString("full_name"));
                    user.setProfilPicture(jsonUser.getString("profile_picture"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            progressDlg.dismiss();

            if (user != null) {
                mSession.store(user);

                mListener.onSuccess(user);
            } else {
                mListener.onError("Failed to get access token");
            }
        }
    }

    public interface InstagramAuthListener {
        public abstract void onSuccess(SourceUser user);
        public abstract void onError(String error);
        public abstract void onCancel();
    }

    private String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

}
