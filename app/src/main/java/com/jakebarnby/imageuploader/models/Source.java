package com.jakebarnby.imageuploader.models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.jakebarnby.imageuploader.models.Image;
import com.jakebarnby.imageuploader.ui.AdapterInterface;

import java.util.ArrayList;

/**
 * Created by jake on 1/17/17.
 */

public abstract class Source {
    private Context mContext;
    private AdapterInterface mAdapterInterface;
    private ArrayList<Image> mImages = new ArrayList<>();
    private ArrayList<String> mAlbumNames = new ArrayList<>();
    private ArrayList<String> mAlbumIds = new ArrayList<>();
    private ArrayList<Uri> mAlbumThumnailUris = new ArrayList<>();
    private boolean mAlbumsLoaded = false;
    private boolean mLoggedIn = false;

    public abstract void login();
    public abstract void login(Activity activity, String[] permissions);
    public abstract void loadAlbums();
    public abstract void loadAllImages();

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
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

    public abstract void onActivityResult(int requestCode, int resultCode, Intent data);
}
