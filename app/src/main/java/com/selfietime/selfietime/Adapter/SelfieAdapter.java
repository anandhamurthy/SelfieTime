package com.selfietime.selfietime.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.pedromassango.doubleclick.DoubleClick;
import com.pedromassango.doubleclick.DoubleClickListener;
import com.selfietime.selfietime.CommentsActivity;
import com.selfietime.selfietime.Fragments.APIService;
import com.selfietime.selfietime.Fragments.ProfileFragment;
import com.selfietime.selfietime.LikesActivity;
import com.selfietime.selfietime.Model.Selfie;
import com.selfietime.selfietime.Model.User;
import com.selfietime.selfietime.Notification.Client;
import com.selfietime.selfietime.Notification.Data;
import com.selfietime.selfietime.Notification.MyResponse;
import com.selfietime.selfietime.Notification.Sender;
import com.selfietime.selfietime.Notification.Token;
import com.selfietime.selfietime.R;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class SelfieAdapter extends RecyclerView.Adapter<SelfieAdapter.ImageViewHolder> {

    APIService apiService;
    private Context mContext;
    private List<Selfie> mSelfies;
    private FirebaseAuth mAuth;
    private DatabaseReference mSelfieDatabase, mSavesDatabase, mLikesDatabase, mNotificationDatabase, mCommentsDatabase, mUsersDatabase;
    private FirebaseUser mFirebaseUser;
    private String mCurrentUserId;
    private FirebaseStorage mStorage;

    public SelfieAdapter(Context context, List<Selfie> selfies) {
        mContext = context;
        mSelfies = selfies;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.selfie_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {

        mAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mAuth.getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();
        mSelfieDatabase = FirebaseDatabase.getInstance().getReference().child("Selfies");
        mSelfieDatabase.keepSynced(true);
        mLikesDatabase = FirebaseDatabase.getInstance().getReference().child("Likes");
        mLikesDatabase.keepSynced(true);
        mSavesDatabase = FirebaseDatabase.getInstance().getReference().child("Saves");
        mSavesDatabase.keepSynced(true);
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        mNotificationDatabase.keepSynced(true);
        mCommentsDatabase = FirebaseDatabase.getInstance().getReference().child("Comments");
        mCommentsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        mStorage = FirebaseStorage.getInstance();

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);


        final Selfie selfie = mSelfies.get(position);

        Glide.with(mContext).load(selfie.getSelfie_image())
                .apply(new RequestOptions().placeholder(R.drawable.image_placeholder))
                .into(holder.Selfie_Image);

        if (selfie.getSelfie_description().equals("")) {
            holder.Selfie_Description.setVisibility(View.GONE);
        } else {
            holder.Selfie_Description.setVisibility(View.VISIBLE);
            holder.Selfie_Description.setText(selfie.getSelfie_description());
        }

        UserImformation(holder.Selfie_Profile_Image, holder.Selfie_User_Name, selfie.getUser_id());
        isLiked(selfie.getSelfie_id(), holder.Selfie_Likes);
        isSaved(selfie.getSelfie_id(), holder.Selfie_Save);
        nrLikes(holder.Selfie_Likes_Count, selfie.getSelfie_id());
        getCommetns(selfie.getSelfie_id(), holder.Selfie_Comments_Count);

        holder.Selfie_Likes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.Selfie_Likes.getTag().equals("like")) {
                    MediaPlayer mp = MediaPlayer.create(mContext, R.raw.like_sound);
                    mp.start();
                    mLikesDatabase.child(selfie.getSelfie_id()).child(mCurrentUserId).setValue(true);

                    if (!selfie.getUser_id().equals(mCurrentUserId)) {
                        mUsersDatabase.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                User user = dataSnapshot.getValue(User.class);
                                sendNotification(selfie.getUser_id(), "Liked your Selfie", user.getUser_name(), mCurrentUserId);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    addNotification(selfie.getUser_id(), selfie.getSelfie_id());
                } else {
                    mLikesDatabase.child(selfie.getSelfie_id()).child(mCurrentUserId).removeValue();
                }
            }
        });

        holder.Selfie_Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.Selfie_Save.getTag().equals("save")) {
                    mSavesDatabase.child(mCurrentUserId).child(selfie.getSelfie_id()).setValue(true);
                } else {
                    mSavesDatabase.child(mCurrentUserId).child(selfie.getSelfie_id()).removeValue();
                }
            }
        });

        holder.Selfie_Profile_Image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", selfie.getUser_id());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            }
        });

        holder.Selfie_User_Name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                editor.putString("profileid", selfie.getUser_id());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            }
        });

        holder.Selfie_Comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", selfie.getSelfie_id());
                intent.putExtra("publisherid", selfie.getUser_id());
                mContext.startActivity(intent);
            }
        });

        holder.Selfie_Comments_Count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", selfie.getSelfie_id());
                intent.putExtra("publisherid", selfie.getUser_id());
                mContext.startActivity(intent);
            }
        });

        holder.Selfie_Image.setOnClickListener(new DoubleClick(new DoubleClickListener() {
            @Override
            public void onSingleClick(View view) {

            }

            @Override
            public void onDoubleClick(View view) {
                holder.Selfie_Image_Liked.setVisibility(View.VISIBLE);
                holder.Selfie_Image_Liked.postDelayed(new Runnable() {
                    public void run() {
                        holder.Selfie_Image_Liked.setVisibility(View.GONE);
                    }
                }, 3000);
                if (holder.Selfie_Likes.getTag().equals("like")) {
                    MediaPlayer mp = MediaPlayer.create(mContext, R.raw.like_sound);
                    mp.start();
                    mLikesDatabase.child(selfie.getSelfie_id()).child(mCurrentUserId).setValue(true);
                    addNotification(selfie.getUser_id(), selfie.getSelfie_id());
                    if (!selfie.getUser_id().equals(mCurrentUserId)) {
                        mUsersDatabase.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                User user = dataSnapshot.getValue(User.class);
                                sendNotification(selfie.getUser_id(), "Liked your Selfie", user.getUser_name(), mCurrentUserId);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                } else {
                    mLikesDatabase.child(selfie.getSelfie_id()).child(mCurrentUserId).removeValue();
                }
            }
        }));

        holder.Selfie_Likes_Count.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, LikesActivity.class);
                intent.putExtra("id", selfie.getSelfie_id());
                intent.putExtra("title", "likes");
                mContext.startActivity(intent);
            }
        });

        holder.Selfie_More.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.edit:
                                editPost(selfie.getSelfie_id());
                                return true;
                            case R.id.delete:
                                final String id = selfie.getSelfie_id();

                                StorageReference photoRef = mStorage.getReferenceFromUrl(selfie.getSelfie_image());
                                photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        mSelfieDatabase.child(selfie.getSelfie_id()).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            deleteNotifications(id, mCurrentUserId);
                                                        }
                                                    }
                                                });
                                    }
                                });

                                return true;
                            case R.id.report:

                                if (!selfie.getUser_id().equals(mCurrentUserId)) {
                                    mUsersDatabase.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            User user = dataSnapshot.getValue(User.class);
                                            sendNotification(selfie.getUser_id(), "Reported on Selfie", user.getUser_name(), mCurrentUserId);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(selfie.getUser_id());

                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put("userid", mCurrentUserId);
                                hashMap.put("text", "Reported on Selfie");
                                hashMap.put("postid", selfie.getSelfie_id());
                                hashMap.put("ispost", true);

                                reference.push().setValue(hashMap);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                if (!selfie.getUser_id().equals(mCurrentUserId)) {
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }
                if (selfie.getUser_id().equals(mCurrentUserId)) {
                    popupMenu.getMenu().findItem(R.id.report).setVisible(false);
                }
                popupMenu.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSelfies.size();
    }

    private void sendNotification(final String Reciever, final String User_Name, final String Message, final String user_id) {

        final DatabaseReference token = FirebaseDatabase.getInstance().getReference().child("Tokens");
        Query query = token.orderByKey().equalTo(Reciever);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    Token token1 = snapshot.getValue(Token.class);
                    Data data = new Data(user_id, R.drawable.icon, User_Name, Message, Reciever);

                    Sender sender = new Sender(data, token1.getToken());
                    apiService.sendNotification(sender).enqueue(
                            new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                                    if (response.code() == 200) {
                                        if (response.body().sucess == 1) {
                                            Toast.makeText(mContext, "Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            }
                    );
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addNotification(String userid, String postid) {
        mNotificationDatabase.child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", mCurrentUserId);
        hashMap.put("text", "liked your post");
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);

        mNotificationDatabase.push().setValue(hashMap);
    }

    private void deleteNotifications(final String postid, String userid) {
        mNotificationDatabase.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.child("postid").getValue().equals(postid)) {
                        snapshot.getRef().removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void nrLikes(final TextView likes, String postId) {
        mLikesDatabase.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 1 || dataSnapshot.getChildrenCount() == 0) {
                    likes.setText(dataSnapshot.getChildrenCount() + " Like");
                } else {
                    likes.setText(dataSnapshot.getChildrenCount() + " Likes");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getCommetns(String postId, final TextView comments) {
        mCommentsDatabase.child(postId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    comments.setText(dataSnapshot.getChildrenCount() + " Comment");
                } else if (dataSnapshot.getChildrenCount() == 1) {
                    comments.setText("View " + dataSnapshot.getChildrenCount() + " Comment");
                } else {
                    comments.setText("View All " + dataSnapshot.getChildrenCount() + " Comments");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void UserImformation(final ImageView profile_image, final TextView user_name, final String userid) {
        mUsersDatabase.child(userid).addValueEventListener(new ValueEventListener() {
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

    private void isLiked(final String postid, final ImageView imageView) {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mLikesDatabase.child(postid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.like);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.dislike);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void isSaved(final String postid, final ImageView imageView) {

        mSavesDatabase.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.post_save);
                    imageView.setTag("saved");
                } else {
                    imageView.setImageResource(R.drawable.saved);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void editPost(final String postid) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("Edit");

        final EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        editText.setLayoutParams(lp);
        alertDialog.setView(editText);

        getText(postid, editText);

        alertDialog.setPositiveButton("Edit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("selfie_description", editText.getText().toString());
                        mSelfieDatabase.child(postid).updateChildren(hashMap);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        alertDialog.show();
    }

    private void getText(String postid, final EditText editText) {
        mSelfieDatabase.child(postid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                editText.setText(dataSnapshot.getValue(Selfie.class).getSelfie_description());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView Selfie_Profile_Image, Selfie_Image, Selfie_Likes, Selfie_Comments, Selfie_Save, Selfie_More, Selfie_Image_Liked;
        public TextView Selfie_User_Name, Selfie_Likes_Count, Selfie_Description, Selfie_Comments_Count;

        public ImageViewHolder(View itemView) {
            super(itemView);

            Selfie_Profile_Image = itemView.findViewById(R.id.selfie_profile_image);
            Selfie_Image = itemView.findViewById(R.id.selfie_image);
            Selfie_Likes = itemView.findViewById(R.id.selfie_likes);
            Selfie_Comments = itemView.findViewById(R.id.selfie_comments);
            Selfie_Save = itemView.findViewById(R.id.selfie_save);
            Selfie_More = itemView.findViewById(R.id.selfie_more);
            Selfie_User_Name = itemView.findViewById(R.id.selfie_user_name);
            Selfie_Likes_Count = itemView.findViewById(R.id.selfie_likes_count);
            Selfie_Description = itemView.findViewById(R.id.selfie_description);
            Selfie_Comments_Count = itemView.findViewById(R.id.selfie_comments_count);
            Selfie_Image_Liked = itemView.findViewById(R.id.selfie_liked);
        }
    }
}