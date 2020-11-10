package com.selfietime.selfietime;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

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

public class FollowingsActivity extends AppCompatActivity {

    UserAdapter mUserAdapter;
    List<User> userList;
    private String id;
    private String title;
    private List<String> idList;
    private RecyclerView mFollowingsList;
    private RelativeLayout mNoFollowings;
    private ImageView Followings_Back;
    private DatabaseReference mFollowingsDatabase, mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followings);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        Followings_Back = findViewById(R.id.followings_back);

        Followings_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mFollowingsDatabase = FirebaseDatabase.getInstance().getReference().child("Follow").child(id).child("following");
        mFollowingsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        mNoFollowings = findViewById(R.id.no_followings);
        mFollowingsList = findViewById(R.id.followings_list);
        mFollowingsList.setHasFixedSize(true);
        mFollowingsList.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        mUserAdapter = new UserAdapter(this, userList, false);
        mFollowingsList.setAdapter(mUserAdapter);

        idList = new ArrayList<>();

        getFollowing();

    }

    private void getFollowing() {
        mFollowingsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mFollowingsList.setVisibility(View.VISIBLE);
                    mNoFollowings.setVisibility(View.GONE);
                    idList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        idList.add(snapshot.getKey());
                    }
                    showUsers();
                } else {
                    mNoFollowings.setVisibility(View.VISIBLE);
                    mFollowingsList.setVisibility(View.GONE);
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
