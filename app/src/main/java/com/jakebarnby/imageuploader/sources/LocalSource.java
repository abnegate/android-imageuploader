package com.jakebarnby.imageuploader.sources;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;

import com.jakebarnby.imageuploader.R;
import com.jakebarnby.imageuploader.models.Image;
import com.jakebarnby.imageuploader.models.Source;
import com.jakebarnby.imageuploader.ui.GridAdapter;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by jake on 1/17/17.
 */

public class LocalSource extends Source {

    public LocalSource(Context context, GridAdapter.AdapterInterface adapterInterface, int progressBarResId) {
        super(context, adapterInterface, progressBarResId);
    }

    @Override
    public void load() {
        if (!isAlbumsLoaded()) {
            loadAllImages();
        }
    }

    @Override
    protected void parseTokenResponse(JSONObject response) {
    }

    @Override
    public void loadAlbums() {
        final ProgressBar progressBar = (ProgressBar) ((Activity)getAdapterInterface()).findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        new LocalImageLoader(progressBar, getImages())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Environment.getExternalStorageDirectory());
    }

    @Override
    public void loadAllImages() {
        getProgressBar().setVisibility(View.VISIBLE);
        new LocalImageLoader(getProgressBar(), getImages())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Environment.getExternalStorageDirectory());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    /**
     * Loader for local images via AsyncTask
     */
    class LocalImageLoader extends AsyncTask<File, Void, ArrayList<Image>> {

        private final ArrayList<Image> mImages;
        private final ProgressBar mProgressBar;

        public LocalImageLoader(ProgressBar progressBar, ArrayList<Image> images) {
            mProgressBar = progressBar;
            mImages = images;
        }

        @Override
        protected ArrayList<Image> doInBackground(File... files) {
            return getImageDirectories(files[0]);
        }

        private ArrayList<Image> getImageDirectories(File dir) {
            File listFile[] = dir.listFiles();
            if (listFile != null && listFile.length > 0) {
                for (File file : listFile) {
                    if (file.isDirectory()) {
                        if (!file.getPath().contains("/Android/data") && !file.getPath().contains(".thumbnails")) {
                            getAlbumNames().add(file.getParent());
                            getImageDirectories(file);
                        }
                    } else {
                        if (file.getName().endsWith(".png")
                                || file.getName().endsWith(".jpg")
                                || file.getName().endsWith(".jpeg")) {

                            if (getAlbumNames().contains(file.getParent())) {
                                //TODO:Get local album thumbnail
                            }

                            mImages.add(new Image(Uri.fromFile(file)));
                        }
                    }
                }
            }
            return mImages;
        }

        @Override
        protected void onPostExecute(ArrayList<Image> imageList) {
            super.onPostExecute(imageList);
            setAlbumsLoaded(true);
            setLoggedIn(true);
            mProgressBar.setVisibility(View.INVISIBLE);
            getAdapterInterface().onDatasetChanged();

        }


    }

}
