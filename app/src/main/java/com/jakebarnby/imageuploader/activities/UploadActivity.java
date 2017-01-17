package com.jakebarnby.imageuploader.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
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

    private final TransferUtility mTransferUtility = S3Manager.Instance().getmTransferUtility();
    private SparseArray<Long> mUploadedProgress = new SparseArray<>();

    private ProgressBar mProgressBar;
    private long mTotalBytesToUpload = 0l;
    private long mTotalBytesUploaded = 0l;
    private int mTotalImageCount;
    private int mUploadCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        mProgressBar = (ProgressBar) findViewById(R.id.progressbar_upload);
        uploadSelectedImagesToS3();
    }

    /**
     *
     */
    private void uploadSelectedImagesToS3() {
        final AmazonS3 s3 = S3Manager.Instance().getS3();
        final ArrayList<Image> selectedImages = SelectedImagesManager.Instance().getmSelectedImages();

        mTotalImageCount =  selectedImages.size();
        mUploadCount = 0;

        final String bucketDir = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        for(Image image: selectedImages) {
            new AsyncTask<Image, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Image... params) {
                    String filename = bucketDir + "/"+ String.valueOf(params[0].getUri().hashCode()) + ".jpg";

                    if (!s3.doesObjectExist(Constants.AWS_BUCKET, filename)) {
                        TransferObserver observer = mTransferUtility.upload(
                                Constants.AWS_BUCKET,
                                filename,
                                new File(params[0].getUri().getPath())
                        );
                        observer.setTransferListener(UploadActivity.this);

                        mTotalBytesToUpload += observer.getBytesTotal();
                        mUploadedProgress.put(observer.getId(), 0l);
                        return true;
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean complete) {
                    super.onPostExecute(complete);
                    mUploadCount++;
                    if (mUploadCount == mTotalImageCount) {
                        mProgressBar.setMax((int) mTotalBytesToUpload);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image);
        }
    }

    @Override
    public void onStateChanged(int id, TransferState state) {
        if (mTotalBytesUploaded == mTotalBytesToUpload) {
            mTotalBytesUploaded = 0l;
            mTotalBytesToUpload = 0l;
            mTotalImageCount = 0;
            mUploadCount = 0;
            startActivity(new Intent(UploadActivity.this, DetailsActivity.class));
        }
    }

    @Override
    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
        long diff = bytesCurrent - mUploadedProgress.get(id);
        mUploadedProgress.put(id, mUploadedProgress.get(id)+diff);
        mTotalBytesUploaded += diff;

        mProgressBar.setProgress((int)mTotalBytesUploaded);
    }

    @Override
    public void onError(int id, Exception ex) {
        //No need to implement retry as AWS implements this automatically
        Log.e(getPackageName(), ex.getLocalizedMessage() + ex.getStackTrace());
    }
}
