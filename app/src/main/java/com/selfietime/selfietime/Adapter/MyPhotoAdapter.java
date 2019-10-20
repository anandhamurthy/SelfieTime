package com.selfietime.selfietime.Adapter;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.selfietime.selfietime.Fragments.PostDetailFragment;
import com.selfietime.selfietime.Model.Selfie;
import com.selfietime.selfietime.R;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class MyPhotoAdapter extends RecyclerView.Adapter<MyPhotoAdapter.ImageViewHolder> {

    private Context mContext;
    private List<Selfie> mSelfies;

    public MyPhotoAdapter(Context context, List<Selfie> selfies) {
        mContext = context;
        mSelfies = selfies;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.photos_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {

        final Selfie selfie = mSelfies.get(position);

        Glide.with(mContext).load(selfie.getSelfie_image()).into(holder.post_image);

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("postid", selfie.getSelfie_id());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new PostDetailFragment()).commit();
            }
        });

    }

    @Override
    public int getItemCount() {
        return mSelfies.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView post_image;


        public ImageViewHolder(View itemView) {
            super(itemView);

            post_image = itemView.findViewById(R.id.post_image);

        }
    }
}