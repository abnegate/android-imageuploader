package com.jakebarnby.imageuploader.models;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.jakebarnby.imageuploader.models.Image;
import com.jakebarnby.imageuploader.ui.AdapterInterface;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
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
    private boolean mLoggedIn;

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

    public AdapterInterface getAdapterInterface() {
        return mAdapterInterface;
    }

    public void setAdapterInterface(AdapterInterface mAdapterInterface) {
        this.mAdapterInterface = mAdapterInterface;
    }



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
}
