package com.selfietime.selfietime.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.selfietime.selfietime.Fragments.PostDetailFragment;
import com.selfietime.selfietime.Fragments.ProfileFragment;
import com.selfietime.selfietime.Model.Notification;
import com.selfietime.selfietime.Model.Selfie;
import com.selfietime.selfietime.Model.User;
import com.selfietime.selfietime.R;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ImageViewHolder> {

    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;

    private DatabaseReference mSelfieDatabase, mUsersDatabase;

    private Context mContext;
    private List<Notification> mNotification;

    public NotificationAdapter(Context context, List<Notification> notification) {
        mContext = context;
        mNotification = notification;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();

        mSelfieDatabase = FirebaseDatabase.getInstance().getReference().child("Selfies");
        mSelfieDatabase.keepSynced(true);

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        final Notification notification = mNotification.get(position);

        holder.Notification_Comment.setText(notification.getText());

        UserInformation(holder.Notification_Profile_Image, holder.Notification_User_Name, notification.getUserid());

        if (notification.isIspost()) {
            holder.Notification_Profile_Image.setVisibility(View.VISIBLE);
            getPostImage(holder.Notification_Profile_Image, notification.getPostid());
        } else {
            holder.Notification_Profile_Image.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notification.isIspost()) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                    editor.putString("postid", notification.getPostid());
                    editor.apply();

                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new PostDetailFragment()).commit();
                } else {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                    editor.putString("profileid", notification.getUserid());
                    editor.apply();

                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return mNotification.size();
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

    private void getPostImage(final ImageView post_image, String postid) {
        mSelfieDatabase.child(postid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Selfie selfie = dataSnapshot.getValue(Selfie.class);
                Glide.with(mContext).load(selfie.getSelfie_image()).into(post_image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView Notification_Profile_Image, Notification_Selfie_Image;
        public TextView Notification_User_Name, Notification_Comment;

        public ImageViewHolder(View itemView) {
            super(itemView);

            Notification_Profile_Image = itemView.findViewById(R.id.single_notification_profile_image);
            Notification_Selfie_Image = itemView.findViewById(R.id.single_notification_selfie_image);
            Notification_User_Name = itemView.findViewById(R.id.single_notification_user_name);
            Notification_Comment = itemView.findViewById(R.id.single_notification_comment);
        }
    }
}