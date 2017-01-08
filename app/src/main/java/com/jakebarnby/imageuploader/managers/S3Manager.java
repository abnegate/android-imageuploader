package com.jakebarnby.imageuploader.managers;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.jakebarnby.imageuploader.util.Constants;

/**
 * Created by Jake on 12/23/2016.
 */

public class S3Manager {

    private static volatile S3Manager sInstance;

    private AmazonS3 s3;
    private TransferUtility mTransferUtility;

    public static S3Manager Instance()
    {
        if (sInstance == null)
        {
            sInstance = new S3Manager();
        }
        return sInstance;
    }

    public void setupAWSCredentials(Context context)
    {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                Constants.AWS_IDENTITY_POOL_KEY,
                Regions.US_WEST_2
        );

        s3 = new AmazonS3Client(credentialsProvider);
        mTransferUtility = new TransferUtility(s3, context);
    }

    public AmazonS3 getS3() {
        return s3;
    }

    public TransferUtility getmTransferUtility() {
        return mTransferUtility;
    }


}
