package com.jakebarnby.imageuploader.models;

import com.jakebarnby.imageuploader.ui.GridAdapter;

import java.util.ArrayList;

/**
 * Created by Jake on 12/20/2016.
 */

class Source {

    private ArrayList<Image> mImages;
    private GridAdapter mAdapter;

    public Source(GridAdapter adapter)
    {
        mAdapter = adapter;
        mImages = new ArrayList<>();
    }

    public Source(ArrayList<Image> images, GridAdapter adapter)
    {
        mAdapter = adapter;
        mImages = images;
    }

    public ArrayList<Image> getmImages() {
        return mImages;
    }

    public void setmImages(ArrayList<Image> mImages) {
        this.mImages = mImages;
    }

    public void addImage(Image image)
    {
        mImages.add(image);
    }

    public void removeImage(Image image)
    {
        mImages.remove(image);
    }

    public GridAdapter getmAdapter() {
        return mAdapter;
    }

    public void setmAdapter(GridAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }
}
