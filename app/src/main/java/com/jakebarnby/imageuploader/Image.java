package com.jakebarnby.imageuploader;

import android.graphics.Bitmap;
import android.net.Uri;

/**
 * Created by Jake on 12/15/2016.
 */
class Image {

    private Uri mUri;
    private boolean selected;

    public Image(Uri uri)
    {
        this.mUri = uri;
        this.selected = false;
    }

    public Uri getmUri()
    {
        return mUri;
    }

    public boolean isSelected()
    {
        return selected;
    }

    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
}
