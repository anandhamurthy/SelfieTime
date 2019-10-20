package com.selfietime.selfietime.Adapter;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.selfietime.selfietime.GreetingActivity;
import com.selfietime.selfietime.Model.Wish;
import com.selfietime.selfietime.Model.User;
import com.selfietime.selfietime.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WishAdapter extends RecyclerView.Adapter<WishAdapter.ImageViewHolder> {

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;

    private FirebaseStorage mStorage;

    private DatabaseReference mWishesDatabase, mUsersDatabase;

    private Context mContext;
    private List<Wish> mWish;

    public WishAdapter(Context context, List<Wish> wishes) {
        mContext = context;
        mWish = wishes;
    }

    @NonNull
    @Override
    public WishAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.greeting_item, parent, false);
        return new WishAdapter.ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final WishAdapter.ImageViewHolder holder, final int position) {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        mStorage = FirebaseStorage.getInstance();

        mWishesDatabase = FirebaseDatabase.getInstance().getReference().child("Wishes");
        mWishesDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        final Wish wish = mWish.get(position);

        UserInformation(holder.Wish_Profile_Image, holder.Wish_User_Name, wish.getFrom());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String today_date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                SimpleDateFormat dateFormat = new SimpleDateFormat(("dd-MM-yyyy"));
                Calendar cal = Calendar.getInstance();
                try {
                    cal.setTime(dateFormat.parse(today_date));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cal.add(Calendar.DATE, 1);
                String convertedDate = dateFormat.format(cal.getTime());
                if (wish.getDate().equals(convertedDate)) {

                    StorageReference photoRef = mStorage.getReferenceFromUrl(wish.getWish_image());
                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {


                            mWishesDatabase.child(wish.getTo()).child(wish.getWish_id()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                }
                            });
                        }
                    });

                } else if (wish.getDate().equals(today_date)) {
                    Intent wishIntent = new Intent(mContext, GreetingActivity.class);
                    wishIntent.putExtra("from_name", wish.getName());
                    wishIntent.putExtra("name", wish.getDate());
                    wishIntent.putExtra("greetings", wish.getGreeting());
                    wishIntent.putExtra("image", wish.getWish_image());
                    mContext.startActivity(wishIntent);
                }


            }
        });


    }

    @Override
    public int getItemCount() {
        return mWish.size();
    }

    private void UserInformation(final ImageView profile_image, final TextView user_name, String user_id) {

        mUsersDatabase.child(user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getProfile_image()).into(profile_image);
                user_name.setText(user.getUser_name());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView Wish_Profile_Image;
        public TextView Wish_User_Name;

        public ImageViewHolder(View itemView) {
            super(itemView);

            Wish_Profile_Image = itemView.findViewById(R.id.single_wish_profile_image);
            Wish_User_Name = itemView.findViewById(R.id.single_wish_user_name);
        }
    }
}