package com.jakebarnby.imageuploader.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.ProgressBar;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.jakebarnby.imageuploader.util.Constants;
import com.jakebarnby.imageuploader.models.Image;
import com.jakebarnby.imageuploader.R;
import com.jakebarnby.imageuploader.managers.S3Manager;
import com.jakebarnby.imageuploader.managers.SelectedImagesManager;

import java.io.File;
import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity implements TransferListener {

    private ProgressBar mProgressBar;
    private int mTotalImageCount;
    private int mUploadCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_upload);
        uploadSelectedImagesToS3();
    }

    private void uploadSelectedImagesToS3() {
        final TransferUtility tranfserUtility = S3Manager.Instance().getmTransferUtility();
        final AmazonS3 s3 = S3Manager.Instance().getS3();

        final ArrayList<Image> selectedImages = SelectedImagesManager.Instance().getmSelectedImages();
        mTotalImageCount =  selectedImages.size();
        mUploadCount = 0;

        final String bucketDir = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        mProgressBar.setMax(mTotalImageCount);

        for(Image image: selectedImages) {
            new AsyncTask<Image, Void, Void>() {
                @Override
                protected Void doInBackground(Image... params) {
                    String filename = bucketDir + "/"+ String.valueOf(params[0].getmUri().hashCode()) + ".jpg";

                    if (!s3.doesObjectExist(Constants.AWS_BUCKET, filename)) {
                        TransferObserver observer = tranfserUtility.upload(
                                Constants.AWS_BUCKET,
                                filename,
                                new File(params[0].getmUri().getPath())
                        );
                        observer.setTransferListener(UploadActivity.this);
                    }
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onStateChanged(int id, TransferState state) {
        //TODO: Check for completion, dont bother with smooth progress

        if (state == TransferState.COMPLETED) {
            mUploadCount++;
            mProgressBar.setProgress(mUploadCount);
        }

        if (mUploadCount == mTotalImageCount) {
            startActivity(new Intent(UploadActivity.this, DetailsActivity.class));
        }
    }

    @Override
    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

    }

    @Override
    public void onError(int id, Exception ex) {

    }
}
