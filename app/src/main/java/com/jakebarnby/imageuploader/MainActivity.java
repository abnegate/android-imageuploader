package com.jakebarnby.imageuploader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
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

        GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, 3);
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
        ProgressDialog progDialog = ProgressDialog.show(this, null,"Please wait...", true);
        new LocalImageLoader(progDialog, mLocalImages)
                .execute(Environment.getExternalStorageDirectory());
        setRecyclerAdapter(mLocalAdapter);
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
}
