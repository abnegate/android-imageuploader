package com.jakebarnby.imageuploader.models;

import android.net.Uri;
import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Jake on 12/15/2016.
 */
public class Image {

    private Uri mUri;
    private boolean mSelected;
    private long size;

    public Image(Uri uri) {
        this.mUri = uri;
        this.mSelected = false;
    }

    public Uri getUri() {
        return mUri;
    }

    public boolean isSelected()
    {
        return mSelected;
    }

    public void setSelected(boolean selected)
    {
        this.mSelected = selected;
    }
}
