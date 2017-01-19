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
import com.amazonaws.services.s3.AmazonS3;
import com.jakebarnby.imageuploader.models.Source;
import com.jakebarnby.imageuploader.util.Constants;
import com.jakebarnby.imageuploader.models.Image;
import com.jakebarnby.imageuploader.R;
import com.jakebarnby.imageuploader.managers.S3Manager;
import com.jakebarnby.imageuploader.managers.SelectedImagesManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity implements TransferListener {

    private ProgressBar mProgressBar;
    private int mTotalImageCount;
    private int mUploadCount = 0;
    private boolean mFilesDownloaded = false;

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
        final ArrayList<Image> selectedImages = SelectedImagesManager.Instance().getSelectedImages();

        mTotalImageCount =  selectedImages.size();
        mUploadCount = 0;
        mProgressBar.setMax(mTotalImageCount);

        final String bucketDir = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        for(Image image: selectedImages) {
            new AsyncTask<Image, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Image... params) {

                    Image curImage = params[0];
                    File toUpload;
                    String filename = bucketDir + "/"+ String.valueOf(curImage.getUri().hashCode()) + ".jpg";

                    if (curImage.getUri().toString().startsWith("http")) {
                        toUpload = new File(getFilesDir().getAbsolutePath()+"/download/");
                        toUpload.mkdirs();
                        toUpload = new File(getFilesDir().getAbsolutePath()+"/download/"+curImage.getUri().hashCode());
                        try {
                            toUpload.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Source.downloadFile(curImage.getUri().toString(), toUpload);
                        mFilesDownloaded = true;
                    } else {
                        toUpload = new File(curImage.getUri().getPath());
                    }

                    if (!s3.doesObjectExist(Constants.AWS_BUCKET, filename)) {
                        TransferObserver observer = S3Manager.Instance().getTransferUtility(UploadActivity.this).upload(
                                Constants.AWS_BUCKET,
                                filename,
                                toUpload
                        );
                        observer.setTransferListener(UploadActivity.this);
                        return true;
                    } else {
                        mTotalImageCount--;
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean complete) {
                    super.onPostExecute(complete);
                    if (!complete) {
                        mProgressBar.setMax(mTotalImageCount);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image);
        }
    }

    @Override
    public void onStateChanged(int id, TransferState state) {
        if (state == TransferState.COMPLETED) {
            mUploadCount++;
            mProgressBar.setProgress(mUploadCount);
        }

        if (mUploadCount == mTotalImageCount) {
            mTotalImageCount = 0;
            mUploadCount = 0;

            if (mFilesDownloaded) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        File[] temp = new File(getFilesDir().getAbsolutePath()+"/download").listFiles();
                        for (File file : temp) {
                            file.delete();
                        }
                        mFilesDownloaded = false;
                    }
                }.start();
            }
            startActivity(new Intent(UploadActivity.this, DetailsActivity.class));
        }
    }

    @Override
    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

    }

    @Override
    public void onError(int id, Exception ex) {
        Log.e(getPackageName(), ex.getLocalizedMessage() + ex.getStackTrace());
    }
}
