package com.selfietime.selfietime;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.selfietime.selfietime.Adapter.UserAdapter;
import com.selfietime.selfietime.Model.User;

import java.util.ArrayList;
import java.util.List;

public class LikesActivity extends AppCompatActivity {

    UserAdapter mUserAdapter;
    List<User> userList;
    private String id;
    private String title;
    private List<String> idList;
    private RecyclerView mLikesList;
    private RelativeLayout mNoLikes;
    private DatabaseReference mLikesDatabase, mUsersDatabase;
    private ImageView Like_Back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        Like_Back = findViewById(R.id.likes_back);
        Like_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        mLikesDatabase = FirebaseDatabase.getInstance().getReference("Likes").child(id);
        mLikesDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mNoLikes = findViewById(R.id.no_likes);
        mLikesList = findViewById(R.id.likes_list);
        mLikesList.setHasFixedSize(true);
        mLikesList.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        mUserAdapter = new UserAdapter(this, userList, false);
        mLikesList.setAdapter(mUserAdapter);

        idList = new ArrayList<>();

        getLikes();
    }

    private void getLikes() {
        mLikesDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mLikesList.setVisibility(View.VISIBLE);
                    mNoLikes.setVisibility(View.GONE);
                    idList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        idList.add(snapshot.getKey());
                    }
                    showUsers();
                } else {
                    mNoLikes.setVisibility(View.VISIBLE);
                    mLikesList.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showUsers() {

        mUsersDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    for (String id : idList) {
                        if (user.getUser_id().equals(id)) {
                            userList.add(user);
                        }
                    }
                }
                mUserAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
