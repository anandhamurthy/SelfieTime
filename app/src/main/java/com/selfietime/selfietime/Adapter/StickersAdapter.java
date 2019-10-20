package com.selfietime.selfietime.Adapter;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.selfietime.selfietime.Model.Stickers;
import com.selfietime.selfietime.R;
import com.selfietime.selfietime.StickerBSFragment;

import java.util.List;

import static android.graphics.BitmapFactory.decodeByteArray;

public class StickersAdapter extends RecyclerView.Adapter<StickersAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Stickers> mStickers;
    private StickerBSFragment.StickerListener mStickerListener;
    private Bitmap bit = null;

    public StickersAdapter(Context context, List<Stickers> stickersList, StickerBSFragment.StickerListener stickerListener) {
        mContext = context;
        mStickers = stickersList;
        mStickerListener = stickerListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.row_sticker, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int i) {
        final Stickers stickers = mStickers.get(i);

        Glide.with(mContext)
                .asBitmap()
                .load(stickers.getSticker_image())
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        holder.post_image.setImageBitmap(resource);
                        bit = resource;
                    }
                });
//        Glide.with(mContext).load(stickers.getSticker_image()).into(holder.post_image);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mStickerListener != null) {
                    mStickerListener.onStickerClick(bit);
                }

                //dismiss();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStickers.size();
    }


    public class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView post_image;

        public ImageViewHolder(final View itemView) {
            super(itemView);
            post_image = itemView.findViewById(R.id.imgSticker);


        }
    }
}