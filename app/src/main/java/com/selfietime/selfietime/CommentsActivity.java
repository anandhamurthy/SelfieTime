package com.selfietime.selfietime;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.selfietime.selfietime.Adapter.CommentAdapter;
import com.selfietime.selfietime.Fragments.APIService;
import com.selfietime.selfietime.Model.Comment;
import com.selfietime.selfietime.Model.User;
import com.selfietime.selfietime.Notification.Client;
import com.selfietime.selfietime.Notification.Data;
import com.selfietime.selfietime.Notification.MyResponse;
import com.selfietime.selfietime.Notification.Sender;
import com.selfietime.selfietime.Notification.Token;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsActivity extends AppCompatActivity {

    APIService apiService;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private EditText Comments_Edit_Text;
    private ImageView Comments_Profile_Image, Comments_Back;
    private TextView Comments_Button;
    private String postid;
    private String publisherid;
    private String mCurrentUserId;
    private FirebaseUser mFirebaseUser;
    private RecyclerView mCommentsList;
    private RelativeLayout mNoComments;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Comments_Back = findViewById(R.id.comments_back);
        mNoComments = findViewById(R.id.no_comments);

        Comments_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        postid = intent.getStringExtra("postid");
        publisherid = intent.getStringExtra("publisherid");

        mCommentsList = findViewById(R.id.comments_list);
        mCommentsList.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mCommentsList.setLayoutManager(mLayoutManager);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList, postid);
        mCommentsList.setAdapter(commentAdapter);

        Comments_Button = findViewById(R.id.comments_button);
        Comments_Edit_Text = findViewById(R.id.comments_edit_text);
        Comments_Profile_Image = findViewById(R.id.comments_profile_image);

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = mFirebaseUser.getUid();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        Comments_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Comments_Edit_Text.getText().toString().equals("")) {
                    Toast.makeText(CommentsActivity.this, "You can't comment empty", Toast.LENGTH_SHORT).show();
                } else {
                    addComment();
                }
            }
        });

        getImage();
        readComments();

    }

    private void addComment() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        String commentid = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("comment", Comments_Edit_Text.getText().toString());
        hashMap.put("publisher", mCurrentUserId);
        hashMap.put("commentid", commentid);

        reference.child(commentid).setValue(hashMap);
        if (!publisherid.equals(mCurrentUserId)) {
            mUsersDatabase.child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    User user = dataSnapshot.getValue(User.class);
                    sendNotification(publisherid, "Commented on your Selfie", user.getUser_name(), mCurrentUserId);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        addNotification();
        Comments_Edit_Text.setText("");

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
                                            Toast.makeText(CommentsActivity.this, "Failed", Toast.LENGTH_SHORT).show();
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

    private void addNotification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherid);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userid", mCurrentUserId);
        hashMap.put("text", "commented: " + Comments_Edit_Text.getText().toString());
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);

        reference.push().setValue(hashMap);
    }

    private void getImage() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(mCurrentUserId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getApplicationContext()).load(user.getProfile_image()).into(Comments_Profile_Image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void readComments() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mCommentsList.setVisibility(View.VISIBLE);
                    mNoComments.setVisibility(View.GONE);
                    commentList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Comment comment = snapshot.getValue(Comment.class);
                        commentList.add(comment);
                    }

                    commentAdapter.notifyDataSetChanged();
                } else {
                    mNoComments.setVisibility(View.VISIBLE);
                    mCommentsList.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
