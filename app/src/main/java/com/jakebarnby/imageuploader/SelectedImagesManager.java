package com.jakebarnby.imageuploader;

import java.util.ArrayList;

/**
 * Created by Jake on 12/15/2016.
 */
public class SelectedImagesManager {

    private static volatile SelectedImagesManager sInstance;
    private static ArrayList<Image> mSelectedImages;

    public SelectedImagesManager(){}

    public static SelectedImagesManager getsInstance()
    {
        if (sInstance == null) {
            sInstance = new SelectedImagesManager();
            mSelectedImages = new ArrayList<Image>();
        }
        return sInstance;
    }

    public void addImage(Image image)
    {
        mSelectedImages.add(image);
    }

    public ArrayList<Image> getmSelectedImages()
    {
        return mSelectedImages;
    }

    public void removeImage(Image mImage) {
        mSelectedImages.remove(mImage);
    }
}
