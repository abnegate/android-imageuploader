package com.jakebarnby.imageuploader.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.jakebarnby.imageuploader.models.Image;
import com.jakebarnby.imageuploader.R;
import com.jakebarnby.imageuploader.managers.SelectedImagesManager;

import java.util.ArrayList;

/**
 * Created by Jake on 12/15/2016.
 */
public class GridAdapter extends RecyclerView.Adapter<GridAdapter.PhotoHolder> {

    private AdapterInterface mAdapterListener;
    private ArrayList<Image> mImages;

    public GridAdapter(ArrayList<Image> images){
        this.mImages = images;
    }

    public GridAdapter(ArrayList<Image> mImages, AdapterInterface adapterInterface) {
        this.mImages = mImages;
        this.mAdapterListener = adapterInterface;
    }

    @Override
    public GridAdapter.PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflatedView;

        if (mAdapterListener != null) {
            inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item_cell, parent, false);
        } else {
            inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_cart_cell, parent, false);
        }
        return new PhotoHolder(inflatedView, mAdapterListener);
    }

    @Override
    public void onBindViewHolder(GridAdapter.PhotoHolder holder, int position) {
        Image itemImage = mImages.get(position);
        holder.bindImage(itemImage);
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    @Override
    public long getItemId(int position) {
        return mImages.get(position).hashCode();
    }

    static class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private AdapterInterface mAdapterListener;
        private ImageView mItemImage;
        private ImageView mItemCheckmark;
        private Image mImage;

        private PhotoHolder(View v, AdapterInterface adapterListener) {
            super(v);

            mItemImage = (ImageView) v.findViewById(R.id.image_cell);
            mItemCheckmark = (ImageView) v.findViewById(R.id.image_checkmark);

            mAdapterListener = adapterListener;
            v.setOnClickListener(this);
        }

        private void bindImage(final Image image) {
            mImage = image;
            setImageSelected(mImage, mImage.isSelected());
            Glide
                    .with(mItemImage.getContext())
                    .load(image.getUri().toString())
                    .placeholder(R.mipmap.ic_launcher)
                    .centerCrop()
                    .override(256, 256)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(mItemImage);
        }

        @Override
        public void onClick(View v) {
            if (mAdapterListener != null) {
                if (mImage.isSelected()) {
                    setImageSelected(mImage, false);
                } else {
                    setImageSelected(mImage, true);
                }
                mAdapterListener.notifyAdapters(getAdapterPosition());
                mAdapterListener.scrollCartToEnd();
            } else {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(mImage.getUri(), "image/*");
                v.getContext().startActivity(intent);
            }
        }

        private void setImageSelected(Image image, boolean selected) {
            if (mAdapterListener != null) {
                boolean alreadyAdded = SelectedImagesManager.Instance().getmSelectedImages().contains(image);

                if (selected) {
                    if (!alreadyAdded) {
                        SelectedImagesManager.Instance().addImage(mImage);
                    }
                    mItemCheckmark.setVisibility(View.VISIBLE);
                } else {
                    if (alreadyAdded) {
                        SelectedImagesManager.Instance().removeImage(mImage);
                    }
                    mItemCheckmark.setVisibility(View.INVISIBLE);
                }
                image.setSelected(selected);
            }
        }
    }
}
