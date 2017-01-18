package com.jakebarnby.imageuploader.ui;

/**
 * Created by Jake on 12/15/2016.
 */
public interface AdapterInterface {
    void scrollCartToEnd();
    void notifyAdapters(int adapterPosition);
    void notifyAdaptersDatasetChanged();
}
