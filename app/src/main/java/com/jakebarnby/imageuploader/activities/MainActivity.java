package com.jakebarnby.imageuploader.activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.jakebarnby.imageuploader.sources.DropboxSource;
import com.jakebarnby.imageuploader.sources.InstagramSource;
import com.jakebarnby.imageuploader.models.SourceUser;
import com.jakebarnby.imageuploader.ui.AdapterInterface;
import com.jakebarnby.imageuploader.ui.GridAdapter;
import com.jakebarnby.imageuploader.util.Constants;
import com.jakebarnby.imageuploader.R;
import com.jakebarnby.imageuploader.managers.S3Manager;
import com.jakebarnby.imageuploader.managers.SelectedImagesManager;
import com.jakebarnby.imageuploader.sources.FacebookSource;
import com.jakebarnby.imageuploader.sources.LocalSource;
import com.jakebarnby.imageuploader.models.Source;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements AdapterInterface, BottomNavigationView.OnNavigationItemSelectedListener {

    private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 100;

    private RecyclerView mRecyclerViewImages;
    private RecyclerView mRecyclerViewCart;

    private HashMap<String, Source> mSources = new HashMap<>();

    private GridAdapter mCartAdapter;

    private TextView mCartCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSources.put(Constants.SOURCE_LOCAL, new LocalSource(this, this));
        mSources.put(Constants.SOURCE_FACEBOOK, new FacebookSource(this, this));
        mSources.put((Constants.SOURCE_INSTAGRAM), new InstagramSource(this, this, Constants.INSTAGRAM_API_BASE_URL));
        mSources.put((Constants.SOURCE_DROPBOX), new DropboxSource(this, this, Constants.DROPBOX_API_BASE_URL));

        S3Manager.Instance().setupAWSCredentials(getApplicationContext());

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        mCartCount = (TextView) findViewById(R.id.textview_cart_count);
        checkPermssions();
    }

    private void checkPermssions() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionRationaleDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_STORAGE);
            }
        } else {
            setupViews();
            loadLocalImages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupViews();
                    loadLocalImages();
                } else {
                    checkPermssions();
                }
                break;
            }
        }
    }

    private void showPermissionRationaleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(R.string.dialog_message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_STORAGE);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setupViews()
    {
        mRecyclerViewImages = (RecyclerView) findViewById(R.id.recyclerview_images);
        mRecyclerViewCart = (RecyclerView) findViewById(R.id.recyclerview_cart);

        GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, Constants.GRID_COLUMNS);
        mRecyclerViewImages.setLayoutManager(mGridLayoutManager);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewCart.setLayoutManager(mLinearLayoutManager);

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
        Source local = mSources.get(Constants.SOURCE_LOCAL);

        if (!local.isAlbumsLoaded()) {
            local.loadAllImages();
        }

        setRecyclerAdapter(local.getAdapter());
    }

    private void loadFacebook() {
        Source facebook = mSources.get(Constants.SOURCE_FACEBOOK);

        if (!facebook.isLoggedIn()) {
            ((FacebookSource)facebook).login(this, new String[]{"user_photos"});
        }

        setRecyclerAdapter(facebook.getAdapter());
    }

    private void loadInstagram() {
        final InstagramSource instagram = (InstagramSource) mSources.get(Constants.SOURCE_INSTAGRAM);

        if (!instagram.getSession().isActive()) {
            instagram.login(new InstagramSource.InstagramAuthListener() {
                @Override
                public void onSuccess(SourceUser user) {
                    String token = user.getAccessToken();
                    instagram.setLoggedIn(true);
                    instagram.loadAllImages(token);
                }

                @Override
                public void onError(String error) {
                    Log.e("TOKEN_RETREIEVE", error);
                }

                @Override
                public void onCancel() {
                }
            });
        } else if (!instagram.isAlbumsLoaded()) {
            instagram.loadAllImages(instagram.getSession().getAccessToken());
        }
        setRecyclerAdapter(instagram.getAdapter());
    }

    private void loadDropbox() {
        DropboxSource dropbox = (DropboxSource) mSources.get(Constants.SOURCE_DROPBOX);

        if (!dropbox.isLoggedIn()) {
            dropbox.login();
        }
        setRecyclerAdapter(dropbox.getAdapter());
    }

    private void setRecyclerAdapter(GridAdapter adapter) {
        mRecyclerViewImages.swapAdapter(adapter, false);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSources.get(Constants.SOURCE_FACEBOOK).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void scrollCartToEnd() {
        mRecyclerViewCart.smoothScrollToPosition(mRecyclerViewCart.getAdapter().getItemCount());
    }

    @Override
    public void notifyAdapters(int adapterPosition) {
        mCartAdapter.notifyDataSetChanged();
        mRecyclerViewImages.getAdapter().notifyItemChanged(adapterPosition);
        mCartCount.setText(String.valueOf(SelectedImagesManager.Instance().getmSelectedImages().size()));
    }

    @Override
    public void notifyAdaptersDatasetChanged() {
        mRecyclerViewImages.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_local:
                if (!item.isChecked()) {
                    loadLocalImages();
                }
                break;
            case R.id.action_facebook:
                if (!item.isChecked()) {
                    loadFacebook();
                }
                break;
            case R.id.action_instagram:
                loadInstagram();
                break;
            case R.id.action_dropbox:
                loadDropbox();
                break;
        }
        return true;
    }
}
