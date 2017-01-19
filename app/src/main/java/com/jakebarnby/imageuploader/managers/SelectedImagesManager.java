package com.jakebarnby.imageuploader.managers;

import com.jakebarnby.imageuploader.models.Image;

import java.util.ArrayList;

/**
 * Created by Jake on 12/15/2016.
 */
public class SelectedImagesManager {

    private static volatile SelectedImagesManager sInstance;
    private static ArrayList<Image> mSelectedImages;

    public static SelectedImagesManager Instance()
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

    public ArrayList<Image> getSelectedImages()
    {
        return mSelectedImages;
    }

    public void removeImage(Image mImage) {
        mSelectedImages.remove(mImage);
    }
}
