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
import com.jakebarnby.imageuploader.sources.PinterestSource;
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
        S3Manager.Instance().setupAWSCredentials(getApplicationContext());

        mSources.put(Constants.SOURCE_LOCAL, new LocalSource(this, this));
        mSources.put(Constants.SOURCE_FACEBOOK, new FacebookSource(this, this));
        mSources.put((Constants.SOURCE_INSTAGRAM), new InstagramSource(this, this, Constants.INSTAGRAM_API_BASE_URL));
        mSources.put((Constants.SOURCE_DROPBOX), new DropboxSource(this, this, Constants.DROPBOX_API_BASE_URL));
        mSources.put((Constants.SOURCE_PINTEREST), new PinterestSource(this, this));

        setupViews();
        checkPermssions();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCartCount.setText(String.valueOf(SelectedImagesManager.Instance().getSelectedImages().size()));
    }

    /**
     * Get references to views
     */
    private void setupViews()
    {
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
        mCartCount = (TextView) findViewById(R.id.textview_cart_count);

        mRecyclerViewImages = (RecyclerView) findViewById(R.id.recyclerview_images);
        mRecyclerViewCart = (RecyclerView) findViewById(R.id.recyclerview_cart);

        GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, Constants.GRID_COLUMNS);
        mRecyclerViewImages.setLayoutManager(mGridLayoutManager);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mRecyclerViewCart.setLayoutManager(mLinearLayoutManager);
        mCartAdapter = new GridAdapter(SelectedImagesManager.Instance().getSelectedImages());
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

    /**
     * Check if the user has granted local storage permission
     */
    private void checkPermssions() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showPermissionRationaleDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_STORAGE);
            }
        } else {
            loadSource(mSources.get(Constants.SOURCE_LOCAL));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupViews();
                    loadSource(mSources.get(Constants.SOURCE_LOCAL));
                } else {
                    checkPermssions();
                }
                break;
            }
        }
    }

    /**
     * Shows a dialog explaining why local storage permission is necessary
     */
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

    /**
     * Loads the given source, initiating a login flow if necessary then retrieving all images
     * @param source       The source to load
     */
    private void loadSource(Source source) {
        source.load();
        setRecyclerAdapter(source.getAdapter());
    }

    /**
     * Set the adapter of the RecyclerView containing source images
     * @param adapter   The adapter to set
     */
    private void setRecyclerAdapter(GridAdapter adapter) {
        mRecyclerViewImages.swapAdapter(adapter, false);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mSources.get(Constants.SOURCE_FACEBOOK).onActivityResult(requestCode, resultCode, data);
        mSources.get(Constants.SOURCE_PINTEREST).onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void scrollCartToEnd() {
        mRecyclerViewCart.smoothScrollToPosition(mRecyclerViewCart.getAdapter().getItemCount());
    }

    @Override
    public void notifyAdapters(int adapterPosition) {
        mCartAdapter.notifyDataSetChanged();
        mRecyclerViewImages.getAdapter().notifyItemChanged(adapterPosition);
        mCartCount.setText(String.valueOf(SelectedImagesManager.Instance().getSelectedImages().size()));
    }

    @Override
    public void notifyAdaptersDatasetChanged() {
        mRecyclerViewImages.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        String key = null;

        switch (item.getItemId()) {
            case R.id.action_local:
                if (!item.isChecked()) {
                    key = Constants.SOURCE_LOCAL;
                }
                break;
            case R.id.action_facebook:
                if (!item.isChecked())
                    key = Constants.SOURCE_FACEBOOK;
                break;
            case R.id.action_instagram:
                if (!item.isChecked())
                    key = Constants.SOURCE_INSTAGRAM;
                break;
            case R.id.action_dropbox:
                if (!item.isChecked())
                    key = Constants.SOURCE_DROPBOX;
                break;
            case R.id.action_pinterest:
                if (!item.isChecked())
                    key = Constants.SOURCE_PINTEREST;
                break;
        }
        if (key != null) {
            loadSource(mSources.get(key));
        }
        return true;
    }
}
