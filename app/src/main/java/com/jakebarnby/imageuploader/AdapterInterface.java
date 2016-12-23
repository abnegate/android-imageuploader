package com.jakebarnby.imageuploader;

import android.content.Intent;

/**
 * Created by Jake on 12/15/2016.
 */
public interface AdapterInterface {
    public void scrollCartToEnd();
    public void notifyAdapters(int adapterPosition);
    public void startActivity(Intent intent);
}
