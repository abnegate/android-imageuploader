package com.jakebarnby.imageuploader;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Jake on 12/15/2016.
 */
public class LocalImageLoader extends AsyncTask<File, Void, ArrayList<Image>> {

    private ArrayList<Image> mImages;
    private ProgressDialog mProgDialog;
    private GridAdapter mGridAdapter;

    public LocalImageLoader(ProgressDialog progDialog, ArrayList<Image> images, GridAdapter gridAdapter)
    {
        mImages = images;
        mProgDialog = progDialog;
        mGridAdapter = gridAdapter;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
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
                    if (!file.getPath().contains("/Android/data") && !file.getPath().contains(".thumbnails"))
                    {
                        getImageDirectories(file);
                    }
                }
                else
                {
                    if (file.getName().endsWith(".png")
                            || file.getName().endsWith(".jpg")
                            || file.getName().endsWith(".jpeg"))
                    {
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
        //mGridAdapter.notifyDataSetChanged();
        mProgDialog.dismiss();
    }


}
