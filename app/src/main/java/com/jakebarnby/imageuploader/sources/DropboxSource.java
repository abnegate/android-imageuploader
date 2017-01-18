package com.jakebarnby.imageuploader.sources;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.GetTemporaryLinkResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchMatch;
import com.dropbox.core.v2.files.SearchResult;
import com.jakebarnby.imageuploader.models.Image;
import com.jakebarnby.imageuploader.models.Source;
import com.jakebarnby.imageuploader.models.SourceLoginDialog;
import com.jakebarnby.imageuploader.ui.AdapterInterface;
import com.jakebarnby.imageuploader.util.Constants;

import java.util.List;

/**
 * Created by Jake on 1/19/2017.
 */

public class DropboxSource extends Source {

    private DbxClientV2 mClient;

    public DropboxSource(Context context, AdapterInterface adapterInterface, String apiBaseUrl) {
        super(context, adapterInterface, apiBaseUrl);
    }

    public void login()  {
        new SourceLoginDialog(getContext(), Constants.DROPBOX_AUTH_URL, Constants.CALLBACK_URL, new SourceLoginDialog.SourceLoginDialogListener() {
            @Override
            public void onSuccess(String code) {
                DbxRequestConfig config = new DbxRequestConfig("ImageUploaderAndroid/0.3");
                mClient = new DbxClientV2(config, code);
                setLoggedIn(true);
                loadAllImages();
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(String error) {
                Log.e("DROPBOX_CODE", error);
            }
        }).show();
    }

    @Override
    public void loadAlbums() {

    }

    @Override
    public void loadAllImages() {
        final ProgressDialog pDialog = ProgressDialog.show(getContext(), null, "Please wait...");
        new AsyncTask<Object, Object, Void>() {
            @Override
            protected Void doInBackground(Object... params) {
                try {
                    SearchResult result = mClient.files().search("", "*.jpg");
                    List<SearchMatch> matches = result.getMatches();
                    for (SearchMatch match : matches) {
                        Metadata data = match.getMetadata();
                        String path = data.getPathLower();
                        GetTemporaryLinkResult resultLink = mClient.files().getTemporaryLink(path);
                        String link = resultLink.getLink();
                        getImages().add(new Image(Uri.parse(link)));
                    }
                }
                catch(DbxException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                pDialog.dismiss();
                getAdapterInterface().notifyAdaptersDatasetChanged();
                setAlbumsLoaded(true);
            }
        }.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
}
