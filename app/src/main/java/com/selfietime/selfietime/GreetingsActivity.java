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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.selfietime.selfietime.Adapter.WishAdapter;
import com.selfietime.selfietime.Model.Wish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GreetingsActivity extends AppCompatActivity {

    private RecyclerView Greetings_List;

    private RelativeLayout No_Greetings;

    private DatabaseReference mWishingDatabase;

    private ImageView Greetings_Back;
    private FirebaseAuth mAuth;

    private String mCurrentUserId;
    private String title;

    private WishAdapter wishAdapter;
    private List<Wish> wishList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_greetings);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        Intent intent = getIntent();
        title = intent.getStringExtra("title");

        Greetings_Back = findViewById(R.id.greetings_back);

        Greetings_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        mWishingDatabase = FirebaseDatabase.getInstance().getReference("Wishes").child(mCurrentUserId);
        mWishingDatabase.keepSynced(true);

        No_Greetings = findViewById(R.id.no_greetings);
        Greetings_List = findViewById(R.id.greetings_list);
        Greetings_List.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(GreetingsActivity.this);
        Greetings_List.setLayoutManager(mLayoutManager);
        wishList = new ArrayList<>();
        wishAdapter = new WishAdapter(GreetingsActivity.this, wishList);
        Greetings_List.setAdapter(wishAdapter);

        readWishes();

    }

    private void readWishes() {

        mWishingDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Greetings_List.setVisibility(View.VISIBLE);
                    No_Greetings.setVisibility(View.GONE);
                    wishList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Wish wish = snapshot.getValue(Wish.class);
                        wishList.add(wish);
                    }

                    Collections.reverse(wishList);
                    wishAdapter.notifyDataSetChanged();
                } else {
                    No_Greetings.setVisibility(View.VISIBLE);
                    Greetings_List.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}

