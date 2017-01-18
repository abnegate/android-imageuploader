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

    public Image(Uri uri) {
        this.mUri = uri;
        this.mSelected = false;
    }

    /** Get the Uri for this image
     * @return     The Uri for this image
     */
    public Uri getUri() {
        return mUri;
    }

    /**
     * Get whether this image is currently selected
     * @return  Whether this image is currently selected
     */
    public boolean isSelected()
    {
        return mSelected;
    }

    /**
     * Set whether this image is currently selected
     * @param selected  Whether this image is currently selected
     */
    public void setSelected(boolean selected)
    {
        this.mSelected = selected;
    }
}
