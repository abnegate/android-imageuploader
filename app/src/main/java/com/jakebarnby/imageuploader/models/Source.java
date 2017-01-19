package com.jakebarnby.imageuploader.models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.jakebarnby.imageuploader.R;
import com.jakebarnby.imageuploader.ui.GridAdapter;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;

/**
 * Created by jake on 1/17/17.
 */

public abstract class Source {
    private Context mContext;
    private GridAdapter mAdapter;
    private SourceSession mSession;
    private SourceUser mUser;
    private SourceAuthListener mListener;
    private GridAdapter.AdapterInterface mAdapterInterface;
    private ArrayList<Image> mImages = new ArrayList<>();
    private ArrayList<String> mAlbumNames = new ArrayList<>();
    private ArrayList<String> mAlbumIds = new ArrayList<>();
    private ArrayList<Uri> mAlbumThumnailUris = new ArrayList<>();
    private final ProgressBar mProgressBar;
    private boolean mAlbumsLoaded = false;
    private boolean mLoggedIn;

    public abstract void load();
    public abstract void loadAlbums();
    public abstract void loadAllImages();

    public Source(Context context, GridAdapter.AdapterInterface adapterInterface, int progressBarResId) {
        mContext = context;
        mAdapterInterface = adapterInterface;
        mAdapter = new GridAdapter(mImages, adapterInterface);
        mUser = new SourceUser();
        mProgressBar  = (ProgressBar) ((Activity)adapterInterface).findViewById(progressBarResId);
    }

    public Source(Context context, GridAdapter.AdapterInterface adapterInterface, int progressBarResId, String apiBaseUrl) {
        mContext = context;
        mAdapterInterface = adapterInterface;
        mAdapter = new GridAdapter(mImages, adapterInterface);
        mUser = new SourceUser();
        mProgressBar  = (ProgressBar) ((Activity)adapterInterface).findViewById(progressBarResId);
    }

    public Context getContext() {
        return mContext;
    }
    public void setContext(Context mContext) {
        this.mContext = mContext;
    }
    public GridAdapter getAdapter() {
        return mAdapter;
    }
    public void setAdapter(GridAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }
    public SourceSession getSession() {
        return mSession;
    }
    public void setSession(SourceSession mSession) {
        this.mSession = mSession;
    }
    public SourceAuthListener getListener() { return mListener; }
    public SourceUser getUser() {
        return mUser;
    }
    public void setListener(SourceAuthListener mListener) {
        this.mListener = mListener;
    }
    public ArrayList<Image> getImages() {
        return mImages;
    }
    public ArrayList<String> getAlbumNames() {
        return mAlbumNames;
    }
    public ArrayList<String> getmAlbumIds() {
        return mAlbumIds;
    }
    public ArrayList<Uri> getAlbumThumnailUris() {
        return mAlbumThumnailUris;
    }
    public ProgressBar getProgressBar() { return mProgressBar; }
    public boolean isAlbumsLoaded() {
        return mAlbumsLoaded;
    }
    public void setAlbumsLoaded(boolean mAlbumsLoaded) {
        this.mAlbumsLoaded = mAlbumsLoaded;
    }
    public boolean isLoggedIn() {
        return mLoggedIn;
    }
    public void setLoggedIn(boolean mLoggedIn) {
        this.mLoggedIn = mLoggedIn;
    }
    public GridAdapter.AdapterInterface getAdapterInterface() {
        return mAdapterInterface;
    }
    public void setAdapterInterface(GridAdapter.AdapterInterface mAdapterInterface) {
        this.mAdapterInterface = mAdapterInterface;
    }

    protected abstract void parseTokenResponse(JSONObject response);

    /**
     * Retreive access token.
     *
     * @param code
     */
    protected void retreiveAccessToken(String code, String apiBaseUrl, String authUrl, List<NameValuePair> params) {
        new AccessTokenTask(code, apiBaseUrl, authUrl, params).execute();
    }

    public class AccessTokenTask extends AsyncTask<URL, Integer, Long> {
        List<NameValuePair> params;
        String apiBaseUrl;
        String authUrl;
        ProgressBar progressBar;
        String code;

        public AccessTokenTask(String code, String apiBaseUrl, String authUrl, List<NameValuePair> params) {
            this.code = code;
            this.apiBaseUrl = apiBaseUrl;
            this.authUrl = authUrl;
            this.params = params;
            progressBar = (ProgressBar) ((Activity) getAdapterInterface()).findViewById(R.id.progress_bar);
        }

        protected void onCancelled() {
            progressBar.setVisibility(View.INVISIBLE);
        }

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Long doInBackground(URL... urls) {
            long result = 0;

            try {
                SourceHTTPRequest request = new SourceHTTPRequest(apiBaseUrl);
                String response	= request.post(authUrl, params);

                if (!response.equals("")) {
                    JSONObject jsonObj 	= (JSONObject) new JSONTokener(response).nextValue();
                    parseTokenResponse(jsonObj);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Long result) {
            progressBar.setVisibility(View.INVISIBLE);

            if (getUser() != null) {
                mSession.store(getUser());
                mListener.onSuccess(getUser());
            } else {
                mListener.onError("Failed to get access token");
            }
        }
    }

    /**
     * Download and store a file
     * @param url           The URL path to the file
     * @param outputFile    The file to save the output to
     */
    public static void downloadFile(String url, File outputFile) {
        try {
            URL u = new URL(url);
            URLConnection conn = u.openConnection();
            int contentLength = conn.getContentLength();

            DataInputStream stream = new DataInputStream(u.openStream());

            byte[] buffer = new byte[contentLength];
            stream.readFully(buffer);
            stream.close();

            DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
            fos.write(buffer);
            fos.flush();
            fos.close();

            Log.d("DOWNLOAD_EVENT", "Download success: " + outputFile.getAbsolutePath());
        } catch(FileNotFoundException e) {
            return; // swallow a 404
        } catch (IOException e) {
            return; // swallow a 404
        }
    }

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);

    public interface SourceAuthListener {
        public abstract void onSuccess(SourceUser user);
        public abstract void onError(String error);
        public abstract void onCancel();
    }
}
