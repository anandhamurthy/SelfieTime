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

public class ViewsActivity extends AppCompatActivity {

    UserAdapter mUserAdapter;
    List<User> userList;
    private String id;
    private String title;
    private List<String> idList;
    private RecyclerView Views_List;
    private RelativeLayout No_Views;
    private ImageView Views_Back;
    private DatabaseReference mViewsDatabase, mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_views);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        Views_Back = findViewById(R.id.views_back);

        Views_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mViewsDatabase = FirebaseDatabase.getInstance().getReference("Story")
                .child(id).child(getIntent().getStringExtra("storyid")).child("views");
        mViewsDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);
        No_Views = findViewById(R.id.no_views);
        Views_List = findViewById(R.id.views_list);
        Views_List.setHasFixedSize(true);
        Views_List.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        mUserAdapter = new UserAdapter(this, userList, false);
        Views_List.setAdapter(mUserAdapter);

        idList = new ArrayList<>();

        getViews();

    }

    private void getViews() {

        mViewsDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Views_List.setVisibility(View.VISIBLE);
                    No_Views.setVisibility(View.GONE);
                    idList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        idList.add(snapshot.getKey());
                    }
                    showUsers();
                } else {
                    No_Views.setVisibility(View.VISIBLE);
                    Views_List.setVisibility(View.GONE);
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

