package com.jakebarnby.imageuploader.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jakebarnby.imageuploader.ui.AdapterInterface;
import com.jakebarnby.imageuploader.ui.GridAdapter;
import com.jakebarnby.imageuploader.models.Image;
import com.jakebarnby.imageuploader.util.Constants;
import com.jakebarnby.imageuploader.R;
import com.jakebarnby.imageuploader.managers.S3Manager;
import com.jakebarnby.imageuploader.managers.SelectedImagesManager;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterInterface {

    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 100;

    private RecyclerView mRecyclerViewImages;
    private RecyclerView mRecyclerViewCart;

    private ArrayList<Image> mLocalImages;
    private ArrayList<Image> mFacebookImages;
    private ArrayList<Image> mInstagramImages;

    private GridAdapter mLocalAdapter;
    private GridAdapter mFacebookAdapter;
    private GridAdapter mInstagramAdapter;

    private GridAdapter mCartAdapter;

    private TextView mCartCount;
    private ProgressDialog mProgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        S3Manager.Instance().setupAWSCredentials(getApplicationContext());

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.bottom_navigation);

        mCartCount = (TextView) findViewById(R.id.textview_cart_count);

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_local:
                                if (!item.isChecked()) {
                                    setRecyclerAdapter(mLocalAdapter);
                                }
                                break;
                            case R.id.action_facebook:
                                if (!item.isChecked()) {
                                    setRecyclerAdapter(mFacebookAdapter);
                                }
                                break;
                            case R.id.action_instagram:
                                //TODO: Load instagram images
                                break;
                        }
                        return false;
                    }
                }
        );

        checkPermssions();
    }

    private void checkPermssions() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                showPermissionRationaleDialog();
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_STORAGE);
            }
        }
        else
        {
            setupViews();
            loadLocalImages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    setupViews();
                    loadLocalImages();
                }
                else
                {
                    //TODO: Re-request permissions
                    checkPermssions();
                }
                break;
            }
        }
    }

    private void showPermissionRationaleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(R.string.dialog_message).setTitle(R.string.dialog_title);
        AlertDialog dialog = builder.create();
        dialog.show();
        checkPermssions();
    }

    private void setupViews()
    {
        mRecyclerViewImages = (RecyclerView) findViewById(R.id.recyclerview_images);
        mRecyclerViewCart = (RecyclerView) findViewById(R.id.recyclerview_cart);

        GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, Constants.GRID_COLUMNS);
        mRecyclerViewImages.setLayoutManager(mGridLayoutManager);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewCart.setLayoutManager(mLinearLayoutManager);

        mCartAdapter = new GridAdapter(SelectedImagesManager.Instance().getmSelectedImages());

        mFacebookImages = new ArrayList<Image>();
        mFacebookAdapter = new GridAdapter(mFacebookImages);

        mLocalImages = new ArrayList<Image>();
        mLocalAdapter = new GridAdapter(mLocalImages, this);

        mInstagramImages = new ArrayList<Image>();
        mInstagramAdapter = new GridAdapter(mInstagramImages, this);

        mRecyclerViewCart.setAdapter(mCartAdapter);

        //Force resuse viewholder on image swap
        DefaultItemAnimator animator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
                return true;
            }
        };

        mRecyclerViewImages.setItemAnimator(animator);

        FloatingActionButton mProceedButton = (FloatingActionButton) findViewById(R.id.button_proceed);
        mProceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, UploadActivity.class));
            }
        });
    }

    private void loadLocalImages()
    {
        mProgDialog = ProgressDialog.show(this, null,"Please wait...", true);
        new LocalImageLoader(mLocalImages)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Environment.getExternalStorageDirectory());
    }

    private void setRecyclerAdapter(GridAdapter adapter)
    {
        mRecyclerViewImages.setAdapter(adapter);
    }

    @Override
    public void scrollCartToEnd() {
        mRecyclerViewCart.smoothScrollToPosition(mLocalImages.size()-1);
    }

    @Override
    public void notifyAdapters(int adapterPosition) {
        mCartAdapter.notifyDataSetChanged();
        mLocalAdapter.notifyItemChanged(adapterPosition);
        mCartCount.setText(String.valueOf(SelectedImagesManager.Instance().getmSelectedImages().size()));
    }

    class LocalImageLoader extends AsyncTask<File, Void, ArrayList<Image>> {

        private final ArrayList<Image> mImages;

        public LocalImageLoader(ArrayList<Image> images)
        {
            mImages = images;
        }

        @Override
        protected ArrayList<Image> doInBackground(File... files) {
            return getImageDirectories(files[0]);
        }

        private ArrayList<Image> getImageDirectories(File dir) {
            File listFile[] = dir.listFiles();
            if (listFile != null && listFile.length > 0) {
                for (File file : listFile) {
                    if (file.isDirectory()) {
                        if (!file.getPath().contains("/Android/data")
                                && !file.getPath().contains(".thumbnails"))
                        {
                            getImageDirectories(file);
                        }
                    }
                    else
                    {
                        if (file.getName().endsWith(".png")
                                || file.getName().endsWith(".jpg")
                                || file.getName().endsWith(".jpeg"))
                        {
                            mImages.add(new Image(Uri.fromFile(file)));
                        }
                    }
                }
            }
            return mImages;
        }

        @Override
        protected void onPostExecute(ArrayList<Image> imageList) {
            super.onPostExecute(imageList);
            mProgDialog.dismiss();
            setRecyclerAdapter(mLocalAdapter);
        }


    }
}
