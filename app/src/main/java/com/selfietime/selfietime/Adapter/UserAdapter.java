package com.selfietime.selfietime.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.selfietime.selfietime.AddStoryActivity;
import com.selfietime.selfietime.Fragments.APIService;
import com.selfietime.selfietime.Fragments.ProfileFragment;
import com.selfietime.selfietime.MainActivity;
import com.selfietime.selfietime.Model.User;
import com.selfietime.selfietime.NewSelfieActivity;
import com.selfietime.selfietime.Notification.Client;
import com.selfietime.selfietime.Notification.Data;
import com.selfietime.selfietime.Notification.MyResponse;
import com.selfietime.selfietime.Notification.Sender;
import com.selfietime.selfietime.Notification.Token;
import com.selfietime.selfietime.R;
import com.selfietime.selfietime.WishActivity;

import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ImageViewHolder> {

    APIService apiService;
    private Context mContext;
    private List<User> mUsers;
    private boolean isFragment;
    private boolean isWish;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String mCurrentUserId = mAuth.getCurrentUser().getUid();


    public UserAdapter(Context context, List<User> users, boolean isFragment, boolean isWish) {
        mContext = context;
        mUsers = users;
        this.isFragment = isFragment;
        this.isWish = isWish;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ImageViewHolder holder, final int position) {

        final User user = mUsers.get(position);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        holder.User_Follow.setVisibility(View.VISIBLE);
        isFollowing(user.getUser_id(), holder.User_Follow);

        holder.User_Name.setText(user.getUser_name());
        Glide.with(mContext).load(user.getProfile_image()).into(holder.User_Profile_Image);

        if (user.getUser_id().equals(mCurrentUserId)) {
            holder.User_Follow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFragment) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getUser_id());
                    editor.apply();

                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                            new ProfileFragment()).commit();
                } else if (isWish) {
                    CharSequence options[] = new CharSequence[]{"Add Wish"};
                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                    builder.setTitle("Options");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int position) {

                            if (position == 0) {
                                Intent SelfieIntent = new Intent(mContext, WishActivity.class);
                                SelfieIntent.putExtra("user_id", user.getUser_id());
                                mContext.startActivity(SelfieIntent);
                            }

                        }
                    });

                    builder.show();
                } else {
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra("publisherid", user.getUser_id());
                    mContext.startActivity(intent);
                }


            }
        });

        holder.User_Follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.User_Follow.getText().toString().equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(mCurrentUserId)
                            .child("following").child(user.getUser_id()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUser_id())
                            .child("followers").child(mCurrentUserId).setValue(true);
                    if (!user.getUser_id().equals(mCurrentUserId)) {
                        final DatabaseReference users = FirebaseDatabase.getInstance().getReference().child("Users");
                        users.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                User user1 = dataSnapshot.getValue(User.class);
                                sendNotification(user.getUser_id(), "is Following You", user1.getUser_name(), mCurrentUserId);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                    addNotification(user.getUser_id());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(mCurrentUserId)
                            .child("following").child(user.getUser_id()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUser_id())
                            .child("followers").child(mCurrentUserId).removeValue();
                }
            }

        });
    }

    private void addNotification(String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", mCurrentUserId);
        hashMap.put("text", "started following you");
        hashMap.put("postid", "");
        hashMap.put("ispost", false);

        reference.push().setValue(hashMap);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
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

    private void isFollowing(final String userid, final Button button) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(mCurrentUserId).child("following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userid).exists()) {
                    button.setText("Following");
                } else {
                    button.setText("Follow");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        public TextView User_Name;
        public CircleImageView User_Profile_Image;
        public Button User_Follow;

        public ImageViewHolder(View itemView) {
            super(itemView);

            User_Name = itemView.findViewById(R.id.user_name);
            User_Profile_Image = itemView.findViewById(R.id.user_profile_image);
            User_Follow = itemView.findViewById(R.id.user_follow_button);
        }
    }
}