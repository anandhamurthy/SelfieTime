package com.selfietime.selfietime;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CompetitionActivity extends AppCompatActivity {

    private DatabaseReference mCompetitionDatabase;

    private String title;

    private ImageView Competitions_Back, Competitions_Image;
    private RelativeLayout No_Competitions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition);

        mCompetitionDatabase = FirebaseDatabase.getInstance().getReference().child("Competition");
        mCompetitionDatabase.keepSynced(true);

        Intent intent = getIntent();
        title = intent.getStringExtra("title");

        Competitions_Back = findViewById(R.id.competition_back);
        No_Competitions = findViewById(R.id.no_competitions);
        Competitions_Image = findViewById(R.id.competition_image);

        Competitions_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mCompetitionDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    Competitions_Image.setVisibility(View.VISIBLE);
                    No_Competitions.setVisibility(View.GONE);

                    Glide.with(getApplicationContext()).load(dataSnapshot.child("image").getValue().toString()).into(Competitions_Image);
                } else {
                    No_Competitions.setVisibility(View.VISIBLE);
                    Competitions_Image.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}


