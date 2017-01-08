package com.jakebarnby.imageuploader.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3;
import com.jakebarnby.imageuploader.util.Constants;
import com.jakebarnby.imageuploader.models.Image;
import com.jakebarnby.imageuploader.R;
import com.jakebarnby.imageuploader.managers.S3Manager;
import com.jakebarnby.imageuploader.managers.SelectedImagesManager;

import java.io.File;
import java.util.ArrayList;

public class UploadActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;

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
        final int[] imageCount =  {selectedImages.size()};
        final int[] uploadCount = {0};

        final String bucketDir = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        mProgressBar.setMax(imageCount[0]);

        for(Image image: selectedImages) {
            new AsyncTask<Image, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Image... params) {
                    String filename = bucketDir + "/"+ String.valueOf(params[0].getmUri().hashCode()) + ".jpg";

                    if (!s3.doesObjectExist(Constants.AWS_BUCKET, filename)) {
                        tranfserUtility.upload(
                                Constants.AWS_BUCKET,
                                filename,
                                new File(params[0].getmUri().getPath())
                        );
                        return true;
                    } else {
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean didUpload) {
                    super.onPostExecute(didUpload);
                    if (didUpload) {
                        uploadCount[0]++;
                        mProgressBar.setProgress(uploadCount[0]);
                    } else {
                        imageCount[0]--;
                        mProgressBar.setMax(imageCount[0]);
                    }
                    if (uploadCount[0] == imageCount[0]) {
                        startActivity(new Intent(UploadActivity.this, DetailsActivity.class));
                    }
                }
            }.execute(image);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
